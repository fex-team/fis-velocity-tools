package com.baidu.fis.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * Created by 2betop on 4/29/14.
 *
 * Fis 资源管理
 */
public class Resource {

    protected static class Res {
        private String content = "";
        private String uri = "";
        private String id = "";
        private String type = "unknown";
        private String prefix = null;
        private String affix = null;
        private Boolean embed = false;
        private Boolean async = false;
        private Boolean isFramework = false;
        private ArrayList<Res> children;

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getUri() {
            return uri;
        }

        public void setUri(String uri) {
            this.uri = uri;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getPrefix() {
            return prefix == null ? "" : prefix;
        }

        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }

        public String getAffix() {
            return affix == null ? "" : affix;
        }

        public void setAffix(String affix) {
            this.affix = affix;
        }

        public Boolean getEmbed() {
            return embed;
        }

        public void setEmbed(Boolean embed) {
            this.embed = embed;
        }

        public Boolean getAsync() {
            return async;
        }

        public void setAsync(Boolean async) {
            this.async = async;
        }

        public ArrayList<Res> getChildren() {
            return children;
        }

        public void setChildren(ArrayList<Res> children) {
            this.children = children;
        }

        public Boolean getIsFramework() {
            return isFramework;
        }

        public void setIsFramework(Boolean isFramework) {
            this.isFramework = isFramework;
        }

        public int fixCode() {
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
    protected MapCache map = null;
    protected Map<String, Boolean> loaded;
    protected ArrayList<Res> res;
    protected ArrayList<Res> js;
    protected ArrayList<Res> css;
    protected ArrayList<Res> asyncs;
    protected Boolean calculated = false;

    public int refs = 0;
    public static Boolean ignorePkg = false;
    public static Boolean inspect = false;
    public Stack<Res> stack = new Stack<Res>();

    // velocity 入口
    public Resource() {
        this.loaded = new HashMap<String, Boolean>();
        this.res = new ArrayList<Res>();

        this.js = new ArrayList<Res>();
        this.css = new ArrayList<Res>();
        this.asyncs = new ArrayList<Res>();

        //this.map = new MapJson();
        this.map = MapCache.getInstance();

        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

            ignorePkg = request.getParameter("debug") != null;
            inspect = request.getParameter("inspect") != null;
        } catch (Exception err) {

            // do nothing.
            ignorePkg = Settings.getBoolean("debug", false);
        }

        if (inspect) {
            Res root = new Res();
            root.id = "root";
            root.uri = "root";
            root.children = new ArrayList<Res>();
            this.stack.push(root);
        }
    }

    // jsp 入口
    public Resource(ServletRequest request) {
        this.loaded = new HashMap<String, Boolean>();
        this.res = new ArrayList<Res>();
        this.js = new ArrayList<Res>();
        this.css = new ArrayList<Res>();
        this.asyncs = new ArrayList<Res>();

        //this.map = new MapJson();
        this.map = MapCache.getInstance();

        ignorePkg = request.getParameter("debug") != null;
        inspect = request.getParameter("inspect") != null;

        if (inspect) {
            Res root = new Res();
            root.id = "root";
            root.uri = "root";
            root.children = new ArrayList<Res>();
            this.stack.push(root);
        }
    }

    public String getFramework() {
        return framework;
    }

    public void setFramework(String framework) {
        this.framework = framework;
    }

    public Boolean contains(String id) {
        JSONObject info = map.getMap(id);
        return info != null && info.containsKey("res") && info.getJSONObject("res").containsKey(id);
    }

    public String add(String id) {
        return add(id, false);
    }

    public String add(String id, Boolean deffer) {
        return add(id, deffer, null);
    }

    public String add(String id, Boolean deffer, String prefix) {
        return add(id, deffer, prefix, null);
    }

    public String add (String id, Boolean deffer, String prefix, String affix) {
        return add(id, deffer, prefix, affix, false);
    }

    public String add(String id, Boolean deffer, String prefix, String affix, Boolean withPkg) {
        if (!contains(id)) {
            return id;
        }

        JSONObject node = map.getNode(id);

        if (node.containsKey("type") && "css".equals(node.getString("type"))) {
            deffer = false;
        }

        // 如果添加过了而且添加的方式也相同则不重复添加。（这里说的方式是指，同步 or 异步）
        // 如果之前是同步的这次异步添加则忽略掉。都同步添加过了，不需要异步再添加一次。
        // 注意：null 不能直接用来和 false\true 比较，否则报错。
        if ( loaded.containsKey(id) && loaded.get(id) == deffer ||
                deffer && loaded.containsKey(id) && !loaded.get(id) ) {
            return getUri(id, true);
        } else if (loaded.containsKey(id) && !deffer && loaded.get(id)) {
            // 如果之前是异步加载，这次是同步加载。
            remove(id, true);
        }

        Res item = new Res();
        item.setId(id);

        if (inspect) {
            if (stack.peek().getChildren() == null) {
                stack.peek().setChildren(new ArrayList<Res>());
            }

            stack.peek().getChildren().add(item);

            stack.add(item);
        }

        loaded.put(id, deffer);

        String pkg = (String) node.get("pkg");

        String uri = node.getString("uri");

        if (withPkg && pkg != null) {
            JSONObject pkgNode = map.getNode(pkg, "pkg");

            uri = pkgNode.getString("uri");

            if (pkgNode.containsKey("has")) {
                for (Object dep: pkgNode.getJSONArray("has")) {
                    loaded.put(dep.toString(), deffer);

                    if (inspect) {
                        Res has = new Res();
                        has.setId(dep.toString());

                        if (item.getChildren() == null) {
                            item.setChildren(new ArrayList<Res>());
                        }

                        item.getChildren().add(has);
                    }
                }
            }
        }

        // 如果有同步依赖，则把同步依赖也添加进来。
        if (node.containsKey("deps")) {
            JSONArray deps = node.getJSONArray("deps");
            for (Object dep : deps) {
                this.add(dep.toString(), deffer, prefix, affix, withPkg);
            }
        }

        // 如果有异步依赖，则添加异步依赖
        if (node.containsKey("extras")) {
            JSONObject extras = node.getJSONObject("extras");
            if (extras.containsKey("async")) {
                JSONArray async = extras.getJSONArray("async");
                for (Object dep : async) {
                    this.add(dep.toString(), true, prefix, affix, withPkg);
                }
            }
        }

        if (inspect) {
            stack.pop();
        }

        String type = node.getString("type");
        if (!type.equals("js") && !type.equals("css")) {
            return getUri(id, true);
        }

        item.setUri(uri);
        item.setPrefix(prefix);
        item.setAffix(affix);
        item.setType(type);
        item.setAsync(deffer);

        res.add(item);

        return getUri(id, true);
    }

    public void remove(String id, Boolean deffer) {
        if (!contains(id)) {
            return;
        }

        JSONObject node = map.getNode(id);

        if (loaded.get(id) != null && loaded.get(id) == deffer) {
            loaded.remove(id);
        }

        if (node.containsKey("extras")) {
            node = node.getJSONObject("extras");
            if (node.containsKey("async")) {
                JSONArray async = node.getJSONArray("async");
                for (Object dep : async) {
                    this.remove(dep.toString(), true);
                }
            }
        }

        if (node.containsKey("deps")) {
            JSONArray deps = node.getJSONArray("deps");
            for (Object dep : deps) {
                this.remove(dep.toString(), deffer);
            }
        }

        String type = node.getString("type");
        if (!type.equals("js") && !type.equals("css")) {
            return;
        }

        for (Res item:res) {
            if (item.getId().equals(id) && item.getAsync() == deffer) {
                res.remove(item);
                break;
            }
        }
    }

    public void addJS(String id) {
        addJS(id, null);
    }

    public void addJS(String id, String prefix) {
        addJS(id, prefix, null);
    }

    public void addJS(String id, String prefix, String affix) {
        if (exists(id)) {
            this.add(id, false, prefix, affix);
        } else {
            Res item = new Res();
            item.setUri(id);
            item.setType("js");
            item.setPrefix(prefix);
            item.setAffix(affix);
            res.add(item);
        }
    }

    public void addJSEmbed(String content) {
        addJSEmbed(content, null, null);
    }

    public void addJSEmbed(String content, String prefix) {
        addJSEmbed(content, prefix, null);
    }

    public void addJSEmbed(String content, String prefix, String affix) {
        Res item = new Res();
        item.setContent(content);
        item.setType("js");
        item.setPrefix(prefix);
        item.setAffix(affix);
        item.setEmbed(true);
        res.add(item);
    }

    public void addCSS(String id) {
        addCSS(id, null);
    }

    public void addCSS(String id, String prefix) {
        addCSS(id, prefix, null);
    }

    public void addCSS(String id, String prefix, String affix) {
        if (exists(id)) {
            this.add(id, false, prefix, affix);
        } else {
            Res item = new Res();
            item.setUri(id);
            item.setType("css");
            item.setPrefix(prefix);
            item.setAffix(affix);
            res.add(item);
        }
    }

    public void addCSSEmbed(String content) {
        addCSSEmbed(content, null, null);
    }

    public void addCSSEmbed(String content, String prefix) {
        addCSSEmbed(content, prefix, null);
    }

    public void addCSSEmbed(String content, String prefix, String affix) {
        Res item = new Res();
        item.setContent(content);
        item.setType("css");
        item.setPrefix(prefix);
        item.setAffix(affix);
        item.setEmbed(true);
        res.add(item);
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
        return add(id, deffer, prefix, affix);
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

    protected void calculate() {
        if (calculated) {
            return;
        }
        calculated = true;

        ArrayList<Res> res = this.res;
        this.res = new ArrayList<Res>();
        this.loaded = new HashMap<String, Boolean>();

        if (inspect) {
            Res root = new Res();
            root.id = "root";
            root.uri = "root";
            root.children = new ArrayList<Res>();
            this.stack.push(root);
        }

        if (this.framework!=null) {
            add(this.framework, false, null, null, !ignorePkg);

            if (this.res.size() > 0) {
                this.res.get(0).setIsFramework(true);
            }
        }

        for (Res item:res) {
            if (!item.getId().isEmpty()) {
                this.add(item.getId(), item.getAsync(), item.getPrefix(), item.getAffix(), !ignorePkg);
            } else {
                this.res.add(item);
            }
        }

        for (Res item:this.res) {
            if (item.getType().equals("js")) {
                if (item.getAsync()) {
                    this.asyncs.add(item);
                } else {
                    this.js.add(item);
                }
            } else if (item.getType().equals("css")) {
                this.css.add(item);
            }
        }
    }

    protected String buildResourceMap() {
        JSONObject res = new JSONObject();
        JSONObject pkg = new JSONObject();

        calculate();

        for (String id: this.loaded.keySet()) {
            Boolean async = loaded.get(id);

            if (!async) {
                continue;
            }

            JSONObject node = map.getNode(id);
            JSONObject item = new JSONObject();
            item.put("url", node.get("uri"));
            item.put("type", node.get("type"));

            if (node.containsKey("deps")) {
                JSONArray deps = node.getJSONArray("deps");
                Iterator<Object> iterator = deps.iterator();

                while (iterator.hasNext()) {
                    String dep = (String) iterator.next();

                    if (loaded.containsKey(id) && !loaded.get(id) || dep.endsWith(".css")) {
                        iterator.remove();
                    }
                }

                item.put("desp", deps);
            }

            res.put(id, item);

            if (node.containsKey("pkg")) {
                item.put("pkg", node.getString("pkg"));
                JSONObject pkgNode = map.getNode(node.getString("pkg"), "pkg");

                JSONObject pkgItem = new JSONObject();
                pkgItem.put("uri", pkgNode.get("uri"));
                pkgItem.put("type", pkgNode.get("type"));

                pkg.put(node.getString("pkg"), pkgItem);
            }
        }

        if (res.isEmpty()) {
            return "";
        }

        JSONObject map = new JSONObject();
        map.put("res", res);

        if (!pkg.isEmpty()) {
            map.put("pkg", pkg);
        }

        return "require.resourceMap(" + JSONObject.toJSON(map) + ");";
    }

    public String buildAMDPath() {
        JSONObject paths = new JSONObject();

        calculate();

        for (String id: this.loaded.keySet()) {
            Boolean async = loaded.get(id);

            if (!async) {
                continue;
            }

            JSONObject node = map.getNode(id);

            if (!node.getString("type").equals("js")) {
                continue;
            }

            String moduleId = node.containsKey("extra") && node.getJSONObject("extra").containsKey("moduleId") ?
                    node.getJSONObject("extra").getString("moduleId") :
                    id.replaceAll("\\.js$", "");
            String uri = node.getString("uri");

            if (node.containsKey("pkg")) {
                JSONObject pkgNode = map.getNode(node.getString("pkg"), "pkg");
                uri = pkgNode.getString("uri");
            }

            paths.put(moduleId, uri.replaceAll("\\.js$", ""));
        }

        if (paths.isEmpty()) {
            return "";
        } else {
            return "require.config({paths: " + JSONObject.toJSON(paths) + "})";
        }
    }

    public String renderFrameWork() {
        for (Res item:this.js) {
            if (item.getIsFramework()) {
                StringBuilder sb = new StringBuilder();

                sb.append("<script type=\"text/javascript\" src=\"");
                sb.append(item.getUri());
                sb.append("\"></script>");

                this.js.remove(item);
                return sb.toString();
            }
        }

        return "";
    }

    public String renderJS() {
        StringBuilder sb = new StringBuilder();

        StringBuilder group = new StringBuilder();
        Res lastItem = null;

        for (Res item:this.js) {
            if (item.getEmbed() && lastItem != null && lastItem.fixCode() == item.fixCode()) {
                group.append(";");
                group.append(item.getContent().trim());
                group.append("\n");
            } else {
                if (group.length() > 0 && lastItem != null) {
                    sb.append(lastItem.getPrefix());
                    sb.append("<script type=\"text/javascript\">");
                    sb.append(group.toString());
                    sb.append("</script>");
                    sb.append(lastItem.getAffix());
                    group = new StringBuilder();
                }

                if (item.getEmbed()) {
                    sb.append(item.getPrefix());
                    sb.append("<script type=\"text/javascript\">");
                    sb.append(item.getContent());
                    sb.append("</script>");
                    sb.append(item.getAffix());
                } else {
                    sb.append(item.getPrefix());
                    sb.append("<script type=\"text/javascript\" src=\"");
                    sb.append(item.getUri());
                    sb.append("\"></script>");
                }
            }

            lastItem = item;
        }

        if (group.length() > 0 && lastItem != null) {
            sb.append(lastItem.getPrefix());
            sb.append("<script type=\"text/javascript\">");
            sb.append(group.toString());
            sb.append("</script>");
            sb.append(lastItem.getAffix());
        }

        return sb.toString();
    }

    public String renderCSS() {
        StringBuilder sb = new StringBuilder();

        StringBuilder group = new StringBuilder();
        Res lastItem = null;

        for (Res item:this.css) {
            if (item.getEmbed() && lastItem != null && lastItem.fixCode() == item.fixCode()) {
                group.append(item.getContent().trim());
                group.append("\n");
            } else {
                if (group.length() > 0 && lastItem != null) {
                    sb.append(lastItem.getPrefix());
                    sb.append("<style type=\"text/css\">");
                    sb.append(group.toString());
                    sb.append("</style>");
                    sb.append(lastItem.getAffix());
                    group = new StringBuilder();
                }

                if (item.getEmbed()) {
                    sb.append(item.getPrefix());
                    sb.append("<style type=\"text/css\">");
                    sb.append(item.getContent());
                    sb.append("</style>");
                    sb.append(item.getAffix());
                } else {
                    sb.append(item.getPrefix());
                    sb.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"");
                    sb.append(item.getUri());
                    sb.append("\" />");
                }
            }

            lastItem = item;
        }

        if (group.length() > 0 && lastItem != null) {
            sb.append(lastItem.getPrefix());
            sb.append("<style type=\"text/css\">");
            sb.append(group.toString());
            sb.append("</style>");
            sb.append(lastItem.getAffix());
        }

        return sb.toString();
    }

    protected String inspectRes(Res item, int depth) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < depth; i++) {
            sb.append("-");
        }

        sb.append(" ");
        sb.append(item.id);

        if (!item.uri.isEmpty()) {
            sb.append("  [");
            sb.append(item.uri);
            sb.append("]");
        }

        if (item.getChildren() != null) {
            for (Res child:item.getChildren()) {
                sb.append("\n");
                sb.append(inspectRes(child, depth + 4));
            }
        }

        return sb.toString();
    }


    public String filterContent(String input) {
        calculate();

        if (inspect) {
            StringBuilder sb = new StringBuilder();

            while (!stack.isEmpty()) {
                sb.append(inspectRes(stack.pop(), 0));
                sb.append("\n\n");
            }

            return sb.toString();
        }

        if (input.contains(Resource.FRAMEWORK_PLACEHOLDER)) {
            input = input.replace(Resource.FRAMEWORK_PLACEHOLDER, renderFrameWork());
        }

        StringBuilder sb = new StringBuilder();

        if (framework != null) {
            String resourcemap = framework.endsWith("mod.js") ? buildResourceMap() : buildAMDPath();

            if (!resourcemap.isEmpty()) {
                if (input.contains(Resource.FRAMEWORK_CONFIG)) {
                    sb.append("<script type=\"text/javascript\">");
                    sb.append(resourcemap);
                    sb.append("</script>");
                } else {
                    Res item = new Res();
                    item.setContent(resourcemap);
                    item.setEmbed(true);
                    item.setType("js");
                    this.js.add(0, item);
                }
            }
        }

        if (input.contains(Resource.FRAMEWORK_CONFIG)) {
            input = input.replace(Resource.FRAMEWORK_CONFIG, sb.toString());
        }

        if (input.contains(Resource.SCRIPT_PLACEHOLDER)) {
            input = input.replace(Resource.SCRIPT_PLACEHOLDER, renderJS());
        }

        if (input.contains(Resource.STYLE_PLACEHOLDER)) {
            input = input.replace(Resource.STYLE_PLACEHOLDER, renderCSS());
        }

        return input.trim();
    }

}
