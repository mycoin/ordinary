package org.lilystudio.httpclient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 公共的操作类
 * 
 * @version 0.1.4, 2009/01/01
 * @author 欧阳先伟
 * @since Common 0.1
 */
public class Utilities {

  /**
   * 从输入流中读取一个状态行的信息, 需要读取的行必须是ISO-8859-2编码
   * 
   * @param in
   *          数据输入流
   * @return 被读出来的行文本
   * @throws IOException
   *           如果数据读取失败产生
   */
  public static String readLine(InputStream in) throws IOException {
    ByteArrayOutputStream bos = new ByteArrayOutputStream(64);
    while (true) {
      int c = in.read();
      if (c == -1 || c == '\n') {
        return new String(bos.toString("ISO-8859-2"));
      } else if (c == '\r') {
      } else {
        bos.write(c);
      }
    }
  }
}
