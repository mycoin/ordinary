package org.lilystudio.javascript.statement;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.lilystudio.javascript.Environment;
import org.lilystudio.javascript.IExpression;
import org.lilystudio.javascript.IStatement;
import org.lilystudio.javascript.StatementList;
import org.lilystudio.javascript.scope.Scope;
import org.mozilla.javascript.Node;
import org.mozilla.javascript.ScriptOrFnNode;
import org.mozilla.javascript.Token;

/**
 * switch语句节点
 * 
 * @version 1.0.0, 2010/01/01
 * @author 欧阳先伟
 * @since Common 0.1
 */
public class SwitchStatement extends Statement {

  /**
   * case项
   */
  static class CaseItem {
    
    /** case的值 */
    IExpression value;

    /** case的语句 */
    IStatement statement;
  }

  /** switch的目标对象 */
  private IExpression target;

  /** case项列表 */
  private List<CaseItem> items = new ArrayList<CaseItem>();

  /**
   * 创建switch语句节点
   * 
   * @param node
   *          语句子节点对应的rhino节点
   * @param root
   *          语句子节点对应的rhino根节点
   * @param scope
   *          语句子节点生存域
   */
  public SwitchStatement(Node node, ScriptOrFnNode root, Scope scope) {
    super(node);
    Node child = node.getFirstChild();

    target = addExpression(child, root, scope);

    while ((child = child.getNext()) != null) {
      CaseItem item = new CaseItem();
      item.value = addExpression(child.getFirstChild(), root, scope);
      items.add(item);
    }

    node = node.getNext();

    for (CaseItem item : items) {
      node = node.getNext().getNext();
      item.statement = addStatement(node, root, scope);
    }

    if (items.size() > 0) {
      CaseItem item = items.get(items.size() - 1);
      if (item.statement instanceof BlockStatement) {
        StatementList statements = ((BlockStatement) item.statement)
            .getStatementList();
        IStatement statement = statements.get(statements.size() - 1);
        if (statement instanceof ControlStatement
            && ((ControlStatement) statement).getType() == Token.BREAK) {
          statements.remove(statements.size() - 1);
        }
      }
    }

    Node nextNode = node.getNext().getNext();
    if (nextNode != null) {
      node = nextNode.getNext();
      if (node.getType() == Token.TARGET && nextNode.getType() == Token.BLOCK) {
        CaseItem item = new CaseItem();
        item.statement = addStatement(nextNode, root, scope);
        items.add(item);
        nextNode = node.getNext();
      }
    }

    setNext(nextNode);
  }

  public boolean isNeedLeftSeparator() {
    return true;
  }

  public boolean isNeedRightSeparator() {
    return false;
  }

  public void write(Writer writer, Environment env) throws IOException {
    writer.write("switch(");
    target.write(writer, env);
    writer.write("){");
    
    boolean flag = false;
    for (CaseItem item : items) {
      if (flag) {
        writer.write(";");
      } else {
        flag = true;
      }
      if (item.value != null) {
        writer.write("case");
        if (item.value.isNeedLeftSeparator()) {
          writer.write(" ");
        }
        item.value.write(writer, env);
      } else {
        writer.write("default");
      }
      writer.write(":");
      if (!item.statement.isNeedRightSeparator()
          || item.statement instanceof EmptyStatement) {
        flag = false;
      } else {
        item.statement.write(writer, env);
      }
    }
    
    writer.write("}");
  }
}
