package org.lilystudio.ordinary.web.cache.je;

/**
 * 数据类型异常
 * 
 * @version 0.1.4, 2009/01/10
 * @author 欧阳先伟
 * @since Ordinary 0.1
 */
public class DataTypeException extends Exception {

  /** 序号化编号 */
  private static final long serialVersionUID = 1L;

  /**
   * 建立数据类型异常
   * 
   * @param message
   *          异常提示信息
   */
  public DataTypeException(String message) {
    super(message);
  }
}
