package org.lilystudio.javascript.expression;

import java.io.IOException;
import java.io.Writer;

import org.lilystudio.javascript.Environment;
import org.lilystudio.javascript.ExpressionList;
import org.lilystudio.javascript.IExpression;
import org.lilystudio.javascript.scope.Scope;
import org.mozilla.javascript.Node;
import org.mozilla.javascript.ScriptOrFnNode;

/**
 * 数组常量表达式节点
 * 
 * @version 1.0.0, 2010/01/01
 * @author 欧阳先伟
 * @since Common 0.1
 */
public class ArrayLiteral extends PrimaryExpression {

  /** 数组项表达式列表 */
  private ExpressionList items = new ExpressionList();

  /**
   * 创建数组常量表达式节点
   * 
   * @param lineno
   *          节点的行号
   */
  public ArrayLiteral(int lineno) {
    super(lineno);
  }

  /**
   * 创建数组常量表达式节点
   * 
   * @param node
   *          表达式子节点对应的rhino节点
   * @param root
   *          表达式子节点对应的rhino根节点
   * @param scope
   *          表达式子节点生存域
   */
  public ArrayLiteral(Node node, ScriptOrFnNode root, Scope scope) {
    super(node.getLineno());
    for (Node childNode = node.getFirstChild(); childNode != null; childNode = childNode
        .getNext()) {
      IExpression item = addExpression(childNode, root, scope);
      items.add(item);
    }
  }

  @Override
  public boolean equals(Object obj) {
    return super.equals(obj) && items.equals(((ArrayLiteral) obj).items);
  }

  public boolean isNeedLeftSeparator() {
    return false;
  }

  public boolean isNeedRightSeparator() {
    return false;
  }

  public void write(Writer writer, Environment env) throws IOException {
    writer.write("[");
    items.write(writer, env);
    writer.write("]");
  }
}
