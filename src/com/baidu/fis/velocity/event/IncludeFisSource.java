package com.baidu.fis.velocity.event;

import com.baidu.fis.velocity.Resource;

/**
 * Created by 2betop on 5/8/14.
 */
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
        if ( includeResourcePath.contains(":") && fisResource != null ) {
            return "/templates/" + fisResource.getUri(includeResourcePath);
        } else if (includeResourcePath.startsWith("/") || includeResourcePath.startsWith("\\") ) {
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
