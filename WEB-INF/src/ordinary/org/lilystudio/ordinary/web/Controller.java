package org.lilystudio.ordinary.web;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.lilystudio.ordinary.ManagerContext;
import org.lilystudio.util.StringWriter;
import org.w3c.dom.Node;

/**
 * 框架控制器, 需要在web.xml中配置<filter>相关标签, 示例如下:
 * 
 * <pre>
 *  &lt;filter&gt;
 *   &lt;filter-name&gt;Control&lt;/filter-name&gt;
 *   &lt;filter-class&gt;org.lilystudio.ordinary.Controller&lt;/filter-class&gt;
 *   &lt;init-param&gt;
 *     &lt;param-name&gt;mode&lt;/param-name&gt;
 *     &lt;param-value&gt;debug&lt;/param-value&gt;
 *   &lt;/init-param&gt;
 *   &lt;init-param&gt;
 *     &lt;param-name&gt;encoding&lt;/param-name&gt;
 *     &lt;param-value&gt;gbk&lt;/param-value&gt;
 *   &lt;/init-param&gt;
 * &lt;/filter&gt;
 * 
 * &lt;filter-mapping&gt;
 *  &lt;filter-name&gt;Control&lt;/filter-name&gt;
 * &lt;url-pattern&gt;/*&lt;/url-pattern&gt;
 *   &lt;dispatcher&gt;REQUEST&lt;/dispatcher&gt;
 *   &lt;dispatcher&gt;FORWARD&lt;/dispatcher&gt;
 * &lt;/filter-mapping&gt;
 * </pre>
 * 
 * @version 0.1.4, 2008/12/12
 * @author 欧阳先伟
 * @since Ordinary 0.1
 */
public class Controller extends ManagerContext implements Filter {

  /** 框架回调时不经过控制器处理而直接下载的关键字名称 */
  private static final String UNDO_FILTER_KEY = "_UNDO_FILTER";

  /** 系统编码配置名称 */
  private static String INIT_KEY_ENCODING = "encoding";

  /** Session范围的名称 */
  private static String INIT_KEY_DOMAIN = "domain";

  /** 系统调试模式配置名称 */
  private static String INIT_KEY_MODE = "mode";

  /** 系统配置文件名 */
  private static String CONFIGURE_FILENAME = "WEB-INF/ordinary-config.xml";

  /** Servlet环境容器 */
  private static ServletContext context;

  /** Servlet环境的系统根目录位置 */
  private static String path;

  /** 框架使用的编码集 */
  private static String encoding;

  /** 框架的session作用域范围 */
  private static String domain;

  /** 框架配置文件路径 */
  private File configure;

  /** HTTP处理接口列表 */
  private List<IProcess> processes = new ArrayList<IProcess>();

  /**
   * 设置系统内部跳转时为不需要框架处理而直接下载对应的链接文件
   * 
   * @param request
   *          HTTP输入对象
   */
  public static void setUndoFilter(ServletRequest request) {
    request.setAttribute(UNDO_FILTER_KEY, Boolean.TRUE);
  }

  /**
   * 获得Servlet应用的容器对象
   * 
   * @return Servlet容器对象
   */
  public static ServletContext getContext() {
    return context;
  }

  /**
   * 获得Servlet应用的根目录位置
   * 
   * @return 应用的根目录位置
   */
  public static String getContextPath() {
    return path;
  }

  /**
   * 取出框架使用的编码集
   * 
   * @return 编码集的名称
   */
  public static String getEncoding() {
    return encoding;
  }

  /**
   * 取出框架使用的域名名称
   * 
   * @return 域名的名称
   */
  public static String getDomain() {
    return domain;
  }

  /**
   * 处理一个URL请求
   * 
   * @param request
   *          URL请求输入对象
   * @param response
   *          URL请求输出对象
   * @param chain
   *          J2EE过滤器链
   * @throws Exception
   *           处理过程中的异常
   * @see com.veeview.smarty4j.ordinary.IProcess
   */
  private void process(HttpServletRequest request,
      HttpServletResponse response, FilterChain chain) throws Exception {
    for (IProcess o : processes) {
      // 逐步检测满足条件的process子节点
      IRelay relay = o.validate(request, response);
      if (relay != null) {
        // 改进了回调处理的方式, 提高速度
        if (relay instanceof DownloadRelay) {
          String path = relay.getRealPath(request.getRequestURI().substring(
              request.getContextPath().length()));
          if (path == null) {
            chain.doFilter(request, response);
          } else {
            setUndoFilter(request);
            request.getRequestDispatcher(path).forward(request, response);
          }
        } else {
          o.execute(relay);
        }
        return;
      }
    }
    chain.doFilter(request, response);
  }

  public void init(FilterConfig filterConfig) throws ServletException {
    context = filterConfig.getServletContext();

    // 准备化配置文件路径名
    path = context.getRealPath("/").replace('\\', '/');
    path = !path.endsWith("/") ? path + '/' : path;

    encoding = filterConfig.getInitParameter(INIT_KEY_ENCODING);
    if (encoding == null) {
      encoding = "UTF-8";
    }

    domain = filterConfig.getInitParameter(INIT_KEY_DOMAIN);

    // 如果是调试模式, cfgPath将被设置并在每次调用时检查更新情况,
    // 否则第一次加载完成后将不再检查
    if ("debug".equals(filterConfig.getInitParameter(INIT_KEY_MODE))) {
      configure = new File(path + CONFIGURE_FILENAME);
    } else {
      try {
        File file = new File(path + CONFIGURE_FILENAME);
        load(file);
      } catch (Throwable e) {
        context.log("", e);
        ServletException servletException = new ServletException(e.getMessage());
        servletException.setStackTrace(e.getStackTrace());
        throw servletException;
      }
    }
  }

  public void doFilter(ServletRequest request, ServletResponse response,
      FilterChain chain) throws ServletException, IOException {
    try {
      // 检查是否不需要调用控制器, 用于跳转后直接下载文件的重定向,
      // 框架将直接向J2EE核心提交数据
      if (request.getAttribute(UNDO_FILTER_KEY) != null) {
        request.removeAttribute(UNDO_FILTER_KEY);
        chain.doFilter(request, response);
      } else {
        request.setCharacterEncoding(encoding);
        // 首先检查配置文件是否被更新, 如果被更新需要重新加载文件
        if (configure != null) {
          if (isUpdated()) {
            synchronized (this) {
              // 两次判断避免重复加载
              if (isUpdated()) {
                load(configure);
              }
            }
          }
        }
        process((HttpServletRequest) request, (HttpServletResponse) response,
            chain);
      }
    } catch (Throwable e) {
      e.printStackTrace();
      ((HttpServletResponse) response).setCharacterEncoding(encoding);
      StringWriter buf = new StringWriter();
      PrintWriter writer = new PrintWriter(buf);
      e.printStackTrace(writer);
      String text = buf.toString();
      int len = text.length();
      StringBuilder sb = new StringBuilder(512);
      sb.append("<html><head><meta http-equiv='Content-Type' content='text/html;charset=utf-8'></head><body>");
      for (int i = 0; i < len; i++) {
        char c = text.charAt(i);
        switch (c) {
        case '\n':
          sb.append("<br />");
          continue;
        case '\r':
          continue;
        case '<':
          sb.append("&lt;");
          continue;
        case '>':
          sb.append("&gt;");
          continue;
        default:
          sb.append(c);
        }
      }
      sb.append("</body></html>");
      response.getWriter().append(sb);
      context.log("", e);
    }
  }

  public void destroy() {
  }

  @Override
  public Node load(File file) throws Throwable {
    Node root = super.load(file);

    // 释放所有被占用的资源
    for (Object process : processes) {
      ManagerContext.release(process);
    }
    processes.clear();

    // 初始化<process>标签处理器列表, 供process方法循环查询使用
    Node parent = getChild(root, "process");
    if (parent != null) {
      Iterator<Node> iterator = super.getElementIterator(parent);
      while (iterator.hasNext()) {
        try {
          processes.add((IProcess) get(iterator.next()));
        } catch (Throwable e) {
          reset();
          throw e;
        }
      }
    }

    return root;
  }
}
