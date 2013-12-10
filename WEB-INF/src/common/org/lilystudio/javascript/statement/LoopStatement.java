package org.lilystudio.javascript.statement;

import java.io.IOException;
import java.io.Writer;

import org.lilystudio.javascript.Environment;
import org.lilystudio.javascript.IExpression;
import org.lilystudio.javascript.INode;
import org.lilystudio.javascript.IStatement;
import org.lilystudio.javascript.expression.InbuildLiteral;
import org.lilystudio.javascript.scope.Constant;
import org.lilystudio.javascript.scope.Scope;
import org.mozilla.javascript.Node;
import org.mozilla.javascript.ScriptOrFnNode;
import org.mozilla.javascript.Token;

/**
 * 循环语句节点
 * 
 * @version 1.0.0, 2010/01/01
 * @author 欧阳先伟
 * @since Common 0.1
 */
public class LoopStatement extends Statement implements IBodyStatement {

  /** 循环的类型 */
  private int tokenType;

  /** 循环的开始节点，用于for语句 */
  private INode startNode;

  /** 循环的条件表达式 */
  private IExpression checkExpression;

  /** 循环的周期结束表达式，用于for语句 */
  private IExpression endExpression;

  /** 循环体语句 */
  private IStatement bodyStatement;

  /**
   * 创建循环语句节点
   * 
   * @param node
   *          语句子节点对应的rhino节点
   * @param root
   *          语句子节点对应的rhino根节点
   * @param scope
   *          语句子节点生存域
   */
  public LoopStatement(Node node, ScriptOrFnNode root, Scope scope) {
    super(node);
    Node childNode = node.getFirstChild();

    switch (childNode.getType()) {
    case Token.EXPR_VOID:
      tokenType = Token.FOR;
      startNode = addExpression(childNode.getFirstChild(), root, scope);
      childNode = childNode.getNext();
      break;

    case Token.VAR:
      tokenType = Token.FOR;
      startNode = addStatement(childNode, root, scope);
      childNode = childNode.getNext();
      break;
    }

    if (childNode.getType() == Token.TARGET) {
      bodyStatement = addStatement(childNode.getNext(), root, scope);
      childNode = bodyStatement.getNext().getNext();
      checkExpression = addExpression(childNode.getFirstChild(), root, scope);
      if (tokenType == 0) {
        tokenType = Token.DO;
      }
    } else if (childNode.getType() == Token.GOTO) {
      bodyStatement = addStatement(childNode.getNext().getNext(), root, scope);
      childNode = bodyStatement.getNext().getNext();

      if (childNode.getType() == Token.EXPR_VOID) {
        tokenType = Token.FOR;
        endExpression = addExpression(childNode.getFirstChild(), root, scope);
        childNode = childNode.getNext().getNext();
      }

      childNode = childNode.getNext();
      if (childNode.getType() == Token.TARGET) {
        checkExpression = addExpression(childNode.getNext().getFirstChild(),
            root, scope);
        tokenType = Token.FOR;
      } else {
        checkExpression = addExpression(childNode.getFirstChild(), root, scope);
        if (tokenType == 0) {
          tokenType = Token.WHILE;
        }
      }
    }
  }

  public boolean isNeedLeftSeparator() {
    return true;
  }

  public boolean isNeedRightSeparator() {
    switch (tokenType) {
    case Token.DO:
      return true;
    default:
      return bodyStatement.isNeedRightSeparator();
    }
  }

  public void write(Writer writer, Environment env) throws IOException {
    switch (tokenType) {
    case Token.DO:
    case Token.WHILE:
      if (checkExpression instanceof InbuildLiteral) {
        Constant literal = ((InbuildLiteral) checkExpression).getLiteral();
        if (literal.getString(false).equals("true")) {
          literal.dec(false);
          writer.write("for(;;)");
          bodyStatement.write(writer, env);
          break;
        }
      }

      if (tokenType == Token.DO) {
        writer.write("do");
        if (bodyStatement.isNeedLeftSeparator()) {
          writer.write(" ");
        }

        bodyStatement.write(writer, env);

        if (bodyStatement.isNeedRightSeparator()) {
          writer.write(";");
        }

        writer.write("while(");
        checkExpression.write(writer, env);
        writer.write(")");
      } else {
        writer.write("while(");
        checkExpression.write(writer, env);
        writer.write(")");
        bodyStatement.write(writer, env);
      }
      break;

    default:
      writer.write("for(");

      if (startNode != null) {
        startNode.write(writer, env);
      }

      writer.write(";");

      if (checkExpression instanceof InbuildLiteral) {
        Constant literal = ((InbuildLiteral) checkExpression).getLiteral();
        if (literal.getString(false).equals("true")) {
          literal.dec(false);
        }
      } else {
        checkExpression.write(writer, env);
      }

      writer.write(";");

      if (endExpression != null) {
        endExpression.write(writer, env);
      }

      writer.write(")");

      bodyStatement.write(writer, env);
    }
  }

  public boolean isIfBody() {
    return tokenType != Token.DO && bodyStatement instanceof IBodyStatement
        && ((IBodyStatement) bodyStatement).isIfBody();
  }
}
