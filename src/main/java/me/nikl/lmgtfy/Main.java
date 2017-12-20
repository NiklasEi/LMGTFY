package me.nikl.lmgtfy;

import me.nikl.lmgtfy.util.FileUtil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;

/**
 * Created by nikl on 19.12.17.
 *
 * Let me google that for you...
 */
public class Main extends JavaPlugin {
    private Language lang;
    private FileConfiguration config;

    @Override
    public void onEnable() {
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

        // save default language files form jar
        FileUtil.copyDefaultLanguageFiles();

        // get gamebox language file
        this.lang = new Language(this);

        this.getCommand("lmgtfy").setExecutor(new LmgtfyCommand(this));

        return true;
    }

    @Override
    public void onDisable(){

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

    public Language getLang() {
        return lang;
    }

    @Override
    public FileConfiguration getConfig() {
        return config;
    }

    public interface Callback<T> {
        void success(T t);
        void fail();
    }
}
