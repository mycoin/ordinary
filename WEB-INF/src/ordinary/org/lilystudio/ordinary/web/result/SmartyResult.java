package org.lilystudio.ordinary.web.result;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.lilystudio.ordinary.web.Controller;
import org.lilystudio.ordinary.web.IRelay;
import org.lilystudio.smarty4j.Context;
import org.lilystudio.smarty4j.Engine;
import org.lilystudio.smarty4j.Template;

/**
 * Smarty输出类, 将框架数据集传递至Smarty数据容器中处理, 并生成页面. <br>
 * <b>属性</b>
 * 
 * <pre>
 * type--指定输出的类型, 默认是text/html
 * path--模板文件的路径
 * </pre>
 * 
 * @version 0.1.4, 2008/12/12
 * @author 欧阳先伟
 * @since Ordinary 0.1
 */
public class SmartyResult extends AbstractParameter {

  /** 模板引擎对象 */
  private static Engine engine;

  /** 初始化模板引擎对象 */
  static {
    engine = new Engine();
    engine.setTemplatePath(Controller.getContextPath());
  }

  /**
   * 获取模板引擎对象
   * 
   * @return 模板引擎对象
   */
  public static Engine getEngine() {
    return engine;
  }

  /** 模板文件路径 */
  private String path;

  /** 输出类型 */
  private String type = "text/html; charset=" + engine.getEncoding();

  /**
   * 设置输出类型
   * 
   * @param value
   *          输出类型的值
   */
  public void setType(String value) {
    type = value + "; charset=" + engine.getEncoding();
  }

  public void execute(HttpServletRequest request, HttpServletResponse response,
      IRelay relay) throws Exception {
    setParameters(relay);
    Template template = engine.getTemplate(relay.getRealPath(path));
    Context context = new Context();
    context.putAll(relay.getDataMap());
    response.setContentType(type);
    response.setCharacterEncoding(engine.getEncoding());
    template.merge(context, response.getWriter());
  }
}