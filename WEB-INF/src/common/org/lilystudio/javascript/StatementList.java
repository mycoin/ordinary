package org.lilystudio.javascript;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;

import org.lilystudio.javascript.statement.BlockStatement;
import org.lilystudio.javascript.statement.EmptyStatement;

/**
 * 语句列表
 * 
 * @version 1.0.0, 2010/01/01
 * @author 欧阳先伟
 * @since Common 0.1
 */
public class StatementList extends ArrayList<IStatement> implements IWriteable {

  private static final long serialVersionUID = 1L;

  public void write(Writer writer, Environment env) throws IOException {
    boolean flag = false;
    for (IStatement statement : this) {
      INode parent = statement.getParent();
      if (statement instanceof EmptyStatement
          && (parent == null || parent instanceof BlockStatement)) {
        continue;
      }

      if (env.isKeepLineno()) {
        while (statement.getLineno() > env.getLineno()) {
          writer.write("\n");
          env.setLineno(env.getLineno() + 1);
        }
      }

      StringWriter s = new StringWriter();
      statement.write(s, env);

      if (s.toString().length() > 0) {
        if (flag) {
          writer.write(";");
        } else {
          flag = true;
        }
        writer.write(s.toString());
      }

      if (!statement.isNeedRightSeparator()) {
        flag = false;
      }
    }
  }
}
