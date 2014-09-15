package com.baidu.fis.velocity.servlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.*;

@MultipartConfig
public class Upload extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            Part filePart = request.getPart("file");
            String to = request.getParameter("to");

            File file = new File(to);

            if (file.isDirectory()) {
                throw new IllegalArgumentException("Can't upload to a folder `" + to + "`");
            }

            if (file.exists()) {
                file.delete();
                file.createNewFile();
            } else {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }

            if (!file.canWrite()) {
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

            response.getWriter().write("0");
        } catch ( Exception e) {
            System.out.print(e.getMessage());
            response.getWriter().write("1");
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.sendError(403, "forbidden");
    }
}
