package tests;

public class QualifiedTests {
	static {
		p.p.ATest aQualifiedTest;
		p.p.A aQualified;
	}
	static {
		p.
		//comment
		p/*internal*/.ATest aQualifiedTest;
		p.
		p //unreadable
		/*stuff*/.A aQualified;
	}
}
