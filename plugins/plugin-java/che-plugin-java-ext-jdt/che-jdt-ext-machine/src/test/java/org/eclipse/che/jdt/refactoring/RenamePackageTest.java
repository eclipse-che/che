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
package org.eclipse.che.jdt.refactoring;

import org.eclipse.che.jdt.testplugin.JavaProjectHelper;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.refactoring.IJavaElementMapper;
import org.eclipse.jdt.core.refactoring.IJavaRefactorings;
import org.eclipse.jdt.core.refactoring.descriptors.RenameJavaElementDescriptor;
import org.eclipse.jdt.internal.core.refactoring.descriptors.RefactoringSignatureDescriptorFactory;
import org.eclipse.jdt.internal.corext.refactoring.rename.RenamePackageProcessor;
import org.eclipse.jdt.internal.corext.refactoring.util.JavaElementUtil;
import org.eclipse.jdt.internal.ui.util.CoreUtility;
import org.eclipse.ltk.core.refactoring.IResourceMapper;
import org.eclipse.ltk.core.refactoring.IUndoManager;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.MoveArguments;
import org.eclipse.ltk.core.refactoring.participants.ProcessorBasedRefactoring;
import org.eclipse.ltk.core.refactoring.participants.RefactoringProcessor;
import org.eclipse.ltk.core.refactoring.participants.RenameArguments;
import org.eclipse.ltk.core.refactoring.participants.RenameRefactoring;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


public class RenamePackageTest extends RefactoringTest {
    private static final boolean BUG_PACKAGE_CANT_BE_RENAMED_TO_A_PACKAGE_THAT_ALREADY_EXISTS = true;
    private static final boolean BUG_6054                                                     = false;
    private static final boolean BUG_54962_71267                                              = false;

    //	private static final Class clazz= RenamePackageTests.class;
    private static final String REFACTORING_PATH = "RenamePackage/";

    private boolean fUpdateReferences;
    private boolean fUpdateTextualMatches;
    private String  fQualifiedNamesFilePatterns;
    private boolean fRenameSubpackages;

//	public RenamePackageTest(String name) {
//		super(name);
//	}
//
//	public static Test suite() {
//		return new Java15Setup(new TestSuite(clazz));
//	}
//
//	public static Test setUpTest(Test someTest) {
//		return new Java15Setup(someTest);
//	}

    //	public void run(TestResult result) {
//		System.out.println("--- " + getName() + " - RenamePackageTests ---");
//		super.run(result);
//	}
    private RefactoringTestSetup setup = new RefactoringTestSetup();

    @BeforeClass
    public static void prepareClass() {
        ParticipantTesting.init();
    }

    @Before
    public void setUp() throws Exception {
        setup.setUp();
        super.setUp();
        fUpdateReferences = true;
        fUpdateTextualMatches = false;
        fQualifiedNamesFilePatterns = null;
        fRenameSubpackages = false;
        // fIsPreDeltaTest= true;

    }

    @Override
    @After
    public void tearDown() throws Exception {
//		super.tearDown();
        setup.tearDown();
    }

    protected String getRefactoringPath() {
        return REFACTORING_PATH;
    }

    // -------------
    private RenameJavaElementDescriptor createRefactoringDescriptor(IPackageFragment pack, String newName) {
        RenameJavaElementDescriptor descriptor =
                RefactoringSignatureDescriptorFactory.createRenameJavaElementDescriptor(IJavaRefactorings.RENAME_PACKAGE);
        descriptor.setJavaElement(pack);
        descriptor.setNewName(newName);
        descriptor.setUpdateReferences(true);
        return descriptor;
    }

    /* non java-doc
     * the 0th one is the one to rename
     */
    private void helper1(String packageNames[], String[][] packageFiles, String newPackageName) throws Exception {
        IPackageFragment[] packages = new IPackageFragment[packageNames.length];
        for (int i = 0; i < packageFiles.length; i++) {
            packages[i] = getRoot().createPackageFragment(packageNames[i], true, null);
            for (int j = 0; j < packageFiles[i].length; j++) {
                createCUfromTestFile(packages[i], packageFiles[i][j], packageNames[i].replace('.', '/') + "/");
                //DebugUtils.dump(cu.getElementName() + "\n" + cu.getSource());
            }
        }
        IPackageFragment thisPackage = packages[0];
        RefactoringStatus result = performRefactoring(createRefactoringDescriptor(thisPackage, newPackageName));
        assertNotNull("precondition was supposed to fail", result);
//		if (fIsVerbose)
//			DebugUtils.dump("" + result);
    }

    private void helper1() throws Exception {
        helper1(new String[]{"r"}, new String[][]{{"A"}}, "p1");
    }

    private RenamePackageProcessor helper2(String[] packageNames, String[][] packageFileNames, String newPackageName) throws Exception {
        ParticipantTesting.reset();
        IPackageFragment[] packages = new IPackageFragment[packageNames.length];
        ICompilationUnit[][] cus = new ICompilationUnit[packageFileNames.length][packageFileNames[0].length];
        for (int i = 0; i < packageNames.length; i++) {
            packages[i] = getRoot().createPackageFragment(packageNames[i], true, null);
            for (int j = 0; j < packageFileNames[i].length; j++) {
                cus[i][j] = createCUfromTestFile(packages[i], packageFileNames[i][j], packageNames[i].replace('.', '/') + "/");
            }
        }
        IPackageFragment thisPackage = packages[0];
        boolean hasSubpackages = thisPackage.hasSubpackages();

        IPath path = thisPackage.getParent().getPath();
        path = path.append(newPackageName.replace('.', '/'));
        IFolder target = ResourcesPlugin.getWorkspace().getRoot().getFolder(path);
        boolean targetExists = target.exists();
        boolean isRename =
                !targetExists && !thisPackage.hasSubpackages() && thisPackage.getResource().getParent().equals(target.getParent());

        String[] createHandles = null;
        String[] moveHandles = null;
        String[] deleteHandles = null;
        boolean doDelete = true;

        String[] renameHandles = null;
        if (isRename) {
            renameHandles = ParticipantTesting.createHandles(thisPackage, thisPackage.getResource());
        } else {
            renameHandles = ParticipantTesting.createHandles(thisPackage);
            IContainer loop = target;
            List handles = new ArrayList();
            while (loop != null && !loop.exists()) {
                handles.add(ParticipantTesting.createHandles(loop)[0]);
                loop = loop.getParent();
            }
            createHandles = (String[])handles.toArray(new String[handles.size()]);
            IFolder source = (IFolder)thisPackage.getResource();
            deleteHandles = ParticipantTesting.createHandles(source);
            IResource members[] = source.members();
            List movedObjects = new ArrayList();
            for (int i = 0; i < members.length; i++) {
                if (members[i] instanceof IFolder) {
                    doDelete = false;
                } else {
                    movedObjects.add(members[i]);
                }
            }
            moveHandles = ParticipantTesting.createHandles(movedObjects.toArray());
        }
        RenameJavaElementDescriptor descriptor = createRefactoringDescriptor(thisPackage, newPackageName);
        descriptor.setUpdateReferences(fUpdateReferences);
        descriptor.setUpdateTextualOccurrences(fUpdateTextualMatches);
        setFilePatterns(descriptor);
        Refactoring refactoring = createRefactoring(descriptor);
        RefactoringStatus result = performRefactoring(refactoring);
        assertEquals("preconditions were supposed to pass", null, result);

        if (isRename) {
            ParticipantTesting.testRename(renameHandles,
                                          new RenameArguments[]{
                                                  new RenameArguments(newPackageName, fUpdateReferences),
                                                  new RenameArguments(target.getName(), fUpdateReferences)
                                          }
                                         );
        } else {
            ParticipantTesting.testRename(renameHandles,
                                          new RenameArguments[]{
                                                  new RenameArguments(newPackageName, fUpdateReferences)});

            ParticipantTesting.testCreate(createHandles);

            List args = new ArrayList();
            for (int i = 0; i < packageFileNames[0].length; i++) {
                args.add(new MoveArguments(target, fUpdateReferences));
            }
            ParticipantTesting.testMove(moveHandles, (MoveArguments[])args.toArray(new MoveArguments[args.size()]));

            if (doDelete) {
                ParticipantTesting.testDelete(deleteHandles);
            } else {
                ParticipantTesting.testDelete(new String[0]);
            }
        }

        //---

        if (hasSubpackages) {
            assertTrue("old package does not exist anymore", getRoot().getPackageFragment(packageNames[0]).exists());
        } else {
            assertTrue("package not renamed", !getRoot().getPackageFragment(packageNames[0]).exists());
        }
        IPackageFragment newPackage = getRoot().getPackageFragment(newPackageName);
        assertTrue("new package does not exist", newPackage.exists());

        for (int i = 0; i < packageFileNames.length; i++) {
            String packageName = (i == 0)
                                 ? newPackageName.replace('.', '/') + "/"
                                 : packageNames[i].replace('.', '/') + "/";
            for (int j = 0; j < packageFileNames[i].length; j++) {
                String s1 = getFileContents(getOutputTestFileName(packageFileNames[i][j], packageName));
                ICompilationUnit cu =
                        (i == 0)
                        ? newPackage.getCompilationUnit(packageFileNames[i][j] + ".java")
                        : cus[i][j];
                //DebugUtils.dump("cu:" + cu.getElementName());
                String s2 = cu.getSource();

                //DebugUtils.dump("expected:" + s1);
                //DebugUtils.dump("was:" + s2);
                assertEqualLines("invalid update in file " + cu.getElementName(), s1, s2);
            }
        }
        RefactoringProcessor processor = ((ProcessorBasedRefactoring)refactoring).getProcessor();
        return (RenamePackageProcessor)processor;
    }

    private void performUndo() throws Exception {
        IUndoManager um = RefactoringCore.getUndoManager();
        assertTrue(um.anythingToUndo());
        um.performUndo(null, new NullProgressMonitor());
        assertFalse(um.anythingToUndo());
        assertTrue(um.anythingToRedo());
    }

    /**
     * Custom project and source folder structure.
     *
     * @param roots source folders
     * @param packageNames package names per root
     * @param newPackageName the new package name for packageNames[0][0]
     * @param cuNames cu names per package
     * @throws Exception if one of the resources cannot be created
     */
    private void helperMultiProjects(IPackageFragmentRoot[] roots, String[][] packageNames, String newPackageName, String[][][] cuNames)
            throws Exception {
        ICompilationUnit[][][] cus = new ICompilationUnit[roots.length][][];
        IPackageFragment thisPackage = null;

        for (int r = 0; r < roots.length; r++) {
            IPackageFragment[] packages = new IPackageFragment[packageNames[r].length];
            cus[r] = new ICompilationUnit[packageNames[r].length][];
            for (int pa = 0; pa < packageNames[r].length; pa++) {
                packages[pa] = roots[r].createPackageFragment(packageNames[r][pa], true, null);
                cus[r][pa] = new ICompilationUnit[cuNames[r][pa].length];
                if (r == 0 && pa == 0)
                    thisPackage = packages[pa];
                for (int typ = 0; typ < cuNames[r][pa].length; typ++) {
                    cus[r][pa][typ] = createCUfromTestFile(packages[pa], cuNames[r][pa][typ],
                                                           roots[r].getElementName() + "/" + packageNames[r][pa].replace('.', '/') + "/");
                }
            }
        }

        RenameJavaElementDescriptor descriptor = createRefactoringDescriptor(thisPackage, newPackageName);
        descriptor.setUpdateReferences(fUpdateReferences);
        descriptor.setUpdateTextualOccurrences(fUpdateTextualMatches);
        setFilePatterns(descriptor);
        descriptor.setUpdateHierarchy(fRenameSubpackages);
        RefactoringStatus result = performRefactoring(descriptor);
        assertEquals("preconditions were supposed to pass", null, result);

        assertTrue("package not renamed", !roots[0].getPackageFragment(packageNames[0][0]).exists());
        IPackageFragment newPackage = roots[0].getPackageFragment(newPackageName);
        assertTrue("new package does not exist", newPackage.exists());

        for (int r = 0; r < cuNames.length; r++) {
            for (int pa = 0; pa < cuNames[r].length; pa++) {
                String packageName = roots[r].getElementName() + "/" +
                                     ((r == 0 && pa == 0) ? newPackageName : packageNames[r][pa]).replace('.', '/') + "/";
                for (int typ = 0; typ < cuNames[r][pa].length; typ++) {
                    String s1 = getFileContents(getOutputTestFileName(cuNames[r][pa][typ], packageName));
                    ICompilationUnit cu = (r == 0 && pa == 0)
                                          ? newPackage.getCompilationUnit(cuNames[r][pa][typ] + ".java")
                                          : cus[r][pa][typ];
                    //DebugUtils.dump("cu:" + cu.getElementName());
                    String s2 = cu.getSource();

                    //DebugUtils.dump("expected:" + s1);
                    //DebugUtils.dump("was:" + s2);
                    assertEqualLines("invalid update in file " + cu.toString(), s1, s2);
                }
            }
        }
    }

    /**
     * 2 Projects with a root each: Project RenamePack2 (root: srcTest) requires project RenamePack1
     * (root: srcPrg).
     *
     * @param packageNames package names per root
     * @param newPackageName the new package name for packageNames[0][0]
     * @param cuNames cu names per package
     * @throws Exception if one of the resources cannot be created
     */
    private void helperProjectsPrgTest(String[][] packageNames, String newPackageName, String[][][] cuNames) throws Exception {
        IJavaProject projectPrg = null;
        IJavaProject projectTest = null;
        try {
            projectPrg = JavaProjectHelper.createJavaProject("RenamePack1", "bin");
            assertNotNull(JavaProjectHelper.addRTJar(projectPrg));
            IPackageFragmentRoot srcPrg = JavaProjectHelper.addSourceContainer(projectPrg, "srcPrg");
            Map optionsPrg = projectPrg.getOptions(false);
            JavaProjectHelper.set15CompilerOptions(optionsPrg);
            projectPrg.setOptions(optionsPrg);

            projectTest = JavaProjectHelper.createJavaProject("RenamePack2", "bin");
            assertNotNull(JavaProjectHelper.addRTJar(projectTest));
            IPackageFragmentRoot srcTest = JavaProjectHelper.addSourceContainer(projectTest, "srcTest");
            Map optionsTest = projectTest.getOptions(false);
            JavaProjectHelper.set15CompilerOptions(optionsTest);
            projectTest.setOptions(optionsTest);

            JavaProjectHelper.addRequiredProject(projectTest, projectPrg);

            helperMultiProjects(new IPackageFragmentRoot[]{srcPrg, srcTest}, packageNames, newPackageName, cuNames);
        } finally {
            JavaProjectHelper.delete(projectPrg);
            JavaProjectHelper.delete(projectTest);
        }
    }

    /*
     * Multiple source folders in the same project.
     * @param newPackageName the new package name for packageNames[0][0]
     */
    private void helperMultiRoots(String[] rootNames, String[][] packageNames, String newPackageName, String[][][] typeNames)
            throws Exception {
        IPackageFragmentRoot[] roots = new IPackageFragmentRoot[rootNames.length];
        try {
            for (int r = 0; r < roots.length; r++)
                roots[r] = JavaProjectHelper.addSourceContainer(getRoot().getJavaProject(), rootNames[r]);
            helperMultiProjects(roots, packageNames, newPackageName, typeNames);
        } catch (CoreException e) {
        }
        for (int r = 0; r < roots.length; r++)
            JavaProjectHelper.removeSourceContainer(getRoot().getJavaProject(), rootNames[r]);
    }

    private void setFilePatterns(RenameJavaElementDescriptor descriptor) {
        descriptor.setUpdateQualifiedNames(fQualifiedNamesFilePatterns != null);
        if (fQualifiedNamesFilePatterns != null)
            descriptor.setFileNamePatterns(fQualifiedNamesFilePatterns);
    }

    private void checkMappingUnchanged(IJavaElementMapper jm, IResourceMapper rm, Object[] resOrJEs) {
        for (int i = 0; i < resOrJEs.length; i++) {
            Object resOrJE = resOrJEs[i];
            if (resOrJE instanceof IJavaElement) {
                IJavaElement javaElement = (IJavaElement)resOrJE;
                resOrJE = javaElement.getResource();
                assertEquals(javaElement, jm.getRefactoredJavaElement(javaElement));
            }
            if (resOrJE instanceof IResource) {
                IResource resource = (IResource)resOrJE;
                assertEquals(resource, rm.getRefactoredResource(resource));
            }
        }
    }

    private void checkMappingChanged(IJavaElementMapper jm, IResourceMapper rm, Object[][] resOrJeToChangeds) {
        for (int i = 0; i < resOrJeToChangeds.length; i++) {
            Object[] resOrJeToChanged = resOrJeToChangeds[i];
            Object resOrJE = resOrJeToChanged[0];
            Object changed = resOrJeToChanged[1];
            if (resOrJE instanceof IJavaElement) {
                IJavaElement javaElement = (IJavaElement)resOrJE;
                assertEquals(changed, jm.getRefactoredJavaElement(javaElement));
                resOrJE = javaElement.getResource();
                changed = ((IJavaElement)resOrJeToChanged[1]).getResource();
            }
            if (resOrJE instanceof IResource) {
                IResource resource = (IResource)resOrJE;
                assertEquals(changed, rm.getRefactoredResource(resource));
            }
        }
    }

    @Test
    public void testPackageRenameWithResource1() throws Exception {
        IPackageFragment fragment = getRoot().createPackageFragment("org.test", true, null);

        StringBuffer buf = new StringBuffer();
        buf.append("package org.test;\n");
        buf.append("public class MyClass {\n");
        buf.append("	org.test.MyClass me;\n");
        buf.append("}\n");
        fragment.createCompilationUnit("MyClass.java", buf.toString(), true, null);

        IFile file = ((IFolder)getRoot().getResource()).getFile("x.properties");
        byte[] content = "This is about 'org.test' and more".getBytes();
        file.create(new ByteArrayInputStream(content), true, null);
        file.refreshLocal(IResource.DEPTH_ONE, null);

        RenameJavaElementDescriptor descriptor =
                RefactoringSignatureDescriptorFactory.createRenameJavaElementDescriptor(IJavaRefactorings.RENAME_PACKAGE);
        descriptor.setJavaElement(fragment);
        descriptor.setNewName("org.test2");
        descriptor.setUpdateReferences(true);
        descriptor.setUpdateQualifiedNames(true);
        descriptor.setFileNamePatterns("*.properties");
        Refactoring refactoring = createRefactoring(descriptor);
        RefactoringStatus status = performRefactoring(refactoring);
        if (status != null)
            assertTrue(status.toString(), status.isOK());

        RefactoringProcessor processor = ((RenameRefactoring)refactoring).getProcessor();
        IResourceMapper rm = (IResourceMapper)processor.getAdapter(IResourceMapper.class);
        IJavaElementMapper jm = (IJavaElementMapper)processor.getAdapter(IJavaElementMapper.class);
        checkMappingUnchanged(jm, rm, new Object[]{getRoot().getJavaProject(), getRoot(), file});
        IFile newFile = ((IContainer)getRoot().getResource()).getFile(new Path("x.properties"));
        assertEquals("This is about 'org.test2' and more", getContents(newFile));
        checkMappingChanged(jm, rm, new Object[][]{
                {fragment, getRoot().getPackageFragment("org.test2")}
        });
    }

    // ---------- tests -------------

    @Test
    public void testPackageRenameWithResource2() throws Exception {
        IPackageFragment fragment = getRoot().createPackageFragment("org.test", true, null);

        StringBuffer buf = new StringBuffer();
        buf.append("package org.test;\n");
        buf.append("public class MyClass {\n");
        buf.append("}\n");
        fragment.createCompilationUnit("MyClass.java", buf.toString(), true, null);

        IFile file = ((IFolder)fragment.getResource()).getFile("x.properties");
        byte[] content = "This is about 'org.test' and more".getBytes();
        file.create(new ByteArrayInputStream(content), true, null);
        file.refreshLocal(IResource.DEPTH_ONE, null);

        RenameJavaElementDescriptor descriptor =
                RefactoringSignatureDescriptorFactory.createRenameJavaElementDescriptor(IJavaRefactorings.RENAME_PACKAGE);
        descriptor.setJavaElement(fragment);
        descriptor.setNewName("org.test2");
        descriptor.setUpdateReferences(true);
        descriptor.setUpdateQualifiedNames(true);
        descriptor.setFileNamePatterns("*.properties");
        Refactoring refactoring = createRefactoring(descriptor);
        RefactoringStatus status = performRefactoring(refactoring);
        if (status != null)
            assertTrue(status.toString(), status.isOK());

        RefactoringProcessor processor = ((RenameRefactoring)refactoring).getProcessor();
        IResourceMapper rm = (IResourceMapper)processor.getAdapter(IResourceMapper.class);
        IJavaElementMapper jm = (IJavaElementMapper)processor.getAdapter(IJavaElementMapper.class);
        checkMappingUnchanged(jm, rm, new Object[]{getRoot().getJavaProject(), getRoot()});
        IPackageFragment newFragment = getRoot().getPackageFragment("org.test2");
        IFile newFile = ((IContainer)newFragment.getResource()).getFile(new Path("x.properties"));
        assertEquals("This is about 'org.test2' and more", getContents(newFile));
        checkMappingChanged(jm, rm, new Object[][]{
                {fragment, newFragment},
                {file, newFile},
        });
    }

    @Test
    @Ignore
    public void testPackageRenameWithResource3() throws Exception {
        // regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=108019
        fIsPreDeltaTest = true;

        fQualifiedNamesFilePatterns = "*.txt";
        String textFileName = "Textfile.txt";

        String textfileContent = getFileContents(getTestPath() + name.getMethodName() + TEST_INPUT_INFIX + "my/pack/" + textFileName);
        IFolder myPackFolder = getRoot().getJavaProject().getProject().getFolder("my").getFolder("pack");
        CoreUtility.createFolder(myPackFolder, true, true, null);
        IFile textfile = myPackFolder.getFile(textFileName);
        textfile.create(new ByteArrayInputStream(textfileContent.getBytes()), true, null);

        helper2(new String[]{"my.pack", "my"}, new String[][]{{}, {}}, "my");

        InputStreamReader reader = new InputStreamReader(textfile.getContents(true));
        StringBuffer newContent = new StringBuffer();
        try {
            int ch;
            while ((ch = reader.read()) != -1)
                newContent.append((char)ch);
        } finally {
            reader.close();
        }
        String definedContent = getFileContents(getTestPath() + name.getMethodName() + TEST_OUTPUT_INFIX + "my/" + textFileName);
        assertEqualLines("invalid updating", definedContent, newContent.toString());
    }

    @Test
    public void testHierarchical01() throws Exception {
        fRenameSubpackages = true;

        PackageRename rename = new PackageRename(new String[]{"my", "my.a", "my.b"}, new String[][]{{"MyA"}, {"ATest"}, {"B"}}, "your");
        IPackageFragment thisPackage = rename.fPackages[0];

        ParticipantTesting.reset();
        List toRename = new ArrayList(Arrays.asList(JavaElementUtil.getPackageAndSubpackages(thisPackage)));
        toRename.add(thisPackage.getResource());
        String[] renameHandles = ParticipantTesting.createHandles(toRename.toArray());

        rename.execute();

        ParticipantTesting.testRename(renameHandles, new RenameArguments[]{
                new RenameArguments(rename.getNewPackageName(rename.fPackageNames[0]), true),
                new RenameArguments(rename.getNewPackageName(rename.fPackageNames[1]), true),
                new RenameArguments(rename.getNewPackageName(rename.fPackageNames[2]), true),
                new RenameArguments("your", true)
        });
    }

    @Test
    @Ignore
    public void testHierarchical02() throws Exception {
        if (BUG_PACKAGE_CANT_BE_RENAMED_TO_A_PACKAGE_THAT_ALREADY_EXISTS) {
            printTestDisabledMessage("package can't be renamed to a package that already exists.");
            return;
        }
        fRenameSubpackages = true;

        PackageRename rename =
                new PackageRename(new String[]{"my", "my.a", "my.b", "your"}, new String[][]{{"MyA"}, {"ATest"}, {"B"}, {"Y"}}, "your");
        IPackageFragment thisPackage = rename.fPackages[0];
        IPath srcPath = thisPackage.getParent().getPath();
        IFolder target = ResourcesPlugin.getWorkspace().getRoot().getFolder(srcPath.append("your"));

        ParticipantTesting.reset();
        String[] createHandles = ParticipantTesting.createHandles(target.getFolder("a"), target.getFolder("b"));
        String[] deleteHandles = ParticipantTesting.createHandles(thisPackage.getResource());
        String[] moveHandles = ParticipantTesting.createHandles(new Object[]{
                rename.fCus[0][0].getResource(),
                rename.fCus[1][0].getResource(),
                rename.fCus[2][0].getResource(),
        });
        String[] renameHandles = ParticipantTesting.createHandles(JavaElementUtil.getPackageAndSubpackages(thisPackage));

        rename.execute();

        ParticipantTesting.testCreate(createHandles);
        ParticipantTesting.testDelete(deleteHandles);
        ParticipantTesting.testMove(moveHandles, new MoveArguments[]{
                new MoveArguments(target, true),
                new MoveArguments(target.getFolder("a"), true),
                new MoveArguments(target.getFolder("b"), true),
        });
        ParticipantTesting.testRename(renameHandles, new RenameArguments[]{
                new RenameArguments(rename.getNewPackageName(rename.fPackageNames[0]), true),
                new RenameArguments(rename.getNewPackageName(rename.fPackageNames[1]), true),
                new RenameArguments(rename.getNewPackageName(rename.fPackageNames[2]), true),
        });
    }

    @Test
    @Ignore
    public void testHierarchical03() throws Exception {
        fRenameSubpackages = true;
        fUpdateTextualMatches = true;

        PackageRename rename = new PackageRename(new String[]{"my", "my.pack"}, new String[][]{{}, {"C"}}, "your");
        IPackageFragment thisPackage = rename.fPackages[0];

        ParticipantTesting.reset();

        List toRename = new ArrayList(Arrays.asList(JavaElementUtil.getPackageAndSubpackages(thisPackage)));
        toRename.add(thisPackage.getResource());
        String[] createHandles = {};
        String[] deleteHandles = {};
        String[] moveHandles = {};
        String[] renameHandles = ParticipantTesting.createHandles(toRename.toArray());

        rename.execute();

        ParticipantTesting.testCreate(createHandles);
        ParticipantTesting.testDelete(deleteHandles);
        ParticipantTesting.testMove(moveHandles, new MoveArguments[]{

        });
        ParticipantTesting.testRename(renameHandles, new RenameArguments[]{
                new RenameArguments(rename.getNewPackageName(rename.fPackageNames[0]), true),
                new RenameArguments(rename.getNewPackageName(rename.fPackageNames[1]), true),
                new RenameArguments("your", true)
        });
    }

    @Test
    @Ignore
    public void testHierarchicalToSubpackage() throws Exception {
        fRenameSubpackages = true;

        PackageRename rename = new PackageRename(
                new String[]{"a", "a.b", "a.b.c", "a.b.c.d", "p"},
                new String[][]{{}, {"B"}, {"C"}, {"D"}},
                "a.b",
                true
        );
        IPackageFragment thisPackage = rename.fPackages[0];

        IFolder src = (IFolder)getRoot().getResource();
        IFolder ab = src.getFolder("a/b");
        IFolder abc = ab.getFolder("c");
        IFolder abcd = abc.getFolder("d");

        IFolder abb = ab.getFolder("b");
        IFolder abbc = abb.getFolder("c");
        IFolder abbcd = abbc.getFolder("d");

        ParticipantTesting.reset();

        String[] createHandles = ParticipantTesting.createHandles(abb, abbc, abbcd);
        String[] deleteHandles = {};
        String[] moveHandles = ParticipantTesting.createHandles(ab.getFile("B.java"), abc.getFile("C.java"), abcd.getFile("D.java"));
        String[] renameHandles = ParticipantTesting.createHandles(JavaElementUtil.getPackageAndSubpackages(thisPackage));

        rename.createAndPerform(RefactoringStatus.WARNING);
        rename.checkExpectedState();

        ParticipantTesting.testCreate(createHandles);
        ParticipantTesting.testDelete(deleteHandles);
        ParticipantTesting.testMove(moveHandles, new MoveArguments[]{
                new MoveArguments(abb, true),
                new MoveArguments(abbc, true),
                new MoveArguments(abbcd, true),
        });
        ParticipantTesting.testRename(renameHandles, new RenameArguments[]{
                new RenameArguments(rename.getNewPackageName(rename.fPackageNames[0]), true),
                new RenameArguments(rename.getNewPackageName(rename.fPackageNames[1]), true),
                new RenameArguments(rename.getNewPackageName(rename.fPackageNames[2]), true),
                new RenameArguments(rename.getNewPackageName(rename.fPackageNames[3]), true),
        });

        performUndo();
        rename.checkOriginalState();
    }

    @Test
    @Ignore
    public void testHierarchicalToSuperpackage() throws Exception {
        fRenameSubpackages = true;

        PackageRename rename = new PackageRename(
                new String[]{"a.b", "a.b.b", "a", "p"},
                new String[][]{{"B"}, {"BB"}, {}},
                "a",
                true
        );
        IPackageFragment thisPackage = rename.fPackages[0];
        IFolder src = (IFolder)getRoot().getResource();
        IFolder a = src.getFolder("a");
        IFolder ab = src.getFolder("a/b");
        IFolder abb = src.getFolder("a/b/b");

        ParticipantTesting.reset();

        String[] createHandles = {};
        String[] deleteHandles = {};
        String[] moveHandles = ParticipantTesting.createHandles(ab.getFile("B.java"), abb.getFile("BB.java"));
        String[] renameHandles = ParticipantTesting.createHandles(JavaElementUtil.getPackageAndSubpackages(thisPackage));

        rename.createAndPerform(RefactoringStatus.OK);
        rename.checkExpectedState();

        ParticipantTesting.testCreate(createHandles);
        ParticipantTesting.testDelete(deleteHandles);
        ParticipantTesting.testMove(moveHandles, new MoveArguments[]{
                new MoveArguments(a, true),
                new MoveArguments(ab, true),
        });
        ParticipantTesting.testRename(renameHandles, new RenameArguments[]{
                new RenameArguments("a", true),
                new RenameArguments("a.b", true),
        });

        performUndo();
        rename.checkOriginalState();
    }

    @Test
    @Ignore
    public void testHierarchicalToSuperpackage2() throws Exception {
        fRenameSubpackages = true;

        PackageRename rename = new PackageRename(
                new String[]{"a.b", "a.b.c", "a.c", "p"},
                new String[][]{{"B"}, {"BC"}, {}},
                "a",
                true
        );
        IPackageFragment thisPackage = rename.fPackages[0];
        IFolder src = (IFolder)getRoot().getResource();
        IFolder a = src.getFolder("a");
        IFolder ab = src.getFolder("a/b");
        IFolder ac = src.getFolder("a/c");
        IFolder abc = src.getFolder("a/b/c");

        ParticipantTesting.reset();

        String[] createHandles = {};
        String[] deleteHandles = ParticipantTesting.createHandles(ab);
        String[] moveHandles = ParticipantTesting.createHandles(ab.getFile("B.java"), abc.getFile("BC.java"));
        String[] renameHandles = ParticipantTesting.createHandles(JavaElementUtil.getPackageAndSubpackages(thisPackage));

        rename.createAndPerform(RefactoringStatus.OK);
        rename.checkExpectedState();

        ParticipantTesting.testCreate(createHandles);
        ParticipantTesting.testDelete(deleteHandles);
        ParticipantTesting.testMove(moveHandles, new MoveArguments[]{
                new MoveArguments(a, true),
                new MoveArguments(ac, true),
        });
        ParticipantTesting.testRename(renameHandles, new RenameArguments[]{
                new RenameArguments("a", true),
                new RenameArguments("a.c", true),
        });

        performUndo();
        rename.fPackageNames = new String[]{"a.b", "a.b.c", "a", "p"};// empty package is not recreated, but that's OK
        rename.checkOriginalState();
    }

    @Test
    public void testHierarchicalToSuperpackageFail() throws Exception {
        fRenameSubpackages = true;

        PackageRename rename = new PackageRename(
                new String[]{"a.b", "a.b.c", "a.c", "a", "p"},
                new String[][]{{"B"}, {"BC"}, {"AC"}},
                "a",
                true
        );

        rename.createAndPerform(RefactoringStatus.FATAL);
        rename.checkOriginalState();
    }

    @Test
    public void testHierarchicalDisabledImport() throws Exception {
        fRenameSubpackages = true;
        fUpdateTextualMatches = true;

        PackageRename rename = new PackageRename(new String[]{"my", "my.pack"}, new String[][]{{}, {"C"}}, "your");
        IPackageFragment thisPackage = rename.fPackages[0];

        ParticipantTesting.reset();

        List toRename = new ArrayList(Arrays.asList(JavaElementUtil.getPackageAndSubpackages(thisPackage)));
        toRename.add(thisPackage.getResource());
        String[] renameHandles = ParticipantTesting.createHandles(toRename.toArray());

        rename.execute();

        ParticipantTesting.testRename(renameHandles, new RenameArguments[]{
                new RenameArguments(rename.getNewPackageName(rename.fPackageNames[0]), true),
                new RenameArguments(rename.getNewPackageName(rename.fPackageNames[1]), true),
                new RenameArguments("your", true)
        });
    }

    @Test
    public void testFail0() throws Exception {
        helper1(new String[]{"r"}, new String[][]{{"A"}}, "9");
    }

//	public void testHierarchicalJUnit() throws Exception {
//		fRenameSubpackages= true;
//
//		File junitSrcArchive= JavaTestPlugin.getDefault().getFileInPlugin(JavaProjectHelper.JUNIT_SRC_381);
//		assertTrue(junitSrcArchive != null && junitSrcArchive.exists());
//		IPackageFragmentRoot src= JavaProjectHelper.addSourceContainerWithImport(getRoot().getJavaProject(), "src", junitSrcArchive,
// JavaProjectHelper.JUNIT_SRC_ENCODING);
//
//		String[] packageNames= new String[]{"junit", "junit.extensions", "junit.framework", "junit.runner", "junit.samples", "junit
// .samples.money", "junit.tests", "junit.tests.extensions", "junit.tests.framework", "junit.tests.runner", "junit.textui"};
//		ICompilationUnit[][] cus= new ICompilationUnit[packageNames.length][];
//		for (int i= 0; i < cus.length; i++) {
//			cus[i]= src.getPackageFragment(packageNames[i]).getCompilationUnits();
//		}
//		IPackageFragment thisPackage= src.getPackageFragment("junit");
//
//		ParticipantTesting.reset();
//		PackageRename rename= new PackageRename(packageNames, new String[packageNames.length][0],"jdiverge");
//
//		RenameArguments[] renameArguments= new RenameArguments[packageNames.length + 1];
//		for (int i= 0; i < packageNames.length; i++) {
//			renameArguments[i]= new RenameArguments(rename.getNewPackageName(packageNames[i]), true);
//		}
//		renameArguments[packageNames.length]= new RenameArguments("jdiverge", true);
//		String[] renameHandles= new String[packageNames.length + 1];
//		System.arraycopy(ParticipantTesting.createHandles(JavaElementUtil.getPackageAndSubpackages(thisPackage)), 0, renameHandles, 0,
// packageNames.length);
//		renameHandles[packageNames.length]= ParticipantTesting.createHandles(thisPackage.getResource())[0];
//
//		// --- execute:
//		RenameJavaElementDescriptor descriptor= createRefactoringDescriptor(thisPackage, "jdiverge");
//		descriptor.setUpdateReferences(fUpdateReferences);
//		descriptor.setUpdateTextualOccurrences(fUpdateTextualMatches);
//		setFilePatterns(descriptor);
//		descriptor.setUpdateHierarchy(fRenameSubpackages);
//		Refactoring ref= createRefactoring(descriptor);
//
//		performDummySearch();
//		IUndoManager undoManager= getUndoManager();
//		CreateChangeOperation create= new CreateChangeOperation(
//			new CheckConditionsOperation(ref, CheckConditionsOperation.ALL_CONDITIONS),
//			RefactoringStatus.FATAL);
//		PerformChangeOperation perform= new PerformChangeOperation(create);
//		perform.setUndoManager(undoManager, ref.getName());
//		ResourcesPlugin.getWorkspace().run(perform, new NullProgressMonitor());
//		RefactoringStatus status= create.getConditionCheckingStatus();
//		assertTrue("Change wasn't executed", perform.changeExecuted());
//		Change undo= perform.getUndoChange();
//		assertNotNull("Undo doesn't exist", undo);
//		assertTrue("Undo manager is empty", undoManager.anythingToUndo());
//
//		assertFalse(status.hasError());
//		assertTrue(status.hasWarning());
//		RefactoringStatusEntry[] statusEntries= status.getEntries();
//		for (int i= 0; i < statusEntries.length; i++) {
//			RefactoringStatusEntry entry= statusEntries[i];
//			assertTrue(entry.isWarning());
//			assertTrue(entry.getCode() == RefactoringStatusCodes.MAIN_METHOD);
//		}
//
//		assertTrue("package not renamed: " + rename.fPackageNames[0], ! src.getPackageFragment(rename.fPackageNames[0]).exists());
//		IPackageFragment newPackage= src.getPackageFragment(rename.fNewPackageName);
//		assertTrue("new package does not exist", newPackage.exists());
//		// ---
//
//		ParticipantTesting.testRename(renameHandles, renameArguments);
//
//		PerformChangeOperation performUndo= new PerformChangeOperation(undo);
//		ResourcesPlugin.getWorkspace().run(performUndo, new NullProgressMonitor());
//
//		assertTrue("new package still exists", ! newPackage.exists());
//		assertTrue("original package does not exist: " + rename.fPackageNames[0], src.getPackageFragment(rename.fPackageNames[0]).exists
// ());
//
//		ZipInputStream zis= new ZipInputStream(new BufferedInputStream(new FileInputStream(junitSrcArchive)));
//		ZipTools.compareWithZipped(src, zis, JavaProjectHelper.JUNIT_SRC_ENCODING);
//	}

    public void testFail1() throws Exception {
        printTestDisabledMessage("needs revisiting");
        //helper1(new String[]{"r.p1"}, new String[][]{{"A"}}, "r");
    }

    @Test
    public void testFail3() throws Exception {
        helper1(new String[]{"r"}, new String[][]{{"A"}}, "fred");
    }

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

    public void testFail7() throws Exception {
        //printTestDisabledMessage("1GK90H4: ITPJCORE:WIN2000 - search: missing package reference");
        printTestDisabledMessage("corner case - name obscuring");
//		helper1(new String[]{"r", "p1"}, new String[][]{{"A"}, {"A"}}, "fred");
    }

    public void testFail8() throws Exception {
        printTestDisabledMessage("corner case - name obscuring");
//		helper1(new String[]{"r", "p1"}, new String[][]{{"A"}, {"A"}}, "fred");
    }

    //native method used r.A as a parameter
    public void testFail9() throws Exception {
        printTestDisabledMessage("corner case - qualified name used  as a parameter of a native method");
        //helper1(new String[]{"r", "p1"}, new String[][]{{"A"}, {"A"}}, "fred");
    }

    public void testFail10() throws Exception {
        helper1(new String[]{"r.p1", "r"}, new String[][]{{"A"}, {"A"}}, "r");
    }

    @Test
    public void test0() throws Exception {
        if (BUG_54962_71267) {
            printTestDisabledMessage("bugs 54962, 71267");
            return;
        }
        fIsPreDeltaTest = true;
    }

    //-------

    @Test
    public void test1() throws Exception {
        fIsPreDeltaTest = true;
        RenamePackageProcessor proc = helper2(new String[]{"r"}, new String[][]{{"A"}}, "p1");
        IJavaElementMapper jm = (IJavaElementMapper)proc.getAdapter(IJavaElementMapper.class);
        IResourceMapper rm = (IResourceMapper)proc.getAdapter(IResourceMapper.class);

        IJavaModel javaModel = JavaCore.create(ResourcesPlugin.getWorkspace().getRoot());
        IJavaProject project = getRoot().getJavaProject();
        IFile _project = project.getProject().getFile(".project");
        checkMappingUnchanged(jm, rm, new Object[]{javaModel, project, _project, getRoot(), getPackageP(),
                                                   getRoot().getPackageFragment("inexistent"), getRoot().getPackageFragment("r.inexistent"),
                                                   getRoot().getPackageFragment("p1.inexistent")});

        IPackageFragment r = getRoot().getPackageFragment("r");
        ICompilationUnit r_A = r.getCompilationUnit("A.java");
        IType r_A_A = r_A.getType("A");
        IField r_A_A_a = r_A_A.getField("a");
        IPackageFragment p1 = getRoot().getPackageFragment("p1");
        ICompilationUnit p1_A = p1.getCompilationUnit("A.java");
        IType p1_A_A = p1_A.getType("A");
        IField p1_A_A_a = p1_A_A.getField("a");
        checkMappingChanged(jm, rm, new Object[][]{
                {r, p1},
                {r_A, p1_A},
                {r_A_A, p1_A_A},
                {r_A_A_a, p1_A_A_a},
        });
    }

    @Test
    public void test2() throws Exception {
        fIsPreDeltaTest = true;
        RenamePackageProcessor processor = helper2(new String[]{"r", "fred"}, new String[][]{{"A"}, {"A"}}, "p1");

        // test that participants are correctly informed after '< Back': https://bugs.eclipse.org/bugs/show_bug.cgi?id=280068
        performUndo();

        ParticipantTesting.reset();
        String secondName = "pipapo";
        processor.setNewElementName(secondName);
        String[] renameHandles = ParticipantTesting.createHandles(new Object[]{
                processor.getPackage(),
                processor.getPackage().getResource()
        });

        RenameRefactoring refactoring = (RenameRefactoring)processor.getRefactoring();
        refactoring.checkFinalConditions(new NullProgressMonitor());
        refactoring.createChange(new NullProgressMonitor());

        ParticipantTesting.testRename(renameHandles, new RenameArguments[]{
                new RenameArguments(secondName, true),
                new RenameArguments(secondName, true)
        });
    }

    @Test
    @Ignore
    public void test3() throws Exception {
        fIsPreDeltaTest = true;
        helper2(new String[]{"fred", "r.r"}, new String[][]{{"A"}, {"B"}}, "r");
    }

    @Test
    public void test4() throws Exception {
        fIsPreDeltaTest = true;

        fQualifiedNamesFilePatterns = "*.txt";
        String textFileName = "Textfile.txt";

        String textfileContent = getFileContents(getTestPath() + name.getMethodName() + TEST_INPUT_INFIX + textFileName);
        IFile textfile = getRoot().getJavaProject().getProject().getFile(textFileName);
        textfile.create(new ByteArrayInputStream(textfileContent.getBytes()), true, null);

        helper2(new String[]{"r.p1", "r"}, new String[][]{{"A"}, {"A"}}, "q");

        InputStreamReader reader = new InputStreamReader(textfile.getContents(true));
        StringBuffer newContent = new StringBuffer();
        try {
            int ch;
            while ((ch = reader.read()) != -1)
                newContent.append((char)ch);
        } finally {
            reader.close();
        }
        String definedContent = getFileContents(getTestPath() + name.getMethodName() + TEST_OUTPUT_INFIX + textFileName);
        assertEqualLines("invalid updating", definedContent, newContent.toString());
    }

    @Test
    public void test5() throws Exception {
        fUpdateReferences = false;
        fIsPreDeltaTest = true;
        helper2(new String[]{"r"}, new String[][]{{"A"}}, "p1");
    }

    @Test
    public void test6() throws Exception { //bug 66250
        fUpdateReferences = false;
        fUpdateTextualMatches = true;
        fIsPreDeltaTest = true;
        helper2(new String[]{"r"}, new String[][]{{"A"}}, "p1");
    }

    @Test
    public void test7() throws Exception {
        helper2(new String[]{"r", "r.s"}, new String[][]{{"A"}, {"B"}}, "q");
    }

    @Test
    @Ignore
    public void test8() throws Exception {
        helper2(new String[]{"java.lang.reflect"}, new String[][]{{"Klass"}}, "nonjava");
    }

    @Test
    @Ignore
    public void testToEmptyPack() throws Exception {
        helper2(new String[]{"r.p1", "fred"}, new String[][]{{"A"}, {}}, "fred");
    }

    @Test
    public void testToEmptySubPack() throws Exception {
        fRenameSubpackages = true;

        PackageRename rename = new PackageRename(new String[]{"p", "p.q"}, new String[][]{{}, {}}, "p.q");
        IPackageFragment p = rename.fPackages[0];
        IPackageFragment pq = rename.fPackages[1];

        ParticipantTesting.reset();
        String[] renameHandles = ParticipantTesting.createHandles(p, pq);

        rename.createAndPerform(RefactoringStatus.OK);
        assertTrue(p.exists());
        assertTrue(pq.exists());
        IPackageFragment ppq = getRoot().getPackageFragment("p.q.q");
        assertTrue(ppq.exists());

        ParticipantTesting.testRename(renameHandles, new RenameArguments[]{
                new RenameArguments(rename.getNewPackageName(rename.fPackageNames[0]), true),
                new RenameArguments(rename.getNewPackageName(rename.fPackageNames[1]), true),
        });
        ParticipantTesting.testCreate(ParticipantTesting.createHandles(
                ppq.getResource()
                                                                      ));
    }

    @Test
    public void testWithEmptySubPack() throws Exception {
        fRenameSubpackages = true;

        PackageRename rename = new PackageRename(new String[]{"p", "p.q"}, new String[][]{{}, {}}, "p1");
        IPackageFragment p = rename.fPackages[0];
        IPackageFragment pq = rename.fPackages[1];

        ParticipantTesting.reset();
        String[] renameHandles = ParticipantTesting.createHandles(p, pq, p.getResource());

        rename.createAndPerform(RefactoringStatus.OK);
        assertFalse(p.exists());
        assertFalse(pq.exists());
        IPackageFragment p1 = getRoot().getPackageFragment("p1");
        IPackageFragment p1q = getRoot().getPackageFragment("p1.q");
        assertTrue(p1.exists());
        assertTrue(p1q.exists());

        ParticipantTesting.testRename(renameHandles, new RenameArguments[]{
                new RenameArguments(rename.getNewPackageName(rename.fPackageNames[0]), true),
                new RenameArguments(rename.getNewPackageName(rename.fPackageNames[1]), true),
                new RenameArguments("p1", true)
        });
    }

    @Test
    public void testReadOnly() throws Exception {
        if (BUG_6054) {
            printTestDisabledMessage("see bug#6054 (renaming a read-only package resets the read-only flag)");
            return;
        }

        fIsPreDeltaTest = true;
        String[] packageNames = new String[]{"r"};
        String[][] packageFileNames = new String[][]{{"A"}};
        String newPackageName = "p1";
        IPackageFragment[] packages = new IPackageFragment[packageNames.length];

        ICompilationUnit[][] cus = new ICompilationUnit[packageFileNames.length][packageFileNames[0].length];
        for (int i = 0; i < packageNames.length; i++) {
            packages[i] = getRoot().createPackageFragment(packageNames[i], true, null);
            for (int j = 0; j < packageFileNames[i].length; j++) {
                cus[i][j] = createCUfromTestFile(packages[i], packageFileNames[i][j], packageNames[i].replace('.', '/') + "/");
            }
        }
        IPackageFragment thisPackage = packages[0];
        final IResource resource = thisPackage.getCorrespondingResource();
        final ResourceAttributes attributes = resource.getResourceAttributes();
        if (attributes != null)
            attributes.setReadOnly(true);
        RefactoringStatus result = performRefactoring(createRefactoringDescriptor(thisPackage, newPackageName));
        assertEquals("preconditions were supposed to pass", null, result);

        assertTrue("package not renamed", !getRoot().getPackageFragment(packageNames[0]).exists());
        IPackageFragment newPackage = getRoot().getPackageFragment(newPackageName);
        assertTrue("new package does not exist", newPackage.exists());
        assertTrue("new package should be read-only", attributes == null || attributes.isReadOnly());
    }

    @Test
    @Ignore
    public void testImportFromMultiRoots1() throws Exception {
        fUpdateTextualMatches = true;
        helperProjectsPrgTest(
                new String[][]{
                        new String[]{"p.p"}, new String[]{"p.p", "tests"}
                },
                "q",
                new String[][][]{
                        new String[][]{new String[]{"A"}},
                        new String[][]{new String[]{"ATest"}, new String[]{"AllTests"}}
                });
    }

    @Test
    @Ignore
    public void testImportFromMultiRoots2() throws Exception {
        helperProjectsPrgTest(
                new String[][]{
                        new String[]{"p.p"},
                        new String[]{"p.p", "tests"}
                },
                "q",
                new String[][][]{
                        new String[][]{new String[]{"A"}},
                        new String[][]{new String[]{"ATest", "TestHelper"}, new String[]{"AllTests", "QualifiedTests"}}
                }
                             );
    }

    @Test
    @Ignore
    public void testImportFromMultiRoots3() throws Exception {
        helperMultiRoots(new String[]{"srcPrg", "srcTest"},
                         new String[][]{
                                 new String[]{"p.p"},
                                 new String[]{"p.p"}
                         },
                         "q",
                         new String[][][]{
                                 new String[][]{new String[]{"ToQ"}},
                                 new String[][]{new String[]{"Ref"}}
                         }
                        );
    }

    @Test
    @Ignore
    public void testImportFromMultiRoots4() throws Exception {
        //circular buildpath references
        IJavaProject projectPrg = null;
        IJavaProject projectTest = null;
        Hashtable options = JavaCore.getOptions();
        Object cyclicPref = JavaCore.getOption(JavaCore.CORE_CIRCULAR_CLASSPATH);
        try {
            projectPrg = JavaProjectHelper.createJavaProject("RenamePack1", "bin");
            assertNotNull(JavaProjectHelper.addRTJar(projectPrg));
            IPackageFragmentRoot srcPrg = JavaProjectHelper.addSourceContainer(projectPrg, "srcPrg");

            projectTest = JavaProjectHelper.createJavaProject("RenamePack2", "bin");
            assertNotNull(JavaProjectHelper.addRTJar(projectTest));
            IPackageFragmentRoot srcTest = JavaProjectHelper.addSourceContainer(projectTest, "srcTest");

            options.put(JavaCore.CORE_CIRCULAR_CLASSPATH, JavaCore.WARNING);
            JavaCore.setOptions(options);
            JavaProjectHelper.addRequiredProject(projectTest, projectPrg);
            JavaProjectHelper.addRequiredProject(projectPrg, projectTest);

            helperMultiProjects(new IPackageFragmentRoot[]{srcPrg, srcTest},
                                new String[][]{
                                        new String[]{"p"},
                                        new String[]{"p"}
                                },
                                "a.b.c",
                                new String[][][]{
                                        new String[][]{new String[]{"A", "B"}},
                                        new String[][]{new String[]{"ATest"}}
                                }
                               );
        } finally {
            options.put(JavaCore.CORE_CIRCULAR_CLASSPATH, cyclicPref);
            JavaCore.setOptions(options);
            JavaProjectHelper.delete(projectPrg);
            JavaProjectHelper.delete(projectTest);
        }
    }

    @Test
    @Ignore
    public void testImportFromMultiRoots5() throws Exception {
        //rename srcTest-p.p to q => ATest now must import p.p.A
        IJavaProject projectPrg = null;
        IJavaProject projectTest = null;
        try {
            projectPrg = JavaProjectHelper.createJavaProject("RenamePack1", "bin");
            assertNotNull(JavaProjectHelper.addRTJar(projectPrg));
            IPackageFragmentRoot srcPrg = JavaProjectHelper.addSourceContainer(projectPrg, "srcPrg");

            projectTest = JavaProjectHelper.createJavaProject("RenamePack2", "bin");
            assertNotNull(JavaProjectHelper.addRTJar(projectTest));
            IPackageFragmentRoot srcTest = JavaProjectHelper.addSourceContainer(projectTest, "srcTest");

            JavaProjectHelper.addRequiredProject(projectTest, projectPrg);

            helperMultiProjects(new IPackageFragmentRoot[]{srcTest, srcPrg},
                                new String[][]{
                                        new String[]{"p.p"}, new String[]{"p.p"}
                                },
                                "q",
                                new String[][][]{
                                        new String[][]{new String[]{"ATest"}},
                                        new String[][]{new String[]{"A"}}
                                }
                               );
        } finally {
            JavaProjectHelper.delete(projectPrg);
            JavaProjectHelper.delete(projectTest);
        }

    }

    @Test
    @Ignore
    public void testImportFromMultiRoots6() throws Exception {
        //rename srcTest-p.p to a.b.c => ATest must retain import p.p.A
        helperMultiRoots(new String[]{"srcTest", "srcPrg"},
                         new String[][]{
                                 new String[]{"p.p"},
                                 new String[]{"p.p"}
                         },
                         "cheese",
                         new String[][][]{
                                 new String[][]{new String[]{"ATest"}},
                                 new String[][]{new String[]{"A"}}
                         }
                        );
    }

    @Test
    @Ignore
    public void testImportFromMultiRoots7() throws Exception {
        IJavaProject prj = null;
        IJavaProject prjRef = null;
        IJavaProject prjOther = null;
        try {
            prj = JavaProjectHelper.createJavaProject("prj", "bin");
            assertNotNull(JavaProjectHelper.addRTJar(prj));
            IPackageFragmentRoot srcPrj = JavaProjectHelper.addSourceContainer(prj, "srcPrj"); //$NON-NLS-1$

            prjRef = JavaProjectHelper.createJavaProject("prj.ref", "bin");
            assertNotNull(JavaProjectHelper.addRTJar(prjRef));
            IPackageFragmentRoot srcPrjRef = JavaProjectHelper.addSourceContainer(prjRef, "srcPrj.ref"); //$NON-NLS-1$

            prjOther = JavaProjectHelper.createJavaProject("prj.other", "bin");
            assertNotNull(JavaProjectHelper.addRTJar(prjOther));
            IPackageFragmentRoot srcPrjOther = JavaProjectHelper.addSourceContainer(prjRef, "srcPrj.other"); //$NON-NLS-1$

            JavaProjectHelper.addRequiredProject(prjRef, prj);
            JavaProjectHelper.addRequiredProject(prjRef, prjOther);

            helperMultiProjects(
                    new IPackageFragmentRoot[]{srcPrj, srcPrjRef, srcPrjOther},
                    new String[][]{
                            new String[]{"pack"},
                            new String[]{"pack", "pack.man"},
                            new String[]{"pack"}
                    },
                    "com.packt",
                    new String[][][]{
                            new String[][]{new String[]{"DingsDa"}},
                            new String[][]{new String[]{"Referer"}, new String[]{"StarImporter"}},
                            new String[][]{new String[]{"Namesake"}}
                    }
                               );
        } finally {
            JavaProjectHelper.delete(prj);
            JavaProjectHelper.delete(prjRef);
            JavaProjectHelper.delete(prjOther);
        }
    }

    @Test
    public void testStatic1() throws Exception {
        helper2(new String[]{"s1.j.l", "s1"}, new String[][]{{"S"}, {"B"}}, "s1.java.lang");
    }

    @Test
    @Ignore
    public void testStaticMultiRoots1() throws Exception {
        helperProjectsPrgTest(
                new String[][]{
                        new String[]{"p.p"}, new String[]{"p.p", "tests"}
                },
                "q",
                new String[][][]{
                        new String[][]{new String[]{"A"}},
                        new String[][]{new String[]{"ATest"}, new String[]{"AllTests"}}
                });
    }

    class PackageRename {
        final String[][] fPackageFileNames;
        final String     fNewPackageName;
        final boolean    fTestWithDummyFiles;
        final IPackageFragment[]   fPackages;
        final ICompilationUnit[][] fCus;
        String[] fPackageNames;

        public PackageRename(String[] packageNames, String[][] packageFileNames, String newPackageName) throws Exception {
            this(packageNames, packageFileNames, newPackageName, false);
        }

        public PackageRename(String[] packageNames, String[][] packageFileNames, String newPackageName, boolean testWithDummyFiles)
                throws Exception {
            fPackageNames = packageNames;
            fPackageFileNames = packageFileNames;
            fNewPackageName = newPackageName;
            fTestWithDummyFiles = testWithDummyFiles;

            fPackages = new IPackageFragment[packageNames.length];
            fCus = new ICompilationUnit[packageFileNames.length][];
            for (int i = 0; i < packageFileNames.length; i++) {
                fPackages[i] = getRoot().createPackageFragment(packageNames[i], true, null);
                fCus[i] = new ICompilationUnit[packageFileNames[i].length];
                for (int j = 0; j < packageFileNames[i].length; j++) {
                    if (testWithDummyFiles) {
                        fCus[i][j] = createDummyCU(fPackages[i], packageFileNames[i][j]);
                    } else {
                        fCus[i][j] = createCUfromTestFile(fPackages[i], packageFileNames[i][j], packageNames[i].replace('.', '/') + "/");
                    }
                }
            }
        }

        private ICompilationUnit createDummyCU(IPackageFragment packageFragment, String typeName) throws JavaModelException {
            String contents = getDummyContents(packageFragment.getElementName(), typeName);
            return packageFragment.createCompilationUnit(typeName + ".java", contents, true, null);
        }

        private String getDummyContents(String packName, String typeName) {
            StringBuffer contents = new StringBuffer();
            if (packName.length() != 0)
                contents.append("package ").append(packName).append(";\n");
            contents.append("public class ").append(typeName).append(" { }\n");
            return contents.toString();
        }

        public void createAndPerform(int expectedSeverity) throws CoreException, Exception {
            IPackageFragment thisPackage = fPackages[0];
            RenameJavaElementDescriptor descriptor = createRefactoringDescriptor(thisPackage, fNewPackageName);
            descriptor.setUpdateReferences(fUpdateReferences);
            descriptor.setUpdateTextualOccurrences(fUpdateTextualMatches);
            setFilePatterns(descriptor);
            descriptor.setUpdateHierarchy(fRenameSubpackages);
            RefactoringStatus result = performRefactoring(descriptor);
            if (expectedSeverity == RefactoringStatus.OK)
                assertEquals("preconditions were supposed to pass", null, result);
            else
                assertEquals(expectedSeverity, result.getSeverity());
        }

        public void execute() throws Exception {
            createAndPerform(RefactoringStatus.OK);

            IPackageFragment oldPackage = getRoot().getPackageFragment(fPackageNames[0]);
            assertTrue("package not renamed: " + fPackageNames[0], !oldPackage.exists());
            IPackageFragment newPackage = getRoot().getPackageFragment(fNewPackageName);
            assertTrue("new package does not exist", newPackage.exists());

            checkExpectedState();
        }

        public void checkExpectedState() throws IOException, JavaModelException {
            for (int i = 0; i < fPackageFileNames.length; i++) {
                String packageName = getNewPackageName(fPackageNames[i]);
                String packagePath = packageName.replace('.', '/') + "/";

                for (int j = 0; j < fPackageFileNames[i].length; j++) {
                    String expected;
                    if (fTestWithDummyFiles) {
                        expected = getDummyContents(packageName, fPackageFileNames[i][j]);
                    } else {
                        expected = getFileContents(getOutputTestFileName(fPackageFileNames[i][j], packagePath));
                    }
                    ICompilationUnit cu = getRoot().getPackageFragment(packageName).getCompilationUnit(fPackageFileNames[i][j] + ".java");
                    String actual = cu.getSource();
                    assertEqualLines("invalid update in file " + cu.getElementName(), expected, actual);
                }
            }
        }

        public String getNewPackageName(String oldPackageName) {
            if (oldPackageName.equals(fPackageNames[0]))
                return fNewPackageName;

            if (fRenameSubpackages && oldPackageName.startsWith(fPackageNames[0] + "."))
                return fNewPackageName + oldPackageName.substring(fPackageNames[0].length());

            return oldPackageName;
        }

        public void checkOriginalState() throws Exception {
            IJavaElement[] rootChildren = getRoot().getChildren();
            ArrayList existingPacks = new ArrayList();
            for (int i = 0; i < rootChildren.length; i++) {
                existingPacks.add(rootChildren[i].getElementName());
            }
            assertEqualSets(Arrays.asList(fPackageNames), existingPacks);

            for (int i = 0; i < fPackageFileNames.length; i++) {
                String packageName = fPackageNames[i];
                String packagePath = packageName.replace('.', '/') + "/";
                IPackageFragment pack = getRoot().getPackageFragment(packageName);

                IJavaElement[] packChildren = pack.getChildren();
                ArrayList existingCUs = new ArrayList();
                for (int j = 0; j < packChildren.length; j++) {
                    String cuName = packChildren[j].getElementName();
                    existingCUs.add(cuName.substring(0, cuName.length() - 5));
                }
                assertEqualSets(Arrays.asList(fPackageFileNames[i]), existingCUs);

                for (int j = 0; j < fPackageFileNames[i].length; j++) {
                    String expected;
                    if (fTestWithDummyFiles) {
                        expected = getDummyContents(packageName, fPackageFileNames[i][j]);
                    } else {
                        expected = getFileContents(getInputTestFileName(fPackageFileNames[i][j], packagePath));
                    }
                    ICompilationUnit cu = pack.getCompilationUnit(fPackageFileNames[i][j] + ".java");
                    String actual = cu.getSource();
                    assertEqualLines("invalid undo in file " + cu.getElementName(), expected, actual);
                }
            }

        }

        private void assertEqualSets(Collection expected, Collection actual) {
            HashSet expectedSet = new HashSet(expected);
            expectedSet.removeAll(actual);
            assertEquals("not all expected in actual", "[]", expectedSet.toString());

            HashSet actualSet = new HashSet(actual);
            actualSet.removeAll(expected);
            assertEquals("not all actual in expected", "[]", actualSet.toString());
        }
    }
}
