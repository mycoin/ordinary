package org.lilystudio.coder;

/**
 * 编码/解码器工作异常
 * 
 * @version 0.1.4, 2008/12/12
 * @author 欧阳先伟
 * @since Ordinary 0.1
 */
public class CoderException extends Exception {

  /** 序列化号码 */
  private static final long serialVersionUID = 1L;

  /** 出错位置附近的代码文本 */
  private String code;

  /**
   * 构造异常体
   * 
   * @param message
   *          异常信息
   * @param code
   *          出错的位置附近的文本
   */
  public CoderException(String message, String code) {
    super(message);
    this.code = code;
  }

  @Override
  public String getMessage() {
    StringBuilder sb = new StringBuilder(super.getMessage());
    // HARDCODE
    sb.append(":\n");
    if (code.length() > 64) {
      sb.append(code.substring(0, 61));
      sb.append("...");
    } else {
      sb.append(code);
    }
    return sb.toString();
  }
}
