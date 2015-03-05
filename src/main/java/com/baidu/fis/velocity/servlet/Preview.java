package com.baidu.fis.velocity.servlet;

import com.baidu.fis.util.Settings;
import org.apache.commons.collections.ExtendedProperties;
import org.apache.velocity.Template;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.tools.view.ServletUtils;
import org.apache.velocity.tools.view.VelocityViewServlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class Preview extends VelocityViewServlet {

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

        if (path.startsWith(Settings.getString("views.path", "/WEB-INF/views"))) {
            path = path.substring(Settings.getString("views.path", "/WEB-INF/views").length());
        }

        return getTemplate(path);
    }


    @Override
    protected void fillContext(Context context, HttpServletRequest request) {
        if (request.getParameter("debug") != null) {
            Settings.put("debug", "true");
        } else {
            Settings.put("debug", "false");
        }

        super.fillContext(context, request);
    }
}
