package p;

/**
 * Local variables: Prefix "pm", Suffix "_pm"
 * Parameters: Prefix "lv", Suffix "_lv"
 *
 */
public class SomeOtherClass {
	
	public void foo1(SomeOtherClass pmSomeOtherClass) {
		SomeOtherClass lvSomeOtherClass;
		SomeOtherClass lvSomeOtherClass_lv;
		SomeOtherClass someOtherClass_lv;
		SomeOtherClass pmSomeOtherClass_pm; // wrong prefixes, but rename anyway.
	}
	
	public void foo2(SomeOtherClass pmSomeOtherClass_pm) {
	}
	
	public void foo3(SomeOtherClass someOtherClass_pm) {
	}
	
}
