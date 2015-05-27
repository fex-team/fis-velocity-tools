package com.baidu.fis.servlet;

/**
 * Created by xuchenhui on 2015/5/25.
 */

import com.baidu.fis.util.MapCache;

import java.io.File;
import java.util.TimerTask;
import java.util.Observable;

/**
 * 自动监听任务
 *
 * @author litao
 *
 */
@SuppressWarnings("unchecked")
public class ListenerTask extends TimerTask {
    private String path;
    private File files;
    private String[] filelist;
    private long lastModified;
    /**
     * 构造一个自动更新任务
     *
     * @param path
     */
    public ListenerTask(String path) {

        this.path = path;
        this.lastModified = System.currentTimeMillis();
        this.files = new File(path);
        if (files.isDirectory()) {
            this.filelist = files.list();
        }
    }
    public void run() {
        docheck();
    }
    protected void setRefresh(){
        MapCache mc = MapCache.getInstance();
        mc.reloadMap();
    }
    /**
     * 监听文件夹下的文件是否被更新。
     */
    private void docheck() {
        if (files.isDirectory()) {
            String[] currentFiles = files.list();
            if (filelist.length == currentFiles.length){
                // 对比文件
                for (int i = 0; i < currentFiles.length; i++) {
                    File file = new File(path + currentFiles[i]);
                    while (isFileUpdated(file)) {
                        setRefresh();
                        System.out.println(currentFiles[i]+" changed");
                        break;
                    }
                }
            } else {
                setRefresh();
                System.out.println("Directory changed");
                filelist = currentFiles;
            }
        }else{
            System.out.println("Directory not found");
        }
    }
    /**
     * 判断物理文件是否已被更新
     *
     * @param file 物理文件名
     * @return 是 true 否 false
     */
    private boolean isFileUpdated(File file) {
        if (file.isFile()) {
            long lastUpdateTime = file.lastModified();
            if (lastUpdateTime > this.lastModified) {
                this.lastModified = lastUpdateTime;
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}