package com.baidu.fis.velocity;

import org.apache.velocity.runtime.RuntimeServices;

import java.io.FileNotFoundException;
import java.io.Writer;

/**
 * Created by 2betop on 4/30/14.
 */
public class ResourceSingleton {
    private static Resource res;

    public static Resource getInstance() {
        if (res == null) {
            res =  new Resource();
        }

        return res;
    }

    public static void init(RuntimeServices rs) {
        getInstance().init(rs);
    }

    public static void reset() {
        getInstance().reset();
    }

    public static void setFramework(String framework) {
        getInstance().setFramework(framework);
    }

    public static String renderCSS() {
        return getInstance().renderCSS();
    }

    public static String renderJS() {
        return getInstance().renderJS();
    }

    public static void  addResource(String id) throws FileNotFoundException{
        getInstance().addResource(id);
    }

    public static void addJS(String uri) {
        getInstance().addJS(uri);
    }

    public static void addJSEmbed(String content) {
        getInstance().addJSEmbed(content);
    }

    public static void addCSS(String uri) {
        getInstance().addCSS(uri);
    }

    public static void addCSSEmbed(String content) {
        getInstance().addCSSEmbed(content);
    }

    public static String getUri(String uri) {
        return getInstance().getUri(uri);
    }
}
