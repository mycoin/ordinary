package org.lilystudio.javascript.expression;

import java.io.IOException;
import java.io.Writer;

import org.lilystudio.javascript.AbstractNode;
import org.lilystudio.javascript.Environment;
import org.lilystudio.javascript.IExpression;
import org.lilystudio.javascript.Utils;
import org.lilystudio.javascript.scope.Scope;
import org.mozilla.javascript.Node;
import org.mozilla.javascript.ScriptOrFnNode;
import org.mozilla.javascript.Token;

/**
 * 二元表达式节点
 * 
 * @version 1.0.0, 2010/01/01
 * @author 欧阳先伟
 * @since Common 0.1
 */
public class BinaryExpression extends AbstractNode implements IExpression {

  /** 二元表达式类型 */
  private int tokenType;

  /** 二元表达式的左部表达式 */
  private IExpression leftExpression;

  /** 二元表达式的右部表达式 */
  private IExpression rightExpression;

  /**
   * 创建二元表达式节点
   * 
   * @param lineno
   *          节点的行号
   * @param tokenType
   *          二元表达式的类型
   * @param leftExpression
   *          二元表达式的左部表达式
   * @param rightExpression
   *          二元表达式的右部表达式
   */
  public BinaryExpression(int lineno, int tokenType,
      IExpression leftExpression, IExpression rightExpression) {
    super(lineno);
    this.tokenType = tokenType;
    this.leftExpression = leftExpression;
    this.rightExpression = rightExpression;
  }

  /**
   * 创建二元表达式节点
   * 
   * @param node
   *          表达式子节点对应的rhino节点
   * @param root
   *          表达式子节点对应的rhino根节点
   * @param scope
   *          表达式子节点生存域
   */
  public BinaryExpression(Node node, ScriptOrFnNode root, Scope scope) {
    super(node.getLineno());
    Node firstChild = node.getFirstChild();

    tokenType = node.getType();

    if (firstChild.getType() != Token.USE_STACK) {
      if (tokenType == Token.SETPROP || tokenType == Token.SETELEM
          || tokenType == Token.SETPROP_OP || tokenType == Token.SETELEM_OP) {
        leftExpression = new MemberExpression(node, root, scope);
        leftExpression.setParent(this);
      } else {
        leftExpression = addExpression(firstChild, root, scope);
      }
    }

    rightExpression = addExpression(node.getLastChild(), root, scope);
    if (tokenType == Token.SETNAME || tokenType == Token.SETPROP
        || tokenType == Token.SETELEM) {
      tokenType = Token.ASSIGN;

      if (rightExpression instanceof BinaryExpression) {
        BinaryExpression binaryExpression = (BinaryExpression) rightExpression;

        boolean flag = leftExpression.equals(binaryExpression.leftExpression);
        if (flag
            || (leftExpression.equals(binaryExpression.rightExpression) && (binaryExpression.tokenType == Token.MUL
                || binaryExpression.tokenType == Token.BITAND
                || binaryExpression.tokenType == Token.BITXOR || binaryExpression.tokenType == Token.BITOR))) {

          switch (binaryExpression.tokenType) {
          case Token.MUL:
            tokenType = Token.ASSIGN_MUL;
            break;

          case Token.DIV:
            tokenType = Token.ASSIGN_DIV;
            break;

          case Token.MOD:
            tokenType = Token.ASSIGN_MOD;
            break;

          case Token.ADD:
            tokenType = Token.ASSIGN_ADD;
            break;

          case Token.SUB:
            tokenType = Token.ASSIGN_SUB;
            break;

          case Token.LSH:
            tokenType = Token.ASSIGN_LSH;
            break;

          case Token.RSH:
            tokenType = Token.ASSIGN_RSH;
            break;

          case Token.URSH:
            tokenType = Token.ASSIGN_URSH;
            break;

          case Token.BITAND:
            tokenType = Token.ASSIGN_BITAND;
            break;

          case Token.BITXOR:
            tokenType = Token.ASSIGN_BITXOR;
            break;

          case Token.BITOR:
            tokenType = Token.ASSIGN_BITOR;
            break;

          default:
            return;
          }
          rightExpression = flag ? binaryExpression.rightExpression
              : binaryExpression.leftExpression;
          rightExpression.setParent(this);
        }
      }
    }
  }

  @Override
  public boolean equals(Object obj) {
    return obj.getClass() == this.getClass()
        && tokenType == ((BinaryExpression) obj).tokenType
        && leftExpression.equals(((BinaryExpression) obj).leftExpression)
        && rightExpression.equals(((BinaryExpression) obj).rightExpression);
  }

  public int getType() {
    return tokenType;
  }

  public IExpression getLeftExpression() {
    return leftExpression;
  }

  public IExpression getRightExpression() {
    return rightExpression;
  }

  public int getLevel() {
    switch (tokenType) {
    case Token.MUL:
    case Token.DIV:
    case Token.MOD:
      return 50;
    case Token.ADD:
    case Token.SUB:
      return 60;
    case Token.LSH:
    case Token.RSH:
    case Token.URSH:
      return 70;
    case Token.LT:
    case Token.LE:
    case Token.GT:
    case Token.GE:
    case Token.INSTANCEOF:
    case Token.IN:
      return 80;
    case Token.EQ:
    case Token.NE:
    case Token.SHEQ:
    case Token.SHNE:
      return 90;
    case Token.BITAND:
      return 101;
    case Token.BITXOR:
      return 102;
    case Token.BITOR:
      return 103;
    case Token.AND:
      return 111;
    case Token.OR:
      return 112;
    case Token.SETNAME:
    case Token.SETPROP:
    case Token.SETELEM:
      return 130;
    default:
      return 140;
    }
  }

  public boolean isNeedLeftSeparator() {
    if (leftExpression.getLevel() <= this.getLevel()) {
      return leftExpression.isNeedLeftSeparator();
    }
    return false;
  }

  public boolean isNeedRightSeparator() {
    if (rightExpression.getLevel() < this.getLevel()) {
      return rightExpression.isNeedRightSeparator();
    }
    return false;
  }

  public void write(Writer writer, Environment env) throws IOException {
    boolean flag = tokenType == Token.INSTANCEOF || tokenType == Token.IN;

    if (leftExpression == null) {
      writer.write(Utils.escapeLiteral(tokenType));
      writer.write("=");
    } else {
      if (leftExpression.getLevel() <= this.getLevel()) {
        leftExpression.write(writer, env);
        if (flag && leftExpression.isNeedRightSeparator()) {
          writer.write(" ");
        }
      } else {
        writer.write("(");
        leftExpression.write(writer, env);
        writer.write(")");
      }

      if (leftExpression instanceof PostfixExpression) {
        if (((PostfixExpression) leftExpression).getType() == Token.INC) {
          if (tokenType == Token.ADD) {
            writer.write(" ");
          }
        } else {
          if (tokenType == Token.SUB) {
            writer.write(" ");
          }
        }
      }

      String s = Utils.escapeLiteral(tokenType);
      writer.write(s != null ? s : "");
    }

    if (rightExpression.getLevel() < this.getLevel()
        || (rightExpression.getLevel() == this.getLevel() && (tokenType == Token.ASSIGN || ((tokenType == Token.AND
            || tokenType == Token.OR
            || tokenType == Token.BITAND
            || tokenType == Token.BITOR
            || tokenType == Token.BITXOR
            || tokenType == Token.ADD || tokenType == Token.MUL) && rightExpression instanceof BinaryExpression)))) {

      if (rightExpression instanceof UnaryExpression) {
        int type = ((UnaryExpression) rightExpression).getType();
        if (type == Token.INC || type == Token.POS) {
          if (tokenType == Token.ADD) {
            writer.write(" ");
          }
        } else if (type == Token.DEC || type == Token.NEG) {
          if (tokenType == Token.SUB) {
            writer.write(" ");
          }
        }
      }

      if (flag && rightExpression.isNeedLeftSeparator()) {
        writer.write(" ");
      }
      rightExpression.write(writer, env);
    } else {
      writer.write("(");
      rightExpression.write(writer, env);
      writer.write(")");
    }
  }
}
