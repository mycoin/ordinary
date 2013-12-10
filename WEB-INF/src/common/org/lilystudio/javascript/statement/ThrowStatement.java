package org.lilystudio.javascript.statement;

import java.io.IOException;
import java.io.Writer;

import org.lilystudio.javascript.Environment;
import org.lilystudio.javascript.IExpression;
import org.lilystudio.javascript.scope.Scope;
import org.mozilla.javascript.Node;
import org.mozilla.javascript.ScriptOrFnNode;

/**
 * throw语句节点
 * 
 * @version 1.0.0, 2010/01/01
 * @author 欧阳先伟
 * @since Common 0.1
 */
public class ThrowStatement extends Statement {

  /** 异常表达式 */
  private IExpression exception;

  /**
   * 创建throw语句节点
   * 
   * @param node
   *          语句子节点对应的rhino节点
   * @param root
   *          语句子节点对应的rhino根节点
   * @param scope
   *          语句子节点生存域
   */
  public ThrowStatement(Node node, ScriptOrFnNode root, Scope scope) {
    super(node);
    exception = addExpression(node.getFirstChild(), root, scope);
  }

  public boolean isNeedLeftSeparator() {
    return true;
  }

  public boolean isNeedRightSeparator() {
    return true;
  }

  public void write(Writer writer, Environment env) throws IOException {
    writer.write("throw ");
    exception.write(writer, env);
  }
}
