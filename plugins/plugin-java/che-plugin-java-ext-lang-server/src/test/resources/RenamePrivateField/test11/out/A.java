package p;
import java.util.List;

class Test {
	static class Element{
	}
	
	static class A {
		private final List<Element> fElements;
		
		public A(List<Element> list) {
			fElements= list;
		}
		public List<Element> getElements() {
			return fElements;
		}
		public void setElements(List<Element> newElements) {
			fList= newElements;
		}
	}
	
	{ 
		A a= new A(new List<Element>());
		a.setElements(a.getElements());
	}
}
