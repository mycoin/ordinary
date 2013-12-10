package org.lilystudio.ordinary.module;

import org.lilystudio.ordinary.ManagerContext;

/**
 * 对象池模块初始化类, 允许多个线程共享对象池中的对象, 使用完成后必须释放,
 * 否则很快就会没有可用的对象. 更多的属性参见父类<br>
 * 
 * <b>属性</b>
 * 
 * <pre>
 * type--对象池的类全名
 * size--对象池中对象的数量
 * </pre>
 * 
 * @see org.lilystudio.ordinary.module.AbstractModule
 * 
 * @version 0.1.4, 2008/12/12
 * @author 欧阳先伟
 * @since Ordinary 0.1
 */
public class PoolModule extends AbstractModule {

  /** 已经建立的对象 */
  private Object[] objects;

  /** 对象池中对象的类型 */
  private Class<?> type;

  /** 当前空余可使用的对象数量 */
  private int size;

  /**
   * 设置对象的类型
   * 
   * @param value
   *          类型的描述字符串
   */
  public void setType(String value) throws Exception {
    this.type = Class.forName(value);
  }

  /**
   * 初始化对象
   * 
   * @throws Exception
   *           如果初始化失败
   */
  public void init() throws Exception {
    objects = new Object[size];
    for (int i = objects.length - 1; i >= 0; i--) {
      objects[i] = createObject();
    }
  }

  /**
   * 获取对象池中的一个对象, 使用完成后需要使用releaseObject方法释放,
   * 如果对象池中暂时没有可用的对象, 将被阻塞至获得对象, 或者超时返回
   * 
   * @return 对象实例
   * @throws Exception
   *           对象无法获取
   */
  public Object getObject() throws Exception {
    while (true) {
      synchronized (this) {
        if (size > 0) {
          size--;
          return objects[size];
        }
      }
      wait();
    }
  }

  /**
   * 释放对象
   * 
   * @param o
   */
  public void releaseObject(Object o) {
    synchronized (this) {
      objects[size] = o;
      size++;
      if (size == 1) {
        notify();
      }
    }
  }

  /**
   * 建立并初始化对象池中的一个对象, 有特殊初始化要求的对象请覆盖这个方法
   * 
   * @param c
   *          类对象, 标记池中所有的对象属性
   * @return 类的实例对象
   * @throws Exception
   *           生成实例对象失败
   */
  protected Object createObject() throws Exception {
    // 缺省情况下直接产生一个对象实例
    return type.newInstance();
  }

  @Override
  protected void finalize() throws Throwable {
    if (objects != null) {
      for (Object o : objects) {
        ManagerContext.release(o);
      }
      objects = null;
    }
  }
}