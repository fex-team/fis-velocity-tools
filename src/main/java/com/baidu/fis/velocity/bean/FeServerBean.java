package com.baidu.fis.velocity.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * 前端服务器
 * Created by yongqingdong on 2015/8/1.
 */
public class FeServerBean {

    /**
     * 前端服务器：Addr:Port
     */
    private String serverId;

    /**
     * 匹配用户的标准
     */
    private String matchStandard;

    /**
     * 匹配用户的列表
     */
    private List<String> userMatchList = new ArrayList<String>();

    /**
     * 是否禁用缓存
     */
    private boolean disableCache = false;

    public FeServerBean() {
    }

    public FeServerBean(String serverId) {
        this.serverId = serverId;
    }

    public FeServerBean(String serverId, boolean disableCache) {
        this.serverId = serverId;
        this.disableCache = disableCache;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public String getMatchStandard() {
        return matchStandard;
    }

    public void setMatchStandard(String matchStandard) {
        this.matchStandard = matchStandard;
    }

    public List<String> getUserMatchList() {
        return userMatchList;
    }

    public void setUserMatchList(List<String> userMatchList) {
        this.userMatchList = userMatchList;
    }

    public boolean isDisableCache() {
        return disableCache;
    }

    public void setDisableCache(boolean disableCache) {
        this.disableCache = disableCache;
    }
}
