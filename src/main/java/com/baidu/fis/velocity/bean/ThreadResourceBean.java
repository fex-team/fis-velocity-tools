package com.baidu.fis.velocity.bean;

import java.util.HashMap;
import java.util.Map;

import org.apache.velocity.runtime.resource.Resource;

import javax.servlet.http.HttpServletRequest;

/**
 * 缓存Velocity资源
 * Created by yongqingdong on 2015/9/10.
 */
public class ThreadResourceBean {

    /**
     * <ResourceName, Resource>
     */
    private static ThreadLocal<Map<String, Resource>> threadLocal = new ThreadLocal<Map<String, Resource>>();

    private static ThreadLocal<HttpServletRequest> threadLocalReqeust = new ThreadLocal<HttpServletRequest>();

    public static void set(Resource resource){

        if(threadLocal.get() == null){
            threadLocal.set(new HashMap<String, Resource>());
        }

        String resourceName = resource.getName();
        threadLocal.get().put(resourceName, resource);
    }

    public static Resource get(String resourceName){

        if(threadLocal.get() == null){
            threadLocal.set(new HashMap<String, Resource>());
        }

        return threadLocal.get().get(resourceName);
    }

    public static void setRequest(HttpServletRequest request){
        threadLocalReqeust.set(request);
    }

    public static HttpServletRequest getRequest(){
        return threadLocalReqeust.get();
    }

}
