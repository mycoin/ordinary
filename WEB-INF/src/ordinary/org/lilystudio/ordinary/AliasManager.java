package org.lilystudio.ordinary;

/**
 * 别名管理器, 通过它访问另一个管理器, 相当于给另一个管理器取了一个额外的名称.
 * 
 * @version 0.1.4, 2008/12/12
 * @author 欧阳先伟
 * @since Ordinary 0.1
 */
public class AliasManager implements IManager {

  /** 管理器容器, 保存所有的管理器, 需要的对象均由容器中相应的管理器产生 */
  private ManagerContext context;

  /** 目标管理器的名称 */
  private String name;

  /**
   * 创建别名管理器
   * 
   * @param context
   *          管理器容器
   * @param name
   *          目标管理器的名称
   */
  public AliasManager(ManagerContext context, String name) {
    this.context = context;
    this.name = name;
  }

  public Object get() throws Exception {
    return context.get(name);
  }
}