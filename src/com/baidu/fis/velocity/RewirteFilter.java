package com.baidu.fis.velocity;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
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
        String path = request.getServletPath();

        String file = path.substring(path.lastIndexOf('/'));
        if (path.endsWith(".vm") || file.indexOf('.') == -1) {

            URL url = req.getServletContext().getResource(path);
            System.out.println("vim Path:  " + url);

            if (url == null) {
                String templatePath = "/templates" + path;
                url = req.getServletContext().getResource(templatePath);

                // 重写规则后文件存在，则 forward 过去
                if ( url == null ) {
                    templatePath = path + ".vm";
                    url = req.getServletContext().getResource(templatePath);
                    if (url == null) {
                        templatePath = "/templates" + templatePath;
                        url = req.getServletContext().getResource(templatePath);
                        if (url != null) {
                            request.getRequestDispatcher(templatePath).forward(req, resp);
                            return;
                        }
                    }else{
                        request.getRequestDispatcher(templatePath).forward(req, resp);
                        return;
                    }
                }else{
                    request.getRequestDispatcher(templatePath).forward(req, resp);
                    return;
                }
            }
        }

        chain.doFilter(req, resp);
    }

    public void init(FilterConfig config) throws ServletException {

    }

}
