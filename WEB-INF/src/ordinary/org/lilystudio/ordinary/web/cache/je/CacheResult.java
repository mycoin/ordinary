package org.lilystudio.ordinary.web.cache.je;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.lilystudio.coder.BASE64Encoder;
import org.lilystudio.ordinary.web.Controller;
import org.lilystudio.ordinary.web.IExecute;
import org.lilystudio.ordinary.web.IRelay;
import org.lilystudio.ordinary.web.IResult;
import org.lilystudio.ordinary.web.cache.je.data.IMetaData;

/**
 * 缓存输出类, 它拦截正常的结果输出, 如果没有更新输出缓存, 否则更新缓存再输出<br>
 * 
 * <b>子标签</b>
 * 
 * <pre>
 * action--实际的逻辑处理
 * result--实际的结果控制器
 * used--是否启用缓存, 默认是true
 * </pre>
 * 
 * @version 0.1.4, 2009/01/10
 * @author 欧阳先伟
 * @since Ordinary 0.1
 */
public class CacheResult extends AbstractKeyDefine implements IResult {

  /**
   * 缓存对象
   */
  private static class Cache extends AbstractCache {

    /** 序列化编号 */
    private static final long serialVersionUID = 1L;

    /** 结果缓存包装器 */
    private transient CacheResult result;

    /** 用户数据集合 */
    private transient IRelay relay;

    /** 关键字列表 */
    private transient List<IMetaData> keys;

    /**
     * 创建缓存对象
     * 
     * @param result
     *          结果缓存包装器
     * @param relay
     *          用户数据集合
     * @param keys
     *          关键字列表
     */
    public Cache(CacheResult result, IRelay relay, List<IMetaData> keys) {
      this.result = result;
      this.relay = relay;
      this.keys = keys;
    }

    public void create() throws Exception {
      String diskPath = result.manager.getDiskRoot();
      int length = diskPath.length();
      StringBuilder s = new StringBuilder(128);
      s.append(diskPath).append(
          BASE64Encoder.encode(MessageDigest.getInstance("MD5").digest(
              keys.toString().getBytes("UTF-8"))));
      s.setLength(length + 33);
      for (int i = 21, j = length + 32; i > 0; i--, j--) {
        if (i % 2 == 1) {
          s.setCharAt(j, '/');
          j--;
        }
        char c = s.charAt(length + i);
        if (c == '/') {
          c = '-';
        } else if (c == '+') {
          c = '.';
        }
        s.setCharAt(j, c);
      }
      String url = relay.get("URI").toString();
      int index = url.lastIndexOf('.');
      s.append(System.nanoTime())
          .append(index >= 0 ? url.substring(index) : "");
      File file = new File(s.toString());
      file.getParentFile().mkdirs();
      FileOutputStream out = new FileOutputStream(file);
      try {
        CacheResponseWrapper wrapper = new CacheResponseWrapper(relay
            .getResponse());
        result.realExecute(relay.getRequest(), wrapper, relay);
        out.write(wrapper.getBytes());
      } catch (Exception e) {
        throw new InvocationTargetException(e);
      } finally {
        out.close();
      }
      setData(file.getAbsolutePath());
    }

    public void remove() {
      File file = new File((String) getData());
      // 递归删除没有内容的目录
      while (true) {
        file.delete();
        file = file.getParentFile();
        if (file == null || file.list().length > 0) {
          return;
        }
      }
    }
  }

  /**
   * 缓存输出流
   */
  private static class CacheOutputStream extends ServletOutputStream {

    /** 字节数组输出流, 临时保存输出的数据 */
    private ByteArrayOutputStream out = new ByteArrayOutputStream(65536);

    /**
     * 得到输出的数据
     * 
     * @return 被拦截的输出数据
     */
    public byte[] toByteArray() {
      return out.toByteArray();
    }

    @Override
    public void write(int c) throws IOException {
      out.write(c);
    }
  }

  /**
   * 缓存输出拦截类
   */
  private static class CacheResponseWrapper extends HttpServletResponseWrapper {

    /** 缓存输出流类 */
    private CacheOutputStream out = new CacheOutputStream();

    /** 字符输出对象 */
    private PrintWriter writer;

    /**
     * 构造输出拦截类
     * 
     * @param response
     *          原始的输出类
     * @throws Exception
     *           错误的编码
     */
    public CacheResponseWrapper(HttpServletResponse response) throws Exception {
      super(response);
      writer = new PrintWriter(new OutputStreamWriter(out, Controller
          .getEncoding()));
    }

    /**
     * 得到输出的数据
     * 
     * @return 被拦截的输出数据
     */
    public byte[] getBytes() {
      writer.flush();
      return out.toByteArray();
    }

    @Override
    public PrintWriter getWriter() {
      return writer;
    }

    @Override
    public ServletOutputStream getOutputStream() {
      return out;
    }
  }

  /** result名称 */
  private String name;

  /** 被缓存的操作处理 */
  private IExecute action;

  /** 被缓存的输出控制器 */
  private IResult result;

  /** 缓存开启标志 */
  private boolean used = true;

  /**
   * 不缓存状态下的处理
   * 
   * @param request
   *          HTTP输入对象
   * @param response
   *          HTTP输出对象
   * @param relay
   *          用户数据容器
   * @throws Exception
   *           结果处理异常
   */
  private void realExecute(HttpServletRequest request,
      HttpServletResponse response, IRelay relay) throws Exception {
    if (action != null) {
      action.execute(relay);
    }
    result.execute(request, response, relay);
  }

  public String getName() {
    return name;
  }

  public void execute(HttpServletRequest request, HttpServletResponse response,
      IRelay relay) throws Exception {
    if (used) {
      IMetaData[] keys = getKeys(relay);
      Cache cache = new Cache(this, relay, Arrays.asList(keys));
      try {
        // 取缓存文件路径
        manager.get(keys, cache);
        String path = (String) cache.getData();
        Controller.setUndoFilter(request);
        request.getRequestDispatcher(
            manager.getRoot() + path.substring(manager.getDiskRoot().length()))
            .forward(request, response);
        return;
      } catch (InvocationTargetException e) {
        // 缓存系统正常但是缓存对象无法创建时, 将实际异常抛出
        throw (Exception) e.getTargetException();
      } catch (Exception e) {
        // 当缓存系统故障时, 直接输出
      }
    }
    realExecute(request, response, relay);
  }
}
