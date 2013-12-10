package org.lilystudio.javascript.scope;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lilystudio.javascript.Environment;
import org.lilystudio.javascript.JSCompressor;
import org.lilystudio.javascript.Utils;
import org.mozilla.javascript.Node;

/**
 * 生存域
 * 
 * @version 1.0.0, 2010/01/01
 * @author 欧阳先伟
 * @since Common 0.1
 */
public class Scope {

  /** 父生存域 */
  private Scope parent;

  /** 生存域是否被锁定(eval指令) */
  private boolean lock;

  /** 标识符表 */
  private Map<String, Identifier> identifiers = new HashMap<String, Identifier>();

  /** 常量表 */
  private Map<String, Constant> constants = new HashMap<String, Constant>();

  /** 标签表 */
  private Map<Node, Label> labels = new HashMap<Node, Label>();

  /** continue标签表 */
  private Map<Node, Label> continues = new HashMap<Node, Label>();

  /** 需要转义的常量 */
  private List<Constant> varConstants = new ArrayList<Constant>();

  /**
   * 创建生存域
   * 
   * @param parent
   *          父生存域
   */
  public Scope(Scope parent) {
    this.parent = parent;
  }

  /**
   * 锁定生存域所有的变量转义
   */
  public void lockScope() {
    lock = true;
    if (parent != null) {
      parent.lockScope();
    }
  }

  /**
   * 在生存域中注册一个标识符，使标识符成为生存域的局域变量
   * 
   * @param name
   *          标识符名称
   * @return 标识符统计对象
   */
  public Identifier registerLocalIdentifier(String name) {
    Identifier identifier = identifiers.get(name);
    if (identifier == null) {
      identifier = new Identifier(name, null);
      identifiers.put(name, identifier);
    } else {
      identifier.local();
    }
    return identifier;
  }

  /**
   * 在生存域中增加标识符的使用次数1
   * 
   * @param name
   *          标识符名称
   * @return 标识符统计对象
   */
  public Identifier addIdentifier(String name) {
    Identifier parentIdentifier = parent.addIdentifier(name);
    Identifier identifier = identifiers.get(name);
    if (identifier == null) {
      identifier = new Identifier(name, parentIdentifier);
      identifiers.put(name, identifier);
    }

    identifier.inc();
    return identifier;
  }

  /**
   * 锁定标识符的转义
   * 
   * @param name
   *          标识符名称
   * @return 标识符统计对象
   */
  public Identifier lockIdentifier(String name) {
    Identifier parentIdentifier = null;
    if (parent != null) {
      parentIdentifier = parent.lockIdentifier(name);
    }
    Identifier identifier = identifiers.get(name);
    if (identifier == null) {
      identifier = new Identifier(name, parentIdentifier);
      identifiers.put(name, identifier);
    }
    identifier.lock();
    return identifier;
  }

  /**
   * 增加常量的使用次数1
   * 
   * @param name
   *          常量名称
   * @param isMember
   *          常量是否为成员
   * @return 常量统计对象
   */
  public Constant addConstant(String name, boolean isMember) {
    Constant parentConstant = null;
    if (!(parent == null || parent instanceof GlobalScope || name
        .equals("this"))) {
      parentConstant = parent.addConstant(name, isMember);
    }

    if (isMember) {
      name = "\"" + name + "\"";
    }

    Constant constant = constants.get(name);
    if (constant == null) {
      constant = new Constant(name, parentConstant);
      constants.put(name, constant);
    }

    constant.inc(isMember);
    return constant;
  }

  /**
   * 增加标签的使用次数1
   * 
   * @param target
   *          标签的rhino节点
   * @return 标签统计对象
   */
  public Label addLabel(Node target) {
    Label label = labels.get(target);
    if (label == null) {
      label = new Label();
      labels.put(target, label);
      continues.put(target.getNext(), label);
    }
    label.inc();
    return label;
  }

  /**
   * 获取标签统计对象
   * 
   * @param target
   *          标签对应的rhino对象
   * @param isBreak
   *          是否为break指令
   * @return 标签统计对象
   */
  public Label getLabel(Node target, boolean isBreak) {
    if (isBreak) {
      return labels.get(target);
    } else {
      return continues.get(target);
    }
  }

  /**
   * 获取转义后的常量统计列表
   * 
   * @return 常量统计列表
   */
  public List<Constant> getVarConstants() {
    return varConstants;
  }

  /**
   * 压缩生存域内的标签/标识符/常量名称
   * 
   * @param isVar
   *          第一条语句是否为var语句
   * @param env
   *          压缩环境
   */
  public void compress(boolean isVar, Environment env) {
    compressLabel();
    if (parent != null && env.getMode() != JSCompressor.LABEL) {
      compressIdentifier(isVar ? 0 : 4, env, null);
    }
  }

  /** 变量名允许使用的字符集 */
  private static char[] chars = new char[64];
  static {
    int i = 0;
    for (; i < 26; i++) {
      chars[i] = (char) ('a' + i);
    }

    for (; i < 52; i++) {
      chars[i] = (char) ('A' + i - 26);
    }

    chars[52] = '$';

    chars[53] = '_';

    for (i = 54; i < 64; i++) {
      chars[i] = (char) ('0' + i - 54);
    }
  }

  /**
   * 压缩标签名称
   */
  private void compressLabel() {
    List<Label> list = new ArrayList<Label>();
    for (Label label : labels.values()) {
      list.add(label);
    }

    Collections.sort(list, new Comparator<Label>() {
      public int compare(Label o1, Label o2) {
        return o2.getUsedCount() - o1.getUsedCount();
      }
    });

    int i = 0;
    StringBuilder sb = new StringBuilder();
    for (Label label : list) {
      sb.setLength(0);
      int index = i;
      sb.append(chars[index % 54]);
      index = index / 54;
      while (index > 0) {
        sb.append(chars[index % 64]);
        index = index / 64;
      }
      label.setString(sb.toString());
      i++;
    }
  }

  /**
   * 压缩标识符与常量名称
   * 
   * @param varLength
   *          var指令的长度，如果函数的第一条指令不是var，则这个值是4，表示常量声明需要额外添加
   *          "var "，否则为0
   * @param env
   *          压缩环境
   * @param global
   *          父生存域全局变量名称列表
   */
  private void compressIdentifier(int varLength, Environment env,
      List<String> global) {
    List<ICount> list = new ArrayList<ICount>();
    global = global != null ? global : new ArrayList<String>();
    for (String name : identifiers.keySet()) {
      Identifier identifier = identifiers.get(name);
      identifier.setString(name);
      if (!lock && parent != null && identifier.isLocal()
          && !identifier.isLocked()) {
        list.add(identifier);
      } else {
        global.add(identifier.getString());
      }
    }

    if (env.getMode() == JSCompressor.SEMANTICS) {
      for (Constant constant : constants.values()) {
        if (constant.isUsed()) {
          global.add(constant.getString(false));
        } else {
          list.add(constant);
        }
      }
    }

    Collections.sort(list, new Comparator<ICount>() {
      public int compare(ICount o1, ICount o2) {
        return o2.getUsedCount() - o1.getUsedCount();
      }
    });

    int i = 0;
    StringBuilder sb = new StringBuilder();
    list: for (ICount count : list) {
      if (env.getMode() == JSCompressor.FOR_GZIP && count instanceof Identifier
          && ((Identifier) count).getName().startsWith("__gzip_direct__")) {
        continue;
      }
      while (true) {
        sb.setLength(0);
        int index = i;
        sb.append(chars[index % 54]);
        index = index / 54;
        while (index > 0) {
          sb.append(chars[index % 64]);
          index = index / 64;
        }
        String s = sb.toString();
        if (Utils.isValidIdentifier(s) && !global.contains(s)) {
          if (count instanceof Constant) {
            int tmp = ((Constant) count).calculate(s.length());
            if (tmp <= 0) {
              continue list;
            } else {
              varLength -= tmp;
              varConstants.add((Constant) count);
            }
          }
          break;
        }
        i++;
      }

      count.setString(sb.toString());
      i++;
    }

    if (varLength >= 0) {
      Map<String, Constant> backup = constants;
      for (Constant constant : varConstants) {
        constant.setString(null);
      }
      varConstants.clear();
      constants = new HashMap<String, Constant>();
      compressIdentifier(-1, env, global);
      constants = backup;
    }
  }
}
