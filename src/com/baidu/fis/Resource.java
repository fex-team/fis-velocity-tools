package com.baidu.fis;

import java.io.Writer;

/**
 * Created by 2betop on 4/29/14.
 */
public class Resource {

    protected String mapDir = null;
    protected String framework = null;


    public Resource() {

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
