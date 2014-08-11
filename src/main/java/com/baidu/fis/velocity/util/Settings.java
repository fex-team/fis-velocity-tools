package com.baidu.fis.velocity.util;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Settings {

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
        Boolean val = getBoolean(key);

        if (val==null) {
            return def;
        }

        return val;
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
            System.out.println(err.getStackTrace());
        }
    }
}
