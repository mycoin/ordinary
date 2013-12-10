package org.lilystudio.ordinary.web.cache.je.data;

/**
 * NULL元数据对象
 * 
 * @version 0.1.4, 2009/01/10
 * @author 欧阳先伟
 * @since Common 0.1
 */
public class NullData extends DefaultData {

  /** 序列化编号 */
  private static final long serialVersionUID = 1L;
  
  /** 空数值 */
  public static final byte[] VALUE = new byte[0];

  /**
   * 初始化NULL元数据
   * 
   * @param name
   *          元数据的名称
   */
  public NullData(String name) {
    super(name);
    setBytes(VALUE);
  }
  
  @Override
  public String toString() {
    return null;
  }
}
