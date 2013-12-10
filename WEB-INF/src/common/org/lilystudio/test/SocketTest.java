package org.lilystudio.test;

import java.util.HashMap;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.lilystudio.ordinary.web.IRelay;
import org.lilystudio.ordinary.web.result.AbstractResult;

public class SocketTest extends AbstractResult {

  private static HashMap<String, ServletOutputStream> servlet = new HashMap<String, ServletOutputStream>();

  private static byte[] EMPTY_DATA = "PCTP/1.0 200\nContent-Length: 0\n\n".getBytes();

  public void execute(HttpServletRequest request, HttpServletResponse response,
      IRelay relay) throws Exception {
    ServletOutputStream out = response.getOutputStream();
    Object login = relay.get("login");

    try {
      if (login != null) {
        servlet.put((String) login, out);
        out.write(EMPTY_DATA);
        out.write(EMPTY_DATA);
        out.write(EMPTY_DATA);
        out.write(EMPTY_DATA);
        out.write(EMPTY_DATA);
        out.write(EMPTY_DATA);
        out.write(EMPTY_DATA);
        out.write(EMPTY_DATA);
        out.write(EMPTY_DATA);
        out.write(EMPTY_DATA);
        out.write(EMPTY_DATA);
        out.write(EMPTY_DATA);
        out.write(EMPTY_DATA);
        out.write(EMPTY_DATA);
        out.write(EMPTY_DATA);
        out.write(EMPTY_DATA);
        out.write(EMPTY_DATA);
        out.write(EMPTY_DATA);
        out.write(EMPTY_DATA);
        out.write(EMPTY_DATA);
        out.write(EMPTY_DATA);
        out.write(EMPTY_DATA);
        out.write(EMPTY_DATA);
        out.write(EMPTY_DATA);
        out.write(EMPTY_DATA);
        out.write(EMPTY_DATA);
        out.write(EMPTY_DATA);
        out.write(EMPTY_DATA);
        out.write(EMPTY_DATA);
        out.write(EMPTY_DATA);
        out.write(EMPTY_DATA);
        out.write(EMPTY_DATA);
        out.write(EMPTY_DATA);
        out.write(EMPTY_DATA);
        out.write(EMPTY_DATA);
        out.write(EMPTY_DATA);
        out.write(EMPTY_DATA);
        out.write(EMPTY_DATA);
        out.write(EMPTY_DATA);
        out.write(EMPTY_DATA);
        out.write(EMPTY_DATA);
        out.write(EMPTY_DATA);
        out.write(EMPTY_DATA);
        out.write(EMPTY_DATA);
        out.write(EMPTY_DATA);
        out.write(EMPTY_DATA);
        out.write(EMPTY_DATA);
        out.write(EMPTY_DATA);
        out.write(EMPTY_DATA);
        out.write(EMPTY_DATA);
        out.write(EMPTY_DATA);
        out.write(EMPTY_DATA);
        out.write(EMPTY_DATA);
        out.write(EMPTY_DATA);
        out.write(EMPTY_DATA);
        out.write(EMPTY_DATA);
        out.write(EMPTY_DATA);
        out.write(EMPTY_DATA);
        out.write(EMPTY_DATA);
        out.write(EMPTY_DATA);
        out.write(EMPTY_DATA);
        out.write(EMPTY_DATA);
        out.write(EMPTY_DATA);
        out.write(EMPTY_DATA);
        out.flush();

        while (true) {
          Thread.sleep(5000);
          out.write(EMPTY_DATA);
          out.flush();
        }
      } else {
        String action = (String) relay.get("action");
        if (action != null) {
          for (String key : servlet.keySet()) {
            if (key != login) {
              ServletOutputStream os = servlet.get(key);
              os.write(("PCTP/1.0 200\nContent-Length: " + action.length() + "\n\n" + action)
                  .getBytes());
              os.flush();
            }
          }
        }
      }
    } catch (Exception ex) {
      ex.printStackTrace();
      servlet.remove(login);
    }
  }
}
