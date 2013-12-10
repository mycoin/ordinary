package org.lilystudio.ordinary.web;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户信息对象, 通过本对象间接的操作Session, 存放用户生存周期的信息.
 * 
 * @version 0.1.4, 2008/12/12
 * @author 欧阳先伟
 * @since Ordinary 0.1
 */
public class UserInformation {

  /** 对象在Session中的属性名 */
  public static final String SESSION_KEY = "_USERINFO";

  /** 用户名 */
  private String name;

  /** 用户的私有属性列表 */
  private Map<String, Object> properties = new HashMap<String, Object>();

  /**
   * 获取用户名
   * 
   * @return 用户名
   */
  public String getName() {
    return name;
  }

  /**
   * 设置用户名
   * 
   * @param name
   *          用户名
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * 获取一个用户属性
   * 
   * @param name
   *          属性名
   * @return 属性对象
   */
  public Object getProperty(String name) {
    return properties.get(name);
  }

  /**
   * 设置一个用户属性
   * 
   * @param name
   *          属性名
   * @param value
   *          属性对象
   */
  public void setProperty(String name, Object value) {
    properties.put(name, value);
  }

  /**
   * 取出所有的用户属性映射
   * 
   * @return 所有的用户属性
   */
  public Map<String, Object> getProperties() {
    return properties;
  }
}