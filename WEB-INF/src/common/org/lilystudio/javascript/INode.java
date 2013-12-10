package org.lilystudio.javascript;

/**
 * 语法树节点接口
 * 
 * @version 1.0.0, 2010/01/01
 * @author 欧阳先伟
 * @since Common 0.1
 */
public interface INode extends IWriteable {

  /**
   * 获取父节点
   * 
   * @return 父节点
   */
  public INode getParent();

  /**
   * 设置父节点
   * 
   * @param parent
   *          父节点
   */
  public void setParent(INode parent);

  /**
   * 获取语句的行号
   * 
   * @return 语句的行号
   */
  public int getLineno();

  /**
   * 判断表达式左部是否需要添加分隔符
   * 
   * @return 左部是否需要分隔符
   */
  public boolean isNeedLeftSeparator();

  /**
   * 判断表达式右部是否需要添加分隔符
   * 
   * @return 右部是否需要分隔符
   */
  public boolean isNeedRightSeparator();
}
