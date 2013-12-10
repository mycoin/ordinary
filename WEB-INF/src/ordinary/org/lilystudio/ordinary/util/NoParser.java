package org.lilystudio.ordinary.util;

import java.util.Map;

/**
 * 不进行处理的文本转换器, 直接将输入文本输出
 * 
 * @version 0.1.3, 2008/09/01
 * @author 欧阳先伟
 * @since Ordinary 0.1
 */
public class NoParser implements IParser {

  /** 原始的文本内容 */
  private String text;

  public void init(String text, String... objects) throws Exception {
    this.text = text;
  }

  public String parse(Map<String, Object> map) throws Exception {
    return text;
  }
}
