package com.baidu.fis.velocity.spring;

import com.baidu.fis.util.Settings;
import org.springframework.web.context.ServletContextAware;

import javax.servlet.ServletContext;

public class FisBean implements ServletContextAware {

    public FisBean() {
    }

    @Override
    public void setServletContext(ServletContext servletContext) {
        Settings.setApplicationAttribute(ServletContext.class.getName(), servletContext);
        Settings.load(servletContext.getResourceAsStream(Settings.DEFAULT_PATH));
    }

    private String mapLoaderType;
    private String mapDir;
    private String debug;

    public String getMapLoaderType() {
        return mapLoaderType;
    }

    public void setMapLoaderType(String mapLoaderType) {
        this.mapLoaderType = mapLoaderType;
        Settings.put("mapLoaderType", mapLoaderType);
    }

    public String getMapDir() {
        return mapDir;
    }

    public void setMapDir(String mapDir) {
        this.mapDir = mapDir;
        Settings.put("mapDir", mapDir);
    }

    public String getDebug() {
        return debug;
    }

    public void setDebug(String debug) {
        this.debug = debug;
        Settings.put("debug", debug);
    }
}
