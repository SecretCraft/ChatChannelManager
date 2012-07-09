package com.github.sirkarpfen.chatchannel;

import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import ru.tehkode.permissions.bukkit.PermissionsEx;

/** Credits to t3hk0d3 for providing the basic ChatManager :)
 *
 * @author t3hk0d3
 * @author sirkarpfen
 */
public class ChatChannelManager extends JavaPlugin {

    protected static Logger log;
    protected ChatListener listener;
    static boolean switchGlobal = false; 
    static boolean switchLocal = true;
    static boolean switchTeam = false;
    static boolean switchTrade = false;

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

        this.listener = new ChatListener(config);

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
    			
    		if(cmdName.equals("global")) {
    			if(args.length != 0) {
    				player.sendMessage(ChatColor.GRAY+"Die korrekte Schreibweise ist: "+ChatColor.WHITE+"/global");
    				return false;
    			}
    				
    			switchGlobal = true;
    			switchLocal = false;
    			switchTeam = false;
    			switchTrade = false;
    			listener.addRecipient(player);
    			player.sendMessage("Global: "+switchGlobal+" Lokal: "+switchLocal+" Team: "+switchTeam+" Handel: "+switchTrade);
    			return true;
    				
    		}
    		
    		if(cmdName.equals("lokal")){
    			if(args.length != 0) {
    				player.sendMessage(ChatColor.GRAY+"Die korrekte Schreibweise ist: "+ChatColor.WHITE+"/lokal");
    				return false;
    			}
    				
    			switchGlobal = false;
    			switchLocal = true;
    			switchTeam = false;
    			switchTrade = false;
    			listener.addRecipient(player);
    			player.sendMessage("Global: "+switchGlobal+" Lokal: "+switchLocal+" Team: "+switchTeam+" Handel: "+switchTrade);
    			return true;
    				
    		}
    			
    		if(cmdName.equals("team")){
    			if(args.length != 0) {
    				player.sendMessage(ChatColor.GRAY+"Die korrekte Schreibweise ist: "+ChatColor.WHITE+"/team");
    				return false;
    			}
    				
    			switchGlobal = false;
    			switchLocal = false;
    			switchTeam = true;
    			switchTrade = false;
    			listener.addRecipient(player);
    			player.sendMessage("Global: "+switchGlobal+" Lokal: "+switchLocal+" Team: "+switchTeam+" Handel: "+switchTrade);
    				return true;
    				
    		}
    			
    		if(cmdName.equals("handel")){
    			if(args.length != 0) {
    				player.sendMessage(ChatColor.GRAY+"Die korrekte Schreibweise ist: "+ChatColor.WHITE+"/handel");
    				return false;
    			}
    				
    			switchGlobal = false;
    			switchLocal = false;
    			switchTeam = false;
    			switchTrade = true;
    			listener.addRecipient(player);
    			player.sendMessage("Global: "+switchGlobal+" Lokal: "+switchLocal+" Team: "+switchTeam+" Handel: "+switchTrade);
    			return true;
    				
    		}
    		
    		if(cmdName.equals("channel")) {
    			if(args[0].equalsIgnoreCase("list")) {
    				if(args.length != 1) {
        				player.sendMessage(ChatColor.GRAY+"Die korrekte Schreibweise ist: "+ChatColor.WHITE+"/channel list");
        				return false;
        			}
    				listener.showRecipients(player);
        			return true;
    			}
    			player.sendMessage(ChatColor.GRAY+"Die korrekte Schreibweise ist: "+ChatColor.WHITE+"/channel list");
    			return false;
    		}
    	}
		return false;
    }

    protected void initializeConfiguration(FileConfiguration config) {
        // At migrate and setup defaults
        PermissionsEx pex = (PermissionsEx) this.getServer().getPluginManager().getPlugin("PermissionsEx");

        FileConfiguration pexConfig = pex.getConfig();

        // Flags
        config.set("enable", pexConfig.getBoolean("permissions.chat.enable", false));
        config.set("message-format", pexConfig.getString("permissions.chat.format", ChatListener.MESSAGE_FORMAT));
        config.set("global-message-format", pexConfig.getString("permissions.chat.global-format", ChatListener.GLOBAL_MESSAGE_FORMAT));
        config.set("team-message-format", pexConfig.getString("permissions.chat.team-format", ChatListener.TEAM_MESSAGE_FORMAT));
        config.set("trade-message-format", pexConfig.getString("permissions.chat.trade-format", ChatListener.TRADE_MESSAGE_FORMAT));
        config.set("ranged-mode", pexConfig.getBoolean("permissions.chat.force-ranged", ChatListener.RANGED_MODE));
        config.set("chat-range", pexConfig.getDouble("permissions.chat.chat-range", ChatListener.CHAT_RANGE));
        
        pex.saveConfig();
    }

}
