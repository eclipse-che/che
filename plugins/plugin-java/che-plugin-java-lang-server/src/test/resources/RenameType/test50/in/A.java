//renaming A to B
package p;
/**
 * Extends {@linkplain A A}.
 * @see A#A()
 */
class A{
	A( ){};
};
class C extends A{
	C(){
		super();
	}
}