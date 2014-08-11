package com.baidu.fis.velocity.util;


import org.apache.commons.collections.map.HashedMap;
import org.apache.velocity.context.InternalContextAdapter;

import java.util.Map;

/**
 * Created by 2betop on 8/11/14.
 */
public class ResourceManager {

    private static final String KEY = Resource.class.toString();

    public static Resource ref(InternalContextAdapter ctx) {
        Resource resource = (Resource)ctx.get(KEY);

        if (resource == null) {
            resource = new Resource();
            ctx.put(KEY, resource);
        }

        resource.refs++;

        return resource;
    }

    public static void unRef(InternalContextAdapter ctx) {
        Resource resource = (Resource)ctx.get(KEY);

        if (resource == null) {
            return;
        }

        if (--resource.refs == 0) {
            ctx.remove(KEY);
        }
    }
}
