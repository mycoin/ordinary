package org.lilystudio.httpclient;

import java.io.IOException;
import java.io.InputStream;

/**
 * 固定长度传输方式输入流
 * 
 * @version 0.1.4, 2009/01/01
 * @author 欧阳先伟
 * @since Common 0.1
 */
public class LengthInputStream extends InputStream {

  /** 剩余的未读取的长度 */
  private int length;

  /** 原始的输入流 */
  private InputStream in;

  /** 读取完成后自动关闭状态 */
  private boolean isAutoClose;

  /**
   * 创建固定长度传输方式输入流
   * 
   * @param in
   *          原始的输入流
   * @param length
   *          传输的数据长度
   * @param isAutoClose
   *          如果是<tt>true</tt>表示读取完成后自动关闭
   */
  public LengthInputStream(InputStream in, int length, boolean isAutoClose) {
    this.in = in;
    this.length = length;
    this.isAutoClose = isAutoClose;
  }

  @Override
  public int read() throws IOException {
    if (length > 0) {
      length--;
      return in.read();
    }
    if (isAutoClose) {
      close();
    }
    return -1;
  }

  @Override
  public void close() throws IOException {
    in.close();
  }
}
