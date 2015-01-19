package com.baidu.fis.servlet;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Iterator;
import java.util.List;

public class Upload extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        boolean isMultipart = ServletFileUpload.isMultipartContent(request);

        if (isMultipart) {

            FileItemFactory factory = new DiskFileItemFactory();
            ServletFileUpload upload = new ServletFileUpload(factory);
            String to = null;
            InputStream fileStream = null;

            try {
                upload.setFileSizeMax(50 * 1024 * 1024);
                List items = upload.parseRequest(request);
                Iterator iterator = items.iterator();

                while (iterator.hasNext()) {
                    FileItem item = (FileItem)iterator.next();
                    String name = item.getFieldName();
                    InputStream stream = item.getInputStream();

                    if (item.isFormField()) {
                        if (name.equals("to")) {
                            to = Streams.asString(stream);
                        }
                    } else if (name.equals("file")) {
                        fileStream = stream;
                    }
                }

                if (to == null || fileStream == null) {
                    throw new RuntimeException("Params Error");
                }


                File file = new File(to);

                if (file.isDirectory()) {
                    throw new IllegalArgumentException("Can't upload to a folder `" + to + "`");
                }

                if (file.exists()) {
                    if (!file.delete() || !file.createNewFile()) {
                        throw new Exception("Permission denied");
                    }
                } else if (!file.getParentFile().mkdirs() || !file.createNewFile()) {
                    throw new Exception("Permission denied");
                }

                if (!file.canWrite()) {
                    throw new Exception("Permission denied.");
                }

                OutputStream dst = new FileOutputStream(file);
                Streams.copy(fileStream, dst, true);
                response.getWriter().write("0");
            } catch ( Exception e) {
                System.out.print(e.getMessage());
                response.getWriter().write("1");

                throw new ServletException(e.getMessage(), e);
            }
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.sendError(403, "forbidden");
    }
}
