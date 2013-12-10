package org.lilystudio.coder;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * JSON编码器, 允许自定义序列化对象的处理方式, 例如: <code>
 * Map<Class<?>, ISerialize> custom = new HashMap<Class<?>, ISerialize>();
 * custom.put(java.util.Date.class, new ISerialize {
 *   public String serialize(Object o) {
 *     return new SimpleDateFormat((Date) o, "yyyy-MM-dd");
 *   }
 * });
 * JSONEncoder.encoder(o, custom);
 * </code>
 * 
 * @version 0.1.5, 2009/06/01
 * @author 欧阳先伟
 * @since Common 0.1
 */
public class JSONEncoder {

  /**
   * 序列化对象接口, 用于自定义特定的序列化对象方式
   */
  public static interface ISerialize {

    /**
     * 序列化对象方法
     * 
     * @param o
     *          需要序列化的对象
     * @return 对象对应的字符串
     */
    String serialize(Object o);
  }

  /**
   * 将数据对象JSON序列化
   * 
   * @param value
   *          需要序列化的JSON对象
   * @return 序列化的JSON字符串
   */
  public static String encode(Object o) {
    return encode(o, null);
  }

  /**
   * 按指定的规则将数据对象JSON序列化
   * 
   * @param value
   *          需要序列化的JSON对象
   * @param custom
   *          自定义序列化对象配置信息
   * @return 序列化的JSON字符串
   */
  public static String encode(Object o, Map<Class<?>, ISerialize> custom) {
    StringBuilder sb = new StringBuilder(256);
    serialize(sb, o, new ArrayList<Object>(), custom);
    return sb.toString();
  }

  /**
   * 序列化对象
   * 
   * @param sb
   *          序列化时的字符串缓冲区
   * @param o
   *          需要序列化的对象
   * @param cached
   *          正在被递归序列化的对象集合, 防止冲突
   * @param custom
   *          自定义序列化对象配置信息
   */
  private static void serialize(StringBuilder sb, Object o,
      Collection<Object> cached, Map<Class<?>, ISerialize> custom) {
    if (o == null) {
      sb.append("null");
    } else if (o instanceof Boolean || o instanceof Number) {
      sb.append(o.toString());
    } else if (o instanceof String || o instanceof Character) {
      serialize(sb, o.toString());
    } else {
      // 防止在属性递归序列化中，一个对象被序列化多次, 避免死循环
      for (Object object : cached) {
        if (object == o) {
          sb.append("null");
          return;
        }
      }
      cached.add(o);
      if (o instanceof Map) {
        Map<?, ?> map = (Map<?, ?>) o;
        sb.append('{');
        for (Map.Entry<?, ?> entry : map.entrySet()) {
          serialize(sb, entry.getKey().toString(), entry.getValue(), cached,
              custom);
        }
        sb.setCharAt(sb.length() - 1, '}');
      } else if (o instanceof List) {
        sb.append('[');
        for (Object value : (List<?>) o) {
          serialize(sb, value, cached, custom);
          sb.append(',');
        }
        sb.setCharAt(sb.length() - 1, ']');
      } else if (o.getClass().isArray()) {
        sb.append('[');
        int len = Array.getLength(o);
        for (int i = 0; i < len; i++) {
          serialize(sb, Array.get(o, i), cached, custom);
          sb.append(',');
        }
        sb.setCharAt(sb.length() - 1, ']');
      } else {
        if (custom != null) {
          // 检查对象是否需要自定义序列化操作
          for (Map.Entry<Class<?>, ISerialize> entry : custom.entrySet()) {
            if (entry.getKey().isAssignableFrom(o.getClass())) {
              serialize(sb, entry.getValue().serialize(o));
              return;
            }
          }
        }
        sb.append('{');
        try {
          // 序列化JavaBean可读属性
          for (PropertyDescriptor prop : Introspector.getBeanInfo(o.getClass())
              .getPropertyDescriptors()) {
            Method accessor = prop.getReadMethod();
            if (accessor != null) {
              String name = prop.getName();
              // class属性不需要序列化
              if (!"class".equals(name)) {
                serialize(sb, name, accessor.invoke(o), cached, custom);
              }
            }
          }
          // 序列化公共属性
          for (Field field : o.getClass().getFields()) {
            serialize(sb, field.getName(), field.get(o), cached, custom);
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
        sb.setCharAt(sb.length() - 1, '}');
      }
      cached.remove(o);
    }
  }

  /**
   * 序列化文本
   * 
   * @param sb
   *          序列化时的字符串缓冲区
   * @param text
   *          需要序列化的文本
   */
  private static void serialize(StringBuilder sb, String text) {
    sb.append('"');
    int length = text.length();
    for (int i = 0; i < length; i++) {
      char c = text.charAt(i);
      switch (c) {
      case '"':
      case '\\':
        sb.append('\\').append(c);
        break;
      case '\b':
        sb.append("\\b");
        break;
      case '\f':
        sb.append("\\f");
        break;
      case '\n':
        sb.append("\\n");
        break;
      case '\r':
        sb.append("\\r");
        break;
      case '\t':
        sb.append("\\t");
        break;
      default:
        if (Character.isISOControl(c)) {
          sb.append("\\u").append(Integer.toHexString(0x10000 + c), 1, 5);
        } else {
          sb.append(c);
        }
      }
    }
    sb.append('"');
  }

  /**
   * 序列化键值对
   * 
   * @param sb
   *          序列化时的字符串缓冲区
   * @param name
   *          需要序列化的键名称
   * @param value
   *          需要序列化的值对象
   * @param cached
   *          正在被递归序列化的对象集合, 防止冲突
   * @param custom
   *          自定义序列化对象配置信息
   */
  private static void serialize(StringBuilder sb, String name, Object value,
      Collection<Object> cached, Map<Class<?>, ISerialize> custom) {
    serialize(sb, name);
    sb.append(':');
    serialize(sb, value, cached, custom);
    sb.append(',');
  }
}
