package org.lilystudio.httpclient;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * HTTP的提交操作基类, 定义了提交操作的基本行为
 * 
 * @version 0.1.4, 2009/01/01
 * @author 欧阳先伟
 * @since Common 0.1
 */
public abstract class AbstractMethod implements IMethod {

  /** 用户工具的名称 */
  private static final byte[] USER_AGENT = "User-Agent: HttpClient/1.0\r\n"
      .getBytes();

  /** 允许服务器传递的编码方式 */
  private static final byte[] ACCEPT_ENCODING = "Accept-Encoding: gzip, deflate\r\n"
      .getBytes();

  /** 所有的HTTP请求头, 其中值保存的是完整的一行数据 */
  private Map<String, byte[]> headers = new HashMap<String, byte[]>();

  /** 操作域名 */
  private String host;

  /** 操作路径 */
  private String path;

  /**
   * 创建HTTP提交操作基类
   * 
   * @param url
   *          需要控制的url名称
   * @throws IOException
   *           如果URL地址不合法, 不是有效的HTTP请求
   */
  public AbstractMethod(String url) throws IOException {
    if (url.startsWith("http://")) {
      int i = url.indexOf('/', 7);
      if (i > 0) {
        host = url.substring(7, i);
        path = url.substring(i);
        headers.put("User-Agent", USER_AGENT);
        headers.put("Accept-Encoding", ACCEPT_ENCODING);
        return;
      }
    }
    // HARDCODE
    throw new IOException("Invalid url");
  }

  public String getHost() {
    return host;
  }

  public String getPath() {
    return path;
  }

  public void setRequestHeader(String name, String value) {
    headers.put(name, (name + ": " + value + "\r\n").getBytes());
  }

  public void execute(OutputStream out) throws IOException {
    out.write("Host: ".getBytes());
    out.write(host.getBytes());
    out.write('\r');
    out.write('\n');
    for (byte[] value : headers.values()) {
      out.write(value);
    }
  }
}
