package me.nikl.lmgtfy;

import me.nikl.lmgtfy.util.FileUtil;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

/**
 * Created by nikl on 19.12.17.
 *
 * Let me google that for you...
 */
public class Main extends JavaPlugin {

    static boolean useShortener = true;

    private Language lang;
    private FileConfiguration config;
    private Shortener shortener;

    private Mode lmgtfyMode;

    @Override
    public void onEnable(){

        getLogger().info("LMGTFY Has been loaded to the server!");
        getLogger().info("Has support for custom short link interface!");
        getLogger().info("Custom search engine already supported!");
        
        if (!reload()) {
            getLogger().severe(" Problem while loading the plugin! Plugin was disabled!");

            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
    }

    private boolean reload() {

        if(!reloadConfiguration()){
            getLogger().severe(" Failed to load config file!");
            return false;
        }

        useShortener = config.getBoolean("useShortener", true);

        shortener = new Shortener(this);
        // save default language files form jar
        FileUtil.copyDefaultLanguageFiles();

        // get language file
        this.lang = new Language(this);

        shortener = new Shortener(this);

        try {
            lmgtfyMode = Mode.valueOf(config.getString("lmgtfyMode", "google").toUpperCase());
        } catch (IllegalArgumentException exception){
            setDefaultLMGTFYMode();
        }

        if(lmgtfyMode.getLmgtfyMode() == null){
            setDefaultLMGTFYMode();
        }

        this.getCommand("lmgtfy").setExecutor(new LmgtfyCommand(this, lmgtfyMode, true));

        for(Mode mode : Mode.values()) {
            this.getCommand(mode.getCommand()).setExecutor(new LmgtfyCommand(this, mode));
        }

        if(config.getBoolean("bStats", true)){
            Metrics metrics = new Metrics(this);

            // Pie chart with the lmgtfy mode
            metrics.addCustomChart(new Metrics.SimplePie("lmgtfy_mode"
                    , () -> String.valueOf(lmgtfyMode.toString().toLowerCase())));
        }

        return true;
    }

    private void setDefaultLMGTFYMode() {
        StringBuilder list = new StringBuilder();
        list.append(Mode.GOOGLE.toString());
        for (Mode mode : Mode.values()) {
            // for google the mode is empty
            if(mode.getLmgtfyMode() != null && !mode.getLmgtfyMode().isEmpty()){
                list.append(", ").append(mode.toString());
            }
        }

        getLogger().info("Invalid mode for lmgtfy. Falling back to default: GOOGLE");
        getLogger().info("Valid options: " + list.toString());
        lmgtfyMode = Mode.GOOGLE;
    }

    private boolean reloadConfiguration(){

        // save the default configuration file if the file does not exist
        File con = new File(this.getDataFolder().toString() + File.separatorChar + "config.yml");
        if(!con.exists()){
            this.saveResource("config.yml", false);
        }

        // reload config
        try {
            this.config = YamlConfiguration.loadConfiguration(new InputStreamReader(new FileInputStream(con), "UTF-8"));
        } catch (UnsupportedEncodingException | FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    Language getLang() {
        return lang;
    }

    @Override
    public FileConfiguration getConfig() {
        return config;
    }

    Shortener getShortener() {
        return shortener;
    }
}
