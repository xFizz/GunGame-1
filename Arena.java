package net.pixelors.styx;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class Arena implements Listener{
	
	
	
	
	public String prefix = "§6[§bGunGame§6] ";
	
	@EventHandler
	public void onSignInteract(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		
			if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
				if (e.getClickedBlock().getState() instanceof Sign) {
					Sign s = (Sign) e.getClickedBlock().getState();
					

					if (s.getLine(0).equalsIgnoreCase("§6[§bGunGame§6]")) {
						p.getInventory().clear();
						p.updateInventory();
						
						
						
						
		
					}
				}
			}
	}
	
	public List<Location> signs = new ArrayList<Location>();
	
	private List<Location> spawns = new ArrayList<Location>();
	
	private Location lobby;
	
	private ArenaManager manager;
	
	private int arenaID;

	private int minPlayers;
	private int maxPlayers;
	
	private boolean isEnabled;
	
	public ArenaPhase phase;
	
	public Arena(ArenaManager main, int arenaID, int minPlayers, int maxPlayers) {
		this.phase = ArenaPhase.LOBBY;
		
		this.isEnabled = true;
		
		this.manager = main;

		this.minPlayers = minPlayers;
		this.maxPlayers = maxPlayers;
		
		this.arenaID = arenaID;
	}
	
	public Arena(ArenaManager main, int arenaID, int minPlayers, int maxPlayers, boolean isEnabled, Location lobby, List<Location> spawns, List<Location> signs) {
		this.isEnabled = isEnabled;
		this.spawns = spawns;
		this.signs = signs;
		this.lobby = lobby;
		
		this.manager = main;

		this.minPlayers = minPlayers;
		this.maxPlayers = maxPlayers;
		
		this.arenaID = arenaID;
	}
	
	public Location getRandomSpawn() {
		Collections.shuffle(this.spawns);
		if (this.spawns.size() > 0) {
			return this.spawns.get(0);
		} else {
			return null;
		}
	}
	
	public int getID() {
		return this.arenaID;
	}
	
	public void setEnabled(Boolean b) {
		this.isEnabled = b;
	}
	
	public boolean isEnabled() {
		return this.isEnabled;
	}
	
	public void updateSigns() {
		for (Location l : this.signs) {
			this.updateSign(l);
		}
	}
	
	public String[] updateSign(Location l) {
		if (l.getBlock().getType() == Material.WALL_SIGN) {
			Block b = l.getBlock();
			Sign s = (Sign) b.getState();
			s.setLine(0, "§6[§bGunGame§6]");
			s.setLine(1, "§a" + (this.arenaID+1));
			
			switch (this.phase) {
			case COUNTDOWN:
				s.setLine(2, "§4Not joinable");
				break;
			case ENDING:
				s.setLine(2, "§4Not joinable");
				break;
			case LOBBY:
				s.setLine(2, "§5§lIn Lobby");
				break;
			case RUNNING:
				s.setLine(2, "§4Not joinable");
				break;
			case STARTING:
				s.setLine(2, "§6Starting...");
				break;
			}
			
			s.setLine(3, this.manager.getPlayerList(this.arenaID).size() + "/" + this.maxPlayers);
			
			s.update(true);
			
			return s.getLines();
		} else {
			return null;
		}
	}
	
	/*
	 * [GunGame]
	 * GG-1
	 * In Lobby
	 * 2 / 10
	 */
	
	public void startArena() {
		List<Location> temp_spawns = this.spawns;
		
		try {
			for (Player p : this.manager.getPlayerList(this.arenaID)) {
				try {
					if (temp_spawns.size() > 0) { 
						p.teleport(temp_spawns.get(0));
						temp_spawns.remove(0);
					} else {
						System.out.println("§cERROR: No spawns available!!");
					}
				} catch (Exception e1) {
				}
			}
		} catch (Exception e1) {
		}
		
		startCountdown();
	}
	
	public void win(Player p) {
		this.phase = ArenaPhase.ENDING;
		
		updateSigns();
		
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "eco give " + p.getName() + " 200");
		p.sendMessage(prefix + "§a§lYou won a GunGame-Round! §6§lYou have been rewarded with §e§l200$");
		
		for (Player pl : this.manager.getPlayerList(this.getID())) {
			if (pl != p) {
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "eco give " + pl.getName() + " 10");
				pl.sendMessage(prefix + "§a§lYou played a GunGame-Round! §6§lYou have been rewarded with §e§l10$");
			}
			pl.teleport(this.manager.plugin.mainSpawn);
			
			this.manager.players.remove(pl.getName());
		}
		
		this.phase = ArenaPhase.LOBBY;
		
		updateSigns();
	}
	
	public void joinLobby(Player p) {
		if (this.maxPlayers > this.manager.getPlayers(this.arenaID) && this.spawns.size() > this.manager.getPlayers(this.arenaID)) {
			
			if (this.phase == ArenaPhase.LOBBY || this.phase == ArenaPhase.COUNTDOWN) {
			
				this.manager.players.put(p.getName(), this.arenaID);
				
				p.teleport(this.lobby);
	
				this.manager.players_oldexp.put(p.getName(), p.getExp());
				this.manager.players_oldlevel.put(p.getName(), p.getLevel());
				
				this.manager.players_level.put(p.getName(), 0);
				p.setExp(1.0F);
				p.setLevel(0);
				
				updateSigns();
				
			} else {
				p.sendMessage(prefix + "§c§lThis arena is already running!");
			}
			
		} else {
			p.sendMessage(prefix + "§cThis arena is already full!");
		}
		
		if (this.minPlayers <= this.manager.getPlayers(this.arenaID) && this.phase == ArenaPhase.LOBBY) {
			
			this.phase = ArenaPhase.STARTING;
			
			updateSigns();
			
			for (Player pl : this.manager.getPlayerList(this.arenaID)) {
				pl.sendMessage(prefix + "§6§lThe game will start in 15 seconds!");
			}
			
			Bukkit.getScheduler().scheduleSyncDelayedTask(this.manager.plugin, new Runnable() {
				
				@Override
				public void run() {
					for (Player pl : manager.getPlayerList(arenaID)) {
						pl.sendMessage(prefix + "§6§lThe game will start in 5 seconds!");
					}
					
					Bukkit.getScheduler().scheduleSyncDelayedTask(manager.plugin, new Runnable() {
						
						@Override
						public void run() {
							for (Player pl : manager.getPlayerList(arenaID)) {
								pl.sendMessage(prefix + "§6§lThe game is starting...");
							}
							
							startArena();
						}
					}, 100);
				}
			}, 200);
			
		}
	}
	
	public void setLobby(Location l) {
		this.lobby = l;
		
		this.manager.plugin.getConfig().set("arenas." + this.arenaID + ".lobby", this.manager.plugin.locToString(l));
		
		this.manager.plugin.saveConfig();
	}
	
	public void addSpawn(Location l) {
		spawns.add(l);
		
		List<String> temp = this.manager.plugin.getConfig().getStringList("arenas." + this.arenaID + ".spawns");
		temp.add(this.manager.plugin.locToString(l));
		this.manager.plugin.getConfig().set("arenas." + this.arenaID + ".spawns", temp);
		
		this.manager.plugin.saveConfig();
	}
	
	public void resetSpawns() {
		spawns.clear();
		
		this.manager.plugin.getConfig().set("arenas." + this.arenaID + ".spawns", new ArrayList<String>());
		
		this.manager.plugin.saveConfig();
	}
	
	public void startCountdown() {
		this.phase = ArenaPhase.COUNTDOWN;
		
		updateSigns();
		
		for (Player pl : manager.getPlayerList(arenaID)) {
			pl.sendMessage(prefix + "§6§lThe game starts in 30sec");
		}
		Bukkit.getScheduler().scheduleSyncDelayedTask(manager.plugin, new Runnable() {
			
			@Override
			public void run() {
				for (Player pl : manager.getPlayerList(arenaID)) {
					pl.sendMessage(prefix + "§6§lThe game starts in 20sec");
				}
				Bukkit.getScheduler().scheduleSyncDelayedTask(manager.plugin, new Runnable() {
					
					@Override
					public void run() {
						for (Player pl : manager.getPlayerList(arenaID)) {
							pl.sendMessage(prefix + "§6§lThe game starts in 10sec");
						}
						Bukkit.getScheduler().scheduleSyncDelayedTask(manager.plugin, new Runnable() {
							
							@Override
							public void run() {
								for (Player pl : manager.getPlayerList(arenaID)) {
									pl.sendMessage(prefix + "§6§lThe game starts in 5sec");
								}
								Bukkit.getScheduler().scheduleSyncDelayedTask(manager.plugin, new Runnable() {
									
									@Override
									public void run() {
										for (Player pl : manager.getPlayerList(arenaID)) {
											pl.sendMessage(prefix + "§6§lThe game starts in 3sec");
										}
										Bukkit.getScheduler().scheduleSyncDelayedTask(manager.plugin, new Runnable() {
											
											@Override
											public void run() {
												for (Player pl : manager.getPlayerList(arenaID)) {
													pl.sendMessage(prefix + "§6§lThe game starts in 2sec");
												}
												Bukkit.getScheduler().scheduleSyncDelayedTask(manager.plugin, new Runnable() {
													
													@Override
													public void run() {
														for (Player pl : manager.getPlayerList(arenaID)) {
															pl.sendMessage(prefix + "§6The game starts in §e1 §6sec");
														}
														Bukkit.getScheduler().scheduleSyncDelayedTask(manager.plugin, new Runnable() {
															
															@Override
															public void run() {
																for (Player pl : manager.getPlayerList(arenaID)) {
																	pl.sendMessage(prefix + "§6§lYou can now attack eachother!");
																}
																
																phase = ArenaPhase.RUNNING;
																
																updateSigns();
															}
														}, 20);
													}
												}, 20);
											}
										}, 20);
									}
								}, 40);
							}
						}, 100);
					}
				}, 200);
			}
		}, 200);
	}

}
