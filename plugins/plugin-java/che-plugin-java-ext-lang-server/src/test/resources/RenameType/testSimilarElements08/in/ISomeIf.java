package p;

public interface ISomeIf {
	
	class X {
		ISomeIf fSomeIf;
		ISomeIf fISomeIf;
		/**
		 * @return Returns the iSomeIf.
		 */
		public ISomeIf getISomeIf() {
			return fISomeIf;
		}
		/**
		 * @param a The a to set.
		 */
		public void setISomeIf(ISomeIf a) {
			fISomeIf = a;
		}
		/**
		 * @return Returns the someIf.
		 */
		public ISomeIf getSomeIf() {
			return fSomeIf;
		}
		/**
		 * @param a The a to set.
		 */
		public void setSomeIf(ISomeIf a) {
			fSomeIf = a;
		}
	}
	
}
