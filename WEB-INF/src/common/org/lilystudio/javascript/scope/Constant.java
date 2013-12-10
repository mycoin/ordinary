package org.lilystudio.javascript.scope;

import java.util.ArrayList;
import java.util.List;

import org.lilystudio.javascript.Utils;

/**
 * 常量统计
 * 
 * @version 1.0.0, 2010/01/01
 * @author 欧阳先伟
 * @since Common 0.1
 */
public class Constant implements ICount {

  /** 常量父对象 */
  private Constant parent;

  /** 子常量对象列表 */
  private List<Constant> children = new ArrayList<Constant>();

  /** 原始的常量名称 */
  private String literal;

  /** 压缩后的名称 */
  private String name;

  /** 使用的次数 */
  private int usedCount;

  /** 作为成员名称使用的次数 */
  private int memberCount;

  /**
   * 创建常量统计对象
   * 
   * @param literal
   *          常量字符串
   * @param parent
   *          常量父对象
   */
  public Constant(String literal, Constant parent) {
    this.literal = literal;
    this.parent = parent;
    if (parent != null) {
      parent.children.add(this);
    }
  }

  /**
   * 获取常量的内容
   * 
   * @return 初始化时的常量
   */
  public String getLiteral() {
    return literal;
  }

  /**
   * 判断常量是否转义过
   * 
   * @return 常量是否转义过
   */
  public boolean isUsed() {
    if (name != null) {
      return true;
    }
    return parent != null ? parent.isUsed() : false;
  }

  /**
   * 获取最终的字符串
   * 
   * @param isMember
   *          是否是成员常量
   * @return 最终的字符串
   */
  public String getString(boolean isMember) {
    if (this.name != null) {
      return isMember ? "[" + this.name + "]" : name;
    }
    if (parent != null) {
      return parent.getString(isMember);
    }
    if (isMember) {
      String name = this.literal.substring(1, this.literal.length() - 1);
      return Utils.isValidIdentifier(name) ? "." + name : "['" + name + "']";
    }
    return this.literal;
  }

  /**
   * 计算转义后节省的字节数
   * 
   * @param varLength
   *          转义后变量的长度
   * @return 转义后节省的字节数
   */
  public int calculate(int varLength) {
    // VAR=CONST;
    int result = -(varLength + 1 + literal.length() + 1);
    // VAR(CONST)
    result += (literal.length() - varLength) * usedCount;

    if (memberCount > 0) {
      String name = this.literal.substring(1, this.literal.length() - 1);
      if (Utils.isValidIdentifier(name)) {
        // .CONST->[VAR]
        result += (1 + name.length() - 2 - varLength) * memberCount;
      } else {
        // [CONST]->[VAR]
        result += (name.length() - varLength) * memberCount;
      }
    }

    return result;
  }

  /**
   * 新增常量的使用次数1
   * 
   * @param isMember
   *          常量是否用于成员
   */
  public void inc(boolean isMember) {
    if (isMember) {
      memberCount++;
    } else {
      usedCount++;
    }
  }

  /**
   * 减少常量的使用次数1
   * 
   * @param isMember
   *          常量是否用于成员
   */
  public void dec(boolean isMember) {
    if (isMember) {
      memberCount--;
    } else {
      usedCount--;
    }
  }

  public int getUsedCount() {
    return memberCount + usedCount;
  }

  public void setString(String name) {
    this.name = name;
  }
}
