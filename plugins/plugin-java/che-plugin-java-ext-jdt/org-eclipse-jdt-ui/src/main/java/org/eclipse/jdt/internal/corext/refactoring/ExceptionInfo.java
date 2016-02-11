/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.corext.refactoring;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeParameter;
import org.eclipse.jdt.core.dom.ITypeBinding;


public class ExceptionInfo {
	private final IJavaElement fElement;
	private final ITypeBinding fTypeBinding;
	private       int          fKind;

	public static final int OLD     = 0;
	public static final int ADDED   = 1;
	public static final int DELETED = 2;

	public ExceptionInfo(IJavaElement element, int kind, ITypeBinding binding) {
		Assert.isNotNull(element);
		Assert.isTrue(element instanceof IType || element instanceof ITypeParameter);
		fElement = element;
		fKind = kind;
		fTypeBinding = binding;
	}

	public static ExceptionInfo createInfoForOldException(IJavaElement element, ITypeBinding binding) {
		return new ExceptionInfo(element, OLD, binding);
	}

	public static ExceptionInfo createInfoForAddedException(IType type) {
		return new ExceptionInfo(type, ADDED, null);
	}

	public void markAsDeleted() {
		Assert.isTrue(!isAdded());//added exception infos should be simply removed from the list
		fKind = DELETED;
	}

	public void markAsOld() {
		Assert.isTrue(isDeleted());
		fKind = OLD;
	}

	public boolean isAdded() {
		return fKind == ADDED;
	}

	public boolean isDeleted() {
		return fKind == DELETED;
	}

	public boolean isOld() {
		return fKind == OLD;
	}

	public IJavaElement getElement() {
		return fElement;
	}
	
	public String getFullyQualifiedName() {
		return fElement instanceof IType ? ((IType) fElement).getFullyQualifiedName('.') : fElement.getElementName();
	}

	public int getKind() {
		return fKind;
	}

	/**
	 * @return ITypeBinding the typeBinding (for OLD and DELETED exceptions) or <code>null</code>
	 */
	public ITypeBinding getTypeBinding() {
		return fTypeBinding;
	}

	@Override
	public String toString() {
		StringBuffer result= new StringBuffer();
		switch (fKind) {
			case OLD : result.append("OLD: "); break; //$NON-NLS-1$
			case ADDED : result.append("ADDED: "); break; //$NON-NLS-1$
			case DELETED : result.append("DELETED: "); break; //$NON-NLS-1$
		}
		if (fElement == null)
			result.append("null"); //$NON-NLS-1$
		else
			result.append(fElement.toString());
		return result.toString();
	}
}
