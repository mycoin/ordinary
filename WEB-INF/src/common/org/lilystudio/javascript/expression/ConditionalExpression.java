package org.lilystudio.javascript.expression;

import java.io.IOException;
import java.io.Writer;

import org.lilystudio.javascript.AbstractNode;
import org.lilystudio.javascript.Environment;
import org.lilystudio.javascript.IExpression;
import org.lilystudio.javascript.scope.Scope;
import org.mozilla.javascript.Node;
import org.mozilla.javascript.ScriptOrFnNode;

import sun.org.mozilla.javascript.internal.Token;

/**
 * 三元条件表达式节点
 * 
 * @version 1.0.0, 2010/01/01
 * @author 欧阳先伟
 * @since Common 0.1
 */
public class ConditionalExpression extends AbstractNode implements IExpression {

  /** 条件表达式 */
  private IExpression checkExpression;

  /** 条件为真时的表达式 */
  private IExpression trueExpression;

  /** 条件为假时的表达式 */
  private IExpression falseExpression;

  /**
   * 创建三元条件表达式节点
   * 
   * @param node
   *          表达式子节点对应的rhino节点
   * @param root
   *          表达式子节点对应的rhino根节点
   * @param scope
   *          表达式子节点生存域
   */
  public ConditionalExpression(Node node, ScriptOrFnNode root, Scope scope) {
    super(node.getLineno());
    Node firstChild = node.getFirstChild();
    checkExpression = addExpression(firstChild, root, scope);
    trueExpression = addExpression(firstChild.getNext(), root, scope);
    falseExpression = addExpression(node.getLastChild(), root, scope);
  }

  @Override
  public boolean equals(Object obj) {
    return obj.getClass() == this.getClass()
        && checkExpression
            .equals(((ConditionalExpression) obj).checkExpression)
        && trueExpression.equals(((ConditionalExpression) obj).trueExpression)
        && falseExpression
            .equals(((ConditionalExpression) obj).falseExpression);
  }

  public int getLevel() {
    return 120;
  }

  public boolean isNeedLeftSeparator() {
    if (checkExpression.getLevel() <= this.getLevel()) {
      return checkExpression.isNeedLeftSeparator();
    }
    return false;
  }

  public boolean isNeedRightSeparator() {
    if (falseExpression.getLevel() <= this.getLevel()) {
      return falseExpression.isNeedRightSeparator();
    }
    return false;
  }

  public void write(Writer writer, Environment env) throws IOException {
    if (checkExpression.getLevel() <= this.getLevel()) {
      checkExpression.write(writer, env);
    } else {
      writer.write("(");
      checkExpression.write(writer, env);
      writer.write(")");
    }

    writer.write("?");

    if (trueExpression.getLevel() <= this.getLevel()) {
      trueExpression.write(writer, env);
    } else {
      writer.write("(");
      trueExpression.write(writer, env);
      writer.write(")");
    }

    writer.write(":");

    if (falseExpression instanceof BinaryExpression
        && ((BinaryExpression) falseExpression).getType() == Token.COMMA) {
      writer.write("(");
      falseExpression.write(writer, env);
      writer.write(")");
    } else {
      falseExpression.write(writer, env);
    }
  }
}
