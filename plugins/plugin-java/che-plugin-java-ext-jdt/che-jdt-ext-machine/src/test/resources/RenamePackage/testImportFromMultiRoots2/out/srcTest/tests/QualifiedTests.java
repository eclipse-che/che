package tests;

public class QualifiedTests {
	static {
		p.p.ATest aQualifiedTest;
		q.A aQualified;
	}
	static {
		p.
		//comment
		p/*internal*/.ATest aQualifiedTest;
		q //unreadable
		/*stuff*/.A aQualified;
	}
}
