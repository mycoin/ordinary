package org.lilystudio.javascript.expression;

import org.lilystudio.javascript.AbstractNode;
import org.lilystudio.javascript.IExpression;

/**
 * 主体表达式节点
 * 
 * @version 1.0.0, 2010/01/01
 * @author 欧阳先伟
 * @since Common 0.1
 */
public abstract class PrimaryExpression extends AbstractNode implements
    IExpression {

  /**
   * 创建主体表达式节点
   * 
   * @param lineno
   *          节点的行号
   */
  public PrimaryExpression(int lineno) {
    super(lineno);
  }

  public int getLevel() {
    return 10;
  }
}
