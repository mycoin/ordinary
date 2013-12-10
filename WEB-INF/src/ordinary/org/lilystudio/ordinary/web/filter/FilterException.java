package org.lilystudio.ordinary.web.filter;

/**
 * 数据检测异常, 当Filter过滤失败时产生
 * 
 * @version 0.1.3, 2008/09/01
 * @author 欧阳先伟
 * @since Ordinary 0.1
 */
public class FilterException extends Exception {

  /** 序列化编号 */
  private static final long serialVersionUID = 1L;

  /** 错误项名称 */
  private String name;

  /**
   * 建立数据检测过滤异常
   */
  public FilterException() {
    super();
  }
  
  /**
   * 建立数据检测过滤异常
   * 
   * @param name
   *          错误项对应的名称
   * @param message
   *          错误提示信息
   */
  public FilterException(String name, String message) {
    super(message);
    this.name = name;
  }

  /**
   * 获取错误项对应的名称
   * 
   * @return 错误项名称
   */
  public String getName() {
    return name;
  }
}