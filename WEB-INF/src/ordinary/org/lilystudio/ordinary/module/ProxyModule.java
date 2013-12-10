package org.lilystudio.ordinary.module;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.LoaderClassPath;
import javassist.Modifier;
import javassist.NotFoundException;

import org.lilystudio.ordinary.ManagerContext;
import org.lilystudio.util.DynamicClassLoader;

/**
 * 代理模块, 用于向已有的类中注入代码. 如果不指定代理配置文件,
 * 将自动使用与类同名的.proxy文件, 需要包含第三方的javassist包
 * 
 * <b>属性</b>
 * 
 * <pre>
 * file--全局代理配置文件名
 * </pre>
 * 
 * <b>proxy子标签</b>具体描述一个需要调试的类<br>
 * <b>proxy子标签属性</b>
 * 
 * <pre>
 * class--需要代理的类名
 * file--类调试配置文件名
 * </pre>
 * 
 * 代理描述文件能定义两种操作, 一是向类中添加属性域, 格式为<br>
 * 类名 属性名;<br>
 * 一是向类中增加方法(必须是原有类中存在的方法), 格式为<br>
 * 方法名[(参数类名,参数类名,...)]<br>
 * 其中(...)可以省略, 表示自动匹配参数, 如果一个方法被多次重载, 需要精确的指定参数,
 * 配置文件示例如下, 向类中新增一个属性aaa, 并且打印输出原来execute方法的执行时间
 * 
 * <pre>
 *  int count;
 * 
 *  execute
 *  {
 *  count++;
 *  long time = (new java.util.Date()).getTime();
 *  super.execute($$);
 *  System.out.println(&quot;Run:&quot; + ((new java.util.Date()).getTime() - time));
 *  }
 * </pre>
 * 
 * <b>示例</b>
 * 
 * <pre>
 * &lt;module class=&quot;org.lilystudio.ordinary.module.ProxyModule&quot;
 *   file=&quot;Result.proxy&quot;&gt;
 *   &lt;proxy class=&quot;org.lilystudio.ordinary.web.result.SmartyResult&quot; /&gt;
 *   &lt;proxy class=&quot;org.lilystudio.ordinary.util.SmartyParser&quot; file=&quot;Parser.proxy&quot; /&gt;
 * &lt;/module&gt;
 * </pre>
 * 
 * @version 0.1.4, 2008/12/12
 * @author 欧阳先伟
 * @since Ordinary 0.1
 */
public class ProxyModule {

  /**
   * 代理具体配置条目对象
   */
  public static class Item {

    /** 代理类的名称 */
    private String className;

    /** 代理类的配置文件 */
    private String file;

    /**
     * 设置代理类的名称
     * 
     * @param value
     *          代理类的名称字符串
     */
    public void setClass(String value) {
      className = value;
    }
  }

  /** 供List生成字符串数组使用的实例 */
  private static final String[] ARRAY = new String[0];

  /** javassist类池 */
  private ClassPool classPool;

  /** 公共代理配置文件名 */
  private String file;

  /** 代理类的条目列表 */
  private List<Item> items = new ArrayList<Item>();

  /**
   * 建立代理模块, 配置基本的系统环境
   */
  public ProxyModule() {
    classPool = new ClassPool();
    classPool.appendClassPath(new LoaderClassPath(this.getClass()
        .getClassLoader()));
  }

  /**
   * 添加一个代理类配置条目
   * 
   * @param item
   *          代理类配置条目
   */
  public void add(Object item) {
    items.add((Item) item);
  }

  /**
   * 生成一个代理类配置条目对象
   * 
   * @return 代理类配置条目对象
   */
  public Object create() {
    return new Item();
  }

  /**
   * 初始化对象
   * 
   * @param context
   *          管理器容器
   * @throws Exception
   *           如果初始化失败
   */
  public void init(ManagerContext context) throws Exception {
    for (Item item : items) {
      // 加载需要调试的类的数据
      String className = item.className;
      // HARDCODE
      String proxyName = className + "$Proxy";
      CtClass clazz = classPool.makeClass(proxyName);
      CtClass superClass = classPool.get(className);
      clazz.setSuperclass(superClass);

      // 类的调试配置文件, 如果不指定则使用全局的调试配置文件
      // 如果全局调试配置文件不存在, 则查询.class文件同目录下的.proxy文件
      String file = item.file;
      if (file == null) {
        // HARDCODE
        file = this.file != null ? this.file : className.replace('.', '/')
            + ".proxy";
      }

      BufferedReader in = new BufferedReader(new InputStreamReader(
          ProxyModule.class.getResourceAsStream("/" + file)));
      try {
        read: while (true) {
          String line = in.readLine();
          if (line == null) {
            break;
          }

          String[] words = analyzer(line);
          int size = words.length;
          if (size == 0) {
            continue;
          }

          if (size == 3 && words[2].equals(";")) {
            // 增加属性域
            clazz
                .addField(new CtField(classPool.get(words[0]), words[1], clazz));
            continue;
          } else if (size == 1) {
            addMethod(clazz, words[0], null, in);
            continue;
          } else if (size == 3
              || (size % 2 == 0 && words[1].equals("(") && words[size - 1]
                  .equals(")"))) {
            size = size / 2 - 1;
            CtClass[] parameters = new CtClass[size];
            for (int i = 0;; i++) {
              if (i == size) {
                addMethod(clazz, words[0], parameters, in);
                continue read;
              }
              if (i > 0 && !words[i * 2 + 1].equals(",")) {
                break;
              }
              parameters[i] = classPool.get(words[i * 2 + 2]);
            }
          }
          // HARDCODE
          throw new Exception("Doesn't recognize the syntax");
        }
      } catch (Exception e) {
        throw new Exception(e.getMessage() + "(" + file + ")");
      } finally {
        try {
          in.close();
        } catch (Exception e) {
        }
      }
      context.register(className, DynamicClassLoader.getClass(proxyName, clazz
          .toBytecode()));
    }
  }

  /**
   * 分析切割一行文本中的单词
   * 
   * @param line
   *          行文本
   * @return 分析切割后得到的单词序列
   */
  private String[] analyzer(String line) {
    List<String> result = new ArrayList<String>();
    int len = line.length();

    int start = -1;
    for (int i = 0; i < len; i++) {
      char c = line.charAt(i);
      if (c == '.' || Character.isJavaIdentifierPart(c)) {
        if (start < 0) {
          start = i;
        }
      } else {
        if (start >= 0) {
          result.add(line.substring(start, i));
          start = -1;
        }
        if (!Character.isWhitespace(c)) {
          result.add(Character.toString(c));
        }
      }
    }
    if (start >= 0) {
      result.add(line.substring(start));
    }
    return result.toArray(ARRAY);
  }

  /**
   * 向类中添加方法
   * 
   * @param clazz
   *          需要添加方法的类
   * @param name
   *          方法名称
   * @param parameters
   *          参数列表, 如果为NULL表示自适应参数
   * @param in
   *          文本输入器
   * @throws Exception
   *           语法错误
   */
  private void addMethod(CtClass clazz, String name, CtClass[] parameters,
      BufferedReader in) throws Exception {
    StringBuilder result = new StringBuilder(64);
    // 记录有多少组括号
    int n = 0;
    while (n >= 0) {
      String line = in.readLine();
      if (line == null) {
        break;
      }
      result.append(line);
      int len = line.length();
      for (int i = 0; i < len; i++) {
        // 识别{}等函数开始与结束符, n表示嵌套的层数
        switch (line.charAt(i)) {
        case '{':
          n++;
          continue;
        case '}':
          n--;
          if (n == 0) {
            // 添加函数
            CtMethod method;
            CtClass superClass = clazz.getSuperclass();
            while (true) {
              // 搜索同名的父类函数
              try {
                method = parameters != null ? superClass.getDeclaredMethod(
                    name, parameters) : superClass.getDeclaredMethod(name);
                break;
              } catch (NotFoundException e) {
                superClass = superClass.getSuperclass();
                if (superClass == null) {
                  throw e;
                }
              }
            }
            clazz.addMethod(CtNewMethod.make((~Modifier.SYNCHRONIZED)
                & method.getModifiers(), method.getReturnType(), name, method
                .getParameterTypes(), method.getExceptionTypes(), result
                .substring(0, result.length() - len + i + 1), clazz));
            return;
          }
          continue;
        case '\'':
          // 处理转义字符
          if (i + 2 < len) {
            if (line.charAt(++i) == '\\') {
              i++;
            }
            if (++i < len) {
              if (line.charAt(i) == '\'') {
                continue;
              }
            }
          }
          // HARDCODE
          throw new Exception("Unterminated character");
        case '"':
          // 处理字符串, 保证其完整性
          str: while (true) {
            if (++i < len) {
              switch (line.charAt(i)) {
              case '"':
                break str;
              case '\\':
                i++;
              }
              continue;
            }
            // HARDCODE
            throw new Exception("Unterminated string literal");
          }
        }
      }
    }
    // HARDCODE
    throw new Exception("There is not the end of the function");
  }
}