package org.lilystudio.ordinary.web.result;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.lilystudio.ordinary.web.Controller;
import org.lilystudio.ordinary.web.IRelay;

/**
 * 重定向跳转/输出类, 使用它将页面进行重定向, 重定向可以在框架内完成,
 * 也可以要求在客户端进行. 需要注意的是在redirect方式下,
 * 以http://开头的跳转参数不能过大. <br>
 * <b>属性</b>
 * 
 * <pre>
 * type--重定向类型, download(下载),proxy(代理, 未实现),framework(服务器端跳转),location(客户端跳转)
 * encoding--当类型为proxy时, 表示编码集, 缺省为UTF-8
 * </pre>
 * 
 * @version 0.1.4, 2008/12/12
 * @author 欧阳先伟
 * @since Ordinary 0.1
 */
public class RedirectResult extends AbstractParameter {

  /** 服务器端重定向 */
  private static final int FRAMEWORK = 0;

  /** 客户端重定向 */
  private static final int LOCATION = 1;

  /** 服务器间代理跳转访问 */
  private static final int DOWNLOAD = 3;

  /** 重定向的类型 */
  private int type = FRAMEWORK;

  /** 重定向的位置 */
  private String url;
  
  /** 转换对应的编码集, 默认使用框架的编码集 */
  private String encoding = Controller.getEncoding();

  /**
   * 设置重定向类型
   * 
   * @param value
   *          配置文件中定义的参数
   */
  public void setType(String value) throws Exception {
    // HARDCODE
    if (value.equals("framework")) {
      type = FRAMEWORK;
    } else if (value.equals("location")) {
      type = LOCATION;
    } else if (value.equals("download")) {
      type = DOWNLOAD;
    } else {
      throw new Exception("The type don't support");
    }
  }

  public void execute(HttpServletRequest request, HttpServletResponse response,
      IRelay relay) throws Exception {
    String url = getURI(relay, this.url, encoding);
    switch (type) {
    case FRAMEWORK:
      setParameters(relay);
      request.setAttribute(IRelay.ATTRIBUTE_KEY, relay);
      request.getRequestDispatcher(url).forward(request, response);
      break;
    case LOCATION:
      if (url.startsWith("http://")) {
        // 跳转到其它网站, 附加参数需要在URL中传递
        url = addParameter(relay, url, encoding);
      } else {
        setParameters(relay);
        request.getSession().setAttribute(IRelay.ATTRIBUTE_KEY, relay);
      }
      response.sendRedirect(url);
      break;
    case DOWNLOAD:
      Controller.setUndoFilter(request);
      request.getRequestDispatcher(url).forward(request, response);
      break;
    default:
    }
  }
}