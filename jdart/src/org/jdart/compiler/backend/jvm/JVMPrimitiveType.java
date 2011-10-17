package org.jdart.compiler.backend.jvm;

public enum JVMPrimitiveType implements JVMType {
  BOOLEAN,
  INT,
  DOUBLE,
  VOID,
  DYNAMIC,
  FUNCTION,
  NONE,
  ;
  
  @Override
  public String toString() {
    return name().toLowerCase();
  }
}
