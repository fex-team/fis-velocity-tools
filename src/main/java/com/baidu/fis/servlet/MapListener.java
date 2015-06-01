package com.baidu.fis.servlet;

import com.baidu.fis.util.MapCache;
import com.baidu.fis.util.Settings;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpServletRequest;

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

    public void contextInitialized(ServletContextEvent event) {
        ServletContext ctx = event.getServletContext();
        Settings.init(ctx);
        MapCache mc = MapCache.getInstance();
        mc.init(ctx);

        System.out.println("Start to listen directory : " + mc.mapPath);
        timer.schedule(new ListenerTask(mc.mapPath), sec_start, sec);
    }

    public void contextDestroyed(ServletContextEvent event) {
        event.getServletContext().log("Stop listener for :" +  MapCache.getInstance().mapPath);
        timer.cancel();
    }
}