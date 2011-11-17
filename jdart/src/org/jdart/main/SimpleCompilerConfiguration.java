package org.jdart.main;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.jdart.compiler.backend.jvm.JVMBackend;
import org.jdart.compiler.backend.jvm.JVMType;
import org.jdart.compiler.backend.jvm.JVMTypeCheckerPhase;

import com.google.dart.compiler.Backend;
import com.google.dart.compiler.CommandLineOptions.CompilerOptions;
import com.google.dart.compiler.CompilerConfiguration;
import com.google.dart.compiler.DartCompilationPhase;
import com.google.dart.compiler.LibrarySource;
import com.google.dart.compiler.UrlLibrarySource;
import com.google.dart.compiler.ast.DartNode;
import com.google.dart.compiler.metrics.CompilerMetrics;
import com.google.dart.compiler.resolver.Resolver.Phase;
import com.google.dart.compiler.type.TypeAnalyzer;

public class SimpleCompilerConfiguration implements CompilerConfiguration {
  private final UrlLibrarySource librarySource = new UrlLibrarySource(new File("core.dart"));
  private final HashMap<DartNode, JVMType> typeMap =
      new HashMap<>();
  
  @Override
  public List<DartCompilationPhase> getPhases() {
    return Arrays.asList(new Phase(), new TypeAnalyzer(), new JVMTypeCheckerPhase(typeMap));
  }

  private final JVMBackend backend = new JVMBackend(typeMap);
  
  @Override
  public List<Backend> getBackends() {
    return Collections.<Backend>singletonList(backend);
  }

  @Override
  public boolean developerModeChecks() {
    return true;
  }

  @Override
  public boolean shouldOptimize() {
    return true;
  }

  @Override
  public CompilerMetrics getCompilerMetrics() {
    return null;
  }

  @Override
  public String getJvmMetricOptions() {
    return null;
  }

  @Override
  public boolean typeErrorsAreFatal() {
    return true;
  }

  @Override
  public boolean warningsAreFatal() {
    return true;
  }

  @Override
  public boolean resolveDespiteParseErrors() {
    return false;
  }

  @Override
  public boolean incremental() {
    return false;
  }

  @Override
  public File getOutputFilename() {
    return null;
  }

  @Override
  public File getOutputDirectory() {
    return null;
  }

  @Override
  public boolean checkOnly() {
    return false;
  }

  @Override
  public boolean expectEntryPoint() {
    return true;
  }

  @Override
  public boolean shouldWarnOnNoSuchType() {
    return true;
  }

  @Override
  public boolean collectComments() {
    return false;
  }

  @Override
  public LibrarySource getSystemLibraryFor(String importSpec) {
    //System.out.println("importSpec "+importSpec);
    return librarySource;
  }

  @Override
  public CompilerOptions getCompilerOptions() {
    return new CompilerOptions();
  }

  @Override
  public boolean printMachineProblems() {
    return false;
  }

}
