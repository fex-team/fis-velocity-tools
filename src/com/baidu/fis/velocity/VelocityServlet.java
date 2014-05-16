package com.baidu.fis.velocity;

import org.apache.velocity.Template;
import org.apache.velocity.context.Context;

import javax.servlet.ServletConfig;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;
import javax.servlet.ServletResponseWrapper;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.net.URL;
import java.util.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import com.alibaba.fastjson.JSONObject;


@SuppressWarnings("deprecated")
public class VelocityServlet extends org.apache.velocity.servlet.VelocityServlet
{
    /**
     * @param ctx
     */
    @Override
    @SuppressWarnings("deprecated")
    protected Template handleRequest(Context ctx) throws Exception {
        HttpServletRequest req = (HttpServletRequest)ctx.get(REQUEST);

        return getTemplate(req.getServletPath());
    }

    /**
     * @param config
     */
    @Override
    @SuppressWarnings("deprecated")
    protected Properties loadConfiguration(ServletConfig config) throws IOException {

        Properties p = super.loadConfiguration(config);

        p.load(getServletContext().getResourceAsStream("WEB-INF/velocity.properties"));

        p.setProperty("file.resource.loader.path", getServletContext().getRealPath("./") + "//");

        return p;
    }

    @Override
    protected Context createContext(HttpServletRequest request, HttpServletResponse response) {
        Context context = super.createContext(request, response);

        attachJson(context, request, response);
        includeJsp(context, request, response);

        return context;
    }

    protected void attachJson(Context context, HttpServletRequest request, HttpServletResponse response) {

        String path = request.getServletPath();
        String jsonPath = path.replaceAll("\\..+$", ".json");

        jsonPath = jsonPath.replaceAll("^/templates", "");
        jsonPath = "/test" + jsonPath;

        try {
            URL url = request.getServletContext().getResource(jsonPath);

            if (url != null) {
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        url.openStream()));
                String data = "";
                String inputLine;
                while ((inputLine = in.readLine()) != null){
                    data += inputLine;
                }
                in.close();
                HashMap<String, JSONObject> obj = JSONObject.parseObject(data, HashMap.class);
                Iterator<Map.Entry<String, JSONObject>> iterator = obj.entrySet().iterator();
                Map.Entry<String, JSONObject> entry;

                while( iterator.hasNext()) {
                    entry = iterator.next();
                    context.put(entry.getKey(), entry.getValue());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void includeJsp(Context context, HttpServletRequest request, HttpServletResponse response){
        String path = request.getServletPath();
        String jspPath = path.replaceAll("\\..+$", ".jsp");

        // 只给 templates 目录下面的 vm 文件自动关联 jsp 文件。
        if (!jspPath.startsWith("/templates/")) {
            return;
        }

        jspPath = jspPath.replaceAll("^/templates", "");
        jspPath = "/test" + jspPath;

        try {
            URL url = request.getServletContext().getResource(jspPath);

            if (url != null) {
                request.setAttribute("context", context);
                request.getRequestDispatcher(jspPath).include(request, response);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
