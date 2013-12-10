package org.lilystudio.test;

import java.util.Date;

import org.lilystudio.ordinary.web.IExecute;
import org.lilystudio.ordinary.web.IRelay;

public class Utils implements IExecute {
  
  public class Item {
    public String getTaskId() {
      return "1";
    }
    
    public String getCustomerId() {
      return "001";
    }

    public String getCustomerName() {
      return "客户A";
    }
    
    public String getTaskCategoryTypeName() {
      return "任务类型1";
    }
    
    public String getManagerId() {
      return "管理员1";
    }
    
    public Date getGenerateDate() {
      return new Date();
    }
    
    public String getStatusName() {
      return "未处理";
    }
    
    public Date getLastProcessDate() {
      return new Date();
    }
    
    public int getStatus() {
      return 0;
    }
    
    public int getPriority() {
      return 1;
    }
  }

  public Item create() {
    return new Item();
  }
  
  public void execute(IRelay relay) throws Exception {
    relay.set("TEMP", this);
  }
}
