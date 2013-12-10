package org.lilystudio.javascript.scope;

/**
 * 标签统计
 * 
 * @version 1.0.0, 2010/01/01
 * @author 欧阳先伟
 * @since Common 0.1
 */
public class Label implements ICount {

  /** 标签名称 */
  private String name;

  /** 使用次数 */
  private int usedCount;

  /**
   * 获取标签的名称字符串
   * 
   * @return 标签名称
   */
  public String getString() {
    if (usedCount > 1) {
      return name;
    } else {
      return null;
    }
  }

  /**
   * 增加标签的使用次数1
   */
  public void inc() {
    usedCount++;
  }

  public int getUsedCount() {
    return usedCount;
  }

  public void setString(String name) {
    this.name = name;
  }
}
