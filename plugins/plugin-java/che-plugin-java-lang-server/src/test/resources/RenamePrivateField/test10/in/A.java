package p;
import java.util.List;

class Test {
	static class Element{
	}
	
	static class A {
		private final List<Element> fList;
		
		public A(List<Element> list) {
			fList= list;
		}
	}
}

