package org.lilystudio.javascript.statement;

import java.io.IOException;
import java.io.Writer;

import org.lilystudio.javascript.Environment;
import org.lilystudio.javascript.IStatement;
import org.lilystudio.javascript.StatementList;
import org.lilystudio.javascript.scope.Scope;
import org.mozilla.javascript.Node;
import org.mozilla.javascript.ScriptOrFnNode;

/**
 * 块语句节点
 * 
 * @version 1.0.0, 2010/01/01
 * @author 欧阳先伟
 * @since Common 0.1
 */
public class BlockStatement extends Statement {

  /** 块语句内的语句列表 */
  private StatementList statements = new StatementList();

  /**
   * 创建块语句节点
   * 
   * @param node
   *          语句子节点对应的rhino节点
   * @param root
   *          语句子节点对应的rhino根节点
   * @param scope
   *          语句子节点生存域
   */
  public BlockStatement(Node node, ScriptOrFnNode root, Scope scope) {
    super(node);

    Node childNode = node.getFirstChild();
    while (childNode != null) {
      IStatement statement = addStatement(childNode, root, scope);
      statements.add(statement);
      childNode = statement.getNext();
    }
  }

  /**
   * 获取语句列表
   * 
   * @return 语句列表
   */
  public StatementList getStatementList() {
    return statements;
  }

  public boolean isNeedLeftSeparator() {
    return getParent() instanceof BlockStatement || getParent() == null
        || getParent() instanceof SwitchStatement;
  }

  public boolean isNeedRightSeparator() {
    return isNeedLeftSeparator();
  }

  public void write(Writer writer, Environment env) throws IOException {
    boolean flag = getParent() instanceof BlockStatement || getParent() == null
        || getParent() instanceof SwitchStatement;
    if (!flag) {
      writer.write("{");
    }

    statements.write(writer, env);

    if (!flag) {
      writer.write("}");
    }
  }
}
