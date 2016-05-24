/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.java.server.jdt.refactoring;

import org.eclipse.che.plugin.java.server.che.BaseTest;
import org.eclipse.che.plugin.java.server.jdt.testplugin.JavaProjectHelper;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.ISourceManipulation;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.refactoring.descriptors.JavaRefactoringDescriptor;
import org.eclipse.jdt.internal.corext.refactoring.util.JavaElementUtil;
import org.eclipse.jdt.internal.corext.util.Strings;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.ChangeDescriptor;
import org.eclipse.ltk.core.refactoring.CheckConditionsOperation;
import org.eclipse.ltk.core.refactoring.CreateChangeOperation;
import org.eclipse.ltk.core.refactoring.IUndoManager;
import org.eclipse.ltk.core.refactoring.PerformChangeOperation;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringChangeDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringContribution;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.junit.Rule;
import org.junit.rules.TestName;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public abstract class RefactoringTest extends BaseTest {

	/**
	 * If <code>true</code> a descriptor is created from the change.
	 * The new descriptor is then used to create the refactoring again
	 * and run the refactoring. As this is very time consuming this should
	 * be <code>false</code> by default.
	 */
	private static final boolean DESCRIPTOR_TEST= false;

	@Rule
	public TestName name = new TestName();

	protected IPackageFragmentRoot fRoot;
	protected IPackageFragment     fPackageP;
	protected IJavaProject         fProject;

	public boolean fIsVerbose      = false;
	public boolean fIsPreDeltaTest = false;

	public static final String TEST_PATH_PREFIX = "";

	protected static final String TEST_INPUT_INFIX  = "/in/";
	protected static final String TEST_OUTPUT_INFIX = "/out/";
	protected static final String CONTAINER         = "src";

	protected static final List/*<String>*/ PROJECT_RESOURCE_CHILDREN = Arrays.asList(new String[]{
			".project", ".classpath", ".settings"});

//	public RefactoringTest(String name) {
//		super(name);
//	}

	public void setUp() throws Exception {
		fRoot = RefactoringTestSetup.getDefaultSourceFolder();
		fPackageP = RefactoringTestSetup.getPackageP();
		fIsPreDeltaTest = false;
		fProject = RefactoringTestSetup.getProject();

		if (fIsVerbose) {
			System.out.println("\n---------------------------------------------");
			System.out.println("\nTest:" + getClass() + ".");
		}
		RefactoringCore.getUndoManager().flush();
	}

	protected void mustPerformDummySearch() throws Exception {
		JavaProjectHelper.mustPerformDummySearch(getPackageP());
	}

	protected void performDummySearch() throws Exception {
		JavaProjectHelper.performDummySearch(getPackageP());
	}

	/**
	 * Removes contents of {@link #getPackageP()}, of {@link #getRoot()} (except for p) and of the
	 * Java project (except for src and the JRE library).
	 *
	 * @throws Exception in case of errors
	 */
	protected void tearDown() throws Exception {
		refreshFromLocal();
		performDummySearch();

		final boolean pExists = getPackageP().exists();
		if (pExists) {
			tryDeletingAllJavaChildren(getPackageP());
			tryDeletingAllNonJavaChildResources(getPackageP());
		}

		if (getRoot().exists()) {
			IJavaElement[] packages = getRoot().getChildren();
			for (int i = 0; i < packages.length; i++) {
				IPackageFragment pack = (IPackageFragment)packages[i];
				if (!pack.equals(getPackageP()) && pack.exists() && !pack.isReadOnly())
					if (pack.isDefaultPackage())
						JavaProjectHelper.deletePackage(pack); // also delete packages with subpackages
					else
						JavaProjectHelper.delete(pack.getResource()); // also delete packages with subpackages
			}
			// Restore package 'p'
			if (!pExists)
				getRoot().createPackageFragment("p", true, null);

			tryDeletingAllNonJavaChildResources(getRoot());
		}

		restoreTestProject();
	}

	private void restoreTestProject() throws Exception {
		IJavaProject javaProject = getRoot().getJavaProject();
		if (javaProject.exists()) {
			IClasspathEntry srcEntry = getRoot().getRawClasspathEntry();
			IClasspathEntry jreEntry = RefactoringTestSetup.getJRELibrary().getRawClasspathEntry();
			IClasspathEntry[] cpes = javaProject.getRawClasspath();
			ArrayList newCPEs = new ArrayList();
			boolean cpChanged = false;
			for (int i = 0; i < cpes.length; i++) {
				IClasspathEntry cpe = cpes[i];
				if (cpe.equals(srcEntry) || cpe.equals(jreEntry)) {
					newCPEs.add(cpe);
				} else {
					cpChanged = true;
				}
			}
			if (cpChanged) {
				IClasspathEntry[] newCPEsArray = (IClasspathEntry[])newCPEs.toArray(new IClasspathEntry[newCPEs.size()]);
				javaProject.setRawClasspath(newCPEsArray, null);
			}

			Object[] nonJavaResources = javaProject.getNonJavaResources();
			for (int i = 0; i < nonJavaResources.length; i++) {
				Object kid = nonJavaResources[i];
				if (kid instanceof IResource) {
					IResource resource = (IResource)kid;
					if (!PROJECT_RESOURCE_CHILDREN.contains(resource.getName())) {
						JavaProjectHelper.delete(resource);
					}
				}
			}
		}
	}

	private void refreshFromLocal() throws CoreException {
		if (getRoot().exists())
			getRoot().getResource().refreshLocal(IResource.DEPTH_INFINITE, null);
		else if (getPackageP().exists())//don't refresh package if root already refreshed
			getPackageP().getResource().refreshLocal(IResource.DEPTH_INFINITE, null);
	}

	private static void tryDeletingAllNonJavaChildResources(IPackageFragment pack) throws CoreException {
		Object[] nonJavaKids= pack.getNonJavaResources();
		for (int i= 0; i < nonJavaKids.length; i++) {
			if (nonJavaKids[i] instanceof IResource) {
				IResource resource= (IResource)nonJavaKids[i];
				JavaProjectHelper.delete(resource);
			}
		}
	}

	private static void tryDeletingAllNonJavaChildResources(IPackageFragmentRoot root) throws CoreException {
		Object[] nonJavaKids= root.getNonJavaResources();
		for (int i= 0; i < nonJavaKids.length; i++) {
			if (nonJavaKids[i] instanceof IResource) {
				IResource resource= (IResource)nonJavaKids[i];
				JavaProjectHelper.delete(resource);
			}
		}
	}
	
	private static void tryDeletingAllJavaChildren(IPackageFragment pack) throws CoreException {
		IJavaElement[] kids= pack.getChildren();
		for (int i= 0; i < kids.length; i++){
			if (kids[i] instanceof ISourceManipulation){
				if (kids[i].exists() && !kids[i].isReadOnly())
					JavaProjectHelper.delete(kids[i]);
			}
		}
	}

	protected IPackageFragmentRoot getRoot() {
		return fRoot;
	}

	protected IPackageFragment getPackageP() {
		return fPackageP;
	}

	protected final RefactoringStatus performRefactoring(RefactoringDescriptor descriptor) throws Exception {
		return performRefactoring(descriptor, true);
	}

	protected final RefactoringStatus performRefactoring(RefactoringDescriptor descriptor, boolean providesUndo) throws Exception {
		Refactoring refactoring= createRefactoring(descriptor);
		return performRefactoring(refactoring, providesUndo);
	}

    protected final Refactoring createRefactoring(RefactoringDescriptor descriptor) throws CoreException {
	    RefactoringStatus status= new RefactoringStatus();
		Refactoring refactoring= descriptor.createRefactoring(status);
		assertNotNull("refactoring should not be null", refactoring);
		assertTrue("status should be ok", status.isOK());
	    return refactoring;
    }

	protected final RefactoringStatus performRefactoring(Refactoring ref) throws Exception {
		return performRefactoring(ref, true);
	}

	protected final RefactoringStatus performRefactoring(Refactoring ref, boolean providesUndo) throws Exception {
		performDummySearch();
		IUndoManager undoManager= getUndoManager();
		if (DESCRIPTOR_TEST){
			final CreateChangeOperation create= new CreateChangeOperation(
					new CheckConditionsOperation(ref, CheckConditionsOperation.ALL_CONDITIONS),
					RefactoringStatus.FATAL);
			create.run(new NullProgressMonitor());
			RefactoringStatus checkingStatus= create.getConditionCheckingStatus();
			if (!checkingStatus.isOK())
				return checkingStatus;
			Change change= create.getChange();
			ChangeDescriptor descriptor= change.getDescriptor();
			if (descriptor instanceof RefactoringChangeDescriptor) {
				RefactoringChangeDescriptor rcd= (RefactoringChangeDescriptor) descriptor;
				RefactoringDescriptor refactoringDescriptor= rcd.getRefactoringDescriptor();
				if (refactoringDescriptor instanceof JavaRefactoringDescriptor) {
					JavaRefactoringDescriptor jrd= (JavaRefactoringDescriptor) refactoringDescriptor;
					RefactoringStatus validation= jrd.validateDescriptor();
					if (!validation.isOK())
						return validation;
					RefactoringStatus refactoringStatus= new RefactoringStatus();
					Class expected= jrd.getClass();
					RefactoringContribution contribution= RefactoringCore.getRefactoringContribution(jrd.getID());
					jrd= (JavaRefactoringDescriptor) contribution.createDescriptor(jrd.getID(), jrd.getProject(), jrd.getDescription(), jrd.getComment(), contribution.retrieveArgumentMap(jrd), jrd.getFlags());
					assertEquals(expected, jrd.getClass());
					ref= jrd.createRefactoring(refactoringStatus);
					if (!refactoringStatus.isOK())
						return refactoringStatus;
					TestRenameParticipantSingle.reset();
					TestCreateParticipantSingle.reset();
					TestMoveParticipantSingle.reset();
					TestDeleteParticipantSingle.reset();
				}
			}
		}
		final CreateChangeOperation create= new CreateChangeOperation(
			new CheckConditionsOperation(ref, CheckConditionsOperation.ALL_CONDITIONS),
			RefactoringStatus.FATAL);
		final PerformChangeOperation perform= new PerformChangeOperation(create);
		perform.setUndoManager(undoManager, ref.getName());
		IWorkspace workspace= ResourcesPlugin.getWorkspace();
		if (fIsPreDeltaTest) {
			IResourceChangeListener listener= new IResourceChangeListener() {
				public void resourceChanged(IResourceChangeEvent event) {
					if (create.getConditionCheckingStatus().isOK() &&  perform.changeExecuted()) {
						TestModelProvider.assertTrue(event.getDelta());
					}
				}
			};
			try {
				TestModelProvider.clearDelta();
				workspace.checkpoint(false);
				workspace.addResourceChangeListener(listener);
				executePerformOperation(perform, workspace);
			} finally {
				workspace.removeResourceChangeListener(listener);
			}
		} else {
			executePerformOperation(perform, workspace);
		}
		RefactoringStatus status= create.getConditionCheckingStatus();
		if (!status.isOK())
			return status;
		assertTrue("Change wasn't executed", perform.changeExecuted());
		Change undo= perform.getUndoChange();
		if (providesUndo) {
			assertNotNull("Undo doesn't exist", undo);
			assertTrue("Undo manager is empty", undoManager.anythingToUndo());
		} else {
			assertNull("Undo manager contains undo but shouldn't", undo);
		}
		return null;
	}

	protected void executePerformOperation(final PerformChangeOperation perform, IWorkspace workspace) throws CoreException {
		workspace.run(perform, new NullProgressMonitor());
	}

	public RefactoringStatus performRefactoringWithStatus(Refactoring ref) throws Exception, CoreException {
		RefactoringStatus status= performRefactoring(ref);
		if (status == null)
			return new RefactoringStatus();
		return status;
	}

	protected final Change performChange(Refactoring refactoring, boolean storeUndo) throws Exception {
		CreateChangeOperation create= new CreateChangeOperation(refactoring);
		PerformChangeOperation perform= new PerformChangeOperation(create);
		if (storeUndo) {
			perform.setUndoManager(getUndoManager(), refactoring.getName());
		}
		ResourcesPlugin.getWorkspace().run(perform, new NullProgressMonitor());
		assertTrue("Change wasn't executed", perform.changeExecuted());
		return perform.getUndoChange();
	}

	protected final Change performChange(final Change change) throws Exception {
		PerformChangeOperation perform= new PerformChangeOperation(change);
		ResourcesPlugin.getWorkspace().run(perform, new NullProgressMonitor());
		assertTrue("Change wasn't executed", perform.changeExecuted());
		return perform.getUndoChange();
	}

	protected IUndoManager getUndoManager() {
		IUndoManager undoManager= RefactoringCore.getUndoManager();
		undoManager.flush();
		return undoManager;
	}

	/* ===================  helpers  ================= */
	protected IType getType(ICompilationUnit cu, String name) throws JavaModelException {
		IType[] types= cu.getAllTypes();
		for (int i= 0; i < types.length; i++)
			if (types[i].getTypeQualifiedName('.').equals(name) ||
			    types[i].getElementName().equals(name))
				return types[i];
		return null;
	}

	/*
	 * subclasses override to inform about the location of their test cases
	 */
	protected String getRefactoringPath() {
		return "";
	}

	/*
	 *  example "RenameType/"
	 */
	protected String getTestPath() {
		return TEST_PATH_PREFIX + getRefactoringPath();
	}

	protected String createTestFileName(String cuName, String infix) {
		return getTestPath() + name.getMethodName() + infix + cuName + ".java";
	}

	protected String getInputTestFileName(String cuName) {
		return createTestFileName(cuName, TEST_INPUT_INFIX);
	}

	/*
	 * @param subDirName example "p/" or "org/eclipse/jdt/"
	 */
	protected String getInputTestFileName(String cuName, String subDirName) {
		return createTestFileName(cuName, TEST_INPUT_INFIX + subDirName);
	}

	protected String getOutputTestFileName(String cuName) {
		return createTestFileName(cuName, TEST_OUTPUT_INFIX);
	}

	/*
	 * @param subDirName example "p/" or "org/eclipse/jdt/"
	 */
	protected String getOutputTestFileName(String cuName, String subDirName) {
		return createTestFileName(cuName, TEST_OUTPUT_INFIX + subDirName);
	}

	protected ICompilationUnit createCUfromTestFile(IPackageFragment pack, String cuName) throws Exception {
		return createCUfromTestFile(pack, cuName, true);
	}

	protected ICompilationUnit createCUfromTestFile(IPackageFragment pack, String cuName, String subDirName) throws Exception {
		return createCUfromTestFile(pack, cuName, subDirName, true);
	}

	protected ICompilationUnit createCUfromTestFile(IPackageFragment pack, String cuName, boolean input) throws Exception {
		String contents= input
					? getFileContents(getInputTestFileName(cuName))
					: getFileContents(getOutputTestFileName(cuName));
		return createCU(pack, cuName + ".java", contents);
	}

	protected ICompilationUnit createCUfromTestFile(IPackageFragment pack, String cuName, String subDirName, boolean input) throws Exception {
		String contents= input
			? getFileContents(getInputTestFileName(cuName, subDirName))
			: getFileContents(getOutputTestFileName(cuName, subDirName));

		return createCU(pack, cuName + ".java", contents);
	}

	protected void printTestDisabledMessage(String explanation){
		System.out.println("\n" +getClass().getName() + "::"+/* getName() + */" disabled (" + explanation + ")");
	}

	//-----------------------
	public static InputStream getStream(String content){
		return new ByteArrayInputStream(content.getBytes());
	}

	public static IPackageFragmentRoot getSourceFolder(IJavaProject javaProject, String name) throws JavaModelException{
		IPackageFragmentRoot[] roots= javaProject.getPackageFragmentRoots();
		for (int i= 0; i < roots.length; i++) {
			if (! roots[i].isArchive() && roots[i].getElementName().equals(name))
				return roots[i];
		}
		return null;
	}

	public String getFileContents(String fileName) throws IOException {
		return getContents(getFileInputStream(fileName));
	}

	public static String getContents(IFile file) throws IOException, CoreException {
		return getContents(file.getContents());
	}

	public static ICompilationUnit createCU(IPackageFragment pack, String name, String contents) throws Exception {
		assertTrue(!pack.getCompilationUnit(name).exists());
		ICompilationUnit cu= pack.createCompilationUnit(name, contents, true, null);
		cu.save(null, true);
		return cu;
	}

	public static String getContents(InputStream in) throws IOException {
		BufferedReader br= new BufferedReader(new InputStreamReader(in));

		StringBuffer sb= new StringBuffer(300);
		try {
			int read= 0;
			while ((read= br.read()) != -1)
				sb.append((char) read);
		} finally {
			br.close();
		}
		return sb.toString();
	}

	public static InputStream getFileInputStream(String fileName) throws IOException {
//		return RefactoringTestPlugin.getDefault().getTestResourceStream(fileName);
        return  RefactoringTest.class.getResource("/" + fileName).openStream();

    }

	public static String removeExtension(String fileName) {
		return fileName.substring(0, fileName.lastIndexOf('.'));
	}

	public static IMember[] merge(IMember[] a1, IMember[] a2, IMember[] a3){
		return JavaElementUtil.merge(JavaElementUtil.merge(a1, a2), a3);
	}

	public static IMember[] merge(IMember[] a1, IMember[] a2){
		return JavaElementUtil.merge(a1, a2);
	}

	public static IField[] getFields(IType type, String[] names) {
		if (names == null )
			return new IField[0];
		Set fields= new HashSet();
		for (int i = 0; i < names.length; i++) {
			IField field= type.getField(names[i]);
			assertTrue("field " + field.getElementName() + " does not exist", field.exists());
			fields.add(field);
		}
		return (IField[]) fields.toArray(new IField[fields.size()]);
	}

	public static IType[] getMemberTypes(IType type, String[] names) {
		if (names == null )
			return new IType[0];
		Set memberTypes= new HashSet();
		for (int i = 0; i < names.length; i++) {
			IType memberType;
			if (names[i].indexOf('.') != -1) {
				String[] path= names[i].split("\\.");
				memberType= type.getType(path[0]);
				for (int j= 1; j < path.length; j++) {
					memberType= memberType.getType(path[j]);
				}
			} else
				memberType= type.getType(names[i]);
			assertTrue("member type " + memberType.getElementName() + " does not exist", memberType.exists());
			memberTypes.add(memberType);
		}
		return (IType[]) memberTypes.toArray(new IType[memberTypes.size()]);
	}

	public static IMethod[] getMethods(IType type, String[] names, String[][] signatures) {
		if (names == null || signatures == null)
			return new IMethod[0];
		List methods= new ArrayList(names.length);
		for (int i = 0; i < names.length; i++) {
			IMethod method= type.getMethod(names[i], signatures[i]);
			assertTrue("method " + method.getElementName() + " does not exist", method.exists());
			if (!methods.contains(method))
				methods.add(method);
		}
		return (IMethod[]) methods.toArray(new IMethod[methods.size()]);
	}

	public static IType[] findTypes(IType[] types, String[] namesOfTypesToPullUp) {
		List found= new ArrayList(types.length);
		for (int i= 0; i < types.length; i++) {
			IType type= types[i];
			for (int j= 0; j < namesOfTypesToPullUp.length; j++) {
				String name= namesOfTypesToPullUp[j];
				if (type.getElementName().equals(name))
					found.add(type);
			}
		}
		return (IType[]) found.toArray(new IType[found.size()]);
	}

	public static IField[] findFields(IField[] fields, String[] namesOfFieldsToPullUp) {
		List found= new ArrayList(fields.length);
		for (int i= 0; i < fields.length; i++) {
			IField field= fields[i];
			for (int j= 0; j < namesOfFieldsToPullUp.length; j++) {
				String name= namesOfFieldsToPullUp[j];
				if (field.getElementName().equals(name))
					found.add(field);
			}
		}
		return (IField[]) found.toArray(new IField[found.size()]);
	}

	public static IMethod[] findMethods(IMethod[] selectedMethods, String[] namesOfMethods, String[][] signaturesOfMethods){
		List found= new ArrayList(selectedMethods.length);
		for (int i= 0; i < selectedMethods.length; i++) {
			IMethod method= selectedMethods[i];
			String[] paramTypes= method.getParameterTypes();
			for (int j= 0; j < namesOfMethods.length; j++) {
				String methodName= namesOfMethods[j];
				if (! methodName.equals(method.getElementName()))
					continue;
				String[] methodSig= signaturesOfMethods[j];
				if (! areSameSignatures(paramTypes, methodSig))
					continue;
				found.add(method);
			}
		}
		return (IMethod[]) found.toArray(new IMethod[found.size()]);
	}

	private static boolean areSameSignatures(String[] s1, String[] s2){
		if (s1.length != s2.length)
			return false;
		for (int i= 0; i < s1.length; i++) {
			if (! s1[i].equals(s2[i]))
				return false;
		}
		return true;
	}

	/**
	 * Line-based version of junit.framework.Assert.assertEquals(String, String)
	 * without considering line delimiters.
	 * @param expected the expected value
	 * @param actual the actual value
	 */
	public static void assertEqualLines(String expected, String actual) {
		assertEqualLines("", expected, actual);
	}

	/**
	 * Line-based version of junit.framework.Assert.assertEquals(String, String, String)
	 * without considering line delimiters.
	 * @param message the message
	 * @param expected the expected value
	 * @param actual the actual value
	 */
	public static void assertEqualLines(String message, String expected, String actual) {
		String[] expectedLines= Strings.convertIntoLines(expected);
		String[] actualLines= Strings.convertIntoLines(actual);

		String expected2= (expectedLines == null ? null : Strings.concatenate(expectedLines, "\n"));
		String actual2= (actualLines == null ? null : Strings.concatenate(actualLines, "\n"));
		assertEquals(message, expected2, actual2);
	}

}
