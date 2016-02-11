package p;
class A{
	static int member;
	static {
		String doIt= "member"; //member
		doIt= "A.member"; //A.member
		doIt= "B. #member"; //B. #member
		doIt= "p.A#member"; //p.A#member
		String dont= "x.p.A#field"; //x.p.A#field
		dont= "xp.A.field"; //xp.A.field
		dont= "B.field"; //B.field
	}	
}