package net.pixelors.styx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class ArenaManager {
	
	public GunGameMain plugin;
	
	private List<Arena> arenas = new ArrayList<Arena>();

	public HashMap<String, Integer> players = new HashMap<String, Integer>();
	public HashMap<String, Integer> players_level = new HashMap<String, Integer>();
	
	public HashMap<String, Integer> players_oldlevel = new HashMap<String, Integer>();
	public HashMap<String, Float> players_oldexp = new HashMap<String, Float>();
	
	public ArenaManager(GunGameMain main) {
		this.plugin = main;
	}
	
	public void readDataFromConfig() {
		this.arenas.clear();
		this.players.clear();
		
		int arenaCount = this.plugin.getConfig().getInt("arenaCount");
		
		for (int x = 0; x < arenaCount; x++) {
			Boolean isEnabled = this.plugin.getConfig().getBoolean("arenas." + x + ".enabled");
			Location lobby = this.plugin.stringToLoc(this.plugin.getConfig().getString("arenas." + x + ".lobby"));
			
			List<String> spawn_strings = this.plugin.getConfig().getStringList("arenas." + x + ".spawns");
			
			List<Location> spawns = new ArrayList<Location>();
			for (String s : spawn_strings) {
				spawns.add(this.plugin.stringToLoc(s));
			}
			
			List<String> sign_strings = this.plugin.getConfig().getStringList("arenas." + x + ".signs");
			
			List<Location> signs = new ArrayList<Location>();
			for (String s : sign_strings) {
				signs.add(this.plugin.stringToLoc(s));
			}
			
			this.arenas.add(new Arena(this, this.arenas.size(), 2, 10, isEnabled, lobby, spawns, signs));
		}
	}
	
	public void createArena(Player p) {
		int arenaCount = this.plugin.getConfig().getInt("arenaCount");
		
		this.arenas.add(new Arena(this, this.arenas.size(), 2, 10));
		
		this.plugin.getConfig().set("arenaCount", arenaCount+1);
		
		this.plugin.getConfig().set("arenas." + arenaCount + ".lobby", this.plugin.locToString(p.getLocation()));
		this.plugin.getConfig().set("arenas." + arenaCount + ".spawns", new ArrayList<String>());
		
		this.plugin.getConfig().set("arenas." + arenaCount + ".enabled", true);
		
		this.plugin.saveConfig();
	}
	
	public void deleteArena(Arena a) {
		this.arenas.remove(a);
		
		this.plugin.getConfig().set("arenas." + a.getID() + ".enabled", false);
		
		this.plugin.saveConfig();
	}
	
	public Arena getArena(int arenaID) {
		return this.arenas.get(arenaID);
	}
	
	public Arena getArena(Player p) {
		if (this.players.containsKey(p.getName())) {
			int arenaID = this.players.get(p.getName());
			return this.arenas.get(arenaID);
		} else {
			return null;
		}
	}
	
	public int getPlayers(int arenaID) {
		int temp = 0;
		
		for (Integer i : this.players.values()) {
			if (i == arenaID) {
				temp++;
			}
		}
		
		return temp;
	}

	public List<Player> getPlayerList(int arenaID) {
		List<Player> temp = new ArrayList<Player>();
		
		for (String s : this.players.keySet()) {
			if (this.players.get(s) == arenaID) {
				Player p = Bukkit.getPlayer(s);
				if (p != null) {
					temp.add(p);
				}
			}
		}
		
		return temp;
	}

}
