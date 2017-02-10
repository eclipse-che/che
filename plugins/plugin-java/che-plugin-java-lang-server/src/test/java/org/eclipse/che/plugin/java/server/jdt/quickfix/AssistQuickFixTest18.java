/*******************************************************************************
 * Copyright (c) 2013, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.java.server.jdt.quickfix;


import org.eclipse.che.plugin.java.server.jdt.testplugin.Java18ProjectTestSetup;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jdt.internal.corext.codemanipulation.StubUtility;
import org.eclipse.jdt.internal.corext.fix.FixMessages;
import org.eclipse.jdt.internal.corext.template.java.CodeTemplateContextType;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.text.correction.AssistContext;
import org.eclipse.jdt.internal.ui.text.correction.CorrectionMessages;
import org.eclipse.che.plugin.java.server.jdt.testplugin.JavaProjectHelper;
import org.eclipse.che.plugin.java.server.jdt.testplugin.TestOptions;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Hashtable;
import java.util.List;

public class AssistQuickFixTest18 extends QuickFixTest {

	private static final Class THIS= AssistQuickFixTest18.class;

	private IJavaProject fJProject1;

	private IPackageFragmentRoot fSourceFolder;

	public AssistQuickFixTest18() {
		super(new Java18ProjectTestSetup());
	}

//	public static Test suite() {
//		return setUpTest(new TestSuite(THIS));
//	}
//
//	public static Test setUpTest(Test test) {
//		return new Java18ProjectTestSetup(test);
//	}

	@Before
	public void setUp() throws Exception {
        super.setUp();
		Hashtable options = TestOptions.getDefaultOptions();
		options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, JavaCore.SPACE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, "4");
		JavaCore.setOptions(options);

		IPreferenceStore store = JavaPlugin.getDefault().getPreferenceStore();
		store.setValue(PreferenceConstants.CODEGEN_ADD_COMMENTS, false);

		StubUtility.setCodeTemplate(CodeTemplateContextType.METHODSTUB_ID, "//TODO\n${body_statement}", null);

		fJProject1 = Java18ProjectTestSetup.getProject();
		fSourceFolder = JavaProjectHelper.addSourceContainer(fJProject1, "src");
	}

    @After
	public void tearDown() throws Exception {
        super.tearDown();
		JavaProjectHelper.clear(fJProject1, Java18ProjectTestSetup.getDefaultClasspath());
	}

    @Test
	public void testAssignParamToField1() throws Exception {
		IPackageFragment pack1 = fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public interface I {\n");
		buf.append("    default void foo(int x) {\n");
		buf.append("        System.out.println(x);\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu = pack1.createCompilationUnit("I.java", buf.toString(), false, null);

		int offset = buf.toString().indexOf("x");
		AssistContext context = getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals = collectAssists(context, false);

		assertNumberOfProposals(proposals, 0);
	}

    @Test
	public void testAssignParamToField2() throws Exception {
		IPackageFragment pack1 = fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public interface I {\n");
		buf.append("    static void bar(int x) {\n");
		buf.append("        System.out.println(x);\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("I.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("x");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 0);
	}

    @Test
	public void testConvertToLambda1() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("interface I {\n");
		buf.append("    void method();\n");
		buf.append("}\n");
		buf.append("public class E {\n");
		buf.append("    void bar(I i) {\n");
		buf.append("    }\n");
		buf.append("    void foo() {\n");
		buf.append("        bar(new I() {\n");
		buf.append("            public void method() {\n");
		buf.append("                System.out.println();\n");
		buf.append("                System.out.println();\n");
		buf.append("            }\n");
		buf.append("        });\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("I()");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 2);
		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("interface I {\n");
		buf.append("    void method();\n");
		buf.append("}\n");
		buf.append("public class E {\n");
		buf.append("    void bar(I i) {\n");
		buf.append("    }\n");
		buf.append("    void foo() {\n");
		buf.append("        bar(() -> {\n");
		buf.append("            System.out.println();\n");
		buf.append("            System.out.println();\n");
		buf.append("        });\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1 });
	}

    @Test
	public void testConvertToLambda2() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("interface I {\n");
		buf.append("    void method(int a, int b);\n");
		buf.append("}\n");
		buf.append("public class E {\n");
		buf.append("    void bar(I i) {\n");
		buf.append("    }\n");
		buf.append("    void foo() {\n");
		buf.append("        bar(new I() {\n");
		buf.append("            public void method(int a, int b) {\n");
		buf.append("                System.out.println(a+b);\n");
		buf.append("                System.out.println(a+b);\n");
		buf.append("            }\n");
		buf.append("        });\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("I()");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 2);
		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("interface I {\n");
		buf.append("    void method(int a, int b);\n");
		buf.append("}\n");
		buf.append("public class E {\n");
		buf.append("    void bar(I i) {\n");
		buf.append("    }\n");
		buf.append("    void foo() {\n");
		buf.append("        bar((a, b) -> {\n");
		buf.append("            System.out.println(a+b);\n");
		buf.append("            System.out.println(a+b);\n");
		buf.append("        });\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1 });
	}

    @Test
	public void testConvertToLambda3() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("interface I {\n");
		buf.append("    void method();\n");
		buf.append("    boolean equals(Object obj);\n");
		buf.append("}\n");
		buf.append("public class E {\n");
		buf.append("    void bar(I i) {\n");
		buf.append("    }\n");
		buf.append("    void foo() {\n");
		buf.append("        bar(new I() {\n");
		buf.append("            public void method() {\n");
		buf.append("                System.out.println();\n");
		buf.append("            }\n");
		buf.append("            public boolean equals(Object obj) {\n");
		buf.append("                return false;\n");
		buf.append("            }\n");
		buf.append("        });\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("I()");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 1);
		assertProposalDoesNotExist(proposals, FixMessages.LambdaExpressionsFix_convert_to_lambda_expression);
	}

    @Test
	public void testConvertToLambda4() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("interface I {\n");
		buf.append("    void method();\n");
		buf.append("}\n");
		buf.append("public class E {\n");
		buf.append("    void bar(I i) {\n");
		buf.append("    }\n");
		buf.append("    void foo() {\n");
		buf.append("        bar(new I() {\n");
		buf.append("            int count=0;\n");
		buf.append("            public void method() {\n");
		buf.append("                System.out.println();\n");
		buf.append("                count++;\n");
		buf.append("            }\n");
		buf.append("        });\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("I()");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 1);
		assertProposalDoesNotExist(proposals, FixMessages.LambdaExpressionsFix_convert_to_lambda_expression);
	}

    @Test
	public void testConvertToLambda5() throws Exception {
		//Quick assist should not be offered in 1.7 mode
		JavaProjectHelper.set17CompilerOptions(fJProject1);
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		try {
			StringBuffer buf= new StringBuffer();
			buf.append("package test1;\n");
			buf.append("interface I {\n");
			buf.append("    void method();\n");
			buf.append("}\n");
			buf.append("public class E {\n");
			buf.append("    void bar(I i) {\n");
			buf.append("    }\n");
			buf.append("    void foo() {\n");
			buf.append("        bar(new I() {\n");
			buf.append("            public void method() {\n");
			buf.append("                System.out.println();\n");
			buf.append("            }\n");
			buf.append("        });\n");
			buf.append("    }\n");
			buf.append("}\n");
			ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

			int offset= buf.toString().indexOf("I()");
			AssistContext context= getCorrectionContext(cu, offset, 0);
			assertNoErrors(context);
			List proposals= collectAssists(context, false);

			assertNumberOfProposals(proposals, 1);
			assertProposalDoesNotExist(proposals, FixMessages.LambdaExpressionsFix_convert_to_lambda_expression);
		} finally {
			JavaProjectHelper.set18CompilerOptions(fJProject1);
		}
	}

    @Test
	public void testConvertToLambda6() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("interface I {\n");
		buf.append("    boolean equals(Object obj);\n");
		buf.append("}\n");
		buf.append("public class E {\n");
		buf.append("    void bar(I i) {\n");
		buf.append("    }\n");
		buf.append("    void foo() {\n");
		buf.append("        bar(new I() {\n");
		buf.append("            public boolean equals(Object obj) {\n");
		buf.append("                return false;\n");
		buf.append("            }\n");
		buf.append("        });\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("I()");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 1);
		assertProposalDoesNotExist(proposals, FixMessages.LambdaExpressionsFix_convert_to_lambda_expression);
	}

    @Test
	public void testConvertToLambda7() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("abstract class C {\n");
		buf.append("    abstract void method();\n");
		buf.append("}\n");
		buf.append("public class E {\n");
		buf.append("    void bar(C c) {\n");
		buf.append("    }\n");
		buf.append("    void foo() {\n");
		buf.append("        bar(new C() {\n");
		buf.append("            public void method() {\n");
		buf.append("                System.out.println();\n");
		buf.append("            }\n");
		buf.append("        });\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("C()");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 1);
		assertProposalDoesNotExist(proposals, FixMessages.LambdaExpressionsFix_convert_to_lambda_expression);
	}

    @Test
	public void testConvertToLambda8() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("interface I {\n");
		buf.append("    void method();\n");
		buf.append("}\n");
		buf.append("public class E {\n");
		buf.append("    void bar(I i) {\n");
		buf.append("    }\n");
		buf.append("    void foo() {\n");
		buf.append("        bar(new I() {\n");
		buf.append("            public void method() {\n");
		buf.append("                System.out.println();\n");
		buf.append("            }\n");
		buf.append("        });\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("I()");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 2);
		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("interface I {\n");
		buf.append("    void method();\n");
		buf.append("}\n");
		buf.append("public class E {\n");
		buf.append("    void bar(I i) {\n");
		buf.append("    }\n");
		buf.append("    void foo() {\n");
		buf.append("        bar(() -> System.out.println());\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1 });
	}

    @Test
	public void testConvertToLambda9() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("interface I {\n");
		buf.append("    int method();\n");
		buf.append("}\n");
		buf.append("public class E {\n");
		buf.append("    void bar(I i) {\n");
		buf.append("    }\n");
		buf.append("    void foo() {\n");
		buf.append("        bar(new I() {\n");
		buf.append("            public int method() {\n");
		buf.append("                return 1;\n");
		buf.append("            }\n");
		buf.append("        });\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("I()");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 2);
		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("interface I {\n");
		buf.append("    int method();\n");
		buf.append("}\n");
		buf.append("public class E {\n");
		buf.append("    void bar(I i) {\n");
		buf.append("    }\n");
		buf.append("    void foo() {\n");
		buf.append("        bar(() -> 1);\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1 });
	}

    @Test
	public void testConvertToLambda10() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("interface I {\n");
		buf.append("    int foo(String s);\n");
		buf.append("}\n");
		buf.append("\n");
		buf.append("interface J {\n");
		buf.append("    Integer foo(String s);\n");
		buf.append("}\n");
		buf.append("\n");
		buf.append("public class X {\n");
		buf.append("    static void goo(I i) { }\n");
		buf.append("\n");
		buf.append("    static void goo(J j) { }\n");
		buf.append("\n");
		buf.append("    public static void main(String[] args) {\n");
		buf.append("        goo(new I() {\n");
		buf.append("            @Override\n");
		buf.append("            public int foo(String s) {\n");
		buf.append("                return 0;\n");
		buf.append("            }\n");
		buf.append("        });\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("I()");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 2);
		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("interface I {\n");
		buf.append("    int foo(String s);\n");
		buf.append("}\n");
		buf.append("\n");
		buf.append("interface J {\n");
		buf.append("    Integer foo(String s);\n");
		buf.append("}\n");
		buf.append("\n");
		buf.append("public class X {\n");
		buf.append("    static void goo(I i) { }\n");
		buf.append("\n");
		buf.append("    static void goo(J j) { }\n");
		buf.append("\n");
		buf.append("    public static void main(String[] args) {\n");
		buf.append("        goo((I) s -> 0);\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1 });
	}

    @Test
	public void testConvertToLambda11() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("interface I {\n");
		buf.append("    int foo(String s);\n");
		buf.append("}\n");
		buf.append("\n");
		buf.append("interface J {\n");
		buf.append("    Integer foo(String s);\n");
		buf.append("}\n");
		buf.append("\n");
		buf.append("public class X extends Y {\n");
		buf.append("    static void goo(I i) { }\n");
		buf.append("    public static void main(String[] args) {\n");
		buf.append("        goo(new I() {\n");
		buf.append("            @Override\n");
		buf.append("            public int foo(String s) {\n");
		buf.append("                return 0;\n");
		buf.append("            }\n");
		buf.append("        });\n");
		buf.append("    }\n");
		buf.append("}\n");
		buf.append("\n");
		buf.append("class Y {\n");
		buf.append("    private static void goo(J j) { }    \n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("I()");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 2);
		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("interface I {\n");
		buf.append("    int foo(String s);\n");
		buf.append("}\n");
		buf.append("\n");
		buf.append("interface J {\n");
		buf.append("    Integer foo(String s);\n");
		buf.append("}\n");
		buf.append("\n");
		buf.append("public class X extends Y {\n");
		buf.append("    static void goo(I i) { }\n");
		buf.append("    public static void main(String[] args) {\n");
		buf.append("        goo(s -> 0);\n");
		buf.append("    }\n");
		buf.append("}\n");
		buf.append("\n");
		buf.append("class Y {\n");
		buf.append("    private static void goo(J j) { }    \n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1 });
	}

    @Test
	public void testConvertToLambda12() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("interface I {\n");
		buf.append("    int foo(String s);\n");
		buf.append("}\n");
		buf.append("\n");
		buf.append("interface J {\n");
		buf.append("    Integer foo(String s);\n");
		buf.append("}\n");
		buf.append("\n");
		buf.append("public class X extends Y {\n");
		buf.append("    static void goo(I i) { }\n");
		buf.append("    public static void main(String[] args) {\n");
		buf.append("        goo(new I() {\n");
		buf.append("            @Override\n");
		buf.append("            public int foo(String s) {\n");
		buf.append("                return 0;\n");
		buf.append("            }\n");
		buf.append("        });\n");
		buf.append("    }\n");
		buf.append("}\n");
		buf.append("\n");
		buf.append("class Y {\n");
		buf.append("    static void goo(J j) { }    \n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("I()");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 2);
		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("interface I {\n");
		buf.append("    int foo(String s);\n");
		buf.append("}\n");
		buf.append("\n");
		buf.append("interface J {\n");
		buf.append("    Integer foo(String s);\n");
		buf.append("}\n");
		buf.append("\n");
		buf.append("public class X extends Y {\n");
		buf.append("    static void goo(I i) { }\n");
		buf.append("    public static void main(String[] args) {\n");
		buf.append("        goo((I) s -> 0);\n");
		buf.append("    }\n");
		buf.append("}\n");
		buf.append("\n");
		buf.append("class Y {\n");
		buf.append("    static void goo(J j) { }    \n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1 });
	}

    @Test
	public void testConvertToLambda13() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("interface J {\n");
		buf.append("    <M> J run(M x);\n");
		buf.append("}\n");
		buf.append("\n");
		buf.append("class Test {\n");
		buf.append("    J j = new J() {\n");
		buf.append("        @Override\n");
		buf.append("        public <M> J run(M x) {\n");
		buf.append("            return null;\n");
		buf.append("        }\n");
		buf.append("    };    \n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("Test.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("J()"); // generic lambda not allowed
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);
		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);
		assertProposalDoesNotExist(proposals, FixMessages.LambdaExpressionsFix_convert_to_lambda_expression);
	}

    @Test
	public void testConvertToLambda14() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("@FunctionalInterface\n");
		buf.append("interface FI {\n");
		buf.append("    int foo(int x, int y, int z);\n");
		buf.append("}\n");
		buf.append("\n");
		buf.append("class C {\n");
		buf.append("    int i;\n");
		buf.append("    private void test(int x) {\n");
		buf.append("        int y;\n");
		buf.append("        FI fi = new FI() {\n");
		buf.append("            @Override\n");
		buf.append("            public int foo(int x/*km*/, int i /*inches*/, int y/*yards*/) {\n");
		buf.append("                return x + i + y;\n");
		buf.append("            }\n");
		buf.append("        };\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("FI()");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 2);
		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("@FunctionalInterface\n");
		buf.append("interface FI {\n");
		buf.append("    int foo(int x, int y, int z);\n");
		buf.append("}\n");
		buf.append("\n");
		buf.append("class C {\n");
		buf.append("    int i;\n");
		buf.append("    private void test(int x) {\n");
		buf.append("        int y;\n");
		buf.append("        FI fi = (x1/*km*/, i /*inches*/, y1/*yards*/) -> x1 + i + y1;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1 });
	}

    @Test
	public void testConvertToLambda15() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("@FunctionalInterface\n");
		buf.append("interface FI {\n");
		buf.append("    int foo(int x, int y, int z);\n");
		buf.append("}\n");
		buf.append("\n");
		buf.append("class C {\n");
		buf.append("    int i;\n");
		buf.append("    private void test(int x, int y, int z) {\n");
		buf.append("        FI fi = new FI() {\n");
		buf.append("            @Override\n");
		buf.append("            public int foo(int a, int b, int z) {\n");
		buf.append("                int x= 0, y=0; \n");
		buf.append("                return x + y + z;\n");
		buf.append("            }\n");
		buf.append("        };\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("FI()");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 2);
		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("@FunctionalInterface\n");
		buf.append("interface FI {\n");
		buf.append("    int foo(int x, int y, int z);\n");
		buf.append("}\n");
		buf.append("\n");
		buf.append("class C {\n");
		buf.append("    int i;\n");
		buf.append("    private void test(int x, int y, int z) {\n");
		buf.append("        FI fi = (a, b, z1) -> {\n");
		buf.append("            int x1= 0, y1=0; \n");
		buf.append("            return x1 + y1 + z1;\n");
		buf.append("        };\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1 });
	}

    @Test
	public void testConvertToLambda16() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("interface FI {\n");
		buf.append("    void foo();\n");
		buf.append("}\n");
		buf.append("\n");
		buf.append("class C1 {\n");
		buf.append("    void fun1() {\n");
		buf.append("        int c = 0; // [1]\n");
		buf.append("        FI test = new FI() {\n");
		buf.append("            @Override\n");
		buf.append("            public void foo() {\n");
		buf.append("                for (int c = 0; c < 10;) { /* [2] */ }\n");
		buf.append("                for (int c = 0; c < 20;) { /* [3] */ }\n");
		buf.append("            }\n");
		buf.append("        };\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C1.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("FI()");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 2);
		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("interface FI {\n");
		buf.append("    void foo();\n");
		buf.append("}\n");
		buf.append("\n");
		buf.append("class C1 {\n");
		buf.append("    void fun1() {\n");
		buf.append("        int c = 0; // [1]\n");
		buf.append("        FI test = () -> {\n");
		buf.append("            for (int c1 = 0; c1 < 10;) { /* [2] */ }\n");
		buf.append("            for (int c2 = 0; c2 < 20;) { /* [3] */ }\n");
		buf.append("        };\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1 });
	}

    @Test
	public void testConvertToLambda17() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("interface X {\n");
		buf.append("    void foo();\n");
		buf.append("}\n");
		buf.append("\n");
		buf.append("public class CX {\n");
		buf.append("    private void fun(int a) {\n");
		buf.append("        X x= new X() {\n");
		buf.append("            @Override\n");
		buf.append("            public void foo() {\n");
		buf.append("                int a; \n");
		buf.append("                int a1;\n");
		buf.append("            }\n");
		buf.append("        };\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("CX.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("X()");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 2);
		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("interface X {\n");
		buf.append("    void foo();\n");
		buf.append("}\n");
		buf.append("\n");
		buf.append("public class CX {\n");
		buf.append("    private void fun(int a) {\n");
		buf.append("        X x= () -> {\n");
		buf.append("            int a2; \n");
		buf.append("            int a1;\n");
		buf.append("        };\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1 });
	}

    @Test
	public void testConvertToLambda18() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("interface FIOther {\n");
		buf.append("    void run(int x);\n");
		buf.append("}\n");
		buf.append("\n");
		buf.append("public class TestOther {\n");
		buf.append("    void init() {\n");
		buf.append("        String x;\n");
		buf.append("        m(x1 -> {\n");
		buf.append("            FIOther fi = new FIOther() {\n");
		buf.append("                @Override\n");
		buf.append("                public void run(int x1) { \n");
		buf.append("                    return;\n");
		buf.append("                }\n");
		buf.append("            };\n");
		buf.append("        });\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("    void m(FIOther fi) {\n");
		buf.append("    };\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("TestOther.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("FIOther()");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 2);
		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("interface FIOther {\n");
		buf.append("    void run(int x);\n");
		buf.append("}\n");
		buf.append("\n");
		buf.append("public class TestOther {\n");
		buf.append("    void init() {\n");
		buf.append("        String x;\n");
		buf.append("        m(x1 -> {\n");
		buf.append("            FIOther fi = x11 -> { \n");
		buf.append("                return;\n");
		buf.append("            };\n");
		buf.append("        });\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("    void m(FIOther fi) {\n");
		buf.append("    };\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1 });
	}

    @Test
	public void testConvertToLambdaAmbiguousOverridden() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.ArrayList;\n");
		buf.append("import java.util.function.Predicate;\n");
		buf.append("\n");
		buf.append("public class Test {\n");
		buf.append("    void foo(ArrayList<String> list) {\n");
		buf.append("        list.removeIf(new Predicate<String>() {\n");
		buf.append("            @Override\n");
		buf.append("            public boolean test(String t) {\n");
		buf.append("                return t.isEmpty();\n");
		buf.append("            }\n");
		buf.append("        });\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("Test.java", buf.toString(), false, null);
		
		int offset= buf.toString().indexOf("public boolean test(");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.ArrayList;\n");
		buf.append("\n");
		buf.append("public class Test {\n");
		buf.append("    void foo(ArrayList<String> list) {\n");
		buf.append("        list.removeIf(t -> t.isEmpty());\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1 });
	}

    @Test
	public void testConvertToAnonymousClassCreation1() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("interface I {\n");
		buf.append("    void method();\n");
		buf.append("}\n");
		buf.append("public class E {\n");
		buf.append("    void bar(I i) {\n");
		buf.append("    }\n");
		buf.append("    void foo() {\n");
		buf.append("        bar(() -> {\n");
		buf.append("            System.out.println();\n");
		buf.append("            System.out.println();\n");
		buf.append("        });\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("->");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 4);
		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("interface I {\n");
		buf.append("    void method();\n");
		buf.append("}\n");
		buf.append("public class E {\n");
		buf.append("    void bar(I i) {\n");
		buf.append("    }\n");
		buf.append("    void foo() {\n");
		buf.append("        bar(new I() {\n");
		buf.append("            @Override\n");
		buf.append("            public void method() {\n");
		buf.append("                System.out.println();\n");
		buf.append("                System.out.println();\n");
		buf.append("            }\n");
		buf.append("        });\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1 });
	}

    @Test
	public void testConvertToAnonymousClassCreation2() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("interface I {\n");
		buf.append("    void method(int a, int b);\n");
		buf.append("}\n");
		buf.append("public class E {\n");
		buf.append("    void bar(I i) {\n");
		buf.append("    }\n");
		buf.append("    void foo() {\n");
		buf.append("        bar((int a, int b) -> {\n");
		buf.append("            System.out.println(a+b);\n");
		buf.append("            System.out.println(a+b);\n");
		buf.append("        });\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("->");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 3);
		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("interface I {\n");
		buf.append("    void method(int a, int b);\n");
		buf.append("}\n");
		buf.append("public class E {\n");
		buf.append("    void bar(I i) {\n");
		buf.append("    }\n");
		buf.append("    void foo() {\n");
		buf.append("        bar(new I() {\n");
		buf.append("            @Override\n");
		buf.append("            public void method(int a, int b) {\n");
		buf.append("                System.out.println(a+b);\n");
		buf.append("                System.out.println(a+b);\n");
		buf.append("            }\n");
		buf.append("        });\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1 });
	}

    @Test
	public void testConvertToAnonymousClassCreation3() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("interface I {\n");
		buf.append("    void method();\n");
		buf.append("}\n");
		buf.append("public class E {\n");
		buf.append("    void bar(I i) {\n");
		buf.append("    }\n");
		buf.append("    void foo() {\n");
		buf.append("        bar(() -> System.out.println());\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("->");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 5);
		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("interface I {\n");
		buf.append("    void method();\n");
		buf.append("}\n");
		buf.append("public class E {\n");
		buf.append("    void bar(I i) {\n");
		buf.append("    }\n");
		buf.append("    void foo() {\n");
		buf.append("        bar(new I() {\n");
		buf.append("            @Override\n");
		buf.append("            public void method() {\n");
		buf.append("                System.out.println();\n");
		buf.append("            }\n");
		buf.append("        });\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1 });
	}

    @Test
	public void testConvertToAnonymousClassCreation4() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("interface I {\n");
		buf.append("    int method();\n");
		buf.append("}\n");
		buf.append("public class E {\n");
		buf.append("    void bar(I i) {\n");
		buf.append("    }\n");
		buf.append("    void foo() {\n");
		buf.append("        bar(() -> 1);\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("->");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 5);
		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("interface I {\n");
		buf.append("    int method();\n");
		buf.append("}\n");
		buf.append("public class E {\n");
		buf.append("    void bar(I i) {\n");
		buf.append("    }\n");
		buf.append("    void foo() {\n");
		buf.append("        bar(new I() {\n");
		buf.append("            @Override\n");
		buf.append("            public int method() {\n");
		buf.append("                return 1;\n");
		buf.append("            }\n");
		buf.append("        });\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1 });
	}

    @Test
	public void testConvertToAnonymousClassCreation5() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("interface FX {\n");
		buf.append("    default int defaultMethod(String x) {\n");
		buf.append("        return -1;\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("    int foo(int x);\n");
		buf.append("}\n");
		buf.append("\n");
		buf.append("class TestX {\n");
		buf.append("    FX fxx = x -> {\n");
		buf.append("        return (new FX() {\n");
		buf.append("            @Override\n");
		buf.append("            public int foo(int x) {\n");
		buf.append("                return 0;\n");
		buf.append("            }\n");
		buf.append("        }).defaultMethod(\"a\");\n");
		buf.append("    };\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("TestX.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("->");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 2);
		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("interface FX {\n");
		buf.append("    default int defaultMethod(String x) {\n");
		buf.append("        return -1;\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("    int foo(int x);\n");
		buf.append("}\n");
		buf.append("\n");
		buf.append("class TestX {\n");
		buf.append("    FX fxx = new FX() {\n");
		buf.append("        @Override\n");
		buf.append("        public int foo(int x) {\n");
		buf.append("            return (new FX() {\n");
		buf.append("                @Override\n");
		buf.append("                public int foo(int x) {\n");
		buf.append("                    return 0;\n");
		buf.append("                }\n");
		buf.append("            }).defaultMethod(\"a\");\n");
		buf.append("        }\n");
		buf.append("    };\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1 });
	}

    @Test
	public void testConvertToAnonymousClassCreation6() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.function.UnaryOperator;\n");
		buf.append("\n");
		buf.append("public class Snippet {\n");
		buf.append("    UnaryOperator<String> fi3 = x -> {\n");
		buf.append("        return x.toString();\n");
		buf.append("    };\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("Snippet.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("->");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 2);
		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.function.UnaryOperator;\n");
		buf.append("\n");
		buf.append("public class Snippet {\n");
		buf.append("    UnaryOperator<String> fi3 = new UnaryOperator<String>() {\n");
		buf.append("        @Override\n");
		buf.append("        public String apply(String x) {\n");
		buf.append("            return x.toString();\n");
		buf.append("        }\n");
		buf.append("    };\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1 });
	}

    @Test
    @Ignore
	// Bug 427694: [1.8][compiler] Functional interface not identified correctly
	public void _testConvertToAnonymousClassCreation7() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("interface I { Object m(Class c); }\n");
		buf.append("interface J<S> { S m(Class<?> c); }\n");
		buf.append("interface K<T> { T m(Class<?> c); }\n");
		buf.append("interface Functional<S,T> extends I, J<S>, K<T> {}\n");
		buf.append("\n");
		buf.append("class C {\n");
		buf.append("    Functional<?, ?> fun= (c) -> { return null;};\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("->");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 2);
		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("interface I { Object m(Class c); }\n");
		buf.append("interface J<S> { S m(Class<?> c); }\n");
		buf.append("interface K<T> { T m(Class<?> c); }\n");
		buf.append("interface Functional<S,T> extends I, J<S>, K<T> {}\n");
		buf.append("\n");
		buf.append("class C {\n");
		buf.append("    Functional<?, ?> fun= new Functional<Object, Object>() {\n");
		buf.append("        @Override\n");
		buf.append("        public Object m(Class c) {\n");
		buf.append("            return null;\n");
		buf.append("        }\n");
		buf.append("    };\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1 });
	}

    @Test
	public void testConvertToAnonymousClassCreationWithParameterName() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.function.IntFunction;\n");
		buf.append("public class E {\n");
		buf.append("    IntFunction<String> toString= (int i) -> Integer.toString(i);\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);
	
		int offset= buf.toString().indexOf("->");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);
	
		assertNumberOfProposals(proposals, 2);
		assertCorrectLabels(proposals);
	
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.function.IntFunction;\n");
		buf.append("public class E {\n");
		buf.append("    IntFunction<String> toString= new IntFunction<String>() {\n");
		buf.append("        @Override\n");
		buf.append("        public String apply(int i) {\n");
		buf.append("            return Integer.toString(i);\n");
		buf.append("        }\n");
		buf.append("    };\n");
		buf.append("}\n");
		String expected1= buf.toString();
	
		assertExpectedExistInProposals(proposals, new String[] { expected1 });
	}

    @Test
	public void testChangeLambdaBodyToBlock1() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("class E {\n");
		buf.append("    FI1 fi1a= x -> -1;\n");
		buf.append("}\n");
		buf.append("\n");
		buf.append("@FunctionalInterface\n");
		buf.append("interface FI1 {\n");
		buf.append("    int foo(int x);\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("->");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 2);
		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("class E {\n");
		buf.append("    FI1 fi1a= x -> {\n");
		buf.append("        return -1;\n");
		buf.append("    };\n");
		buf.append("}\n");
		buf.append("\n");
		buf.append("@FunctionalInterface\n");
		buf.append("interface FI1 {\n");
		buf.append("    int foo(int x);\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1 });
	}

    @Test
	public void testChangeLambdaBodyToBlock2() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("class E {\n");
		buf.append("    FI1 fi1b= x -> m1();\n");
		buf.append("    int m1(){ return 0; }\n");
		buf.append("}\n");
		buf.append("\n");
		buf.append("@FunctionalInterface\n");
		buf.append("interface FI1 {\n");
		buf.append("    int foo(int x);\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("->");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 2);
		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("class E {\n");
		buf.append("    FI1 fi1b= x -> {\n");
		buf.append("        return m1();\n");
		buf.append("    };\n");
		buf.append("    int m1(){ return 0; }\n");
		buf.append("}\n");
		buf.append("\n");
		buf.append("@FunctionalInterface\n");
		buf.append("interface FI1 {\n");
		buf.append("    int foo(int x);\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1 });
	}

    @Test
	public void testChangeLambdaBodyToBlock3() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("class E {\n");
		buf.append("    FI2 fi2b= x -> m1();\n");
		buf.append("    int m1() { return 0; }\n");
		buf.append("}\n");
		buf.append("\n");
		buf.append("@FunctionalInterface\n");
		buf.append("interface FI2 {\n");
		buf.append("    void foo(int x);\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("->");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 2);
		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("class E {\n");
		buf.append("    FI2 fi2b= x -> {\n");
		buf.append("        m1();\n");
		buf.append("    };\n");
		buf.append("    int m1() { return 0; }\n");
		buf.append("}\n");
		buf.append("\n");
		buf.append("@FunctionalInterface\n");
		buf.append("interface FI2 {\n");
		buf.append("    void foo(int x);\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1 });
	}

    @Test
	public void testChangeLambdaBodyToBlock4() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("class E {\n");
		buf.append("    FI2 fi2a= x -> System.out.println();\n");
		buf.append("}\n");
		buf.append("\n");
		buf.append("@FunctionalInterface\n");
		buf.append("interface FI2 {\n");
		buf.append("    void foo(int x);\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("->");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 2);
		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("class E {\n");
		buf.append("    FI2 fi2a= x -> {\n");
		buf.append("        System.out.println();\n");
		buf.append("    };\n");
		buf.append("}\n");
		buf.append("\n");
		buf.append("@FunctionalInterface\n");
		buf.append("interface FI2 {\n");
		buf.append("    void foo(int x);\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1 });
	}

    @Test
	public void testChangeLambdaBodyToExpression1() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("class E {\n");
		buf.append("    FI1 fi1= x -> {\n");
		buf.append("        return x=0;\n");
		buf.append("    };\n");
		buf.append("}\n");
		buf.append("\n");
		buf.append("@FunctionalInterface\n");
		buf.append("interface FI1 {\n");
		buf.append("    int foo(int x);\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("->");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 2);
		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("class E {\n");
		buf.append("    FI1 fi1= x -> x=0;\n");
		buf.append("}\n");
		buf.append("\n");
		buf.append("@FunctionalInterface\n");
		buf.append("interface FI1 {\n");
		buf.append("    int foo(int x);\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1 });
	}

    @Test
	public void testChangeLambdaBodyToExpression2() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("class E {\n");
		buf.append("    FI2 fi2= x -> {\n");
		buf.append("        new Runnable() {\n");
		buf.append("            public void run() {\n");
		buf.append("            }\n");
		buf.append("        };\n");
		buf.append("    };\n");
		buf.append("}\n");
		buf.append("\n");
		buf.append("@FunctionalInterface\n");
		buf.append("interface FI2 {\n");
		buf.append("    void foo(int x);\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("->");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 2);
		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("class E {\n");
		buf.append("    FI2 fi2= x -> new Runnable() {\n");
		buf.append("        public void run() {\n");
		buf.append("        }\n");
		buf.append("    };\n");
		buf.append("}\n");
		buf.append("\n");
		buf.append("@FunctionalInterface\n");
		buf.append("interface FI2 {\n");
		buf.append("    void foo(int x);\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1 });
	}

    @Test
	public void testChangeLambdaBodyToExpression3() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("class E {\n");
		buf.append("    FI2 fi2= x -> { m1(); };\n");
		buf.append("    int m1(){ return 0; }\n");
		buf.append("}\n");
		buf.append("\n");
		buf.append("@FunctionalInterface\n");
		buf.append("interface FI2 {\n");
		buf.append("    void foo(int x);\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("->");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 2);
		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("class E {\n");
		buf.append("    FI2 fi2= x -> m1();\n");
		buf.append("    int m1(){ return 0; }\n");
		buf.append("}\n");
		buf.append("\n");
		buf.append("@FunctionalInterface\n");
		buf.append("interface FI2 {\n");
		buf.append("    void foo(int x);\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1 });
	}

    @Test
	public void testChangeLambdaBodyToExpression4() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("class E {\n");
		buf.append("    FI2 fi2= x -> {\n");
		buf.append("        super.toString();\n");
		buf.append("    };\n");
		buf.append("}\n");
		buf.append("\n");
		buf.append("@FunctionalInterface\n");
		buf.append("interface FI2 {\n");
		buf.append("    void foo(int x);\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("->");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("class E {\n");
		buf.append("    FI2 fi2= x -> super.toString();\n");
		buf.append("}\n");
		buf.append("\n");
		buf.append("@FunctionalInterface\n");
		buf.append("interface FI2 {\n");
		buf.append("    void foo(int x);\n");
		buf.append("}\n");
		assertNumberOfProposals(proposals, 2);
		assertCorrectLabels(proposals);

		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1 });
	}

    @Test
	public void testChangeLambdaBodyToExpression5() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("class E {\n");
		buf.append("    FI2 fi2= x -> {\n");
		buf.append("        --x;\n");
		buf.append("    };\n");
		buf.append("}\n");
		buf.append("\n");
		buf.append("@FunctionalInterface\n");
		buf.append("interface FI2 {\n");
		buf.append("    void foo(int x);\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("->");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("class E {\n");
		buf.append("    FI2 fi2= x -> --x;\n");
		buf.append("}\n");
		buf.append("\n");
		buf.append("@FunctionalInterface\n");
		buf.append("interface FI2 {\n");
		buf.append("    void foo(int x);\n");
		buf.append("}\n");
		assertNumberOfProposals(proposals, 2);
		assertCorrectLabels(proposals);

		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1 });
	}

    @Test
	public void testChangeLambdaBodyToExpression6() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("class E {\n");
		buf.append("    FI2 fi2z= x -> { };\n");
		buf.append("}\n");
		buf.append("\n");
		buf.append("@FunctionalInterface\n");
		buf.append("interface FI2 {\n");
		buf.append("    void foo(int x);\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("->");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);
		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);
		assertProposalDoesNotExist(proposals, CorrectionMessages.QuickAssistProcessor_change_lambda_body_to_expression);
	}

    @Test
	public void testChangeLambdaBodyToExpression7() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("class E {\n");
		buf.append("    FI2 fi2c = x ->    {\n");
		buf.append("        return;\n");
		buf.append("    };\n");
		buf.append("}\n");
		buf.append("\n");
		buf.append("@FunctionalInterface\n");
		buf.append("interface FI2 {\n");
		buf.append("    void foo(int x);\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("->");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);
		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);
		assertProposalDoesNotExist(proposals, CorrectionMessages.QuickAssistProcessor_change_lambda_body_to_expression);
	}

    @Test
	public void testChangeLambdaBodyToExpression8() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("class E {\n");
		buf.append("    FI2 fi2c = x ->    {\n");
		buf.append("        int n= 0;\n");
		buf.append("    };\n");
		buf.append("}\n");
		buf.append("\n");
		buf.append("@FunctionalInterface\n");
		buf.append("interface FI2 {\n");
		buf.append("    void foo(int x);\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("->");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);
		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);
		assertProposalDoesNotExist(proposals, CorrectionMessages.QuickAssistProcessor_change_lambda_body_to_expression);
	}

    @Test
	public void testBug433754() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("\n");
		buf.append("class E {\n");
		buf.append("    private void foo() {\n");
		buf.append("        for (String str : new String[1]) {\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("str");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		assertNoErrors(context);
		List proposals= collectAssists(context, false);
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("\n");
		buf.append("class E {\n");
		buf.append("    private void foo() {\n");
		buf.append("        String[] strings = new String[1];\n");
		buf.append("        for (int i = 0; i < strings.length; i++) {\n");
		buf.append("            String str = strings[i];\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);

		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1 });
	}
}
