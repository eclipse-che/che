/*******************************************************************************
 * Copyright (c) 2013, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.jdt.refactoring;


import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.SourceRange;
import org.eclipse.jdt.core.refactoring.IJavaRefactorings;
import org.eclipse.jdt.core.refactoring.descriptors.RenameJavaElementDescriptor;
import org.eclipse.jdt.internal.core.refactoring.descriptors.RefactoringSignatureDescriptorFactory;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.RenameArguments;
import org.eclipse.ltk.core.refactoring.participants.RenameRefactoring;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Rename18Test extends RefactoringTest {
    public static final String SQUARE_BRACKET_OPEN= "/*[*/";
    public static final String SQUARE_BRACKET_CLOSE=   "/*]*/";
	private static final String REFACTORING_PATH= "RenameTests18/";
    Java18Setup setup = new Java18Setup();

	protected String getRefactoringPath() {
		return REFACTORING_PATH;
	}
    @BeforeClass
    public static void prepareClass() {
        ParticipantTesting.init();
    }

    @Before
    public void setUp() throws Exception {
        setup.setUp();
        super.setUp();
        Hashtable options= JavaCore.getOptions();
        JavaCore.setOptions(options);
        fIsPreDeltaTest= true;

    }

    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
        Hashtable options= JavaCore.getOptions();
        JavaCore.setOptions(options);
    }

	private ISourceRange getSelection(ICompilationUnit cu) throws Exception {
		String source= cu.getSource();
		//Warning: this *includes* the SQUARE_BRACKET_OPEN!
		int offset= source.indexOf(SQUARE_BRACKET_OPEN);
		int end= source.indexOf(SQUARE_BRACKET_CLOSE);
		return new SourceRange(offset + SQUARE_BRACKET_OPEN.length(), end - offset);
	}

	private void renameLocalVariable(String newFieldName, boolean updateReferences) throws Exception {
		ParticipantTesting.reset();
		ICompilationUnit cu= createCUfromTestFile(getPackageP(), "A");

		ISourceRange selection= getSelection(cu);
		IJavaElement[] elements= cu.codeSelect(selection.getOffset(), selection.getLength());
		assertEquals(1, elements.length);
		RenameJavaElementDescriptor descriptor= RefactoringSignatureDescriptorFactory.createRenameJavaElementDescriptor(IJavaRefactorings.RENAME_LOCAL_VARIABLE);
		descriptor.setJavaElement(elements[0]);
		descriptor.setNewName(newFieldName);
		descriptor.setUpdateReferences(updateReferences);
		descriptor.setUpdateTextualOccurrences(false);

		RenameRefactoring refactoring= (RenameRefactoring) createRefactoring(descriptor);
		List list= new ArrayList();
		list.add(elements[0]);
		List args= new ArrayList();
		args.add(new RenameArguments(newFieldName, updateReferences));
		String[] renameHandles= ParticipantTesting.createHandles(list.toArray());

		RefactoringStatus result= performRefactoring(refactoring);
		assertEquals("was supposed to pass", null, result);
		assertEqualLines("invalid renaming", getFileContents(getOutputTestFileName("A")), cu.getSource());

		ParticipantTesting.testRename(
			renameHandles,
			(RenameArguments[]) args.toArray(new RenameArguments[args.size()]));

		assertTrue("anythingToUndo", RefactoringCore.getUndoManager().anythingToUndo());
		assertTrue("! anythingToRedo", !RefactoringCore.getUndoManager().anythingToRedo());

		RefactoringCore.getUndoManager().performUndo(null, new NullProgressMonitor());
		assertEqualLines("invalid undo", getFileContents(getInputTestFileName("A")), cu.getSource());

		assertTrue("! anythingToUndo", !RefactoringCore.getUndoManager().anythingToUndo());
		assertTrue("anythingToRedo", RefactoringCore.getUndoManager().anythingToRedo());

		RefactoringCore.getUndoManager().performRedo(null, new NullProgressMonitor());
		assertEqualLines("invalid redo", getFileContents(getOutputTestFileName("A")), cu.getSource());
	}

    @Test
	public void testLambda0() throws Exception {
		renameLocalVariable("renamedF", true);
	}

    @Test
	public void testLambda1() throws Exception {
		renameLocalVariable("renamedP", true);
	}

    @Test
	public void testLambda2() throws Exception {
		renameLocalVariable("renamedIi", true);
	}

    @Test
	public void testLambda3() throws Exception {
		renameLocalVariable("x_renamed", true);
	}


	private void renameMethod(String methodName, String newMethodName, String[] signatures, boolean shouldPass, boolean updateReferences, boolean createDelegate) throws Exception{
		ICompilationUnit cu= createCUfromTestFile(getPackageP(), "A");
		IType typeI= getType(cu, "I");
		IMethod method= typeI.getMethod(methodName, signatures);

		RenameJavaElementDescriptor descriptor= RefactoringSignatureDescriptorFactory.createRenameJavaElementDescriptor(IJavaRefactorings.RENAME_METHOD);
		descriptor.setJavaElement(method);
		descriptor.setUpdateReferences(updateReferences);
		descriptor.setNewName(newMethodName);
		descriptor.setKeepOriginal(createDelegate);
		descriptor.setDeprecateDelegate(true);

		assertEquals("was supposed to pass", null, performRefactoring(descriptor));
		if (!shouldPass){
			assertTrue("incorrect renaming because of a java model bug", ! getFileContents(getOutputTestFileName("A")).equals(cu.getSource()));
			return;
		}
		assertEqualLines("incorrect renaming", getFileContents(getOutputTestFileName("A")), cu.getSource());

		assertTrue("anythingToUndo", RefactoringCore.getUndoManager().anythingToUndo());
		assertTrue("! anythingToRedo", !RefactoringCore.getUndoManager().anythingToRedo());
		//assertEquals("1 to undo", 1, Refactoring.getUndoManager().getRefactoringLog().size());

		RefactoringCore.getUndoManager().performUndo(null, new NullProgressMonitor());
		assertEqualLines("invalid undo", getFileContents(getInputTestFileName("A")), cu.getSource());

		assertTrue("! anythingToUndo", !RefactoringCore.getUndoManager().anythingToUndo());
		assertTrue("anythingToRedo", RefactoringCore.getUndoManager().anythingToRedo());
		//assertEquals("1 to redo", 1, Refactoring.getUndoManager().getRedoStack().size());

		RefactoringCore.getUndoManager().performRedo(null, new NullProgressMonitor());
		assertEqualLines("invalid redo", getFileContents(getOutputTestFileName("A")), cu.getSource());
	}

	private void renameMethodInInterface() throws Exception{
		renameMethod("m", "k", new String[0], true, true, false);
	}
	
	// method with a lambda method as reference

    @Test
	public void testMethod0() throws Exception{
		renameMethodInInterface();
	}
	
	// method with method references as reference

    @Test
	public void testMethod1() throws Exception{
		renameMethodInInterface();
	}

    @Test
	public void testMethod2() throws Exception {
		renameMethodInInterface();
	}

    @Test
	public void testMethodReference0() throws Exception {
		renameMethod("searchForRefs", "searchForRefs1", new String[0], true, true, false);
	}
}
