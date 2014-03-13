package me.mcluke300.playerlogger.listeners;

import java.util.Map;

import me.mcluke300.playerlogger.playerlogger;
import me.mcluke300.playerlogger.config.*;
import me.mcluke300.playerlogger.mysql.*;
import me.mcluke300.playerlogger.filehandler.*;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.inventory.ItemStack;

public class PListener implements Listener{
	playerlogger plugin;
	addData datadb;

	public PListener(playerlogger instance) {
		plugin = instance;
		datadb = new addData(plugin);
	}

	//Player Join
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled=true)
	public void onPlayerJoin(PlayerLoginEvent event) {
		if (getConfig.PlayerJoins()) {
			if(event.getResult() == PlayerLoginEvent.Result.ALLOWED) {
				Player player = event.getPlayer();
				World world = player.getWorld();
				String worldname = world.getName();
				String playername = player.getName();
				String ip = "Error";
				Boolean staff = false;
				ip = event.getAddress().getHostAddress();
				double x = (int) Math.floor(player.getLocation().getX());
				double y = (int) Math.floor(player.getLocation().getY());
				double z = (int) Math.floor(player.getLocation().getZ());
				if (player.hasPermission("PlayerLogger.staff")) {
					staff = true;
				}
				if (getConfig.logFilesEnabled()) {
					filehandler.logLogin(playername, worldname, x, y, z, ip, staff);
				}
				if (getConfig.MySQLEnabled()) {
					datadb.add(playername,"join", ip, x, y, z, worldname, staff);
				}
			}
		}
	}

	//Player Quit
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled=true)
	public void onPlayerQuit(PlayerQuitEvent event) {
		if (getConfig.PlayerQuit()) {
			Player player = event.getPlayer();
			World world = player.getWorld();
			String worldname = world.getName();
			String playername = player.getName();
			Boolean staff = false;
			double x = (int) Math.floor(player.getLocation().getX());
			double y = (int) Math.floor(player.getLocation().getY());
			double z = (int) Math.floor(player.getLocation().getZ());
			if (player.hasPermission("PlayerLogger.staff")) {
				staff = true;
			}
			if (getConfig.logFilesEnabled()) {
				filehandler.logQuit(playername, worldname, x, y, z, staff);
			}
			if (getConfig.MySQLEnabled()) {
				datadb.add(playername,"quit", "", x, y, z, worldname, staff);	
			}
		}
	}

	//Player Chat
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled=true)
	public void onPlayerChat(final AsyncPlayerChatEvent event) {
		if(getConfig.PlayerChat()) {
			final Player player = event.getPlayer();
			World world = player.getWorld();
			final String worldname = world.getName();
			final String playername = player.getName();
			final String msg = event.getMessage();
			final double x = (int) Math.floor(player.getLocation().getX());
			final double y = (int) Math.floor(player.getLocation().getY());
			final double z = (int) Math.floor(player.getLocation().getZ());
			Boolean staff = false;
			if (player.hasPermission("PlayerLogger.staff")) {
				staff = true;
			}
			if (getConfig.logFilesEnabled()) {
				filehandler.logChat(playername, msg, worldname, x, y, z, staff);
			}
			if (getConfig.MySQLEnabled()) {
				datadb.add(playername,"chat", msg, x, y, z, worldname, staff);
			}
		}
	}
		
	//Player Command
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled=true)
	public void onPlayerCmd(final PlayerCommandPreprocessEvent event) {
		if (getConfig.PlayerCommands()) {
			final Player player = event.getPlayer();
			World world = player.getWorld();
			final String worldname = world.getName();
			final String playername = player.getName();
			final String msg = event.getMessage();
			final String msg2[] = event.getMessage().split(" "); 
			Boolean staff = false;	
			Boolean log = true;
			final double x = (int) Math.floor(player.getLocation().getX());
			final double y = (int) Math.floor(player.getLocation().getY());
			final double z = (int) Math.floor(player.getLocation().getZ());
			if (getConfig.BlackListCommands() || getConfig.BlackListCommandsMySQL()) {
				for (String m : getConfig.CommandsToBlock()) {
					m = m.toString().toLowerCase();
					if (msg2[0].equalsIgnoreCase(m)) {
						log = false;
						break;
					}
				}
			}
	
			if (player.hasPermission("PlayerLogger.staff")) {
				staff = true;
			}
	
			// Log this command
			if (log) {
				if (getConfig.logFilesEnabled()) {
					filehandler.logChat(playername, msg, worldname, x, y, z, staff);
				}
				if (getConfig.MySQLEnabled()) {
					datadb.add(playername,"command", msg, x, y, z, worldname, staff);
				}
			}
		}
	}

	//Player Deaths
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled=true)
	public void onEntityDeath(EntityDeathEvent event){
		Entity ent = event.getEntity();
		if(ent instanceof Player){
			Player player = (Player)event.getEntity();
			World world = player.getWorld();
			String worldname = world.getName();
			String playername = player.getName();
			Boolean staff = false;	
			String reason = event.getEventName();
			double x = (int) Math.floor(player.getLocation().getX());
			double y = (int) Math.floor(player.getLocation().getY());
			double z = (int) Math.floor(player.getLocation().getZ());
			if (getConfig.PlayerDeaths()) {
				if (player.hasPermission("PlayerLogger.staff")) {
					staff = true;
				}
				if (getConfig.logFilesEnabled()) {
					filehandler.logPlayerDeath(playername, reason, worldname, x, y, z, staff);
				}
				if (getConfig.MySQLEnabled()) {
					datadb.add(playername,"death", "", x, y, z, worldname, staff);
				}
			}
		}
	}


	//Player Enchant
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled=true)
	public void onEnchant(EnchantItemEvent event){
		Player player = (Player)event.getEnchanter();
		String playername = player.getName();
		World world = player.getWorld();
		Boolean staff = false;		
		String worldname = world.getName();
		Map<Enchantment, Integer> ench = event.getEnchantsToAdd();
		ItemStack item = event.getItem();
		int cost = event.getExpLevelCost();
		double x = (int) Math.floor(player.getLocation().getX());
		double y = (int) Math.floor(player.getLocation().getY());
		double z = (int) Math.floor(player.getLocation().getZ());
		if (getConfig.PlayerEnchants()) {
			if (player.hasPermission("PlayerLogger.staff")) {
				staff = true;
			}
			if (getConfig.logFilesEnabled()) {
				filehandler.logEnchant(playername, ench, item, cost, worldname, x, y, z, staff);
			}
			if (getConfig.MySQLEnabled()) {
				datadb.add(playername,"enchant", item+" "+ench+" Xp Cost:"+cost, x, y, z, worldname, staff);
			}
		}
	}

	//Player Bucket
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled=true)
	public void onBucket(PlayerBucketEmptyEvent event){
		if (getConfig.PlayerBucketPlace()) {
			if (event.isCancelled() == false) {
				Player player = event.getPlayer();
				String playername = player.getName();
				World world = player.getWorld();
				Boolean lava = false;
				String worldname = world.getName();
				int x;
				int y;
				Boolean staff = false;
				int z;
				if (event.getBucket() != null && event.getBucket()==Material.LAVA_BUCKET) {
					lava = true;
					x = event.getBlockClicked().getLocation().getBlockX();
					y = event.getBlockClicked().getLocation().getBlockY();
					z = event.getBlockClicked().getLocation().getBlockZ();
					if (player.hasPermission("PlayerLogger.staff")) {
						staff = true;
					}
					if (getConfig.logFilesEnabled()) {
						filehandler.logBucket(playername, worldname, x, y, z, lava, staff);
					}
					if (getConfig.MySQLEnabled()) {
						datadb.add(playername,"bucket", "Lava", x, y, z, worldname, staff);
					}

				}
				else if (event.getBucket() != null && event.getBucket()==Material.WATER_BUCKET) {
					lava = false;
					x = event.getBlockClicked().getLocation().getBlockX();
					y = event.getBlockClicked().getLocation().getBlockY();
					z = event.getBlockClicked().getLocation().getBlockZ();
					if (player.hasPermission("PlayerLogger.staff")) {
						staff = true;
					}
					if (getConfig.logFilesEnabled()) {
						filehandler.logBucket(playername, worldname, x, y, z, lava, staff);
					}
					if (getConfig.MySQLEnabled()) {
						datadb.add(playername,"bucket", "Water", x, y, z, worldname, staff);
					}
				}
			}
		}
	}

	//Player Sign Change event
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled=true)
	public void onSign(SignChangeEvent event){
		if (event.isCancelled() == false) {
			if (getConfig.PlayerSignText()) {
				Player player = event.getPlayer();
				String playername = player.getName();
				World world = player.getWorld();
				Boolean staff = false;	
				String[] lines = event.getLines();
				String worldname = world.getName();
				int x = event.getBlock().getLocation().getBlockX();
				int y = event.getBlock().getLocation().getBlockY();
				int z = event.getBlock().getLocation().getBlockZ();
				if (player.hasPermission("PlayerLogger.staff")) {
					staff = true;
				}
				if (getConfig.logFilesEnabled()) {
					filehandler.logSign(playername, worldname, x, y, z, lines, staff);
				}
				if (getConfig.MySQLEnabled()) {
					datadb.add(playername,"sign", "["+lines[0]+"]"+"["+lines[1]+"]"+"["+lines[2]+"]"+"["+lines[3]+"]", x, y, z, worldname, staff);
				}
			}
		}
	}

	//PlayerPvp
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled=true)
	public void onEntityDeath1(org.bukkit.event.entity.EntityDeathEvent event) {
		if (getConfig.PlayerPvp()) {
			org.bukkit.entity.Entity ply = event.getEntity();
			if (event.getEntity().getLastDamageCause() instanceof org.bukkit.event.entity.EntityDamageByEntityEvent) {
				org.bukkit.entity.Entity dmgr = ((org.bukkit.event.entity.EntityDamageByEntityEvent) event.getEntity().getLastDamageCause()).getDamager();
				if (ply instanceof Player) {            
					if (dmgr instanceof Player) {
						String worldname = ply.getWorld().getName();
						String player = ((Player) ply).getName();
						String damager = ((Player) dmgr).getName();
						Boolean staff = false;				
						Boolean staff2 = false;				
						double x = Math.floor(dmgr.getLocation().getX());
						double y = Math.floor(dmgr.getLocation().getY());
						double z = Math.floor(dmgr.getLocation().getZ());
						double x2 = Math.floor(ply.getLocation().getX());
						double y2 = Math.floor(ply.getLocation().getY());
						double z2 = Math.floor(ply.getLocation().getZ());

						if (((Player) dmgr).hasPermission("PlayerLogger.staff")) {
							staff = true;
						}
						if (((Player) ply).hasPermission("PlayerLogger.staff")) {
							staff2 = true;
						}
						if (getConfig.logFilesEnabled()) {
							filehandler.logKill(player, damager, x, y, z, worldname, staff);
							filehandler.logKilledBy(player, damager, x2, y2, z2, worldname, staff2);
						}
						if (getConfig.MySQLEnabled()) {
							datadb.add(damager,"kill",player, x, y, z, worldname, staff);
							datadb.add(player,"killedby",damager, x, y, z, worldname, staff2);
						}
					}
				}
			}
		}
	}

	//Console Logger
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled=true)
	public void onPlayerConsoleCommand(ServerCommandEvent event) {
		String msg = event.getCommand();
		if (getConfig.ConsoleCommands() && getConfig.logFilesEnabled()) {
			filehandler.logConsole(msg);
		}
		if (getConfig.ConsoleCommands() && getConfig.MySQLEnabled()) {
			datadb.add("","console", msg, 0, 0, 0, "", true);
		}
	}

	//BlockPlace
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled=true)
	public void onBlockPlace(BlockPlaceEvent event) {
		if (event.isCancelled() == false) {
			if(getConfig.LogBlackListedBlocks()) {
				Player player = event.getPlayer();
				String playername = player.getName();
				World world = player.getWorld();
				String worldname = world.getName();
				Boolean staff = false;		
				int x = event.getBlock().getLocation().getBlockX();
				int y = event.getBlock().getLocation().getBlockY();
				int z = event.getBlock().getLocation().getBlockZ();
				String blockid = "" + event.getBlock().getTypeId();
				Boolean log = false;
				for (String m : getConfig.Blocks()) {
					m = m.toString().toLowerCase();
					if (blockid.equals(m) || m.equalsIgnoreCase("*")) {
						log = true;
						break;
					}
				}
				if (log) {
					String blockname = event.getBlock().getType().toString();
					blockname = blockname.replaceAll("_", " ");
					//Checks
					if (player.hasPermission("PlayerLogger.staff")) {
						staff = true;
					}
					//Logging
					if (getConfig.logFilesEnabled()) {
						filehandler.logPlace(playername, worldname, blockname, x, y, z, staff);
					}
					if (getConfig.MySQLEnabled()) {
						datadb.add(playername,"place", blockname, x, y, z, worldname, staff);
					}
				}
			}
		}
	}

	//BlockBreak
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled=true)
	public void onBlockBreak(BlockBreakEvent event) {
		if (event.isCancelled() == false) {
			if(getConfig.LogBlackListedBlocks()) {
				Player player = event.getPlayer();
				String playername = player.getName();
				World world = player.getWorld();
				String worldname = world.getName();
				Boolean staff = false;	
				int x = event.getBlock().getLocation().getBlockX();
				int y = event.getBlock().getLocation().getBlockY();
				int z = event.getBlock().getLocation().getBlockZ();
				String blockid = "" + event.getBlock().getTypeId();
				Boolean log = false;
				for (String m : getConfig.Blocks()) {
					m = m.toString().toLowerCase();
					if (blockid.equalsIgnoreCase(m) || m.equalsIgnoreCase("*")) {
						log = true;
						break;
					}
				}
				if (log) {
					String blockname = event.getBlock().getType().toString();
					blockname = blockname.replaceAll("_", " ");
					//Checks
					if (player.hasPermission("PlayerLogger.staff")) {
						staff = true;
					}
					//Logging
					if (getConfig.logFilesEnabled()) {
						filehandler.logBreak(playername, worldname, blockname, x, y, z, staff);
					}
					if (getConfig.MySQLEnabled()) {
						datadb.add(playername,"break", blockname, x, y, z, worldname, staff);
					}
				}
			}
		}
	}
}
