package org.lilystudio.javascript.statement;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import org.lilystudio.javascript.Environment;
import org.lilystudio.javascript.ExpressionList;
import org.lilystudio.javascript.IExpression;
import org.lilystudio.javascript.JSCompressor;
import org.lilystudio.javascript.expression.BinaryExpression;
import org.lilystudio.javascript.expression.IdentifierExpression;
import org.lilystudio.javascript.expression.NumericLiteral;
import org.lilystudio.javascript.scope.Identifier;
import org.lilystudio.javascript.scope.Scope;
import org.mozilla.javascript.Node;
import org.mozilla.javascript.ScriptOrFnNode;
import org.mozilla.javascript.Token;

/**
 * 变量声明语句节点
 * 
 * @version 1.0.0, 2010/01/01
 * @author 欧阳先伟
 * @since Common 0.1
 */
public class VariableStatement extends Statement {

  /** 变量表达式列表 */
  private ExpressionList params = new ExpressionList();

  /**
   * 创建变量声明语句节点
   * 
   * @param lineno
   *          语句代码行号
   * @param scope
   *          语句子节点生存域
   */
  public VariableStatement(int lineno) {
    super(lineno);
  }

  /**
   * 创建变量声明语句节点
   * 
   * @param node
   *          语句子节点对应的rhino节点
   * @param root
   *          语句子节点对应的rhino根节点
   * @param scope
   *          语句子节点生存域
   */
  public VariableStatement(Node node, ScriptOrFnNode root, Scope scope) {
    super(node);
    boolean isFirst = true;
    while (true) {
      int i = 0;
      for (Node childNode = node.getFirstChild(); childNode != null; childNode = childNode
          .getNext()) {
        Node valueNode = childNode.getFirstChild();
        IdentifierExpression id = (IdentifierExpression) addExpression(
            childNode, root, scope);
        if (valueNode != null) {
          IExpression value = addExpression(valueNode, root, scope);
          BinaryExpression assignment = new BinaryExpression(node.getLineno(),
              Token.ASSIGN, id, value);
          assignment.setParent(this);
          if (value instanceof NumericLiteral
              && ((NumericLiteral) value).getValue() == 0
              && id.getName().getString().length() == 1) {
            params.add(i++, assignment);
            id.getName().inc(10000000 - i * 10000);
          } else {
            params.add(assignment);
          }
        } else {
          params.add(id);
        }
        if (isFirst) {
          id.getName().inc(1000);
          isFirst = false;
        }
      }

      node = node.getNext();
      setNext(node);
      if (node == null || node.getType() != Token.VAR) {
        return;
      }
    }
  }

  /**
   * 添加一个声明的变量到变量声明语句中
   * 
   * @param lineno
   *          节点的行号
   * @param name
   *          变量名
   * @param value
   *          变量值
   */
  public void addParam(int lineno, String name, String value) {
    BinaryExpression assignment = new BinaryExpression(lineno, Token.ASSIGN,
        new IdentifierExpression(lineno, name), new IdentifierExpression(
            lineno, value));
    assignment.setParent(this);
    params.add(0, assignment);
  }

  public boolean isNeedLeftSeparator() {
    return true;
  }

  public boolean isNeedRightSeparator() {
    return true;
  }

  public void write(Writer writer, Environment env) throws IOException {
    if (env.getMode() == JSCompressor.FOR_GZIP) {
      for (int i = params.size() - 1; i >= 0; i--) {
        IExpression param = params.get(i);
        if (param instanceof BinaryExpression) {
          BinaryExpression binary = (BinaryExpression) param;
          Identifier id = ((IdentifierExpression) binary.getLeftExpression())
              .getName();
          if (id.getName().startsWith("__gzip_direct__")) {
            Writer s = new StringWriter();
            binary.getRightExpression().write(s, env);
            id.setString(s.toString());
            params.remove(i);
          }
        }
      }
    }
    if (params.size() > 0) {
      writer.write("var ");
      params.write(writer, env);
    }
  }
}
