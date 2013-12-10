package org.lilystudio.javascript.expression;

import java.io.IOException;
import java.io.Writer;

import org.lilystudio.javascript.AbstractNode;
import org.lilystudio.javascript.Environment;
import org.lilystudio.javascript.IExpression;
import org.lilystudio.javascript.Utils;
import org.lilystudio.javascript.scope.Constant;
import org.lilystudio.javascript.scope.Scope;
import org.mozilla.javascript.Node;
import org.mozilla.javascript.ScriptOrFnNode;
import org.mozilla.javascript.Token;

/**
 * 一元表达式节点
 * 
 * @version 1.0.0, 2010/01/01
 * @author 欧阳先伟
 * @since Common 0.1
 */
public class UnaryExpression extends AbstractNode implements IExpression {

  /** 一元表达式类型 */
  private int tokenType;

  /** 一元表达式需要操作的表达式 */
  private IExpression target;

  /** 成员常量 */
  private Constant member;

  /** 成员表达式 */
  private IExpression memberExpression;

  /**
   * 创建一元表达式节点
   * 
   * @param lineno
   *          节点的行号
   * @param target
   *          表达式
   * @param tokenType
   *          表达式类型
   */
  public UnaryExpression(int lineno, int tokenType, IExpression target) {
    super(lineno);
    this.tokenType = tokenType;
    this.target = target;
  }

  /**
   * 创建一元表达式节点
   * 
   * @param node
   *          表达式子节点对应的rhino节点
   * @param root
   *          表达式子节点对应的rhino根节点
   * @param scope
   *          表达式子节点生存域
   */
  public UnaryExpression(Node node, ScriptOrFnNode root, Scope scope) {
    super(node.getLineno());
    tokenType = node.getType();
    if (tokenType == Token.TYPEOFNAME) {
      tokenType = Token.TYPEOF;
      target = new IdentifierExpression(node, root, scope);
      target.setParent(this);
    } else {
      Node firstChild = node.getFirstChild();
      if (tokenType == Token.DELPROP) {
        if (firstChild.getType() == Token.BINDNAME) {
          target = new IdentifierExpression(firstChild.getLineno(), firstChild
              .getString());
          target.setParent(this);
          return;
        }

        node = firstChild.getNext();
        if (node.getType() == Token.STRING) {
          member = scope.addConstant(node.getString(), true);
        } else {
          memberExpression = addExpression(node, root, scope);
        }
      }

      target = addExpression(firstChild, root, scope);
    }
  }

  /**
   * 获取表达式的类型
   * 
   * @return 表达式的类型
   */
  public int getType() {
    return tokenType;
  }

  @Override
  public boolean equals(Object obj) {
    return obj.getClass() == this.getClass()
        && tokenType == ((UnaryExpression) obj).tokenType
        && target.equals(((UnaryExpression) obj).target)
        && (member != null ? member.equals(((UnaryExpression) obj).member)
            : memberExpression.equals(((UnaryExpression) obj).memberExpression));
  }

  public int getLevel() {
    return 40;
  }

  public boolean isNeedLeftSeparator() {
    return tokenType == Token.TYPEOF || tokenType == Token.DELPROP
        || tokenType == Token.DEL_REF || tokenType == Token.VOID;
  }

  public boolean isNeedRightSeparator() {
    if (member != null) {
      return true;
    }
    if (target.getLevel() < this.getLevel()) {
      return target.isNeedRightSeparator();
    }
    return false;
  }

  public void write(Writer writer, Environment env) throws IOException {
    writer.write(Utils.escapeLiteral(tokenType));

    if (target.getLevel() < this.getLevel()) {
      if (tokenType == Token.DELPROP || tokenType == Token.TYPEOF
          || tokenType == Token.VOID && target.isNeedLeftSeparator()) {
        writer.write(" ");
      }
      target.write(writer, env);
    } else {
      writer.write("(");
      target.write(writer, env);
      writer.write(")");
    }

    if (member != null) {
      writer.write(member.getString(true));
    } else if (memberExpression != null) {
      writer.write("[");
      memberExpression.write(writer, env);
      writer.write("]");
    }
  }
}
