package org.lilystudio.coder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * JSON解码器
 * 
 * @version 0.1.4, 2008/12/12
 * @author 欧阳先伟
 * @since Common 0.1
 */
public class JSONDecoder {

  /**
   * 根据字符序列生成对象, 集合生成Map对象, 列表生成List对象
   * 
   * @param cs
   *          字符序列
   * @return 与序列对应的对象
   * @throws CoderException
   *           字符序列编码错误
   */
  public static Object decode(CharSequence cs) throws CoderException {
    Object[] result = new Object[1];
    findObject(cs, 0, cs.length(), result);
    return result[0];
  }

  /**
   * 从字符序列中生成对象, 解析出的对象存放在result[0]中
   * 
   * @param cs
   *          字符序列
   * @param pos
   *          开始读取的位置
   * @param length
   *          结束读取的位置
   * @param result
   *          [0] 结果对象
   * @return 新的字符序列开始读取的位置
   * @throws CoderException
   *           字符序列编码错误
   */
  private static int findObject(CharSequence cs, int pos, int length,
      Object[] result) throws CoderException {
    pos = skipComment(cs, pos, length);
    int start = pos;
    char c = cs.charAt(pos);
    switch (c) {
    case '"': {
      // 从字符序列中生成字符串
      StringBuilder sb = new StringBuilder();
      main: while (++pos < length) {
        c = cs.charAt(pos);
        if (c == '\\') {
          // 字符转义
          if (++pos < length) {
            c = cs.charAt(pos);
            switch (c) {
            case '"':
            case '\\':
            case '/':
            case '\'':
              sb.append(c);
              continue;
            case 'b':
              sb.append('\b');
              continue;
            case 'f':
              sb.append('\f');
              continue;
            case 'n':
              sb.append('\n');
              continue;
            case 'r':
              sb.append('\r');
              continue;
            case 't':
              sb.append('\t');
              continue;
            case 'u':
              pos += 4;
              if (pos >= length) {
                break main;
              }
              sb.append((char) Integer.parseInt(cs
                  .subSequence(pos - 3, pos + 1).toString(), 16));
              continue;
            case 'x':
              pos += 2;
              if (pos >= length) {
                break main;
              }
              sb.append((char) Integer.parseInt(cs
                  .subSequence(pos - 1, pos + 1).toString(), 16));
              continue;
            }
          }
          break;
        } else if (c == '"') {
          result[0] = sb.toString();
          return ++pos;
        } else {
          sb.append(c);
        }
      }
      // HARDCODE
      throw new CoderException("Unterminated string literal", cs.subSequence(
          start, pos).toString());
    }
    case '-':
    case '.':
    case '0':
    case '1':
    case '2':
    case '3':
    case '4':
    case '5':
    case '6':
    case '7':
    case '8':
    case '9': {
      // 数值加载
      char firstChar = c;
      if (c == '-') {
        c = cs.charAt(++pos);
      }
      // 状态, 0表示小数点浮点数, 1表示科学计数法(未读科学计数符号),
      // 2表示科学计数法(已读科学计数符号以及指数), 3表示科学计数法(未读指数),
      // 10表示十进制数, 8表示8进制数, 16表示16进制数
      main: while (true) {
        int status = -1;
        if (c == '0') {
          if (++pos < length) {
            c = cs.charAt(pos);
            if (c == 'x' || c == 'X') {
              status = 16;
              pos++;
            } else if (c >= '0' && c <= '9') {
              status = 8;
            }
          }
        } else if (c >= '1' && c <= '9') {
          status = 10;
          pos++;
        }
        if (c == '.') {
          status = 0;
          pos++;
        }
        for (; pos < length; pos++) {
          c = cs.charAt(pos);
          if (isSeparator(c)) {
            break;
          }
          switch (status) {
          case 10:
            if (c == '.') {
              status = 0;
              continue;
            }
          case 0:
            if (c == 'e' || c == 'E') {
              status = 1;
              continue;
            }
          case 2:
            if (c >= '0' && c <= '9') {
              continue;
            }
            break main;
          case 1:
            if (c == '+' || c == '-') {
              status = 3;
              continue;
            }
          case 3:
            if (c >= '0' && c <= '9') {
              status = 2;
              continue;
            }
            break main;
          case 8:
            if (c >= '0' && c <= '7') {
              continue;
            }
            break main;
          case 16:
            if ((c >= '0' && c <= '9') || (c >= 'A' && c <= 'Z')
                || (c >= 'a' && c <= 'z')) {
              continue;
            }
            break main;
          }
        }
        String text = status == 16 ? firstChar
            + cs.subSequence(start + (firstChar == '-' ? 3 : 2), pos)
                .toString() : cs.subSequence(start, pos).toString();
        if (status % 2 == 0) {
          if (status < 3) {
            result[0] = Double.valueOf(text);
          } else {
            try {
              result[0] = Integer.valueOf(text, status);
            } catch (NumberFormatException e) {
              result[0] = Long.valueOf(text, status);
            }
          }
          return pos;
        }
        break main;
      }
      // HARDCODE
      throw new CoderException("Invalid number", cs.subSequence(start, pos)
          .toString());
    }
    case '[': {
      // 列表数据加载
      ArrayList<Object> list = new ArrayList<Object>();
      pos = skipComment(cs, ++pos, length);
      if (cs.charAt(pos) != ']') {
        pos = findObject(cs, pos, length, result);
        list.add(result[0]);
        while (true) {
          pos = skipComment(cs, pos, length);
          c = cs.charAt(pos);
          if (c == ']') {
            break;
          } else if (c == ',') {
            pos = findObject(cs, ++pos, length, result);
            list.add(result[0]);
          } else {
            // HARDCODE
            throw new CoderException("Missing ] after element list", cs
                .subSequence(start, pos).toString());
          }
        }
      }
      result[0] = list;
      return ++pos;
    }
    case '{': {
      // 集合数据加载
      Map<Object, Object> map = new HashMap<Object, Object>();
      pos = skipComment(cs, ++pos, length);
      if (cs.charAt(pos) != '}') {
        while (true) {
          pos = findObject(cs, pos, length, result);
          Object key = result[0];
          if (!(key instanceof String)) {
            // HARDCODE
            throw new CoderException("Invalid property id", cs.subSequence(
                start, pos).toString());
          }
          pos = skipComment(cs, pos, length);
          c = cs.charAt(pos);
          if (c != ':') {
            // HARDCODE
            throw new CoderException("Missing : after property id", cs
                .subSequence(start, pos).toString());
          }
          pos = skipComment(cs, findObject(cs, ++pos, length, result), length);
          c = cs.charAt(pos);
          map.put(key, result[0]);
          if (c == '}') {
            break;
          } else if (c != ',') {
            // HARDCODE
            throw new CoderException("Missing } after property list", cs
                .subSequence(start, pos).toString());
          } else {
            pos++;
          }
        }
      }
      result[0] = map;
      return ++pos;
    }
    default:
      if (c >= 'a' && c <= 'z') {
        // 识别关键字
        while (++pos < length) {
          if (!Character.isJavaIdentifierPart(cs.charAt(pos))) {
            break;
          }
        }
        String key = cs.subSequence(start, pos).toString();
        if ("true".equals(key)) {
          result[0] = Boolean.TRUE;
          return pos;
        } else if ("false".equals(key)) {
          result[0] = Boolean.FALSE;
          return pos;
        } else if ("null".equals(key)) {
          result[0] = null;
          return pos;
        }
      }
      // HARDCODE
      throw new CoderException("Illegal character", cs.subSequence(start, pos)
          .toString());
    }
  }

  /**
   * 越过字符序列中的注释信息
   * 
   * @param cs
   *          字符序列
   * @param pos
   *          开始读取的位置
   * @param length
   *          结束读取的位置
   * @return 新的字符序列开始读取的位置
   * @throws CoderException
   *           字符序列编码错误
   */
  private static int skipComment(CharSequence cs, int pos, int length)
      throws CoderException {
    main: for (; pos < length; pos++) {
      char c = cs.charAt(pos);
      if (Character.isWhitespace(c)) {
        continue;
      }
      if (c == '/') {
        int start = pos;
        if (++pos < length) {
          c = cs.charAt(pos);
          if (c == '/') {
            while (true) {
              if (++pos >= length) {
                break;
              }
              c = cs.charAt(pos);
              if (c == '\n') {
                continue main;
              }
            }
          } else if (c == '*') {
            loop: while (true) {
              if (++pos >= length) {
                break;
              }
              c = cs.charAt(pos);
              while (c == '*') {
                if (++pos >= length) {
                  break loop;
                }
                c = cs.charAt(pos);
                if (c == '/') {
                  continue main;
                }
              }
            }
          }
        }
        // HARDCODE
        throw new CoderException("Unterminated comment", cs.subSequence(start,
            pos).toString());
      }
      break;
    }
    return pos;
  }

  /**
   * 判断一个字符是否为JSON分隔符号
   * 
   * @param c
   *          需要判断的字符
   * @return 字符是分隔符号返回<tt>true</tt>
   */
  private static boolean isSeparator(char c) {
    return c == ']' || c == '}' || c == ',' || Character.isWhitespace(c);
  }
}
