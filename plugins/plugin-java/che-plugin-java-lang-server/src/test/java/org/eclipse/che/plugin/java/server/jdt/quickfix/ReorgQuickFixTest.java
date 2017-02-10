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
package org.eclipse.che.plugin.java.server.jdt.quickfix;


import org.eclipse.che.plugin.java.server.jdt.testplugin.JavaProjectHelper;
import org.eclipse.che.plugin.java.server.jdt.testplugin.ProjectTestSetup;
import org.eclipse.che.plugin.java.server.jdt.testplugin.TestOptions;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.text.correction.AssistContext;
import org.eclipse.jdt.internal.ui.text.correction.ProblemLocation;
import org.eclipse.jdt.internal.ui.text.correction.proposals.CorrectMainTypeNameProposal;
import org.eclipse.jdt.internal.ui.text.correction.proposals.CorrectPackageDeclarationProposal;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jdt.ui.text.java.correction.CUCorrectionProposal;
import org.eclipse.jdt.ui.text.java.correction.ChangeCorrectionProposal;
import org.eclipse.jface.preference.IPreferenceStore;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Hashtable;

import static org.junit.Assert.assertTrue;

public class ReorgQuickFixTest extends QuickFixTest {

	private static final Class THIS= ReorgQuickFixTest.class;

	private IJavaProject         fJProject1;
	private IPackageFragmentRoot fSourceFolder;

	public ReorgQuickFixTest() {
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
		options.put(JavaCore.COMPILER_PB_UNUSED_IMPORT, JavaCore.ERROR);
		JavaCore.setOptions(options);

		IPreferenceStore store = JavaPlugin.getDefault().getPreferenceStore();
		store.setValue(PreferenceConstants.CODEGEN_ADD_COMMENTS, false);

		fJProject1 = ProjectTestSetup.getProject();

		fSourceFolder = JavaProjectHelper.addSourceContainer(fJProject1, "src");
	}


    @After
	public void tearDown() throws Exception {
        super.tearDown();
		JavaProjectHelper.clear(fJProject1, ProjectTestSetup.getDefaultClasspath());
	}

    @Test
	public void testUnusedImports() throws Exception {
		IPackageFragment pack1 = fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.Vector;\n");
		buf.append("public class E {\n");
		buf.append("}\n");
		ICompilationUnit cu = pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot = getASTRoot(cu);
		ArrayList proposals = collectCorrections(cu, astRoot);
		assertNumberOfProposals(proposals, 2);
		assertCorrectLabels(proposals);

		Object p1 = proposals.get(0);
		if (!(p1 instanceof CUCorrectionProposal)) {
			p1 = proposals.get(1);
		}

		CUCorrectionProposal proposal = (CUCorrectionProposal)p1;
		String preview = getPreviewContent(proposal);

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

    @Test
	public void testUnusedImportsInDefaultPackage() throws Exception {
		IPackageFragment pack1 = fSourceFolder.createPackageFragment("", false, null);
		StringBuffer buf = new StringBuffer();
		buf.append("import java.util.Vector;\n");
		buf.append("public class E {\n");
		buf.append("}\n");
		ICompilationUnit cu = pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot = getASTRoot(cu);
		ArrayList proposals = collectCorrections(cu, astRoot);
		assertNumberOfProposals(proposals, 2);
		assertCorrectLabels(proposals);

		Object p1 = proposals.get(0);
		if (!(p1 instanceof CUCorrectionProposal)) {
			p1 = proposals.get(1);
		}

		CUCorrectionProposal proposal = (CUCorrectionProposal)p1;
		String preview = getPreviewContent(proposal);

		buf = new StringBuffer();
		buf.append("\n");
		buf.append("public class E {\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

    @Test
	public void testUnusedImportOnDemand() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("\n");
		buf.append("import java.util.Vector;\n");
		buf.append("import java.net.*;\n");
		buf.append("\n");
		buf.append("public class E {\n");
		buf.append("    Vector v;\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= getASTRoot(cu);
		ArrayList proposals= collectCorrections(cu, astRoot);
		assertNumberOfProposals(proposals, 2);
		assertCorrectLabels(proposals);

		Object p1= proposals.get(0);
		if (!(p1 instanceof CUCorrectionProposal)) {
			p1= proposals.get(1);
		}

		CUCorrectionProposal proposal= (CUCorrectionProposal) p1;
		String preview= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("\n");
		buf.append("import java.util.Vector;\n");
		buf.append("\n");
		buf.append("public class E {\n");
		buf.append("    Vector v;\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

    @Test
	public void testCollidingImports() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("\n");
		buf.append("import java.security.Permission;\n");
		buf.append("import java.security.acl.Permission;\n");
		buf.append("import java.util.Vector;\n");
		buf.append("\n");
		buf.append("public class E {\n");
		buf.append("    Permission p;\n");
		buf.append("    Vector v;\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= getASTRoot(cu);
		ArrayList proposals= collectCorrections(cu, astRoot);
		assertNumberOfProposals(proposals, 2);
		assertCorrectLabels(proposals);

		Object p1= proposals.get(0);
		if (!(p1 instanceof CUCorrectionProposal)) {
			p1= proposals.get(1);
		}

		CUCorrectionProposal proposal= (CUCorrectionProposal) p1;
		String preview= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("\n");
		buf.append("import java.security.Permission;\n");
		buf.append("import java.util.Vector;\n");
		buf.append("\n");
		buf.append("public class E {\n");
		buf.append("    Permission p;\n");
		buf.append("    Vector v;\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

    @Test
	public void testWrongPackageStatement() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test2;\n");
		buf.append("\n");
		buf.append("public class E {\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= getASTRoot(cu);
		ArrayList proposals= collectCorrections(cu, astRoot);
		assertNumberOfProposals(proposals, 2);
		assertCorrectLabels(proposals);

		boolean hasRename= true, hasMove= true;

		for (int i= 0; i < proposals.size(); i++) {
			ChangeCorrectionProposal curr= (ChangeCorrectionProposal) proposals.get(i);
			if (curr instanceof CorrectPackageDeclarationProposal) {
				assertTrue("Duplicated proposal", hasRename);
				hasRename= false;

				CUCorrectionProposal proposal= (CUCorrectionProposal) curr;
				String preview= getPreviewContent(proposal);
				buf= new StringBuffer();
				buf.append("package test1;\n");
				buf.append("\n");
				buf.append("public class E {\n");
				buf.append("}\n");
				assertEqualString(preview, buf.toString());
			} else {
				assertTrue("Duplicated proposal", hasMove);
				hasMove= false;
				curr.apply(null);

				IPackageFragment pack2= fSourceFolder.getPackageFragment("test2");
				ICompilationUnit cu2= pack2.getCompilationUnit("E.java");
				assertTrue("CU does not exist", cu2.exists());
				buf= new StringBuffer();
				buf.append("package test2;\n");
				buf.append("\n");
				buf.append("public class E {\n");
				buf.append("}\n");
				assertEqualStringIgnoreDelim(cu2.getSource(), buf.toString());
			}
		}
	}

    @Test
	public void testWrongPackageStatementInEnum() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test2;\n");
		buf.append("\n");
		buf.append("public enum E {\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= getASTRoot(cu);
		ArrayList proposals= collectCorrections(cu, astRoot);
		assertNumberOfProposals(proposals, 2);
		assertCorrectLabels(proposals);

		boolean hasRename= true, hasMove= true;

		for (int i= 0; i < proposals.size(); i++) {
			ChangeCorrectionProposal curr= (ChangeCorrectionProposal) proposals.get(i);
			if (curr instanceof CorrectPackageDeclarationProposal) {
				assertTrue("Duplicated proposal", hasRename);
				hasRename= false;

				CUCorrectionProposal proposal= (CUCorrectionProposal) curr;
				String preview= getPreviewContent(proposal);
				buf= new StringBuffer();
				buf.append("package test1;\n");
				buf.append("\n");
				buf.append("public enum E {\n");
				buf.append("}\n");
				assertEqualString(preview, buf.toString());
			} else {
				assertTrue("Duplicated proposal", hasMove);
				hasMove= false;
				curr.apply(null);

				IPackageFragment pack2= fSourceFolder.getPackageFragment("test2");
				ICompilationUnit cu2= pack2.getCompilationUnit("E.java");
				assertTrue("CU does not exist", cu2.exists());
				buf= new StringBuffer();
				buf.append("package test2;\n");
				buf.append("\n");
				buf.append("public enum E {\n");
				buf.append("}\n");
				assertEqualStringIgnoreDelim(cu2.getSource(), buf.toString());
			}
		}
	}

    @Test
	public void testWrongPackageStatementFromDefault() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test2;\n");
		buf.append("\n");
		buf.append("public class E {\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= getASTRoot(cu);
		ArrayList proposals= collectCorrections(cu, astRoot);
		assertNumberOfProposals(proposals, 2);
		assertCorrectLabels(proposals);

		boolean hasRename= true, hasMove= true;

		for (int i= 0; i < proposals.size(); i++) {
			ChangeCorrectionProposal curr= (ChangeCorrectionProposal) proposals.get(i);
			if (curr instanceof CorrectPackageDeclarationProposal) {
				assertTrue("Duplicated proposal", hasRename);
				hasRename= false;

				CUCorrectionProposal proposal= (CUCorrectionProposal) curr;
				String preview= getPreviewContent(proposal);
				buf= new StringBuffer();
				buf.append("\n");
				buf.append("\n");
				buf.append("public class E {\n");
				buf.append("}\n");
				assertEqualString(preview, buf.toString());
			} else {
				assertTrue("Duplicated proposal", hasMove);
				hasMove= false;
				curr.apply(null);

				IPackageFragment pack2= fSourceFolder.getPackageFragment("test2");
				ICompilationUnit cu2= pack2.getCompilationUnit("E.java");
				assertTrue("CU does not exist", cu2.exists());
				buf= new StringBuffer();
				buf.append("package test2;\n");
				buf.append("\n");
				buf.append("public class E {\n");
				buf.append("}\n");
				assertEqualStringIgnoreDelim(cu2.getSource(), buf.toString());
			}
		}
	}

    @Test
	public void testWrongDefaultPackageStatement() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test2", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("public class E {\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= getASTRoot(cu);
		ArrayList proposals= collectCorrections(cu, astRoot);
		assertNumberOfProposals(proposals, 2);
		assertCorrectLabels(proposals);

		boolean hasRename= true, hasMove= true;

		for (int i= 0; i < proposals.size(); i++) {
			ChangeCorrectionProposal curr= (ChangeCorrectionProposal) proposals.get(i);
			if (curr instanceof CorrectPackageDeclarationProposal) {
				assertTrue("Duplicated proposal", hasRename);
				hasRename= false;

				CUCorrectionProposal proposal= (CUCorrectionProposal) curr;
				String preview= getPreviewContent(proposal);
				buf= new StringBuffer();
				buf.append("package test2;\n");
				buf.append("\n");
				buf.append("public class E {\n");
				buf.append("}\n");
				assertEqualString(preview, buf.toString());
			} else {
				assertTrue("Duplicated proposal", hasMove);
				hasMove= false;
				curr.apply(null);

				IPackageFragment pack2= fSourceFolder.getPackageFragment("");
				ICompilationUnit cu2= pack2.getCompilationUnit("E.java");
				assertTrue("CU does not exist", cu2.exists());
				buf= new StringBuffer();
				buf.append("public class E {\n");
				buf.append("}\n");
				assertEqualStringIgnoreDelim(cu2.getSource(), buf.toString());
			}
		}
	}

    @Test
	public void testWrongPackageStatementButColliding() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test2;\n");
		buf.append("\n");
		buf.append("public class E {\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		IPackageFragment pack2= fSourceFolder.createPackageFragment("test2", false, null);
		buf.append("package test2;\n");
		buf.append("\n");
		buf.append("public class E {\n");
		buf.append("}\n");
		pack2.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= getASTRoot(cu);
		ArrayList proposals= collectCorrections(cu, astRoot);
		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);

		CUCorrectionProposal proposal= (CUCorrectionProposal) proposals.get(0);
		String preview= getPreviewContent(proposal);
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("\n");
		buf.append("public class E {\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

    @Test
	public void testWrongTypeName() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("\n");
		buf.append("public class E {\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		CompilationUnit astRoot= getASTRoot(cu);
		ArrayList proposals= collectCorrections(cu, astRoot);
		assertNumberOfProposals(proposals, 2);
		assertCorrectLabels(proposals);

		boolean hasRename= true, hasMove= true;

		for (int i= 0; i < proposals.size(); i++) {
			ChangeCorrectionProposal curr= (ChangeCorrectionProposal) proposals.get(i);
			if (curr instanceof CorrectMainTypeNameProposal) {
				assertTrue("Duplicated proposal", hasRename);
				hasRename= false;

				CUCorrectionProposal proposal= (CUCorrectionProposal) curr;
				String preview= getPreviewContent(proposal);
				buf= new StringBuffer();
				buf.append("package test1;\n");
				buf.append("\n");
				buf.append("public class X {\n");
				buf.append("}\n");
				assertEqualString(preview, buf.toString());
			} else {
				assertTrue("Duplicated proposal", hasMove);
				hasMove= false;
				curr.apply(null);

				ICompilationUnit cu2= pack1.getCompilationUnit("E.java");
				assertTrue("CU does not exist", cu2.exists());
				buf= new StringBuffer();
				buf.append("package test1;\n");
				buf.append("\n");
				buf.append("public class E {\n");
				buf.append("}\n");
				assertEqualStringIgnoreDelim(cu2.getSource(), buf.toString());
			}
		}
	}

    @Test
	public void testWrongTypeName_bug180330() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("p", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package p;\n");
		buf.append("public class \\u0042 {\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		CompilationUnit astRoot= getASTRoot(cu);
		ArrayList proposals= collectCorrections(cu, astRoot);

		assertCorrectLabels(proposals);
		assertNumberOfProposals(proposals, 2);

		boolean hasRename= true, hasMove= true;

		for (int i= 0; i < proposals.size(); i++) {
			ChangeCorrectionProposal curr= (ChangeCorrectionProposal) proposals.get(i);
			if (curr instanceof CorrectMainTypeNameProposal) {
				assertTrue("Duplicated proposal", hasRename);
				hasRename= false;

				CUCorrectionProposal proposal= (CUCorrectionProposal) curr;
				String preview= getPreviewContent(proposal);
				buf= new StringBuffer();
				buf.append("package p;\n");
				buf.append("public class C {\n");
				buf.append("}\n");
				assertEqualString(preview, buf.toString());
			} else {
				assertTrue("Duplicated proposal", hasMove);
				hasMove= false;
				curr.apply(null);

				ICompilationUnit cu2= pack1.getCompilationUnit("B.java");
				assertTrue("CU does not exist", cu2.exists());
				buf= new StringBuffer();
				buf.append("package p;\n");
				buf.append("public class \\u0042 {\n");
				buf.append("}\n");
				assertEqualStringIgnoreDelim(cu2.getSource(), buf.toString());
			}
		}
	}

    @Test
	public void testWrongTypeNameButColliding() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("\n");
		buf.append("public class X {\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		buf.append("package test1;\n");
		buf.append("\n");
		buf.append("public class X {\n");
		buf.append("}\n");
		pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		CompilationUnit astRoot= getASTRoot(cu);
		ArrayList proposals= collectCorrections(cu, astRoot);
		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);

		CUCorrectionProposal proposal= (CUCorrectionProposal) proposals.get(0);

		String preview= getPreviewContent(proposal);
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("\n");
		buf.append("public class E {\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

    @Test
	public void testWrongTypeNameWithConstructor() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("\n");
		buf.append("public class X {\n");
		buf.append("    public X() {\n");
		buf.append("        X other;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		buf.append("package test1;\n");
		buf.append("\n");
		buf.append("public class X {\n");
		buf.append("}\n");
		pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		CompilationUnit astRoot= getASTRoot(cu);
		ArrayList proposals= collectCorrections(cu, astRoot);
		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);

		CUCorrectionProposal proposal= (CUCorrectionProposal) proposals.get(0);

		String preview= getPreviewContent(proposal);
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("\n");
		buf.append("public class E {\n");
		buf.append("    public E() {\n");
		buf.append("        E other;\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

    @Test
	public void testWrongTypeNameInEnum() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("\n");
		buf.append("public enum X {\n");
		buf.append("    A;\n");
		buf.append("    X() {\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		buf.append("package test1;\n");
		buf.append("\n");
		buf.append("public class X {\n");
		buf.append("}\n");
		pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		CompilationUnit astRoot= getASTRoot(cu);
		ArrayList proposals= collectCorrections(cu, astRoot);
		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);

		CUCorrectionProposal proposal= (CUCorrectionProposal) proposals.get(0);

		String preview= getPreviewContent(proposal);
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("\n");
		buf.append("public enum E {\n");
		buf.append("    A;\n");
		buf.append("    E() {\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

    @Test
	public void testWrongTypeNameInAnnot() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("\n");
		buf.append("public @interface X {\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		buf.append("package test1;\n");
		buf.append("\n");
		buf.append("public @interface X {\n");
		buf.append("}\n");
		pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		CompilationUnit astRoot= getASTRoot(cu);
		ArrayList proposals= collectCorrections(cu, astRoot);
		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);

		CUCorrectionProposal proposal= (CUCorrectionProposal) proposals.get(0);

		String preview= getPreviewContent(proposal);
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("\n");
		buf.append("public @interface E {\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

    @Test
	public void testTodoTasks1() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        // TODO: XXX\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		String str= "TODO: XXX";
		AssistContext context= getCorrectionContext(cu, buf.toString().indexOf(str), 0);
		ProblemLocation problem= new ProblemLocation(buf.toString().indexOf(str), str.length(), IProblem.Task, new String[0], true, IJavaModelMarker.TASK_MARKER);
		ArrayList proposals= collectCorrections(context, problem);



		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);

		CUCorrectionProposal proposal= (CUCorrectionProposal) proposals.get(0);
		String preview= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

    @Test
	public void testTodoTasks2() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        // Some other text TODO: XXX\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		String str= "TODO: XXX";
		AssistContext context= getCorrectionContext(cu, buf.toString().indexOf(str), 0);
		ProblemLocation problem= new ProblemLocation(buf.toString().indexOf(str), str.length(), IProblem.Task, new String[0], true, IJavaModelMarker.TASK_MARKER);
		ArrayList proposals= collectCorrections(context, problem);

		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);

		CUCorrectionProposal proposal= (CUCorrectionProposal) proposals.get(0);
		String preview= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        // Some other text \n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

    @Test
	public void testTodoTasks3() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        /* TODO: XXX */\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		String str= "TODO: XXX";
		AssistContext context= getCorrectionContext(cu, buf.toString().indexOf(str), 0);
		ProblemLocation problem= new ProblemLocation(buf.toString().indexOf(str), str.length(), IProblem.Task, new String[0], true, IJavaModelMarker.TASK_MARKER);
		ArrayList proposals= collectCorrections(context, problem);

		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);

		CUCorrectionProposal proposal= (CUCorrectionProposal) proposals.get(0);
		String preview= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

    @Test
	public void testTodoTasks4() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        /**\n");
		buf.append("        TODO: XXX\n");
		buf.append("        */\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		String str= "TODO: XXX";
		AssistContext context= getCorrectionContext(cu, buf.toString().indexOf(str), 0);
		ProblemLocation problem= new ProblemLocation(buf.toString().indexOf(str), str.length(), IProblem.Task, new String[0], true, IJavaModelMarker.TASK_MARKER);
		ArrayList proposals= collectCorrections(context, problem);

		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);

		CUCorrectionProposal proposal= (CUCorrectionProposal) proposals.get(0);
		String preview= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

    @Test
	public void testTodoTasks5() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        /**\n");
		buf.append("        Some other text: TODO: XXX\n");
		buf.append("        */\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		String str= "TODO: XXX";
		AssistContext context= getCorrectionContext(cu, buf.toString().indexOf(str), 0);
		ProblemLocation problem= new ProblemLocation(buf.toString().indexOf(str), str.length(), IProblem.Task, new String[0], true, IJavaModelMarker.TASK_MARKER);
		ArrayList proposals= collectCorrections(context, problem);

		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);

		CUCorrectionProposal proposal= (CUCorrectionProposal) proposals.get(0);
		String preview= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        /**\n");
		buf.append("        Some other text: \n");
		buf.append("        */\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

    @Test
	public void testTodoTasks6() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        ;// TODO: XXX\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		String str= "TODO: XXX";
		AssistContext context= getCorrectionContext(cu, buf.toString().indexOf(str), 0);
		ProblemLocation problem= new ProblemLocation(buf.toString().indexOf(str), str.length(), IProblem.Task, new String[0], true, IJavaModelMarker.TASK_MARKER);
		ArrayList proposals= collectCorrections(context, problem);

		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);

		CUCorrectionProposal proposal= (CUCorrectionProposal) proposals.get(0);
		String preview= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        ;\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

    @Test
	public void testTodoTasks7() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        /* TODO: XXX*/;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		String str= "TODO: XXX";
		AssistContext context= getCorrectionContext(cu, buf.toString().indexOf(str), 0);
		ProblemLocation problem= new ProblemLocation(buf.toString().indexOf(str), str.length(), IProblem.Task, new String[0], true, IJavaModelMarker.TASK_MARKER);
		ArrayList proposals= collectCorrections(context, problem);

		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);

		CUCorrectionProposal proposal= (CUCorrectionProposal) proposals.get(0);
		String preview= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        ;\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

//    @Test
//	public void testAddToClasspathSourceFolder() throws Exception {
//		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
//		StringBuffer buf= new StringBuffer();
//		buf.append("package test1;\n");
//		buf.append("import mylib.Foo;\n");
//		buf.append("public class E {\n");
//		buf.append("}\n");
//		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);
//		IClasspathEntry[] prevClasspath= cu.getJavaProject().getRawClasspath();
//
//		IJavaProject otherProject= JavaProjectHelper.createJavaProject("other", "bin");
//		try {
//			IPackageFragmentRoot otherRoot= JavaProjectHelper.addSourceContainer(otherProject, "src");
//			IPackageFragment otherPack= otherRoot.createPackageFragment("mylib", false, null);
//			buf= new StringBuffer();
//			buf.append("package mylib;\n");
//			buf.append("public class Foo {\n");
//			buf.append("}\n");
//			otherPack.createCompilationUnit("Foo.java", buf.toString(), false, null);
//
//			MultiStatus status= new MultiStatus(JavaUI.ID_PLUGIN, IStatus.OK, "", null);
//			ClasspathFixProposal[] proposals= ClasspathFixProcessorDescriptor.getProposals(cu.getJavaProject(), "mylib.Foo", status);
//			assertEquals(1, proposals.length);
//			assertTrue(status.isOK());
//
//			assertAddedClassPathEntry(proposals[0], otherProject.getPath(), cu, prevClasspath);
//
//		} finally {
//			JavaProjectHelper.delete(otherProject);
//		}
//	}
//
//	public void testAddToClasspathIntJAR() throws Exception {
//		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
//		StringBuffer buf= new StringBuffer();
//		buf.append("package test1;\n");
//		buf.append("import mylib.Foo;\n");
//		buf.append("public class E {\n");
//		buf.append("}\n");
//		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);
//		IClasspathEntry[] prevClasspath= cu.getJavaProject().getRawClasspath();
//
//		IJavaProject otherProject= JavaProjectHelper.createJavaProject("other", "bin");
//		try {
//			File lib= JavaTestPlugin.getDefault().getFileInPlugin(JavaProjectHelper.MYLIB);
//			assertTrue("lib does not exist",  lib != null && lib.exists());
//			IPackageFragmentRoot otherRoot= JavaProjectHelper.addLibraryWithImport(otherProject, Path.fromOSString(lib.getPath()), null, null);
//
//			MultiStatus status= new MultiStatus(JavaUI.ID_PLUGIN, IStatus.OK, "", null);
//			ClasspathFixProposal[] proposals= ClasspathFixProcessorDescriptor.getProposals(cu.getJavaProject(), "mylib.Foo", status);
//			assertEquals(1, proposals.length);
//			assertTrue(status.isOK());
//
//			assertAddedClassPathEntry(proposals[0], otherRoot.getPath(), cu, prevClasspath);
//		} finally {
//			JavaProjectHelper.delete(otherProject);
//		}
//	}
//
//	private void assertAddedClassPathEntry(ClasspathFixProposal curr, IPath addedPath, ICompilationUnit cu, IClasspathEntry[] prevClasspath) throws CoreException {
//		new PerformChangeOperation(curr.createChange(null)).run(null);
//
//		IClasspathEntry[] newClasspath= cu.getJavaProject().getRawClasspath();
//		assertEquals(prevClasspath.length + 1, newClasspath.length);
//		assertEquals(addedPath, newClasspath[prevClasspath.length].getPath());
//	}
//
//
//	public void testAddToClasspathExportedExtJAR() throws Exception {
//		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
//		StringBuffer buf= new StringBuffer();
//		buf.append("package test1;\n");
//		buf.append("import mylib.Foo;\n");
//		buf.append("public class E {\n");
//		buf.append("}\n");
//		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);
//		IClasspathEntry[] prevClasspath= cu.getJavaProject().getRawClasspath();
//
//		IJavaProject otherProject= JavaProjectHelper.createJavaProject("other", "bin");
//		try {
//			File lib= JavaTestPlugin.getDefault().getFileInPlugin(JavaProjectHelper.MYLIB);
//			assertTrue("lib does not exist",  lib != null && lib.exists());
//
//			IPath path= Path.fromOSString(lib.getPath());
//
//			// exported external JAR
//			IClasspathEntry entry= JavaCore.newLibraryEntry(path, null, null, true);
//			JavaProjectHelper.addToClasspath(otherProject, entry);
//
//			MultiStatus status= new MultiStatus(JavaUI.ID_PLUGIN, IStatus.OK, "", null);
//			ClasspathFixProposal[] proposals= ClasspathFixProcessorDescriptor.getProposals(cu.getJavaProject(), "mylib.Foo", status);
//			assertEquals(2, proposals.length);
//			assertTrue(status.isOK());
//
//			assertAddedClassPathEntry(proposals[0], otherProject.getPath(), cu, prevClasspath);
//			assertAddedClassPathEntry(proposals[1], path, cu, prevClasspath);
//
//		} finally {
//			JavaProjectHelper.delete(otherProject);
//		}
//	}
//
//	public void testAddToClasspathContainer() throws Exception {
//		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
//		StringBuffer buf= new StringBuffer();
//		buf.append("package test1;\n");
//		buf.append("import mylib.Foo;\n");
//		buf.append("public class E {\n");
//		buf.append("}\n");
//		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);
//		IClasspathEntry[] prevClasspath= cu.getJavaProject().getRawClasspath();
//
//		IJavaProject otherProject= JavaProjectHelper.createJavaProject("other", "bin");
//		try {
//			File lib= JavaTestPlugin.getDefault().getFileInPlugin(JavaProjectHelper.MYLIB);
//			assertTrue("lib does not exist",  lib != null && lib.exists());
//			IPath path= Path.fromOSString(lib.getPath());
//			final IClasspathEntry[] entries= { JavaCore.newLibraryEntry(path, null, null) };
//			final IPath containerPath= new Path(JavaCore.USER_LIBRARY_CONTAINER_ID).append("MyUserLibrary");
//
//
//			IClasspathContainer newContainer= new IClasspathContainer() {
//				public IClasspathEntry[] getClasspathEntries() {
//					return entries;
//				}
//
//				public String getDescription() {
//					return "MyUserLibrary";
//				}
//
//				public int getKind() {
//					return IClasspathContainer.K_APPLICATION;
//				}
//
//				public IPath getPath() {
//					return containerPath;
//				}
//			};
//			ClasspathContainerInitializer initializer= JavaCore.getClasspathContainerInitializer(JavaCore.USER_LIBRARY_CONTAINER_ID);
//			initializer.requestClasspathContainerUpdate(containerPath, otherProject, newContainer);
//
//			IClasspathEntry entry= JavaCore.newContainerEntry(containerPath);
//			JavaProjectHelper.addToClasspath(otherProject, entry);
//
//			MultiStatus status= new MultiStatus(JavaUI.ID_PLUGIN, IStatus.OK, "", null);
//			ClasspathFixProposal[] proposals= ClasspathFixProcessorDescriptor.getProposals(cu.getJavaProject(), "mylib.Foo", status);
//			assertEquals(1, proposals.length);
//			assertTrue(status.isOK());
//
//			assertAddedClassPathEntry(proposals[0], containerPath, cu, prevClasspath);
//		} finally {
//			JavaProjectHelper.delete(otherProject);
//		}
//	}




}
