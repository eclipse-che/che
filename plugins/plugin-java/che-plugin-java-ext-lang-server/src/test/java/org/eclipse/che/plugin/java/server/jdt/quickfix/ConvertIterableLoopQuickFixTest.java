/*******************************************************************************
 * Copyright (c) 2005, 2013 IBM Corporation and others.
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
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.text.correction.AssistContext;
import org.eclipse.jdt.internal.ui.text.correction.QuickAssistProcessor;
import org.eclipse.jdt.internal.ui.text.correction.proposals.FixCorrectionProposal;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Hashtable;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public final class ConvertIterableLoopQuickFixTest extends QuickFixTest {

	private FixCorrectionProposal fConvertLoopProposal;

	private IJavaProject fProject;

	private IPackageFragmentRoot fSourceFolder;

	public ConvertIterableLoopQuickFixTest() {
		super(new ProjectTestSetup());
	}

	private List fetchConvertingProposal(StringBuffer buf, ICompilationUnit cu) throws Exception {
		int offset = buf.toString().indexOf("for");
		AssistContext context = getCorrectionContext(cu, offset, 0);
		List proposals = collectAssists(context, false);
		fConvertLoopProposal = (FixCorrectionProposal)findProposalByCommandId(QuickAssistProcessor.CONVERT_FOR_LOOP_ID, proposals);
		return proposals;
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

		fProject = ProjectTestSetup.getProject();

		fSourceFolder = JavaProjectHelper.addSourceContainer(fProject, "src");
		fConvertLoopProposal = null;
	}

    @After
	public void tearDown() throws Exception {
        super.tearDown();
		JavaProjectHelper.clear(fProject, ProjectTestSetup.getDefaultClasspath());
		fConvertLoopProposal = null;
		fProject = null;
		fSourceFolder = null;
	}

    @Test
	public void testSimplestSmokeCase() throws Exception {
		IPackageFragment pack = fSourceFolder.createPackageFragment("test", false, null);
		StringBuffer buf = new StringBuffer();
		buf.append("package test;\r\n");
		buf.append("import java.util.Collection;\r\n");
		buf.append("import java.util.Iterator;\r\n");
		buf.append("public class A {\r\n");
		buf.append("	Collection<String> c;\r\n");
		buf.append("	public A() {\r\n");
		buf.append("		for (final Iterator<String> iterator= c.iterator(); iterator.hasNext();) {\r\n");
		buf.append("			String test= iterator.next();\r\n");
		buf.append("			System.out.println(test);\r\n");
		buf.append("		}\r\n");
		buf.append("	}\r\n");
		buf.append("}");
		ICompilationUnit unit = pack.createCompilationUnit("A.java", buf.toString(), false, null);

		List proposals= fetchConvertingProposal(buf, unit);

		assertNotNull(fConvertLoopProposal);

		assertCorrectLabels(proposals);

		String preview= getPreviewContent(fConvertLoopProposal);

		buf= new StringBuffer();
		buf.append("package test;\r\n");
		buf.append("import java.util.Collection;\r\n");
		buf.append("public class A {\r\n");
		buf.append("	Collection<String> c;\r\n");
		buf.append("	public A() {\r\n");
		buf.append("		for (String test : c) {\r\n");
		buf.append("			System.out.println(test);\r\n");
		buf.append("		}\r\n");
		buf.append("	}\r\n");
		buf.append("}");
		String expected= buf.toString();
		assertEqualString(preview, expected);
	}

    @Test
	public void testEnumeration() throws Exception {
		IPackageFragment pack= fSourceFolder.createPackageFragment("test", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test;\r\n");
		buf.append("import java.util.Enumeration;\r\n");
		buf.append("import java.util.Vector;\r\n");
		buf.append("public class A {\r\n");
		buf.append("	Vector<String> c;\r\n");
		buf.append("	public A() {\r\n");
		buf.append("		for (Enumeration<String> e= c.elements(); e.hasMoreElements(); ) {\r\n");
		buf.append("			String nextElement = e.nextElement();\r\n");
		buf.append("			System.out.println(nextElement);\r\n");
		buf.append("		}\r\n");
		buf.append("	}\r\n");
		buf.append("}");
		ICompilationUnit unit= pack.createCompilationUnit("A.java", buf.toString(), false, null);

		List proposals= fetchConvertingProposal(buf, unit);

		assertNotNull(fConvertLoopProposal);

		assertCorrectLabels(proposals);

		String preview= getPreviewContent(fConvertLoopProposal);

		buf= new StringBuffer();
		buf.append("package test;\r\n");
		buf.append("import java.util.Vector;\r\n");
		buf.append("public class A {\r\n");
		buf.append("	Vector<String> c;\r\n");
		buf.append("	public A() {\r\n");
		buf.append("		for (String nextElement : c) {\r\n");
		buf.append("			System.out.println(nextElement);\r\n");
		buf.append("		}\r\n");
		buf.append("	}\r\n");
		buf.append("}");
		String expected= buf.toString();
		assertEqualString(preview, expected);
	}

    @Test
	public void testSplitAssignment() throws Exception {
		IPackageFragment pack= fSourceFolder.createPackageFragment("test", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test;\r\n");
		buf.append("import java.util.Collection;\r\n");
		buf.append("import java.util.Iterator;\r\n");
		buf.append("public class A {\r\n");
		buf.append("	Collection<String> c;\r\n");
		buf.append("	public A() {\r\n");
		buf.append("		for (final Iterator<String> iterator= c.iterator(); iterator.hasNext();) {\r\n");
		buf.append("			String test= null;\r\n");
		buf.append("			test= iterator.next();\r\n");
		buf.append("			System.out.println(test);\r\n");
		buf.append("		}\r\n");
		buf.append("	}\r\n");
		buf.append("}");
		ICompilationUnit unit= pack.createCompilationUnit("A.java", buf.toString(), false, null);

		List proposals= fetchConvertingProposal(buf, unit);

		assertNotNull(fConvertLoopProposal);

		assertCorrectLabels(proposals);

		String preview= getPreviewContent(fConvertLoopProposal);

		buf= new StringBuffer();
		buf.append("package test;\r\n");
		buf.append("import java.util.Collection;\r\n");
		buf.append("public class A {\r\n");
		buf.append("	Collection<String> c;\r\n");
		buf.append("	public A() {\r\n");
		buf.append("		for (String test : c) {\r\n");
		buf.append("			System.out.println(test);\r\n");
		buf.append("		}\r\n");
		buf.append("	}\r\n");
		buf.append("}");
		String expected= buf.toString();
		assertEqualString(preview, expected);
	}

    @Test
	public void testIndirectUsage() throws Exception {
		IPackageFragment pack= fSourceFolder.createPackageFragment("test", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test;\r\n");
		buf.append("import java.util.Collection;\r\n");
		buf.append("import java.util.Iterator;\r\n");
		buf.append("public class A {\r\n");
		buf.append("	Collection<String> c;\r\n");
		buf.append("	public A() {\r\n");
		buf.append("		for (final Iterator<String> iterator= c.iterator(); iterator.hasNext();) {\r\n");
		buf.append("			String test= null;\r\n");
		buf.append("			test= iterator.next();\r\n");
		buf.append("			String backup= test;\r\n");
		buf.append("			System.out.println(backup);\r\n");
		buf.append("		}\r\n");
		buf.append("	}\r\n");
		buf.append("}");
		ICompilationUnit unit= pack.createCompilationUnit("A.java", buf.toString(), false, null);

		List proposals= fetchConvertingProposal(buf, unit);

		assertNotNull(fConvertLoopProposal);

		assertCorrectLabels(proposals);

		String preview= getPreviewContent(fConvertLoopProposal);

		buf= new StringBuffer();
		buf.append("package test;\r\n");
		buf.append("import java.util.Collection;\r\n");
		buf.append("public class A {\r\n");
		buf.append("	Collection<String> c;\r\n");
		buf.append("	public A() {\r\n");
		buf.append("		for (String test : c) {\r\n");
		buf.append("			String backup= test;\r\n");
		buf.append("			System.out.println(backup);\r\n");
		buf.append("		}\r\n");
		buf.append("	}\r\n");
		buf.append("}");
		String expected= buf.toString();
		assertEqualString(preview, expected);
	}

    @Test
	public void testMethodCall1() throws Exception {
		IPackageFragment pack= fSourceFolder.createPackageFragment("test", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test;\r\n");
		buf.append("import java.util.Collection;\r\n");
		buf.append("import java.util.Iterator;\r\n");
		buf.append("public class A {\r\n");
		buf.append("	Collection<String> c;\r\n");
		buf.append("	private Collection<String> getCollection() {\r\n");
		buf.append("		return c;\r\n");
		buf.append("	}\r\n");
		buf.append("	public A() {\r\n");
		buf.append("		for (final Iterator<String> iterator= getCollection().iterator(); iterator.hasNext();) {\r\n");
		buf.append("			String test= iterator.next();\r\n");
		buf.append("			String backup= test;\r\n");
		buf.append("			System.out.println(backup);\r\n");
		buf.append("		}\r\n");
		buf.append("	}\r\n");
		buf.append("}");
		ICompilationUnit unit= pack.createCompilationUnit("A.java", buf.toString(), false, null);

		List proposals= fetchConvertingProposal(buf, unit);

		assertNotNull(fConvertLoopProposal);

		assertCorrectLabels(proposals);

		String preview= getPreviewContent(fConvertLoopProposal);

		buf= new StringBuffer();
		buf.append("package test;\r\n");
		buf.append("import java.util.Collection;\r\n");
		buf.append("public class A {\r\n");
		buf.append("	Collection<String> c;\r\n");
		buf.append("	private Collection<String> getCollection() {\r\n");
		buf.append("		return c;\r\n");
		buf.append("	}\r\n");
		buf.append("	public A() {\r\n");
		buf.append("		for (String test : getCollection()) {\r\n");
		buf.append("			String backup= test;\r\n");
		buf.append("			System.out.println(backup);\r\n");
		buf.append("		}\r\n");
		buf.append("	}\r\n");
		buf.append("}");
		String expected= buf.toString();
		assertEqualString(preview, expected);
	}

    @Test
	public void testMethodCall2() throws Exception {
		IPackageFragment pack= fSourceFolder.createPackageFragment("test", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test;\r\n");
		buf.append("import java.util.Collection;\r\n");
		buf.append("import java.util.Iterator;\r\n");
		buf.append("public class A {\r\n");
		buf.append("	Collection<String> c;\r\n");
		buf.append("	private Collection<String> getCollection() {\r\n");
		buf.append("		return c;\r\n");
		buf.append("	}\r\n");
		buf.append("	public A() {\r\n");
		buf.append("		for (final Iterator<String> iterator= this.getCollection().iterator(); iterator.hasNext();) {\r\n");
		buf.append("			String test= iterator.next();\r\n");
		buf.append("			String backup= test;\r\n");
		buf.append("			System.out.println(backup);\r\n");
		buf.append("		}\r\n");
		buf.append("	}\r\n");
		buf.append("}");
		ICompilationUnit unit= pack.createCompilationUnit("A.java", buf.toString(), false, null);

		List proposals= fetchConvertingProposal(buf, unit);

		assertNotNull(fConvertLoopProposal);

		assertCorrectLabels(proposals);

		String preview= getPreviewContent(fConvertLoopProposal);

		buf= new StringBuffer();
		buf.append("package test;\r\n");
		buf.append("import java.util.Collection;\r\n");
		buf.append("public class A {\r\n");
		buf.append("	Collection<String> c;\r\n");
		buf.append("	private Collection<String> getCollection() {\r\n");
		buf.append("		return c;\r\n");
		buf.append("	}\r\n");
		buf.append("	public A() {\r\n");
		buf.append("		for (String test : this.getCollection()) {\r\n");
		buf.append("			String backup= test;\r\n");
		buf.append("			System.out.println(backup);\r\n");
		buf.append("		}\r\n");
		buf.append("	}\r\n");
		buf.append("}");
		String expected= buf.toString();
		assertEqualString(preview, expected);
	}

    @Test
	public void testNested() throws Exception {
		IPackageFragment pack= fSourceFolder.createPackageFragment("test", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test;\r\n");
		buf.append("import java.util.Collection;\r\n");
		buf.append("import java.util.Iterator;\r\n");
		buf.append("public class A {\r\n");
		buf.append("	public A() {\r\n");
		buf.append("		Collection<Collection<String>> cc= null;\r\n");
		buf.append("		for (final Iterator<Collection<String>> outer= cc.iterator(); outer.hasNext();) {\r\n");
		buf.append("			final Collection<String> c = outer.next();\r\n");
		buf.append("			for (final Iterator<String> inner= c.iterator(); inner.hasNext();) {\r\n");
		buf.append("				System.out.println(inner.next());\r\n");
		buf.append("			}\r\n");
		buf.append("		}\r\n");
		buf.append("	}\r\n");
		buf.append("}");
		ICompilationUnit unit= pack.createCompilationUnit("A.java", buf.toString(), false, null);

		List proposals= fetchConvertingProposal(buf, unit);

		assertNotNull(fConvertLoopProposal);

		assertCorrectLabels(proposals);

		String preview= getPreviewContent(fConvertLoopProposal);

		buf= new StringBuffer();
		buf.append("package test;\r\n");
		buf.append("import java.util.Collection;\r\n");
		buf.append("import java.util.Iterator;\r\n");
		buf.append("public class A {\r\n");
		buf.append("	public A() {\r\n");
		buf.append("		Collection<Collection<String>> cc= null;\r\n");
		buf.append("		for (Collection<String> c : cc) {\r\n");
		buf.append("			for (final Iterator<String> inner= c.iterator(); inner.hasNext();) {\r\n");
		buf.append("				System.out.println(inner.next());\r\n");
		buf.append("			}\r\n");
		buf.append("		}\r\n");
		buf.append("	}\r\n");
		buf.append("}");
		String expected= buf.toString();
		assertEqualString(preview, expected);
	}

    @Test
	public void testMethodCall3() throws Exception {
		IPackageFragment pack= fSourceFolder.createPackageFragment("test", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test;\r\n");
		buf.append("import java.util.Collection;\r\n");
		buf.append("import java.util.Iterator;\r\n");
		buf.append("public class A {\r\n");
		buf.append("	Collection<String> c;\r\n");
		buf.append("	private Collection<String> getCollection() {\r\n");
		buf.append("		return c;\r\n");
		buf.append("	}\r\n");
		buf.append("	public A() {\r\n");
		buf.append("		for (final Iterator<String> iterator= new A().getCollection().iterator(); iterator.hasNext();) {\r\n");
		buf.append("			String test= iterator.next();\r\n");
		buf.append("			String backup= test;\r\n");
		buf.append("			System.out.println(backup);\r\n");
		buf.append("		}\r\n");
		buf.append("	}\r\n");
		buf.append("}");
		ICompilationUnit unit= pack.createCompilationUnit("A.java", buf.toString(), false, null);

		List proposals= fetchConvertingProposal(buf, unit);

		assertNotNull(fConvertLoopProposal);

		assertCorrectLabels(proposals);

		String preview= getPreviewContent(fConvertLoopProposal);

		buf= new StringBuffer();
		buf.append("package test;\r\n");buf.append("import java.util.Collection;\r\n");buf.append("public class A {\r\n");buf.append("	Collection<String> c;\r\n");buf.append("	private Collection<String> getCollection() {\r\n");buf.append("		return c;\r\n");buf.append("	}\r\n");buf.append("	public A() {\r\n");buf.append("		for (String test : new A().getCollection()) {\r\n");buf.append("			String backup= test;\r\n");buf.append("			System.out.println(backup);\r\n");buf.append("		}\r\n");buf.append("	}\r\n");buf.append("}");
		String expected= buf.toString();
		assertEqualString(preview, expected);
	}

    @Test
	public void testNoAssignment() throws Exception {
		IPackageFragment pack= fSourceFolder.createPackageFragment("test", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test;\r\n");
		buf.append("import java.util.Collection;\r\n");
		buf.append("import java.util.Iterator;\r\n");
		buf.append("public class A {\r\n");
		buf.append("	public A() {\r\n");
		buf.append("		Collection<Collection<String>> cc= null;\r\n");
		buf.append("		for (final Iterator<Collection<String>> outer= cc.iterator(); outer.hasNext();) {\r\n");
		buf.append("			for (final Iterator<String> inner= outer.next().iterator(); inner.hasNext();) {\r\n");
		buf.append("				System.out.println(inner.next());\r\n");
		buf.append("			}\r\n");
		buf.append("		}\r\n");
		buf.append("	}\r\n");
		buf.append("}");
		ICompilationUnit unit= pack.createCompilationUnit("A.java", buf.toString(), false, null);

		List proposals= fetchConvertingProposal(buf, unit);

		assertNotNull(fConvertLoopProposal);

		assertCorrectLabels(proposals);

		String preview= getPreviewContent(fConvertLoopProposal);

		buf= new StringBuffer();
		buf.append("package test;\r\n");
		buf.append("import java.util.Collection;\r\n");
		buf.append("import java.util.Iterator;\r\n");
		buf.append("public class A {\r\n");
		buf.append("	public A() {\r\n");
		buf.append("		Collection<Collection<String>> cc= null;\r\n");
		buf.append("		for (Collection<String> collection : cc) {\r\n");
		buf.append("			for (final Iterator<String> inner= collection.iterator(); inner.hasNext();) {\r\n");
		buf.append("				System.out.println(inner.next());\r\n");
		buf.append("			}\r\n");
		buf.append("		}\r\n");
		buf.append("	}\r\n");
		buf.append("}");
		String expected= buf.toString();
		assertEqualString(preview, expected);
	}

    @Test
	public void testOutsideAssignment1() throws Exception {
		IPackageFragment pack= fSourceFolder.createPackageFragment("test", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test;\r\n");
		buf.append("import java.util.Collection;\r\n");
		buf.append("import java.util.Iterator;\r\n");
		buf.append("public class A {\r\n");
		buf.append("	Collection<String> c;\r\n");
		buf.append("	public A() {\r\n");
		buf.append("		String test= null;\r\n");
		buf.append("		for (final Iterator<String> iterator= c.iterator(); iterator.hasNext();) {\r\n");
		buf.append("			test= iterator.next();\r\n");
		buf.append("			String backup= test;\r\n");
		buf.append("			System.out.println(backup);\r\n");
		buf.append("		}\r\n");
		buf.append("	}\r\n");
		buf.append("}");
		ICompilationUnit unit= pack.createCompilationUnit("A.java", buf.toString(), false, null);

		List proposals= fetchConvertingProposal(buf, unit);

		assertNull(fConvertLoopProposal);

		assertCorrectLabels(proposals);
	}

    @Test
	public void testOutsideAssignment2() throws Exception {
		IPackageFragment pack= fSourceFolder.createPackageFragment("test", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test;\r\n");
		buf.append("import java.util.Collection;\r\n");
		buf.append("import java.util.Iterator;\r\n");
		buf.append("public class A {\r\n");
		buf.append("	Collection<String> c;\r\n");
		buf.append("	public A() {\r\n");
		buf.append("		String test;\r\n");
		buf.append("		for (final Iterator<String> iterator= c.iterator(); iterator.hasNext();) {\r\n");
		buf.append("			test= iterator.next();\r\n");
		buf.append("			String backup= test;\r\n");
		buf.append("			System.out.println(backup);\r\n");
		buf.append("		}\r\n");
		buf.append("	}\r\n");
		buf.append("}");
		ICompilationUnit unit= pack.createCompilationUnit("A.java", buf.toString(), false, null);

		List proposals= fetchConvertingProposal(buf, unit);

		assertNull(fConvertLoopProposal);

		assertCorrectLabels(proposals);
	}

    @Test
	public void testWildcard1() throws Exception {
		IPackageFragment pack= fSourceFolder.createPackageFragment("a", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package a;\n");
		buf.append("import java.util.Iterator;\n");
		buf.append("import java.util.List;\n");
		buf.append("public class A {\n");
		buf.append("  void f(List<? super Number> x){\n");
		buf.append("    for (Iterator<? super Number> iter = x.iterator(); iter.hasNext();) {\n");
		buf.append("    }\n");
		buf.append("  }\n");
		buf.append("}\n");
		ICompilationUnit unit= pack.createCompilationUnit("A.java", buf.toString(), false, null);

		List proposals= fetchConvertingProposal(buf, unit);

		assertNotNull(fConvertLoopProposal);

		assertCorrectLabels(proposals);

		String preview= getPreviewContent(fConvertLoopProposal);

		buf= new StringBuffer();
		buf.append("package a;\n");
		buf.append("import java.util.List;\n");
		buf.append("public class A {\n");
		buf.append("  void f(List<? super Number> x){\n");
		buf.append("    for (Object number : x) {\n");
		buf.append("    }\n");
		buf.append("  }\n");
		buf.append("}\n");
		String expected= buf.toString();
		assertEqualString(preview, expected);
	}

    @Test
	public void testWildcard2() throws Exception {
		IPackageFragment pack= fSourceFolder.createPackageFragment("a", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package a;\n");
		buf.append("import java.util.Iterator;\n");
		buf.append("import java.util.List;\n");
		buf.append("public class A {\n");
		buf.append("  void f(List<? extends Number> x){\n");
		buf.append("    for (Iterator<? extends Number> iter = x.iterator(); iter.hasNext();) {\n");
		buf.append("    }\n");
		buf.append("  }\n");
		buf.append("}\n");
		ICompilationUnit unit= pack.createCompilationUnit("A.java", buf.toString(), false, null);

		List proposals= fetchConvertingProposal(buf, unit);

		assertNotNull(fConvertLoopProposal);

		assertCorrectLabels(proposals);

		String preview= getPreviewContent(fConvertLoopProposal);

		buf= new StringBuffer();
		buf.append("package a;\n");
		buf.append("import java.util.List;\n");
		buf.append("public class A {\n");
		buf.append("  void f(List<? extends Number> x){\n");
		buf.append("    for (Number number : x) {\n");
		buf.append("    }\n");
		buf.append("  }\n");
		buf.append("}\n");
		String expected= buf.toString();
		assertEqualString(preview, expected);
	}

    @Test
	public void testBug129508_1() throws Exception {
		IPackageFragment pack= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.Iterator;\n");
		buf.append("import java.util.List;\n");
		buf.append("public class E1 {\n");
		buf.append("    public void foo(List<Integer> list) {\n");
		buf.append("       for (Iterator<Integer> iter = list.iterator(); iter.hasNext();) {\n");
		buf.append("            Integer id = iter.next();\n");
		buf.append("            iter.remove();\n");
		buf.append("       } \n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit unit= pack.createCompilationUnit("E1.java", buf.toString(), false, null);

		List proposals= fetchConvertingProposal(buf, unit);
		assertNull(fConvertLoopProposal);
		assertCorrectLabels(proposals);
	}

    @Test
	public void testBug129508_2() throws Exception {
		IPackageFragment pack= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.Iterator;\n");
		buf.append("import java.util.List;\n");
		buf.append("public class E1 {\n");
		buf.append("    public void foo(List<Integer> list) {\n");
		buf.append("       for (Iterator<Integer> iter = list.iterator(); iter.hasNext();) {\n");
		buf.append("            Integer id = iter.next();\n");
		buf.append("            iter.next();\n");
		buf.append("       } \n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit unit= pack.createCompilationUnit("E1.java", buf.toString(), false, null);

		List proposals= fetchConvertingProposal(buf, unit);
		assertNull(fConvertLoopProposal);
		assertCorrectLabels(proposals);
	}

    @Test
	public void testBug129508_3() throws Exception {
		IPackageFragment pack= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.Iterator;\n");
		buf.append("import java.util.List;\n");
		buf.append("public class E1 {\n");
		buf.append("    public void foo(List<Integer> list) {\n");
		buf.append("       for (Iterator<Integer> iter = list.iterator(); iter.hasNext();) {\n");
		buf.append("            Integer id = iter.next();\n");
		buf.append("            boolean x= iter.hasNext();\n");
		buf.append("       } \n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit unit= pack.createCompilationUnit("E1.java", buf.toString(), false, null);

		List proposals= fetchConvertingProposal(buf, unit);
		assertNull(fConvertLoopProposal);
		assertCorrectLabels(proposals);
	}

    @Test
	public void testBug129508_4() throws Exception {
		IPackageFragment pack= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.Iterator;\n");
		buf.append("import java.util.List;\n");
		buf.append("public class E1 {\n");
		buf.append("    public void foo(List<Integer> list) {\n");
		buf.append("       for (Iterator<Integer> iter = list.iterator(); iter.hasNext();) {\n");
		buf.append("            Integer id = iter.next();\n");
		buf.append("            Integer id2= iter.next();\n");
		buf.append("       } \n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit unit= pack.createCompilationUnit("E1.java", buf.toString(), false, null);

		List proposals= fetchConvertingProposal(buf, unit);
		assertNull(fConvertLoopProposal);
		assertCorrectLabels(proposals);
	}

    @Test
	public void testBug110599() throws Exception {
		IPackageFragment pack= fSourceFolder.createPackageFragment("a", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package a;\n");
		buf.append("import java.util.Iterator;\n");
		buf.append("import java.util.List;\n");
		buf.append("public class A {\n");
		buf.append("    public void a(List<String> l) {\n");
		buf.append("        //Comment\n");
		buf.append("        for (Iterator<String> iterator = l.iterator(); iterator.hasNext();) {\n");
		buf.append("            String str = iterator.next();\n");
		buf.append("            System.out.println(str);\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit unit= pack.createCompilationUnit("A.java", buf.toString(), false, null);

		List proposals= fetchConvertingProposal(buf, unit);

		assertNotNull(fConvertLoopProposal);

		assertCorrectLabels(proposals);

		String preview= getPreviewContent(fConvertLoopProposal);

		buf= new StringBuffer();
		buf.append("package a;\n");
		buf.append("import java.util.List;\n");
		buf.append("public class A {\n");
		buf.append("    public void a(List<String> l) {\n");
		buf.append("        //Comment\n");
		buf.append("        for (String str : l) {\n");
		buf.append("            System.out.println(str);\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected= buf.toString();
		assertEqualString(preview, expected);
	}

    @Test
	public void testBug176595() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test;\n");
		buf.append("import java.util.Iterator;\n");
		buf.append("import java.util.List;\n");
		buf.append("public class E1 {\n");
		buf.append("    public void foo(List<Object> list1, List list2) {\n");
		buf.append("        for (Iterator<?> it1 = list1.iterator(), it2 = null; it1.hasNext();) {\n");
		buf.append("                Object e1 = it1.next();\n");
		buf.append("                System.out.println(it2.toString());\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E1.java", buf.toString(), false, null);

		fetchConvertingProposal(buf, cu);

		assertNull(fConvertLoopProposal);
	}

    @Test
	public void testBug176502() throws Exception {
		IPackageFragment pack= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.Iterator;\n");
		buf.append("import java.util.List;\n");
		buf.append("import java.util.Vector;\n");
		buf.append("public class E1 {\n");
		buf.append("    public void foo(List<String> l) {\n");
		buf.append("        for (Iterator<String> iterator = l.iterator(); iterator.hasNext();) {\n");
		buf.append("            new Vector<String>();\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit unit= pack.createCompilationUnit("E1.java", buf.toString(), false, null);

		List proposals= fetchConvertingProposal(buf, unit);

		assertNotNull(fConvertLoopProposal);

		assertCorrectLabels(proposals);

		String preview= getPreviewContent(fConvertLoopProposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.List;\n");
		buf.append("import java.util.Vector;\n");
		buf.append("public class E1 {\n");
		buf.append("    public void foo(List<String> l) {\n");
		buf.append("        for (String string : l) {\n");
		buf.append("            new Vector<String>();\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected= buf.toString();
		assertEqualString(preview, expected);
	}

    @Test
	public void testBug203693() throws Exception {
		IPackageFragment pack= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.Collection;\n");
		buf.append("import java.util.Iterator;\n");
		buf.append("\n");
		buf.append("public class E1 {\n");
		buf.append("    public void foo(Collection<String> col) {\n");
		buf.append("        for (Iterator<String> iter = col.iterator(); iter.hasNext();) {\n");
		buf.append("            String item = iter.next();\n");
		buf.append("            System.out.println(item);\n");
		buf.append("\n");
		buf.append("            String dummy = null;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit unit= pack.createCompilationUnit("E1.java", buf.toString(), false, null);

		List proposals= fetchConvertingProposal(buf, unit);

		assertNotNull(fConvertLoopProposal);

		assertCorrectLabels(proposals);

		String preview= getPreviewContent(fConvertLoopProposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.Collection;\n");
		buf.append("\n");
		buf.append("public class E1 {\n");
		buf.append("    public void foo(Collection<String> col) {\n");
		buf.append("        for (String item : col) {\n");
		buf.append("            System.out.println(item);\n");
		buf.append("\n");
		buf.append("            String dummy = null;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected= buf.toString();
		assertEqualString(preview, expected);
	}

    @Test
	public void testBug194639() throws Exception {
		IPackageFragment pack= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test;\n");
		buf.append("import java.util.Collections;\n");
		buf.append("import java.util.Iterator;\n");
		buf.append("public class E01 {\n");
		buf.append("    public void foo(Integer i) {\n");
		buf.append("        for (Iterator iterator = Collections.singleton(i).iterator(); iterator.hasNext();) {\n");
		buf.append("            Integer inter = (Integer) iterator.next();\n");
		buf.append("            System.out.println(inter);\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit unit= pack.createCompilationUnit("E1.java", buf.toString(), false, null);

		List proposals= fetchConvertingProposal(buf, unit);

		assertNotNull(fConvertLoopProposal);

		assertCorrectLabels(proposals);

		String preview= getPreviewContent(fConvertLoopProposal);

		buf= new StringBuffer();
		buf.append("package test;\n");
		buf.append("import java.util.Collections;\n");
		buf.append("public class E01 {\n");
		buf.append("    public void foo(Integer i) {\n");
		buf.append("        for (Object element : Collections.singleton(i)) {\n");
		buf.append("            Integer inter = (Integer) element;\n");
		buf.append("            System.out.println(inter);\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected= buf.toString();
		assertEqualString(preview, expected);
	}

    @Test
	public void testWrongIteratorMethod() throws Exception {
		IPackageFragment pack= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package snippet;\n");
		buf.append("import java.util.Iterator;\n");
		buf.append("import java.util.Set;\n");
		buf.append("interface NavigableSet<T> extends Set<T> {\n");
		buf.append("    Iterator<?> descendingIterator();\n");
		buf.append("}\n");

		buf.append("public class Snippet {\n");
		buf.append("    public static void main(String[] args) {\n");
		buf.append("        NavigableSet<String> set= null;\n");
		buf.append("        for (Iterator<?> it = set.descendingIterator(); it.hasNext();) {\n");
		buf.append("            Object element = it.next();\n");
		buf.append("            System.out.println(element);\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");

		ICompilationUnit unit= pack.createCompilationUnit("E1.java", buf.toString(), false, null);

		List proposals= fetchConvertingProposal(buf, unit);

		assertTrue(fConvertLoopProposal.getFixStatus() != null && fConvertLoopProposal.getFixStatus().getCode() == IStatus.WARNING);

		assertCorrectLabels(proposals);

		assertNotNull(fConvertLoopProposal.getStatusMessage());

		String preview= getPreviewContent(fConvertLoopProposal);

		buf= new StringBuffer();
		buf.append("package snippet;\n");
		buf.append("import java.util.Iterator;\n");
		buf.append("import java.util.Set;\n");
		buf.append("interface NavigableSet<T> extends Set<T> {\n");
		buf.append("    Iterator<?> descendingIterator();\n");
		buf.append("}\n");

		buf.append("public class Snippet {\n");
		buf.append("    public static void main(String[] args) {\n");
		buf.append("        NavigableSet<String> set= null;\n");
		buf.append("        for (Object element : set) {\n");
		buf.append("            System.out.println(element);\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");

		String expected= buf.toString();
		assertEqualString(preview, expected);
	}

    @Test
	public void testWrongIteratorMethod_bug411588() throws Exception {
		IPackageFragment pack= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package p;\n");
		buf.append("import java.util.Iterator;\n");
		buf.append("\n");
		buf.append("public class TestSaveActionConvertToEnhancedForLoop {\n");
		buf.append("    static class Something implements Iterable<Object>{\n");
		buf.append("        @Override\n");
		buf.append("        public Iterator<Object> iterator() {\n");
		buf.append("            return null;\n");
		buf.append("        }\n");
		buf.append("        \n");
		buf.append("        public Iterator<Object> iterator(int filter) {\n");
		buf.append("            return null;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("    \n");
		buf.append("    public static void main(String[] args) {          \n");
		buf.append("        Something s = new Something();\n");
		buf.append("        for (Iterator<Object> it = s.iterator(42) ; it.hasNext(); ) {\n");
		buf.append("             Object obj = it.next();\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");

		ICompilationUnit unit= pack.createCompilationUnit("TestSaveActionConvertToEnhancedForLoop.java", buf.toString(), false, null);

		List proposals= fetchConvertingProposal(buf, unit);

		assertTrue(fConvertLoopProposal.getFixStatus() != null && fConvertLoopProposal.getFixStatus().getCode() == IStatus.WARNING);

		assertCorrectLabels(proposals);

		assertNotNull(fConvertLoopProposal.getStatusMessage());

		String preview= getPreviewContent(fConvertLoopProposal);

		buf= new StringBuffer();
		buf.append("package p;\n");
		buf.append("import java.util.Iterator;\n");
		buf.append("\n");
		buf.append("public class TestSaveActionConvertToEnhancedForLoop {\n");
		buf.append("    static class Something implements Iterable<Object>{\n");
		buf.append("        @Override\n");
		buf.append("        public Iterator<Object> iterator() {\n");
		buf.append("            return null;\n");
		buf.append("        }\n");
		buf.append("        \n");
		buf.append("        public Iterator<Object> iterator(int filter) {\n");
		buf.append("            return null;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("    \n");
		buf.append("    public static void main(String[] args) {          \n");
		buf.append("        Something s = new Something();\n");
		buf.append("        for (Object obj : s) {\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");

		String expected= buf.toString();
		assertEqualString(preview, expected);

	}

    @Test
	public void testCorrectIteratorMethod() throws Exception {
		IPackageFragment pack= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package snippet;\n");
		buf.append("import java.util.Iterator;\n");
		buf.append("import java.util.Set;\n");
		buf.append("interface NavigableSet<T> extends Set<T> {\n");
		buf.append("    Iterator<?> descendingIterator();\n");
		buf.append("}\n");

		buf.append("public class Snippet {\n");
		buf.append("    public static void main(String[] args) {\n");
		buf.append("        NavigableSet<String> set= null;\n");
		buf.append("        for (Iterator<?> it = set.iterator(); it.hasNext();) {\n");
		buf.append("            Object element = it.next();\n");
		buf.append("            System.out.println(element);\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");

		ICompilationUnit unit= pack.createCompilationUnit("E1.java", buf.toString(), false, null);

		List proposals= fetchConvertingProposal(buf, unit);

		assertTrue(fConvertLoopProposal.getFixStatus() != null && fConvertLoopProposal.getFixStatus().isOK());

		assertCorrectLabels(proposals);

		assertNotNull(fConvertLoopProposal.getStatusMessage());

		String preview= getPreviewContent(fConvertLoopProposal);

		buf= new StringBuffer();
		buf.append("package snippet;\n");
		buf.append("import java.util.Iterator;\n");
		buf.append("import java.util.Set;\n");
		buf.append("interface NavigableSet<T> extends Set<T> {\n");
		buf.append("    Iterator<?> descendingIterator();\n");
		buf.append("}\n");

		buf.append("public class Snippet {\n");
		buf.append("    public static void main(String[] args) {\n");
		buf.append("        NavigableSet<String> set= null;\n");
		buf.append("        for (Object element : set) {\n");
		buf.append("            System.out.println(element);\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");

		String expected= buf.toString();
		assertEqualString(preview, expected);
	}
}
