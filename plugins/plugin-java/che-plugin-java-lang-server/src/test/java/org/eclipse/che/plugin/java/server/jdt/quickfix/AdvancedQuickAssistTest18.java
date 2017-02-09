/*******************************************************************************
 * Copyright (c) 2014 IBM Corporation and others.
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
import org.junit.Test;

import java.util.Hashtable;
import java.util.List;

public class AdvancedQuickAssistTest18 extends QuickFixTest {


	private IJavaProject fJProject1;

	private IPackageFragmentRoot fSourceFolder;

	public AdvancedQuickAssistTest18() {
		super(new Java18ProjectTestSetup());
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

		fJProject1 = Java18ProjectTestSetup.getProject();

		fSourceFolder = JavaProjectHelper.addSourceContainer(fJProject1, "src");
	}


    @After
	public void tearDown() throws Exception {
        super.tearDown();
		JavaProjectHelper.clear(fJProject1, Java18ProjectTestSetup.getDefaultClasspath());
	}

    @Test
	public void testConvertToIfReturn1() throws Exception {
		// 'if' in lambda body - positive cases
		IPackageFragment pack1 = fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("@FunctionalInterface\n");
		buf.append("interface A {\n");
		buf.append("    void run(int n);\n");
		buf.append("}\n");
		buf.append("\n");
		buf.append("@FunctionalInterface\n");
		buf.append("interface B {\n");
		buf.append("    A foo(int x);\n");
		buf.append("}\n");
		buf.append("\n");
		buf.append("public class Test {\n");
		buf.append("    A fi0 = (n1) -> {\n");
		buf.append("        if (n1 == 0) {\n");
		buf.append("            System.out.println(n1);\n");
		buf.append("            return;\n");
		buf.append("        }\n");
		buf.append("    };\n");
		buf.append("    \n");
		buf.append("    int fun1(int a, int b) {\n");
		buf.append("        A fi2 = (n2) -> {\n");
		buf.append("            if (a == b) {\n");
		buf.append("                System.out.println(n2);\n");
		buf.append("                return;\n");
		buf.append("            }\n");
		buf.append("        };\n");
		buf.append("        return a + b;\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("    A fun2(int a1, int b1) {\n");
		buf.append("        return (n) -> {\n");
		buf.append("            if (a1 == b1) {\n");
		buf.append("                System.out.println(n);\n");
		buf.append("                return;\n");
		buf.append("            }\n");
		buf.append("        };\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("    int fun3(int a2, int b2) {\n");
		buf.append("        B fi3 = (x) -> (n) -> {\n");
		buf.append("            if (a2 == b2) {\n");
		buf.append("                System.out.println(a2);\n");
		buf.append("                return;\n");
		buf.append("            }\n");
		buf.append("        };\n");
		buf.append("        return a2 + b2;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("Test.java", buf.toString(), false, null);

		String str= "if (n1 == 0)";
		AssistContext context= getCorrectionContext(cu, buf.toString().indexOf(str) + str.length(), 0);
		List proposals= collectAssists(context, false);
		assertNumberOfProposals(proposals, 4);
		assertCorrectLabels(proposals);
		StringBuffer buf1= new StringBuffer();
		buf1.append("package test1;\n");
		buf1.append("@FunctionalInterface\n");
		buf1.append("interface A {\n");
		buf1.append("    void run(int n);\n");
		buf1.append("}\n");
		buf1.append("\n");
		buf1.append("@FunctionalInterface\n");
		buf1.append("interface B {\n");
		buf1.append("    A foo(int x);\n");
		buf1.append("}\n");
		buf1.append("\n");
		buf1.append("public class Test {\n");
		buf1.append("    A fi0 = (n1) -> {\n");
		buf1.append("        if (n1 != 0)\n");
		buf1.append("            return;\n");
		buf1.append("        System.out.println(n1);\n");
		buf1.append("    };\n");
		buf1.append("    \n");
		buf1.append("    int fun1(int a, int b) {\n");
		buf1.append("        A fi2 = (n2) -> {\n");
		buf1.append("            if (a == b) {\n");
		buf1.append("                System.out.println(n2);\n");
		buf1.append("                return;\n");
		buf1.append("            }\n");
		buf1.append("        };\n");
		buf1.append("        return a + b;\n");
		buf1.append("    }\n");
		buf1.append("\n");
		buf1.append("    A fun2(int a1, int b1) {\n");
		buf1.append("        return (n) -> {\n");
		buf1.append("            if (a1 == b1) {\n");
		buf1.append("                System.out.println(n);\n");
		buf1.append("                return;\n");
		buf1.append("            }\n");
		buf1.append("        };\n");
		buf1.append("    }\n");
		buf1.append("\n");
		buf1.append("    int fun3(int a2, int b2) {\n");
		buf1.append("        B fi3 = (x) -> (n) -> {\n");
		buf1.append("            if (a2 == b2) {\n");
		buf1.append("                System.out.println(a2);\n");
		buf1.append("                return;\n");
		buf1.append("            }\n");
		buf1.append("        };\n");
		buf1.append("        return a2 + b2;\n");
		buf1.append("    }\n");
		buf1.append("}\n");
		String expected1= buf1.toString();
		assertExpectedExistInProposals(proposals, new String[] { expected1 });

		str= "if (a == b)";
		context= getCorrectionContext(cu, buf.toString().indexOf(str) + str.length(), 0);
		proposals= collectAssists(context, false);
		assertNumberOfProposals(proposals, 3);
		assertCorrectLabels(proposals);
		buf1= new StringBuffer();
		buf1.append("package test1;\n");
		buf1.append("@FunctionalInterface\n");
		buf1.append("interface A {\n");
		buf1.append("    void run(int n);\n");
		buf1.append("}\n");
		buf1.append("\n");
		buf1.append("@FunctionalInterface\n");
		buf1.append("interface B {\n");
		buf1.append("    A foo(int x);\n");
		buf1.append("}\n");
		buf1.append("\n");
		buf1.append("public class Test {\n");
		buf1.append("    A fi0 = (n1) -> {\n");
		buf1.append("        if (n1 == 0) {\n");
		buf1.append("            System.out.println(n1);\n");
		buf1.append("            return;\n");
		buf1.append("        }\n");
		buf1.append("    };\n");
		buf1.append("    \n");
		buf1.append("    int fun1(int a, int b) {\n");
		buf1.append("        A fi2 = (n2) -> {\n");
		buf1.append("            if (a != b)\n");
		buf1.append("                return;\n");
		buf1.append("            System.out.println(n2);\n");
		buf1.append("        };\n");
		buf1.append("        return a + b;\n");
		buf1.append("    }\n");
		buf1.append("\n");
		buf1.append("    A fun2(int a1, int b1) {\n");
		buf1.append("        return (n) -> {\n");
		buf1.append("            if (a1 == b1) {\n");
		buf1.append("                System.out.println(n);\n");
		buf1.append("                return;\n");
		buf1.append("            }\n");
		buf1.append("        };\n");
		buf1.append("    }\n");
		buf1.append("\n");
		buf1.append("    int fun3(int a2, int b2) {\n");
		buf1.append("        B fi3 = (x) -> (n) -> {\n");
		buf1.append("            if (a2 == b2) {\n");
		buf1.append("                System.out.println(a2);\n");
		buf1.append("                return;\n");
		buf1.append("            }\n");
		buf1.append("        };\n");
		buf1.append("        return a2 + b2;\n");
		buf1.append("    }\n");
		buf1.append("}\n");
		expected1= buf1.toString();
		assertExpectedExistInProposals(proposals, new String[] { expected1 });


		str= "if (a1 == b1)";
		context= getCorrectionContext(cu, buf.toString().indexOf(str) + str.length(), 0);
		proposals= collectAssists(context, false);
		assertNumberOfProposals(proposals, 3);
		assertCorrectLabels(proposals);
		buf1= new StringBuffer();
		buf1.append("package test1;\n");
		buf1.append("@FunctionalInterface\n");
		buf1.append("interface A {\n");
		buf1.append("    void run(int n);\n");
		buf1.append("}\n");
		buf1.append("\n");
		buf1.append("@FunctionalInterface\n");
		buf1.append("interface B {\n");
		buf1.append("    A foo(int x);\n");
		buf1.append("}\n");
		buf1.append("\n");
		buf1.append("public class Test {\n");
		buf1.append("    A fi0 = (n1) -> {\n");
		buf1.append("        if (n1 == 0) {\n");
		buf1.append("            System.out.println(n1);\n");
		buf1.append("            return;\n");
		buf1.append("        }\n");
		buf1.append("    };\n");
		buf1.append("    \n");
		buf1.append("    int fun1(int a, int b) {\n");
		buf1.append("        A fi2 = (n2) -> {\n");
		buf1.append("            if (a == b) {\n");
		buf1.append("                System.out.println(n2);\n");
		buf1.append("                return;\n");
		buf1.append("            }\n");
		buf1.append("        };\n");
		buf1.append("        return a + b;\n");
		buf1.append("    }\n");
		buf1.append("\n");
		buf1.append("    A fun2(int a1, int b1) {\n");
		buf1.append("        return (n) -> {\n");
		buf1.append("            if (a1 != b1)\n");
		buf1.append("                return;\n");
		buf1.append("            System.out.println(n);\n");
		buf1.append("        };\n");
		buf1.append("    }\n");
		buf1.append("\n");
		buf1.append("    int fun3(int a2, int b2) {\n");
		buf1.append("        B fi3 = (x) -> (n) -> {\n");
		buf1.append("            if (a2 == b2) {\n");
		buf1.append("                System.out.println(a2);\n");
		buf1.append("                return;\n");
		buf1.append("            }\n");
		buf1.append("        };\n");
		buf1.append("        return a2 + b2;\n");
		buf1.append("    }\n");
		buf1.append("}\n");
		expected1= buf1.toString();
		assertExpectedExistInProposals(proposals, new String[] { expected1 });


		str= "if (a2 == b2)";
		context= getCorrectionContext(cu, buf.toString().indexOf(str) + str.length(), 0);
		proposals= collectAssists(context, false);
		assertNumberOfProposals(proposals, 3);
		assertCorrectLabels(proposals);
		buf1= new StringBuffer();
		buf1.append("package test1;\n");
		buf1.append("@FunctionalInterface\n");
		buf1.append("interface A {\n");
		buf1.append("    void run(int n);\n");
		buf1.append("}\n");
		buf1.append("\n");
		buf1.append("@FunctionalInterface\n");
		buf1.append("interface B {\n");
		buf1.append("    A foo(int x);\n");
		buf1.append("}\n");
		buf1.append("\n");
		buf1.append("public class Test {\n");
		buf1.append("    A fi0 = (n1) -> {\n");
		buf1.append("        if (n1 == 0) {\n");
		buf1.append("            System.out.println(n1);\n");
		buf1.append("            return;\n");
		buf1.append("        }\n");
		buf1.append("    };\n");
		buf1.append("    \n");
		buf1.append("    int fun1(int a, int b) {\n");
		buf1.append("        A fi2 = (n2) -> {\n");
		buf1.append("            if (a == b) {\n");
		buf1.append("                System.out.println(n2);\n");
		buf1.append("                return;\n");
		buf1.append("            }\n");
		buf1.append("        };\n");
		buf1.append("        return a + b;\n");
		buf1.append("    }\n");
		buf1.append("\n");
		buf1.append("    A fun2(int a1, int b1) {\n");
		buf1.append("        return (n) -> {\n");
		buf1.append("            if (a1 == b1) {\n");
		buf1.append("                System.out.println(n);\n");
		buf1.append("                return;\n");
		buf1.append("            }\n");
		buf1.append("        };\n");
		buf1.append("    }\n");
		buf1.append("\n");
		buf1.append("    int fun3(int a2, int b2) {\n");
		buf1.append("        B fi3 = (x) -> (n) -> {\n");
		buf1.append("            if (a2 != b2)\n");
		buf1.append("                return;\n");
		buf1.append("            System.out.println(a2);\n");
		buf1.append("        };\n");
		buf1.append("        return a2 + b2;\n");
		buf1.append("    }\n");
		buf1.append("}\n");
		expected1= buf1.toString();
		assertExpectedExistInProposals(proposals, new String[] { expected1 });
	}

    @Test
	public void testConvertToIfReturn2() throws Exception {
		// 'if' in lambda body - negative cases
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("@FunctionalInterface\n");
		buf.append("interface A {\n");
		buf.append("    void run(int n);\n");
		buf.append("}\n");
		buf.append("\n");
		buf.append("@FunctionalInterface\n");
		buf.append("interface B {\n");
		buf.append("    A foo(int x);\n");
		buf.append("}\n");
		buf.append("\n");
		buf.append("public class Test {\n");
		buf.append("    int f1(int a2, int b2) {\n");
		buf.append("        B fi3 = (x) -> {\n");
		buf.append("            if (x != 100) {\n");
		buf.append("                return (n) -> System.out.println(n + x);\n");
		buf.append("            }\n");
		buf.append("        };\n");
		buf.append("        return a2 + b2;\n");
		buf.append("    }\n");
		buf.append("    \n");
		buf.append("    void f2(int a1, int b1) {\n");
		buf.append("        A a= (n) -> {\n");
		buf.append("            if (a1 == b1) {\n");
		buf.append("                System.out.println(n);\n");
		buf.append("                return;\n");
		buf.append("            }\n");
		buf.append("            bar();\n");
		buf.append("        };\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("    private void bar() {}\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("Test.java", buf.toString(), false, null);

		String str= "if (x != 100)"; // #foo does not return void
		AssistContext context= getCorrectionContext(cu, buf.toString().indexOf(str) + str.length(), 0);
		List proposals= collectAssists(context, false);
		assertNumberOfProposals(proposals, 3);
		assertCorrectLabels(proposals);
		assertProposalDoesNotExist(proposals, CorrectionMessages.AdvancedQuickAssistProcessor_convertToIfReturn);

		str= "if (a1 == b1)"; // not the last executable statement in lambda body
		context= getCorrectionContext(cu, buf.toString().indexOf(str) + str.length(), 0);
		proposals= collectAssists(context, false);
		assertNumberOfProposals(proposals, 2);
		assertCorrectLabels(proposals);
		assertProposalDoesNotExist(proposals, CorrectionMessages.AdvancedQuickAssistProcessor_convertToIfReturn);
	}
}
