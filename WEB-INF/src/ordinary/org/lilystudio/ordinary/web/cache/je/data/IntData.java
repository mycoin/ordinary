package org.lilystudio.ordinary.web.cache.je.data;

/**
 * 整型元数据操作接口, 用于基本数据向字节数据的转换
 * 
 * @version 0.1.4, 2009/01/10
 * @author 欧阳先伟
 * @since Common 0.1
 */
public class IntData extends DefaultData {

  /** 序列化编号 */
  private static final long serialVersionUID = 1L;

  /** 整型数值 */
  private final int value;

  /**
   * 初始化整数元数据
   * 
   * @param name
   *          元数据的名称
   * @param value
   *          整型数值
   */
  public IntData(String name, int value) {
    super(name);
    setBytes(toBytes(value));
    this.value = value;
  }

  /**
   * 将整数转换为字节数组
   * 
   * @param value
   *          整数值
   * @return 转换的结果
   */
  public static byte[] toBytes(int value) {
    byte[] data = new byte[4];
    data[0] = (byte) (value >> 24);
    data[1] = (byte) (value >> 16);
    data[2] = (byte) (value >> 8);
    data[3] = (byte) (value & 0xFF);
    return data;
  }

  @Override
  public String toString() {
    return Integer.toString(value);
  }
}
