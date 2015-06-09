package com.baidu.fis.filter;

import com.alibaba.fastjson.JSONObject;
import com.baidu.fis.util.Resource;
import com.baidu.fis.util.ResponseWrapper;
import com.baidu.fis.util.Settings;
import com.baidu.fis.util.UnicodeReader;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;


public class MockFilter implements Filter {

    // implements org.apache.velocity.context.Context 是为了兼容老用法。
    // 都怪我当时年少无知！
    public static class Context implements org.apache.velocity.context.Context {
        private ServletContext ctx;

        public Context(ServletContext ctx) {
            this.ctx = ctx;
        }

        /**
         * Adds a name/value pair to the context.
         *
         * @param key   The name to key the provided value with.
         * @param value The corresponding value.
         * @return The old object or null if there was no old object.
         */
        @Override
        public Object put(String key, Object value) {
            ctx.setAttribute(key, value);
            return value;
        }

        /**
         * Gets the value corresponding to the provided key from the context.
         *
         * @param key The name of the desired value.
         * @return The value corresponding to the provided key.
         */
        @Override
        public Object get(String key) {
            return ctx.getAttribute(key);
        }

        /**
         * Indicates whether the specified key is in the context.
         *
         * @param key The key to look for.
         * @return Whether the key is in the context.
         */
        @Override
        public boolean containsKey(Object key) {
            return get(key.toString()) != null;
        }

        /**
         * Get all the keys for the values in the context.
         *
         * @return All the keys for the values in the context.
         */
        @Override
        public Object[] getKeys() {
            return new Object[0];
        }

        /**
         * Removes the value associated with the specified key from the context.
         *
         * @param key The name of the value to remove.
         * @return The value that the key was mapped to, or <code>null</code>
         * if unmapped.
         */
        @Override
        public Object remove(Object key) {
            ctx.removeAttribute(key.toString());
            return null;
        }
    }

    final static String key = MockFilter.class.getName();

    /**
     * Called by the web container to indicate to a filter that it is
     * being placed into service.
     * <p/>
     * <p>The servlet container calls the init
     * method exactly once after instantiating the filter. The init
     * method must complete successfully before the filter is asked to do any
     * filtering work.
     * <p/>
     * <p>The web container cannot place the filter into service if the init
     * method either
     * <ol>
     * <li>Throws a ServletException
     * <li>Does not return within a time period defined by the web container
     * </ol>
     *
     * @param filterConfig
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    /**
     * The <code>doFilter</code> method of the Filter is called by the
     * container each time a request/response pair is passed through the
     * chain due to a client request for a resource at the end of the chain.
     * The FilterChain passed in to this method allows the Filter to pass
     * on the request and response to the next entity in the chain.
     * <p/>
     * <p>A typical implementation of this method would follow the following
     * pattern:
     * <ol>
     * <li>Examine the request
     * <li>Optionally wrap the request object with a custom implementation to
     * filter content or headers for input filtering
     * <li>Optionally wrap the response object with a custom implementation to
     * filter content or headers for output filtering
     * <li>
     * <ul>
     * <li><strong>Either</strong> invoke the next entity in the chain
     * using the FilterChain object
     * (<code>chain.doFilter()</code>),
     * <li><strong>or</strong> not pass on the request/response pair to
     * the next entity in the filter chain to
     * block the request processing
     * </ul>
     * <li>Directly set headers on the response after invocation of the
     * next entity in the filter chain.
     * </ol>
     *
     * @param request
     * @param response
     * @param chain
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        // 避免重复添加
        if (request.getAttribute(key) == null) {
            request.setAttribute(key, true);

            HttpServletRequest req = (HttpServletRequest)request;

            Context ctx = new Context(req.getSession().getServletContext());

            if (req.getParameter("inspect") != null) {
                Resource.inspect = true;
            } else {
                Resource.inspect = false;
            }

            if (req.getParameter("debug") != null) {
                Resource.ignorePkg = true;
            } else {
                Resource.ignorePkg = false;
            }

            this.attachJson(ctx, req);
            this.includeJsp(ctx, req, (HttpServletResponse) response);

        }

        chain.doFilter(request, response);
    }

    /**
     * Called by the web container to indicate to a filter that it is being
     * taken out of service.
     * <p/>
     * <p>This method is only called once all threads within the filter's
     * doFilter method have exited or after a timeout period has passed.
     * After the web container calls this method, it will not call the
     * doFilter method again on this instance of the filter.
     * <p/>
     * <p>This method gives the filter an opportunity to clean up any
     * resources that are being held (for example, memory, file handles,
     * threads) and make sure that any persistent state is synchronized
     * with the filter's current state in memory.
     */
    @Override
    public void destroy() {

    }

    @SuppressWarnings("unchecked")
    protected void attachJson(Context context, HttpServletRequest request) {

        ArrayList<String> tryPaths = new ArrayList<String>();
        String path = (String)request.getAttribute("javax.servlet.forward.request_uri");

        if (path != null) {

            if (!request.getContextPath().isEmpty()) {
                path = path.substring(request.getContextPath().length());
            }

            tryPaths.add(path.replaceAll("(^/|/$|\\..+$)", ""));
        }

        path = (String) request.getAttribute("requestFISID");

        if (path != null) {
            tryPaths.add(path.replace(":", "/").replaceAll("(^/|/$|\\..+$)", ""));
        }

        path = request.getServletPath();

        if (path.endsWith(".jsp") && path.startsWith(Settings.getString("jspDir", "/WEB-INF/views"))){
            path = path.substring(Settings.getString("jspDir", "/WEB-INF/views").length());
            tryPaths.add(path.replaceAll("(^/|/$|\\..+$)", ""));
        } else if (path.startsWith(Settings.getString("views.path", "/WEB-INF/views"))) {
            path = path.substring(Settings.getString("views.path", "/WEB-INF/views").length());
            tryPaths.add(path.replaceAll("(^/|/$|\\..+$)", ""));
        } else {
            tryPaths.add(path.replaceAll("(^/|/$|\\..+$)", ""));
        }

        JSONObject jsonData = new JSONObject();


        for (String path2:tryPaths) {
            if (path2.isEmpty()) {
                continue;
            }
            String[] parts = path2.split("/+");
            String prefix = "/test";

            for (String part:parts) {
                String jsonPath = prefix + "/" + part + ".json";

                try {
                    URL url = request.getSession().getServletContext().getResource(jsonPath);
                    if (url != null) {
                        String enc = Settings.getString("encoding", "UTF-8");

                        BufferedReader in = new BufferedReader(new UnicodeReader(
                                url.openStream(), enc));
                        String data = "";
                        String inputLine;
                        while ((inputLine = in.readLine()) != null){
                            data += inputLine;
                        }
                        in.close();

                        JSONObject obj = JSONObject.parseObject(data);
                        this.extendJson(jsonData, obj);
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }

                prefix += "/" + part;
            }
        }

        for (String key:jsonData.keySet()) {
            context.put(key, jsonData.get(key));
        }
    }

    protected void extendJson(JSONObject source, JSONObject target) {

        if (source == null || target == null) {
            return;
        }

        for (String key:target.keySet()) {
            Object value = target.get(key);
            if (source.containsKey(key) && source.get(key) instanceof JSONObject && value instanceof JSONObject) {
                this.extendJson(source.getJSONObject(key), target.getJSONObject(key));
            } else {
                source.put(key, value);
            }
        }
    }

    protected void includeJsp(Context context, HttpServletRequest request, HttpServletResponse response){
        ArrayList<String> tryPaths = new ArrayList<String>();
        String path = (String)request.getAttribute("javax.servlet.forward.request_uri");

        if (path != null) {

            if (!request.getContextPath().isEmpty()) {
                path = path.substring(request.getContextPath().length());
            }

            tryPaths.add(path.replaceAll("(^/|/$|\\..+$)", ""));
        }

        path = (String) request.getAttribute("requestFISID");

        if (path != null) {
            tryPaths.add(path.replace(":", "/").replaceAll("(^/|/$|\\..+$)", ""));
        }

        path = request.getServletPath();

        if (path.endsWith(".jsp") && path.startsWith(Settings.getString("jspDir", "/WEB-INF/views"))){
            path = path.substring(Settings.getString("jspDir", "/WEB-INF/views").length());
            tryPaths.add(path.replaceAll("(^/|/$|\\..+$)", ""));
        } else if (path.startsWith(Settings.getString("views.path", "/WEB-INF/views"))) {
            path = path.substring(Settings.getString("views.path", "/WEB-INF/views").length());
            tryPaths.add(path.replaceAll("(^/|/$|\\..+$)", ""));
        } else {
            tryPaths.add(path.replaceAll("(^/|/$|\\..+$)", ""));
        }

        for (String path2:tryPaths) {

            String[] parts = path2.split("/+");
            String prefix = "/test";

            for (String part:parts) {
                String jspPath = prefix + "/" + part + ".jsp";

                try {
                    URL url = request.getSession().getServletContext().getResource(jspPath);
                    if (url != null) {
                        ServletResponseWrapper resp = new ResponseWrapper(response);
                        request.setAttribute("context", context);
                        request.getRequestDispatcher(jspPath).include(request, resp);
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }

                prefix += "/" + part;
            }
        }
    }
}
