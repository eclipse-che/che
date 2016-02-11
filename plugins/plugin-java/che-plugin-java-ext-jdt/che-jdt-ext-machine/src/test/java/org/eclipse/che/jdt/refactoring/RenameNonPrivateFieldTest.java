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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.refactoring.IJavaRefactorings;
import org.eclipse.jdt.core.refactoring.descriptors.RenameJavaElementDescriptor;
import org.eclipse.jdt.internal.core.refactoring.descriptors.RefactoringSignatureDescriptorFactory;
import org.eclipse.jdt.internal.corext.refactoring.rename.RenameFieldProcessor;
import org.eclipse.jdt.internal.corext.util.JdtFlags;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.RenameArguments;
import org.eclipse.ltk.core.refactoring.participants.RenameRefactoring;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class RenameNonPrivateFieldTest extends RefactoringTest {
    private final RefactoringTestSetup setup = new RefactoringTestSetup();
	private static final String
			REFACTORING_PATH        = "RenameNonPrivateField/";

	private static final boolean BUG_79990_CORE_SEARCH_METHOD_DECL = true;

	private Object fPrefixPref;

	//Test methods can configure these fields:
	private boolean fUpdateReferences     = true;
	private boolean fUpdateTextualMatches = false;
	private boolean fRenameGetter         = false;
	private boolean fRenameSetter         = false;


//	public RenameNonPrivateFieldTests(String name) {
//		super(name);
//	}

//	public static Test suite() {
//		return new RefactoringTestSetup(new TestSuite(clazz));
//	}

//	public static Test setUpTest(Test someTest) {
//		return new RefactoringTestSetup(someTest);
//	}

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
		Hashtable options = JavaCore.getOptions();
		fPrefixPref = options.get(JavaCore.CODEASSIST_FIELD_PREFIXES);
		options.put(JavaCore.CODEASSIST_FIELD_PREFIXES, getPrefixes());
		JavaCore.setOptions(options);
		fIsPreDeltaTest = true;
	}

	@After
	public void tearDown() throws Exception {
		setup.tearDown();
		super.tearDown();
		Hashtable options = JavaCore.getOptions();
		options.put(JavaCore.CODEASSIST_FIELD_PREFIXES, fPrefixPref);
		JavaCore.setOptions(options);
	}

	private String getPrefixes() {
		return "";
	}

	private void helper1_0(String fieldName, String newFieldName) throws Exception {
		IType classA = getType(createCUfromTestFile(getPackageP(), "A"), "A");
		IField field = classA.getField(fieldName);
		RenameJavaElementDescriptor descriptor =
				RefactoringSignatureDescriptorFactory.createRenameJavaElementDescriptor(IJavaRefactorings.RENAME_FIELD);
		descriptor.setJavaElement(field);
		descriptor.setUpdateReferences(true);
		descriptor.setNewName(newFieldName);
		RefactoringStatus result = performRefactoring(descriptor);
		assertNotNull("precondition was supposed to fail", result);
	}

	private void helper1() throws Exception {
		helper1_0("f", "g");
	}

	/**
	 * Configure options by setting instance fields to non-default values.
	 * @param fieldName
	 * @param newFieldName
	 * @throws Exception
	 */
	private void helper2(String fieldName, String newFieldName) throws Exception {
		helper2(fieldName, newFieldName, false);
	}

	private void helper2(String fieldName, String newFieldName, boolean createDelegates) throws Exception {
		ParticipantTesting.reset();
		ICompilationUnit cu = createCUfromTestFile(getPackageP(), "A");
		IType classA = getType(cu, "A");
		IField field = classA.getField(fieldName);
		boolean isEnum= JdtFlags.isEnum(field);
		String id= isEnum ? IJavaRefactorings.RENAME_ENUM_CONSTANT : IJavaRefactorings.RENAME_FIELD;
		RenameJavaElementDescriptor descriptor= RefactoringSignatureDescriptorFactory.createRenameJavaElementDescriptor(id);
		descriptor.setJavaElement(field);
		descriptor.setNewName(newFieldName);
		descriptor.setUpdateReferences(fUpdateReferences);
		descriptor.setUpdateTextualOccurrences(fUpdateTextualMatches);
		if (!isEnum) {
			descriptor.setRenameGetters(fRenameGetter);
			descriptor.setRenameSetters(fRenameSetter);
			descriptor.setKeepOriginal(createDelegates);
			descriptor.setDeprecateDelegate(true);
		}
		RenameRefactoring refactoring= (RenameRefactoring) createRefactoring(descriptor);
		RenameFieldProcessor processor= (RenameFieldProcessor) refactoring.getProcessor();

		List elements= new ArrayList();
		elements.add(field);
		List args= new ArrayList();
		args.add(new RenameArguments(newFieldName, fUpdateReferences));
		if (fRenameGetter) {
			elements.add(processor.getGetter());
			args.add(new RenameArguments(processor.getNewGetterName(), fUpdateReferences));
		}
		if (fRenameSetter) {
			elements.add(processor.getSetter());
			args.add(new RenameArguments(processor.getNewSetterName(), fUpdateReferences));
		}
		String[] renameHandles= ParticipantTesting.createHandles(elements.toArray());

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

	private void helper2() throws Exception{
		helper2("f", "g");
	}

	//--------- tests ----------
	@Test
	public void testFail0() throws Exception{
		helper1();
	}

    @Test
	public void testFail1() throws Exception{
		helper1();
	}

    @Test
	public void testFail2() throws Exception{
		helper1();
	}

    @Test
	public void testFail3() throws Exception{
		helper1();
	}

    @Test
	public void testFail4() throws Exception{
		helper1();
	}

    @Test
	public void testFail5() throws Exception{
		helper1();
	}

    @Test
	public void testFail6() throws Exception{
		helper1();
	}

    @Test
	public void testFail7() throws Exception{
		helper1();
	}

    @Test
	public void testFail8() throws Exception{
		helper1();
	}

    @Test
	public void testFail9() throws Exception{
		helper1();
	}

    @Test
	public void testFail10() throws Exception{
		helper1();
	}

    @Test
	public void testFail11() throws Exception{
		helper1();
	}

    @Test
	public void testFail12() throws Exception{
		helper1();
	}

    @Test
	public void testFail13() throws Exception{
		//printTestDisabledMessage("1GKZ8J6: ITPJCORE:WIN2000 - search: missing field occurrecnces");
		helper1();
	}

    @Test
	public void testFail14() throws Exception{
		//printTestDisabledMessage("1GKZ8J6: ITPJCORE:WIN2000 - search: missing field occurrecnces");
		helper1();
	}

	// ------

    @Test
	public void test0() throws Exception{
		helper2();
	}

    @Test
	public void test1() throws Exception{
		helper2();
	}

    @Test
	public void test2() throws Exception{
		helper2();
	}

    @Test
	public void test3() throws Exception{
		helper2();
	}

    @Test
	public void test4() throws Exception{
		helper2();
		//printTestDisabledMessage("1GKZ8J6: ITPJCORE:WIN2000 - search: missing field occurrecnces");
	}

    @Test
	public void test5() throws Exception{
		helper2();
	}

    @Test
	public void test6() throws Exception{
		//printTestDisabledMessage("1GKB9YH: ITPJCORE:WIN2000 - search for field refs - incorrect results");
		helper2();
	}

    @Test
	public void test7() throws Exception{
		helper2();
	}

    @Test
	public void test8() throws Exception{
		//printTestDisabledMessage("1GD79XM: ITPJCORE:WINNT - Search - search for field references - not all found");
		helper2();
	}

    @Test
	public void test9() throws Exception{
		helper2();
	}

    @Test
	public void test10() throws Exception{
		helper2();
	}

    @Test
	public void test11() throws Exception{
		helper2();
	}

    @Test
	public void test12() throws Exception{
		//System.out.println("\nRenameNonPrivateField::" + name() + " disabled (1GIHUQP: ITPJCORE:WINNT - search for static field should be more accurate)");
		helper2();
	}

    @Test
	public void test13() throws Exception{
		//System.out.println("\nRenameNonPrivateField::" + name() + " disabled (1GIHUQP: ITPJCORE:WINNT - search for static field should be more accurate)");
		helper2();
	}

    @Test
	public void test14() throws Exception{
		fUpdateReferences= false;
		fUpdateTextualMatches= false;
		helper2();
	}

    @Test
	public void test15() throws Exception{
		fUpdateReferences= false;
		fUpdateTextualMatches= false;
		helper2();
	}

    @Test
	public void test16() throws Exception{
//		printTestDisabledMessage("text for bug 20693");
		helper2();
	}

    @Test
	public void test17() throws Exception{
//		printTestDisabledMessage("test for bug 66250, 79131 (corner case: reference "A.f" to p.A#f)");
		fUpdateReferences= false;
		fUpdateTextualMatches= true;
		helper2("f", "g");
	}

    @Test
	public void test18() throws Exception{
//		printTestDisabledMessage("test for 79131 (corner case: reference "A.f" to p.A#f)");
		fUpdateReferences= false;
		fUpdateTextualMatches= true;
		helper2("field", "member");
	}

//--- test 1.5 features: ---

    @Test
	public void test19() throws Exception{
		fRenameGetter= true;
		fRenameSetter= true;
		helper2("list", "items");
	}

    @Test
	public void test20() throws Exception{
		helper2("list", "items");
	}

    @Test
	public void test21() throws Exception{
		helper2("fValue", "fOrdinal");
	}

    @Test
	public void test22() throws Exception{
		fRenameGetter= true;
		fRenameSetter= true;
		helper2("tee", "thing");
	}

    @Test
	public void test23() throws Exception{
		fRenameGetter= true;
		fRenameSetter= true;
		helper2();
	}

//--- end test 1.5 features. ---

    @Test
	public void testBug5821() throws Exception{
		helper2("test", "test1");
	}

    @Test
	public void testStaticImport() throws Exception{
		//bug 77622
		IPackageFragment test1= getRoot().createPackageFragment("test1", true, null);
		ICompilationUnit cuC= null;
		ICompilationUnit cuB= createCUfromTestFile(test1, "B");
		cuC= createCUfromTestFile(getRoot().getPackageFragment(""), "C");

		helper2("PI", "e");

		assertEqualLines("invalid renaming", getFileContents(getOutputTestFileName("B")), cuB.getSource());
		assertEqualLines("invalid renaming", getFileContents(getOutputTestFileName("C")), cuC.getSource());
	}

    @Test
	public void testEnumConst() throws Exception {
		//bug 77619
		IPackageFragment test1= getRoot().createPackageFragment("test1", true, null);
		ICompilationUnit cuC= null;
		ICompilationUnit cuB= createCUfromTestFile(test1, "B");
		cuC= createCUfromTestFile(getRoot().getPackageFragment(""), "C");

		helper2("RED", "REDDISH");

		assertEqualLines("invalid renaming", getFileContents(getOutputTestFileName("B")), cuB.getSource());
		assertEqualLines("invalid renaming", getFileContents(getOutputTestFileName("C")), cuC.getSource());
	}

    @Test
	public void testGenerics1() throws Exception {
		helper2();
	}

    @Test
	public void testGenerics2() throws Exception {
		fRenameSetter= true;
		fRenameGetter= true;
		helper2();
	}

    @Test
	@Ignore
	public void testGenerics3() throws Exception {
		if (BUG_79990_CORE_SEARCH_METHOD_DECL) {
			printTestDisabledMessage("BUG_79990_CORE_SEARCH_METHOD_DECL");
			return;
		}
		fRenameSetter= true;
		fRenameGetter= true;
		helper2();
	}

    @Test
	public void testGenerics4() throws Exception {
		fRenameSetter= true;
		fRenameGetter= true;
		helper2("count", "number");
	}

    @Test
	public void testEnumField() throws Exception {
		fRenameSetter= true;
		fRenameGetter= true;
		helper2("buddy", "other");
	}

    @Test
	public void testAnnotation1() throws Exception {
		helper2("ZERO", "ZORRO");
	}

    @Test
	public void testAnnotation2() throws Exception {
		helper2("ZERO", "ZORRO");
	}

    @Test
	public void testDelegate01() throws Exception {
		// a simple delegate
		helper2("f", "g", true);
	}

    @Test
	public void testDelegate02() throws Exception {
		// nonstatic field, getter and setter
		fRenameSetter= true;
		fRenameGetter= true;
		helper2("f", "g", true);
	}

    @Test
	public void testDelegate03() throws Exception {
		// create delegates for the field and a getter
		fRenameGetter= true;
		helper2("f", "g", true);
	}

    @Test
    @Ignore
	public void testRenameNLSAccessor01() throws Exception {
		IFile file= createPropertiesFromTestFile("messages");

		helper2("f", "g");

		assertEqualLines(getExpectedFileConent("messages"), getContents(file));
	}

	private String getExpectedFileConent(String propertyName) throws IOException {
		String fileName= getOutputTestFileName(propertyName);
		fileName= fileName.substring(0, fileName.length() - ".java".length()) + ".properties";
		return getContents(getFileInputStream(fileName));
	}

	private IFile createPropertiesFromTestFile(String propertyName) throws IOException, CoreException {
		IFolder pack= (IFolder) getPackageP().getResource();
		IFile file= pack.getFile(propertyName + ".properties");

		String fileName= getInputTestFileName(propertyName);
		fileName= fileName.substring(0, fileName.length() - ".java".length()) + ".properties";
		InputStream inputStream= getFileInputStream(fileName);
		file.create(inputStream, true, null);

		return file;
	}
}
