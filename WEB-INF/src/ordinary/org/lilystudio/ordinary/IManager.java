package org.lilystudio.ordinary;

/**
 * 管理器接口, 框架内使用的对象全部由管理器生成.
 * 
 * @version 0.1.4, 2008/12/12
 * @author 欧阳先伟
 * @since Ordinary 0.1
 */
public interface IManager {

  /**
   * 从管理器中取出被管理的对象
   * 
   * @return 由当前管理器管理的对象
   * @throws Exception
   *           如果对象池已经被锁定或其它原因不能取出对象
   */
  Object get() throws Exception;
}