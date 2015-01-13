package com.baidu.fis.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by 2betop on 5/16/14.
 *
 * 只需要屏蔽掉 jsp 的输出。
 */
public class ResponseWrapper extends HttpServletResponseWrapper {

    private HttpServletResponse response;

    public ResponseWrapper(HttpServletResponse response) {
        super(response);
        this.response = response;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        return new PrintWriter(new StringWriter());
    }

    @Override
    public void setHeader(String name, String value) {
        response.setHeader(name, value);
    }

    /**
     * The default behavior of this method is to call addCookie(Cookie cookie)
     * on the wrapped response object.
     *
     * @param cookie
     */
    @Override
    public void addCookie(Cookie cookie) {
        response.addCookie(cookie);
    }

    /**
     * The default behavior of this method is to call sendError(int sc, String msg)
     * on the wrapped response object.
     *
     * @param sc
     * @param msg
     */
    @Override
    public void sendError(int sc, String msg) throws IOException {
        response.sendError(sc, msg);
    }

    /**
     * The default behavior of this method is to call sendError(int sc)
     * on the wrapped response object.
     *
     * @param sc
     */
    @Override
    public void sendError(int sc) throws IOException {
        response.sendError(sc);
    }

    /**
     * The default behavior of this method is to return sendRedirect(String location)
     * on the wrapped response object.
     *
     * @param location
     */
    @Override
    public void sendRedirect(String location) throws IOException {
        response.sendRedirect(location);
    }

    /**
     * The default behavior of this method is to call setDateHeader(String name, long date)
     * on the wrapped response object.
     *
     * @param name
     * @param date
     */
    @Override
    public void setDateHeader(String name, long date) {
        response.setDateHeader(name, date);
    }

    /**
     * The default behavior of this method is to call addDateHeader(String name, long date)
     * on the wrapped response object.
     *
     * @param name
     * @param date
     */
    @Override
    public void addDateHeader(String name, long date) {
        response.addDateHeader(name, date);
    }

    /**
     * The default behavior of this method is to return addHeader(String name, String value)
     * on the wrapped response object.
     *
     * @param name
     * @param value
     */
    @Override
    public void addHeader(String name, String value) {
        response.addHeader(name, value);
    }

    /**
     * The default behavior of this method is to call setIntHeader(String name, int value)
     * on the wrapped response object.
     *
     * @param name
     * @param value
     */
    @Override
    public void setIntHeader(String name, int value) {
        response.setIntHeader(name, value);
    }

    /**
     * The default behavior of this method is to call addIntHeader(String name, int value)
     * on the wrapped response object.
     *
     * @param name
     * @param value
     */
    @Override
    public void addIntHeader(String name, int value) {
        response.addIntHeader(name, value);
    }

    /**
     * The default behavior of this method is to call setStatus(int sc)
     * on the wrapped response object.
     *
     * @param sc
     */
    @Override
    public void setStatus(int sc) {
        response.setStatus(sc);
    }

    /**
     * The default behavior of this method is to call
     * setStatus(int sc, String sm) on the wrapped response object.
     *
     * @param sc
     * @param sm
     * @deprecated As of version 2.1, due to ambiguous meaning of the
     * message parameter. To set a status code
     * use {@link #setStatus(int)}, to send an error with a description
     * use {@link #sendError(int, String)}
     */
    @Override
    public void setStatus(int sc, String sm) {
        response.setStatus(sc, sm);
    }
}
