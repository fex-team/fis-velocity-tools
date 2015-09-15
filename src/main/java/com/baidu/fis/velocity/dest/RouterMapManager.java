package com.baidu.fis.velocity.dest;

import com.baidu.fis.velocity.bean.FeServerBean;
import com.baidu.fis.velocity.bean.UserFeServerMap;
import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * 用户与前端服务器的映射管理。
 * Created by yongqingdong on 2015/8/1.
 */
public class RouterMapManager extends UserFeServerMap {

    /**
     * 是否启用该功能
     */
    protected boolean enabled = false;

    protected static RouterMapManager instance = null;

    protected RoutableResourceLoader route = null;

    protected RouterMapManager(){

    };

    public static RouterMapManager getInstance(){

        if(instance == null){
            instance = new RouterMapManager();
        }
        return instance;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Boolean getSearchFromNextLoader() {
        if(this.getRoute() != null){
            return this.getRoute().getSearchFromNextLoader();
        }
        return false;
    }

    public void setSearchFromNextLoader(Boolean searchFromNextLoader) {
        if(this.getRoute() != null){
            this.getRoute().setSearchFromNextLoader(searchFromNextLoader);
        }
    }

    public RoutableResourceLoader getRoute() {
        return route;
    }

    public void setRoute(RoutableResourceLoader route) {
        this.route = route;
    }

    /**
     * 将FeServerList映射到userFeServerMap中
     */
    public void convertFeServerToUserMap(){

        // 清空所有
        this.userFeServerMap.clear();

        for(FeServerBean feServer : this.feServerList){
            List<String> userList = feServer.getUserMatchList();
            if(!StringUtils.isEmpty(feServer.getMatchStandard())){
                // 如果匹配标准不为空，则按标准匹配
                this.userFeServerMap.put(feServer.getMatchStandard(), feServer.getServerId());
            }else{
                // 如果匹配标准为空，则按IP匹配
                for(String userAddr : userList){
                    this.userFeServerMap.put(userAddr, feServer.getServerId());
                }
            }
        }
    }

    /**
     * 匹配多个server
     */
    private void case01(){

        // Server
        FeServerBean feServer01 = new FeServerBean("localhost:8081");
        FeServerBean feServer02 = new FeServerBean("localhost:8082");

        this.getFeServerList().add(feServer01);
        this.getFeServerList().add(feServer02);

        // Test00: 默认都没有匹配到，访问本地

        // Case01: 匹配所有用户
        feServer01.setMatchStandard("*");

        // Test01: 默认使用缓存，404抛错
//        feServer01.setMatchStandard("0:0:0:0:0:0:0:1");

        // Test02：不使用缓存，404抛错
        /*FeServerBean re0201 = new FeServerBean(Pattern.compile("0:0:0:0:0:0:0:1"), vs01.getServerIdentifier(), false);
        this.getRouterList().add(re0201);*/

        // Test03: 不使用缓存，且404时不抛错，使用本地的VM
//        FeServerBean re0301 = new FeServerBean(Pattern.compile("0:0:0:0:0:0:0:1"), vs01.getServerIdentifier(), false, false);
//        this.getRouterList().add(re0301);

//        // Test03:
//        FeServerBean re0201 = new FeServerBean(Pattern.compile("0:0:0:0:0:0:0:1"), vs01.getServerIdentifier());
//        FeServerBean re0202 = new FeServerBean(Pattern.compile("0:0:0:0:0:0:0:1"), vs02.getServerIdentifier());
//        this.getRouterList().add(re0201);
//        this.getRouterList().add(re0202);

          this.convertFeServerToUserMap();
    }
}
