package org.lilystudio.ordinary.web.filter;

import org.lilystudio.ordinary.util.IEscape;
import org.lilystudio.ordinary.web.IRelay;

/**
 * 字符串长度过滤器, 如果使用了编码设置,
 * 原对象名加上_BYTES后缀组成的新名称保存的是字节数组, 在设置了trim属性后,
 * 如果有多行数据, 仅取一行. <br>
 * 
 * <b>属性</b>
 * 
 * <pre>
 * encoding--编码方式, 如果被设置, 表示按字节方式编码后验证长度, 如果没有设置, 按字符方式验证
 * trim--是否过滤两端的空格, 默认不过滤
 * min--最短长度, 默认值0, 即不限制
 * max--最长长度, 默认无限制
 * escape--过滤完成后启用的转换器的名称, 可以不设置, 一般sql过滤长度后需要进行转义防止注入
 * </pre>
 * 
 * @version 0.1.4, 2009/01/10
 * @author 欧阳先伟
 * @since Ordinary 0.1
 */
public class LengthFilter extends DefaultFilter {

  /** 字符串的编码方式 */
  private String encoding;

  /** 是否过滤两端的空白字符 */
  private boolean trim;

  /** 最小的合法长度 */
  private int min;

  /** 最大的合法长度 */
  private int max = Integer.MAX_VALUE;

  /** 是否SQL语句, 如果是需要转义'字符 */
  private IEscape escape;

  @Override
  public void execute(IRelay relay, Object value) throws FilterException {
    String s = value.toString();
    if (trim) {
      int len = s.length();
      outer: for (int i = 0; i < len; i++) {
        // 判断检测长度时是否要过滤字符串两端的空白
        if (!Character.isWhitespace(s.charAt(i))) {
          int j = s.indexOf('\n', i);
          for (j = j >= 0 ? j : len - 1; j > i; j--) {
            if (!Character.isWhitespace(s.charAt(j))) {
              s = s.substring(i, j + 1);
              break outer;
            }
          }
        }
      }
      relay.set(name, s);
    }
    int len;
    if (encoding != null) {
      try {
        byte[] bytes = s.getBytes(encoding);
        len = bytes.length;
        if (escape == null) {
          // 不需要字符串转义, 保存字节数据
          // HARDCODE
          relay.set(name + "_BYTES", bytes);
        }
      } catch (Exception e) {
        len = 0;
      }
    } else {
      len = s.length();
    }
    if (len < min) {
      // HARDCODE
      throw new FilterException(name, message != null ? message
          : "The length of " + name + " at least " + min);
    }
    if (len > max) {
      // HARDCODE
      throw new FilterException(name, message != null ? message
          : "The length of " + name + " at most " + max);
    }
    if (escape != null) {
      relay.set(name, escape.escape(s));
    }
  }
}