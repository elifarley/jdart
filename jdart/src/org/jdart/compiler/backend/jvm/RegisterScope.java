package org.jdart.compiler.backend.jvm;

import java.util.HashMap;

import org.objectweb.asm.Type;

import com.google.dart.compiler.resolver.Element;

public class RegisterScope {
  private final RegisterScope scope;
  private int slotCount;
  private final HashMap<Element, Integer> slotMap =
      new HashMap<>();

  public RegisterScope(RegisterScope scope) {
    this.scope = scope;
  }
  
  public int store(Element element, Type type) {
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
