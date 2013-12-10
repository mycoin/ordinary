package org.lilystudio.javascript;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.lilystudio.javascript.expression.ArrayLiteral;
import org.lilystudio.javascript.expression.BinaryExpression;
import org.lilystudio.javascript.expression.CallExpression;
import org.lilystudio.javascript.expression.ConditionalExpression;
import org.lilystudio.javascript.expression.FunctionLiteral;
import org.lilystudio.javascript.expression.InbuildLiteral;
import org.lilystudio.javascript.expression.IdentifierExpression;
import org.lilystudio.javascript.expression.MemberExpression;
import org.lilystudio.javascript.expression.NumericLiteral;
import org.lilystudio.javascript.expression.ObjectLiteral;
import org.lilystudio.javascript.expression.PostfixExpression;
import org.lilystudio.javascript.expression.RegexpLiteral;
import org.lilystudio.javascript.expression.StringLiteral;
import org.lilystudio.javascript.expression.ThisExpression;
import org.lilystudio.javascript.expression.UnaryExpression;
import org.lilystudio.javascript.scope.Scope;
import org.lilystudio.javascript.statement.BlockStatement;
import org.lilystudio.javascript.statement.EmptyStatement;
import org.lilystudio.javascript.statement.ExpressionStatement;
import org.lilystudio.javascript.statement.ForinStatement;
import org.lilystudio.javascript.statement.FunctionStatement;
import org.lilystudio.javascript.statement.IfStatement;
import org.lilystudio.javascript.statement.ControlStatement;
import org.lilystudio.javascript.statement.LabelStatement;
import org.lilystudio.javascript.statement.LoopStatement;
import org.lilystudio.javascript.statement.SwitchStatement;
import org.lilystudio.javascript.statement.ThrowStatement;
import org.lilystudio.javascript.statement.TryStatement;
import org.lilystudio.javascript.statement.VariableStatement;
import org.lilystudio.javascript.statement.WithStatement;
import org.mozilla.javascript.Node;
import org.mozilla.javascript.ScriptOrFnNode;
import org.mozilla.javascript.Token;

/**
 * 工具类，提供静态方法
 * 
 * @version 1.0.0, 2010/01/01
 * @author 欧阳先伟
 * @since Common 0.1
 */
public class Utils {

  /** 类型常量表 */
  private static final Map<Integer, String> literals = new HashMap<Integer, String>();

  static {
    literals.put(new Integer(Token.GET), "get");
    literals.put(new Integer(Token.SET), "set");
    literals.put(new Integer(Token.TRUE), "true");
    literals.put(new Integer(Token.FALSE), "false");
    literals.put(new Integer(Token.NULL), "null");
    literals.put(new Integer(Token.THIS), "this");
    literals.put(new Integer(Token.FUNCTION), "function");
    literals.put(new Integer(Token.COMMA), ",");
    literals.put(new Integer(Token.LC), "{");
    literals.put(new Integer(Token.RC), "}");
    literals.put(new Integer(Token.LP), "(");
    literals.put(new Integer(Token.RP), ")");
    literals.put(new Integer(Token.LB), "[");
    literals.put(new Integer(Token.RB), "]");
    literals.put(new Integer(Token.DOT), ".");
    literals.put(new Integer(Token.NEW), "new");
    literals.put(new Integer(Token.DELPROP), "delete");
    literals.put(new Integer(Token.IF), "if");
    literals.put(new Integer(Token.ELSE), "else");
    literals.put(new Integer(Token.FOR), "for");
    literals.put(new Integer(Token.IN), "in");
    literals.put(new Integer(Token.WITH), "with");
    literals.put(new Integer(Token.WHILE), "while");
    literals.put(new Integer(Token.DO), "do");
    literals.put(new Integer(Token.TRY), "try");
    literals.put(new Integer(Token.CATCH), "catch");
    literals.put(new Integer(Token.FINALLY), "finally");
    literals.put(new Integer(Token.THROW), "throw");
    literals.put(new Integer(Token.SWITCH), "switch");
    literals.put(new Integer(Token.BREAK), "break");
    literals.put(new Integer(Token.CONTINUE), "continue");
    literals.put(new Integer(Token.CASE), "case");
    literals.put(new Integer(Token.DEFAULT), "default");
    literals.put(new Integer(Token.RETURN), "return");
    literals.put(new Integer(Token.VAR), "var");
    literals.put(new Integer(Token.SEMI), ";");
    literals.put(new Integer(Token.ASSIGN), "=");
    literals.put(new Integer(Token.ASSIGN_ADD), "+=");
    literals.put(new Integer(Token.ASSIGN_SUB), "-=");
    literals.put(new Integer(Token.ASSIGN_MUL), "*=");
    literals.put(new Integer(Token.ASSIGN_DIV), "/=");
    literals.put(new Integer(Token.ASSIGN_MOD), "%=");
    literals.put(new Integer(Token.ASSIGN_BITOR), "|=");
    literals.put(new Integer(Token.ASSIGN_BITXOR), "^=");
    literals.put(new Integer(Token.ASSIGN_BITAND), "&=");
    literals.put(new Integer(Token.ASSIGN_LSH), "<<=");
    literals.put(new Integer(Token.ASSIGN_RSH), ">>=");
    literals.put(new Integer(Token.ASSIGN_URSH), ">>>=");
    literals.put(new Integer(Token.HOOK), "?");
    literals.put(new Integer(Token.OBJECTLIT), ":");
    literals.put(new Integer(Token.COLON), ":");
    literals.put(new Integer(Token.OR), "||");
    literals.put(new Integer(Token.AND), "&&");
    literals.put(new Integer(Token.BITOR), "|");
    literals.put(new Integer(Token.BITXOR), "^");
    literals.put(new Integer(Token.BITAND), "&");
    literals.put(new Integer(Token.SHEQ), "===");
    literals.put(new Integer(Token.SHNE), "!==");
    literals.put(new Integer(Token.EQ), "==");
    literals.put(new Integer(Token.NE), "!=");
    literals.put(new Integer(Token.LE), "<=");
    literals.put(new Integer(Token.LT), "<");
    literals.put(new Integer(Token.GE), ">=");
    literals.put(new Integer(Token.GT), ">");
    literals.put(new Integer(Token.INSTANCEOF), "instanceof");
    literals.put(new Integer(Token.LSH), "<<");
    literals.put(new Integer(Token.RSH), ">>");
    literals.put(new Integer(Token.URSH), ">>>");
    literals.put(new Integer(Token.TYPEOF), "typeof");
    literals.put(new Integer(Token.VOID), "void");
    literals.put(new Integer(Token.CONST), "const");
    literals.put(new Integer(Token.NOT), "!");
    literals.put(new Integer(Token.BITNOT), "~");
    literals.put(new Integer(Token.POS), "+");
    literals.put(new Integer(Token.NEG), "-");
    literals.put(new Integer(Token.INC), "++");
    literals.put(new Integer(Token.DEC), "--");
    literals.put(new Integer(Token.ADD), "+");
    literals.put(new Integer(Token.SUB), "-");
    literals.put(new Integer(Token.MUL), "*");
    literals.put(new Integer(Token.DIV), "/");
    literals.put(new Integer(Token.MOD), "%");
    literals.put(new Integer(Token.COLONCOLON), "::");
    literals.put(new Integer(Token.DOTDOT), "..");
    literals.put(new Integer(Token.DOTQUERY), ".(");
    literals.put(new Integer(Token.XMLATTR), "@");
  }

  /**
   * 转换Token类型为对应的描述字符串
   * 
   * @param tokenType
   *          类型
   * @return 类型的字符串
   */
  public static String escapeLiteral(int tokenType) {
    return literals.get(tokenType);
  }

  /** 保留字表 */
  private static final List<String> reserved = new ArrayList<String>();

  static {
    reserved.add("break");
    reserved.add("case");
    reserved.add("catch");
    reserved.add("continue");
    reserved.add("default");
    reserved.add("delete");
    reserved.add("do");
    reserved.add("else");
    reserved.add("finally");
    reserved.add("for");
    reserved.add("function");
    reserved.add("if");
    reserved.add("in");
    reserved.add("instanceof");
    reserved.add("new");
    reserved.add("return");
    reserved.add("switch");
    reserved.add("this");
    reserved.add("throw");
    reserved.add("try");
    reserved.add("typeof");
    reserved.add("var");
    reserved.add("void");
    reserved.add("while");
    reserved.add("with");
    reserved.add("abstract");
    reserved.add("boolean");
    reserved.add("byte");
    reserved.add("char");
    reserved.add("class");
    reserved.add("const");
    reserved.add("debugger");
    reserved.add("double");
    reserved.add("enum");
    reserved.add("export");
    reserved.add("extends");
    reserved.add("final");
    reserved.add("float");
    reserved.add("goto");
    reserved.add("implements");
    reserved.add("import");
    reserved.add("int");
    reserved.add("interface");
    reserved.add("long");
    reserved.add("native");
    reserved.add("package");
    reserved.add("private");
    reserved.add("protected");
    reserved.add("public");
    reserved.add("short");
    reserved.add("static");
    reserved.add("super");
    reserved.add("synchronized");
    reserved.add("throws");
    reserved.add("transient");
    reserved.add("volatile");
    reserved.add("arguments");
    reserved.add("eval");
    reserved.add("true");
    reserved.add("false");
    reserved.add("Infinity");
    reserved.add("NaN");
    reserved.add("null");
    reserved.add("undefined");
  }

  private static final Pattern SIMPLE_IDENTIFIER_NAME_PATTERN = Pattern
      .compile("^[a-zA-Z$_][a-zA-Z0-9$_]*$");

  /**
   * 判断字符串是否为有效的标识符
   * 
   * @param id
   *          需要判断的字符串
   * @return 是否为有效的标识符
   */
  public static boolean isValidIdentifier(String id) {
    Matcher m = SIMPLE_IDENTIFIER_NAME_PATTERN.matcher(id);
    return m.matches() && !reserved.contains(id);
  }

  /**
   * 转换字符串成为最短的JS字符串常量
   * 
   * @param s
   *          需要转换的字符串
   * @return JS字符串
   */
  public static String escapeJSString(String s) {
    int flag = 0;
    int len = s.length();
    for (int i = 0; i < len; i++) {
      char c = s.charAt(i);
      if (c == '"') {
        flag++;
      } else if (c == '\'') {
        flag--;
      }
    }
    StringBuilder sb = new StringBuilder();
    sb.append(flag > 0 ? '\'' : '"');
    for (int i = 0; i < len; i++) {
      char c = s.charAt(i);
      if (c > 255) {
        sb.append("\\u");
        if (c < 256 * 16) {
          sb.append("0");
        }
        sb.append(Integer.toHexString(c));
        continue;
      }
      switch (c) {
      case '\b':
        sb.append("\\b");
        continue;
      case '\f':
        sb.append("\\f");
        continue;
      case '\n':
        sb.append("\\n");
        continue;
      case '\r':
        sb.append("\\r");
        continue;
      case '\t':
        sb.append("\\t");
        continue;
      case '\\':
        sb.append("\\\\");
        continue;
      }
      if (c == '\'' && flag > 0 || c == '"' && flag <= 0) {
        sb.append('\\');
      }
      sb.append(c);
    }
    sb.append(flag > 0 ? '\'' : '"');
    return sb.toString();
  }

  /**
   * 根据rhino节点建立一个语句子节点
   * 
   * @param node
   *          语句子节点对应的rhino节点
   * @param root
   *          语句子节点对应的rhino根节点
   * @param scope
   *          语句子节点生存域
   * @return 语句子节点
   */
  public static IStatement createStatement(Node node, ScriptOrFnNode root,
      Scope scope) {
    switch (node.getType()) {
    case Token.BLOCK: {
      BlockStatement blockStatement = new BlockStatement(node, root, scope);
      StatementList statements = blockStatement.getStatementList();
      for (int i = statements.size() - 1; i >= 0; i--) {
        IStatement statement = statements.get(i);
        if (statement instanceof EmptyStatement) {
          statements.remove(i);
        }
      }

      IStatement statement;
      switch (statements.size()) {
      case 0:
        // 块语句没有内容等同于空语句
        statement = new EmptyStatement(node, root, scope);
        statement.setNext(blockStatement.getNext());
        statement.setParent(blockStatement.getParent());
        return statement;
      case 1:
        // 块语句只有一条语句等同于那一条语句
        statement = statements.get(0);
        statement.setNext(blockStatement.getNext());
        statement.setParent(blockStatement.getParent());
        return statement;
      default:
        return blockStatement;
      }
    }

    case Token.VAR:
      return new VariableStatement(node, root, scope);

    case Token.EMPTY:
      return new EmptyStatement(node, root, scope);

    case Token.EXPR_RESULT:
    case Token.EXPR_VOID:
      return new ExpressionStatement(node, root, scope);

    case Token.IFNE:
      return new IfStatement(node, root, scope);

    case Token.LOOP:
      return new LoopStatement(node, root, scope);

    case Token.LOCAL_BLOCK: {
      switch (node.getFirstChild().getType()) {
      case Token.LOOP:
        return new ForinStatement(node, root, scope);
      case Token.TRY:
        return new TryStatement(node, root, scope);
      default:
        throw new RuntimeException(node.getFirstChild().getType() + "");
      }
    }

    case Token.CONTINUE:
    case Token.BREAK:
    case Token.RETURN:
      return new ControlStatement(node, root, scope);

    case Token.ENTERWITH:
      return new WithStatement(node, root, scope);

    case Token.SWITCH:
      return new SwitchStatement(node, root, scope);

    case Token.LABEL:
      return new LabelStatement(node, root, scope);

    case Token.THROW:
      return new ThrowStatement(node, root, scope);

    case Token.FUNCTION:
      return new FunctionStatement(node, root, scope);

    default:
      throw new RuntimeException(node.getType() + "");
    }
  }

  /**
   * 根据rhino节点建立一个表达式节点
   * 
   * @param node
   *          表达式子节点对应的rhino节点
   * @param root
   *          表达式子节点对应的rhino根节点
   * @param scope
   *          表达式子节点生存域
   * @return 表达式节点
   */
  public static IExpression createExpression(Node node, ScriptOrFnNode root,
      Scope scope) {
    switch (node.getType()) {
    case Token.THIS:
      return new ThisExpression(node, root, scope);

    case Token.NAME:
    case Token.BINDNAME:
      return new IdentifierExpression(node, root, scope);

    case Token.TRUE:
    case Token.FALSE:
    case Token.NULL:
      return new InbuildLiteral(node, root, scope);

    case Token.STRING:
      return new StringLiteral(node, root, scope);

    case Token.NUMBER:
      return new NumericLiteral(node, root, scope);

    case Token.REGEXP:
      return new RegexpLiteral(node, root, scope);

    case Token.ARRAYLIT:
      return new ArrayLiteral(node, root, scope);

    case Token.OBJECTLIT:
      return new ObjectLiteral(node, root, scope);

    case Token.GETPROP:
    case Token.GETELEM:
      return new MemberExpression(node, root, scope);

    case Token.CALL:
    case Token.NEW: {
      Node firstChild = node.getFirstChild();
      if (firstChild.getType() == Token.NAME) {
        String target = firstChild.getString();
        Node next = firstChild.getNext();
        if (target.equals("Object")) {
          // Object类不需要使用new
          node.setType(Token.CALL);
          if (next == null) {
            return new ObjectLiteral(node.getLineno());
          }
        } else if (target.equals("Array")) {
          // Array类可以使用[]代替
          node.setType(Token.CALL);
          if (next == null
              || (next.getType() == Token.NUMBER && next.getDouble() == 0)
              || next.getNext() != null) {
            if (next != null && next.getType() == Token.NUMBER
                && next.getDouble() == 0) {
              node.removeChild(next);
            }
            node.removeChild(firstChild);
            return new ArrayLiteral(node, root, scope);
          }
        }
      }
      return new CallExpression(node, root, scope);
    }

    case Token.INC:
    case Token.DEC:
      if (node.getIntProp(Node.INCRDECR_PROP, -1) > 1) {
        return new PostfixExpression(node, root, scope);
      }
    case Token.DELPROP:
    case Token.TYPEOF:
    case Token.TYPEOFNAME:
    case Token.VOID:
    case Token.POS:
    case Token.NEG:
    case Token.NOT:
    case Token.BITNOT:
      return new UnaryExpression(node, root, scope);

    case Token.MUL:
    case Token.DIV:
    case Token.MOD:
    case Token.ADD:
    case Token.SUB:
    case Token.LSH:
    case Token.RSH:
    case Token.URSH:
    case Token.LT:
    case Token.LE:
    case Token.GT:
    case Token.GE:
    case Token.INSTANCEOF:
    case Token.IN:
    case Token.EQ:
    case Token.NE:
    case Token.SHEQ:
    case Token.SHNE:
    case Token.BITAND:
    case Token.BITXOR:
    case Token.BITOR:
    case Token.AND:
    case Token.OR:
    case Token.SETNAME:
    case Token.SETPROP:
    case Token.SETELEM:
    case Token.SETPROP_OP:
    case Token.SETELEM_OP:
    case Token.COMMA:
      return new BinaryExpression(node, root, scope);

    case Token.HOOK:
      return new ConditionalExpression(node, root, scope);

    case Token.FUNCTION:
      return new FunctionLiteral(node, root, scope);

    default:
      throw new RuntimeException(node.getType() + "");
    }
  }
}
