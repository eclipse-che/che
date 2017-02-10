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
import org.eclipse.che.plugin.java.server.jdt.testplugin.JavaProjectHelper;
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

import java.util.Hashtable;
import java.util.List;

public class AssistQuickFixTest17 extends QuickFixTest {

	private static final String REMOVE_CATCH_CLAUSE= CorrectionMessages.QuickAssistProcessor_removecatchclause_description;
	private static final String REPLACE_CATCH_CLAUSE_WITH_THROWS= CorrectionMessages.QuickAssistProcessor_catchclausetothrows_description;
	private static final String REMOVE_SURROUNDING_TRY_BLOCK= CorrectionMessages.QuickAssistProcessor_unwrap_trystatement;
	private static final String CONVERT_TO_A_SINGLE_MULTI_CATCH_BLOCK= CorrectionMessages.QuickAssistProcessor_convert_to_single_multicatch_block;
	private static final String CONVERT_TO_SEPARATE_CATCH_BLOCKS= CorrectionMessages.QuickAssistProcessor_convert_to_multiple_singletype_catch_blocks;


	private IJavaProject fJProject1;

	private IPackageFragmentRoot fSourceFolder;

	public AssistQuickFixTest17() {
		super(new Java17ProjectTestSetup());
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

		fJProject1 = Java17ProjectTestSetup.getProject();

		fSourceFolder = JavaProjectHelper.addSourceContainer(fJProject1, "src");
	}

    @After
	public void tearDown() throws Exception {
        super.tearDown();
		JavaProjectHelper.clear(fJProject1, Java17ProjectTestSetup.getDefaultClasspath());
	}

    @Test
	public void testConvertToMultiCatch1() throws Exception {
		IPackageFragment pack1 = fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    void foo() {\n");
		buf.append("        try {\n");
		buf.append("            System.out.println(\"foo\");\n");
		buf.append("        } catch (IllegalArgumentException ex) {\n");
		buf.append("            ex.printStackTrace();\n");
		buf.append("        } catch (NullPointerException ex) {\n");
		buf.append("            ex.printStackTrace();\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu = pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset = buf.toString().indexOf("catch");
		AssistContext context = getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals = collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    void foo() {\n");
		buf.append("        try {\n");
		buf.append("            System.out.println(\"foo\");\n");
		buf.append("        } catch (IllegalArgumentException | NullPointerException ex) {\n");
		buf.append("            ex.printStackTrace();\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1 });
	}

    @Test
	public void testConvertToMultiCatch2() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    void foo() {\n");
		buf.append("        try {\n");
		buf.append("            System.out.println(\"foo\");\n");
		buf.append("        } catch (IllegalArgumentException e) {\n");
		buf.append("            e.printStackTrace();\n");
		buf.append("        } catch (NullPointerException e) {\n");
		buf.append("            e.printStackTrace();\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("catch");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    void foo() {\n");
		buf.append("        try {\n");
		buf.append("            System.out.println(\"foo\");\n");
		buf.append("        } catch (IllegalArgumentException | NullPointerException e) {\n");
		buf.append("            e.printStackTrace();\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1 });

	}

    @Test
	public void testConvertToMultiCatch3() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    void foo() {\n");
		buf.append("        try {\n");
		buf.append("            System.out.println(\"foo\");\n");
		buf.append("        } catch (IllegalArgumentException | NullPointerException e) {\n");
		buf.append("            e.printStackTrace();\n");
		buf.append("        } catch (RuntimeException e) {\n");
		buf.append("            e.printStackTrace();\n");
		buf.append("        }\n");
		buf.append("        // a comment at the end\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("catch");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    void foo() {\n");
		buf.append("        try {\n");
		buf.append("            System.out.println(\"foo\");\n");
		buf.append("        } catch (IllegalArgumentException | NullPointerException | RuntimeException e) {\n");
		buf.append("            e.printStackTrace();\n");
		buf.append("        }\n");
		buf.append("        // a comment at the end\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1 });
	}

    @Test
	public void testConvertToMultiCatch4() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    void foo() {\n");
		buf.append("        try {\n");
		buf.append("            System.out.println(\"foo\");\n");
		buf.append("        } catch (IllegalArgumentException e) {\n");
		buf.append("            e.printStackTrace();\n");
		buf.append("        } catch (NullPointerException e) {\n");
		buf.append("            \n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("catch");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertProposalDoesNotExist(proposals, CONVERT_TO_A_SINGLE_MULTI_CATCH_BLOCK);
	}

    @Test
	public void testConvertToMultiCatch5() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    void foo() {\n");
		buf.append("        try {\n");
		buf.append("            System.out.println(\"foo\");\n");
		buf.append("        } catch (IllegalArgumentException e) {\n");
		buf.append("            e.printStackTrace();\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("catch");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertProposalDoesNotExist(proposals, CONVERT_TO_A_SINGLE_MULTI_CATCH_BLOCK);
	}

    @Test
	public void testConvertToMultiCatch6() throws Exception {
		//Quick assist should not be offered in 1.5 mode
		JavaProjectHelper.set15CompilerOptions(fJProject1);
		try {
			IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
			StringBuffer buf= new StringBuffer();
			buf.append("package test1;\n");
			buf.append("public class E {\n");
			buf.append("    void foo() {\n");
			buf.append("        try {\n");
			buf.append("            System.out.println(\"foo\");\n");
			buf.append("        } catch (IllegalArgumentException e) {\n");
			buf.append("            e.printStackTrace();\n");
			buf.append("        } catch (NullPointerException e) {\n");
			buf.append("            e.printStackTrace();\n");
			buf.append("        }\n");
			buf.append("    }\n");
			buf.append("}\n");
			ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

			int offset= buf.toString().indexOf("catch");
			AssistContext context= getCorrectionContext(cu, offset, 0);
			assertNoErrors(context);
			List proposals= collectAssists(context, false);

			assertProposalDoesNotExist(proposals, CONVERT_TO_A_SINGLE_MULTI_CATCH_BLOCK);
		} finally {
			JavaProjectHelper.set17CompilerOptions(fJProject1);
		}
	}

    @Test
	public void testUnrollMultiCatch1() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    void foo() {\n");
		buf.append("        try {\n");
		buf.append("            System.out.println(\"foo\");\n");
		buf.append("        } catch (IllegalArgumentException | NullPointerException ex) {\n");
		buf.append("            ex.printStackTrace();\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("catch");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    void foo() {\n");
		buf.append("        try {\n");
		buf.append("            System.out.println(\"foo\");\n");
		buf.append("        } catch (IllegalArgumentException ex) {\n");
		buf.append("            ex.printStackTrace();\n");
		buf.append("        } catch (NullPointerException ex) {\n");
		buf.append("            ex.printStackTrace();\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1 });
	}

    @Test
	public void testUnrollMultiCatch2() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    void foo() {\n");
		buf.append("        try {\n");
		buf.append("            System.out.println(\"foo\");\n");
		buf.append("        } catch (IllegalArgumentException | NullPointerException e) {\n");
		buf.append("            e.printStackTrace();\n");
		buf.append("        } catch (RuntimeException e) {\n");
		buf.append("            e.printStackTrace();\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("catch");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    void foo() {\n");
		buf.append("        try {\n");
		buf.append("            System.out.println(\"foo\");\n");
		buf.append("        } catch (IllegalArgumentException e) {\n");
		buf.append("            e.printStackTrace();\n");
		buf.append("        } catch (NullPointerException e) {\n");
		buf.append("            e.printStackTrace();\n");
		buf.append("        } catch (RuntimeException e) {\n");
		buf.append("            e.printStackTrace();\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1 });
	}

    @Test
	public void testUnrollMultiCatch3() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    void foo() {\n");
		buf.append("        try {\n");
		buf.append("            System.out.println(\"foo\");\n");
		buf.append("        } catch (IllegalArgumentException e) {\n");
		buf.append("            e.printStackTrace();\n");
		buf.append("        } catch (NullPointerException | ClassCastException e) {\n");
		buf.append("            e.printStackTrace();\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("catch (NullPointerException");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    void foo() {\n");
		buf.append("        try {\n");
		buf.append("            System.out.println(\"foo\");\n");
		buf.append("        } catch (IllegalArgumentException e) {\n");
		buf.append("            e.printStackTrace();\n");
		buf.append("        } catch (NullPointerException e) {\n");
		buf.append("            e.printStackTrace();\n");
		buf.append("        } catch (ClassCastException e) {\n");
		buf.append("            e.printStackTrace();\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1 });
	}

    @Test
	public void testUnrollMultiCatch4() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    void foo() {\n");
		buf.append("        try {\n");
		buf.append("            System.out.println(\"foo\");\n");
		buf.append("        } catch (IllegalArgumentException e) {\n");
		buf.append("            e.printStackTrace();\n");
		buf.append("        } catch (NullPointerException | ClassCastException e) {\n");
		buf.append("            e.printStackTrace();\n");
		buf.append("        } catch (ArrayIndexOutOfBoundsException e) {\n");
		buf.append("            e.printStackTrace();\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("catch (NullPointerException");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    void foo() {\n");
		buf.append("        try {\n");
		buf.append("            System.out.println(\"foo\");\n");
		buf.append("        } catch (IllegalArgumentException e) {\n");
		buf.append("            e.printStackTrace();\n");
		buf.append("        } catch (NullPointerException e) {\n");
		buf.append("            e.printStackTrace();\n");
		buf.append("        } catch (ClassCastException e) {\n");
		buf.append("            e.printStackTrace();\n");
		buf.append("        } catch (ArrayIndexOutOfBoundsException e) {\n");
		buf.append("            e.printStackTrace();\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1 });
	}

    @Test
	public void testUnrollMultiCatch5() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    void foo() {\n");
		buf.append("        try {\n");
		buf.append("            System.out.println(\"foo\");\n");
		buf.append("        } catch (NullPointerException ex) {\n");
		buf.append("            ex.printStackTrace();\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("catch");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertProposalDoesNotExist(proposals, CONVERT_TO_SEPARATE_CATCH_BLOCKS);
	}

    @Test
	public void testUnrollMultiCatch6() throws Exception {
		//https://bugs.eclipse.org/bugs/show_bug.cgi?id=350285#c12
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.lang.reflect.InvocationTargetException;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        try {\n");
		buf.append("            String.class.getConstructor().newInstance();\n");
		buf.append("        } catch (InstantiationException | IllegalAccessException\n");
		buf.append("                | IllegalArgumentException | InvocationTargetException\n");
		buf.append("                | NoSuchMethodException | SecurityException e) {\n");
		buf.append("            e.printStackTrace();\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("catch");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.lang.reflect.InvocationTargetException;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        try {\n");
		buf.append("            String.class.getConstructor().newInstance();\n");
		buf.append("        } catch (InstantiationException e) {\n");
		buf.append("            e.printStackTrace();\n");
		buf.append("        } catch (IllegalAccessException e) {\n");
		buf.append("            e.printStackTrace();\n");
		buf.append("        } catch (IllegalArgumentException e) {\n");
		buf.append("            e.printStackTrace();\n");
		buf.append("        } catch (InvocationTargetException e) {\n");
		buf.append("            e.printStackTrace();\n");
		buf.append("        } catch (NoSuchMethodException e) {\n");
		buf.append("            e.printStackTrace();\n");
		buf.append("        } catch (SecurityException e) {\n");
		buf.append("            e.printStackTrace();\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1 });
	}

    @Test
	public void testReplaceMultiCatchClauseWithThrows1() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        try {\n");
		buf.append("            goo();\n");
		buf.append("        } catch (IllegalArgumentException | NullPointerException e) {\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("catch");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 4);
		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() throws IllegalArgumentException, NullPointerException {\n");
		buf.append("        goo();\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        goo();\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected2= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1, expected2 });
	}

    @Test
	public void testReplaceMultiCatchClauseWithThrows2() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    void foo() {\n");
		buf.append("        try {\n");
		buf.append("            System.out.println(\"foo\");\n");
		buf.append("        } catch (Outer<String>.Inner | NullPointerException ex) {\n");
		buf.append("            ex.printStackTrace();\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		buf.append("class Outer<E> {\n");
		buf.append("    class Inner extends IllegalArgumentException { }\n"); // yes, that's a compile error
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("Inner");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 2);
		assertProposalDoesNotExist(proposals, REMOVE_CATCH_CLAUSE);
		assertProposalDoesNotExist(proposals, REPLACE_CATCH_CLAUSE_WITH_THROWS);
	}

    @Test
	public void testReplaceMultiCatchClauseWithThrows3() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        try {\n");
		buf.append("            goo();\n");
		buf.append("        } catch (IllegalArgumentException | NullPointerException e) {\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("IllegalArgumentException");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 4);
		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        try {\n");
		buf.append("            goo();\n");
		buf.append("        } catch (NullPointerException e) {\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() throws IllegalArgumentException {\n");
		buf.append("        try {\n");
		buf.append("            goo();\n");
		buf.append("        } catch (NullPointerException e) {\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected2= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        try {\n");
		buf.append("            goo();\n");
		buf.append("        } catch (NullPointerException e) {\n");
		buf.append("        } catch (IllegalArgumentException e) {\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected3= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1, expected2, expected3 });
	}

    @Test
	public void testReplaceMultiCatchClauseWithThrows4() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.lang.reflect.InvocationTargetException;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        try {\n");
		buf.append("            String.class.getConstructor().newInstance();\n");
		buf.append("        } catch (InstantiationException | IllegalAccessException\n");
		buf.append("                | IllegalArgumentException | InvocationTargetException\n");
		buf.append("                | NoSuchMethodException | SecurityException e) {\n");
		buf.append("            e.printStackTrace();\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("IllegalArgumentException");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 4);
		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.lang.reflect.InvocationTargetException;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        try {\n");
		buf.append("            String.class.getConstructor().newInstance();\n");
		buf.append("        } catch (InstantiationException | IllegalAccessException\n");
		buf.append("                | InvocationTargetException\n");
		buf.append("                | NoSuchMethodException | SecurityException e) {\n");
		buf.append("            e.printStackTrace();\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.lang.reflect.InvocationTargetException;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() throws IllegalArgumentException {\n");
		buf.append("        try {\n");
		buf.append("            String.class.getConstructor().newInstance();\n");
		buf.append("        } catch (InstantiationException | IllegalAccessException\n");
		buf.append("                | InvocationTargetException\n");
		buf.append("                | NoSuchMethodException | SecurityException e) {\n");
		buf.append("            e.printStackTrace();\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected2= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.lang.reflect.InvocationTargetException;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        try {\n");
		buf.append("            String.class.getConstructor().newInstance();\n");
		buf.append("        } catch (InstantiationException | IllegalAccessException\n");
		buf.append("                | InvocationTargetException\n");
		buf.append("                | NoSuchMethodException | SecurityException e) {\n");
		buf.append("            e.printStackTrace();\n");
		buf.append("        } catch (IllegalArgumentException e) {\n");
		buf.append("            e.printStackTrace();\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected3= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1, expected2, expected3 });
	}

    @Test
	public void testPickoutTypeFromMulticatch1() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.lang.reflect.InvocationTargetException;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        try {\n");
		buf.append("            String.class.getConstructor().newInstance();\n");
		buf.append("        } catch (InstantiationException | IllegalAccessException\n");
		buf.append("                | IllegalArgumentException | InvocationTargetException\n");
		buf.append("                | NoSuchMethodException | SecurityException e) {\n");
		buf.append("            e.printStackTrace();\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		String string= "IllegalArgumentException | InvocationTargetException";
		int offset= buf.toString().indexOf(string);
		int length= string.length();
		AssistContext context= getCorrectionContext(cu, offset, length);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 5);
		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.lang.reflect.InvocationTargetException;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        String.class.getConstructor().newInstance();\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.lang.reflect.InvocationTargetException;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {\n");
		buf.append("        String.class.getConstructor().newInstance();\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected2= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.lang.reflect.InvocationTargetException;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        try {\n");
		buf.append("            String.class.getConstructor().newInstance();\n");
		buf.append("        } catch (InstantiationException | IllegalAccessException\n");
		buf.append("                | NoSuchMethodException | SecurityException e) {\n");
		buf.append("            e.printStackTrace();\n");
		buf.append("        } catch (IllegalArgumentException | InvocationTargetException e) {\n");
		buf.append("            e.printStackTrace();\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected3= buf.toString();
		
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.lang.reflect.InvocationTargetException;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        try {\n");
		buf.append("            String.class.getConstructor().newInstance();\n");
		buf.append("        } catch (InstantiationException e) {\n");
		buf.append("            e.printStackTrace();\n");
		buf.append("        } catch (IllegalAccessException e) {\n");
		buf.append("            e.printStackTrace();\n");
		buf.append("        } catch (IllegalArgumentException e) {\n");
		buf.append("            e.printStackTrace();\n");
		buf.append("        } catch (InvocationTargetException e) {\n");
		buf.append("            e.printStackTrace();\n");
		buf.append("        } catch (NoSuchMethodException e) {\n");
		buf.append("            e.printStackTrace();\n");
		buf.append("        } catch (SecurityException e) {\n");
		buf.append("            e.printStackTrace();\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected4= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1, expected2, expected3, expected4 });
	}

    @Test
	public void testPickoutTypeFromMulticatch2() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.lang.reflect.InvocationTargetException;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        try {\n");
		buf.append("            String.class.getConstructor().newInstance();\n");
		buf.append("        } catch (InstantiationException | IllegalAccessException\n");
		buf.append("                | IllegalArgumentException | InvocationTargetException\n");
		buf.append("                | java.lang.NoSuchMethodException | SecurityException e) {\n");
		buf.append("            e.printStackTrace();\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		String string= "MethodException";
		int offset= buf.toString().indexOf(string);
		AssistContext context= getCorrectionContext(cu, offset, 0);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 4);
		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.lang.reflect.InvocationTargetException;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        try {\n");
		buf.append("            String.class.getConstructor().newInstance();\n");
		buf.append("        } catch (InstantiationException | IllegalAccessException\n");
		buf.append("                | IllegalArgumentException | InvocationTargetException\n");
		buf.append("                | SecurityException e) {\n");
		buf.append("            e.printStackTrace();\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.lang.reflect.InvocationTargetException;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() throws java.lang.NoSuchMethodException {\n");
		buf.append("        try {\n");
		buf.append("            String.class.getConstructor().newInstance();\n");
		buf.append("        } catch (InstantiationException | IllegalAccessException\n");
		buf.append("                | IllegalArgumentException | InvocationTargetException\n");
		buf.append("                | SecurityException e) {\n");
		buf.append("            e.printStackTrace();\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected2= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.lang.reflect.InvocationTargetException;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        try {\n");
		buf.append("            String.class.getConstructor().newInstance();\n");
		buf.append("        } catch (InstantiationException | IllegalAccessException\n");
		buf.append("                | IllegalArgumentException | InvocationTargetException\n");
		buf.append("                | SecurityException e) {\n");
		buf.append("            e.printStackTrace();\n");
		buf.append("        } catch (java.lang.NoSuchMethodException e) {\n");
		buf.append("            e.printStackTrace();\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected3= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1, expected2, expected3 });
	}

    @Test
	public void testSplitDeclaration1() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    void foo() throws Exception {\n");
		buf.append("        try (FileReader reader = new FileReader(\"file\")) {\n");
		buf.append("            int ch;\n");
		buf.append("            while ((ch = reader.read()) != -1) {\n");
		buf.append("                System.out.println(ch);\n");
		buf.append("            }\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		String str= "reader";
		AssistContext context= getCorrectionContext(cu, buf.toString().indexOf(str), 0);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);

		assertProposalDoesNotExist(proposals, "Split variable declaration");
	}

    @Test
	public void testUnwrapTryStatement() throws Exception {

		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.io.FileReader;\n");
		buf.append("public class E {\n");
		buf.append("    void foo() throws Exception {\n");
		buf.append("        try (FileReader reader1 = new FileReader(\"file\")) {\n");
		buf.append("            int ch;\n");
		buf.append("            while ((ch = reader1.read()) != -1) {\n");
		buf.append("                System.out.println(ch);\n");
		buf.append("            }\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		String str= "try";
		AssistContext context= getCorrectionContext(cu, buf.toString().indexOf(str) + str.length(), 0);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 1);
		assertProposalDoesNotExist(proposals, REMOVE_SURROUNDING_TRY_BLOCK);
	}

    @Test
	public void testInferDiamondArguments() throws Exception {

		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.HashMap;\n");
		buf.append("import java.util.Map;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        Map<String, ? extends Number> m = new HashMap<>(12);\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		String str= "<>";
		AssistContext context= getCorrectionContext(cu, buf.toString().indexOf(str), 0);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);

		CUCorrectionProposal proposal= (CUCorrectionProposal) proposals.get(0);
		String preview= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.HashMap;\n");
		buf.append("import java.util.Map;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        Map<String, ? extends Number> m = new HashMap<String, Number>(12);\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

}
