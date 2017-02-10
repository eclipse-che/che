package p;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

class A{
	List<String> list= new ArrayList<String>();
	
	public List<String> getList() {
		return list;
	}
	
	public void setList(List<String> list) {
		this.list= list;
	}
}

class B {
	static {
		A a= new A();
		a.setList(new LinkedList<String>());
		List<String> list= a.getList();
		list.addAll(a.list);
	}
}