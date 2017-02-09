/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Konstantin Scheglov (scheglov_ke@nlmk.ru) - initial API and implementation
 *          (reports 71244 & 74746: New Quick Assist's [quick assist])
 *   Benjamin Muskalla (buskalla@innoopract.com) - 104021: [quick fix] Introduce
 *   		new local with casted type applied more than once
 *   Billy Huang <billyhuang31@gmail.com> - [quick assist] concatenate/merge string literals - https://bugs.eclipse.org/77632
 *******************************************************************************/
package org.eclipse.che.plugin.java.server.jdt.quickfix;

import org.eclipse.che.plugin.java.server.jdt.testplugin.JavaProjectHelper;
import org.eclipse.che.plugin.java.server.jdt.testplugin.ProjectTestSetup;
import org.eclipse.che.plugin.java.server.jdt.testplugin.TestOptions;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jdt.internal.corext.codemanipulation.StubUtility;
import org.eclipse.jdt.internal.corext.template.java.CodeTemplateContextType;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.text.correction.AssistContext;
import org.eclipse.jdt.internal.ui.text.correction.CorrectionMessages;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jdt.ui.text.java.correction.CUCorrectionProposal;
import org.eclipse.jface.preference.IPreferenceStore;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

public class AdvancedQuickAssistTest extends QuickFixTest {

	private IJavaProject         fJProject1;
	private IPackageFragmentRoot fSourceFolder;

	public AdvancedQuickAssistTest() {
		super(new ProjectTestSetup());
	}



	@Before
	public void setUp() throws Exception {
        super.setUp();
		Hashtable options = TestOptions.getDefaultOptions();
		options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, JavaCore.SPACE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, "4");

		JavaCore.setOptions(options);

		IPreferenceStore store = JavaPlugin.getDefault().getPreferenceStore();
		store.setValue(PreferenceConstants.CODEGEN_ADD_COMMENTS, false);
		store.setValue(PreferenceConstants.CODEGEN_KEYWORD_THIS, false);

		StubUtility.setCodeTemplate(CodeTemplateContextType.METHODSTUB_ID, "//TODO\n${body_statement}", null);

//		Preferences corePrefs = JavaPlugin.getJavaCorePluginPreferences();
//		corePrefs.setValue(JavaCore.CODEASSIST_FIELD_PREFIXES, "");
//		corePrefs.setValue(JavaCore.CODEASSIST_STATIC_FIELD_PREFIXES, "");
//		corePrefs.setValue(JavaCore.CODEASSIST_FIELD_SUFFIXES, "");
//		corePrefs.setValue(JavaCore.CODEASSIST_STATIC_FIELD_SUFFIXES, "");

		fJProject1 = ProjectTestSetup.getProject();

		fSourceFolder = JavaProjectHelper.addSourceContainer(fJProject1, "src");
	}


    @After
	public void tearDown() throws Exception {
        super.tearDown();
		JavaProjectHelper.clear(fJProject1, ProjectTestSetup.getDefaultClasspath());
	}

    @Test
	public void testSplitIfCondition1() throws Exception {
		IPackageFragment pack1 = fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(boolean a, int b) {\n");
		buf.append("        if (a && (b == 0)) {\n");
		buf.append("            b= 9;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu = pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset = buf.toString().indexOf("&&");
		AssistContext context = getCorrectionContext(cu, offset, 0);
		List proposals = collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(boolean a, int b) {\n");
		buf.append("        if (a) {\n");
		buf.append("            if (b == 0) {\n");
		buf.append("                b= 9;\n");
		buf.append("            }\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] {expected1});

	}

    @Test
	public void testSplitIfCondition2() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(boolean a, int b, boolean c) {\n");
		buf.append("        if (a && (b == 0) && c) {\n");
		buf.append("            b= 9;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("&& (");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(boolean a, int b, boolean c) {\n");
		buf.append("        if (a) {\n");
		buf.append("            if (b == 0 && c) {\n");
		buf.append("                b= 9;\n");
		buf.append("            }\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] {expected1});

	}

    @Test
	public void testSplitIfCondition3() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(boolean a, int b, boolean c) {\n");
		buf.append("        if (a && (b == 0) && c) {\n");
		buf.append("            b= 9;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("&& c");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(boolean a, int b, boolean c) {\n");
		buf.append("        if (a && (b == 0)) {\n");
		buf.append("            if (c) {\n");
		buf.append("                b= 9;\n");
		buf.append("            }\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] {expected1});

	}

    @Test
	public void testSplitIfElseCondition() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(boolean a, int b) {\n");
		buf.append("        if (a && (b == 0)) {\n");
		buf.append("            b= 9;\n");
		buf.append("        } else {\n");
		buf.append("            b= 2;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("&&");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 5);
		assertCorrectLabels(proposals);

		ArrayList previews= new ArrayList();
		ArrayList expecteds= new ArrayList();
		
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(boolean a, int b) {\n");
		buf.append("        if (a) {\n");
		buf.append("            if (b == 0) {\n");
		buf.append("                b= 9;\n");
		buf.append("            } else {\n");
		buf.append("                b= 2;\n");
		buf.append("            }\n");
		buf.append("        } else {\n");
		buf.append("            b= 2;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		addPreviewAndExpected(proposals, buf, expecteds, previews);
		
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(boolean a, int b) {\n");
		buf.append("        if ((b == 0) && a) {\n");
		buf.append("            b= 9;\n");
		buf.append("        } else {\n");
		buf.append("            b= 2;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		addPreviewAndExpected(proposals, buf, expecteds, previews);
		
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(boolean a, int b) {\n");
		buf.append("        boolean c = a && (b == 0);\n");
		buf.append("        if (c) {\n");
		buf.append("            b= 9;\n");
		buf.append("        } else {\n");
		buf.append("            b= 2;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		addPreviewAndExpected(proposals, buf, expecteds, previews);
		
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(boolean a, int b) {\n");
		buf.append("        boolean c = a && (b == 0);\n");
		buf.append("        if (c) {\n");
		buf.append("            b= 9;\n");
		buf.append("        } else {\n");
		buf.append("            b= 2;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		addPreviewAndExpected(proposals, buf, expecteds, previews);
		
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(boolean a, int b) {\n");
		buf.append("        if ((a && (b == 0))) {\n");
		buf.append("            b= 9;\n");
		buf.append("        } else {\n");
		buf.append("            b= 2;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		addPreviewAndExpected(proposals, buf, expecteds, previews);

		assertEqualStringsIgnoreOrder(previews, expecteds);
	}

    @Test
	public void testJoinAndIfStatements1() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(boolean a, int b, boolean c) {\n");
		buf.append("        if (a && (b == 0)) {\n");
		buf.append("            if (c) {\n");
		buf.append("                b= 9;\n");
		buf.append("            }\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("if (a");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(boolean a, int b, boolean c) {\n");
		buf.append("        if (a && (b == 0) && c) {\n");
		buf.append("            b= 9;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] {expected1});

	}

    @Test
	public void testJoinAndIfStatements2() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(boolean a, int b, boolean c) {\n");
		buf.append("        if (a && (b == 0))\n");
		buf.append("            if (c) {\n");
		buf.append("                b= 9;\n");
		buf.append("            }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("if (a");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(boolean a, int b, boolean c) {\n");
		buf.append("        if (a && (b == 0) && c) {\n");
		buf.append("            b= 9;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1 });

	}

    @Test
	public void testJoinAndIfStatements3() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(boolean a, int b, boolean c) {\n");
		buf.append("        if (a && (b == 0)) {\n");
		buf.append("            if (c) {\n");
		buf.append("                b= 9;\n");
		buf.append("            }\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("if (c");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(boolean a, int b, boolean c) {\n");
		buf.append("        if (a && (b == 0) && c) {\n");
		buf.append("            b= 9;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1 });

	}

    @Test
	public void testJoinAndIfStatements4() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(boolean a, int b, boolean c) {\n");
		buf.append("        if (a && (b == 0))\n");
		buf.append("            if (c) {\n");
		buf.append("                b= 9;\n");
		buf.append("            }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("if (c");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(boolean a, int b, boolean c) {\n");
		buf.append("        if (a && (b == 0) && c) {\n");
		buf.append("            b= 9;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] {expected1});

	}

    @Test
	public void testJoinAndIfStatementsBug335173() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(Object a, int x) {\n");
		buf.append("        if (a instanceof String) {\n");
		buf.append("            if (x > 2) {\n");
		buf.append("            }\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("if (a");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(Object a, int x) {\n");
		buf.append("        if (a instanceof String && x > 2) {\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1 });

	}

    @Test
	public void testJoinOrIfStatements1() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(boolean a, int b, boolean c) {\n");
		buf.append("        if (a)\n");
		buf.append("            return;\n");
		buf.append("        if (b == 5)\n");
		buf.append("            return;\n");
		buf.append("        b= 9;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset1= buf.toString().indexOf("if (a");
		int offset2= buf.toString().lastIndexOf("b= 9;");
		AssistContext context= getCorrectionContext(cu, offset1, offset2 - offset1);
		List proposals= collectAssists(context, false);

		for (Iterator I= proposals.iterator(); I.hasNext();) {
			Object o= I.next();
			if (!(o instanceof CUCorrectionProposal))
				I.remove();
		}

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(boolean a, int b, boolean c) {\n");
		buf.append("        if (a || b == 5)\n");
		buf.append("            return;\n");
		buf.append("        b= 9;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] {expected1});

	}

    @Test
	public void testJoinOrIfStatementsBug335173() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(Object a, int x) {\n");
		buf.append("        if (a instanceof String)\n");
		buf.append("            return;\n");
		buf.append("        if (x > 2)\n");
		buf.append("            return;\n");
		buf.append("        x= 9;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset1= buf.toString().indexOf("if (a");
		int offset2= buf.toString().lastIndexOf("x= 9;");
		AssistContext context= getCorrectionContext(cu, offset1, offset2 - offset1);
		List proposals= collectAssists(context, false);

		for (Iterator I= proposals.iterator(); I.hasNext();) {
			Object o= I.next();
			if (!(o instanceof CUCorrectionProposal))
				I.remove();
		}

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(Object a, int x) {\n");
		buf.append("        if (a instanceof String || x > 2)\n");
		buf.append("            return;\n");
		buf.append("        x= 9;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1 });

	}

    @Test
	public void testSplitOrCondition1() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(boolean a, int b, boolean c) {\n");
		buf.append("        if (a || b == 5)\n");
		buf.append("            return;\n");
		buf.append("        b= 9;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("||");
		AssistContext context= getCorrectionContext(cu, offset, 0);

		List proposals= collectAssists(context, false);
		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(boolean a, int b, boolean c) {\n");
		buf.append("        if (a)\n");
		buf.append("            return;\n");
		buf.append("        else if (b == 5)\n");
		buf.append("            return;\n");
		buf.append("        b= 9;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] {expected1});
	}

    @Test
	public void testSplitOrCondition2() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(boolean a, int b, boolean c) {\n");
		buf.append("        if (a || b == 5)\n");
		buf.append("            return;\n");
		buf.append("        else {\n");
		buf.append("            b= 8;\n");
		buf.append("        }\n");
		buf.append("        b= 9;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("||");
		AssistContext context= getCorrectionContext(cu, offset, 0);

		List proposals= collectAssists(context, false);
		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(boolean a, int b, boolean c) {\n");
		buf.append("        if (a)\n");
		buf.append("            return;\n");
		buf.append("        else if (b == 5)\n");
		buf.append("            return;\n");
		buf.append("        else {\n");
		buf.append("            b= 8;\n");
		buf.append("        }\n");
		buf.append("        b= 9;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1 });
	}

    @Test
	public void testIfReturnIntoIfElseAtEndOfVoidMethod1() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(boolean a, int b) {\n");
		buf.append("        if (a) {\n");
		buf.append("            b= 9;\n");
		buf.append("            return;\n");
		buf.append("        }\n");
		buf.append("        b= 0;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("if (");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(boolean a, int b) {\n");
		buf.append("        if (a) {\n");
		buf.append("            b= 9;\n");
		buf.append("        } else {\n");
		buf.append("            b= 0;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] {expected1});

	}

    @Test
	public void testInverseIfContinueIntoIfThenInLoops1() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.*;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(boolean a, ArrayList list) {\n");
		buf.append("        for (Iterator I = list.iterator(); I.hasNext();) {\n");
		buf.append("            if (a) {\n");
		buf.append("                b= 9;\n");
		buf.append("            }\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("if (");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.*;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(boolean a, ArrayList list) {\n");
		buf.append("        for (Iterator I = list.iterator(); I.hasNext();) {\n");
		buf.append("            if (!a)\n");
		buf.append("                continue;\n");
		buf.append("            b= 9;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] {expected1});

	}

    @Test
	public void testInverseIfIntoContinueInLoops1() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.*;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(boolean a, ArrayList list) {\n");
		buf.append("        for (Iterator I = list.iterator(); I.hasNext();) {\n");
		buf.append("            if (!a)\n");
		buf.append("                continue;\n");
		buf.append("            b= 9;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("if (");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.*;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(boolean a, ArrayList list) {\n");
		buf.append("        for (Iterator I = list.iterator(); I.hasNext();) {\n");
		buf.append("            if (a) {\n");
		buf.append("                b= 9;\n");
		buf.append("            }\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] {expected1});

	}

    @Test
	public void testRemoveExtraParentheses1() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(boolean a, int b, Object o) {\n");
		buf.append("        if (a && (b == 0) && (o instanceof Integer) && (a || b)) {\n");
		buf.append("            b= 9;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset1= buf.toString().indexOf("if (");
		int offset2= buf.toString().indexOf(") {", offset1);
		AssistContext context= getCorrectionContext(cu, offset1, offset2 - offset1);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(boolean a, int b, Object o) {\n");
		buf.append("        if (a && b == 0 && o instanceof Integer && (a || b)) {\n");
		buf.append("            b= 9;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] {expected1});

	}

    @Test
	public void testRemoveExtraParentheses2() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public int foo() {\n");
		buf.append("        return (9+ 8);\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		String str= "(9+ 8)";
		AssistContext context= getCorrectionContext(cu, buf.toString().indexOf(str), 0);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public int foo() {\n");
		buf.append("        return 9+ 8;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] {expected1});
	}

    @Test
	public void testAddParanoidalParentheses1() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(boolean a, int b, int c, Object o) {\n");
		buf.append("        if (a && b == 0 && b + c > 3 && o instanceof Integer) {\n");
		buf.append("            b= 9;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset1= buf.toString().indexOf("if (");
		int offset2= buf.toString().indexOf(") {", offset1);
		AssistContext context= getCorrectionContext(cu, offset1, offset2 - offset1);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(boolean a, int b, int c, Object o) {\n");
		buf.append("        if (a && (b == 0) && ((b + c) > 3) && (o instanceof Integer)) {\n");
		buf.append("            b= 9;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] {expected1});

	}

    @Test
	public void testAddParenthesesForExpression1() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(Object obj) {\n");
		buf.append("        if (obj instanceof String) {\n");
		buf.append("            String string = (String) obj;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		AssistContext context= getCorrectionContext(cu, buf.toString().indexOf("(String) obj"), 0);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(Object obj) {\n");
		buf.append("        if (obj instanceof String) {\n");
		buf.append("            String string = ((String) obj);\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1 });

	}

    @Test
	public void testAddParenthesesForExpression2() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(Object obj) {\n");
		buf.append("        if (obj instanceof String) {\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		AssistContext context= getCorrectionContext(cu, buf.toString().indexOf("instanceof"), 0);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(Object obj) {\n");
		buf.append("        if ((obj instanceof String)) {\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1 });

	}

    @Test
	public void testAddParenthesesForExpression3() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(int a, int b, int c) {\n");
		buf.append("        if (a + b == 0 && b + c > 3) {\n");
		buf.append("            b= 9;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		AssistContext context= getCorrectionContext(cu, buf.toString().indexOf("=="), 0);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(int a, int b, int c) {\n");
		buf.append("        if ((a + b == 0) && b + c > 3) {\n");
		buf.append("            b= 9;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1 });

	}

    @Test
	public void testAddParenthesesForExpression4() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(int a, int b, int c) {\n");
		buf.append("        if (a + b == 0 && b + c > 3) {\n");
		buf.append("            b= 9;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		AssistContext context= getCorrectionContext(cu, buf.toString().indexOf("+"), 0);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(int a, int b, int c) {\n");
		buf.append("        if ((a + b) == 0 && b + c > 3) {\n");
		buf.append("            b= 9;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1 });

	}

    @Test
	public void testAddParenthesesForExpression5() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(int a, int b, int c) {\n");
		buf.append("        int d = a > 10 ? b : c;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		AssistContext context= getCorrectionContext(cu, buf.toString().indexOf("?"), 0);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(int a, int b, int c) {\n");
		buf.append("        int d = (a > 10 ? b : c);\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1 });

	}

    @Test
	public void testAddParenthesesForExpression6() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(int a, int b, int c) {\n");
		buf.append("        if (a > 3 && b > 5) {\n");
		buf.append("            a= 3;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		AssistContext context= getCorrectionContext(cu, buf.toString().indexOf("a > 3"), "a > 3".length());
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);
		assertNumberOfProposals(proposals, 7);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(int a, int b, int c) {\n");
		buf.append("        if ((a > 3) && b > 5) {\n");
		buf.append("            a= 3;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1 });

	}

    @Test
	public void testAddParenthesesForExpression7() throws Exception {
		//https://bugs.eclipse.org/bugs/show_bug.cgi?id=338675
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(int a, int b, int c) {\n");
		buf.append("        if (a > 3 && b > 5) {\n");
		buf.append("            a= 3;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		AssistContext context= getCorrectionContext(cu, buf.toString().indexOf("a >"), "a >".length());
		List proposals= collectAssists(context, false);

		assertProposalDoesNotExist(proposals, "Put '>' expression in parentheses");
		
	}

    @Test
	public void testAddParenthesesForExpression8() throws Exception {
		//https://bugs.eclipse.org/bugs/show_bug.cgi?id=338675
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(int a, int b, int c) {\n");
		buf.append("        if (a > 3 && b > 5) {\n");
		buf.append("            a= 3;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		AssistContext context= getCorrectionContext(cu, buf.toString().indexOf("a >"), 1);
		List proposals= collectAssists(context, false);

		assertProposalDoesNotExist(proposals, "Put '>' expression in parentheses");
		
	}

    @Test
	public void testInverseIfCondition1() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(boolean a, int b) {\n");
		buf.append("        if (a && (b == 0)) {\n");
		buf.append("            return;\n");
		buf.append("        } else {\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("if (");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(boolean a, int b) {\n");
		buf.append("        if (!a || (b != 0)) {\n");
		buf.append("        } else {\n");
		buf.append("            return;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] {expected1});

	}

    @Test
	public void testInverseIfCondition2() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(boolean a, boolean b, boolean c) {\n");
		buf.append("        if (a || b && c) {\n");
		buf.append("            return;\n");
		buf.append("        } else {\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("if (");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(boolean a, boolean b, boolean c) {\n");
		buf.append("        if (!a && (!b || !c)) {\n");
		buf.append("        } else {\n");
		buf.append("            return;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] {expected1});

	}

    @Test
	public void testInverseIfCondition3() throws Exception {
		//https://bugs.eclipse.org/bugs/show_bug.cgi?id=75109
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(boolean a, boolean b) {\n");
		buf.append("        if (a)\n");
		buf.append("            if (b) //inverse\n");
		buf.append("                return 1;\n");
		buf.append("            else\n");
		buf.append("                return 2;\n");
		buf.append("        return 17;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("if (b");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(boolean a, boolean b) {\n");
		buf.append("        if (a)\n");
		buf.append("            if (!b)\n");
		buf.append("                return 2;\n");
		buf.append("            else\n");
		buf.append("                return 1;\n");
		buf.append("        return 17;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] {expected1});

	}

    @Test
	public void testInverseIfCondition4() throws Exception {
		//https://bugs.eclipse.org/bugs/show_bug.cgi?id=74580
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(boolean a, boolean b, boolean c) {\n");
		buf.append("        if (a) {\n");
		buf.append("            one();\n");
		buf.append("        } else if (b) {\n");
		buf.append("            two();\n");
		buf.append("        } else {\n");
		buf.append("            three();\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("if (a");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(boolean a, boolean b, boolean c) {\n");
		buf.append("        if (!a) {\n");
		buf.append("            if (b) {\n");
		buf.append("                two();\n");
		buf.append("            } else {\n");
		buf.append("                three();\n");
		buf.append("            }\n");
		buf.append("        } else {\n");
		buf.append("            one();\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] {expected1});

	}

    @Test
	public void testInverseIfCondition5() throws Exception {
		//https://bugs.eclipse.org/bugs/show_bug.cgi?id=74580
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(int i) {\n");
		buf.append("        if (i == 1)\n");
		buf.append("            one();\n");
		buf.append("        else if (i == 2)\n");
		buf.append("            two();\n");
		buf.append("        else\n");
		buf.append("            three();\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("if (i == 1");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(int i) {\n");
		buf.append("        if (i != 1) {\n");
		buf.append("            if (i == 2)\n");
		buf.append("                two();\n");
		buf.append("            else\n");
		buf.append("                three();\n");
		buf.append("        } else\n");
		buf.append("            one();\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] {expected1});

	}

    @Test
	public void testInverseIfCondition_bug119251() throws Exception {
		//https://bugs.eclipse.org/bugs/show_bug.cgi?id=119251
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test;\n");
		buf.append("public class E {\n");
		buf.append("    private boolean a() { return false; }\n");
		buf.append("    private void foo(int i) {}\n");
		buf.append("    public void b() {\n");
		buf.append("        if (!a() && !a() && !a() && !a())\n");
		buf.append("            foo(1);\n");
		buf.append("        else\n");
		buf.append("            foo(2);\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("if");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test;\n");
		buf.append("public class E {\n");
		buf.append("    private boolean a() { return false; }\n");
		buf.append("    private void foo(int i) {}\n");
		buf.append("    public void b() {\n");
		buf.append("        if (a() || a() || a() || a())\n");
		buf.append("            foo(2);\n");
		buf.append("        else\n");
		buf.append("            foo(1);\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] {expected1});
	}

    @Test
	public void testInverseIfCondition6() throws Exception {
		//https://bugs.eclipse.org/bugs/show_bug.cgi?id=119251
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test;\n");
		buf.append("public class E {\n");
		buf.append("    private boolean a() { return false; }\n");
		buf.append("    private void foo(int i) {}\n");
		buf.append("    public void b() {\n");
		buf.append("        if (!a() && !a() || !a() && !a())\n");
		buf.append("            foo(1);\n");
		buf.append("        else\n");
		buf.append("            foo(2);\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("if");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test;\n");
		buf.append("public class E {\n");
		buf.append("    private boolean a() { return false; }\n");
		buf.append("    private void foo(int i) {}\n");
		buf.append("    public void b() {\n");
		buf.append("        if ((a() || a()) && (a() || a()))\n");
		buf.append("            foo(2);\n");
		buf.append("        else\n");
		buf.append("            foo(1);\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] {expected1});
	}

    @Test
	public void testInverseIfConditionUnboxing() throws Exception {
		//https://bugs.eclipse.org/bugs/show_bug.cgi?id=297645
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(Boolean b) {\n");
		buf.append("        if (b) {\n");
		buf.append("            System.out.println(\"######\");\n");
		buf.append("        } else {\n");
		buf.append("            System.out.println(\"-\");\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);
		
		int offset= buf.toString().indexOf("if");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		List proposals= collectAssists(context, false);
		
		assertCorrectLabels(proposals);
		
		buf= new StringBuffer();
		buf.append("package test;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(Boolean b) {\n");
		buf.append("        if (!b) {\n");
		buf.append("            System.out.println(\"-\");\n");
		buf.append("        } else {\n");
		buf.append("            System.out.println(\"######\");\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();
		
		assertExpectedExistInProposals(proposals, new String[] {expected1});
	}

    @Test
	public void testInverseIfConditionEquals() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(boolean a, boolean b) {\n");
		buf.append("        if (a == (b && a))\n");
		buf.append("            return 1;\n");
		buf.append("        else\n");
		buf.append("            return 2;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);
	
		int offset= buf.toString().indexOf("if (");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		List proposals= collectAssists(context, false);
	
		assertCorrectLabels(proposals);
	
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(boolean a, boolean b) {\n");
		buf.append("        if (a != (b && a))\n");
		buf.append("            return 2;\n");
		buf.append("        else\n");
		buf.append("            return 1;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();
	
		assertExpectedExistInProposals(proposals, new String[] {expected1});
	}

    @Test
	public void testInverseIfCondition_bug117960() throws Exception {
		//https://bugs.eclipse.org/bugs/show_bug.cgi?id=117960
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(boolean a, boolean b) {\n");
		buf.append("        if (a || b ? a : b) {\n");
		buf.append("            System.out.println();\n");
		buf.append("        } else {\n");
		buf.append("            return;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("if");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(boolean a, boolean b) {\n");
		buf.append("        if (a || b ? !a : !b) {\n");
		buf.append("            return;\n");
		buf.append("        } else {\n");
		buf.append("            System.out.println();\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1 });
	}

    @Test
	public void testInverseIfCondition_bug388074() throws Exception {
		//https://bugs.eclipse.org/bugs/show_bug.cgi?id=388074
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(boolean a, boolean b) {\n");
		buf.append("        if (!(a || b) || c) {\n");
		buf.append("            return 0;\n");
		buf.append("        } else {\n");
		buf.append("            return 1;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("if (!");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(boolean a, boolean b) {\n");
		buf.append("        if ((a || b) && !c) {\n");
		buf.append("            return 1;\n");
		buf.append("        } else {\n");
		buf.append("            return 0;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1 });

	}

    @Test
	public void testInverseConditionalStatement1() throws Exception {
		//https://bugs.eclipse.org/bugs/show_bug.cgi?id=74746
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public int foo(boolean a) {\n");
		buf.append("        return a ? 4 : 5;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("?");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public int foo(boolean a) {\n");
		buf.append("        return !a ? 5 : 4;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] {expected1});

	}

    @Test
	public void testInverseConditionalStatement2() throws Exception {
		//https://bugs.eclipse.org/bugs/show_bug.cgi?id=74746
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public int foo(int a) {\n");
		buf.append("        return a + 6 == 9 ? 4 : 5;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("?");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public int foo(int a) {\n");
		buf.append("        return a + 6 != 9 ? 5 : 4;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] {expected1});

	}

    @Test
	public void testInnerAndOuterIfConditions1() throws Exception {
		//https://bugs.eclipse.org/bugs/show_bug.cgi?id=74746
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public int foo(int a, Object b) {\n");
		buf.append("        if (a == 8) {\n");
		buf.append("            if (b instanceof String) {\n");
		buf.append("                return 0;\n");
		buf.append("            }\n");
		buf.append("        }\n");
		buf.append("        return 1;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("if (a");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public int foo(int a, Object b) {\n");
		buf.append("        if (b instanceof String) {\n");
		buf.append("            if (a == 8) {\n");
		buf.append("                return 0;\n");
		buf.append("            }\n");
		buf.append("        }\n");
		buf.append("        return 1;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] {expected1});

	}

    @Test
	public void testInnerAndOuterIfConditions2() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public int foo(int a, Object b) {\n");
		buf.append("        if (a == 8)\n");
		buf.append("            if (b instanceof String) {\n");
		buf.append("                return 0;\n");
		buf.append("            }\n");
		buf.append("        return 1;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("if (a");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public int foo(int a, Object b) {\n");
		buf.append("        if (b instanceof String)\n");
		buf.append("            if (a == 8) {\n");
		buf.append("                return 0;\n");
		buf.append("            }\n");
		buf.append("        return 1;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1 });

	}

    @Test
	public void testInnerAndOuterIfConditions3() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public int foo(int a, Object b) {\n");
		buf.append("        if (a == 8) {\n");
		buf.append("            if (b instanceof String) {\n");
		buf.append("                return 0;\n");
		buf.append("            }\n");
		buf.append("        }\n");
		buf.append("        return 1;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("if (b");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public int foo(int a, Object b) {\n");
		buf.append("        if (b instanceof String) {\n");
		buf.append("            if (a == 8) {\n");
		buf.append("                return 0;\n");
		buf.append("            }\n");
		buf.append("        }\n");
		buf.append("        return 1;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1 });

	}

    @Test
	public void testInnerAndOuterIfConditions4() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public int foo(int a, Object b) {\n");
		buf.append("        if (a == 8)\n");
		buf.append("            if (b instanceof String) {\n");
		buf.append("                return 0;\n");
		buf.append("            }\n");
		buf.append("        return 1;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("if (b");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public int foo(int a, Object b) {\n");
		buf.append("        if (b instanceof String)\n");
		buf.append("            if (a == 8) {\n");
		buf.append("                return 0;\n");
		buf.append("            }\n");
		buf.append("        return 1;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1 });

	}

    @Test
	public void testExchangeOperands1() throws Exception {
		//https://bugs.eclipse.org/bugs/show_bug.cgi?id=74746
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public boolean foo(int a, Object b) {\n");
		buf.append("        return a == b.hashCode();\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("==");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public boolean foo(int a, Object b) {\n");
		buf.append("        return b.hashCode() == a;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] {expected1});

	}

    @Test
	public void testExchangeOperands2() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public boolean foo(int a, int b) {\n");
		buf.append("        return (0 == (a & b));\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("==");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public boolean foo(int a, int b) {\n");
		buf.append("        return ((a & b) == 0);\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1 });

	}

    @Test
	public void testExchangeOperands3() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        int n = (2 + 3) * (4 + 5);\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("*");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        int n = (4 + 5) * (2 + 3);\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1 });

	}

    @Test
	public void testExchangeOperands4() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public boolean foo(int a, int b) {\n");
		buf.append("        return (a < b);\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("<");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public boolean foo(int a, int b) {\n");
		buf.append("        return (b > a);\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1 });

	}

    @Test
	public void testExchangeOperands5() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public boolean foo(int a, int b) {\n");
		buf.append("        return (a <= b);\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("<=");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public boolean foo(int a, int b) {\n");
		buf.append("        return (b >= a);\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1 });

	}

    @Test
	public void testExchangeOperands6() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public boolean foo(int a, int b) {\n");
		buf.append("        return (a > b);\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf(">");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public boolean foo(int a, int b) {\n");
		buf.append("        return (b < a);\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1 });

	}

    @Test
	public void testExchangeOperands7() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public boolean foo(int a, int b) {\n");
		buf.append("        return (a >= b);\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf(">=");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public boolean foo(int a, int b) {\n");
		buf.append("        return (b <= a);\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1 });

	}

    @Test
	public void testExchangeOperandsBug332019_1() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public boolean foo(int a, int b) {\n");
		buf.append("        return b != 0 != (a == b);\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("!= (");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public boolean foo(int a, int b) {\n");
		buf.append("        return (a == b) != (b != 0);\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1 });

	}

    @Test
	public void testExchangeOperandsBug332019_2() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public boolean foo(int a, int b) {\n");
		buf.append("        return b > 0 != (a == b);\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("!=");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public boolean foo(int a, int b) {\n");
		buf.append("        return (a == b) != b > 0;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1 });

	}

    @Test
	public void testExchangeOperandsBug332019_3() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public boolean foo(int a, int b) {\n");
		buf.append("        return b == 0 == true == false;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("== false");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public boolean foo(int a, int b) {\n");
		buf.append("        return false == (b == 0 == true);\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1 });

	}

    @Test
	public void testExchangeOperandsBug332019_4() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public boolean foo(int a, int b) {\n");
		buf.append("        return b + 1 != a - 1;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("!=");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public boolean foo(int a, int b) {\n");
		buf.append("        return a - 1 != b + 1;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1 });

	}

    @Test
	public void testAssignAndCast1() throws Exception {
		//https://bugs.eclipse.org/bugs/show_bug.cgi?id=75066
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(int a, Object b) {\n");
		buf.append("        if (b instanceof String) {\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("instanceof");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(int a, Object b) {\n");
		buf.append("        if (b instanceof String) {\n");
		buf.append("            String string = (String) b;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] {expected1});

	}

    @Test
	public void testAssignAndCast2() throws Exception {
		//https://bugs.eclipse.org/bugs/show_bug.cgi?id=75066
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(int a, Object b) {\n");
		buf.append("        while (b instanceof String)\n");
		buf.append("            return;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("instanceof");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(int a, Object b) {\n");
		buf.append("        while (b instanceof String) {\n");
		buf.append("            String string = (String) b;\n");
		buf.append("            return;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] {expected1});

	}

    @Test
	public void testAssignAndCastBug_104021() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(int a, Object b) {\n");
		buf.append("        if (b instanceof String) {\n");
		buf.append("            String string = \"\";\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("instanceof");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(int a, Object b) {\n");
		buf.append("        if (b instanceof String) {\n");
		buf.append("            String string2 = (String) b;\n");
		buf.append("            String string = \"\";\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] {expected1});

	}

    @Test
	public void testAssignAndCastBug129336_1() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(int a, Object b) {\n");
		buf.append("        if (!(b instanceof String)) {\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("instanceof");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(int a, Object b) {\n");
		buf.append("        if (!(b instanceof String)) {\n");
		buf.append("        }\n");
		buf.append("        String string = (String) b;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] {expected1});

	}

    @Test
	public void testAssignAndCast129336_2() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(int a, Object b) {\n");
		buf.append("        while (!(b instanceof String))\n");
		buf.append("            return;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("instanceof");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(int a, Object b) {\n");
		buf.append("        while (!(b instanceof String))\n");
		buf.append("            return;\n");
		buf.append("        String string = (String) b;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] {expected1});

	}

    @Test
	public void testAssignAndCastBug129336_3() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(int a, Object b) {\n");
		buf.append("        if (!(b instanceof String)) {\n");
		buf.append("        } else {\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("instanceof");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(int a, Object b) {\n");
		buf.append("        if (!(b instanceof String)) {\n");
		buf.append("        } else {\n");
		buf.append("            String string = (String) b;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] {expected1});

	}

    @Test
	public void testAssignAndCastBug331195_1() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(int a, Object b) {\n");
		buf.append("        if (b instanceof String) {\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("}") - 1;
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(int a, Object b) {\n");
		buf.append("        if (b instanceof String) {\n");
		buf.append("            String string = (String) b;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1 });

	}

    @Test
	public void testAssignAndCastBug331195_2() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(int a, Object b) {\n");
		buf.append("        while (b instanceof String) {\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("}") - 1;
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(int a, Object b) {\n");
		buf.append("        while (b instanceof String) {\n");
		buf.append("            String string = (String) b;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1 });

	}

    @Test
	public void testAssignAndCastBug331195_3() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(int a, Object b) {\n");
		buf.append("        if (b instanceof String) {\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("b instanceof");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(int a, Object b) {\n");
		buf.append("        if (b instanceof String) {\n");
		buf.append("            String string = (String) b;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1 });

	}

    @Test
	public void testAssignAndCastBug331195_4() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(int a, Object b) {\n");
		buf.append("        if (b instanceof String) {\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("String)");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(int a, Object b) {\n");
		buf.append("        if (b instanceof String) {\n");
		buf.append("            String string = (String) b;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1 });

	}

    @Test
	public void testAssignAndCastBug331195_5() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(int a, Object b) {\n");
		buf.append("        if (b instanceof String && a > 10) {\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("}") - 1;
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(int a, Object b) {\n");
		buf.append("        if (b instanceof String && a > 10) {\n");
		buf.append("            String string = (String) b;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1 });

	}

    @Test
	public void testAssignAndCastBug331195_6() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(int a, Object b) {\n");
		buf.append("        if (b instanceof String) {\n");
		buf.append("            int x=10;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("int x") - 1;
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(int a, Object b) {\n");
		buf.append("        if (b instanceof String) {\n");
		buf.append("            String string = (String) b;\n");
		buf.append("            int x=10;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1 });

	}

    @Test
	public void testAssignAndCastBug331195_7() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(int a, Object b) {\n");
		buf.append("        if (b instanceof String) \n");
		buf.append("            return;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("return") - 1;
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(int a, Object b) {\n");
		buf.append("        if (b instanceof String) {\n");
		buf.append("            String string = (String) b;\n");
		buf.append("            return;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1 });

	}

    @Test
	public void testAssignAndCastBug331195_8() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(int a, Object b) {\n");
		buf.append("        while (b instanceof String) \n");
		buf.append("            System.out.println(b);\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("System") - 1;
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(int a, Object b) {\n");
		buf.append("        while (b instanceof String) {\n");
		buf.append("            String string = (String) b;\n");
		buf.append("            System.out.println(b);\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1 });

	}

    @Test
	public void testReplaceReturnConditionWithIf1() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public String foo(Object b) {\n");
		buf.append("        return (b == null) ? null : b.toString();\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("?");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public String foo(Object b) {\n");
		buf.append("        if (b == null)\n");
		buf.append("            return null;\n");
		buf.append("        else\n");
		buf.append("            return b.toString();\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] {expected1});
	}

    @Test
	public void testReplaceReturnConditionWithIf2() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public String foo(Object b) {\n");
		buf.append("        return (b == null) ? null : b.toString();\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("return");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public String foo(Object b) {\n");
		buf.append("        if (b == null)\n");
		buf.append("            return null;\n");
		buf.append("        else\n");
		buf.append("            return b.toString();\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1 });
	}

    @Test
	public void testReplaceReturnConditionWithIf3() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public String foo(Object b) {\n");
		buf.append("        return (b == null) ? null : b.toString();\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int startOffset= buf.toString().indexOf("return");
		int endOffset= buf.toString().indexOf("    }");
		AssistContext context= getCorrectionContext(cu, startOffset, endOffset - startOffset - 1);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public String foo(Object b) {\n");
		buf.append("        if (b == null)\n");
		buf.append("            return null;\n");
		buf.append("        else\n");
		buf.append("            return b.toString();\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1 });
	}

    @Test
	public void testReplaceReturnConditionWithIf4() throws Exception {
		//https://bugs.eclipse.org/bugs/show_bug.cgi?id=112443
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.Collections;\n");
		buf.append("import java.util.List;\n");
		buf.append("public class E {\n");
		buf.append("    List<String> foo(List<String> list) {\n");
		buf.append("        return list != null ? list : Collections.emptyList();\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("?");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.Collections;\n");
		buf.append("import java.util.List;\n");
		buf.append("public class E {\n");
		buf.append("    List<String> foo(List<String> list) {\n");
		buf.append("        if (list != null)\n");
		buf.append("            return list;\n");
		buf.append("        else\n");
		buf.append("            return Collections.emptyList();\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1 });
	}

    @Test
	public void testReplaceAssignConditionWithIf1() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(Object b) {\n");
		buf.append("        Object res= (b == null) ? null : b.toString();\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("?");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(Object b) {\n");
		buf.append("        Object res;\n");
		buf.append("        if (b == null)\n");
		buf.append("            res = null;\n");
		buf.append("        else\n");
		buf.append("            res = b.toString();\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] {expected1});
	}

    @Test
	public void testReplaceAssignConditionWithIf2() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(Object b) {\n");
		buf.append("        Object res;\n");
		buf.append("        res= (b == null) ? null : b.toString();\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("?");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(Object b) {\n");
		buf.append("        Object res;\n");
		buf.append("        if (b == null)\n");
		buf.append("            res = null;\n");
		buf.append("        else\n");
		buf.append("            res = b.toString();\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] {expected1});
	}

    @Test
	public void testReplaceAssignConditionWithIf3() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(boolean b) {\n");
		buf.append("        int i = 42;\n");
		buf.append("        i += ( b ) ? 1 : 2;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("?");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(boolean b) {\n");
		buf.append("        int i = 42;\n");
		buf.append("        if (b)\n");
		buf.append("            i += 1;\n");
		buf.append("        else\n");
		buf.append("            i += 2;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] {expected1});
	}

    @Test
	public void testReplaceAssignConditionWithIf4() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(Object b) {\n");
		buf.append("        Object res= (b == null) ? null : b.toString();\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("res");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(Object b) {\n");
		buf.append("        Object res;\n");
		buf.append("        if (b == null)\n");
		buf.append("            res = null;\n");
		buf.append("        else\n");
		buf.append("            res = b.toString();\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1 });
	}

    @Test
	public void testReplaceAssignConditionWithIf5() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(Object b) {\n");
		buf.append("        Object res= (b == null) ? null : b.toString();\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("Object res");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(Object b) {\n");
		buf.append("        Object res;\n");
		buf.append("        if (b == null)\n");
		buf.append("            res = null;\n");
		buf.append("        else\n");
		buf.append("            res = b.toString();\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1 });
	}

    @Test
	public void testReplaceAssignConditionWithIf6() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(Object b) {\n");
		buf.append("        Object res= (b == null) ? null : b.toString();\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int startOffset= buf.toString().indexOf("Object res");
		int endOffset= buf.toString().indexOf("    }");
		AssistContext context= getCorrectionContext(cu, startOffset, endOffset - startOffset - 1);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(Object b) {\n");
		buf.append("        Object res;\n");
		buf.append("        if (b == null)\n");
		buf.append("            res = null;\n");
		buf.append("        else\n");
		buf.append("            res = b.toString();\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1 });
	}

    @Test
	public void testReplaceAssignConditionWithIf7() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(Object b) {\n");
		buf.append("        Object res;\n");
		buf.append("        res= (b == null) ? null : b.toString();\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("res=");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(Object b) {\n");
		buf.append("        Object res;\n");
		buf.append("        if (b == null)\n");
		buf.append("            res = null;\n");
		buf.append("        else\n");
		buf.append("            res = b.toString();\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1 });
	}

    @Test
	public void testReplaceAssignConditionWithIf8() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(Object b) {\n");
		buf.append("        Object res;\n");
		buf.append("        res= (b == null) ? null : b.toString();\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int startOffset= buf.toString().indexOf("res=");
		int endOffset= buf.toString().indexOf("    }");
		AssistContext context= getCorrectionContext(cu, startOffset, endOffset - startOffset - 1);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(Object b) {\n");
		buf.append("        Object res;\n");
		buf.append("        if (b == null)\n");
		buf.append("            res = null;\n");
		buf.append("        else\n");
		buf.append("            res = b.toString();\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1 });
	}

    @Test
	public void testReplaceReturnIfWithCondition() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public String foo(Object b) {\n");
		buf.append("        if (b == null) {\n");
		buf.append("            return null;\n");
		buf.append("        } else {\n");
		buf.append("            return b.toString();\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");

		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("if");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public String foo(Object b) {\n");
		buf.append("        return b == null ? null : b.toString();\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] {expected1});
	}

    @Test
	public void testReplaceReturnIfWithCondition2() throws Exception {
		try {
			JavaProjectHelper.set14CompilerOptions(fJProject1);

			IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
			StringBuffer buf= new StringBuffer();
			buf.append("package test1;\n");
			buf.append("public class E {\n");
			buf.append("    public Number foo(Integer integer) {\n");
			buf.append("        if (integer != null) {\n");
			buf.append("            return integer;\n");
			buf.append("        } else {\n");
			buf.append("            return new Double(Double.MAX_VALUE);\n");
			buf.append("        }\n");
			buf.append("    }\n");
			buf.append("}\n");

			ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

			int offset= buf.toString().indexOf("if");
			AssistContext context= getCorrectionContext(cu, offset, 0);
			assertNoErrors(context);
			List proposals= collectAssists(context, false);

			assertCorrectLabels(proposals);

			buf= new StringBuffer();
			buf.append("package test1;\n");
			buf.append("public class E {\n");
			buf.append("    public Number foo(Integer integer) {\n");
			buf.append("        return integer != null ? integer : (Number) new Double(Double.MAX_VALUE);\n");
			buf.append("    }\n");
			buf.append("}\n");
			String expected1= buf.toString();

			assertExpectedExistInProposals(proposals, new String[] {expected1});
		} finally {
			JavaProjectHelper.set15CompilerOptions(fJProject1);
		}
	}

    @Test
	public void testReplaceAssignIfWithCondition1() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(Object b) {\n");
		buf.append("        Object res;\n");
		buf.append("        if (b == null) {\n");
		buf.append("            res = null;\n");
		buf.append("        } else {\n");
		buf.append("            res = b.toString();\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("if");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(Object b) {\n");
		buf.append("        Object res;\n");
		buf.append("        res = b == null ? null : b.toString();\n");
		buf.append("    }\n");
		buf.append("}\n");

		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] {expected1});

	}

    @Test
	public void testReplaceAssignIfWithCondition2() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(boolean b) {\n");
		buf.append("        int res= 0;\n");
		buf.append("        if (b) {\n");
		buf.append("            res -= 2;\n");
		buf.append("        } else {\n");
		buf.append("            res -= 3;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("if");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(boolean b) {\n");
		buf.append("        int res= 0;\n");
		buf.append("        res -= b ? 2 : 3;\n");
		buf.append("    }\n");
		buf.append("}\n");

		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] {expected1});

	}


    @Test
	public void testInverseVariable1() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public boolean foo(boolean b) {\n");
		buf.append("        boolean var= false;\n");
		buf.append("        boolean d= var && b;\n");
		buf.append("        return d;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("var");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public boolean foo(boolean b) {\n");
		buf.append("        boolean notVar= true;\n");
		buf.append("        boolean d= !notVar && b;\n");
		buf.append("        return d;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] {expected1});
	}

    @Test
	public void testInverseVariable2() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(boolean b) {\n");
		buf.append("        boolean var= b && !b;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("var");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(boolean b) {\n");
		buf.append("        boolean notVar= !b || b;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] {expected1});
	}

    @Test
	public void testInverseVariable2b() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(boolean b) {\n");
		buf.append("        boolean var= b & !b;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("var");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(boolean b) {\n");
		buf.append("        boolean notVar= !b | b;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] {expected1});
	}

    @Test
	public void testInverseVariable3() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(boolean b) {\n");
		buf.append("        boolean var= true;\n");
		buf.append("        b= var && !var;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("var");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(boolean b) {\n");
		buf.append("        boolean notVar= false;\n");
		buf.append("        b= !notVar && notVar;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] {expected1});

	}

    @Test
	public void testInverseVariable4() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(boolean b) {\n");
		buf.append("        boolean var= false;\n");
		buf.append("        var |= b;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("var");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(boolean b) {\n");
		buf.append("        boolean notVar= true;\n");
		buf.append("        notVar &= !b;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] {expected1});

	}

    @Test
	public void testInverseVariableBug117960() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(boolean a, boolean b) {\n");
		buf.append("        boolean var= a || b ? a : b;\n");
		buf.append("        var |= b;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("var");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(boolean a, boolean b) {\n");
		buf.append("        boolean notVar= a || b ? !a : !b;\n");
		buf.append("        notVar &= !b;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1 });

	}

    @Test
	public void testInverseCondition1() throws Exception {
		//https://bugs.eclipse.org/bugs/show_bug.cgi?id=334876
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(Object a, Object b) {\n");
		buf.append("        if (a == null ^ b == null) {\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("a ==");
		int length= "a == null ^ b == null".length();
		AssistContext context= getCorrectionContext(cu, offset, length);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(Object a, Object b) {\n");
		buf.append("        if (!(a == null ^ b == null)) {\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1 });

	}

    @Test
	public void testInverseCondition2() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(Object a) {\n");
		buf.append("        if (!(a instanceof String)) {\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("!");
		int length= "!(a instanceof String)".length();
		AssistContext context= getCorrectionContext(cu, offset, length);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(Object a) {\n");
		buf.append("        if (a instanceof String) {\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1 });

	}

    @Test
	public void testInverseCondition3() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(Object a) {\n");
		buf.append("        while (!(a instanceof String)) {\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("!");
		int length= "!(a instanceof String)".length();
		AssistContext context= getCorrectionContext(cu, offset, length);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(Object a) {\n");
		buf.append("        while (a instanceof String) {\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1 });

	}

    @Test
	public void testInverseCondition4() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(Object a) {\n");
		buf.append("        for (int i = 0; !(a instanceof String); i++) {\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("!");
		int length= "!(a instanceof String)".length();
		AssistContext context= getCorrectionContext(cu, offset, length);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(Object a) {\n");
		buf.append("        for (int i = 0; a instanceof String; i++) {\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1 });

	}

    @Test
	public void testInverseCondition5() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(Object a) {\n");
		buf.append("        do {\n");
		buf.append("        } while (!(a instanceof String));\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("!");
		int length= "!(a instanceof String)".length();
		AssistContext context= getCorrectionContext(cu, offset, length);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(Object a) {\n");
		buf.append("        do {\n");
		buf.append("        } while (a instanceof String);\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1 });

	}

    @Test
	public void testInverseCondition6() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(Object a) {\n");
		buf.append("        assert !(a instanceof String);\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("!");
		int length= "!(a instanceof String)".length();
		AssistContext context= getCorrectionContext(cu, offset, length);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(Object a) {\n");
		buf.append("        assert a instanceof String;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1 });

	}

    @Test
	public void testPushNegationDown1() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(int i, int j, int k) {\n");
		buf.append("        boolean b= (i > 1) || !(j < 2 || k < 3);\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("!(");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(int i, int j, int k) {\n");
		buf.append("        boolean b= (i > 1) || j >= 2 && k >= 3;\n");
		buf.append("    }\n");
		buf.append("}\n");

		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] {expected1});

	}

    @Test
	public void testPushNegationDown2() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(int i, int j, int k) {\n");
		buf.append("        boolean b= (i > 1) && !(j < 2 && k < 3);\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("!(");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(int i, int j, int k) {\n");
		buf.append("        boolean b= (i > 1) && (j >= 2 || k >= 3);\n");
		buf.append("    }\n");
		buf.append("}\n");

		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] {expected1});

	}

    @Test
	public void testPushNegationDown3() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(int i, int j, int k) {\n");
		buf.append("        boolean b= (i > 1) || !(j < 2 || k < 3);\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("(j < 2");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(int i, int j, int k) {\n");
		buf.append("        boolean b= (i > 1) || j >= 2 && k >= 3;\n");
		buf.append("    }\n");
		buf.append("}\n");

		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1 });

	}

    @Test
	public void testPushNegationDownBug335778_1() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(boolean b) {\n");
		buf.append("        if (!(b)) {\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("!(");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		assertProposalDoesNotExist(proposals, "Push negation down");
	}

    @Test
	public void testPushNegationDownBug335778_2() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(Object a) {\n");
		buf.append("        if (!(a instanceof String)) {\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("!(");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		assertProposalDoesNotExist(proposals, "Push negation down");
	}

    @Test
	public void testPushNegationDownBug117960() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(boolean a, boolean b) {\n");
		buf.append("        if (!(a || b ? !a : !b)) {\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("!(");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(boolean a, boolean b) {\n");
		buf.append("        if (a || b ? a : b) {\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");

		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1 });

	}

    @Test
	public void testPullNegationUp() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(int i, int j, int k, int m, int n) {\n");
		buf.append("        boolean b = i > 1 || j >= 2 && k >= 3 || m > 4 || n > 5;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset1= buf.toString().indexOf("j >= 2");
		int offset2= buf.toString().indexOf(" || m > 4");
		AssistContext context= getCorrectionContext(cu, offset1, offset2 - offset1);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(int i, int j, int k, int m, int n) {\n");
		buf.append("        boolean b = i > 1 || !(j < 2 || k < 3) || m > 4 || n > 5;\n");
		buf.append("    }\n");
		buf.append("}\n");

		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] {expected1});

	}

    @Test
	public void testPullNegationUpBug335778_1() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(boolean b) {\n");
		buf.append("        if (!b) {\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("!b");
		int length= "!b".length();
		AssistContext context= getCorrectionContext(cu, offset, length);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		assertProposalDoesNotExist(proposals, "Pull negation up");
	}

    @Test
	public void testPullNegationUpBug335778_2() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(Object a) {\n");
		buf.append("        if (!(a instanceof String)) {\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("!(");
		int length= "!(a instanceof String)".length();
		AssistContext context= getCorrectionContext(cu, offset, length);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		assertProposalDoesNotExist(proposals, "Pull negation up");
	}

    @Test
	public void testPullNegationUpBug117960() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(boolean a, boolean b) {\n");
		buf.append("        if (a || b ? a : b) {\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("a || b");
		int length= "a || b ? a : b".length();
		AssistContext context= getCorrectionContext(cu, offset, length);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(boolean a, boolean b) {\n");
		buf.append("        if (!(a || b ? !a : !b)) {\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");

		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1 });

	}

    @Test
	public void testJoinIfListInIfElseIf() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(int a, int b) {\n");
		buf.append("        if (a == 1)\n");
		buf.append("            System.out.println(1);\n");
		buf.append("        if (a == 2)\n");
		buf.append("            if (b > 0)\n");
		buf.append("                System.out.println(2);\n");
		buf.append("        if (a == 3)\n");
		buf.append("            if (b > 0)\n");
		buf.append("                System.out.println(3);\n");
		buf.append("            else\n");
		buf.append("                System.out.println(-3);\n");
		buf.append("        if (a == 4)\n");
		buf.append("            System.out.println(4);\n");
		buf.append("        int stop;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset1= buf.toString().indexOf("if (a == 1)");
		int offset2= buf.toString().indexOf("int stop;");
		AssistContext context= getCorrectionContext(cu, offset1, offset2 - offset1);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(int a, int b) {\n");
		buf.append("        if (a == 1)\n");
		buf.append("            System.out.println(1);\n");
		buf.append("        else if (a == 2) {\n");
		buf.append("            if (b > 0)\n");
		buf.append("                System.out.println(2);\n");
		buf.append("        } else if (a == 3)\n");
		buf.append("            if (b > 0)\n");
		buf.append("                System.out.println(3);\n");
		buf.append("            else\n");
		buf.append("                System.out.println(-3); else if (a == 4)\n");
		buf.append("                System.out.println(4);\n");
		buf.append("        int stop;\n");
		buf.append("    }\n");
		buf.append("}\n");

		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] {expected1});

	}

    @Test
	public void testConvertSwitchToIf() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(int a) {\n");
		buf.append("        switch (a) {\n");
		buf.append("            case 1:\n");
		buf.append("                {\n");
		buf.append("                    System.out.println(1);\n");
		buf.append("                    break;\n");
		buf.append("                }\n");
		buf.append("            case 2:\n");
		buf.append("            case 3:\n");
		buf.append("                System.out.println(2);\n");
		buf.append("                break;\n");
		buf.append("            case 4:\n");
		buf.append("                System.out.println(4);\n");
		buf.append("                return;\n");
		buf.append("            default:\n");
		buf.append("                System.out.println(-1);\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("switch");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(int a) {\n");
		buf.append("        if (a == 1) {\n");
		buf.append("            System.out.println(1);\n");
		buf.append("        } else if (a == 2 || a == 3) {\n");
		buf.append("            System.out.println(2);\n");
		buf.append("        } else if (a == 4) {\n");
		buf.append("            System.out.println(4);\n");
		buf.append("            return;\n");
		buf.append("        } else {\n");
		buf.append("            System.out.println(-1);\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");

		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] {expected1});

	}

    @Test
	public void testConvertSwitchToIf2() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("pack", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack;\n");
		buf.append("\n");
		buf.append("public class A {\n");
		buf.append("    public enum TimeUnit {\n");
		buf.append("        SECONDS, MILLISECONDS, MICROSECONDS, NANOSECONDS\n");
		buf.append("    }\n");
		buf.append("    public static int getPower(TimeUnit unit) {\n");
		buf.append("        switch (unit) {\n");
		buf.append("        case SECONDS:\n");
		buf.append("                return 0;\n");
		buf.append("        case MILLISECONDS:\n");
		buf.append("                return -3;\n");
		buf.append("        case MICROSECONDS:\n");
		buf.append("                return -6;\n");
		buf.append("        case NANOSECONDS:\n");
		buf.append("                return -9;\n");
		buf.append("        default:\n");
		buf.append("                throw new InternalError();\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("A.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("switch");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package pack;\n");
		buf.append("\n");
		buf.append("public class A {\n");
		buf.append("    public enum TimeUnit {\n");
		buf.append("        SECONDS, MILLISECONDS, MICROSECONDS, NANOSECONDS\n");
		buf.append("    }\n");
		buf.append("    public static int getPower(TimeUnit unit) {\n");
		buf.append("        if (unit == TimeUnit.SECONDS) {\n");
		buf.append("            return 0;\n");
		buf.append("        } else if (unit == TimeUnit.MILLISECONDS) {\n");
		buf.append("            return -3;\n");
		buf.append("        } else if (unit == TimeUnit.MICROSECONDS) {\n");
		buf.append("            return -6;\n");
		buf.append("        } else if (unit == TimeUnit.NANOSECONDS) {\n");
		buf.append("            return -9;\n");
		buf.append("        } else {\n");
		buf.append("            throw new InternalError();\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] {expected1});
	}

    @Test
	public void testConvertSwitchToIf3() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("pack", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack;\n");
		buf.append("\n");
		buf.append("public class A {\n");
		buf.append("    final static int SECONDS=1, MILLISECONDS=2, MICROSECONDS=4,NANOSECONDS=8;\n");
		buf.append("    public static int getPower(int unit) {\n");
		buf.append("        switch (unit) {\n");
		buf.append("        case SECONDS:\n");
		buf.append("                return 0;\n");
		buf.append("        case MILLISECONDS:\n");
		buf.append("                return -3;\n");
		buf.append("        case MICROSECONDS:\n");
		buf.append("                return -6;\n");
		buf.append("        case NANOSECONDS:\n");
		buf.append("                return -9;\n");
		buf.append("        default:\n");
		buf.append("                throw new InternalError();\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("A.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("switch");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package pack;\n");
		buf.append("\n");
		buf.append("public class A {\n");
		buf.append("    final static int SECONDS=1, MILLISECONDS=2, MICROSECONDS=4,NANOSECONDS=8;\n");
		buf.append("    public static int getPower(int unit) {\n");
		buf.append("        if (unit == SECONDS) {\n");
		buf.append("            return 0;\n");
		buf.append("        } else if (unit == MILLISECONDS) {\n");
		buf.append("            return -3;\n");
		buf.append("        } else if (unit == MICROSECONDS) {\n");
		buf.append("            return -6;\n");
		buf.append("        } else if (unit == NANOSECONDS) {\n");
		buf.append("            return -9;\n");
		buf.append("        } else {\n");
		buf.append("            throw new InternalError();\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] {expected1});
	}

    @Test
	public void testConvertSwitchToIfBug252104_1() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foobar() {\n");
		buf.append("        switch (getFoo() ? getBar() : getBar()) {\n");
		buf.append("        case 1:\n");
		buf.append("            System.out.println();\n");
		buf.append("            break;\n");
		buf.append("        case 2:\n");
		buf.append("            System.out.println();\n");
		buf.append("            break;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("    private int getBar() {\n");
		buf.append("        return 0;\n");
		buf.append("    }\n");
		buf.append("    private boolean getFoo() {\n");
		buf.append("        return false;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("switch");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foobar() {\n");
		buf.append("        int i = getFoo() ? getBar() : getBar();\n");
		buf.append("        if (i == 1) {\n");
		buf.append("            System.out.println();\n");
		buf.append("        } else if (i == 2) {\n");
		buf.append("            System.out.println();\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("    private int getBar() {\n");
		buf.append("        return 0;\n");
		buf.append("    }\n");
		buf.append("    private boolean getFoo() {\n");
		buf.append("        return false;\n");
		buf.append("    }\n");
		buf.append("}\n");

		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1 });

	}

    @Test
	public void testConvertSwitchToIfBug252104_2() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(int x, int y) {\n");
		buf.append("        switch (x + y) {\n");
		buf.append("        case 1:\n");
		buf.append("            System.out.println();\n");
		buf.append("            break;\n");
		buf.append("        case 2:\n");
		buf.append("            System.out.println();\n");
		buf.append("            break;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("switch");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(int x, int y) {\n");
		buf.append("        int i = x + y;\n");
		buf.append("        if (i == 1) {\n");
		buf.append("            System.out.println();\n");
		buf.append("        } else if (i == 2) {\n");
		buf.append("            System.out.println();\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");

		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1 });

	}

    @Test
	public void testConvertSwitchToIfBug252040_1() throws Exception {
		//https://bugs.eclipse.org/bugs/show_bug.cgi?id=252040
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        switch (getSomethingWithSideEffects()) {\n");
		buf.append("        case 1:\n");
		buf.append("            System.out.println();\n");
		buf.append("            break;\n");
		buf.append("        case 2:\n");
		buf.append("            System.out.println();\n");
		buf.append("            break;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("    private int getSomethingWithSideEffects() {\n");
		buf.append("        System.out.println(\"side effect\");\n");
		buf.append("        return 2;\n");
		buf.append("    }\n");
		buf.append("}\n");


		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("switch");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        int somethingWithSideEffects = getSomethingWithSideEffects();\n");
		buf.append("        if (somethingWithSideEffects == 1) {\n");
		buf.append("            System.out.println();\n");
		buf.append("        } else if (somethingWithSideEffects == 2) {\n");
		buf.append("            System.out.println();\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("    private int getSomethingWithSideEffects() {\n");
		buf.append("        System.out.println(\"side effect\");\n");
		buf.append("        return 2;\n");
		buf.append("    }\n");
		buf.append("}\n");

		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1 });

	}

    @Test
	public void testConvertSwitchToIfBug252040_2() throws Exception {
		//https://bugs.eclipse.org/bugs/show_bug.cgi?id=252040
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        if (true)\n");
		buf.append("            switch (getSomethingWithSideEffects()) {\n");
		buf.append("            case 1:\n");
		buf.append("                System.out.println();\n");
		buf.append("                break;\n");
		buf.append("            case 2:\n");
		buf.append("                System.out.println();\n");
		buf.append("                break;\n");
		buf.append("            }\n");
		buf.append("    }\n");
		buf.append("    private int getSomethingWithSideEffects() {\n");
		buf.append("        System.out.println(\"side effect\");\n");
		buf.append("        return 2;\n");
		buf.append("    }\n");
		buf.append("}\n");


		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("switch");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        if (true) {\n");
		buf.append("            int somethingWithSideEffects = getSomethingWithSideEffects();\n");
		buf.append("            if (somethingWithSideEffects == 1) {\n");
		buf.append("                System.out.println();\n");
		buf.append("            } else if (somethingWithSideEffects == 2) {\n");
		buf.append("                System.out.println();\n");
		buf.append("            }\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("    private int getSomethingWithSideEffects() {\n");
		buf.append("        System.out.println(\"side effect\");\n");
		buf.append("        return 2;\n");
		buf.append("    }\n");
		buf.append("}\n");

		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1 });

	}

    @Test
	public void testConvertSwitchToIfBug352422() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(int a) {\n");
		buf.append("        switch (a) {\n");
		buf.append("            case 1:\n");
		buf.append("                System.out.println(1);\n");
		buf.append("                break;\n");
		buf.append("            case 2:\n");
		buf.append("            case 3:\n");
		buf.append("                System.out.println(2);\n");
		buf.append("                break;\n");
		buf.append("            case 4:\n");
		buf.append("            case 5:\n");
		buf.append("            default:\n");
		buf.append("                System.out.println(-1);\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("switch");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(int a) {\n");
		buf.append("        if (a == 1) {\n");
		buf.append("            System.out.println(1);\n");
		buf.append("        } else if (a == 2 || a == 3) {\n");
		buf.append("            System.out.println(2);\n");
		buf.append("        } else if (a == 4 || a == 5 || true) {\n");
		buf.append("            System.out.println(-1);\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");

		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] {expected1});
	}

    @Test
	public void testConvertSwitchToIfBug352422_2() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(int a) {\n");
		buf.append("        switch (a) {\n");
		buf.append("            case 1:\n");
		buf.append("                System.out.println(1);\n");
		buf.append("                break;\n");
		buf.append("            case 2:\n");
		buf.append("            case 3:\n");
		buf.append("                System.out.println(2);\n");
		buf.append("                break;\n");
		buf.append("            case 4:\n");
		buf.append("            default:\n");
		buf.append("            case 5:\n");
		buf.append("                System.out.println(-1);\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);
		
		int offset= buf.toString().indexOf("switch");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);
		
		assertNumberOfProposals(proposals, 0);
	}

    @Test
	public void testConvertIfToSwitch1() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(int a) {\n");
		buf.append("        if (a == 1) {\n");
		buf.append("            System.out.println(1);\n");
		buf.append("        } else if (a == 2 || a == 3 || a == 4 || a == 5) {\n");
		buf.append("            System.out.println(2);\n");
		buf.append("        } else if (a == 6) {\n");
		buf.append("            System.out.println(4);\n");
		buf.append("        } else {\n");
		buf.append("            System.out.println(-1);\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("if");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 4);
		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(int a) {\n");
		buf.append("        switch (a) {\n");
		buf.append("            case 1 :\n");
		buf.append("                System.out.println(1);\n");
		buf.append("                break;\n");
		buf.append("            case 2 :\n");
		buf.append("            case 3 :\n");
		buf.append("            case 4 :\n");
		buf.append("            case 5 :\n");
		buf.append("                System.out.println(2);\n");
		buf.append("                break;\n");
		buf.append("            case 6 :\n");
		buf.append("                System.out.println(4);\n");
		buf.append("                break;\n");
		buf.append("            default :\n");
		buf.append("                System.out.println(-1);\n");
		buf.append("                break;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");

		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1 });
	}

    @Test
	public void testConvertIfToSwitch2() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(String s) {\n");
		buf.append("        if (\"abc\".equals(s)) {\n");
		buf.append("            System.out.println();\n");
		buf.append("        } else if (\"xyz\".equals(s)) {\n");
		buf.append("            System.out.println();\n");
		buf.append("        } else {\n");
		buf.append("            System.out.println();\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("if");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 3);
		assertCorrectLabels(proposals);

		assertProposalDoesNotExist(proposals, CorrectionMessages.AdvancedQuickAssistProcessor_convertIfElseToSwitch);
	}

    @Test
	public void testConvertIfToSwitch3() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("pack", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack;\n");
		buf.append("\n");
		buf.append("public class A {\n");
		buf.append("    public enum TimeUnit {\n");
		buf.append("        SECONDS, MILLISECONDS, MICROSECONDS, NANOSECONDS\n");
		buf.append("    }\n");
		buf.append("    public static int getPower(TimeUnit unit) {\n");
		buf.append("        if (unit == TimeUnit.SECONDS) {\n");
		buf.append("            return 0;\n");
		buf.append("        } else if (unit == TimeUnit.MILLISECONDS) {\n");
		buf.append("            return -3;\n");
		buf.append("        } else if (unit == TimeUnit.MICROSECONDS) {\n");
		buf.append("            return -6;\n");
		buf.append("        } else if (unit == TimeUnit.NANOSECONDS) {\n");
		buf.append("            return -9;\n");
		buf.append("        } else {\n");
		buf.append("            throw new InternalError();\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("A.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("if");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 4);
		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package pack;\n");
		buf.append("\n");
		buf.append("public class A {\n");
		buf.append("    public enum TimeUnit {\n");
		buf.append("        SECONDS, MILLISECONDS, MICROSECONDS, NANOSECONDS\n");
		buf.append("    }\n");
		buf.append("    public static int getPower(TimeUnit unit) {\n");
		buf.append("        switch (unit) {\n");
		buf.append("            case SECONDS :\n");
		buf.append("                return 0;\n");
		buf.append("            case MILLISECONDS :\n");
		buf.append("                return -3;\n");
		buf.append("            case MICROSECONDS :\n");
		buf.append("                return -6;\n");
		buf.append("            case NANOSECONDS :\n");
		buf.append("                return -9;\n");
		buf.append("            default :\n");
		buf.append("                throw new InternalError();\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1 });
	}

    @Test
	public void testConvertIfToSwitch4() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("pack", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack;\n");
		buf.append("\n");
		buf.append("public class A {\n");
		buf.append("    public enum TimeUnit {\n");
		buf.append("        SECONDS, MILLISECONDS, MICROSECONDS, NANOSECONDS\n");
		buf.append("    }\n");
		buf.append("    public static int getPower(TimeUnit unit) {\n");
		buf.append("        if (unit.equals(TimeUnit.SECONDS)) {\n");
		buf.append("            return 0;\n");
		buf.append("        } else if (unit.equals(TimeUnit.MILLISECONDS)) {\n");
		buf.append("            return -3;\n");
		buf.append("        } else if (unit.equals(TimeUnit.MICROSECONDS)) {\n");
		buf.append("            return -6;\n");
		buf.append("        } else if (unit.equals(TimeUnit.NANOSECONDS)) {\n");
		buf.append("            return -9;\n");
		buf.append("        } else {\n");
		buf.append("            throw new InternalError();\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("A.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("if");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 4);
		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package pack;\n");
		buf.append("\n");
		buf.append("public class A {\n");
		buf.append("    public enum TimeUnit {\n");
		buf.append("        SECONDS, MILLISECONDS, MICROSECONDS, NANOSECONDS\n");
		buf.append("    }\n");
		buf.append("    public static int getPower(TimeUnit unit) {\n");
		buf.append("        switch (unit) {\n");
		buf.append("            case SECONDS :\n");
		buf.append("                return 0;\n");
		buf.append("            case MILLISECONDS :\n");
		buf.append("                return -3;\n");
		buf.append("            case MICROSECONDS :\n");
		buf.append("                return -6;\n");
		buf.append("            case NANOSECONDS :\n");
		buf.append("                return -9;\n");
		buf.append("            default :\n");
		buf.append("                throw new InternalError();\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1 });
	}

    @Test
	public void testConvertIfToSwitch5() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("pack", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack;\n");
		buf.append("\n");
		buf.append("public class A {\n");
		buf.append("    final static int SECONDS=1, MILLISECONDS=2, MICROSECONDS=4,NANOSECONDS=8;\n");
		buf.append("    public static int getPower(int unit) {\n");
		buf.append("        if (unit == SECONDS) {\n");
		buf.append("            return 0;\n");
		buf.append("        } else if (unit == MILLISECONDS) {\n");
		buf.append("            return -3;\n");
		buf.append("        } else if (unit == MICROSECONDS) {\n");
		buf.append("            return -6;\n");
		buf.append("        } else if (unit == NANOSECONDS) {\n");
		buf.append("            return -9;\n");
		buf.append("        } else {\n");
		buf.append("            throw new InternalError();\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("A.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("if");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 4);
		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package pack;\n");
		buf.append("\n");
		buf.append("public class A {\n");
		buf.append("    final static int SECONDS=1, MILLISECONDS=2, MICROSECONDS=4,NANOSECONDS=8;\n");
		buf.append("    public static int getPower(int unit) {\n");
		buf.append("        switch (unit) {\n");
		buf.append("            case SECONDS :\n");
		buf.append("                return 0;\n");
		buf.append("            case MILLISECONDS :\n");
		buf.append("                return -3;\n");
		buf.append("            case MICROSECONDS :\n");
		buf.append("                return -6;\n");
		buf.append("            case NANOSECONDS :\n");
		buf.append("                return -9;\n");
		buf.append("            default :\n");
		buf.append("                throw new InternalError();\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1 });
	}

    @Test
	public void testConvertIfToSwitch6() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    private int a= 10;\n");
		buf.append("    public void foo() {\n");
		buf.append("        if (this.a == 1) {\n");
		buf.append("            System.out.println(1);\n");
		buf.append("        } else if (this.a == 2 || this.a == 3 || this.a == 4) {\n");
		buf.append("            System.out.println(2);\n");
		buf.append("        } else if (this.a == 5) {\n");
		buf.append("            System.out.println(4);\n");
		buf.append("        } else {\n");
		buf.append("            System.out.println(-1);\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("if");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 4);
		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    private int a= 10;\n");
		buf.append("    public void foo() {\n");
		buf.append("        switch (this.a) {\n");
		buf.append("            case 1 :\n");
		buf.append("                System.out.println(1);\n");
		buf.append("                break;\n");
		buf.append("            case 2 :\n");
		buf.append("            case 3 :\n");
		buf.append("            case 4 :\n");
		buf.append("                System.out.println(2);\n");
		buf.append("                break;\n");
		buf.append("            case 5 :\n");
		buf.append("                System.out.println(4);\n");
		buf.append("                break;\n");
		buf.append("            default :\n");
		buf.append("                System.out.println(-1);\n");
		buf.append("                break;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");

		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1 });
	}

    @Test
	public void testConvertIfToSwitch7() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    private int a= 10;\n");
		buf.append("    public int getA() {\n");
		buf.append("        return a;\n");
		buf.append("    }\n");
		buf.append("    public void foo() {\n");
		buf.append("        if (getA() == 1) {\n");
		buf.append("            System.out.println(1);\n");
		buf.append("        } else if (getA() == 2) {\n");
		buf.append("            System.out.println(2);\n");
		buf.append("        } else if (getA() == 3) {\n");
		buf.append("            System.out.println(3);\n");
		buf.append("        } else {\n");
		buf.append("            System.out.println(-1);\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("if");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 4);
		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    private int a= 10;\n");
		buf.append("    public int getA() {\n");
		buf.append("        return a;\n");
		buf.append("    }\n");
		buf.append("    public void foo() {\n");
		buf.append("        switch (getA()) {\n");
		buf.append("            case 1 :\n");
		buf.append("                System.out.println(1);\n");
		buf.append("                break;\n");
		buf.append("            case 2 :\n");
		buf.append("                System.out.println(2);\n");
		buf.append("                break;\n");
		buf.append("            case 3 :\n");
		buf.append("                System.out.println(3);\n");
		buf.append("                break;\n");
		buf.append("            default :\n");
		buf.append("                System.out.println(-1);\n");
		buf.append("                break;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");

		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1 });
	}

    @Test
	public void testConvertIfToSwitch8() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    private int a= 10;\n");
		buf.append("    public int getA() {\n");
		buf.append("        return a;\n");
		buf.append("    }\n");
		buf.append("    public void foo() {\n");
		buf.append("        if (getA() == 1) {\n");
		buf.append("            System.out.println(1);\n");
		buf.append("        } else if (this.a == 2) {\n");
		buf.append("            System.out.println(2);\n");
		buf.append("        } else if (getA() == 3) {\n");
		buf.append("            System.out.println(3);\n");
		buf.append("        } else {\n");
		buf.append("            System.out.println(-1);\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("if");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 3);
		assertCorrectLabels(proposals);

		assertProposalDoesNotExist(proposals, CorrectionMessages.AdvancedQuickAssistProcessor_convertIfElseToSwitch);
	}

    @Test
	public void testConvertIfToSwitch9() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("pack", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack;\n");
		buf.append("\n");
		buf.append("public class A {\n");
		buf.append("    public enum TimeUnit {\n");
		buf.append("        SECONDS, MILLISECONDS, MICROSECONDS, NANOSECONDS\n");
		buf.append("    }\n");
		buf.append("    public static int getPower(TimeUnit unit) {\n");
		buf.append("        if (TimeUnit.SECONDS.equals(unit)) {\n");
		buf.append("            return 0;\n");
		buf.append("        } else if (TimeUnit.MILLISECONDS.equals(unit)) {\n");
		buf.append("            return -3;\n");
		buf.append("        } else if (TimeUnit.MICROSECONDS.equals(unit)) {\n");
		buf.append("            return -6;\n");
		buf.append("        } else if (TimeUnit.NANOSECONDS.equals(unit)) {\n");
		buf.append("            return -9;\n");
		buf.append("        } else {\n");
		buf.append("            throw new InternalError();\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("A.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("if");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 5);
		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package pack;\n");
		buf.append("\n");
		buf.append("public class A {\n");
		buf.append("    public enum TimeUnit {\n");
		buf.append("        SECONDS, MILLISECONDS, MICROSECONDS, NANOSECONDS\n");
		buf.append("    }\n");
		buf.append("    public static int getPower(TimeUnit unit) {\n");
		buf.append("        switch (unit) {\n");
		buf.append("            case SECONDS :\n");
		buf.append("                return 0;\n");
		buf.append("            case MILLISECONDS :\n");
		buf.append("                return -3;\n");
		buf.append("            case MICROSECONDS :\n");
		buf.append("                return -6;\n");
		buf.append("            case NANOSECONDS :\n");
		buf.append("                return -9;\n");
		buf.append("            default :\n");
		buf.append("                throw new InternalError();\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		buf= new StringBuffer();
		buf.append("package pack;\n");
		buf.append("\n");
		buf.append("public class A {\n");
		buf.append("    public enum TimeUnit {\n");
		buf.append("        SECONDS, MILLISECONDS, MICROSECONDS, NANOSECONDS\n");
		buf.append("    }\n");
		buf.append("    public static int getPower(TimeUnit unit) {\n");
		buf.append("        if (unit == null) {\n");
		buf.append("            throw new InternalError();\n");
		buf.append("        } else {\n");
		buf.append("            switch (unit) {\n");
		buf.append("                case SECONDS :\n");
		buf.append("                    return 0;\n");
		buf.append("                case MILLISECONDS :\n");
		buf.append("                    return -3;\n");
		buf.append("                case MICROSECONDS :\n");
		buf.append("                    return -6;\n");
		buf.append("                case NANOSECONDS :\n");
		buf.append("                    return -9;\n");
		buf.append("                default :\n");
		buf.append("                    throw new InternalError();\n");
		buf.append("            }\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected2= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1, expected2 });
	}

    @Test
	public void testConvertIfToSwitchBug392847() throws Exception {
		//https://bugs.eclipse.org/bugs/show_bug.cgi?id=392847
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(String[] args) {\n");
		buf.append("        int n = 42;\n");
		buf.append("        if (n == args.length)\n");
		buf.append("            System.out.println();\n");
		buf.append("        else {\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("if");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 3);
		assertCorrectLabels(proposals);

		assertProposalDoesNotExist(proposals, CorrectionMessages.AdvancedQuickAssistProcessor_convertIfElseToSwitch);
	}

    @Test
	public void testConvertIfToSwitchBug393147() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        if (equals(\"a\")) {\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("if");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);

		assertProposalDoesNotExist(proposals, CorrectionMessages.AdvancedQuickAssistProcessor_convertIfElseToSwitch);
	}

    @Test
	public void testConvertIfToSwitchBug393147_2() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        if (this.equals(\"a\")) {\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("if");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);

		assertProposalDoesNotExist(proposals, CorrectionMessages.AdvancedQuickAssistProcessor_convertIfElseToSwitch);
	}


//	public void testSurroundWithTemplate01() throws Exception {
//		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
//		StringBuffer buf= new StringBuffer();
//		buf.append("package test1;\n");
//		buf.append("public class E1 {\n");
//		buf.append("    public void foo() {\n");
//		buf.append("        System.out.println(1);\n");
//		buf.append("    }\n");
//		buf.append("}\n");
//		ICompilationUnit cu= pack1.createCompilationUnit("E1.java", buf.toString(), false, null);
//
//		String selection= "System.out.println(1);";
//		int offset= buf.toString().indexOf(selection);
//
//		AssistContext context= getCorrectionContext(cu, offset, selection.length());
//		assertNoErrors(context);
//		List proposals= Arrays.asList(new QuickTemplateProcessor().getAssists(context, null));
//
//		assertCorrectLabels(proposals);
//		assertNumberOfProposals(proposals, 7);
//
//		String[] expected= new String[7];
//
//		buf= new StringBuffer();
//		buf.append("package test1;\n");
//		buf.append("public class E1 {\n");
//		buf.append("    public void foo() {\n");
//		buf.append("        do {\n");
//		buf.append("            System.out.println(1);\n");
//		buf.append("        } while (condition);\n");
//		buf.append("    }\n");
//		buf.append("}\n");
//		expected[0]= buf.toString();
//
//		buf= new StringBuffer();
//		buf.append("package test1;\n");
//		buf.append("public class E1 {\n");
//		buf.append("    public void foo() {\n");
//		buf.append("        for (int i = 0; i < array.length; i++) {\n");
//		buf.append("            System.out.println(1);\n");
//		buf.append("        }\n");
//		buf.append("    }\n");
//		buf.append("}\n");
//		expected[1]= buf.toString();
//
//		buf= new StringBuffer();
//		buf.append("package test1;\n");
//		buf.append("public class E1 {\n");
//		buf.append("    public void foo() {\n");
//		buf.append("        if (condition) {\n");
//		buf.append("            System.out.println(1);\n");
//		buf.append("        }\n");
//		buf.append("    }\n");
//		buf.append("}\n");
//		expected[2]= buf.toString();
//
//		buf= new StringBuffer();
//		buf.append("package test1;\n");
//		buf.append("public class E1 {\n");
//		buf.append("    public void foo() {\n");
//		buf.append("        new Runnable() {\n");
//		buf.append("            public void run() {\n");
//		buf.append("                System.out.println(1);\n");
//		buf.append("            }\n");
//		buf.append("        }\n");
//		buf.append("    }\n");
//		buf.append("}\n");
//		expected[3]= buf.toString();
//
//		buf= new StringBuffer();
//		buf.append("package test1;\n");
//		buf.append("public class E1 {\n");
//		buf.append("    public void foo() {\n");
//		buf.append("        synchronized (mutex) {\n");
//		buf.append("            System.out.println(1);\n");
//		buf.append("        }\n");
//		buf.append("    }\n");
//		buf.append("}\n");
//		expected[4]= buf.toString();
//
//		buf= new StringBuffer();
//		buf.append("package test1;\n");
//		buf.append("public class E1 {\n");
//		buf.append("    public void foo() {\n");
//		buf.append("        try {\n");
//		buf.append("            System.out.println(1);\n");
//		buf.append("        } catch (Exception e) {\n");
//		buf.append("            // TODO: handle exception\n");
//		buf.append("        }\n");
//		buf.append("    }\n");
//		buf.append("}\n");
//		expected[5]= buf.toString();
//
//		buf= new StringBuffer();
//		buf.append("package test1;\n");
//		buf.append("public class E1 {\n");
//		buf.append("    public void foo() {\n");
//		buf.append("        while (condition) {\n");
//		buf.append("            System.out.println(1);\n");
//		buf.append("        }\n");
//		buf.append("    }\n");
//		buf.append("}\n");
//		expected[6]= buf.toString();
//
//		assertExpectedExistInProposals(proposals, expected);
//	}
//
//	public void testSurroundWithTemplate02() throws Exception {
//		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
//		StringBuffer buf= new StringBuffer();
//		buf.append("package test1;\n");
//		buf.append("public class E1 {\n");
//		buf.append("    public void foo() {\n");
//		buf.append("        int i= 10;\n");
//		buf.append("        System.out.println(i);\n");
//		buf.append("    }\n");
//		buf.append("}\n");
//		ICompilationUnit cu= pack1.createCompilationUnit("E1.java", buf.toString(), false, null);
//
//		String selection= "System.out.println(i);";
//		int offset= buf.toString().indexOf(selection);
//
//		AssistContext context= getCorrectionContext(cu, offset, selection.length());
//		assertNoErrors(context);
//		List proposals= Arrays.asList(new QuickTemplateProcessor().getAssists(context, null));
//
//		assertCorrectLabels(proposals);
//		assertNumberOfProposals(proposals, 7);
//
//		String[] expected= new String[7];
//
//		buf= new StringBuffer();
//		buf.append("package test1;\n");
//		buf.append("public class E1 {\n");
//		buf.append("    public void foo() {\n");
//		buf.append("        int i= 10;\n");
//		buf.append("        do {\n");
//		buf.append("            System.out.println(i);\n");
//		buf.append("        } while (condition);\n");
//		buf.append("    }\n");
//		buf.append("}\n");
//		expected[0]= buf.toString();
//
//		buf= new StringBuffer();
//		buf.append("package test1;\n");
//		buf.append("public class E1 {\n");
//		buf.append("    public void foo() {\n");
//		buf.append("        int i= 10;\n");
//		buf.append("        for (int j = 0; j < array.length; j++) {\n");
//		buf.append("            System.out.println(i);\n");
//		buf.append("        }\n");
//		buf.append("    }\n");
//		buf.append("}\n");
//		expected[1]= buf.toString();
//
//		buf= new StringBuffer();
//		buf.append("package test1;\n");
//		buf.append("public class E1 {\n");
//		buf.append("    public void foo() {\n");
//		buf.append("        int i= 10;\n");
//		buf.append("        if (condition) {\n");
//		buf.append("            System.out.println(i);\n");
//		buf.append("        }\n");
//		buf.append("    }\n");
//		buf.append("}\n");
//		expected[2]= buf.toString();
//
//		buf= new StringBuffer();
//		buf.append("package test1;\n");
//		buf.append("public class E1 {\n");
//		buf.append("    public void foo() {\n");
//		buf.append("        final int i= 10;\n");
//		buf.append("        new Runnable() {\n");
//		buf.append("            public void run() {\n");
//		buf.append("                System.out.println(i);\n");
//		buf.append("            }\n");
//		buf.append("        }\n");
//		buf.append("    }\n");
//		buf.append("}\n");
//		expected[3]= buf.toString();
//
//		buf= new StringBuffer();
//		buf.append("package test1;\n");
//		buf.append("public class E1 {\n");
//		buf.append("    public void foo() {\n");
//		buf.append("        int i= 10;\n");
//		buf.append("        synchronized (mutex) {\n");
//		buf.append("            System.out.println(i);\n");
//		buf.append("        }\n");
//		buf.append("    }\n");
//		buf.append("}\n");
//		expected[4]= buf.toString();
//
//		buf= new StringBuffer();
//		buf.append("package test1;\n");
//		buf.append("public class E1 {\n");
//		buf.append("    public void foo() {\n");
//		buf.append("        int i= 10;\n");
//		buf.append("        try {\n");
//		buf.append("            System.out.println(i);\n");
//		buf.append("        } catch (Exception e) {\n");
//		buf.append("            // TODO: handle exception\n");
//		buf.append("        }\n");
//		buf.append("    }\n");
//		buf.append("}\n");
//		expected[5]= buf.toString();
//
//		buf= new StringBuffer();
//		buf.append("package test1;\n");
//		buf.append("public class E1 {\n");
//		buf.append("    public void foo() {\n");
//		buf.append("        int i= 10;\n");
//		buf.append("        while (condition) {\n");
//		buf.append("            System.out.println(i);\n");
//		buf.append("        }\n");
//		buf.append("    }\n");
//		buf.append("}\n");
//		expected[6]= buf.toString();
//
//		assertExpectedExistInProposals(proposals, expected);
//	}
//
//	public void testSurroundWithTemplate03() throws Exception {
//		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
//		StringBuffer buf= new StringBuffer();
//		buf.append("package test1;\n");
//		buf.append("public class E1 {\n");
//		buf.append("    public void foo() {\n");
//		buf.append("        int i= 10;\n");
//		buf.append("        System.out.println(i);\n");
//		buf.append("        System.out.println(i);\n");
//		buf.append("    }\n");
//		buf.append("}\n");
//		ICompilationUnit cu= pack1.createCompilationUnit("E1.java", buf.toString(), false, null);
//
//		String selection= "int i= 10;\n        System.out.println(i);";
//		int offset= buf.toString().indexOf(selection);
//
//		AssistContext context= getCorrectionContext(cu, offset, selection.length());
//		assertNoErrors(context);
//		List proposals= Arrays.asList(new QuickTemplateProcessor().getAssists(context, null));
//
//		assertCorrectLabels(proposals);
//		assertNumberOfProposals(proposals, 7);
//
//		String[] expected= new String[7];
//
//		buf= new StringBuffer();
//		buf.append("package test1;\n");
//		buf.append("public class E1 {\n");
//		buf.append("    public void foo() {\n");
//		buf.append("        int i;\n");
//		buf.append("        do {\n");
//		buf.append("            i = 10;\n");
//		buf.append("            System.out.println(i);\n");
//		buf.append("        } while (condition);\n");
//		buf.append("        System.out.println(i);\n");
//		buf.append("    }\n");
//		buf.append("}\n");
//		expected[0]= buf.toString();
//
//		buf= new StringBuffer();
//		buf.append("package test1;\n");
//		buf.append("public class E1 {\n");
//		buf.append("    public void foo() {\n");
//		buf.append("        int i;\n");
//		buf.append("        for (int j = 0; j < array.length; j++) {\n");
//		buf.append("            i = 10;\n");
//		buf.append("            System.out.println(i);\n");
//		buf.append("        }\n");
//		buf.append("        System.out.println(i);\n");
//		buf.append("    }\n");
//		buf.append("}\n");
//		expected[1]= buf.toString();
//
//		buf= new StringBuffer();
//		buf.append("package test1;\n");
//		buf.append("public class E1 {\n");
//		buf.append("    public void foo() {\n");
//		buf.append("        int i;\n");
//		buf.append("        if (condition) {\n");
//		buf.append("            i = 10;\n");
//		buf.append("            System.out.println(i);\n");
//		buf.append("        }\n");
//		buf.append("        System.out.println(i);\n");
//		buf.append("    }\n");
//		buf.append("}\n");
//		expected[2]= buf.toString();
//
//		buf= new StringBuffer();
//		buf.append("package test1;\n");
//		buf.append("public class E1 {\n");
//		buf.append("    public void foo() {\n");
//		buf.append("        int i;\n");
//		buf.append("        new Runnable() {\n");
//		buf.append("            public void run() {\n");
//		buf.append("                i = 10;\n");
//		buf.append("                System.out.println(i);\n");
//		buf.append("            }\n");
//		buf.append("        }\n");
//		buf.append("        System.out.println(i);\n");
//		buf.append("    }\n");
//		buf.append("}\n");
//		expected[3]= buf.toString();
//
//		buf= new StringBuffer();
//		buf.append("package test1;\n");
//		buf.append("public class E1 {\n");
//		buf.append("    public void foo() {\n");
//		buf.append("        int i;\n");
//		buf.append("        synchronized (mutex) {\n");
//		buf.append("            i = 10;\n");
//		buf.append("            System.out.println(i);\n");
//		buf.append("        }\n");
//		buf.append("        System.out.println(i);\n");
//		buf.append("    }\n");
//		buf.append("}\n");
//		expected[4]= buf.toString();
//
//		buf= new StringBuffer();
//		buf.append("package test1;\n");
//		buf.append("public class E1 {\n");
//		buf.append("    public void foo() {\n");
//		buf.append("        int i;\n");
//		buf.append("        try {\n");
//		buf.append("            i = 10;\n");
//		buf.append("            System.out.println(i);\n");
//		buf.append("        } catch (Exception e) {\n");
//		buf.append("            // TODO: handle exception\n");
//		buf.append("        }\n");
//		buf.append("        System.out.println(i);\n");
//		buf.append("    }\n");
//		buf.append("}\n");
//		expected[5]= buf.toString();
//
//		buf= new StringBuffer();
//		buf.append("package test1;\n");
//		buf.append("public class E1 {\n");
//		buf.append("    public void foo() {\n");
//		buf.append("        int i;\n");
//		buf.append("        while (condition) {\n");
//		buf.append("            i = 10;\n");
//		buf.append("            System.out.println(i);\n");
//		buf.append("        }\n");
//		buf.append("        System.out.println(i);\n");
//		buf.append("    }\n");
//		buf.append("}\n");
//		expected[6]= buf.toString();
//
//		assertExpectedExistInProposals(proposals, expected);
//	}
//
//	public void testSurroundWithTemplate04() throws Exception {
//		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
//		StringBuffer buf= new StringBuffer();
//		buf.append("package test1;\n");
//		buf.append("public class E1 {\n");
//		buf.append("    public void foo() {\n");
//		buf.append("        int i= 10;\n");
//		buf.append("        System.out.println(i);\n");
//		buf.append("        System.out.println(i);\n");
//		buf.append("    }\n");
//		buf.append("}\n");
//		ICompilationUnit cu= pack1.createCompilationUnit("E1.java", buf.toString(), false, null);
//
//		String selection= "System.out.println(i);";
//		int offset= buf.toString().indexOf(selection);
//
//		AssistContext context= getCorrectionContext(cu, offset, selection.length());
//		assertNoErrors(context);
//		List proposals= Arrays.asList(new QuickTemplateProcessor().getAssists(context, null));
//
//		assertCorrectLabels(proposals);
//		assertNumberOfProposals(proposals, 7);
//
//		String[] expected= new String[7];
//
//		buf= new StringBuffer();
//		buf.append("package test1;\n");
//		buf.append("public class E1 {\n");
//		buf.append("    public void foo() {\n");
//		buf.append("        int i= 10;\n");
//		buf.append("        do {\n");
//		buf.append("            System.out.println(i);\n");
//		buf.append("        } while (condition);\n");
//		buf.append("        System.out.println(i);\n");
//		buf.append("    }\n");
//		buf.append("}\n");
//		expected[0]= buf.toString();
//
//		buf= new StringBuffer();
//		buf.append("package test1;\n");
//		buf.append("public class E1 {\n");
//		buf.append("    public void foo() {\n");
//		buf.append("        int i= 10;\n");
//		buf.append("        for (int j = 0; j < array.length; j++) {\n");
//		buf.append("            System.out.println(i);\n");
//		buf.append("        }\n");
//		buf.append("        System.out.println(i);\n");
//		buf.append("    }\n");
//		buf.append("}\n");
//		expected[1]= buf.toString();
//
//		buf= new StringBuffer();
//		buf.append("package test1;\n");
//		buf.append("public class E1 {\n");
//		buf.append("    public void foo() {\n");
//		buf.append("        int i= 10;\n");
//		buf.append("        if (condition) {\n");
//		buf.append("            System.out.println(i);\n");
//		buf.append("        }\n");
//		buf.append("        System.out.println(i);\n");
//		buf.append("    }\n");
//		buf.append("}\n");
//		expected[2]= buf.toString();
//
//		buf= new StringBuffer();
//		buf.append("package test1;\n");
//		buf.append("public class E1 {\n");
//		buf.append("    public void foo() {\n");
//		buf.append("        final int i= 10;\n");
//		buf.append("        new Runnable() {\n");
//		buf.append("            public void run() {\n");
//		buf.append("                System.out.println(i);\n");
//		buf.append("            }\n");
//		buf.append("        }\n");
//		buf.append("        System.out.println(i);\n");
//		buf.append("    }\n");
//		buf.append("}\n");
//		expected[3]= buf.toString();
//
//		buf= new StringBuffer();
//		buf.append("package test1;\n");
//		buf.append("public class E1 {\n");
//		buf.append("    public void foo() {\n");
//		buf.append("        int i= 10;\n");
//		buf.append("        synchronized (mutex) {\n");
//		buf.append("            System.out.println(i);\n");
//		buf.append("        }\n");
//		buf.append("        System.out.println(i);\n");
//		buf.append("    }\n");
//		buf.append("}\n");
//		expected[4]= buf.toString();
//
//		buf= new StringBuffer();
//		buf.append("package test1;\n");
//		buf.append("public class E1 {\n");
//		buf.append("    public void foo() {\n");
//		buf.append("        int i= 10;\n");
//		buf.append("        try {\n");
//		buf.append("            System.out.println(i);\n");
//		buf.append("        } catch (Exception e) {\n");
//		buf.append("            // TODO: handle exception\n");
//		buf.append("        }\n");
//		buf.append("        System.out.println(i);\n");
//		buf.append("    }\n");
//		buf.append("}\n");
//		expected[5]= buf.toString();
//
//		buf= new StringBuffer();
//		buf.append("package test1;\n");
//		buf.append("public class E1 {\n");
//		buf.append("    public void foo() {\n");
//		buf.append("        int i= 10;\n");
//		buf.append("        while (condition) {\n");
//		buf.append("            System.out.println(i);\n");
//		buf.append("        }\n");
//		buf.append("        System.out.println(i);\n");
//		buf.append("    }\n");
//		buf.append("}\n");
//		expected[6]= buf.toString();
//
//		assertExpectedExistInProposals(proposals, expected);
//	}

    @Test
	public void testPickOutStringProposals1() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        String string = \"Hello World\";\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("llo");
		int length= "llo".length();
		AssistContext context= getCorrectionContext(cu, offset, length);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        String string = \"He\" + \"llo\" + \" World\";\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected });

	}

    @Test
	public void testPickOutStringProposals2() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        String string = \"Hello World\";\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("Hel");
		int length= "Hel".length();
		AssistContext context= getCorrectionContext(cu, offset, length);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        String string = \"Hel\" + \"lo World\";\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected });

	}

    @Test
	public void testPickOutStringProposals3() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        String string = \"Hello World\";\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("World");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		List proposals= collectAssists(context, false);

		assertProposalDoesNotExist(proposals, "Pick out selected part of String");

	}

    @Test
	public void testPickOutStringProposals4() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        String string = \"Hello World\";\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("Hello");
		int length= "Hello World".length();
		AssistContext context= getCorrectionContext(cu, offset, length);
		List proposals= collectAssists(context, false);

		assertProposalDoesNotExist(proposals, "Pick out selected part of String");

	}

    @Test
	public void testCombineStringsProposals1() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        String string = \"Hello\" + \" World\";\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("\"Hello\"");
		int length= "\"Hello\" + \"World\"".length();
		AssistContext context= getCorrectionContext(cu, offset, length);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);
		
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        String string = \"Hello World\";\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected });

	}

    @Test
	public void testCombineStringsProposals2() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        String string = \"Hello\" + \" \" + \"World\";\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("\"Hello\"");
		int length= "\"Hello\" + \" \" + \"World\"".length();
		AssistContext context= getCorrectionContext(cu, offset, length);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);
		
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        String string = \"Hello World\";\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected });

	}

    @Test
	public void testCombineStringsProposals3() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        String string = \"Hello World\";\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("\"Hello World\"");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		List proposals= collectAssists(context, false);

		assertProposalDoesNotExist(proposals, CorrectionMessages.AdvancedQuickAssistProcessor_combineSelectedStrings);

	}

    @Test
	public void testCombineStringsProposals4() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        System.out.println(\"Hello\" + \" \" + \"World\");\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("\"Hello\"");
		int length= "\"Hello\" + \" \" + \"World\"".length();
		AssistContext context= getCorrectionContext(cu, offset, length);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);
		
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        System.out.println(\"Hello World\");\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected });

	}

    @Test
	public void testCombineStringsProposals5() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        String string = \"Hello\" + \"World\" + 2;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("\"Hello\" + \"World\"");
		int length= "\"Hello\" + \"World\"".length();
		AssistContext context= getCorrectionContext(cu, offset, length);
		List proposals= collectAssists(context, false);

		assertProposalDoesNotExist(proposals, CorrectionMessages.AdvancedQuickAssistProcessor_combineSelectedStrings);

	}

    @Test
	public void testConvertToIfReturn1() throws Exception {
		// positive cases
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo1() {\n");
		buf.append("        if (a) {\n");
		buf.append("            System.out.println(\"1\");\n");
		buf.append("            System.out.println(\"11\");\n");
		buf.append("        }\n");
		buf.append("    }\n\n");
		buf.append("    public void foo2() {\n");
		buf.append("        bar();\n");
		buf.append("        if (b) {\n");
		buf.append("            System.out.println(\"2\");\n");
		buf.append("            System.out.println(\"22\");\n");
		buf.append("        }\n");
		buf.append("    }\n\n");
		buf.append("    public void foo3() {\n");
		buf.append("        if (c) {\n");
		buf.append("            if (d) {\n");
		buf.append("                System.out.println(\"3\");\n");
		buf.append("                System.out.println(\"33\");\n");
		buf.append("        	}\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		String str= "if (a)";
		AssistContext context= getCorrectionContext(cu, buf.toString().indexOf(str) + str.length(), 0);
		List proposals= collectAssists(context, false);
		assertNumberOfProposals(proposals, 3);
		assertCorrectLabels(proposals);
		StringBuffer buf1= new StringBuffer();
		buf1.append("package test1;\n");
		buf1.append("public class E {\n");
		buf1.append("    public void foo1() {\n");
		buf1.append("        if (!a)\n");
		buf1.append("            return;\n");
		buf1.append("        System.out.println(\"1\");\n");
		buf1.append("        System.out.println(\"11\");\n");
		buf1.append("    }\n\n");
		buf1.append("    public void foo2() {\n");
		buf1.append("        bar();\n");
		buf1.append("        if (b) {\n");
		buf1.append("            System.out.println(\"2\");\n");
		buf1.append("            System.out.println(\"22\");\n");
		buf1.append("        }\n");
		buf1.append("    }\n\n");
		buf1.append("    public void foo3() {\n");
		buf1.append("        if (c) {\n");
		buf1.append("            if (d) {\n");
		buf1.append("                System.out.println(\"3\");\n");
		buf1.append("                System.out.println(\"33\");\n");
		buf1.append("        	}\n");
		buf1.append("        }\n");
		buf1.append("    }\n");
		buf1.append("}\n");
		String expected1= buf1.toString();
		assertExpectedExistInProposals(proposals, new String[] { expected1 });

		str= "if (b)";
		context= getCorrectionContext(cu, buf.toString().indexOf(str) + str.length(), 0);
		proposals= collectAssists(context, false);
		assertNumberOfProposals(proposals, 3);
		assertCorrectLabels(proposals);
		buf1= new StringBuffer();
		buf1.append("package test1;\n");
		buf1.append("public class E {\n");
		buf1.append("    public void foo1() {\n");
		buf1.append("        if (a) {\n");
		buf1.append("            System.out.println(\"1\");\n");
		buf1.append("            System.out.println(\"11\");\n");
		buf1.append("        }\n");
		buf1.append("    }\n\n");
		buf1.append("    public void foo2() {\n");
		buf1.append("        bar();\n");
		buf1.append("        if (!b)\n");
		buf1.append("            return;\n");
		buf1.append("        System.out.println(\"2\");\n");
		buf1.append("        System.out.println(\"22\");\n");
		buf1.append("    }\n\n");
		buf1.append("    public void foo3() {\n");
		buf1.append("        if (c) {\n");
		buf1.append("            if (d) {\n");
		buf1.append("                System.out.println(\"3\");\n");
		buf1.append("                System.out.println(\"33\");\n");
		buf1.append("        	}\n");
		buf1.append("        }\n");
		buf1.append("    }\n");
		buf1.append("}\n");
		String expected2= buf1.toString();
		assertExpectedExistInProposals(proposals, new String[] { expected2 });

		str= "if (d)";
		context= getCorrectionContext(cu, buf.toString().indexOf(str) + str.length(), 0);
		proposals= collectAssists(context, false);
		assertNumberOfProposals(proposals, 5);
		assertCorrectLabels(proposals);
		buf1= new StringBuffer();
		buf1.append("package test1;\n");
		buf1.append("public class E {\n");
		buf1.append("    public void foo1() {\n");
		buf1.append("        if (a) {\n");
		buf1.append("            System.out.println(\"1\");\n");
		buf1.append("            System.out.println(\"11\");\n");
		buf1.append("        }\n");
		buf1.append("    }\n\n");
		buf1.append("    public void foo2() {\n");
		buf1.append("        bar();\n");
		buf1.append("        if (b) {\n");
		buf1.append("            System.out.println(\"2\");\n");
		buf1.append("            System.out.println(\"22\");\n");
		buf1.append("        }\n");
		buf1.append("    }\n\n");
		buf1.append("    public void foo3() {\n");
		buf1.append("        if (c) {\n");
		buf1.append("            if (!d)\n");
		buf1.append("                return;\n");
		buf1.append("            System.out.println(\"3\");\n");
		buf1.append("            System.out.println(\"33\");\n");
		buf1.append("        }\n");
		buf1.append("    }\n");
		buf1.append("}\n");
		String expected3= buf1.toString();
		assertExpectedExistInProposals(proposals, new String[] { expected3 });
	}

    @Test
	public void testConvertToIfReturn2() throws Exception {
		// negative cases
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo1() {\n");
		buf.append("        if (true) {\n");
		buf.append("            System.out.println(\"1\");\n");
		buf.append("            System.out.println(\"2\");\n");
		buf.append("        }\n");
		buf.append("        bar();");
		buf.append("    }\n\n");
		buf.append("    public void foo2() {\n");
		buf.append("        if (a) \n");
		buf.append("            if (b) {\n");
		buf.append("                System.out.println(\"1\");\n");
		buf.append("                System.out.println(\"2\");\n");
		buf.append("        	}\n");
		buf.append("    }\n\n");
		buf.append("    public void foo3() {\n");
		buf.append("        if (c) {\n");
		buf.append("            return;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		String str= "if (true)"; // not the last executable statement in the method
		AssistContext context= getCorrectionContext(cu, buf.toString().indexOf(str) + str.length(), 0);
		List proposals= collectAssists(context, false);
		assertNumberOfProposals(proposals, 2);
		assertCorrectLabels(proposals);
		assertProposalDoesNotExist(proposals, CorrectionMessages.AdvancedQuickAssistProcessor_convertToIfReturn);

		str= "if (b)"; // not present in a block
		context= getCorrectionContext(cu, buf.toString().indexOf(str) + str.length(), 0);
		proposals= collectAssists(context, false);
		assertNumberOfProposals(proposals, 4);
		assertCorrectLabels(proposals);
		assertProposalDoesNotExist(proposals, CorrectionMessages.AdvancedQuickAssistProcessor_convertToIfReturn);

		str= "if (c)"; // no other statement in 'then' part other than 'return'
		context= getCorrectionContext(cu, buf.toString().indexOf(str) + str.length(), 0);
		proposals= collectAssists(context, false);
		assertNumberOfProposals(proposals, 4);
		assertCorrectLabels(proposals);
		assertProposalDoesNotExist(proposals, CorrectionMessages.AdvancedQuickAssistProcessor_convertToIfReturn);
	}

    @Test
	public void testConvertToIfReturn3() throws Exception {
		// 'if' should be in a 'method' returning 'void'
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    static {\n");
		buf.append("        if (a) {\n");
		buf.append("            System.out.println(\"1\");\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("    public String foo1() {\n");
		buf.append("        if (b) {\n");
		buf.append("            System.out.println(\"1\");\n");
		buf.append("            return \"foo\"\n");
		buf.append("        }\n");
		buf.append("    }\n\n");

		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		String str= "if (a)"; // not in a method
		AssistContext context= getCorrectionContext(cu, buf.toString().indexOf(str) + str.length(), 0);
		List proposals= collectAssists(context, false);
		assertNumberOfProposals(proposals, 3);
		assertCorrectLabels(proposals);
		assertProposalDoesNotExist(proposals, CorrectionMessages.AdvancedQuickAssistProcessor_convertToIfReturn);

		str= "if (b)"; // method does not return 'void'
		context= getCorrectionContext(cu, buf.toString().indexOf(str) + str.length(), 0);
		proposals= collectAssists(context, false);
		assertNumberOfProposals(proposals, 2);
		assertCorrectLabels(proposals);
		assertProposalDoesNotExist(proposals, CorrectionMessages.AdvancedQuickAssistProcessor_convertToIfReturn);
	}

    @Test
	public void testConvertToIfReturn4() throws Exception {
		// 'if' should not be in a loop
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo1() {\n");
		buf.append("        for (int i; i < 3; i++) {\n");
		buf.append("            if (a) {\n");
		buf.append("                System.out.println(\"1\");\n");
		buf.append("        	}\n");
		buf.append("        }\n");
		buf.append("    }\n\n");
		buf.append("    public void foo2() {\n");
		buf.append("        List<String> strs= new ArrayList<String>;\n");
		buf.append("        for (String s : strs) {\n");
		buf.append("            if (b) {\n");
		buf.append("                System.out.println(\"2\");\n");
		buf.append("        	}\n");
		buf.append("        }\n");
		buf.append("    }\n\n");
		buf.append("    public void foo3() {\n");
		buf.append("        do {\n");
		buf.append("            if (c) {\n");
		buf.append("                System.out.println(\"3\");\n");
		buf.append("        	}\n");
		buf.append("        } while (true)\n");
		buf.append("    }\n\n");
		buf.append("    public void foo4() {\n");
		buf.append("        while (true) {\n");
		buf.append("            if (d) {\n");
		buf.append("                System.out.println(\"4\");\n");
		buf.append("        	}\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		String str= "if (a)";
		AssistContext context= getCorrectionContext(cu, buf.toString().indexOf(str) + str.length(), 0);
		List proposals= collectAssists(context, false);
		assertNumberOfProposals(proposals, 4);
		assertCorrectLabels(proposals);
		assertProposalDoesNotExist(proposals, CorrectionMessages.AdvancedQuickAssistProcessor_convertToIfReturn);

		str= "if (b)";
		context= getCorrectionContext(cu, buf.toString().indexOf(str) + str.length(), 0);
		proposals= collectAssists(context, false);
		assertNumberOfProposals(proposals, 3);
		assertCorrectLabels(proposals);
		assertProposalDoesNotExist(proposals, CorrectionMessages.AdvancedQuickAssistProcessor_convertToIfReturn);

		str= "if (c)";
		context= getCorrectionContext(cu, buf.toString().indexOf(str) + str.length(), 0);
		proposals= collectAssists(context, false);
		assertNumberOfProposals(proposals, 3);
		assertCorrectLabels(proposals);
		assertProposalDoesNotExist(proposals, CorrectionMessages.AdvancedQuickAssistProcessor_convertToIfReturn);

		str= "if (d)";
		context= getCorrectionContext(cu, buf.toString().indexOf(str) + str.length(), 0);
		proposals= collectAssists(context, false);
		assertNumberOfProposals(proposals, 4);
		assertCorrectLabels(proposals);
		assertProposalDoesNotExist(proposals, CorrectionMessages.AdvancedQuickAssistProcessor_convertToIfReturn);
	}
}
