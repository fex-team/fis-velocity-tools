package com.baidu.fis.velocity.spring;

import com.baidu.fis.util.Settings;
import org.springframework.web.context.ServletContextAware;

import javax.servlet.ServletContext;

public class FisBean implements ServletContextAware {

    @Override
    public void setServletContext(ServletContext servletContext) {
        Settings.setApplicationAttribute(ServletContext.class.getName(), servletContext);
        Settings.load(servletContext.getResourceAsStream(Settings.DEFAULT_PATH));
    }
}
