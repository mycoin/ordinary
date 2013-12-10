package org.lilystudio.ordinary.web.cache.je;

import java.io.Serializable;

/**
 * 缓存操作接口, 用于定义缓存对象的操作行为
 * 
 * @version 0.1.4, 2009/01/10
 * @author 欧阳先伟
 * @since Common 0.1
 */
public interface ICache extends Serializable {

  /**
   * 读取缓存操作接口中的数据
   * 
   * @return 缓存的数据
   */
  Object getData();

  /**
   * 将数据写入缓存操作接口
   * 
   * @param data
   *          缓存的数据
   */
  void setData(Object data);

  /**
   * 创建缓存对象
   * 
   * @throws Exception
   *           数据操作异常
   */
  void create() throws Exception;

  /**
   * 清除缓存对象
   */
  void remove();
}
