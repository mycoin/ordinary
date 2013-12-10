package org.lilystudio.coder;

/**
 * BASE编码器
 * 
 * @version 0.1.4, 2008/12/12
 * @author 欧阳先伟
 * @since Common 0.1
 */
public class BASE64Encoder {

  /** 编码表 */
  private static final char[] table = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H',
      'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V',
      'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
      'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x',
      'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/' };

  /**
   * 对数据进行BASE64编码
   * 
   * @param bs
   *          需要编码的数据
   * @return 编码后的结果
   */
  public static String encode(byte[] bs) {
    int dataLength = bs.length;
    int charLength = ((dataLength + 2) / 3) * 4;
    char[] chars = new char[charLength];

    // 按每3个一组生成4个字符
    for (int i = 2, j = 0; i < dataLength; i += 3, j += 4) {
      int tmp = (bs[i - 2] << 16) | ((bs[i - 1] & 0xFF) << 8) | (bs[i] & 0xFF);
      chars[j] = table[(tmp >> 18) & 0x3F];
      chars[j + 1] = table[(tmp >> 12) & 0x3F];
      chars[j + 2] = table[(tmp >> 6) & 0x3F];
      chars[j + 3] = table[tmp & 0x3F];
    }

    // 将不够3个一组的尾部结束的字节转换成字符
    switch (dataLength % 3) {
    case 1: {
      int tmp = bs[dataLength - 1] & 0xFF;
      chars[charLength - 4] = table[(tmp >> 2) & 0x3F];
      chars[charLength - 3] = table[(tmp << 4) & 0x3F];
      chars[charLength - 2] = '=';
      chars[charLength - 1] = '=';
      break;
    }
    case 2: {
      int tmp = (bs[dataLength - 2] << 8) | (bs[dataLength - 1] & 0xFF);
      chars[charLength - 4] = table[(tmp >> 10) & 0x3F];
      chars[charLength - 3] = table[(tmp >> 4) & 0x3F];
      chars[charLength - 2] = table[(tmp << 2) & 0x3F];
      chars[charLength - 1] = '=';
    }
    }

    return new String(chars);
  }
}
