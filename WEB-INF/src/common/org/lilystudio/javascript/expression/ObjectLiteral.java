package org.lilystudio.javascript.expression;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.lilystudio.javascript.Environment;
import org.lilystudio.javascript.IExpression;
import org.lilystudio.javascript.Utils;
import org.lilystudio.javascript.scope.Scope;
import org.mozilla.javascript.Node;
import org.mozilla.javascript.ScriptOrFnNode;

/**
 * 对象常量表达式节点
 * 
 * @version 1.0.0, 2010/01/01
 * @author 欧阳先伟
 * @since Common 0.1
 */
public class ObjectLiteral extends PrimaryExpression {

  /**
   * 对象属性项
   */
  static class Item {
    String id;
    IExpression value;

    /**
     * 创建对象属性项
     * 
     * @param id
     *          对象属性名
     * @param value
     *          对象属性值
     */
    public Item(String id, IExpression value) {
      this.id = id;
      this.value = value;
    }

    @Override
    public boolean equals(Object obj) {
      return obj.getClass() == this.getClass() && id.equals(((Item) obj).id)
          && value.equals(((Item) obj).value);
    }
  }

  /** 对象属性项列表 */
  private List<Item> items = new ArrayList<Item>();

  /**
   * 创建对象常量表达式节点
   * 
   * @param lineno
   *          节点的行号
   */
  public ObjectLiteral(int lineno) {
    super(lineno);
  }

  /**
   * 创建对象常量表达式节点
   * 
   * @param node
   *          表达式子节点对应的rhino节点
   * @param root
   *          表达式子节点对应的rhino根节点
   * @param scope
   *          表达式子节点生存域
   */
  public ObjectLiteral(Node node, ScriptOrFnNode root, Scope scope) {
    super(node.getLineno());
    int i = 0;
    Object[] ids = (Object[]) node.getProp(Node.OBJECT_IDS_PROP);

    for (Node childNode = node.getFirstChild(); childNode != null; childNode = childNode
        .getNext()) {
      items.add(new Item(ids[i].toString(), addExpression(childNode, root,
          scope)));
      i++;
    }
  }

  @Override
  public boolean equals(Object obj) {
    return super.equals(obj) && items.equals(((ObjectLiteral) obj).items);
  }

  public int getLevel() {
    return super.getLevel() + 5;
  }

  public boolean isNeedLeftSeparator() {
    return false;
  }

  public boolean isNeedRightSeparator() {
    return false;
  }

  public void write(Writer writer, Environment env) throws IOException {
    writer.write("{");

    boolean flag = false;
    for (Item item : items) {
      if (flag) {
        writer.write(",");
      } else {
        flag = true;
      }

      writer.write(Utils.isValidIdentifier(item.id) ? item.id : Utils
          .escapeJSString(item.id));
      writer.write(":");
      item.value.write(writer, env);
    }

    writer.write("}");
  }
}
