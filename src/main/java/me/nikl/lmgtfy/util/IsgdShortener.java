package me.nikl.lmgtfy.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * Created by nikl on 19.12.17.
 */
public class IsgdShortener {
    private final static String REQ = "https://is.gd/create.php?format=simple&url=";

    public static String shorten(String longUrl) throws IOException {
        String isgdUrlLookup = REQ + longUrl;
        BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(isgdUrlLookup).openStream()));
        String isgdUrl = reader.readLine();
        return isgdUrl;
    }
}
