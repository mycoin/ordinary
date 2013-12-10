package org.lilystudio.ordinary.util;

/**
 * 字符串转义接口, 用于防止SQL注入等
 * 
 * @version 0.1.4, 2009/01/10
 * @author 欧阳先伟
 * @since Ordinary 0.1
 */
public interface IEscape {

  /**
   * 字符串转义
   * 
   * @param source
   *          源字符串
   * @return 转义后的字符串
   */
  String escape(String source);
}
