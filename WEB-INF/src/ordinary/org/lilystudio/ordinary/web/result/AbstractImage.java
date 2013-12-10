package org.lilystudio.ordinary.web.result;

import java.awt.Color;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.lilystudio.ordinary.web.Controller;
import org.lilystudio.ordinary.web.IRelay;

/**
 * 图片输出基类, 自动判断图片类型设置HTTP输对对象. <br>
 * 
 * @version 0.1.4, 2008/12/12
 * @author 欧阳先伟
 * @since Ordinary 0.1
 */
public abstract class AbstractImage extends AbstractResult {

  /** 透明色 */
  public static final Color TRANSLUCENT = new Color(0, 0, 0, 0);

  /**
   * 处理并输出图像数据
   * 
   * @param out
   *          用于保存输出的图像数据的流
   * @param relay
   *          数据集合
   * @param alpha
   *          是否为透明背景
   * @throws Exception
   *           图像生成错误
   */
  public abstract void execute(OutputStream out, IRelay relay, boolean alpha)
      throws Exception;

  public void execute(HttpServletRequest request, HttpServletResponse response,
      IRelay relay) throws Exception {
    // HARDCODE
    boolean alpha = "image/png".equals(Controller.getContext().getMimeType(
        relay.get("URI").toString()));
    response.setContentType(alpha ? "image/png" : "image/jpeg");
    execute(response.getOutputStream(), relay, alpha);
  }
}
