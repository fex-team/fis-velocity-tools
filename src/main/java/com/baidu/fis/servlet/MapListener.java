package com.baidu.fis.servlet;

import com.baidu.fis.util.Settings;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import java.util.Timer;
import java.util.TimerTask;

import java.io.File;

/**
 * Created by xuchenhui on 2015/5/25.
 */
public class MapListener implements ServletContextListener {

    /**
     * 自动监听时钟
     */
    private final Timer timer = new Timer();

    /**
     * 时钟间隔周期
     */
    private final int sec = 1000*10;

    /**
     * 启动服务后多久开始进行监听
     */
    private final int sec_start = 1000*30;

    protected String dir = "/WEB-INF/config";
    protected String loaderType = "webapp";
    protected String mapDir = null;

    public void contextInitialized(ServletContextEvent event) {
        ServletContext ctx = event.getServletContext();
        //String mapDir = Settings.getString("mapDir", dir);
        String mdir = ctx.getRealPath("map");

        System.out.println("Start to listen directory : " + mdir);
        //event.getServletContext().log("Start to listen directory : " + mdir);
        timer.schedule(new ListenerTask(mdir), sec_start, sec);
    }

    public void contextDestroyed(ServletContextEvent event) {
        event.getServletContext().log("Stop listener for :" + mapDir);
        timer.cancel();
    }
}