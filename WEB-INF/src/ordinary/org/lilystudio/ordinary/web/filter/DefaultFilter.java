package org.lilystudio.ordinary.web.filter;

import org.lilystudio.ordinary.web.IExecute;
import org.lilystudio.ordinary.web.IRelay;

/**
 * 默认值设置, 如果指定的对象不存在, 则使用默认值代替. <br>
 * <b>属性</b>
 * 
 * <pre>
 * name--对象名
 * default--默认值
 * </pre>
 * 
 * @version 0.1.4, 2009/01/10
 * @author 欧阳先伟
 * @since Ordinary 0.1
 */
public class DefaultFilter implements IExecute {

  /** 对象名 */
  protected String name;

  /** 错误提示信息 */
  protected String message;

  /** 默认值 */
  private String value;

  /**
   * 设置对象默认值
   * 
   * @param value
   *          默认值
   */
  public void setDefault(String value) {
    this.value = value;
  }

  public void execute(IRelay relay) throws FilterException {
    Object value = relay.get(name);
    if (value != null) {
      execute(relay, value);
    } else if (this.value != null) {
      relay.set(name, this.value);
    } else {
      throw new FilterException(name, message != null ? message
          : "The value of " + name + " required");
    }
  }

  /**
   * 实际的检测处理过程, 子类通过这个函数来进行进一步的数据验证
   * 
   * @param relay
   *          数据集合
   * @param value
   *          对象的值
   * @throws Exception
   *           验证错误
   */
  protected void execute(IRelay relay, Object value) throws FilterException {
  }
}
