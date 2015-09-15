package com.baidu.fis.filter;

import com.baidu.fis.velocity.bean.ThreadResourceBean;
import com.baidu.fis.velocity.dest.RouterMapManager;
import com.baidu.fis.velocity.dest.ThreadRequestUtil;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 资源重新定位，如：JS、CSS、Image
 * Created by yongqingdong on 2015/9/10.
 */
public class DestRedirectFilter implements Filter {

    public static String resourceType = null;

    public static Map<String, Boolean> resourceTypeHash = new HashMap<String, Boolean>();

    public static String REDIRECT_URL = "http://%s%s";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

        // 加载匹配资源类型
        resourceType = filterConfig.getInitParameter("resourceType");
        String[] resourceTypeList = resourceType.split(",");
        for(String resourceType : resourceTypeList){
            resourceTypeHash.put(resourceType.trim(), true);
        }

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest)request;
        HttpServletResponse res = (HttpServletResponse)response;
        String path = req.getServletPath();

        // Cross Domain
        if(path.contains("/fismanage")){
            // Cross Domain
            res.addHeader("Access-Control-Allow-Origin", "*");
            res.addHeader("Access-Control-Allow-Methods", "Cache-Control, Pragma, Origin, Authorization, Content-Type, X-Requested-With");
            res.addHeader("Access-Control-Allow-Headers", "GET, PUT, OPTIONS, X-XSRF-TOKEN");
        }

        if(RouterMapManager.getInstance().isEnabled()){

            ThreadResourceBean.setRequest(req);

            // 如果开启该功能
            String feServerId = ThreadRequestUtil.getThreadFeServerId();
            if(feServerId == null){
                // 当前线程请求不需要Redirect，直接到本地
                chain.doFilter(request, response);
            }else{
                // 当前线程要可能要定向
                int lastDotPos = path.lastIndexOf(".");
                if(lastDotPos > 0){
                    String suffix = path.substring(lastDotPos + 1);
                    if(resourceTypeHash.get(suffix) == true){
                        // 如果匹配，则forward。
                        String redirect = String.format(REDIRECT_URL, feServerId, path);
                        res.sendRedirect(redirect);
                        return;
                    }
                }
                chain.doFilter(request, response);
            }
        }else{
            // 没有开启，则直接请求
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {
        resourceType = null;
        resourceTypeHash = null;
    }
}
