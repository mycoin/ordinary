package org.lilystudio.javascript;

import jargs.gnu.CmdLineParser;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.lilystudio.javascript.scope.GlobalScope;
import org.lilystudio.javascript.statement.VariableStatement;
import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.Node;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.ScriptOrFnNode;

/**
 * JavaScript压缩器
 * 
 * @version 1.0.0, 2010/01/01
 * @author 欧阳先伟
 * @since Common 0.1
 */
public class JSCompressor {

  /** 仅压缩标签 */
  public final static int LABEL = 0;

  /** 为gzip/packer等提供优化 */
  public final static int FOR_GZIP = 1;

  /** 最大语义压缩率 */
  public final static int SEMANTICS = 2;

  /** 最大文本压缩率 */
  public final static int TEXT_COMPRESS = 3;

  /** 错误输出对象 */
  private ErrorReporter reporter;

  /**
   * 创建JavaScript压缩器
   */
  public JSCompressor() {

    /** 创建向控制台进行错误输出的对象 */
    this.reporter = new ErrorReporter() {

      public void warning(String message, String sourceName, int line,
          String lineSource, int lineOffset) {
        if (line < 0) {
          System.err.println("\n[WARNING] " + message);
        } else {
          System.err.println("\n[WARNING] " + line + ':' + lineOffset + ':'
              + message);
        }
      }

      public void error(String message, String sourceName, int line,
          String lineSource, int lineOffset) {
        if (line < 0) {
          System.err.println("\n[ERROR] " + message);
        } else {
          System.err.println("\n[ERROR] " + line + ':' + lineOffset + ':'
              + message);
        }
      }

      public EvaluatorException runtimeError(String message, String sourceName,
          int line, String lineSource, int lineOffset) {
        error(message, sourceName, line, lineSource, lineOffset);
        return new EvaluatorException(message);
      }
    };
  }

  /**
   * 创建JavaScript压缩器
   * 
   * @param reporter
   *          错误输出对象
   */
  public JSCompressor(ErrorReporter reporter) {
    this.reporter = reporter;
  }

  /**
   * 执行JavaScript压缩
   * 
   * @param reader
   *          JavaScript原始内容输入器
   * @param writer
   *          压缩结果输出器
   * @param keepLineno
   *          压缩的结果是否保持源文件中的行号
   * @param mode
   *          压缩模式
   * @throws Exception
   *           压缩过程中的错误
   */
  public void compress(Reader reader, Writer writer, boolean keepLineno,
      int mode) throws Exception {
    Parser parser = new Parser(new CompilerEnvirons(), reporter);
    ScriptOrFnNode root = parser.parse(reader, null, 1);

    Environment env = new Environment(keepLineno, mode);
    GlobalScope globalScope = new GlobalScope();
    StatementList statements = new StatementList();
    Node node = root.getFirstChild();
    while (node != null) {
      IStatement statement = Utils.createStatement(node, root, globalScope);
      statements.add(statement);
      node = statement.getNext();
    }
    if (statements.size() > 0) {
      globalScope.compress(statements.get(0) instanceof VariableStatement, env);
    }

    if (env.isKeepLineno()) {
      env.setLineno(1);
    }
    if (mode == TEXT_COMPRESS) {
      Writer bufWriter = new StringWriter();
      statements.write(bufWriter, env);
      writer
          .write("eval((function(s){function g(s,i){return(s.charCodeAt(i++)-32)*95+s.charCodeAt(i)-32}var d=[],i=95,j=95,c=g(s,0),r=[],p,t,o;for(;i--;)d[i]=String.fromCharCode(i+32);r.push(p=d[c]);for(i=2;i<s.length;i+=2){c=g(s,i);t=d[c];o=(t||p).charAt(0);r.push(t?t:p+o);d[j++]=p+o;p=t}return r.join('')})(");
      writer.write(Utils.escapeJSString(encode(bufWriter.toString())));
      writer.write("))");
    } else {
      statements.write(writer, env);
    }
  }

  private String escapeIndex(int index) {
    return String.valueOf((char) (index / 95 + 32))
        + String.valueOf((char) (index % 95 + 32));
  }

  private String encode(String s) {
    Map<String, Integer> dict = new HashMap<String, Integer>();
    String prefix = "";
    int j = 95;
    StringBuilder result = new StringBuilder();

    for (int i = 0; i < 95; i++) {
      dict.put(String.valueOf((char) (i + 32)), i);
    }

    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      String key = prefix + c;
      if (dict.containsKey(key)) {
        prefix = key;
      } else {
        if (j < 95 * 95) {
          dict.put(key, j++);
        }
        result.append(escapeIndex(dict.get(prefix)));
        prefix = String.valueOf(c);
      }
    }

    result.append(escapeIndex(dict.get(prefix)));
    return result.toString();
  }

  public static void main(String[] args) {
    CmdLineParser parser = new CmdLineParser();
    CmdLineParser.Option modeOpt = parser.addStringOption("mode");
    CmdLineParser.Option linebreakOpt = parser.addBooleanOption("line-break");
    CmdLineParser.Option charsetOpt = parser.addStringOption("charset");
    CmdLineParser.Option outputFilenameOpt = parser.addStringOption('o',
        "output");

    Reader in = null;
    Writer out = null;

    try {
      parser.parse(args);
      int mode = 1;
      try {
        mode = Integer.valueOf((String) parser.getOptionValue(modeOpt));
      } catch (Exception e) {
      }
      boolean linebreak = parser.getOptionValue(linebreakOpt) != null;
      String charset = (String) parser.getOptionValue(charsetOpt);
      if (charset == null) {
        charset = System.getProperty("file.encoding");
        if (charset == null) {
          charset = "UTF-8";
        }
      }
      String outputFilename = (String) parser.getOptionValue(outputFilenameOpt);

      String[] fileArgs = parser.getRemainingArgs();
      if (fileArgs.length == 0 || outputFilename == null) {
        System.out.println("参数错误");
        System.exit(1);
      }

      String inputFilename = fileArgs[0];
      in = new InputStreamReader(new FileInputStream(inputFilename), charset);
      out = new OutputStreamWriter(new FileOutputStream(outputFilename),
          charset);
      new JSCompressor().compress(in, out, linebreak, mode);
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    } finally {
      if (in != null) {
        try {
          in.close();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }

      if (out != null) {
        try {
          out.close();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
  }
}
