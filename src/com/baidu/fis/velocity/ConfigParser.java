package com.baidu.fis.velocity;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;

/**
 * Created by 2betop on 5/16/14.
 */
public class ConfigParser {

    final public static String DEFAULT_PATH = "WEB-INF/server.conf";

    protected class Ruler {
        final public static int TYPE_REWRITE = 0;
        final public static int TYPE_REDIRECT = 1;

        public int type = 0;
        public String match;
        public String target;
    }

    protected ArrayList<Ruler> rules = new ArrayList<Ruler>();

    public ConfigParser(File file) throws FileNotFoundException, IOException {
        initFromFile(file, Charset.forName("UTF-8"));
    }

    public ConfigParser(File file, Charset charset) throws FileNotFoundException, IOException {
        initFromFile(file, charset);
    }

    public ConfigParser(String filename) throws FileNotFoundException, IOException{
        initFromFile(new File(filename), Charset.forName("UTF-8"));
    }

    public ConfigParser(String filename, Charset charset) throws FileNotFoundException, IOException{
        initFromFile(new File(filename), charset);
    }


    protected void initFromFile(File file, Charset charset) throws FileNotFoundException, IOException{
        FileInputStream filestream = new FileInputStream(file);
        InputStreamReader instream = new InputStreamReader(filestream, charset);
        BufferedReader reader = new BufferedReader(instream);
    }
}
