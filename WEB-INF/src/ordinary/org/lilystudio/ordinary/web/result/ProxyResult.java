package org.lilystudio.ordinary.web.result;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.lilystudio.httpclient.GetMethod;
import org.lilystudio.httpclient.HttpClient;
import org.lilystudio.httpclient.IMethod;
import org.lilystudio.httpclient.PostMethod;
import org.lilystudio.ordinary.web.Controller;
import org.lilystudio.ordinary.web.IRelay;
import org.lilystudio.ordinary.web.UserInformation;

/**
 * 代码重定向跳转/输出类, 使用它允许通过代理的方式访问其它网站的页面并将结果返回<br>
 * <b>属性</b>
 * 
 * <pre>
 * encoding--表示编码集, 缺省为UTF-8
 * </pre>
 * 
 * @version 0.1.4, 2008/12/12
 * @author 欧阳先伟
 * @since Ordinary 0.1
 */
public class ProxyResult extends AbstractParameter {

  /** Session的特殊保留字 */
  private static final String SESSION_COOKIE_KEY = "_JSESSIONID";

  /** 数据的编码集, 默认使用框架的编码集 */
  private String encoding = Controller.getEncoding();

  /** 重定向的位置 */
  private String url;

  public void execute(HttpServletRequest request, HttpServletResponse response,
      IRelay relay) throws Exception {
    String url = getURI(relay, this.url, encoding);
    // 设置连接参数
    boolean isGet;
    if (url.startsWith("post://")) {
      isGet = false;
      url = "http" + url.substring(4);
    } else {
      isGet = true;
      addParameter(relay, url, encoding);
    }
    IMethod httpMethod = isGet ? new GetMethod(url) : new PostMethod(url);
    // 设置客户端特殊的信息
    httpMethod.setRequestHeader("If-None-Match", request
        .getHeader("If-None-Match"));
    httpMethod.setRequestHeader("If-Modified-Since", request
        .getHeader("If-Modified-Since"));
    // 取出服务器端可能用到的Cookie信息
    Cookie[] cookies = request.getCookies();
    if (cookies != null) {
      StringBuilder s = new StringBuilder(64);
      for (Cookie cookie : cookies) {
        String name = cookie.getName();
        if (!name.equals("JSESSIONID")) {
          s.append(name).append('=').append(cookie.getValue()).append(';');
        }
      }
      UserInformation info = relay.getUserInformation(false);
      if (info != null) {
        Object o = info.getProperty(SESSION_COOKIE_KEY);
        if (o != null) {
          s.append(o);
        }
      }
      int len = s.length();
      if (len > 0) {
        s.setLength(len - 1);
      }
      httpMethod.setRequestHeader("Cookie", s.toString());
    }
    HttpClient httpClient = new HttpClient();
    httpClient.setAutoDecode(true);
    try {
      if (param != null) {
        if (!isGet) {
          PostMethod method = (PostMethod) httpMethod;
          int size = param.size();
          for (int i = 0; i < size; i++) {
            Parameter item = param.get(i);
            Object value = item.getValue(relay);
            method.addRequestBody(item.name, value != null ? URLDecoder.decode(
                value.toString(), encoding) : "");
          }
        }
      }
      while (true) {
        // 计算是否需要跳转
        int statusCode = 0;
        statusCode = httpClient.execute(httpMethod);
        if (statusCode == HttpServletResponse.SC_MOVED_PERMANENTLY
            || statusCode == HttpServletResponse.SC_MOVED_TEMPORARILY) {
          String locationHeader = httpClient.getResponseHeader("Location");
          if (locationHeader != null) {
            httpMethod = new GetMethod(locationHeader);
            continue;
          }
        }
        response.setStatus(statusCode);
        break;
      }

      // 将得到的结果的头部输出
      Map<String, String> headers = httpClient.getResponseHeaders();
      for (Map.Entry<String, String> header : headers.entrySet()) {
        String name = header.getKey();
        String value = header.getValue();
        if (name.equalsIgnoreCase("Set-Cookie")) {
          // 如果返回的cookie中有jsessionid, 为防止冲突,
          // 保存在用户的专属信息中
          int index = value.indexOf("JSESSIONID");
          if (index >= 0) {
            int len = value.length();
            int endIndex = (value.indexOf(';', index + 10) + len) % len + 1;
            String sessionId = value.substring(index, endIndex);
            value = value.substring(0, index) + value.substring(endIndex);
            response.setHeader(name, value);
            UserInformation info = relay.getUserInformation(true);
            info.setProperty("_JSESSIONID", sessionId);
            continue;
          }
        }
        response.setHeader(name, value);
      }

      // 将得到的结果输出
      InputStream in = httpClient.getResponseBodyAsStream();
      if (in != null) {
        OutputStream out = response.getOutputStream();
        byte[] buf = new byte[1024];
        while (true) {
          int len = in.read(buf);
          if (len < 0) {
            break;
          }
          out.write(buf, 0, len);
        }
        out.flush();
      }
    } finally {
      httpClient.close();
    }
  }
}