package org.jdart.main;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.dart.compiler.CommandLineOptions.DartRunnerOptions;
import com.google.dart.compiler.CompilerConfiguration;
import com.google.dart.compiler.DartArtifactProvider;
import com.google.dart.compiler.DartCompilationError;
import com.google.dart.compiler.DartCompiler;
import com.google.dart.compiler.DartCompilerListener;
import com.google.dart.compiler.DefaultDartCompilerListener;
import com.google.dart.compiler.DefaultErrorFormatter;
import com.google.dart.compiler.LibrarySource;
import com.google.dart.compiler.Source;
import com.google.dart.compiler.UrlLibrarySource;
import com.google.dart.runner.RunnerError;

public class Main {
  public static void main(String[] args) throws RunnerError, IOException {
    throwingMain(args, System.out, System.err);   
  }

  public static void throwingMain(String[] args,
      PrintStream stdout,
      final PrintStream stderr)
          throws RunnerError, IOException {

    if (args.length != 1) {
      System.err.println("jdart, a Dart to Java compiler");
      System.err.println("  synopsis: jdart yourfile.dart");
      System.exit(1);
    }
    String script = args[0];
    ArrayList<String> scriptArguments = new ArrayList<>();

    LibrarySource app = new UrlLibrarySource(new File(script));

    ArrayList<LibrarySource> imports = new ArrayList<>();

    DefaultDartCompilerListener listener = new DefaultDartCompilerListener() {
      {
        ((DefaultErrorFormatter) formatter).setOutputStream(stderr);
      }
      @Override
      public void compilationWarning(DartCompilationError event) {
        compilationError(event);
      }
    };

    compileApp(app, imports, new DartRunnerOptions(), listener);

    if (listener.getProblemCount() != 0) {
      throw new RunnerError("Compilation failed.");
    }
  }

  private static class RunnerDartArtifactProvider extends DartArtifactProvider {
    private final Map<String, StringWriter> artifacts = new ConcurrentHashMap<>();

    RunnerDartArtifactProvider() {
      // avoid to create a private constructor
    }

    @Override
    public Reader getArtifactReader(Source source, String part, String ext) {
      String key = getKey(source, part, ext);
      StringWriter w = artifacts.get(key);
      if (w == null) {
        return null;
      }
      return new StringReader(w.toString());
    }

    @Override
    public URI getArtifactUri(Source source, String part, String ext) {
      String key = getKey(source, part, ext);
      return URI.create(key);
    }

    @Override
    public Writer getArtifactWriter(Source source, String part, String ext) {
      StringWriter w = new StringWriter();
      String key = getKey(source, part, ext);
      StringWriter oldValue = artifacts.put(key, w);
      if (oldValue != null) {
        throw new RuntimeException("Can only write artifact once for " + key);
      }
      return w;
    }

    private String getKey(Source source, String part, String ext) {
      String keyPart = (part.isEmpty()) ? "" : "$" + part;
      return source.getName() + keyPart + "." + ext;
    }

    public String getGeneratedFileContents(String name) {
      StringWriter w = artifacts.get(name);
      if (w == null) {
        return null;
      }
      return w.toString();
    }

    @Override
    public boolean isOutOfDate(Source source, Source base, String ext) {
      return true;
    }
  }

  private static void compileApp (LibrarySource app, List<LibrarySource> imports,
      DartRunnerOptions options, DartCompilerListener listener) throws RunnerError, IOException {
    CompilerConfiguration config = new SimpleCompilerConfiguration();
    compileApp(app, imports, config, listener);
  }

  /**
   * Parses and compiles an application to Javascript.
   * @throws IOException 
   */
  private static void compileApp(LibrarySource app,
      List<LibrarySource> imports,
      CompilerConfiguration config,
      DartCompilerListener listener) throws RunnerError, IOException {

    final RunnerDartArtifactProvider provider = new RunnerDartArtifactProvider();
    String errmsg = DartCompiler.compileLib(app, imports, config, provider, listener);
    if (errmsg != null) {
      throw new RunnerError(errmsg);
    }
  }
}

