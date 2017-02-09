package p.p;

import q.*;

public class ATest {
	A aFromOtherPackageFragment;
	q.A aQualifiedFromNamesake;
	
	public void test1() {
		TestHelper.log("x");
	}
}
