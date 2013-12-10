package org.lilystudio.ordinary.web.action;

import java.util.List;

import org.lilystudio.ordinary.web.IExecute;
import org.lilystudio.ordinary.web.IRelay;

/**
 * 如果一个HTTP请求需要调用多个数据集合的处理操作, 使用这个标签将它们合并起来顺序访问,
 * 每个子标签都会被认为是一个处理
 * 
 * @version 0.1.4, 2008/12/12
 * @author 欧阳先伟
 * @since Ordinary 0.1
 */
public class ListAction implements IExecute {

  /** 所有的动作列表 */
  private List<IExecute> action;

  public void execute(IRelay relay) throws Exception {
    if (action != null) {
      for (IExecute o : action) {
        o.execute(relay);
      }
    }
  }
}
