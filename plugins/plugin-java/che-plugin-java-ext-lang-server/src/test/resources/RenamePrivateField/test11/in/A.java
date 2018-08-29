package p;
import Test.Element;

import java.util.List;

class Test {
	static class Element{
	}
	
	static class A {
		private final List<Element> fList;
		
		public A(List<Element> list) {
			fList= list;
		}
		public List<Element> getList() {
			return fList;
		}
		public void setList(List<Element> newList) {
			fList= newList;
		}
	}
	
	{ 
		A a= new A(new List<Element>());
		a.setList(a.getList());
	}
}
