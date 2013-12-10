package org.lilystudio.ordinary.web.jdbc;

import java.sql.Connection;
import java.util.List;

import javax.sql.DataSource;

import org.lilystudio.ordinary.web.IExecute;
import org.lilystudio.ordinary.web.IRelay;

/**
 * 数据库事务处理, 子标签是具体的数据库执行语句类<br>
 * 
 * <b>属性</b>
 * 
 * <pre>
 * ds--jdbc连接池
 * </pre>
 * 
 * @version 0.1.4, 2008/12/12
 * @author 欧阳先伟
 * @since Ordinary 0.1
 */
public class TransactionAction implements IExecute {

  /** 数据连接池 */
  private DataSource ds;

  /** 数据库全部语句 */
  private List<StatementAction> action;

  public void execute(IRelay relay) throws Exception {
    if (action != null) {
      Connection conn = ds.getConnection();
      boolean rollback = false;
      try {
        // 遍历执行所有的SQL语句, 执行过程是一个完整的事务调用,
        // 如果失败将恢复到调用前
        conn.setAutoCommit(false);
        for (StatementAction statement : action) {
          statement.execute(relay, conn);
          rollback = true;
        }
        conn.commit();
        conn.setAutoCommit(true);
      } finally {
        if (rollback) {
          try {
            conn.rollback();
          } catch (Exception e) {
          }
        }
        try {
          conn.close();
        } catch (Exception e) {
        }
      }
    }
  }
}
