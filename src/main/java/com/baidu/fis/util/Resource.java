package com.baidu.fis.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * Created by 2betop on 4/29/14.
 *
 * Fis 资源管理
 */
public class Resource {

    protected static class Res {
        private String value;
        private String prefix;
        private String affix;

        public Res(String value) {
            init(value, null, null);
        }

        public Res(String value, String prefix) {
            init(value, prefix, null);
        }

        public Res(String value, String prefix, String affix) {
            init(value, prefix, affix);
        }

        public void init(String url, String prefix, String affix) {
            this.value = url;
            this.prefix = prefix;
            this.affix = affix;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getPrefix() {
            return prefix;
        }

        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }

        public String getAffix() {
            return affix;
        }

        public void setAffix(String affix) {
            this.affix = affix;
        }

        public int myCode() {
            int result = prefix != null ? prefix.hashCode() : 0;
            result = 31 * result + (affix != null ? affix.hashCode() : 0);
            return result;
        }
    }

    public static final String STYLE_PLACEHOLDER = "<!--FIS_STYLE_PLACEHOLDER-->";
    public static final String SCRIPT_PLACEHOLDER = "<!--FIS_SCRIPT_PLACEHOLDER-->";
    public static final String FRAMEWORK_PLACEHOLDER = "<!--FIS_FRAMEWORK_PLACEHOLDER-->";
    public static final String FRAMEWORK_CONFIG = "<!--FIS_FRAMEWORK_CONFIG-->";

    protected String framework = null;
    protected MapJson map = null;
    protected Map<String, Boolean> loaded;
    protected Map<String, ArrayList<Res>> collection;
    protected Map<String, ArrayList<Res>> embed;
    public int refs = 0;
    public Boolean ignorePkg = false;

    // velocity 入口
    public Resource() {
        this.loaded = new HashMap<String, Boolean>();
        this.collection = new HashMap<String, ArrayList<Res>>();
        this.embed = new HashMap<String, ArrayList<Res>>();
        this.map = new MapJson();

        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

            if (request.getParameter("debug") != null) {
                ignorePkg = true;
            }
        } catch (Exception err) {
            // do nothing.
            ignorePkg = Settings.getBoolean("debug", false);
        }
    }

    // jsp 入口
    public Resource(ServletRequest request) {
        this.loaded = new HashMap<String, Boolean>();
        this.collection = new HashMap<String, ArrayList<Res>>();
        this.embed = new HashMap<String, ArrayList<Res>>();
        this.map = new MapJson();

        ignorePkg = request.getParameter("debug") != null;
    }

    public String getFramework() {
        return framework;
    }

    public void setFramework(String framework) {
        this.framework = framework;
    }

    public void addJS(String id) {
        addJS(id, null, null);
    }

    public void addJS(String id, String prefix) {
        addJS(id, prefix, null);
    }

    public void addJS(String id, String prefix, String affix) {
       if (id.contains(":") && !id.contains(":/") && !id.contains(":\\")) {
           this.addResource(id);
       } else {
           String type = "js";
           ArrayList<Res> list = collection.get(type);

           if (list == null) {
               list = new ArrayList<Res>();
               collection.put(type, list);
           }

           list.add(new Res(id, prefix, affix));
       }
    }

    public void addJSEmbed(String content) {
        addJSEmbed(content, null, null);
    }

    public void addJSEmbed(String content, String prefix) {
        addJSEmbed(content, prefix, null);
    }

    public void addJSEmbed(String content, String prefix, String affix) {
        ArrayList<Res> list = embed.get("js");

        if (list == null) {
            list = new ArrayList<Res>();
            embed.put("js", list);
        }

        list.add(new Res(content, prefix, affix));
    }

    public void addCSS(String id) {
        addCSS(id, null, null);
    }

    public void addCSS(String id, String prefix) {
        addCSS(id, prefix, null);
    }

    public void addCSS(String id, String prefix, String affix) {
        if (id.contains(":") && !id.contains(":/") && !id.contains(":\\")) {
            this.addResource(id);
        } else {
            String type = "css";
            ArrayList<Res> list = collection.get(type);

            if (list == null) {
                list = new ArrayList<Res>();
                collection.put(type, list);
            }

            list.add(new Res(id, prefix, affix));
        }
    }

    public void addCSSEmbed(String content) {
        addCSSEmbed(content, null, null);
    }

    public void addCSSEmbed(String content, String prefix) {
        addCSSEmbed(content, prefix, null);
    }

    public void addCSSEmbed(String content, String prefix, String affix) {
        ArrayList<Res> list = embed.get("css");

        if (list == null) {
            list = new ArrayList<Res>();
            embed.put("css", list);
        }

        list.add(new Res(content, prefix, affix));
    }

    public Boolean contains(String id) {
        JSONObject info = map.getMap(id);
        return info != null && info.containsKey("res") && info.getJSONObject("res").containsKey(id);
    }

    public String addResource(String id){
        return this.addResource(id, false, false, null, null);
    }

    public String addResource(String id, Boolean deffer) {
        return this.addResource(id, deffer, false, null, null);
    }

    public String addResource(String id, Boolean deffer, Boolean drop) {
        return this.addResource(id, deffer, drop, null, null);
    }

    public String addResource(String id, Boolean deffer, Boolean drop, String prefix) {
        return this.addResource(id, deffer, drop, prefix, null);
    }

    public String addResource(String id, Boolean deffer, Boolean drop, String prefix, String affix) {
        JSONObject info, node;
        String uri;

        if (!contains(id)) {
            return id;
        }

        // 如果添加过了而且添加的方式也相同则不重复添加。（这里说的方式是指，同步 or 异步）
        // 如果之前是同步的这次异步添加则忽略掉。都同步添加过了，不需要异步再添加一次。
        // 注意：null 不能直接用来和 false\true 比较，否则报错。
        if ( loaded.get(id) != null && loaded.get(id) == deffer ||
                deffer && loaded.get(id) != null && !loaded.get(id) ) {
            return getUri(id, true);
        } else if (loaded.get(id) != null && !deffer && loaded.get(id)) {
            // 如果之前是异步加载，这次是同步加载。
            removeDefferFromList(id);
            loaded.remove(id);
        }

        info = map.getNode(id);
        String pkg = (String) info.get("pkg");

        if (!ignorePkg && pkg != null) {
            info = map.getNode(pkg, "pkg");
            uri = info.getString("uri");

            if (info.containsKey("has")) {
                JSONArray has = info.getJSONArray("has");

                for (Object obj : has) {
                    loaded.put(obj.toString(), deffer);

                    if (deffer && info.get("type").toString().equals("js")) {
                        ArrayList<Res> list = collection.get("jsDeffer");

                        if (list == null) {
                            list = new ArrayList<Res>();
                            collection.put("jsDeffer", list);
                        }

                        if (!drop) {
                            list.add(new Res(obj.toString(), prefix, affix));
                        }
                    }
                }
            }
        } else {
            uri = info.getString("uri");
            loaded.put(id, deffer);
        }


        try {
            // 如果有异步依赖，则添加异步依赖
            if (info.containsKey("extras")) {
                node = info.getJSONObject("extras");
                if (node.containsKey("async")) {
                    JSONArray async = node.getJSONArray("async");
                    for (Object dep : async) {
                        this.addResource(dep.toString(), true);
                    }
                }
            }

            // 如果有同步依赖，则把同步依赖也添加进来。
            if (info.containsKey("deps")) {
                JSONArray deps = info.getJSONArray("deps");
                for (Object dep : deps) {
                    this.addResource(dep.toString(), deffer);
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        String type = info.get("type").toString();

        if (type.equals("js") && deffer) {
            type = "jsDeffer";

            // 如果是异步 js，用 id 代替 uri。因为还要生成依赖。
            // 注意：此处 uri 已不再是 uri。
            uri = id;
        }

        ArrayList<Res> list = collection.get(type);

        if (list == null) {
            list = new ArrayList<Res>();
            collection.put(type, list);
        }

        if (!drop) {
            list.add(new Res(uri, prefix, affix));
        }

        return uri;
    }

    protected Res findResInList(ArrayList<Res> list, String uri) {
        for (Res res:list) {
            if (res.getValue().equals(uri)) {
                return res;
            }
        }

        return null;
    }

    protected void removeDefferFromList(String id) {
        JSONObject info, node;
        String uri;

        if (!contains(id)) {
            return;
        }

        ArrayList<Res> list = collection.get("jsDeffer");
        if (list == null) {
            return;
        }

        info = map.getNode(id);
        String pkg = (String) info.get("pkg");

        if (!ignorePkg && pkg != null) {
            info = map.getNode(pkg, "pkg");
            uri = info.getString("uri");
            Res res = findResInList(list, uri);

            // if found, then remove it.
            if (res != null) {
                list.remove(res);
            }

            if (info.containsKey("has")) {
                JSONArray has = info.getJSONArray("has");

                for (Object obj : has) {
                    removeDefferFromList(obj.toString());
                }
            }
        } else {
            uri = info.getString("uri");
            Res res = findResInList(list, uri);

            // if found, then remove it.
            if (res != null) {
                list.remove(res);
            }
        }

        // 如果有同步依赖，则把同步依赖也添加进来。
        if (info.containsKey("deps")) {
            JSONArray deps = info.getJSONArray("deps");
            for (Object dep : deps) {
                removeDefferFromList(dep.toString());
            }
        }
    }

    public Boolean exists(String id) {
        return getUri(id) != null;
    }

    public String getUri(String id) {
        return getUri(id, false);
    }

    public String getUri(String id, Boolean usePkg) {
        JSONObject node = map.getNode(id);

        if (node == null) {
            return null;
        }

        if (usePkg && !ignorePkg) {
            String pkg = (String) node.get("pkg");

            if (pkg != null) {
                node = map.getNode(pkg, "pkg");
            }
        }

        return node.getString("uri");
    }

    public String renderCSS() {
        StringBuilder sb = new StringBuilder();
        ArrayList<Res> arr = collection.get("css");

        if (arr != null) {
            for (Res res : arr) {
                if (res.prefix != null) {
                    sb.append(res.prefix);
                }

                sb.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"");
                sb.append(res.value);
                sb.append("\"/>");

                if (res.affix != null) {
                    sb.append(res.affix);
                }
            }
        }

        ArrayList<Res> list = embed.get("css");

        if (list != null && !list.isEmpty()) {
            Stack<StringBuilder> group = new Stack<StringBuilder>();
            Res lastRes = null;

            for (Res item:list) {
                if (lastRes == null || item.myCode() != lastRes.myCode()) {
                    if (lastRes != null) {
                        group.lastElement().append("</style>");
                        if (lastRes.affix != null) {
                            group.lastElement().append(lastRes.affix);
                        }
                    }

                    group.add(new StringBuilder());
                    if (item.prefix != null) {
                        group.lastElement().append(item.prefix);
                    }
                    group.lastElement().append("<style type=\"text/css\">");
                }

                group.lastElement().append(item.value);
                lastRes = item;
            }

            group.lastElement().append("</style>");
            if (lastRes != null && lastRes.affix != null) {
                group.lastElement().append(lastRes.affix);
            }

            for (StringBuilder item:group) {
                sb.append(item);
            }
        }

        return sb.toString();
    }

    protected String modJs = "";
    public String renderFrameWork() {
        StringBuilder sb = new StringBuilder();
        ArrayList<Res> arr = collection.get("js");

        Boolean needModJs = framework != null && (arr != null && !arr.isEmpty() || collection.get("jsDeffer") != null);

        if (needModJs) {
            modJs = addResource(framework, false, true);
            sb.append("<script type=\"text/javascript\" src=\"");
            sb.append(modJs);
            sb.append("\"></script>");
        }

        return sb.toString();
    }

    public String renderFrameworkConfig() {
        StringBuilder sb = new StringBuilder();

        if (collection.get("jsDeffer") != null) {
            Boolean useAmd = !(framework != null && framework.endsWith("mod.js"));

            if (!useAmd && Settings.getBoolean("sourceMap", true)) {
                Map<String, Map> defferMap = this.buildDefferMap();
                sb.append("<script type=\"text/javascript\">require.resourceMap(");
                sb.append(JSONObject.toJSON(defferMap));
                sb.append(");</script>");
            } else {
                // 输出 amd 方式 require.config({paths: {}});
                Map<String, String> paths = this.buildAmdPaths();
                sb.append("<script type=\"text/javascript\">require.config({paths:");
                sb.append(JSONObject.toJSON(paths));
                sb.append("});</script>");
            }
        }

        return sb.toString();
    }

    public String renderJS() {
        StringBuilder sb = new StringBuilder();
        ArrayList<Res> arr = collection.get("js");

        if (arr != null) {
            for (Res res : arr) {
                if (res.value.equals(modJs)) {
                    continue;
                }
                if (res.prefix != null) {
                    sb.append(res.prefix);
                }
                sb.append("<script type=\"text/javascript\" src=\"");
                sb.append(res.value);
                sb.append("\"></script>");
                if (res.affix != null) {
                    sb.append(res.affix);
                }
            }
        }

        // 输出 embed js
        ArrayList<Res> list = embed.get("js");

        if (list != null && !list.isEmpty()) {
            Stack<StringBuilder> group = new Stack<StringBuilder>();
            Res lastRes = null;

            for (Res item:list) {
                if (lastRes == null || item.myCode() != lastRes.myCode()) {
                    if (lastRes != null) {
                        group.lastElement().append("</script>");
                        if (lastRes.affix != null) {
                            group.lastElement().append(lastRes.affix);
                        }
                    }

                    group.add(new StringBuilder());
                    if (item.prefix != null) {
                        group.lastElement().append(item.prefix);
                    }
                    group.lastElement().append("<script type=\"text/javascript\">");
                }

                group.lastElement().append(";").append(item.value);
                lastRes = item;
            }

            group.lastElement().append("</script>");
            if (lastRes != null && lastRes.affix != null) {
                group.lastElement().append(lastRes.affix);
            }

            for (StringBuilder item:group) {
                sb.append(item);
            }
        }

        return sb.toString();
    }

    /**
     * 生成异步JS资源表。
     * @return
     */
    protected Map<String, Map> buildDefferMap() {
        Map<String, Map> defferMap = new HashMap<String, Map>();
        Map<String, JSONObject> res = new HashMap<String, JSONObject>();
        Map<String, JSONObject> pkgMap = new HashMap<String, JSONObject>();

        ArrayList<Res> list = collection.get("jsDeffer");
        JSONObject info;

        if (list != null) {

            for (Res item : list) {

                // 已经同步加载，则忽略。
                if (loaded.get(item.value) != null && !loaded.get(item.value)) {
                    continue;
                }

                info = map.getNode(item.value);


                if (info == null) {
                    throw new IllegalArgumentException("missing resource [" + item.value + "]");
                }

                // 先加 res
                String pkg = info.getString("pkg");

                JSONObject infoCopy = new JSONObject();
                infoCopy.put("url", info.getString("uri"));

                // 保留 pkg 信息
                if (!ignorePkg && pkg != null) {
                    infoCopy.put("pkg", pkg);
                }

                // 过滤掉非 .js 的依赖。
                // 同时过滤掉已经同步加载的依赖。
                if (info.containsKey("deps")) {
                    JSONArray deps = info.getJSONArray("deps");
                    JSONArray depsFilter = new JSONArray();

                    for (Object dep : deps) {
                        String sDep = dep.toString();

                        if (!sDep.endsWith(".js")) {
                            continue;
                        } else if ( loaded.get(sDep) != null && !loaded.get(sDep)) {

                            // 同步中已经依赖。
                            continue;
                        }

                        depsFilter.add(sDep);
                    }

                    if (!depsFilter.isEmpty()) {
                        infoCopy.put("deps", depsFilter);
                    }
                }

                // 再把对应的 pkg 加入。
                if (!ignorePkg && pkg != null) {
                    info = map.getNode(pkg, "pkg");

                    info.put("url", info.getString("uri"));
                    info.remove("uri");

                    pkgMap.put(pkg, info);
                }

                res.put(item.value, infoCopy);
            }
        }

        if (!res.isEmpty()) {
            defferMap.put("res", res);
        }

        if (!pkgMap.isEmpty()) {
            defferMap.put("pkg", pkgMap);
        }

        return defferMap;
    }

    public Map<String, String> buildAmdPaths() {
        Map<String, String> paths = new HashMap<String, String>();
        JSONObject info;

        for (String id : loaded.keySet()) {
            if (loaded.get(id) == null || !loaded.get(id)) {
                continue;
            }

            // 异步依赖
            info = map.getNode(id);

            if (!info.getString("type").equals("js")) {
                continue;
            }


            if (info.containsKey("extras")) {
                String uri = info.getString("uri");

                    if (!ignorePkg && info.containsKey("pkg")) {
                        JSONObject pkg = map.getNode(info.getString("pkg"), "pkg");
                        uri = pkg.getString("uri");
                    }

                if (uri.endsWith(".js")) {
                    uri = uri.substring(0, uri.length() - 3);
                }

                if (info.getJSONObject("extras").containsKey("moduleId")) {
                    paths.put(info.getJSONObject("extras").getString("moduleId"), uri);
                }
            }

        }

        return paths;
    }

    public String filterContent(String input) {

        if (input.contains(Resource.FRAMEWORK_PLACEHOLDER)) {
            input = input.replace(Resource.FRAMEWORK_PLACEHOLDER, renderFrameWork());
        }

        if (input.contains(Resource.FRAMEWORK_CONFIG)) {
            input = input.replace(Resource.FRAMEWORK_CONFIG, renderFrameworkConfig());
        }

        if (input.contains(Resource.SCRIPT_PLACEHOLDER)) {
            input = input.replace(Resource.SCRIPT_PLACEHOLDER, renderJS());
        }

        if (input.contains(Resource.STYLE_PLACEHOLDER)) {
            input = input.replace(Resource.STYLE_PLACEHOLDER, renderCSS());
        }

        return input;
    }

}
