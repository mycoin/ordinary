package org.lilystudio.ordinary.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * HTTP请求处理接口, 用于描述配置文件中的&lt;processes&gt;标签,
 * 框架将轮询找到HTTP的对应处理类.
 * 
 * @version 0.1.4, 2008/12/12
 * @author 欧阳先伟
 * @since Ordinary 0.1
 */
public interface IProcess extends IExecute {

  /**
   * 检测当前处理对象能否处理这个调用请求
   * 
   * @param request
   *          HTTP输入对象
   * @param response
   *          HTTP输出对象
   * @return 如果能处理请求, 将生成参数数据集合
   * @throws Exception
   *           检测过程异常
   */
  IRelay validate(HttpServletRequest request, HttpServletResponse response)
      throws Exception;
}
