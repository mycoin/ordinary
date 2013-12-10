package org.lilystudio.test;

import org.lilystudio.ordinary.web.IExecute;
import org.lilystudio.ordinary.web.IRelay;

public class UtilsFilter implements IExecute {
  private String name;
  
  private Object obj;
  
  public void setClassName(String name) throws Exception {
    obj = Class.forName(name).newInstance();
  }
  
  public void execute(IRelay relay) throws Exception {
    relay.set(name, obj);
  }
}
