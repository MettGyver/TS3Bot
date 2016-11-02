package com.github.theholywaffle.teamspeak3;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;

import com.github.theholywaffle.teamspeak3.api.TextMessageTargetMode;
import com.github.theholywaffle.teamspeak3.api.event.ChannelCreateEvent;
import com.github.theholywaffle.teamspeak3.api.event.ChannelDeletedEvent;
import com.github.theholywaffle.teamspeak3.api.event.ChannelDescriptionEditedEvent;
import com.github.theholywaffle.teamspeak3.api.event.ChannelEditedEvent;
import com.github.theholywaffle.teamspeak3.api.event.ChannelMovedEvent;
import com.github.theholywaffle.teamspeak3.api.event.ChannelPasswordChangedEvent;
import com.github.theholywaffle.teamspeak3.api.event.ClientJoinEvent;
import com.github.theholywaffle.teamspeak3.api.event.ClientLeaveEvent;
import com.github.theholywaffle.teamspeak3.api.event.ClientMovedEvent;
import com.github.theholywaffle.teamspeak3.api.event.PrivilegeKeyUsedEvent;
import com.github.theholywaffle.teamspeak3.api.event.ServerEditedEvent;
import com.github.theholywaffle.teamspeak3.api.event.TS3Listener;
import com.github.theholywaffle.teamspeak3.api.event.TextMessageEvent;

/**
 * @author Julian Ziesche
 *
 */

public class TS3Bot {

	public static void main(String[] args) {

		final TS3Config config = new TS3Config();
		config.setHost("ip");
		config.setDebugLevel(Level.ALL);

		final TS3Query query = new TS3Query(config);
		query.connect();

		final TS3Api api = query.getApi();
		api.login("login", "password");
		api.selectVirtualServerById(1);
		api.setNickname("V-Lan.de Bot");
		System.out.println(api.getChannels());

		// api.sendServerMessage("Hallo, ich bims V-Lan.de Bot!");

		final int clientId = api.whoAmI().getId();

		api.registerAllEvents();
		api.addTS3Listeners(

				new TS3Listener() {

					TS3MM CS = new TS3MM("cs");
					TS3MM OW = new TS3MM("ow");
					TS3MM DB = new TS3MM("db");

					public void onClientJoin(ClientJoinEvent e) {
						api.sendPrivateMessage(e.getClientId(), "Willkommen " + e.getClientNickname());
					}

					@Override
					public void onTextMessage(TextMessageEvent e) {

						if (e.getTargetMode() == TextMessageTargetMode.SERVER
								|| e.getTargetMode() == TextMessageTargetMode.CLIENT && e.getInvokerId() != clientId) {
							String message = e.getMessage().toLowerCase();

							switch (message) {
							case "!bot chat":
								api.sendPrivateMessage(e.getInvokerId(),
										"Hey! Hier ist dein persönliches Chatfenster um Befehle auszuführen.");
								break;
							case "!mm del":
								CS.deleteList();
								api.sendPrivateMessage(e.getInvokerId(), "MM Liste gelöscht");
								break;
							case "!info":
								api.sendPrivateMessage(e.getInvokerId(),
										"Alle infos über unseren Teamspeak3-Bot findest du auf unserer Homepage 'www.V-Lan.de/ts3bot'");
								break;
							case "!zeit":
								Calendar cal = Calendar.getInstance();
								SimpleDateFormat sdf = new SimpleDateFormat("HH: mm: ss");
								api.sendPrivateMessage(e.getInvokerId(), sdf.format(cal.getTime()));
								break;
							default:
								break;
							}
							// Überartbeitunswürdige Methode
							if (message.startsWith("!join")) {
								String[] arr = message.split(" ");
								addPlayerToList(e, arr[1]);
							} else if (message.startsWith("!leave")) {
								String[] arr = message.split(" ");
								deletePlayerFromList(e, arr[1]);
							} else if (message.startsWith("!show")) {
								String[] arr = message.split(" ");
								showQueues(arr[1]);
							}
						}
					}

					@Override
					public void onClientLeave(ClientLeaveEvent e) {
						// ToDo
						// eins.removePlayer(e.getInvokerName(),
						// e.getClientId());
					}

					@Override
					public void onServerEdit(ServerEditedEvent e) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onChannelEdit(ChannelEditedEvent e) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onChannelDescriptionChanged(ChannelDescriptionEditedEvent e) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onClientMoved(ClientMovedEvent e) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onChannelCreate(ChannelCreateEvent e) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onChannelDeleted(ChannelDeletedEvent e) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onChannelMoved(ChannelMovedEvent e) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onChannelPasswordChanged(ChannelPasswordChangedEvent e) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onPrivilegeKeyUsed(PrivilegeKeyUsedEvent e) {
						// TODO Auto-generated method stub

					}

					public void checkIfCueFullAndMove(TS3MM a) {
						if (a.getNumberOfPlayers() == a.getSize()) {
							for (int j = 0; j <= a.getSize(); j++) {
								api.moveClient(a.id.get(j), a.getDestChannel());
							}
							a.deleteList();
						}
					}

					public void addPlayerToList(TextMessageEvent e, String s) {
						// auslagern
						switch (s) {
						case "cs":
							checkAndAdd(e, CS);
							break;
						case "ow":
							checkAndAdd(e, OW);
							break;
						case "db":
							checkAndAdd(e, DB);
						default:
							break;
						}
					}

					public void deletePlayerFromList(TextMessageEvent e, String s) {
						// auslagern
						switch (s) {
						case "cs":
							deletePlayerFromList(e, CS);
							break;
						case "ow":
							deletePlayerFromList(e, OW);
							break;
						case "db":
							deletePlayerFromList(e, DB);
						default:
							break;
						}
					}

					public void showQueues(String s) {
						switch (s) {
						case "cs":
							showSingleQueue(CS);
							break;
						case "ow":
							showSingleQueue(OW);
							break;
						case "db":
							showSingleQueue(DB);
						case "all":
							showAllQueues();
							break;
						default:
							break;
						}
					}

					/**
					 * @param e
					 *            TextEvent
					 * @param a
					 *            Ensprechende Liste Guckt, ob spieler schon in
					 *            der Liste -> True (schon in der Liste); False
					 *            -> Spieler wird hinzugefügt.
					 */
					public void checkAndAdd(TextMessageEvent e, TS3MM a) {
						if (a.check(e.getInvokerName()) == true) {
							api.sendPrivateMessage(e.getInvokerId(), e.getInvokerName() + " ist schon in der Liste!");
						} else {
							a.addPlayer(e.getInvokerName(), e.getInvokerId());
							api.sendPrivateMessage(e.getInvokerId(),
									e.getInvokerName() + ", du wurdest der Liste hinzugefügt!"
											+ "\n Momentan in der Liste: " + a.MatchMaker);
							for (int j = 0; j <= a.MatchMaker.size() && e.getInvokerId() != a.id.get(j); j++) {
								api.sendPrivateMessage(a.id.get(j),
										e.getInvokerName() + " wurde der Liste hinzugefügt");
								checkIfCueFullAndMove(a);
							}
						}
					}

					/**
					 * @param e
					 *            TextEvent
					 * @param a
					 *            Ensprechende Liste Wenn Spieler in der Liste,
					 *            dann wird er entfernt, wenn nicht dann null.
					 */
					public void deletePlayerFromList(TextMessageEvent e, TS3MM a) {
						if (a.check(e.getInvokerName()) == true) {
							api.sendPrivateMessage(e.getInvokerId(),
									"Du hast die Liste: " + a.getNameVoll() + " erfolgreich verlassen!");
							a.removePlayer(e.getInvokerName(), e.getInvokerId());
						} else {
							api.sendPrivateMessage(e.getInvokerId(),
									"Du bist gar nicht in der Liste " + a.getNameVoll());
						}

					}

					/**
					 * Zeigt alle vorhandenen Queues (Listen) an
					 */
					public void showAllQueues() {
						api.sendServerMessage("\n [B][U]Momentane Warteschlangen[/U][/B]\n" + "Counter-Strike: ["
								+ CS.getNumberOfPlayers() + "/" + CS.getSize() + "]" + "\n" + " Overwatch: ["
								+ OW.getNumberOfPlayers() + "/" + OW.getSize() + "]" + "\n" + "Debug: ["
								+ DB.getNumberOfPlayers() + "/" + DB.getSize() + "]");
					}

					/**
					 * @param a
					 *            Die Queue Zeigt eine einzige Queue an.
					 */
					public void showSingleQueue(TS3MM a) {
						api.sendServerMessage(
								"\nMomentan in der Queue \n" + a.getNameVoll() + ": [" + a.getNumberOfPlayers() + "/"
										+ a.getSize() + "]" + "\n" + "Deine m8's : " + a.MatchMaker);
					}
				});

	}
}
