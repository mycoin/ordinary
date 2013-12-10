package org.lilystudio.ordinary.module;

import javax.sql.DataSource;

import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * C3P0连接池对象, 更多的属性参见父类, 需要包含第三方的c3p0包
 * 
 * <b>示例</b>
 * 
 * <pre>
 * &lt;module name=&quot;test&quot;
 *   class=&quot;org.lilystudio.ordinary.module.C3P0DataSource&quot;
 *   driver=&quot;org.gjt.mm.mysql.Driver&quot;
 *   user=&quot;root&quot; password=&quot;111111&quot;
 *   url=&quot;jdbc:mysql://localhost:3306/test&quot; size=&quot;20&quot; /&gt;
 * </pre>
 * 
 * @version 0.1.4, 2008/12/12
 * @author 欧阳先伟
 * @since Ordinary 0.1
 */
public class C3P0DataSource extends AbstractDataSource {

  /** 内部保存的连接池引用, 用于释放资源 */
  private ComboPooledDataSource ds;

  @Override
  protected DataSource getDataSource() throws Exception {
    ds = new ComboPooledDataSource();
    // 当连接池中的连接耗尽的时候c3p0一次同时获取的连接数
    ds.setAcquireIncrement(5);
    // 如果连接失败重试连接的间隔时间
    ds.setAcquireRetryDelay(60);
    // 初始化时获取的连接数
    ds.setInitialPoolSize(minSize);
    // 连接池中保留的最小连接数
    ds.setMinPoolSize(minSize);
    // 连接池中保留的最大连接数
    ds.setMaxPoolSize(maxSize);
    // 最大空亲时间, 30秒内未使用则连接被释放
    ds.setMaxIdleTime(30);
    ds.setJdbcUrl(url);
    ds.setUser(user);
    ds.setPassword(password);
    // 每1800秒检查所有连接池中的空闲连接
    ds.setIdleConnectionTestPeriod(1800);
    return ds;
  }

  @Override
  protected void finalize() throws Throwable {
    ds.close();
  }
}
