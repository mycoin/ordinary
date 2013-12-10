package org.lilystudio.ordinary.web.action;

import java.util.HashMap;
import java.util.Map;

import org.lilystudio.ordinary.web.IExecute;
import org.lilystudio.ordinary.web.IRelay;
import org.lilystudio.ordinary.web.UserInformation;

/**
 * 在session中读写需要临时存储的数据. 常用于查询/编辑操作中, 例如查询的分页,
 * 在上下翻页过程中, 就不再需要传递所有的查询条件, 只需要返回的分组信息,
 * 就可以自动恢复相应的查询条件, 或者在编辑操作进入另一个页面后,
 * 编辑完成需要返回原来的列表位置, 也是需要将这个分组信息传递即可.
 * 假定groupName的值为group:<br>
 * 1.如果数据容器中没有group的值, 则清空所有的临时数据;<br>
 * 2.如果数据容器中的group值为空字符串, 清空上一次的临时数据,
 * 将当前的数据保存成临时数据, 并且自动生成一个group值;<br>
 * 3.如果数据容器中包含group值但与临时数据中的group值不相等, 产生操作异常;<br>
 * 4.如果数据容器中包含group值且与临时数据中的group值相等,
 * 首先将数据容器中的值写入临时数据中, 然后将所有临时数据回写到数据容器中,
 * 这样临时数据部分修改后向数据容器中填充<br>
 * <b>属性</b>
 * 
 * <pre>
 * groupName--分组变量的名称, 默认为group
 * </pre>
 * 
 * @version 0.1.5, 2009/06/01
 * @author 欧阳先伟
 * @since Ordinary 0.1
 */
public class SessionAction implements IExecute {

  /**
   * 临时数据组名称错误异常
   */
  public static class GroupException extends Exception {

    /** 序号化编号 */
    private static final long serialVersionUID = 1L;
  }

  /** 临时数据的名称 */
  private static final String NAME = "_GROUP";

  /** 组变量名称 */
  private String groupName = "group";

  @SuppressWarnings("unchecked")
  public void execute(IRelay relay) throws Exception {
    // 分组session操作不会自己创建session
    UserInformation info = relay.getUserInformation(false);
    if (info != null) {
      Object group = relay.get(groupName);
      if (group == null) {
        info.setProperty(NAME, null);
      } else if (group.equals("")) {
        group = Long.toString(System.currentTimeMillis());
        info.setProperty(NAME, new HashMap<String, Object>(relay.getDataMap()));
      } else {
        Map<String, Object> tmpData = (Map<String, Object>) info
            .getProperty(NAME);
        if (tmpData == null || !group.equals(tmpData.get(groupName))) {
          throw new GroupException();
        }
        Map<String, Object> data = relay.getDataMap();
        tmpData.putAll(data);
        data.putAll(tmpData);
      }
    }
  }
}
