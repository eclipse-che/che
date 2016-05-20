//renaming A to B
package p;
/**
 * Extends {@linkplain B A}.
 * @see B#B()
 */
class B{
	B( ){};
};
class C extends B{
	C(){
		super();
	}
}