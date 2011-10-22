package org.jdart.compiler.backend.jvm;

import java.util.HashMap;

import org.objectweb.asm.Type;

import com.google.dart.compiler.resolver.Element;

public class RegisterScope {
  private final RegisterScope scope;
  private int slotCount;
  private final HashMap<Element, Integer> slotMap =
      new HashMap<>();

  public RegisterScope(RegisterScope scope, boolean isStatic) {
    this.scope = scope;
    this.slotCount = (isStatic)? 0: 1;
  }
  
  public int store(Element element, Type type) {
    assert !slotMap.containsKey(element);
    
    int slot = slotCount;
    slotMap.put(element, slot);
    slotCount += type.getSize();
    return slot;
  }
  
  public int load(Element element) {
    Integer slot = slotMap.get(element);
    if (slot != null) {
      return slot;
    }
    if (scope == null) {
      throw new AssertionError("unknwon element "+element);
    }
    return scope.load(element);
  }
}
