package org.lilystudio.javascript.statement;

import java.io.IOException;
import java.io.Writer;

import org.lilystudio.javascript.Environment;
import org.lilystudio.javascript.IExpression;
import org.lilystudio.javascript.scope.Scope;
import org.mozilla.javascript.Node;
import org.mozilla.javascript.ScriptOrFnNode;

/**
 * 函数语句节点
 * 
 * @version 1.0.0, 2010/01/01
 * @author 欧阳先伟
 * @since Common 0.1
 */
public class FunctionStatement extends Statement {

  /** 函数表达式 */
  private IExpression expression;

  /**
   * 创建函数语句节点
   * 
   * @param node
   *          语句子节点对应的rhino节点
   * @param root
   *          语句子节点对应的rhino根节点
   * @param scope
   *          语句子节点生存域
   */
  public FunctionStatement(Node node, ScriptOrFnNode root, Scope scope) {
    super(node);
    expression = addExpression(node, root, scope);
  }

  @Override
  public int getLineno() {
    return expression.getLineno();
  }
  
  public boolean isNeedLeftSeparator() {
    return expression.isNeedLeftSeparator();
  }

  public boolean isNeedRightSeparator() {
    return expression.isNeedRightSeparator();
  }

  public void write(Writer writer, Environment env) throws IOException {
    expression.write(writer, env);
  }
}
