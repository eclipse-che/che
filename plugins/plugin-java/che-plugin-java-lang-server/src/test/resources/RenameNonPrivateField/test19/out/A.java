package p;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

class A{
	List<String> items= new ArrayList<String>();
	
	public List<String> getItems() {
		return items;
	}
	
	public void setItems(List<String> list) {
		this.items= list;
	}
}

class B {
	static {
		A a= new A();
		a.setItems(new LinkedList<String>());
		List<String> list= a.getItems();
		list.addAll(a.items);
	}
}