package org.lilystudio.ordinary.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * mapping标签处理类, 用于实现高速的command标签匹配,
 * 普通的写在host中的command标签, 使用正则表达式匹配有效的URI,
 * 而写在mapping中的command标签, 直接使用完全匹配的方式. <br>
 * <b>子标签</b>
 * 
 * <pre>
 * filter--指定数据过滤器
 * command--指定快速URL关联列表
 * capture--指定发生异常时的处理
 * </pre>
 * 
 * @see org.lilystudio.ordinary.web.Capture
 * @see org.lilystudio.ordinary.web.Command
 * @see org.lilystudio.ordinary.web.IExecute
 * 
 * @version 0.1.4, 2008/12/12
 * @author 欧阳先伟
 * @since Ordinary 0.1
 */
public class Mapping implements IProcess {

  /**
   * Mapping中使用的Command对象
   */
  public class MappingCommand extends Command {

    /**
     * 设置Mapping中的名称, 用于快速访问
     */
    public void setName(String value) {
      commands.put(value, this);
    }
  }

  /** 域的公共过滤器列表 */
  private List<IExecute> filter;

  /** 处理模块列表 */
  private Map<String, Command> commands = new HashMap<String, Command>();

  /** 域的公共异常捕获器列表 */
  private List<Capture> capture;

  /**
   * 添加command子标签对象
   * 
   * @param o
   *          子标签对象
   */
  public void addCommand(Object o) {
  }

  /**
   * 创建一个command子标签对象
   * 
   * @return 子标签对象
   */
  public MappingCommand createCommand() {
    return new MappingCommand();
  }

  public IRelay validate(HttpServletRequest request,
      HttpServletResponse response) throws Exception {
    String uri = request.getServletPath();
    // 根据URL的, 检测是否有与其name属性相同的<command>标签
    Command command = commands.get(uri);
    if (command != null) {
      IRelay relay = command.getRelay(request, response);
      // HARDCODE
      relay.set("URL", request.getRequestURL());
      relay.set("PATH", uri.substring(0, uri.lastIndexOf('/') + 1));
      relay.set("URI", uri);
      return relay;
    }
    return null;
  }

  public void execute(IRelay relay) throws Exception {
    try {
      // 执行过滤器过滤客户端提交的数据
      if (filter != null) {
        for (IExecute o : filter) {
          o.execute(relay);
        }
      }
      relay.getProcess().execute(relay);
    } catch (Exception e) {
      Capture.process(relay, e, capture);
    }
  }
}