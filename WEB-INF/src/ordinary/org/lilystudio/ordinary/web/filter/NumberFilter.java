package org.lilystudio.ordinary.web.filter;

import org.lilystudio.ordinary.web.IRelay;

/**
 * 数值过滤器, 支持浮点数, 匹配时请注意双精度数造成的误差<br>
 * <b>属性</b>
 * 
 * <pre>
 * min--最小值, 默认值0
 * max--最大值, 默认无限制
 * </pre>
 * 
 * @version 0.1.3, 2008/09/01
 * @author 欧阳先伟
 * @since Ordinary 0.1
 */
public class NumberFilter extends DefaultFilter {

  /** 最小值 */
  private double min;

  /** 最大值 */
  private double max = Double.MAX_VALUE;

  @Override
  public void execute(IRelay relay, Object value) throws FilterException {
    try {
      double number = Double.parseDouble(value.toString());
      if (number < min) {
        // HARDCODE
        throw new FilterException(name, message != null ? message
            : "The value of " + name + " can't be less than " + min);
      }
      if (number > max) {
        // HARDCODE
        throw new FilterException(name, message != null ? message
            : "The value of " + name + " can't be more than " + max);
      }
    } catch (NumberFormatException e) {
      // HARDCODE
      throw new FilterException(name, message != null ? message
          : "The value of " + name + " is not a numerical format");
    }
  }
}