package com.baidu.fis.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.velocity.exception.VelocityException;

import java.io.*;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

/**
 * 可以获取远端Server的MAP JSON.
 * Created by yongqingdong on 2015/9/8.
 */
public class RemoteMapCache extends MapCache {

    /**
     * 获取MAP的JSON文件名列表
     */
    public static String URL_MAP_JSON_LIST = "http://$0/manage/map";

    /**
     * 获取MAP的每个JSON文件
     */
    public static String URL_MAP_JSON_FILE = "http://$0/map/$1";

    public void initFeServerMap(String feServerId) throws Exception{

        JSONObject feServerMap = this.fillFeServerMap(feServerId);

        this.mapList.put(feServerId, feServerMap);
    }

    /**
     * 根据ID获取并填充MAP。
     * @param feServerId
     * @return
     */
    public JSONObject fillFeServerMap(String feServerId) throws Exception{
        // 要返回的JSON MAP
        JSONObject feServerMap = new JSONObject();
        JSONObject resMap = new JSONObject();
        JSONObject pkgMap = new JSONObject();

        feServerMap.put("res", resMap);
        feServerMap.put("pkg", pkgMap);

        List<String> jsonFileList = this.getMapJsonList(feServerId);

        for(String jsonFileName : jsonFileList){
            JSONObject jsonFileMap = this.getJsonByMapFile(feServerId, jsonFileName);

            feServerMap.getJSONObject("res").putAll(jsonFileMap.getJSONObject("res"));
            feServerMap.getJSONObject("pkg").putAll(jsonFileMap.getJSONObject("pkg"));
        }

        return feServerMap;
    }

    /**
     * 获取MAP的JSON文件列表
     * @param feServerId
     * @return
     */
    public List<String> getMapJsonList(String feServerId) throws Exception{

        List<String> jsonFileNameList = null;

        String http = URL_MAP_JSON_LIST.replaceAll("\\$0", feServerId);
        JSONObject jsonRes = this.getResponseForURL(http, null);
        System.out.format("HTTP Server[%s] Map JSON File List: [%s].", feServerId, jsonRes);

        jsonFileNameList = (List<String>)jsonRes.get("data");

        return jsonFileNameList;
    }

    /**
     * 根据JSON文件名获取JSONObject映射对象
     * @param filename
     * @return
     */
    public JSONObject getJsonByMapFile(String feServerId, String filename) throws Exception{

        String http = URL_MAP_JSON_FILE.replaceAll("\\$0", feServerId).replaceAll("\\$1", filename);
        JSONObject jsonRes = this.getResponseForURL(http, null);
        System.out.format("HTTP Server[%s] Map JSON File: [%s].", feServerId, filename);

        return jsonRes;
    }


    public JSONObject getResponseForURL(String sURL, String data) throws Exception{

        JSONObject res = null;
        int timeout = 1000 * 10;

        // Convert
        data = data == null ? "" : data;

        // 1. 设置Timeout
        Method[] timeoutMethods = null;
        if (timeout > 0) {
            try {
                Class[] types = new Class[]{Integer.TYPE};
                Method conn = URLConnection.class.getMethod("setConnectTimeout", types);
                Method read = URLConnection.class.getMethod("setReadTimeout", types);
                timeoutMethods = new Method[]{conn, read};
            } catch (NoSuchMethodException nsme) {
                System.err.println("ResourceManager : Java 1.5+ is required to customize timeout!\n" + nsme.getMessage());
                timeout = -1;
            }
        }

        // 2. 发送请求Server
        URL u = new URL(sURL);
        URLConnection conn = u.openConnection();
        HttpURLConnection httpConn = (HttpURLConnection)conn;

        // POST
        httpConn.setDoOutput(true);
        httpConn.setRequestMethod("GET");
        httpConn.setRequestProperty("Accept-Charset", "utf-8");

        if("POST".equals(httpConn.getRequestMethod())){
            httpConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            httpConn.setRequestProperty("Content-Length", String.valueOf(data.length()));

            // Append Param data.
            OutputStream outputStream = httpConn.getOutputStream();
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
            outputStreamWriter.write(data.toString());
            outputStreamWriter.flush();
        }

        tryToSetTimeout(conn, timeout, timeoutMethods);
        InputStream inputStream = conn.getInputStream();
        StringBuffer responseBuffer = new StringBuffer();

        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String tempLine;
            while ((tempLine = reader.readLine()) != null) {
                responseBuffer.append(tempLine);
            }
            System.out.println("HTTP Response output: " + responseBuffer.toString());

            // 3.解析返回的数据
            res = JSONObject.parseObject(responseBuffer.toString());
        }

        // System.err.println("HTTP Response exception: " + ioe.getMessage());
        return res;
    }

    private void tryToSetTimeout(URLConnection conn, int timeout, Method[] timeoutMethods) {
        if (timeout > 0) {
            Object[] arg = new Object[]{new Integer(timeout)};
            try {
                timeoutMethods[0].invoke(conn, arg);
                timeoutMethods[1].invoke(conn, arg);
            } catch (Exception e) {
                String msg = "Unexpected exception while setting connection timeout for " + conn;
                System.err.println(msg);
            }
        }
    }
}
