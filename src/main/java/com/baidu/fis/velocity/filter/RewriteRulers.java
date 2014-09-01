package com.baidu.fis.velocity.filter;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;

/**
 * Created by 2betop on 5/16/14.
 */
public class RewriteRulers {

    final public static String DEFAULT_PATH = "/WEB-INF/server.conf";

    protected static class Ruler {
        final public static int TYPE_REWRITE = 0;
        final public static int TYPE_REDIRECT = 1;

        public int type = 0;
        public String pattern;
        public String target;
        public String dest;
    }

    protected ArrayList<Ruler> rulers = new ArrayList<Ruler>();

    public RewriteRulers() {

    }

    public RewriteRulers(InputStream stream) throws IOException{
        this.load(stream, Charset.forName("UTF-8"));
    }

    public RewriteRulers(InputStream stream, Charset charset) throws IOException{
        this.load(stream, charset);
    }

    public void load(InputStream stream) throws IOException{
        this.load(stream, Charset.forName("UTF-8"));
    }

    public void load(InputStream stream, Charset charset) throws IOException{
        InputStreamReader instream = new InputStreamReader(stream, charset);
        BufferedReader reader = new BufferedReader(instream);

        String line;

        while ((line = reader.readLine()) != null) {
            line = line.trim();

            // 只识别 rewrite/redirect ，其他的不识别。
            if (line.isEmpty() || !line.startsWith("rewrite") && !line.startsWith("redirect")) {
                continue;
            }

            String []parts = line.split("\\s+");
            Ruler ruler = new Ruler();

            if (parts[0].toLowerCase().equals("rewrite")) {
                ruler.type = Ruler.TYPE_REWRITE;
            } else if (parts[0].toLowerCase().equals("redirect")) {
                ruler.type = Ruler.TYPE_REDIRECT;
            }

            ruler.pattern = parts[1];
            ruler.target = parts[2];

            rulers.add(ruler);
        }

        reader.close();
    }

    public Ruler getRuler(String path) {

        for (Ruler ruler:rulers) {

            if (path.matches(ruler.pattern)) {
                ruler.dest = path.replaceAll(ruler.pattern, ruler.target);
                return ruler;
            }
        }

        return null;
    }

}
