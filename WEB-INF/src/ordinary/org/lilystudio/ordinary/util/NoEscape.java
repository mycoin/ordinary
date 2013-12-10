package org.lilystudio.ordinary.util;

/**
 * 不进行字符串转义
 * 
 * @version 0.1.5, 2009/06/01
 * @author 欧阳先伟
 * @since Ordinary 0.1
 */
public class NoEscape implements IEscape {

  public String escape(String source) {
    return source;
  }
}
