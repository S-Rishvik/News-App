package com.rstudios.simplesortingtask;

import org.json.JSONException;
import org.json.JSONObject;

public class ListItem {
    private String imgUrl,title,desc,source,pageUrl;

    ListItem(JSONObject jsonObject) throws JSONException {
        source=jsonObject.getJSONObject("source").getString("name")+"";
        imgUrl=jsonObject.getString("urlToImage");
        pageUrl=jsonObject.getString("url");
        title=jsonObject.getString("title").split("-")[0];
        desc=jsonObject.getString("description");
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getPageUrl() {
        return pageUrl;
    }

    public void setPageUrl(String pageUrl) {
        this.pageUrl = pageUrl;
    }
}
