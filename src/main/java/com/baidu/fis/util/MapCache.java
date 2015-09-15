package com.baidu.fis.util;

import com.alibaba.fastjson.JSONObject;
import com.baidu.fis.velocity.dest.RouterMapManager;
import com.baidu.fis.velocity.dest.ThreadRequestUtil;

import javax.servlet.ServletContext;
import java.io.*;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by xuchenhui on 2015/5/25.
 */
public class MapCache {

    /**
     * WEB所在的本地资源
     */
    public static final String FESERVER_LOCAL = "localhost";

    // 缓存并操控map表
    public static JSONObject map = null;
    //public void setMap(JSONObject newMap){ map = newMap; }

    public void reloadMap(){
        String dir = Settings.getMapDir();
        if (map != null){
            System.out.println("Reload all map files in " + dir + "[" + map.hashCode() + "]");
        }

        try{
            map = loadAllMap(dir);
            System.out.println("Reload finished all maps [" + map.hashCode() + "]");
        }catch(Exception e){
            // 捕获可能的异常，不影响下次map的重新加载，否则导致当前线程退出，就不会再加载了。
            System.err.println("Failed to reload all maps: " + e.getMessage());
            e.printStackTrace();
        }

        // 将本地放入到HashMap中
        mapList.put(FESERVER_LOCAL, map);
    }
    public JSONObject getMap(){
        return map;
    }

    // 向后兼容旧方法
    public JSONObject getMap(String id){

        // 根据IP请求获取VM服务器的地址
        String feServerId = ThreadRequestUtil.getThreadFeServerId();
        JSONObject feServerMap = null;

        if(feServerId == null){
            // 从本地索取
            feServerId = FESERVER_LOCAL;
            feServerMap = mapList.get(feServerId);
        }else{
            // 从指定FeServer索取
            feServerMap = mapList.get(feServerId);

            boolean searchFromNextLoader = RouterMapManager.getInstance().getSearchFromNextLoader();
            boolean notExistedInFeServerMap = feServerMap.containsKey("res") && !feServerMap.getJSONObject("res").containsKey(id);
            boolean existedInLocalServerMap = notExistedInFeServerMap && mapList.get(FESERVER_LOCAL).containsKey("res") &&  mapList.get(FESERVER_LOCAL).getJSONObject("res").containsKey(id);

            if(notExistedInFeServerMap && searchFromNextLoader && existedInLocalServerMap){
                System.out.format("FIS use local resource, feServerId[%s] does not exists:%s \n", feServerId, id);
                // 如果启用该功能，并且FeServer不存在，则支持从本地索取
                feServerMap = mapList.get(FESERVER_LOCAL);
            }
        }


        if(feServerMap == null){
            String message = "Can't find MapCache for feServerId: " + feServerId;
            System.err.println(message);
        }

        return feServerMap;
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
    protected JSONObject loadAllMap(String filePath) throws Exception{
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
    protected JSONObject loadJson(File file) throws Exception {
        FileInputStream input = null;

        try {
            if (file.canRead()) {
                //System.out.println("Read map file : " + file.toPath());
                input = new FileInputStream(file);
            }
        } catch (Exception ex) {
            System.out.println("Error while load " + file.getAbsolutePath() + ".\n Error:" + ex.getMessage());
            input = null;
        }

        if (input == null) {
            return null;
        }

        try{
            String data = readStream(input);
            if (data != null) {
                return JSONObject.parseObject(data);
            }
        }catch(Exception e){
            String msg = "Error while parse JSON file: " + file.getName() + e.getMessage();
            System.err.println(msg);
            throw new Exception(msg);
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
    protected MapCache() {}
    private static MapCache instance = null;

    protected static Map<String, JSONObject> mapList = new HashMap<String, JSONObject>();

    public static synchronized MapCache getInstance() {
        if (instance == null) {
            instance = new RemoteMapCache();
        }
        return instance;
    }
}
