package org.lilystudio.javascript.statement;

import java.io.IOException;
import java.io.Writer;

import org.lilystudio.javascript.Environment;
import org.lilystudio.javascript.IExpression;
import org.lilystudio.javascript.IStatement;
import org.lilystudio.javascript.scope.Scope;
import org.mozilla.javascript.Node;
import org.mozilla.javascript.ScriptOrFnNode;
import org.mozilla.javascript.Token;

/**
 * try语句节点
 * 
 * @version 1.0.0, 2010/01/01
 * @author 欧阳先伟
 * @since Common 0.1
 */
public class TryStatement extends Statement {

  /** catch语句的变量生存域 */
//  private Scope catchScope;

  /** try语句的主体 */
  private IStatement bodyStatement;

  /** catch的异常表达式 */
  private IExpression exception;

  /** catch的语句 */
  private IStatement catchStatement;

  /** finally的语句 */
  private IStatement finallyStatement;

  /**
   * 创建try语句节点
   * 
   * @param node
   *          语句子节点对应的rhino节点
   * @param root
   *          语句子节点对应的rhino根节点
   * @param scope
   *          语句子节点生存域
   */
  public TryStatement(Node node, ScriptOrFnNode root, Scope scope) {
    super(node);
    boolean catchScope = false;
    
    node = node.getFirstChild().getFirstChild();
    bodyStatement = addStatement(node, root, scope);

    node = node.getNext().getNext().getNext();
    Node childNode = node.getFirstChild();
    if (childNode != null) {
      catchScope = true;
//      catchScope = new Scope(scope);

      Node tmpNode = childNode.getFirstChild();
      scope.registerLocalIdentifier(tmpNode.getString());
//      catchScope.registerLocalIdentifier(tmpNode.getString());

      exception = addExpression(tmpNode, root, scope);
//      exception = addExpression(tmpNode, root, catchScope);

      childNode = childNode.getNext().getFirstChild().getNext().getFirstChild();

      for (tmpNode = childNode.getFirstChild(); tmpNode != null; tmpNode = tmpNode
          .getNext()) {
        if (tmpNode.getType() == Token.LEAVEWITH) {
          childNode.removeChild(tmpNode.getNext());
          childNode.removeChild(tmpNode);
        }
      }

      catchStatement = addStatement(childNode, root, scope);
//      catchStatement = addStatement(childNode, root, catchScope);
    }

    childNode = node.getNext();
    if (catchScope == false) {
      finallyStatement = addStatement(childNode.getFirstChild(), root, scope);
    } else if (childNode.getNext() != null) {
      finallyStatement = addStatement(childNode.getNext().getNext().getNext()
          .getNext().getFirstChild(), root, scope);
    }
  }

  public boolean isNeedLeftSeparator() {
    return true;
  }

  public boolean isNeedRightSeparator() {
    return false;
  }

  public void write(Writer writer, Environment env) throws IOException {

    writer.write("try");
    if (bodyStatement instanceof BlockStatement) {
      bodyStatement.write(writer, env);
    } else {
      writer.write("{");
      bodyStatement.write(writer, env);
      writer.write("}");
    }

    if (catchStatement != null) {
//      catchScope.compress(false, env);
      writer.write("catch(");
      exception.write(writer, env);
      writer.write(")");
      if (catchStatement instanceof BlockStatement) {
        catchStatement.write(writer, env);
      } else {
        writer.write("{");
        catchStatement.write(writer, env);
        writer.write("}");
      }
    }

    if (finallyStatement != null) {
      writer.write("finally");
      if (finallyStatement instanceof BlockStatement) {
        finallyStatement.write(writer, env);
      } else {
        writer.write("{");
        finallyStatement.write(writer, env);
        writer.write("}");
      }
    }
  }
}
