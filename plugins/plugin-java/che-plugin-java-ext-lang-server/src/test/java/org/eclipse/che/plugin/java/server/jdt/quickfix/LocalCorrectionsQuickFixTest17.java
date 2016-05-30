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

import org.eclipse.che.plugin.java.server.jdt.testplugin.Java17ProjectTestSetup;
import org.eclipse.che.plugin.java.server.jdt.testplugin.JavaProjectHelper;
import org.eclipse.che.plugin.java.server.jdt.testplugin.TestOptions;
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
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jdt.ui.text.java.correction.CUCorrectionProposal;
import org.eclipse.jface.preference.IPreferenceStore;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Hashtable;

public class LocalCorrectionsQuickFixTest17 extends QuickFixTest {

	private static final Class THIS= LocalCorrectionsQuickFixTest17.class;

	private IJavaProject fJProject1;

	private IPackageFragmentRoot fSourceFolder;


	public LocalCorrectionsQuickFixTest17() {
		super(new Java17ProjectTestSetup());
	}
//
//	public static Test suite() {
//		return setUpTest(new TestSuite(THIS));
//	}
//
//	public static Test setUpTest(Test test) {
//		return new Java17ProjectTestSetup(test);
//	}


	@Before
    @Override
	public void setUp() throws Exception {
        super.setUp();
		Hashtable options = TestOptions.getDefaultOptions();
		options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, JavaCore.SPACE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, "4");
		options.put(DefaultCodeFormatterConstants.FORMATTER_NUMBER_OF_EMPTY_LINES_TO_PRESERVE, String.valueOf(99));
		options.put(JavaCore.COMPILER_PB_STATIC_ACCESS_RECEIVER, JavaCore.ERROR);
		options.put(JavaCore.COMPILER_PB_UNCHECKED_TYPE_OPERATION, JavaCore.IGNORE);
		options.put(JavaCore.COMPILER_PB_MISSING_HASHCODE_METHOD, JavaCore.WARNING);
		options.put(JavaCore.COMPILER_PB_REDUNDANT_TYPE_ARGUMENTS, JavaCore.WARNING);

		JavaCore.setOptions(options);

		IPreferenceStore store = JavaPlugin.getDefault().getPreferenceStore();
		store.setValue(PreferenceConstants.CODEGEN_ADD_COMMENTS, false);

		StubUtility.setCodeTemplate(CodeTemplateContextType.CATCHBLOCK_ID, "", null);
		StubUtility.setCodeTemplate(CodeTemplateContextType.CONSTRUCTORSTUB_ID, "", null);
		StubUtility.setCodeTemplate(CodeTemplateContextType.METHODSTUB_ID, "", null);

		fJProject1 = Java17ProjectTestSetup.getProject();

		fSourceFolder = JavaProjectHelper.addSourceContainer(fJProject1, "src");
	}


	@Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
		JavaProjectHelper.clear(fJProject1, Java17ProjectTestSetup.getDefaultClasspath());
	}

    @Test
	public void testUncaughtExceptionUnionType() throws Exception {
		IPackageFragment pack1 = fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.io.FileNotFoundException;\n");
		buf.append("import java.io.IOException;\n");
		buf.append("import java.io.InterruptedIOException;\n");
		buf.append("public class E {\n");
		buf.append("    void foo(int a) {\n");
		buf.append("        try {\n");
		buf.append("            if (a < 10)\n");
		buf.append("                throw new FileNotFoundException();\n");
		buf.append("            else if (a < 20)\n");
		buf.append("                throw new InterruptedIOException();\n");
		buf.append("            else\n");
		buf.append("                throw new IOException();\n");
		buf.append("        } catch (FileNotFoundException | InterruptedIOException ex) {\n");
		buf.append("            ex.printStackTrace();\n");
		buf.append("        } \n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= getASTRoot(cu);
		ArrayList proposals= collectCorrections(cu, astRoot);
		assertNumberOfProposals(proposals, 4);
		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.io.FileNotFoundException;\n");
		buf.append("import java.io.IOException;\n");
		buf.append("import java.io.InterruptedIOException;\n");
		buf.append("public class E {\n");
		buf.append("    void foo(int a) throws IOException {\n");
		buf.append("        try {\n");
		buf.append("            if (a < 10)\n");
		buf.append("                throw new FileNotFoundException();\n");
		buf.append("            else if (a < 20)\n");
		buf.append("                throw new InterruptedIOException();\n");
		buf.append("            else\n");
		buf.append("                throw new IOException();\n");
		buf.append("        } catch (FileNotFoundException | InterruptedIOException ex) {\n");
		buf.append("            ex.printStackTrace();\n");
		buf.append("        } \n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.io.FileNotFoundException;\n");
		buf.append("import java.io.IOException;\n");
		buf.append("import java.io.InterruptedIOException;\n");
		buf.append("public class E {\n");
		buf.append("    void foo(int a) {\n");
		buf.append("        try {\n");
		buf.append("            if (a < 10)\n");
		buf.append("                throw new FileNotFoundException();\n");
		buf.append("            else if (a < 20)\n");
		buf.append("                throw new InterruptedIOException();\n");
		buf.append("            else\n");
		buf.append("                throw new IOException();\n");
		buf.append("        } catch (FileNotFoundException | InterruptedIOException ex) {\n");
		buf.append("            ex.printStackTrace();\n");
		buf.append("        } catch (IOException e) {\n");
		buf.append("        } \n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected2= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.io.FileNotFoundException;\n");
		buf.append("import java.io.IOException;\n");
		buf.append("import java.io.InterruptedIOException;\n");
		buf.append("public class E {\n");
		buf.append("    void foo(int a) {\n");
		buf.append("        try {\n");
		buf.append("            if (a < 10)\n");
		buf.append("                throw new FileNotFoundException();\n");
		buf.append("            else if (a < 20)\n");
		buf.append("                throw new InterruptedIOException();\n");
		buf.append("            else\n");
		buf.append("                throw new IOException();\n");
		buf.append("        } catch (FileNotFoundException | InterruptedIOException | IOException ex) {\n");
		buf.append("            ex.printStackTrace();\n");
		buf.append("        } \n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected3= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1, expected2, expected3 });
	}

    @Test
	public void testUncaughtExceptionTryWithResources1() throws Exception {

		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.io.FileInputStream;\n");
		buf.append("class MyException extends Exception {\n");
		buf.append("    static final long serialVersionUID = 1L;\n");
		buf.append("}\n");
		buf.append("public class E {\n");
		buf.append("    void bar(int n) throws IllegalArgumentException, MyException {\n");
		buf.append("        if (n == 1)\n");
		buf.append("            throw new IllegalArgumentException();\n");
		buf.append("        else\n");
		buf.append("            throw new MyException();\n");
		buf.append("    }\n");
		buf.append("    void foo(String name, boolean b) {\n");
		buf.append("        try (FileInputStream fis = new FileInputStream(name)) {\n");
		buf.append("            bar(1);\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");

		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= getASTRoot(cu);
		ArrayList proposals= collectCorrections(cu, astRoot, 3, 0); //quick fix on 1st problem
		assertNumberOfProposals(proposals, 3);
		assertCorrectLabels(proposals);


		CUCorrectionProposal proposal= (CUCorrectionProposal)proposals.get(0);
		String preview1= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.io.FileInputStream;\n");
		buf.append("import java.io.FileNotFoundException;\n");
		buf.append("import java.io.IOException;\n");
		buf.append("class MyException extends Exception {\n");
		buf.append("    static final long serialVersionUID = 1L;\n");
		buf.append("}\n");
		buf.append("public class E {\n");
		buf.append("    void bar(int n) throws IllegalArgumentException, MyException {\n");
		buf.append("        if (n == 1)\n");
		buf.append("            throw new IllegalArgumentException();\n");
		buf.append("        else\n");
		buf.append("            throw new MyException();\n");
		buf.append("    }\n");
		buf.append("    void foo(String name, boolean b) throws FileNotFoundException, IOException {\n");
		buf.append("        try (FileInputStream fis = new FileInputStream(name)) {\n");
		buf.append("            bar(1);\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		proposal= (CUCorrectionProposal)proposals.get(1);
		String preview2= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.io.FileInputStream;\n");
		buf.append("import java.io.FileNotFoundException;\n");
		buf.append("import java.io.IOException;\n");
		buf.append("class MyException extends Exception {\n");
		buf.append("    static final long serialVersionUID = 1L;\n");
		buf.append("}\n");
		buf.append("public class E {\n");
		buf.append("    void bar(int n) throws IllegalArgumentException, MyException {\n");
		buf.append("        if (n == 1)\n");
		buf.append("            throw new IllegalArgumentException();\n");
		buf.append("        else\n");
		buf.append("            throw new MyException();\n");
		buf.append("    }\n");
		buf.append("    void foo(String name, boolean b) {\n");
		buf.append("        try (FileInputStream fis = new FileInputStream(name)) {\n");
		buf.append("            bar(1);\n");
		buf.append("        } catch (FileNotFoundException e) {\n");
		buf.append("        } catch (IOException e) {\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected2= buf.toString();

		proposal= (CUCorrectionProposal)proposals.get(2);
		String preview3= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.io.FileInputStream;\n");
		buf.append("import java.io.FileNotFoundException;\n");
		buf.append("import java.io.IOException;\n");
		buf.append("class MyException extends Exception {\n");
		buf.append("    static final long serialVersionUID = 1L;\n");
		buf.append("}\n");
		buf.append("public class E {\n");
		buf.append("    void bar(int n) throws IllegalArgumentException, MyException {\n");
		buf.append("        if (n == 1)\n");
		buf.append("            throw new IllegalArgumentException();\n");
		buf.append("        else\n");
		buf.append("            throw new MyException();\n");
		buf.append("    }\n");
		buf.append("    void foo(String name, boolean b) {\n");
		buf.append("        try (FileInputStream fis = new FileInputStream(name)) {\n");
		buf.append("            bar(1);\n");
		buf.append("        } catch (FileNotFoundException | IOException e) {\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected3= buf.toString();

		assertEqualStringsIgnoreOrder(new String[] { preview1, preview2, preview3 }, new String[] { expected1, expected2, expected3 });
	}

    @Test
	public void testUncaughtExceptionTryWithResources2() throws Exception {

		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.io.FileInputStream;\n");
		buf.append("class MyException extends Exception {\n");
		buf.append("    static final long serialVersionUID = 1L;\n");
		buf.append("}\n");
		buf.append("public class E {\n");
		buf.append("    void bar(int n) throws IllegalArgumentException, MyException {\n");
		buf.append("        if (n == 1)\n");
		buf.append("            throw new IllegalArgumentException();\n");
		buf.append("        else\n");
		buf.append("            throw new MyException();\n");
		buf.append("    }\n");
		buf.append("    void foo(String name, boolean b) {\n");
		buf.append("        try (FileInputStream fis = new FileInputStream(name)) {\n");
		buf.append("            bar(1);\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");

		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= getASTRoot(cu);
		ArrayList proposals= collectCorrections(cu, astRoot, 3, 1); //quick fix on 2nd problem
		assertNumberOfProposals(proposals, 3);
		assertCorrectLabels(proposals);


		CUCorrectionProposal proposal= (CUCorrectionProposal)proposals.get(0);
		String preview1= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.io.FileInputStream;\n");
		buf.append("import java.io.FileNotFoundException;\n");
		buf.append("import java.io.IOException;\n");
		buf.append("class MyException extends Exception {\n");
		buf.append("    static final long serialVersionUID = 1L;\n");
		buf.append("}\n");
		buf.append("public class E {\n");
		buf.append("    void bar(int n) throws IllegalArgumentException, MyException {\n");
		buf.append("        if (n == 1)\n");
		buf.append("            throw new IllegalArgumentException();\n");
		buf.append("        else\n");
		buf.append("            throw new MyException();\n");
		buf.append("    }\n");
		buf.append("    void foo(String name, boolean b) throws FileNotFoundException, IOException {\n");
		buf.append("        try (FileInputStream fis = new FileInputStream(name)) {\n");
		buf.append("            bar(1);\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		proposal= (CUCorrectionProposal)proposals.get(1);
		String preview2= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.io.FileInputStream;\n");
		buf.append("import java.io.FileNotFoundException;\n");
		buf.append("import java.io.IOException;\n");
		buf.append("class MyException extends Exception {\n");
		buf.append("    static final long serialVersionUID = 1L;\n");
		buf.append("}\n");
		buf.append("public class E {\n");
		buf.append("    void bar(int n) throws IllegalArgumentException, MyException {\n");
		buf.append("        if (n == 1)\n");
		buf.append("            throw new IllegalArgumentException();\n");
		buf.append("        else\n");
		buf.append("            throw new MyException();\n");
		buf.append("    }\n");
		buf.append("    void foo(String name, boolean b) {\n");
		buf.append("        try (FileInputStream fis = new FileInputStream(name)) {\n");
		buf.append("            bar(1);\n");
		buf.append("        } catch (FileNotFoundException e) {\n");
		buf.append("        } catch (IOException e) {\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected2= buf.toString();

		proposal= (CUCorrectionProposal)proposals.get(2);
		String preview3= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.io.FileInputStream;\n");
		buf.append("import java.io.FileNotFoundException;\n");
		buf.append("import java.io.IOException;\n");
		buf.append("class MyException extends Exception {\n");
		buf.append("    static final long serialVersionUID = 1L;\n");
		buf.append("}\n");
		buf.append("public class E {\n");
		buf.append("    void bar(int n) throws IllegalArgumentException, MyException {\n");
		buf.append("        if (n == 1)\n");
		buf.append("            throw new IllegalArgumentException();\n");
		buf.append("        else\n");
		buf.append("            throw new MyException();\n");
		buf.append("    }\n");
		buf.append("    void foo(String name, boolean b) {\n");
		buf.append("        try (FileInputStream fis = new FileInputStream(name)) {\n");
		buf.append("            bar(1);\n");
		buf.append("        } catch (FileNotFoundException | IOException e) {\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected3= buf.toString();

		assertEqualStringsIgnoreOrder(new String[] { preview1, preview2, preview3 }, new String[] { expected1, expected2, expected3 });
	}

    @Test
	public void testUncaughtExceptionTryWithResources3() throws Exception {

		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.io.FileInputStream;\n");
		buf.append("class MyException extends Exception {\n");
		buf.append("    static final long serialVersionUID = 1L;\n");
		buf.append("}\n");
		buf.append("public class E {\n");
		buf.append("    void bar(int n) throws IllegalArgumentException, MyException {\n");
		buf.append("        if (n == 1)\n");
		buf.append("            throw new IllegalArgumentException();\n");
		buf.append("        else\n");
		buf.append("            throw new MyException();\n");
		buf.append("    }\n");
		buf.append("    void foo(String name, boolean b) {\n");
		buf.append("        try (FileInputStream fis = new FileInputStream(name)) {\n");
		buf.append("            bar(1);\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");

		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= getASTRoot(cu);
		ArrayList proposals= collectCorrections(cu, astRoot, 3, 2); //quick fix on 3rd problem
		assertNumberOfProposals(proposals, 5);
		assertCorrectLabels(proposals);


		CUCorrectionProposal proposal= (CUCorrectionProposal)proposals.get(0);
		String preview1= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.io.FileInputStream;\n");
		buf.append("class MyException extends Exception {\n");
		buf.append("    static final long serialVersionUID = 1L;\n");
		buf.append("}\n");
		buf.append("public class E {\n");
		buf.append("    void bar(int n) throws IllegalArgumentException, MyException {\n");
		buf.append("        if (n == 1)\n");
		buf.append("            throw new IllegalArgumentException();\n");
		buf.append("        else\n");
		buf.append("            throw new MyException();\n");
		buf.append("    }\n");
		buf.append("    void foo(String name, boolean b) throws IllegalArgumentException, MyException {\n");
		buf.append("        try (FileInputStream fis = new FileInputStream(name)) {\n");
		buf.append("            bar(1);\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		proposal= (CUCorrectionProposal)proposals.get(1);
		String preview2= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.io.FileInputStream;\n");
		buf.append("class MyException extends Exception {\n");
		buf.append("    static final long serialVersionUID = 1L;\n");
		buf.append("}\n");
		buf.append("public class E {\n");
		buf.append("    void bar(int n) throws IllegalArgumentException, MyException {\n");
		buf.append("        if (n == 1)\n");
		buf.append("            throw new IllegalArgumentException();\n");
		buf.append("        else\n");
		buf.append("            throw new MyException();\n");
		buf.append("    }\n");
		buf.append("    void foo(String name, boolean b) {\n");
		buf.append("        try (FileInputStream fis = new FileInputStream(name)) {\n");
		buf.append("            bar(1);\n");
		buf.append("        } catch (IllegalArgumentException e) {\n");
		buf.append("        } catch (MyException e) {\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected2= buf.toString();

		proposal= (CUCorrectionProposal)proposals.get(2);
		String preview3= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.io.FileInputStream;\n");
		buf.append("class MyException extends Exception {\n");
		buf.append("    static final long serialVersionUID = 1L;\n");
		buf.append("}\n");
		buf.append("public class E {\n");
		buf.append("    void bar(int n) throws IllegalArgumentException, MyException {\n");
		buf.append("        if (n == 1)\n");
		buf.append("            throw new IllegalArgumentException();\n");
		buf.append("        else\n");
		buf.append("            throw new MyException();\n");
		buf.append("    }\n");
		buf.append("    void foo(String name, boolean b) {\n");
		buf.append("        try (FileInputStream fis = new FileInputStream(name)) {\n");
		buf.append("            bar(1);\n");
		buf.append("        } catch (IllegalArgumentException | MyException e) {\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected3= buf.toString();

		proposal= (CUCorrectionProposal)proposals.get(3);
		String preview4= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.io.FileInputStream;\n");
		buf.append("class MyException extends Exception {\n");
		buf.append("    static final long serialVersionUID = 1L;\n");
		buf.append("}\n");
		buf.append("public class E {\n");
		buf.append("    void bar(int n) throws IllegalArgumentException, MyException {\n");
		buf.append("        if (n == 1)\n");
		buf.append("            throw new IllegalArgumentException();\n");
		buf.append("        else\n");
		buf.append("            throw new MyException();\n");
		buf.append("    }\n");
		buf.append("    void foo(String name, boolean b) {\n");
		buf.append("        try (FileInputStream fis = new FileInputStream(name)) {\n");
		buf.append("            try {\n");
		buf.append("                bar(1);\n");
		buf.append("            } catch (IllegalArgumentException e) {\n");
		buf.append("            } catch (MyException e) {\n");
		buf.append("            }\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected4= buf.toString();

		proposal= (CUCorrectionProposal)proposals.get(4);
		String preview5= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.io.FileInputStream;\n");
		buf.append("class MyException extends Exception {\n");
		buf.append("    static final long serialVersionUID = 1L;\n");
		buf.append("}\n");
		buf.append("public class E {\n");
		buf.append("    void bar(int n) throws IllegalArgumentException, MyException {\n");
		buf.append("        if (n == 1)\n");
		buf.append("            throw new IllegalArgumentException();\n");
		buf.append("        else\n");
		buf.append("            throw new MyException();\n");
		buf.append("    }\n");
		buf.append("    void foo(String name, boolean b) {\n");
		buf.append("        try (FileInputStream fis = new FileInputStream(name)) {\n");
		buf.append("            try {\n");
		buf.append("                bar(1);\n");
		buf.append("            } catch (IllegalArgumentException | MyException e) {\n");
		buf.append("            }\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected5= buf.toString();

		assertEqualStringsIgnoreOrder(new String[] { preview1, preview2, preview3, preview4, preview5 }, new String[] { expected1, expected2, expected3, expected4, expected5 });
	}

    @Test
	public void testUncaughtExceptionTryWithResources4() throws Exception {
		//https://bugs.eclipse.org/bugs/show_bug.cgi?id=351464
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.io.FileInputStream;\n");
		buf.append("import java.io.IOException;\n");
		buf.append("class MyException extends Exception {\n");
		buf.append("    static final long serialVersionUID = 1L;\n");
		buf.append("}\n");
		buf.append("public class E {\n");
		buf.append("    void bar() throws MyException {\n");
		buf.append("        throw new MyException();\n");
		buf.append("    }\n");
		buf.append("    void foo(String name, boolean b) throws IOException {\n");
		buf.append("        try (FileInputStream fis = new FileInputStream(name)) {\n");
		buf.append("            bar();\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");

		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= getASTRoot(cu);
		ArrayList proposals= collectCorrections(cu, astRoot);
		assertNumberOfProposals(proposals, 3);
		assertCorrectLabels(proposals);


		CUCorrectionProposal proposal= (CUCorrectionProposal)proposals.get(0);
		String preview1= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.io.FileInputStream;\n");
		buf.append("import java.io.IOException;\n");
		buf.append("class MyException extends Exception {\n");
		buf.append("    static final long serialVersionUID = 1L;\n");
		buf.append("}\n");
		buf.append("public class E {\n");
		buf.append("    void bar() throws MyException {\n");
		buf.append("        throw new MyException();\n");
		buf.append("    }\n");
		buf.append("    void foo(String name, boolean b) throws IOException, MyException {\n");
		buf.append("        try (FileInputStream fis = new FileInputStream(name)) {\n");
		buf.append("            bar();\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		proposal= (CUCorrectionProposal)proposals.get(1);
		String preview2= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.io.FileInputStream;\n");
		buf.append("import java.io.IOException;\n");
		buf.append("class MyException extends Exception {\n");
		buf.append("    static final long serialVersionUID = 1L;\n");
		buf.append("}\n");
		buf.append("public class E {\n");
		buf.append("    void bar() throws MyException {\n");
		buf.append("        throw new MyException();\n");
		buf.append("    }\n");
		buf.append("    void foo(String name, boolean b) throws IOException {\n");
		buf.append("        try (FileInputStream fis = new FileInputStream(name)) {\n");
		buf.append("            bar();\n");
		buf.append("        } catch (MyException e) {\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected2= buf.toString();

		proposal= (CUCorrectionProposal)proposals.get(2);
		String preview3= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.io.FileInputStream;\n");
		buf.append("import java.io.IOException;\n");
		buf.append("class MyException extends Exception {\n");
		buf.append("    static final long serialVersionUID = 1L;\n");
		buf.append("}\n");
		buf.append("public class E {\n");
		buf.append("    void bar() throws MyException {\n");
		buf.append("        throw new MyException();\n");
		buf.append("    }\n");
		buf.append("    void foo(String name, boolean b) throws IOException {\n");
		buf.append("        try (FileInputStream fis = new FileInputStream(name)) {\n");
		buf.append("            try {\n");
		buf.append("                bar();\n");
		buf.append("            } catch (MyException e) {\n");
		buf.append("            }\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected3= buf.toString();

		assertEqualStringsIgnoreOrder(new String[] { preview1, preview2, preview3 }, new String[] { expected1, expected2, expected3 });
	}

    @Test
	public void testUncaughtExceptionTryWithResources5() throws Exception {
		//https://bugs.eclipse.org/bugs/show_bug.cgi?id=139231
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.io.FileInputStream;\n");
		buf.append("public class E {\n");
		buf.append("    void foo(String name, boolean b) {\n");
		buf.append("        String e;\n");
		buf.append("        try (FileInputStream fis = new FileInputStream(name)) {\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");

		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= getASTRoot(cu);
		ArrayList proposals= collectCorrections(cu, astRoot, 2, 0); //quick fix on 1st problem
		assertNumberOfProposals(proposals, 3);
		assertCorrectLabels(proposals);


		CUCorrectionProposal proposal= (CUCorrectionProposal)proposals.get(0);
		String preview1= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.io.FileInputStream;\n");
		buf.append("import java.io.FileNotFoundException;\n");
		buf.append("import java.io.IOException;\n");
		buf.append("public class E {\n");
		buf.append("    void foo(String name, boolean b) throws FileNotFoundException, IOException {\n");
		buf.append("        String e;\n");
		buf.append("        try (FileInputStream fis = new FileInputStream(name)) {\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		proposal= (CUCorrectionProposal)proposals.get(1);
		String preview2= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.io.FileInputStream;\n");
		buf.append("import java.io.FileNotFoundException;\n");
		buf.append("import java.io.IOException;\n");
		buf.append("public class E {\n");
		buf.append("    void foo(String name, boolean b) {\n");
		buf.append("        String e;\n");
		buf.append("        try (FileInputStream fis = new FileInputStream(name)) {\n");
		buf.append("        } catch (FileNotFoundException e1) {\n");
		buf.append("        } catch (IOException e1) {\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected2= buf.toString();

		proposal= (CUCorrectionProposal)proposals.get(2);
		String preview3= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.io.FileInputStream;\n");
		buf.append("import java.io.FileNotFoundException;\n");
		buf.append("import java.io.IOException;\n");
		buf.append("public class E {\n");
		buf.append("    void foo(String name, boolean b) {\n");
		buf.append("        String e;\n");
		buf.append("        try (FileInputStream fis = new FileInputStream(name)) {\n");
		buf.append("        } catch (FileNotFoundException | IOException e1) {\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected3= buf.toString();

		assertEqualStringsIgnoreOrder(new String[] { preview1, preview2, preview3 }, new String[] { expected1, expected2, expected3 });
	}

    @Test
	public void testUnneededCaughtException1() throws Exception {

		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.io.FileNotFoundException;\n");
		buf.append("import java.io.IOException;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        try {\n");
		buf.append("            throw new FileNotFoundException();\n");
		buf.append("        } catch (FileNotFoundException | IOException e) {\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= getASTRoot(cu);
		ArrayList proposals= collectCorrections(cu, astRoot);
		assertNumberOfProposals(proposals, 2);
		assertCorrectLabels(proposals);


		CUCorrectionProposal proposal= (CUCorrectionProposal)proposals.get(0);
		String preview1= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.io.FileNotFoundException;\n");
		buf.append("import java.io.IOException;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        try {\n");
		buf.append("            throw new FileNotFoundException();\n");
		buf.append("        } catch (IOException e) {\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		proposal= (CUCorrectionProposal)proposals.get(1);
		String preview2= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.io.FileNotFoundException;\n");
		buf.append("import java.io.IOException;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() throws FileNotFoundException {\n");
		buf.append("        try {\n");
		buf.append("            throw new FileNotFoundException();\n");
		buf.append("        } catch (IOException e) {\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected2= buf.toString();

		assertEqualStringsIgnoreOrder(new String[] { preview1, preview2 }, new String[] { expected1, expected2 });
	}

    @Test
	public void testUnneededCaughtException2() throws Exception {

		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.io.FileNotFoundException;\n");
		buf.append("import java.io.IOException;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        try {\n");
		buf.append("            throw new FileNotFoundException();\n");
		buf.append("        } catch (java.io.FileNotFoundException | java.io.IOException e) {\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= getASTRoot(cu);
		ArrayList proposals= collectCorrections(cu, astRoot);
		assertNumberOfProposals(proposals, 2);
		assertCorrectLabels(proposals);


		CUCorrectionProposal proposal= (CUCorrectionProposal)proposals.get(0);
		String preview1= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.io.FileNotFoundException;\n");
		buf.append("import java.io.IOException;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        try {\n");
		buf.append("            throw new FileNotFoundException();\n");
		buf.append("        } catch (java.io.IOException e) {\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		proposal= (CUCorrectionProposal)proposals.get(1);
		String preview2= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.io.FileNotFoundException;\n");
		buf.append("import java.io.IOException;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() throws java.io.FileNotFoundException {\n");
		buf.append("        try {\n");
		buf.append("            throw new FileNotFoundException();\n");
		buf.append("        } catch (java.io.IOException e) {\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected2= buf.toString();

		assertEqualStringsIgnoreOrder(new String[] { preview1, preview2 }, new String[] { expected1, expected2 });
	}

    @Test
	public void testUnneededCatchBlockTryWithResources() throws Exception {

		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.io.FileReader;\n");
		buf.append("class MyException extends Exception {\n");
		buf.append("    static final long serialVersionUID = 1L;\n");
		buf.append("}\n");
		buf.append("public class E {\n");
		buf.append("    void foo() throws Exception {\n");
		buf.append("        try (FileReader reader1 = new FileReader(\"file\")) {\n");
		buf.append("            int ch;\n");
		buf.append("            while ((ch = reader1.read()) != -1) {\n");
		buf.append("                System.out.println(ch);\n");
		buf.append("            }\n");
		buf.append("        } catch (MyException e) {\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= getASTRoot(cu);
		ArrayList proposals= collectCorrections(cu, astRoot);
		assertNumberOfProposals(proposals, 2);
		assertCorrectLabels(proposals);


		CUCorrectionProposal proposal= (CUCorrectionProposal)proposals.get(0);
		String preview1= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.io.FileReader;\n");
		buf.append("class MyException extends Exception {\n");
		buf.append("    static final long serialVersionUID = 1L;\n");
		buf.append("}\n");
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
		String expected1= buf.toString();

		proposal= (CUCorrectionProposal)proposals.get(1);
		String preview2= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.io.FileReader;\n");
		buf.append("class MyException extends Exception {\n");
		buf.append("    static final long serialVersionUID = 1L;\n");
		buf.append("}\n");
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
		String expected2= buf.toString();

		assertEqualStringsIgnoreOrder(new String[] { preview1, preview2 }, new String[] { expected1, expected2 });
	}

    @Test
	public void testRemoveRedundantTypeArguments1() throws Exception {

		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.ArrayList;\n");
		buf.append("import java.util.List;\n");
		buf.append("public class E {\n");
		buf.append("    void foo() {\n");
		buf.append("        List<String> a = new ArrayList<java.lang.String>();\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= getASTRoot(cu);
		ArrayList proposals= collectCorrections(cu, astRoot);
		assertNumberOfProposals(proposals, 3);
		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.ArrayList;\n");
		buf.append("import java.util.List;\n");
		buf.append("public class E {\n");
		buf.append("    void foo() {\n");
		buf.append("        List<String> a = new ArrayList<>();\n");
		buf.append("    }\n");
		buf.append("}\n");

		String expected1= buf.toString();
		assertExpectedExistInProposals(proposals, new String[] { expected1 });
	}

    @Test
	public void testRemoveRedundantTypeArguments2() throws Exception {

		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.HashMap;\n");
		buf.append("import java.util.Map;\n");
		buf.append("public class E {\n");
		buf.append("    void foo() {\n");
		buf.append("        Map<String,String> a = new HashMap<String,String>();\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= getASTRoot(cu);
		ArrayList proposals= collectCorrections(cu, astRoot);
		assertNumberOfProposals(proposals, 3);
		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.HashMap;\n");
		buf.append("import java.util.Map;\n");
		buf.append("public class E {\n");
		buf.append("    void foo() {\n");
		buf.append("        Map<String,String> a = new HashMap<>();\n");
		buf.append("    }\n");
		buf.append("}\n");

		String expected1= buf.toString();
		assertExpectedExistInProposals(proposals, new String[] { expected1 });
	}

    @Test
	public void testRemoveRedundantTypeArguments3() throws Exception {

		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.ArrayList;\n");
		buf.append("import java.util.List;\n");
		buf.append("import java.util.Map;\n");
		buf.append("public class E {\n");
		buf.append("    void foo() {\n");
		buf.append("        List<Map<String, String>> a = new ArrayList<Map<String, String>>();;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= getASTRoot(cu);
		ArrayList proposals= collectCorrections(cu, astRoot);
		assertNumberOfProposals(proposals, 3);
		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.ArrayList;\n");
		buf.append("import java.util.List;\n");
		buf.append("import java.util.Map;\n");
		buf.append("public class E {\n");
		buf.append("    void foo() {\n");
		buf.append("        List<Map<String, String>> a = new ArrayList<>();;\n");
		buf.append("    }\n");
		buf.append("}\n");

		String expected1= buf.toString();
		assertExpectedExistInProposals(proposals, new String[] { expected1 });
	}

    @Test
	public void testRemoveRedundantTypeArguments4() throws Exception {

		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.HashMap;\n");
		buf.append("import java.util.Map;\n");
		buf.append("public class E {\n");
		buf.append("    void foo() {\n");
		buf.append("        Map<Map<String, String>, Map<String, String>> a = new HashMap<Map<String, String>, Map<String, String>>();;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= getASTRoot(cu);
		ArrayList proposals= collectCorrections(cu, astRoot);
		assertNumberOfProposals(proposals, 3);
		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.HashMap;\n");
		buf.append("import java.util.Map;\n");
		buf.append("public class E {\n");
		buf.append("    void foo() {\n");
		buf.append("        Map<Map<String, String>, Map<String, String>> a = new HashMap<>();;\n");
		buf.append("    }\n");
		buf.append("}\n");

		String expected1= buf.toString();
		assertExpectedExistInProposals(proposals, new String[] { expected1 });
	}

}
