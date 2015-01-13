package com.baidu.fis.velocity.spring;

import com.baidu.fis.util.Settings;
import org.springframework.web.context.ServletContextAware;

import javax.servlet.ServletContext;

/**
 * Created by 2betop on 8/11/14.
 */
public class FisBean implements ServletContextAware {

    @Override
    public void setServletContext(ServletContext servletContext) {
        Settings.setApplicationAttribute(ServletContext.class.getName(), servletContext);
        Settings.load(servletContext.getResourceAsStream(Settings.DEFAULT_PATH));
    }
}
