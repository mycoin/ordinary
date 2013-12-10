package org.lilystudio.ordinary.web.cache.je;

/**
 * 缓存操作基类, 用于提供缓存对象的基本操作方式<br>
 * 
 * @version 0.1.4, 2009/01/10
 * @author 欧阳先伟
 * @since Ordinary 0.1
 */
public abstract class AbstractCache implements ICache {

  /** 序列化编号 */
  private static final long serialVersionUID = 1L;

  /** 缓存数据 */
  private Object data;
  
  public Object getData() {
    return data;
  }

  public void setData(Object data) {
    this.data = data;
  }
}
