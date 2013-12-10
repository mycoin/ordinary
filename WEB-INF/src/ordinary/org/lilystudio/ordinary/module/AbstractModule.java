package org.lilystudio.ordinary.module;

import org.lilystudio.ordinary.ManagerContext;
import org.w3c.dom.Node;

/**
 * 需要保存在类管理池中的模块基础实现类, 全局模块需要保存在类管理池.
 * 如果模块对象基于类管理器接口, 则作为类管理器加载, 否则, 作为共享对象加载.
 * 
 * <b>属性</b>
 * 
 * <pre>
 * name--管理器容器中使用的全局名称
 * </pre>
 * 
 * @version 0.1.4, 2008/12/12
 * @author 欧阳先伟
 * @since Ordinary 0.1
 */
public abstract class AbstractModule {

  /** 对象名称 */
  private String name;

  /**
   * 初始化对象
   * 
   * @param context
   *          管理器容器
   * @param node
   *          节点对象
   * @throws Exception
   *           如果初始化失败
   */
  public void init(ManagerContext context, Node node) throws Exception {
    context.register(name, this);
  }
}
