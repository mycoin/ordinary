package org.lilystudio.ordinary.web.cache.je;

import org.lilystudio.ordinary.web.IExecute;
import org.lilystudio.ordinary.web.IRelay;

/**
 * 清除缓存操作, 更多的属性参见父类
 * 
 * <b>属性</b>
 * 
 * <pre>
 * manager--缓存管理器模块名称
 * names--需要清除的关键字名称组, 此关键字所有的缓存都将被清空, 用,号分隔
 * </pre>
 * 
 * @version 0.1.4, 2009/01/10
 * @author 欧阳先伟
 * @since Ordinary 0.1
 */
public class ClearAction extends AbstractKeyDefine implements IExecute {

  /** 需要清空的关键字名称 */
  private String[] names;

  /**
   * 设置需要清除的关键字列表
   * 
   * @param value
   *          配置文件中定义的关键字列表
   */
  public void setNames(String value) {
    names = value.split(",");
  }

  @Override
  public void init() throws Exception {
    if (key != null) {
      super.init();
    }
  }
  
  public void execute(IRelay relay) throws Exception {
    if (names != null) {
      for (String name : names) {
        manager.clear(name);
      }
    }
    if (key != null) {
      manager.clear(getKeys(relay));
    }
  }
}
