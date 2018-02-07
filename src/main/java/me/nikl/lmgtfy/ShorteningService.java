package me.nikl.lmgtfy;

import org.bukkit.scheduler.BukkitRunnable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Send requests to a shortening service and catch the shortened link.
 *
 * @author Niklas Eicker
 */
public class ShorteningService {
    private Main plugin;
    private String shorteningServiceURL = "https://is.gd/api.php?longurl=";

    ShorteningService(Main plugin){
        this.plugin = plugin;
    }

    void reload(){
        this.shorteningServiceURL = plugin.getConfig().getString("shortener", "https://is.gd/api.php?longurl=");
        runShorteningTest();
    }

    void shortenAsync(String link, Callable<String> callable){
        new Lookup(link, callable).runTaskAsynchronously(plugin);
    }

    private void runShorteningTest() {
        Callable<String> test = new Callable<String>() {
            @Override
            public void success(String s) {
                // everything is fine...
            }

            @Override
            public void fail(String s) {
                plugin.getLogger().warning(" A test with the configured shortening service failed!");
                plugin.getLogger().warning("   Used service: " + shorteningServiceURL);
                plugin.getLogger().warning("   Either your rate limit with this service is used up,");
                plugin.getLogger().warning("   or the configured URL is not valid.");
            }
        };
        new Lookup("https://www.nikl.me/some_content_äüö", test).runTaskAsynchronously(plugin);
    }

    public interface Callable<T>{
        void success(T t);
        void fail(T t);
    }

    private class Lookup extends BukkitRunnable{
        private String link;
        private Callable<String> callable;

        Lookup(String link, Callable<String> callable){
            this.link = link;
            this.callable = callable;
        }

        @Override
        public void run() {
            try {
                final String shortLink = shorten(link);
                // since chat can be handled async there is no need to jump back on main
                callable.success(shortLink);
            } catch (IOException e) {
                callable.fail(link);
            }
        }

        private String shorten(String longUrl) throws IOException {
            String urlLookup = shorteningServiceURL + URLEncoder.encode(longUrl, "UTF-8");
            BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(urlLookup).openStream()));
            return reader.readLine();
        }
    }
}
