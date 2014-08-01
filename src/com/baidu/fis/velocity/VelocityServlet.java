package com.baidu.fis.velocity;

import org.apache.velocity.Template;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.context.Context;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletResponseWrapper;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.*;

import java.io.BufferedReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.alibaba.fastjson.JSONObject;
import org.apache.velocity.runtime.RuntimeSingleton;
import org.apache.velocity.runtime.RuntimeConstants;


@SuppressWarnings("deprecated")
public class VelocityServlet extends org.apache.velocity.servlet.VelocityServlet
{

    @Override
    protected void initVelocity(ServletConfig config) throws ServletException {
        Velocity.setApplicationAttribute(ServletContext.class.getName(), this.getServletContext());
        super.initVelocity(config);
    }

    /**
     * @param ctx
     */
    @Override
    @SuppressWarnings("deprecated")
    protected Template handleRequest(Context ctx) throws Exception {
        HttpServletRequest req = (HttpServletRequest)ctx.get(REQUEST);
        String path = req.getServletPath();

        Pattern reg = Pattern.compile("^/([^/]+)/page/(.*)$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = reg.matcher(path);

        if (matcher.find()) {
            String ns = matcher.group(1);
            String file = matcher.group(2);
            MapJson map = new MapJson();

            JSONObject info = map.getNode(ns + ":page/" + file);

            if (info!=null) {
                path = info.getString("uri");
            }
        }

        return getTemplate(path);
    }

    /**
     * @param config
     */
    @Override
    @SuppressWarnings("deprecated")
    protected Properties loadConfiguration(ServletConfig config) throws IOException {

        Properties p = super.loadConfiguration(config);

        p.load(getServletContext().getResourceAsStream("WEB-INF/velocity.properties"));
        p.setProperty("file.resource.loader.path", getServletContext().getRealPath("/") + Settings.getString("velocity.path", "."));

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

        // 只给 templates 目录下面的 vm 文件自动关联 jsp 文件。
        if (!jsonPath.startsWith("/templates/")) {
            return;
        }

        jsonPath = jsonPath.replaceAll("^/templates", "");
        jsonPath = "/test" + jsonPath;

        try {
            URL url = request.getServletContext().getResource(jsonPath);

            if (url != null) {
                String enc = RuntimeSingleton.getString(RuntimeConstants.INPUT_ENCODING);
                BufferedReader in = new BufferedReader(new UnicodeReader(
                        url.openStream(), enc));
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

        // 只给 templates 目录下面的 vm 文件自动关联 jsp 文件。
        if (!path.startsWith("/templates/")) {
            return;
        }

        String jspPath = path.replaceAll("\\..+$", ".jsp");
        jspPath = jspPath.replaceAll("^/templates", "");
        jspPath = "/test" + jspPath;

        try {
            URL url = request.getServletContext().getResource(jspPath);

            if (url != null) {
                ServletResponseWrapper resp = new ResponseWrapper(response);

                request.setAttribute("context", context);
                request.getRequestDispatcher(jspPath).include(request, resp);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void error( HttpServletRequest request, HttpServletResponse response, Exception cause )
            throws ServletException, IOException
    {
        StringBuffer html = new StringBuffer();
        html.append("<html>");
        html.append("<title>Error</title>");
        html.append("<body bgcolor=\"#ffffff\">");
        html.append("<h2>VelocityServlet: Error processing the template</h2>");
        html.append("<pre>");
        String why = cause.getMessage();
        if (why != null && why.trim().length() > 0)
        {
            html.append(why);
            html.append("<br>");
        }

        StringWriter sw = new StringWriter();
        cause.printStackTrace( new PrintWriter( sw ) );

        html.append("<h3>Detail:</h3>");
        html.append( sw.toString()  );
        html.append("</pre>");
        html.append("</body>");
        html.append("</html>");
        response.getOutputStream().print( html.toString() );
    }
}
