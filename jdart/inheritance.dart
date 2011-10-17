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
