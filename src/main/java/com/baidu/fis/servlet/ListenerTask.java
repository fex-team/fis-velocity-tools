package com.baidu.fis.servlet;

/**
 * Created by xuchenhui on 2015/5/25.
 */

import com.baidu.fis.util.MapCache;
import com.baidu.fis.util.Settings;

import java.io.File;
import java.util.Arrays;
import java.util.TimerTask;

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
    public void docheck() {
        String newPath = Settings.getMapDir();

        if (path == null || !newPath.equals(path)) {
            path = newPath;
            files = new File(newPath);
        }

        if (files.isDirectory() && files.exists()) {
            String[] currentFiles = files.list();

            if (filelist == null || filelist.length != currentFiles.length){
                System.out.println("Directory changed");
                setRefresh();
                filelist = currentFiles;
            }else{
                // 对比文件
                for (int i = 0; i < currentFiles.length; i++) {
                    if (Arrays.binarySearch(filelist, currentFiles[i]) > -1){
                        File file = new File(path, currentFiles[i]);

//                        System.out.println("Check " + file.toPath());
                        if (file.exists() && isFileUpdated(file)) {
                            setRefresh();
                            System.out.println(currentFiles[i]+" changed");
                            break;
                        }
                    }else{
                        System.out.println("File " +  currentFiles[i] + " has be created.");
                        setRefresh();
                        filelist = currentFiles;
                        break;
                    }
                }
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