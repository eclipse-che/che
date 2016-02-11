package p;

public class Bla {
	
	Bla blaReturn() { return null; }
	void blaArg(Bla arg) { }
	void blaElement(java.util.List<Bla> argList) { }
	
	void tryDont() { }
	<T extends Bla> void tryC(int tryKind) {	}
	void tryA(@Constants(Bla.class) int tryKind) { }
	void tryB(int tryKind) {
		@Constants(Bla.class) int tryCopy, tryCopy2= tryKind;
	}
	@Constants(value= Bla.class) Object fTryA, fTryB;
}

@interface Constants {
	Class<?> value();
}