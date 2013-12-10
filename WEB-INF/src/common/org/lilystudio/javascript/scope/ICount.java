package org.lilystudio.javascript.scope;

/**
 * 统计接口
 * 
 * @version 1.0.0, 2010/01/01
 * @author 欧阳先伟
 * @since Common 0.1
 */
public interface ICount {

  /**
   * 获取使用的次数
   * 
   * @return 使用的次数
   */
  public int getUsedCount();

  /**
   * 设置压缩后的名称字符串
   * 
   * @param name
   *          名称
   */
  public void setString(String name);
}
