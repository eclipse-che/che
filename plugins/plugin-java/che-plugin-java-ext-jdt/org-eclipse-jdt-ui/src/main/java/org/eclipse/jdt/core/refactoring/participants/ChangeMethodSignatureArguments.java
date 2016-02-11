/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.refactoring.participants;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.Signature;
import org.eclipse.ltk.core.refactoring.participants.RefactoringArguments;

/**
 * Change method signature arguments describe the data that a processor
 * provides to its change signature participants
 * <p>
 * This class is not intended to be subclassed by clients.
 * </p>
 *
 * @since 1.2
 *
 * @noextend This class is not intended to be subclassed by clients.
 */
public class ChangeMethodSignatureArguments extends RefactoringArguments {

	/**
	 * Instances of {@link ChangeMethodSignatureArguments.Parameter} are used to describe the new parameters
	 * after a change method signature refactoring.
	 */
	public final static class Parameter {

		private final int fOldIndex;
		private final String fNewName;
		private final String fNewSignature;
		private final String fDefaultValue;

		/**
		 * Creates a {@link ChangeMethodSignatureArguments.Parameter}.
		 *
		 * @param oldIndex the index of the parameter in the original method or <code>-1</code> if the parameter is a new parameter.
		 * @param newName the new name of the parameter.
		 * @param newSignature the new type of the parameter in signature notation (See {@link Signature}).
		 * @param defaultValue the default value for new parameters or <code>null</code>.
		 */
		public Parameter(int oldIndex, String newName, String newSignature, String defaultValue) {
			fOldIndex= oldIndex;
			fNewName= newName;
			fNewSignature= newSignature;
			fDefaultValue= defaultValue;
		}

		/**
		 * Returns the index of the parameter in the original method or <code>-1</code> if the parameter
		 * has been added.
		 *
		 * @return the index of the parameter in the original method or <code>-1</code> if the parameter
		 * has been added
		 */
		public int getOldIndex() {
			return fOldIndex;
		}

		/**
		 * Returns the new name of the parameter. If the name has not been changed by the refactoring,
		 * the original parameter name is returned.
		 *
		 * @return the new parameter name
		 */
		public String getName() {
			return fNewName;
		}

		/**
		 * Returns the new type of the parameter in signature notation (See {@link Signature}).
		 * If the type has not been changed by the refactoring, the original type signature is returned.
		 *
		 * @return the the new type
		 */
		public String getType() {
			return fNewSignature;
		}

		/**
		 * The default value for new parameters or <code>null</code>.
		 *
		 * @return returns the default value for new parameters or <code>null</code>.
		 */
		public String getDefaultValue() {
			return fDefaultValue;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		public String toString() {
			return "name: " + fNewName + ", type: " + fNewSignature + ", oldIndex: " + fOldIndex + ", defaultValue: " + fDefaultValue; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		}

	}

	/**
	 * Instances of {@link ChangeMethodSignatureArguments.ThrownException} are used to describe the new thrown
	 * exceptions after a change method signature refactoring.
	 */
	public final static class ThrownException {

		private final int fOldIndex;
		private final String fType;

		/**
		 * Creates a {@link ChangeMethodSignatureArguments.ThrownException}.
		 *
		 * @param oldIndex the index of the thrown exception in the original method
		 *                             or <code>-1</code> if the thrown exception is a new thrown exception.
		 * @param newSignature the new type of the thrown exception in signature notation
		 *                             (See {@link Signature}).
		 */
		public ThrownException(int oldIndex, String newSignature) {
			fOldIndex= oldIndex;
			fType= newSignature;
		}

		/**
		 * Returns the index of the thrown exception in the original method or <code>-1</code> if the thrown exception
		 * has been added.
		 *
		 * @return the index of the parameter in the original method or <code>-1</code> if the thrown exception
		 * has been added.
		 */
		public int getOldIndex() {
			return fOldIndex;
		}

		/**
		 * Returns the new type of the thrown exception in signature notation (See {@link Signature}).
		 *
		 * @return the the new type
		 */
		public String getType() {
			return fType;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		public String toString() {
			return "type: " + fType + ", oldIndex: " + fOldIndex; //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	private String fNewName;
	private String fNewReturnType;

	private final int fNewVisibility;
	private final Parameter[] fNewParameters;
	private final ThrownException[] fThrownExceptions;
	private final boolean fKeepOriginal;

	/**
	 * Creates new change method signature arguments.
	 *
	 * @param newName the new name of the element to be changed
	 * @param newReturnType the new method return type in signature notation (see {@link Signature}).
	 * @param newVisibility the new visibility; one of {@link Flags#AccPublic}, {@link Flags#AccProtected},
	 * {@link Flags#AccPrivate} or <code>0</code> for the default visibility.
	 * @param newParameters the new parameters of this method
	 * @param thrownExceptions the new exceptions thrown by this method
	 * @param keepOriginal <code>true</code> if the original method is kept as a delegate to the new one,
	 * <code>false</code> otherwise
	 */
	public ChangeMethodSignatureArguments(String newName, String newReturnType, int newVisibility, Parameter[] newParameters, ThrownException[] thrownExceptions, boolean keepOriginal) {
		Assert.isNotNull(newName);
		fNewName= newName;
		fNewReturnType= newReturnType;
		fNewVisibility= newVisibility;
		fNewParameters= newParameters;
		fThrownExceptions= thrownExceptions;
		fKeepOriginal= keepOriginal;
	}

	/**
	 * Returns the new method name. If the name has not been changed by the refactoring, the original
	 * parameter name is returned.
	 *
	 * @return the method name
	 */
	public String getNewName() {
		return fNewName;
	}

	/**
	 * Returns the type signature of the new return type of this method.
	 * For constructors, this returns the signature for void.
	 * If the return type has not been changed by the refactoring, the original
	 * return type signature is returned.
	 *
	 * @return the new return type
	 */
	public String getNewReturnType() {
		return fNewReturnType;
	}

	/**
	 * Returns the new visibility of this method. The visibility is one of {@link Flags#AccPublic}, {@link Flags#AccProtected},
	 * {@link Flags#AccPrivate} or <code>0</code> for the default visibility. If the visibility has not been changed by the
	 * refactoring, the original visibility is returned.
	 *
	 * @return the visibility of the method
	 */
	public int getNewVisibility() {
		return fNewVisibility;
	}

	/**
	 * Returns the new parameters of this method.
	 *
	 * @return the new parameters of this method
	 */
	public Parameter[] getNewParameters() {
		return fNewParameters;
	}

	/**
	 * Returns the new thrown exceptions of this method.
	 *
	 * @return new thrown exceptions of this method
	 */
	public ThrownException[] getThrownExceptions() {
		return fThrownExceptions;
	}

	/**
	 * Returns whether the original method is kept as a delegate to the new one.
	 *
	 * @return returns <code>true</code> if the original method is kept <code>false</code> otherwise
	 */
	public boolean getKeepOriginal() {
		return fKeepOriginal;
	}

	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		StringBuffer buf= new StringBuffer("change signature to "); //$NON-NLS-1$
		buf.append("\n\tvisibility: ").append(Flags.toString(fNewVisibility)); //$NON-NLS-1$
		buf.append("\n\treturn type sig: ").append(fNewReturnType); //$NON-NLS-1$
		buf.append("\n\tnew name: ").append(fNewName); //$NON-NLS-1$
		buf.append("\n\tkeep original: ").append(fKeepOriginal); //$NON-NLS-1$
		for (int i= 0; i < fNewParameters.length; i++) {
			buf.append("\n\tparameter ").append(i).append(": ").append(fNewParameters[i]); //$NON-NLS-1$ //$NON-NLS-2$
		}
		for (int i= 0; i < fThrownExceptions.length; i++) {
			buf.append("\n\texception ").append(i).append(": ").append(fThrownExceptions[i]); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return buf.toString();
	}
}
