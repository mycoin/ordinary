package org.lilystudio.ordinary.web.cache.je.data;

/**
 * 布尔元数据对象
 * 
 * @version 0.1.4, 2009/01/10
 * @author 欧阳先伟
 * @since Common 0.1
 */
public class BooleanData extends DefaultData {

  /** 序列化编号 */
  private static final long serialVersionUID = 1L;

  /** 布尔真数值 */
  public static final byte[] TRUE = new byte[] { 1 };

  /** 布尔假数值 */
  public static final byte[] FALSE = new byte[] { 0 };

  /** 布尔数值 */
  private final boolean value;

  /**
   * 初始化布尔元数据
   * 
   * @param name
   *          元数据的名称
   * @param value
   *          布尔数值
   */
  public BooleanData(String name, boolean value) {
    super(name);
    setBytes(value ? TRUE : FALSE);
    this.value = value;
  }

  @Override
  public String toString() {
    return Boolean.toString(value);
  }
}
