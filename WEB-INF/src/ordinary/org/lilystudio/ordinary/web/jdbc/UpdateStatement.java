package org.lilystudio.ordinary.web.jdbc;

import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.lilystudio.ordinary.web.IRelay;

/**
 * 数据库更新语句处理, 更新语句只允许执行insert与update命令,
 * 否则将产生非法操作异常, 更多的属性参见父类
 * 
 * @version 0.1.4, 2008/12/12
 * @author 欧阳先伟
 * @since Ordinary 0.1
 */
public class UpdateStatement extends StatementAction {

  @Override
  public void execute(IRelay relay, Connection conn) throws Exception {
    Statement stat = conn.createStatement();
    try {
      // 检测语句是否为空
      String text = parser.parse(relay.getDataMap()).trim();
      if (text.length() == 0) {
        if (nullException != null) {
          throw (Exception) nullException.newInstance();
        } else {
          return;
        }
      }
      // 取出被分解的sql语句组
      List<String> sqls = disassemble(text);
      List<Object> result = new ArrayList<Object>();

      for (String sql : sqls) {
        String lowerSql = sql.toLowerCase();
        if (!lowerSql.startsWith("insert") && !lowerSql.startsWith("update")) {
          throw new IllegalAccessException();
        }
        result.add(stat.executeUpdate(sql));
      }
      relay.set(name, multiple ? result : result.get(0));
    } finally {
      stat.close();
    }
  }
}
