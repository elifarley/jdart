A compiler that takes a [Dart](http://www.dartlang.org/) file and generate a jar file that can run on any [Java 7 compatible VM](http://jdk7.java.net/).

Currently only few instructions are compiled to bytecode because
I want to implement all ways of calling a function/method before
moving to something more classic.

Also the generated jar contains the whole runtime, so you can run your dart file
anywhere without installing more than just a JVM.

A first example, an helloworld written in a file named test.dart
```
main() {
  print("hello world");
}
```
is compiled to
```
public class test {
  public static void main(java.lang.String[]);
    Code:
       0: invokedynamic #18,  0             // InvokeDynamic #0:__main__:()V
       5: return        

  public static java.lang.Object __main__();
    Code:
       0: ldc           #21                 // String hello world
       2: invokedynamic #27,  0             // InvokeDynamic #1:print:(Ljava/lang/String;)V
       7: aconst_null   
       8: areturn       
}
```

Another example with a small inheritance hierarchy
```
class A {
  A() { }
  f() {
    print("A");
  }
}

class B extends A {
  B():super() {}
  f() {
    print("B");
  }
}

main() {
  var a1 = new A();
  a1.f();
  var a2 = new B();
  a2.f();
  
  final a3 = new A();
  a3.f();
  final a4 = new B();
  a4.f();
  
  A a5 = new A();
  a5.f();
  B a6 = new B();
  a6.f();
}
```
is compiled to
```
public class A {
  public A();
    Code:
       0: aload_0       
       1: invokespecial #9                  // Method java/lang/Object."<init>":()V
       4: return        

  public void A();
    Code:
       0: return        

  public java.lang.Object f();
    Code:
       0: ldc           #12                 // String A
       2: invokedynamic #25,  0             // InvokeDynamic #0:print:(Ljava/lang/String;)V
       7: aconst_null   
       8: areturn       
}
public class B extends A {
  public B();
    Code:
       0: aload_0       
       1: invokespecial #9                  // Method A."<init>":()V
       4: return        

  public void B();
    Code:
       0: aload_0       
       1: invokedynamic #19,  0             // InvokeDynamic #0:A:(LA;)V
       6: return        

  public java.lang.Object f();
    Code:
       0: ldc           #22                 // String B
       2: invokedynamic #33,  0             // InvokeDynamic #1:print:(Ljava/lang/String;)V
       7: aconst_null   
       8: areturn       
}

public class inheritance {
  public static void main(java.lang.String[]);
    Code:
       0: invokedynamic #18,  0             // InvokeDynamic #0:__main__:()V
       5: return        

  public static java.lang.Object __main__();
    Code:
       0: invokedynamic #28,  0             // InvokeDynamic #1:A:()LA;
       5: astore_0      
       6: aload_0       
       7: invokedynamic #36,  0             // InvokeDynamic #2:f:(Ljava/lang/Object;)V
      12: invokedynamic #40,  0             // InvokeDynamic #1:B:()LB;
      17: astore_1      
      18: aload_1       
      19: invokedynamic #36,  0             // InvokeDynamic #2:f:(Ljava/lang/Object;)V
      24: invokedynamic #28,  0             // InvokeDynamic #1:A:()LA;
      29: astore_2      
      30: aload_2       
      31: invokedynamic #43,  0             // InvokeDynamic #2:f:(LA;)V
      36: invokedynamic #40,  0             // InvokeDynamic #1:B:()LB;
      41: astore_3      
      42: aload_3       
      43: invokedynamic #46,  0             // InvokeDynamic #2:f:(LB;)V
      48: invokedynamic #28,  0             // InvokeDynamic #1:A:()LA;
      53: astore        4
      55: aload         4
      57: invokedynamic #43,  0             // InvokeDynamic #2:f:(LA;)V
      62: invokedynamic #40,  0             // InvokeDynamic #1:B:()LB;
      67: astore        5
      69: aload         5
      71: invokedynamic #46,  0             // InvokeDynamic #2:f:(LB;)V
      76: aconst_null   
      77: areturn       
}
```

You can notice that the compiler fully infers the type of variables declared final
and also use the context to correctly typecheck each call,
here the call to f() is a statement so each call return type is inferred as void
.

At runtime, each callsites get a specialized version of the classchecks and
if a call is polymorphic, each override get a specialized version too in order
to perform only the required runtime checks.
That's why the compiler only implement the developer mode of Dart,
said in another way, there is no way to bypass the classcheck.

The current implementation doesn't fully obey to the current spec of Dart because
all primitive types (boolean, int, double) are not nullable because I think it's
the right(tm) thing to do (i.e. the spec should change).