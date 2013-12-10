package org.lilystudio.javascript;

import org.mozilla.javascript.Node;

/**
 * 语句节点接口
 * 
 * @version 1.0.0, 2010/01/01
 * @author 欧阳先伟
 * @since Common 0.1
 */
public interface IStatement extends INode {

  /**
   * 获取下一个语句的rhino节点
   * 
   * @return 下一个语句的节点
   */
  public Node getNext();

  /**
   * 设置下一个语句的rhino节点
   * 
   * @param nextNode
   *          下一个语句的节点
   */
  public void setNext(Node nextNode);
}
