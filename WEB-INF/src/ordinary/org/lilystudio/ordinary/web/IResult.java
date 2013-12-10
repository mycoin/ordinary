package org.lilystudio.ordinary.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 结果数据操作接口, 对应command中的result标签的处理
 * 
 * @see org.lilystudio.ordinary.web.Command
 * 
 * @version 0.1.4, 2008/12/12
 * @author 欧阳先伟
 * @since Ordinary 0.1
 */
public interface IResult {

  /**
   * 获取名称
   * 
   * @return 对象名称
   */
  String getName();

  /**
   * 结果数据处理
   * 
   * @param request
   *          HTTP输入对象
   * @param response
   *          HTTP输出对象
   * @param relay
   *          用户数据容器
   * @throws Exception
   *           结果处理异常
   */
  void execute(HttpServletRequest request, HttpServletResponse response,
      IRelay relay) throws Exception;
}