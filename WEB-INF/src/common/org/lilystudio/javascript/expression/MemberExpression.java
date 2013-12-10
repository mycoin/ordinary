package org.lilystudio.javascript.expression;

import java.io.IOException;
import java.io.Writer;

import org.lilystudio.javascript.Environment;
import org.lilystudio.javascript.IExpression;
import org.lilystudio.javascript.scope.Constant;
import org.lilystudio.javascript.scope.Scope;
import org.mozilla.javascript.Node;
import org.mozilla.javascript.ScriptOrFnNode;
import org.mozilla.javascript.Token;

/**
 * 成员表达式节点
 * 
 * @version 1.0.0, 2010/01/01
 * @author 欧阳先伟
 * @since Common 0.1
 */
public class MemberExpression extends PrimaryExpression {

  /** 成员父对象表达式 */
  private IExpression target;

  /** 成员名称常量 */
  private Constant member;

  /** 成员表达式 */
  private IExpression memberExpression;

  /**
   * 创建成员表达式节点
   * 
   * @param node
   *          表达式子节点对应的rhino节点
   * @param root
   *          表达式子节点对应的rhino根节点
   * @param scope
   *          表达式子节点生存域
   */
  public MemberExpression(Node node, ScriptOrFnNode root, Scope scope) {
    super(node.getLineno());
    Node firstChild = node.getFirstChild();
    target = addExpression(firstChild, root, scope);

    node = firstChild.getNext();
    if (node.getType() == Token.STRING) {
      member = scope.addConstant(node.getString(), true);
    } else {
      memberExpression = addExpression(node, root, scope);
    }
  }

  @Override
  public boolean equals(Object obj) {
    return super.equals(obj)
        && target.equals(((MemberExpression) obj).target)
        && (member != null ? member.equals(((MemberExpression) obj).member)
            : memberExpression
                .equals(((MemberExpression) obj).memberExpression));
  }

  public boolean isNeedLeftSeparator() {
    if (this.getLevel() <= target.getLevel()) {
      return target.isNeedLeftSeparator();
    }
    return false;
  }

  public boolean isNeedRightSeparator() {
    return member != null;
  }

  public void write(Writer writer, Environment env) throws IOException {
    if (this.getLevel() >= target.getLevel()) {
      target.write(writer, env);
    } else {
      writer.write("(");
      target.write(writer, env);
      writer.write(")");
    }

    if (member != null) {
      writer.write(member.getString(true));
    } else {
      writer.write("[");
      memberExpression.write(writer, env);
      writer.write("]");
    }
  }
}
