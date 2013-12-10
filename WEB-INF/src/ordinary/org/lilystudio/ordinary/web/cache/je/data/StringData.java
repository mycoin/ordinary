package org.lilystudio.ordinary.web.cache.je.data;

/**
 * 字符串元数据操作接口, 用于基本数据向字节数据的转换
 * 
 * @version 0.1.4, 2009/01/10
 * @author 欧阳先伟
 * @since Common 0.1
 */
public class StringData extends DefaultData {

  /** 序列化编号 */
  private static final long serialVersionUID = 1L;

  /** 字符串 */
  private String value;

  /**
   * 初始化字符串元数据
   * 
   * @param name
   *          元数据的名称
   * @param value
   *          字符串
   */
  public StringData(String name, String value) {
    super(name);
    try {
      setBytes(value.getBytes("UTF-8"));
    } catch (Exception e) {
    }
    this.value = value;
  }

  @Override
  public String toString() {
    return value;
  }
}
