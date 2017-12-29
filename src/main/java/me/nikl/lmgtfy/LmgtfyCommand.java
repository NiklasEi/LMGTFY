package me.nikl.lmgtfy;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.UUID;

/**
 * Created by nikl on 19.12.17.
 *
 */
public class LmgtfyCommand implements CommandExecutor {

    private Language lang;
    private final String clickCommand = UUID.randomUUID().toString();
    private Shortener shortener;

    private Mode mode;

    private boolean lmgtfy;

    public LmgtfyCommand(Main plugin, Mode mode, boolean lmgtfy){
        this.lang = plugin.getLang();
        this.mode = mode;
        this.shortener = plugin.getShortener();

        this.lmgtfy = lmgtfy;
    }

    public LmgtfyCommand(Main plugin, Mode mode){
        this(plugin, mode, false);
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
            ((Player) sender).chat(lang.CHAT_MESSAGE.replace("%link%", args[1]));
            return true;
        }

        String query = String.join(" ", args);

        String url;
        try {
            if(lmgtfy){
                url = "https://lmgtfy.com/?" + mode.getLmgtfyMode() + "q=" + URLEncoder.encode(query, "UTF-8");
            } else {
                switch (mode) {
                    case GOOGLE:
                        url = "https://www.google.com/search?q=" + URLEncoder.encode(query, "UTF-8");
                        break;

                    case BING:
                        url = "https://www.bing.com/search?q=" + URLEncoder.encode(query, "UTF-8");
                        break;

                    case YAHOO:
                        url = "https://search.yahoo.com/search?p=" + URLEncoder.encode(query, "UTF-8");
                        break;

                    case DUCKDUCKGO:
                        url = "https://duckduckgo.com/?q=" + URLEncoder.encode(query, "UTF-8");
                        break;

                    case BAIDU:
                        url = "https://www.baidu.com/s?word=" + URLEncoder.encode(query, "UTF-8");
                        break;

                    case YANDEX:
                        url = "https://www.yandex.ru/search/?text=" + URLEncoder.encode(query, "UTF-8");
                        break;

                    default:
                        return true;
                }
            }
        } catch (UnsupportedEncodingException e) {
            sender.sendMessage(lang.PREFIX + " Failed to create valid url...");
            return true;
        }

        if(!(sender instanceof Player)) {
            sender.sendMessage(lang.PREFIX + " " + url);
            return true;
        }

        // ToDo: I don't like the workaround with tellraw, but sadly bukkit has no other way without packages.
        //       For Spigot one could use SpigotPlayer and send the JSON without a command.

        if(Main.useShortener) {
            shortener.shortenAsync(url, new Shortener.Callable<String>() {
                // called async!
                @Override
                public void success(String s) {
                    sender.sendMessage(lang.PREFIX + lang.CMD_SHORTENED_SUCCESS);
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender()
                            , "tellraw " + createJSON(s, sender.getName(), cmd.getName()));
                }

                // called async!
                @Override
                public void fail(String s) {
                    sender.sendMessage(lang.PREFIX + lang.CMD_SHORTENED_FAILED);
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender()
                            , "tellraw " + createJSON(s, sender.getName(), cmd.getName()));
                }
            });
        } else {
            sender.sendMessage(lang.PREFIX + lang.CMD_SUCCESS);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender()
                    , "tellraw " + createJSON(url, sender.getName(), cmd.getName()));
        }
        return true;
    }

    /**
     * Creates a JSON string that is ready to be send to a player per 'tellraw'
     *
     * @param url link to send
     * @param name player
     * @return JSON string
     */
    private String createJSON(String url, String name, String cmd){
        boolean boldClick = true;

        String secondClick = "{\"text\":\"" + lang.CMD_MESSAGE_CLICK_TEXT_2.replace("%link%", url) + "\",\"color\":\""
                + lang.CMD_MESSAGE_CLICK_COLOR_2 + "\",\"bold\":" + boldClick + ",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"" + url
                + "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"" + lang.CMD_MESSAGE_HOVER_TEXT_2.replace("%link%", url) + "\",\"color\":\""
                + lang.CMD_MESSAGE_HOVER_COLOR_2 +"\"}}},";

        return name
                + " [{\"text\":\"" + lang.JSON_PREFIX_PRE_TEXT.replace("%link%", url) + "\",\"color\":\""
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
