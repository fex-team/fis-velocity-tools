package com.baidu.fis.velocity.servlet;

import com.baidu.fis.util.Settings;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.tools.view.JeeConfig;
import org.apache.velocity.tools.view.VelocityView;

public class View extends VelocityView {
    public View(JeeConfig config) {
        super(config);
    }

    @Override
    protected void configure(JeeConfig config, VelocityEngine velocity) {
        super.configure(config, velocity);
        Settings.init(config.getServletContext());
        velocity.setProperty("webapp.resource.loader.path", Settings.getString("views.path", "/WEB-INF/views"));
    }
}
