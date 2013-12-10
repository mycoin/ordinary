package org.lilystudio.javascript.statement;

import org.lilystudio.javascript.AbstractNode;
import org.lilystudio.javascript.IStatement;
import org.mozilla.javascript.Node;

/**
 * 语句节点虚基类
 * 
 * @version 1.0.0, 2010/01/01
 * @author 欧阳先伟
 * @since Common 0.1
 */
public abstract class Statement extends AbstractNode implements IStatement {

  /** 下一个语句的rhino节点 */
  private Node nextNode;

  /**
   * 创建语句节点
   * 
   * @param lineno
   *          语句子节点的行号
   */
  public Statement(int lineno) {
    super(lineno);
  }

  /**
   * 创建语句节点
   * 
   * @param node
   *          语句子节点对应的rhino节点
   */
  public Statement(Node node) {
    super(node.getLineno());
    nextNode = node.getNext();
  }

  public Node getNext() {
    return nextNode;
  }

  public void setNext(Node nextNode) {
    this.nextNode = nextNode;
  }
}
