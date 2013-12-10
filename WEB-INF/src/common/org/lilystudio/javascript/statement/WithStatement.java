package org.lilystudio.javascript.statement;

import java.io.IOException;
import java.io.Writer;

import org.lilystudio.javascript.Environment;
import org.lilystudio.javascript.IExpression;
import org.lilystudio.javascript.IStatement;
import org.lilystudio.javascript.scope.Scope;
import org.mozilla.javascript.Node;
import org.mozilla.javascript.ScriptOrFnNode;

/**
 * with语句节点
 * 
 * @version 1.0.0, 2010/01/01
 * @author 欧阳先伟
 * @since Common 0.1
 */
public class WithStatement extends Statement implements IBodyStatement {

  /** with的表达式 */
  private IExpression target;

  /** with的主体 */
  private IStatement bodyStatement;

  /**
   * 创建with语句节点
   * 
   * @param node
   *          语句子节点对应的rhino节点
   * @param root
   *          语句子节点对应的rhino根节点
   * @param scope
   *          语句子节点生存域
   */
  public WithStatement(Node node, ScriptOrFnNode root, Scope scope) {
    super(node);
    target = addExpression(node.getFirstChild(), root, scope);

    node = node.getNext();
    bodyStatement = addStatement(node.getFirstChild(), root, new Scope(scope));

    setNext(node.getNext().getNext());
  }

  public boolean isNeedLeftSeparator() {
    return true;
  }

  public boolean isNeedRightSeparator() {
    return bodyStatement.isNeedRightSeparator();
  }

  public void write(Writer writer, Environment env) throws IOException {
    writer.write("with(");
    target.write(writer, env);
    writer.write(")");
    bodyStatement.write(writer, env);
  }

  public boolean isIfBody() {
    return bodyStatement instanceof IBodyStatement
        && ((IBodyStatement) bodyStatement).isIfBody();
  }
}
