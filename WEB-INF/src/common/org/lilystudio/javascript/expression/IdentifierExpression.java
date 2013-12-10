package org.lilystudio.javascript.expression;

import java.io.IOException;
import java.io.Writer;

import org.lilystudio.javascript.Environment;
import org.lilystudio.javascript.scope.Scope;
import org.lilystudio.javascript.scope.Identifier;
import org.mozilla.javascript.Node;
import org.mozilla.javascript.ScriptOrFnNode;

/**
 * 标识符表达式节点
 * 
 * @version 1.0.0, 2010/01/01
 * @author 欧阳先伟
 * @since Common 0.1
 */
public class IdentifierExpression extends PrimaryExpression {

  /** 标识符名称 */
  private Identifier name;

  /** 锁定名称的标识符 */
  private String bindName;

  /**
   * 创建函数表达式节点
   * 
   * @param lineno
   *          节点的行号
   * @param bindName
   *          绑定的标识符名称
   */
  public IdentifierExpression(int lineno, String bindName) {
    super(lineno);
    this.bindName = bindName;
  }

  /**
   * 创建标识符表达式节点
   * 
   * @param node
   *          表达式子节点对应的rhino节点
   * @param root
   *          表达式子节点对应的rhino根节点
   * @param scope
   *          表达式子节点生存域
   */
  public IdentifierExpression(Node node, ScriptOrFnNode root, Scope scope) {
    super(node.getLineno());
    if (node.getString().equals("eval")) {
      scope.lockScope();
    }
    name = scope.addIdentifier(node.getString());
  }

  @Override
  public boolean equals(Object obj) {
    return super.equals(obj)
        && (name != null ? name.equals(((IdentifierExpression) obj).name)
            : bindName.equals(((IdentifierExpression) obj).bindName));
  }

  public Identifier getName() {
    return name;
  }
  
  public boolean isNeedLeftSeparator() {
    return true;
  }

  public boolean isNeedRightSeparator() {
    return true;
  }

  public void write(Writer writer, Environment env) throws IOException {
    writer.write(bindName != null ? bindName : name.getString());
  }
}
