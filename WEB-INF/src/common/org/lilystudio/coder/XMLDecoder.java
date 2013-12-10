package org.lilystudio.coder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * 将XML格式转换为数据对象
 * 
 * @version 0.1.4, 2008/12/12
 * @author 欧阳先伟
 * @since Common 0.1
 */
public class XMLDecoder {

  /**
   * XML节点解码变为Map对象, 节点的子结点和属性都转化为Map的键值,
   * 如果拥有同名的键, 将自动转化成List结果保存
   * 
   * @param in
   *          XML文件源(dom4j对象)
   */
  @SuppressWarnings("unchecked")
  public static Object decode(Node root) {
    Map<String, Object> result = new HashMap<String, Object>();
    // 读取并设置所有的属性信息
    NamedNodeMap attributes = root.getAttributes();
    int size;
    if (attributes != null) {
      size = attributes.getLength();
      for (int i = 0; i < size; i++) {
        Node attribute = attributes.item(i);
        result.put(attribute.getNodeName(), attribute.getNodeValue());
      }
    }
    StringBuilder text = new StringBuilder(64);
    // 读取下属的每一个子节点信息
    Node node = root.getFirstChild();
    while (node != null) {
      switch (node.getNodeType()) {
      case Node.CDATA_SECTION_NODE:
      case Node.TEXT_NODE: {
        // 处理文本操作
        String s = node.getTextContent();
        if (s.trim().length() > 0) {
          text.append(s);
        }
        break;
      }
      case Node.ELEMENT_NODE:
        Object value = decode(node);
        String name = node.getNodeName();
        Object data = result.get(name);
        if (data != null) {
          List<Object> list;
          if (data instanceof List) {
            list = ((List<Object>) data);
          } else {
            list = new ArrayList<Object>();
            list.add(data);
            result.put(name, list);
          }
          list.add(value);
        } else {
          result.put(name, value);
        }
      }
      node = node.getNextSibling();
    }

    if (result.size() == 0) {
      // 节点没有需要返回的子结点, 直接返回节点文本
      return text.toString();
    } else {
      if (text.length() > 0) {
        // 节点有需要返回的文本, 将文本内容加入列表
        // HARDCODE
        result.put("#text", text.toString());
      }
      return result;
    }
  }
}