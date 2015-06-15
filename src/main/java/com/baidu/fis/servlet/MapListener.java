package com.baidu.fis.servlet;

import com.baidu.fis.util.MapCache;
import com.baidu.fis.util.Settings;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import java.util.Timer;

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
    private int sec = 1000*10;

    /**
     * 启动服务后多久开始进行监听
     */
    private int sec_start = 1000*30;

    public void contextInitialized(ServletContextEvent event) {
        ServletContext ctx = event.getServletContext();
        Settings.init(ctx);

        sec = Integer.parseInt(Settings.getString("listener.interval", "10")) * 1000;
        sec_start = Integer.parseInt(Settings.getString("listener.start", "30")) * 1000;

        MapCache mc = MapCache.getInstance();
        mc.init(ctx);

        System.out.println("Start to listen directory : " + Settings.getMapDir());
        ListenerTask task = new ListenerTask(Settings.getMapDir());
        timer.schedule(task, sec_start, sec);

        Settings.setApplicationAttribute(ListenerTask.class.getName(), task);
    }

    public void contextDestroyed(ServletContextEvent event) {
        event.getServletContext().log("Stop listener for :" +  Settings.getMapDir());
        timer.cancel();
    }
}