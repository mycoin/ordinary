package org.lilystudio.javascript;

import java.io.IOException;
import java.io.Writer;

/**
 * 输出接口
 * 
 * @version 1.0.0, 2010/01/01
 * @author 欧阳先伟
 * @since Common 0.1
 */
public interface IWriteable {

  /**
   * 向输出器中输出压缩后的JavaScript代码
   * 
   * @param writer
   *          输出器
   * @param env
   *          压缩环境
   * @throws IOException
   *           IO操作异常
   */
  public void write(Writer writer, Environment env) throws IOException;
}
