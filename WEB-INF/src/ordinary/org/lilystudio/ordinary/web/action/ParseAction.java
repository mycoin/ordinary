package org.lilystudio.ordinary.web.action;

import org.lilystudio.ordinary.util.IParser;
import org.lilystudio.ordinary.util.NoParser;
import org.lilystudio.ordinary.web.IExecute;
import org.lilystudio.ordinary.web.IRelay;
import org.w3c.dom.Node;

/**
 * 解释/脚本处理操作, 通过挂接处理器parser来对数据进行加工, 并指定需要回写的数据名称<br>
 * <b>属性</b>
 * 
 * <pre>
 * parser--文本转换器对象名
 * rewrite--需要回写的名称列表
 * </pre>
 * 
 * @see org.lilystudio.ordinary.util.IParser
 * 
 * @version 0.1.4, 2008/12/12
 * @author 欧阳先伟
 * @since Ordinary 0.1
 */
public class ParseAction implements IExecute {

  /** 文本转换器 */
  protected IParser parser;

  /** 附加的参数列表 */
  protected String[] rewrite;

  /**
   * 初始化附加参数列表
   * 
   * @param value
   *          配置文件中指定的附加参数列表
   * @throws Exception
   *           文本标签存在语法错误
   */
  public void setRewrite(String value) {
    rewrite = value.split(",");
  }

  /**
   * 对象初始化, 在属性, 子标签初始化之后进行
   * 
   * @param node
   *          节点对象
   * @throws Exception
   *           初始化异常
   */
  public void init(Node node) throws Exception {
    // 不设置转换器时, 使用默认的不进行任何转换的转换器
    if (parser == null) {
      parser = new NoParser();
    }
    parser.init(node.getTextContent(), rewrite);
  }

  public void execute(IRelay relay) throws Exception {
    parser.parse(relay.getDataMap());
  }
}
