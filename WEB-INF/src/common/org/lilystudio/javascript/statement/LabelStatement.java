package org.lilystudio.javascript.statement;

import java.io.IOException;
import java.io.Writer;

import org.lilystudio.javascript.Environment;
import org.lilystudio.javascript.IStatement;
import org.lilystudio.javascript.scope.Scope;
import org.lilystudio.javascript.scope.Label;
import org.mozilla.javascript.Node;
import org.mozilla.javascript.ScriptOrFnNode;

/**
 * 标签语句节点
 * 
 * @version 1.0.0, 2010/01/01
 * @author 欧阳先伟
 * @since Common 0.1
 */
public class LabelStatement extends Statement {

  /** 标签对象 */
  private Label label;

  /** 标签对应的语句 */
  private IStatement bodyStatement;

  /**
   * 创建标签语句节点
   * 
   * @param node
   *          语句子节点对应的rhino节点
   * @param root
   *          语句子节点对应的rhino根节点
   * @param scope
   *          语句子节点生存域
   */
  public LabelStatement(Node node, ScriptOrFnNode root, Scope scope) {
    super(node);
    label = scope.addLabel(node);
    node = node.getNext();
    bodyStatement = addStatement(node, root, scope);
    setNext(node.getNext().getNext());
  }

  public boolean isNeedLeftSeparator() {
    return true;
  }

  public boolean isNeedRightSeparator() {
    return bodyStatement.isNeedRightSeparator();
  }

  public void write(Writer writer, Environment env) throws IOException {
    String name = this.label.getString();
    if (name != null) {
      writer.write(name);
      writer.write(":");
    }
    bodyStatement.write(writer, env);
  }
}
