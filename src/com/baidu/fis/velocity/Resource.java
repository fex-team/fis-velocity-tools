package com.baidu.fis.velocity;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.log.Log;
import org.apache.velocity.runtime.resource.ContentResource;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by 2betop on 4/29/14.
 */
public class Resource {

    public static final String STYLE_PLACEHOLDER = "<!--FIS_STYLE_PLACEHOLDER-->";
    public static final String SCRIPT_PLACEHOLDER = "<!--FIS_SCRIPT_PLACEHOLDER-->";

    protected Boolean debug = false;
    protected String mapDir = null;
    protected String framework = null;

    // 存 map.json 数据， key 为 namespace, value 为 json 数据
    protected Map<String, JSONObject> map;
    protected Map<String, Boolean> loaded;
    protected Map<String, ArrayList<String>> collection;
    protected Map<String, StringBuilder> embed;


    protected Log log;
    protected RuntimeServices rs;


    public Resource() {
        this.map = new HashMap<String, JSONObject>();
        this.loaded = new HashMap<String, Boolean>();
        this.collection = new HashMap<String, ArrayList<String>>();
        this.embed = new HashMap<String, StringBuilder>();
    }

    public void init(RuntimeServices rs) {

        // 避免重复初始化
        if (this.rs != null) {
            return;
        }

        this.rs = rs;
        log = rs.getLog();

        // 从velocity.properties里面读取fis.mapDir。
        // 用来指定map文件存放目录。
        mapDir = rs.getString("fis.mapDir", "WEB-INF/config");
        debug = rs.getBoolean("fis.debug", false);
    }

    public String getMapDir() {
        return mapDir;
    }

    public void setMapDir(String mapDir) {
        this.mapDir = mapDir;
    }

    public String getFramework() {
        return framework;
    }

    public void setFramework(String framework) {
        this.framework = framework;
    }

    public void addJS(String uri) {

    }

    public void addJSEmbed(String content) {

    }

    public void addCSS(String uri) {

    }

    public void addCSSEmbed(String content) {

    }

    public void addResource(String id) throws FileNotFoundException{
        this.addResource(id, false);
    }

    public void addResource(String id, Boolean deffer) throws FileNotFoundException {
        JSONObject map, node, info;

        // 如果添加过了而且添加的方式也相同则不重复添加。（这里说的方式是指，同步 or 异步）
        // 如果之前是同步的这次异步添加则忽略掉。
        // 注意：null 不能直接用来和 false\true 比较，否则报错。
        if ( loaded.get(id) != null && loaded.get(id) == deffer ||
                deffer && loaded.get(id) != null && !loaded.get(id) ) {
            return;
        }

        map = this.getMap(id);
        node = map.getJSONObject("res");
        info = node.getJSONObject(id);

        String uri;

        if (info == null) {
            throw new IllegalArgumentException("missing resource [" + id + "]");
        }

        String pkg = (String) info.get("pkg");

        if (!debug && pkg != null) {
            node = map.getJSONObject("pkg");
            info = node.getJSONObject(pkg);
            uri = info.getString("uri");

            if (info.containsKey("has")) {
                JSONArray has = info.getJSONArray("has");

                for (Object obj : has) {
                    loaded.put(obj.toString(), deffer);
                }
            }
        } else {
            uri = info.getString("uri");
            loaded.put(id, deffer);
        }


        // 如果有异步依赖，则添加异步依赖
        if (info.containsKey("extras")) {
            node = info.getJSONObject("extras");
            if (node.containsKey("async")) {
                JSONArray async = node.getJSONArray("async");
                for (Object dep : async) {
                    this.addResource(dep.toString(), true);
                }
            }
        }

        // 如果有同步依赖，则把同步依赖也添加进来。
        if (info.containsKey("deps")) {
            JSONArray deps = info.getJSONArray("deps");
            for (Object dep : deps) {
                this.addResource(dep.toString(), deffer);
            }
        }

        String type = info.get("type").toString();

        if (type.equals("js") && deffer) {
            type = "jsDeffer";
        }

        ArrayList<String> list = collection.get(type);

        if (list == null) {
            list = new ArrayList<String>();
            collection.put(type, list);
        }

        list.add(uri);
    }

    public String getUri(String str) {
        JSONObject map, node, info;

        String uri;

        if (info == null) {
            throw new IllegalArgumentException("missing resource [" + id + "]");
        }

        String pkg = (String) info.get("pkg");

        if (!debug && pkg != null) {
            node = map.getJSONObject("pkg");
            info = node.getJSONObject(pkg);
            uri = info.getString("uri");

            if (info.containsKey("has")) {
                JSONArray has = info.getJSONArray("has");

                for (Object obj : has) {
                    loaded.put(obj.toString(), deffer);
                }
            }
        } else {
            uri = info.getString("uri");
            loaded.put(id, deffer);
        }
    }

    public void reset() {

    }

    public String renderCSS() {
        return "";
    }

    public String renderJS() {
        log.warn(collection);
        return "";
    }

    /**
     * 根据资源的 namespace 读取对应的 fis map 产出表。
     *
     * @param id
     * @return
     * @throws FileNotFoundException
     */
    protected JSONObject getMap(String id) throws FileNotFoundException {
        String ns = "__global__";
        int pos = id.indexOf(":");

        if (pos != -1) {
            ns = id.substring(0, pos);
        }

        if (!this.map.containsKey(ns)) {
            String filename = mapDir + "/" + (ns.equals("__global__") ? "map.json" : ns + "-map.json");

            // 通过 velocity 的资源加载器读取内容。
            // 实在是没找到读取 servlet context 的方法，导致定位不到文件。
            // 所以还是用 velocity 的 RuntimeServices 吧
            ContentResource file = rs.getContent(filename);

            this.map.put(ns, JSONObject.parseObject(file.getData().toString()));
        }

        return this.map.get(ns);
    }

}
