package com.github.sirkarpfen.chatchannel;

import java.util.Hashtable;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

/** Credits to t3hk0d3 for providing the basic ChatManager :)
 *
 * @author t3hk0d3
 * @author sirkarpfen
 */
public class ChatChannelManager extends JavaPlugin {

    public static Logger log;
    protected ChatListener listener;
    private Hashtable<Player,String> playerList = new Hashtable<Player,String>();

    @Override
    public void onEnable() {
    	log = this.getLogger();
    	
        // At first check PEX existence
        try {
            PermissionsEx.getPermissionManager();
        } catch (Throwable e) {
            log.severe("PermissionsEx not found, disabling");
            this.getPluginLoader().disablePlugin(this);
            return;
        }

        FileConfiguration config = this.getConfig();

        if (config.get("enable") == null) { // Migrate
            this.initializeConfiguration(config);
        }

        this.listener = new ChatListener(config, this);

        if (config.getBoolean("enable", false)) {
            this.getServer().getPluginManager().registerEvents(listener, this);
            log.info("ChatManager enabled!");
            // Make sure MV didn't load before we did.
            this.listener.checkForMultiverse(this.getServer().getPluginManager().getPlugin("Multiverse-Core"));
        } else {
        	log.info("ChatManager disabled. Check config.yml!");
            this.getPluginLoader().disablePlugin(this);
        }

        this.saveConfig();
    }

    @Override
    public void onDisable() {
        this.listener = null;
        
        log.info("ChatManager disabled!");
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String [] args) {
    	if(sender instanceof Player) {
    		Player player = (Player)sender;
    		String cmdName = command.getName().toLowerCase();
    		PermissionUser user = PermissionsEx.getPermissionManager().getUser(player);
    		String worldName = player.getWorld().getName();
    		
    		if(cmdName.equals("channel")) {
    			
    			if(args.length != 1) {
    				player.sendMessage(ChatColor.GRAY+"Die korrekte Schreibweise ist: "+ChatColor.WHITE+"/channel <Argumente>");
    				return false;
    			}
    			if(playerList.isEmpty()) {
    				playerList.put(player, "lokal");
    			}
    			if(!(playerList.containsKey(player))) {
					playerList.put(player, "lokal");
				}
    			
    			if(args[0].equalsIgnoreCase("global")) {
    				playerList.put(player,"global");
    				player.sendMessage(ChatColor.GRAY+"In den Channel: "+ChatColor.YELLOW+"Global "+ChatColor.GRAY+"gewechselt.");
    				return true;
    				
    			}
    		
    			if(args[0].equalsIgnoreCase("lokal")){
    				playerList.put(player, "lokal");
    				player.sendMessage(ChatColor.GRAY+"In den Channel: "+ChatColor.WHITE+"Lokal "+ChatColor.GRAY+"gewechselt.");
    				return true;
    				
    			}
    			
    			if(args[0].equalsIgnoreCase("team")){
    				if(user.has("chatmanager.chat.team", worldName)) {
    					playerList.put(player, "team");
    					player.sendMessage(ChatColor.GRAY+"In den Channel: "+ChatColor.RED+"Team "+ChatColor.GRAY+"gewechselt.");
    					return true;
    				}
    				player.sendMessage(ChatColor.RED+"You do not have the Permission to use this Command.");
					return false;
    			}
    			
    			if(args[0].equalsIgnoreCase("handel")){
    				playerList.put(player, "handel");
    				player.sendMessage(ChatColor.GRAY+"In den Channel: "+ChatColor.AQUA+"Handel "+ChatColor.GRAY+"gewechselt.");
    				return true;
    				
    			}
    		
    			if(args[0].equalsIgnoreCase("list")) {
    				
    				listener.showRecipients(player, playerList);
        			return true;
        			
    			}
    			player.sendMessage(ChatColor.GRAY+"Die korrekte Schreibweise ist: "+ChatColor.WHITE+"/channel list");
    			return false;
    		}
    	}
		return false;
    }
    
    public Hashtable<Player,String> getHashtable() {
		return playerList;
    	
    }

    protected void initializeConfiguration(FileConfiguration config) {
        // At migrate and setup defaults
        PermissionsEx pex = (PermissionsEx) this.getServer().getPluginManager().getPlugin("PermissionsEx");

        FileConfiguration pexConfig = pex.getConfig();

        // Flags
        config.set("enable", pexConfig.getBoolean("permissions.chat.enable", false));
        config.set("display-name-format", pexConfig.getString("permissions.chat.format", ChatListener.DISPLAY_NAME_FORMAT));
        config.set("local-message-format", pexConfig.getString("permissions.chat.local-format", ChatListener.LOCAL_MESSAGE_FORMAT));
        config.set("global-message-format", pexConfig.getString("permissions.chat.global-format", ChatListener.GLOBAL_MESSAGE_FORMAT));
        config.set("team-message-format", pexConfig.getString("permissions.chat.team-format", ChatListener.TEAM_MESSAGE_FORMAT));
        config.set("trade-message-format", pexConfig.getString("permissions.chat.trade-format", ChatListener.TRADE_MESSAGE_FORMAT));
        config.set("ranged-mode", pexConfig.getBoolean("permissions.chat.force-ranged", ChatListener.RANGED_MODE));
        config.set("chat-range", pexConfig.getDouble("permissions.chat.chat-range", ChatListener.CHAT_RANGE));
        
        pex.saveConfig();
    }

}
