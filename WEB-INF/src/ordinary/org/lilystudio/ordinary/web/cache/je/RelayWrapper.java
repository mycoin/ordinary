package org.lilystudio.ordinary.web.cache.je;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.lilystudio.ordinary.web.IProcess;
import org.lilystudio.ordinary.web.IRelay;
import org.lilystudio.ordinary.web.UserInformation;

/**
 * 用户数据集合包装类, 用于记录当前调用改变了用户数据集合中的值
 * 
 * @version 0.1.4, 2009/01/10
 * @author 欧阳先伟
 * @since Ordinary 0.1
 */
public class RelayWrapper implements IRelay {

  /**
   * 映射数据集合包装类
   */
  private class MapWrapper implements Map<String, Object> {

    /** 序列化编号 */
    private static final long serialVersionUID = 1L;

    /** 被包装的数据集合 */
    private Map<String, Object> map;

    /**
     * 创建数据集合包装类
     * 
     * @param map
     */
    private MapWrapper(Map<String, Object> map) {
      this.map = map;
    }

    public void clear() {
      map.clear();
    }

    public boolean containsKey(Object key) {
      return map.containsKey(key);
    }

    public boolean containsValue(Object value) {
      return map.containsValue(value);
    }

    public Set<Map.Entry<String, Object>> entrySet() {
      return map.entrySet();
    }

    public Object get(Object key) {
      return map.get(key);
    }

    public boolean isEmpty() {
      return map.isEmpty();
    }

    public Set<String> keySet() {
      return map.keySet();
    }

    public Object put(String key, Object value) {
      data.put(key, value);
      return map.put(key, value);
    }

    public void putAll(Map<? extends String, ? extends Object> m) {
      data.putAll(m);
      map.putAll(m);
    }

    public Object remove(Object key) {
      return map.remove(key);
    }

    public int size() {
      return map.size();
    }

    public Collection<Object> values() {
      return map.values();
    }

  }

  /** 被包装的用户数据集合 */
  private IRelay relay;

  /** 被改变的变量 */
  private Map<String, Object> data = new HashMap<String, Object>();

  /**
   * 创建用户数据集合包装类
   * 
   * @param relay
   *          用户数据集合
   */
  public RelayWrapper(IRelay relay) {
    this.relay = relay;
  }

  /** 获取被改变的变量 */
  public Map<String, Object> getData() {
    return data;
  }

  public Object get(String name) {
    return relay.get(name);
  }

  public Map<String, Object> getDataMap() {
    return new MapWrapper(relay.getDataMap());
  }

  public IProcess getProcess() {
    return relay.getProcess();
  }

  public String getRealPath(String path) {
    return relay.getRealPath(path);
  }

  public HttpServletRequest getRequest() {
    return relay.getRequest();
  }

  public HttpServletResponse getResponse() {
    return relay.getResponse();
  }

  public String getResultName() {
    return relay.getResultName();
  }

  public UserInformation getUserInformation(boolean create) {
    return relay.getUserInformation(create);
  }

  public void init(HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    relay.init(request, response);
  }

  public void set(String name, Object value) {
    data.put(name, value);
    relay.set(name, value);
  }

  public void setContextRoot(String root) {
    relay.setContextRoot(root);
  }

  public void setProcess(IProcess process) {
    relay.setProcess(process);
  }

  public void setResultName(String name) {
    relay.setResultName(name);
  }
}
