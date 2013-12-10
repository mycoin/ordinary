package org.lilystudio.ordinary.module;

import javax.sql.DataSource;

import org.lilystudio.ordinary.IManager;

/**
 * 加载DataSource对象, 如果失败请检查Web服务器日志, 更多的属性参见父类
 * 
 * <b>属性</b>
 * 
 * <pre>
 * driver--jdbc驱动类的名称, 如mysql 5.0是org.gjt.mm.mysql.Driver
 * url--jdbc连接字符串
 * user--数据库登录用户名
 * password--数据库登录密码
 * minSize--连接池中保留的最小连接数
 * maxSize--连接池中保留的最大连接数
 * </pre>
 * 
 * @version 0.1.4, 2008/12/12
 * @author 欧阳先伟
 * @since Ordinary 0.1
 */
public abstract class AbstractDataSource extends AbstractModule implements
    IManager {

  /** jdbc连接字符串 */
  protected String url;

  /** 数据库登录用户名 */
  protected String user;

  /** 数据库登录密码 */
  protected String password;

  /** 连接池中保留的最小连接数 */
  protected int minSize = 8;

  /** 连接池中保留的最大连接数 */
  protected int maxSize = 32;

  /** 用于返回的共享的连接池对象 */
  private DataSource ds;

  /**
   * 设置驱动
   * 
   * @param value
   *          驱动类的名称
   */
  public void setDriver(String value) throws Exception {
    Class.forName(value);
  }

  /**
   * 初始化连接池
   */
  public void init() throws Exception {
    ds = getDataSource();
  }

  /**
   * 获取连接池对象
   * 
   * @return 连接池对象
   * @throws Exception
   *           建立连接池失败
   */
  protected abstract DataSource getDataSource() throws Exception;

  public Object get() throws Exception {
    return ds;
  }
}