package com.baidu.fis.velocity;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by 2betop on 7/30/14.
 */
public class Settings {

    protected static Properties data = null;

    // 路径是相对与 WEB-INF 目录的。
    protected static String path = "/../fis.properties";

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
        if (data == null) {
            load();
        }

        return data.getProperty(key);
    }

    protected static void load() {
        InputStream input = Settings.class.getResourceAsStream(path);
        data = new Properties();

        if (input!=null) {
            try {
                data.load(input);
            } catch (Exception err) {
                System.out.println(err.getMessage());
            }
        }
    }
}
