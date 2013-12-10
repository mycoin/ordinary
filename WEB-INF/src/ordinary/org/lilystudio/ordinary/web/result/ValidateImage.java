package org.lilystudio.ordinary.web.result;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;

import javax.imageio.ImageIO;

import org.lilystudio.ordinary.web.IRelay;

/**
 * 生成随机数字或字母串, 以图像方式显示, 用于人工识别, 使程序很难识别.
 * 减小系统被程序自动攻击的可能性. 生成的图形颜色由红、黑、蓝、紫4中随机组合而成,
 * 数字或字母垂直方向位置在一定范围内也是随机的, 减少被程序自动识别的几率.
 * 由于数字的0,1,2易和字母的o,l,z混淆, 使人眼难以识别,
 * 因此不生成数字和字母的混合串, 生成的串字母统一使用小写. <br>
 * <b>属性</b>
 * 
 * <pre>
 * charHeight--字符的高度, 默认为10
 * charWidth--字符的宽度, 默认为15
 * fontSize--字符的大小, 默认为16
 * count--要生成的字符个数, 默认4个
 * type--指定输出字符或者数字, 只能是number(默认)或者alpha
 * </pre>
 * 
 * @version 0.1.4, 2008/12/12
 * @author 欧阳先伟
 * @since Ordinary 0.1
 */
public class ValidateImage extends AbstractImage {

  /** 保存在session中的属性名称 */
  public static final String ATTRIBUTE_NAME = "_VALIDATE";

  /** 颜色数组, 绘制字串时随机选择一个 */
  private static final Color[] CHAR_COLOR = { Color.RED, Color.BLUE,
      Color.GREEN, Color.MAGENTA };

  /** 垂直方向起始位置 */
  private static final int INITYPOS = 5;

  /** 数字串模式 */
  private static final int NUMBER = 0;

  /** 字母串模式 */
  private static final int ALPHA = 1;

  /** 字符占用的高度, 单位为像素 */
  private int charHeight = 10;

  /** 字符占用的宽度, 单位为像素 */
  private int charWidth = 15;

  /** 字符大小 */
  private int fontSize = 16;

  /** 要生成的字符个数 */
  private int count = 4;

  /** 随机数生成器 */
  private Random r = new Random(System.currentTimeMillis());

  /** 设置输出为字母或者数字 */
  private int type = NUMBER;

  /**
   * 设置格式字符串是纯数字或者是纯字母
   * 
   * @param value
   *          配置文件中定义的参数
   */
  public void setType(String value) throws Exception {
    // HARDCODE
    if (value.equals("number")) {
      type = NUMBER;
    } else if (value.equals("alpha")) {
      type = ALPHA;
    } else {
      throw new Exception("The type don't support");
    }
  }

  /**
   * 随机生成一个数字串, 并以图像方式绘制, 绘制结果输出到流out中
   * 
   * @param out
   *          图像结果输出流
   * @param alpha
   *          是否为透明背景
   * @return 随机生成的串的值
   * @throws IOException
   */
  public String drawNumber(OutputStream out, boolean alpha) throws IOException {
    String charValue = "";
    for (int i = 0; i < count; i++) {
      charValue += String.valueOf(randomInt(0, 10));
    }
    return draw(charValue, out, alpha);
  }

  /** */
  /**
   * 随机生成一个字母串, 并以图像方式绘制, 绘制结果输出到流out中
   * 
   * @param out
   *          图像结果输出流
   * @param alpha
   *          是否为透明背景
   * @return 随机生成的串的值
   * @throws IOException
   */
  public String drawAlpha(OutputStream out, boolean alpha) throws IOException {
    // 随机生成的串的值
    String charValue = "";
    for (int i = 0; i < count; i++) {
      char c = (char) (randomInt(0, 26) + 'a');
      charValue += String.valueOf(c);
    }
    return draw(charValue, out, alpha);
  }

  /**
   * 以图像方式绘制字符串, 绘制结果输出到流out中
   * 
   * @param charValue
   *          要绘制的字符串
   * @param out
   *          图像结果输出流
   * @param alpha
   *          是否为透明背景
   * @return 随机生成的串的值
   * @throws IOException
   */
  private String draw(String charValue, OutputStream out, boolean alpha)
      throws IOException {

    // 计算图像的宽度和高度
    int w = (count + 2) * charWidth;
    int h = charHeight * 3;

    // 创建内存图像区
    BufferedImage bi = new BufferedImage(w, h,
        alpha ? BufferedImage.TYPE_4BYTE_ABGR : BufferedImage.TYPE_3BYTE_BGR);
    Graphics2D g = bi.createGraphics();

    // 设置背景色
    g.setBackground(alpha ? TRANSLUCENT : Color.WHITE);
    g.fillRect(0, 0, w, h);

    // 设置font
    g.setFont(new Font(null, Font.BOLD, fontSize));
    // 绘制charValue,每个字符颜色随机
    for (int i = 0; i < count; i++) {
      String c = charValue.substring(i, i + 1);
      g.setColor(CHAR_COLOR[randomInt(0, CHAR_COLOR.length)]);
      int xpos = (i + 1) * charWidth + randomInt(0, charWidth / 3);
      // 垂直方向上随机
      int ypos = INITYPOS + randomInt(charHeight, charHeight * 2);
      g.drawString(c, xpos, ypos);
    }
    g.dispose();
    bi.flush();
    // 输出到流
    ImageIO.write(bi, alpha ? "png" : "jpeg", out);

    return charValue;
  }

  /**
   * 返回[from,to)之间的一个随机整数
   * 
   * @param from
   *          起始值
   * @param to
   *          结束值
   * @return [from,to)之间的一个随机整数
   */
  protected int randomInt(int from, int to) {
    return from + r.nextInt(to - from);
  }

  @Override
  public void execute(OutputStream out, IRelay relay, boolean alpha)
      throws Exception {
    relay.getUserInformation(true).setProperty(ATTRIBUTE_NAME,
        type == ALPHA ? drawAlpha(out, alpha) : drawNumber(out, alpha));
  }
}
