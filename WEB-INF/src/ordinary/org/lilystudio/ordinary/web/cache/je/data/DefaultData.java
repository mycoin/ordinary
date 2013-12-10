package org.lilystudio.ordinary.web.cache.je.data;

/**
 * 缺省的元数据操作接口, 用于基本数据向字节数据的转换
 * 
 * @version 0.1.4, 2009/01/10
 * @author 欧阳先伟
 * @since Common 0.1
 */
public class DefaultData implements IMetaData {

  /** 序列化编号 */
  private static final long serialVersionUID = 1L;

  /** 元数据名称 */
  private String name;

  /** 结果数据 */
  private byte[] data;

  /**
   * 初始化整数元数据
   * 
   * @param name
   *          元数据的名称
   */
  public DefaultData(String name) {
    this.name = name;
  }

  /**
   * 设置元数据的字节数组
   * 
   * @param data
   *          数据数组
   */
  protected void setBytes(byte[] data) {
    this.data = data;
  }

  public String getName() {
    return name;
  }

  public byte[] getBytes() {
    return data;
  }
}
