package org.lilystudio.ordinary.util;

import java.util.Map;

/**
 * 文本转换器接口, 将一段文本按一定的规则转换为另一段文本
 * 
 * @version 0.1.3, 2008/09/01
 * @author 欧阳先伟
 * @since Ordinary 0.1
 */
public interface IParser {

  /**
   * 设置需要转换的原始文本信息
   * 
   * @param text
   *          原始文本信息
   * @param objects
   *          文本操作的参数
   * @throws Exception
   *           初始化失败
   */
  void init(String text, String... objects) throws Exception;

  /**
   * 解析生成新字符串
   * 
   * @param map
   *          转换中使用的参数映射
   * @return 生成的新字符串
   * @throws Exception
   *           转换错误
   */
  String parse(Map<String, Object> map) throws Exception;
}
