package com.baidu.fis;

import org.apache.velocity.runtime.RuntimeSingleton;
import org.apache.velocity.runtime.log.Log;

import java.io.Writer;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by 2betop on 4/29/14.
 */
public class Resource {

    protected Boolean debug = false;
    protected String mapDir = null;
    protected String framework = null;

    protected Map<String, Map> map;
    protected Map<String, String> loaded;
    protected Map<String, ArrayList<String>> collection;


    public Log log;


    public Resource() {

    }

    public Boolean getDebug() {
        return debug;
    }

    public void setDebug(Boolean debug) {
        this.debug = debug;
    }


    public String getMapDir() {
        return mapDir;
    }

    public void setMapDir(String mapDir) {
        this.mapDir = mapDir;
    }

    public String getFramework() {
        return framework;
    }

    public void setFramework(String framework) {
        this.framework = framework;
    }

    public void addJS(String uri) {

    }

    public void addJSEmbed(String content) {

    }

    public void  addCSS(String uri) {

    }

    public void addCSSEmbed(String content) {

    }

    public void addResource(String uri) {

    }

    public String getUri(String str) {
        return null;
    }

    public void reset() {

    }

    public void renderCSS(Writer writer) {

    }

    public void renderJS(Writer writer) {

    }

}
