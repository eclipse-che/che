package a.b.c;

import p.ATest;

public class A {
	public class ARunner implements Runnable {
		public void run() {
			new ATest.ATestI();
			new ATest.ATestI.ATestIIb();
		}
	}
	B.BRunner br;
}
