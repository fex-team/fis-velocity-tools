package com.baidu.fis.util;

import com.alibaba.fastjson.JSONObject;
import javax.servlet.ServletContext;
import java.io.*;
import java.util.Set;


/**
 * Created by xuchenhui on 2015/5/25.
 */
public class MapCache {
    // 缓存并操控map表
    public static JSONObject map = null;
    //public void setMap(JSONObject newMap){ map = newMap; }
    public void reloadMap(){
        String dir = Settings.getMapDir();
        if (map != null){
            System.out.println("Reload all map files in " + dir + "[" + map.hashCode() + "]");
        }
        map = loadAllMap(dir);
        System.out.println("Reload finished all maps [" + map.hashCode() + "]");
    }
    public JSONObject getMap(){
        return map;
    }
    // 向后兼容旧方法
    public JSONObject getMap(String id){
        return map;
    }
    public void resetMap(){
        map.clear();
    }

    private static JSONObject mergeJSONObjects(JSONObject json1, JSONObject json2) {
        try {
            json1.putAll(json2);
        } catch (Exception e) {
            throw new RuntimeException("JSON Exception" + e);
        }
        return json1;
    }
    // 重新读取所有的map文件并生成map表
    protected JSONObject loadAllMap(String filePath){
        JSONObject resMap = new JSONObject();
        JSONObject pkgMap = new JSONObject();

        JSONObject newMap = new JSONObject();
        newMap.put("res", resMap);
        newMap.put("pkg", pkgMap);

        System.out.println("Load map files in : " + filePath);

        File root = new File(filePath);

        if (!root.exists() || !root.isDirectory()) {

            System.out.println("Map dir is not exists or is not an directory. `" + filePath + "`");

            return newMap;
        }

        File[] files = root.listFiles();

        for(File file:files) {
            if (!file.isDirectory() && file.canRead()) {
                String fileName = file.getName();

                if (fileName.matches(".*\\.json")){
                    JSONObject json = this.loadJson(file);

                    if (json != null) {
                        System.out.println("Load map file : " + fileName);
                        resMap = mergeJSONObjects(resMap, json.getJSONObject("res"));
                        pkgMap = mergeJSONObjects(pkgMap, json.getJSONObject("pkg"));
                    }
                }
            }
        }



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
    protected JSONObject loadJson(File file) {
        FileInputStream input = null;

        try {
            if (file.canRead()) {
                //System.out.println("Read map file : " + file.toPath());
                input = new FileInputStream(file);
            }
        } catch (Exception ex) {
            System.out.println("Got error: while load " + file.getAbsolutePath() + ".\n Error:" + ex.getMessage());
            input = null;
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

    /// 初始化方法
    public void init(ServletContext context){
        // 首次实例化加载
        if (map == null){
            reloadMap();
        }
    }

    // 单例模式
    private MapCache() {}
    private static MapCache instance = null;
    public static synchronized MapCache getInstance() {
        if (instance == null) {
            instance = new MapCache();
        }
        return instance;
    }
}
