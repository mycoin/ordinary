package org.lilystudio.ordinary.web.cache.je;

import java.util.Collections;
import java.util.List;

import org.lilystudio.ordinary.web.IRelay;
import org.lilystudio.ordinary.web.cache.je.data.BooleanData;
import org.lilystudio.ordinary.web.cache.je.data.IMetaData;
import org.lilystudio.ordinary.web.cache.je.data.IntData;
import org.lilystudio.ordinary.web.cache.je.data.LongData;
import org.lilystudio.ordinary.web.cache.je.data.NullData;
import org.lilystudio.ordinary.web.cache.je.data.StringData;

/**
 * 缓存定义控件基类, 用于提供关键字和缓存管理器的基本控制<br>
 * 
 * <b>属性</b>
 * 
 * <pre>
 * manager--缓存管理模块的名称
 * </pre>
 * 
 * <b>子标签</b>
 * 
 * <pre>
 * key--与当前缓存相关的关键字名称信息
 * </pre>
 * 
 * @version 0.1.4, 2009/01/10
 * @author 欧阳先伟
 * @since Ordinary 0.1
 */
public abstract class AbstractKeyDefine {

  /** 缓存模块 */
  protected CacheManager manager;

  /** 关键字信息描述列表 */
  protected List<Key> key;

  /**
   * 缓存控件初始化, 需要对缓存的关键字排序
   * 
   * @throws Exception
   *           初始化格式错误
   */
  public void init() throws Exception {
    if (key == null) {
      // HARDCODE
      throw new Exception("The element must contain one key element at least");
    }
    Collections.sort(key, manager.comparator);
  }

  /**
   * 建立关键字信息描述对象
   * 
   * @return 关键字信息描述对象
   */
  public Object createKey() {
    return new Key();
  }

  /**
   * 合并生成缓存关键字列表数据
   * 
   * @param relay
   *          用户数据集合
   * @return 关键字列表
   * @throws DataTypeException
   *           没有关键字数据或者数据类型错误
   */
  public IMetaData[] getKeys(IRelay relay) throws DataTypeException {
    IMetaData[] data = new IMetaData[key.size()];
    for (int i = key.size() - 1; i >= 0; i--) {
      Key key = this.key.get(i);
      String name = key.getName();
      String value = key.getValue();
      if (value != null) {
        data[i] = new StringData(name, value);
      } else {
        Object o = relay.get(name);
        if (o == null) {
          data[i] = new NullData(name);
        } else {
          Class<?> c = o.getClass();
          if (c == Boolean.class) {
            data[i] = new BooleanData(name, (Boolean) o);
          } else if (c == Integer.class) {
            data[i] = new IntData(name, (Integer) o);
          } else if (c == Long.class) {
            data[i] = new LongData(name, (Long) o);
          } else if (c == String.class) {
            data[i] = new StringData(name, (String) o);
          } else {
            // HARDCODE
            throw new DataTypeException("Only boolean, integer, long and string are allowed for keyword");
          }
        }
      }
    }
    return data;
  }
}
