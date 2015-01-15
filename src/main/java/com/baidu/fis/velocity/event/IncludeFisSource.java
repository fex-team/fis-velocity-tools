package com.baidu.fis.velocity.event;

import com.baidu.fis.util.Resource;

public class IncludeFisSource implements org.apache.velocity.app.event.IncludeEventHandler {

    protected Resource fisResource = null;

    public Resource getFisResource() {
        return fisResource;
    }

    public void setFisResource(Resource fisResource) {
        this.fisResource = fisResource;
    }

    @Override
    public String includeEvent(String includeResourcePath, String currentResourcePath, String directiveName) {

        String uri = null;

        try {
            uri = fisResource.getUri(includeResourcePath);
        } catch (Exception err) {
            // System.out.println(err.getMessage());
        }

        if (uri != null) {
            return uri;
        }

        if (includeResourcePath.startsWith("/") || includeResourcePath.startsWith("\\") ) {
            return includeResourcePath;
        }

        int lastslashpos = Math.max(
                currentResourcePath.lastIndexOf("/"),
                currentResourcePath.lastIndexOf("\\")
        );


        // root of resource tree
        if (lastslashpos == -1) {
            return includeResourcePath;
        }

        // prepend path to the include path
        return currentResourcePath.substring(0,lastslashpos) + "/" + includeResourcePath;
    }
}
