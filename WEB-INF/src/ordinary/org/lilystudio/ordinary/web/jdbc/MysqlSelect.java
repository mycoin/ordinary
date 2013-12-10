package org.lilystudio.ordinary.web.jdbc;

/**
 * Mysql数据库查询语句处理, 更多的属性参见父类
 * 
 * @version 0.1.4, 2008/12/12
 * @author 欧阳先伟
 * @since Ordinary 0.1
 */
public class MysqlSelect extends SelectStatement {

  public String pagination(String sql, int page, int size) {
    return sql + " limit " + ((page - 1) * size) + "," + size;  
  }
}
