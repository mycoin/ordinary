package org.lilystudio.httpclient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * HTTP的POST提交操作
 * 
 * @version 0.1.4, 2009/01/01
 * @author 欧阳先伟
 * @since Common 0.1
 */
public class PostMethod extends AbstractMethod {

  /** 操作的名称 */
  private static final byte[] NAME = "POST".getBytes();

  /** 数据长度字段定义的名称 */
  private static final byte[] CONTENT_LENGTH = "Content-Length: ".getBytes();

  /** 附加到POST请求中的数据数组 */
  private ByteArrayOutputStream body = new ByteArrayOutputStream(64);

  /**
   * 创建HTTP的POST提交操作
   * 
   * @param url
   *          需要控制的url名称
   * @throws IOException
   *           如果URL地址不合法, 不是有效的HTTP请求
   */
  public PostMethod(String url) throws IOException {
    super(url);
  }

  /**
   * 向输出数据流中添加新的数据
   * 
   * @param name
   *          数据的名称
   * @param value
   *          数据的值
   * @throws IOException
   *           如果数据传输失败
   */
  public void addRequestBody(String name, String value) throws IOException {
    if (body.size() > 0) {
      body.write('&');
    }
    body.write(name.getBytes());
    body.write('=');
    body.write(value.getBytes());
  }

  @Override
  public void execute(OutputStream out) throws IOException {
    super.execute(out);
    out.write(CONTENT_LENGTH);
    out.write(Integer.toString(body.size()).getBytes());
    out.write('\r');
    out.write('\n');
    out.write('\r');
    out.write('\n');
    body.writeTo(out);
  }

  public byte[] getName() {
    return NAME;
  }
}
