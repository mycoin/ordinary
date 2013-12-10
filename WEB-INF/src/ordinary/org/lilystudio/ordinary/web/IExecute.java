package org.lilystudio.ordinary.web;

import org.lilystudio.ordinary.web.IRelay;

/**
 * 数据集合处理接口, 对应filter标签以及command标签下的action标签,
 * 如果它不存在, 表示不需要处理数据
 * 
 * @version 0.1.4, 2008/12/12
 * @author 欧阳先伟
 * @since Ordinary 0.1
 */
public interface IExecute {

  /**
   * 处理提交的数据
   * 
   * @param relay
   *          数据集合
   * @throws Exception
   *           处理过程中产生的异常
   */
  void execute(IRelay relay) throws Exception;
}
