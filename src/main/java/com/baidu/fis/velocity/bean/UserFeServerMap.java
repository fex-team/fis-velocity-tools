package com.baidu.fis.velocity.bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 保存前端服务器与用户地址的映射关系
 * Created by yongqingdong on 2015/8/1.
 */
public class UserFeServerMap {

    /**
     * 当前设置的前端服务器
     */
    protected List<FeServerBean> feServerList = new ArrayList<FeServerBean>();

    /**
     * 是否启用远端请求的功能
     */
    protected Boolean enabled = null;

    /**
     * 当前运行的端口
     */
    protected int port;

    /**
     * 配置的用户IP请求与前端服务器的映射：<UserIP, FeServerId>
     */
    protected Map<String, String> userFeServerMap = new HashMap<String, String>();

    /**
     * 保存之前访问的的IP请求映射：<IP~ResourceName, FeServerId>，供清理缓存之类的使用
     */
    protected Map<String, String> userAccessedMap = new HashMap<String, String>();



    public List<FeServerBean> getFeServerList() {
        return feServerList;
    }

    public void setFeServerList(List<FeServerBean> feServerList) {
        this.feServerList = feServerList;
    }

    public Map<String, String> getUserFeServerMap() {
        return userFeServerMap;
    }

    public void setUserFeServerMap(Map<String, String> userFeServerMap) {
        this.userFeServerMap = userFeServerMap;
    }

    public Map<String, String> getUserAccessedMap() {
        return userAccessedMap;
    }

    public void setUserAccessedMap(Map<String, String> userAccessedMap) {
        this.userAccessedMap = userAccessedMap;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
