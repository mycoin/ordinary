package org.lilystudio.test;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {

  /**
   * 生成一个日期对象
   * 
   * @return 日期对象
   */
  public Date createDate() {
    return new Date();
  }

  public String formatDate(Date date, String format) {
    if (date == null) {
      return "";
    }
    return new SimpleDateFormat(format).format(date);
  }
}
