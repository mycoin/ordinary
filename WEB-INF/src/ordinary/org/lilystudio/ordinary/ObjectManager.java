package org.lilystudio.ordinary;

/**
 * 对象管理器, 只包含一个对象实例, 所有的用户调用均共享同一个对象, 类似单例模式.
 * 如果类中使用了Shared标签, 将默认使用此管理器.
 * 
 * @version 0.1.4, 2008/12/12
 * @author 欧阳先伟
 * @since Ordinary 0.1
 */
public class ObjectManager implements IManager {

  /** 共享的对象实例, 每次调用都将它返回 */
  private Object instance;

  /**
   * 创建对象管理器
   * 
   * @param o
   *          已经初始化的对象实例
   */
  public ObjectManager(Object o) {
    instance = o;
  }

  @Override
  protected void finalize() throws Throwable {
    if (instance != null) {
      ManagerContext.release(instance);
      instance = null;
    }
  }

  public Object get() {
    return instance;
  }
}
