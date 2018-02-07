package me.nikl.lmgtfy;

/**
 * Created by nikl on 29.12.17.
 *
 */
public enum SearchEngine {
    GOOGLE("google", "", "https://www.google.com/search?q="),
    BING("bing", "s=b&", "https://www.bing.com/search?q="),
    YAHOO("yahoo", "s=y&", "https://search.yahoo.com/search?p="),
    DUCKDUCKGO("duckduckgo", "s=d&", "https://duckduckgo.com/?q="),
    BAIDU("baidu", null, "https://www.baidu.com/s?ie=utf-8&word="),
    YANDEX("yandex", null, "https://www.yandex.ru/search/?text=");

    private String command, lmgtfyMode, searchLink;

    SearchEngine(String command, String lmgtfyMode, String searchLink){
        this.command = command;
        this.lmgtfyMode = lmgtfyMode;
        this.searchLink = searchLink;
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

    public String getSearchLink() {
        return searchLink;
    }
}
