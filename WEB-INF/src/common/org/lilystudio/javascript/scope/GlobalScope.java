package org.lilystudio.javascript.scope;

/**
 * 全局生存域
 * 
 * @version 1.0.0, 2010/01/01
 * @author 欧阳先伟
 * @since Common 0.1
 */
public class GlobalScope extends Scope {

  /**
   * 创建全局生存域
   */
  public GlobalScope() {
    super(null);
  }

  @Override
  public Identifier addIdentifier(String name) {
    Identifier identifier = registerLocalIdentifier(name);
    identifier.inc();
    return identifier;
  }
}
