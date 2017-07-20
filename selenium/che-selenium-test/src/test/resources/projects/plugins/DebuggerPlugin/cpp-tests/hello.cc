// Simple Hello World

#include <iostream>
using namespace std;

class Hello {
  public:
  string sayHello(string);
};

string Hello::sayHello(string name) {
  return "Hello World, " + name + "!";
}

int main()
{
  Hello hello;
  std::cout << hello.sayHello("man") << std::endl;
  return 0;
}
