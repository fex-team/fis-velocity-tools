package com.baidu.fis.util;

import javax.servlet.ServletContext;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Settings {

    final static String Key = ServletContext.class.getName();
    
    public static void init(ServletContext context) {
        if (Settings.getApplicationAttribute(Key) != null) {
            return;
        }

        Settings.setApplicationAttribute(Key, context);
        Settings.load(context.getResourceAsStream(Settings.DEFAULT_PATH));
    }

    protected static Properties data = new Properties();

    protected static Map<String, Object> map = new HashMap<String, Object>();

    public static void setApplicationAttribute(String key, Object val) {
        map.put(key, val);
    }

    public static Object getApplicationAttribute(String key) {
        return map.get(key);
    }

    public static String DEFAULT_PATH = "/WEB-INF/fis.properties";

    public static Boolean getBoolean(String key, Boolean def) {
        String val = getString(key);

        if (val==null) {
            return def;
        }

        return getBoolean(key);
    }

    public static Boolean getBoolean(String key) {
        String val = getString(key);
        return val != null && val.equalsIgnoreCase("true");
    }

    public static String getString(String key, String def) {
        String val =  getString(key);

        if (val==null) {
            return def;
        }

        return val;
    }

    public static String getString(String key) {
        return data.getProperty(key);
    }

    public static void load(InputStream input) {
        if (input == null) {
            return;
        }

        try {
            data.load(input);
        } catch (Exception err) {
            System.out.println(err.getMessage());
        }
    }

    public static void put(String key, String val) {
        data.setProperty(key, val);
    }

    public static String getMapDir() {
        String mapDir = Settings.getString("mapDir", "/WEB-INF/config");
        String mapDirType = Settings.getString("mapLoaderType", "webapp");

        if (mapDirType.equals("webapp")) {
            ServletContext context = (ServletContext)Settings.getApplicationAttribute(Key);

            if (!mapDir.startsWith("/")) {
                mapDir = "/" + mapDir;
            }

            mapDir = context.getRealPath(mapDir);
        }

        return mapDir;
    }

    public static void reload() {
        if (Settings.getApplicationAttribute(Key) == null) {
            return;
        }

        ServletContext context = (ServletContext)Settings.getApplicationAttribute(Key);
        Settings.load(context.getResourceAsStream(Settings.DEFAULT_PATH));
    }
}
