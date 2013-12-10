package org.lilystudio.ordinary.web;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 环境集合操作接口
 * 
 * @version 0.1.4, 2008/12/12
 * @author 欧阳先伟
 * @since Ordinary 0.1
 */
public interface IRelay {

  /** REQUEST对象的属性名, 在request.setAttribute时使用 */
  String ATTRIBUTE_KEY = "_RELAY";

  /**
   * 初始化数据
   * 
   * @param request
   *          HTTP输入对象
   * @param response
   *          HTTP输出对象
   * @throws Exception
   */
  void init(HttpServletRequest request, HttpServletResponse response)
      throws Exception;

  /**
   * 获取HTTP输入对象
   * 
   * @return HTTP输入对象
   */
  HttpServletRequest getRequest();

  /**
   * 获取HTTP输出对象
   * 
   * @return HTTP输出对象
   */
  HttpServletResponse getResponse();

  /**
   * 获取当前调用的处理器对象
   * 
   * @return 处理器对象实例
   */
  IProcess getProcess();

  /**
   * 设置当前调用的处理器对象
   * 
   * @param process
   *          处理器对象实例
   */
  void setProcess(IProcess process);

  /**
   * 获得Forward名称, 调用一次后将自动被重置
   * 
   * @return Forward名称
   * @see org.lilystudio.ordinary.standard.IForward
   */
  String getResultName();

  /**
   * 设置Forward名称, 指定处理完成后应该转向的位置
   * 
   * @param name
   *          Forward名称
   * @see org.lilystudio.ordinary.standard.IForward
   */
  void setResultName(String name);

  /**
   * 获取用户信息对象
   * 
   * @param create
   *          如果不存在是否新建
   * @return 用户信息对象
   */
  UserInformation getUserInformation(boolean create);

  /**
   * 获取指定的数据
   * 
   * @param name
   *          数据名
   * @return 数据对象
   */
  Object get(String name);

  /**
   * 设置数据映射
   * 
   * @param name
   *          数据名
   * @param value
   *          数据对象
   */
  void set(String name, Object value);

  /**
   * 获取全部的数据映射
   * 
   * @return 数据映射
   */
  Map<String, Object> getDataMap();

  /**
   * 设置当前调用的环境根路径
   * 
   * @param root
   *          环境根路径
   */
  void setContextRoot(String root);

  /**
   * 根据相对路径计算真实路径名称
   * 
   * @param path
   *          相对路径名称, 如果以/开头, 以当前J2EE容器路径为根路径进行扩展,
   *          如果以 ./开头, 以Relay中设置的环境路径为根路径进行扩展,
   *          其它的情况,
   *          相对路径以(当前环境设置的路径+URI路径)为根路径进行扩展.
   * @return 真实路径名称
   */
  String getRealPath(String path);
}