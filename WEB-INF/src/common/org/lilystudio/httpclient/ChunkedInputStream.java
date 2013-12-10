package org.lilystudio.httpclient;

import java.io.IOException;
import java.io.InputStream;

/**
 * 字节块编码传输方式输入流
 * 
 * @version 0.1.4, 2009/01/01
 * @author 欧阳先伟
 * @since Common 0.1
 */
public class ChunkedInputStream extends InputStream {

  /** 字节块当前剩余的长度 */
  private int length;

  /** 原始的输入流 */
  private InputStream in;

  /** 读取完成后自动关闭状态 */
  private boolean isAutoClose;

  /**
   * 创建字节块编码传输方式输入流
   * 
   * @param in
   *          原始的输入流
   * @param isAutoClose
   *          如果是<tt>true</tt>表示读取完成后自动关闭
   */
  public ChunkedInputStream(InputStream in, boolean isAutoClose) {
    this.in = in;
    this.isAutoClose = isAutoClose;
  }

  @Override
  public int read() throws IOException {
    if (length == -1) {
      return -1;
    }
    // 读取字节块大小
    if (length == 0) {
      String line;
      do {
        line = Utilities.readLine(in);
      } while (line.length() == 0);
      int i = line.indexOf(' ');
      length = Integer.parseInt(i > 0 ? line.substring(0, i) : line, 16);
    }
    length--;
    if (length < 0) {
      // 字节块的数据已经全部读完, 过滤剩余的无效扩展信息
      while (true) {
        String line = Utilities.readLine(in);
        if (line.length() == 0) {
          if (isAutoClose) {
            close();
          }
          return -1;
        }
      }
    }
    return in.read();
  }

  @Override
  public void close() throws IOException {
    in.close();
  }
}
