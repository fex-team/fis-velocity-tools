package com.baidu.fis.velocity.dest;

import com.baidu.fis.velocity.bean.FeServerBean;
import com.baidu.fis.velocity.bean.ThreadResourceBean;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

public class ThreadRequestUtil {

    /**
     * 获取HTTP请求的IP地址
     * @param request
     * @return
     */
    public static String getRequestAddr(HttpServletRequest request) {

        String remoteAddr = null;

        if(request != null){
            String ip = request.getHeader("X-Forwarded-For");
            if (ip != null && !"".equals(ip) && !"unKnown".equalsIgnoreCase(ip)) {
                //多次反向代理后会有多个ip值，第一个ip才是真实ip
                int index = ip.indexOf(",");
                if (index != -1) {
                    return ip.substring(0, index);
                } else {
                    return ip;
                }
            }
            ip = request.getHeader("X-Real-IP");
            if (ip != null && !"".equals(ip) && !"unKnown".equalsIgnoreCase(ip)) {
                return ip;
            }

            remoteAddr = request.getRemoteAddr();
        }else{
            remoteAddr = "localhost";
        }

        // 匹配
        if("0:0:0:0:0:0:0:1".equals(remoteAddr)){
            remoteAddr = "localhost";
        }else if("127.0.0.1".equals(remoteAddr)){
            remoteAddr = "localhost";
        }
        return remoteAddr;
    }

    /**
     * 获取当前线程HTTP请求的IP地址
     * @return
     */
    public static String getThreadRequestAddr(){
        String requestAddr = null;
        HttpServletRequest request = null;
        try {
            // 当前HTTP请求
            request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        } catch (IllegalStateException ise) {
            // System.err.format("HTTP Request get error[%s]: %s\n", ise.getClass().getName(), ise.getMessage());
            request = ThreadResourceBean.getRequest();
        }
        // HTTP请求IP地址
        requestAddr = ThreadRequestUtil.getRequestAddr(request);
        return requestAddr;
    }

    /**
     * 获取当前线程HTTP请求的前端服务器地址
     * @return
     */
    public static String getThreadFeServerId() {
        String feServerId = null;

        // HTTP请求IP地址
        String requestAddr = ThreadRequestUtil.getThreadRequestAddr();
        // 根据映射来发送请求
        RouterMapManager rmm = RouterMapManager.getInstance();
        // 根据IP请求获取VM服务器的地址
        if(rmm.getUserFeServerMap().containsKey(requestAddr)){
            feServerId = rmm.getUserFeServerMap().get(requestAddr);
        }else{
            feServerId = rmm.getUserFeServerMap().get("*");
        }

        return feServerId;
    }

    /**
     * 获取当前HTTP请求的前端服务器
     * @return
     */
    public static FeServerBean getThreadFeServer(){
        String feServerId = ThreadRequestUtil.getThreadFeServerId();

        for(FeServerBean feServer : RouterMapManager.getInstance().getFeServerList()){
            if(feServer.getServerId().equals(feServerId)){
                return feServer;
            }
        }

        return null;
    }

}
