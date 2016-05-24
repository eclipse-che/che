/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Benjamin Muskalla <bmuskalla@eclipsesource.com> - [quick fix] proposes wrong cast from Object to primitive int - https://bugs.eclipse.org/bugs/show_bug.cgi?id=100593
 *     Benjamin Muskalla <bmuskalla@eclipsesource.com> - [quick fix] "Add exceptions to..." quickfix does nothing - https://bugs.eclipse.org/bugs/show_bug.cgi?id=107924
 *******************************************************************************/
package org.eclipse.che.plugin.java.server.jdt.quickfix;


import org.eclipse.che.jdt.util.JavaModelUtil;
import org.eclipse.che.jface.text.contentassist.ICompletionProposal;
import org.eclipse.che.plugin.java.server.jdt.testplugin.JavaProjectHelper;
import org.eclipse.che.plugin.java.server.jdt.testplugin.ProjectTestSetup;
import org.eclipse.che.plugin.java.server.jdt.testplugin.TestOptions;
import org.eclipse.che.plugin.java.server.test.Accessor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jdt.internal.corext.codemanipulation.StubUtility;
import org.eclipse.jdt.internal.corext.template.java.CodeTemplateContextType;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.text.correction.proposals.TypeChangeCorrectionProposal;
import org.eclipse.jdt.internal.ui.viewsupport.BindingLabelProvider;
import org.eclipse.jdt.ui.JavaElementLabels;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jdt.ui.text.java.correction.ASTRewriteCorrectionProposal;
import org.eclipse.jdt.ui.text.java.correction.CUCorrectionProposal;
import org.eclipse.jface.preference.IPreferenceStore;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

public class TypeMismatchQuickFixTests extends QuickFixTest {

	private static final Class THIS= TypeMismatchQuickFixTests.class;

	private IJavaProject         fJProject1;
	private IPackageFragmentRoot fSourceFolder;


	public TypeMismatchQuickFixTests() {
		super(new ProjectTestSetup());
	}

//	public static Test suite() {
//		return setUpTest(new TestSuite(THIS));
//	}
//
//	public static Test setUpTest(Test test) {
//		return new ProjectTestSetup(test);
//	}

	@Before
	public void setUp() throws Exception {
        super.setUp();
		Hashtable options = TestOptions.getDefaultOptions();
		options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, JavaCore.SPACE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, "4");
		options.put(DefaultCodeFormatterConstants.FORMATTER_NUMBER_OF_EMPTY_LINES_TO_PRESERVE, String.valueOf(99));
		options.put(JavaCore.COMPILER_PB_STATIC_ACCESS_RECEIVER, JavaCore.ERROR);
		options.put(JavaCore.COMPILER_PB_UNCHECKED_TYPE_OPERATION, JavaCore.IGNORE);

		JavaCore.setOptions(options);

		IPreferenceStore store = JavaPlugin.getDefault().getPreferenceStore();
		store.setValue(PreferenceConstants.CODEGEN_ADD_COMMENTS, false);

		StubUtility.setCodeTemplate(CodeTemplateContextType.CATCHBLOCK_ID, "", null);
		StubUtility.setCodeTemplate(CodeTemplateContextType.CONSTRUCTORSTUB_ID, "", null);
		StubUtility.setCodeTemplate(CodeTemplateContextType.METHODSTUB_ID, "", null);

		fJProject1 = ProjectTestSetup.getProject();

		fSourceFolder = JavaProjectHelper.addSourceContainer(fJProject1, "src");
	}


    @After
	public void tearDown() throws Exception {
        super.tearDown();
		JavaProjectHelper.clear(fJProject1, ProjectTestSetup.getDefaultClasspath());
	}


    @Test
	public void testTypeMismatchInVarDecl() throws Exception {
		IPackageFragment pack1 = fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(Object o) {\n");
		buf.append("        Thread th= o;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu = pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot = getASTRoot(cu);
		ArrayList proposals = collectCorrections(cu, astRoot);
		assertNumberOfProposals(proposals, 3);
		assertCorrectLabels(proposals);

		CUCorrectionProposal proposal = (CUCorrectionProposal)proposals.get(0);
		String preview1 = getPreviewContent(proposal);

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(Object o) {\n");
		buf.append("        Thread th= (Thread) o;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1 = buf.toString();

		proposal = (CUCorrectionProposal)proposals.get(1);
		String preview2 = getPreviewContent(proposal);

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(Object o) {\n");
		buf.append("        Object th= o;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected2= buf.toString();

		proposal= (CUCorrectionProposal) proposals.get(2);
		String preview3= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(Thread o) {\n");
		buf.append("        Thread th= o;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected3= buf.toString();

		assertEqualStringsIgnoreOrder(new String[] { preview1, preview2, preview3 }, new String[] { expected1, expected2, expected3 });
	}

    @Test
	public void testTypeMismatchInVarDecl2() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.List;\n");
		buf.append("public class Container {\n");
		buf.append("    public List[] getLists() {\n");
		buf.append("        return null;\n");
		buf.append("    }\n");
		buf.append("}\n");
		pack1.createCompilationUnit("Container.java", buf.toString(), false, null);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.ArrayList;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(Container c) {\n");
		buf.append("         ArrayList[] lists= c.getLists();\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= getASTRoot(cu);
		ArrayList proposals= collectCorrections(cu, astRoot);
		assertNumberOfProposals(proposals, 3);
		assertCorrectLabels(proposals);

		CUCorrectionProposal proposal= (CUCorrectionProposal) proposals.get(0);
		String preview1= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.ArrayList;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(Container c) {\n");
		buf.append("         ArrayList[] lists= (ArrayList[]) c.getLists();\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		proposal= (CUCorrectionProposal) proposals.get(1);
		String preview2= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.ArrayList;\n");
		buf.append("import java.util.List;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(Container c) {\n");
		buf.append("         List[] lists= c.getLists();\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected2= buf.toString();

		proposal= (CUCorrectionProposal) proposals.get(2);
		String preview3= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.ArrayList;\n");
		buf.append("import java.util.List;\n");
		buf.append("public class Container {\n");
		buf.append("    public ArrayList[] getLists() {\n");
		buf.append("        return null;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected3= buf.toString();

		assertEqualStringsIgnoreOrder(new String[] { preview1, preview2, preview3 }, new String[] { expected1, expected2, expected3 });

	}

    @Test
	public void testTypeMismatchInVarDecl3() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        Thread th= foo();\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= getASTRoot(cu);
		ArrayList proposals= collectCorrections(cu, astRoot);
		assertNumberOfProposals(proposals, 1);

		CUCorrectionProposal proposal= (CUCorrectionProposal) proposals.get(0);
		String preview1= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public Thread foo() {\n");
		buf.append("        Thread th= foo();\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertEqualStringsIgnoreOrder(new String[] { preview1 }, new String[] { expected1 });
	}

    @Test
	public void testTypeMismatchInVarDecl4() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.List;\n");
		buf.append("public class Container {\n");
		buf.append("    public List getLists()[] {\n");
		buf.append("        return null;\n");
		buf.append("    }\n");
		buf.append("}\n");
		pack1.createCompilationUnit("Container.java", buf.toString(), false, null);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.ArrayList;\n");
		buf.append("public class E extends Container {\n");
		buf.append("    public void foo() {\n");
		buf.append("         ArrayList[] lists= super.getLists();\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= getASTRoot(cu);
		ArrayList proposals= collectCorrections(cu, astRoot);
		assertNumberOfProposals(proposals, 3);
		assertCorrectLabels(proposals);

		CUCorrectionProposal proposal= (CUCorrectionProposal) proposals.get(0);
		String preview1= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.ArrayList;\n");
		buf.append("public class E extends Container {\n");
		buf.append("    public void foo() {\n");
		buf.append("         ArrayList[] lists= (ArrayList[]) super.getLists();\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		proposal= (CUCorrectionProposal) proposals.get(1);
		String preview2= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.ArrayList;\n");
		buf.append("import java.util.List;\n");
		buf.append("public class E extends Container {\n");
		buf.append("    public void foo() {\n");
		buf.append("         List[] lists= super.getLists();\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected2= buf.toString();

		proposal= (CUCorrectionProposal) proposals.get(2);
		String preview3= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.ArrayList;\n");
		buf.append("import java.util.List;\n");
		buf.append("public class Container {\n");
		buf.append("    public ArrayList[] getLists() {\n");
		buf.append("        return null;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected3= buf.toString();

		assertEqualStringsIgnoreOrder(new String[] { preview1, preview2, preview3 }, new String[] { expected1, expected2, expected3 });

	}

    @Test
	public void testTypeMismatchForInterface1() throws Exception {

		IPackageFragment pack0= fSourceFolder.createPackageFragment("test0", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test0;\n");
		buf.append("public interface PrimaryContainer {\n");
		buf.append("}\n");
		pack0.createCompilationUnit("PrimaryContainer.java", buf.toString(), false, null);

		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class Container {\n");
		buf.append("    public static Container getContainer() {\n");
		buf.append("        return null;\n");
		buf.append("    }\n");
		buf.append("}\n");
		pack1.createCompilationUnit("Container.java", buf.toString(), false, null);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import test0.PrimaryContainer;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("         PrimaryContainer list= Container.getContainer();\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= getASTRoot(cu);
		ArrayList proposals= collectCorrections(cu, astRoot);
		assertNumberOfProposals(proposals, 4);
		assertCorrectLabels(proposals);

		CUCorrectionProposal proposal= (CUCorrectionProposal) proposals.get(0);
		String preview1= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import test0.PrimaryContainer;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("         Container list= Container.getContainer();\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		proposal= (CUCorrectionProposal) proposals.get(1);
		String preview2= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import test0.PrimaryContainer;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("         PrimaryContainer list= (PrimaryContainer) Container.getContainer();\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected2= buf.toString();

		proposal= (CUCorrectionProposal) proposals.get(2);
		String preview3= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("\n");
		buf.append("import test0.PrimaryContainer;\n");
		buf.append("\n");
		buf.append("public class Container {\n");
		buf.append("    public static PrimaryContainer getContainer() {\n");
		buf.append("        return null;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected3= buf.toString();

		proposal= (CUCorrectionProposal) proposals.get(3);
		String preview4= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("\n");
		buf.append("import test0.PrimaryContainer;\n");
		buf.append("\n");
		buf.append("public class Container implements PrimaryContainer {\n");
		buf.append("    public static Container getContainer() {\n");
		buf.append("        return null;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected4= buf.toString();

		assertEqualStringsIgnoreOrder(new String[] { preview1, preview2, preview3, preview4 }, new String[] { expected1, expected2, expected3, expected4 });

	}

    @Test
	public void testTypeMismatchForInterface2() throws Exception {
		IPackageFragment pack0= fSourceFolder.createPackageFragment("test0", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test0;\n");
		buf.append("public interface PrimaryContainer {\n");
		buf.append("    PrimaryContainer duplicate(PrimaryContainer container);\n");
		buf.append("}\n");
		pack0.createCompilationUnit("PrimaryContainer.java", buf.toString(), false, null);


		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class Container {\n");
		buf.append("    public static Container getContainer() {\n");
		buf.append("        return null;\n");
		buf.append("    }\n");
		buf.append("}\n");
		pack1.createCompilationUnit("Container.java", buf.toString(), false, null);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import test0.PrimaryContainer;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(PrimaryContainer primary) {\n");
		buf.append("         primary.duplicate(Container.getContainer());\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= getASTRoot(cu);
		ArrayList proposals= collectCorrections(cu, astRoot);
		assertNumberOfProposals(proposals, 5);
		assertCorrectLabels(proposals);

		CUCorrectionProposal proposal= (CUCorrectionProposal) proposals.get(0);
		String preview1= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import test0.PrimaryContainer;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(PrimaryContainer primary) {\n");
		buf.append("         primary.duplicate((PrimaryContainer) Container.getContainer());\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		proposal= (CUCorrectionProposal) proposals.get(1);
		String preview2= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("\n");
		buf.append("import test0.PrimaryContainer;\n");
		buf.append("\n");
		buf.append("public class Container {\n");
		buf.append("    public static PrimaryContainer getContainer() {\n");
		buf.append("        return null;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected2= buf.toString();

		proposal= (CUCorrectionProposal) proposals.get(2);
		String preview3= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("\n");
		buf.append("import test0.PrimaryContainer;\n");
		buf.append("\n");
		buf.append("public class Container implements PrimaryContainer {\n");
		buf.append("    public static Container getContainer() {\n");
		buf.append("        return null;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected3= buf.toString();

		proposal= (CUCorrectionProposal) proposals.get(3);
		String preview4= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test0;\n");
		buf.append("\n");
		buf.append("import test1.Container;\n");
		buf.append("\n");
		buf.append("public interface PrimaryContainer {\n");
		buf.append("    PrimaryContainer duplicate(Container container);\n");
		buf.append("}\n");
		String expected4= buf.toString();

		proposal= (CUCorrectionProposal) proposals.get(4);
		String preview5= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test0;\n");
		buf.append("\n");
		buf.append("import test1.Container;\n");
		buf.append("\n");
		buf.append("public interface PrimaryContainer {\n");
		buf.append("    PrimaryContainer duplicate(PrimaryContainer container);\n");
		buf.append("\n");
		buf.append("    void duplicate(Container container);\n");
		buf.append("}\n");
		String expected5= buf.toString();

		assertEqualStringsIgnoreOrder(new String[] { preview1, preview2, preview3, preview4, preview5 }, new String[] { expected1, expected2, expected3, expected4, expected5 });
	}

    @Test
	public void testTypeMismatchForInterfaceInGeneric() throws Exception {

		IPackageFragment pack0= fSourceFolder.createPackageFragment("test0", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test0;\n");
		buf.append("public interface PrimaryContainer<A> {\n");
		buf.append("}\n");
		pack0.createCompilationUnit("PrimaryContainer.java", buf.toString(), false, null);

		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class Container<A> {\n");
		buf.append("    public Container<A> getContainer() {\n");
		buf.append("        return null;\n");
		buf.append("    }\n");
		buf.append("}\n");
		pack1.createCompilationUnit("Container.java", buf.toString(), false, null);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import test0.PrimaryContainer;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(Container<String> c) {\n");
		buf.append("         PrimaryContainer<String> list= c.getContainer();\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= getASTRoot(cu);
		ArrayList proposals= collectCorrections(cu, astRoot);
		assertNumberOfProposals(proposals, 4);
		assertCorrectLabels(proposals);

		CUCorrectionProposal proposal= (CUCorrectionProposal) proposals.get(0);
		String preview1= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import test0.PrimaryContainer;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(Container<String> c) {\n");
		buf.append("         Container<String> list= c.getContainer();\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		proposal= (CUCorrectionProposal) proposals.get(1);
		String preview2= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import test0.PrimaryContainer;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(Container<String> c) {\n");
		buf.append("         PrimaryContainer<String> list= (PrimaryContainer<String>) c.getContainer();\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected2= buf.toString();

		proposal= (CUCorrectionProposal) proposals.get(2);
		String preview3= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("\n");
		buf.append("import test0.PrimaryContainer;\n");
		buf.append("\n");
		buf.append("public class Container<A> {\n");
		buf.append("    public PrimaryContainer<String> getContainer() {\n");
		buf.append("        return null;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected3= buf.toString();

		proposal= (CUCorrectionProposal) proposals.get(3);
		String preview4= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("\n");
		buf.append("import test0.PrimaryContainer;\n");
		buf.append("\n");
		buf.append("public class Container<A> implements PrimaryContainer<String> {\n");
		buf.append("    public Container<A> getContainer() {\n");
		buf.append("        return null;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected4= buf.toString();

		assertEqualStringsIgnoreOrder(new String[] { preview1, preview2, preview3, preview4 }, new String[] { expected1, expected2, expected3, expected4 });

	}

    @Test
	public void testTypeMismatchForInterfaceInGeneric2() throws Exception {

		IPackageFragment pack0= fSourceFolder.createPackageFragment("test0", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test0;\n");
		buf.append("public interface PrimaryContainer<A> {\n");
		buf.append("}\n");
		pack0.createCompilationUnit("PrimaryContainer.java", buf.toString(), false, null);

		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class Container<A> {\n");
		buf.append("    public Container<A> getContainer() {\n");
		buf.append("        return null;\n");
		buf.append("    }\n");
		buf.append("}\n");
		pack1.createCompilationUnit("Container.java", buf.toString(), false, null);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.List;\n");
		buf.append("import test0.PrimaryContainer;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(Container<List<?>> c) {\n");
		buf.append("         PrimaryContainer<?> list= c.getContainer();\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= getASTRoot(cu);
		ArrayList proposals= collectCorrections(cu, astRoot);
		assertNumberOfProposals(proposals, 3);
		assertCorrectLabels(proposals);

		CUCorrectionProposal proposal= (CUCorrectionProposal) proposals.get(0);
		String preview1= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.List;\n");
		buf.append("import test0.PrimaryContainer;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(Container<List<?>> c) {\n");
		buf.append("         Container<List<?>> list= c.getContainer();\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		proposal= (CUCorrectionProposal) proposals.get(1);
		String preview2= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.List;\n");
		buf.append("import test0.PrimaryContainer;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(Container<List<?>> c) {\n");
		buf.append("         PrimaryContainer<?> list= (PrimaryContainer<?>) c.getContainer();\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected2= buf.toString();

		proposal= (CUCorrectionProposal) proposals.get(2);
		String preview3= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("\n");
		buf.append("import test0.PrimaryContainer;\n");
		buf.append("\n");
		buf.append("public class Container<A> {\n");
		buf.append("    public PrimaryContainer<?> getContainer() {\n");
		buf.append("        return null;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected3= buf.toString();

		assertEqualStringsIgnoreOrder(new String[] { preview1, preview2, preview3 }, new String[] { expected1, expected2, expected3 });

	}

    @Test
	public void testTypeMismatchForParameterizedType() throws Exception {
		Map<String, String> options= fJProject1.getOptions(false);
		try {
			Map<String, String> tempOptions= new HashMap<String, String>(options);
			tempOptions.put(JavaCore.COMPILER_PB_UNCHECKED_TYPE_OPERATION, JavaCore.WARNING);
			tempOptions.put(JavaCore.COMPILER_PB_RAW_TYPE_REFERENCE, JavaCore.WARNING);
			fJProject1.setOptions(tempOptions);
		
			IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
			StringBuffer buf= new StringBuffer();
			buf.append("package test1;\n");
			buf.append("import java.util.*;\n");
			buf.append("public class E {\n");
			buf.append("    public void foo() {\n");
			buf.append("        List list= new ArrayList<Integer>();\n");
			buf.append("    }\n");
			buf.append("}\n");
			ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);
		
			CompilationUnit astRoot= getASTRoot(cu);
			ArrayList proposals= collectCorrections(cu, astRoot);
			assertCorrectLabels(proposals);
			assertNumberOfProposals(proposals, 5);
		
			String[] expected= new String[2];
		
			buf= new StringBuffer();
			buf.append("package test1;\n");
			buf.append("import java.util.*;\n");
			buf.append("public class E {\n");
			buf.append("    public void foo() {\n");
			buf.append("        List<Integer> list= new ArrayList<Integer>();\n");
			buf.append("    }\n");
			buf.append("}\n");
			expected[0]= buf.toString();

			buf= new StringBuffer();
			buf.append("package test1;\n");
			buf.append("import java.util.*;\n");
			buf.append("public class E {\n");
			buf.append("    public void foo() {\n");
			buf.append("        ArrayList<Integer> list= new ArrayList<Integer>();\n");
			buf.append("    }\n");
			buf.append("}\n");
			expected[0]= buf.toString();
			
			assertExpectedExistInProposals(proposals, expected);
			
			
			
		} finally {
			fJProject1.setOptions(options);
		}
	}

    @Test
	public void testTypeMismatchForParameterizedType2() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.*;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        List<Integer> list= new ArrayList<Number>();\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);
		
		CompilationUnit astRoot= getASTRoot(cu);
		ArrayList proposals= collectCorrections(cu, astRoot);
		assertNumberOfProposals(proposals, 1);
		
		CUCorrectionProposal proposal= (CUCorrectionProposal) proposals.get(0);
		String preview1= getPreviewContent(proposal);
		
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.*;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        List<Number> list= new ArrayList<Number>();\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();
		
		assertEqualStringsIgnoreOrder(new String[] { preview1 }, new String[] { expected1 });
		
		Accessor accessor= new Accessor(proposal, TypeChangeCorrectionProposal.class);
		ITypeBinding[] typeProposals= (ITypeBinding[]) accessor.get("fTypeProposals");
		String[] typeNames= new String[typeProposals.length];
		for (int i= 0; i < typeNames.length; i++) {
			typeNames[i]= BindingLabelProvider.getBindingLabel(typeProposals[i], JavaElementLabels.T_TYPE_PARAMETERS | JavaElementLabels.T_FULLY_QUALIFIED);
		}
		String[] expectedNames= new String[] {
				"java.util.List<Number>",
				"java.util.ArrayList<Number>",
				"java.util.Collection<Number>",
				"java.lang.Iterable<Number>",
				"java.util.RandomAccess",
				"java.lang.Cloneable",
				"java.io.Serializable",
				"java.util.AbstractList<Number>",
				"java.util.AbstractCollection<Number>",
				"java.lang.Object",
		};
		assertArrayEquals(expectedNames, typeNames);
	}

    @Test
	public void testTypeMismatchInFieldDecl() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    int time= System.currentTimeMillis();\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= getASTRoot(cu);
		ArrayList proposals= collectCorrections(cu, astRoot);
		assertNumberOfProposals(proposals, 2);
		assertCorrectLabels(proposals);

		CUCorrectionProposal proposal= (CUCorrectionProposal) proposals.get(0);
		String preview1= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    int time= (int) System.currentTimeMillis();\n");
		buf.append("}\n");
		String expected1= buf.toString();

		proposal= (CUCorrectionProposal) proposals.get(1);
		String preview2= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    long time= System.currentTimeMillis();\n");
		buf.append("}\n");
		String expected2= buf.toString();

		assertEqualStringsIgnoreOrder(new String[] { preview1, preview2 }, new String[] { expected1, expected2 });
	}

    @Test
	public void testTypeMismatchInFieldDeclNoImport() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    private class StringBuffer { }\n");
		buf.append("    private final StringBuffer sb;\n");
		buf.append("    public E() {\n");
		buf.append("        sb= new java.lang.StringBuffer();\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);
		
		CompilationUnit astRoot= getASTRoot(cu);
		ArrayList proposals= collectCorrections(cu, astRoot);
		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);
		
		CUCorrectionProposal proposal= (CUCorrectionProposal) proposals.get(0);
		String preview1= getPreviewContent(proposal);
		
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    private class StringBuffer { }\n");
		buf.append("    private final java.lang.StringBuffer sb;\n");
		buf.append("    public E() {\n");
		buf.append("        sb= new java.lang.StringBuffer();\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();
		
		assertEqualString(preview1, expected1);
	}

    @Test
	public void testTypeMismatchInAssignment() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.Iterator;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(Iterator iter) {\n");
		buf.append("        String str;\n");
		buf.append("        str= iter.next();\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= getASTRoot(cu);
		ArrayList proposals= collectCorrections(cu, astRoot);
		assertNumberOfProposals(proposals, 2);
		assertCorrectLabels(proposals);

		CUCorrectionProposal proposal= (CUCorrectionProposal) proposals.get(0);
		String preview1= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.Iterator;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(Iterator iter) {\n");
		buf.append("        String str;\n");
		buf.append("        str= (String) iter.next();\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		proposal= (CUCorrectionProposal) proposals.get(1);
		String preview2= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.Iterator;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(Iterator iter) {\n");
		buf.append("        Object str;\n");
		buf.append("        str= iter.next();\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected2= buf.toString();

		assertEqualStringsIgnoreOrder(new String[] { preview1, preview2 }, new String[] { expected1, expected2 });

	}

    @Test
	public void testTypeMismatchInAssignment2() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.Iterator;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(Iterator iter) {\n");
		buf.append("        String str, str2;\n");
		buf.append("        str= iter.next();\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= getASTRoot(cu);
		ArrayList proposals= collectCorrections(cu, astRoot);
		assertNumberOfProposals(proposals, 2);
		assertCorrectLabels(proposals);

		CUCorrectionProposal proposal= (CUCorrectionProposal) proposals.get(0);
		String preview1= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.Iterator;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(Iterator iter) {\n");
		buf.append("        String str, str2;\n");
		buf.append("        str= (String) iter.next();\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		proposal= (CUCorrectionProposal) proposals.get(1);
		String preview2= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.Iterator;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(Iterator iter) {\n");
		buf.append("        Object str;\n");
		buf.append("        String str2;\n");
		buf.append("        str= iter.next();\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected2= buf.toString();

		assertEqualStringsIgnoreOrder(new String[] { preview1, preview2 }, new String[] { expected1, expected2 });

	}

    @Test
	public void testTypeMismatchInAssignment3() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.Iterator;\n");
		buf.append("public enum E {\n");
		buf.append("    A, B;\n");
		buf.append("    String str, str2;\n");
		buf.append("    public void foo(Iterator iter) {\n");
		buf.append("        str2= iter.next();\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= getASTRoot(cu);
		ArrayList proposals= collectCorrections(cu, astRoot);
		assertNumberOfProposals(proposals, 2);
		assertCorrectLabels(proposals);

		CUCorrectionProposal proposal= (CUCorrectionProposal) proposals.get(0);
		String preview1= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.Iterator;\n");
		buf.append("public enum E {\n");
		buf.append("    A, B;\n");
		buf.append("    String str, str2;\n");
		buf.append("    public void foo(Iterator iter) {\n");
		buf.append("        str2= (String) iter.next();\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		proposal= (CUCorrectionProposal) proposals.get(1);
		String preview2= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.Iterator;\n");
		buf.append("public enum E {\n");
		buf.append("    A, B;\n");
		buf.append("    String str;\n");
		buf.append("    Object str2;\n");
		buf.append("    public void foo(Iterator iter) {\n");
		buf.append("        str2= iter.next();\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected2= buf.toString();

		assertEqualStringsIgnoreOrder(new String[] { preview1, preview2 }, new String[] { expected1, expected2 });
	}

    @Test
	public void testTypeMismatchInExpression() throws Exception {

		IPackageFragment pack0= fSourceFolder.createPackageFragment("test0", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test0;\n");
		buf.append("public class Other {\n");
		buf.append("    public Object[] toArray() {\n");
		buf.append("        return null;\n");
		buf.append("    }\n");
		buf.append("}\n");
		pack0.createCompilationUnit("Other.java", buf.toString(), false, null);


		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import test0.Other;\n");
		buf.append("public class E {\n");
		buf.append("    public String[] foo(Other other) {\n");
		buf.append("        return other.toArray();\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= getASTRoot(cu);
		ArrayList proposals= collectCorrections(cu, astRoot);
		assertNumberOfProposals(proposals, 3);
		assertCorrectLabels(proposals);

		CUCorrectionProposal proposal= (CUCorrectionProposal) proposals.get(0);
		String preview1= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import test0.Other;\n");
		buf.append("public class E {\n");
		buf.append("    public String[] foo(Other other) {\n");
		buf.append("        return (String[]) other.toArray();\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		proposal= (CUCorrectionProposal) proposals.get(1);
		String preview2= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import test0.Other;\n");
		buf.append("public class E {\n");
		buf.append("    public Object[] foo(Other other) {\n");
		buf.append("        return other.toArray();\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected2= buf.toString();

		proposal= (CUCorrectionProposal) proposals.get(2);
		String preview3= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test0;\n");
		buf.append("public class Other {\n");
		buf.append("    public String[] toArray() {\n");
		buf.append("        return null;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected3= buf.toString();

		assertEqualStringsIgnoreOrder(new String[] { preview1, preview2, preview3 }, new String[] { expected1, expected2, expected3 });
	}

    @Test
	public void testCastOnCastExpression() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.ArrayList;\n");
		buf.append("import java.util.List;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(List list) {\n");
		buf.append("        ArrayList a= (Cloneable) list;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= getASTRoot(cu);
		ArrayList proposals= collectCorrections(cu, astRoot);
		assertNumberOfProposals(proposals, 2);
		assertCorrectLabels(proposals);

		CUCorrectionProposal proposal= (CUCorrectionProposal) proposals.get(0);
		String preview1= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.ArrayList;\n");
		buf.append("import java.util.List;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(List list) {\n");
		buf.append("        ArrayList a= (ArrayList) list;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		proposal= (CUCorrectionProposal) proposals.get(1);
		String preview2= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.ArrayList;\n");
		buf.append("import java.util.List;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(List list) {\n");
		buf.append("        Cloneable a= (Cloneable) list;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected2= buf.toString();

		assertEqualStringsIgnoreOrder(new String[] { preview1, preview2 }, new String[] { expected1, expected2 });
	}

    @Test
	public void testMismatchingReturnType1() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class Base {\n");
		buf.append("    public String getName() {\n");
		buf.append("        return null;\n");
		buf.append("    }\n");
		buf.append("}\n");
		pack1.createCompilationUnit("Base.java", buf.toString(), false, null);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E extends Base {\n");
		buf.append("    public char[] getName() {\n");
		buf.append("        return null;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= getASTRoot(cu);
		ArrayList proposals= collectCorrections(cu, astRoot);
		assertNumberOfProposals(proposals, 2);
		assertCorrectLabels(proposals);

		CUCorrectionProposal proposal= (CUCorrectionProposal) proposals.get(0);
		String preview1= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E extends Base {\n");
		buf.append("    public String getName() {\n");
		buf.append("        return null;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		proposal= (CUCorrectionProposal) proposals.get(1);
		String preview2= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class Base {\n");
		buf.append("    public char[] getName() {\n");
		buf.append("        return null;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected2= buf.toString();

		assertEqualStringsIgnoreOrder(new String[] { preview1, preview2 }, new String[] { expected1, expected2 });
	}

    @Test
	public void testMismatchingReturnType2() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.List;\n");
		buf.append("public interface IBase {\n");
		buf.append("    List getCollection();\n");
		buf.append("}\n");
		pack1.createCompilationUnit("IBase.java", buf.toString(), false, null);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E implements IBase {\n");
		buf.append("    public String[] getCollection() {\n");
		buf.append("        return null;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= getASTRoot(cu);
		ArrayList proposals= collectCorrections(cu, astRoot);
		assertNumberOfProposals(proposals, 2);
		assertCorrectLabels(proposals);

		CUCorrectionProposal proposal= (CUCorrectionProposal) proposals.get(0);
		String preview1= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("\n");
		buf.append("import java.util.List;\n");
		buf.append("\n");
		buf.append("public class E implements IBase {\n");
		buf.append("    public List getCollection() {\n");
		buf.append("        return null;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		proposal= (CUCorrectionProposal) proposals.get(1);
		String preview2= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.List;\n");
		buf.append("public interface IBase {\n");
		buf.append("    String[] getCollection();\n");
		buf.append("}\n");
		String expected2= buf.toString();

		assertEqualStringsIgnoreOrder(new String[] { preview1, preview2 }, new String[] { expected1, expected2 });
	}

    @Test
	public void testMismatchingReturnTypeOnGeneric() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class Base<T extends Number> {\n");
		buf.append("    public String getName(T... t) {\n");
		buf.append("        return null;\n");
		buf.append("    }\n");
		buf.append("}\n");
		pack1.createCompilationUnit("Base.java", buf.toString(), false, null);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E extends Base<Integer> {\n");
		buf.append("    public char[] getName(Integer... i) {\n");
		buf.append("        return null;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= getASTRoot(cu);
		ArrayList proposals= collectCorrections(cu, astRoot);
		assertNumberOfProposals(proposals, 2);
		assertCorrectLabels(proposals);

		CUCorrectionProposal proposal= (CUCorrectionProposal) proposals.get(0);
		String preview1= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E extends Base<Integer> {\n");
		buf.append("    public String getName(Integer... i) {\n");
		buf.append("        return null;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		proposal= (CUCorrectionProposal) proposals.get(1);
		String preview2= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class Base<T extends Number> {\n");
		buf.append("    public char[] getName(T... t) {\n");
		buf.append("        return null;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected2= buf.toString();

		assertEqualStringsIgnoreOrder(new String[] { preview1, preview2 }, new String[] { expected1, expected2 });
	}

    @Test
	public void testMismatchingReturnTypeOnGeneric2() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class Base {\n");
		buf.append("    public Number getVal() {\n");
		buf.append("        return null;\n");
		buf.append("    }\n");
		buf.append("}\n");
		pack1.createCompilationUnit("Base.java", buf.toString(), false, null);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E<T> extends Base {\n");
		buf.append("    public T getVal() {\n");
		buf.append("        return null;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= getASTRoot(cu);
		ArrayList proposals= collectCorrections(cu, astRoot);
		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);

		CUCorrectionProposal proposal= (CUCorrectionProposal) proposals.get(0);
		String preview1= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E<T> extends Base {\n");
		buf.append("    public Number getVal() {\n");
		buf.append("        return null;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertEqualStringsIgnoreOrder(new String[] { preview1 }, new String[] { expected1 });
	}

    @Test
	public void testMismatchingReturnTypeOnGenericMethod() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.lang.annotation.Annotation;\n");
		buf.append("import java.lang.reflect.AccessibleObject;\n");
		buf.append("public class E {\n");
		buf.append("    void m() {\n");
		buf.append("        new AccessibleObject() {\n");
		buf.append("            public <T extends Annotation> void getAnnotation(Class<T> annotationClass) {\n");
		buf.append("            }\n");
		buf.append("        };\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);
		
		CompilationUnit astRoot= getASTRoot(cu);
		ArrayList proposals= collectCorrections(cu, astRoot);
		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);
		
		CUCorrectionProposal proposal= (CUCorrectionProposal) proposals.get(0);
		String preview1= getPreviewContent(proposal);
		
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.lang.annotation.Annotation;\n");
		buf.append("import java.lang.reflect.AccessibleObject;\n");
		buf.append("public class E {\n");
		buf.append("    void m() {\n");
		buf.append("        new AccessibleObject() {\n");
		buf.append("            public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {\n");
		buf.append("            }\n");
		buf.append("        };\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();
		
		assertEqualStringsIgnoreOrder(new String[] { preview1 }, new String[] { expected1 });
	}

    @Test
	public void testMismatchingReturnTypeOnGenericMethod14() throws Exception {
		Map<String, String> options= fJProject1.getOptions(false);
		try {
			Map<String, String> options14= new HashMap<String, String>(options);
			JavaModelUtil.setComplianceOptions(options14, JavaCore.VERSION_1_4);
			fJProject1.setOptions(options14);
			IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
			
			StringBuffer buf= new StringBuffer();
			buf.append("package test1;\n");
			buf.append("import java.lang.reflect.AccessibleObject;\n");
			buf.append("public class E {\n");
			buf.append("    void m() {\n");
			buf.append("        new AccessibleObject() {\n");
			buf.append("            public void getAnnotation(Class annotationClass) {\n");
			buf.append("            }\n");
			buf.append("        };\n");
			buf.append("    }\n");
			buf.append("}\n");
			ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);
			
			CompilationUnit astRoot= getASTRoot(cu);
			ArrayList proposals= collectCorrections(cu, astRoot);
			assertNumberOfProposals(proposals, 1);
			assertCorrectLabels(proposals);
			
			CUCorrectionProposal proposal= (CUCorrectionProposal) proposals.get(0);
			String preview1= getPreviewContent(proposal);
			
			buf= new StringBuffer();
			buf.append("package test1;\n");
			buf.append("import java.lang.annotation.Annotation;\n");
			buf.append("import java.lang.reflect.AccessibleObject;\n");
			buf.append("public class E {\n");
			buf.append("    void m() {\n");
			buf.append("        new AccessibleObject() {\n");
			buf.append("            public Annotation getAnnotation(Class annotationClass) {\n");
			buf.append("            }\n");
			buf.append("        };\n");
			buf.append("    }\n");
			buf.append("}\n");
			String expected1= buf.toString();
			
			assertEqualStringsIgnoreOrder(new String[] { preview1 }, new String[] { expected1 });
		} finally {
			fJProject1.setOptions(options);
		}
	}

    @Test
	public void testMismatchingReturnTypeParameterized() throws Exception {
		// test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=165913
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class Base {\n");
		buf.append("    public Number getVal() {\n");
		buf.append("        return null;\n");
		buf.append("    }\n");
		buf.append("}\n");
		pack1.createCompilationUnit("Base.java", buf.toString(), false, null);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E<T> extends Base {\n");
		buf.append("    public E<T> getVal() {\n");
		buf.append("        return null;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= getASTRoot(cu);
		ArrayList proposals= collectCorrections(cu, astRoot);
		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);

		CUCorrectionProposal proposal= (CUCorrectionProposal) proposals.get(0);
		String preview1= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E<T> extends Base {\n");
		buf.append("    public Number getVal() {\n");
		buf.append("        return null;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertEqualStringsIgnoreOrder(new String[] { preview1 }, new String[] { expected1 });
	}

    @Test
	public void testMismatchingReturnTypeOnWildcardExtends() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.ArrayList;\n");
		buf.append("public class E {\n");
		buf.append("    public Integer getIt(ArrayList<? extends Number> b) {\n");
		buf.append("        return b.get(0);\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= getASTRoot(cu);
		ArrayList proposals= collectCorrections(cu, astRoot);
		assertNumberOfProposals(proposals, 2);
		assertCorrectLabels(proposals);

		ASTRewriteCorrectionProposal proposal= (ASTRewriteCorrectionProposal) proposals.get(0);
		String preview1= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.ArrayList;\n");
		buf.append("public class E {\n");
		buf.append("    public Number getIt(ArrayList<? extends Number> b) {\n");
		buf.append("        return b.get(0);\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		proposal= (ASTRewriteCorrectionProposal) proposals.get(1);
		String preview2= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.ArrayList;\n");
		buf.append("public class E {\n");
		buf.append("    public Integer getIt(ArrayList<? extends Number> b) {\n");
		buf.append("        return (Integer) b.get(0);\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected2= buf.toString();

		assertEqualStringsIgnoreOrder(new String[] { preview1, preview2 }, new String[] { expected1, expected2 });
	}

    @Test
	public void testMismatchingReturnTypeOnWildcardSuper() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.ArrayList;\n");
		buf.append("public class E {\n");
		buf.append("    public Integer getIt(ArrayList<? super Number> b) {\n");
		buf.append("        return b.get(0);\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= getASTRoot(cu);
		ArrayList proposals= collectCorrections(cu, astRoot);
		assertNumberOfProposals(proposals, 2);
		assertCorrectLabels(proposals);

		ASTRewriteCorrectionProposal proposal= (ASTRewriteCorrectionProposal) proposals.get(0);
		String preview1= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.ArrayList;\n");
		buf.append("public class E {\n");
		buf.append("    public Object getIt(ArrayList<? super Number> b) {\n");
		buf.append("        return b.get(0);\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		proposal= (ASTRewriteCorrectionProposal) proposals.get(1);
		String preview2= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.ArrayList;\n");
		buf.append("public class E {\n");
		buf.append("    public Integer getIt(ArrayList<? super Number> b) {\n");
		buf.append("        return (Integer) b.get(0);\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected2= buf.toString();

		assertEqualStringsIgnoreOrder(new String[] { preview1, preview2 }, new String[] { expected1, expected2 });
	}

    @Test
	public void testMismatchingExceptions1() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public interface IBase {\n");
		buf.append("    String[] getValues();\n");
		buf.append("}\n");
		pack1.createCompilationUnit("IBase.java", buf.toString(), false, null);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.io.IOException;\n");
		buf.append("public class E implements IBase {\n");
		buf.append("    public String[] getValues() throws IOException {\n");
		buf.append("        return null;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= getASTRoot(cu);
		ArrayList proposals= collectCorrections(cu, astRoot);
		assertNumberOfProposals(proposals, 2);
		assertCorrectLabels(proposals);

		CUCorrectionProposal proposal= (CUCorrectionProposal) proposals.get(0);
		String preview1= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("\n");
		buf.append("import java.io.IOException;\n");
		buf.append("\n");
		buf.append("public interface IBase {\n");
		buf.append("    String[] getValues() throws IOException;\n");
		buf.append("}\n");
		String expected1= buf.toString();

		proposal= (CUCorrectionProposal) proposals.get(1);
		String preview2= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.io.IOException;\n");
		buf.append("public class E implements IBase {\n");
		buf.append("    public String[] getValues() {\n");
		buf.append("        return null;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected2= buf.toString();

		assertEqualStringsIgnoreOrder(new String[] { preview1, preview2 }, new String[] { expected1, expected2 });
	}

    @Test
	public void testMismatchingExceptions2() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.io.IOException;\n");
		buf.append("public class Base {\n");
		buf.append("    String[] getValues() throws IOException {\n");
		buf.append("        return null;\n");
		buf.append("    }\n");
		buf.append("}\n");
		pack1.createCompilationUnit("Base.java", buf.toString(), false, null);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.io.EOFException;\n");
		buf.append("import java.text.ParseException;\n");
		buf.append("public class E extends Base {\n");
		buf.append("    public String[] getValues() throws EOFException, ParseException {\n");
		buf.append("        return null;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= getASTRoot(cu);
		ArrayList proposals= collectCorrections(cu, astRoot);
		assertNumberOfProposals(proposals, 2);
		assertCorrectLabels(proposals);

		CUCorrectionProposal proposal= (CUCorrectionProposal) proposals.get(0);
		String preview1= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.io.IOException;\n");
		buf.append("import java.text.ParseException;\n");
		buf.append("public class Base {\n");
		buf.append("    String[] getValues() throws IOException, ParseException {\n");
		buf.append("        return null;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		proposal= (CUCorrectionProposal) proposals.get(1);
		String preview2= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.io.EOFException;\n");
		buf.append("import java.text.ParseException;\n");
		buf.append("public class E extends Base {\n");
		buf.append("    public String[] getValues() throws EOFException {\n");
		buf.append("        return null;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected2= buf.toString();

		assertEqualStringsIgnoreOrder(new String[] { preview1, preview2 }, new String[] { expected1, expected2 });
	}

    @Test
	public void testMismatchingExceptions3() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.io.IOException;\n");
		buf.append("public class Base {\n");
		buf.append("    /**\n");
		buf.append("     * @param i The parameter\n");
		buf.append("     *                  More about the parameter\n");
		buf.append("     * @return The returned argument\n");
		buf.append("     * @throws IOException IO problems\n");
		buf.append("     * @since 3.0\n");
		buf.append("     */\n");
		buf.append("    String[] getValues(int i) throws IOException {\n");
		buf.append("        return null;\n");
		buf.append("    }\n");
		buf.append("}\n");
		pack1.createCompilationUnit("Base.java", buf.toString(), false, null);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.io.EOFException;\n");
		buf.append("import java.text.ParseException;\n");
		buf.append("public class E extends Base {\n");
		buf.append("    /**\n");
		buf.append("     * @param i The parameter\n");
		buf.append("     *                  More about the parameter\n");
		buf.append("     * @return The returned argument\n");
		buf.append("     * @throws EOFException EOF problems\n");
		buf.append("     * @throws ParseException Parse problems\n");
		buf.append("     */\n");
		buf.append("    public String[] getValues(int i) throws EOFException, ParseException {\n");
		buf.append("        return null;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= getASTRoot(cu);
		ArrayList proposals= collectCorrections(cu, astRoot);
		assertNumberOfProposals(proposals, 2);
		assertCorrectLabels(proposals);

		CUCorrectionProposal proposal= (CUCorrectionProposal) proposals.get(0);
		String preview1= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.io.IOException;\n");
		buf.append("import java.text.ParseException;\n");
		buf.append("public class Base {\n");
		buf.append("    /**\n");
		buf.append("     * @param i The parameter\n");
		buf.append("     *                  More about the parameter\n");
		buf.append("     * @return The returned argument\n");
		buf.append("     * @throws IOException IO problems\n");
		buf.append("     * @throws ParseException \n");
		buf.append("     * @since 3.0\n");
		buf.append("     */\n");
		buf.append("    String[] getValues(int i) throws IOException, ParseException {\n");
		buf.append("        return null;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		proposal= (CUCorrectionProposal) proposals.get(1);
		String preview2= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.io.EOFException;\n");
		buf.append("import java.text.ParseException;\n");
		buf.append("public class E extends Base {\n");
		buf.append("    /**\n");
		buf.append("     * @param i The parameter\n");
		buf.append("     *                  More about the parameter\n");
		buf.append("     * @return The returned argument\n");
		buf.append("     * @throws EOFException EOF problems\n");
		buf.append("     */\n");
		buf.append("    public String[] getValues(int i) throws EOFException {\n");
		buf.append("        return null;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected2= buf.toString();

		assertEqualStringsIgnoreOrder(new String[] { preview1, preview2 }, new String[] { expected1, expected2 });
	}

    @Test
	public void testMismatchingExceptionsOnGeneric() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public interface IBase<T> {\n");
		buf.append("    T[] getValues();\n");
		buf.append("}\n");
		pack1.createCompilationUnit("IBase.java", buf.toString(), false, null);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.io.IOException;\n");
		buf.append("public class E implements IBase<String> {\n");
		buf.append("    public String[] getValues() throws IOException {\n");
		buf.append("        return null;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= getASTRoot(cu);
		ArrayList proposals= collectCorrections(cu, astRoot);
		assertNumberOfProposals(proposals, 2);
		assertCorrectLabels(proposals);

		CUCorrectionProposal proposal= (CUCorrectionProposal) proposals.get(0);
		String preview1= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("\n");
		buf.append("import java.io.IOException;\n");
		buf.append("\n");
		buf.append("public interface IBase<T> {\n");
		buf.append("    T[] getValues() throws IOException;\n");
		buf.append("}\n");
		String expected1= buf.toString();

		proposal= (CUCorrectionProposal) proposals.get(1);
		String preview2= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.io.IOException;\n");
		buf.append("public class E implements IBase<String> {\n");
		buf.append("    public String[] getValues() {\n");
		buf.append("        return null;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected2= buf.toString();

		assertEqualStringsIgnoreOrder(new String[] { preview1, preview2 }, new String[] { expected1, expected2 });
	}

    @Test
	public void testMismatchingExceptionsOnBinaryParent() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E implements Runnable {\n");
		buf.append("    public void run() throws ClassNotFoundException {\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= getASTRoot(cu);
		ArrayList proposals= collectCorrections(cu, astRoot);
		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);

		CUCorrectionProposal proposal= (CUCorrectionProposal) proposals.get(0);
		String preview= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E implements Runnable {\n");
		buf.append("    public void run() {\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected= buf.toString();

		assertEqualString(preview, expected);
	}

    @Test
	public void testTypeMismatchInAnnotationValues1() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("pack", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack;\n");
		buf.append("public class E {\n");
		buf.append("    public @interface Annot {\n");
		buf.append("        String newAttrib();\n");
		buf.append("    }\n");
		buf.append("    @Annot(newAttrib= 1)\n");
		buf.append("    public void foo() {\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= getASTRoot(cu);
		ArrayList proposals= collectCorrections(cu, astRoot);

		assertCorrectLabels(proposals);
		assertNumberOfProposals(proposals, 1);

		String[] expected= new String[1];
		buf= new StringBuffer();
		buf.append("package pack;\n");
		buf.append("public class E {\n");
		buf.append("    public @interface Annot {\n");
		buf.append("        int newAttrib();\n");
		buf.append("    }\n");
		buf.append("    @Annot(newAttrib= 1)\n");
		buf.append("    public void foo() {\n");
		buf.append("    }\n");
		buf.append("}\n");
		expected[0]= buf.toString();

		assertExpectedExistInProposals(proposals, expected);
	}

    @Test
	public void testTypeMismatchInAnnotationValues2() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("pack", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack;\n");
		buf.append("public class Other<T> {\n");
		buf.append("    public @interface Annot {\n");
		buf.append("        String newAttrib();\n");
		buf.append("    }\n");
		buf.append("}\n");
		pack1.createCompilationUnit("Other.java", buf.toString(), false, null);

		buf= new StringBuffer();
		buf.append("package pack;\n");
		buf.append("public class E {\n");
		buf.append("    @Other.Annot(newAttrib= 1)\n");
		buf.append("    public void foo() {\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= getASTRoot(cu);
		ArrayList proposals= collectCorrections(cu, astRoot);

		assertCorrectLabels(proposals);
		assertNumberOfProposals(proposals, 1);

		String[] expected= new String[1];
		buf= new StringBuffer();
		buf.append("package pack;\n");
		buf.append("public class Other<T> {\n");
		buf.append("    public @interface Annot {\n");
		buf.append("        int newAttrib();\n");
		buf.append("    }\n");
		buf.append("}\n");
		expected[0]= buf.toString();

		assertExpectedExistInProposals(proposals, expected);
	}

    @Test
	public void testTypeMismatchInSingleMemberAnnotation() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("pack", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack;\n");
		buf.append("public class E {\n");
		buf.append("    public @interface Annot {\n");
		buf.append("        String value();\n");
		buf.append("    }\n");
		buf.append("    @Annot(1)\n");
		buf.append("    public void foo() {\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= getASTRoot(cu);
		ArrayList proposals= collectCorrections(cu, astRoot);

		assertCorrectLabels(proposals);
		assertNumberOfProposals(proposals, 1);

		String[] expected= new String[1];
		buf= new StringBuffer();
		buf.append("package pack;\n");
		buf.append("public class E {\n");
		buf.append("    public @interface Annot {\n");
		buf.append("        int value();\n");
		buf.append("    }\n");
		buf.append("    @Annot(1)\n");
		buf.append("    public void foo() {\n");
		buf.append("    }\n");
		buf.append("}\n");
		expected[0]= buf.toString();

		assertExpectedExistInProposals(proposals, expected);
	}

    @Test
	public void testTypeMismatchWithEnumConstant() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("pack", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack;\n");
		buf.append("public enum E {\n");
		buf.append("    ONE;\n");
		buf.append("    int m(int i) {\n");
		buf.append("            return ONE;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= getASTRoot(cu);
		ArrayList proposals= collectCorrections(cu, astRoot);

		assertCorrectLabels(proposals);
		assertNumberOfProposals(proposals, 1);

		String[] expected= new String[1];
		buf= new StringBuffer();
		buf.append("package pack;\n");
		buf.append("public enum E {\n");
		buf.append("    ONE;\n");
		buf.append("    E m(int i) {\n");
		buf.append("            return ONE;\n");
		buf.append("    }\n");
		buf.append("}\n");
		expected[0]= buf.toString();

		assertExpectedExistInProposals(proposals, expected);
	}

    @Test
	public void testTypeMismatchWithArrayLength() throws Exception {
		// test for bug 126488
		IPackageFragment pack1= fSourceFolder.createPackageFragment("pack", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack;\n");
		buf.append("public class TestShort {\n");
		buf.append("        public static void main(String[] args) {\n");
		buf.append("                short test=args.length;\n");
		buf.append("        }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("TestShort.java", buf.toString(), false, null);

		CompilationUnit astRoot= getASTRoot(cu);
		ArrayList proposals= collectCorrections(cu, astRoot);

		assertCorrectLabels(proposals);
		assertNumberOfProposals(proposals, 2);

		String[] expected= new String[2];
		buf= new StringBuffer();
		buf.append("package pack;\n");
		buf.append("public class TestShort {\n");
		buf.append("        public static void main(String[] args) {\n");
		buf.append("                short test=(short) args.length;\n");
		buf.append("        }\n");
		buf.append("}\n");
		expected[0]= buf.toString();

		buf= new StringBuffer();
		buf.append("package pack;\n");
		buf.append("public class TestShort {\n");
		buf.append("        public static void main(String[] args) {\n");
		buf.append("                int test=args.length;\n");
		buf.append("        }\n");
		buf.append("}\n");
		expected[1]= buf.toString();

		assertExpectedExistInProposals(proposals, expected);
	}

    @Test
	public void testTypeMismatchWithTypeInSamePackage() throws Exception {
		// test for bug 198586
		IPackageFragment pack2= fSourceFolder.createPackageFragment("test2", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {}\n");
		pack2.createCompilationUnit("E.java", buf.toString(), false, null);

		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {}\n");
		pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class Test {\n");
		buf.append("    test2.E e2= new Object();\n");
		buf.append("    E e1;\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("Test.java", buf.toString(), false, null);

		CompilationUnit astRoot= getASTRoot(cu);
		ArrayList proposals= collectCorrections(cu, astRoot);

		assertCorrectLabels(proposals);
		assertNumberOfProposals(proposals, 2);

		String[] expected= new String[2];
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class Test {\n");
		buf.append("    test2.E e2= (test2.E) new Object();\n");
		buf.append("    E e1;\n");
		buf.append("}\n");
		expected[0]= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class Test {\n");
		buf.append("    Object e2= new Object();\n");
		buf.append("    E e1;\n");
		buf.append("}\n");
		expected[1]= buf.toString();

		assertExpectedExistInProposals(proposals, expected);
	}

    @Test
	public void testTypeMismatchInForEachProposalsList() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("pack", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack;\n");
		buf.append("\n");
		buf.append("import java.util.List;\n");
		buf.append("\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        List<String> l= null;    \n");
		buf.append("        for (Number e : l) {\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= getASTRoot(cu);
		ArrayList proposals= collectCorrections(cu, astRoot);

		assertCorrectLabels(proposals);
		assertNumberOfProposals(proposals, 1);

		String[] expected= new String[1];
		buf= new StringBuffer();
		buf.append("package pack;\n");
		buf.append("\n");
		buf.append("import java.util.List;\n");
		buf.append("\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        List<String> l= null;    \n");
		buf.append("        for (String e : l) {\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		expected[0]= buf.toString();

		assertExpectedExistInProposals(proposals, expected);
	}

    @Test
	public void testTypeMismatchInForEachProposalsListExtends() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("pack", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack;\n");
		buf.append("\n");
		buf.append("import java.util.List;\n");
		buf.append("\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        List<? extends String> l= null;    \n");
		buf.append("        for (Number e : l) {\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);
		
		CompilationUnit astRoot= getASTRoot(cu);
		ArrayList proposals= collectCorrections(cu, astRoot);
		
		assertCorrectLabels(proposals);
		assertNumberOfProposals(proposals, 1);
		
		String[] expected= new String[1];
		buf= new StringBuffer();
		buf.append("package pack;\n");
		buf.append("\n");
		buf.append("import java.util.List;\n");
		buf.append("\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        List<? extends String> l= null;    \n");
		buf.append("        for (String e : l) {\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		expected[0]= buf.toString();
		
		assertExpectedExistInProposals(proposals, expected);
	}

    @Test
	public void testTypeMismatchInForEachProposalsListSuper() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("pack", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack;\n");
		buf.append("\n");
		buf.append("import java.util.List;\n");
		buf.append("\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        List<? super String> l= null;    \n");
		buf.append("        for (Number e : l) {\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);
		
		CompilationUnit astRoot= getASTRoot(cu);
		ArrayList proposals= collectCorrections(cu, astRoot);
		
		assertCorrectLabels(proposals);
		assertNumberOfProposals(proposals, 1);
		
		String[] expected= new String[1];
		buf= new StringBuffer();
		buf.append("package pack;\n");
		buf.append("\n");
		buf.append("import java.util.List;\n");
		buf.append("\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        List<? super String> l= null;    \n");
		buf.append("        for (Object e : l) {\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		expected[0]= buf.toString();
		
		assertExpectedExistInProposals(proposals, expected);
	}

    @Test
	public void testTypeMismatchInForEachProposalsArrays() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("pack", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack;\n");
		buf.append("\n");
		buf.append("import java.util.List;\n");
		buf.append("\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        String[] l= null;\n");
		buf.append("        for (Number e : l) {\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= getASTRoot(cu);
		ArrayList proposals= collectCorrections(cu, astRoot);

		assertCorrectLabels(proposals);
		assertNumberOfProposals(proposals, 1);

		String[] expected= new String[1];
		buf= new StringBuffer();
		buf.append("package pack;\n");
		buf.append("\n");
		buf.append("import java.util.List;\n");
		buf.append("\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        String[] l= null;\n");
		buf.append("        for (String e : l) {\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		expected[0]= buf.toString();

		assertExpectedExistInProposals(proposals, expected);
	}

    @Test
	public void testTypeMismatchInForEachMissingType() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("pack", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack;\n");
		buf.append("\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(String[] strings) {\n");
		buf.append("        for (s: strings) {\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);
		
		CompilationUnit astRoot= getASTRoot(cu);
		ArrayList proposals= collectCorrections(cu, astRoot, 3, 2);
		
		assertCorrectLabels(proposals);
		assertNumberOfProposals(proposals, 1);
		
		String[] expected= new String[1];
		buf= new StringBuffer();
		buf.append("package pack;\n");
		buf.append("\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(String[] strings) {\n");
		buf.append("        for (String s: strings) {\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		expected[0]= buf.toString();
		
		assertExpectedExistInProposals(proposals, expected);
	}

    @Test
	public void testNullCheck() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("pack", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack;\n");
		buf.append("\n");
		buf.append("public class E {\n");
		buf.append("    public static void main(String arg) {\n");
		buf.append("        while (arg) {\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		buf.append("\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= getASTRoot(cu);
		ArrayList proposals= collectCorrections(cu, astRoot);

		assertCorrectLabels(proposals);
		assertNumberOfProposals(proposals, 2);

		String[] expected= new String[2];
		buf= new StringBuffer();
		buf.append("package pack;\n");
		buf.append("\n");
		buf.append("public class E {\n");
		buf.append("    public static void main(boolean arg) {\n");
		buf.append("        while (arg) {\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		buf.append("\n");
		expected[0]= buf.toString();

		buf= new StringBuffer();
		buf.append("package pack;\n");
		buf.append("\n");
		buf.append("public class E {\n");
		buf.append("    public static void main(String arg) {\n");
		buf.append("        while (arg != null) {\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		buf.append("\n");
		expected[1]= buf.toString();

		assertExpectedExistInProposals(proposals, expected);
	}

    @Test
	public void testTypeMismatchObjectAndPrimitiveType() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("pack", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack;\n");
		buf.append("\n");
		buf.append("\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        Object o= new Object();\n");
		buf.append("        int i= o;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= getASTRoot(cu);
		ArrayList proposals= collectCorrections(cu, astRoot);

		assertCorrectLabels(proposals);
		assertNumberOfProposals(proposals, 3);

		ICompletionProposal proposal= (ICompletionProposal) proposals.get(0);
		assertTrue(proposal.getDisplayString().indexOf("Integer") != -1);
		
		String[] expected= new String[3];
		buf= new StringBuffer();
		buf.append("package pack;\n");
		buf.append("\n");
		buf.append("\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        Object o= new Object();\n");
		buf.append("        int i= (Integer) o;\n");
		buf.append("    }\n");
		buf.append("}\n");
		expected[0]= buf.toString();

		buf= new StringBuffer();
		buf.append("package pack;\n");
		buf.append("\n");
		buf.append("\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        int o= new Object();\n");
		buf.append("        int i= o;\n");
		buf.append("    }\n");
		buf.append("}\n");
		expected[1]= buf.toString();

		buf= new StringBuffer();
		buf.append("package pack;\n");
		buf.append("\n");
		buf.append("\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        Object o= new Object();\n");
		buf.append("        Object i= o;\n");
		buf.append("    }\n");
		buf.append("}\n");
		expected[2]= buf.toString();

		assertExpectedExistInProposals(proposals, expected);
	}

    @Test
	public void testTypeMismatchPrimitiveTypes() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("pack", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack;\n");
		buf.append("\n");
		buf.append("\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(long o) {\n");
		buf.append("        int i= o;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= getASTRoot(cu);
		ArrayList proposals= collectCorrections(cu, astRoot);

		assertCorrectLabels(proposals);
		assertNumberOfProposals(proposals, 3);

		ICompletionProposal proposal= (ICompletionProposal) proposals.get(0);
		assertTrue(proposal.getDisplayString().indexOf("Integer") == -1);
		
		ICompletionProposal proposal2= (ICompletionProposal) proposals.get(1);
		assertTrue(proposal2.getDisplayString().indexOf("Integer") == -1);
		
		ICompletionProposal proposal3= (ICompletionProposal) proposals.get(2);
		assertTrue(proposal3.getDisplayString().indexOf("Integer") == -1);
		
		String[] expected= new String[3];
		buf= new StringBuffer();
		buf.append("package pack;\n");
		buf.append("\n");
		buf.append("\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(long o) {\n");
		buf.append("        int i= (int) o;\n");
		buf.append("    }\n");
		buf.append("}\n");
		expected[0]= buf.toString();

		buf= new StringBuffer();
		buf.append("package pack;\n");
		buf.append("\n");
		buf.append("\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(int o) {\n");
		buf.append("        int i= o;\n");
		buf.append("    }\n");
		buf.append("}\n");
		expected[1]= buf.toString();

		buf= new StringBuffer();
		buf.append("package pack;\n");
		buf.append("\n");
		buf.append("\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(long o) {\n");
		buf.append("        long i= o;\n");
		buf.append("    }\n");
		buf.append("}\n");
		expected[2]= buf.toString();

		assertExpectedExistInProposals(proposals, expected);
	}
	
}
