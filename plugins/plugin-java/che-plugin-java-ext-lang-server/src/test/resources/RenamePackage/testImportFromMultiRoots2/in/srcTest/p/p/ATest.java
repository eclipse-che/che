package p.p;

import p.p.*; //myself

public class ATest {
	A aFromOtherPackageFragment;
	p.p.A aQualifiedFromNamesake;
	
	public void test1() {
		TestHelper.log("x");
	}
}
