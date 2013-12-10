package org.lilystudio.javascript.statement;

import java.io.IOException;
import java.io.Writer;

import org.lilystudio.javascript.Environment;
import org.lilystudio.javascript.IExpression;
import org.lilystudio.javascript.Utils;
import org.lilystudio.javascript.scope.Scope;
import org.lilystudio.javascript.scope.Label;
import org.mozilla.javascript.Node;
import org.mozilla.javascript.ScriptOrFnNode;
import org.mozilla.javascript.Token;

/**
 * 控制语句节点，控制语句包括break,continue与return
 * 
 * @version 1.0.0, 2010/01/01
 * @author 欧阳先伟
 * @since Common 0.1
 */
public class ControlStatement extends Statement {

  /** 控制语句的类型 */
  private int tokenType;

  /** 跳转到的标签 */
  private Label label;

  /** 返回值表达式 */
  private IExpression retValue;

  /**
   * 创建控制语句节点
   * 
   * @param node
   *          语句子节点对应的rhino节点
   * @param root
   *          语句子节点对应的rhino根节点
   * @param scope
   *          语句子节点生存域
   */
  public ControlStatement(Node node, ScriptOrFnNode root, Scope scope) {
    super(node);
    tokenType = node.getType();
    if (tokenType == Token.CONTINUE || tokenType == Token.BREAK) {
      node = ((Node.Jump) node).getJumpStatement();
      if ((label = scope.getLabel(node, tokenType == Token.BREAK)) != null) {
        label.inc();
      }
    } else {
      node = node.getFirstChild();
      if (node != null) {
        retValue = addExpression(node, root, scope);
      }
    }
  }

  /**
   * 获取控制语句的类型，break/continue/return/return xxx
   * 
   * @return 控制语句的类型
   */
  public int getType() {
    return retValue != null ? 0 : tokenType;
  }

  public boolean isNeedLeftSeparator() {
    return true;
  }

  public boolean isNeedRightSeparator() {
    return true;
  }

  public void write(Writer writer, Environment env) throws IOException {
    writer.write(Utils.escapeLiteral(tokenType));
    if (label != null) {
      writer.write(" ");
      writer.write(label.getString());
    } else if (retValue != null) {
      if (retValue.isNeedLeftSeparator()) {
        writer.write(" ");
      }
      retValue.write(writer, env);
    }
  }
}
