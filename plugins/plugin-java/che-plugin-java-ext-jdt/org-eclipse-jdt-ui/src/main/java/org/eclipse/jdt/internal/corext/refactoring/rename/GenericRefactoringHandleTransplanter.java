/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.corext.refactoring.rename;

import org.eclipse.jdt.core.IAnnotatable;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IImportContainer;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IInitializer;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageDeclaration;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeParameter;

public class GenericRefactoringHandleTransplanter {

	public final IJavaElement transplantHandle(IJavaElement element) {
		IJavaElement parent= element.getParent();
		if (parent != null)
			parent= transplantHandle(parent); // recursive

		switch (element.getElementType()) {
			case IJavaElement.JAVA_MODEL:
				return transplantHandle((IJavaModel) element);

			case IJavaElement.JAVA_PROJECT:
				return transplantHandle((IJavaProject) element);

			case IJavaElement.PACKAGE_FRAGMENT_ROOT:
				return transplantHandle((IJavaProject) parent, (IPackageFragmentRoot) element);

			case IJavaElement.PACKAGE_FRAGMENT:
				return transplantHandle((IPackageFragmentRoot) parent, (IPackageFragment) element);

			case IJavaElement.COMPILATION_UNIT:
				return transplantHandle((IPackageFragment) parent, (ICompilationUnit) element);

			case IJavaElement.CLASS_FILE:
				return transplantHandle((IPackageFragment) parent, (IClassFile) element);

			case IJavaElement.TYPE:
				return transplantHandle(parent, (IType) element);

			case IJavaElement.FIELD:
				return transplantHandle((IType) parent, (IField) element);

			case IJavaElement.METHOD:
				return transplantHandle((IType) parent, (IMethod) element);

			case IJavaElement.INITIALIZER:
				return transplantHandle((IType) parent, (IInitializer) element);

			case IJavaElement.PACKAGE_DECLARATION:
				return transplantHandle((ICompilationUnit) parent, (IPackageDeclaration) element);

			case IJavaElement.IMPORT_CONTAINER:
				return transplantHandle((ICompilationUnit) parent, (IImportContainer) element);

			case IJavaElement.IMPORT_DECLARATION:
				return transplantHandle((IImportContainer) parent, (IImportDeclaration) element);

			case IJavaElement.LOCAL_VARIABLE:
				return transplantHandle((ILocalVariable) element);

			case IJavaElement.TYPE_PARAMETER:
				return transplantHandle((IMember) parent, (ITypeParameter) element);

			case IJavaElement.ANNOTATION:
				return transplantHandle((IAnnotatable) parent, (IAnnotation) element);

			default:
				throw new IllegalArgumentException(element.toString());
		}

	}

	protected IJavaModel transplantHandle(IJavaModel element) {
		return element;
	}

	protected IJavaProject transplantHandle(IJavaProject element) {
		return element;
	}

	protected IPackageFragmentRoot transplantHandle(IJavaProject parent, IPackageFragmentRoot element) {
		return element;
	}

	protected IPackageFragment transplantHandle(IPackageFragmentRoot parent, IPackageFragment element) {
		return parent.getPackageFragment(element.getElementName());
	}

	protected ICompilationUnit transplantHandle(IPackageFragment parent, ICompilationUnit element) {
		return parent.getCompilationUnit(element.getElementName());
	}

	protected IClassFile transplantHandle(IPackageFragment parent, IClassFile element) {
		return parent.getClassFile(element.getElementName());
	}

	protected IType transplantHandle(IJavaElement parent, IType element) {
		switch (parent.getElementType()) {
			case IJavaElement.COMPILATION_UNIT:
				return ((ICompilationUnit) parent).getType(element.getElementName());
			case IJavaElement.CLASS_FILE:
				return ((IClassFile) parent).getType();
			case IJavaElement.METHOD:
				return ((IMethod) parent).getType(element.getElementName(), element.getOccurrenceCount());
			case IJavaElement.FIELD:
				return ((IField) parent).getType(element.getElementName(), element.getOccurrenceCount());
			case IJavaElement.INITIALIZER:
				return ((IInitializer) parent).getType(element.getElementName(), element.getOccurrenceCount());
			case IJavaElement.TYPE:
				return ((IType) parent).getType(element.getElementName(), element.getOccurrenceCount());
			default:
				throw new IllegalStateException(element.toString());
		}
	}

	protected IField transplantHandle(IType parent, IField element) {
		return parent.getField(element.getElementName());
	}

	protected IMethod transplantHandle(IType parent, IMethod element) {
		return parent.getMethod(element.getElementName(), element.getParameterTypes());
	}

	protected IInitializer transplantHandle(IType parent, IInitializer element) {
		return parent.getInitializer(element.getOccurrenceCount());
	}

	protected IPackageDeclaration transplantHandle(ICompilationUnit parent, IPackageDeclaration element) {
		return parent.getPackageDeclaration(element.getElementName());
	}

	protected IImportContainer transplantHandle(ICompilationUnit parent, IImportContainer element) {
		return parent.getImportContainer();
	}

	protected IImportDeclaration transplantHandle(IImportContainer parent, IImportDeclaration element) {
		return parent.getImport(element.getElementName());
	}

	protected ILocalVariable transplantHandle(ILocalVariable element) {
		return element; // can't get from parent!
	}

	protected IAnnotation transplantHandle(IAnnotatable parent, IAnnotation element) {
		return parent.getAnnotation(element.getElementName());
	}

	protected ITypeParameter transplantHandle(IMember parent, ITypeParameter element) {
		switch (parent.getElementType()) {
			case IJavaElement.TYPE:
				return ((IType) parent).getTypeParameter(element.getElementName());
			case IJavaElement.METHOD:
				return ((IMethod) parent).getTypeParameter(element.getElementName());
			default:
				throw new IllegalStateException(element.toString());
		}
	}
}
