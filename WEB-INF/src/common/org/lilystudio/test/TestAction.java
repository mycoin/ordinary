package org.lilystudio.test;

import org.lilystudio.ordinary.web.IExecute;
import org.lilystudio.ordinary.web.IRelay;

public class TestAction implements IExecute {

  private String resultName;
  
  public void execute(IRelay relay) throws Exception {
    Object value = relay.get(resultName);
    relay.setResultName(value != null ? value.toString() : null);
  }

}
