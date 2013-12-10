package org.lilystudio.ordinary.web;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 空环境数据集合, 仅设定新的根目录位置, IProcess中需要立刻下载时返回这个对象,
 * 框架将直接产生回调.
 * 
 * @version 0.1.4, 2008/12/12
 * @author 欧阳先伟
 * @since Ordinary 0.1
 */
public class DownloadRelay implements IRelay {

  /** 相对根路径 */
  private String root;

  public void init(HttpServletRequest request, HttpServletResponse response)
      throws Exception {
  }

  public HttpServletRequest getRequest() {
    return null;
  }

  public HttpServletResponse getResponse() {
    return null;
  }

  public IProcess getProcess() {
    return null;
  }

  public void setProcess(IProcess process) {
  }

  public String getResultName() {
    return null;
  }

  public void setResultName(String name) {
  }

  public UserInformation getUserInformation(boolean create) {
    return null;
  }

  public Object get(String name) {
    return null;
  }

  public void set(String name, Object value) {
  }

  public Map<String, Object> getDataMap() {
    return null;
  }

  public void setContextRoot(String root) {
    this.root = root;
  }

  public String getRealPath(String path) {
    return root != null ? root + path : null;
  }
}
