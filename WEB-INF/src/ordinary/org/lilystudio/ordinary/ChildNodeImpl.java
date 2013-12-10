package org.lilystudio.ordinary;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.lilystudio.ordinary.ManagerContext.InitializeException;
import org.w3c.dom.Node;

/**
 * 子节点操作类
 * 
 * @version 0.1.4, 2008/12/12
 * @author 欧阳先伟
 * @since Ordinary 0.1
 */
class ChildNodeImpl {

  /**
   * 新增子节点对象接口
   */
  private static interface IAdd {

    /**
     ** 添加一个子节点对象
     * 
     * @param self
     *          需要被操作的对象
     * @param childBean
     *          需要被添加的对象
     * @param childNode
     *          子节点
     * @throws Exception
     *           如果子节点对象添加失败
     */
    void add(Object self, Object childBean, Node childNode) throws Exception;
  }

  /**
   * 对象型属性域的新增操作
   */
  private static class ObjectFieldAdd implements IAdd {

    /** 子节点对应的列表对象 */
    private Field field;

    /**
     * 创建对象型属性域添加子结点的接口
     * 
     * @param field
     *          子结点相关的属性域
     */
    private ObjectFieldAdd(Field field) {
      this.field = field;
    }

    public void add(Object self, Object childBean, Node childNode)
        throws Exception {
      if (field.get(self) != null) {
        // HARDCODE
        throw new InitializeException(childNode, "The element with a name defined by repeated");
      }
      field.set(self, childBean);
    }
  }

  /**
   * 列表型属性域的新增方式
   */
  private static class ListFieldAdd implements IAdd {

    /** 子节点对应的列表对象 */
    private Field field;

    /**
     * 创建列表型属性域添加子结点的接口
     * 
     * @param field
     *          子结点相关的属性域
     */
    private ListFieldAdd(Field field) {
      this.field = field;
    }

    @SuppressWarnings("unchecked")
    public void add(Object self, Object childBean, Node childNode)
        throws Exception {
      List<Object> list = (List<Object>) field.get(self);
      if (list == null) {
        list = new ArrayList();
        field.set(self, list);
      }
      list.add(childBean);
    }
  }

  /**
   * 普通方法型的新增方式, 方法仅有一个子对象参数
   */
  private static class NormalMethodAdd implements IAdd {

    /** 添加子节点的方法 */
    private Method method;

    /**
     * 创建普通方法型添加子结点的接口
     * 
     * @param method
     *          添加子结点的方法
     */
    private NormalMethodAdd(Method method) {
      this.method = method;
    }

    public void add(Object self, Object childBean, Node childNode)
        throws Exception {
      method.invoke(self, childBean);
    }
  }

  /**
   * 增强方法型的新增方式, 比普通方法型多传递一个节点对象的参数
   */
  private static class AdvancedMethodAdd implements IAdd {

    /** 添加子节点的方法 */
    private Method method;

    /**
     * 创建增强方法型添加子结点的接口
     * 
     * @param method
     *          添加子结点的方法
     */
    private AdvancedMethodAdd(Method method) {
      this.method = method;
    }

    public void add(Object self, Object childBean, Node childNode)
        throws Exception {
      method.invoke(self, childBean, childNode);
    }
  }

  /** 新增一个子节点的操作接口 */
  private IAdd addImpl;

  /** 创建子节点的方法 */
  private Method createImpl;

  /**
   * 创建子节点操作类
   * 
   * @param clazz
   *          子节点操作类实例
   * @param name
   *          子节点名称
   */
  ChildNodeImpl(Class<?> clazz, String name) {
    String beanName = Character.toUpperCase(name.charAt(0)) + name.substring(1);

    // 查找添加子节点的操作接口
    find: while (true) {
      // 首先查找是否有同名的addXXX方法
      try {
        addImpl = new AdvancedMethodAdd(clazz.getMethod("add" + beanName,
            Object.class, Node.class));
        break;
      } catch (Exception e) {
      }
      try {
        addImpl = new NormalMethodAdd(clazz.getMethod("add" + beanName,
            Object.class));
        break;
      } catch (Exception e) {
      }
      // 其次查找有无公共的add方法
      try {
        addImpl = new AdvancedMethodAdd(clazz.getMethod("add", Object.class,
            Node.class));
        break;
      } catch (Exception e) {
      }
      try {
        addImpl = new NormalMethodAdd(clazz.getMethod("add", Object.class));
        break;
      } catch (Exception e) {
      }

      for (Class<?> c = clazz; c != Object.class; c = c.getSuperclass()) {
        try {
          // 如果存在同名的List型对象属性, 则生成一个新对象并加入,
          // 同时返回这个新对象, 否则直接将对象从数据池中取出来向同名属性赋值
          Field field = c.getDeclaredField(name);
          field.setAccessible(true);
          if (List.class.isAssignableFrom(field.getType())) {
            addImpl = new ListFieldAdd(field);
          } else {
            addImpl = new ObjectFieldAdd(field);
          }
          break find;
        } catch (Exception e) {
        }
      }

      break;
    }

    while (true) {
      // 查找新增操作的方法
      try {
        createImpl = clazz.getMethod("create" + beanName);
        break;
      } catch (Exception e) {
      }
      try {
        createImpl = clazz.getMethod("create");
      } catch (Exception e) {
      }
      break;
    }
  }

  /**
   * 判断子结点的添加操作接口状态
   * 
   * @return 如果子结点有对应的添加操作接口返回<tt>true</tt>
   */
  boolean containAddImpl() {
    return addImpl != null;
  }

  /**
   ** 创建一个子节点对应的Bean对象
   * 
   * @param self
   *          需要被操作的对象
   * @return 子结点对应的Bean对象, 如果不能直接创建返回<tt>null</tt>
   * @throws Exception
   *           如果创建失败
   */
  Object create(Object self) throws Exception {
    if (createImpl != null) {
      return createImpl.invoke(self);
    }
    return null;
  }

  /**
   ** 添加一个子节点对应的Bean对象
   * 
   * @param self
   *          需要被操作的对象
   * @param childBean
   *          需要被添加的子结点对应的Bean对象
   * @param childNode
   *          子节点
   * @throws Exception
   *           如果添加失败
   */
  void add(Object self, Object childBean, Node childNode) throws Exception {
    addImpl.add(self, childBean, childNode);
  }
}
