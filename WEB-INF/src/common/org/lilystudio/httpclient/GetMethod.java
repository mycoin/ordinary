package org.lilystudio.httpclient;

import java.io.IOException;
import java.io.OutputStream;

/**
 * HTTP的GET提交操作
 * 
 * @version 0.1.4, 2009/01/01
 * @author 欧阳先伟
 * @since Common 0.1
 */
public class GetMethod extends AbstractMethod {

  /** 操作的名称 */
  private static final byte[] NAME = "GET".getBytes();

  /**
   * 创建HTTP的GET提交操作
   * 
   * @param url
   *          需要控制的url名称
   * @throws IOException
   *           如果URL地址不合法, 不是有效的HTTP请求
   */
  public GetMethod(String url) throws IOException {
    super(url);
  }

  @Override
  public void execute(OutputStream out) throws IOException {
    super.execute(out);
    out.write('\r');
    out.write('\n');
  }

  public byte[] getName() {
    return NAME;
  }
}
