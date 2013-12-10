package org.lilystudio.ordinary.web;

import java.lang.reflect.Method;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * command标签处理类, 用于匹配URI是否满足条件,
 * 在relay中将会设置RESULT用于识别进入的哪个result. <br>
 * <b>属性</b>
 * 
 * <pre>
 * name--缺省下表示URI匹配的正则表达式, 如果是用于mapping标签内, 表示完全的匹配
 * relay--表示使用的数据集合类名称, 默认情况下使用DefaultRelay对象
 * </pre>
 * 
 * <b>子标签</b>
 * 
 * <pre>
 * filter--指定数据集合过滤器
 * action--指定数据集合处理类
 * result--指定处理完成后的合法转向列表
 * capture--指定发生异常时的处理
 * </pre>
 * 
 * @see org.lilystudio.ordinary.web.Capture
 * @see org.lilystudio.ordinary.web.IExecute
 * @see org.lilystudio.ordinary.web.IResult
 * 
 * @version 0.1.4, 2008/12/12
 * @author 欧阳先伟
 * @since Ordinary 0.1
 */
public class Command implements IProcess {

  /** 命令能识别的URI的正则表达式定义 */
  private Pattern name;

  /** 命令内部的数据集合管理类名称 */
  private Class<IRelay> relayClass;

  /** 命令内部的过滤器列表 */
  private List<IExecute> filter;

  /** 命令内部的数据集合处理对象 */
  private IExecute action;

  /** 命令内部的结果集处理列表 */
  private List<IResult> result;

  /** 命令内部的异常捕获器列表 */
  private List<Capture> capture;

  /**
   * 设置满足命令捕获条件的正则表达式
   * 
   * @param value
   *          正则表达式文本
   */
  public void setName(String value) {
    name = Pattern.compile("^" + value + "$");
  }

  /**
   * 设置数据集合管理类
   * 
   * @param value
   *          正则表达式文本
   */
  @SuppressWarnings("unchecked")
  public void setRelay(String value) throws Exception {
    relayClass = (Class<IRelay>) Class.forName(value);
  }

  /**
   * 根据标签生成调用中的数据对象
   * 
   * @param request
   *          HTTP输入对象
   * @param response
   *          HTTP输出对象
   * @return 数据对象
   * @throws Exception
   *           初始化异常
   */
  public IRelay getRelay(HttpServletRequest request,
      HttpServletResponse response) throws Exception {
    // 首先在request中查找relay, 如果没有, 在session中查找relay
    IRelay relay = (IRelay) request.getAttribute(IRelay.ATTRIBUTE_KEY);
    if (relay == null) {
      HttpSession session = request.getSession(false);
      if (session != null) {
        relay = (IRelay) session.getAttribute(IRelay.ATTRIBUTE_KEY);
        session.removeAttribute(IRelay.ATTRIBUTE_KEY);
      }
    }
    // 生成数据对象, 如果没有通过属性relay指定对象名称, 则使用缺省的内置对象
    if (relay == null) {
      if (relayClass != null) {
        relay = relayClass.newInstance();
      } else {
        relay = new DefaultRelay();
      }
      relay.init(request, response);
    }
    relay.setProcess(this);
    return relay;
  }
  
  public IRelay validate(HttpServletRequest request,
      HttpServletResponse response) throws Exception {
    // 检查路径的匹配程度, 如果匹配就需要初始化路径的完整信息,
    // 路径的匹配正则表达式写在name属性中
    String uri = request.getServletPath();
    Matcher m = name.matcher(uri);
    if (m.find()) {
      IRelay relay = getRelay(request, response);
      // HARDCODE
      for (int i = m.groupCount(); i > 0; i--) {
        relay.set("URI" + i, m.group(i));
      }
      relay.set("URL", request.getRequestURL());
      relay.set("PATH", uri.substring(0, uri.lastIndexOf('/') + 1));
      relay.set("URI", m.group());
      return relay;
    }
    return null;
  }

  public void execute(IRelay relay) throws Exception {
    try {
      // 依序完成filter对象的过滤处理
      if (filter != null) {
        for (IExecute o : filter) {
          o.execute(relay);
        }
      }
      // 调用数据集合处理对象
      if (action != null) {
        if (action instanceof MethodAction) {
          Method method = ((MethodAction) action).getMethod();
          if (method != null) {
            method.invoke(action, relay);
          } else {
            action.execute(relay);
          }
        } else {
          action.execute(relay);
        }
      }

      // 查询跳转对象, 允许在不同的条件下跳转到不同的位置,
      // 具体通过Relay.setForwardName进行设置
      String name = relay.getResultName();
      // HARDCODE
      relay.set("RESULT", name);
      for (IResult o : result) {
        if (name == null) {
          if (o.getName() != null) {
            continue;
          }
        } else if (!name.equals(o.getName())) {
          continue;
        }
        o.execute(relay.getRequest(), relay.getResponse(), relay);
        return;
      }
      throw new ResultNotFoundException(name);
    } catch (Exception e) {
      Capture.process(relay, e, capture);
    }
  }
}