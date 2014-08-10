package com.baidu.fis.velocity;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.*;

/**
 * Created by 2betop on 5/30/14.
 * 用来配合 fis deploy 工作。
 *
 * ！！！注意：还没有完工！！！！！
 */
public class UploadServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            Part filePart = request.getPart("file");
            String to = request.getParameter("to");

            System.out.println(request.getParameter("abc"));
            System.out.println(request.getParameter("to"));

            File file = new File(to);

            if (file.isDirectory()) {
                throw new IllegalArgumentException("Can't upload to a folder.");
            } else if (!file.canWrite()) {
                throw new IllegalArgumentException("Permission denied.");
            }

            InputStream src = filePart.getInputStream();
            OutputStream dst = new FileOutputStream(file);

            byte[] buf = new byte[1024];
            int len;
            while((len=src.read(buf))>0){
                dst.write(buf,0,len);
            }
            src.close();
            dst.close();
        } catch ( Exception e) {
            response.sendError(500, e.getMessage());
            // "500 Internal Server Error"
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.sendError(403, "forbidden");
    }
}
