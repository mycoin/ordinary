package org.lilystudio.ordinary.web.jdbc;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.lilystudio.ordinary.web.IRelay;
import org.lilystudio.ordinary.web.action.ParseAction;

/**
 * 数据库执行语句处理, 更多的属性参见父类<br>
 * 
 * <b>属性</b>
 * 
 * <pre>
 * ds--jdbc连接池名称
 * name--结果集的数据集合键名称, 默认为result
 * multiple--是否允许一次执行多条语句, 如果是查询, 等于对结果进行增一维扩展; 如果是更新语句, 依次保存返回结果列表
 * null--sql内容为空时产生的异常类名称, 默认产生的是NullStatementException, 如果为allow, 表示不产生异常
 * 文本--sql执行语句
 * </pre>
 * 
 * @see org.lilystudio.ordinary.util.IParser
 * 
 * @version 0.1.4, 2008/12/12
 * @author 欧阳先伟
 * @since Ordinary 0.1
 */
public abstract class StatementAction extends ParseAction {

  /**
   * 数据库更新语句为空时产生的默认异常
   */
  public class NullStatementException extends Exception {

    private static final long serialVersionUID = 1L;
  }
  
  /** 是否允许一次执行多条语句 */
  protected boolean multiple;

  /** 结果集的名称 */
  protected String name = "result";

  /** 设置当SQL语句为空时产生的异常 */
  protected Class<?> nullException = NullStatementException.class;

  /** 数据库连接池 */
  private DataSource ds;

  /**
   * 设置为空时产生的异常类名称
   * 
   * @param value
   *          配置文件中指定的异常类全名
   * @throws Exception
   *           异常类不存在或者无法初始化
   */
  public void setNull(String value) throws Exception {
    // HARDCODE
    nullException = "allow".equals(value) ? null : Class.forName(value);
  }

  /**
   * 分解sql语句, 如果不支持多条sql语句执行的模式, 则只返回第一条sql
   * 
   * @param sql
   *          原始的sql语句集合
   * @return 分解后的sql语句列表
   */
  public List<String> disassemble(String sql) {
    List<String> result = new ArrayList<String>();
    int start = 0;
    int length = sql.length();
    for (int end = start; end < length; end++) {
      switch (sql.charAt(end)) {
      case '\'':
        while (true) {
          end++;
          if (sql.charAt(end) == '\'') {
            if (end + 1 == length || sql.charAt(end + 1) != '\'') {
              break;
            }
            end++;
          }
        }
        break;
      case ';':
        // ;号不出现在引号中时, 表示语句间的分隔
        result.add(sql.substring(start, end).trim());
        if (!multiple) {
          // 不允许多条语句执行时, 找到第一条就结束
          return result;
        }
        start = end + 1;
        break;
      }
    }
    if (result.isEmpty()) {
      // 保证至少能分解出一条sql
      result.add(sql);
    }
    return result;
  }

  /**
   * 执行SQL语句
   * 
   * @param relay
   *          数据集合
   * @param conn
   *          当前活跃的连接
   * @throws Exception
   *           SQL执行错误
   */
  public abstract void execute(IRelay relay, Connection conn) throws Exception;

  @Override
  public void execute(IRelay relay) throws Exception {
    Connection conn = ds.getConnection();
    try {
      conn.setAutoCommit(true);
      execute(relay, conn);
    } finally {
      try {
        conn.close();
      } catch (Exception e) {
      }
    }
  }
}
