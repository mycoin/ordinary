package org.lilystudio.javascript.expression;

import java.io.IOException;
import java.io.Writer;

import org.lilystudio.javascript.Environment;
import org.lilystudio.javascript.scope.Constant;
import org.lilystudio.javascript.scope.Scope;
import org.mozilla.javascript.Node;
import org.mozilla.javascript.ScriptOrFnNode;

/**
 * 正则表达式常量节点
 * 
 * @version 1.0.0, 2010/01/01
 * @author 欧阳先伟
 * @since Common 0.1
 */
public class RegexpLiteral extends PrimaryExpression {

  private Constant literal;

  /**
   * 创建正则表达式常量节点
   * 
   * @param node
   *          表达式子节点对应的rhino节点
   * @param root
   *          表达式子节点对应的rhino根节点
   * @param scope
   *          表达式子节点生存域
   */
  public RegexpLiteral(Node node, ScriptOrFnNode root, Scope scope) {
    super(node.getLineno());
    int index = node.getIntProp(Node.REGEXP_PROP, 0);
    literal = scope.addConstant("/" + root.getRegexpString(index) + "/"
        + root.getRegexpFlags(index), false);
  }

  @Override
  public boolean equals(Object obj) {
    return super.equals(obj) && literal.equals(((RegexpLiteral) obj).literal);
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
