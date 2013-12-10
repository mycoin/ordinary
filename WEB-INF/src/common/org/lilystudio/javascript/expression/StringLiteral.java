package org.lilystudio.javascript.expression;

import java.io.IOException;
import java.io.Writer;

import org.lilystudio.javascript.Environment;
import org.lilystudio.javascript.Utils;
import org.lilystudio.javascript.scope.Constant;
import org.lilystudio.javascript.scope.Scope;
import org.mozilla.javascript.Node;
import org.mozilla.javascript.ScriptOrFnNode;

/**
 * 字符串常量表达式节点
 * 
 * @version 1.0.0, 2010/01/01
 * @author 欧阳先伟
 * @since Common 0.1
 */
public class StringLiteral extends PrimaryExpression {

  /** 字符串常量 */
  private Constant literal;

  /**
   * 创建字符串常量表达式节点
   * 
   * @param node
   *          表达式子节点对应的rhino节点
   * @param root
   *          表达式子节点对应的rhino根节点
   * @param scope
   *          表达式子节点生存域
   */
  public StringLiteral(Node node, ScriptOrFnNode root, Scope scope) {
    super(node.getLineno());
    literal = scope.addConstant(Utils.escapeJSString(node.getString()), false);
  }

  @Override
  public boolean equals(Object obj) {
    return super.equals(obj) && literal.equals(((StringLiteral) obj).literal);
  }

  public boolean isNeedLeftSeparator() {
    return literal.isUsed();
  }

  public boolean isNeedRightSeparator() {
    return literal.isUsed();
  }

  public void write(Writer writer, Environment env) throws IOException {
    writer.write(literal.getString(false));
  }
}
