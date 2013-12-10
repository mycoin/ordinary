package org.lilystudio.javascript;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

/**
 * 表达式列表
 * 
 * @version 1.0.0, 2010/01/01
 * @author 欧阳先伟
 * @since Common 0.1
 */
public class ExpressionList extends ArrayList<IExpression> implements
    IWriteable {

  private static final long serialVersionUID = 1L;

  public void write(Writer writer, Environment env) throws IOException {
    boolean flag = false;
    for (IExpression expression : this) {
      if (flag) {
        writer.write(",");
      } else {
        flag = true;
      }
      if (env.isKeepLineno()) {
        while (expression.getLineno() > env.getLineno()) {
          writer.write("\n");
          env.setLineno(env.getLineno() + 1);
        }
      }
      expression.write(writer, env);
    }
  }
}
