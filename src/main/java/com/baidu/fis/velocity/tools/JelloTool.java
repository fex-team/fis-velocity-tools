package com.baidu.fis.velocity.tools;

import com.alibaba.fastjson.JSON;
import org.apache.velocity.tools.config.DefaultKey;

@DefaultKey("jello")
public class JelloTool {
    public static String jsonEncode(Object obj) {
        return JSON.toJSONString(obj);
    }
}
