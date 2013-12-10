package org.lilystudio.test;

public class StringUtils {

  public String escapeHTML(String s) {
    if (s == null) {
      return null;
    }
    return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
        .replace(" ", "&nbsp;");
  }

  public String escapeHTML(Number s) {
    return s.toString();
  }

  public String escapeJS(String s) {
    if (s == null) {
      return null;
    }
    return s.replace("\\", "\\\\").replace("\t", "\\t").replace("\n", "\\n")
        .replace("\"", "\\\"").replace("'", "\\'");
  }

  public String escapeAttr(String s) {
    if (s == null) {
      return null;
    }
    return escapeHTML(s).replace("\"", "&#34;");
  }

  public float parseFloat(String s) {
    return Float.valueOf(s);
  }

  public int parseInt(String s) {
    return Integer.valueOf(s);
  }
  
  public static void main(String[] args) {
    System.out.println((int) '"');
  }
}
