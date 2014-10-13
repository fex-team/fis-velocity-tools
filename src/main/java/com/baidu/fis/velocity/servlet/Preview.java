package com.baidu.fis.velocity.servlet;

import com.alibaba.fastjson.JSONObject;
import com.baidu.fis.velocity.util.MapJson;
import com.baidu.fis.velocity.util.ResponseWrapper;
import com.baidu.fis.velocity.util.Settings;
import com.baidu.fis.velocity.util.UnicodeReader;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.RuntimeSingleton;
import org.apache.velocity.tools.view.ServletUtils;
import org.apache.velocity.tools.view.VelocityViewServlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletResponseWrapper;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Preview extends VelocityViewServlet {

    @Override
    public void init(ServletConfig config) throws ServletException {
        ServletContext context = config.getServletContext();
        Settings.setApplicationAttribute(ServletContext.class.getName(), context);
        Settings.load(context.getResourceAsStream(Settings.DEFAULT_PATH));
        super.init(config);
    }

    /**
     * <p>This was a common extension point, but now it is usually
     * simpler to override {@link #fillContext} to add custom things
     * to the {@link org.apache.velocity.context.Context} or override a {@link #getTemplate}
     * method to change how {@link org.apache.velocity.Template}s are retrieved.
     * This is only recommended for more complicated use-cases.</p>
     *
     * @param request  client request
     * @param response client response
     * @param ctx      VelocityContext to fill
     * @return Velocity Template object or null
     */
    @Override
    protected Template handleRequest(HttpServletRequest request, HttpServletResponse response, Context ctx) {
        String path = ServletUtils.getPath(request);
        Pattern reg = Pattern.compile("^/(?:([^/]+)/)?page/(.*)$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = reg.matcher(path);

        if (matcher.find()) {
            String ns = matcher.group(1);
            String file = matcher.group(2);

            try {
                MapJson map = new MapJson();
                JSONObject info = ns != null ? map.getNode(ns + ":page/" + file) : map.getNode("page/" + file);

                if (info!=null) {
                    path = info.getString("uri");
                }
            } catch (Exception err) {
                System.out.println(err.getMessage());
            }
        }

        return getTemplate(path);
    }

    /**
     * Handles with both GET and POST requests
     *
     * @param request  HttpServletRequest object containing client request
     * @param response HttpServletResponse object for the response
     */
    @Override
    protected void doRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Context context = null;
        try
        {
            // then get a context
            context = createContext(request, response);

            // call standard extension point
            fillContext(context, request, response);

            setContentType(request, response);

            // get the template
            Template template = handleRequest(request, response, context);

            // merge the template and context into the response
            mergeTemplate(template, context, response);
        } catch (IOException e) {
            error(request, response, e);
            throw e;
        }
        catch (ResourceNotFoundException e)
        {
            manageResourceNotFound(request, response, e);
        }
        catch (RuntimeException e)
        {
            error(request, response, e);
            throw e;
        }
        finally
        {
            requestCleanup(request, response, context);
        }
    }

    protected void fillContext(Context context, HttpServletRequest request, HttpServletResponse response) {
        if (request.getParameter("debug") != null) {
            Settings.put("debug", "true");
        } else {
            Settings.put("debug", "false");
        }

        attachJson(context, request);
        includeJsp(context, request, response);

        super.fillContext(context, request);
    }

    @SuppressWarnings("unchecked")
    protected void attachJson(Context context, HttpServletRequest request) {

        String path = request.getServletPath();
        String[] parts = path.replaceAll("\\..+$", "").split("/+");
        String prefix = "/test";

        JSONObject jsonData = new JSONObject();
        for (String part:parts) {
            String jsonPath = prefix + "/" + part + ".json";

            try {
                URL url = request.getServletContext().getResource(jsonPath);
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
        String path = request.getServletPath();
        String[] parts = path.replaceAll("\\..+$", "").split("/+");
        String prefix = "/test";

        for (String part:parts) {
            String jspPath = prefix + "/" + part + ".jsp";

            try {
                URL url = request.getServletContext().getResource(jspPath);
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
