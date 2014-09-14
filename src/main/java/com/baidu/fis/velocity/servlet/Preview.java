package com.baidu.fis.velocity.servlet;

import com.alibaba.fastjson.JSONObject;
import com.baidu.fis.velocity.util.ResponseWrapper;
import com.baidu.fis.velocity.util.Settings;
import com.baidu.fis.velocity.util.UnicodeReader;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.RuntimeSingleton;
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

public class Preview extends VelocityViewServlet {

    @Override
    public void init(ServletConfig config) throws ServletException {
        ServletContext context = config.getServletContext();
        Settings.setApplicationAttribute(ServletContext.class.getName(), context);
        Settings.load(context.getResourceAsStream(Settings.DEFAULT_PATH));
        super.init(config);
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
        attachJson(context, request);
        includeJsp(context, request, response);

        super.fillContext(context, request);
    }


    protected void attachJson(Context context, HttpServletRequest request) {

        String path = request.getServletPath();
        String jsonPath = path.replaceAll("\\..+$", ".json");

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

        String jspPath = "/test" + path.replaceAll("\\..+$", ".jsp");

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
}
