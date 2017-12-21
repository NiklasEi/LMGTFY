package me.nikl.lmgtfy;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by nikl on 24.10.17.
 *
 * Super class for all Language classes.
 * Holds the file configurations and some basic messages.
 *
 * Provides methods to load messages from the files.
 */
public class Language {

    protected Main plugin;

    protected File languageFile;

    protected FileConfiguration defaultLanguage;
    protected FileConfiguration language;

    public String PREFIX = "["+ChatColor.DARK_AQUA+"LMGTFY"+ChatColor.RESET+"]";
    public String NAME = ChatColor.DARK_AQUA+"LMGTFY"+ChatColor.RESET;
    public String PLAIN_PREFIX = ChatColor.stripColor(PREFIX);
    public String PLAIN_NAME = ChatColor.stripColor(NAME);
    public String DEFAULT_NAME, DEFAULT_PLAIN_NAME;



    // player input
    public String CMD_MESSAGE_PRE_TEXT, CMD_MESSAGE_PRE_COLOR, CMD_MESSAGE_CLICK_TEXT
            , CMD_MESSAGE_CLICK_COLOR, CMD_MESSAGE_HOVER_TEXT, CMD_MESSAGE_HOVER_COLOR
            , CMD_MESSAGE_AFTER_TEXT, CMD_MESSAGE_AFTER_COLOR, CMD_MESSAGE_CLICK_TEXT_2
            , CMD_MESSAGE_CLICK_COLOR_2, CMD_MESSAGE_HOVER_TEXT_2, CMD_MESSAGE_HOVER_COLOR_2;

    // JSON prefix parts (click invite message)
    public String JSON_PREFIX_PRE_TEXT, JSON_PREFIX_PRE_COLOR, JSON_PREFIX_TEXT, JSON_PREFIX_COLOR
            , JSON_PREFIX_AFTER_TEXT, JSON_PREFIX_AFTER_COLOR;

    // link message
    public String CHAT_MESSAGE, CMD_SHORTENED_SUCCESS, CMD_SHORTENED_FAILED, CMD_SUCCESS;

    public String CMD_MISSING_QUERY, CMD_NO_PERM;


    public Language(Main plugin){
        this.plugin = plugin;

        getLangFile(plugin.getConfig());

        PREFIX = getString("prefix");
        NAME = getString("name");
        PLAIN_PREFIX = ChatColor.stripColor(PREFIX);
        PLAIN_NAME = ChatColor.stripColor(NAME);

        DEFAULT_NAME = ChatColor.translateAlternateColorCodes('&'
                , defaultLanguage.getString("name", "LMGTFY"));
        DEFAULT_PLAIN_NAME = ChatColor.stripColor(DEFAULT_NAME);

        loadMessages();
    }

    /**
     * Load all messages from the language file
     */
    private void loadMessages(){

        // JSON prefix

        this.JSON_PREFIX_PRE_TEXT = getString("jsonPrefix.preText");
        this.JSON_PREFIX_PRE_COLOR = getString("jsonPrefix.preColor");
        this.JSON_PREFIX_TEXT = getString("jsonPrefix.text");
        this.JSON_PREFIX_COLOR = getString("jsonPrefix.color");
        this.JSON_PREFIX_AFTER_TEXT = getString("jsonPrefix.afterText");
        this.JSON_PREFIX_AFTER_COLOR = getString("jsonPrefix.afterColor");
        getLinkMessage();

        CHAT_MESSAGE = getString("chatMessage");

        CMD_MISSING_QUERY = getString("command.noPermission");
        CMD_NO_PERM = getString("command.missingQuery");

        CMD_SHORTENED_SUCCESS = getString("command.shortenedSuccess");
        CMD_SHORTENED_FAILED = getString("command.shortenedFail");
        CMD_SUCCESS = getString("command.linkSuccess");
    }

    private void getLinkMessage() {
        // clickable invite message
        this.CMD_MESSAGE_PRE_TEXT = getString("linkMessage.preText");
        this.CMD_MESSAGE_PRE_COLOR = getString("linkMessage.preColor");
        this.CMD_MESSAGE_CLICK_TEXT = getString("linkMessage.clickText");
        this.CMD_MESSAGE_CLICK_COLOR = getString("linkMessage.clickColor");
        this.CMD_MESSAGE_HOVER_TEXT = getString("linkMessage.hoverText");
        this.CMD_MESSAGE_HOVER_COLOR = getString("linkMessage.hoverColor");
        this.CMD_MESSAGE_AFTER_TEXT = getString("linkMessage.afterText");
        this.CMD_MESSAGE_AFTER_COLOR = getString("linkMessage.afterColor");

        this.CMD_MESSAGE_CLICK_TEXT_2 = getString("linkMessage.clickText2");
        this.CMD_MESSAGE_CLICK_COLOR_2 = getString("linkMessage.clickColor2");
        this.CMD_MESSAGE_HOVER_TEXT_2 = getString("linkMessage.hoverText2");
        this.CMD_MESSAGE_HOVER_COLOR_2 = getString("linkMessage.hoverColor2");
    }

    /**
     * Try loading the language file specified in the
     * passed file configuration.
     *
     * The required set option is 'langFile'. Possible options
     * are:
     * 'default'/'default.yml': loads the english language file from inside the jar
     * 'lang_xx.yml': will try to load the given file inside the namespaces language folder
     * @param config
     */
    protected void getLangFile(FileConfiguration config) {
        // load default language
        try {
            String defaultLangName = "language/lang_en.yml";
            defaultLanguage = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(plugin.getResource(defaultLangName), "UTF-8"));
        } catch (UnsupportedEncodingException e2) {
            plugin.getLogger().warning("Failed to load default language file.");
            e2.printStackTrace();
        }

        String fileName = config.getString("langFile");

        if(fileName != null && (fileName.equalsIgnoreCase("default") || fileName.equalsIgnoreCase("default.yml"))) {
            language = defaultLanguage;
            return;
        }

        if(fileName == null || !fileName.endsWith(".yml")){
            plugin.getLogger().warning("Language file is not specified in config.");
            plugin.getLogger().warning("Falling back to the default file...");
            language = defaultLanguage;
            return;
        }

        languageFile =
                new File(plugin.getDataFolder().toString() + File.separatorChar + "language" + File.separatorChar
                        + fileName);

        if(!languageFile.exists()){
            plugin.getLogger().warning("The in config as 'langFile' configured file '" + fileName + "' does not exist!");
            plugin.getLogger().warning("Falling back to the default file...");
            language = defaultLanguage;
            return;
        }

        // File exists
        try {
            language = YamlConfiguration
                    .loadConfiguration(new InputStreamReader(new FileInputStream(languageFile)
                            , "UTF-8"));
        } catch (UnsupportedEncodingException | FileNotFoundException e) {
            e.printStackTrace();
            plugin.getLogger().warning("Language file '" + plugin.getDataFolder().toString() + File.separatorChar + "language" + File.separatorChar
                    + fileName + "' is not a valid yml.");
            plugin.getLogger().warning("Falling back to the default file...");
            language = defaultLanguage;
        }

        return;
    }


    /**
     * Find all string messages that are missing in the language file.
     *
     * This method compares all message keys that hold a String in the default english
     * file with all set keys in the used language file. All missing keys are
     * collected and returned.
     *
     * @return list of all missing keys (can be empty list)
     */
    public List<String> findMissingStringMessages(){

        List<String> toReturn = new ArrayList<>();

        if(defaultLanguage.equals(language)) return toReturn;

        for(String key : defaultLanguage.getKeys(true)){
            if(defaultLanguage.isString(key)){
                if(!language.isString(key)){
                    // there is a message missing
                    toReturn.add(key);
                }
            }
        }
        return toReturn;
    }

    /**
     * Find all string messages that are missing in the language file.
     *
     * This method compares all message keys that hold a list in the default english
     * file with all set keys in the used language file. All missing keys are
     * collected and returned.
     *
     * @return list of all missing keys (can be empty list)
     */
    public List<String> findMissingListMessages(){

        List<String> toReturn = new ArrayList<>();

        if(defaultLanguage.equals(language)) return toReturn;

        for(String key : defaultLanguage.getKeys(true)){
            if (defaultLanguage.isList(key)){
                if(!language.isList(key)){
                    // there is a list missing
                    toReturn.add(key);
                }
            }
        }
        return toReturn;
    }


    /**
     * Load list messages from the language file
     *
     * If the requested path is not valid for the chosen
     * language file the corresponding list from the default
     * file is returned.
     * ChatColor can be translated here.
     * @param path path to the message
     * @param color if set, color the loaded message
     * @return message
     */
    protected List<String> getStringList(String path, boolean color) {
        List<String> toReturn;

        // load from default file if path is not valid
        if(!language.isList(path)){
            toReturn = defaultLanguage.getStringList(path);
            if(color && toReturn != null){
                for(int i = 0; i<toReturn.size(); i++){
                    toReturn.set(i, ChatColor.translateAlternateColorCodes('&',toReturn.get(i)));
                }
            }
            return toReturn;
        }

        // load from language file
        toReturn = language.getStringList(path);
        if(color && toReturn != null) {
            for (int i = 0; i < toReturn.size(); i++) {
                toReturn.set(i, ChatColor.translateAlternateColorCodes('&', toReturn.get(i)));
            }
        }
        return toReturn;
    }

    protected List<String> getStringList(String path){
        return getStringList(path, true);
    }

    /**
     * Get a message from the language file
     *
     * If the requested path is not valid for the
     * configured language file the corresponding
     * message from the default file is returned.
     * ChatColor is translated when reading the message.
     * @param path path to the message
     * @param color if set, color the loaded message
     * @return message
     */
    protected String getString(String path, boolean color) {
        String toReturn;
        if(!language.isString(path)){
            toReturn = defaultLanguage.getString(path);
            if(color && toReturn != null){
                return ChatColor.translateAlternateColorCodes('&', defaultLanguage.getString(path));
            }
            return toReturn;
        }
        toReturn = language.getString(path);
        if(!color) return toReturn;
        return ChatColor.translateAlternateColorCodes('&',toReturn);
    }

    protected String getString(String path){
        return getString(path, true);
    }

}
