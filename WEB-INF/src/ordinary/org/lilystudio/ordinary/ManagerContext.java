package org.lilystudio.ordinary;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.lilystudio.util.StringWriter;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 管理器容器, 保存所有的管理器, 使用get()方法取出名称对应的对象.
 * 
 * @version 0.1.4, 2008/12/12
 * @author 欧阳先伟
 * @since Ordinary 0.1
 */
public class ManagerContext {

  /**
   * 框架初始化异常
   */
  public static class InitializeException extends RuntimeException {

    /** 序号化编号 */
    private static final long serialVersionUID = 1L;

    /** 产生异常时的节点对象 */
    private Node node;

    /**
     * 建立组件初始化异常
     * 
     * @param node
     *          产生异常时的节点对象
     * @param message
     *          异常提示信息
     */
    public InitializeException(Node node, String message) {
      super(message);
      this.node = node;
    }

    @Override
    public String getMessage() {
      StringWriter out = new StringWriter();
      try {
        out.write(super.getMessage());
        out.write('\n');
        Transformer transformer = TransformerFactory.newInstance()
            .newTransformer();
        transformer.setOutputProperty(OutputKeys.METHOD, "html");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.transform(new DOMSource(node), new StreamResult(out));
        return out.toString().replaceAll("\\s+\\n", "\n");
      } catch (Exception e) {
        return super.getMessage();
      }
    }
  }

  /**
   * DOM元素迭代器, 用于控制一组节点中元素节点的读取,
   * 遇到import-dom节点将自动插入新的节点
   */
  private class ElementIterator implements Iterator<Node> {

    /** 全部的DOM元素列表 */
    private NodeList nodes;

    /** 全部的DOM元素数量 */
    private int size;

    /** 当前待读取的位置 */
    private int index;

    /**
     * 创建子元素迭代器
     * 
     * @param parent
     *          需要被迭代子节点元素的父节点
     */
    private ElementIterator(Node parent) {
      nodes = parent.getChildNodes();
      size = nodes.getLength();
    }

    public boolean hasNext() {
      while (index < size) {
        Node node = nodes.item(index);
        if (node.getNodeType() != Node.ELEMENT_NODE) {
          index++;
          continue;
        }
        String name = node.getNodeName();
        // 遇到导入标签, 首先查询有没有别名定义的标签组, 如果没有读取xml文件
        // HARDCODE
        if ("import-dom".equals(name)) {
          name = node.getAttributes().getNamedItem("name").getNodeValue();
          try {
            IManager manager = getManager(name, false);
            NodeList list;
            if (manager == null) {
              File file = new File(workPath + "/" + name);
              // 导入的文件也需要记录最后更新时间
              lastModified = Math.max(file.lastModified(), lastModified);
              associatedFiles.add(file);
              Node root = DocumentBuilderFactory.newInstance()
                  .newDocumentBuilder().parse(file).getDocumentElement();
              // 将当前标签下的所有子标签放入列表中
              list = node.getOwnerDocument().importNode(root, true)
                  .getChildNodes();
              register(name, list);
            } else {
              list = (NodeList) manager.get();
            }

            Node parent = node.getParentNode();
            int listSize = list.getLength();
            for (int i = 0; i < listSize; i++) {
              parent.insertBefore(list.item(i).cloneNode(true), node);
            }
            size += listSize - 1;
            parent.removeChild(node);
            continue;
          } catch (Exception e) {
            // HARDCODE
            throw new InitializeException(node, "Import '" + name
                + "' failed - " + e.getMessage());
          }
        }
        return true;
      }
      return false;
    }

    public Node next() {
      Node node = nodes.item(index);
      index++;
      return node;
    }

    public void remove() {
    }
  }

  /** createXXX时建立逻辑上的空对象 */
  public static final Object NULL = new Object();

  /** 类管理器映射集 */
  private Map<String, IManager> managers = new HashMap<String, IManager>();

  /** 配置文件及相关联的文件最后修改时间 */
  private long lastModified;

  /** 配置文件所在的路径 */
  private String workPath;

  /** 与配置文件的相关联XML文件列表, 在调试模式下, 跟踪是否需要重新加载框架 */
  private List<File> associatedFiles = new ArrayList<File>();

  /** 缺省标签属性映射, 每个标签都可以有自己的缺省属性值 */
  private Map<String, NamedNodeMap> defaultAttributes = new HashMap<String, NamedNodeMap>();

  /**
   * 标签的基本名称映射, 保存的是别名标签的基本Tag名称, 例如: <action
   * class="list" ... />的别名为list, 基本名称为action
   */
  private Map<String, String> basicNames = new HashMap<String, String>();

  /** 保存所有的类信息 */
  private Map<Class<?>, ClassInfo> classInfos = new HashMap<Class<?>, ClassInfo>();

  /**
   * 释放一个对象占用的资源, 实际上是强制调用finalize方法
   * 
   * @param o
   *          需要释放资源的变量
   */
  public static void release(Object o) {
    for (Class<?> c = o.getClass(); c != null; c = c.getSuperclass()) {
      try {
        Method m = c.getDeclaredMethod("finalize");
        try {
          m.invoke(o);
        } catch (Throwable e) {
        }
        return;
      } catch (Throwable e) {
      } finally {
      }
    }
  }

  /**
   * 根据名称获取对象, 这个名称是被register方法所指定的
   * 
   * @param name
   *          管理器的名称
   * @return 从管理器中获取的对象
   * @throws Exception
   *           如果无法取出对象
   */
  public Object get(String name) throws Exception {
    return getManager(name).get();
  }

  /**
   * 向管理器池中注册一个新的管理器, 这个管理器返回的对象每一次都是重新生成的
   * 
   * @param name
   *          管理器的名称
   * @param clazz
   *          需要被管理的类
   * @return 注册成功后创建的管理器
   * @throws Exception
   *           如果注册失败
   */
  public synchronized IManager register(String name, Class<?> clazz)
      throws Exception {
    if (clazz.isAnnotationPresent(Shared.class)) {
      return register(name, clazz.newInstance());
    }
    return register(name, new ClassManager(clazz));
  }

  /**
   * 向管理器池中注册一个新的管理器, 这个管理器返回的对象总是唯一的,
   * 请注意因此引起的互斥操作.
   * 
   * @param name
   *          管理器的名称
   * @param o
   *          需要被管理的实例
   * @return 注册成功后创建的管理器
   */
  public synchronized IManager register(String name, Object o) {
    if (o instanceof IManager) {
      return register(name, (IManager) o);
    }
    return register(name, new ObjectManager(o));
  }

  /**
   * 向管理器池中注册一个新的管理器
   * 
   * @param name
   *          管理器的名称
   * @param manager
   *          管理器
   * @return 管理器
   */
  public synchronized IManager register(String name, IManager manager) {
    managers.put(name, manager);
    return manager;
  }

  /**
   * 管理器容器复位, 清空所有以前定义的数据
   */
  public void reset() {
    // 释放所有的资源
    for (Object manager : managers.values()) {
      release(manager);
    }
    managers.clear();
    associatedFiles.clear();
    defaultAttributes.clear();
    basicNames.clear();
    classInfos.clear();
  }

  /**
   * 检查配置文件是否更新
   * 
   * @return <tt>true</tt>表示配置文件发生了更新
   */
  public boolean isUpdated() {
    if (associatedFiles.size() == 0) {
      // 框架还没有加载过
      return true;
    }
    for (File file : associatedFiles) {
      if (lastModified < file.lastModified()) {
        return true;
      }
    }
    return false;
  }

  /**
   * 加载配置文件
   * 
   * @param file
   *          配置文件描述符对象
   * @return 配置文件的DOM对象
   * @throws Throwable
   *           如果加载失败
   */
  public Node load(File file) throws Throwable {
    workPath = file.getParent().replace('\\', '/');
    lastModified = file.lastModified();
    reset();

    try {
      Node root = DocumentBuilderFactory.newInstance().newDocumentBuilder()
          .parse(file).getDocumentElement();

      // 将自身加入关联
      associatedFiles.add(file);
      Node parent;

      // 别名<alias>标签的处理
      parent = getChild(root, "alias");
      if (parent != null) {
        ElementIterator iterator = new ElementIterator(parent);
        while (iterator.hasNext()) {
          Node node = iterator.next();
          NamedNodeMap attributes = node.getAttributes();
          // 建立别名与原始标签名之间的联系
          Node attribute = attributes.getNamedItem("name");
          String name;
          if (attribute != null) {
            name = attribute.getNodeValue();
            basicNames.put(name, node.getNodeName());
          } else {
            // 没有指定别名, 使用标签名称作为别名
            name = node.getNodeName();
          }
          // 区分是指定类的别名还是标签组的别名
          attribute = attributes.getNamedItem("class");
          if (attribute != null) {
            register(name, new AliasManager(this, attribute.getNodeValue()));
            // 保存缺省值信息
            ElementIterator i = new ElementIterator(node);
            while (i.hasNext()) {
              node = i.next();
              if (node.getNodeName().equals("default")) {
                defaultAttributes.put(name, node.getAttributes());
                break;
              }
            }
          } else {
            // 保存子节点树
            register(name, node.getChildNodes());
          }
        }
      }

      // 模块<modules>标签的处理
      parent = getChild(root, "modules");
      // 遍历所有的子节点, 递归的方式初始化子节点
      if (parent != null) {
        ElementIterator iterator = new ElementIterator(parent);
        while (iterator.hasNext()) {
          get(iterator.next());
        }
      }
      return root;
    } catch (Throwable e) {
      reset();
      throw e;
    }
  }

  /**
   * 获取节点的全部子节点迭代器
   * 
   * @param node
   *          节点
   * @return 子节点迭代器
   */
  public Iterator<Node> getElementIterator(Node node) {
    return new ElementIterator(node);
  }

  /**
   * 取出节点中指定名称的子节点
   * 
   * @param node
   *          节点
   * @param childName
   *          子节点的名称
   * @return 如果没有找到, 返回<tt>null</tt>, 否则返回找到的子节点
   */
  public Node getChild(Node node, String childName) {
    node = node.getFirstChild();
    while (node != null) {
      if (node.getNodeType() == Node.ELEMENT_NODE
          && node.getNodeName().equals(childName)) {
        return node;
      }
      node = node.getNextSibling();
    }
    return null;
  }

  /**
   * 获取管理器对象
   * 
   * @param name
   *          管理器的名称
   * @return 符合名称的管理器
   * @throws Exception
   *           如果无法取出管理器
   */
  protected IManager getManager(String name) throws Exception {
    return getManager(name, true);
  }

  /**
   * 根据名称获取管理器
   * 
   * @param name
   *          管理器的名称
   * @param create
   *          <tt>true>表示如果没有需要自动建立
   * @return 符合名称的管理器
   * @throws Exception
   *           如果无法取出管理器
   */
  protected IManager getManager(String name, boolean create) throws Exception {
    IManager manager = managers.get(name);
    if (manager == null && create) {
      // 如果类管理器在类管理器池中不存在, 则将名称当成类名进行初始
      Class<?> c = Class.forName(name);
      // 注册新的类管理器
      manager = register(name, c);
    }
    return manager;
  }

  /**
   * 根据节点获得对应的Bean对象, 首先查找节点中的class属性,
   * 如果不存在直接将节点名称作为class, 方法内会自动对对象完成初始化
   * 
   * @param node
   *          节点
   * @return 节点对应的Bean对象
   * @throws InitializeException
   *           如果对象生成失败
   */
  protected Object get(Node node) throws InitializeException {
    NamedNodeMap attributes = node.getAttributes();
    Node attribute = attributes.getNamedItem("class");
    String name;
    // 检查是否有class属性, 如果没有将使用节点名称做为对象名称
    if (attribute != null) {
      name = attribute.getNodeValue();
      attributes.removeNamedItem("class");
    } else {
      name = node.getNodeName();
    }

    try {
      Object o = get(name);
      // 从对象池中获取对象后, 检查指定对象是否拥有缺省属性, 如果存在需要设置缺省值.
      // 缺省属性指<alias>节点下的子节点是否包含<default>子节点
      NamedNodeMap defaultAttribute = defaultAttributes.get(name);
      if (defaultAttribute != null) {
        // 合并缺省值标签信息
        int size = defaultAttribute.getLength();
        for (int i = 0; i < size; i++) {
          Node item = defaultAttribute.item(i);
          if (attributes.getNamedItem(item.getNodeName()) == null) {
            attributes.setNamedItem(item.cloneNode(false));
          }
        }
      }
      initElement(o, node);

      return o;
    } catch (InitializeException e) {
      throw e;
    } catch (InvocationTargetException e) {
      InitializeException ex = new InitializeException(node, e
          .getTargetException().getMessage());
      ex.setStackTrace(e.getStackTrace());
      throw ex;
    } catch (Exception e) {
      throw new InitializeException(node, e.getMessage());
    } finally {
      if (attribute != null) {
        attributes.setNamedItem(attribute);
      }
    }
  }

  /**
   * 初始化配置文件条目, 将信息填充进入对象中, 条目的属性与对象一一映射. 在初始化过程中,
   * 首先查找有没有set方法, 如果存在, 将文本内容作为参数传入;
   * 然后遍历查找有没有属性对应的set方法, 如存在aaa属性, 则会查找setAaa方法;
   * 然后查找是否存在与条目属性同名的对象内的属性, 如存在aaa属性,
   * 则会查找对象内的属性aaa; 然后查找是否存在与子条目标签同名的add***方法,
   * 或者是否存在与子条目标签同名的类型为List型的对象属性,
   * 它们将指定添加一个子条目标签的方式, 子条目标签的建立使用create***方法来定义,
   * 如果不存在, 将直接get递归这个过程得到一个子对象, 最后将调用init()完成初始化.
   * 
   * @param self
   *          需要被操作的对象
   * @param node
   *          节点
   * @throws Exception
   *           如果当前节点的子节点实例化失败
   */
  private void initElement(Object self, Node node) throws Exception {
    Class<?> c = self.getClass();
    ClassInfo info = classInfos.get(c);
    if (info == null) {
      info = new ClassInfo(this, c);
      classInfos.put(c, info);
    }

    // 设置节点的属性
    // 首先调用set方法设置节点的值
    if (info.containSetTextImpl()) {
      info.setText(self, node.getTextContent());
    }
    NamedNodeMap attributes = node.getAttributes();
    int size = attributes.getLength();
    for (int i = 0; i < size; i++) {
      Node attribute = attributes.item(i);
      // 递归设置节点属性, 首先检查是否有与属性同名的类似setXXX的方法调用
      String name = attribute.getNodeName();
      info.set(self, name, attribute.getNodeValue());
    }

    // 遍历所有的子节点, 递归的方式初始化子节点
    ElementIterator iterator = new ElementIterator(node);
    while (iterator.hasNext()) {
      Node childNode = iterator.next();
      String name = childNode.getNodeName();
      // 没有指定class属性的情况下, 需要检查这个标签名称是否有原始的标签名称
      if (childNode.getAttributes().getNamedItem("class") == null) {
        String aliasName = basicNames.get(name);
        if (aliasName != null) {
          name = aliasName;
        }
      }

      ChildNodeImpl impl = info.getChildNodeImpl(self, name);
      // 需要添加子节点操作
      if (impl.containAddImpl()) {
        Object o = impl.create(self);
        if (o != NULL) {
          if (o == null) {
            o = get(childNode);
          } else {
            initElement(o, childNode);
          }
          impl.add(self, o, childNode);
        }
      } else {
        // HARDCODE
        throw new InitializeException(childNode,
            "The element can't be supported");
      }
    }
    info.init(self, node);
  }
}