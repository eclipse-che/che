package p;
public class SomeClass{
	
	SomeNewInnerClass someNewInnerClass;

	/**
	 * 
	 * This is p.SomeClass.SomeNewInnerClass.
	 * 
	 */
	class SomeNewInnerClass {
		
	}

	/**
	 * @return Returns the someNewInnerClass.
	 */
	public SomeNewInnerClass getSomeNewInnerClass() {
		return someNewInnerClass;
	}

	/**
	 * @param someNewInnerClass The someNewInnerClass to set.
	 */
	public void setSomeNewInnerClass(SomeNewInnerClass someNewInnerClass) {
		this.someNewInnerClass = someNewInnerClass;
	}
	
};
