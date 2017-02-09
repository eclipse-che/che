/*******************************************************************************
 * Copyright (c) 2011, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.java.server.jdt.quickfix;


import org.eclipse.che.plugin.java.server.jdt.testplugin.Java17ProjectTestSetup;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jdt.internal.corext.codemanipulation.StubUtility;
import org.eclipse.jdt.internal.corext.template.java.CodeTemplateContextType;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.text.correction.CorrectionMessages;
import org.eclipse.che.plugin.java.server.jdt.testplugin.JavaProjectHelper;
import org.eclipse.che.plugin.java.server.jdt.testplugin.TestOptions;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Hashtable;

public class ModifierCorrectionsQuickFixTest17 extends QuickFixTest {

	private static final Class THIS= ModifierCorrectionsQuickFixTest17.class;

	private IJavaProject fJProject1;

	private IPackageFragmentRoot fSourceFolder;

	public ModifierCorrectionsQuickFixTest17() {
		super(new Java17ProjectTestSetup());
	}

//	public static Test suite() {
//		return setUpTest(new TestSuite(THIS));
//	}
//
//	public static Test setUpTest(Test test) {
//		return new Java17ProjectTestSetup(test);
//	}

	@Before
	public void setUp() throws Exception {
        super.setUp();
		Hashtable options = TestOptions.getDefaultOptions();
		options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, JavaCore.SPACE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, "4");
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_AFTER_ANNOTATION_ON_LOCAL_VARIABLE, JavaCore.DO_NOT_INSERT);


		JavaCore.setOptions(options);

		IPreferenceStore store = JavaPlugin.getDefault().getPreferenceStore();
		store.setValue(PreferenceConstants.CODEGEN_ADD_COMMENTS, false);

		fJProject1 = Java17ProjectTestSetup.getProject();

		StubUtility.setCodeTemplate(CodeTemplateContextType.METHODSTUB_ID, "", null);
		StubUtility.setCodeTemplate(CodeTemplateContextType.CONSTRUCTORSTUB_ID, "", null);

		fSourceFolder = JavaProjectHelper.addSourceContainer(fJProject1, "src");
	}


    @After
	public void tearDown() throws Exception {
        super.tearDown();
		JavaProjectHelper.clear(fJProject1, Java17ProjectTestSetup.getDefaultClasspath());
	}

    @Test
	public void testAddSafeVarargs1() throws Exception {
		IPackageFragment pack1 = fSourceFolder.createPackageFragment("p", false, null);
		StringBuffer buf = new StringBuffer();
		buf.append("package p;\n");
		buf.append("import java.util.List;\n");
		buf.append("public class E {\n");
		buf.append("    public static <T> List<T> asList(T ... a) {\n");
		buf.append("        return null;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu = pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot = getASTRoot(cu);
		ArrayList proposals= collectCorrections(cu, astRoot);

		assertCorrectLabels(proposals);
		assertNumberOfProposals(proposals, 3);

		String[] expected= new String[1];
		buf= new StringBuffer();
		buf.append("package p;\n");
		buf.append("import java.util.List;\n");
		buf.append("public class E {\n");
		buf.append("    @SafeVarargs\n");
		buf.append("    public static <T> List<T> asList(T ... a) {\n");
		buf.append("        return null;\n");
		buf.append("    }\n");
		buf.append("}\n");
		expected[0]= buf.toString();

		assertExpectedExistInProposals(proposals, expected);
	}

    @Test
	public void testAddSafeVarargs2() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("p", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package p;\n");
		buf.append("import java.util.List;\n");
		buf.append("public class E {\n");
		buf.append("    public final <T> List<T> asList(T ... a) {\n");
		buf.append("        return null;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= getASTRoot(cu);
		ArrayList proposals= collectCorrections(cu, astRoot);

		assertCorrectLabels(proposals);
		assertNumberOfProposals(proposals, 3);

		String[] expected= new String[1];
		buf= new StringBuffer();
		buf.append("package p;\n");
		buf.append("import java.util.List;\n");
		buf.append("public class E {\n");
		buf.append("    @SafeVarargs\n");
		buf.append("    public final <T> List<T> asList(T ... a) {\n");
		buf.append("        return null;\n");
		buf.append("    }\n");
		buf.append("}\n");
		expected[0]= buf.toString();

		assertExpectedExistInProposals(proposals, expected);
	}

    @Test
	public void testAddSafeVarargs3() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("p", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package p;\n");
		buf.append("import java.util.List;\n");
		buf.append("public class E {\n");
		buf.append("    @Deprecated\n");
		buf.append("    public static <T> List<T> asList(T ... a) {\n");
		buf.append("        return null;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= getASTRoot(cu);
		ArrayList proposals= collectCorrections(cu, astRoot);

		assertCorrectLabels(proposals);
		assertNumberOfProposals(proposals, 3);

		String[] expected= new String[1];
		buf= new StringBuffer();
		buf.append("package p;\n");
		buf.append("import java.util.List;\n");
		buf.append("public class E {\n");
		buf.append("    @SafeVarargs\n");
		buf.append("    @Deprecated\n");
		buf.append("    public static <T> List<T> asList(T ... a) {\n");
		buf.append("        return null;\n");
		buf.append("    }\n");
		buf.append("}\n");
		expected[0]= buf.toString();

		assertExpectedExistInProposals(proposals, expected);
	}

    @Test
	public void testAddSafeVarargs4() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("p", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package p;\n");
		buf.append("public class E {\n");
		buf.append("    public <T> E(T ... a) {\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= getASTRoot(cu);
		ArrayList proposals= collectCorrections(cu, astRoot);

		assertCorrectLabels(proposals);
		assertNumberOfProposals(proposals, 3);

		String[] expected= new String[1];
		buf= new StringBuffer();
		buf.append("package p;\n");
		buf.append("public class E {\n");
		buf.append("    @SafeVarargs\n");
		buf.append("    public <T> E(T ... a) {\n");
		buf.append("    }\n");
		buf.append("}\n");
		expected[0]= buf.toString();

		assertExpectedExistInProposals(proposals, expected);
	}

    @Test
	public void testAddSafeVarargs5() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("p", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package p;\n");
		buf.append("import java.util.List;\n");
		buf.append("public class E {\n");
		buf.append("    public <T> List<T> asList(T ... a) {\n");
		buf.append("        return null;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= getASTRoot(cu);
		ArrayList proposals= collectCorrections(cu, astRoot);

		assertCorrectLabels(proposals);
		assertNumberOfProposals(proposals, 2);

		assertProposalDoesNotExist(proposals, CorrectionMessages.VarargsWarningsSubProcessor_add_safevarargs_label);
	}

    @Test
	public void testAddSafeVarargsToDeclaration1() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("p", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package p;\n");
		buf.append("import java.util.List;\n");
		buf.append("public class E {\n");
		buf.append("    void foo() {\n");
		buf.append("        Y.asList(Y.asList(\"Hello\", \" World\"));\n");
		buf.append("    }\n");
		buf.append("}\n");
		buf.append("class Y {\n");
		buf.append("    public static <T> List<T> asList(T... a) {\n");
		buf.append("        return null;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= getASTRoot(cu);
		ArrayList proposals= collectCorrections(cu, astRoot, 2);

		assertCorrectLabels(proposals);
		assertNumberOfProposals(proposals, 2);

		String[] expected= new String[1];
		buf= new StringBuffer();
		buf.append("package p;\n");
		buf.append("import java.util.List;\n");
		buf.append("public class E {\n");
		buf.append("    void foo() {\n");
		buf.append("        Y.asList(Y.asList(\"Hello\", \" World\"));\n");
		buf.append("    }\n");
		buf.append("}\n");
		buf.append("class Y {\n");
		buf.append("    @SafeVarargs\n");
		buf.append("    public static <T> List<T> asList(T... a) {\n");
		buf.append("        return null;\n");
		buf.append("    }\n");
		buf.append("}\n");
		expected[0]= buf.toString();

		assertExpectedExistInProposals(proposals, expected);
	}

    @Test
	public void testAddSafeVarargsToDeclaration2() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("p", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package p;\n");
		buf.append("import java.util.List;\n");
		buf.append("public class E {\n");
		buf.append("    void foo() {\n");
		buf.append("        Y.asList(Y.asList(\"Hello\", \" World\"));\n");
		buf.append("    }\n");
		buf.append("}\n");
		buf.append("class Y {\n");
		buf.append("    @Deprecated\n");
		buf.append("    public static <T> List<T> asList(T... a) {\n");
		buf.append("        return null;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= getASTRoot(cu);
		ArrayList proposals= collectCorrections(cu, astRoot, 2);

		assertCorrectLabels(proposals);
		assertNumberOfProposals(proposals, 2);

		String[] expected= new String[1];
		buf= new StringBuffer();
		buf.append("package p;\n");
		buf.append("import java.util.List;\n");
		buf.append("public class E {\n");
		buf.append("    void foo() {\n");
		buf.append("        Y.asList(Y.asList(\"Hello\", \" World\"));\n");
		buf.append("    }\n");
		buf.append("}\n");
		buf.append("class Y {\n");
		buf.append("    @SafeVarargs\n");
		buf.append("    @Deprecated\n");
		buf.append("    public static <T> List<T> asList(T... a) {\n");
		buf.append("        return null;\n");
		buf.append("    }\n");
		buf.append("}\n");
		expected[0]= buf.toString();

		assertExpectedExistInProposals(proposals, expected);
	}

    @Test
	public void testAddSafeVarargsToDeclaration3() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("p", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package p;\n");
		buf.append("public class E {\n");
		buf.append("    void foo() {\n");
		buf.append("        Y.asList(Y.asList(\"Hello\", \" World\"));\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		buf= new StringBuffer();
		buf.append("package p;\n");
		buf.append("import java.util.List;\n");
		buf.append("class Y {\n");
		buf.append("    public static <T> List<T> asList(T... a) {\n");
		buf.append("        return null;\n");
		buf.append("    }\n");
		buf.append("}\n");
		pack1.createCompilationUnit("Y.java", buf.toString(), false, null);

		CompilationUnit astRoot= getASTRoot(cu);
		ArrayList proposals= collectCorrections(cu, astRoot, 1);

		assertCorrectLabels(proposals);
		assertNumberOfProposals(proposals, 2);

		String[] expected= new String[1];
		buf= new StringBuffer();
		buf.append("package p;\n");
		buf.append("import java.util.List;\n");
		buf.append("class Y {\n");
		buf.append("    @SafeVarargs\n");
		buf.append("    public static <T> List<T> asList(T... a) {\n");
		buf.append("        return null;\n");
		buf.append("    }\n");
		buf.append("}\n");
		expected[0]= buf.toString();

		assertExpectedExistInProposals(proposals, expected);
	}

    @Test
	public void testAddSafeVarargsToDeclaration4() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("p", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package p;\n");
		buf.append("import java.util.List;\n");
		buf.append("public class E {\n");
		buf.append("    void foo() {\n");
		buf.append("        new Y(Y.asList(\"Hello\", \" World\"));\n");
		buf.append("    }\n");
		buf.append("}\n");
		buf.append("class Y {\n");
		buf.append("    @SafeVarargs\n");
		buf.append("    public static <T> List<T> asList(T... a) {\n");
		buf.append("        return null;\n");
		buf.append("    }\n");
		buf.append("    public <T> Y(T ... a) {\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= getASTRoot(cu);
		ArrayList proposals= collectCorrections(cu, astRoot, 2);

		assertCorrectLabels(proposals);
		assertNumberOfProposals(proposals, 2);

		String[] expected= new String[1];
		buf= new StringBuffer();
		buf.append("package p;\n");
		buf.append("import java.util.List;\n");
		buf.append("public class E {\n");
		buf.append("    void foo() {\n");
		buf.append("        new Y(Y.asList(\"Hello\", \" World\"));\n");
		buf.append("    }\n");
		buf.append("}\n");
		buf.append("class Y {\n");
		buf.append("    @SafeVarargs\n");
		buf.append("    public static <T> List<T> asList(T... a) {\n");
		buf.append("        return null;\n");
		buf.append("    }\n");
		buf.append("    @SafeVarargs\n");
		buf.append("    public <T> Y(T ... a) {\n");
		buf.append("    }\n");
		buf.append("}\n");
		expected[0]= buf.toString();

		assertExpectedExistInProposals(proposals, expected);
	}

    @Test
	public void testAddSafeVarargsToDeclaration5() throws Exception {
		JavaProjectHelper.set15CompilerOptions(fJProject1);
		try {
			IPackageFragment pack1= fSourceFolder.createPackageFragment("p", false, null);
			StringBuffer buf= new StringBuffer();
			buf.append("package p;\n");
			buf.append("import java.util.List;\n");
			buf.append("public class E {\n");
			buf.append("    void foo() {\n");
			buf.append("        Y.asList(Y.asList(\"Hello\", \" World\"));\n");
			buf.append("    }\n");
			buf.append("}\n");
			buf.append("class Y {\n");
			buf.append("    public static <T> List<T> asList(T... a) {\n");
			buf.append("        return null;\n");
			buf.append("    }\n");
			buf.append("}\n");
			ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

			CompilationUnit astRoot= getASTRoot(cu);
			ArrayList proposals= collectCorrections(cu, astRoot);
			assertNumberOfProposals(proposals, 1);

			assertProposalDoesNotExist(proposals, "Add @SafeVarargs to 'asList(..)'");
		} finally {
			JavaProjectHelper.set17CompilerOptions(fJProject1);
		}
	}

    @Test
	public void testRemoveSafeVarargs1() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("p", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package p;\n");
		buf.append("import java.util.List;\n");
		buf.append("public class E {\n");
		buf.append("    @SafeVarargs\n");
		buf.append("    public static <T> List<T> asList() {\n");
		buf.append("        return null;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= getASTRoot(cu);
		ArrayList proposals= collectCorrections(cu, astRoot);

		assertCorrectLabels(proposals);
		assertNumberOfProposals(proposals, 1);

		String[] expected= new String[1];
		buf= new StringBuffer();
		buf.append("package p;\n");
		buf.append("import java.util.List;\n");
		buf.append("public class E {\n");
		buf.append("    public static <T> List<T> asList() {\n");
		buf.append("        return null;\n");
		buf.append("    }\n");
		buf.append("}\n");
		expected[0]= buf.toString();

		assertExpectedExistInProposals(proposals, expected);
	}

    @Test
	public void testRemoveSafeVarargs2() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("p", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package p;\n");
		buf.append("import java.util.List;\n");
		buf.append("public class E {\n");
		buf.append("    @SafeVarargs\n");
		buf.append("    public <T> List<T> asList2(T... a) {\n");
		buf.append("        return null;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= getASTRoot(cu);
		ArrayList proposals= collectCorrections(cu, astRoot);

		assertCorrectLabels(proposals);
		assertNumberOfProposals(proposals, 1);

		String[] expected= new String[1];
		buf= new StringBuffer();
		buf.append("package p;\n");
		buf.append("import java.util.List;\n");
		buf.append("public class E {\n");
		buf.append("    public <T> List<T> asList2(T... a) {\n");
		buf.append("        return null;\n");
		buf.append("    }\n");
		buf.append("}\n");
		expected[0]= buf.toString();

		assertExpectedExistInProposals(proposals, expected);
	}

    @Test
	public void testRemoveSafeVarargs3() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("p", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package p;\n");
		buf.append("import java.util.List;\n");
		buf.append("public class E {\n");
		buf.append("    @SafeVarargs\n");
		buf.append("    @Deprecated\n");
		buf.append("    public <T> List<T> asList2(T... a) {\n");
		buf.append("        return null;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= getASTRoot(cu);
		ArrayList proposals= collectCorrections(cu, astRoot);

		assertCorrectLabels(proposals);
		assertNumberOfProposals(proposals, 1);

		String[] expected= new String[1];
		buf= new StringBuffer();
		buf.append("package p;\n");
		buf.append("import java.util.List;\n");
		buf.append("public class E {\n");
		buf.append("    @Deprecated\n");
		buf.append("    public <T> List<T> asList2(T... a) {\n");
		buf.append("        return null;\n");
		buf.append("    }\n");
		buf.append("}\n");
		expected[0]= buf.toString();

		assertExpectedExistInProposals(proposals, expected);
	}

}
