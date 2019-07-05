package p;

public interface ISomeIf2 {
	
	class X {
		ISomeIf2 fSomeIf2;
		ISomeIf2 fISomeIf2;
		/**
		 * @return Returns the iSomeIf.
		 */
		public ISomeIf2 getISomeIf2() {
			return fISomeIf2;
		}
		/**
		 * @param a The a to set.
		 */
		public void setISomeIf2(ISomeIf2 a) {
			fISomeIf2 = a;
		}
		/**
		 * @return Returns the someIf.
		 */
		public ISomeIf2 getSomeIf2() {
			return fSomeIf2;
		}
		/**
		 * @param a The a to set.
		 */
		public void setSomeIf2(ISomeIf2 a) {
			fSomeIf2 = a;
		}
	}
	
}
