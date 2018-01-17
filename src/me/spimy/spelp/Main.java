package me.spimy.spelp;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatColor;

public class Main extends JavaPlugin implements Listener {

	// ArrayLists
	ArrayList<String> ranks = new ArrayList<String>();
	List<String> list = getConfig().getStringList("ranks");

	ArrayList<Player> vanishlist = new ArrayList<Player>();
	// --------------------------------

	// Files & Config files
	private File spawnf;
	private FileConfiguration spawn;
	// --------------------------------

	// Spawn.yml Config
	public void setup() {
		if (!getDataFolder().exists()) {
			getDataFolder().mkdir();
		}

		spawnf = new File(getDataFolder(), "spawn.yml");

		if (!spawnf.exists()) {
			try {
				spawnf.createNewFile();
				Bukkit.getConsoleSender().sendMessage(
						ChatColor.translateAlternateColorCodes('&', "&f[&bSpelp&f] &7» &aspawn.yml has been created!"));
			} catch (IOException e) {
				Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&',
						"&f[&bSpelp&f] &7» &cspawn.yml could not be created!"));
			}
		}

		spawn = YamlConfiguration.loadConfiguration(spawnf);
	}

	public FileConfiguration getSpawnConfig() {
		return spawn;
	}

	public void saveSpawnConfig() {
		try {
			spawn.save(spawnf);
			Bukkit.getConsoleSender().sendMessage(
					ChatColor.translateAlternateColorCodes('&', "&f[&bSpelp&f] &7» &aspawn.yml has been saved!"));
		} catch (IOException e) {
			Bukkit.getConsoleSender().sendMessage(
					ChatColor.translateAlternateColorCodes('&', "&f[&bSpelp&f] &7» &cspawn.yml could not be saved!"));
		}
	}

	public void reloadSpawnConfig() {
		spawn = YamlConfiguration.loadConfiguration(spawnf);
		Bukkit.getConsoleSender().sendMessage(
				ChatColor.translateAlternateColorCodes('&', "&f[&bSpelp&f] &7» &aspawn.yml has been reloaded!"));
	}
	// --------------------------------

	// Plugin Enable/Disable
	public static Main pl;

	@Override
	public void onEnable() {
		getLogger().info("Spelp enabled");
		Bukkit.getServer().getPluginManager().registerEvents(this, this);
		setup();
		saveSpawnConfig();
		reloadSpawnConfig();
		getConfig().options().copyDefaults(true);
		saveDefaultConfig();

		pl = this;

	}

	@Override
	public void onDisable() {
		getLogger().info("Spelp disabled");
		pl = null;
	}

	public void reload() {
		reloadConfig();
	}
	// --------------------------------

	// First Join Events + Join Message
	@EventHandler
	public boolean onPlayerJoin(PlayerJoinEvent e) {
		e.setJoinMessage(null);
		Player p = (Player) e.getPlayer();
		World world = Bukkit.getWorld(getSpawnConfig().getString("spawn.world"));
		double x = getSpawnConfig().getDouble("spawn.x");
		double y = getSpawnConfig().getDouble("spawn.y");
		double z = getSpawnConfig().getDouble("spawn.z");
		float yaw = getSpawnConfig().getInt("spawn.yaw");
		float pitch = getSpawnConfig().getInt("spawn.pitch");
		p.teleport(new Location(world, x, y, z, yaw, pitch));
		if (getConfig().getBoolean("FirstJoinMsgEnabled") == false) {
			return false;
		} else {
			if (!p.hasPlayedBefore() && getConfig().getBoolean("FirstJoinMsgEnabled") == true) {
				String message = getConfig().getString("FirstJoinMessage");
				message = message.replace("{player}", p.getName());
				message = message.replace("{prefix}", getConfig().getString("prefix"));
				Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', message));
			} else if (getConfig().getBoolean("JoinMsgEnabled") == false) {
				return false;
			} else {
				if (getConfig().getBoolean("JoinMsgEnabled") == true && p.hasPermission("spelp.staffjoin")) {
					String message = getConfig().getString("StaffJoinMsg");
					message = message.replace("{player}", p.getPlayer().getName());
					message = message.replace("{prefix}", getConfig().getString("prefix"));
					Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', message));
				} else if (getConfig().getBoolean("JoinMsgEnabled") == true && !p.hasPermission("spelp.staffjoin")) {
					String message = getConfig().getString("JoinMsg");
					message = message.replace("{player}", p.getPlayer().getName());
					message = message.replace("{prefix}", getConfig().getString("prefix"));
					Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', message));
				}
			}
		}
		return true;
	}

	// --------------------------------

	// Quit Message
	@EventHandler
	public boolean onQuit(PlayerQuitEvent e) {
		e.setQuitMessage(null);
		Player p = (Player) e.getPlayer();
		if (getConfig().getBoolean("QuitMsgEnabled") == false) {
			return false;
		} else {
			if (getConfig().getBoolean("QuitMsgEnabled") == true && p.hasPermission("spelp.staffquit")) {
				String message = getConfig().getString("StaffQuitMsg");
				message = message.replace("{player}", p.getPlayer().getName());
				message = message.replace("{prefix}", getConfig().getString("prefix"));
				Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', message));
			} else if (getConfig().getBoolean("QuitMsgEnabled") == true && !p.hasPermission("spelp.staffquit")) {
				String message = getConfig().getString("QuitMsg");
				message = message.replace("{player}", p.getPlayer().getName());
				message = message.replace("{prefix}", getConfig().getString("prefix"));
				Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', message));
			}
		}
		return true;
	}
	// --------------------------------

	// Block /plugin, /p, /help, /?
	@EventHandler(priority = EventPriority.HIGHEST)
	public boolean blockCommand(PlayerCommandPreprocessEvent e) {
		Player p = e.getPlayer();
		if (e.getMessage().equals("/?") | e.getMessage().equalsIgnoreCase("/help") && !p.hasPermission("spelp.helpmsg")
				&& getConfig().getBoolean("helpmsgcmdEnabled") == false) {
			e.setCancelled(true);
			String message = getConfig().getString("helpmsgcmdDisabledMsg");
			message = message.replace("{player}", p.getName());
			message = message.replace("{prefix}", getConfig().getString("prefix"));
			p.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
		} else {
			if (!getConfig().getBoolean("helpmsgcmdEnabled") == false) {
				e.setCancelled(false);
			}
		}
		if (e.getMessage().equalsIgnoreCase("/plugins") | e.getMessage().equalsIgnoreCase("/pl") && !p.hasPermission("spelp.pluginlist")
				&& getConfig().getBoolean("plugincmdEnabled") == false) {
			e.setCancelled(true);
			String message = getConfig().getString("plugincmdDisabledMsg");
			message = message.replace("{player}", p.getName());
			message = message.replace("{prefix}", getConfig().getString("prefix"));
			p.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
		} else {
			if (!getConfig().getBoolean("plugincmdEnabled") == false) {
				e.setCancelled(false);
			}
		}
		return true;
	}
	// --------------------------------

	// Commands & Permissions
	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("website")) {
			if (!sender.hasPermission("spelp.website")) {
				String message = getConfig().getString("noperm");
				message = message.replace("{player}", sender.getName());
				message = message.replace("{prefix}", getConfig().getString("prefix"));
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
				return true;
			} else {
				if (getConfig().getBoolean("websiteEnabled") == false) {
					String message = getConfig().getString("disabled");
					message = message.replace("{player}", sender.getName());
					message = message.replace("{prefix}", getConfig().getString("prefix"));
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
				} else {
					getConfig().getBoolean("websiteEnabled");
					String message = getConfig().getString("websitemsg");
					message = message.replace("{player}", sender.getName());
					message = message.replace("{prefix}", getConfig().getString("prefix"));
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
					return true;
				}
			}
		} else if (cmd.getName().equalsIgnoreCase("spreload")) {
			if (!sender.hasPermission("spelp.reload")) {
				String message = getConfig().getString("noperm");
				message = message.replace("{player}", sender.getName());
				message = message.replace("{prefix}", getConfig().getString("prefix"));
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
				return true;
			}
			Bukkit.getPluginManager().disablePlugin(this);
			Bukkit.getPluginManager().enablePlugin(this);
			Bukkit.getPluginManager().getPlugin("Spelp").reloadConfig();
			String message = getConfig().getString("reloadmsg");
			message = message.replace("{player}", sender.getName());
			message = message.replace("{prefix}", getConfig().getString("prefix"));
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', message));
			return true;
		} else if (cmd.getName().equalsIgnoreCase("discord")) {
			if (!sender.hasPermission("spelp.discord")) {
				String message = getConfig().getString("noperm");
				message = message.replace("{player}", sender.getName());
				message = message.replace("{prefix}", getConfig().getString("prefix"));
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
				return true;
			} else {
				if (getConfig().getBoolean("discordEnabled") == false) {
					String message = getConfig().getString("disabled");
					message = message.replace("{player}", sender.getName());
					message = message.replace("{prefix}", getConfig().getString("prefix"));
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
				} else {
					getConfig().getBoolean("discordEnabled");
					String message = getConfig().getString("discordmsg");
					message = message.replace("{player}", sender.getName());
					message = message.replace("{prefix}", getConfig().getString("prefix"));
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
					return true;
				}
			}
		} else if (cmd.getName().equalsIgnoreCase("store")) {
			if (!sender.hasPermission("spelp.store")) {
				String message = getConfig().getString("noperm");
				message = message.replace("{player}", sender.getName());
				message = message.replace("{prefix}", getConfig().getString("prefix"));
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
				return true;
			} else {
				if (getConfig().getBoolean("storeEnabled") == false) {
					String message = getConfig().getString("disabled");
					message = message.replace("{player}", sender.getName());
					message = message.replace("{prefix}", getConfig().getString("prefix"));
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
				} else {
					getConfig().getBoolean("storeEnabled");
					String message = getConfig().getString("storemsg");
					message = message.replace("{player}", sender.getName());
					message = message.replace("{prefix}", getConfig().getString("prefix"));
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
					return true;
				}
			}
		} else if (cmd.getName().equalsIgnoreCase("forums")) {
			if (!sender.hasPermission("spelp.forums")) {
				String message = getConfig().getString("noperm");
				message = message.replace("{player}", sender.getName());
				message = message.replace("{prefix}", getConfig().getString("prefix"));
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
				return true;
			} else {
				if (getConfig().getBoolean("forumsEnabled") == false) {
					String message = getConfig().getString("disabled");
					message = message.replace("{player}", sender.getName());
					message = message.replace("{prefix}", getConfig().getString("prefix"));
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
				} else {
					getConfig().getBoolean("forumsEnabled");
					String message = getConfig().getString("forumsmsg");
					message = message.replace("{player}", sender.getName());
					message = message.replace("{prefix}", getConfig().getString("prefix"));
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
					return true;
				}
			}
		} else if (cmd.getName().equalsIgnoreCase("ranks")) {
			if (!sender.hasPermission("spelp.ranks")) {
				String message = getConfig().getString("noperm");
				message = message.replace("{player}", sender.getName());
				message = message.replace("{prefix}", getConfig().getString("prefix"));
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
				return true;
			} else {
				if (getConfig().getBoolean("ranksEnabled") == false) {
					String message = getConfig().getString("disabled");
					message = message.replace("{player}", sender.getName());
					message = message.replace("{prefix}", getConfig().getString("prefix"));
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
				} else {
					getConfig().getBoolean("ranksEnabled");
					for (String s : list) {
						ranks.add(ChatColor.translateAlternateColorCodes('&', s));
						s = s.replace("{player}", sender.getName());
						s = s.replace("{prefix}", getConfig().getString("prefix"));
						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', s));
					}
					return true;
				}
			}
		} else if (cmd.getName().equalsIgnoreCase("helpme")) {
			if (!sender.hasPermission("spelp.helpme")) {
				String message = getConfig().getString("noperm");
				message = message.replace("{player}", sender.getName());
				message = message.replace("{prefix}", getConfig().getString("prefix"));
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
				return true;
			} else {
				if (getConfig().getBoolean("helpmeEnabled") == false) {
					String message = getConfig().getString("disabled");
					message = message.replace("{player}", sender.getName());
					message = message.replace("{prefix}", getConfig().getString("prefix"));
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
				} else {
					for (Player p : Bukkit.getOnlinePlayers()) {
						if (p.hasPermission("spelp.helpreceive")) {
							getConfig().getBoolean("helpmeEnabled");
							String message = getConfig().getString("HelpmeMsg");
							String msgsent = getConfig().getString("SentHelpMessage");
							msgsent = msgsent.replace("{player}", sender.getName());
							message = message.replace("{player}", sender.getName());
							message = message.replace("{prefix}", getConfig().getString("prefix"));
							msgsent = msgsent.replace("{prefix}", getConfig().getString("prefix"));
							p.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
							sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msgsent));
							return true;
						}

					}

				}

			}

		} else if (cmd.getName().equalsIgnoreCase("setspawn")) {
			Player p = (Player) sender;
			if (!sender.hasPermission("spelp.setspawn")) {
				String message = getConfig().getString("noperm");
				message = message.replace("{player}", sender.getName());
				message = message.replace("{prefix}", getConfig().getString("prefix"));
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
				return true;
			} else {
				// what the command does
				getSpawnConfig().set("spawn.world", p.getLocation().getWorld().getName());
				getSpawnConfig().set("spawn.yaw", p.getLocation().getYaw());
				getSpawnConfig().set("spawn.pitch", p.getLocation().getPitch());
				getSpawnConfig().set("spawn.x", p.getLocation().getX());
				getSpawnConfig().set("spawn.y", p.getLocation().getY());
				getSpawnConfig().set("spawn.z", p.getLocation().getZ());
				saveSpawnConfig();
				/*
				 * spawn: world: (WORLD_NAME) yaw: (YAW) pitch: (PITCH) x: (COORDINATE) y:
				 * (COORDINATE) z: (COORDINATE)
				 */
				String message = getConfig().getString("SpawnSetMsg");
				message = message.replace("{player}", sender.getName());
				message = message.replace("{prefix}", getConfig().getString("prefix"));
				p.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
				return true;
			}
		} else if (cmd.getName().equalsIgnoreCase("spawn")) {
			Player p = (Player) sender;
			if (!sender.hasPermission("spelp.spawn")) {
				String message = getConfig().getString("noperm");
				message = message.replace("{player}", sender.getName());
				message = message.replace("{prefix}", getConfig().getString("prefix"));
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
				return true;
			} else {
				if (getSpawnConfig().getConfigurationSection("spawn") == null) {
					String message = getConfig().getString("SpawnNotSetMsg");
					message = message.replace("{player}", sender.getName());
					message = message.replace("{prefix}", getConfig().getString("prefix"));
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
					return true;
				}
				// what the command does
				World world = Bukkit.getWorld(getSpawnConfig().getString("spawn.world"));
				double x = getSpawnConfig().getDouble("spawn.x");
				double y = getSpawnConfig().getDouble("spawn.y");
				double z = getSpawnConfig().getDouble("spawn.z");
				float yaw = getSpawnConfig().getInt("spawn.yaw");
				float pitch = getSpawnConfig().getInt("spawn.pitch");
				p.teleport(new Location(world, x, y, z, yaw, pitch));
				p.playSound(p.getLocation(), Sound.ENDERMAN_TELEPORT, 10, 1);
				String message = getConfig().getString("SpawnTeleportedMsg");
				message = message.replace("{player}", sender.getName());
				message = message.replace("{prefix}", getConfig().getString("prefix"));
				p.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
				return true;
			}

		} else if (cmd.getName().equalsIgnoreCase("spelp")) {
			if (!sender.hasPermission("spelp.help")) {
				String message = getConfig().getString("noperm");
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
			} else {
				if (getConfig().getBoolean("spelpEnabled") == false) {
					String message = getConfig().getString("disabled");
					message = message.replace("{player}", sender.getName());
					message = message.replace("{prefix}", getConfig().getString("prefix"));
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
				} else {
					if (args.length < 1) {
						Player p = (Player) sender;
						getConfig().getBoolean("spelpEnabled");
						p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&r &r &7------- &6Spelp &7-------"));
						p.sendMessage(
								ChatColor.translateAlternateColorCodes('&', "&6/website: &aDisplays website URL"));
						p.sendMessage(ChatColor.translateAlternateColorCodes('&',
								"&6/discord: &aDisplays discord invite URL"));
						p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6/store: &aDisplays store URL"));
						p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6/forums: &aDisplays forums URL"));
						p.sendMessage(ChatColor.translateAlternateColorCodes('&',
								"&6/ranks: &aDisplays ranks available for the server"));
						p.sendMessage(ChatColor.translateAlternateColorCodes('&',
								"&6/helpme: &aCall online staff members for help"));
						p.sendMessage(
								ChatColor.translateAlternateColorCodes('&', "&6/spreload: &aReloads the config file"));
						p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6/spelp: &aShows this message"));
						p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f          "));
						p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aDo &6/spelp 2 &a for more info"));
						p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&r &7---------------------"));
						return true;

					} else if (args.length > 0 && args[0].equalsIgnoreCase("2")) {
						Player p = (Player) sender;
						getConfig().getBoolean("spelpEnabled");
						p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&r &7------- &6Spelp 2 &7-------"));
						p.sendMessage(
								ChatColor.translateAlternateColorCodes('&', "&6/setspawn: &aSets the spawn location"));
						p.sendMessage(
								ChatColor.translateAlternateColorCodes('&', "&6/spawn: &aTeleports you to spawn"));
						p.sendMessage(
								ChatColor.translateAlternateColorCodes('&', "&6/vanish: &aVanish/Unvanish yourself"));
						p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6/vanishlist: &aList all vanished players"));
						p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&r &7---------------------"));
						return true;
					}
				}
			}
		} else if (!sender.hasPermission("spelp.vanish")) {
			String message = getConfig().getString("noperm");
			message = message.replace("{player}", sender.getName());
			message = message.replace("{prefix}", getConfig().getString("prefix"));
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
		} else {
			if (getConfig().getBoolean("vanishEnabled") == false) {
				String message = getConfig().getString("disabled");
				message = message.replace("{player}", sender.getName());
				message = message.replace("{prefix}", getConfig().getString("prefix"));
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
			} else {
				if (!(sender instanceof Player)) {
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
							"&f[&bSpelp&f] &7» &cOnly a player can execute this command!"));
				} else {
					Player p = (Player) sender;
					if ((cmd.getName().equalsIgnoreCase("vanish")) && (sender.hasPermission("spelp.vanish"))) {
						if (this.vanishlist.contains(p)) {
							String message = getConfig().getString("UNvanishMsg");
							message = message.replace("{player}", sender.getName());
							message = message.replace("{prefix}", getConfig().getString("prefix"));
							p.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
							p.playEffect(p.getLocation(), Effect.POTION_BREAK, 100);
							this.vanishlist.remove(p);
							for (Player all : Bukkit.getOnlinePlayers()) {
								all.showPlayer(p);
							}
						} else {
							String message = getConfig().getString("vanishMsg");
							message = message.replace("{player}", sender.getName());
							message = message.replace("{prefix}", getConfig().getString("prefix"));
							p.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
							p.playEffect(p.getLocation(), Effect.POTION_BREAK, 100);
							this.vanishlist.add(p);
							for (Player all : Bukkit.getOnlinePlayers()) {
								all.hidePlayer(p);
							}
						}
					} else if ((cmd.getName().equalsIgnoreCase("vanishlist"))
							&& (sender.hasPermission("spelp.vanishlist")) && !vanishlist.isEmpty()) {
						String message = getConfig().getString("vanishedList");
						message = message.replace("{player}", sender.getName());
						message = message.replace("{prefix}", getConfig().getString("prefix"));
						p.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
						for (Player all : this.vanishlist) {
							p.sendMessage(ChatColor.translateAlternateColorCodes('&',
									"&7- " + getConfig().getString("vanishlistNameColor") + all.getName()));
						}
					} else {
						if ((cmd.getName().equalsIgnoreCase("vanishlist"))
								&& !(sender.hasPermission("spelp.vanishlist"))) {
							String message = getConfig().getString("noperm");
							message = message.replace("{player}", sender.getName());
							message = message.replace("{prefix}", getConfig().getString("prefix"));
							sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
						} else {
							if (vanishlist.isEmpty()) {
								String message = getConfig().getString("EmptyvanishedList");
								message = message.replace("{player}", sender.getName());
								message = message.replace("{prefix}", getConfig().getString("prefix"));
								sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
							}
						}
					}
				}
			}
		}
		return true;
	}

	// --------------------------------
}