package org.lilystudio.ordinary.web;

/**
 * 不存在的转向异常, 在找不到Relay中指定的Forward名称时产生
 * 
 * @see org.lilystudio.ordinary.web.Capture
 * @see org.lilystudio.ordinary.web.IResult
 * 
 * @version 0.1.4, 2008/12/12
 * @author 欧阳先伟
 * @since Ordinary 0.1
 */
public class ResultNotFoundException extends Exception {

  /** 序列化编号 */
  private static final long serialVersionUID = 1L;

  /**
   * 构造异常体
   * 
   * @param name
   *          转向的名称(不存在而产生异常)
   */
  public ResultNotFoundException(String name) {
    super(name);
  }

  @Override
  public String getMessage() {
    // HARDCODE
    return "The result element named '" + super.getMessage() + "' does not exist";
  }
}
