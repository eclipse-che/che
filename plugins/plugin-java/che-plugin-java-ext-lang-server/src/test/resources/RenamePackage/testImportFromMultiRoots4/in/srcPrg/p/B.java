package p;

import p.ATest.ATestI;
import p.ATest.ATestI.ATestIIb;
import p.A.ARunner;

public class B {
	public class BRunner implements Runnable {
		public void run() {
			(new ATestI()).new ATestII();
			new ATestIIb();
		}
	}
	ARunner ar;
}
