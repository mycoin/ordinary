package org.lilystudio.ordinary;

/**
 * 类管理器, 每一次请求都将根据类产生一个新的实例对象
 * 
 * @version 0.1.4, 2008/12/12
 * @author 欧阳先伟
 * @since Ordinary 0.1
 */
public class ClassManager implements IManager {

  /** 类对象, 使用它的newInstance()方法生成对象实例 */
  private Class<?> clazz;

  /**
   * 创建类管理器
   * 
   * @param clazz
   *          类对象
   */
  public ClassManager(Class<?> clazz) {
    this.clazz = clazz;
  }
  
  public Object get() throws Exception {
    return clazz.newInstance();
  }
}
