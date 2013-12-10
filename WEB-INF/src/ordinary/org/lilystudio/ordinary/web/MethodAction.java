package org.lilystudio.ordinary.web;

import java.lang.reflect.Method;

/**
 * 允许指定Action中需要被执行的方法, 这样一个Action可以实现多个功能,
 * 减少Action的数量, 被重载的方法参数与execute方法相同.
 * 
 * @see org.lilystudio.ordinary.web.IExecute
 * 
 * @version 0.1.4, 2008/12/12
 * @author 欧阳先伟
 * @since Ordinary 0.1
 */
public class MethodAction implements IExecute {

  /** 需要执行的方法对象 */
  private Method method;

  /**
   * 设置当前action用于替代execute方法的方法名称
   * 
   * @param name
   *          替代方法的名称
   */
  public void setMethod(String name) {
    try {
      method = this.getClass().getMethod(name, IRelay.class);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * 获取替代方法对象
   * 
   * @return 替代方法对象
   */
  public Method getMethod() {
    return method;
  }

  public void execute(IRelay relay) {
  }
}
