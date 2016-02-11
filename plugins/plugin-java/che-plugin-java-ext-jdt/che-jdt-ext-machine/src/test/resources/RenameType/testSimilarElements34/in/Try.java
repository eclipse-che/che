package p;

public class Try {
	
	Try tryReturn() { return null; }
	void tryArg(Try arg) { }
	void tryElement(java.util.List<Try> argList) { }
	
	void tryDont() { }
	<T extends Try> void tryC(int tryKind) {	}
	void tryA(@Constants(Try.class) int tryKind) { }
	void tryB(int tryKind) {
		@Constants(Try.class) int tryCopy, tryCopy2= tryKind;
	}
	@Constants(value= Try.class) Object fTryA, fTryB;
}

@interface Constants {
	Class<?> value();
}