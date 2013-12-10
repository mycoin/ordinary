package org.lilystudio.javascript.expression;

import java.io.IOException;
import java.io.Writer;

import org.lilystudio.javascript.Environment;
import org.lilystudio.javascript.scope.Constant;
import org.lilystudio.javascript.scope.Scope;
import org.mozilla.javascript.Node;
import org.mozilla.javascript.ScriptOrFnNode;

/**
 * this常量表达式节点
 * 
 * @version 1.0.0, 2010/01/01
 * @author 欧阳先伟
 * @since Common 0.1
 */
public class ThisExpression extends PrimaryExpression {

  /** this常量 */
  private Constant name;

  /**
   * 创建this常量表达式节点
   * 
   * @param node
   *          表达式子节点对应的rhino节点
   * @param root
   *          表达式子节点对应的rhino根节点
   * @param scope
   *          表达式子节点生存域
   */
  public ThisExpression(Node node, ScriptOrFnNode root, Scope scope) {
    super(node.getLineno());
    name = scope.addConstant("this", false);
  }

  @Override
  public boolean equals(Object obj) {
    return super.equals(obj) && name.equals(((ThisExpression) obj).name);
  }

  public boolean isNeedLeftSeparator() {
    return true;
  }

  public boolean isNeedRightSeparator() {
    return true;
  }

  public void write(Writer writer, Environment env) throws IOException {
    writer.write(name.getString(false));
  }
}
