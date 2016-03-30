package net.pixelors.styx;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class GunGameMain extends JavaPlugin implements CommandExecutor {

	public String prefix = "§6[§bGunGame§6] ";

	public ArenaManager manager;

	public Location mainSpawn;

	@Override
	public void onEnable() {
		manager = new ArenaManager(this);
		getCommand("gga").setExecutor(this);
		getCommand("gg").setExecutor(this);

		FileConfiguration cfg = this.getConfig();
		cfg.options().copyDefaults(true);
		this.saveConfig();

		if (this.getConfig().getString("mainSpawn") != null) {
			this.mainSpawn = stringToLoc(this.getConfig().getString("mainSpawn"));
		}

		this.manager.readDataFromConfig();

		Bukkit.getPluginManager().registerEvents(new GunGameListener(this), this);

		System.out.println(prefix + "The Plugin " + this.getDescription().getName() + " Version "
				+ this.getDescription().getVersion() + " loaded!");
	}

	@Override
	public void onDisable() {

	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("gg")) {
			Player p = (Player) sender;

			if (args.length == 0) {
				sender.sendMessage("§6 §m------------ §bGunGame §6§m------------");
				sender.sendMessage("§aVersion: §b" + this.getDescription().getVersion());
				sender.sendMessage("§aAuthor: §b" + this.getDescription().getAuthors());
				sender.sendMessage("§6 §m------------ §bGunGame §6§m------------");
			}
			
			if (args[0].equalsIgnoreCase("join")) {

				if (args.length == 1) {
					p.sendMessage(prefix + "§c/gg join <ArenaID>");
				} else {
					int arenaID = Integer.valueOf(args[1]);
					this.manager.players.put(p.getName(), arenaID);
					
					p.sendMessage(prefix + "§aSuccessfully joined the game §eGG-" + args[1] + "!");
				}

			}

		} else if (cmd.getName().equalsIgnoreCase("gga")) {
			if (sender instanceof Player) {
				Player p = (Player) sender;

				if (!p.hasPermission("gungame.admin")) {
					p.sendMessage(prefix + "§cYou don't have the Permission to use that Command!");
					return true;
				}

				if (args.length == 0) {
					p.sendMessage("§cGunGame-Help:");
					p.sendMessage("§6/gga setlobby §8to set the arenalobby");
					p.sendMessage("§6/gga addspawn §8to add an arenaspawn");
					p.sendMessage("§6/gga resetspawns §8to reset the arenaspawns");
					p.sendMessage("§6/gga addarena §8to create an arena!");

				}

				if (args[0].equalsIgnoreCase("setmainspawn")) {

					this.mainSpawn = p.getLocation();
					this.getConfig().set("mainSpawn", locToString(this.mainSpawn));
					p.sendMessage(prefix + "§aThe main spawn was successfully set!");

				}
				if (args[0].equalsIgnoreCase("addarena")) {

					this.manager.createArena(p);
					p.sendMessage(prefix + "§aSuccessfully created a new Arena!");

				}
				if (args[0].equalsIgnoreCase("setlobby")) {

					if (args.length == 1) {
						p.sendMessage(prefix + "§c/gga setlobby <ArenaID>");
					} else {
						int arenaID = Integer.valueOf(args[1]);
						Arena a = this.manager.getArena(arenaID);
						a.setLobby(p.getLocation());
						p.sendMessage(prefix + "§aSuccessfully updated LobbyLocation!");
					}

				}
				if (args[0].equalsIgnoreCase("addspawn")) {

					if (args.length == 1) {
						p.sendMessage(prefix + "§c/gga addspawn <ArenaID>");
					} else {
						int arenaID = Integer.valueOf(args[1]);
						Arena a = this.manager.getArena(arenaID);
						a.addSpawn(p.getLocation());
						p.sendMessage(prefix + "§aSuccessfully added SpawnLocation!");
					}

				}

				if (args[0].equalsIgnoreCase("resetspawns")) {

					if (args.length == 1) {
						p.sendMessage(prefix + "§c/gga resetspawns <ArenaID>");
					} else {
						int arenaID = Integer.valueOf(args[1]);
						Arena a = this.manager.getArena(arenaID);
						a.resetSpawns();
						p.sendMessage(prefix + "§aSuccessfully reset SpawnLocations!");
					}
				}

			} else {
				sender.sendMessage(prefix + "§cOnly for Players!");
			}
		} else {
			sender.sendMessage(prefix + "§cTry /gg");
		}

		return true;
	}

	public String locToString(Location l) {
		String ret;
		ret = l.getWorld().getName() + "," + l.getBlockX() + "," + l.getBlockY() + "," + l.getBlockZ();
		return ret;
	}

	public Location stringToLoc(String s) {

		String[] a = s.split("\\,");

		World w = Bukkit.getServer().getWorld(a[0]);

		float x = Float.parseFloat(a[1]);
		float y = Float.parseFloat(a[2]);
		float z = Float.parseFloat(a[3]);

		return new Location(w, x, y, z);
	}

}
