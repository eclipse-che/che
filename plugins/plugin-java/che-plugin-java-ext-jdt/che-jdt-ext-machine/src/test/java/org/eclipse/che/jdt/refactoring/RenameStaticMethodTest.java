/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
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
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.refactoring.IJavaRefactorings;
import org.eclipse.jdt.core.refactoring.descriptors.RenameJavaElementDescriptor;
import org.eclipse.jdt.internal.core.refactoring.descriptors.RefactoringSignatureDescriptorFactory;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class RenameStaticMethodTest extends RefactoringTest {
    private static final String REFACTORING_PATH = "RenameStaticMethod/";

    private static final boolean BUG_83332_SPLIT_SINGLE_IMPORT = true;


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
        IMethod method = classA.getMethod(methodName, signatures);
        RenameJavaElementDescriptor descriptor =
                RefactoringSignatureDescriptorFactory.createRenameJavaElementDescriptor(IJavaRefactorings.RENAME_METHOD);
        descriptor.setJavaElement(method);
        descriptor.setNewName(newMethodName);
        descriptor.setUpdateReferences(true);
        RefactoringStatus result = performRefactoring(descriptor);
        assertNotNull("precondition was supposed to fail", result);
    }

    private void helper1() throws Exception {
        helper1_0("m", "k", new String[0]);
    }

    private void helper2_0(String methodName, String newMethodName, String[] signatures, boolean updateReferences, boolean createDelegate)
            throws Exception {
        ICompilationUnit cu = createCUfromTestFile(getPackageP(), "A");
        IType classA = getType(cu, "A");
        IMethod method = classA.getMethod(methodName, signatures);
        RenameJavaElementDescriptor descriptor =
                RefactoringSignatureDescriptorFactory.createRenameJavaElementDescriptor(IJavaRefactorings.RENAME_METHOD);
        descriptor.setUpdateReferences(updateReferences);
        descriptor.setJavaElement(method);
        descriptor.setNewName(newMethodName);
        descriptor.setKeepOriginal(createDelegate);
        descriptor.setDeprecateDelegate(true);

        assertEquals("was supposed to pass", null, performRefactoring(descriptor));
        assertEqualLines("invalid renaming", getFileContents(getOutputTestFileName("A")), cu.getSource());

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

    //testFail3 deleted

    @Test
    public void testFail4() throws Exception {
        helper1();
    }

    @Test
    public void testFail5() throws Exception {
        helper1();
    }

    @Test
    public void testFail6() throws Exception {
        helper1();
    }

    @Test
    public void testFail7() throws Exception {
        helper1();
    }

    @Test
    public void testFail8() throws Exception {
        helper1();
    }

    @Test
    public void test0() throws Exception {
        helper2();
    }

    @Test
    public void test1() throws Exception {
        helper2();
    }

    @Test
    public void test2() throws Exception {
        helper2();
    }

    @Test
    public void test3() throws Exception {
        helper2();
    }

    @Test
    public void test4() throws Exception {
        helper2();
    }

    @Test
    public void test5() throws Exception {
        helper2();
    }

    @Test
    public void test6() throws Exception {
        helper2();
    }

    @Test
    public void test7() throws Exception {
        helper2_0("m", "k", new String[]{Signature.SIG_INT});
    }

    @Test
    public void test8() throws Exception {
        helper2_0("m", "k", new String[]{Signature.SIG_INT});
    }

    @Test
    public void test9() throws Exception {
        helper2_0("m", "k", new String[]{Signature.SIG_INT}, false, false);
    }

    @Test
    public void test10() throws Exception {
//		printTestDisabledMessage("bug 40628");
//		if (true)	return;
        ICompilationUnit cuA = createCUfromTestFile(getPackageP(), "A");
        ICompilationUnit cuB = createCUfromTestFile(getPackageP(), "B");

        IType classB = getType(cuB, "B");
        IMethod method = classB.getMethod("method", new String[0]);
        RenameJavaElementDescriptor descriptor =
                RefactoringSignatureDescriptorFactory.createRenameJavaElementDescriptor(IJavaRefactorings.RENAME_METHOD);
        descriptor.setUpdateReferences(true);
        descriptor.setJavaElement(method);
        descriptor.setNewName("newmethod");

        assertEquals("was supposed to pass", null, performRefactoring(descriptor));
        assertEqualLines("invalid renaming in A", getFileContents(getOutputTestFileName("A")), cuA.getSource());
        assertEqualLines("invalid renaming in B", getFileContents(getOutputTestFileName("B")), cuB.getSource());
    }

    @Test
    public void test11() throws Exception {
//		printTestDisabledMessage("bug 40452");
//		if (true)	return;
        IPackageFragment packageA = getRoot().createPackageFragment("a", false, new NullProgressMonitor());
        IPackageFragment packageB = getRoot().createPackageFragment("b", false, new NullProgressMonitor());
        ICompilationUnit cuA = createCUfromTestFile(packageA, "A");
        ICompilationUnit cuB = createCUfromTestFile(packageB, "B");

        IType classA = getType(cuA, "A");
        IMethod method = classA.getMethod("method2", new String[0]);
        RenameJavaElementDescriptor descriptor =
                RefactoringSignatureDescriptorFactory.createRenameJavaElementDescriptor(IJavaRefactorings.RENAME_METHOD);
        descriptor.setUpdateReferences(true);
        descriptor.setJavaElement(method);
        descriptor.setNewName("fred");

        assertEquals("was supposed to pass", null, performRefactoring(descriptor));
        assertEqualLines("invalid renaming in A", getFileContents(getOutputTestFileName("A")), cuA.getSource());
        assertEqualLines("invalid renaming in B", getFileContents(getOutputTestFileName("B")), cuB.getSource());
    }

    @Test
    public void testUnicode01() throws Exception {
        helper2_0("e", "f", new String[]{});
    }

    @Test
    public void testStaticImportFail0() throws Exception {
        helper1();
    }

    @Test
    public void testStaticImport1() throws Exception {
        ICompilationUnit cuA = createCUfromTestFile(getPackageP(), "C");
        helper2();
        assertEqualLines("invalid renaming in C", getFileContents(getOutputTestFileName("C")), cuA.getSource());
    }

    @Test
    public void testStaticImport2() throws Exception {
        ICompilationUnit cuA = createCUfromTestFile(getPackageP(), "C");
        helper2();
        assertEqualLines("invalid renaming in C", getFileContents(getOutputTestFileName("C")), cuA.getSource());
    }

    public void testStaticImport3() throws Exception {
        if (BUG_83332_SPLIT_SINGLE_IMPORT) {
            printTestDisabledMessage("BUG_83332_SPLIT_SINGLE_IMPORT");
            return;
        }
        helper2();
    }

    @Test
    public void testStaticImport4() throws Exception {
        helper2();
    }

    public void testStaticImport5() throws Exception {
        if (BUG_83332_SPLIT_SINGLE_IMPORT) {
            printTestDisabledMessage("BUG_83332_SPLIT_SINGLE_IMPORT");
            return;
        }
        helper2();
    }

    @Test
    public void testDelegate01() throws Exception {
        // simple static delegate
        helperDelegate();
    }

}
