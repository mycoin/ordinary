package org.lilystudio.ordinary.web.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lilystudio.ordinary.util.IParser;
import org.lilystudio.ordinary.web.IRelay;

/**
 * 数据库查询处理, 更多的属性参见父类<br>
 * 
 * <b>属性</b>
 * 
 * <pre>
 * pageName--页码的数据集合键名称, 默认为page
 * totalName--全部条目数量的数据集合键名称, 默认为total
 * sizeName--单页记录数量大的数据集合键名称, 默认为size, 定义它接受前端要求的页大小
 * default--默认的单页记录数量, 如果不设置它将不分页显示, 即size,page的设置无效
 * top--取置顶的记录数, 设置它后, 请不要设置default,size,page的值
 * totalSql--指定查询结果集总量的语句, 在自动分析得到的处理速度过慢时使用
 * resultFormat--结果集的处理, 为true表示只使用一行保存结果, 此时数组是一维的, 而一般情况下, 每一条记录占用一行, 数组是二维的
 * </pre>
 * 
 * @version 0.1.4, 2008/12/12
 * @author 欧阳先伟
 * @since Ordinary 0.1
 */
public abstract class SelectStatement extends StatementAction {

  /** List<List>型 */
  private static final int LIST_LIST = 0;
  
  /** List<Object>单行型 */
  private static final int SIMPLE_ROW = 1;
  
  /** List<Map>型 */
  private static final int LIST_MAP = 2;

  /** 页码的名称 */
  private String pageName = "page";

  /** 全部条目数的名称 */
  private String totalName;

  /** 单页记录数量大的数据集合键名称 */
  private String sizeName = "size";

  /** 用于总量查询的语句转换器接口 */
  private IParser totalParser;

  /** 默认的单页大小 */
  private int defSize;

  /** 结果的格式 */
  private int resultFormat;

  /**
   * 设置默认的单页大小
   * 
   * @param value
   *          配置文件定义的值
   * @throws Exception
   *           配置文件里设置的值不是数字
   */
  public void setDefault(String value) throws Exception {
    defSize = Integer.parseInt(value);
  }

  /**
   * 设置取顶部的多少条记录结果
   * 
   * @param value
   *          配置文件定义的值
   * @throws Exception
   *           配置文件里设置的值不是数字
   */
  public void setTop(String value) throws Exception {
    defSize = -Integer.parseInt(value);
  }

  /**
   * 设置查询总量的sql语句
   * 
   * @param value
   *          语句的解析表达式
   * @throws Exception
   *           解析错误
   */
  public void setTotalSql(String value) throws Exception {
    totalParser = parser.getClass().newInstance();
    totalParser.init(value);
  }

  /**
   * 设置结果的格式
   * 
   * @param value
   *          语句的解析表达式
   * @throws Exception
   *           解析错误
   */
  public void setResultFormat(String value) throws Exception {
    // HARDCODE
    if (value.equals("row")) {
      resultFormat = SIMPLE_ROW;
    } else if (value.equals("map")) {
      resultFormat = LIST_MAP;
    } else {
      resultFormat = LIST_LIST;
    }
  }

  /**
   * 分页的SQL语句生成
   * 
   * @param sql
   *          原始的SQL语句
   * @param page
   *          需要显示的页, 从1开始计数
   * @param size
   *          单页的大小
   * @return 合成的SQL语句
   */
  public abstract String pagination(String sql, int page, int size);

  /**
   * 统计数据总量的SQL语句生成(过段时间需要考虑改写)
   * 
   * @param sql
   *          原始的SQL语句
   * @return 合成的SQL语句
   */
  private String count(String sql) {
    List<String> parts = new ArrayList<String>();
    StringBuilder buf = new StringBuilder(sql.toLowerCase());
    int start = -1;
    int len = sql.length() - 1;
    for (int i = 0; i <= len; i++) {
      char c = buf.charAt(i);
      if (c == '`' || c == '\'') {
        start = i;
        while (i < len) {
          i++;
          char d = buf.charAt(i);
          if (c == d) {
            if (i < len - 1 && d == '\'' && buf.charAt(i + 1) == '\'') {
              i++;
              continue;
            }
            parts.add(buf.substring(start, i + 1));
            buf.replace(start, i + 1, "\0" + (char) (parts.size() - 1));
            len = buf.length() - 1;
            i = start + 1;
            break;
          }
        }
      }
    }
    // 括号的级数
    int level = 0;
    for (int i = 0; i <= len; i++) {
      char c = buf.charAt(i);
      if (c == '(') {
        if (level == 0) {
          start = i;
        }
        level++;
      } else if (c == ')') {
        level--;
        if (level == 0) {
          parts.add(buf.substring(start, i + 1));
          buf.replace(start, i + 1, "\0" + (char) (parts.size() - 1));
          len = buf.length() - 1;
          i = start + 1;
        }
      }
    }
    sql = buf.toString();
    buf.setLength(0);
    if (sql.matches(".+\\s+group\\s+by\\s+.+")) {
      buf.append("select count(*) from (").append(sql).append(") t");
    } else {
      String[] sentence = sql.split("\\s+union\\s+");
      len = sentence.length;
      if (len > 1) {
        buf.append("select sum(c) from (");
      }
      for (int i = 0; i < len; i++) {
        if (i > 0) {
          buf.append(" union ");
        }
        buf
            .append(sentence[i]
                .replace('\n', ' ')
                .replaceAll(
                    "\\s+order\\s+by\\s+(\\w+|\\x00.)(\\s*\\.\\s*(\\w+|\\x00.))?(\\s+(desc|asc))?(\\s*,\\s*(\\w+|\\x00.)(\\s*\\.\\s*(\\w+|\\x00.))?(\\s+(desc|asc))?)*",
                    "").replaceAll("(^|\\s+)select\\s+.+\\sfrom\\s+",
                    "$1select count(*) as c from "));
      }
      if (len > 1) {
        buf.append(") t");
      }
    }
    len = buf.length();
    for (int i = 0; i < len; i++) {
      char c = buf.charAt(i);
      if (c == '\0') {
        buf.replace(i, i + 2, parts.get(buf.charAt(i + 1)));
        len = buf.length();
      }
    }
    return buf.toString();
  }

  @Override
  public void execute(IRelay relay, Connection conn) throws Exception {
    // 检测语句是否为空
    String text = parser.parse(relay.getDataMap()).trim();
    if (text.length() == 0) {
      if (nullException != null) {
        throw (Exception) nullException.newInstance();
      } else {
        return;
      }
    }
    Statement stat = conn.createStatement();
    try {
      // 检查是否需要查询数据总量
      if (totalName != null) {
        ResultSet rs = stat.executeQuery(totalParser != null ? totalParser
            .parse(relay.getDataMap()) : count(text));
        rs.next();
        relay.set(totalName, rs.getObject(1));
      }
      // 检测是否需要分页
      if (defSize < 0) {
        // 只需要查询顶部的若干条数据
        text = pagination(text, 1, -defSize);
      } else if (defSize > 0) {
        int size;
        try {
          size = Integer.parseInt(relay.get(sizeName).toString());
        } catch (Exception e) {
          size = defSize;
        }
        relay.set(sizeName, size);
        int page;
        try {
          page = Math.max(Integer.parseInt(relay.get(pageName).toString()), 1);
          if (totalName != null) {
            page = Math.min(page, (Integer.parseInt(relay.get(totalName)
                .toString()) - 1)
                / size + 1);
          }
        } catch (Exception e) {
          page = 1;
        }
        relay.set(pageName, page);
        text = pagination(text, page, size);
      }
      // 取出被分解的sql语句组
      List<String> sqls = disassemble(text);
      List<Object> result = new ArrayList<Object>();

      for (String sql : sqls) {
        ResultSet rs = stat.executeQuery(sql);
        List<Object> table;
        if (multiple) {
          // 如果支持多语句查询, 每一条查询语句的结果占用一个空间
          table = new ArrayList<Object>();
          result.add(table);
        } else {
          table = result;
        }

        // 格式化查询的结果, 每一条记录对应List中的一行
        int count = rs.getMetaData().getColumnCount();
        if (resultFormat == LIST_MAP) {
          ResultSetMetaData md = rs.getMetaData();
          String[] names = new String[count + 1];
          for (int i = 1; i <= count; i++) {
            String name = md.getColumnLabel(i);
            while (true) {
              int j = name.indexOf('_');
              if (j < 0) {
                break;
              }
              name = name.substring(0, j)
                  + Character.toUpperCase(name.charAt(j + 1))
                  + name.substring(j + 2);
            }
            names[i] = name;
          }
          while (rs.next()) {
            Map<String, Object> map = new HashMap<String, Object>();
            for (int i = 1; i <= count; i++) {
              map.put(names[i], rs.getObject(i));
            }
            table.add(map);
          }
        } else {
          while (rs.next()) {
            List<Object> list;
            if (resultFormat == SIMPLE_ROW) {
              // 检查是否为单行结果模式
              list = table;
            } else {
              list = new ArrayList<Object>();
              table.add(list);
            }
            for (int i = 1; i <= count; i++) {
              list.add(rs.getObject(i));
            }
          }
        }
      }
      relay.set(name, result);
    } finally {
      stat.close();
    }
  }
}
