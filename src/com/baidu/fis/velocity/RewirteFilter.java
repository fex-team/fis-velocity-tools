package com.baidu.fis.velocity;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Created by 2betop on 5/7/14.
 */
public class RewirteFilter implements Filter {
    public void destroy() {
    }

    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws ServletException, IOException {

        HttpServletRequest request = (HttpServletRequest) req;
        String path = request.getServletPath();

        URL url = req.getServletContext().getResource(path);

        // 找不到资源
        if (url == null) {
            ArrayList<String> candidates = new ArrayList<String>();

            // 如果不是.vm 结尾则尝试加 .vm
            if (!path.endsWith(".vm")) {
                candidates.add(path + ".vm");
            }

            // 如果不在 templates目录下，则尝试 templates 目录下是否有此文件
            if (!path.startsWith("/templates")) {
                candidates.add("/templates" + path);

                // 尝试加 .vm 后缀
                if (!path.endsWith(".vm")) {
                    candidates.add("/templates" + path + ".vm");
                }
            }

            // 尝试各个候选路径，如果存在则 forward 过去。
            for (String candidate : candidates) {
                url = req.getServletContext().getResource(candidate);

                if (url != null) {
                    request.getRequestDispatcher(candidate).forward(req, resp);
                    return;
                }
            }
        }


//        String file = path.substring(path.lastIndexOf('/'));
//        if (path.endsWith(".vm") || file.indexOf('.') == -1) {
//
//            URL url = req.getServletContext().getResource(path);
//            System.out.println("vim Path:  " + url);
//
//            if (url == null) {
//                String templatePath = "/templates" + path;
//                url = req.getServletContext().getResource(templatePath);
//
//                // 重写规则后文件存在，则 forward 过去
//                if ( url == null ) {
//                    templatePath = path + ".vm";
//                    url = req.getServletContext().getResource(templatePath);
//                    if (url == null) {
//                        templatePath = "/templates" + templatePath;
//                        url = req.getServletContext().getResource(templatePath);
//                        if (url != null) {
//                            request.getRequestDispatcher(templatePath).forward(req, resp);
//                            return;
//                        }
//                    }else{
//                        request.getRequestDispatcher(templatePath).forward(req, resp);
//                        return;
//                    }
//                }else{
//                    request.getRequestDispatcher(templatePath).forward(req, resp);
//                    return;
//                }
//            }
//        }

        chain.doFilter(req, resp);
    }

    public void init(FilterConfig config) throws ServletException {

    }

}
