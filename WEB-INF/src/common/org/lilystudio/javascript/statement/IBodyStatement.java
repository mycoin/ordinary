package org.lilystudio.javascript.statement;

/**
 * 区块语句接口
 * 
 * @version 1.0.0, 2010/01/01
 * @author 欧阳先伟
 * @since Common 0.1
 */
public interface IBodyStatement {

  /**
   * 判断区块的内容是否仅为没有else的if语句
   * 
   * @return 区块的内容是否仅为没有else的if语句
   */
  public boolean isIfBody();
}
