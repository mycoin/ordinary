package org.lilystudio.ordinary.util;

/**
 * Mysql字符串转义
 * 
 * @version 0.1.4, 2009/01/10
 * @author 欧阳先伟
 * @since Ordinary 0.1
 */
public class MysqlEscape implements IEscape {

  public String escape(String source) {
    StringBuilder s = new StringBuilder(64);
    int size = source.length();
    for (int i = 0; i < size; i++) {
      char c = source.charAt(i);
      switch (c) {
      case '\\':
        s.append(c);
        break;
      case '\'':
        s.append(c);
        break;
      case '\n':
        s.append("\\n");
        continue;
      case '\r':
        s.append(' ');
        continue;
      }
      s.append(c);
    }
    return s.toString();
  }
}
