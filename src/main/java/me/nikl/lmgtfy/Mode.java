package me.nikl.lmgtfy;

/**
 * Created by nikl on 29.12.17.
 *
 */
public enum Mode {
    GOOGLE("google", ""),
    BING("bing", "s=b&"),
    YAHOO("yahoo", "s=y&"),
    DUCKDUCKGO("duckduckgo", "s=d&"),
    BAIDU("baidu", null),
    YANDEX("yandex", null),
	SEARCH("search", null);

    private String command, lmgtfyMode;

    Mode(String command, String lmgtfyMode){
        this.command = command;
        this.lmgtfyMode = lmgtfyMode;
    }

    String getCommand(){
        return this.command;
    }

    /**
     * Get the parameter that changes the lmgtfy search engine.
     *
     * @return lmgtfy se parameter
     */
    String getLmgtfyMode(){
        return this.lmgtfyMode;
    }
}
