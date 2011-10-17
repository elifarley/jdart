package org.jdart.compiler.backend.jvm;

import java.util.Arrays;
import java.util.List;

public class JVMFunctionType implements JVMType {
  private final List<JVMType> parameterTypes;
  private final JVMType returnType;
  
  public JVMFunctionType(JVMType[] parameterTypes, JVMType returnType) {
    this(Arrays.asList(parameterTypes), returnType);
  }
  
  private JVMFunctionType(List<JVMType> parameterTypes, JVMType returnType) {
    this.parameterTypes = parameterTypes;
    this.returnType = returnType;
  }
  
  public List<JVMType> getParameterTypes() {
    return parameterTypes;
  }
  public JVMType getReturnType() {
    return returnType;
  }
  
  public JVMFunctionType insertReceiverType(JVMType receiverType) {
    int size = this.parameterTypes.size();
    JVMType[] parameterTypes = new JVMType[size + 1];
    System.arraycopy(this.parameterTypes.toArray(), 0, parameterTypes, 1, size);
    parameterTypes[0] = receiverType;
    return new JVMFunctionType(parameterTypes, returnType);
  }
  
  public JVMFunctionType withReturnType(JVMType returnType) {
    if (returnType == this.returnType) {
      return this;
    }
    return new JVMFunctionType(parameterTypes.toArray(new JVMType[parameterTypes.size()]), returnType);
  }
  
  @Override
  public String toString() {
    return parameterTypes+":"+returnType;
  }

  
}
