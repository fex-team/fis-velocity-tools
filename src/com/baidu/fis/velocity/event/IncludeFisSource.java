package com.baidu.fis.velocity.event;

import com.baidu.fis.velocity.ResourceSingleton;

/**
 * Created by 2betop on 5/8/14.
 */
public class IncludeFisSource implements org.apache.velocity.app.event.IncludeEventHandler {


    @Override
    public String includeEvent(String includeResourcePath, String currentResourcePath, String directiveName) {

        System.out.println("include Path" + includeResourcePath);
        System.out.println("Current path" + currentResourcePath);

        if ( includeResourcePath.contains(":") ) {
            return "/templates/" + ResourceSingleton.getUri(includeResourcePath);
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

        System.out.println( currentResourcePath.substring(0,lastslashpos) + "/" + includeResourcePath );

        // prepend path to the include path
        return currentResourcePath.substring(0,lastslashpos) + "/" + includeResourcePath;
    }
}
