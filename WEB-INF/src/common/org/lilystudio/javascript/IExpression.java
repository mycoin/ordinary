package org.lilystudio.javascript;

/**
 * 表达式节点接口
 * 
 * @version 1.0.0, 2010/01/01
 * @author 欧阳先伟
 * @since Common 0.1
 */
public interface IExpression extends INode {

  /**
   * 获取表达式节点的优先级
   * 
   * @return 表达式节点的优先级
   */
  public int getLevel();
}
