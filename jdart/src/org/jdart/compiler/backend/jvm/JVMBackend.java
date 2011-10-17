package org.jdart.compiler.backend.jvm;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.util.CheckClassAdapter;

import com.google.dart.compiler.DartCompilerContext;
import com.google.dart.compiler.DartSource;
import com.google.dart.compiler.LibrarySource;
import com.google.dart.compiler.ast.DartNode;
import com.google.dart.compiler.ast.DartUnit;
import com.google.dart.compiler.ast.LibraryUnit;
import com.google.dart.compiler.backend.common.AbstractBackend;
import com.google.dart.compiler.resolver.CoreTypeProvider;

public class JVMBackend extends AbstractBackend {
  private final Map<DartNode, JVMType> typeMap;
  private final LinkedHashMap<String, ClassWriter> writerMap =
      new LinkedHashMap<>();
  
  public JVMBackend(Map<DartNode, JVMType> typeMap) {
    this.typeMap = typeMap;
  }

  @Override
  public boolean isOutOfDate(DartSource src, DartCompilerContext context) {
    return false;
  }

  @Override
  public void compileUnit(DartUnit unit, DartSource src, DartCompilerContext context, CoreTypeProvider typeProvider) throws IOException {
    //System.out.println("compile unit ");
    unit.accept(new JVMGen(typeMap, writerMap).getBridge());
  }

  @Override
  public void packageApp(LibrarySource app, Collection<LibraryUnit> libraries, DartCompilerContext context, CoreTypeProvider typeProvider) throws IOException {
    //System.out.println("gen package");
    /*
    Path directory = Paths.get("gen");
    try {
      directory = Files.createDirectory(directory);
    } catch(FileAlreadyExistsException e) {
      // do nothing
    }
    */
    
    String appName = app.getName();
    int index = appName.lastIndexOf('.');
    if (index != -1) {
      appName = appName.substring(0, index);
    }
    //System.out.println(appName);
    
    try(FileOutputStream fos = new FileOutputStream(new File(appName + ".jar"));
        final JarOutputStream output = new JarOutputStream(fos)) {
      
      output.putNextEntry(new JarEntry("META-INF/MANIFEST.MF"));
      output.write(("Main-Class: "+appName+"\n").getBytes(StandardCharsets.ISO_8859_1));
      output.closeEntry();
      
      for(Entry<String, ClassWriter> entry: writerMap.entrySet()) {
        String filename = entry.getKey() +".class";
        output.putNextEntry(new JarEntry(filename));
        
        ClassWriter classWriter = entry.getValue();
        byte[] byteArray = classWriter.toByteArray();
        //CheckClassAdapter.verify(new ClassReader(byteArray), false, new PrintWriter(System.err));
        
        output.write(byteArray);
        output.closeEntry();

        //Files.write(directory.resolve(filename), classWriter.toByteArray());
      }
      
      // append runtime classes
      final Path rtClassDirectory = Paths.get("rt", "classes");
      Files.walkFileTree(rtClassDirectory, new FileVisitor<Path>() {
        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
          return FileVisitResult.CONTINUE;
        }
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
            throws IOException {
          String filename = file.toString();
          if (filename.endsWith(".class")) {
            String entryName = rtClassDirectory.relativize(file).toString();
            output.putNextEntry(new JarEntry(entryName));
            output.write(Files.readAllBytes(file));
            output.closeEntry();
          }
          return FileVisitResult.CONTINUE;
        }
        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
          throw exc;
        }
        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
          return FileVisitResult.CONTINUE;
        }
      });
    }
  }

  @Override
  public String getAppExtension() {
    return ".jar";
  }

  @Override
  public String getSourceMapExtension() {
    throw new UnsupportedOperationException();
  }
}