/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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
import org.eclipse.jdt.internal.corext.refactoring.rename.RenameMethodProcessor;
import org.eclipse.jdt.internal.corext.refactoring.rename.RenameNonVirtualMethodProcessor;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.RenameArguments;
import org.eclipse.ltk.core.refactoring.participants.RenameRefactoring;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class RenamePrivateMethodTest extends RefactoringTest {

    private static final String REFACTORING_PATH = "RenamePrivateMethod/";

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

    private void helper1_0(String methodName, String newMethodName, String[] signatures) throws Exception {
        IType classA = getType(createCUfromTestFile(getPackageP(), "A"), "A");
        RenameMethodProcessor processor = new RenameNonVirtualMethodProcessor(classA.getMethod(methodName, signatures));
        RenameRefactoring refactoring = new RenameRefactoring(processor);
        processor.setNewElementName(newMethodName);
        RefactoringStatus result = performRefactoring(refactoring);
        assertNotNull("precondition was supposed to fail", result);
    }

    private void helper1() throws Exception {
        helper1_0("m", "k", new String[0]);
    }

    private void helper2_0(String methodName, String newMethodName, String[] signatures, boolean updateReferences, boolean createDelegate)
            throws Exception {
        ParticipantTesting.reset();
        ICompilationUnit cu = createCUfromTestFile(getPackageP(), "A");
        IType classA = getType(cu, "A");
        IMethod method = classA.getMethod(methodName, signatures);
        String[] handles = ParticipantTesting.createHandles(method);
        RenameMethodProcessor processor = new RenameNonVirtualMethodProcessor(method);
        RenameRefactoring refactoring = new RenameRefactoring(processor);
        processor.setUpdateReferences(updateReferences);
        processor.setNewElementName(newMethodName);
        processor.setDelegateUpdating(createDelegate);
        assertEquals("was supposed to pass", null, performRefactoring(refactoring));
        assertEqualLines("invalid renaming", getFileContents(getOutputTestFileName("A")), cu.getSource());

        ParticipantTesting.testRename(
                handles,
                new RenameArguments[]{
                        new RenameArguments(newMethodName, updateReferences)});

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

    private void helper2_0(String methodName, String newMethodName, String[] signatures) throws Exception {
        helper2_0(methodName, newMethodName, signatures, true, false);
    }

    private void helper2(boolean updateReferences) throws Exception {
        helper2_0("m", "k", new String[0], updateReferences, false);
    }

    private void helper2() throws Exception {
        helper2(true);
    }

    private void helperDelegate() throws Exception {
        helper2_0("m", "k", new String[0], true, true);
    }

    @Test
    public void testFail0() throws Exception {
        helper1();
    }

    @Test
    public void testFail1() throws Exception {
        helper1();
    }

    @Test
    public void testFail2() throws Exception {
        helper1();
    }

    @Test
    public void testFail5() throws Exception {
        helper1();
    }

    @Test
    public void test0() throws Exception {
        helper2();
    }

    @Test
    public void test10() throws Exception {
        helper2();
    }

    @Test
    public void test11() throws Exception {
        helper2();
    }

    @Test
    public void test12() throws Exception {
        helper2();
    }

    @Test
    public void test13() throws Exception {
        helper2();
    }

    @Test
    public void test14() throws Exception {
        helper2();
    }

    @Test
    public void test15() throws Exception {
        helper2_0("m", "k", new String[]{"I"});
    }

    @Test
    public void test16() throws Exception {
        helper2_0("m", "fred", new String[]{"I"});
    }

    @Test
    public void test17() throws Exception {
        helper2_0("m", "kk", new String[]{"I"});
    }

    @Test
    public void test18() throws Exception {
        ICompilationUnit cu = createCUfromTestFile(getPackageP(), "A");
        ICompilationUnit cuC = createCUfromTestFile(getPackageP(), "C");

        IType classB = getType(cu, "B");
        RenameMethodProcessor processor = new RenameNonVirtualMethodProcessor(classB.getMethod("m", new String[]{"I"}));
        RenameRefactoring refactoring = new RenameRefactoring(processor);
        processor.setNewElementName("kk");

        assertEquals("was supposed to pass", null, performRefactoring(refactoring));
        assertEqualLines("invalid renaming A", getFileContents(getOutputTestFileName("A")), cu.getSource());
        assertEqualLines("invalid renaming C", getFileContents(getOutputTestFileName("C")), cuC.getSource());

    }

    @Test
    public void test2() throws Exception {
        helper2_0("m", "fred", new String[0]);
    }

    @Test
    public void test20() throws Exception {
        helper2_0("m", "fred", new String[]{"I"});
    }

    @Test
    public void test23() throws Exception {
        helper2_0("m", "k", new String[0]);
    }

    @Test
    public void test24() throws Exception {
        helper2_0("m", "k", new String[]{"QString;"});
    }

    @Test
    public void test25() throws Exception {
        helper2_0("m", "k", new String[]{"[QString;"});
    }

    @Test
    public void test26() throws Exception {
        helper2_0("m", "k", new String[0]);
    }

    @Test
    public void test27() throws Exception {
        helper2_0("m", "k", new String[0], false, false);
    }

    @Test
    public void testAnon0() throws Exception {
        helper2();
    }

    @Test
    public void testDelegate01() throws Exception {
        // simple static delegate
        helperDelegate();
    }
}
