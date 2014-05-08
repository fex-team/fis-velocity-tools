package com.baidu.fis.velocity;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by 2betop on 5/7/14.
 */
public class RewirteFilter implements Filter {
    public void destroy() {
    }

    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws ServletException, IOException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse)resp;
        String path = request.getServletPath();


        if (path.endsWith(".vm")) {

            URL url = req.getServletContext().getResource(path);

            if (url == null) {
                path = "/templates" + path;
                url = req.getServletContext().getResource(path);

                // 重写规则后文件存在，则 forward 过去
                if ( url != null ) {
                    // req.setAttribute("javax.servlet.include.servlet_path", path);
                    request.getRequestDispatcher(path).forward(req, resp);
                    return;
                }
            }
        }

        chain.doFilter(req, resp);
    }

    public void init(FilterConfig config) throws ServletException {

    }

}
