package org.lilystudio.javascript.statement;

import java.io.IOException;
import java.io.Writer;

import org.lilystudio.javascript.Environment;
import org.lilystudio.javascript.IExpression;
import org.lilystudio.javascript.INode;
import org.lilystudio.javascript.IStatement;
import org.lilystudio.javascript.expression.IdentifierExpression;
import org.lilystudio.javascript.scope.Scope;
import org.mozilla.javascript.Node;
import org.mozilla.javascript.ScriptOrFnNode;
import org.mozilla.javascript.Token;

/**
 * for in语句节点
 * 
 * @version 1.0.0, 2010/01/01
 * @author 欧阳先伟
 * @since Common 0.1
 */
public class ForinStatement extends Statement implements IBodyStatement {

  /** 用于in循环的变量 */
  private INode id;

  /** 用于in循环的表达式 */
  private IExpression expression;

  /** 循环主体语句 */
  private IStatement bodyStatement;

  /**
   * 创建for in语句节点
   * 
   * @param node
   *          语句子节点对应的rhino节点
   * @param root
   *          语句子节点对应的rhino根节点
   * @param scope
   *          语句子节点生存域
   */
  public ForinStatement(Node node, ScriptOrFnNode root, Scope scope) {
    super(node);

    node = node.getFirstChild().getFirstChild();
    if (node.getType() == Token.VAR) {
      id = addStatement(node, root, scope);
      node = node.getNext();
    }

    expression = addExpression(node.getFirstChild(), root, scope);
    node = node.getNext().getNext().getNext().getFirstChild();
    if (id == null) {
      id = new IdentifierExpression(node.getFirstChild().getFirstChild(), root,
          scope);
    }
    id.setParent(this);
    bodyStatement = addStatement(node.getNext(), root, scope);
  }

  public boolean isNeedLeftSeparator() {
    return bodyStatement.isNeedLeftSeparator();
  }

  public boolean isNeedRightSeparator() {
    return bodyStatement.isNeedRightSeparator();
  }

  public void write(Writer writer, Environment env) throws IOException {
    writer.write("for(");
    id.write(writer, env);
    if (id.isNeedRightSeparator()) {
      writer.write(" ");
    }
    writer.write("in");
    if (expression.isNeedLeftSeparator()) {
      writer.write(" ");
    }
    expression.write(writer, env);
    writer.write(")");
    bodyStatement.write(writer, env);
  }

  public boolean isIfBody() {
    return bodyStatement instanceof IBodyStatement
        && ((IBodyStatement) bodyStatement).isIfBody();
  }
}
