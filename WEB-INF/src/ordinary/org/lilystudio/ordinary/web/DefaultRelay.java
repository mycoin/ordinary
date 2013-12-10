package org.lilystudio.ordinary.web;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 标准用户数据集合管理类, 用于保存在一个调用周期的用户信息, 初始化后内置了几个基本的变量,
 * 如SERVER表示服务器的域名, CLIENT表示客户端的地址,
 * LOCALE表示浏览器的地域信息, 变量名称支持使用.分隔的层次集合
 * 
 * @version 0.1.5, 2009/06/01
 * @author 欧阳先伟
 * @since Ordinary 0.1
 */
public class DefaultRelay implements IRelay {

  /** 当前调用对应的用户信息 */
  private UserInformation userInformation;

  /** HTTP输入对象 */
  private HttpServletRequest request;

  /** HTTP输出对象 */
  private HttpServletResponse response;

  /** 调用完成后需要转向的位置 */
  private String resultName;

  /** 当前调用对应的容器相对根目录的位置 */
  private String root = "";

  /** 响应当前调用的处理器接口 */
  private IProcess process;

  /** 数据对象 */
  private Map<String, Object> data = new HashMap<String, Object>();

  /**
   * 初始化用户信息
   * 
   * @param request
   *          HTTP输入对象
   * @param response
   *          HTTP输出对象
   */
  protected void initUserInformation(HttpServletRequest request,
      HttpServletResponse response) {
    // 设置客户端的基本属性
    // HARDCODE
    set("SERVER", request.getServerName());
    set("CLIENT", request.getRemoteAddr());
    set("USER_AGENT", request.getHeader("User-Agent"));
    set("LOCALE", request.getLocale());

    Cookie[] cookies = request.getCookies();
    if (cookies != null) {
      HashMap<String, String> map = new HashMap<String, String>();
      for (int i = 0; i < cookies.length; i++) {
        map.put(cookies[i].getName(), cookies[i].getValue());
      }
      set("COOKIE", map);
    }

    HttpSession session = request.getSession(false);
    if (session != null) {
      userInformation = (UserInformation) session
          .getAttribute(UserInformation.SESSION_KEY);
    }

    this.request = request;
    this.response = response;
  }

  public HttpServletRequest getRequest() {
    return request;
  }

  public HttpServletResponse getResponse() {
    return response;
  }

  public IProcess getProcess() {
    return process;
  }

  public void setProcess(IProcess process) {
    this.process = process;
  }

  public String getResultName() {
    String name = resultName;
    resultName = null;
    return name;
  }

  public void setResultName(String name) {
    resultName = name;
  }

  @SuppressWarnings("unchecked")
  public void init(HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    Enumeration<?> names = request.getParameterNames();
    // 初始化客户提交的数据
    while (names.hasMoreElements()) {
      String name = names.nextElement().toString();
      String[] values = request.getParameterValues(name);
      Map<String, Object> data = this.data;
      while (true) {
        int i = name.indexOf('.');
        if (i < 0) {
          break;
        }
        String parentName = name.substring(0, i);
        Object o = data.get(parentName);
        if (!(o instanceof Map)) {
          o = new HashMap<String, Object>();
          data.put(parentName, o);
        }
        data = (HashMap<String, Object>) o;
        name = name.substring(i + 1);
      }
      if (!data.containsKey(name)) {
        set(name, values == null || values.length > 1 ? values : values[0]);
      }
    }
    // 初始化Session信息
    initUserInformation(request, response);
  }

  @SuppressWarnings("unchecked")
  public UserInformation getUserInformation(boolean create) {
    if (create && (userInformation == null)) {
      userInformation = new UserInformation();
      // 初始化用户信息, 设置cookie的有效域, 从而实现session的有效访问域
      HttpSession session = request.getSession();
      session.setAttribute(UserInformation.SESSION_KEY, userInformation);
      String domain = Controller.getDomain();
      if (domain != null) {
        Cookie cookie = new Cookie("JSESSIONID", session.getId());
        cookie.setDomain(domain);
        response.addCookie(cookie);
      }
      Map<String, String> cookies = (Map<String, String>) get("COOKIE");
      if (cookies == null) {
        cookies = new HashMap<String, String>();
        set("COOKIE", cookies);
      }
      cookies.put("JSESSIONID", session.getId());
    }
    return userInformation;
  }

  public Object get(String name) {
    Map<?, ?> data = this.data;
    while (true) {
      int i = name.indexOf('.');
      if (i < 0) {
        break;
      }
      Object o = data.get(name.substring(0, i));
      if (!(o instanceof Map)) {
        return null;
      }
      data = (HashMap<?, ?>) o;
      name = name.substring(i + 1);
    }
    return data.get(name);
  }

  @SuppressWarnings("unchecked")
  public void set(String name, Object value) {
    Map<String, Object> data = this.data;
    while (true) {
      int i = name.indexOf('.');
      if (i < 0) {
        break;
      }
      String parentName = name.substring(0, i);
      Object o = data.get(parentName);
      if (o == null) {
        o = new HashMap<String, Object>();
        data.put(parentName, o);
      } else if (!(o instanceof Map)) {
        return;
      }
      data = (Map<String, Object>) o;
      name = name.substring(i + 1);
    }
    data.put(name, value);
  }

  public Map<String, Object> getDataMap() {
    return data;
  }

  public void setContextRoot(String root) {
    if (root != null) {
      this.root = root;
    }
  }

  public String getRealPath(String path) {
    if (path == null) {
      return root + get("URI");
    } else if (path.length() > 1) {
      char c = path.charAt(0);
      if (c == '/') {
        return path;
      } else if (c == '.' && path.charAt(1) == '/') {
        return root + path.substring(1);
      }
    }
    return root + get("PATH") + path;
  }
}