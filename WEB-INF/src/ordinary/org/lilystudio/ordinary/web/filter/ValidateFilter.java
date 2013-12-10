package org.lilystudio.ordinary.web.filter;

import org.lilystudio.ordinary.web.IRelay;
import org.lilystudio.ordinary.web.UserInformation;
import org.lilystudio.ordinary.web.result.ValidateImage;

/**
 * 验证图形码的值, 每次验证成功后, 将清空图形验证码的值
 * 
 * @version 0.1.3, 2008/09/01
 * @author 欧阳先伟
 * @since Ordinary 0.1
 */
public class ValidateFilter extends DefaultFilter {
  
  /**
   * 图形码验证异常, 当使用ValidateImage后, 用户提交的验证码值与图片不符合时产生
   */
  public class ValidateException extends FilterException {

    /** 序列化编号 */
    private static final long serialVersionUID = 1L;
  }
  
  @Override
  public void execute(IRelay relay, Object value) throws FilterException {
    UserInformation info = relay.getUserInformation(true);
    if (info == null || !value.equals(info.getProperty(ValidateImage.ATTRIBUTE_NAME))) {
      // 没有验证码, 或者验证码不相等将产生异常
      throw new ValidateException();
    } else {
      // 验证成功将清除上次的信息
      info.setProperty(ValidateImage.ATTRIBUTE_NAME, null);
    }
  }
}
