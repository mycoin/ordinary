package org.lilystudio.ordinary.web;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * capture标签处理类, 用于将捕获到的异常交给指定的result子标签处理. <br>
 * <b>属性</b>
 * 
 * <pre>
 * handle--异常类名称, 将保存在数据集合的HANDLE键值中
 * status--异常页返回状态号, 默认为200, 即HTTP OK
 * message--额外增加的附加消息文本, 将保存在数据集合的MESSAGE键值中
 * console--是否同步输出异常信息到控制台
 * </pre>
 * 
 * <b>子标签</b>
 * 
 * <pre>
 * result--指定处理的转向
 * </pre>
 * 
 * @see org.lilystudio.ordinary.web.IResult
 * 
 * @version 0.1.4, 2008/12/12
 * @author 欧阳先伟
 * @since Ordinary 0.1
 */
public class Capture {

  /**
   * 循环搜索满足条件的异常捕获器
   * 
   * @param relay
   *          用户数据接口
   * @param e
   *          异常对象体
   * @param captures
   *          捕获器列表
   * @throws Exception
   *           不能处理的异常将进一步抛出
   */
  public static void process(IRelay relay, Exception e, List<Capture> captures)
      throws Exception {
    if (captures != null) {
      Class<?> c = e.getClass();
      for (Capture o : captures) {
        if (o.handle.isAssignableFrom(c)) {
          // 将错误信息设置到两个特定的变量MESSAGE与HANDLE中
          HttpServletRequest request = relay.getRequest();
          HttpServletResponse response = relay.getResponse();
          response.setStatus(o.status);
          // HARDCODE
          relay.set("MESSAGE", o.message);
          relay.set("HANDLE", e);
          o.result.execute(request, response, relay);
          if (o.console) {
            e.printStackTrace();
          }
          return;
        }
      }
    }
    throw e;
  }

  /** 当前捕获器的异常对象基类 */
  private Class<?> handle;

  /** 产生异常时需要返回到客户端的服务器状态号, 默认是200即HTTP OK */
  private int status = 200;

  /** 产生异常时需要附加到数据集中的信息文本 */
  private String message;

  /** 是否同步输出到控制台 */
  private boolean console;

  /** 产生异常后的转向处理对象 */
  private IResult result;

  /**
   * 设置异常对象基类
   * 
   * @param name
   *          异常对象基类的名称
   * @throws Exception
   *           对象不存在
   */
  public void setHandle(String name) throws Exception {
    this.handle = Class.forName(name);
  }
}