package org.lilystudio.ordinary.web;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * host标签处理类, 用于匹配域名是否满足条件, 它将在匹配成功时向数据容器中添加HOST值,
 * 表示当前被匹配的域名, 如果域名正则表达式包含聚合表达式, 则按顺序生成HOST*等值,
 * 有关生成的规则参见Matcher.group函数.
 * 在某些情况下需要对一些command或者mapping分组处理filter或者capture标签时
 * , 也可以通过省写name的方式实现, 它与name=".*"最大的区别在于,
 * 省写name时, 如果内部没有匹配请求, 整个标签就没有被匹配,
 * validate的返回结果是null. 如果设置了name=".*", 即使内部没有匹配请求,
 * 也会返回整体被匹配并请求下载指定路径的文件<br>
 * &lt;host&gt;<br>
 * ...<br>
 * &lt;/host&gt;<br>
 * <b>属性</b>
 * 
 * <pre>
 * name--表示HOST匹配的正则表达式
 * root--域名相对容器的根路径
 * </pre>
 * 
 * <b>子标签</b>
 * 
 * <pre>
 * filter--指定数据过滤器
 * mapping--指定快速URL映射
 * command--指定URL正则表达式关系
 * capture--指定需要拦截的异常并处理
 * </pre>
 * 
 * @see org.lilystudio.ordinary.web.Capture
 * @see org.lilystudio.ordinary.web.Command
 * @see org.lilystudio.ordinary.web.IExecute
 * @see org.lilystudio.ordinary.web.Mapping
 * 
 * @version 0.1.4, 2008/12/12
 * @author 欧阳先伟
 * @since Ordinary 0.1
 */
public class Host implements IProcess {

  /** REQUEST对象的属性名, 在request.setAttribute时使用 */
  private static final String ATTRIBUTE_KEY = "_HOST";

  /** 域匹配正则表达式 */
  private Pattern name;

  /** 域的相对根路径 */
  private String root = null;

  /** 域的公共过滤器列表 */
  private List<IExecute> filter;

  /** 路径处理模块列表, 用于完整的路径正则表达式匹配 */
  private List<IProcess> processes = new ArrayList<IProcess>();

  /** 域的公共异常捕获器列表 */
  private List<Capture> capture;

  /**
   * 设置一个强制使用的域名, 用于框架的内部跳转流程控制,
   * 例如需要将一些域名转换成其它的域名用于框架内部的处理
   * 
   * @param request
   *          HTTP输入对象
   * @param host
   *          新的域名
   */
  public static void setHost(HttpServletRequest request, String host) {
    request.setAttribute(ATTRIBUTE_KEY, host);
  }

  /**
   * 设置域的正则表达式
   * 
   * @param value
   *          正则表达式文本
   */
  public void setName(String value) {
    this.name = Pattern.compile("^" + value + "$");
  }

  /**
   * 添加一个Command子标签对象
   * 
   * @param o
   *          子标签对象
   */
  public void addCommand(Object o) {
    processes.add((IProcess) o);
  }

  /**
   * 添加一个Mapping子标签对象
   * 
   * @param o
   *          子标签对象
   */
  public void addMapping(Object o) {
    processes.add((IProcess) o);
  }

  /**
   * 获取当前请求中的域名信息
   * 
   * @param request
   *          HTTP输入对象
   * @return 当前请求中的域名信息
   */
  public String getHost(HttpServletRequest request) {
    // 从环境中读取是否有强制设定要求使用的域名
    String host = (String) request.getAttribute(ATTRIBUTE_KEY);
    if (host == null) {
      return request.getServerName();
    }
    return host;
  }

  public IRelay validate(HttpServletRequest request,
      HttpServletResponse response) throws Exception {
    if (name == null) {
      // 循环检测URL名称是否满足条件
      for (IProcess o : processes) {
        IRelay relay = o.validate(request, response);
        if (relay != null) {
          // HARDCODE
          relay.set("HOST", getHost(request));
          relay.setContextRoot(root);
          return relay;
        }
      }
    } else {
      // 检测域名是否满足匹配条件
      Matcher m = name.matcher(getHost(request));
      if (m.find()) {
        // 检测域名内是否有URL处理类满足匹配条件
        IRelay relay;
        // 循环检测URL名称是否满足条件
        for (IProcess o : processes) {
          relay = o.validate(request, response);
          if (relay != null) {
            // 初始化域名信息, 根据正则表达式分组依次填充HOST1,HOST2,
            // HOST3等
            // HARDCODE
            for (int i = m.groupCount(); i > 0; i--) {
              relay.set("HOST" + i, m.group(i));
            }
            relay.set("HOST", m.group());
            relay.setContextRoot(root);
            return relay;
          }
        }
        // 没有找到, 返回特殊的空数据包
        relay = new DownloadRelay();
        // 设置域名的根路径
        relay.setContextRoot(root);
        return relay;
      }
    }
    return null;
  }

  public void execute(IRelay relay) throws Exception {
    try {
      IProcess process = relay.getProcess();
      // 调用过滤器过滤客户提交的数据
      if (filter != null) {
        for (IExecute o : filter) {
          o.execute(relay);
        }
      }
      // 开始处理请求
      process.execute(relay);
    } catch (Exception e) {
      Capture.process(relay, e, capture);
    }
  }
}