package com.baidu.fis.util;

import com.alibaba.fastjson.JSONObject;
import javax.servlet.ServletContext;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

/**
 * Created by xuchenhui on 2015/5/25.
 */
public class MapCache {
    protected String dir = "/WEB-INF/config";
    protected String loaderType = "webapp";

    // 用于判定map表是否被缓存
    private static Boolean refreshMap = false;
    public final static Integer lock = 1;

    /**
     * 设置下次请求时更新整个map表缓存
     */
    public synchronized void setRefreshMap(Boolean map) {
        refreshMap = map;
    }
    public synchronized Boolean getRefreshMap(){ return refreshMap; }

    // 缓存并操控map表
    public static JSONObject map = null;
    public synchronized void setMap(JSONObject newMap){ map = newMap; }
    public void reloadMap(){
        if (map != null){
            System.out.println("Reload all map files in " + this.dir + "[" + map.hashCode() + "]");
        }
        setMap(loadAllMap(dir));
        System.out.println("Reload finished all maps [" + map.hashCode() + "]");
    }
    public JSONObject getMap(){
        return map;
    }
    // 向后兼容旧方法
    public JSONObject getMap(String id){
        return map;
    }
    public synchronized void resetMap(){
        map.clear();
    }

    private static JSONObject mergeJSONObjects(JSONObject json1, JSONObject json2) {
        JSONObject mergedJSON = new JSONObject();
        try {
            mergedJSON.putAll(json1);
            mergedJSON.putAll(json2);
        } catch (Exception e) {
            throw new RuntimeException("JSON Exception" + e);
        }
        return mergedJSON;
    }
    // 重新读取所有的map文件并生成map表
    protected JSONObject loadAllMap(String filePath){
        JSONObject resMap = new JSONObject();
        JSONObject pkgMap = new JSONObject();

        ServletContext ctx = (ServletContext)Settings.getApplicationAttribute(ServletContext.class.getName());
        if (ctx == null) {
            System.out.println("Please set the servlet context through Setting.setApplicationAttribute");
            throw new IllegalArgumentException("miss calling Setting.setApplicationAttribute");
        }
        String realDirPath = ctx.getRealPath(filePath);
        System.out.println("Load map files in : " + realDirPath);

        File root = new File(realDirPath);
        File[] files = root.listFiles();
        for(File file:files) {
            if (file.isDirectory()) {
                this.loadAllMap(file.getAbsolutePath());
            }else{
                String fileName = file.getName();
                if (fileName.matches(".*\\.json")){
                    JSONObject json = this.loadJson(fileName);
                    if (json != null) {
                        System.out.println("Load map file : " + fileName);
                        resMap = mergeJSONObjects(resMap, json.getJSONObject("res"));
                        pkgMap = mergeJSONObjects(pkgMap, json.getJSONObject("pkg"));
                    }
                }
            }
        }

        JSONObject newMap = new JSONObject();
        newMap.put("res", resMap);
        newMap.put("pkg", pkgMap);

        // 用于输出整体map表
        /*try{
            File outfile=new File(realDirPath + "\\allmap.json");
            if(!outfile.exists()) {
                outfile.createNewFile();
            }
            FileOutputStream out= new FileOutputStream(outfile, false); //如果追加方式用true
            StringBuffer sb = new StringBuffer(newMap.toJSONString());
            out.write(sb.toString().getBytes("utf-8"));//注意需要转换对应的字符集
            out.close();
        }catch (Exception e){

        }*/
        return newMap;
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

    // 通过id获取map表节点
    public JSONObject getNode(String key, String type){
        JSONObject node, info;
        synchronized (lock) {
            if (!this.getRefreshMap()){
                // 首先标志已经更新缓存
                this.setRefreshMap(true);
                // 而后开始更新缓存
                try{
                    this.reloadMap();
                }catch(Exception e){
                    // 更新缓存失败，重新设置下次继续更新
                    this.setRefreshMap(false);
                }
            }
        }

        // 尝试读取
        try{
            node = map.getJSONObject(type);
            info = node.getJSONObject(key);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
        return info;
    }
    public JSONObject getNode(String key) {
        return getNode(key, "res");
    }

    // 单例模式
    private MapCache() {
        this.dir = Settings.getString("mapDir", dir);
        this.loaderType = Settings.getString("mapLoaderType", this.loaderType);
        // 首次实例化加载
        if (!refreshMap && map == null){
            this.reloadMap();
        }
    }
    private static final MapCache instance = new MapCache();
    public static MapCache getInstance() {
        return instance;
    }
}
