package org.lilystudio.ordinary.web.cache.je.data;

import java.io.Serializable;

/**
 * 元数据操作接口, 用于基本数据向字节数据的转换
 * 
 * @version 0.1.4, 2009/01/10
 * @author 欧阳先伟
 * @since Common 0.1
 */
public interface IMetaData extends Serializable {

  /**
   * 读取元数据的名称
   * 
   * @return 元数据的名称
   */
  String getName();

  /**
   * 读取元数据对应的字节数据
   * 
   * @return 字节数据
   */
  byte[] getBytes();
}
