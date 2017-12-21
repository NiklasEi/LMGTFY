package me.nikl.lmgtfy;

import org.bukkit.scheduler.BukkitRunnable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by nikl on 19.12.17.
 *
 * Send requests to a shortening service and catch the shortened link
 */
public class Shortener {
    private Main plugin;

    public Shortener(Main plugin){
        this.plugin = plugin;
    }

    public void shortenAsync(String link, Callable<String> callable){
        new Lookup(link, callable).runTaskAsynchronously(plugin);
    }


    public interface Callable<T>{
        void success(T t);
        void fail(T t);
    }

    private class Lookup extends BukkitRunnable{
        private final static String REQ = "https://is.gd/create.php?format=simple&url=";
        private String link;
        private Callable<String> callable;

        public Lookup(String link, Callable<String> callable){
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
            String isgdUrlLookup = REQ + URLEncoder.encode(longUrl, "UTF-8");
            BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(isgdUrlLookup).openStream()));
            String isgdUrl = reader.readLine();
            return isgdUrl;
        }
    }
}
