package org.lilystudio.javascript.expression;

import java.io.IOException;
import java.io.Writer;

import org.lilystudio.javascript.Environment;
import org.lilystudio.javascript.ExpressionList;
import org.lilystudio.javascript.IExpression;
import org.lilystudio.javascript.scope.Scope;
import org.mozilla.javascript.Node;
import org.mozilla.javascript.ScriptOrFnNode;
import org.mozilla.javascript.Token;

/**
 * 函数调用表达式节点
 * 
 * @version 1.0.0, 2010/01/01
 * @author 欧阳先伟
 * @since Common 0.1
 */
public class CallExpression extends PrimaryExpression {

  /** 函数调用的类型 */
  private int tokenType;

  /** 函数调用者的表达式 */
  private IExpression caller;

  /** 函数参数 */
  private ExpressionList params = new ExpressionList();

  /**
   * 创建函数调用表达式节点
   * 
   * @param node
   *          表达式子节点对应的rhino节点
   * @param root
   *          表达式子节点对应的rhino根节点
   * @param scope
   *          表达式子节点生存域
   */
  public CallExpression(Node node, ScriptOrFnNode root, Scope scope) {
    super(node.getLineno());
    tokenType = node.getType();

    Node child = node.getFirstChild();
    caller = addExpression(child, root, scope);
    while ((child = child.getNext()) != null) {
      params.add(addExpression(child, root, scope));
    }
  }

  @Override
  public boolean equals(Object obj) {
    return super.equals(obj) && caller.equals(((CallExpression) obj).caller)
        && params.equals(((CallExpression) obj).params);
  }

  public int getLevel() {
    return super.getLevel() + (tokenType == Token.NEW ? 1 : 0);
  }

  public boolean isNeedLeftSeparator() {
    if (caller.getLevel() <= this.getLevel()) {
      return caller.isNeedLeftSeparator();
    }
    return false;
  }

  public boolean isNeedRightSeparator() {
    return true;
  }

  public void write(Writer writer, Environment env) throws IOException {
    if (tokenType == Token.NEW) {
      writer.write("new");
      if (caller.getLevel() <= this.getLevel()) {
        writer.write(" ");
      }
    }

    if (caller.getLevel() <= this.getLevel()) {
      caller.write(writer, env);
    } else {
      writer.write("(");
      caller.write(writer, env);
      writer.write(")");
    }

    writer.write("(");
    params.write(writer, env);
    writer.write(")");
  }
}
