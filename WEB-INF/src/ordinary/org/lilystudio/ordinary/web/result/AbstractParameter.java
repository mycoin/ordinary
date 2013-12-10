package org.lilystudio.ordinary.web.result;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

import org.lilystudio.ordinary.web.IRelay;

/**
 * 需要处理额外参数的实现基类<br>
 * 
 * <b>子标签</b>
 * 
 * <pre>
 * param--结果集中需要使用的特殊的参数定义, 支持的属性有name,value, value如果以$开始, 则后面的部门当成键值取出数据
 * </pre>
 * 
 * @see org.lilystudio.ordinary.web.IResult
 * 
 * @version 0.1.4, 2009/01/10
 * @author 欧阳先伟
 * @since Ordinary 0.1
 */
public abstract class AbstractParameter extends AbstractResult {

  /**
   * 跳转过程中需要传递的参数信息类
   */
  public class Parameter {

    /** 参数名称 */
    public String name;

    /** 参数值 */
    private String value;

    /** 设置参数的值 */
    public void set(String value) {
      if (value.length() != 0) {
        this.value = value;
      }
    }

    /**
     * 得到参数的值, 如果值名称以$开头, 表示需要从数据集合中动态取得
     * 
     * @param relay
     *          当前的数据集合
     * @return 返回的结果数值
     */
    protected Object getValue(IRelay relay) {
      if (value == null) {
        return relay.get(name);
      }
      return value;
    }
  }

  /** 参数列表 */
  protected List<Parameter> param;

  /**
   * 建立参数对象
   * 
   * @return 参数对象
   */
  public Object createParam() {
    return new Parameter();
  }

  /**
   * 设置参数放入数据集合中
   * 
   * @param relay
   *          需要保存的数据集合
   */
  protected void setParameters(IRelay relay) {
    if (param != null) {
      // 设置参数
      for (Parameter item : param) {
        if (item.value != null) {
          relay.set(item.name, item.value);
        }
      }
    }
  }

  /**
   * 获得一个能够在HTTP协议中使用的URI值
   * 
   * @param relay
   *          数据容器
   * @param uri
   *          URI的输入值, 原始字符串
   * @param encoding
   *          URI编码方式
   * @return 转换得到的URI
   * @throws IOException
   *           如果不支持编码集
   */
  protected String getURI(IRelay relay, String uri, String encoding)
      throws IOException {
    // 如果没有指定重定向的地址, 使用数据集合中URI的值作为地址,
    // 如果uri以$开始, 将在数据集合中取同名的数据作为地址
    if (uri == null) {
      uri = relay.get("URI").toString();
    } else {
      if (uri.charAt(0) == '$') {
        uri = (String) relay.get(uri.substring(1));
      }
    }
    // 对地址进行MIME编码, 将所有非ASCII字符编码成标准可显示字符
    return URLEncoder.encode(uri, encoding).replaceAll("%2F", "/").replaceAll(
        "%3A", ":");
  }

  /**
   * 添加url需要的参数
   * 
   * @param relay
   *          数据集合
   * @param url
   *          原始的url字符串
   * @param encoding
   *          字符集编码名称
   * @return 新的带参数的url字符串
   */
  protected String addParameter(IRelay relay, String url, String encoding)
      throws Exception {
    if (param != null) {
      int length = url.length();
      StringBuilder s = new StringBuilder(url);
      for (Parameter item : param) {
        Object value = item.getValue(relay);
        if (value == null) {
          continue;
        }
        s.append('&').append(item.name).append('=').append(
            URLEncoder.encode(value.toString(), encoding));
      }
      if (s.length() > length) {
        s.setCharAt(length, '?');
        return s.toString();
      }
    }
    return url;
  }
}