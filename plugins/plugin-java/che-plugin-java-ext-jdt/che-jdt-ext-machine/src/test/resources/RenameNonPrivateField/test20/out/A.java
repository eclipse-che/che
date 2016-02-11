package p;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

class A{
	List<String> items= new ArrayList<String>();
	
	public List<String> getList() {
		return items;
	}
	
	public void setList(List<String> list) {
		this.items= list;
	}
}

class B {
	static {
		A a= new A();
		a.setList(new LinkedList<String>());
		List<String> list= a.getList();
		list.addAll(a.items);
	}
}