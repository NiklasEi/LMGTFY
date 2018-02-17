package me.nikl.lmgtfy;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Command for any SE that has a corresponding {@link SearchEngine}.
 *
 * @author Niklas Eicker
 */
public class LmgtfyCommand implements CommandExecutor {
    private Language lang;
    private final String clickCommand = UUID.randomUUID().toString();
    private ShorteningService shorteningService;
    private SearchEngine searchEngine;
    private boolean lmgtfy;
    private Map<UUID, Long> cooldowns = new HashMap<>();
    private Main plugin;

    LmgtfyCommand(Main plugin, SearchEngine searchEngine, boolean lmgtfy){
        this.plugin = plugin;
        this.searchEngine = searchEngine;
        reloadCommand();

        this.lmgtfy = lmgtfy;
    }

    LmgtfyCommand(Main plugin, SearchEngine searchEngine){
        this(plugin, searchEngine, false);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(!sender.hasPermission("lmgtfy.use")){
            sender.sendMessage(lang.PREFIX + lang.CMD_NO_PERM);
            return true;
        }
        if(args == null || args.length == 0){
            sender.sendMessage(lang.PREFIX + lang.CMD_MISSING_QUERY);
            return true;
        }
        // handle click on the click action...
        //    this will send the link in the chat as the issuing player
        if(args.length == 2 && args[0].equals(clickCommand)){
            if(!(sender instanceof Player)){
                // cannot happen
                return true;
            }
            if(Main.cooldown > 0 && !handleCoolDown(sender)) return true;
            Bukkit.dispatchCommand(Bukkit.getConsoleSender()
                        , "tellraw @a" + " " + createJSONChatMessage(args[1], sender.getName()));
            return true;
        }
        String query = String.join(" ", args);
        String url;
        try {
            url = getUrl(query);
        } catch (UnsupportedEncodingException e) {
            sender.sendMessage(lang.PREFIX + " Failed to create valid url...");
            return true;
        }

        if(!(sender instanceof Player)) {
            handleNonPlayerRequest(sender, url);
            return true;
        }
        handlePlayerRequest(sender, url, cmd.getName());
        return true;
    }

    void reloadCommand() {
        this.lang = plugin.getLang();
        this.shorteningService = plugin.getShorteningService();
        if(lmgtfy) searchEngine = plugin.getLmgtfyMode();
    }

    private boolean handleCoolDown(CommandSender sender) {
        Player player = (Player) sender;
        Long lastUsed;
        if((lastUsed = cooldowns.get(player.getUniqueId())) != null){
            long currentMillis = System.currentTimeMillis();
            if(currentMillis < lastUsed + Main.cooldown * 1000 && !player.hasPermission("lmgtfy.bypass")){
                long secondsTotal = ((lastUsed + Main.cooldown * 1000) - currentMillis)/1000;
                long minutes = secondsTotal / 60;
                long seconds = secondsTotal % 60;
                sender.sendMessage(lang.PREFIX + lang.CMD_COOL_DOWN
                        .replace("%minutes%", String.valueOf(minutes))
                        .replace("%seconds%", String.valueOf(seconds))
                        .replace("%mm:ss%", ((minutes > 9?String.valueOf(minutes):"0" + minutes) + ":" + (seconds > 9?String.valueOf(seconds):"0" + seconds))));
                return false;
            }
        }
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
        return true;
    }

    private String createJSONChatMessage(String url, String name) {
        return  "[{\"text\":\"" + lang.JSON_PREFIX_PRE_TEXT.replace("%link%", url) + "\",\"color\":\""
                + lang.JSON_PREFIX_PRE_COLOR + "\"},{\"text\":\"" + lang.JSON_PREFIX_TEXT + "\",\"color\":\""
                + lang.JSON_PREFIX_COLOR + "\"},{\"text\":\"" + lang.JSON_PREFIX_AFTER_TEXT + "\",\"color\":\""
                + lang.JSON_PREFIX_AFTER_COLOR + "\"}" + ",{\"text\":\"" + lang.JSON_CHAT_MESSAGE_PRE_TEXT.replace("%player%", name) + "\",\"color\":\""
                + lang.JSON_CHAT_MESSAGE_PRE_COLOR + "\"},"
                + "{\"text\":\"" + lang.JSON_CHAT_MESSAGE_PRE_TEXT_2.replace("%player%", name) + "\",\"color\":\""
                + lang.JSON_CHAT_MESSAGE_PRE_COLOR_2 + "\"},"
                +"{\"text\":\"" + lang.JSON_CHAT_MESSAGE_CLICK_TEXT.replace("%player%", name) + "\",\"color\":\""
                + lang.JSON_CHAT_MESSAGE_CLICK_COLOR + "\",\"bold\":" + true + ",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"" + url
                + "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"" + lang.JSON_CHAT_MESSAGE_HOVER_TEXT.replace("%player%", name) + "\",\"color\":\""
                + lang.JSON_CHAT_MESSAGE_HOVER_COLOR +"\"}}},"
                + "{\"text\":\"" + lang.JSON_CHAT_MESSAGE_AFTER_TEXT.replace("%player%", name) + "\",\"color\":\""
                + lang.JSON_CHAT_MESSAGE_AFTER_COLOR + "\"}]";
    }

    private void handleNonPlayerRequest(CommandSender sender, String url) {
        if(Main.useShorteningService) {
            shorteningService.shortenAsync(url, new ShorteningService.Callable<String>() {
                // called async!
                @Override
                public void success(String s) {
                    sender.sendMessage(lang.PREFIX + lang.CMD_SHORTENED_SUCCESS);
                    sender.sendMessage("Link: " + s);
                }

                // called async!
                @Override
                public void fail(String s) {
                    sender.sendMessage(lang.PREFIX + lang.CMD_SHORTENED_FAILED);
                    sender.sendMessage("Link: " + url);
                }
            });
        } else {
            sender.sendMessage(lang.PREFIX + lang.CMD_SUCCESS);
            sender.sendMessage("Link: " + url);
        }
    }

    private void handlePlayerRequest(CommandSender sender, String url, String commandName) {
        // ToDo: I don't like the workaround with tellraw, but sadly bukkit has no other way without packages.
        //       For Spigot one could use SpigotPlayer and send the JSON without a command.

        if(Main.useShorteningService) {
            shorteningService.shortenAsync(url, new ShorteningService.Callable<String>() {
                // called async!
                @Override
                public void success(String shortenedLink) {
                    sender.sendMessage(lang.PREFIX + lang.CMD_SHORTENED_SUCCESS);
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender()
                            , "tellraw " + sender.getName() + " " + createJSON(shortenedLink, commandName));
                }

                // called async!
                @Override
                public void fail(String s) {
                    sender.sendMessage(lang.PREFIX + lang.CMD_SHORTENED_FAILED);
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender()
                            , "tellraw " + sender.getName() + " " + createJSON(url, commandName));
                }
            });
        } else {
            sender.sendMessage(lang.PREFIX + lang.CMD_SUCCESS);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender()
                    , "tellraw " + sender.getName() + " " + createJSON(url, commandName));
        }
    }

    private String getUrl(String query) throws UnsupportedEncodingException {
        if(lmgtfy) {
            return "https://lmgtfy.com/?" + searchEngine.getLmgtfyMode() + "q=" + URLEncoder.encode(query, "UTF-8");
        }
        return searchEngine.getSearchLink() + URLEncoder.encode(query, "UTF-8");
    }

    /**
     * Creates a JSON string that is ready to be send to a player per 'tellraw'
     *
     * @param url link to send
     * @param cmd command
     * @return JSON string
     */
    private String createJSON(String url, String cmd){
        boolean boldClick = true;
        String secondClick = "{\"text\":\"" + lang.CMD_MESSAGE_CLICK_TEXT_2.replace("%link%", url) + "\",\"color\":\""
                + lang.CMD_MESSAGE_CLICK_COLOR_2 + "\",\"bold\":" + boldClick + ",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"" + url
                + "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"" + lang.CMD_MESSAGE_HOVER_TEXT_2.replace("%link%", url) + "\",\"color\":\""
                + lang.CMD_MESSAGE_HOVER_COLOR_2 +"\"}}},";
        return "[{\"text\":\"" + lang.JSON_PREFIX_PRE_TEXT.replace("%link%", url) + "\",\"color\":\""
                + lang.JSON_PREFIX_PRE_COLOR + "\"},{\"text\":\"" + lang.JSON_PREFIX_TEXT + "\",\"color\":\""
                + lang.JSON_PREFIX_COLOR + "\"},{\"text\":\"" + lang.JSON_PREFIX_AFTER_TEXT + "\",\"color\":\""
                + lang.JSON_PREFIX_AFTER_COLOR + "\"}"
                + ",{\"text\":\"" + lang.CMD_MESSAGE_PRE_TEXT.replace("%link%", url) + "\",\"color\":\""
                + lang.CMD_MESSAGE_PRE_COLOR + "\"},{\"text\":\""
                + lang.CMD_MESSAGE_CLICK_TEXT.replace("%link%", url) + "\",\"color\":\""
                + lang.CMD_MESSAGE_CLICK_COLOR + "\",\"bold\":" + boldClick
                + ",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/" + cmd + " "
                + clickCommand + " " + url
                + "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\""
                + lang.CMD_MESSAGE_HOVER_TEXT.replace("%link%", url) + "\",\"color\":\""
                + lang.CMD_MESSAGE_HOVER_COLOR + "\"}}}, " + secondClick +  " {\"text\":\""
                + lang.CMD_MESSAGE_AFTER_TEXT.replace("%link%", url) + "\",\"color\":\""
                + lang.CMD_MESSAGE_AFTER_COLOR + "\"}]";
    }
}
