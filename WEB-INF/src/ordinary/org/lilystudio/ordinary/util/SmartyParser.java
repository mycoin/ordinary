package org.lilystudio.ordinary.util;

import java.util.Map;

import org.lilystudio.ordinary.web.result.SmartyResult;
import org.lilystudio.smarty4j.Context;
import org.lilystudio.smarty4j.Template;
import org.lilystudio.util.StringWriter;

/**
 * Smarty文本转换器, 这里的附加参数是指需要回写的参数名称列表, 否则,
 * 传入的数据集合不会被smarty语句改变
 * 
 * @version 0.1.3, 2008/09/01
 * @author 欧阳先伟
 * @since Ordinary 0.1
 */
public class SmartyParser implements IParser {

  /** 模板对象 */
  private Template template;

  /** 需要回写保存的参数名称 */
  private String[] names;

  public void init(String text, String... objects) throws Exception {
    template = new Template(SmartyResult.getEngine(), text);
    names = objects;
  }

  public String parse(Map<String, Object> map) throws Exception {
    Context context = new Context();
    context.putAll(map);
    // 进行临时的输出, 成功才真正的返回得到的结果
    StringWriter out = new StringWriter();
    template.merge(context, out);
    // 回写指定的参数
    if (names != null) {
      for (String name : names) {
        map.put(name, context.get(name));
      }
    }
    return out.toString();
  }
}
