package org.lilystudio.javascript;

/**
 * 压缩工作环境
 * 
 * @version 1.0.0, 2010/01/01
 * @author 欧阳先伟
 * @since Common 0.1
 */
public class Environment {

  /** 是否保持行号 */
  private boolean keepLineno;

  /** 压缩模式 */
  private int mode;

  /** 当前处理到的行号 */
  private int lineno;

  /**
   * 创建压缩工作环境
   * 
   * @param keepLineno
   *          是否保持行号
   * @param mode
   *          压缩模式
   */
  public Environment(boolean keepLineno, int mode) {
    this.keepLineno = keepLineno;
    this.mode = mode;
  }

  /**
   * 判断是否保持行号
   * 
   * @return 是否保持行号
   */
  public boolean isKeepLineno() {
    return keepLineno && mode != JSCompressor.TEXT_COMPRESS;
  }

  /**
   * 获取压缩模式
   * 
   * @return 压缩模式
   */
  public int getMode() {
    return mode;
  }

  /**
   * 获取当前压缩到的行号
   * 
   * @return 当前行号
   */
  public int getLineno() {
    return lineno;
  }

  /**
   * 设置当前压缩到的行号
   * 
   * @param lineno
   */
  public void setLineno(int lineno) {
    this.lineno = lineno;
  }
}
