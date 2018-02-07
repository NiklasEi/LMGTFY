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

    static boolean useShorteningService = true;

    private Language lang;
    private FileConfiguration config;
    private ShorteningService shorteningService;

    private SearchEngine lmgtfyMode;

    @Override
    public void onEnable(){
        if (!reload()) {
            getLogger().severe(" Problem while loading the plugin! Plugin was disabled!");

            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        setUpCommands();
        setUpMetrics();
    }

    boolean reload() {
        if(!reloadConfiguration()){
            getLogger().severe(" Failed to load config file!");
            return false;
        }
        useShorteningService = config.getBoolean("useShorteningService", true);
        if(shorteningService == null) this.shorteningService = new ShorteningService(this);
        if(lang == null) this.lang = new Language(this);
        FileUtil.copyDefaultLanguageFiles();
        lang.reload();
        shorteningService.reload();
        setLMGTFYMode();
        return true;
    }

    private void setUpCommands() {
        this.getCommand("lmgtfyreload").setExecutor(new ReloadCommand(this));
        this.getCommand("lmgtfy").setExecutor(new LmgtfyCommand(this, lmgtfyMode, true));
        for(SearchEngine mode : SearchEngine.values()) {
            this.getCommand(mode.getCommand()).setExecutor(new LmgtfyCommand(this, mode));
        }
    }

    private void setUpMetrics() {
        if(config.getBoolean("bStats", true)){
            Metrics metrics = new Metrics(this);
            // Pie chart with the lmgtfy mode
            metrics.addCustomChart(new Metrics.SimplePie("lmgtfy_mode"
                    , () -> String.valueOf(lmgtfyMode.toString().toLowerCase())));
        }
    }

    private void setLMGTFYMode() {
        try {
            lmgtfyMode = SearchEngine.valueOf(config.getString("lmgtfyMode", "google").toUpperCase());
        } catch (IllegalArgumentException exception){
            setDefaultLMGTFYMode();
        }
        if(lmgtfyMode.getLmgtfyMode() == null){
            setDefaultLMGTFYMode();
        }
    }

    private void setDefaultLMGTFYMode() {
        StringBuilder list = new StringBuilder();
        list.append(SearchEngine.GOOGLE.toString());
        for (SearchEngine mode : SearchEngine.values()) {
            // for google the mode is empty
            if(mode.getLmgtfyMode() != null && !mode.getLmgtfyMode().isEmpty()){
                list.append(", ").append(mode.toString());
            }
        }
        getLogger().info("Invalid mode for lmgtfy. Falling back to default: GOOGLE");
        getLogger().info("Valid options: " + list.toString());
        lmgtfyMode = SearchEngine.GOOGLE;
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

    ShorteningService getShorteningService() {
        return shorteningService;
    }
}
