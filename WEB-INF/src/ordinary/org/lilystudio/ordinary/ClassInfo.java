package org.lilystudio.ordinary;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Node;

/**
 * 类的方法操作信息
 * 
 * @version 0.1.4, 2008/12/12
 * @author 欧阳先伟
 * @since Ordinary 0.1
 */
class ClassInfo {

  /**
   * 给Bean属性赋值操作接口
   */
  private interface ISet {

    /** 对象类型 */
    int OBJECT = 0;

    /** 布尔类型, 仅接受true,false两个值 */
    int BOOLEAN = 1;

    /** 字节类型 */
    int BYTE = 2;

    /** 短整型 */
    int SHORT = 3;

    /** 整型 */
    int INT = 4;

    /** 长整型 */
    int LONG = 5;

    /** 单精度浮点型 */
    int FLOAT = 6;

    /** 双精度浮点型 */
    int DOUBLE = 7;

    /** 字符型 */
    int CHAR = 8;

    /** 字符串型 */
    int STRING = 9;

    /**
     * 向对象的指定属性赋值, 对属性值的缺省设置,
     * 检查节点对象是否包含与XML属性同名的set方法或者对象属性, 如果包含,
     * 根据对象属性的类似分别进行整数或字符串等的设置, 对于不可识别的boolean值(非
     * <tt>false</tt>)设置为<tt>true</tt>
     * 如果对象属性不属于基本数据类型, 则从对象池中搜索与字符串同名的对象进行赋值
     * 
     * @param self
     *          需要被操作的对象
     * @param value
     *          需要赋予的值
     * @throws Exception
     *           如果赋值操作失败
     */
    void set(Object self, String value) throws Exception;
  }

  /**
   * 设置操作基类
   */
  private abstract class AbstractSet implements ISet {

    /** 被设置的参数类型 */
    int type;

    /**
     * 创建设置操作对象
     * 
     * @param typeClass
     *          参数类型
     */
    private AbstractSet(Class<?> typeClass) {
      if (typeClass == boolean.class) {
        type = BOOLEAN;
      } else if (typeClass == byte.class) {
        type = BYTE;
      } else if (typeClass == short.class) {
        type = SHORT;
      } else if (typeClass == int.class) {
        type = INT;
      } else if (typeClass == long.class) {
        type = LONG;
      } else if (typeClass == float.class) {
        type = FLOAT;
      } else if (typeClass == double.class) {
        type = DOUBLE;
      } else if (typeClass == char.class) {
        type = CHAR;
      } else if (typeClass == String.class) {
        type = STRING;
      }
    }
  }

  /**
   * 通过set方法赋值的操作
   */
  private class MethodSet extends AbstractSet {

    /** set方法对象 */
    private Method method;

    /**
     * 创建一个set赋值操作对象
     * 
     * @param method
     *          set方法对象
     */
    private MethodSet(Method method) {
      super(method.getParameterTypes()[0]);
      this.method = method;
    }

    public void set(Object self, String value) throws Exception {
      switch (type) {
      case BOOLEAN:
        method.invoke(self, Boolean.valueOf(value));
        break;
      case BYTE:
        method.invoke(self, Byte.valueOf(value));
        break;
      case SHORT:
        method.invoke(self, Short.valueOf(value));
        break;
      case INT:
        method.invoke(self, Integer.valueOf(value));
        break;
      case LONG:
        method.invoke(self, Long.valueOf(value));
        break;
      case FLOAT:
        method.invoke(self, Float.valueOf(value));
        break;
      case DOUBLE:
        method.invoke(self, Double.valueOf(value));
        break;
      case CHAR:
        method.invoke(self, Character.valueOf(value.charAt(0)));
        break;
      case STRING:
        method.invoke(self, value);
        break;
      default:
        method.invoke(self, context.get(value));
      }
    }
  }

  /**
   * 直接向对象属性域赋值的操作
   */
  private class FieldSet extends AbstractSet {

    /** 属性域对象 */
    private Field field;

    /**
     * 创建一个向属性域赋值操作对象
     * 
     * @param field
     *          属性域对象
     */
    private FieldSet(Field field) {
      super(field.getType());
      this.field = field;
    }

    public void set(Object self, String value) throws Exception {
      switch (type) {
      case BOOLEAN:
        field.setBoolean(self, Boolean.parseBoolean(value));
        break;
      case BYTE:
        field.setByte(self, Byte.parseByte(value));
        break;
      case SHORT:
        field.setShort(self, Short.parseShort(value));
        break;
      case INT:
        field.setInt(self, Integer.parseInt(value));
        break;
      case LONG:
        field.setLong(self, Long.parseLong(value));
        break;
      case FLOAT:
        field.setFloat(self, Float.parseFloat(value));
        break;
      case DOUBLE:
        field.setDouble(self, Double.parseDouble(value));
        break;
      case CHAR:
        field.setChar(self, value.charAt(0));
        break;
      case STRING:
        field.set(self, value);
        break;
      default:
        field.set(self, context.get(value));
      }
    }
  }

  /**
   * 节点初始化操作
   */
  private interface IInit {

    /**
     * 节点初始化
     * 
     * @param self
     *          需要被操作的对象
     * @param node
     *          节点
     * @throws Exception
     *           如果节点初始化失败
     */
    void init(Object self, Node node) throws Exception;
  }

  /**
   * 无参数信息的初始化操作, 初始化没有任何参数
   */
  private class NoInfoInit implements IInit {

    /** 初始化方法 */
    private Method method;

    /**
     * 创建无参数信息的初始化操作对象
     * 
     * @param method
     *          初始化方法
     */
    private NoInfoInit(Method method) {
      this.method = method;
    }

    public void init(Object self, Node node) throws Exception {
      method.invoke(self);
    }
  }

  /**
   * 包含节点信息的初始化操作, 初始化参数包含节点信息
   */
  private class NodeInfoInit implements IInit {

    /** 初始化方法 */
    private Method method;

    /**
     * 创建包含节点信息的初始化操作对象
     * 
     * @param method
     *          初始化方法
     */
    private NodeInfoInit(Method method) {
      this.method = method;
    }

    public void init(Object self, Node node) throws Exception {
      method.invoke(self, node);
    }
  }

  /**
   * 包含容器信息的初始化操作, 初始化参数包含容器信息
   */
  private class ContextInfoInit implements IInit {

    /** 初始化方法 */
    private Method method;

    /**
     * 创建包含容器信息的初始化操作对象
     * 
     * @param method
     *          初始化方法
     */
    private ContextInfoInit(Method method) {
      this.method = method;
    }

    public void init(Object self, Node node) throws Exception {
      method.invoke(self, context);
    }
  }

  /**
   * 包含全部信息的初始化操作, 初始化参数包含管理器容器与节点信息
   */
  private class AllInfoInit implements IInit {

    /** 初始化方法 */
    private Method method;

    /**
     * 创建包含全部信息的初始化操作对象
     * 
     * @param method
     *          初始化方法
     */
    private AllInfoInit(Method method) {
      this.method = method;
    }

    public void init(Object self, Node node) throws Exception {
      method.invoke(self, context, node);
    }
  }

  /** 子节点的信息 */
  private Map<String, ChildNodeImpl> childNodes = new HashMap<String, ChildNodeImpl>();

  /** set操作方法集合 */
  private Map<String, ISet> sets = new HashMap<String, ISet>();

  /** 管理器容器 */
  private ManagerContext context;

  /** 初始化节点操作集合 */
  private IInit[] initImpl = new IInit[4];

  /** 设置节点文本操作 */
  private ISet setImpl;

  /**
   * 创建类信息对象
   * 
   * @param context
   *          管理器容器
   * @param clazz
   *          需要创建的类
   */
  ClassInfo(ManagerContext context, Class<?> clazz) {
    this.context = context;

    // 生成方法赋值操作对象
    for (Method method : clazz.getMethods()) {
      String name = method.getName();
      if (name.equals("init")) {
        // 初始化方法设置
        Class<?>[] parameters = method.getParameterTypes();
        switch (parameters.length) {
        case 0:
          initImpl[0] = new NoInfoInit(method);
          continue;
        case 1:
          if (parameters[0] == Node.class) {
            initImpl[1] = new NodeInfoInit(method);
          } else if (ManagerContext.class.isAssignableFrom(parameters[0])) {
            initImpl[2] = new ContextInfoInit(method);
          }
          continue;
        case 2:
          if (ManagerContext.class.isAssignableFrom(parameters[0])
              && parameters[1] == Node.class) {
            initImpl[3] = new AllInfoInit(method);
          }
        }
        continue;
      } else if (name.startsWith("set")) {
        Class<?>[] parameters = method.getParameterTypes();
        if (parameters.length == 1) {
          if (name.length() == 3) {
            setImpl = new MethodSet(method);
          } else {
            sets.put(Character.toLowerCase(name.charAt(3)) + name.substring(4),
                new MethodSet(method));
          }
        }
      }
    }

    // 递归查找父类中对应的所有属性名称
    for (Class<?> c = clazz; c != Object.class; c = c.getSuperclass()) {
      for (Field field : c.getDeclaredFields()) {
        String name = field.getName();
        if (!sets.containsKey(name)) {
          field.setAccessible(true);
          sets.put(name, new FieldSet(field));
        }
      }
    }
  }

  /**
   ** 获取指定的类对象子结点操作实例
   * 
   * @param self
   *          需要被操作的对象
   * @param name
   *          子节点对象的名称
   * @return 子节点对象的操作实例
   */
  ChildNodeImpl getChildNodeImpl(Object self, String name) {
    ChildNodeImpl impl = childNodes.get(name);
    if (impl == null) {
      impl = new ChildNodeImpl(self.getClass(), name);
      childNodes.put(name, impl);
    }
    return impl;
  }

  /**
   * 判断设置文本操作接口的状态<tt>true</tt>
   * 
   * @return 如果设置文本操作接口存在返回<tt>true</tt>
   */
  boolean containSetTextImpl() {
    return setImpl != null;
  }

  /**
   * 设置节点文本信息
   * 
   * @param self
   *          需要被操作的对象
   * @param value
   *          需要设置的文本
   * @param Exception
   *          如果文本设置失败
   */
  void setText(Object self, String value) throws Exception {
    setImpl.set(self, value);
  }

  /**
   * 向对象中写入指定的属性值
   * 
   * @param self
   *          需要被操作的对象
   * @param name
   *          属性的名称
   * @param value
   *          属性的值
   * @param Exception
   *          如果赋值失败
   */
  void set(Object self, String name, String value) throws Exception {
    ISet setImpl = sets.get(name);
    if (setImpl != null) {
      setImpl.set(self, value);
    }
  }

  /**
   * 初始化节点
   * 
   * @param self
   *          需要被操作的对象
   * @param node
   *          节点
   * @param Exception
   *          如果初始化失败
   */
  void init(Object self, Node node) throws Exception {
    for (IInit impl : initImpl) {
      if (impl != null) {
        impl.init(self, node);
      }
    }
  }
}
