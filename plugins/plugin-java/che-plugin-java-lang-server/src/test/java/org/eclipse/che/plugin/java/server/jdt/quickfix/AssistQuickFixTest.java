/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sebastian Davids <sdavids@gmx.de> - testInvertEquals1-23
 *     Lukas Hanke <hanke@yatta.de> - Bug 241696 [quick fix] quickfix to iterate over a collection - https://bugs.eclipse.org/bugs/show_bug.cgi?id=241696
 *     Lukas Hanke <hanke@yatta.de> - Bug 430818 [1.8][quick fix] Quick fix for "for loop" is not shown for bare local variable/argument/field - https://bugs.eclipse.org/bugs/show_bug.cgi?id=430818
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
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.corext.codemanipulation.StubUtility;
import org.eclipse.jdt.internal.corext.fix.FixMessages;
import org.eclipse.jdt.internal.corext.template.java.CodeTemplateContextType;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.text.correction.AssistContext;
import org.eclipse.jdt.internal.ui.text.correction.QuickAssistProcessor;
import org.eclipse.jdt.internal.ui.text.correction.proposals.AssignToVariableAssistProposal;
import org.eclipse.jdt.internal.ui.text.correction.proposals.LinkedNamesAssistProposal;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jdt.ui.text.java.correction.CUCorrectionProposal;
import org.eclipse.jface.preference.IPreferenceStore;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class AssistQuickFixTest extends QuickFixTest {

//	private static final Class THIS= AssistQuickFixTest.class;

	private static final String CHANGE_MODIFIER_TO_FINAL= FixMessages.VariableDeclarationFix_changeModifierOfUnknownToFinal_description;

	private IJavaProject fJProject1;
	private IPackageFragmentRoot fSourceFolder;

	public AssistQuickFixTest() {
		super(new ProjectTestSetup());
	}

//	public static Test suite() {
//		return setUpTest(new TestSuite(THIS));
//	}

//	public static Test setUpTest(Test test) {
//		return new ProjectTestSetup(test);
//	}

//	@Override
	@Before
	public void setUp() throws Exception {
        super.setUp();
		Hashtable options= TestOptions.getDefaultOptions();
		options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, JavaCore.SPACE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, "4");
		options.put(JavaCore.CODEASSIST_FIELD_PREFIXES, "");
		options.put(JavaCore.CODEASSIST_STATIC_FIELD_PREFIXES, "");
		options.put(JavaCore.CODEASSIST_FIELD_SUFFIXES, "");
		options.put(JavaCore.CODEASSIST_STATIC_FIELD_SUFFIXES, "");

		JavaCore.setOptions(options);

		IPreferenceStore store= JavaPlugin.getDefault().getPreferenceStore();
		store.setValue(PreferenceConstants.CODEGEN_ADD_COMMENTS, false);
		store.setValue(PreferenceConstants.CODEGEN_KEYWORD_THIS, false);


		StubUtility.setCodeTemplate(CodeTemplateContextType.METHODSTUB_ID, "//TODO\n${body_statement}", null);

//		Preferences corePrefs= JavaPlugin.getJavaCorePluginPreferences();
//		corePrefs.setValue(JavaCore.CODEASSIST_FIELD_PREFIXES, "");
//		corePrefs.setValue(JavaCore.CODEASSIST_STATIC_FIELD_PREFIXES, "");
//		corePrefs.setValue(JavaCore.CODEASSIST_FIELD_SUFFIXES, "");
//		corePrefs.setValue(JavaCore.CODEASSIST_STATIC_FIELD_SUFFIXES, "");

		fJProject1= ProjectTestSetup.getProject();

		fSourceFolder= JavaProjectHelper.addSourceContainer(fJProject1, "src");
        new org.eclipse.jdt.core.JavaCore();
	}


//	@Override
	@After
	public void tearDown() throws Exception {
		super.tearDown();
		JavaProjectHelper.clear(fJProject1, ProjectTestSetup.getDefaultClasspath());
	}

	@Test
	public void testAssignToLocal() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        getClass();\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("getClass()");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 2);
		assertCorrectLabels(proposals);

		CUCorrectionProposal proposal= (CUCorrectionProposal) proposals.get(0);
		String preview1= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    private Class<? extends E> class1;\n");
		buf.append("\n");
		buf.append("    public void foo() {\n");
		buf.append("        class1 = getClass();\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		proposal= (CUCorrectionProposal) proposals.get(1);
		String preview2= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        Class<? extends E> class1 = getClass();\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected2= buf.toString();

		assertEqualStringsIgnoreOrder(new String[] { preview1, preview2 }, new String[] { expected1, expected2 });

	}
	@Test
	public void testAssignToLocal2() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.Vector;\n");
		buf.append("public class E {\n");
		buf.append("    public Vector goo() {\n");
		buf.append("        return null;\n");
		buf.append("    }\n");
		buf.append("    public void foo() {\n");
		buf.append("        goo().iterator();\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("goo().iterator()");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 2);
		assertCorrectLabels(proposals);

		CUCorrectionProposal proposal= (CUCorrectionProposal) proposals.get(0);
		String preview1= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.Iterator;\n");
		buf.append("import java.util.Vector;\n");
		buf.append("public class E {\n");
		buf.append("    private Iterator iterator;\n");
		buf.append("    public Vector goo() {\n");
		buf.append("        return null;\n");
		buf.append("    }\n");
		buf.append("    public void foo() {\n");
		buf.append("        iterator = goo().iterator();\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		proposal= (CUCorrectionProposal) proposals.get(1);
		String preview2= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.Iterator;\n");
		buf.append("import java.util.Vector;\n");
		buf.append("public class E {\n");
		buf.append("    public Vector goo() {\n");
		buf.append("        return null;\n");
		buf.append("    }\n");
		buf.append("    public void foo() {\n");
		buf.append("        Iterator iterator = goo().iterator();\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected2= buf.toString();


		assertEqualStringsIgnoreOrder(new String[] { preview1, preview2 }, new String[] { expected1, expected2 });
	}

	@Test
	public void testAssignToLocal3() throws Exception {
		// test prefixes and this qualification

		IPreferenceStore store= JavaPlugin.getDefault().getPreferenceStore();
		store.setValue(PreferenceConstants.CODEGEN_KEYWORD_THIS, true);
//		Preferences corePrefs= JavaPlugin.getDefault();
		fJProject1.setOption(org.eclipse.jdt.core.JavaCore.CODEASSIST_FIELD_PREFIXES, "f");
		fJProject1.setOption(org.eclipse.jdt.core.JavaCore.CODEASSIST_LOCAL_PREFIXES, "_");

//		corePrefs.setValue(JavaCore.CODEASSIST_FIELD_PREFIXES, "f");
//		corePrefs.setValue(JavaCore.CODEASSIST_LOCAL_PREFIXES, "_");


		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("\n");
		buf.append("    private int fCount;\n");
		buf.append("\n");
		buf.append("    public void foo() {\n");
		buf.append("        System.getSecurityManager();\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("System");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 2);
		assertCorrectLabels(proposals);

		CUCorrectionProposal proposal= (CUCorrectionProposal) proposals.get(0);
		String preview1= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("\n");
		buf.append("    private int fCount;\n");
		buf.append("    private SecurityManager fSecurityManager;\n");
		buf.append("\n");
		buf.append("    public void foo() {\n");
		buf.append("        this.fSecurityManager = System.getSecurityManager();\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		proposal= (CUCorrectionProposal) proposals.get(1);
		String preview2= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("\n");
		buf.append("    private int fCount;\n");
		buf.append("\n");
		buf.append("    public void foo() {\n");
		buf.append("        SecurityManager _securityManager = System.getSecurityManager();\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected2= buf.toString();

		assertEqualStringsIgnoreOrder(new String[] { preview1, preview2 }, new String[] { expected1, expected2 });
	}

	@Test
	public void testAssignToLocal4() throws Exception {
		// test name conflict

		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("\n");
		buf.append("    private int f;\n");
		buf.append("\n");
		buf.append("    public void foo() {\n");
		buf.append("        Math.min(1.0f, 2.0f);\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("Math");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 2);
		assertCorrectLabels(proposals);

		CUCorrectionProposal proposal= (CUCorrectionProposal) proposals.get(0);
		String preview1= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("\n");
		buf.append("    private int f;\n");
		buf.append("    private float min;\n");
		buf.append("\n");
		buf.append("    public void foo() {\n");
		buf.append("        min = Math.min(1.0f, 2.0f);\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		proposal= (CUCorrectionProposal) proposals.get(1);
		String preview2= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("\n");
		buf.append("    private int f;\n");
		buf.append("\n");
		buf.append("    public void foo() {\n");
		buf.append("        float min = Math.min(1.0f, 2.0f);\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected2= buf.toString();

		assertEqualStringsIgnoreOrder(new String[] { preview1, preview2 }, new String[] { expected1, expected2 });
	}

	@Test
	public void testAssignToLocal5() throws Exception {
		// test prefixes and this qualification on static method

		IPreferenceStore store= JavaPlugin.getDefault().getPreferenceStore();
		store.setValue(PreferenceConstants.CODEGEN_KEYWORD_THIS, true);
//		Preferences corePrefs= JavaPlugin.getJavaCorePluginPreferences();
		fJProject1.setOption(org.eclipse.jdt.core.JavaCore.CODEASSIST_FIELD_PREFIXES, "f");
		fJProject1.setOption(org.eclipse.jdt.core.JavaCore.CODEASSIST_STATIC_FIELD_PREFIXES, "fg");
		fJProject1.setOption(org.eclipse.jdt.core.JavaCore.CODEASSIST_LOCAL_PREFIXES, "_");

//		corePrefs.setValue(JavaCore.CODEASSIST_FIELD_PREFIXES, "f");
//		corePrefs.setValue(JavaCore.CODEASSIST_STATIC_FIELD_PREFIXES, "fg");
//		corePrefs.setValue(JavaCore.CODEASSIST_LOCAL_PREFIXES, "_");


		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("\n");
		buf.append("    private int fCount;\n");
		buf.append("\n");
		buf.append("    public static void foo() {\n");
		buf.append("        System.getSecurityManager();\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("System");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 2);
		assertCorrectLabels(proposals);

		CUCorrectionProposal proposal= (CUCorrectionProposal) proposals.get(0);
		String preview1= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("\n");
		buf.append("    private int fCount;\n");
		buf.append("    private static SecurityManager fgSecurityManager;\n");
		buf.append("\n");
		buf.append("    public static void foo() {\n");
		buf.append("        E.fgSecurityManager = System.getSecurityManager();\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		proposal= (CUCorrectionProposal) proposals.get(1);
		String preview2= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("\n");
		buf.append("    private int fCount;\n");
		buf.append("\n");
		buf.append("    public static void foo() {\n");
		buf.append("        SecurityManager _securityManager = System.getSecurityManager();\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected2= buf.toString();

		assertEqualStringsIgnoreOrder(new String[] { preview1, preview2 }, new String[] { expected1, expected2 });
	}

	@Test
	public void testAssignToLocal6() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    static {\n");
		buf.append("        getClass(); // comment\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("getClass()");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 2);
		assertCorrectLabels(proposals);

		CUCorrectionProposal proposal= (CUCorrectionProposal) proposals.get(0);
		String preview1= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    private static Class<? extends E> class1;\n");
		buf.append("\n");
		buf.append("    static {\n");
		buf.append("        class1 = getClass(); // comment\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		proposal= (CUCorrectionProposal) proposals.get(1);
		String preview2= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    static {\n");
		buf.append("        Class<? extends E> class1 = getClass(); // comment\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected2= buf.toString();

		assertEqualStringsIgnoreOrder(new String[] { preview1, preview2 }, new String[] { expected1, expected2 });
	}

	@Test
	public void testAssignToLocal7() throws Exception {
		// test name conflict: name used later

		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.Vector;\n");
		buf.append("public class E {\n");
		buf.append("    public Vector goo() {\n");
		buf.append("        return null;\n");
		buf.append("    }\n");
		buf.append("    public void foo() {\n");
		buf.append("        goo().iterator();\n");
		buf.append("        Object iterator= null;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("goo().iterator()");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 2);
		assertCorrectLabels(proposals);

		CUCorrectionProposal proposal= (CUCorrectionProposal) proposals.get(0);
		String preview1= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.Iterator;\n");
		buf.append("import java.util.Vector;\n");
		buf.append("public class E {\n");
		buf.append("    private Iterator iterator2;\n");
		buf.append("    public Vector goo() {\n");
		buf.append("        return null;\n");
		buf.append("    }\n");
		buf.append("    public void foo() {\n");
		buf.append("        iterator2 = goo().iterator();\n");
		buf.append("        Object iterator= null;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		proposal= (CUCorrectionProposal) proposals.get(1);
		String preview2= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.Iterator;\n");
		buf.append("import java.util.Vector;\n");
		buf.append("public class E {\n");
		buf.append("    public Vector goo() {\n");
		buf.append("        return null;\n");
		buf.append("    }\n");
		buf.append("    public void foo() {\n");
		buf.append("        Iterator iterator2 = goo().iterator();\n");
		buf.append("        Object iterator= null;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected2= buf.toString();


		assertEqualStringsIgnoreOrder(new String[] { preview1, preview2 }, new String[] { expected1, expected2 });
	}

	@Test
	public void testAssignToLocal8() throws Exception {
		// assign to local of field access

		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public class MyLayout {\n");
		buf.append("        int indent;\n");
		buf.append("    }\n");
		buf.append("    public void foo() {\n");
		buf.append("        new MyLayout().indent;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("new MyLayout().indent;");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		List proposals= collectAssists(context, false);

		int numberOfProposals= 5;
		assertNumberOfProposals(proposals, numberOfProposals);
		assertCorrectLabels(proposals);

		ArrayList previews= new ArrayList();
		ArrayList expecteds= new ArrayList();
		
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public class MyLayout {\n");
		buf.append("        int indent;\n");
		buf.append("    }\n");
		buf.append("    public void foo() {\n");
		buf.append("        int indent = new MyLayout().indent;\n");
		buf.append("    }\n");
		buf.append("}\n");
		addPreviewAndExpected(proposals, buf, expecteds, previews);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    private int indent;\n");
		buf.append("    public class MyLayout {\n");
		buf.append("        int indent;\n");
		buf.append("    }\n");
		buf.append("    public void foo() {\n");
		buf.append("        indent = new MyLayout().indent;\n");
		buf.append("    }\n");
		buf.append("}\n");
		addPreviewAndExpected(proposals, buf, expecteds, previews);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public class MyLayout {\n");
		buf.append("        int indent;\n");
		buf.append("    }\n");
		buf.append("    public void foo() {\n");
		buf.append("        MyLayout myLayout = new MyLayout();\n");
		buf.append("        myLayout.indent;\n");
		buf.append("    }\n");
		buf.append("}\n");
		addPreviewAndExpected(proposals, buf, expecteds, previews);
		
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public class MyLayout {\n");
		buf.append("        int indent;\n");
		buf.append("    }\n");
		buf.append("    public void foo() {\n");
		buf.append("        MyLayout myLayout = new MyLayout();\n");
		buf.append("        myLayout.indent;\n");
		buf.append("    }\n");
		buf.append("}\n");
		addPreviewAndExpected(proposals, buf, expecteds, previews);
		
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    private static final MyLayout MY_LAYOUT = new MyLayout();\n");
		buf.append("    public class MyLayout {\n");
		buf.append("        int indent;\n");
		buf.append("    }\n");
		buf.append("    public void foo() {\n");
		buf.append("        MY_LAYOUT.indent;\n");
		buf.append("    }\n");
		buf.append("}\n");
		addPreviewAndExpected(proposals, buf, expecteds, previews);
		
		assertEqualStringsIgnoreOrder(previews, expecteds);
	}

	@Test
	public void testAssignToLocal9() throws Exception {
		// assign to local of field access

		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    private int[] fField;\n");
		buf.append("    public void foo() {\n");
		buf.append("        fField[0];\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		String string= "fField[0];";
		int offset= buf.toString().indexOf(string);
		AssistContext context= getCorrectionContext(cu, offset, string.length());
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 3);
		assertCorrectLabels(proposals);

		String[] expected= new String[3];

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    private int[] fField;\n");
		buf.append("    public void foo() {\n");
		buf.append("        int i = fField[0];\n");
		buf.append("    }\n");
		buf.append("}\n");
		expected[0]= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    private int[] fField;\n");
		buf.append("    private int i;\n");
		buf.append("    public void foo() {\n");
		buf.append("        i = fField[0];\n");
		buf.append("    }\n");
		buf.append("}\n");
		expected[1]= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    private int[] fField;\n");
		buf.append("    public void foo() {\n");
		buf.append("        extracted();\n");
		buf.append("    }\n");
		buf.append("    private void extracted() {\n");
		buf.append("        fField[0];\n");
		buf.append("    }\n");
		buf.append("}\n");
		expected[2]= buf.toString();

		assertExpectedExistInProposals(proposals, expected);
	}

	@Test
	public void testAssignToLocal10() throws Exception {
		// assign to local with recovered statement

		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        System.getProperties()\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("System.getProperties()");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 2);
		assertCorrectLabels(proposals);

		CUCorrectionProposal proposal= (CUCorrectionProposal) proposals.get(0);
		String preview1= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("\n");
		buf.append("import java.util.Properties;\n");
		buf.append("\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        Properties properties = System.getProperties();\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		proposal= (CUCorrectionProposal) proposals.get(1);
		String preview2= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("\n");
		buf.append("import java.util.Properties;\n");
		buf.append("\n");
		buf.append("public class E {\n");
		buf.append("    private Properties properties;\n");
		buf.append("\n");
		buf.append("    public void foo() {\n");
		buf.append("        properties = System.getProperties();\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected2= buf.toString();


		assertEqualStringsIgnoreOrder(new String[] { preview1, preview2 }, new String[] { expected1, expected2 });
	}

	@Test
	public void testAssignToLocal11() throws Exception {
		// assign to statement in if body with no brackets

		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(int i) {\n");
		buf.append("        if (i == 0)\n");
		buf.append("            System.getProperties();\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("System.getProperties()");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 2);
		assertCorrectLabels(proposals);

		CUCorrectionProposal proposal= (CUCorrectionProposal) proposals.get(0);
		String preview1= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("\n");
		buf.append("import java.util.Properties;\n");
		buf.append("\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(int i) {\n");
		buf.append("        if (i == 0) {\n");
		buf.append("            Properties properties = System.getProperties();\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		proposal= (CUCorrectionProposal) proposals.get(1);
		String preview2= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("\n");
		buf.append("import java.util.Properties;\n");
		buf.append("\n");
		buf.append("public class E {\n");
		buf.append("    private Properties properties;\n");
		buf.append("\n");
		buf.append("    public void foo(int i) {\n");
		buf.append("        if (i == 0)\n");
		buf.append("            properties = System.getProperties();\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected2= buf.toString();


		assertEqualStringsIgnoreOrder(new String[] { preview1, preview2 }, new String[] { expected1, expected2 });
	}

	@Test
	public void testAssignToLocal12() throws Exception {
		// assign to recovered statement in if body with no brackets

		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(int i) {\n");
		buf.append("        if (i == 0)\n");
		buf.append("           i++\n");
		buf.append("        else\n");
		buf.append("            System.getProperties()\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("System.getProperties()");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 2);
		assertCorrectLabels(proposals);

		CUCorrectionProposal proposal= (CUCorrectionProposal) proposals.get(0);
		String preview1= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("\n");
		buf.append("import java.util.Properties;\n");
		buf.append("\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(int i) {\n");
		buf.append("        if (i == 0)\n");
		buf.append("           i++\n");
		buf.append("        else {\n");
		buf.append("            Properties properties = System.getProperties();\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		proposal= (CUCorrectionProposal) proposals.get(1);
		String preview2= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("\n");
		buf.append("import java.util.Properties;\n");
		buf.append("\n");
		buf.append("public class E {\n");
		buf.append("    private Properties properties;\n");
		buf.append("\n");
		buf.append("    public void foo(int i) {\n");
		buf.append("        if (i == 0)\n");
		buf.append("           i++\n");
		buf.append("        else\n");
		buf.append("            properties = System.getProperties();\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected2= buf.toString();


		assertEqualStringsIgnoreOrder(new String[] { preview1, preview2 }, new String[] { expected1, expected2 });
	}

	@Test
	public void testAssignToLocal13() throws Exception {
		// assign to local in context that requires fully qualified type, https://bugs.eclipse.org/bugs/show_bug.cgi?id=239735

		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class Timer {\n");
		buf.append("    public static void main(String[] args) {\n");
		buf.append("        new java.util.Timer();\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("Timer.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("new java.util.Timer()");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 5);
		assertCorrectLabels(proposals);

		String[] expecteds= new String[5];
		
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class Timer {\n");
		buf.append("    public static void main(String[] args) {\n");
		buf.append("        java.util.Timer timer = new java.util.Timer();\n");
		buf.append("    }\n");
		buf.append("}\n");
		expecteds[0]= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class Timer {\n");
		buf.append("    private static java.util.Timer timer;\n");
		buf.append("\n");
		buf.append("    public static void main(String[] args) {\n");
		buf.append("        timer = new java.util.Timer();\n");
		buf.append("    }\n");
		buf.append("}\n");
		expecteds[1]= buf.toString();
		
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class Timer {\n");
		buf.append("    public static void main(String[] args) {\n");
		buf.append("        java.util.Timer timer = new java.util.Timer();\n");
		buf.append("    }\n");
		buf.append("}\n");
		expecteds[2]= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class Timer {\n");
		buf.append("    public static void main(String[] args) {\n");
		buf.append("        java.util.Timer timer = new java.util.Timer();\n");
		buf.append("    }\n");
		buf.append("}\n");
		expecteds[3]= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class Timer {\n");
		buf.append("    private static final java.util.Timer TIMER = new java.util.Timer();\n");
		buf.append("\n");
		buf.append("    public static void main(String[] args) {\n");
		buf.append("        new java.util.Timer();\n");
		buf.append("    }\n");
		buf.append("}\n");
		expecteds[4]= buf.toString();

		assertExpectedExistInProposals(proposals, expecteds);
	}

	@Test
	// bug 217984
	public void testAssignToLocal14() throws Exception {
			IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
			StringBuffer buf= new StringBuffer();
			buf.append("package test1;\n");
			buf.append("import java.util.ArrayList;\n");
			buf.append("import java.util.List;\n");
			buf.append("import java.util.RandomAccess;\n");
			buf.append("\n");
			buf.append("class Gen<E extends List<String> & RandomAccess> extends ArrayList<E> {\n");
			buf.append("    void foo() {\n");
			buf.append("        Gen<?> g = new Gen<>();\n");
			buf.append("        g.get(0);\n");
			buf.append("    }\n");
			buf.append("}\n");
			ICompilationUnit cu= pack1.createCompilationUnit("Gen.java", buf.toString(), false, null);
	
			String str= "g.get(0);";
			AssistContext context= getCorrectionContext(cu, buf.toString().indexOf(str) + str.length(), 0);
			List proposals= collectAssists(context, false);
			assertNumberOfProposals(proposals, 5);
			assertCorrectLabels(proposals);
	
			buf= new StringBuffer();
			buf.append("package test1;\n");
			buf.append("import java.util.ArrayList;\n");
			buf.append("import java.util.List;\n");
			buf.append("import java.util.RandomAccess;\n");
			buf.append("\n");
			buf.append("class Gen<E extends List<String> & RandomAccess> extends ArrayList<E> {\n");
			buf.append("    void foo() {\n");
			buf.append("        Gen<?> g = new Gen<>();\n");
			buf.append("        List<String> list = g.get(0);\n");
			buf.append("    }\n");
			buf.append("}\n");
			String expected1= buf.toString();
	
			buf= new StringBuffer();
			buf.append("package test1;\n");
			buf.append("import java.util.ArrayList;\n");
			buf.append("import java.util.List;\n");
			buf.append("import java.util.RandomAccess;\n");
			buf.append("\n");
			buf.append("class Gen<E extends List<String> & RandomAccess> extends ArrayList<E> {\n");
			buf.append("    private List<String> list;\n");
			buf.append("\n");
			buf.append("    void foo() {\n");
			buf.append("        Gen<?> g = new Gen<>();\n");
			buf.append("        list = g.get(0);\n");
			buf.append("    }\n");
			buf.append("}\n");
			String expected2= buf.toString();
	
			assertExpectedExistInProposals(proposals, new String[] { expected1, expected2 });
	
		}

	// bug 217984
	@Test
	public void testAssignToLocal15() throws Exception {
			IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
			StringBuffer buf= new StringBuffer();
			buf.append("package test1;\n");
			buf.append("import java.util.ArrayList;\n");
			buf.append("import java.util.List;\n");
			buf.append("import java.util.RandomAccess;\n");
			buf.append("\n");
			buf.append("class Gen<E extends List<String> & RandomAccess> extends ArrayList<E> {\n");
			buf.append("    void foo() {\n");
			buf.append("        Gen<? extends Cloneable> ge = new Gen<>();\n");
			buf.append("        ge.get(0);\n");
			buf.append("    }\n");
			buf.append("}\n");
			ICompilationUnit cu= pack1.createCompilationUnit("Gen.java", buf.toString(), false, null);
	
			String str= "ge.get(0)";
			AssistContext context= getCorrectionContext(cu, buf.toString().indexOf(str) + str.length(), 0);
			List proposals= collectAssists(context, false);
			assertNumberOfProposals(proposals, 7);
			assertCorrectLabels(proposals);
	
			buf= new StringBuffer();
			buf.append("package test1;\n");
			buf.append("import java.util.ArrayList;\n");
			buf.append("import java.util.List;\n");
			buf.append("import java.util.RandomAccess;\n");
			buf.append("\n");
			buf.append("class Gen<E extends List<String> & RandomAccess> extends ArrayList<E> {\n");
			buf.append("    void foo() {\n");
			buf.append("        Gen<? extends Cloneable> ge = new Gen<>();\n");
			buf.append("        Cloneable cloneable = ge.get(0);\n");
			buf.append("    }\n");
			buf.append("}\n");
			String expected1= buf.toString();
	
			buf= new StringBuffer();
			buf.append("package test1;\n");
			buf.append("import java.util.ArrayList;\n");
			buf.append("import java.util.List;\n");
			buf.append("import java.util.RandomAccess;\n");
			buf.append("\n");
			buf.append("class Gen<E extends List<String> & RandomAccess> extends ArrayList<E> {\n");
			buf.append("    private Cloneable cloneable;\n");
			buf.append("\n");
			buf.append("    void foo() {\n");
			buf.append("        Gen<? extends Cloneable> ge = new Gen<>();\n");
			buf.append("        cloneable = ge.get(0);\n");
			buf.append("    }\n");
			buf.append("}\n");
			String expected2= buf.toString();
	
			assertExpectedExistInProposals(proposals, new String[] { expected1, expected2 });
		}

	// bug 217984
	@Test
	public void testAssignToLocal16() throws Exception {
			IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
			StringBuffer buf= new StringBuffer();
			buf.append("package test1;\n");
			buf.append("import java.util.ArrayList;\n");
			buf.append("import java.util.List;\n");
			buf.append("import java.util.RandomAccess;\n");
			buf.append("import java.util.Vector;\n");
			buf.append("\n");
			buf.append("class Gen<E extends List<String> & RandomAccess> extends ArrayList<E> {\n");
			buf.append("    void foo() {\n");
			buf.append("        Gen<? super Vector<String>> gs = new Gen<>();\n");
			buf.append("        gs.get(0);\n");
			buf.append("    }\n");
			buf.append("}\n");
			ICompilationUnit cu= pack1.createCompilationUnit("Gen.java", buf.toString(), false, null);
	
			String str= "gs.get(0)";
			AssistContext context= getCorrectionContext(cu, buf.toString().indexOf(str) + str.length(), 0);
			List proposals= collectAssists(context, false);
			assertNumberOfProposals(proposals, 7);
			assertCorrectLabels(proposals);
	
			buf= new StringBuffer();
			buf.append("package test1;\n");
			buf.append("import java.util.ArrayList;\n");
			buf.append("import java.util.List;\n");
			buf.append("import java.util.RandomAccess;\n");
			buf.append("import java.util.Vector;\n");
			buf.append("\n");
			buf.append("class Gen<E extends List<String> & RandomAccess> extends ArrayList<E> {\n");
			buf.append("    void foo() {\n");
			buf.append("        Gen<? super Vector<String>> gs = new Gen<>();\n");
			buf.append("        List<String> list = gs.get(0);\n");
			buf.append("    }\n");
			buf.append("}\n");
			String expected1= buf.toString();
	
			buf= new StringBuffer();
			buf.append("package test1;\n");
			buf.append("import java.util.ArrayList;\n");
			buf.append("import java.util.List;\n");
			buf.append("import java.util.RandomAccess;\n");
			buf.append("import java.util.Vector;\n");
			buf.append("\n");
			buf.append("class Gen<E extends List<String> & RandomAccess> extends ArrayList<E> {\n");
			buf.append("    private List<String> list;\n");
			buf.append("\n");
			buf.append("    void foo() {\n");
			buf.append("        Gen<? super Vector<String>> gs = new Gen<>();\n");
			buf.append("        list = gs.get(0);\n");
			buf.append("    }\n");
			buf.append("}\n");
			String expected2= buf.toString();
	
			assertExpectedExistInProposals(proposals, new String[] { expected1, expected2 });
		}

	@Test
	public void testAssignParamToField() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public  E(int count) {\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		String selection= "count";
		int offset= buf.toString().indexOf(selection);
		AssistContext context= getCorrectionContext(cu, offset, selection.length());
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    private int count;\n");
		buf.append("\n");
		buf.append("    public  E(int count) {\n");
		buf.append("        this.count = count;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String ex1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { ex1 });
	}

	@Test
	public void testAssignParamToField2() throws Exception {
//		Preferences corePrefs= JavaPlugin.getJavaCorePluginPreferences();
//		corePrefs.setValue(JavaCore.CODEASSIST_FIELD_PREFIXES, "f");
		fJProject1.setOption(org.eclipse.jdt.core.JavaCore.CODEASSIST_FIELD_PREFIXES, "f");

		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.Vector;\n");
		buf.append("public class E {\n");
		buf.append("    public  E(int count, Vector vec[]) {\n");
		buf.append("        super();\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		String selection= "vec";
		int offset= buf.toString().indexOf(selection);
		AssistContext context= getCorrectionContext(cu, offset, selection.length());
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.Vector;\n");
		buf.append("public class E {\n");
		buf.append("    private Vector[] fVec;\n");
		buf.append("\n");
		buf.append("    public  E(int count, Vector vec[]) {\n");
		buf.append("        super();\n");
		buf.append("        fVec = vec;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String ex1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { ex1 });
	}

	@Test
	public void testAssignParamToField3() throws Exception {
//		Preferences corePrefs= JavaPlugin.getJavaCorePluginPreferences();
//		corePrefs.setValue(JavaCore.CODEASSIST_STATIC_FIELD_PREFIXES, "fg");
		fJProject1.setOption(org.eclipse.jdt.core.JavaCore.CODEASSIST_STATIC_FIELD_PREFIXES, "fg");

		IPreferenceStore store= JavaPlugin.getDefault().getPreferenceStore();
		store.setValue(PreferenceConstants.CODEGEN_KEYWORD_THIS, true);

		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.Vector;\n");
		buf.append("public class E {\n");
		buf.append("    private int fgVec;\n");
		buf.append("\n");
		buf.append("    public static void foo(int count, Vector vec[]) {\n");
		buf.append("        count++;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		String selection= "vec";
		int offset= buf.toString().indexOf(selection);
		AssistContext context= getCorrectionContext(cu, offset, selection.length());
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.Vector;\n");
		buf.append("public class E {\n");
		buf.append("    private int fgVec;\n");
		buf.append("    private static Vector[] fgVec2;\n");
		buf.append("\n");
		buf.append("    public static void foo(int count, Vector vec[]) {\n");
		buf.append("        E.fgVec2 = vec;\n");
		buf.append("        count++;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String ex1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { ex1 });
	}
	@Test
	public void testAssignParamToField4() throws Exception {
		IPreferenceStore store= JavaPlugin.getDefault().getPreferenceStore();
		store.setValue(PreferenceConstants.CODEGEN_KEYWORD_THIS, true);

		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    private long count;\n");
		buf.append("\n");
		buf.append("    public void foo(int count) {\n");
		buf.append("        count++;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("int count");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 2);
		assertCorrectLabels(proposals);

		CUCorrectionProposal proposal= (CUCorrectionProposal) proposals.get(0);
		String preview1= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    private long count;\n");
		buf.append("    private int count2;\n");
		buf.append("\n");
		buf.append("    public void foo(int count) {\n");
		buf.append("        this.count2 = count;\n");
		buf.append("        count++;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		proposal= (CUCorrectionProposal) proposals.get(1);
		String preview2= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    private long count;\n");
		buf.append("\n");
		buf.append("    public void foo(int count) {\n");
		buf.append("        this.count = count;\n");
		buf.append("        count++;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected2= buf.toString();

		assertEqualStringsIgnoreOrder(new String[] { preview1, preview2 }, new String[] { expected1, expected2 });
	}

	@Test
	public void testAssignParamToField5() throws Exception {
		IPreferenceStore store= JavaPlugin.getDefault().getPreferenceStore();
		store.setValue(PreferenceConstants.CODEGEN_KEYWORD_THIS, true);

		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    private int p1;\n");
		buf.append("\n");
		buf.append("    public void foo(int p1, int p2) {\n");
		buf.append("        this.p1 = p1;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("int p2");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 2);
		assertCorrectLabels(proposals);

		CUCorrectionProposal proposal= (CUCorrectionProposal) proposals.get(0);
		String preview1= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    private int p1;\n");
		buf.append("    private int p2;\n");
		buf.append("\n");
		buf.append("    public void foo(int p1, int p2) {\n");
		buf.append("        this.p1 = p1;\n");
		buf.append("        this.p2 = p2;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		proposal= (CUCorrectionProposal) proposals.get(1);
		String preview2= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    private int p1;\n");
		buf.append("\n");
		buf.append("    public void foo(int p1, int p2) {\n");
		buf.append("        this.p1 = p1;\n");
		buf.append("        this.p1 = p2;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected2= buf.toString();

		assertEqualStringsIgnoreOrder(new String[] { preview1, preview2 }, new String[] { expected1, expected2 });
	}

	@Test
	public void testAssignParamToField6() throws Exception {
		IPreferenceStore store= JavaPlugin.getDefault().getPreferenceStore();
		store.setValue(PreferenceConstants.CODEGEN_KEYWORD_THIS, true);

		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    private Float p1;\n");
		buf.append("    private Number p2;\n");
		buf.append("\n");
		buf.append("    public void foo(Float p1, Integer p2) {\n");
		buf.append("        this.p1 = p1;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("Integer p2");
		AssistContext context= getCorrectionContext(cu, offset, 0);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 2);
		assertCorrectLabels(proposals);

		CUCorrectionProposal proposal= (CUCorrectionProposal) proposals.get(0);
		String preview1= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    private Float p1;\n");
		buf.append("    private Number p2;\n");
		buf.append("\n");
		buf.append("    public void foo(Float p1, Integer p2) {\n");
		buf.append("        this.p1 = p1;\n");
		buf.append("        this.p2 = p2;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		proposal= (CUCorrectionProposal) proposals.get(1);
		String preview2= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    private Float p1;\n");
		buf.append("    private Number p2;\n");
		buf.append("    private Integer p22;\n");
		buf.append("\n");
		buf.append("    public void foo(Float p1, Integer p2) {\n");
		buf.append("        this.p1 = p1;\n");
		buf.append("        this.p22 = p2;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected2= buf.toString();

		assertEqualStringsIgnoreOrder(new String[] { preview1, preview2 }, new String[] { expected1, expected2 });
	}

	@Test
	public void testAssignParamToFieldInGeneric() throws Exception {
//		Preferences corePrefs= JavaPlugin.getJavaCorePluginPreferences();
//		corePrefs.setValue(JavaCore.CODEASSIST_FIELD_PREFIXES, "f");
		fJProject1.setOption(org.eclipse.jdt.core.JavaCore.CODEASSIST_FIELD_PREFIXES, "f");

		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.Vector;\n");
		buf.append("public class E<T> {\n");
		buf.append("    public  E(int count, Vector<String>[] vec) {\n");
		buf.append("        super();\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		String selection= "vec";
		int offset= buf.toString().indexOf(selection);
		AssistContext context= getCorrectionContext(cu, offset, selection.length());
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.Vector;\n");
		buf.append("public class E<T> {\n");
		buf.append("    private Vector<String>[] fVec;\n");
		buf.append("\n");
		buf.append("    public  E(int count, Vector<String>[] vec) {\n");
		buf.append("        super();\n");
		buf.append("        fVec = vec;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String ex1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { ex1 });
	}

	@Test
	public void testAssignToLocal2CursorAtEnd() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.Vector;\n");
		buf.append("public class E {\n");
		buf.append("    public Vector goo() {\n");
		buf.append("        return null;\n");
		buf.append("    }\n");
		buf.append("    public void foo() {\n");
		buf.append("        goo().toArray();\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		String str= "goo().toArray();";
		AssistContext context= getCorrectionContext(cu, buf.toString().indexOf(str) + str.length(), 0);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 4);
		assertCorrectLabels(proposals);

		CUCorrectionProposal proposal= (CUCorrectionProposal) proposals.get(0);
		String preview1= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.Vector;\n");
		buf.append("public class E {\n");
		buf.append("    private Object[] array;\n");
		buf.append("    public Vector goo() {\n");
		buf.append("        return null;\n");
		buf.append("    }\n");
		buf.append("    public void foo() {\n");
		buf.append("        array = goo().toArray();\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		proposal= (CUCorrectionProposal) proposals.get(1);
		String preview2= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.Vector;\n");
		buf.append("public class E {\n");
		buf.append("    public Vector goo() {\n");
		buf.append("        return null;\n");
		buf.append("    }\n");
		buf.append("    public void foo() {\n");
		buf.append("        Object[] array = goo().toArray();\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected2= buf.toString();

		assertEqualStringsIgnoreOrder(new String[] { preview1, preview2 }, new String[] { expected1, expected2 });
	}
	@Test
	public void testExtractToLocalVariable1() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public E() {\n");
		buf.append("        int a = 1;\n");
		buf.append("        int b = 1;\n");
		buf.append("        int d = a + b;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		String selection= "a + b";
		int offset= buf.toString().indexOf(selection);
		AssistContext context= getCorrectionContext(cu, offset, selection.length());
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 5);
		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public E() {\n");
		buf.append("        int a = 1;\n");
		buf.append("        int b = 1;\n");
		buf.append("        int i = a + b;\n");
		buf.append("        int d = i;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String ex1= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public E() {\n");
		buf.append("        int a = 1;\n");
		buf.append("        int b = 1;\n");
		buf.append("        int i = a + b;\n");
		buf.append("        int d = i;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String ex2= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public E() {\n");
		buf.append("        int a = 1;\n");
		buf.append("        int b = 1;\n");
		buf.append("        int d = extracted(a, b);\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("    private int extracted(int a, int b) {\n");
		buf.append("        return a + b;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String ex3= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public E() {\n");
		buf.append("        int a = 1;\n");
		buf.append("        int b = 1;\n");
		buf.append("        int d = (a + b);\n");
		buf.append("    }\n");
		buf.append("}\n");
		String ex4= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public E() {\n");
		buf.append("        int a = 1;\n");
		buf.append("        int b = 1;\n");
		buf.append("        int d = b + a;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String ex5= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { ex1, ex2, ex3, ex4, ex5 });
	}

	@Test
	public void testExtractToLocalVariable2() throws Exception {
		//https://bugs.eclipse.org/bugs/show_bug.cgi?id=276467
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public E() {\n");
		buf.append("        int a = 1;\n");
		buf.append("        int b = 1;\n");
		buf.append("        int c = 1;\n");
		buf.append("        int d = a + b + c;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		String selection= "b + c";
		int offset= buf.toString().indexOf(selection);
		AssistContext context= getCorrectionContext(cu, offset, selection.length());
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 3);
		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public E() {\n");
		buf.append("        int a = 1;\n");
		buf.append("        int b = 1;\n");
		buf.append("        int c = 1;\n");
		buf.append("        int i = b + c;\n");
		buf.append("        int d = a + i;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String ex1= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public E() {\n");
		buf.append("        int a = 1;\n");
		buf.append("        int b = 1;\n");
		buf.append("        int c = 1;\n");
		buf.append("        int i = b + c;\n");
		buf.append("        int d = a + i;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String ex2= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public E() {\n");
		buf.append("        int a = 1;\n");
		buf.append("        int b = 1;\n");
		buf.append("        int c = 1;\n");
		buf.append("        int d = c + a + b;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String ex3= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { ex1, ex2, ex3 });
	}

	@Test
	public void testExtractToLocalVariable3() throws Exception {
		//https://bugs.eclipse.org/bugs/show_bug.cgi?id=276467
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public E() {\n");
		buf.append("        int a = 1;\n");
		buf.append("        int b = 1;\n");
		buf.append("        int c = 1;\n");
		buf.append("        int d = a + b + c;\n");
		buf.append("        int e = a + b + c;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		String selection= "b + c";
		int offset= buf.toString().indexOf(selection);
		AssistContext context= getCorrectionContext(cu, offset, selection.length());
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 3);
		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public E() {\n");
		buf.append("        int a = 1;\n");
		buf.append("        int b = 1;\n");
		buf.append("        int c = 1;\n");
		buf.append("        int i = b + c;\n");
		buf.append("        int d = a + i;\n");
		buf.append("        int e = a + i;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String ex1= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public E() {\n");
		buf.append("        int a = 1;\n");
		buf.append("        int b = 1;\n");
		buf.append("        int c = 1;\n");
		buf.append("        int i = b + c;\n");
		buf.append("        int d = a + i;\n");
		buf.append("        int e = a + b + c;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String ex2= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public E() {\n");
		buf.append("        int a = 1;\n");
		buf.append("        int b = 1;\n");
		buf.append("        int c = 1;\n");
		buf.append("        int d = c + a + b;\n");
		buf.append("        int e = a + b + c;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String ex3= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { ex1, ex2, ex3 });
	}

	@Test
	public void testExtractToMethod1() throws Exception {
		//https://bugs.eclipse.org/bugs/show_bug.cgi?id=41302
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public E() {\n");
		buf.append("        int a = 1;\n");
		buf.append("        int b = 1;\n");
		buf.append("        int d = a + b;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		String selection= "a + b";
		int offset= buf.toString().indexOf(selection);
		AssistContext context= getCorrectionContext(cu, offset, selection.length());
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 5);
		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public E() {\n");
		buf.append("        int a = 1;\n");
		buf.append("        int b = 1;\n");
		buf.append("        int d = extracted(a, b);\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("    private int extracted(int a, int b) {\n");
		buf.append("        return a + b;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String ex1= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public E() {\n");
		buf.append("        int a = 1;\n");
		buf.append("        int b = 1;\n");
		buf.append("        int i = a + b;\n");
		buf.append("        int d = i;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String ex2= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public E() {\n");
		buf.append("        int a = 1;\n");
		buf.append("        int b = 1;\n");
		buf.append("        int i = a + b;\n");
		buf.append("        int d = i;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String ex3= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public E() {\n");
		buf.append("        int a = 1;\n");
		buf.append("        int b = 1;\n");
		buf.append("        int d = (a + b);\n");
		buf.append("    }\n");
		buf.append("}\n");
		String ex4= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public E() {\n");
		buf.append("        int a = 1;\n");
		buf.append("        int b = 1;\n");
		buf.append("        int d = b + a;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String ex5= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { ex1, ex2, ex3, ex4, ex5 });
	}

	@Test
	public void testExtractToMethod2() throws Exception {
		//https://bugs.eclipse.org/bugs/show_bug.cgi?id=41302
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    void foo() {\n");
		buf.append("        int a = 1;\n");
		buf.append("        int b = 1;\n");
		buf.append("        int d = a + b;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset1= buf.toString().indexOf("int b = 1;");
		int offset2= buf.toString().indexOf("a + b;") + 6;
		AssistContext context= getCorrectionContext(cu, offset1, offset2 - offset1);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 2);
		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    void foo() {\n");
		buf.append("        int a = 1;\n");
		buf.append("        extracted(a);\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("    private void extracted(int a) {\n");
		buf.append("        int b = 1;\n");
		buf.append("        int d = a + b;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String ex1= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    void foo() {\n");
		buf.append("        int a = 1;\n");
		buf.append("        final int b = 1;\n");
		buf.append("        final int d = a + b;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String ex2= buf.toString();

		buf= new StringBuffer();
		String ex3= null; // Wrap in buf.append() (to clipboard)

		assertExpectedExistInProposals(proposals, new String[] { ex1, ex2, ex3 });
	}

	@Test
	public void testExtractToMethod3() throws Exception {
		//https://bugs.eclipse.org/bugs/show_bug.cgi?id=41302
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    void foo() {\n");
		buf.append("        int a = 1;\n");
		buf.append("        int b = 1;\n");
		buf.append("        int d = a + b;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset1= buf.toString().indexOf("int a = 1;");
		int offset2= buf.toString().indexOf("a + b;") + 6;
		AssistContext context= getCorrectionContext(cu, offset1, offset2 - offset1);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 2);
		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    void foo() {\n");
		buf.append("        extracted();\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("    private void extracted() {\n");
		buf.append("        int a = 1;\n");
		buf.append("        int b = 1;\n");
		buf.append("        int d = a + b;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String ex1= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    void foo() {\n");
		buf.append("        final int a = 1;\n");
		buf.append("        final int b = 1;\n");
		buf.append("        final int d = a + b;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String ex2= buf.toString();

		buf= new StringBuffer();
		String ex3= null; // Wrap in buf.append() (to clipboard)

		assertExpectedExistInProposals(proposals, new String[] { ex1, ex2, ex3 });
	}

	@Test
	public void testExtractToMethod4() throws Exception {
		//https://bugs.eclipse.org/bugs/show_bug.cgi?id=41302
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    void foo() {\n");
		buf.append("        int i = 0;\n");
		buf.append("        for (; true;)\n");
		buf.append("            i++;\n");
		buf.append("    }\n");

		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		String selection= "i++;";
		int offset= buf.toString().indexOf(selection);
		AssistContext context= getCorrectionContext(cu, offset, selection.length());

		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 4);
		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    void foo() {\n");
		buf.append("        int i = 0;\n");
		buf.append("        for (; true;)\n");
		buf.append("            i = extracted(i);\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("    private int extracted(int i) {\n");
		buf.append("        i++;\n");
		buf.append("        return i;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String ex1= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    void foo() {\n");
		buf.append("        int i = 0;\n");
		buf.append("        for (; true;) {\n");
		buf.append("            int j = i++;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String ex2= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    private int j;\n");
		buf.append("\n");
		buf.append("    void foo() {\n");
		buf.append("        int i = 0;\n");
		buf.append("        for (; true;)\n");
		buf.append("            j = i++;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String ex3= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    void foo() {\n");
		buf.append("        int i = 0;\n");
		buf.append("        for (; true;) {\n");
		buf.append("            i++;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String ex4= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { ex1, ex2, ex3, ex4 });
	}

	@Test
	public void testReplaceCatchClauseWithThrowsWithFinally() throws Exception {

		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.io.IOException;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        try {\n");
		buf.append("            goo();\n");
		buf.append("        } catch (IOException e) {\n");
		buf.append("        } finally {\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		String str= "(IOException e)";
		AssistContext context= getCorrectionContext(cu, buf.toString().indexOf(str), 0);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 2);
		assertCorrectLabels(proposals);

		CUCorrectionProposal proposal= (CUCorrectionProposal) proposals.get(0);
		String preview1= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.io.IOException;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() throws IOException {\n");
		buf.append("        try {\n");
		buf.append("            goo();\n");
		buf.append("        } finally {\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		proposal= (CUCorrectionProposal) proposals.get(1);
		String preview2= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.io.IOException;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        try {\n");
		buf.append("            goo();\n");
		buf.append("        } finally {\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected2= buf.toString();

		assertEqualStringsIgnoreOrder(new String[] { preview1, preview2 }, new String[] { expected1, expected2 });

	}

	@Test
	public void testReplaceSingleCatchClauseWithThrows() throws Exception {

		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.io.IOException;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        try {\n");
		buf.append("            goo();\n");
		buf.append("        } catch (IOException e) {\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		String str= "(IOException e)";
		AssistContext context= getCorrectionContext(cu, buf.toString().indexOf(str) + str.length(), 0);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 3);
		assertCorrectLabels(proposals);

		CUCorrectionProposal proposal= (CUCorrectionProposal) proposals.get(0);
		String preview1= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.io.IOException;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() throws IOException {\n");
		buf.append("        goo();\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		proposal= (CUCorrectionProposal) proposals.get(1);
		String preview2= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.io.IOException;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        goo();\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected2= buf.toString();

		proposal= (CUCorrectionProposal) proposals.get(2);
		String preview3= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.io.IOException;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        try {\n");
		buf.append("            goo();\n");
		buf.append("        } catch (IOException e) {\n");
		buf.append("        } finally {\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected3= buf.toString();

		assertEqualStringsIgnoreOrder(new String[] { preview1, preview2, preview3 }, new String[] { expected1, expected2, expected3 });

	}

	@Test
	public void testUnwrapForLoop() throws Exception {

		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        for (int i= 0; i < 3; i++) {\n");
		buf.append("            goo();\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		String str= "for";
		AssistContext context= getCorrectionContext(cu, buf.toString().indexOf(str) + str.length(), 0);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 2);
		assertCorrectLabels(proposals);

		CUCorrectionProposal proposal= (CUCorrectionProposal) proposals.get(0);
		String preview= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        goo();\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

		proposal= (CUCorrectionProposal)proposals.get(1);
		preview= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        for (int i= 0; i < 3; i++)\n");
		buf.append("            goo();\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

	@Test
	public void testUnwrapDoStatement() throws Exception {

		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        do {\n");
		buf.append("            goo();\n");
		buf.append("            goo();\n");
		buf.append("            goo();\n");
		buf.append("        } while (true);\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		String str= "do";
		AssistContext context= getCorrectionContext(cu, buf.toString().indexOf(str) + str.length(), 0);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);

		CUCorrectionProposal proposal= (CUCorrectionProposal) proposals.get(0);
		String preview= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        goo();\n");
		buf.append("        goo();\n");
		buf.append("        goo();\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

	@Test
	public void testUnwrapWhileLoop2Statements() throws Exception {

		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        while (true) {\n");
		buf.append("            goo();\n");
		buf.append("            System.out.println();\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		String str= "while";
		AssistContext context= getCorrectionContext(cu, buf.toString().indexOf(str) + str.length(), 0);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);

		CUCorrectionProposal proposal= (CUCorrectionProposal) proposals.get(0);
		String preview= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        goo();\n");
		buf.append("        System.out.println();\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

	@Test
	public void testUnwrapIfStatement() throws Exception {

		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        if (1+ 3 == 6) {\n");
		buf.append("            StringBuffer buf= new StringBuffer();\n");
		buf.append("            buf.append(1);\n");
		buf.append("            buf.append(2);\n");
		buf.append("            buf.append(3);\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		String str= "if";
		AssistContext context= getCorrectionContext(cu, buf.toString().indexOf(str) + str.length(), 0);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 4);
		assertCorrectLabels(proposals);

		CUCorrectionProposal proposal= (CUCorrectionProposal) proposals.get(0);
		String preview1= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        StringBuffer buf= new StringBuffer();\n");
		buf.append("        buf.append(1);\n");
		buf.append("        buf.append(2);\n");
		buf.append("        buf.append(3);\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		proposal= (CUCorrectionProposal) proposals.get(1);
		String preview2= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        if (1+ 3 == 6) {\n");
		buf.append("            StringBuffer buf= new StringBuffer();\n");
		buf.append("            buf.append(1);\n");
		buf.append("            buf.append(2);\n");
		buf.append("            buf.append(3);\n");
		buf.append("        } else {\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected2= buf.toString();

		proposal= (CUCorrectionProposal)proposals.get(2);
		String preview3= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        if (1+ 3 != 6)\n");
		buf.append("            return;\n");
		buf.append("        StringBuffer buf= new StringBuffer();\n");
		buf.append("        buf.append(1);\n");
		buf.append("        buf.append(2);\n");
		buf.append("        buf.append(3);\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected3= buf.toString();

		proposal= (CUCorrectionProposal)proposals.get(3);
		String preview4= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        switch (6) {\n");
		buf.append("            case 1+ 3 :\n");
		buf.append("                StringBuffer buf= new StringBuffer();\n");
		buf.append("                buf.append(1);\n");
		buf.append("                buf.append(2);\n");
		buf.append("                buf.append(3);\n");
		buf.append("                break;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected4= buf.toString();

		assertEqualStringsIgnoreOrder(new String[] { preview1, preview2, preview3, preview4 }, new String[] { expected1, expected2, expected3, expected4 });
	}

	@Test
	public void testUnwrapTryStatement() throws Exception {

		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        try {\n");
		buf.append("            StringBuffer buf= new StringBuffer();\n");
		buf.append("            buf.append(1);\n");
		buf.append("            buf.append(2);\n");
		buf.append("            buf.append(3);\n");
		buf.append("        } finally {\n");
		buf.append("            return;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		String str= "try";
		AssistContext context= getCorrectionContext(cu, buf.toString().indexOf(str) + str.length(), 0);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);

		CUCorrectionProposal proposal= (CUCorrectionProposal) proposals.get(0);
		String preview= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        StringBuffer buf= new StringBuffer();\n");
		buf.append("        buf.append(1);\n");
		buf.append("        buf.append(2);\n");
		buf.append("        buf.append(3);\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

	@Test
	public void testUnwrapAnonymous() throws Exception {

		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        Runnable run= new Runnable() {\n");
		buf.append("            public void run() { \n");
		buf.append("                throw new NullPointerException();\n");
		buf.append("            }\n");
		buf.append("        };\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		String str= "};";
		AssistContext context= getCorrectionContext(cu, buf.toString().indexOf(str), 0);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);

		CUCorrectionProposal proposal= (CUCorrectionProposal) proposals.get(0);
		String preview= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        throw new NullPointerException();\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

	@Test
	public void testUnwrapBlock() throws Exception {

		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        {\n");
		buf.append("            { \n");
		buf.append("                throw new NullPointerException();\n");
		buf.append("            }//comment\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		String str= "}//comment";
		AssistContext context= getCorrectionContext(cu, buf.toString().indexOf(str), 0);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);

		CUCorrectionProposal proposal= (CUCorrectionProposal) proposals.get(0);
		String preview= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        {\n");
		buf.append("            throw new NullPointerException();\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

	@Test
	public void testUnwrapMethodInvocation() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public int foo() {\n");
		buf.append("        return Math.abs(9+ 8);\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		String str= "Math.abs(9+ 8)";
		AssistContext context= getCorrectionContext(cu, buf.toString().indexOf(str) + str.length(), 0);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 4);
		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public int foo() {\n");
		buf.append("        return 9+ 8;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public int foo() {\n");
		buf.append("        int abs = Math.abs(9+ 8);\n");
		buf.append("        return abs;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected2= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public int foo() {\n");
		buf.append("        int abs = Math.abs(9+ 8);\n");
		buf.append("        return abs;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected3= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    private static final int ABS = Math.abs(9+ 8);\n");
		buf.append("\n");
		buf.append("    public int foo() {\n");
		buf.append("        return ABS;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected4= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1, expected2, expected3, expected4 });
	}

	@Test
	public void testSplitDeclaration1() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        int i = 9;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		String str= "=";
		AssistContext context= getCorrectionContext(cu, buf.toString().indexOf(str), 0);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);

		CUCorrectionProposal proposal= (CUCorrectionProposal) proposals.get(0);
		String preview= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        int i;\n");
		buf.append("        i = 9;\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

	@Test
	public void testSplitDeclaration2() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        for (int i = 0; i < 9; i++) {\n");
		buf.append("       }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		String str= "=";
		AssistContext context= getCorrectionContext(cu, buf.toString().indexOf(str), 0);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);

		CUCorrectionProposal proposal= (CUCorrectionProposal) proposals.get(0);
		String preview= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        int i;\n");
		buf.append("        for (i = 0; i < 9; i++) {\n");
		buf.append("       }\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

	@Test
	public void testSplitDeclaration3() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        final int i[] = null;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		String str= "i[]";
		AssistContext context= getCorrectionContext(cu, buf.toString().indexOf(str), 0);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 2);
		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        final int i[];\n");
		buf.append("        i = null;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String ex1= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    private int is[];\n");
		buf.append("\n");
		buf.append("    public void foo() {\n");
		buf.append("        is = null;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String ex2= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { ex1, ex2 });
	}

	@Test
	public void testSplitDeclaration4() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package e;\n");
		buf.append("public class Test {\n");
		buf.append("    public void test() {\n");
		buf.append("        String[] test = new String[0];\n");
		buf.append("    }\n");
		buf.append("}");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		String str= "=";
		AssistContext context= getCorrectionContext(cu, buf.toString().indexOf(str), 0);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);

		CUCorrectionProposal proposal= (CUCorrectionProposal) proposals.get(0);
		String preview= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package e;\n");
		buf.append("public class Test {\n");
		buf.append("    public void test() {\n");
		buf.append("        String[] test;\n");
		buf.append("        test = new String[0];\n");
		buf.append("    }\n");
		buf.append("}");
		assertEqualString(preview, buf.toString());
	}

	@Test
	public void testSplitDeclaration5() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package e;\n");
		buf.append("public class Test {\n");
		buf.append("    public void test() {\n");
		buf.append("        String[] test = { null };\n");
		buf.append("    }\n");
		buf.append("}");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		String str= "=";
		AssistContext context= getCorrectionContext(cu, buf.toString().indexOf(str), 0);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);

		CUCorrectionProposal proposal= (CUCorrectionProposal) proposals.get(0);
		String preview= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package e;\n");
		buf.append("public class Test {\n");
		buf.append("    public void test() {\n");
		buf.append("        String[] test;\n");
		buf.append("        test = new String[]{ null };\n");
		buf.append("    }\n");
		buf.append("}");
		assertEqualString(preview, buf.toString());
	}

	@Test
	public void testSplitDeclaration6() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package e;\n");
		buf.append("public class Test {\n");
		buf.append("    public void test() {\n");
		buf.append("        String[] test = { \"a\" };\n");
		buf.append("    }\n");
		buf.append("}");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		String str= "=";
		AssistContext context= getCorrectionContext(cu, buf.toString().indexOf(str), 0);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);

		CUCorrectionProposal proposal= (CUCorrectionProposal) proposals.get(0);
		String preview= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package e;\n");
		buf.append("public class Test {\n");
		buf.append("    public void test() {\n");
		buf.append("        String[] test;\n");
		buf.append("        test = new String[]{ \"a\" };\n");
		buf.append("    }\n");
		buf.append("}");
		assertEqualString(preview, buf.toString());
	}

	@Test
	public void testSplitDeclaration7() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package e;\n");
		buf.append("public class Test {\n");
		buf.append("    public void test() {\n");
		buf.append("        String[] test = x;\n");
		buf.append("    }\n");
		buf.append("}");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		String str= "=";
		AssistContext context= getCorrectionContext(cu, buf.toString().indexOf(str), 0);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);

		CUCorrectionProposal proposal= (CUCorrectionProposal) proposals.get(0);
		String preview= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package e;\n");
		buf.append("public class Test {\n");
		buf.append("    public void test() {\n");
		buf.append("        String[] test;\n");
		buf.append("        test = x;\n");
		buf.append("    }\n");
		buf.append("}");
		assertEqualString(preview, buf.toString());
	}

	@Test
	public void testJoinDeclaration1() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        int var[];\n");
		buf.append("        foo();\n");
		buf.append("        var = null;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		String str= "var[]";
		AssistContext context= getCorrectionContext(cu, buf.toString().indexOf(str), 0);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 2);
		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        int var[] = null;\n");
		buf.append("        foo();\n");
		buf.append("    }\n");
		buf.append("}\n");
		String ex1= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    private int vars[];\n");
		buf.append("\n");
		buf.append("    public void foo() {\n");
		buf.append("        foo();\n");
		buf.append("        vars = null;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String ex2= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { ex1, ex2 });
	}

	@Test
	public void testJoinDeclaration2() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        int var[];\n");
		buf.append("        foo();\n");
		buf.append("        var = null;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		String str= "var = ";
		AssistContext context= getCorrectionContext(cu, buf.toString().indexOf(str), 0);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 4);
		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        foo();\n");
		buf.append("        int var[] = null;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        int var[];\n");
		buf.append("        foo();\n");
		buf.append("        int[] var2 = var;\n");
		buf.append("        var = null;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected2= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        int var[];\n");
		buf.append("        foo();\n");
		buf.append("        int[] var2 = var;\n");
		buf.append("        var = null;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected3= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    private int vars[];\n");
		buf.append("\n");
		buf.append("    public void foo() {\n");
		buf.append("        foo();\n");
		buf.append("        vars = null;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected4= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1, expected2, expected3, expected4 });
	}

	@Test
	public void testJoinDeclaration3() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        int var[] = null;\n");
		buf.append("        foo();\n");
		buf.append("        var = new int[10];\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		String str= "var[]";
		AssistContext context= getCorrectionContext(cu, buf.toString().indexOf(str), 0);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 3);
		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        int var[] = new int[10];\n");
		buf.append("        foo();\n");
		buf.append("    }\n");
		buf.append("}\n");
		String ex1= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        int var[];\n");
		buf.append("        var = null;\n");
		buf.append("        foo();\n");
		buf.append("        var = new int[10];\n");
		buf.append("    }\n");
		buf.append("}\n");
		String ex2= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    private int vars[];\n");
		buf.append("\n");
		buf.append("    public void foo() {\n");
		buf.append("        vars = null;\n");
		buf.append("        foo();\n");
		buf.append("        vars = new int[10];\n");
		buf.append("    }\n");
		buf.append("}\n");
		String ex3= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { ex1, ex2, ex3 });
	}

	@Test
	public void testJoinDeclaration4() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        // 1;\n");
		buf.append("        \n");
		buf.append("        String message;\n");
		buf.append("        \n");
		buf.append("        // 2;\n");
		buf.append("        \n");
		buf.append("        message = \"\";\n");
		buf.append("        \n");
		buf.append("        // 3;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);
		
		String str= "message;";
		AssistContext context= getCorrectionContext(cu, buf.toString().indexOf(str), 0);
		List proposals= collectAssists(context, false);
		
		assertNumberOfProposals(proposals, 2);
		assertCorrectLabels(proposals);
		
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        // 1;\n");
		buf.append("        \n");
		buf.append("        String message = \"\";\n");
		buf.append("        \n");
		buf.append("        // 2;\n");
		buf.append("\n");
		buf.append("        \n");
		buf.append("        // 3;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String ex1= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    private String message;\n");
		buf.append("\n");
		buf.append("    public void foo() {\n");
		buf.append("        // 1;\n");
		buf.append("        \n");
		buf.append("        \n");
		buf.append("        \n");
		buf.append("        // 2;\n");
		buf.append("        \n");
		buf.append("        message = \"\";\n");
		buf.append("        \n");
		buf.append("        // 3;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String ex2= buf.toString();
		
		assertExpectedExistInProposals(proposals, new String[] { ex1, ex2 });
	}

	@Test
	public void testJoinDeclaration5() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        // 1;\n");
		buf.append("        \n");
		buf.append("        String message;\n");
		buf.append("        \n");
		buf.append("        // 2;\n");
		buf.append("        \n");
		buf.append("        message = \"\";\n");
		buf.append("        \n");
		buf.append("        // 3;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);
		
		String str= "message =";
		AssistContext context= getCorrectionContext(cu, buf.toString().indexOf(str), 0);
		List proposals= collectAssists(context, false);
		
		assertNumberOfProposals(proposals, 4);
		assertCorrectLabels(proposals);
		
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        // 1;\n");
		buf.append("        \n");
		buf.append("        \n");
		buf.append("        \n");
		buf.append("        // 2;\n");
		buf.append("        \n");
		buf.append("        String message = \"\";\n");
		buf.append("        \n");
		buf.append("        // 3;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String ex1= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        // 1;\n");
		buf.append("        \n");
		buf.append("        String message;\n");
		buf.append("        \n");
		buf.append("        // 2;\n");
		buf.append("        \n");
		buf.append("        String message2 = message;\n");
		buf.append("        message = \"\";\n");
		buf.append("        \n");
		buf.append("        // 3;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String ex2= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        // 1;\n");
		buf.append("        \n");
		buf.append("        String message;\n");
		buf.append("        \n");
		buf.append("        // 2;\n");
		buf.append("        \n");
		buf.append("        String message2 = message;\n");
		buf.append("        message = \"\";\n");
		buf.append("        \n");
		buf.append("        // 3;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String ex3= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    private String message;\n");
		buf.append("\n");
		buf.append("    public void foo() {\n");
		buf.append("        // 1;\n");
		buf.append("        \n");
		buf.append("        \n");
		buf.append("        \n");
		buf.append("        // 2;\n");
		buf.append("        \n");
		buf.append("        message = \"\";\n");
		buf.append("        \n");
		buf.append("        // 3;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String ex4= buf.toString();
		
		assertExpectedExistInProposals(proposals, new String[] { ex1, ex2, ex3, ex4 });
	}
	
	private static final Class[] FILTER_EQ= {LinkedNamesAssistProposal.class, /* RenameRefactoringProposal.class,*/
											 AssignToVariableAssistProposal.class};

	@Test
	public void testInvertEquals1() throws Exception {
		IPackageFragment pack1 = fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        \"a\".equals(\"b\");\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu = pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		String str = "equals";
		AssistContext context = getCorrectionContext(cu, buf.toString().indexOf(str), 0);
		List proposals = collectAssists(context, FILTER_EQ);

		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);

		CUCorrectionProposal proposal = (CUCorrectionProposal)proposals.get(0);
		String preview = getPreviewContent(proposal);

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        \"b\".equals(\"a\");\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

		cu = pack1.createCompilationUnit("E.java", buf.toString(), true, null);
		context = getCorrectionContext(cu, buf.toString().indexOf(str), 0);
		proposals = collectAssists(context, FILTER_EQ);

		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);

		proposal = (CUCorrectionProposal)proposals.get(0);
		preview = getPreviewContent(proposal);

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        \"a\".equals(\"b\");\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

	@Test
	public void testInvertEquals2() throws Exception {
		IPackageFragment pack1 = fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        String s= \"a\";\n");
		buf.append("        s.equals(\"a\");\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu = pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		String str = "s.equals(\"a\")";
		AssistContext context = getCorrectionContext(cu, buf.toString().indexOf(str), str.length());
		List proposals = collectAssists(context, FILTER_EQ);

		assertNumberOfProposals(proposals, 4);
		assertCorrectLabels(proposals);

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        String s= \"a\";\n");
		buf.append("        \"a\".equals(s);\n");
		buf.append("    }\n");
		buf.append("}\n");
		String ex1 = buf.toString();

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        String s= \"a\";\n");
		buf.append("        boolean equals = s.equals(\"a\");\n");
		buf.append("    }\n");
		buf.append("}\n");
		String ex2 = buf.toString();

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        String s= \"a\";\n");
		buf.append("        boolean equals = s.equals(\"a\");\n");
		buf.append("    }\n");
		buf.append("}\n");
		String ex3 = buf.toString();

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        String s= \"a\";\n");
		buf.append("        extracted(s);\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("    private boolean extracted(String s) {\n");
		buf.append("        return s.equals(\"a\");\n");
		buf.append("    }\n");
		buf.append("}\n");
		String ex4 = buf.toString();

		assertExpectedExistInProposals(proposals, new String[]{ex1, ex2, ex3, ex4});

		cu = pack1.createCompilationUnit("E.java", ex1, true, null);
		str = "\"a\".equals(s)";
		context = getCorrectionContext(cu, ex1.indexOf(str), 0);
		proposals = collectAssists(context, FILTER_EQ);

		assertNumberOfProposals(proposals, 4);
		assertCorrectLabels(proposals);

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        String s= \"a\";\n");
		buf.append("        s.equals(\"a\");\n");
		buf.append("    }\n");
		buf.append("}\n");
		ex1 = buf.toString();

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        String string = \"a\";\n");
		buf.append("        String s= string;\n");
		buf.append("        string.equals(s);\n");
		buf.append("    }\n");
		buf.append("}\n");
		ex2 = buf.toString();

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        String s= \"a\";\n");
		buf.append("        String string = \"a\";\n");
		buf.append("        string.equals(s);\n");
		buf.append("    }\n");
		buf.append("}\n");
		ex3 = buf.toString();

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    private static final String A = \"a\";\n");
		buf.append("\n");
		buf.append("    public void foo() {\n");
		buf.append("        String s= A;\n");
		buf.append("        A.equals(s);\n");
		buf.append("    }\n");
		buf.append("}\n");
		ex4 = buf.toString();

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        String s= \"a\";\n");
		buf.append("        \"A\".equals(s);\n");
		buf.append("    }\n");
		buf.append("}\n");
		String ex5 = buf.toString();

		assertExpectedExistInProposals(proposals, new String[]{ex1, ex2, ex3, ex4/*, ex5*/});
	}

	@Test
	public void testInvertEquals3() throws Exception {
		IPackageFragment pack1 = fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    private String a= \"a\";\n");
		buf.append("    private String b= \"b\";\n");
		buf.append("    public void foo() {\n");
		buf.append("        a.equals(b);\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu = pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		String str = "equals";
		AssistContext context = getCorrectionContext(cu, buf.toString().indexOf(str), str.length());
		List proposals = collectAssists(context, FILTER_EQ);

		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);

		CUCorrectionProposal proposal = (CUCorrectionProposal)proposals.get(0);
		String preview = getPreviewContent(proposal);

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    private String a= \"a\";\n");
		buf.append("    private String b= \"b\";\n");
		buf.append("    public void foo() {\n");
		buf.append("        b.equals(a);\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

		cu = pack1.createCompilationUnit("E.java", buf.toString(), true, null);
		context = getCorrectionContext(cu, buf.toString().indexOf(str), str.length());
		proposals = collectAssists(context, FILTER_EQ);

		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);

		proposal = (CUCorrectionProposal)proposals.get(0);
		preview = getPreviewContent(proposal);

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    private String a= \"a\";\n");
		buf.append("    private String b= \"b\";\n");
		buf.append("    public void foo() {\n");
		buf.append("        a.equals(b);\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

	@Test
	public void testInvertEquals4() throws Exception {
		IPackageFragment pack1 = fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("class S {\n");
		buf.append("    protected String sup= \"a\";\n");
		buf.append("}\n");
		buf.append("public class E extends S {\n");
		buf.append("    private String a= \"a\";\n");
		buf.append("    public void foo() {\n");
		buf.append("        sup.equals(this.a);\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu = pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		String str = "equals";
		AssistContext context = getCorrectionContext(cu, buf.toString().indexOf(str), 0);
		List proposals = collectAssists(context, FILTER_EQ);

		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);

		CUCorrectionProposal proposal = (CUCorrectionProposal)proposals.get(0);
		String preview = getPreviewContent(proposal);

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("class S {\n");
		buf.append("    protected String sup= \"a\";\n");
		buf.append("}\n");
		buf.append("public class E extends S {\n");
		buf.append("    private String a= \"a\";\n");
		buf.append("    public void foo() {\n");
		buf.append("        this.a.equals(sup);\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

		cu = pack1.createCompilationUnit("E.java", buf.toString(), true, null);
		context = getCorrectionContext(cu, buf.toString().indexOf(str), 0);
		proposals = collectAssists(context, FILTER_EQ);

		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);

		proposal = (CUCorrectionProposal)proposals.get(0);
		preview = getPreviewContent(proposal);

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("class S {\n");
		buf.append("    protected String sup= \"a\";\n");
		buf.append("}\n");
		buf.append("public class E extends S {\n");
		buf.append("    private String a= \"a\";\n");
		buf.append("    public void foo() {\n");
		buf.append("        sup.equals(this.a);\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

	@Test
	public void testInvertEquals5() throws Exception {
		IPackageFragment pack1 = fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("class A {\n");
		buf.append("    static String A= \"a\";\n");
		buf.append("}\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        \"a\".equals(A.A);\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu = pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		String str = "equals";
		AssistContext context = getCorrectionContext(cu, buf.toString().indexOf(str), 0);
		List proposals = collectAssists(context, FILTER_EQ);

		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);

		CUCorrectionProposal proposal = (CUCorrectionProposal)proposals.get(0);
		String preview = getPreviewContent(proposal);

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("class A {\n");
		buf.append("    static String A= \"a\";\n");
		buf.append("}\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        A.A.equals(\"a\");\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

		cu = pack1.createCompilationUnit("E.java", buf.toString(), true, null);
		context = getCorrectionContext(cu, buf.toString().indexOf(str), 0);
		proposals = collectAssists(context, FILTER_EQ);

		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);

		proposal = (CUCorrectionProposal)proposals.get(0);
		preview = getPreviewContent(proposal);

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("class A {\n");
		buf.append("    static String A= \"a\";\n");
		buf.append("}\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        \"a\".equals(A.A);\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

	@Test
	public void testInvertEquals6() throws Exception {
		IPackageFragment pack1 = fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("class A {\n");
		buf.append("    static String get() {\n");
		buf.append("        return \"a\";\n");
		buf.append("    }\n");
		buf.append("}\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        \"a\".equals(A.get());\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu = pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		String str = "equals";
		AssistContext context = getCorrectionContext(cu, buf.toString().indexOf(str), 0);
		List proposals = collectAssists(context, FILTER_EQ);

		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);

		cu = pack1.createCompilationUnit("E.java", buf.toString(), true, null);
		CUCorrectionProposal proposal = (CUCorrectionProposal)proposals.get(0);
		String preview = getPreviewContent(proposal);

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("class A {\n");
		buf.append("    static String get() {\n");
		buf.append("        return \"a\";\n");
		buf.append("    }\n");
		buf.append("}\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        A.get().equals(\"a\");\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

		cu = pack1.createCompilationUnit("E.java", buf.toString(), true, null);
		context = getCorrectionContext(cu, buf.toString().indexOf(str), 0);
		proposals = collectAssists(context, FILTER_EQ);

		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);

		proposal = (CUCorrectionProposal)proposals.get(0);
		preview = getPreviewContent(proposal);

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("class A {\n");
		buf.append("    static String get() {\n");
		buf.append("        return \"a\";\n");
		buf.append("    }\n");
		buf.append("}\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        \"a\".equals(A.get());\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

	@Test
	public void testInvertEquals7() throws Exception {
		IPackageFragment pack1 = fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        \"a\".getClass().equals(String.class);\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu = pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		String str = "equals";
		AssistContext context = getCorrectionContext(cu, buf.toString().indexOf(str), 0);
		List proposals = collectAssists(context, FILTER_EQ);

		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);

		CUCorrectionProposal proposal = (CUCorrectionProposal)proposals.get(0);
		String preview = getPreviewContent(proposal);

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        String.class.equals(\"a\".getClass());\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

		cu = pack1.createCompilationUnit("E.java", buf.toString(), true, null);
		context = getCorrectionContext(cu, buf.toString().indexOf(str), 0);
		proposals = collectAssists(context, FILTER_EQ);

		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);

		proposal = (CUCorrectionProposal)proposals.get(0);
		preview = getPreviewContent(proposal);

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        \"a\".getClass().equals(String.class);\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

	@Test
	public void testInvertEquals8() throws Exception {
		IPackageFragment pack1 = fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        boolean x = false && \"a\".equals(get());\n");
		buf.append("    }\n");
		buf.append("    String get() {\n");
		buf.append("        return \"a\";\n");
		buf.append("    }\n");
		buf.append("}\n");

		ICompilationUnit cu = pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		String str = "equals";
		AssistContext context = getCorrectionContext(cu, buf.toString().indexOf(str), 0);
		List proposals = collectAssists(context, FILTER_EQ);

		assertNumberOfProposals(proposals, 2);
		assertCorrectLabels(proposals);

		CUCorrectionProposal proposal = (CUCorrectionProposal)proposals.get(0);
		String preview = getPreviewContent(proposal);

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        boolean x = false && get().equals(\"a\");\n");
		buf.append("    }\n");
		buf.append("    String get() {\n");
		buf.append("        return \"a\";\n");
		buf.append("    }\n");
		buf.append("}\n");
		String ex1 = buf.toString();
		assertEqualString(preview, ex1);

		proposal = (CUCorrectionProposal)proposals.get(1);
		preview = getPreviewContent(proposal);

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        boolean x = (false && \"a\".equals(get()));\n");
		buf.append("    }\n");
		buf.append("    String get() {\n");
		buf.append("        return \"a\";\n");
		buf.append("    }\n");
		buf.append("}\n");
		String ex2 = buf.toString();
		assertEqualString(preview, ex2);

		cu = pack1.createCompilationUnit("E.java", ex1, true, null);
		context = getCorrectionContext(cu, ex1.indexOf(str), 0);
		proposals = collectAssists(context, FILTER_EQ);

		assertNumberOfProposals(proposals, 2);
		assertCorrectLabels(proposals);

		proposal = (CUCorrectionProposal)proposals.get(0);
		preview = getPreviewContent(proposal);

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        boolean x = false && \"a\".equals(get());\n");
		buf.append("    }\n");
		buf.append("    String get() {\n");
		buf.append("        return \"a\";\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

		proposal = (CUCorrectionProposal)proposals.get(1);
		preview = getPreviewContent(proposal);

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        boolean x = (false && get().equals(\"a\"));\n");
		buf.append("    }\n");
		buf.append("    String get() {\n");
		buf.append("        return \"a\";\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

	@Test
	public void testInvertEquals9() throws Exception {
		IPackageFragment pack1 = fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        equals(new E());\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu = pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		String str = "equals";
		AssistContext context = getCorrectionContext(cu, buf.toString().indexOf(str), 0);
		List proposals = collectAssists(context, FILTER_EQ);

		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);

		CUCorrectionProposal proposal = (CUCorrectionProposal)proposals.get(0);
		String preview = getPreviewContent(proposal);

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        new E().equals(this);\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

		cu = pack1.createCompilationUnit("E.java", buf.toString(), true, null);
		context = getCorrectionContext(cu, buf.toString().indexOf(str), 0);
		proposals = collectAssists(context, FILTER_EQ);

		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);

		proposal = (CUCorrectionProposal)proposals.get(0);
		preview = getPreviewContent(proposal);

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        equals(new E());\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

	@Test
	public void testInvertEquals10() throws Exception {
		IPackageFragment pack1 = fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        \"a\".equals(null);\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu = pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		String str = "equals";
		AssistContext context = getCorrectionContext(cu, buf.toString().indexOf(str), 0);
		List proposals = collectAssists(context, FILTER_EQ);

		assertNumberOfProposals(proposals, 0);
		assertCorrectLabels(proposals);
	}

	@Test
	public void testInvertEquals11() throws Exception {
		IPackageFragment pack1 = fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    boolean equals(Object o, boolean a) {\n");
		buf.append("        return false;\n");
		buf.append("    }\n");
		buf.append("    public void foo() {\n");
		buf.append("        new E().equals(\"a\", false);\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu = pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		String str = "E().equals";
		AssistContext context = getCorrectionContext(cu, buf.toString().indexOf(str) + str.length(), 0);
		List proposals = collectAssists(context, FILTER_EQ);

		assertNumberOfProposals(proposals, 0);
		assertCorrectLabels(proposals);

		context = getCorrectionContext(cu, buf.toString().lastIndexOf(str), 0);
		proposals = collectAssists(context, FILTER_EQ);

		assertNumberOfProposals(proposals, 0);
		assertCorrectLabels(proposals);
	}

	@Test
	public void testInvertEquals12() throws Exception {
		IPackageFragment pack1 = fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    boolean equals(boolean b) {\n");
		buf.append("        return false;\n");
		buf.append("    }\n");
		buf.append("    public void foo() {\n");
		buf.append("        new E().equals(false);\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu = pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		String str = "E().equals";
		AssistContext context = getCorrectionContext(cu, buf.toString().indexOf(str) + str.length(), 0);
		List proposals = collectAssists(context, FILTER_EQ);

		assertNumberOfProposals(proposals, 0);
		assertCorrectLabels(proposals);

		context = getCorrectionContext(cu, buf.toString().lastIndexOf(str), 0);
		proposals = collectAssists(context, FILTER_EQ);

		assertNumberOfProposals(proposals, 0);
		assertCorrectLabels(proposals);
	}

	@Test
	public void testInvertEquals13() throws Exception {
		IPackageFragment pack1 = fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    boolean equals(boolean b) {\n");
		buf.append("        return false;\n");
		buf.append("    }\n");
		buf.append("    public void foo() {\n");
		buf.append("        new E().equals(true ? true : false);\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu = pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		String str = "E().equals";
		AssistContext context = getCorrectionContext(cu, buf.toString().indexOf(str) + str.length(), 0);
		List proposals = collectAssists(context, FILTER_EQ);

		assertNumberOfProposals(proposals, 0);
		assertCorrectLabels(proposals);

		context = getCorrectionContext(cu, buf.toString().lastIndexOf(str), 0);
		proposals = collectAssists(context, FILTER_EQ);

		assertNumberOfProposals(proposals, 0);
		assertCorrectLabels(proposals);
	}

	@Test
	public void testInvertEquals14() throws Exception {
		IPackageFragment pack1 = fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("class Super {\n");
		buf.append("    protected boolean sBool= false;\n");
		buf.append("}\n");
		buf.append("public class E extends Super {\n");
		buf.append("    boolean equals(boolean b) {\n");
		buf.append("        return false;\n");
		buf.append("    }\n");
		buf.append("    public void foo() {\n");
		buf.append("        new E().equals(sBool);\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu = pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		String str = "E().equals";
		AssistContext context = getCorrectionContext(cu, buf.toString().indexOf(str) + str.length(), 0);
		List proposals = collectAssists(context, FILTER_EQ);

		assertNumberOfProposals(proposals, 0);
		assertCorrectLabels(proposals);

		context = getCorrectionContext(cu, buf.toString().lastIndexOf(str), 0);
		proposals = collectAssists(context, FILTER_EQ);

		assertNumberOfProposals(proposals, 0);
		assertCorrectLabels(proposals);
	}

	@Test
	public void testInvertEquals15() throws Exception {
		IPackageFragment pack1 = fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    boolean equals(int i) {\n");
		buf.append("        return false;\n");
		buf.append("    }\n");
		buf.append("    public void foo() {\n");
		buf.append("        new E().equals(1 + 1);\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu = pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		String str = "E().equals";
		AssistContext context = getCorrectionContext(cu, buf.toString().indexOf(str) + str.length(), 0);
		List proposals = collectAssists(context, FILTER_EQ);

		assertNumberOfProposals(proposals, 0);
		assertCorrectLabels(proposals);

		context = getCorrectionContext(cu, buf.toString().lastIndexOf(str), 0);
		proposals = collectAssists(context, FILTER_EQ);

		assertNumberOfProposals(proposals, 0);
		assertCorrectLabels(proposals);
	}

	@Test
	public void testInvertEquals16() throws Exception {
		IPackageFragment pack1 = fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    boolean equals(int i) {\n");
		buf.append("        return false;\n");
		buf.append("    }\n");
		buf.append("    public void foo() {\n");
		buf.append("        int i= 1;\n");
		buf.append("        new E().equals(i + i);\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu = pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		String str = "E().equals";
		AssistContext context = getCorrectionContext(cu, buf.toString().indexOf(str) + str.length(), 0);
		List proposals = collectAssists(context, FILTER_EQ);

		assertNumberOfProposals(proposals, 0);
		assertCorrectLabels(proposals);

		context = getCorrectionContext(cu, buf.toString().lastIndexOf(str), 0);
		proposals = collectAssists(context, FILTER_EQ);

		assertNumberOfProposals(proposals, 0);
		assertCorrectLabels(proposals);
	}

	@Test
	public void testInvertEquals17() throws Exception {
		IPackageFragment pack1 = fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("       \"a\".equals(null);\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu = pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		String str = "equals";
		AssistContext context = getCorrectionContext(cu, buf.toString().indexOf(str), 0);
		List proposals = collectAssists(context, FILTER_EQ);

		assertNumberOfProposals(proposals, 0);
		assertCorrectLabels(proposals);
	}

	@Test
	public void testInvertEquals18() throws Exception {
		IPackageFragment pack1 = fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public boolean equals(Object o) {\n");
		buf.append("       return super.equals(o);\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu = pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		String str = "equals(o)";
		AssistContext context = getCorrectionContext(cu, buf.toString().indexOf(str), 0);
		List proposals = collectAssists(context, FILTER_EQ);

		assertNumberOfProposals(proposals, 0);
		assertCorrectLabels(proposals);

		context = getCorrectionContext(cu, buf.toString().lastIndexOf(str), 0);
		proposals = collectAssists(context, FILTER_EQ);

		assertNumberOfProposals(proposals, 0);
		assertCorrectLabels(proposals);
	}

	@Test
	public void testInvertEquals19() throws Exception {
		IPackageFragment pack1 = fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    private String a= \"a\";\n");
		buf.append("    public void foo() {\n");
		buf.append("        a.equals((Object) \"a\");\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu = pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		String str = "equals";
		AssistContext context = getCorrectionContext(cu, buf.toString().indexOf(str), 0);
		List proposals = collectAssists(context, FILTER_EQ);

		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);

		CUCorrectionProposal proposal = (CUCorrectionProposal)proposals.get(0);
		String preview = getPreviewContent(proposal);

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    private String a= \"a\";\n");
		buf.append("    public void foo() {\n");
		buf.append("        ((Object) \"a\").equals(a);\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

		cu = pack1.createCompilationUnit("E.java", buf.toString(), true, null);
		context = getCorrectionContext(cu, buf.toString().indexOf(str), 0);
		proposals = collectAssists(context, FILTER_EQ);

		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);

		proposal = (CUCorrectionProposal)proposals.get(0);
		preview = getPreviewContent(proposal);

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    private String a= \"a\";\n");
		buf.append("    public void foo() {\n");
		buf.append("        a.equals((Object) \"a\");\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

	@Test
	public void testInvertEquals20() throws Exception {
		IPackageFragment pack1 = fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        String s= null;\n");
		buf.append("        \"a\".equals(s = \"a\");\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu = pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		String str = "equals";
		AssistContext context = getCorrectionContext(cu, buf.toString().indexOf(str), 0);
		List proposals = collectAssists(context, FILTER_EQ);

		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);

		CUCorrectionProposal proposal = (CUCorrectionProposal)proposals.get(0);
		String preview = getPreviewContent(proposal);

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        String s= null;\n");
		buf.append("        (s = \"a\").equals(\"a\");\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

		cu = pack1.createCompilationUnit("E.java", buf.toString(), true, null);
		context = getCorrectionContext(cu, buf.toString().indexOf(str), 0);
		proposals = collectAssists(context, FILTER_EQ);

		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);

		proposal = (CUCorrectionProposal)proposals.get(0);
		preview = getPreviewContent(proposal);

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        String s= null;\n");
		buf.append("        \"a\".equals(s = \"a\");\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

	@Test
	public void testInvertEquals21() throws Exception {
		IPackageFragment pack1 = fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        \"aaa\".equals(\"a\" + \"a\" + \"a\");\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu = pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		String str = "equals";
		AssistContext context = getCorrectionContext(cu, buf.toString().indexOf(str), 0);
		List proposals = collectAssists(context, FILTER_EQ);

		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);

		CUCorrectionProposal proposal = (CUCorrectionProposal)proposals.get(0);
		String preview = getPreviewContent(proposal);

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        (\"a\" + \"a\" + \"a\").equals(\"aaa\");\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

		cu = pack1.createCompilationUnit("E.java", buf.toString(), true, null);
		context = getCorrectionContext(cu, buf.toString().indexOf(str), 0);
		proposals = collectAssists(context, FILTER_EQ);

		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);

		proposal = (CUCorrectionProposal)proposals.get(0);
		preview = getPreviewContent(proposal);

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        \"aaa\".equals(\"a\" + \"a\" + \"a\");\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

	@Test
	public void testInvertEquals22() throws Exception {
		IPackageFragment pack1 = fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        \"a\".equals(true ? \"a\" : \"b\");\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu = pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		String str = "equals";
		AssistContext context = getCorrectionContext(cu, buf.toString().indexOf(str), 0);
		List proposals = collectAssists(context, FILTER_EQ);

		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);

		CUCorrectionProposal proposal = (CUCorrectionProposal)proposals.get(0);
		String preview = getPreviewContent(proposal);

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        (true ? \"a\" : \"b\").equals(\"a\");\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

		cu = pack1.createCompilationUnit("E.java", buf.toString(), true, null);
		context = getCorrectionContext(cu, buf.toString().indexOf(str), 0);
		proposals = collectAssists(context, FILTER_EQ);

		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);

		proposal = (CUCorrectionProposal)proposals.get(0);
		preview = getPreviewContent(proposal);

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        \"a\".equals(true ? \"a\" : \"b\");\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

	@Test
	public void testInvertEquals23() throws Exception {
		IPackageFragment pack1 = fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        \"a\".equals((\"a\"));\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu = pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		String str = "equals";
		AssistContext context = getCorrectionContext(cu, buf.toString().indexOf(str), 0);
		List proposals = collectAssists(context, FILTER_EQ);

		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);

		CUCorrectionProposal proposal = (CUCorrectionProposal)proposals.get(0);
		String preview = getPreviewContent(proposal);

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        (\"a\").equals(\"a\");\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

		cu = pack1.createCompilationUnit("E.java", buf.toString(), true, null);
		context = getCorrectionContext(cu, buf.toString().indexOf(str), 0);
		proposals = collectAssists(context, FILTER_EQ);

		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);

		proposal = (CUCorrectionProposal)proposals.get(0);
		preview = getPreviewContent(proposal);

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        \"a\".equals(\"a\");\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

	@Test
	public void testInvertEquals24() throws Exception {
		//https://bugs.eclipse.org/bugs/show_bug.cgi?id=385389
		IPackageFragment pack1 = fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(Enum e) {\n");
		buf.append("        e.equals(Enum.e1);\n");
		buf.append("    }\n");
		buf.append("}\n");
		buf.append("enum Enum {\n");
		buf.append("    e1, e2;\n");
		buf.append("}\n");
		ICompilationUnit cu = pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		String str = "equals";
		AssistContext context = getCorrectionContext(cu, buf.toString().indexOf(str), 0);
		List proposals = collectAssists(context, FILTER_EQ);

		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);

		CUCorrectionProposal proposal = (CUCorrectionProposal)proposals.get(0);
		String preview = getPreviewContent(proposal);

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(Enum e) {\n");
		buf.append("        Enum.e1.equals(e);\n");
		buf.append("    }\n");
		buf.append("}\n");
		buf.append("enum Enum {\n");
		buf.append("    e1, e2;\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

		cu = pack1.createCompilationUnit("E.java", buf.toString(), true, null);
		context = getCorrectionContext(cu, buf.toString().indexOf(str), 0);
		proposals = collectAssists(context, FILTER_EQ);

		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);

		proposal = (CUCorrectionProposal)proposals.get(0);
		preview = getPreviewContent(proposal);

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(Enum e) {\n");
		buf.append("        e.equals(Enum.e1);\n");
		buf.append("    }\n");
		buf.append("}\n");
		buf.append("enum Enum {\n");
		buf.append("    e1, e2;\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

	@Test
	public void testAddTypeToArrayInitializer() throws Exception {

		IPackageFragment pack1 = fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        int[][] numbers= {{ 1, 2 }, { 3, 4 }, { 4, 5 }};\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu = pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		String str = "{{";
		AssistContext context = getCorrectionContext(cu, buf.toString().indexOf(str) + str.length(), 0);
		List proposals = collectAssists(context, false);

		assertNumberOfProposals(proposals, 2);
		assertCorrectLabels(proposals);

		ArrayList previews = new ArrayList();
		ArrayList expecteds = new ArrayList();

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        int[][] numbers= new int[][]{{ 1, 2 }, { 3, 4 }, { 4, 5 }};\n");
		buf.append("    }\n");
		buf.append("}\n");
		addPreviewAndExpected(proposals, buf, expecteds, previews);

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    private static final int[] INTS = { 1, 2 };\n");
		buf.append("\n");
		buf.append("    public void foo() {\n");
		buf.append("        int[][] numbers= {INTS, { 3, 4 }, { 4, 5 }};\n");
		buf.append("    }\n");
		buf.append("}\n");
		addPreviewAndExpected(proposals, buf, expecteds, previews);

		assertEqualStringsIgnoreOrder(previews, expecteds);
	}

	@Test
	public void testCreateInSuper() throws Exception {

		IPackageFragment pack1 = fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("}\n");
		pack1.createCompilationUnit("A.java", buf.toString(), false, null);

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public interface IB {\n");
		buf.append("}\n");
		pack1.createCompilationUnit("IB.java", buf.toString(), false, null);

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.io.IOException;\n");
		buf.append("import java.util.Vector;\n");
		buf.append("public class E extends A implements IB {\n");
		buf.append("    public Vector foo(int count) throws IOException {\n");
		buf.append("        return null;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu = pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		String str = "foo";
		AssistContext context = getCorrectionContext(cu, buf.toString().indexOf(str) + str.length(), 0);
		List proposals = collectAssists(context, false);

		assertNumberOfProposals(proposals, 2);
		assertCorrectLabels(proposals);

		CUCorrectionProposal proposal = (CUCorrectionProposal)proposals.get(0);
		String preview1 = getPreviewContent(proposal);

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("\n");
		buf.append("import java.io.IOException;\n");
		buf.append("import java.util.Vector;\n");
		buf.append("\n");
		buf.append("public interface IB {\n");
		buf.append("\n");
		buf.append("    Vector foo(int count) throws IOException;\n");
		buf.append("}\n");
		String expected1 = buf.toString();

		proposal = (CUCorrectionProposal)proposals.get(1);
		String preview2 = getPreviewContent(proposal);

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("\n");
		buf.append("import java.io.IOException;\n");
		buf.append("import java.util.Vector;\n");
		buf.append("\n");
		buf.append("public class A {\n");
		buf.append("\n");
		buf.append("    public Vector foo(int count) throws IOException {\n");
		buf.append("        //TODO\n");
		buf.append("        return null;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected2 = buf.toString();

		assertEqualStringsIgnoreOrder(new String[]{preview1, preview2}, new String[]{expected1, expected2});

	}

	@Test
	public void testCreateInSuperInGeneric() throws Exception {

		IPackageFragment pack1 = fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A<T> {\n");
		buf.append("}\n");
		pack1.createCompilationUnit("A.java", buf.toString(), false, null);

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public interface IB<T> {\n");
		buf.append("}\n");
		pack1.createCompilationUnit("IB.java", buf.toString(), false, null);

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.io.IOException;\n");
		buf.append("import java.util.Vector;\n");
		buf.append("public class E extends A<String> implements IB<String> {\n");
		buf.append("    public Vector<String> foo(int count) throws IOException {\n");
		buf.append("        return null;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu = pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		String str = "foo";
		AssistContext context = getCorrectionContext(cu, buf.toString().indexOf(str) + str.length(), 0);
		List proposals = collectAssists(context, false);

		assertNumberOfProposals(proposals, 2);
		assertCorrectLabels(proposals);

		CUCorrectionProposal proposal = (CUCorrectionProposal)proposals.get(0);
		String preview1 = getPreviewContent(proposal);

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("\n");
		buf.append("import java.io.IOException;\n");
		buf.append("import java.util.Vector;\n");
		buf.append("\n");
		buf.append("public interface IB<T> {\n");
		buf.append("\n");
		buf.append("    Vector<String> foo(int count) throws IOException;\n");
		buf.append("}\n");
		String expected1 = buf.toString();

		proposal = (CUCorrectionProposal)proposals.get(1);
		String preview2 = getPreviewContent(proposal);

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("\n");
		buf.append("import java.io.IOException;\n");
		buf.append("import java.util.Vector;\n");
		buf.append("\n");
		buf.append("public class A<T> {\n");
		buf.append("\n");
		buf.append("    public Vector<String> foo(int count) throws IOException {\n");
		buf.append("        //TODO\n");
		buf.append("        return null;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected2 = buf.toString();

		assertEqualStringsIgnoreOrder(new String[]{preview1, preview2}, new String[]{expected1, expected2});

	}

	@Test
	public void testChangeIfStatementToBlock() throws Exception {

		IPackageFragment pack1 = fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        if (true) \n");
		buf.append("            ;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu = pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		String str = "if (";
		AssistContext context = getCorrectionContext(cu, buf.toString().indexOf(str), 0);
		List proposals = collectAssists(context, false);

		assertNumberOfProposals(proposals, 4);
		assertCorrectLabels(proposals);

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        if (true) {\n");
		buf.append("            ;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1 = buf.toString();

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        if (true) \n");
		buf.append("            ;\n");
		buf.append("        else {\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected2 = buf.toString();

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        ;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected3 = buf.toString();

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        if (false)\n");
		buf.append("            return;\n");
		buf.append("        ;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected4 = buf.toString();

		assertExpectedExistInProposals(proposals, new String[]{expected1, expected2, expected3, expected4});
	}

	@Test
	public void testChangeElseStatementToBlock() throws Exception {

		IPackageFragment pack1 = fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        if (true) {\n");
		buf.append("            ;;\n");
		buf.append("        } else\n");
		buf.append("            ;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu = pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		String str = "else";
		AssistContext context = getCorrectionContext(cu, buf.toString().indexOf(str), 0);
		List proposals = collectAssists(context, false);

		assertNumberOfProposals(proposals, 2);
		assertCorrectLabels(proposals);

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        if (true) {\n");
		buf.append("            ;;\n");
		buf.append("        } else {\n");
		buf.append("            ;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1 = buf.toString();

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        if (false)\n");
		buf.append("            ;\n");
		buf.append("        else {\n");
		buf.append("            ;;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected2 = buf.toString();

		assertExpectedExistInProposals(proposals, new String[]{expected1, expected2});
	}

	@Test
	public void testChangeIfWithElseStatementToBlock() throws Exception {

		IPackageFragment pack1 = fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        if (true)\n");
		buf.append("            ;\n");
		buf.append("        else {\n");
		buf.append("            ;;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu = pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		String str = "if (";
		AssistContext context = getCorrectionContext(cu, buf.toString().indexOf(str), 0);
		List proposals = collectAssists(context, false);

		assertNumberOfProposals(proposals, 2);
		assertCorrectLabels(proposals);

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        if (true) {\n");
		buf.append("            ;\n");
		buf.append("        } else {\n");
		buf.append("            ;;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1 = buf.toString();

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        if (false) {\n");
		buf.append("            ;;\n");
		buf.append("        } else\n");
		buf.append("            ;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected2 = buf.toString();

		assertExpectedExistInProposals(proposals, new String[]{expected1, expected2});
	}

	@Test
	public void testChangeIfAndElseStatementToBlock1() throws Exception {

		IPackageFragment pack1 = fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        if (true)\n");
		buf.append("            ;\n");
		buf.append("        else\n");
		buf.append("            ;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu = pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		String str = "if (";
		AssistContext context = getCorrectionContext(cu, buf.toString().indexOf(str), 0);
		List proposals = collectAssists(context, false);

		assertNumberOfProposals(proposals, 3);
		assertCorrectLabels(proposals);

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        if (true) {\n");
		buf.append("            ;\n");
		buf.append("        } else\n");
		buf.append("            ;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1 = buf.toString();

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        if (true) {\n");
		buf.append("            ;\n");
		buf.append("        } else {\n");
		buf.append("            ;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected2 = buf.toString();

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        if (false)\n");
		buf.append("            ;\n");
		buf.append("        else\n");
		buf.append("            ;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected3 = buf.toString();

		assertExpectedExistInProposals(proposals, new String[]{expected1, expected2, expected3});
	}

	@Test
	public void testChangeIfAndElseStatementToBlock2() throws Exception {

		IPackageFragment pack1 = fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        if (true)\n");
		buf.append("            ;\n");
		buf.append("        else\n");
		buf.append("            ;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu = pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		String str = "else";
		AssistContext context = getCorrectionContext(cu, buf.toString().indexOf(str), 0);
		List proposals = collectAssists(context, false);

		assertNumberOfProposals(proposals, 3);
		assertCorrectLabels(proposals);

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        if (true)\n");
		buf.append("            ;\n");
		buf.append("        else {\n");
		buf.append("            ;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1 = buf.toString();

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        if (true) {\n");
		buf.append("            ;\n");
		buf.append("        } else {\n");
		buf.append("            ;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected2 = buf.toString();

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        if (false)\n");
		buf.append("            ;\n");
		buf.append("        else\n");
		buf.append("            ;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected3 = buf.toString();

		assertExpectedExistInProposals(proposals, new String[]{expected1, expected2, expected3});
	}

	@Test
	public void testChangeIfAndElseIfStatementToBlock() throws Exception {

		IPackageFragment pack1 = fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        if (true)\n");
		buf.append("            ;\n");
		buf.append("        else if (true)\n");
		buf.append("            ;\n");
		buf.append("        else if (false)\n");
		buf.append("            ;\n");
		buf.append("        else\n");
		buf.append("            ;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu = pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		String str = "else if (";
		AssistContext context = getCorrectionContext(cu, buf.toString().indexOf(str), 0);
		List proposals = collectAssists(context, false);

		assertNumberOfProposals(proposals, 3);
		assertCorrectLabels(proposals);

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        if (true)\n");
		buf.append("            ;\n");
		buf.append("        else {\n");
		buf.append("            if (true)\n");
		buf.append("                ;\n");
		buf.append("            else if (false)\n");
		buf.append("                ;\n");
		buf.append("            else\n");
		buf.append("                ;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1 = buf.toString();

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        if (true) {\n");
		buf.append("            ;\n");
		buf.append("        } else if (true) {\n");
		buf.append("            ;\n");
		buf.append("        } else if (false) {\n");
		buf.append("            ;\n");
		buf.append("        } else {\n");
		buf.append("            ;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected2 = buf.toString();

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        if (false) {\n");
		buf.append("            if (true)\n");
		buf.append("                ;\n");
		buf.append("            else if (false)\n");
		buf.append("                ;\n");
		buf.append("            else\n");
		buf.append("                ;\n");
		buf.append("        } else\n");
		buf.append("            ;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected3 = buf.toString();

		assertExpectedExistInProposals(proposals, new String[]{expected1, expected2, expected3});
	}

	@Test
	public void testChangeIfAndElseIfStatementWithBlockToBlock() throws Exception {

		IPackageFragment pack1 = fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        if (true)\n");
		buf.append("            ;\n");
		buf.append("        else if (true) {\n");
		buf.append("            ;\n");
		buf.append("        } else if (false)\n");
		buf.append("            ;\n");
		buf.append("        else\n");
		buf.append("            ;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu = pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		String str = "else if (";
		AssistContext context = getCorrectionContext(cu, buf.toString().indexOf(str), 0);
		List proposals = collectAssists(context, false);

		assertNumberOfProposals(proposals, 3);
		assertCorrectLabels(proposals);

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        if (true)\n");
		buf.append("            ;\n");
		buf.append("        else {\n");
		buf.append("            if (true) {\n");
		buf.append("                ;\n");
		buf.append("            } else if (false)\n");
		buf.append("                ;\n");
		buf.append("            else\n");
		buf.append("                ;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1 = buf.toString();

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        if (true) {\n");
		buf.append("            ;\n");
		buf.append("        } else if (true) {\n");
		buf.append("            ;\n");
		buf.append("        } else if (false) {\n");
		buf.append("            ;\n");
		buf.append("        } else {\n");
		buf.append("            ;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected2 = buf.toString();

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        if (false) {\n");
		buf.append("            if (true) {\n");
		buf.append("                ;\n");
		buf.append("            } else if (false)\n");
		buf.append("                ;\n");
		buf.append("            else\n");
		buf.append("                ;\n");
		buf.append("        } else\n");
		buf.append("            ;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected3 = buf.toString();

		assertExpectedExistInProposals(proposals, new String[]{expected1, expected2, expected3});
	}

	@Test
	public void testRemoveIfBlock01() throws Exception {

		IPackageFragment pack1 = fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        if (true) {\n");
		buf.append("            ;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu = pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		String str = "if (";
		AssistContext context = getCorrectionContext(cu, buf.toString().indexOf(str), 0);
		List proposals = collectAssists(context, false);

		assertNumberOfProposals(proposals, 4);
		assertCorrectLabels(proposals);

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        if (true)\n");
		buf.append("            ;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1 = buf.toString();

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        if (true) {\n");
		buf.append("            ;\n");
		buf.append("        } else {\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected2 = buf.toString();

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        ;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected3 = buf.toString();

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        if (false)\n");
		buf.append("            return;\n");
		buf.append("        ;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected4 = buf.toString();

		assertExpectedExistInProposals(proposals, new String[]{expected1, expected2, expected3, expected4});
	}

	@Test
	public void testRemoveIfBlock02() throws Exception {

		IPackageFragment pack1 = fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        if (true) {\n");
		buf.append("            ;\n");
		buf.append("        } else {\n");
		buf.append("            ;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu = pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		String str = "if (";
		AssistContext context = getCorrectionContext(cu, buf.toString().indexOf(str), 0);
		List proposals = collectAssists(context, false);

		assertNumberOfProposals(proposals, 4);
		assertCorrectLabels(proposals);

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        if (true) {\n");
		buf.append("            ;\n");
		buf.append("        } else\n");
		buf.append("            ;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1 = buf.toString();

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        if (true)\n");
		buf.append("            ;\n");
		buf.append("        else {\n");
		buf.append("            ;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected2 = buf.toString();

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        if (true)\n");
		buf.append("            ;\n");
		buf.append("        else\n");
		buf.append("            ;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected3 = buf.toString();

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        if (false) {\n");
		buf.append("            ;\n");
		buf.append("        } else {\n");
		buf.append("            ;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected4 = buf.toString();

		assertExpectedExistInProposals(proposals, new String[]{expected1, expected2, expected3, expected4});
	}

	@Test
	public void testRemoveIfBlock03() throws Exception {

		IPackageFragment pack1 = fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        if (true) {\n");
		buf.append("            ;\n");
		buf.append("        } else {\n");
		buf.append("            ;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu = pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		String str = "{\n            ;";
		AssistContext context = getCorrectionContext(cu, buf.toString().indexOf(str), 0);
		List proposals = collectAssists(context, false);

		assertNumberOfProposals(proposals, 3);
		assertCorrectLabels(proposals);

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        if (true) {\n");
		buf.append("            ;\n");
		buf.append("        } else\n");
		buf.append("            ;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1 = buf.toString();

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        if (true)\n");
		buf.append("            ;\n");
		buf.append("        else {\n");
		buf.append("            ;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected2 = buf.toString();

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        if (true)\n");
		buf.append("            ;\n");
		buf.append("        else\n");
		buf.append("            ;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected3 = buf.toString();

		assertExpectedExistInProposals(proposals, new String[]{expected1, expected2, expected3});
	}

	@Test
	public void testRemoveIfBlock04() throws Exception {

		IPackageFragment pack1 = fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public int foo() {\n");
		buf.append("        if (true)\n");
		buf.append("            return 1; /* comment*/\n");
		buf.append("        else\n");
		buf.append("            return 2;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu = pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		String str = "/* comment*/";
		int indexOf = buf.toString().indexOf(str) + str.length();
		AssistContext context = getCorrectionContext(cu, indexOf, 0);
		List proposals = collectAssists(context, false);

		assertNumberOfProposals(proposals, 4);
		assertCorrectLabels(proposals);

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public int foo() {\n");
		buf.append("        if (true) {\n");
		buf.append("            return 1; /* comment*/\n");
		buf.append("        } else\n");
		buf.append("            return 2;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1 = buf.toString();

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public int foo() {\n");
		buf.append("        if (true) {\n");
		buf.append("            return 1; /* comment*/\n");
		buf.append("        } else {\n");
		buf.append("            return 2;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected2 = buf.toString();

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public int foo() {\n");
		buf.append("        if (false)\n");
		buf.append("            return 2;\n");
		buf.append("        else\n");
		buf.append("            return 1; /* comment*/\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected3 = buf.toString();

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public int foo() {\n");
		buf.append("        return true ? 1 : 2;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected4 = buf.toString();

		assertExpectedExistInProposals(proposals, new String[]{expected1, expected2, expected3, expected4});
	}

	@Test
	public void testRemoveIfBlockBug128843() throws Exception {

		IPackageFragment pack1 = fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        if (true) {\n");
		buf.append("            ;\n");
		buf.append("        } else if (false) {\n");
		buf.append("            ;\n");
		buf.append("        } else {\n");
		buf.append("            ;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu = pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		String str = " (false) {";
		AssistContext context = getCorrectionContext(cu, buf.toString().indexOf(str), 0);
		List proposals = collectAssists(context, false);

		assertNumberOfProposals(proposals, 4);
		assertCorrectLabels(proposals);

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        if (true) {\n");
		buf.append("            ;\n");
		buf.append("        } else if (false)\n");
		buf.append("            ;\n");
		buf.append("        else {\n");
		buf.append("            ;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1 = buf.toString();

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        if (true) {\n");
		buf.append("            ;\n");
		buf.append("        } else if (false) {\n");
		buf.append("            ;\n");
		buf.append("        } else\n");
		buf.append("            ;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected2 = buf.toString();

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        if (true)\n");
		buf.append("            ;\n");
		buf.append("        else if (false)\n");
		buf.append("            ;\n");
		buf.append("        else\n");
		buf.append("            ;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected3 = buf.toString();

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        if (true) {\n");
		buf.append("            ;\n");
		buf.append("        } else if (true) {\n");
		buf.append("            ;\n");
		buf.append("        } else {\n");
		buf.append("            ;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected4 = buf.toString();

		assertExpectedExistInProposals(proposals, new String[]{expected1, expected2, expected3, expected4});
	}

	@Test
	public void testRemoveIfBlockBug138628() throws Exception {

		IPackageFragment pack1 = fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        if (true) {\n");
		buf.append("            if (true)\n");
		buf.append("                ;\n");
		buf.append("        } else\n");
		buf.append("            ;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu = pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		String str = " (true) {";
		AssistContext context = getCorrectionContext(cu, buf.toString().indexOf(str), 0);
		List proposals = collectAssists(context, false);

		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        if (false)\n");
		buf.append("            ;\n");
		buf.append("        else {\n");
		buf.append("            if (true)\n");
		buf.append("                ;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1 = buf.toString();

		assertExpectedExistInProposals(proposals, new String[]{expected1});
	}

	@Test
	public void testRemoveIfBlockBug149990_1() throws Exception {

		IPackageFragment pack1 = fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf = new StringBuffer();
		buf.append("package test;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        if (false) {\n");
		buf.append("            while (true)\n");
		buf.append("                if (false) {\n");
		buf.append("                    ;\n");
		buf.append("                }\n");
		buf.append("        } else\n");
		buf.append("            ;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu = pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		String str = " (false) {";
		AssistContext context = getCorrectionContext(cu, buf.toString().indexOf(str), 0);
		List proposals = collectAssists(context, false);

		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);

		buf = new StringBuffer();
		buf.append("package test;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        if (true)\n");
		buf.append("            ;\n");
		buf.append("        else {\n");
		buf.append("            while (true)\n");
		buf.append("                if (false) {\n");
		buf.append("                    ;\n");
		buf.append("                }\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1 = buf.toString();

		assertExpectedExistInProposals(proposals, new String[]{expected1});
	}

	@Test
	public void testRemoveIfBlockBug139675() throws Exception {

		IPackageFragment pack1 = fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        if (false)\n");
		buf.append("            if (true) {\n");
		buf.append("                ;\n");
		buf.append("            } else if (false) {\n");
		buf.append("                ;\n");
		buf.append("            } else {\n");
		buf.append("                ;\n");
		buf.append("            }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu = pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		String str = " (true) {";
		AssistContext context = getCorrectionContext(cu, buf.toString().indexOf(str), 0);
		List proposals = collectAssists(context, false);

		assertNumberOfProposals(proposals, 3);
		assertCorrectLabels(proposals);

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        if (false)\n");
		buf.append("            if (true)\n");
		buf.append("                ;\n");
		buf.append("            else if (false) {\n");
		buf.append("                ;\n");
		buf.append("            } else {\n");
		buf.append("                ;\n");
		buf.append("            }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1 = buf.toString();

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        if (false)\n");
		buf.append("            if (true)\n");
		buf.append("                ;\n");
		buf.append("            else if (false)\n");
		buf.append("                ;\n");
		buf.append("            else\n");
		buf.append("                ;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected2 = buf.toString();

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        if (false)\n");
		buf.append("            if (false) {\n");
		buf.append("                if (false) {\n");
		buf.append("                    ;\n");
		buf.append("                } else {\n");
		buf.append("                    ;\n");
		buf.append("                }\n");
		buf.append("            } else {\n");
		buf.append("                ;\n");
		buf.append("            }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected3 = buf.toString();

		assertExpectedExistInProposals(proposals, new String[]{expected1, expected2, expected3});
	}

	@Test
	public void testRemoveIfBlockBug149990_2() throws Exception {

		IPackageFragment pack1 = fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf = new StringBuffer();
		buf.append("package test;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        if (false)\n");
		buf.append("            while (true)\n");
		buf.append("                while (true) {\n");
		buf.append("                    while (true)\n");
		buf.append("                        if (false)\n");
		buf.append("                            ;\n");
		buf.append("                }\n");
		buf.append("        else\n");
		buf.append("            ;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu = pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		String str = " (true) {";
		AssistContext context = getCorrectionContext(cu, buf.toString().indexOf(str), 0);
		List proposals = collectAssists(context, false);

		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);

		buf = new StringBuffer();
		buf.append("package test;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        if (false)\n");
		buf.append("            while (true)\n");
		buf.append("                while (true)\n");
		buf.append("                    if (false)\n");
		buf.append("                        ;\n");
		buf.append("        else\n");
		buf.append("            ;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1 = buf.toString();

		assertExpectedExistInProposals(proposals, new String[]{expected1});

	}

	@Test
	public void testRemoveWhileBlock01() throws Exception {

		IPackageFragment pack1 = fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        while (true) {\n");
		buf.append("            ;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu = pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		String str = "while (";
		AssistContext context = getCorrectionContext(cu, buf.toString().indexOf(str), 0);
		List proposals = collectAssists(context, false);

		assertNumberOfProposals(proposals, 2);
		assertCorrectLabels(proposals);

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        while (true);\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1 = buf.toString();

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        ;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected2 = buf.toString();

		assertExpectedExistInProposals(proposals, new String[]{expected1, expected2});
	}

	@Test
	public void testRemoveForBlock01() throws Exception {

		IPackageFragment pack1 = fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        for (;;) {\n");
		buf.append("            ;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu = pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		String str = "for (";
		AssistContext context = getCorrectionContext(cu, buf.toString().indexOf(str), 0);
		List proposals = collectAssists(context, false);

		assertNumberOfProposals(proposals, 2);
		assertCorrectLabels(proposals);

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        for (;;);\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1 = buf.toString();

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        ;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected2 = buf.toString();

		assertExpectedExistInProposals(proposals, new String[]{expected1, expected2});
	}

	@Test
	public void testRemoveDoBlock01() throws Exception {

		IPackageFragment pack1 = fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        do {\n");
		buf.append("            ;\n");
		buf.append("        } while (true);\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu = pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		String str = "do {";
		AssistContext context = getCorrectionContext(cu, buf.toString().indexOf(str), 0);
		List proposals = collectAssists(context, false);

		assertNumberOfProposals(proposals, 2);
		assertCorrectLabels(proposals);

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        do; while (true);\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1 = buf.toString();

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        ;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected2 = buf.toString();

		assertExpectedExistInProposals(proposals, new String[]{expected1, expected2});
	}

	@Test
	public void testMakeFinal01() throws Exception {
		IPackageFragment pack1 = fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf = new StringBuffer();
		buf.append("package test;\n");
		buf.append("public class E {\n");
		buf.append("    private int i= 0;\n");
		buf.append("    private void foo() {\n");
		buf.append("        System.out.println(i);\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu = pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset1 = buf.toString().indexOf("public");
		int offset2 = buf.toString().lastIndexOf("}");
		AssistContext context = getCorrectionContext(cu, offset1, offset2 - offset1);
		List proposals = collectAssists(context, false);

		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);

		buf = new StringBuffer();
		buf.append("package test;\n");
		buf.append("public class E {\n");
		buf.append("    private final int i= 0;\n");
		buf.append("    private void foo() {\n");
		buf.append("        System.out.println(i);\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1 = buf.toString();

		String expected2 = null; // Wrap in buf.append() (to clipboard)

		assertExpectedExistInProposals(proposals, new String[]{expected1/*, expected2*/});
	}

	@Test
	public void testMakeFinal02() throws Exception {
		IPackageFragment pack1 = fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf = new StringBuffer();
		buf.append("package test;\n");
		buf.append("public class E {\n");
		buf.append("    private final int i= 0;\n");
		buf.append("    private void foo() {\n");
		buf.append("        System.out.println(i);\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu = pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset1 = buf.toString().indexOf("public");
		int offset2 = buf.toString().lastIndexOf("}");
		AssistContext context = getCorrectionContext(cu, offset1, offset2 - offset1);
		List proposals = collectAssists(context, false);

		assertNumberOfProposals(proposals, 0);
		assertProposalDoesNotExist(proposals, CHANGE_MODIFIER_TO_FINAL);

//		buf= new StringBuffer();
//		String expected1= null; // Wrap in buf.append() (to clipboard)
//		assertExpectedExistInProposals(proposals, new String[] { expected1 });
	}

	@Test
	@Ignore
	public void testMakeFinal03() throws Exception {
		IPackageFragment pack1 = fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf = new StringBuffer();
		buf.append("package test;\n");
		buf.append("public class E {\n");
		buf.append("    public int i= 0;\n");
		buf.append("    private void foo() {\n");
		buf.append("        System.out.println(i);\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu = pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset = buf.toString().indexOf("i=");
		AssistContext context = getCorrectionContext(cu, offset, 1);
		List proposals = collectAssists(context, false);

		assertNumberOfProposals(proposals, 1);
		assertProposalDoesNotExist(proposals, CHANGE_MODIFIER_TO_FINAL);

		buf = new StringBuffer();
		buf.append("package test;\n");
		buf.append("public class E {\n");
		buf.append("    private int i= 0;\n");
		buf.append("    private void foo() {\n");
		buf.append("        System.out.println(getI());\n");
		buf.append("    }\n");
		buf.append("    public int getI() {\n");
		buf.append("        return i;\n");
		buf.append("    }\n");
		buf.append("    public void setI(int i) {\n");
		buf.append("        this.i = i;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1 = buf.toString();
		assertExpectedExistInProposals(proposals, new String[]{expected1});
	}

	@Test
	public void testMakeFinal04() throws Exception {
		IPackageFragment pack1 = fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf = new StringBuffer();
		buf.append("package test;\n");
		buf.append("public class E {\n");
		buf.append("    private void foo() {\n");
		buf.append("        int i= 0, j= 0;\n");
		buf.append("        System.out.println(i);\n");
		buf.append("        System.out.println(j);\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu = pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset = buf.toString().indexOf("int i= 0");
		int length = "int i= 0".length();
		AssistContext context = getCorrectionContext(cu, offset, length);
		List proposals = collectAssists(context, false);

		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);

		buf = new StringBuffer();
		buf.append("package test;\n");
		buf.append("public class E {\n");
		buf.append("    private void foo() {\n");
		buf.append("        final int i= 0;\n");
		buf.append("        int j= 0;\n");
		buf.append("        System.out.println(i);\n");
		buf.append("        System.out.println(j);\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertExpectedExistInProposals(proposals, new String[]{buf.toString()});
	}

	@Test
	public void testMakeFinal05() throws Exception {
		IPackageFragment pack1 = fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf = new StringBuffer();
		buf.append("package test;\n");
		buf.append("public class E {\n");
		buf.append("    private void foo(int i, int j) {\n");
		buf.append("        System.out.println(i);\n");
		buf.append("        System.out.println(j);\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu = pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset = buf.toString().indexOf("int i");
		int length = "int i".length();
		AssistContext context = getCorrectionContext(cu, offset, length);
		List proposals = collectAssists(context, false);

		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);

		buf = new StringBuffer();
		buf.append("package test;\n");
		buf.append("public class E {\n");
		buf.append("    private void foo(final int i, int j) {\n");
		buf.append("        System.out.println(i);\n");
		buf.append("        System.out.println(j);\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertExpectedExistInProposals(proposals, new String[]{buf.toString()});
	}

	@Test
	public void testMakeFinal06() throws Exception {
		IPackageFragment pack1 = fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf = new StringBuffer();
		buf.append("package test;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        int i= 0;\n");
		buf.append("        i= 1;\n");
		buf.append("        System.out.println(i);\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu = pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset1 = buf.toString().indexOf("public");
		int offset2 = buf.toString().lastIndexOf("}");
		AssistContext context = getCorrectionContext(cu, offset1, offset2 - offset1);
		List proposals = collectAssists(context, false);

		assertNumberOfProposals(proposals, 0);
		assertProposalDoesNotExist(proposals, CHANGE_MODIFIER_TO_FINAL);

		assertExpectedExistInProposals(proposals, new String[]{null}); // Wrap in buf.append() (to clipboard)
	}

	@Test
	public void testMakeFinal07() throws Exception {
		IPackageFragment pack1 = fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf = new StringBuffer();
		buf.append("package test;\n");
		buf.append("public class E {\n");
		buf.append("    private int i= 0;\n");
		buf.append("    public void foo() {\n");
		buf.append("        System.out.println(i);\n");
		buf.append("    }\n");
		buf.append("    public void set(int i) {\n");
		buf.append("        this.i= i;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu = pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset = buf.toString().indexOf("private int i= 0");
		int length = "private int i= 0".length();
		AssistContext context = getCorrectionContext(cu, offset, length);
		List proposals = collectAssists(context, false);

		assertNumberOfProposals(proposals, 0);
		assertProposalDoesNotExist(proposals, CHANGE_MODIFIER_TO_FINAL);
	}

	@Test
	public void testMakeFinal08() throws Exception {
		IPackageFragment pack1 = fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf = new StringBuffer();
		buf.append("package test;\n");
		buf.append("public class E {\n");
		buf.append("    private int i= 0;\n");
		buf.append("    public void foo() {\n");
		buf.append("        System.out.println(i);\n");
		buf.append("    }\n");
		buf.append("    public void reset() {\n");
		buf.append("        i= 0;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu = pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset1 = buf.toString().indexOf("public");
		int offset2 = buf.toString().lastIndexOf("}");
		AssistContext context = getCorrectionContext(cu, offset1, offset2 - offset1);
		List proposals = collectAssists(context, false);

		assertNumberOfProposals(proposals, 0);
		assertProposalDoesNotExist(proposals, CHANGE_MODIFIER_TO_FINAL);

		assertExpectedExistInProposals(proposals, new String[]{null}); // Wrap in buf.append() (to clipboard)
	}

	@Test
	public void testMakeFinal09() throws Exception {
		IPackageFragment pack1 = fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf = new StringBuffer();
		buf.append("package test;\n");
		buf.append("public class E {\n");
		buf.append("    private int i= 0;\n");
		buf.append("    public void foo() {\n");
		buf.append("        System.out.println(i);\n");
		buf.append("    }\n");
		buf.append("    public void reset() {\n");
		buf.append("        i--;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu = pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset1 = buf.toString().indexOf("public");
		int offset2 = buf.toString().lastIndexOf("}");
		AssistContext context = getCorrectionContext(cu, offset1, offset2 - offset1);
		List proposals = collectAssists(context, false);

		assertNumberOfProposals(proposals, 0);
		assertProposalDoesNotExist(proposals, CHANGE_MODIFIER_TO_FINAL);

		assertExpectedExistInProposals(proposals, new String[]{null}); // Wrap in buf.append() (to clipboard)
	}

	@Test
	public void testMakeFinal10() throws Exception {
		IPackageFragment pack1 = fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf = new StringBuffer();
		buf.append("package test;\n");
		buf.append("public class E {\n");
		buf.append("    private int i= 0;\n");
		buf.append("    public void foo() {\n");
		buf.append("        System.out.println(i);\n");
		buf.append("    }\n");
		buf.append("    public void reset() {\n");
		buf.append("        this.i++;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu = pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset1 = buf.toString().indexOf("public");
		int offset2 = buf.toString().lastIndexOf("}");
		AssistContext context = getCorrectionContext(cu, offset1, offset2 - offset1);
		List proposals = collectAssists(context, false);

		assertNumberOfProposals(proposals, 0);
		assertProposalDoesNotExist(proposals, CHANGE_MODIFIER_TO_FINAL);

		assertExpectedExistInProposals(proposals, new String[]{null}); // Wrap in buf.append() (to clipboard)
	}

	@Test
	public void testMakeFinal11() throws Exception {
		IPackageFragment pack1 = fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf = new StringBuffer();
		buf.append("package test;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        for (int j= 0, i= 0; j < (new int[0]).length; j++) {\n");
		buf.append("            System.out.println(i);\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu = pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset1 = buf.toString().indexOf("public");
		int offset2 = buf.toString().lastIndexOf("}");
		AssistContext context = getCorrectionContext(cu, offset1, offset2 - offset1);
		List proposals = collectAssists(context, false);

		assertNumberOfProposals(proposals, 0);
		assertProposalDoesNotExist(proposals, CHANGE_MODIFIER_TO_FINAL);

		assertExpectedExistInProposals(proposals, new String[]{null}); // Wrap in buf.append() (to clipboard)
	}

	@Test
	public void testMakeFinal12() throws Exception {
		IPackageFragment pack1 = fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf = new StringBuffer();
		buf.append("package test;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        int i= 1, j= i + 1, h= j + 1;\n");
		buf.append("        System.out.println(i);\n");
		buf.append("        System.out.println(j);\n");
		buf.append("        System.out.println(h);\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu = pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset = buf.toString().indexOf("int i= 1");
		int length = "int i= 1".length();
		AssistContext context = getCorrectionContext(cu, offset, length);
		List proposals = collectAssists(context, false);

		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);

		buf = new StringBuffer();
		buf.append("package test;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        final int i= 1;\n");
		buf.append("        int j= i + 1, h= j + 1;\n");
		buf.append("        System.out.println(i);\n");
		buf.append("        System.out.println(j);\n");
		buf.append("        System.out.println(h);\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertExpectedExistInProposals(proposals, new String[]{buf.toString()});
	}

	@Test
	public void testMakeFinal13() throws Exception {
		IPackageFragment pack1 = fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf = new StringBuffer();
		buf.append("package test;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        int i= 1, j= i + 1, h= j + 1;\n");
		buf.append("        System.out.println(i);\n");
		buf.append("        System.out.println(j);\n");
		buf.append("        System.out.println(h);\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu = pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset = buf.toString().indexOf("j= i + 1");
		int length = "j= i + 1".length();
		AssistContext context = getCorrectionContext(cu, offset, length);
		List proposals = collectAssists(context, false);

		assertNumberOfProposals(proposals, 2);
		assertCorrectLabels(proposals);

		buf = new StringBuffer();
		buf.append("package test;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        int i= 1;\n");
		buf.append("        final int j= i + 1;\n");
		buf.append("        int h= j + 1;\n");
		buf.append("        System.out.println(i);\n");
		buf.append("        System.out.println(j);\n");
		buf.append("        System.out.println(h);\n");
		buf.append("    }\n");
		buf.append("}\n");
		String ex1 = buf.toString();

		buf = new StringBuffer();
		buf.append("package test;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        int i= 1, j, h= j + 1;\n");
		buf.append("        j = i + 1;\n");
		buf.append("        System.out.println(i);\n");
		buf.append("        System.out.println(j);\n");
		buf.append("        System.out.println(h);\n");
		buf.append("    }\n");
		buf.append("}\n");
		String ex2 = buf.toString();

		assertExpectedExistInProposals(proposals, new String[]{ex1, ex2});
	}

	@Test
	public void testMakeFinal14() throws Exception {
		IPackageFragment pack1 = fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf = new StringBuffer();
		buf.append("package test;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        int i= 1, j= i + 1, h= j + 1;\n");
		buf.append("        System.out.println(i);\n");
		buf.append("        System.out.println(j);\n");
		buf.append("        System.out.println(h);\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu = pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset = buf.toString().indexOf("h= j + 1");
		int length = "h= j + 1".length();
		AssistContext context = getCorrectionContext(cu, offset, length);
		List proposals = collectAssists(context, false);

		assertNumberOfProposals(proposals, 2);
		assertCorrectLabels(proposals);

		buf = new StringBuffer();
		buf.append("package test;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        int i= 1, j= i + 1;\n");
		buf.append("        final int h= j + 1;\n");
		buf.append("        System.out.println(i);\n");
		buf.append("        System.out.println(j);\n");
		buf.append("        System.out.println(h);\n");
		buf.append("    }\n");
		buf.append("}\n");
		String ex1 = buf.toString();

		buf = new StringBuffer();
		buf.append("package test;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        int i= 1, j= i + 1, h;\n");
		buf.append("        h = j + 1;\n");
		buf.append("        System.out.println(i);\n");
		buf.append("        System.out.println(j);\n");
		buf.append("        System.out.println(h);\n");
		buf.append("    }\n");
		buf.append("}\n");
		String ex2 = buf.toString();

		assertExpectedExistInProposals(proposals, new String[]{ex1, ex2});
	}

	@Test
	public void testMakeFinal15() throws Exception {
		IPackageFragment pack1 = fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf = new StringBuffer();
		buf.append("package test;\n");
		buf.append("import java.io.Serializable;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        Serializable ser= new Serializable() {\n");
		buf.append("            private int i= 0;\n");
		buf.append("            Serializable ser2= new Serializable() {\n");
		buf.append("                public void foo() {\n");
		buf.append("                    System.out.println(i);\n");
		buf.append("                }\n");
		buf.append("            };\n");
		buf.append("        };\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu = pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset1 = buf.toString().indexOf("public");
		int offset2 = buf.toString().lastIndexOf("}");
		AssistContext context = getCorrectionContext(cu, offset1, offset2 - offset1);
		List proposals = collectAssists(context, false);

		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);

		buf = new StringBuffer();
		buf.append("package test;\n");
		buf.append("import java.io.Serializable;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        final Serializable ser= new Serializable() {\n");
		buf.append("            private final int i= 0;\n");
		buf.append("            Serializable ser2= new Serializable() {\n");
		buf.append("                public void foo() {\n");
		buf.append("                    System.out.println(i);\n");
		buf.append("                }\n");
		buf.append("            };\n");
		buf.append("        };\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		String expected2= null; // Wrap in buf.append() (to clipboard)

		assertExpectedExistInProposals(proposals, new String[] { expected1/*, expected2*/ });
	}

	@Test
	public void testMakeFinal16() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        int i= 0;\n");
		buf.append("        Integer in= new Integer(i++);\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("int i= 0");
		int length= "int i= 0".length();
		AssistContext context= getCorrectionContext(cu, offset, length);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 0);
		assertProposalDoesNotExist(proposals, CHANGE_MODIFIER_TO_FINAL);
	}

	@Test
    @Ignore
	public void testMakeFinal17() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test;\n");
		buf.append("public class E {\n");
		buf.append("    private int i= 0;\n");
		buf.append("    private void foo() {\n");
		buf.append("        System.out.println(i);\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);
	
		int offset= buf.toString().indexOf("i=");
		AssistContext context= getCorrectionContext(cu, offset, 1);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 1);
		assertProposalDoesNotExist(proposals, CHANGE_MODIFIER_TO_FINAL);
		
		buf= new StringBuffer();
		buf.append("package test;\n");
		buf.append("public class E {\n");
		buf.append("    private int i= 0;\n");
		buf.append("    private void foo() {\n");
		buf.append("        System.out.println(getI());\n");
		buf.append("    }\n");
		buf.append("    public int getI() {\n");
		buf.append("        return i;\n");
		buf.append("    }\n");
		buf.append("    public void setI(int i) {\n");
		buf.append("        this.i = i;\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertExpectedExistInProposals(proposals, new String[] { buf.toString() });
	}

	@Test
	public void testMakeFinal18() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test;\n");
		buf.append("public class E {\n");
		buf.append("    private int i= 0;\n");
		buf.append("    private void foo() {\n");
		buf.append("        System.out.println(i);\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("E");
		AssistContext context= getCorrectionContext(cu, offset, 1);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 0);
		assertProposalDoesNotExist(proposals, CHANGE_MODIFIER_TO_FINAL);
	}

	@Test
	public void testMakeFinal19() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test;\n");
		buf.append("public class E {\n");
		buf.append("    private void foo() {\n");
		buf.append("        int i= 0;\n");
		buf.append("        System.out.println(i);\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("foo");
		int length= "foo".length();
		AssistContext context= getCorrectionContext(cu, offset, length);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 0);
		assertProposalDoesNotExist(proposals, CHANGE_MODIFIER_TO_FINAL);
	}

	@Test
	public void testMakeFinalBug148373() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(Integer i) {\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		String selection= "public void foo(Integer i)";
		int offset= buf.toString().indexOf(selection);
		AssistContext context= getCorrectionContext(cu, offset, selection.length());
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(final Integer i) {\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertExpectedExistInProposals(proposals, new String[] {buf.toString()});
	}

	@Test
	public void testConvertAnonymousToNested1() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("pack", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack;\n");
		buf.append("public class E {\n");
		buf.append("    public Object foo(final String name) {\n");
		buf.append("        return new Runnable() {\n");
		buf.append("            public void run() {\n");
		buf.append("                foo(name);\n");
		buf.append("            }\n");
		buf.append("        };\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("Runnable");
		AssistContext context= getCorrectionContext(cu, offset, 1);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package pack;\n");
		buf.append("public class E {\n");
		buf.append("    private final class RunnableImplementation implements Runnable {\n");
		buf.append("        private final String name;\n");
		buf.append("        private RunnableImplementation(String name) {\n");
		buf.append("            this.name = name;\n");
		buf.append("        }\n");
		buf.append("        public void run() {\n");
		buf.append("            foo(name);\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("    public Object foo(final String name) {\n");
		buf.append("        return new RunnableImplementation(name);\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertExpectedExistInProposals(proposals, new String[] {buf.toString()});
	}

	@Test
	public void testConvertAnonymousToNested2() throws Exception {
		JavaModelManager.getIndexManager().indexAll(fJProject1.getProject());
//		Preferences corePrefs= JavaPlugin.getJavaCorePluginPreferences();
		fJProject1.setOption(JavaCore.CODEASSIST_FIELD_PREFIXES, "f");
		fJProject1.setOption(JavaCore.CODEASSIST_LOCAL_PREFIXES, "l");
		fJProject1.setOption(JavaCore.CODEASSIST_ARGUMENT_PREFIXES, "p");

		IPackageFragment pack1= fSourceFolder.createPackageFragment("pack", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack;\n");
		buf.append("import java.util.ArrayList;\n");
		buf.append("public class E {\n");
		buf.append("    public Object foo(final String pName) {\n");
		buf.append("        int lVar= 8;\n");
		buf.append("        return new ArrayList(lVar) {\n");
		buf.append("            String fExisting= pName;\n");
		buf.append("            public void run() {\n");
		buf.append("                foo(fExisting);\n");
		buf.append("            }\n");
		buf.append("        };\n");
		buf.append("    }\n");
		buf.append("}\n");
		buf.append("class ArrayListExtension {\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("ArrayList(lVar)");
		AssistContext context= getCorrectionContext(cu, offset, 1);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package pack;\n");
		buf.append("import java.util.ArrayList;\n");
		buf.append("public class E {\n");
		buf.append("    private final class ArrayListExtension2 extends ArrayList {\n");
		buf.append("        private final String fName;\n");
		buf.append("        String fExisting;\n");
		buf.append("        private ArrayListExtension2(int pArg0, String pName) {\n");
		buf.append("            super(pArg0);\n");
		buf.append("            fName = pName;\n");
		buf.append("            fExisting = fName;\n");
		buf.append("        }\n");
		buf.append("        public void run() {\n");
		buf.append("            foo(fExisting);\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("    public Object foo(final String pName) {\n");
		buf.append("        int lVar= 8;\n");
		buf.append("        return new ArrayListExtension2(lVar, pName);\n");
		buf.append("    }\n");
		buf.append("}\n");
		buf.append("class ArrayListExtension {\n");
		buf.append("}\n");
		assertExpectedExistInProposals(proposals, new String[] {buf.toString()});
	}

	@Test
	public void testConvertToStringBuffer1() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    public void foo() {\n");
		buf.append("        String strX = \"foo\"+\"bar\"+\"baz\"+\"biz\";\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("A.java", buf.toString(), false, null);

		AssistContext context= getCorrectionContext(cu, buf.toString().indexOf("\"+\""), 0);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 6);
		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    public void foo() {\n");
		buf.append("        StringBuilder stringBuilder = new StringBuilder();\n");
		buf.append("        stringBuilder.append(\"foo\");\n");
		buf.append("        stringBuilder.append(\"bar\");\n");
		buf.append("        stringBuilder.append(\"baz\");\n");
		buf.append("        stringBuilder.append(\"biz\");\n");
		buf.append("        String strX = stringBuilder.toString();\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    public void foo() {\n");
		buf.append("        String string = \"foo\";\n");
		buf.append("        String strX = string+\"bar\"+\"baz\"+\"biz\";\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected2= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    public void foo() {\n");
		buf.append("        String string = \"foo\";\n");
		buf.append("        String strX = string+\"bar\"+\"baz\"+\"biz\";\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected3= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    private static final String FOO = \"foo\";\n");
		buf.append("\n");
		buf.append("    public void foo() {\n");
		buf.append("        String strX = FOO+\"bar\"+\"baz\"+\"biz\";\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected4= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    public void foo() {\n");
		buf.append("        String strX = (\"foo\"+\"bar\"+\"baz\"+\"biz\");\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected5= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    public void foo() {\n");
		buf.append("        String strX = \"foobarbazbiz\";\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected6= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    public void foo() {\n");
		buf.append("        String strX = \"FOO\"+\"bar\"+\"baz\"+\"biz\";\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected7= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1, expected2, expected3, expected4, expected5, expected6/*, expected7*/ });
	}

	@Test
	public void testConvertToStringBufferStringAndVar() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    public void foo() {\n");
		buf.append("        String foo = \"foo\";\n");
		buf.append("        String fuu = \"fuu\";\n");
		buf.append("        String strX = foo+\"bar\"+fuu;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("A.java", buf.toString(), false, null);

		AssistContext context= getCorrectionContext(cu, buf.toString().indexOf("strX ="), 0);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 5);
		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    public void foo() {\n");
		buf.append("        String foo = \"foo\";\n");
		buf.append("        String fuu = \"fuu\";\n");
		buf.append("        StringBuilder stringBuilder = new StringBuilder();\n");
		buf.append("        stringBuilder.append(foo);\n");
		buf.append("        stringBuilder.append(\"bar\");\n");
		buf.append("        stringBuilder.append(fuu);\n");
		buf.append("        String strX = stringBuilder.toString();\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    public void foo() {\n");
		buf.append("        String foo = \"foo\";\n");
		buf.append("        String fuu = \"fuu\";\n");
		buf.append("        String strX;\n");
		buf.append("        strX = foo+\"bar\"+fuu;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected2= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    public void foo() {\n");
		buf.append("        String foo = \"foo\";\n");
		buf.append("        String fuu = \"fuu\";\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected3= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    private String strX;\n");
		buf.append("\n");
		buf.append("    public void foo() {\n");
		buf.append("        String foo = \"foo\";\n");
		buf.append("        String fuu = \"fuu\";\n");
		buf.append("        strX = foo+\"bar\"+fuu;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected4= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("\n");
		buf.append("import java.text.MessageFormat;\n");
		buf.append("\n");
		buf.append("public class A {\n");
		buf.append("    public void foo() {\n");
		buf.append("        String foo = \"foo\";\n");
		buf.append("        String fuu = \"fuu\";\n");
		buf.append("        String strX = MessageFormat.format(\"{0}bar{1}\", foo, fuu);\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected5= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1, expected2, expected3, expected4, expected5 });
	}

	@Test
	public void testConvertToStringBufferNoFixWithoutString() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    public void foo() {\n");
		buf.append("        int strX = 5+1;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("A.java", buf.toString(), false, null);

		AssistContext context= getCorrectionContext(cu, buf.toString().indexOf("strX ="), 0);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 3);
		assertCommandIdDoesNotExist(proposals, QuickAssistProcessor.CONVERT_TO_STRING_BUFFER_ID);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    public void foo() {\n");
		buf.append("        int strX;\n");
		buf.append("        strX = 5+1;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    public void foo() {\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected2= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    private int strX;\n");
		buf.append("\n");
		buf.append("    public void foo() {\n");
		buf.append("        strX = 5+1;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected3= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1, expected2, expected3 });

	}

	@Test
	public void testConvertToStringBufferNoFixWithoutString2() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    public void foo() {\n");
		buf.append("        int strX;\n");
		buf.append("        strX = 5+1;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("A.java", buf.toString(), false, null);

		AssistContext context= getCorrectionContext(cu, buf.toString().indexOf("strX ="), 0);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 4);
		assertCommandIdDoesNotExist(proposals, QuickAssistProcessor.CONVERT_TO_STRING_BUFFER_ID);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    public void foo() {\n");
		buf.append("        int strX = 5+1;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    public void foo() {\n");
		buf.append("        int strX;\n");
		buf.append("        int strX2 = strX;\n");
		buf.append("        strX = 5+1;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected2= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    public void foo() {\n");
		buf.append("        int strX;\n");
		buf.append("        int strX2 = strX;\n");
		buf.append("        strX = 5+1;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected3= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    private int strX;\n");
		buf.append("\n");
		buf.append("    public void foo() {\n");
		buf.append("        strX = 5+1;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected4= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1, expected2, expected3, expected4 });
	}

	@Test
    @Ignore
	public void testConvertToStringBufferNoFixOutsideMethod() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    String strX = \"foo\"+\"bar\"\n");
		buf.append("    public void foo() {\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("A.java", buf.toString(), false, null);

		AssistContext context= getCorrectionContext(cu, buf.toString().indexOf("strX ="), 0);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 1);
		assertCommandIdDoesNotExist(proposals, QuickAssistProcessor.CONVERT_TO_STRING_BUFFER_ID);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    private String strX = \"foo\"+\"bar\"\n");
		buf.append("    public void foo() {\n");
		buf.append("    }\n");
		buf.append("    public String getStrX() {\n");
		buf.append("        return strX;\n");
		buf.append("    }\n");
		buf.append("    public void setStrX(String strX) {\n");
		buf.append("        this.strX = strX;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1 });
	}

	@Test
	public void testConvertToStringBufferDupVarName() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    public void foo() {\n");
		buf.append("        int stringBuilder = 5;\n");
		buf.append("        String stringBuilder2;\n");
		buf.append("        StringBuilder stringBuilder3 = null;\n");
		buf.append("        String strX = \"foo\"+\"bar\";\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("A.java", buf.toString(), false, null);

		AssistContext context= getCorrectionContext(cu, buf.toString().indexOf("strX ="), 0);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 4);
		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    public void foo() {\n");
		buf.append("        int stringBuilder = 5;\n");
		buf.append("        String stringBuilder2;\n");
		buf.append("        StringBuilder stringBuilder3 = null;\n");
		buf.append("        String strX;\n");
		buf.append("        strX = \"foo\"+\"bar\";\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    public void foo() {\n");
		buf.append("        int stringBuilder = 5;\n");
		buf.append("        String stringBuilder2;\n");
		buf.append("        StringBuilder stringBuilder3 = null;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected2= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    private String strX;\n");
		buf.append("\n");
		buf.append("    public void foo() {\n");
		buf.append("        int stringBuilder = 5;\n");
		buf.append("        String stringBuilder2;\n");
		buf.append("        StringBuilder stringBuilder3 = null;\n");
		buf.append("        strX = \"foo\"+\"bar\";\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected3= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    public void foo() {\n");
		buf.append("        int stringBuilder = 5;\n");
		buf.append("        String stringBuilder2;\n");
		buf.append("        StringBuilder stringBuilder3 = null;\n");
		buf.append("        StringBuilder stringBuilder4 = new StringBuilder();\n");
		buf.append("        stringBuilder4.append(\"foo\");\n");
		buf.append("        stringBuilder4.append(\"bar\");\n");
		buf.append("        String strX = stringBuilder4.toString();\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected4= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1, expected2, expected3, expected4 });
	}

	@Test
	public void testConvertToStringBufferInIfStatement() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    public void foo() {\n");
		buf.append("        String strX;\n");
		buf.append("        if(true) strX = \"foo\"+\"bar\";\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("A.java", buf.toString(), false, null);

		AssistContext context= getCorrectionContext(cu, buf.toString().indexOf("\"+\""), 0);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 6);
		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    public void foo() {\n");
		buf.append("        String strX;\n");
		buf.append("        if(true) {\n");
		buf.append("            StringBuilder stringBuilder = new StringBuilder();\n");
		buf.append("            stringBuilder.append(\"foo\");\n");
		buf.append("            stringBuilder.append(\"bar\");\n");
		buf.append("            strX = stringBuilder.toString();\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    public void foo() {\n");
		buf.append("        String strX;\n");
		buf.append("        if(true) {\n");
		buf.append("            String string = \"foo\";\n");
		buf.append("            strX = string+\"bar\";\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected2= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    private static final String FOO = \"foo\";\n");
		buf.append("\n");
		buf.append("    public void foo() {\n");
		buf.append("        String strX;\n");
		buf.append("        if(true) strX = FOO+\"bar\";\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected3= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    public void foo() {\n");
		buf.append("        String strX;\n");
		buf.append("        if(true) {\n");
		buf.append("            String string = \"foo\";\n");
		buf.append("            strX = string+\"bar\";\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected4= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    public void foo() {\n");
		buf.append("        String strX;\n");
		buf.append("        if(true) strX = (\"foo\"+\"bar\");\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected5= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    public void foo() {\n");
		buf.append("        String strX;\n");
		buf.append("        if(true) strX = \"foobar\";\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected6= buf.toString();

//		buf= new StringBuffer();
//		buf.append("package test1;\n");
//		buf.append("public class A {\n");
//		buf.append("    public void foo() {\n");
//		buf.append("        String strX;\n");
//		buf.append("        if(true) strX = \"FOO\"+\"bar\";\n");
//		buf.append("    }\n");
//		buf.append("}\n");
//		String expected7= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1, expected2, expected3, expected4, expected5, expected6/*, expected7*/ });
	}

	@Test
	public void testConvertToStringBufferAsParamter() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    public void foo() {\n");
		buf.append("        System.out.println(\"foo\"+\"bar\");\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("A.java", buf.toString(), false, null);

		AssistContext context= getCorrectionContext(cu, buf.toString().indexOf("\"+\""), 0);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 6);
		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    public void foo() {\n");
		buf.append("        StringBuilder stringBuilder = new StringBuilder();\n");
		buf.append("        stringBuilder.append(\"foo\");\n");
		buf.append("        stringBuilder.append(\"bar\");\n");
		buf.append("        System.out.println(stringBuilder.toString());\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    public void foo() {\n");
		buf.append("        String string = \"foo\";\n");
		buf.append("        System.out.println(string+\"bar\");\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected2= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    public void foo() {\n");
		buf.append("        String string = \"foo\";\n");
		buf.append("        System.out.println(string+\"bar\");\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected3= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    private static final String FOO = \"foo\";\n");
		buf.append("\n");
		buf.append("    public void foo() {\n");
		buf.append("        System.out.println(FOO+\"bar\");\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected4= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    public void foo() {\n");
		buf.append("        System.out.println((\"foo\"+\"bar\"));\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected5= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    public void foo() {\n");
		buf.append("        System.out.println(\"foobar\");\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected6= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    public void foo() {\n");
		buf.append("        System.out.println(\"FOO\"+\"bar\");\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected7= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1, expected2, expected3, expected4, expected5, expected6/*, expected7*/ });
	}

//	public void testConvertToStringBufferJava14() throws Exception {
//
//		Map oldOptions= fJProject1.getOptions(false);
//		Map newOptions= new HashMap(oldOptions);
//		JavaCore.setComplianceOptions(JavaCore.VERSION_1_4, newOptions);
//		fJProject1.setOptions(newOptions);
//
//		try {
//			IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
//			StringBuffer buf= new StringBuffer();
//			buf.append("package test1;\n");
//			buf.append("public class A {\n");
//			buf.append("    public void foo() {\n");
//			buf.append("        System.out.println(\"foo\"+\"bar\");\n");
//			buf.append("    }\n");
//			buf.append("}\n");
//			ICompilationUnit cu= pack1.createCompilationUnit("A.java", buf.toString(), false, null);
//
//			AssistContext context= getCorrectionContext(cu, buf.toString().indexOf("\"+\""), 0);
//			List proposals= collectAssists(context, false);
//
//			assertNumberOfProposals(proposals, 7);
//			assertCorrectLabels(proposals);
//
//			buf= new StringBuffer();
//			buf.append("package test1;\n");
//			buf.append("public class A {\n");
//			buf.append("    public void foo() {\n");
//			buf.append("        StringBuffer stringBuffer = new StringBuffer();\n");
//			buf.append("        stringBuffer.append(\"foo\");\n");
//			buf.append("        stringBuffer.append(\"bar\");\n");
//			buf.append("        System.out.println(stringBuffer.toString());\n");
//			buf.append("    }\n");
//			buf.append("}\n");
//			String expected1= buf.toString();
//
//			buf= new StringBuffer();
//			buf.append("package test1;\n");
//			buf.append("public class A {\n");
//			buf.append("    public void foo() {\n");
//			buf.append("        String string = \"foo\";\n");
//			buf.append("        System.out.println(string+\"bar\");\n");
//			buf.append("    }\n");
//			buf.append("}\n");
//			String expected2= buf.toString();
//
//			buf= new StringBuffer();
//			buf.append("package test1;\n");
//			buf.append("public class A {\n");
//			buf.append("    public void foo() {\n");
//			buf.append("        String string = \"foo\";\n");
//			buf.append("        System.out.println(string+\"bar\");\n");
//			buf.append("    }\n");
//			buf.append("}\n");
//			String expected3= buf.toString();
//
//			buf= new StringBuffer();
//			buf.append("package test1;\n");
//			buf.append("public class A {\n");
//			buf.append("    private static final String FOO = \"foo\";\n");
//			buf.append("\n");
//			buf.append("    public void foo() {\n");
//			buf.append("        System.out.println(FOO+\"bar\");\n");
//			buf.append("    }\n");
//			buf.append("}\n");
//			String expected4= buf.toString();
//
//			buf= new StringBuffer();
//			buf.append("package test1;\n");
//			buf.append("public class A {\n");
//			buf.append("    public void foo() {\n");
//			buf.append("        System.out.println((\"foo\"+\"bar\"));\n");
//			buf.append("    }\n");
//			buf.append("}\n");
//			String expected5= buf.toString();
//
//			buf= new StringBuffer();
//			buf.append("package test1;\n");
//			buf.append("public class A {\n");
//			buf.append("    public void foo() {\n");
//			buf.append("        System.out.println(\"foobar\");\n");
//			buf.append("    }\n");
//			buf.append("}\n");
//			String expected6= buf.toString();
//
//			buf= new StringBuffer();
//			buf.append("package test1;\n");
//			buf.append("public class A {\n");
//			buf.append("    public void foo() {\n");
//			buf.append("        System.out.println(\"FOO\"+\"bar\");\n");
//			buf.append("    }\n");
//			buf.append("}\n");
//			String expected7= buf.toString();
//
//			assertExpectedExistInProposals(proposals, new String[] { expected1, expected2, expected3, expected4, expected5, expected6, expected7 });
//		} finally {
//			fJProject1.setOptions(oldOptions);
//		}
//	}

	@Test
	public void testConvertToStringBufferExisting1() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    public void foo() {\n");
		buf.append("        StringBuffer buf= new StringBuffer();\n");
		buf.append("        buf.append(\"high\" + 5);\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("A.java", buf.toString(), false, null);

		AssistContext context= getCorrectionContext(cu, buf.toString().indexOf("\" + 5"), 0);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 8);
		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    public void foo() {\n");
		buf.append("        StringBuffer buf= new StringBuffer();\n");
		buf.append("        buf.append(\"high\");\n");
		buf.append("        buf.append(5);\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    public void foo() {\n");
		buf.append("        StringBuffer buf= new StringBuffer();\n");
		buf.append("        StringBuffer append = buf.append(\"high\" + 5);\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected2= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    private StringBuffer append;\n");
		buf.append("\n");
		buf.append("    public void foo() {\n");
		buf.append("        StringBuffer buf= new StringBuffer();\n");
		buf.append("        append = buf.append(\"high\" + 5);\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected3= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    public void foo() {\n");
		buf.append("        StringBuffer buf= new StringBuffer();\n");
		buf.append("        String string = \"high\";\n");
		buf.append("        buf.append(string + 5);\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected4= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    public void foo() {\n");
		buf.append("        StringBuffer buf= new StringBuffer();\n");
		buf.append("        String string = \"high\";\n");
		buf.append("        buf.append(string + 5);\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected5= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    private static final String HIGH = \"high\";\n");
		buf.append("\n");
		buf.append("    public void foo() {\n");
		buf.append("        StringBuffer buf= new StringBuffer();\n");
		buf.append("        buf.append(HIGH + 5);\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected6= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("\n");
		buf.append("import java.text.MessageFormat;\n");
		buf.append("\n");
		buf.append("public class A {\n");
		buf.append("    public void foo() {\n");
		buf.append("        StringBuffer buf= new StringBuffer();\n");
		buf.append("        buf.append(MessageFormat.format(\"high{0}\", 5));\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected7= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    public void foo() {\n");
		buf.append("        StringBuffer buf= new StringBuffer();\n");
		buf.append("        buf.append((\"high\" + 5));\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected8= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    public void foo() {\n");
		buf.append("        StringBuffer buf= new StringBuffer();\n");
		buf.append("        buf.append(\"HIGH\" + 5);\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected9= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1, expected2, expected3, expected4, expected5, expected6, expected7, expected8/*, expected9*/ });
	}

	@Test
	public void testConvertToStringBufferExisting2() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    public void foo() {\n");
		buf.append("        StringBuilder sb= new StringBuilder();\n");
		buf.append("        sb.append(\"high\" + 5 + \" ho\");\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("A.java", buf.toString(), false, null);
		
		AssistContext context= getCorrectionContext(cu, buf.toString().indexOf("\" + 5"), 0);
		List proposals= collectAssists(context, false);
		
		assertNumberOfProposals(proposals, 8);
		assertCorrectLabels(proposals);
		
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    public void foo() {\n");
		buf.append("        StringBuilder sb= new StringBuilder();\n");
		buf.append("        sb.append(\"high\");\n");
		buf.append("        sb.append(5);\n");
		buf.append("        sb.append(\" ho\");\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();
		
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    public void foo() {\n");
		buf.append("        StringBuilder sb= new StringBuilder();\n");
		buf.append("        StringBuilder append = sb.append(\"high\" + 5 + \" ho\");\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected2= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    private StringBuilder append;\n");
		buf.append("\n");
		buf.append("    public void foo() {\n");
		buf.append("        StringBuilder sb= new StringBuilder();\n");
		buf.append("        append = sb.append(\"high\" + 5 + \" ho\");\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected3= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    public void foo() {\n");
		buf.append("        StringBuilder sb= new StringBuilder();\n");
		buf.append("        String string = \"high\";\n");
		buf.append("        sb.append(string + 5 + \" ho\");\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected4= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    public void foo() {\n");
		buf.append("        StringBuilder sb= new StringBuilder();\n");
		buf.append("        String string = \"high\";\n");
		buf.append("        sb.append(string + 5 + \" ho\");\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected5= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    private static final String HIGH = \"high\";\n");
		buf.append("\n");
		buf.append("    public void foo() {\n");
		buf.append("        StringBuilder sb= new StringBuilder();\n");
		buf.append("        sb.append(HIGH + 5 + \" ho\");\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected6= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("\n");
		buf.append("import java.text.MessageFormat;\n");
		buf.append("\n");
		buf.append("public class A {\n");
		buf.append("    public void foo() {\n");
		buf.append("        StringBuilder sb= new StringBuilder();\n");
		buf.append("        sb.append(MessageFormat.format(\"high{0} ho\", 5));\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected7= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    public void foo() {\n");
		buf.append("        StringBuilder sb= new StringBuilder();\n");
		buf.append("        sb.append((\"high\" + 5 + \" ho\"));\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected8= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    public void foo() {\n");
		buf.append("        StringBuilder sb= new StringBuilder();\n");
		buf.append("        sb.append(\"HIGH\" + 5 + \" ho\");\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected9= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1, expected2, expected3, expected4, expected5, expected6, expected7, expected8/*, expected9*/ });
	}
	
//	public void testConvertToMessageFormat14() throws Exception {
//
//		Map oldOptions= fJProject1.getOptions(false);
//		Map newOptions= new HashMap(oldOptions);
//		JavaCore.setComplianceOptions(JavaCore.VERSION_1_4, newOptions);
//		fJProject1.setOptions(newOptions);
//
//		try {
//			IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
//			StringBuffer buf= new StringBuffer();
//			buf.append("package test1;\n");
//			buf.append("public class A {\n");
//			buf.append("    public void foo(Object o1, Object o2) {\n");
//			buf.append("        System.out.println(\"foo\" + o1 + \" \\\"bar\\\" \" + o2);\n");
//			buf.append("    }\n");
//			buf.append("}\n");
//			ICompilationUnit cu= pack1.createCompilationUnit("A.java", buf.toString(), false, null);
//
//			AssistContext context= getCorrectionContext(cu, buf.toString().indexOf("\" + o1 + \""), 0);
//			List proposals= collectAssists(context, false);
//
//			assertNumberOfProposals(proposals, 7);
//			assertCorrectLabels(proposals);
//
//			buf= new StringBuffer();
//			buf.append("package test1;\n");
//			buf.append("\n");
//			buf.append("import java.text.MessageFormat;\n");
//			buf.append("\n");
//			buf.append("public class A {\n");
//			buf.append("    public void foo(Object o1, Object o2) {\n");
//			buf.append("        System.out.println(MessageFormat.format(\"foo{0} \\\"bar\\\" {1}\", new Object[]{o1,\n");
//			buf.append("                o2}));\n");
//			buf.append("    }\n");
//			buf.append("}\n");
//			String expected1= buf.toString();
//
//			buf= new StringBuffer();
//			buf.append("package test1;\n");
//			buf.append("public class A {\n");
//			buf.append("    public void foo(Object o1, Object o2) {\n");
//			buf.append("        String string = \"foo\";\n");
//			buf.append("        System.out.println(string + o1 + \" \\\"bar\\\" \" + o2);\n");
//			buf.append("    }\n");
//			buf.append("}\n");
//			String expected2= buf.toString();
//
//			buf= new StringBuffer();
//			buf.append("package test1;\n");
//			buf.append("public class A {\n");
//			buf.append("    public void foo(Object o1, Object o2) {\n");
//			buf.append("        String string = \"foo\";\n");
//			buf.append("        System.out.println(string + o1 + \" \\\"bar\\\" \" + o2);\n");
//			buf.append("    }\n");
//			buf.append("}\n");
//			String expected3= buf.toString();
//
//			buf= new StringBuffer();
//			buf.append("package test1;\n");
//			buf.append("public class A {\n");
//			buf.append("    private static final String FOO = \"foo\";\n");
//			buf.append("\n");
//			buf.append("    public void foo(Object o1, Object o2) {\n");
//			buf.append("        System.out.println(FOO + o1 + \" \\\"bar\\\" \" + o2);\n");
//			buf.append("    }\n");
//			buf.append("}\n");
//			String expected4= buf.toString();
//
//			buf= new StringBuffer();
//			buf.append("package test1;\n");
//			buf.append("public class A {\n");
//			buf.append("    public void foo(Object o1, Object o2) {\n");
//			buf.append("        StringBuffer stringBuffer = new StringBuffer();\n");
//			buf.append("        stringBuffer.append(\"foo\");\n");
//			buf.append("        stringBuffer.append(o1);\n");
//			buf.append("        stringBuffer.append(\" \\\"bar\\\" \");\n");
//			buf.append("        stringBuffer.append(o2);\n");
//			buf.append("        System.out.println(stringBuffer.toString());\n");
//			buf.append("    }\n");
//			buf.append("}\n");
//			String expected5= buf.toString();
//
//			buf= new StringBuffer();
//			buf.append("package test1;\n");
//			buf.append("public class A {\n");
//			buf.append("    public void foo(Object o1, Object o2) {\n");
//			buf.append("        System.out.println((\"foo\" + o1 + \" \\\"bar\\\" \" + o2));\n");
//			buf.append("    }\n");
//			buf.append("}\n");
//			String expected6= buf.toString();
//
//			buf= new StringBuffer();
//			buf.append("package test1;\n");
//			buf.append("public class A {\n");
//			buf.append("    public void foo(Object o1, Object o2) {\n");
//			buf.append("        System.out.println(\"FOO\" + o1 + \" \\\"bar\\\" \" + o2);\n");
//			buf.append("    }\n");
//			buf.append("}\n");
//			String expected7= buf.toString();
//
//			assertExpectedExistInProposals(proposals, new String[] { expected1, expected2, expected3, expected4, expected5, expected6, expected7 });
//		} finally {
//			fJProject1.setOptions(oldOptions);
//		}
//	}

	@Test
	public void testConvertToMessageFormatStringConcat() throws Exception {

		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    public void foo(Object o1, Object o2) {\n");
		buf.append("        System.out.println(\"foo\" + \"\" + \" \\\"bar\\\" \");\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("A.java", buf.toString(), false, null);

		AssistContext context= getCorrectionContext(cu, buf.toString().indexOf("\" + \"\" + \""), 0);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 6);
		assertCorrectLabels(proposals);

		assertCommandIdDoesNotExist(proposals, QuickAssistProcessor.CONVERT_TO_MESSAGE_FORMAT_ID);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    public void foo(Object o1, Object o2) {\n");
		buf.append("        String string = \"foo\";\n");
		buf.append("        System.out.println(string + \"\" + \" \\\"bar\\\" \");\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    public void foo(Object o1, Object o2) {\n");
		buf.append("        String string = \"foo\";\n");
		buf.append("        System.out.println(string + \"\" + \" \\\"bar\\\" \");\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected2= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    private static final String FOO = \"foo\";\n");
		buf.append("\n");
		buf.append("    public void foo(Object o1, Object o2) {\n");
		buf.append("        System.out.println(FOO + \"\" + \" \\\"bar\\\" \");\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected3= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    public void foo(Object o1, Object o2) {\n");
		buf.append("        StringBuilder stringBuilder = new StringBuilder();\n");
		buf.append("        stringBuilder.append(\"foo\");\n");
		buf.append("        stringBuilder.append(\"\");\n");
		buf.append("        stringBuilder.append(\" \\\"bar\\\" \");\n");
		buf.append("        System.out.println(stringBuilder.toString());\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected4= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    public void foo(Object o1, Object o2) {\n");
		buf.append("        System.out.println((\"foo\" + \"\" + \" \\\"bar\\\" \"));\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected5= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    public void foo(Object o1, Object o2) {\n");
		buf.append("        System.out.println(\"foo \\\"bar\\\" \");\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected6= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    public void foo(Object o1, Object o2) {\n");
		buf.append("        System.out.println(\"FOO\" + \"\" + \" \\\"bar\\\" \");\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected7= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1, expected2, expected3, expected4, expected5, expected6/*, expected7*/ });
	}

//	public void testConvertToMessageFormatStringBoxing14() throws Exception {
//		Map oldOptions= fJProject1.getOptions(false);
//		Map newOptions= new HashMap(oldOptions);
//		JavaCore.setComplianceOptions(JavaCore.VERSION_1_4, newOptions);
//		fJProject1.setOptions(newOptions);
//
//		try {
//			IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
//			StringBuffer buf= new StringBuffer();
//			buf.append("package test1;\n");
//			buf.append("public class A {\n");
//			buf.append("    public void foo(Object o1, Object o2) {\n");
//			buf.append("        System.out.println(\"foo\" + 1 + \" \\\"bar\\\" \");\n");
//			buf.append("    }\n");
//			buf.append("}\n");
//			ICompilationUnit cu= pack1.createCompilationUnit("A.java", buf.toString(), false, null);
//
//			AssistContext context= getCorrectionContext(cu, buf.toString().indexOf("1 + \""), 0);
//			List proposals= collectAssists(context, false);
//
//			assertNumberOfProposals(proposals, 6);
//			assertCorrectLabels(proposals);
//
//			buf= new StringBuffer();
//			buf.append("package test1;\n");
//			buf.append("\n");
//			buf.append("import java.text.MessageFormat;\n");
//			buf.append("\n");
//			buf.append("public class A {\n");
//			buf.append("    public void foo(Object o1, Object o2) {\n");
//			buf.append("        System.out.println(MessageFormat.format(\"foo{0} \\\"bar\\\" \", new Object[]{new Integer(1)}));\n");
//			buf.append("    }\n");
//			buf.append("}\n");
//			String expected1= buf.toString();
//
//			buf= new StringBuffer();
//			buf.append("package test1;\n");
//			buf.append("public class A {\n");
//			buf.append("    public void foo(Object o1, Object o2) {\n");
//			buf.append("        int i = 1;\n");
//			buf.append("        System.out.println(\"foo\" + i + \" \\\"bar\\\" \");\n");
//			buf.append("    }\n");
//			buf.append("}\n");
//			String expected2= buf.toString();
//
//			buf= new StringBuffer();
//			buf.append("package test1;\n");
//			buf.append("public class A {\n");
//			buf.append("    public void foo(Object o1, Object o2) {\n");
//			buf.append("        int i = 1;\n");
//			buf.append("        System.out.println(\"foo\" + i + \" \\\"bar\\\" \");\n");
//			buf.append("    }\n");
//			buf.append("}\n");
//			String expected3= buf.toString();
//
//			buf= new StringBuffer();
//			buf.append("package test1;\n");
//			buf.append("public class A {\n");
//			buf.append("    private static final int _1 = 1;\n");
//			buf.append("\n");
//			buf.append("    public void foo(Object o1, Object o2) {\n");
//			buf.append("        System.out.println(\"foo\" + _1 + \" \\\"bar\\\" \");\n");
//			buf.append("    }\n");
//			buf.append("}\n");
//			String expected4= buf.toString();
//
//			buf= new StringBuffer();
//			buf.append("package test1;\n");
//			buf.append("public class A {\n");
//			buf.append("    public void foo(Object o1, Object o2) {\n");
//			buf.append("        StringBuffer stringBuffer = new StringBuffer();\n");
//			buf.append("        stringBuffer.append(\"foo\");\n");
//			buf.append("        stringBuffer.append(1);\n");
//			buf.append("        stringBuffer.append(\" \\\"bar\\\" \");\n");
//			buf.append("        System.out.println(stringBuffer.toString());\n");
//			buf.append("    }\n");
//			buf.append("}\n");
//			String expected5= buf.toString();
//
//			buf= new StringBuffer();
//			buf.append("package test1;\n");
//			buf.append("public class A {\n");
//			buf.append("    public void foo(Object o1, Object o2) {\n");
//			buf.append("        System.out.println((\"foo\" + 1 + \" \\\"bar\\\" \"));\n");
//			buf.append("    }\n");
//			buf.append("}\n");
//			String expected6= buf.toString();
//
//			assertExpectedExistInProposals(proposals, new String[] { expected1, expected2, expected3, expected4, expected5, expected6 });
//		} finally {
//			fJProject1.setOptions(oldOptions);
//		}
//	}

	@Test
	public void testConvertToMessageFormatStringBoxing15() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    public void foo(Object o1, Object o2) {\n");
		buf.append("        System.out.println(\"foo\" + 1 + \" \\\"bar\\\" \");\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("A.java", buf.toString(), false, null);

		AssistContext context= getCorrectionContext(cu, buf.toString().indexOf("\" + 1 + \""), 0);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 6);
		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("\n");
		buf.append("import java.text.MessageFormat;\n");
		buf.append("\n");
		buf.append("public class A {\n");
		buf.append("    public void foo(Object o1, Object o2) {\n");
		buf.append("        System.out.println(MessageFormat.format(\"foo{0} \\\"bar\\\" \", 1));\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    public void foo(Object o1, Object o2) {\n");
		buf.append("        String string = \"foo\";\n");
		buf.append("        System.out.println(string + 1 + \" \\\"bar\\\" \");\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected2= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    public void foo(Object o1, Object o2) {\n");
		buf.append("        String string = \"foo\";\n");
		buf.append("        System.out.println(string + 1 + \" \\\"bar\\\" \");\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected3= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    private static final String FOO = \"foo\";\n");
		buf.append("\n");
		buf.append("    public void foo(Object o1, Object o2) {\n");
		buf.append("        System.out.println(FOO + 1 + \" \\\"bar\\\" \");\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected4= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    public void foo(Object o1, Object o2) {\n");
		buf.append("        StringBuilder stringBuilder = new StringBuilder();\n");
		buf.append("        stringBuilder.append(\"foo\");\n");
		buf.append("        stringBuilder.append(1);\n");
		buf.append("        stringBuilder.append(\" \\\"bar\\\" \");\n");
		buf.append("        System.out.println(stringBuilder.toString());\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected5= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    public void foo(Object o1, Object o2) {\n");
		buf.append("        System.out.println((\"foo\" + 1 + \" \\\"bar\\\" \"));\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected6= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    public void foo(Object o1, Object o2) {\n");
		buf.append("        System.out.println(\"FOO\" + 1 + \" \\\"bar\\\" \");\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected7= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1, expected2, expected3, expected4, expected5, expected6/*, expected7*/ });
	}

	@Test
	public void testConvertToMessageFormat15() throws Exception {

		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    public void foo(Object o1, Object o2) {\n");
		buf.append("        System.out.println(\"foo\" + o1 + \" \\\"bar\\\" \" + o2);\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("A.java", buf.toString(), false, null);

		AssistContext context= getCorrectionContext(cu, buf.toString().indexOf("\" + o1 + \""), 0);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 6);
		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("\n");
		buf.append("import java.text.MessageFormat;\n");
		buf.append("\n");
		buf.append("public class A {\n");
		buf.append("    public void foo(Object o1, Object o2) {\n");
		buf.append("        System.out.println(MessageFormat.format(\"foo{0} \\\"bar\\\" {1}\", o1, o2));\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    public void foo(Object o1, Object o2) {\n");
		buf.append("        String string = \"foo\";\n");
		buf.append("        System.out.println(string + o1 + \" \\\"bar\\\" \" + o2);\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected2= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    public void foo(Object o1, Object o2) {\n");
		buf.append("        String string = \"foo\";\n");
		buf.append("        System.out.println(string + o1 + \" \\\"bar\\\" \" + o2);\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected3= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    private static final String FOO = \"foo\";\n");
		buf.append("\n");
		buf.append("    public void foo(Object o1, Object o2) {\n");
		buf.append("        System.out.println(FOO + o1 + \" \\\"bar\\\" \" + o2);\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected4= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    public void foo(Object o1, Object o2) {\n");
		buf.append("        StringBuilder stringBuilder = new StringBuilder();\n");
		buf.append("        stringBuilder.append(\"foo\");\n");
		buf.append("        stringBuilder.append(o1);\n");
		buf.append("        stringBuilder.append(\" \\\"bar\\\" \");\n");
		buf.append("        stringBuilder.append(o2);\n");
		buf.append("        System.out.println(stringBuilder.toString());\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected5= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    public void foo(Object o1, Object o2) {\n");
		buf.append("        System.out.println((\"foo\" + o1 + \" \\\"bar\\\" \" + o2));\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected6= buf.toString();

//		buf= new StringBuffer();
//		buf.append("package test1;\n");
//		buf.append("public class A {\n");
//		buf.append("    public void foo(Object o1, Object o2) {\n");
//		buf.append("        System.out.println(\"FOO\" + o1 + \" \\\"bar\\\" \" + o2);\n");
//		buf.append("    }\n");
//		buf.append("}\n");
//		String expected7= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1, expected2, expected3, expected4, expected5, expected6/*, expected7*/ });
	}

	@Test
	public void testConvertToMessageFormatApostrophe() throws Exception {

		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    public void foo(Object o1, Object o2) {\n");
		buf.append("        System.out.println(\"foo'\" + o1 + \"' \\\"bar\\\" \" + o2);\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("A.java", buf.toString(), false, null);

		AssistContext context= getCorrectionContext(cu, buf.toString().indexOf("\" + o1 + \""), 0);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 6);
		assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("\n");
		buf.append("import java.text.MessageFormat;\n");
		buf.append("\n");
		buf.append("public class A {\n");
		buf.append("    public void foo(Object o1, Object o2) {\n");
		buf.append("        System.out.println(MessageFormat.format(\"foo''{0}'' \\\"bar\\\" {1}\", o1, o2));\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    public void foo(Object o1, Object o2) {\n");
		buf.append("        String string = \"foo'\";\n");
		buf.append("        System.out.println(string + o1 + \"' \\\"bar\\\" \" + o2);\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected2= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    public void foo(Object o1, Object o2) {\n");
		buf.append("        String string = \"foo'\";\n");
		buf.append("        System.out.println(string + o1 + \"' \\\"bar\\\" \" + o2);\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected3= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    private static final String FOO = \"foo'\";\n");
		buf.append("\n");
		buf.append("    public void foo(Object o1, Object o2) {\n");
		buf.append("        System.out.println(FOO + o1 + \"' \\\"bar\\\" \" + o2);\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected4= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    public void foo(Object o1, Object o2) {\n");
		buf.append("        StringBuilder stringBuilder = new StringBuilder();\n");
		buf.append("        stringBuilder.append(\"foo'\");\n");
		buf.append("        stringBuilder.append(o1);\n");
		buf.append("        stringBuilder.append(\"' \\\"bar\\\" \");\n");
		buf.append("        stringBuilder.append(o2);\n");
		buf.append("        System.out.println(stringBuilder.toString());\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected5= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    public void foo(Object o1, Object o2) {\n");
		buf.append("        System.out.println((\"foo'\" + o1 + \"' \\\"bar\\\" \" + o2));\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected6= buf.toString();

//		buf= new StringBuffer();
//		buf.append("package test1;\n");
//		buf.append("public class A {\n");
//		buf.append("    public void foo(Object o1, Object o2) {\n");
//		buf.append("        System.out.println(\"FOO'\" + o1 + \"' \\\"bar\\\" \" + o2);\n");
//		buf.append("    }\n");
//		buf.append("}\n");
//		String expected7= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1, expected2, expected3, expected4, expected5, expected6/*, expected7 */});
	}

	@Test
	public void testConvertToMessageFormatExtendedOperands() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    public void foo() {\n");
		buf.append("        String s2= \"a\" + \"b\" + 3L + \"c\" + (4-2) + \"d\" + \"e\" + \"f\";\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("A.java", buf.toString(), false, null);
		
		AssistContext context= getCorrectionContext(cu, buf.toString().indexOf(" + "), 0);
		List proposals= collectAssists(context, false);
		
		assertNumberOfProposals(proposals,7);
		assertCorrectLabels(proposals);
		
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("\n");
		buf.append("import java.text.MessageFormat;\n");
		buf.append("\n");
		buf.append("public class A {\n");
		buf.append("    public void foo() {\n");
		buf.append("        String s2= MessageFormat.format(\"ab{0}c{1}def\", 3L, (4-2));\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();
		
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    public void foo() {\n");
		buf.append("        StringBuilder stringBuilder = new StringBuilder();\n");
		buf.append("        stringBuilder.append(\"a\");\n");
		buf.append("        stringBuilder.append(\"b\");\n");
		buf.append("        stringBuilder.append(3L);\n");
		buf.append("        stringBuilder.append(\"c\");\n");
		buf.append("        stringBuilder.append((4-2));\n");
		buf.append("        stringBuilder.append(\"d\");\n");
		buf.append("        stringBuilder.append(\"e\");\n");
		buf.append("        stringBuilder.append(\"f\");\n");
		buf.append("        String s2= stringBuilder.toString();\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected2= buf.toString();
		
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    public void foo() {\n");
		buf.append("        String string = \"a\";\n");
		buf.append("        String s2= string + \"b\" + 3L + \"c\" + (4-2) + \"d\" + \"e\" + \"f\";\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected3= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    private static final String A = \"a\";\n");
		buf.append("\n");
		buf.append("    public void foo() {\n");
		buf.append("        String s2= A + \"b\" + 3L + \"c\" + (4-2) + \"d\" + \"e\" + \"f\";\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected4= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    public void foo() {\n");
		buf.append("        String s2= (\"a\" + \"b\") + 3L + \"c\" + (4-2) + \"d\" + \"e\" + \"f\";\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected5= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    public void foo() {\n");
		buf.append("        String s2= \"ab\" + 3L + \"c\" + (4-2) + \"d\" + \"e\" + \"f\";\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected6= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    public void foo() {\n");
		buf.append("        String string = \"a\";\n");
		buf.append("        String s2= string + \"b\" + 3L + \"c\" + (4-2) + \"d\" + \"e\" + \"f\";\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected7= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    public void foo() {\n");
		buf.append("        String s2= \"A\" + \"b\" + 3L + \"c\" + (4-2) + \"d\" + \"e\" + \"f\";\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected8= buf.toString();

		assertExpectedExistInProposals(proposals, new String[] { expected1, expected2, expected3, expected4, expected5, expected6, expected7/*, expected8*/ });
	}

	@Test
	public void testMissingEnumConstantsInCase1() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("p", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package p;\n");
		buf.append("\n");
		buf.append("public class E {\n");
		buf.append("    enum MyEnum {\n");
		buf.append("        X1, X2, X3\n");
		buf.append("    }\n");
		buf.append("    \n");
		buf.append("    public void foo(MyEnum x) {\n");
		buf.append("        switch (x) {\n");
		buf.append("        \n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		AssistContext context= getCorrectionContext(cu, buf.toString().indexOf("switch"), 0);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);
		assertNumberOfProposals(proposals, 3);

		String[] expected= new String[3];
		buf= new StringBuffer();
		buf.append("package p;\n");
		buf.append("\n");
		buf.append("public class E {\n");
		buf.append("    enum MyEnum {\n");
		buf.append("        X1, X2, X3\n");
		buf.append("    }\n");
		buf.append("    \n");
		buf.append("    public void foo(MyEnum x) {\n");
		buf.append("        switch (x) {\n");
		buf.append("            default :\n");
		buf.append("                break;\n");
		buf.append("        \n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		expected[0]= buf.toString();

		buf= new StringBuffer();
		buf.append("package p;\n");
		buf.append("\n");
		buf.append("public class E {\n");
		buf.append("    enum MyEnum {\n");
		buf.append("        X1, X2, X3\n");
		buf.append("    }\n");
		buf.append("    \n");
		buf.append("    public void foo(MyEnum x) {\n");
		buf.append("        switch (x) {\n");
		buf.append("            case X1 :\n");
		buf.append("                break;\n");
		buf.append("            case X2 :\n");
		buf.append("                break;\n");
		buf.append("            case X3 :\n");
		buf.append("                break;\n");
		buf.append("            default :\n");
		buf.append("                break;\n");
		buf.append("        \n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		expected[1]= buf.toString();

		buf= new StringBuffer();
		buf.append("package p;\n");
		buf.append("\n");
		buf.append("public class E {\n");
		buf.append("    enum MyEnum {\n");
		buf.append("        X1, X2, X3\n");
		buf.append("    }\n");
		buf.append("    \n");
		buf.append("    public void foo(MyEnum x) {\n");
		buf.append("    }\n");
		buf.append("}\n");
		expected[2]= buf.toString();

		assertExpectedExistInProposals(proposals, expected);
	}

	@Test
	public void testMissingEnumConstantsInCase2() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("p", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package p;\n");
		buf.append("\n");
		buf.append("public class E {\n");
		buf.append("    enum MyEnum {\n");
		buf.append("        X1, X2, X3\n");
		buf.append("    }\n");
		buf.append("    \n");
		buf.append("    public void foo(MyEnum x) {\n");
		buf.append("        switch (x) {\n");
		buf.append("            case X1 :\n");
		buf.append("                break;\n");
		buf.append("        \n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		AssistContext context= getCorrectionContext(cu, buf.toString().indexOf("break;") + 7, 0);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);
		assertNumberOfProposals(proposals, 3);

		String[] expected= new String[3];
		buf= new StringBuffer();
		buf.append("package p;\n");
		buf.append("\n");
		buf.append("public class E {\n");
		buf.append("    enum MyEnum {\n");
		buf.append("        X1, X2, X3\n");
		buf.append("    }\n");
		buf.append("    \n");
		buf.append("    public void foo(MyEnum x) {\n");
		buf.append("        switch (x) {\n");
		buf.append("            case X1 :\n");
		buf.append("                break;\n");
		buf.append("            default :\n");
		buf.append("                break;\n");
		buf.append("        \n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		expected[0]= buf.toString();

		buf= new StringBuffer();
		buf.append("package p;\n");
		buf.append("\n");
		buf.append("public class E {\n");
		buf.append("    enum MyEnum {\n");
		buf.append("        X1, X2, X3\n");
		buf.append("    }\n");
		buf.append("    \n");
		buf.append("    public void foo(MyEnum x) {\n");
		buf.append("        switch (x) {\n");
		buf.append("            case X1 :\n");
		buf.append("                break;\n");
		buf.append("            case X2 :\n");
		buf.append("                break;\n");
		buf.append("            case X3 :\n");
		buf.append("                break;\n");
		buf.append("            default :\n");
		buf.append("                break;\n");
		buf.append("        \n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		expected[1]= buf.toString();

		buf= new StringBuffer();
		buf.append("package p;\n");
		buf.append("\n");
		buf.append("public class E {\n");
		buf.append("    enum MyEnum {\n");
		buf.append("        X1, X2, X3\n");
		buf.append("    }\n");
		buf.append("    \n");
		buf.append("    public void foo(MyEnum x) {\n");
		buf.append("        if (x == MyEnum.X1) {\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		expected[2]= buf.toString();

		assertExpectedExistInProposals(proposals, expected);
	}

	@Test
	public void testMissingEnumConstantsInCase3() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("p", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package p;\n");
		buf.append("\n");
		buf.append("public class E {\n");
		buf.append("    enum MyEnum {\n");
		buf.append("        X1, X2, X3\n");
		buf.append("    }\n");
		buf.append("    \n");
		buf.append("    public void foo(MyEnum x) {\n");
		buf.append("        switch (x) {\n");
		buf.append("            case X1 :\n");
		buf.append("                break;\n");
		buf.append("        \n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		AssistContext context= getCorrectionContext(cu, buf.toString().indexOf("case"), 0);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);
		assertNumberOfProposals(proposals, 2);

		String[] expected= new String[2];
		buf= new StringBuffer();
		buf.append("package p;\n");
		buf.append("\n");
		buf.append("public class E {\n");
		buf.append("    enum MyEnum {\n");
		buf.append("        X1, X2, X3\n");
		buf.append("    }\n");
		buf.append("    \n");
		buf.append("    public void foo(MyEnum x) {\n");
		buf.append("        switch (x) {\n");
		buf.append("            case X1 :\n");
		buf.append("                break;\n");
		buf.append("            default :\n");
		buf.append("                break;\n");
		buf.append("        \n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		expected[0]= buf.toString();

		buf= new StringBuffer();
		buf.append("package p;\n");
		buf.append("\n");
		buf.append("public class E {\n");
		buf.append("    enum MyEnum {\n");
		buf.append("        X1, X2, X3\n");
		buf.append("    }\n");
		buf.append("    \n");
		buf.append("    public void foo(MyEnum x) {\n");
		buf.append("        switch (x) {\n");
		buf.append("            case X1 :\n");
		buf.append("                break;\n");
		buf.append("            case X2 :\n");
		buf.append("                break;\n");
		buf.append("            case X3 :\n");
		buf.append("                break;\n");
		buf.append("            default :\n");
		buf.append("                break;\n");
		buf.append("        \n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		expected[1]= buf.toString();

		assertExpectedExistInProposals(proposals, expected);
	}

	@Test
	public void testMissingEnumConstantsInCase4() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("p", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package p;\n");
		buf.append("\n");
		buf.append("public class E {\n");
		buf.append("    enum MyEnum {\n");
		buf.append("        X1, X2, X3\n");
		buf.append("    }\n");
		buf.append("    \n");
		buf.append("    public void foo(MyEnum x) {\n");
		buf.append("        switch (x) {\n");
		buf.append("            case X1 :\n");
		buf.append("                break;\n");
		buf.append("            default :\n");
		buf.append("                break;\n");
		buf.append("        \n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		AssistContext context= getCorrectionContext(cu, buf.toString().indexOf("default"), 0);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);
		assertNumberOfProposals(proposals, 1);

		String[] expected= new String[1];
		buf= new StringBuffer();
		buf.append("package p;\n");
		buf.append("\n");
		buf.append("public class E {\n");
		buf.append("    enum MyEnum {\n");
		buf.append("        X1, X2, X3\n");
		buf.append("    }\n");
		buf.append("    \n");
		buf.append("    public void foo(MyEnum x) {\n");
		buf.append("        switch (x) {\n");
		buf.append("            case X1 :\n");
		buf.append("                break;\n");
		buf.append("            case X2 :\n");
		buf.append("            case X3 :\n");
		buf.append("            default :\n");
		buf.append("                break;\n");
		buf.append("        \n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		expected[0]= buf.toString();

		assertExpectedExistInProposals(proposals, expected);
	}

	@Test
	public void testMissingEnumConstantsInCase5() throws Exception {
		//https://bugs.eclipse.org/bugs/show_bug.cgi?id=372840
		IPackageFragment pack1= fSourceFolder.createPackageFragment("p", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package p;\n");
		buf.append("\n");
		buf.append("public class E {\n");
		buf.append("    enum MyEnum {\n");
		buf.append("        X1, X2, X3\n");
		buf.append("    }\n");
		buf.append("    \n");
		buf.append("    public void foo(MyEnum x) {\n");
		buf.append("        switch (x) {\n");
		buf.append("            case X1 :\n");
		buf.append("                break;\n");
		buf.append("            case X2 :\n");
		buf.append("                break;\n");
		buf.append("            case X3 :\n");
		buf.append("                break;\n");
		buf.append("        \n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		AssistContext context= getCorrectionContext(cu, buf.toString().indexOf("switch"), 0);
		List proposals= collectAssists(context, false);

		assertCorrectLabels(proposals);
		assertNumberOfProposals(proposals, 2);

		String[] expected= new String[2];
		buf= new StringBuffer();
		buf.append("package p;\n");
		buf.append("\n");
		buf.append("public class E {\n");
		buf.append("    enum MyEnum {\n");
		buf.append("        X1, X2, X3\n");
		buf.append("    }\n");
		buf.append("    \n");
		buf.append("    public void foo(MyEnum x) {\n");
		buf.append("        switch (x) {\n");
		buf.append("            case X1 :\n");
		buf.append("                break;\n");
		buf.append("            case X2 :\n");
		buf.append("                break;\n");
		buf.append("            case X3 :\n");
		buf.append("                break;\n");
		buf.append("            default :\n");
		buf.append("                break;\n");
		buf.append("        \n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		expected[0]= buf.toString();

		buf= new StringBuffer();
		buf.append("package p;\n");
		buf.append("\n");
		buf.append("public class E {\n");
		buf.append("    enum MyEnum {\n");
		buf.append("        X1, X2, X3\n");
		buf.append("    }\n");
		buf.append("    \n");
		buf.append("    public void foo(MyEnum x) {\n");
		buf.append("        if (x == MyEnum.X1) {\n");
		buf.append("        } else if (x == MyEnum.X2) {\n");
		buf.append("        } else if (x == MyEnum.X3) {\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		expected[1]= buf.toString();

		assertExpectedExistInProposals(proposals, expected);
	}

	@Test
	public void testConvertEnhancedForArray01() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public static void main(String... args) {\n");
		buf.append("        for (final @Deprecated String arg : args) {\n");
		buf.append("            System.out.print(arg);\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		AssistContext context= getCorrectionContext(cu, buf.toString().indexOf("for"), 0);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 3);
		assertCorrectLabels(proposals);

		String[] expected= new String[3];
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public static void main(String... args) {\n");
		buf.append("        for (int i = 0; i < args.length; i++) {\n");
		buf.append("            final @Deprecated String arg = args[i];\n");
		buf.append("            System.out.print(arg);\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		expected[0]= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public static void main(String... args) {\n");
		buf.append("        System.out.print(arg);\n");
		buf.append("    }\n");
		buf.append("}\n");
		expected[1]= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public static void main(String... args) {\n");
		buf.append("        for (final @Deprecated String arg : args)\n");
		buf.append("            System.out.print(arg);\n");
		buf.append("    }\n");
		buf.append("}\n");
		expected[2]= buf.toString();

		assertExpectedExistInProposals(proposals, expected);
	}

	@Test
	public void testConvertEnhancedForArray02() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(int[][][] ints) {\n");
		buf.append("        outer: for (int[] is[] : ints.clone ()) {\n");
		buf.append("            //convert this\n");
		buf.append("            for (int i : is) {\n");
		buf.append("                System.out.print(i);\n");
		buf.append("                System.out.print(\", \");\n");
		buf.append("            }\n");
		buf.append("            System.out.println();\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		AssistContext context= getCorrectionContext(cu, buf.toString().indexOf("for"), 0);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 2);
		assertCorrectLabels(proposals);

		String[] expected= new String[2];
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(int[][][] ints) {\n");
		buf.append("        int[][][] clone = ints.clone ();\n");
		buf.append("        outer: for (int j = 0; j < clone.length; j++) {\n");
		buf.append("            int[] is[] = clone[j];\n");
		buf.append("            //convert this\n");
		buf.append("            for (int i : is) {\n");
		buf.append("                System.out.print(i);\n");
		buf.append("                System.out.print(\", \");\n");
		buf.append("            }\n");
		buf.append("            System.out.println();\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		expected[0]= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(int[][][] ints) {\n");
		buf.append("        outer: //convert this\n");
		buf.append("        for (int i : is) {\n");
		buf.append("            System.out.print(i);\n");
		buf.append("            System.out.print(\", \");\n");
		buf.append("        }\n");
		buf.append("        System.out.println();\n");
		buf.append("    }\n");
		buf.append("}\n");
		expected[1]= buf.toString();

		assertExpectedExistInProposals(proposals, expected);
	}

	@Test
	public void testConvertEnhancedForList01() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.Arrays;\n");
		buf.append("import java.util.List;\n");
		buf.append("public class E {\n");
		buf.append("    void foo() {\n");
		buf.append("        for (Number number : getNums()) {\n");
		buf.append("            System.out.println(number.doubleValue());\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("    private List<? extends Number> getNums() {\n");
		buf.append("        return Arrays.asList(1, 2.34, 0xFFFF);\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		AssistContext context= getCorrectionContext(cu, buf.toString().indexOf(":"), 0);
		List proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 4);
		assertCorrectLabels(proposals);

		String[] expected= new String[4];
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.Arrays;\n");
		buf.append("import java.util.List;\n");
		buf.append("public class E {\n");
		buf.append("    void foo() {\n");
		buf.append("        List<? extends Number> nums = getNums();\n");
		buf.append("        for (int i = 0; i < nums.size(); i++) {\n");
		buf.append("            Number number = nums.get(i);\n");
		buf.append("            System.out.println(number.doubleValue());\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("    private List<? extends Number> getNums() {\n");
		buf.append("        return Arrays.asList(1, 2.34, 0xFFFF);\n");
		buf.append("    }\n");
		buf.append("}\n");
		expected[0]= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.Arrays;\n");
		buf.append("import java.util.Iterator;\n");
		buf.append("import java.util.List;\n");
		buf.append("public class E {\n");
		buf.append("    void foo() {\n");
		buf.append("        for (Iterator<? extends Number> iterator = getNums().iterator(); iterator\n");
		buf.append("                .hasNext();) {\n");
		buf.append("            Number number = iterator.next();\n");
		buf.append("            System.out.println(number.doubleValue());\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("    private List<? extends Number> getNums() {\n");
		buf.append("        return Arrays.asList(1, 2.34, 0xFFFF);\n");
		buf.append("    }\n");
		buf.append("}\n");
		expected[1]= buf.toString();
		
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.Arrays;\n");
		buf.append("import java.util.List;\n");
		buf.append("public class E {\n");
		buf.append("    void foo() {\n");
		buf.append("        System.out.println(number.doubleValue());\n");
		buf.append("    }\n");
		buf.append("    private List<? extends Number> getNums() {\n");
		buf.append("        return Arrays.asList(1, 2.34, 0xFFFF);\n");
		buf.append("    }\n");
		buf.append("}\n");
		expected[2]= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.Arrays;\n");
		buf.append("import java.util.List;\n");
		buf.append("public class E {\n");
		buf.append("    void foo() {\n");
		buf.append("        for (Number number : getNums())\n");
		buf.append("            System.out.println(number.doubleValue());\n");
		buf.append("    }\n");
		buf.append("    private List<? extends Number> getNums() {\n");
		buf.append("        return Arrays.asList(1, 2.34, 0xFFFF);\n");
		buf.append("    }\n");
		buf.append("}\n");
		expected[3]= buf.toString();

		assertExpectedExistInProposals(proposals, expected);
	}

	@Test
	public void testConvertEnhancedForCollection01() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.Collection;\n");
		buf.append("import java.util.List;\n");
		buf.append("public class E {\n");
		buf.append("    void foo(Collection<? extends List<? extends Number>> allNums) {\n");
		buf.append("        for (List<? extends Number> nums : allNums) {\n");
		buf.append("            for (Number number : nums) {\n");
		buf.append("                System.out.println(number.doubleValue());\n");
		buf.append("            }\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);
	
		AssistContext context= getCorrectionContext(cu, buf.toString().indexOf("for"), 0);
		List proposals= collectAssists(context, false);
	
		assertNumberOfProposals(proposals, 3);
		assertCorrectLabels(proposals);
	
		String[] expected= new String[3];
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.Collection;\n");
		buf.append("import java.util.Iterator;\n");
		buf.append("import java.util.List;\n");
		buf.append("public class E {\n");
		buf.append("    void foo(Collection<? extends List<? extends Number>> allNums) {\n");
		buf.append("        for (Iterator<? extends List<? extends Number>> iterator = allNums\n");
		buf.append("                .iterator(); iterator.hasNext();) {\n");
		buf.append("            List<? extends Number> nums = iterator.next();\n");
		buf.append("            for (Number number : nums) {\n");
		buf.append("                System.out.println(number.doubleValue());\n");
		buf.append("            }\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		expected[0]= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.Collection;\n");
		buf.append("import java.util.List;\n");
		buf.append("public class E {\n");
		buf.append("    void foo(Collection<? extends List<? extends Number>> allNums) {\n");
		buf.append("        for (Number number : nums) {\n");
		buf.append("            System.out.println(number.doubleValue());\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		expected[1]= buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.Collection;\n");
		buf.append("import java.util.List;\n");
		buf.append("public class E {\n");
		buf.append("    void foo(Collection<? extends List<? extends Number>> allNums) {\n");
		buf.append("        for (List<? extends Number> nums : allNums)\n");
		buf.append("            for (Number number : nums) {\n");
		buf.append("                System.out.println(number.doubleValue());\n");
		buf.append("            }\n");
		buf.append("    }\n");
		buf.append("}\n");
		expected[2]= buf.toString();
	
		assertExpectedExistInProposals(proposals, expected);
	}

	@Test
	public void testGenerateForSimple() throws Exception {
//		if (LocalCorrectionsQuickFixTest.BUG_430818)
//			return;
		
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.Collection;\n");
		buf.append("public class E {\n");
		buf.append("    void foo(Collection<String> collection) {\n");
		buf.append("        collection\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		Map saveOptions= fJProject1.getOptions(false);
		Map newOptions= new HashMap(saveOptions);
		newOptions.put(DefaultCodeFormatterConstants.FORMATTER_PUT_EMPTY_STATEMENT_ON_NEW_LINE, "true");
		try {
			fJProject1.setOptions(newOptions);
			String selection= "collection";
			AssistContext context= getCorrectionContext(cu, buf.toString().lastIndexOf(selection) + selection.length(), 0);
			List proposals= collectAssists(context, false);

			assertNumberOfProposals(proposals, 4);
			assertCorrectLabels(proposals);

			String[] expected= new String[2];
			buf= new StringBuffer();
			buf.append("package test1;\n");
			buf.append("import java.util.Collection;\n");
			buf.append("public class E {\n");
			buf.append("    void foo(Collection<String> collection) {\n");
			buf.append("        for (String string : collection) {\n");
			buf.append("            \n");
			buf.append("        }\n");
			buf.append("    }\n");
			buf.append("}\n");
			expected[0]= buf.toString();

			buf= new StringBuffer();
			buf.append("package test1;\n");
			buf.append("import java.util.Collection;\n");
			buf.append("import java.util.Iterator;\n");
			buf.append("public class E {\n");
			buf.append("    void foo(Collection<String> collection) {\n");
			buf.append("        for (Iterator<String> iterator = collection.iterator(); iterator.hasNext();) {\n");
			buf.append("            String string = iterator.next();\n");
			buf.append("            \n");
			buf.append("        }\n");
			buf.append("    }\n");
			buf.append("}\n");
			expected[1]= buf.toString();

			assertExpectedExistInProposals(proposals, expected);
		} finally {
			fJProject1.setOptions(saveOptions);
		}
	}

	@Test
	public void testGenerateForWithSemicolon() throws Exception {
//		if (LocalCorrectionsQuickFixTest.BUG_430818)
//			return;
		
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.Collection;\n");
		buf.append("public class E {\n");
		buf.append("    void foo(Collection<String> collection) {\n");
		buf.append("        collection;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		Map saveOptions= fJProject1.getOptions(false);
		Map newOptions= new HashMap(saveOptions);
		newOptions.put(DefaultCodeFormatterConstants.FORMATTER_PUT_EMPTY_STATEMENT_ON_NEW_LINE, "true");
		try {
			fJProject1.setOptions(newOptions);
			String selection= "collection;";
			AssistContext context= getCorrectionContext(cu, buf.toString().lastIndexOf(selection) + selection.length(), 0);
			List proposals= collectAssists(context, false);

			assertNumberOfProposals(proposals, 2);
			assertCorrectLabels(proposals);

			String[] expected= new String[2];
			buf= new StringBuffer();
			buf.append("package test1;\n");
			buf.append("import java.util.Collection;\n");
			buf.append("public class E {\n");
			buf.append("    void foo(Collection<String> collection) {\n");
			buf.append("        for (String string : collection) {\n");
			buf.append("            \n");
			buf.append("        }\n");
			buf.append("    }\n");
			buf.append("}\n");
			expected[0]= buf.toString();

			buf= new StringBuffer();
			buf.append("package test1;\n");
			buf.append("import java.util.Collection;\n");
			buf.append("import java.util.Iterator;\n");
			buf.append("public class E {\n");
			buf.append("    void foo(Collection<String> collection) {\n");
			buf.append("        for (Iterator<String> iterator = collection.iterator(); iterator.hasNext();) {\n");
			buf.append("            String string = iterator.next();\n");
			buf.append("            \n");
			buf.append("        }\n");
			buf.append("    }\n");
			buf.append("}\n");
			expected[1]= buf.toString();

			assertExpectedExistInProposals(proposals, expected);
		} finally {
			fJProject1.setOptions(saveOptions);
		}
	}

	@Test
	public void testGenerateForMethodInvocation() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.Map;\n");
		buf.append("public class E {\n");
		buf.append("    void foo(Map<String, String> map) {\n");
		buf.append("        map.keySet()\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		Map saveOptions= fJProject1.getOptions(false);
		Map newOptions= new HashMap(saveOptions);
		newOptions.put(DefaultCodeFormatterConstants.FORMATTER_PUT_EMPTY_STATEMENT_ON_NEW_LINE, "true");
		try {
			fJProject1.setOptions(newOptions);
			String selection= "keySet()";
			AssistContext context= getCorrectionContext(cu, buf.toString().indexOf(selection) + selection.length(), 0);
			List proposals= collectAssists(context, false);

			assertNumberOfProposals(proposals, 6);
			assertCorrectLabels(proposals);

			String[] expected= new String[2];
			buf= new StringBuffer();
			buf.append("package test1;\n");
			buf.append("import java.util.Map;\n");
			buf.append("public class E {\n");
			buf.append("    void foo(Map<String, String> map) {\n");
			buf.append("        for (String string : map.keySet()) {\n");
			buf.append("            \n");
			buf.append("        }\n");
			buf.append("    }\n");
			buf.append("}\n");
			expected[0]= buf.toString();

			buf= new StringBuffer();
			buf.append("package test1;\n");
			buf.append("import java.util.Iterator;\n");
			buf.append("import java.util.Map;\n");
			buf.append("public class E {\n");
			buf.append("    void foo(Map<String, String> map) {\n");
			buf.append("        for (Iterator<String> iterator = map.keySet().iterator(); iterator\n");
			buf.append("                .hasNext();) {\n");
			buf.append("            String string = iterator.next();\n");
			buf.append("            \n");
			buf.append("        }\n");
			buf.append("    }\n");
			buf.append("}\n");
			expected[1]= buf.toString();

			assertExpectedExistInProposals(proposals, expected);
		} finally {
			fJProject1.setOptions(saveOptions);
		}
	}

	@Test
	public void testGenerateForComplexParametrization() throws Exception {
//		if (LocalCorrectionsQuickFixTest.BUG_430818)
//			return;
		
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.LinkedList;\n");
		buf.append("public class E {\n");
		buf.append("    void foo(MySecondOwnIterable collection) {\n");
		buf.append("        collection\n");
		buf.append("    }\n");
		buf.append("private class MyFirstOwnIterable<T, K> extends LinkedList<K>{}");
		buf.append("private class MySecondOwnIterable extends MyFirstOwnIterable<Integer, String>{}");
		buf.append("}\n");

		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		Map saveOptions= fJProject1.getOptions(false);
		Map newOptions= new HashMap(saveOptions);
		newOptions.put(DefaultCodeFormatterConstants.FORMATTER_PUT_EMPTY_STATEMENT_ON_NEW_LINE, "true");
		try {
			fJProject1.setOptions(newOptions);
			String selection= "collection";
			AssistContext context= getCorrectionContext(cu, buf.toString().lastIndexOf(selection) + selection.length(), 0);
			List proposals= collectAssists(context, false);

			assertNumberOfProposals(proposals, 5);
			assertCorrectLabels(proposals);

			String[] expected= new String[3];
			buf= new StringBuffer();
			buf.append("package test1;\n");
			buf.append("import java.util.LinkedList;\n");
			buf.append("public class E {\n");
			buf.append("    void foo(MySecondOwnIterable collection) {\n");
			buf.append("        for (String string : collection) {\n");
			buf.append("            \n");
			buf.append("        }\n");
			buf.append("    }\n");
			buf.append("private class MyFirstOwnIterable<T, K> extends LinkedList<K>{}");
			buf.append("private class MySecondOwnIterable extends MyFirstOwnIterable<Integer, String>{}");
			buf.append("}\n");
			expected[0]= buf.toString();

			buf= new StringBuffer();
			buf.append("package test1;\n");
			buf.append("import java.util.Iterator;\n");
			buf.append("import java.util.LinkedList;\n");
			buf.append("public class E {\n");
			buf.append("    void foo(MySecondOwnIterable collection) {\n");
			buf.append("        for (Iterator<String> iterator = collection.iterator(); iterator.hasNext();) {\n");
			buf.append("            String string = iterator.next();\n");
			buf.append("            \n");
			buf.append("        }\n");
			buf.append("    }\n");
			buf.append("private class MyFirstOwnIterable<T, K> extends LinkedList<K>{}");
			buf.append("private class MySecondOwnIterable extends MyFirstOwnIterable<Integer, String>{}");
			buf.append("}\n");
			expected[1]= buf.toString();
			
			buf= new StringBuffer();
			buf.append("package test1;\n");
			buf.append("import java.util.LinkedList;\n");
			buf.append("public class E {\n");
			buf.append("    void foo(MySecondOwnIterable collection) {\n");
			buf.append("        for (int i = 0; i < collection.size(); i++) {\n");
			buf.append("            String string = collection.get(i);\n");
			buf.append("            \n");
			buf.append("        }\n");
			buf.append("    }\n");
			buf.append("private class MyFirstOwnIterable<T, K> extends LinkedList<K>{}");
			buf.append("private class MySecondOwnIterable extends MyFirstOwnIterable<Integer, String>{}");
			buf.append("}\n");
			expected[2]= buf.toString();

			assertExpectedExistInProposals(proposals, expected);
		} finally {
			fJProject1.setOptions(saveOptions);
		}
	}

	@Test
	public void testGenerateForGenerics() throws Exception {
//		if (LocalCorrectionsQuickFixTest.BUG_430818)
//			return;
		
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.Collection;\n");
		buf.append("import java.util.Date;\n");
		buf.append("public class E {\n");
		buf.append("    void <T extends Date> foo(Collection<T> collection) {\n");
		buf.append("        collection\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		Map saveOptions= fJProject1.getOptions(false);
		Map newOptions= new HashMap(saveOptions);
		newOptions.put(DefaultCodeFormatterConstants.FORMATTER_PUT_EMPTY_STATEMENT_ON_NEW_LINE, "true");
		try {
			fJProject1.setOptions(newOptions);
			String selection= "collection";
			AssistContext context= getCorrectionContext(cu, buf.toString().lastIndexOf(selection) + selection.length(), 0);
			List proposals= collectAssists(context, false);

			assertNumberOfProposals(proposals, 4);
			assertCorrectLabels(proposals);

			String[] expected= new String[2];
			buf= new StringBuffer();
			buf.append("package test1;\n");
			buf.append("import java.util.Collection;\n");
			buf.append("import java.util.Date;\n");
			buf.append("public class E {\n");
			buf.append("    void <T extends Date> foo(Collection<T> collection) {\n");
			buf.append("        for (T t : collection) {\n");
			buf.append("            \n");
			buf.append("        }\n");
			buf.append("    }\n");
			buf.append("}\n");
			expected[0]= buf.toString();

			buf= new StringBuffer();
			buf.append("package test1;\n");
			buf.append("import java.util.Collection;\n");
			buf.append("import java.util.Date;\n");
			buf.append("import java.util.Iterator;\n");
			buf.append("public class E {\n");
			buf.append("    void <T extends Date> foo(Collection<T> collection) {\n");
			buf.append("        for (Iterator<T> iterator = collection.iterator(); iterator.hasNext();) {\n");
			buf.append("            T t = iterator.next();\n");
			buf.append("            \n");
			buf.append("        }\n");
			buf.append("    }\n");
			buf.append("}\n");
			expected[1]= buf.toString();

			assertExpectedExistInProposals(proposals, expected);
		} finally {
			fJProject1.setOptions(saveOptions);
		}
	}

	@Test
	public void testGenerateForComplexGenerics() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.List;\n");
		buf.append("public class E {\n");
		buf.append("    void foo() {\n");
		buf.append("        getIterable()\n");
		buf.append("    }\n");
		buf.append("    <T extends Iterable<? super Number> & Comparable<Number>> Iterable<T> getIterable() {\n");
		buf.append("        return null;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		Map saveOptions= fJProject1.getOptions(false);
		Map newOptions= new HashMap(saveOptions);
		newOptions.put(DefaultCodeFormatterConstants.FORMATTER_PUT_EMPTY_STATEMENT_ON_NEW_LINE, "true");
		try {
			fJProject1.setOptions(newOptions);
			String selection= "getIterable()";
			AssistContext context= getCorrectionContext(cu, buf.toString().indexOf(selection) + selection.length(), 0);
			List proposals= collectAssists(context, false);

			assertNumberOfProposals(proposals, 6);
			assertCorrectLabels(proposals);

			String[] expected= new String[2];
			buf= new StringBuffer();
			buf.append("package test1;\n");
			buf.append("import java.util.List;\n");
			buf.append("public class E {\n");
			buf.append("    void foo() {\n");
			buf.append("        for (Iterable<? super Number> iterable : getIterable()) {\n");
			buf.append("            \n");
			buf.append("        }\n");
			buf.append("    }\n");
			buf.append("    <T extends Iterable<? super Number> & Comparable<Number>> Iterable<T> getIterable() {\n");
			buf.append("        return null;\n");
			buf.append("    }\n");
			buf.append("}\n");
			expected[0]= buf.toString();

			buf= new StringBuffer();
			buf.append("package test1;\n");
			buf.append("import java.util.Iterator;\n");
			buf.append("import java.util.List;\n");
			buf.append("public class E {\n");
			buf.append("    void foo() {\n");
			buf.append("        for (Iterator<? extends Iterable<? super Number>> iterator = getIterable()\n");
			buf.append("                .iterator(); iterator.hasNext();) {\n");
			buf.append("            Iterable<? super Number> iterable = iterator.next();\n");
			buf.append("            \n");
			buf.append("        }\n");
			buf.append("    }\n");
			buf.append("    <T extends Iterable<? super Number> & Comparable<Number>> Iterable<T> getIterable() {\n");
			buf.append("        return null;\n");
			buf.append("    }\n");
			buf.append("}\n");
			expected[1]= buf.toString();

			assertExpectedExistInProposals(proposals, expected);
		} finally {
			fJProject1.setOptions(saveOptions);
		}
	}

	@Test
	public void testGenerateForUpperboundWildcard() throws Exception {
//		if (LocalCorrectionsQuickFixTest.BUG_430818)
//			return;
		
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.List;\n");
		buf.append("import java.util.Date;\n");
		buf.append("public class E {\n");
		buf.append("    void foo(List<? extends Date> list) {\n");
		buf.append("        list\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		Map saveOptions= fJProject1.getOptions(false);
		Map newOptions= new HashMap(saveOptions);
		newOptions.put(DefaultCodeFormatterConstants.FORMATTER_PUT_EMPTY_STATEMENT_ON_NEW_LINE, "true");
		try {
			fJProject1.setOptions(newOptions);
			String selection= "list";
			AssistContext context= getCorrectionContext(cu, buf.toString().lastIndexOf(selection) + selection.length(), 0);
			List proposals= collectAssists(context, false);

			assertNumberOfProposals(proposals, 5);
			assertCorrectLabels(proposals);

			String[] expected= new String[3];
			buf= new StringBuffer();
			buf.append("package test1;\n");
			buf.append("import java.util.List;\n");
			buf.append("import java.util.Date;\n");
			buf.append("public class E {\n");
			buf.append("    void foo(List<? extends Date> list) {\n");
			buf.append("        for (Date date : list) {\n");
			buf.append("            \n");
			buf.append("        }\n");
			buf.append("    }\n");
			buf.append("}\n");
			expected[0]= buf.toString();

			buf= new StringBuffer();
			buf.append("package test1;\n");
			buf.append("import java.util.Iterator;\n");
			buf.append("import java.util.List;\n");
			buf.append("import java.util.Date;\n");
			buf.append("public class E {\n");
			buf.append("    void foo(List<? extends Date> list) {\n");
			buf.append("        for (Iterator<? extends Date> iterator = list.iterator(); iterator\n");
			buf.append("                .hasNext();) {\n");
			buf.append("            Date date = iterator.next();\n");
			buf.append("            \n");
			buf.append("        }\n");
			buf.append("    }\n");
			buf.append("}\n");
			expected[1]= buf.toString();

			buf= new StringBuffer();
			buf.append("package test1;\n");
			buf.append("import java.util.List;\n");
			buf.append("import java.util.Date;\n");
			buf.append("public class E {\n");
			buf.append("    void foo(List<? extends Date> list) {\n");
			buf.append("        for (int i = 0; i < list.size(); i++) {\n");
			buf.append("            Date date = list.get(i);\n");
			buf.append("            \n");
			buf.append("        }\n");
			buf.append("    }\n");
			buf.append("}\n");
			expected[2]= buf.toString();

			assertExpectedExistInProposals(proposals, expected);
		} finally {
			fJProject1.setOptions(saveOptions);
		}
	}

	@Test
	public void testGenerateForLowerboundWildcard() throws Exception {
//		if (LocalCorrectionsQuickFixTest.BUG_430818)
//			return;
		
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.List;\n");
		buf.append("import java.util.Date;\n");
		buf.append("public class E {\n");
		buf.append("    void foo(List<? super Date> list) {\n");
		buf.append("        list\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		Map saveOptions= fJProject1.getOptions(false);
		Map newOptions= new HashMap(saveOptions);
		newOptions.put(DefaultCodeFormatterConstants.FORMATTER_PUT_EMPTY_STATEMENT_ON_NEW_LINE, "true");
		try {
			fJProject1.setOptions(newOptions);
			String selection= "list";
			AssistContext context= getCorrectionContext(cu, buf.toString().lastIndexOf(selection) + selection.length(), 0);
			List proposals= collectAssists(context, false);

			assertNumberOfProposals(proposals, 5);
			assertCorrectLabels(proposals);

			String[] expected= new String[3];
			buf= new StringBuffer();
			buf.append("package test1;\n");
			buf.append("import java.util.List;\n");
			buf.append("import java.util.Date;\n");
			buf.append("public class E {\n");
			buf.append("    void foo(List<? super Date> list) {\n");
			buf.append("        for (Object object : list) {\n");
			buf.append("            \n");
			buf.append("        }\n");
			buf.append("    }\n");
			buf.append("}\n");
			expected[0]= buf.toString();

			buf= new StringBuffer();
			buf.append("package test1;\n");
			buf.append("import java.util.Iterator;\n");
			buf.append("import java.util.List;\n");
			buf.append("import java.util.Date;\n");
			buf.append("public class E {\n");
			buf.append("    void foo(List<? super Date> list) {\n");
			buf.append("        for (Iterator<? super Date> iterator = list.iterator(); iterator\n");
			buf.append("                .hasNext();) {\n");
			buf.append("            Object object = iterator.next();\n");
			buf.append("            \n");
			buf.append("        }\n");
			buf.append("    }\n");
			buf.append("}\n");
			expected[1]= buf.toString();

			buf= new StringBuffer();
			buf.append("package test1;\n");
			buf.append("import java.util.List;\n");
			buf.append("import java.util.Date;\n");
			buf.append("public class E {\n");
			buf.append("    void foo(List<? super Date> list) {\n");
			buf.append("        for (int i = 0; i < list.size(); i++) {\n");
			buf.append("            Object object = list.get(i);\n");
			buf.append("            \n");
			buf.append("        }\n");
			buf.append("    }\n");
			buf.append("}\n");
			expected[2]= buf.toString();

			assertExpectedExistInProposals(proposals, expected);
		} finally {
			fJProject1.setOptions(saveOptions);
		}
	}

	@Test
	public void testGenerateForComplexInnerLowerboundWildcard() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.List;\n");
		buf.append("public class E {\n");
		buf.append("    private abstract class Inner<T, K> implements Iterable<K>{}\n");
		buf.append("    void foo() {\n");
		buf.append("        getList()\n");
		buf.append("    }\n");
		buf.append("    Inner<? super List<Number>, ? super List<List<Number>>> getList() {\n");
		buf.append("        return null;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		Map saveOptions= fJProject1.getOptions(false);
		Map newOptions= new HashMap(saveOptions);
		newOptions.put(DefaultCodeFormatterConstants.FORMATTER_PUT_EMPTY_STATEMENT_ON_NEW_LINE, "true");
		try {
			fJProject1.setOptions(newOptions);
			String selection= "getList()";
			AssistContext context= getCorrectionContext(cu, buf.toString().indexOf(selection) + selection.length(), 0);
			List proposals= collectAssists(context, false);

			assertNumberOfProposals(proposals, 6);
			assertCorrectLabels(proposals);

			String[] expected= new String[2];
			buf= new StringBuffer();
			buf.append("package test1;\n");
			buf.append("import java.util.List;\n");
			buf.append("public class E {\n");
			buf.append("    private abstract class Inner<T, K> implements Iterable<K>{}\n");
			buf.append("    void foo() {\n");
			buf.append("        for (Object object : getList()) {\n");
			buf.append("            \n");
			buf.append("        }\n");
			buf.append("    }\n");
			buf.append("    Inner<? super List<Number>, ? super List<List<Number>>> getList() {\n");
			buf.append("        return null;\n");
			buf.append("    }\n");
			buf.append("}\n");
			expected[0]= buf.toString();

			buf= new StringBuffer();
			buf.append("package test1;\n");
			buf.append("import java.util.Iterator;\n");
			buf.append("import java.util.List;\n");
			buf.append("public class E {\n");
			buf.append("    private abstract class Inner<T, K> implements Iterable<K>{}\n");
			buf.append("    void foo() {\n");
			buf.append("        for (Iterator<? super List<List<Number>>> iterator = getList()\n");
			buf.append("                .iterator(); iterator.hasNext();) {\n");
			buf.append("            Object object = iterator.next();\n");
			buf.append("            \n");
			buf.append("        }\n");
			buf.append("    }\n");
			buf.append("    Inner<? super List<Number>, ? super List<List<Number>>> getList() {\n");
			buf.append("        return null;\n");
			buf.append("    }\n");
			buf.append("}\n");
			expected[1]= buf.toString();

			assertExpectedExistInProposals(proposals, expected);
		} finally {
			fJProject1.setOptions(saveOptions);
		}
	}

	@Test
	public void testGenerateForMissingParametrization() throws Exception {
//		if (LocalCorrectionsQuickFixTest.BUG_430818)
//			return;
		
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.Collection;\n");
		buf.append("public class E {\n");
		buf.append("    void foo(Collection collection) {\n");
		buf.append("        collection\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		Map saveOptions= fJProject1.getOptions(false);
		Map newOptions= new HashMap(saveOptions);
		newOptions.put(DefaultCodeFormatterConstants.FORMATTER_PUT_EMPTY_STATEMENT_ON_NEW_LINE, "true");
		try {
			fJProject1.setOptions(newOptions);
			String selection= "collection";
			AssistContext context= getCorrectionContext(cu, buf.toString().lastIndexOf(selection) + selection.length(), 0);
			List proposals= collectAssists(context, false);

			assertNumberOfProposals(proposals, 4);
			assertCorrectLabels(proposals);

			String[] expected= new String[2];
			buf= new StringBuffer();
			buf.append("package test1;\n");
			buf.append("import java.util.Collection;\n");
			buf.append("public class E {\n");
			buf.append("    void foo(Collection collection) {\n");
			buf.append("        for (Object object : collection) {\n");
			buf.append("            \n");
			buf.append("        }\n");
			buf.append("    }\n");
			buf.append("}\n");
			expected[0]= buf.toString();

			buf= new StringBuffer();
			buf.append("package test1;\n");
			buf.append("import java.util.Collection;\n");
			buf.append("import java.util.Iterator;\n");
			buf.append("public class E {\n");
			buf.append("    void foo(Collection collection) {\n");
			buf.append("        for (Iterator iterator = collection.iterator(); iterator.hasNext();) {\n");
			buf.append("            Object object = iterator.next();\n");
			buf.append("            \n");
			buf.append("        }\n");
			buf.append("    }\n");
			buf.append("}\n");
			expected[1]= buf.toString();

			assertExpectedExistInProposals(proposals, expected);
		} finally {
			fJProject1.setOptions(saveOptions);
		}
	}

//	public void testGenerateForLowVersion() throws Exception {
////		if (LocalCorrectionsQuickFixTest.BUG_430818)
////			return;
//
//		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
//		StringBuffer buf= new StringBuffer();
//		buf.append("package test1;\n");
//		buf.append("import java.util.Collection;\n");
//		buf.append("public class E {\n");
//		buf.append("    void foo(Collection collection) {\n");
//		buf.append("        collection\n");
//		buf.append("    }\n");
//		buf.append("}\n");
//		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);
//
//		Map saveOptions= fJProject1.getOptions(false);
//		Map newOptions= new HashMap();
//		JavaCore.setComplianceOptions(JavaCore.VERSION_1_4, newOptions);
//		newOptions.put(DefaultCodeFormatterConstants.FORMATTER_PUT_EMPTY_STATEMENT_ON_NEW_LINE, "true");
//		try {
//			fJProject1.setOptions(newOptions);
//
//			String selection= "collection";
//			AssistContext context= getCorrectionContext(cu, buf.toString().lastIndexOf(selection) + selection.length(), 0);
//			List proposals= collectAssists(context, false);
//
//			assertNumberOfProposals(proposals, 3);
//			assertProposalDoesNotExist(proposals, CorrectionMessages.QuickAssistProcessor_generate_enhanced_for_loop);
//			assertCorrectLabels(proposals);
//
//			String[] expected= new String[1];
//
//			// no generics should be added to iterator since the version is too low
//			buf= new StringBuffer();
//			buf.append("package test1;\n");
//			buf.append("import java.util.Collection;\n");
//			buf.append("import java.util.Iterator;\n");
//			buf.append("public class E {\n");
//			buf.append("    void foo(Collection collection) {\n");
//			buf.append("        for (Iterator iterator = collection.iterator(); iterator.hasNext();) {\n");
//			buf.append("            Object object = iterator.next();\n");
//			buf.append("            \n");
//			buf.append("        }\n");
//			buf.append("    }\n");
//			buf.append("}\n");
//			expected[0]= buf.toString();
//
//			assertExpectedExistInProposals(proposals, expected);
//		} finally {
//			fJProject1.setOptions(saveOptions);
//		}
//	}

	@Test
	public void testGenerateForArray() throws Exception {
//		if (LocalCorrectionsQuickFixTest.BUG_430818)
//			return;
		
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    void foo(String[] array) {\n");
		buf.append("        array\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		Map saveOptions= fJProject1.getOptions(false);
		Map newOptions= new HashMap(saveOptions);
		newOptions.put(DefaultCodeFormatterConstants.FORMATTER_PUT_EMPTY_STATEMENT_ON_NEW_LINE, "true");
		try {
			fJProject1.setOptions(newOptions);
			String selection= "array";
			AssistContext context= getCorrectionContext(cu, buf.toString().lastIndexOf(selection) + selection.length(), 0);
			List proposals= collectAssists(context, false);
	
			assertNumberOfProposals(proposals, 4);
			assertCorrectLabels(proposals);
	
			String[] expected= new String[2];
			buf= new StringBuffer();
			buf.append("package test1;\n");
			buf.append("public class E {\n");
			buf.append("    void foo(String[] array) {\n");
			buf.append("        for (int i = 0; i < array.length; i++) {\n");
			buf.append("            String string = array[i];\n");
			buf.append("            \n");
			buf.append("        }\n");
			buf.append("    }\n");
			buf.append("}\n");
			expected[0]= buf.toString();
	
			buf= new StringBuffer();
			buf.append("package test1;\n");
			buf.append("public class E {\n");
			buf.append("    void foo(String[] array) {\n");
			buf.append("        for (String string : array) {\n");
			buf.append("            \n");
			buf.append("        }\n");
			buf.append("    }\n");
			buf.append("}\n");
			expected[1]= buf.toString();
	
			assertExpectedExistInProposals(proposals, expected);
		} finally {
			fJProject1.setOptions(saveOptions);
		}
	}

	@Test
	public void testGenerateForNameClash() throws Exception {
//		if (LocalCorrectionsQuickFixTest.BUG_430818)
//			return;
		
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    private int[] nums;\n");
		buf.append("    void foo() {\n");
		buf.append("        nums\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);
	
		Map saveOptions= fJProject1.getOptions(false);
		Map newOptions= new HashMap(saveOptions);
		newOptions.put(DefaultCodeFormatterConstants.FORMATTER_PUT_EMPTY_STATEMENT_ON_NEW_LINE, "true");
		try {
			fJProject1.setOptions(newOptions);
			String selection= "nums";
			AssistContext context= getCorrectionContext(cu, buf.toString().lastIndexOf(selection) + selection.length(), 0);
			List proposals= collectAssists(context, false);
	
			assertNumberOfProposals(proposals, 4);
			assertCorrectLabels(proposals);
	
			String[] expected= new String[2];
			buf= new StringBuffer();
			buf.append("package test1;\n");
			buf.append("public class E {\n");
			buf.append("    private int[] nums;\n");
			buf.append("    void foo() {\n");
			buf.append("        for (int i = 0; i < nums.length; i++) {\n");
			buf.append("            int j = nums[i];\n");
			buf.append("            \n");
			buf.append("        }\n");
			buf.append("    }\n");
			buf.append("}\n");
			expected[0]= buf.toString();
	
			buf= new StringBuffer();
			buf.append("package test1;\n");
			buf.append("public class E {\n");
			buf.append("    private int[] nums;\n");
			buf.append("    void foo() {\n");
			buf.append("        for (int i : nums) {\n");
			buf.append("            \n");
			buf.append("        }\n");
			buf.append("    }\n");
			buf.append("}\n");
			expected[1]= buf.toString();
	
			assertExpectedExistInProposals(proposals, expected);
		} finally {
			fJProject1.setOptions(saveOptions);
		}
	}

	@Test
	public void testGenerateForImportsAndFormat1() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    class Iterator {}\n");
		buf.append("    void foo() {\n");
		buf.append("        B.get( /*important: empty*/ );\n");
		buf.append("    }\n");
		buf.append("}\n");

		StringBuffer buf2= new StringBuffer();
		buf2.append("package test1;\n");
		buf2.append("import java.util.ArrayList;\n");
		buf2.append("import java.util.Date;\n");
		buf2.append("import java.util.Set;\n");
		buf2.append("public class B {\n");
		buf2.append("    static ArrayList<Date> get() {\n");
		buf2.append("        return new ArrayList<Date>();\n");
		buf2.append("    }\n");
		buf2.append("    static Set raw(int i) {\n");
		buf2.append("        return java.util.Collections.emptySet();\n");
		buf2.append("    }\n");
		buf2.append("}");
		
		ICompilationUnit cu= pack1.createCompilationUnit("A.java", buf.toString(), false, null);
		pack1.createCompilationUnit("B.java", buf2.toString(), false, null);

		Map saveOptions= fJProject1.getOptions(false);
		Map newOptions= new HashMap();
		JavaCore.setComplianceOptions(org.eclipse.jdt.core.JavaCore.VERSION_1_5, newOptions);
		newOptions.put(DefaultCodeFormatterConstants.FORMATTER_PUT_EMPTY_STATEMENT_ON_NEW_LINE, "true");
		try {
			fJProject1.setOptions(newOptions);
		
			String selection= "B.get( /*important: empty*/ );";
			AssistContext context= getCorrectionContext(cu, buf.toString().lastIndexOf(selection) + selection.length(), 0);
			List proposals= collectAssists(context, false);
	
			assertNumberOfProposals(proposals, 5);
			assertCorrectLabels(proposals);
	
			String[] expected= new String[3];
			buf= new StringBuffer();
			buf.append("package test1;\n");
			buf.append("\n");
			buf.append("import java.util.Date;\n");
			buf.append("\n");
			buf.append("public class A {\n");
			buf.append("    class Iterator {}\n");
			buf.append("    void foo() {\n");
			buf.append("        for (Date date : B.get( /*important: empty*/ )) {\n");
			buf.append("            \n");
			buf.append("        }\n");
			buf.append("    }\n");
			buf.append("}\n");
			expected[0]= buf.toString();
	
			buf= new StringBuffer();
			buf.append("package test1;\n");
			buf.append("\n");
			buf.append("import java.util.Date;\n");
			buf.append("\n");
			buf.append("public class A {\n");
			buf.append("    class Iterator {}\n");
			buf.append("    void foo() {\n");
			buf.append("        for (java.util.Iterator<Date> iterator = B.get( /*important: empty*/ ).iterator(); iterator\n");
			buf.append("                .hasNext();) {\n");
			buf.append("            Date date = iterator.next();\n");
			buf.append("            \n");
			buf.append("        }\n");
			buf.append("    }\n");
			buf.append("}\n");
			expected[1]= buf.toString();
			
			buf= new StringBuffer();
			buf.append("package test1;\n");
			buf.append("\n");
			buf.append("import java.util.Date;\n");
			buf.append("\n");
			buf.append("public class A {\n");
			buf.append("    class Iterator {}\n");
			buf.append("    void foo() {\n");
			buf.append("        for (int i = 0; i < B.get( /*important: empty*/ ).size(); i++) {\n");
			buf.append("            Date date = B.get( /*important: empty*/ ).get(i);\n");
			buf.append("            \n");
			buf.append("        }\n");
			buf.append("    }\n");
			buf.append("}\n");
			expected[2]= buf.toString();
	
			assertExpectedExistInProposals(proposals, expected);
		} finally {
			fJProject1.setOptions(saveOptions);
		}
	}

	@Test
	public void testGenerateForImportsAndFormat2() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    class Object {}\n");
		buf.append("    class Iterator {}\n");
		buf.append("    void foo() {\n");
		buf.append("        B.raw(1+ 2);\n");
		buf.append("    }\n");
		buf.append("}\n");

		StringBuffer buf2= new StringBuffer();
		buf2.append("package test1;\n");
		buf2.append("import java.util.ArrayList;\n");
		buf2.append("import java.util.Date;\n");
		buf2.append("import java.util.Set;\n");
		buf2.append("public class B {\n");
		buf2.append("    static ArrayList<Date> get() {\n");
		buf2.append("        return new ArrayList<Date>();\n");
		buf2.append("    }\n");
		buf2.append("    static Set raw(int i) {\n");
		buf2.append("        return java.util.Collections.emptySet();\n");
		buf2.append("    }\n");
		buf2.append("}");
		
		ICompilationUnit cu= pack1.createCompilationUnit("A.java", buf.toString(), false, null);
		pack1.createCompilationUnit("B.java", buf2.toString(), false, null);

		Map saveOptions= fJProject1.getOptions(false);
		Map newOptions= new HashMap();
		JavaCore.setComplianceOptions(org.eclipse.jdt.core.JavaCore.VERSION_1_5, newOptions);
		newOptions.put(DefaultCodeFormatterConstants.FORMATTER_PUT_EMPTY_STATEMENT_ON_NEW_LINE, "true");
		try {
			fJProject1.setOptions(newOptions);
		
			String selection= "B.raw(1+ 2);";
			AssistContext context= getCorrectionContext(cu, buf.toString().lastIndexOf(selection) + selection.length(), 0);
			List proposals= collectAssists(context, false);
	
			assertNumberOfProposals(proposals, 4);
			assertCorrectLabels(proposals);
	
			String[] expected= new String[2];
			buf= new StringBuffer();
			buf.append("package test1;\n");
			buf.append("public class A {\n");
			buf.append("    class Object {}\n");
			buf.append("    class Iterator {}\n");
			buf.append("    void foo() {\n");
			buf.append("        for (java.lang.Object object : B.raw(1+ 2)) {\n");
			buf.append("            \n");
			buf.append("        }\n");
			buf.append("    }\n");
			buf.append("}\n");
			expected[0]= buf.toString();
	
			buf= new StringBuffer();
			buf.append("package test1;\n");
			buf.append("public class A {\n");
			buf.append("    class Object {}\n");
			buf.append("    class Iterator {}\n");
			buf.append("    void foo() {\n");
			buf.append("        for (java.util.Iterator iterator = B.raw(1+ 2).iterator(); iterator\n");
			buf.append("                .hasNext();) {\n");
			buf.append("            java.lang.Object object = iterator.next();\n");
			buf.append("            \n");
			buf.append("        }\n");
			buf.append("    }\n");
			buf.append("}\n");
			expected[1]= buf.toString();
	
			assertExpectedExistInProposals(proposals, expected);
		} finally {
			fJProject1.setOptions(saveOptions);
		}
	}

	@Test
	public void testGenerateForImportsArray() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    class Date {}\n");
		buf.append("    void foo() {\n");
		buf.append("        B.get();\n");
		buf.append("    }\n");
		buf.append("}\n");

		StringBuffer buf2= new StringBuffer();
		buf2.append("package test1;\n");
		buf2.append("import java.util.Date;\n");
		buf2.append("public class B {\n");
		buf2.append("    static Date[] get() {\n");
		buf2.append("        return new Date[1];\n");
		buf2.append("    }\n");
		buf2.append("}");
		
		ICompilationUnit cu= pack1.createCompilationUnit("A.java", buf.toString(), false, null);
		pack1.createCompilationUnit("B.java", buf2.toString(), false, null);

		Map saveOptions= fJProject1.getOptions(false);
		Map newOptions= new HashMap();
		JavaCore.setComplianceOptions(org.eclipse.jdt.core.JavaCore.VERSION_1_5, newOptions);
		newOptions.put(DefaultCodeFormatterConstants.FORMATTER_PUT_EMPTY_STATEMENT_ON_NEW_LINE, "true");
		try {
			fJProject1.setOptions(newOptions);
		
			String selection= "B.get();";
			AssistContext context= getCorrectionContext(cu, buf.toString().lastIndexOf(selection) + selection.length(), 0);
			List proposals= collectAssists(context, false);
	
			assertNumberOfProposals(proposals, 4);
			assertCorrectLabels(proposals);
	
			String[] expected= new String[1];
			buf= new StringBuffer();
			buf.append("package test1;\n");
			buf.append("public class A {\n");
			buf.append("    class Date {}\n");
			buf.append("    void foo() {\n");
			buf.append("        for (java.util.Date date : B.get()) {\n");
			buf.append("            \n");
			buf.append("        }\n");
			buf.append("    }\n");
			buf.append("}\n");
			expected[0]= buf.toString();
	
			assertExpectedExistInProposals(proposals, expected);
		} finally {
			fJProject1.setOptions(saveOptions);
		}
	}
}
