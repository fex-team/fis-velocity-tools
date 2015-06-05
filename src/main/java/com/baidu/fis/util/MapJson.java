package com.baidu.fis.util;

import com.alibaba.fastjson.JSONObject;
import javax.servlet.ServletContext;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Use MapCache instead.
 * @deprecated
 */
public class MapJson {

    protected String dir = "/WEB-INF/config";
    protected String loaderType = "webapp";
    protected Map<String, JSONObject> map;

    public MapJson() {
        this.map = new HashMap<String, JSONObject>();
        this.dir = Settings.getString("mapDir", dir);
        this.loaderType = Settings.getString("mapLoaderType", this.loaderType);
    }

    public JSONObject getNode(String key, String type) {
        JSONObject map, node, info;

        map = this.getMap(key);

        if (map == null) {
            return null;
        }

        node = map.getJSONObject(type);
        info = node.getJSONObject(key);

        return info;
    }

    public JSONObject getNode(String key) {
        return getNode(key, "res");
    }

    /**
     * 根据资源的 namespace 读取对应的 fis map 产出表。
     *
     * @param id
     */
    protected JSONObject getMap(String id) {
        String ns = "__global__";
        int pos = id.indexOf(":");

        if (pos != -1) {
            ns = id.substring(0, pos);
        }

        if (!this.map.containsKey(ns)) {

            JSONObject json = this.loadJson(ns.equals("__global__") ? "map.json" : ns + "-map.json");

            if (json != null) {
                this.map.put(ns, json);
            }


        }

        return this.map.get(ns);
    }

    protected JSONObject loadJson(String filename) {
        InputStream input = null;

        if (this.loaderType.equals("file")) {
            File file = new File(dir, filename);

            if (file.canRead()) {
                try {
                    input = new FileInputStream(file.getAbsolutePath());
                } catch (FileNotFoundException ex) {
                    input = null;
                }
            }
        } else {
            if (!dir.isEmpty()) {
                filename = dir + "/" + filename;
            }
            ServletContext ctx = (ServletContext)Settings.getApplicationAttribute(ServletContext.class.getName());

            if (ctx == null) {
                System.out.println("Please set the servlet context through Setting.setApplicationAttribute");
                throw new IllegalArgumentException("miss calling Setting.setApplicationAttribute");
            }

            input = ctx.getResourceAsStream(filename);
        }

        if (input == null) {
            return null;
        }

        String data = readStream(input);

        if (data != null) {
            return JSONObject.parseObject(data);
        }

        return null;
    }

    protected String readStream(InputStream input) {
        String data = null;
        try {
            String enc = Settings.getString("encoding", "UTF-8");
            BufferedReader in = new BufferedReader(new UnicodeReader(input, enc));
            data = "";
            String inputLine;
            while ((inputLine = in.readLine()) != null){
                data += inputLine;
            }
            in.close();
        } catch (Exception ex) {
            // do nothing.
        }

        return data;
    }

    private static MapJson instance = null;

    public static MapJson getInstance() {
        if (instance == null) {
            instance = new MapJson();
        }
        return instance;
    }
}
