package org.jdart.compiler.backend.jvm;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

import com.google.dart.compiler.DartCompilationPhase;
import com.google.dart.compiler.DartCompilerContext;
import com.google.dart.compiler.ast.DartNode;
import com.google.dart.compiler.ast.DartUnit;
import com.google.dart.compiler.ast.LibraryUnit;
import com.google.dart.compiler.resolver.CoreTypeProvider;

public class JVMTypeCheckerPhase implements DartCompilationPhase {
  private final TypeChecker typeChecker;
  private final HashSet<LibraryUnit> librarySet =
      new HashSet<>();
  
  public JVMTypeCheckerPhase(Map<DartNode, JVMType> typeMap) {
    this.typeChecker = new TypeChecker(typeMap);
  }

  @Override
  public DartUnit exec(DartUnit unit, DartCompilerContext context, CoreTypeProvider typeProvider) {
    // always typecheck all imports before typechecking a unit
    LibraryUnit library = unit.getLibrary();
    for(LibraryUnit importLibrary: library.getImports()) {
      typeCheck(importLibrary);
    }
    
    // then typecheck the current library
    typeCheck(library);
    
    return unit;
  }
  
  private void typeCheck(LibraryUnit libraryUnit) {
    if (librarySet.contains(libraryUnit)) {
      // already typechecked
      return; 
    }
    librarySet.add(libraryUnit);
    
    for(DartUnit unit: libraryUnit.getUnits()) {
      unit.accept(typeChecker.getBridge());  
    }
  }
}
