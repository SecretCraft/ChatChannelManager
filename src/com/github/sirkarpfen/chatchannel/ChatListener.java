/*
 * ChatManager - PermissionsEx Chat management plugin for Bukkit
 * Copyright (C) 2011 t3hk0d3 http://www.tehkode.ru
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.github.sirkarpfen.chatchannel;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;

import com.onarandombox.MultiverseCore.MultiverseCore;

import com.github.sirkarpfen.chatchannel.utils.MultiverseConnector;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

/** Credits to t3hk0d3 for providing the basic ChatManager
 *
 * @author t3hk0d3
 * @author sirkarpfen
 */
public class ChatListener implements Listener {
	protected static Pattern chatColorPattern = Pattern.compile("(?i)&([0-9A-F])");
	protected static Pattern chatMagicPattern = Pattern.compile("(?i)&([K])");
	protected static Pattern chatBoldPattern = Pattern.compile("(?i)&([L])");
	protected static Pattern chatStrikethroughPattern = Pattern.compile("(?i)&([M])");
	protected static Pattern chatUnderlinePattern = Pattern.compile("(?i)&([N])");
	protected static Pattern chatItalicPattern = Pattern.compile("(?i)&([O])");
	protected static Pattern chatResetPattern = Pattern.compile("(?i)&([R])");
	
	public final static String DISPLAY_NAME_FORMAT = "%prefix%player%suffix";
	public final static String LOCAL_MESSAGE_FORMAT = "&f%message";
	public final static String GLOBAL_MESSAGE_FORMAT = "&e%message";
	public final static String TEAM_MESSAGE_FORMAT = "&c%message";
	public final static String TRADE_MESSAGE_FORMAT = "&b%message";
	public final static Boolean RANGED_MODE = false;
	public final static double CHAT_RANGE = 100d;
	protected String displayMessageFormat = DISPLAY_NAME_FORMAT;
	protected String localMessageFormat = LOCAL_MESSAGE_FORMAT;
	protected String globalMessageFormat = GLOBAL_MESSAGE_FORMAT;
	protected String teamMessageFormat = TEAM_MESSAGE_FORMAT;
	protected String tradeMessageFormat = TRADE_MESSAGE_FORMAT;
	protected boolean rangedMode = RANGED_MODE;
	protected double chatRange = CHAT_RANGE;
	protected double range;
	protected String optionChatRange = "chat-range";
	protected String optionDisplayFormat = "display-name-format";
	protected String optionLocalMessageFormat = "local-message-format";
	protected String optionGlobalMessageFormat = "global-message-format";
	protected String optionTeamMessageFormat = "team-message-format";
	protected String optionTradeMessageFormat = "trade-message-format";
	protected String optionRangedMode = "force-ranged-mode";
	protected String optionDisplayname = "display-name-format";
	protected String permissionChatColor = "chatmanager.chat.color";
	protected String permissionChatMagic = "chatmanager.chat.magic";
	protected String permissionChatBold = "chatmanager.chat.bold";
	protected String permissionChatStrikethrough = "chatmanager.chat.strikethrough";
	protected String permissionChatUnderline = "chatmanager.chat.underline";
	protected String permissionChatItalic = "chatmanager.chat.italic";
	private MultiverseConnector multiverseConnector;
	private List<Player> playerGlobalList = new LinkedList<Player>();
	private List<Player> playerTeamList = new LinkedList<Player>();
	private List<Player> playerTradeList = new LinkedList<Player>();
	private List<Player> playerLocalList = new LinkedList<Player>();
			
	public ChatListener(FileConfiguration config) {
		this.displayMessageFormat = config.getString("diplay-name-format", this.displayMessageFormat);
		this.localMessageFormat = config.getString("local-message-format", this.localMessageFormat);
		this.globalMessageFormat = config.getString("global-message-format", this.globalMessageFormat);
		this.teamMessageFormat = config.getString("team-message-format", this.teamMessageFormat);
		this.tradeMessageFormat = config.getString("trade-message-format", this.tradeMessageFormat);
		this.rangedMode = config.getBoolean("ranged-mode", this.rangedMode);
		this.chatRange = config.getDouble("chat-range", this.chatRange);
	}

	@EventHandler
	public void onPlayerChat(PlayerChatEvent event) {
		if (event.isCancelled()) {
			return;
		}

		Player player = event.getPlayer();
		
		String worldName = player.getWorld().getName();
		
		PermissionUser user = PermissionsEx.getPermissionManager().getUser(player);
		if (user == null) {
			return;
		}
		String displayNameFormat = user.getOption(this.optionDisplayFormat, worldName, displayMessageFormat);
		String message = displayNameFormat + " " + user.getOption(this.optionLocalMessageFormat, worldName, localMessageFormat);
		boolean localChat = user.getOptionBoolean(this.optionRangedMode, worldName, rangedMode);

		String chatMessage = event.getMessage();
		if (ChatChannelManager.switchGlobal) {
			localChat = false;
			
			message = displayNameFormat + " " + user.getOption(this.optionGlobalMessageFormat, worldName, globalMessageFormat);
		}
		
		if (ChatChannelManager.switchTeam && user.has("chatmanager.chat.team", worldName)) {
			localChat = false;
			
			message = displayNameFormat + " " + user.getOption(this.optionTeamMessageFormat, worldName, teamMessageFormat);
			
			event.getRecipients().clear();
			event.getRecipients().addAll(playerTeamList);
		}
		
		if (ChatChannelManager.switchTrade) {
			localChat = false;
			
			message = displayNameFormat + " " + user.getOption(this.optionTradeMessageFormat, worldName, tradeMessageFormat);
			
			event.getRecipients().clear();
			event.getRecipients().addAll(playerTradeList);
		}

		message = this.translateColorCodes(message);

		chatMessage = this.translateColorCodes(chatMessage, user, worldName);

		message = message.replace("%message", "%2$s").replace("%displayname", "%1$s");
		message = this.replacePlayerPlaceholders(player, message);
		message = this.replaceTime(message);

		event.setFormat(message);
		event.setMessage(chatMessage);

		if (localChat && ChatChannelManager.switchLocal) {
			range = user.getOptionDouble(this.optionChatRange, worldName, chatRange);

			event.getRecipients().clear();
			playerLocalList = this.getLocalRecipients(player, message, range);
			event.getRecipients().addAll(playerLocalList);
		}
	}

	protected void updateDisplayNames() {
		for (Player player : Bukkit.getServer().getOnlinePlayers()) {
			updateDisplayName(player);
		}
	}

	protected void updateDisplayName(Player player) {
		PermissionUser user = PermissionsEx.getPermissionManager().getUser(player);
		if (user == null) {
			return;
		}

		//String worldName = player.getWorld().getName();
		//player.setDisplayName(this.translateColorCodes(this.replacePlayerPlaceholders(player, user.getOption(this.optionDisplayname, worldName, this.displayNameFormat))));
	}

	protected String replacePlayerPlaceholders(Player player, String format) {
		PermissionUser user = PermissionsEx.getPermissionManager().getUser(player);
		String worldName = player.getWorld().getName();
		return format.replace("%prefix", this.translateColorCodes(user.getPrefix(worldName))).replace("%suffix", this.translateColorCodes(user.getSuffix(worldName))).replace("%world", this.getWorldAlias(worldName)).replace("%player", player.getName());
	}
	
	protected void addRecipient(Player player) {
		if(ChatChannelManager.switchTrade) {
			playerTradeList.add(player);
			if(playerTeamList.contains(player)) {
				playerTeamList.remove(player);
			}
			if(playerGlobalList.contains(player)) {
				playerGlobalList.remove(player);
			}
		}
		if(ChatChannelManager.switchTeam){
			playerTeamList.add(player);
			if(playerTradeList.contains(player)) {
				playerTradeList.remove(player);
			}
			if(playerGlobalList.contains(player)) {
				playerGlobalList.remove(player);
			}
		}
		if(ChatChannelManager.switchGlobal) {
			playerGlobalList.add(player);
			if(playerTradeList.contains(player)) {
				playerTradeList.remove(player);
			}
			if(playerTeamList.contains(player)) {
				playerTeamList.remove(player);
			}
		}
		if(ChatChannelManager.switchLocal) {
			if(playerGlobalList.contains(player)) {
				playerGlobalList.remove(player);
			}
			if(playerTradeList.contains(player)) {
				playerTradeList.remove(player);
			}
			if(playerTeamList.contains(player)) {
				playerTeamList.remove(player);
			}
		}
	}
	
	protected void showRecipients(Player player) {
		ChatColor gray = ChatColor.GRAY;
		ChatColor red = ChatColor.RED;
		ChatColor aqua = ChatColor.AQUA;
		ChatColor yellow = ChatColor.YELLOW;
		ChatColor white = ChatColor.WHITE;
		if(ChatChannelManager.switchGlobal) {
			player.sendMessage(gray+"Channel: "+yellow+"Global"+gray+","+white+" "+playerGlobalList.size()+" "+gray+"Spieler online");
		}
		if(ChatChannelManager.switchTeam) {
			player.sendMessage(gray+"Channel: "+red+"Team"+gray+","+white+" "+playerTeamList.size()+" "+gray+"Spieler online");
		}
		if(ChatChannelManager.switchTrade) {
			player.sendMessage(gray+"Channel: "+aqua+"Handel"+gray+","+white+" "+playerTradeList.size()+" "+gray+"Spieler online");
		}
		if(ChatChannelManager.switchLocal) {
			player.sendMessage(gray+"Channel: "+white+"Lokal"+gray+","+white+" "+getLocalRecipients(player, range).size()+" "+gray+"Spieler im Umkreis");
		}
	}

	protected List<Player> getLocalRecipients(Player sender, String message, double range) {
		Location playerLocation = sender.getLocation();
		List<Player> recipients = new LinkedList<Player>();
		double squaredDistance = Math.pow(range, 2);
		PermissionManager manager = PermissionsEx.getPermissionManager();
		for (Player recipient : Bukkit.getServer().getOnlinePlayers()) {
			// Recipient are not from same world
			if (!recipient.getWorld().equals(sender.getWorld())) {
				continue;
			}

			if (playerLocation.distanceSquared(recipient.getLocation()) > squaredDistance && !manager.has(sender, "chatmanager.override.ranged")) {
				continue;
			}

			recipients.add(recipient);
		}
		return recipients;
	}
	
	protected List<Player> getLocalRecipients(Player sender, double range) {
		Location playerLocation = sender.getLocation();
		List<Player> recipients = new LinkedList<Player>();
		double squaredDistance = Math.pow(range, 2);
		PermissionManager manager = PermissionsEx.getPermissionManager();
		for (Player recipient : Bukkit.getServer().getOnlinePlayers()) {
			// Recipient are not from same world
			if (!recipient.getWorld().equals(sender.getWorld())) {
				continue;
			}

			if (playerLocation.distanceSquared(recipient.getLocation()) > squaredDistance && !manager.has(sender, "chatmanager.override.ranged")) {
				continue;
			}

			recipients.add(recipient);
		}
		return recipients;
	}

	protected String replaceTime(String message) {
		Calendar calendar = Calendar.getInstance();

		if (message.contains("%h")) {
			message = message.replace("%h", String.format("%02d", calendar.get(Calendar.HOUR)));
		}

		if (message.contains("%H")) {
			message = message.replace("%H", String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)));
		}

		if (message.contains("%g")) {
			message = message.replace("%g", Integer.toString(calendar.get(Calendar.HOUR)));
		}

		if (message.contains("%G")) {
			message = message.replace("%G", Integer.toString(calendar.get(Calendar.HOUR_OF_DAY)));
		}

		if (message.contains("%i")) {
			message = message.replace("%i", String.format("%02d", calendar.get(Calendar.MINUTE)));
		}

		if (message.contains("%s")) {
			message = message.replace("%s", String.format("%02d", calendar.get(Calendar.SECOND)));
		}

		if (message.contains("%a")) {
			message = message.replace("%a", (calendar.get(Calendar.AM_PM) == 0) ? "am" : "pm");
		}

		if (message.contains("%A")) {
			message = message.replace("%A", (calendar.get(Calendar.AM_PM) == 0) ? "AM" : "PM");
		}

		return message;
	}

	protected String translateColorCodes(String string) {
		if (string == null) {
			return "";
		}

		String newstring = string;
		newstring = chatColorPattern.matcher(newstring).replaceAll("\u00A7$1");
		newstring = chatMagicPattern.matcher(newstring).replaceAll("\u00A7$1");
		newstring = chatBoldPattern.matcher(newstring).replaceAll("\u00A7$1");
		newstring = chatStrikethroughPattern.matcher(newstring).replaceAll("\u00A7$1");
		newstring = chatUnderlinePattern.matcher(newstring).replaceAll("\u00A7$1");
		newstring = chatItalicPattern.matcher(newstring).replaceAll("\u00A7$1");
		newstring = chatResetPattern.matcher(newstring).replaceAll("\u00A7$1");
		return newstring;
	}

	protected String translateColorCodes(String string, PermissionUser user, String worldName) {
		if (string == null) {
			return "";
		}

		String newstring = string;
		if (user.has(permissionChatColor, worldName)) {
			newstring = chatColorPattern.matcher(newstring).replaceAll("\u00A7$1");
		}
		if (user.has(permissionChatMagic, worldName)) {
			newstring = chatMagicPattern.matcher(newstring).replaceAll("\u00A7$1");
		}
		if (user.has(permissionChatBold, worldName)) {
			newstring = chatBoldPattern.matcher(newstring).replaceAll("\u00A7$1");
		}
		if (user.has(permissionChatStrikethrough, worldName)) {
			newstring = chatStrikethroughPattern.matcher(newstring).replaceAll("\u00A7$1");
		}
		if (user.has(permissionChatUnderline, worldName)) {
			newstring = chatUnderlinePattern.matcher(newstring).replaceAll("\u00A7$1");
		}
		if (user.has(permissionChatItalic, worldName)) {
			newstring = chatItalicPattern.matcher(newstring).replaceAll("\u00A7$1");
		}
		newstring = chatResetPattern.matcher(newstring).replaceAll("\u00A7$1");
		return newstring;
	}

    /**
     * Initializes the MVConnector.
     *
     * @param conn The MultiverseConnector instance
     */
    protected void setupMultiverseConnector(MultiverseConnector conn) {
        this.multiverseConnector = conn;
    }
    
    /**
     * Returns a colored world string provided by Multiverse
     *
     * @param world The world to retrieve the string about.
     * @return A colored worldstring if the connector is present, the normal world if it is not.
     */
    private String getWorldAlias(String world) {
        if (this.multiverseConnector != null) {
            return multiverseConnector.getColoredAliasForWorld(world);
        }
        return world;
    }
    
    @EventHandler
    public void onPluginEnable(PluginEnableEvent event) {
        this.checkForMultiverse(event.getPlugin());
    }
    
    public void checkForMultiverse(Plugin p) {
        if (p != null && p.getDescription().getName().equalsIgnoreCase("Multiverse-Core")) {
            this.setupMultiverseConnector(new MultiverseConnector((MultiverseCore) p));
            ChatChannelManager.log.info("Multiverse 2 integration enabled!");
        }
    }
}
