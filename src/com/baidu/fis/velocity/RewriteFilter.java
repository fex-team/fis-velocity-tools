package com.baidu.fis.velocity;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by 2betop on 5/7/14.
 *
 * 负责两件事
 *
 * 1. 读取 server.conf 把设置的转发规则实现
 * 2. 其他转发的实现。如请求时没有加 .vm 后缀，转发到加 .vm 后缀上去。
 *
 */
public class RewriteFilter implements Filter {
    public void destroy() {
    }

    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws ServletException, IOException {

        // 先执行 rewrite.
        if (handleRewrite((HttpServletRequest)req, (HttpServletResponse)resp)) {
            return;
        }

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
                    System.out.println("Forward from " + path + " to " + candidate);
                    request.getRequestDispatcher(candidate).forward(req, resp);
                    return;
                }
            }
        }

        chain.doFilter(req, resp);
    }

    public void init(FilterConfig config) throws ServletException {

    }

    // 读取 server.conf 进行转发
    protected Boolean handleRewrite(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException{
        RewriteRulers parser = new RewriteRulers();

        InputStream stream = req.getServletContext().getResourceAsStream(RewriteRulers.DEFAULT_PATH);

        if (stream!=null) {
            parser.load(stream);
        }

        RewriteRulers.Ruler ruler = parser.getRuler(req.getRequestURI());

        if (ruler!=null) {
            if (ruler.type == RewriteRulers.Ruler.TYPE_REDIRECT) {
                resp.sendRedirect(ruler.target);
            } else if(ruler.type == RewriteRulers.Ruler.TYPE_REWRITE) {
                req.getRequestDispatcher(ruler.target).forward(req, resp);
            }
            return true;
        }

        return false;
    }

}
