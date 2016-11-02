package com.github.theholywaffle.teamspeak3;

import java.lang.invoke.SwitchPoint;
import java.util.ArrayList;
import java.util.List;

public class TS3MM extends TS3Bot {
	private String name;
	private String namevoll;
	private int groesse;
	private int destChannel;
	// api.sendChannelMessage("V-Lan.de Bot ist online!");
	List<String> MatchMaker;
	List<Integer> id;

	public TS3MM() {

	}

	public TS3MM(String name) {

		this.MatchMaker = new ArrayList<String>();
		this.id = new ArrayList<Integer>();

		this.name = name;

		switch (name) {
		case "cs":
		case "lol":
			this.groesse = 5;
			this.destChannel = 73;
			this.namevoll = "Counter-Strike";
			break;
		case "ow":
			this.groesse = 6;
			this.namevoll = "Overwatch";
			this.destChannel = 76;
			break;
		case "db":
			this.groesse = 3;
			this.destChannel = 78;
			this.namevoll = "Debug";
			break;
		default:
			break;
		}
	}

	public void addPlayer(String s, int t) {
		MatchMaker.add(s);
		id.add(t);
	}

	// public void checkIfCueFull(TS3MM){
	//
	// }

	public int getListLength() {
		int length = MatchMaker.size();
		return length;
	}

	public String getName() {
		return this.name;
	}

	public String getNameVoll() {
		return this.namevoll;
	}

	public void setName(String s) {
		this.name = s;
	}

	public int getSize() {
		return groesse;
	}

	public void deleteList() {
		MatchMaker.clear();
		id.clear();
	}

	public boolean check(String s) {
		if (MatchMaker.contains(s)) {
			return true;
		} else {
			return false;
		}
	}

	public void removePlayer(String invokerName, int invokerId) {
		MatchMaker.remove(invokerName);
		id.remove(invokerId);
	}

	public int getNumberOfPlayers() {
		return MatchMaker.size();
	}

	public int getID(String s) {
		return 0;
	}

	public int getDestChannel() {
		return destChannel;
	}
}
