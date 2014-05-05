package com.baidu.fis.velocity;

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

    protected Boolean debug = false;
    protected String mapDir = null;
    protected String framework = null;

    protected Map<String, Map> map;
    protected Map<String, String> loaded;
    protected Map<String, ArrayList<String>> collection;
    protected Map<String, StringBuilder> embed;


    protected Log log;
    protected RuntimeServices rs;


    public Resource() {
        this.map = new HashMap<String, Map>();
        this.loaded = new HashMap<String, String>();
        this.collection = new HashMap<String, ArrayList<String>>();
        this.embed = new HashMap<String, StringBuilder>();
    }

    public void init(RuntimeServices rs) {

        // 已经初始化过。
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

    public void addResource(String id) throws FileNotFoundException {

        // 已经添加了
        if (loaded.get(id) != null) {
            return;
        } else {
            Map<String, Map> map = this.getMap(id);
        }
    }

    public String getUri(String str) {
        return null;
    }

    public void reset() {

    }

    public void renderCSS(Writer writer) {

    }

    public void renderJS(Writer writer) {

    }

    /**
     * 根据资源的 namespace 读取对应的 fis map 产出表。
     * @param id
     * @return
     * @throws FileNotFoundException
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected Map<String, Map> getMap(String id) throws FileNotFoundException {
        String ns = "__global__";
        int pos = id.indexOf(":");

        if (pos != -1) {
            ns = id.substring(0, pos);
        }

        if (!this.map.containsKey(ns)) {
            String filename = mapDir + "/" + (ns.equals("__global__") ? "map.json" : ns + "-map.json");

            // 通过 velocity 的资源加载器读取内容。
            // 实在是没找到读取 servlet context 的方法，导致定位不到文件。
            ContentResource file = rs.getContent(filename);
            this.map.put(ns, JSONObject.parseObject(file.getData().toString(), HashMap.class));
        }

        return this.map.get(ns);
    }

}
