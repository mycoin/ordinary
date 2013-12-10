package org.lilystudio.javascript.statement;

import java.io.IOException;
import java.io.Writer;

import org.lilystudio.javascript.Environment;
import org.lilystudio.javascript.IExpression;
import org.lilystudio.javascript.IStatement;
import org.lilystudio.javascript.expression.UnaryExpression;
import org.lilystudio.javascript.scope.Scope;
import org.mozilla.javascript.Node;
import org.mozilla.javascript.ScriptOrFnNode;
import org.mozilla.javascript.Token;

/**
 * if语句节点
 * 
 * @version 1.0.0, 2010/01/01
 * @author 欧阳先伟
 * @since Common 0.1
 */
public class IfStatement extends Statement implements IBodyStatement {

  /** 条件表达式 */
  private IExpression checkExpression;

  /** 条件为真时的执行语句 */
  private IStatement trueStatement;

  /** 条件为假时的执行语句 */
  private IStatement falseStatement;

  /**
   * 创建if语句节点
   * 
   * @param node
   *          语句子节点对应的rhino节点
   * @param root
   *          语句子节点对应的rhino根节点
   * @param scope
   *          语句子节点生存域
   */
  public IfStatement(Node node, ScriptOrFnNode root, Scope scope) {
    super(node);
    checkExpression = addExpression(node.getFirstChild(), root, scope);

    Node nextNode = node.getNext();
    if (nextNode.getType() != Token.GOTO && nextNode.getType() != Token.TARGET) {
      trueStatement = addStatement(nextNode, root, scope);
      if (trueStatement instanceof EmptyStatement) {
        trueStatement = null;
      }
      nextNode = nextNode.getNext();
    }

    if (nextNode.getType() == Token.GOTO) {
      nextNode = nextNode.getNext().getNext();
      falseStatement = addStatement(nextNode, root, scope);
      if (falseStatement instanceof EmptyStatement) {
        falseStatement = null;
      }
      nextNode = nextNode.getNext();
    }

    setNext(nextNode.getNext());

    if (trueStatement == null && falseStatement != null) {
      checkExpression = new UnaryExpression(node.getLineno(), Token.NOT,
          checkExpression);
      trueStatement = falseStatement;
      falseStatement = null;
    }
  }

  public boolean isNeedLeftSeparator() {
    return true;
  }

  public boolean isNeedRightSeparator() {
    if (trueStatement == null) {
      return checkExpression.isNeedRightSeparator();
    } else if (falseStatement == null) {
      return trueStatement.isNeedRightSeparator();
    } else {
      return falseStatement.isNeedRightSeparator();
    }
  }

  public void write(Writer writer, Environment env) throws IOException {
    if (trueStatement == null) {
      checkExpression.write(writer, env);
      return;
    }

    writer.write("if(");
    checkExpression.write(writer, env);
    writer.write(")");

    if (trueStatement != null) {
      if (trueStatement instanceof IBodyStatement
          && ((IBodyStatement) trueStatement).isIfBody()) {
        writer.write("{");
        trueStatement.write(writer, env);
        writer.write("}");
      } else {
        trueStatement.write(writer, env);
      }
    }

    if (falseStatement != null) {
      if (trueStatement.isNeedRightSeparator()
          && !(trueStatement instanceof IBodyStatement && ((IBodyStatement) trueStatement)
              .isIfBody())) {
        writer.write(";");
      }
      writer.write("else");
      if (falseStatement.isNeedLeftSeparator()) {
        writer.write(" ");
      }
      falseStatement.write(writer, env);
    }
  }

  public boolean isIfBody() {
    return falseStatement == null || falseStatement instanceof IBodyStatement
        && ((IBodyStatement) falseStatement).isIfBody();
  }
}
