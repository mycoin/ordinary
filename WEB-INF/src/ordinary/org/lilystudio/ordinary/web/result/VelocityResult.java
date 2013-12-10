package org.lilystudio.ordinary.web.result;

import java.io.InputStream;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.RuntimeInstance;
import org.lilystudio.ordinary.web.Controller;
import org.lilystudio.ordinary.web.IRelay;

/**
 * Velocity输出类, 将框架数据集传递至Velocity数据容器中处理, 并生成页面. <br>
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
public class VelocityResult extends AbstractParameter {

  /** 模板引擎对象 */
  private static RuntimeInstance engine;

  /** 模板编码方式 */
  private static String encoding;

  /** 初始化模板引擎对象 */
  static {
    engine = new RuntimeInstance();
    Properties p = new Properties();
    InputStream in = Thread.currentThread().getContextClassLoader()
        .getResourceAsStream("velocity.properties");
    if (in != null) {
      try {
        p.load(in);
        p.setProperty(Velocity.FILE_RESOURCE_LOADER_PATH, Controller
            .getContextPath());
        engine.init(p);
        encoding = (String) engine.getProperty(Velocity.OUTPUT_ENCODING);
      } catch (Exception e) {
      }
    } else {
      engine.setProperty(Velocity.FILE_RESOURCE_LOADER_PATH, Controller
          .getContextPath());
      encoding = (String) engine.getProperty(Velocity.INPUT_ENCODING);
      if (encoding == null) {
        encoding = Controller.getEncoding();
        engine.setProperty(Velocity.INPUT_ENCODING, encoding);
        engine.setProperty(Velocity.OUTPUT_ENCODING, encoding);
      }
      try {
        engine.init();
      } catch (Exception e) {
      }
    }
  }

  /**
   * 获取模板引擎对象
   * 
   * @return 模板引擎对象
   */
  public static RuntimeInstance getEngine() {
    return engine;
  }

  /** 模板文件路径 */
  private String path;

  /** 输出类型 */
  private String type = "text/html; charset=" + encoding;

  /**
   * 设置输出类型
   * 
   * @param value
   *          输出类型的值
   */
  public void setType(String value) {
    type = value + "; charset=" + encoding;
  }

  public void execute(HttpServletRequest request, HttpServletResponse response,
      IRelay relay) throws Exception {
    setParameters(relay);
    Template template = engine.getTemplate(relay.getRealPath(path));
    VelocityContext context = new VelocityContext(relay.getDataMap());
    response.setContentType(type);
    response.setCharacterEncoding(encoding);
    template.merge(context, response.getWriter());
  }
}