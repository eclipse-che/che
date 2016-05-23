package p;
class A{
	static int field;
	static {
		String doIt= "field"; //field
		doIt= "A.field"; //A.field
		doIt= "B. #field"; //B. #field
		doIt= "p.A#field"; //p.A#field
		String dont= "x.p.A#field"; //x.p.A#field
		dont= "xp.A.field"; //xp.A.field
		dont= "B.field"; //B.field
	}	
}