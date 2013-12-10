package org.lilystudio.coder;

/**
 * BASE解码器
 * 
 * @version 0.1.4, 2008/12/12
 * @author 欧阳先伟
 * @since Common 0.1
 */
public class BASE64Decoder {

  /** 解码表 */
  private static final int[] table;

  static {
    table = new int[128];
    for (int i = 0; i < 128; i++) {
      table[i] = -1;
    }
    for (int i = 'A'; i <= 'Z'; i++) {
      table[i] = (byte) (i - 'A');
    }
    for (int i = 'a'; i <= 'z'; i++) {
      table[i] = (byte) (i - 'a' + 26);
    }
    for (int i = '0'; i <= '9'; i++) {
      table[i] = (byte) (i - '0' + 52);
    }
    table['+'] = 62;
    table['/'] = 63;
  }

  /**
   * 对数据进行BASE64解码
   * 
   * @param cs
   *          需要解码的数据
   * @return 解码后的结果
   */
  public static byte[] decode(CharSequence cs) {
    int length = cs.length();
    // 去除尾部多余的符号
    while (cs.charAt(length - 1) == '=') {
      length--;
    }
    // 转换结果
    byte[] bytes = new byte[length / 4 * 3 + (length % 4) * 3 / 4];
    int byteLength = 0;
    // 读取所有合法的数据
    int value = 0;
    for (int i = 0; i < length; i++) {
      char c = cs.charAt(i);
      if (c < 128) {
        value = (value << 6) | table[c];
        if (i % 4 == 3) {
          bytes[byteLength] = (byte) (value >> 16);
          bytes[byteLength + 1] = (byte) (value >> 8);
          bytes[byteLength + 2] = (byte) value;
          byteLength += 3;
          value = 0;
        }
      }
    }

    // 输出尾部不足3个字节的部分的数据
    switch (length % 4) {
    case 2:
      bytes[byteLength] = (byte) (value >> 4);
      break;
    case 3:
      bytes[byteLength] = (byte) (value >> 10);
      bytes[byteLength + 1] = (byte) (value >> 2);
    }

    return bytes;
  }
}
