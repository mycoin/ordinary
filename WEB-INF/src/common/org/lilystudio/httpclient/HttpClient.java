package org.lilystudio.httpclient;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.DeflaterInputStream;
import java.util.zip.GZIPInputStream;

/**
 * HTTP的客户端模拟
 * 
 * @version 0.1.4, 2009/01/01
 * @author 欧阳先伟
 * @since Common 0.1
 */
public class HttpClient {

  /** 保持连接状态下的数据头发送 */
  private static final byte[] KEEP_ALIVE = "HTTP/1.1\r\nConnection: Keep-Alive\r\n"
      .getBytes();

  /** 不保持连接状态下的数据头发送 */
  private static final byte[] NO_KEEP_ALIVE = "HTTP/1.0\r\n".getBytes();

  /** 连接套接字 */
  private Socket socket;

  /** 套接字输入流 */
  private InputStream in;

  /** 套接字输出流 */
  private OutputStream out;

  /** 当前接收数据的主输入流 */
  private InputStream body;

  /** 在自动解码状态下, 已经被读出来的全部数据 */
  private byte[] data;

  /** 保持连接状态标志, <tt>true</tt>表示保持连接 */
  private boolean keepAlive;

  /** 自动解码状态标志, <tt>true</tt>表示自动解码 */
  private boolean autoDecode;

  /** 上一次连接的域名与端口号信息 */
  private String host;

  /** HTTP应答状态号 */
  private int statusCode;

  /** HTTP应答的头部信息 */
  private Map<String, String> headers = new HashMap<String, String>();

  /**
   * 设置保持连接状态位
   * 
   * @param isKeepAlive
   *          <tt>true</tt>表示需要保持连接
   */
  public void setKeepAlive(boolean keepAlive) {
    this.keepAlive = keepAlive;
  }

  /**
   * 设置自动解码状态位
   * 
   * @param autoDecode
   *          <tt>true</tt>表示自动解码, 会自动分析结果是否被压缩过
   */
  public void setAutoDecode(boolean autoDecode) {
    this.autoDecode = autoDecode;
  }

  /**
   * 执行一次客户端调用
   * 
   * @param method
   *          用于调用发送请求的方法操作接口
   * @return 调用产生的状态号
   * @throws IOException
   *           如果数据传输失败
   */
  public int execute(IMethod method) throws IOException {
    String host = method.getHost();
    if (socket != null) {
      // 有上一次的调用, 先清除上一次的输入流信息
      clearInputStream();
      if (!keepAlive || socket.isInputShutdown() || !host.equals(this.host)) {
        // 如果不保持连接, 或者流被关闭, 或者访问的域名不相同, 关闭连接
        close();
      }
    }
    if (socket == null) {
      // 初始化连接信息
      this.host = host;
      int i = host.indexOf(':');
      int port;
      if (i > 0) {
        port = Integer.parseInt(host.substring(i + 1));
        host = host.substring(0, i);
      } else {
        port = 80;
      }
      socket = new Socket(host, port);
      in = new BufferedInputStream(socket.getInputStream());
      out = new BufferedOutputStream(socket.getOutputStream());
    }

    // 发送HTTP请求信息
    out.write(method.getName());
    out.write(' ');
    out.write(method.getPath().getBytes());
    out.write(' ');
    if (keepAlive) {
      out.write(KEEP_ALIVE);
    } else {
      out.write(NO_KEEP_ALIVE);
    }
    method.execute(out);
    out.flush();

    // 读取HTTP应答信息
    readStatusLine();
    readResponseHeaders();
    readResponseBody();
    return statusCode;
  }

  /**
   * 获取HTTP应答头部信息
   * 
   * @param name
   *          头部名称
   * @return 头部值
   */
  public String getResponseHeader(String name) {
    return headers.get(name);
  }

  /**
   * 获取所有的HTTP应答头部信息
   * 
   * @return 所有的HTTP应答头部信息
   */
  public Map<String, String> getResponseHeaders() {
    return headers;
  }

  /**
   * 获取HTTP应答主体信息输入流
   * 
   * @return HTTP应答主体信息输入流
   */
  public InputStream getResponseBodyAsStream() {
    return body;
  }

  /**
   * 获取HTTP应答主体信息数据
   * 
   * @return HTTP应答主体信息数据
   * @throws IOException
   *           如果数据传输失败
   */
  public byte[] getResponseBody() throws IOException {
    if (data != null) {
      return data;
    }
    ByteArrayOutputStream bos = new ByteArrayOutputStream(1024);
    while (true) {
      int c = body.read();
      if (c == -1) {
        break;
      }
      bos.write(c);
    }
    return bos.toByteArray();
  }

  /**
   * 关闭HTTP连接
   * 
   * @throws IOException
   *           如果关闭时引发数据例外
   */
  public void close() throws IOException {
    socket.close();
    socket = null;
    if (body != null) {
      body.close();
      body = null;
    }
  }

  /**
   * 读取HTTP应答状态行
   * 
   * @throws IOException
   *           如果数据传输失败或格式不合法
   */
  private void readStatusLine() throws IOException {
    String line = Utilities.readLine(in);
    if (line.startsWith("HTTP/")) {
      int i = line.indexOf(' ', 5);
      int j = line.lastIndexOf(' ');
      if (i != j) {
        statusCode = Integer.parseInt(line.substring(i + 1, j));
        return;
      }
    }
    // 没有头部, 认为返回的都是文本, 状态号200
    statusCode = 200;
  }

  /**
   * 读取HTTP应答头部信息
   * 
   * @throws IOException
   *           如果数据传输失败或格式不合法
   */
  private void readResponseHeaders() throws IOException {
    while (true) {
      String line = Utilities.readLine(in);
      if (line.length() == 0) {
        return;
      }
      int i = line.indexOf(':');
      if (i > 0) {
        headers.put(line.substring(0, i).trim(), line.substring(i + 1).trim());
      }
    }
  }

  /**
   * 设置HTTP主体信息读入流
   * 
   * @throws IOException
   *           如果数据传输失败或格式不合法
   */
  private void readResponseBody() throws IOException {
    String value = headers.get("Content-Length");
    boolean isAutoClose = headers.containsKey("Connection");
    if (value != null) {
      body = new LengthInputStream(in, Integer.parseInt(value), isAutoClose);
    } else if ("chunked".equals(headers.get("Transfer-Encoding"))) {
      body = new ChunkedInputStream(in, isAutoClose);
    }
    if (autoDecode) {
      value = headers.get("Content-Encoding");
      if ("gzip".equals(value)) {
        body = new GZIPInputStream(body);
      } else if ("deflate".equals(value)) {
        body = new DeflaterInputStream(body);
      } else {
        return;
      }
      headers.remove("Content-Encoding");
      data = getResponseBody();
      body = new ByteArrayInputStream(data);
      headers.put("Content-Length", Integer.toString(data.length));
    }
  }

  /**
   * 清除上一次HTTP请求的剩余数据流
   * 
   * @throws IOException
   *           如果数据传输失败
   */
  private void clearInputStream() throws IOException {
    while (body.read() != -1)
      ;
  }
}
