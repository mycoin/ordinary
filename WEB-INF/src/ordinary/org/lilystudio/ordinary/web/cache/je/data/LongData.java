package org.lilystudio.ordinary.web.cache.je.data;

/**
 * 长整型元数据操作接口, 用于基本数据向字节数据的转换
 * 
 * @version 0.1.4, 2009/01/10
 * @author 欧阳先伟
 * @since Common 0.1
 */
public class LongData extends DefaultData {

  /** 序列化编号 */
  private static final long serialVersionUID = 1L;

  /** 长整型数值 */
  private final long value;

  /**
   * 初始化长整数元数据
   * 
   * @param name
   *          元数据的名称
   * @param value
   *          长整型数值
   */
  public LongData(String name, long value) {
    super(name);
    setBytes(toBytes(value));
    this.value = value;
  }

  /**
   * 将长整数转换为字节数组
   * 
   * @param value
   *          长整数值
   * @return 转换的结果
   */
  public static byte[] toBytes(long value) {
    byte[] bs = new byte[8];
    bs[0] = (byte) (value >> 56);
    bs[1] = (byte) (value >> 48);
    bs[2] = (byte) (value >> 40);
    bs[3] = (byte) (value >> 32);
    bs[4] = (byte) (value >> 24);
    bs[5] = (byte) (value >> 16);
    bs[6] = (byte) (value >> 8);
    bs[7] = (byte) (value & 0xFF);
    return bs;
  }

  @Override
  public String toString() {
    return Long.toString(value);
  }
}
