package org.lilystudio.ordinary.web.filter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.lilystudio.ordinary.web.IRelay;

/**
 * 正则表达式过滤器. <br>
 * <b>属性</b>
 * 
 * <pre>
 * regular--正则表达式语句, Java的Pattern标准, 如果没有定义translate, 表示完全匹配
 * translate--需要进行的转换, 如果被设置, 表示将上面匹配中的字符串转换成其它的字符串
 * </pre>
 * 
 * @version 0.1.3, 2008/09/01
 * @author 欧阳先伟
 * @since Ordinary 0.1
 */
public class PatternFilter extends DefaultFilter {

  /** 正则表达式处理器 */
  private Pattern regular;

  /** 正则表达式转换规则 */
  private String translate;

  /**
   * 设置正则表达式处理器
   * 
   * @param value
   *          正则表达式语法
   */
  public void setRegular(String value) {
    regular = Pattern.compile(value);
  }

  @Override
  public void execute(IRelay relay, Object value) throws FilterException {
    Matcher matcher = regular.matcher(value.toString());
    if (translate != null) {
      relay.set(name, matcher.replaceAll(translate));
      return;
    } else if (matcher.find()) {
      // 如果不需要转换, 则需要完全的匹配
      if (value.equals(matcher.group())) {
        return;
      }
    }
    // HARDCODE
    throw new FilterException(name, message != null ? message : "The value of "
        + name + " doesn't match the regex expression");
  }
}