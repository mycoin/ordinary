package org.lilystudio.javascript;

import org.lilystudio.javascript.scope.Scope;
import org.mozilla.javascript.Node;
import org.mozilla.javascript.ScriptOrFnNode;

/**
 * 语法树节点虚基类
 * 
 * @version 1.0.0, 2010/01/01
 * @author 欧阳先伟
 * @since Common 0.1
 */
public abstract class AbstractNode implements INode {

  /** 语法树节点接口 */
  private INode parent;

  /** 节点所在的行 */
  private int lineno;

  /**
   * 创建节点
   * 
   * @param lineno
   *          节点的行号
   */
  public AbstractNode(int lineno) {
    this.lineno = lineno;
  }

  /**
   * 为节点添加一个语句子节点
   * 
   * @param node
   *          语句子节点对应的rhino节点
   * @param root
   *          语句子节点对应的rhino根节点
   * @param scope
   *          语句子节点生存域
   * @return 语句子节点
   */
  public IStatement addStatement(Node node, ScriptOrFnNode root, Scope scope) {
    IStatement statement = Utils.createStatement(node, root, scope);
    statement.setParent(this);
    return statement;
  }

  /**
   * 为节点添加一个表达式子节点
   * 
   * @param node
   *          表达式子节点对应的rhino节点
   * @param root
   *          表达式子节点对应的rhino根节点
   * @param scope
   *          表达式子节点生存域
   * @return 表达式子节点
   */
  public IExpression addExpression(Node node, ScriptOrFnNode root, Scope scope) {
    IExpression expression = Utils.createExpression(node, root, scope);
    expression.setParent(this);
    return expression;
  }

  @Override
  public boolean equals(Object obj) {
    return obj.getClass() == this.getClass();
  }

  public INode getParent() {
    return parent;
  }

  public void setParent(INode parent) {
    this.parent = parent;
  }

  public int getLineno() {
    return lineno;
  }
}
