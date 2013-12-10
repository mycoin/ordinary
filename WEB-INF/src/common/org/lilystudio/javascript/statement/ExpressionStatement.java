package org.lilystudio.javascript.statement;

import java.io.IOException;
import java.io.Writer;

import org.lilystudio.javascript.Environment;
import org.lilystudio.javascript.IExpression;
import org.lilystudio.javascript.expression.FunctionLiteral;
import org.lilystudio.javascript.expression.ObjectLiteral;
import org.lilystudio.javascript.scope.Scope;
import org.mozilla.javascript.Node;
import org.mozilla.javascript.ScriptOrFnNode;

/**
 * 表达式语句节点
 * 
 * @version 1.0.0, 2010/01/01
 * @author 欧阳先伟
 * @since Common 0.1
 */
public class ExpressionStatement extends Statement {

  /** 表达式节点 */
  private IExpression expression;

  /**
   * 创建表达式语句节点
   * 
   * @param node
   *          语句子节点对应的rhino节点
   * @param root
   *          语句子节点对应的rhino根节点
   * @param scope
   *          语句子节点生存域
   */
  public ExpressionStatement(Node node, ScriptOrFnNode root, Scope scope) {
    super(node);
    expression = addExpression(node.getFirstChild(), root, scope);
  }

  public boolean isNeedLeftSeparator() {
    return expression.isNeedLeftSeparator();
  }

  public boolean isNeedRightSeparator() {
    return true;
  }

  public void write(Writer writer, Environment env) throws IOException {
    boolean flag = expression instanceof ObjectLiteral
        || expression instanceof FunctionLiteral;
    if (flag) {
      writer.write("(");
    }

    expression.write(writer, env);

    if (flag) {
      writer.write(")");
    }
  }
}
