package org.lilystudio.ordinary.web.cache.je;

/**
 * 缓存关键字描述信息对象<br>
 * 
 * <b>属性</b>
 * 
 * <pre>
 * name--关键字名称
 * value--关键字的值, 如果省略, 将从用户数据集合中取名称为name的对象
 * </pre>
 * 
 * @version 0.1.4, 2009/01/10
 * @author 欧阳先伟
 * @since Ordinary 0.1
 */
public class Key {

  /** 关键字名称 */
  private String name;

  /** 关键字值的名称 */
  private String value;

  /**
   * 获取关键字名称
   * 
   * @return 关键字名称
   */
  public String getName() {
    return name;
  }

  /**
   * 获取关键字的默认值
   * 
   * @return 关键字默认值
   */
  public String getValue() {
    return value;
  }
}
