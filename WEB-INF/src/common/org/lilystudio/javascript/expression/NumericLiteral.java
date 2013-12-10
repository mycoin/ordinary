package org.lilystudio.javascript.expression;

import java.io.IOException;
import java.io.Writer;

import org.lilystudio.javascript.Environment;
import org.lilystudio.javascript.scope.Constant;
import org.lilystudio.javascript.scope.Scope;
import org.mozilla.javascript.Node;
import org.mozilla.javascript.ScriptOrFnNode;

/**
 * 数值常量表达式节点
 * 
 * @version 1.0.0, 2010/01/01
 * @author 欧阳先伟
 * @since Common 0.1
 */
public class NumericLiteral extends PrimaryExpression {

  private Constant literal;

  /**
   * 创建数值常量表达式节点
   * 
   * @param node
   *          表达式子节点对应的rhino节点
   * @param root
   *          表达式子节点对应的rhino根节点
   * @param scope
   *          表达式子节点生存域
   */
  public NumericLiteral(Node node, ScriptOrFnNode root, Scope scope) {
    super(node.getLineno());
    String literal = Double.toString(node.getDouble());
    if (literal.endsWith(".0")) {
      literal = literal.substring(0, literal.length() - 2);
    } else if (literal.startsWith("0.")) {
      literal = literal.substring(1);
    }
    this.literal = scope.addConstant(literal, false);
  }

  public double getValue() {
    return Double.parseDouble(literal.getLiteral());
  }
  @Override
  public boolean equals(Object obj) {
    return super.equals(obj) && literal.equals(((NumericLiteral) obj).literal);
  }

  public boolean isNeedLeftSeparator() {
    return true;
  }

  public boolean isNeedRightSeparator() {
    return true;
  }

  public void write(Writer writer, Environment env) throws IOException {
    writer.write(literal.getString(false));
  }
}
