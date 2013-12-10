package org.lilystudio.httpclient;

import java.io.IOException;
import java.io.OutputStream;

/**
 * HTTP的提交操作描述接口
 * 
 * @version 0.1.4, 2009/01/01
 * @author 欧阳先伟
 * @since Common 0.1
 */
public interface IMethod {

  /**
   * 提交方式的名称, 一般是GET或者POST
   * 
   * @return 提交方式的名称
   */
  byte[] getName();

  /**
   * 获取操作的域名, 即需要连接的HTTP服务器域名
   * 
   * @return 操作域名
   */
  String getHost();

  /**
   * 获取操作的路径, 即实际需要访问的HTTP服务器文件路径
   * 
   * @return 操作路径
   */
  String getPath();

  /**
   * 设置请求数据包的头信息
   * 
   * @param name
   *          头信息名称
   * @param value
   *          头信息值
   */
  void setRequestHeader(String name, String value);

  /**
   * 向HTTP服务器提交请求
   * 
   * @param out
   *          向HTTP服务器进行输出的数据流
   * @throws IOException
   *           如果流操作失败
   */
  void execute(OutputStream out) throws IOException;
}
