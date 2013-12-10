package org.lilystudio.javascript.scope;

/**
 * 标识符统计
 * 
 * @version 1.0.0, 2010/01/01
 * @author 欧阳先伟
 * @since Common 0.1
 */
public class Identifier implements ICount {

  /** 父标识符 */
  private Identifier parent;

  /** 标识符名称 */
  private String name;

  /** 标识符使用次数 */
  private int usedCount;

  /** 标识符是否被锁定(在with语句) */
  private boolean lock;

  /**
   * 创建标识符统计
   * 
   * @param name
   *          标识符名称
   * @param parent
   *          父生存域标识符接口
   */
  public Identifier(String name, Identifier parent) {
    this.name = name;
    this.parent = parent;
  }

  /**
   * 获取标识符的名称
   * 
   * @return 标识符名称
   */
  public String getString() {
    return parent != null ? parent.getString() : name;
  }

  /**
   * 将标识符设置为本地变量
   */
  public void local() {
    for (Identifier parent = this.parent; parent != null; parent = parent.parent) {
      parent.usedCount -= usedCount;
    }
    this.parent = null;
  }

  /**
   * 判断标识符是否为本地变量
   * 
   * @return 标识符是否为本地变量
   */
  public boolean isLocal() {
    return parent == null;
  }

  /**
   * 锁定标识符不进行转换
   */
  public void lock() {
    lock = true;
    if (parent != null) {
      parent.lock();
    }
  }

  /**
   * 判断标识符是否被锁定
   * 
   * @return 标识符是否被锁定
   */
  public boolean isLocked() {
    return lock;
  }

  /**
   * 标识符使用次数加1
   */
  public void inc() {
    usedCount++;
  }

  public void inc(int count) {
    usedCount += count;
  }

  public int getUsedCount() {
    return parent != null ? parent.usedCount : usedCount;
  }

  public String getName() {
    return name;
  }
  
  public void setString(String name) {
    this.name = name;
  }
}
