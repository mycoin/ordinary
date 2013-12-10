package org.lilystudio.javascript.expression;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.lilystudio.javascript.Environment;
import org.lilystudio.javascript.IStatement;
import org.lilystudio.javascript.StatementList;
import org.lilystudio.javascript.Utils;
import org.lilystudio.javascript.scope.Constant;
import org.lilystudio.javascript.scope.Identifier;
import org.lilystudio.javascript.scope.Scope;
import org.lilystudio.javascript.statement.ControlStatement;
import org.lilystudio.javascript.statement.VariableStatement;
import org.mozilla.javascript.FunctionNode;
import org.mozilla.javascript.Node;
import org.mozilla.javascript.NodeTransformer;
import org.mozilla.javascript.ScriptOrFnNode;
import org.mozilla.javascript.Token;

/**
 * 函数表达式节点
 * 
 * @version 1.0.0, 2010/01/01
 * @author 欧阳先伟
 * @since Common 0.1
 */
public class FunctionLiteral extends PrimaryExpression {

  /** 函数生存域 */
  private Scope fnScope;

  /** 函数名 */
  private Identifier name;

  /** 函数参数 */
  private List<Identifier> params = new ArrayList<Identifier>();

  /** 函数主体语句列表 */
  private StatementList statements = new StatementList();

  /**
   * 创建函数表达式节点
   * 
   * @param node
   *          表达式子节点对应的rhino节点
   * @param root
   *          表达式子节点对应的rhino根节点
   * @param scope
   *          表达式子节点生存域
   */
  public FunctionLiteral(Node node, ScriptOrFnNode root, Scope scope) {
    super(root.getFunctionNode(node.getIntProp(Node.FUNCTION_PROP, -1))
        .getLineno());
    FunctionNode fnNode = root.getFunctionNode(node.getIntProp(
        Node.FUNCTION_PROP, -1));
    fnScope = new Scope(scope);

    if (fnNode.getFunctionName().length() > 0) {
      name = scope.addIdentifier(fnNode.getFunctionName());
    }

    node = fnNode.getFirstChild().getFirstChild();
    if (node.getType() == Token.EXPR_VOID) {
      Node firstChild = node.getFirstChild();
      if (firstChild.getType() == Token.SETNAME
          && firstChild.getLastChild().getType() == Token.THISFN) {
        node = node.getNext();
      }
    }
    while (node != null) {
      IStatement statement = Utils.createStatement(node, fnNode, fnScope);
      statements.add(statement);
      node = statement.getNext();
    }

    int size = statements.size() - 1;
    if (statements.size() >= 0) {
      IStatement statement = statements.get(size);
      if (statement instanceof ControlStatement
          && ((ControlStatement) statement).getType() == Token.RETURN) {
        statements.remove(size);
      }
    }

    new NodeTransformer().transform(fnNode);
    String[] symbols = fnNode.getParamAndVarNames();
    for (int i = 0; i < symbols.length; i++) {
      String symbol = symbols[i];
      fnScope.registerLocalIdentifier(symbol);
      if (i < fnNode.getParamCount()) {
        Identifier identifier = fnScope.addIdentifier(symbol);
        identifier.inc((fnNode.getParamCount() - i) * 10000000);
        params.add(identifier);
      }
    }
  }

  public int getLevel() {
    return super.getLevel() + 5;
  }

  public boolean isNeedLeftSeparator() {
    return true;
  }

  public boolean isNeedRightSeparator() {
    return name == null;
  }

  public void write(Writer writer, Environment env) throws IOException {
    boolean isVar = false;
    if (statements.size() > 0) {
      isVar = statements.get(0) instanceof VariableStatement;
      fnScope.compress(isVar, env);
    }
    List<Constant> constants = fnScope.getVarConstants();
    int size = constants.size();
    if (size > 0) {
      VariableStatement statement;
      if (isVar) {
        statement = (VariableStatement) statements.get(0);
      } else {
        statement = new VariableStatement(getLineno());
        statement.setParent(this);
        statements.add(0, statement);
      }
      for (size--; size >= 0; size--) {
        Constant constant = constants.get(size);
        statement.addParam(statement.getLineno(), constant.getString(false),
            constant.getLiteral());
      }
    }

    writer.write("function");
    if (name != null) {
      writer.write(" ");
      writer.write(name.getString());
    }
    writer.write("(");
    boolean flag = false;
    for (Identifier param : params) {
      if (flag) {
        writer.write(",");
      } else {
        flag = true;
      }
      writer.write(param.getString());
    }
    writer.write("){");
    statements.write(writer, env);
    writer.write("}");
  }
}
