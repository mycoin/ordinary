package org.lilystudio.javascript.expression;

import java.io.IOException;
import java.io.Writer;

import org.lilystudio.javascript.AbstractNode;
import org.lilystudio.javascript.Environment;
import org.lilystudio.javascript.IExpression;
import org.lilystudio.javascript.Utils;
import org.lilystudio.javascript.scope.Scope;
import org.mozilla.javascript.Node;
import org.mozilla.javascript.ScriptOrFnNode;

/**
 * 自增/减表达式节点
 * 
 * @version 1.0.0, 2010/01/01
 * @author 欧阳先伟
 * @since Common 0.1
 */
public class PostfixExpression extends AbstractNode implements IExpression {

  /** 需要自增/减操作的表达式 */
  private IExpression target;

  /** 操作类型 */
  private int tokenType;

  /**
   * 创建自增/减表达式节点
   * 
   * @param node
   *          表达式子节点对应的rhino节点
   * @param root
   *          表达式子节点对应的rhino根节点
   * @param scope
   *          表达式子节点生存域
   */
  public PostfixExpression(Node node, ScriptOrFnNode root, Scope scope) {
    super(node.getLineno());
    target = addExpression(node.getFirstChild(), root, scope);
    tokenType = node.getType();
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
        && target.equals(((PostfixExpression) obj).target)
        && tokenType == ((PostfixExpression) obj).tokenType;
  }

  public int getLevel() {
    return 30;
  }

  public boolean isNeedLeftSeparator() {
    if (target.getLevel() <= this.getLevel()) {
      return target.isNeedLeftSeparator();
    }
    return false;
  }

  public boolean isNeedRightSeparator() {
    return true;
  }

  public void write(Writer writer, Environment env) throws IOException {
    if (target.getLevel() <= this.getLevel()) {
      target.write(writer, env);
    } else {
      writer.write("(");
      target.write(writer, env);
      writer.write(")");
    }
    writer.write(Utils.escapeLiteral(tokenType));
  }
}
