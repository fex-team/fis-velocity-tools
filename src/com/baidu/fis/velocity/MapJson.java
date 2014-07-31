package com.baidu.fis.velocity;

import com.alibaba.fastjson.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by 2betop on 7/30/14.
 */
public class MapJson {

    protected String dir = "WEB-INF/map";
    protected Map<String, JSONObject> map;

    public MapJson() {
        this.map = new HashMap<String, JSONObject>();

        setDir(Settings.getString("mapDir", dir));
    }

    public void setDir(String dir) {
        this.dir = dir;
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
            String filename = "/../../" + dir + "/" + (ns.equals("__global__") ? "map.json" : ns + "-map.json");

            try {
                InputStream input = this.getClass().getResourceAsStream(filename);
                String enc = "UTF-8";
                BufferedReader in = new BufferedReader(new UnicodeReader(input, enc));
                String data = "";
                String inputLine;
                while ((inputLine = in.readLine()) != null){
                    data += inputLine;
                }
                in.close();

                this.map.put(ns, JSONObject.parseObject(data));

            } catch ( Exception error ) {
                System.out.print(error.getMessage());
            }
        }

        JSONObject ret = this.map.get(ns);

        if (ret == null) {
            throw new IllegalArgumentException("missing map json of [" + id + "]");
        }

        return ret;
    }

    private static MapJson instance = null;

    public static MapJson getInstance() {
        if (instance == null) {
            instance = new MapJson();
        }
        return instance;
    }
}
