package org.jdart.compiler.backend.jvm;

public class JVMInterfaceType implements JVMType {
  private final String name;

  public JVMInterfaceType(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
  
  @Override
  public int hashCode() {
    return name.hashCode();
  }
  
  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof JVMInterfaceType)) {
      return false;
    }
    return name.equals(((JVMInterfaceType)o).name);
  }
  
  @Override
  public String toString() {
    return name;
  }
}
