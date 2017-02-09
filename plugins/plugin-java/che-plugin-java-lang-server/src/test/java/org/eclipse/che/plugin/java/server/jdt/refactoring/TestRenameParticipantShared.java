/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.java.server.jdt.refactoring;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.refactoring.IJavaElementMapper;
import org.eclipse.jdt.core.refactoring.RenameTypeArguments;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.ISharableParticipant;
import org.eclipse.ltk.core.refactoring.participants.RefactoringArguments;
import org.eclipse.ltk.core.refactoring.participants.RenameArguments;
import org.eclipse.ltk.core.refactoring.participants.RenameParticipant;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestRenameParticipantShared extends RenameParticipant implements ISharableParticipant {

	static TestRenameParticipantShared fgInstance;
	List fElements= new ArrayList(3);
	List fHandles= new ArrayList(3);
	List fArguments= new ArrayList(3);
	Map fSimilarToHandle= new HashMap();
	Map fSimilarToNewName= new HashMap();

	public boolean initialize(Object element) {
		fgInstance= this;
		fElements.add(element);
		fArguments.add(getArguments());
		if (element instanceof IJavaElement)
			fHandles.add(((IJavaElement)element).getHandleIdentifier());
		else
			fHandles.add(((IResource)element).getFullPath().toString());

		IJavaElementMapper updating= (IJavaElementMapper)getProcessor().getAdapter(IJavaElementMapper.class);
		if ((updating != null) && getArguments() instanceof RenameTypeArguments) {
			RenameTypeArguments arguments= (RenameTypeArguments)getArguments();
			if (arguments.getUpdateSimilarDeclarations()) {
				IJavaElement[] elements= arguments.getSimilarDeclarations();
				for (int i= 0; i < elements.length; i++) {
					IJavaElement updated= updating.getRefactoredJavaElement(elements[i]);
					if (updated!=null) {
						fSimilarToHandle.put(elements[i].getHandleIdentifier(), getKey(updated));
						fSimilarToNewName.put(elements[i].getHandleIdentifier(), updated.getElementName());
					}
				}
			}
		}

		return true;
	}

	private String getKey(IJavaElement updated) {
		if (updated instanceof IType)
			return ((IType)updated).getKey();
		else if (updated instanceof IMethod)
			return ((IMethod)updated).getKey();
		else if (updated instanceof IField)
			return ((IField)updated).getKey();
		return "";
	}

	public void addElement(Object element, RefactoringArguments args) {
		fElements.add(element);
		fArguments.add(args);
		if (element instanceof IJavaElement)
			fHandles.add(((IJavaElement)element).getHandleIdentifier());
		else
			fHandles.add(((IResource)element).getFullPath().toString());
	}

	public String getName() {
		return getClass().getName();
	}

	public RefactoringStatus checkConditions(IProgressMonitor pm, CheckConditionsContext context) {
		return new RefactoringStatus();
	}

	public Change createChange(IProgressMonitor pm) throws CoreException {
		return null;
	}

	public static void testNumberOfElements(int expected) {
		if (expected == 0) {
			Assert.assertTrue(fgInstance == null);
		} else {
			Assert.assertEquals(expected, fgInstance.fElements.size());
			Assert.assertEquals(expected, fgInstance.fArguments.size());
		}
	}

	public static void testArguments(RenameArguments[] args) {
		testNumberOfElements(args.length);
		for (int i= 0; i < args.length; i++) {
			RenameArguments expected= args[i];
			RenameArguments actual= (RenameArguments)fgInstance.fArguments.get(i);
			Assert.assertEquals(expected.getNewName(), actual.getNewName());
			Assert.assertEquals(expected.getUpdateReferences(), actual.getUpdateReferences());
		}
	}

	public static void reset() {
		fgInstance= null;
	}

	public static void testNumberOfSimilarElements(int expected) {
		if (expected == 0)
			Assert.assertTrue(fgInstance == null);
		else
			Assert.assertEquals(expected, fgInstance.fSimilarToHandle.size());
	}

	public static void testSimilarElements(List similarList, List similarNewNameList, List similarNewHandleList) {
		for (int i=0; i< similarList.size(); i++) {
			String handle= (String) similarList.get(i);
			String newHandle= (String)similarNewHandleList.get(i);
			String newName= (String)similarNewNameList.get(i);
			String actualNewHandle= (String)fgInstance.fSimilarToHandle.get(handle);
			String actualNewName= (String)fgInstance.fSimilarToNewName.get(handle);
			Assert.assertEquals("New element handle not as expected", newHandle, actualNewHandle);
			Assert.assertEquals("New element name not as expected", newName, actualNewName);
		}
		Assert.assertEquals(similarList.size(), fgInstance.fSimilarToHandle.size());
	}
}
