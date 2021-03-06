package com.github.theholywaffle.teamspeak3;

import com.github.theholywaffle.teamspeak3.api.Callback;
import com.github.theholywaffle.teamspeak3.api.exception.TS3ConnectionFailedException;
import com.github.theholywaffle.teamspeak3.api.reconnect.ConnectionHandler;
import com.github.theholywaffle.teamspeak3.commands.CQuit;
import com.github.theholywaffle.teamspeak3.commands.Command;
import com.github.theholywaffle.teamspeak3.log.LogHandler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TS3Query {

	public static class FloodRate {

		public static final FloodRate DEFAULT = new FloodRate(350);
		public static final FloodRate UNLIMITED = new FloodRate(0);

		public static final FloodRate custom(int milliseconds) {
			if (milliseconds < 0) throw new IllegalArgumentException("Timeout must be positive");
			return new FloodRate(milliseconds);
		}

		private final int ms;

		private FloodRate(int ms) {
			this.ms = ms;
		}

		public int getMs() {
			return ms;
		}
	}

	public static final Logger log = Logger.getLogger(TS3Query.class.getName());

	private final ExecutorService userThreadPool = Executors.newCachedThreadPool();
	private final EventManager eventManager = new EventManager();
	private final FileTransferHelper fileTransferHelper;
	private final TS3Config config;
	private final ConnectionHandler connectionHandler;

	private QueryIO io;
	private TS3Api api;
	private TS3ApiAsync asyncApi;

	/**
	 * Creates a TS3Query that connects to a TS3 server at
	 * {@code localhost:10011} using default settings.
	 */
	public TS3Query() {
		this(new TS3Config());
	}

	/**
	 * Creates a customized TS3Query that connects to a server
	 * specified by {@code config}.
	 *
	 * @param config
	 * 		configuration for this TS3Query
	 */
	public TS3Query(TS3Config config) {
		log.setUseParentHandlers(false);
		log.addHandler(new LogHandler(config.getDebugToFile()));
		log.setLevel(config.getDebugLevel());
		this.config = config;
		this.fileTransferHelper = new FileTransferHelper(config.getHost());
		this.connectionHandler = config.getReconnectStrategy().create(config.getConnectionHandler());
	}

	// PUBLIC

	public TS3Query connect() {
		QueryIO oldIO = io;
		if (oldIO != null) {
			oldIO.disconnect();
		}

		try {
			io = new QueryIO(this, config);
		} catch (TS3ConnectionFailedException conFailed) {
			fireDisconnect();
			throw conFailed;
		}

		try {
			connectionHandler.onConnect(this);
		} catch (Throwable t) {
			log.log(Level.SEVERE, "ConnectionHandler threw exception in connect handler", t);
		}
		io.continueFrom(oldIO);

		return this;
	}

	/**
	 * Removes and closes all used resources to the teamspeak server.
	 */
	public void exit() {
		// Send a quit command synchronously
		// This will guarantee that all previously sent commands have been processed
		doCommand(new CQuit());

		io.disconnect();
		userThreadPool.shutdown();
		for (final Handler lh : log.getHandlers()) {
			log.removeHandler(lh);
		}
	}

	public TS3Api getApi() {
		if (api == null) {
			api = new TS3Api(this);
		}
		return api;
	}

	public TS3ApiAsync getAsyncApi() {
		if (asyncApi == null) {
			asyncApi = new TS3ApiAsync(this);
		}
		return asyncApi;
	}

	// INTERNAL

	boolean doCommand(Command c) {
		final long end = System.currentTimeMillis() + config.getCommandTimeout();
		final Object signal = new Object();
		c.setCallback(new Callback() {
			@Override
			public void handle() {
				synchronized (signal) {
					signal.notifyAll();
				}
			}
		});

		io.enqueueCommand(c);

		boolean interrupted = false;
		while (!c.isAnswered() && System.currentTimeMillis() < end) {
			try {
				synchronized (signal) {
					signal.wait(end - System.currentTimeMillis());
				}
			} catch (final InterruptedException e) {
				interrupted = true;
			}
		}
		if (interrupted) {
			// Restore the interrupt
			Thread.currentThread().interrupt();
		}

		if (!c.isAnswered()) {
			log.severe("Command " + c.getName() + " was not answered in time.");
			return false;
		}

		return c.getError().isSuccessful();
	}

	void doCommandAsync(Command c, Callback callback) {
		if (callback != null) c.setCallback(callback);
		io.enqueueCommand(c);
	}

	void submitUserTask(Runnable task) {
		userThreadPool.submit(task);
	}

	EventManager getEventManager() {
		return eventManager;
	}

	FileTransferHelper getFileTransferHelper() {
		return fileTransferHelper;
	}

	void fireDisconnect() {
		userThreadPool.submit(new Runnable() {
			@Override
			public void run() {
				try {
					connectionHandler.onDisconnect(TS3Query.this);
				} catch (Throwable t) {
					log.log(Level.SEVERE, "ConnectionHandler threw exception in disconnect handler", t);
				}
			}
		});
	}
}
