package org.lilystudio.ordinary.web.cache.je;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.lilystudio.ordinary.web.IExecute;
import org.lilystudio.ordinary.web.IRelay;

/**
 * Action缓存, 它拦截正常的Action调用, 优先从缓存中读取这次操作的结果.
 * 更多的属性参见父类<br>
 * 
 * <b>子标签</b>
 * 
 * <pre>
 * action--实际的业务处理
 * used--是否启用缓存, 默认是true
 * </pre>
 * 
 * @version 0.1.4, 2009/01/10
 * @author 欧阳先伟
 * @since Ordinary 0.1
 */
public class CacheAction extends AbstractKeyDefine implements IExecute {

  /**
   * 缓存对象
   */
  private static class Cache extends AbstractCache {

    /** 序列化编号 */
    private static final long serialVersionUID = 1L;

    /** 缓存所属的action标签对象引用 */
    private transient IExecute action;

    /** 用户数据集合 */
    private transient IRelay relay;

    /**
     * 创建缓存对象
     * 
     * @param action
     *          缓存所属的action标签对象引用
     * @param relay
     *          用户数据集合
     */
    public Cache(IExecute action, IRelay relay) {
      this.action = action;
      this.relay = relay;
    }

    public void create() throws Exception {
      RelayWrapper relay = new RelayWrapper(this.relay);
      action.execute(relay);
      setData(relay.getData());
    }

    public void remove() {
    }
  }

  /** 原来的业务处理接口 */
  private IExecute action;

  /** 缓存开启标志 */
  private boolean used = true;

  @SuppressWarnings("unchecked")
  public void execute(IRelay relay) throws Exception {
    if (used) {
      Cache cache = new Cache(action, relay);
      try {
        manager.get(getKeys(relay), cache);
        for (Map.Entry<String, Object> entry : ((Map<String, Object>) cache
            .getData()).entrySet()) {
          relay.set(entry.getKey(), entry.getValue());
        }
        return;
      } catch (InvocationTargetException e) {
        // 缓存系统正常但是缓存对象无法创建时, 将实际异常抛出
        throw (Exception) e.getTargetException();
      } catch (Exception e) {
        // 其它的情况不进行缓存
      }
    }
    action.execute(relay);
  }
}
