/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
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
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeParameter;
import org.eclipse.jdt.internal.corext.refactoring.rename.RenameTypeParameterProcessor;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.RenameRefactoring;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class RenameTypeParameterTest extends RefactoringTest {

	private static final String REFACTORING_PATH= "RenameTypeParameter/";
    private RefactoringTestSetup setup = new RefactoringTestSetup();

    @BeforeClass
    public static void prepareClass() {
        ParticipantTesting.init();
    }

    @Before
    public void setUp() throws Exception {
        setup.setUp();
        super.setUp();

    }

    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
        setup.tearDown();
    }

    protected String getRefactoringPath() {
        return REFACTORING_PATH;
    }

    private void helper1(String parameterName, String newParameterName, String typeName, boolean references) throws Exception {
        IType declaringType = getType(createCUfromTestFile(getPackageP(), "A"), typeName);
        RenameTypeParameterProcessor processor = new RenameTypeParameterProcessor(declaringType.getTypeParameter(parameterName));
        RenameRefactoring refactoring = new RenameRefactoring(processor);
        processor.setNewElementName(newParameterName);
        processor.setUpdateReferences(references);
        RefactoringStatus result = performRefactoring(refactoring);
        assertNotNull("precondition was supposed to fail", result);
    }

    private void helper1(String parameterName, String newParameterName, String typeName, String methodName, String[] methodSignature,
                         boolean references) throws Exception {
        IType declaringType = getType(createCUfromTestFile(getPackageP(), "A"), typeName);
		IMethod[] declaringMethods= getMethods(declaringType, new String[] { methodName}, new String[][] { methodSignature});
		RenameTypeParameterProcessor processor= new RenameTypeParameterProcessor(declaringMethods[0].getTypeParameter(parameterName));
		RenameRefactoring refactoring= new RenameRefactoring(processor);
		processor.setNewElementName(newParameterName);
		processor.setUpdateReferences(references);
		RefactoringStatus result= performRefactoring(refactoring);
		assertNotNull("precondition was supposed to fail", result);
	}

	private void helper2(String parameterName, String newParameterName, String typeName, boolean references) throws Exception {
		ParticipantTesting.reset();
		ICompilationUnit cu= createCUfromTestFile(getPackageP(), "A");
		IType declaringType= getType(cu, typeName);
		ITypeParameter typeParameter= declaringType.getTypeParameter(parameterName);
		RenameTypeParameterProcessor processor= new RenameTypeParameterProcessor(typeParameter);
		RenameRefactoring refactoring= new RenameRefactoring(processor);
		processor.setNewElementName(newParameterName);
		processor.setUpdateReferences(references);

		RefactoringStatus result= performRefactoring(refactoring);
		assertEquals("was supposed to pass", null, result);
		assertEqualLines("invalid renaming", getFileContents(getOutputTestFileName("A")), cu.getSource());

		assertTrue("anythingToUndo", RefactoringCore.getUndoManager().anythingToUndo());
		assertTrue("! anythingToRedo", !RefactoringCore.getUndoManager().anythingToRedo());

		RefactoringCore.getUndoManager().performUndo(null, new NullProgressMonitor());
		assertEqualLines("invalid undo", getFileContents(getInputTestFileName("A")), cu.getSource());

		assertTrue("! anythingToUndo", !RefactoringCore.getUndoManager().anythingToUndo());
		assertTrue("anythingToRedo", RefactoringCore.getUndoManager().anythingToRedo());

		RefactoringCore.getUndoManager().performRedo(null, new NullProgressMonitor());
		assertEqualLines("invalid redo", getFileContents(getOutputTestFileName("A")), cu.getSource());
	}

	private void helper2(String parameterName, String newParameterName, String typeName, String methodName, String[] methodSignature, boolean references) throws Exception {
		ParticipantTesting.reset();
		ICompilationUnit cu= createCUfromTestFile(getPackageP(), "A");
		IType declaringType= getType(cu, typeName);
		IMethod[] declaringMethods= getMethods(declaringType, new String[] { methodName}, new String[][] { methodSignature});
		ITypeParameter typeParameter= declaringMethods[0].getTypeParameter(parameterName);
		RenameTypeParameterProcessor processor= new RenameTypeParameterProcessor(typeParameter);
		RenameRefactoring refactoring= new RenameRefactoring(processor);
		processor.setNewElementName(newParameterName);
		processor.setUpdateReferences(references);

		RefactoringStatus result= performRefactoring(refactoring);
		assertEquals("was supposed to pass", null, result);
		assertEqualLines("invalid renaming", getFileContents(getOutputTestFileName("A")), cu.getSource());

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
	public void test0() throws Exception {
		helper2("T", "S", "A", true);
	}

    @Test
	public void test1() throws Exception {
		helper2("T", "S", "A", true);
	}

    @Test
	public void test2() throws Exception {
		helper2("T", "S", "A", false);
	}

    @Test
	public void test3() throws Exception {
		helper2("T", "S", "A", true);
	}

    @Test
	public void test4() throws Exception {
		helper2("T", "S", "A", false);
	}

    @Test
	public void test5() throws Exception {
		helper2("T", "S", "A", true);
	}

    @Test
	public void test6() throws Exception {
		helper2("S", "T", "A", true);
	}

    @Test
	public void test7() throws Exception {
		helper2("T", "S", "A", false);
	}

    @Test
	public void test8() throws Exception {
		helper2("S", "T", "A", false);
	}

    @Test
	public void test9() throws Exception {
		helper2("T", "S", "A", "f", new String[] { "QT;"}, true);
	}

    @Test
	public void test10() throws Exception {
		helper2("T", "S", "B", "f", new String[] { "QT;"}, true);
	}

    @Test
	public void test11() throws Exception {
		helper2("T", "S", "A", "f", new String[] { "QT;"}, false);
	}

    @Test
	public void test12() throws Exception {
		helper2("T", "S", "B", "f", new String[] { "QT;"}, false);
	}

    @Test
	public void test13() throws Exception {
		helper2("T", "S", "A", true);
	}

    @Test
	public void test14() throws Exception {
		helper2("ELEMENT", "E", "A", true);
	}

    @Test
	public void test15() throws Exception {
		helper2("T", "S", "A", true);
	}
	
// ------------------------------------------------

    @Test
	public void testFail0() throws Exception {
		helper1("T", "S", "A", true);
	}

    @Test
	public void testFail1() throws Exception {
		helper1("T", "S", "A", true);
	}

    @Test
	public void testFail2() throws Exception {
		helper1("T", "S", "A", true);
	}

    @Test
	public void testFail3() throws Exception {
		helper1("T", "S", "A", true);
	}

    @Test
	public void testFail4() throws Exception {
		helper1("T", "S", "A", true);
	}

    @Test
	public void testFail5() throws Exception {
		helper1("T", "S", "B", "f", new String[] { "QT;"}, true);
	}
}
