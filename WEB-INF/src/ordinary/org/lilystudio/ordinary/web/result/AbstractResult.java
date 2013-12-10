package org.lilystudio.ordinary.web.result;

import org.lilystudio.ordinary.web.IResult;

/**
 * 结果处理实现基类, 保存了结果处理的名称<br>
 * 
 * <b>属性</b>
 * 
 * <pre>
 * name--结果处理的名称
 * </pre>
 * 
 * @see org.lilystudio.ordinary.web.IResult
 * 
 * @version 0.1.4, 2009/01/10
 * @author 欧阳先伟
 * @since Ordinary 0.1
 */
public abstract class AbstractResult implements IResult {

  /** 对象名称, 通过Relay对象中设置的setResultName, 来查找相关的对象 */
  private String name;

  public String getName() {
    return name;
  }
}