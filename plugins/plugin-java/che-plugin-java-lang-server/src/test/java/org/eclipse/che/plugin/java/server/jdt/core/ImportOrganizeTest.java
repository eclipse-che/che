/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.java.server.jdt.core;

import org.eclipse.che.plugin.java.server.jdt.search.JUnitSourceSetup;
import org.eclipse.che.plugin.java.server.jdt.testplugin.JavaProjectHelper;
import org.eclipse.che.plugin.java.server.jdt.testplugin.ProjectTestSetup;
import org.eclipse.che.plugin.java.server.jdt.testplugin.TestOptions;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jdt.core.search.TypeNameMatch;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.corext.codemanipulation.OrganizeImportsOperation;
import org.eclipse.jdt.internal.corext.codemanipulation.OrganizeImportsOperation.IChooseImportQuery;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Hashtable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


public class ImportOrganizeTest extends CoreTests {

	private static final Class THIS= ImportOrganizeTest.class;

	private IJavaProject fJProject1;

	public ImportOrganizeTest() {
		super(new ProjectTestSetup());
	}

	@Before
	public void setUp() throws Exception {
		super.setUp();
		fJProject1= ProjectTestSetup.getProject();

		Hashtable options= TestOptions.getDefaultOptions();
		options.put(DefaultCodeFormatterConstants.FORMATTER_NUMBER_OF_EMPTY_LINES_TO_PRESERVE, String.valueOf(99));
		JavaCore.setOptions(options);
	}

    @After
	public void tearDown() throws Exception {
		super.tearDown();
		setOrganizeImportSettings(null, 99, 99, fJProject1);
		JavaProjectHelper.clear(fJProject1, ProjectTestSetup.getDefaultClasspath());

		JavaModelManager.getIndexManager().indexAll(fJProject1.getProject());
	}

	protected IChooseImportQuery createQuery(final String name, final String[] choices, final int[] nEntries) {
		return new IChooseImportQuery() {
			public TypeNameMatch[] chooseImports(TypeNameMatch[][] openChoices, ISourceRange[] ranges) {
				assertTrue(name + "-query-nchoices1", choices.length == openChoices.length);
				assertTrue(name + "-query-nchoices2", nEntries.length == openChoices.length);
				for (int i= 0; i < nEntries.length; i++) {
					assertTrue(name + "-query-cnt" + i, openChoices[i].length == nEntries[i]);
				}
				TypeNameMatch[] res= new TypeNameMatch[openChoices.length];
				for (int i= 0; i < openChoices.length; i++) {
					TypeNameMatch[] selection= openChoices[i];
					assertNotNull(name + "-query-setset" + i, selection);
					assertTrue(name + "-query-setlen" + i, selection.length > 0);
					TypeNameMatch found= null;
					for (int k= 0; k < selection.length; k++) {
						if (selection[k].getFullyQualifiedName().equals(choices[i])) {
							found= selection[k];
						}
					}
					assertNotNull(name + "-query-notfound" + i, found);
					res[i]= found;
				}
				return res;
			}
		};
	}

	private void assertImports(ICompilationUnit cu, String[] imports) throws Exception {
		IImportDeclaration[] desc= cu.getImports();
		assertEquals(cu.getElementName() + "-count", imports.length, desc.length);
		for (int i= 0; i < imports.length; i++) {
			assertEquals(cu.getElementName() + "-cmpentries" + i, desc[i].getElementName(), imports[i]);
		}
	}

	@Test
	public void test1() throws Exception {
		File junitSrcArchive= new File(JUnitSourceSetup.class.getClassLoader().getResource(JavaProjectHelper.JUNIT_SRC_381.toOSString()).getFile());
		assertTrue("junit src not found", junitSrcArchive != null && junitSrcArchive.exists());

		JavaProjectHelper.addSourceContainerWithImport(fJProject1, "src", junitSrcArchive, JavaProjectHelper.JUNIT_SRC_ENCODING);

		ICompilationUnit cu= (ICompilationUnit) fJProject1.findElement(new Path("junit/runner/BaseTestRunner.java"));
		assertNotNull("BaseTestRunner.java", cu);

		IPackageFragmentRoot root= (IPackageFragmentRoot)cu.getParent().getParent();
		IPackageFragment pack= root.createPackageFragment("mytest", true, null);

		ICompilationUnit colidingCU= pack.getCompilationUnit("TestListener.java");
		colidingCU.createType("public abstract class TestListener {\n}\n", null, true, null);


		String[] order= new String[0];
		IChooseImportQuery query= createQuery("BaseTestRunner", new String[] { "junit.framework.TestListener" }, new int[] { 2 });

		OrganizeImportsOperation op= createOperation(cu, order, 99, false, true, true, query);
		op.run(null);

		assertImports(cu, new String[] {
			"java.io.BufferedReader",
			"java.io.File",
			"java.io.FileInputStream",
			"java.io.FileOutputStream",
			"java.io.IOException",
			"java.io.InputStream",
			"java.io.PrintWriter",
			"java.io.StringReader",
			"java.io.StringWriter",
			"java.lang.reflect.InvocationTargetException",
			"java.lang.reflect.Method",
			"java.lang.reflect.Modifier",
			"java.text.NumberFormat",
			"java.util.Properties",
			"junit.framework.AssertionFailedError",
			"junit.framework.Test",
			"junit.framework.TestListener",
			"junit.framework.TestSuite"
		});
	}

	@Test
	public void test1WithOrder() throws Exception {
		File junitSrcArchive= new File(JUnitSourceSetup.class.getClassLoader().getResource(JavaProjectHelper.JUNIT_SRC_381.toOSString()).getFile());
		assertTrue("junit src not found", junitSrcArchive != null && junitSrcArchive.exists());

		JavaProjectHelper.addSourceContainerWithImport(fJProject1, "src", junitSrcArchive, JavaProjectHelper.JUNIT_SRC_ENCODING);

		ICompilationUnit cu= (ICompilationUnit) fJProject1.findElement(new Path("junit/runner/BaseTestRunner.java"));
		assertNotNull("BaseTestRunner.java", cu);

		IPackageFragmentRoot root= (IPackageFragmentRoot)cu.getParent().getParent();
		IPackageFragment pack= root.createPackageFragment("mytest", true, null);

		ICompilationUnit colidingCU= pack.getCompilationUnit("TestListener.java");
		colidingCU.createType("public abstract class TestListener {\n}\n", null, true, null);


		String[] order= new String[] { "junit", "java.text", "java.io", "java" };
		IChooseImportQuery query= createQuery("BaseTestRunner", new String[] { "junit.framework.TestListener" }, new int[] { 2 });

		OrganizeImportsOperation op= createOperation(cu, order, 99, false, true, true, query);
		op.run(null);

		assertImports(cu, new String[] {
			"junit.framework.AssertionFailedError",
			"junit.framework.Test",
			"junit.framework.TestListener",
			"junit.framework.TestSuite",
			"java.text.NumberFormat",
			"java.io.BufferedReader",
			"java.io.File",
			"java.io.FileInputStream",
			"java.io.FileOutputStream",
			"java.io.IOException",
			"java.io.InputStream",
			"java.io.PrintWriter",
			"java.io.StringReader",
			"java.io.StringWriter",
			"java.lang.reflect.InvocationTargetException",
			"java.lang.reflect.Method",
			"java.lang.reflect.Modifier",
			"java.util.Properties"
		});
	}

    @Test
	public void test2() throws Exception {
		File junitSrcArchive= new File(JUnitSourceSetup.class.getClassLoader().getResource(JavaProjectHelper.JUNIT_SRC_381.toOSString()).getFile());
		assertTrue("junit src not found", junitSrcArchive != null && junitSrcArchive.exists());

		JavaProjectHelper.addSourceContainerWithImport(fJProject1, "src", junitSrcArchive, JavaProjectHelper.JUNIT_SRC_ENCODING);

		ICompilationUnit cu= (ICompilationUnit) fJProject1.findElement(new Path("junit/runner/LoadingTestCollector.java"));
		assertNotNull("LoadingTestCollector.java", cu);

		String[] order= new String[0];
		IChooseImportQuery query= createQuery("LoadingTestCollector", new String[] { }, new int[] { });

		OrganizeImportsOperation op= createOperation(cu, order, 99, false, true, true, query);
		op.run(null);

		assertImports(cu, new String[] {
			"java.lang.reflect.Modifier",
			"junit.framework.Test",
			"junit.framework.TestSuite",
		});
	}

	@Test
	public void test3() throws Exception {
		File junitSrcArchive= new File(JUnitSourceSetup.class.getClassLoader().getResource(JavaProjectHelper.JUNIT_SRC_381.toOSString()).getFile());
		assertTrue("junit src not found", junitSrcArchive != null && junitSrcArchive.exists());

		JavaProjectHelper.addSourceContainerWithImport(fJProject1, "src", junitSrcArchive, JavaProjectHelper.JUNIT_SRC_ENCODING);

		ICompilationUnit cu= (ICompilationUnit) fJProject1.findElement(new Path("junit/runner/TestCaseClassLoader.java"));
		assertNotNull("TestCaseClassLoader.java", cu);

		String[] order= new String[0];
		IChooseImportQuery query= createQuery("TestCaseClassLoader", new String[] { }, new int[] { });

		OrganizeImportsOperation op= createOperation(cu, order, 3, false, true, true, query);
		op.run(null);

		assertImports(cu, new String[] {
			"java.io.*",
			"java.net.URL",
			"java.util.*",
			"java.util.zip.ZipEntry",
			"java.util.zip.ZipFile",
		});
	}

	@Test
	public void test4() throws Exception {
		File junitSrcArchive= new File(JUnitSourceSetup.class.getClassLoader().getResource(JavaProjectHelper.JUNIT_SRC_381.toOSString()).getFile());
		assertTrue("junit src not found", junitSrcArchive != null && junitSrcArchive.exists());

		JavaProjectHelper.addSourceContainerWithImport(fJProject1, "src", junitSrcArchive, JavaProjectHelper.JUNIT_SRC_ENCODING);

		ICompilationUnit cu= (ICompilationUnit) fJProject1.findElement(new Path("junit/textui/TestRunner.java"));
		assertNotNull("TestRunner.java", cu);

		String[] order= new String[0];
		IChooseImportQuery query= createQuery("TestRunner", new String[] {}, new int[] {});

		OrganizeImportsOperation op= createOperation(cu, order, 99, false, true, true, query);
		op.run(null);

		assertImports(cu, new String[] {
			"java.io.PrintStream",
			"junit.framework.Test",
			"junit.framework.TestResult",
			"junit.framework.TestSuite",
			"junit.runner.BaseTestRunner",
			"junit.runner.StandardTestSuiteLoader",
			"junit.runner.TestSuiteLoader",
			"junit.runner.Version"
		});
	}

	@Test
	public void testVariousTypeReferences() throws Exception {
		IPackageFragmentRoot sourceFolder= JavaProjectHelper.addSourceContainer(fJProject1, "src");

		IPackageFragment pack= sourceFolder.createPackageFragment("test", false, null);
		for (int ch= 'A'; ch < 'M'; ch++) {
			String name= String.valueOf((char) ch);
			ICompilationUnit cu= pack.getCompilationUnit(name + ".java");
			String content= "public class " + name + " {}";
			cu.createType(content, null, false, null);
		}
		for (int ch= 'A'; ch < 'M'; ch++) {
			String name= "I" + String.valueOf((char) ch);
			ICompilationUnit cu= pack.getCompilationUnit(name + ".java");
			String content= "public interface " + name + " {}";
			cu.createType(content, null, false, null);
		}

		StringBuffer buf= new StringBuffer();
		buf.append("public class ImportTest extends A implements IA, IB {\n");
		buf.append("  private B fB;\n");
		buf.append("  private Object fObj= new C();\n");
		buf.append("  public IB foo(IC c, ID d) throws IOException {\n");
		buf.append("   Object local= (D) fObj;\n");
		buf.append("   if (local instanceof E) {};\n");
		buf.append("   return null;\n");
		buf.append("  }\n");
		buf.append("}\n");

		pack= sourceFolder.createPackageFragment("other", false, null);
		ICompilationUnit cu= pack.getCompilationUnit("ImportTest.java");
		cu.createType(buf.toString(), null, false, null);

		String[] order= new String[0];
		IChooseImportQuery query= createQuery("ImportTest", new String[] {}, new int[] {});

		OrganizeImportsOperation op= createOperation(cu, order, 99, false, true, true, query);
		op.run(null);

		assertImports(cu, new String[] {
				"java.io.IOException",
				"test.A",
				"test.B",
				"test.C",
				"test.D",
				"test.E",
				"test.IA",
				"test.IB",
				"test.IC",
				"test.ID",
				});
	}

	@Test
	public void testInnerClassVisibility() throws Exception {
		IPackageFragmentRoot sourceFolder= JavaProjectHelper.addSourceContainer(fJProject1, "src");

		IPackageFragment pack1= sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class C {\n");
		buf.append("  public static class C1 {\n");
		buf.append("    public static class C2 {\n");
		buf.append("    }\n");
		buf.append("  }\n");
		buf.append("}\n");
		pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		IPackageFragment pack2= sourceFolder.createPackageFragment("test2", false, null);

		buf= new StringBuffer();
		buf.append("package test2;\n");

		buf.append("import test2.A.A1;\n");
		buf.append("import test2.A.A1.A2;\n");
		buf.append("import test2.A.A1.A2.A3;\n");
		buf.append("import test2.A.B1;\n");
		buf.append("import test2.A.B1.B2;\n");
		buf.append("import test1.C;\n");
		buf.append("import test1.C.C1.C2;\n");

		buf.append("public class A {\n");
		buf.append("    public static class A1 {\n");
		buf.append("        public static class A2 {\n");
		buf.append("            public static class A3 {\n");
		buf.append("            }\n");
		buf.append("        }\n");
		buf.append("    }\n");

		buf.append("    public static class B1 {\n");
		buf.append("        public static class B2 {\n");
		buf.append("        }\n");

		buf.append("        public static class B3 {\n");
		buf.append("            public static class B4 extends C {\n");
		buf.append("                B4 b4;\n");
		buf.append("                B3 b3;\n");
		buf.append("                B2 b2;\n");
		buf.append("                B1 b1;\n");
		buf.append("                A1 a1;\n");
		buf.append("                A2 a2;\n");
		buf.append("                A3 a3;\n");
		buf.append("                C1 c1;\n");
		buf.append("                C2 c2;\n");
		buf.append("            }\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu2= pack2.createCompilationUnit("A.java", buf.toString(), false, null);


		String[] order= new String[0];
		IChooseImportQuery query= createQuery("A", new String[] {}, new int[] {});

		OrganizeImportsOperation op= createOperation(cu2, order, 99, false, true, true, query);
		op.run(null);

		assertImports(cu2, new String[] {
			"test1.C",
			"test1.C.C1.C2",
			"test2.A.A1.A2",
			"test2.A.A1.A2.A3"
		});
	}


	@Test
	public void testClearImports() throws Exception {
		IPackageFragmentRoot sourceFolder= JavaProjectHelper.addSourceContainer(fJProject1, "src");

		IPackageFragment pack1= sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.Vector;\n");
		buf.append("public class C {\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);


		String[] order= new String[0];
		IChooseImportQuery query= createQuery("C", new String[] {}, new int[] {});

		OrganizeImportsOperation op= createOperation(cu, order, 99, false, true, true, query);
		op.run(null);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class C {\n");
		buf.append("}\n");
		assertEqualString(cu.getSource(), buf.toString());
	}

	@Test
	public void testNewImports() throws Exception {
		IPackageFragmentRoot sourceFolder= JavaProjectHelper.addSourceContainer(fJProject1, "src");

		IPackageFragment pack1= sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class C extends Vector {\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);


		String[] order= new String[0];
		IChooseImportQuery query= createQuery("C", new String[] {}, new int[] {});

		OrganizeImportsOperation op= createOperation(cu, order, 99, false, true, true, query);
		op.run(null);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("\n");
		buf.append("import java.util.Vector;\n");
		buf.append("\n");
		buf.append("public class C extends Vector {\n");
		buf.append("}\n");
		assertEqualString(cu.getSource(), buf.toString());
	}

	@Test
	public void testReplaceImports() throws Exception {
		IPackageFragmentRoot sourceFolder= JavaProjectHelper.addSourceContainer(fJProject1, "src");

		IPackageFragment pack1= sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("\n");
		buf.append("import java.util.Set;\n");
		buf.append("\n");
		buf.append("public class C extends Vector {\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);


		String[] order= new String[0];
		IChooseImportQuery query= createQuery("C", new String[] {}, new int[] {});

		OrganizeImportsOperation op= createOperation(cu, order, 99, false, true, true, query);
		op.run(null);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("\n");
		buf.append("import java.util.Vector;\n");
		buf.append("\n");
		buf.append("public class C extends Vector {\n");
		buf.append("}\n");
		assertEqualString(cu.getSource(), buf.toString());
	}

	@Test
	public void testClearImportsNoPackage() throws Exception {
		IPackageFragmentRoot sourceFolder= JavaProjectHelper.addSourceContainer(fJProject1, "src");

		IPackageFragment pack1= sourceFolder.getPackageFragment("");
		StringBuffer buf= new StringBuffer();
		buf.append("import java.util.Vector;\n");
		buf.append("public class C {\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);


		String[] order= new String[0];
		IChooseImportQuery query= createQuery("C", new String[] {}, new int[] {});

		OrganizeImportsOperation op= createOperation(cu, order, 99, false, true, true, query);
		op.run(null);

		buf= new StringBuffer();
		buf.append("public class C {\n");
		buf.append("}\n");
		assertEqualString(cu.getSource(), buf.toString());
	}

	@Test
	public void testNewImportsNoPackage() throws Exception {
		IPackageFragmentRoot sourceFolder= JavaProjectHelper.addSourceContainer(fJProject1, "src");

		IPackageFragment pack1= sourceFolder.getPackageFragment("");
		StringBuffer buf= new StringBuffer();
		buf.append("public class C extends Vector {\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);


		String[] order= new String[0];
		IChooseImportQuery query= createQuery("C", new String[] {}, new int[] {});

		OrganizeImportsOperation op= createOperation(cu, order, 99, false, true, true, query);
		op.run(null);

		buf= new StringBuffer();
		buf.append("import java.util.Vector;\n");
		buf.append("\n");
		buf.append("public class C extends Vector {\n");
		buf.append("}\n");
		assertEqualString(cu.getSource(), buf.toString());
	}

	@Test
	public void testReplaceImportsNoPackage() throws Exception {
		IPackageFragmentRoot sourceFolder= JavaProjectHelper.addSourceContainer(fJProject1, "src");

		IPackageFragment pack1= sourceFolder.getPackageFragment("");
		StringBuffer buf= new StringBuffer();
		buf.append("import java.util.Set;\n");
		buf.append("\n");
		buf.append("public class C extends Vector {\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);


		String[] order= new String[0];
		IChooseImportQuery query= createQuery("C", new String[] {}, new int[] {});

		OrganizeImportsOperation op= createOperation(cu, order, 99, false, true, true, query);
		op.run(null);

		buf= new StringBuffer();
		buf.append("import java.util.Vector;\n");
		buf.append("\n");
		buf.append("public class C extends Vector {\n");
		buf.append("}\n");
		assertEqualString(cu.getSource(), buf.toString());
	}

	@Test
	public void testCommentAfterImport() throws Exception {
		IPackageFragmentRoot sourceFolder= JavaProjectHelper.addSourceContainer(fJProject1, "src");

		IPackageFragment pack1= sourceFolder.createPackageFragment("pack1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack1;\r\n");
		buf.append("\r\n");
		buf.append("import x;\r\n");
		buf.append("import java.util.Vector; //comment\n");
		buf.append("\r\n");
		buf.append("public class C {\r\n");
		buf.append("    Vector v;\r\n");
		buf.append("}\r\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);


		String[] order= new String[0];
		IChooseImportQuery query= createQuery("C", new String[] {}, new int[] {});

		OrganizeImportsOperation op= createOperation(cu, order, 99, false, true, true, query);
		op.run(null);

		buf= new StringBuffer();
		buf.append("package pack1;\r\n");
		buf.append("\r\n");
		buf.append("import java.util.Vector; //comment\n");
		buf.append("\r\n");
		buf.append("public class C {\r\n");
		buf.append("    Vector v;\r\n");
		buf.append("}\r\n");
		assertEqualString(cu.getSource(), buf.toString());
	}

	@Test
	public void testImportToStar() throws Exception {
		IPackageFragmentRoot sourceFolder= JavaProjectHelper.addSourceContainer(fJProject1, "src");

		IPackageFragment pack2= sourceFolder.createPackageFragment("pack", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack;\n");
		buf.append("public class List {\n");
		buf.append("}\n");
		pack2.createCompilationUnit("List.java", buf.toString(), false, null);


		IPackageFragment pack1= sourceFolder.createPackageFragment("pack1", false, null);
		buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import java.util.Set;\n");
		buf.append("import java.util.Vector;\n");
		buf.append("import java.util.Map;\n");
		buf.append("\n");
		buf.append("import pack.List;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("    Vector v;\n");
		buf.append("    Set v2;\n");
		buf.append("    Map v3;\n");
		buf.append("    List v4;\n");
		buf.append("    String v5;\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);


		String[] order= new String[] { "java", "pack" };
		IChooseImportQuery query= createQuery("C", new String[] {}, new int[] {});

		OrganizeImportsOperation op= createOperation(cu, order, 2, false, true, true, query);
		op.run(null);

		buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import java.util.*;\n");
		buf.append("\n");
		buf.append("import pack.List;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("    Vector v;\n");
		buf.append("    Set v2;\n");
		buf.append("    Map v3;\n");
		buf.append("    List v4;\n");
		buf.append("    String v5;\n");
		buf.append("}\n");
		assertEqualString(cu.getSource(), buf.toString());
	}

	@Test
	public void testImportToStarWithComments() throws Exception {
		IPackageFragmentRoot sourceFolder= JavaProjectHelper.addSourceContainer(fJProject1, "src");

		IPackageFragment pack2= sourceFolder.createPackageFragment("pack", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack;\n");
		buf.append("public class List {\n");
		buf.append("}\n");
		pack2.createCompilationUnit("List.java", buf.toString(), false, null);


		IPackageFragment pack1= sourceFolder.createPackageFragment("pack1", false, null);
		buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("// comment 1\n");
		buf.append("/*lead 1*/import java.util.Set;//test1\n");
		buf.append("/*lead 2*/ import java.util.Vector;/*test2*/\n");
		buf.append("/**lead 3*/import java.util.Map; //test3\n");
		buf.append("/**comment 2*/\n");
		buf.append("import pack.List;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("    Vector v;\n");
		buf.append("    Set v2;\n");
		buf.append("    Map v3;\n");
		buf.append("    List v4;\n");
		buf.append("    String v5;\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);


		String[] order= new String[] { "java", "pack" };
		IChooseImportQuery query= createQuery("C", new String[] {}, new int[] {});

		OrganizeImportsOperation op= createOperation(cu, order, 2, false, true, true, query);
		op.run(null);

		buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("// comment 1\n");
		buf.append("/*lead 1*//*lead 2*/ /**lead 3*/import java.util.*; //test3\n");
		buf.append("//test1/*test2*/\n");
		buf.append("");
		buf.append("\n");
		buf.append("/**comment 2*/\n");
		buf.append("import pack.List;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("    Vector v;\n");
		buf.append("    Set v2;\n");
		buf.append("    Map v3;\n");
		buf.append("    List v4;\n");
		buf.append("    String v5;\n");
		buf.append("}\n");
		assertEqualString(cu.getSource(), buf.toString());
	}

	@Test
	public void testImportToStarWithExplicit() throws Exception {
		IPackageFragmentRoot sourceFolder= JavaProjectHelper.addSourceContainer(fJProject1, "src");

		IPackageFragment pack2= sourceFolder.createPackageFragment("pack", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack;\n");
		buf.append("public class List {\n");
		buf.append("}\n");
		pack2.createCompilationUnit("List.java", buf.toString(), false, null);

		buf= new StringBuffer();
		buf.append("package pack;\n");
		buf.append("public class List2 {\n");
		buf.append("}\n");
		pack2.createCompilationUnit("List2.java", buf.toString(), false, null);


		IPackageFragment pack1= sourceFolder.createPackageFragment("pack1", false, null);
		buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import java.util.Set;\n");
		buf.append("import java.util.Vector;\n");
		buf.append("import java.util.Map;\n");
		buf.append("\n");
		buf.append("import pack.List;\n");
		buf.append("import pack.List2;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("    Vector v;\n");
		buf.append("    Set v2;\n");
		buf.append("    Map v3;\n");
		buf.append("    List v4;\n");
		buf.append("    List2 v5;\n");
		buf.append("    String v6;\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);


		String[] order= new String[] { "java", "pack" };
		IChooseImportQuery query= createQuery("C", new String[] {}, new int[] {});

		OrganizeImportsOperation op= createOperation(cu, order, 2, false, true, true, query);
		op.run(null);

		buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import java.util.*;\n");
		buf.append("\n");
		buf.append("import pack.*;\n");
		buf.append("import pack.List;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("    Vector v;\n");
		buf.append("    Set v2;\n");
		buf.append("    Map v3;\n");
		buf.append("    List v4;\n");
		buf.append("    List2 v5;\n");
		buf.append("    String v6;\n");
		buf.append("}\n");
		assertEqualString(cu.getSource(), buf.toString());
	}

	@Test
	public void testImportToStarWithExplicit2() throws Exception {
		IPackageFragmentRoot sourceFolder= JavaProjectHelper.addSourceContainer(fJProject1, "src");

		IPackageFragment pack2= sourceFolder.createPackageFragment("pack", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack;\n");
		buf.append("public class List {\n");
		buf.append("}\n");
		pack2.createCompilationUnit("List.java", buf.toString(), false, null);

		IPackageFragment pack1= sourceFolder.createPackageFragment("pack1", false, null);
		buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import java.util.Set;\n");
		buf.append("import java.util.Vector;\n");
		buf.append("import java.util.Map;\n");
		buf.append("\n");
		buf.append("import pack.List;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("    Vector v;\n");
		buf.append("    Set v2;\n");
		buf.append("    Map v3;\n");
		buf.append("    List v4;\n");
		buf.append("    String v6;\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);


		String[] order= new String[] { "java", "pack" };
		IChooseImportQuery query= createQuery("C", new String[] {}, new int[] {});

		OrganizeImportsOperation op= createOperation(cu, order, 2, false, true, true, query);
		op.run(null);

		buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import java.util.*;\n");
		buf.append("\n");
		buf.append("import pack.List;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("    Vector v;\n");
		buf.append("    Set v2;\n");
		buf.append("    Map v3;\n");
		buf.append("    List v4;\n");
		buf.append("    String v6;\n");
		buf.append("}\n");
		assertEqualString(cu.getSource(), buf.toString());
	}

	@Test
	public void testImportToStarWithExplicit3() throws Exception {
		IPackageFragmentRoot sourceFolder= JavaProjectHelper.addSourceContainer(fJProject1, "src");

		IPackageFragment pack2= sourceFolder.createPackageFragment("pack", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack;\n");
		buf.append("public class List {\n");
		buf.append("}\n");
		pack2.createCompilationUnit("List.java", buf.toString(), false, null);

		buf= new StringBuffer();
		buf.append("package pack;\n");
		buf.append("public class Set {\n");
		buf.append("}\n");
		pack2.createCompilationUnit("Set.java", buf.toString(), false, null);

		IPackageFragment pack1= sourceFolder.createPackageFragment("pack1", false, null);
		buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import java.util.Set;\n");
		buf.append("import java.util.Vector;\n");
		buf.append("import java.util.Map;\n");
		buf.append("\n");
		buf.append("import pack.List;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("    Vector v;\n");
		buf.append("    Set v2;\n");
		buf.append("    Map v3;\n");
		buf.append("    List v4;\n");
		buf.append("    String v6;\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);


		String[] order= new String[] { "java", "pack" };
		IChooseImportQuery query= createQuery("C", new String[] {}, new int[] {});

		OrganizeImportsOperation op= createOperation(cu, order, 1, false, true, true, query);
		op.run(null);

		buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import java.util.*;\n");
		buf.append("import java.util.Set;\n");
		buf.append("\n");
		buf.append("import pack.List;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("    Vector v;\n");
		buf.append("    Set v2;\n");
		buf.append("    Map v3;\n");
		buf.append("    List v4;\n");
		buf.append("    String v6;\n");
		buf.append("}\n");
		assertEqualString(cu.getSource(), buf.toString());
	}

	@Test
	public void testImportToStarWithExplicit4() throws Exception {
		IPackageFragmentRoot sourceFolder= JavaProjectHelper.addSourceContainer(fJProject1, "src");

		IPackageFragment pack2= sourceFolder.createPackageFragment("pack", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack;\n");
		buf.append("public class List {\n");
		buf.append("}\n");
		pack2.createCompilationUnit("List.java", buf.toString(), false, null);

		IPackageFragment pack3= sourceFolder.createPackageFragment("pack3", false, null);
		buf= new StringBuffer();
		buf.append("package pack3;\n");
		buf.append("public class List {\n");
		buf.append("}\n");
		pack3.createCompilationUnit("List.java", buf.toString(), false, null);

		IPackageFragment pack1= sourceFolder.createPackageFragment("pack1", false, null);
		buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import java.util.Set;\n");
		buf.append("import java.util.Vector;\n");
		buf.append("import java.util.Map;\n");
		buf.append("\n");
		buf.append("import pack.List;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("    Vector v;\n");
		buf.append("    Set v2;\n");
		buf.append("    Map v3;\n");
		buf.append("    List v4;\n");
		buf.append("    String v6;\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);


		String[] order= new String[] { "java", "pack" };
		IChooseImportQuery query= createQuery("C", new String[] {}, new int[] {});

		OrganizeImportsOperation op= createOperation(cu, order, 1, false, true, true, query);
		op.run(null);

		buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import java.util.*;\n");
		buf.append("\n");
		buf.append("import pack.List;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("    Vector v;\n");
		buf.append("    Set v2;\n");
		buf.append("    Map v3;\n");
		buf.append("    List v4;\n");
		buf.append("    String v6;\n");
		buf.append("}\n");
		assertEqualString(cu.getSource(), buf.toString());
	}


	@Test
	public void testImportToStarWithExplicit5() throws Exception {
		// unrelated project, to fill the all types cache
		IJavaProject project2 = JavaProjectHelper.createJavaProject("TestProject2", "bin");
		try {
			assertTrue("rt not found", JavaProjectHelper.addRTJar(project2) != null);
			IPackageFragmentRoot sourceFolder2= JavaProjectHelper.addSourceContainer(project2, "src");

			IPackageFragment pack22= sourceFolder2.createPackageFragment("packx", false, null);
			StringBuffer buf= new StringBuffer();
			buf.append("package pack;\n");
			buf.append("public class Vector {\n");
			buf.append("}\n");
			pack22.createCompilationUnit("List.java", buf.toString(), false, null);

			buf= new StringBuffer();
			buf.append("package pack;\n");
			buf.append("public class Set {\n");
			buf.append("}\n");
			pack22.createCompilationUnit("Set.java", buf.toString(), false, null);

			IPackageFragmentRoot sourceFolder= JavaProjectHelper.addSourceContainer(fJProject1, "src");

			IPackageFragment pack2= sourceFolder.createPackageFragment("pack", false, null);
			buf= new StringBuffer();
			buf.append("package pack;\n");
			buf.append("public class List {\n");
			buf.append("}\n");
			pack2.createCompilationUnit("List.java", buf.toString(), false, null);

			IPackageFragment pack1= sourceFolder.createPackageFragment("pack1", false, null);
			buf= new StringBuffer();
			buf.append("package pack1;\n");
			buf.append("\n");
			buf.append("import java.util.Set;\n");
			buf.append("import java.util.Vector;\n");
			buf.append("import java.util.Map;\n");
			buf.append("\n");
			buf.append("import pack.List;\n");
			buf.append("\n");
			buf.append("public class C {\n");
			buf.append("    Vector v;\n");
			buf.append("    Set v2;\n");
			buf.append("    Map v3;\n");
			buf.append("    List v4;\n");
			buf.append("    String v6;\n");
			buf.append("}\n");
			ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);


			String[] order= new String[] { "java", "pack" };
			IChooseImportQuery query= createQuery("C", new String[] {}, new int[] {});

			OrganizeImportsOperation op= createOperation(cu, order, 1, false, true, true, query);
			op.run(null);

			buf= new StringBuffer();
			buf.append("package pack1;\n");
			buf.append("\n");
			buf.append("import java.util.*;\n");
			buf.append("\n");
			buf.append("import pack.List;\n");
			buf.append("\n");
			buf.append("public class C {\n");
			buf.append("    Vector v;\n");
			buf.append("    Set v2;\n");
			buf.append("    Map v3;\n");
			buf.append("    List v4;\n");
			buf.append("    String v6;\n");
			buf.append("}\n");
			assertEqualString(cu.getSource(), buf.toString());
		} finally {
			JavaProjectHelper.delete(project2);
		}
	}

	@Test
	public void testGroups1() throws Exception {
		IPackageFragmentRoot sourceFolder= JavaProjectHelper.addSourceContainer(fJProject1, "src");

		IPackageFragment pack2= sourceFolder.createPackageFragment("pack0", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack0;\n");
		buf.append("public class List1 {\n");
		buf.append("}\n");
		pack2.createCompilationUnit("List1.java", buf.toString(), false, null);

		IPackageFragment pack1= sourceFolder.createPackageFragment("pack1", false, null);
		buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("    File f;\n");
		buf.append("    IOException f1;\n");
		buf.append("    RandomAccessFile f2;\n");
		buf.append("    ArrayList f3;\n");
		buf.append("    List1 f4;\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		String[] order= new String[] { "java.io", "java.util" };
		IChooseImportQuery query= createQuery("C", new String[] {}, new int[] {});

		OrganizeImportsOperation op= createOperation(cu, order, 99, false, true, true, query);
		op.run(null);

		buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import java.io.File;\n");
		buf.append("import java.io.IOException;\n");
		buf.append("import java.io.RandomAccessFile;\n");
		buf.append("\n");
		buf.append("import java.util.ArrayList;\n");
		buf.append("\n");
		buf.append("import pack0.List1;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("    File f;\n");
		buf.append("    IOException f1;\n");
		buf.append("    RandomAccessFile f2;\n");
		buf.append("    ArrayList f3;\n");
		buf.append("    List1 f4;\n");
		buf.append("}\n");
		assertEqualString(cu.getSource(), buf.toString());
	}

	@Test
	public void testImportFromDefault() throws Exception {
		IPackageFragmentRoot sourceFolder= JavaProjectHelper.addSourceContainer(fJProject1, "src");

		IPackageFragment pack2= sourceFolder.createPackageFragment("", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("public class List1 {\n");
		buf.append("}\n");
		pack2.createCompilationUnit("List1.java", buf.toString(), false, null);

		IPackageFragment pack1= sourceFolder.createPackageFragment("pack1", false, null);
		buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import java.util.Set;\n");
		buf.append("import java.util.Vector;\n");
		buf.append("import java.util.Map;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("    Vector v;\n");
		buf.append("    Set v2;\n");
		buf.append("    Map v3;\n");
		buf.append("    List1 v4;\n");
		buf.append("    String v5;\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);


		String[] order= new String[] { "java", "pack" };
		IChooseImportQuery query= createQuery("C", new String[] {}, new int[] {});

		OrganizeImportsOperation op= createOperation(cu, order, 2, false, true, true, query);
		op.run(null);

		buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import java.util.*;\n");  // no imports from default in compatibility >= 1.4
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("    Vector v;\n");
		buf.append("    Set v2;\n");
		buf.append("    Map v3;\n");
		buf.append("    List1 v4;\n");
		buf.append("    String v5;\n");
		buf.append("}\n");
		assertEqualString(cu.getSource(), buf.toString());
	}

	@Test
	public void testImportFromDefaultWithStar() throws Exception {
		IPackageFragmentRoot sourceFolder= JavaProjectHelper.addSourceContainer(fJProject1, "src");

		IPackageFragment pack2= sourceFolder.createPackageFragment("", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("public class List1 {\n");
		buf.append("}\n");
		pack2.createCompilationUnit("List1.java", buf.toString(), false, null);

		buf= new StringBuffer();
		buf.append("public class List2 {\n");
		buf.append("}\n");
		pack2.createCompilationUnit("List2.java", buf.toString(), false, null);


		IPackageFragment pack1= sourceFolder.createPackageFragment("pack1", false, null);
		buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import java.util.Set;\n");  // no imports from default in compatibility >= 1.4
		buf.append("import java.util.Vector;\n");
		buf.append("import java.util.Map;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("    Vector v;\n");
		buf.append("    Set v2;\n");
		buf.append("    Map v3;\n");
		buf.append("    List1 v4;\n");
		buf.append("    List2 v5;\n");
		buf.append("    String v6;\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);


		String[] order= new String[] { "java", "pack" };
		IChooseImportQuery query= createQuery("C", new String[] {}, new int[] {});

		OrganizeImportsOperation op= createOperation(cu, order, 2, false, true, true, query);
		op.run(null);

		buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import java.util.*;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("    Vector v;\n");
		buf.append("    Set v2;\n");
		buf.append("    Map v3;\n");
		buf.append("    List1 v4;\n");
		buf.append("    List2 v5;\n");
		buf.append("    String v6;\n");
		buf.append("}\n");
		assertEqualString(cu.getSource(), buf.toString());
	}

	@Test
	public void testImportOfMemberFromLocal() throws Exception {
		IPackageFragmentRoot sourceFolder= JavaProjectHelper.addSourceContainer(fJProject1, "src");


		IPackageFragment pack1= sourceFolder.createPackageFragment("pack1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("    public void foo() {\n");
		buf.append("        class Local {\n");
		buf.append("            class LocalMember {\n");
		buf.append("            }\n");
		buf.append("            LocalMember x;\n");
		buf.append("            Vector v;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);


		String[] order= new String[] { "java", "pack" };
		IChooseImportQuery query= createQuery("C", new String[] {}, new int[] {});

		OrganizeImportsOperation op= createOperation(cu, order, 99, false, true, true, query);
		op.run(null);

		buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import java.util.Vector;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("    public void foo() {\n");
		buf.append("        class Local {\n");
		buf.append("            class LocalMember {\n");
		buf.append("            }\n");
		buf.append("            LocalMember x;\n");
		buf.append("            Vector v;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(cu.getSource(), buf.toString());
	}

	@Test
	public void testBaseGroups1() throws Exception {
		IPackageFragmentRoot sourceFolder= JavaProjectHelper.addSourceContainer(fJProject1, "src");

		IPackageFragment pack2= sourceFolder.createPackageFragment("pack0", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack0;\n");
		buf.append("public class List1 {\n");
		buf.append("}\n");
		pack2.createCompilationUnit("List1.java", buf.toString(), false, null);

		IPackageFragment pack1= sourceFolder.createPackageFragment("pack1", false, null);
		buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("    File f;\n");
		buf.append("    IOException f1;\n");
		buf.append("    RandomAccessFile f2;\n");
		buf.append("    ArrayList f3;\n");
		buf.append("    List1 f4;\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);


		String[] order= new String[] { "java", "java.io" };
		IChooseImportQuery query= createQuery("C", new String[] {}, new int[] {});

		OrganizeImportsOperation op= createOperation(cu, order, 99, false, true, true, query);
		op.run(null);

		buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import java.util.ArrayList;\n");
		buf.append("\n");
		buf.append("import java.io.File;\n");
		buf.append("import java.io.IOException;\n");
		buf.append("import java.io.RandomAccessFile;\n");
		buf.append("\n");
		buf.append("import pack0.List1;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("    File f;\n");
		buf.append("    IOException f1;\n");
		buf.append("    RandomAccessFile f2;\n");
		buf.append("    ArrayList f3;\n");
		buf.append("    List1 f4;\n");
		buf.append("}\n");
		assertEqualString(cu.getSource(), buf.toString());
	}

	@Test
	public void testVisibility_bug26746() throws Exception {
		IPackageFragmentRoot sourceFolder= JavaProjectHelper.addSourceContainer(fJProject1, "src");

		IPackageFragment pack2= sourceFolder.createPackageFragment("pack0", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack0;\n");
		buf.append("public interface MyInterface {\n");
		buf.append("	public interface MyInnerInterface {\n");
		buf.append("	}\n");
		buf.append("}\n");
		pack2.createCompilationUnit("MyInterface.java", buf.toString(), false, null);

		buf= new StringBuffer();
		buf.append("package pack0;\n");
		buf.append("\n");
		buf.append("import pack0.MyInterface.MyInnerInterface;\n");
		buf.append("public class MyClass implements MyInterface {\n");
		buf.append("	public MyInnerInterface myMethod() {\n");
		buf.append("		return null;\n");
		buf.append("	}\n");
		buf.append("}\n");
		ICompilationUnit cu= pack2.createCompilationUnit("MyClass.java", buf.toString(), false, null);


		String[] order= new String[] {};
		IChooseImportQuery query= createQuery("MyClass", new String[] {}, new int[] {});

		OrganizeImportsOperation op= createOperation(cu, order, 99, false, true, true, query);
		op.run(null);

		buf= new StringBuffer();
		buf.append("package pack0;\n");
		buf.append("\n");
		buf.append("public class MyClass implements MyInterface {\n");
		buf.append("	public MyInnerInterface myMethod() {\n");
		buf.append("		return null;\n");
		buf.append("	}\n");
		buf.append("}\n");
		assertEqualString(cu.getSource(), buf.toString());
	}

	@Test
	public void testVisibility_bug37299a() throws Exception {
		IPackageFragmentRoot sourceFolder= JavaProjectHelper.addSourceContainer(fJProject1, "src");

		IPackageFragment pack1= sourceFolder.createPackageFragment("pack1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("public class ClusterSingletonStepped {\n");
		buf.append("	public interface SingletonStep {\n");
		buf.append("	}\n");
		buf.append("}\n");
		pack1.createCompilationUnit("ClusterSingletonStepped.java", buf.toString(), false, null);

		IPackageFragment pack2= sourceFolder.createPackageFragment("pack0", false, null);
		buf= new StringBuffer();
		buf.append("package pack0;\n");
		buf.append("\n");
		buf.append("import pack1.ClusterSingletonStepped;\n");
		buf.append("import pack1.ClusterSingletonStepped.SingletonStep;\n");
		buf.append("\n");
		buf.append("public class TestFile extends ClusterSingletonStepped implements SingletonStep {\n");
		buf.append("    SingletonStep step;\n");
		buf.append("}\n");
		ICompilationUnit cu= pack2.createCompilationUnit("TestFile.java", buf.toString(), false, null);


		String[] order= new String[] {};
		IChooseImportQuery query= createQuery("TestFile", new String[] {}, new int[] {});

		OrganizeImportsOperation op= createOperation(cu, order, 99, false, true, true, query);
		op.run(null);

		buf= new StringBuffer();
		buf.append("package pack0;\n");
		buf.append("\n");
		buf.append("import pack1.ClusterSingletonStepped;\n");
		buf.append("import pack1.ClusterSingletonStepped.SingletonStep;\n");
		buf.append("\n");
		buf.append("public class TestFile extends ClusterSingletonStepped implements SingletonStep {\n");
		buf.append("    SingletonStep step;\n");
		buf.append("}\n");
		assertEqualString(cu.getSource(), buf.toString());
	}

	@Test
	public void testVisibility_bug37299b() throws Exception {
		IPackageFragmentRoot sourceFolder= JavaProjectHelper.addSourceContainer(fJProject1, "src");

		IPackageFragment pack1= sourceFolder.createPackageFragment("pack1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("public class ClusterSingletonStepped {\n");
		buf.append("	public interface SingletonStep {\n");
		buf.append("	}\n");
		buf.append("}\n");
		pack1.createCompilationUnit("ClusterSingletonStepped.java", buf.toString(), false, null);

		IPackageFragment pack2= sourceFolder.createPackageFragment("pack0", false, null);
		buf= new StringBuffer();
		buf.append("package pack0;\n");
		buf.append("\n");
		buf.append("import pack1.ClusterSingletonStepped;\n");
		buf.append("import pack1.ClusterSingletonStepped.SingletonStep;\n");
		buf.append("\n");
		buf.append("public class TestFile extends ClusterSingletonStepped {\n");
		buf.append("    SingletonStep step;\n");
		buf.append("}\n");
		ICompilationUnit cu= pack2.createCompilationUnit("TestFile.java", buf.toString(), false, null);


		String[] order= new String[] {};
		IChooseImportQuery query= createQuery("TestFile", new String[] {}, new int[] {});

		OrganizeImportsOperation op= createOperation(cu, order, 99, false, true, true, query);
		op.run(null);

		buf= new StringBuffer();
		buf.append("package pack0;\n");
		buf.append("\n");
		buf.append("import pack1.ClusterSingletonStepped;\n");
		buf.append("\n");
		buf.append("public class TestFile extends ClusterSingletonStepped {\n");
		buf.append("    SingletonStep step;\n");
		buf.append("}\n");
		assertEqualString(cu.getSource(), buf.toString());
	}

	@Test
	public void testVisibility_bug56704() throws Exception {
		IPackageFragmentRoot sourceFolder= JavaProjectHelper.addSourceContainer(fJProject1, "src");

		IPackageFragment pack2= sourceFolder.createPackageFragment("pack0", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack0;\n");
		buf.append("public class A {\n");
		buf.append("	public class AX {\n");
		buf.append("	}\n");
		buf.append("}\n");
		pack2.createCompilationUnit("A.java", buf.toString(), false, null);

		buf= new StringBuffer();
		buf.append("package pack0;\n");
		buf.append("\n");
		buf.append("import pack0.A.AX;\n");
		buf.append("public class B extends A {\n");
		buf.append("	public class BX extends AX {\n");
		buf.append("	}\n");
		buf.append("}\n");
		ICompilationUnit cu= pack2.createCompilationUnit("B.java", buf.toString(), false, null);


		String[] order= new String[] {};
		IChooseImportQuery query= createQuery("MyClass", new String[] {}, new int[] {});

		OrganizeImportsOperation op= createOperation(cu, order, 99, false, true, true, query);
		op.run(null);

		buf= new StringBuffer();
		buf.append("package pack0;\n");
		buf.append("\n");
		buf.append("public class B extends A {\n");
		buf.append("	public class BX extends AX {\n");
		buf.append("	}\n");
		buf.append("}\n");
		assertEqualString(cu.getSource(), buf.toString());
	}

	@Test
	public void testVisibility_bug67644() throws Exception {
		IPackageFragmentRoot sourceFolder= JavaProjectHelper.addSourceContainer(fJProject1, "src");

		IPackageFragment pack1= sourceFolder.createPackageFragment("pack1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("public class A {\n");
		buf.append("	public class AX {\n");
		buf.append("	}\n");
		buf.append("}\n");
		pack1.createCompilationUnit("A.java", buf.toString(), false, null);

		IPackageFragment pack2= sourceFolder.createPackageFragment("pack2", false, null);

		buf= new StringBuffer();
		buf.append("package pack2;\n");
		buf.append("\n");
		buf.append("import pack1.A;\n");
		buf.append("import pack1.AX;\n");
		buf.append("public class B {\n");
		buf.append("	public void foo() {\n");
		buf.append("	  Object x= new A().new AX();\n");
		buf.append("	}\n");
		buf.append("}\n");
		ICompilationUnit cu= pack2.createCompilationUnit("B.java", buf.toString(), false, null);


		String[] order= new String[] {};
		IChooseImportQuery query= createQuery("testVisibility_bug67644", new String[] {}, new int[] {});

		OrganizeImportsOperation op= createOperation(cu, order, 99, false, true, true, query);
		op.run(null);

		buf= new StringBuffer();
		buf.append("package pack2;\n");
		buf.append("\n");
		buf.append("import pack1.A;\n");
		buf.append("public class B {\n");
		buf.append("	public void foo() {\n");
		buf.append("	  Object x= new A().new AX();\n");
		buf.append("	}\n");
		buf.append("}\n");
		assertEqualString(cu.getSource(), buf.toString());
	}

	@Test
	public void testVisibility_bug85831() throws Exception {
		IPackageFragmentRoot sourceFolder= JavaProjectHelper.addSourceContainer(fJProject1, "src");

		IPackageFragment pack2= sourceFolder.createPackageFragment("pack2", false, null);

		StringBuffer buf= new StringBuffer();
		buf.append("package pack2;\n");
		buf.append("\n");
		buf.append("class A {\n");
		buf.append("	public class AX {\n");
		buf.append("	}\n");
		buf.append("}\n");

		buf.append("public class B {\n");
		buf.append("	Object x= new A().new AX();\n");
		buf.append("}\n");
		ICompilationUnit cu= pack2.createCompilationUnit("B.java", buf.toString(), false, null);


		String[] order= new String[] {};
		IChooseImportQuery query= createQuery("testVisibility_bug85831", new String[] {}, new int[] {});

		OrganizeImportsOperation op= createOperation(cu, order, 99, false, true, true, query);
		op.run(null);

		buf= new StringBuffer();
		buf.append("package pack2;\n");
		buf.append("\n");
		buf.append("class A {\n");
		buf.append("	public class AX {\n");
		buf.append("	}\n");
		buf.append("}\n");

		buf.append("public class B {\n");
		buf.append("	Object x= new A().new AX();\n");
		buf.append("}\n");
		assertEqualString(cu.getSource(), buf.toString());
	}

	@Test
	public void testVisibility_bug79174() throws Exception {
		IPackageFragmentRoot sourceFolder= JavaProjectHelper.addSourceContainer(fJProject1, "src");

		IPackageFragment pack1= sourceFolder.createPackageFragment("pack1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("public interface A<X> {\n");
		buf.append("	public interface AX<Y> {\n");
		buf.append("	}\n");
		buf.append("}\n");
		pack1.createCompilationUnit("A.java", buf.toString(), false, null);

		IPackageFragment pack2= sourceFolder.createPackageFragment("pack2", false, null);

		buf= new StringBuffer();
		buf.append("package pack2;\n");
		buf.append("\n");
		buf.append("import pack1.A;\n");
		buf.append("import pack1.AX;\n");
		buf.append("public class B implements A<String> {\n");
		buf.append("	public void foo(AX<String> a) {\n");
		buf.append("	}\n");
		buf.append("}\n");
		ICompilationUnit cu= pack2.createCompilationUnit("B.java", buf.toString(), false, null);


		String[] order= new String[] {};
		IChooseImportQuery query= createQuery("testVisibility_bug79174", new String[] {}, new int[] {});

		OrganizeImportsOperation op= createOperation(cu, order, 99, false, true, true, query);
		op.run(null);

		buf= new StringBuffer();
		buf.append("package pack2;\n");
		buf.append("\n");
		buf.append("import pack1.A;\n");
		buf.append("public class B implements A<String> {\n");
		buf.append("	public void foo(AX<String> a) {\n");
		buf.append("	}\n");
		buf.append("}\n");
		assertEqualString(cu.getSource(), buf.toString());
	}

	@Test
	public void testVisibility_bug131305() throws Exception {
		IPackageFragmentRoot sourceFolder= JavaProjectHelper.addSourceContainer(fJProject1, "src");


		IPackageFragment packUtil= sourceFolder.createPackageFragment("util", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package util;\n");
		buf.append("\n");
		buf.append("public interface Map \n");
		buf.append("        public static interface Entry {\n");
		buf.append("        }\n");
		buf.append("}\n");
		packUtil.createCompilationUnit("Map.java", buf.toString(), false, null);

		buf= new StringBuffer();
		buf.append("package util;\n");
		buf.append("\n");
		buf.append("public interface HashMap implements Map {\n");
		buf.append("        private static interface Entry {\n");
		buf.append("        }\n");
		buf.append("}\n");
		packUtil.createCompilationUnit("HashMap.java", buf.toString(), false, null);

		IPackageFragment pack1= sourceFolder.createPackageFragment("pack1", false, null);
		buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import util.HashMap;\n");
		buf.append("import util.Map;\n");
		buf.append("import util.Map.Entry;\n");
		buf.append("\n");
		buf.append("public class A extends HashMap {\n");
		buf.append("        public A(Map m, Entry e) {\n");
		buf.append("        }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("A.java", buf.toString(), false, null);

		String[] order= new String[] {};
		IChooseImportQuery query= createQuery("testVisibility_bug131305", new String[] {}, new int[] {});

		OrganizeImportsOperation op= createOperation(cu, order, 99, false, true, true, query);
		op.run(null);

		assertEqualString(cu.getSource(), buf.toString()); // no changes, import for Entry is required
	}

	@Test
	public void testVisibility_bug159638() throws Exception {
		IPackageFragmentRoot sourceFolder= JavaProjectHelper.addSourceContainer(fJProject1, "src");


		IPackageFragment pack0= sourceFolder.createPackageFragment("pack0", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack0;\n");
		buf.append("public abstract class Parent<E> {\n");
		buf.append("    public static class Inner {\n");
		buf.append("    }\n");
		buf.append("    public @interface Tag{\n");
		buf.append("        String value();\n");
		buf.append("    }\n");
		buf.append("}\n");
		pack0.createCompilationUnit("Parent.java", buf.toString(), false, null);


		IPackageFragment pack1= sourceFolder.createPackageFragment("pack1", false, null);
		buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import pack0.Parent;\n");
		buf.append("import pack0.Parent.Inner;\n");
		buf.append("import pack0.Parent.Tag;\n");
		buf.append("\n");
		buf.append("@Tag(\"foo\")\n");
		buf.append("public class Child extends Parent<Inner> {\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("Child.java", buf.toString(), false, null);

		String[] order= new String[] {};
		IChooseImportQuery query= createQuery("testVisibility_bug159638", new String[] {}, new int[] {});

		OrganizeImportsOperation op= createOperation(cu, order, 99, false, true, true, query);
		op.run(null);

		assertEqualString(cu.getSource(), buf.toString()); // no changes, imports for Inner and tag are required
	}

	@Test
	public void test5() throws Exception {

		String[] types= new String[] {
			"org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader",
			"org.eclipse.core.resources.IContainer",
			"org.eclipse.core.runtime.IPath",
			"org.eclipse.core.runtime.CoreException",
			"org.eclipse.core.resources.IResource",
			"org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer"
		};
		String[] order= new String[] { "org.eclipse.jdt", "org.eclipse" };

		IPackageFragmentRoot sourceFolder= JavaProjectHelper.addSourceContainer(fJProject1, "src");
		for (int i= 0; i < types.length; i++) {
			String pack= Signature.getQualifier(types[i]);
			String name= Signature.getSimpleName(types[i]);

			IPackageFragment pack2= sourceFolder.createPackageFragment(pack, false, null);
			StringBuffer buf= new StringBuffer();
			buf.append("package "); buf.append(pack); buf.append(";\n");
			buf.append("public class "); buf.append(name); buf.append(" {\n");
			buf.append("}\n");
			pack2.createCompilationUnit(name + ".java", buf.toString(), false, null);
		}

		StringBuffer body= new StringBuffer();
		body.append("public class C {\n");
		for (int i= 0; i < types.length; i++) {
			String name= Signature.getSimpleName(types[i]);
			body.append(name); body.append(" a"); body.append(i); body.append(";\n");
		}
		body.append("}\n");

		IPackageFragment pack1= sourceFolder.createPackageFragment("pack1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append(body.toString());

		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);
		IChooseImportQuery query= createQuery("C", new String[] {}, new int[] {});

		OrganizeImportsOperation op= createOperation(cu, order, 99, false, true, true, query);
		op.run(null);

		buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;\n");
		buf.append("import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;\n");
		buf.append("\n");
		buf.append("import org.eclipse.core.resources.IContainer;\n");
		buf.append("import org.eclipse.core.resources.IResource;\n");
		buf.append("import org.eclipse.core.runtime.CoreException;\n");
		buf.append("import org.eclipse.core.runtime.IPath;\n");
		buf.append("\n");
		buf.append(body.toString());

		assertEqualString(cu.getSource(), buf.toString());
	}

	@Test
	public void test_bug25773() throws Exception {

		String[] types= new String[] {
			"java.util.Vector",
			"java.util.Map",
			"java.util.Set",
			"org.eclipse.gef.X1",
			"org.eclipse.gef.X2",
			"org.eclipse.gef.X3",
			"org.eclipse.core.runtime.IAdaptable",
			"org.eclipse.draw2d.IFigure",
			"org.eclipse.draw2d.LayoutManager",
			"org.eclipse.draw2d.geometry.Point",
			"org.eclipse.draw2d.geometry.Rectangle",
			"org.eclipse.swt.accessibility.ACC",
			"org.eclipse.swt.accessibility.AccessibleControlEvent"
		};

		String[] order= new String[] { "java", "org.eclipse", "org.eclipse.gef", "org.eclipse.draw2d", "org.eclipse.gef.examples" };
		int threshold= 3;

		IPackageFragmentRoot sourceFolder= JavaProjectHelper.addSourceContainer(fJProject1, "src");
		for (int i= 0; i < types.length; i++) {
			String pack= Signature.getQualifier(types[i]);
			if (!pack.startsWith("java.")) {
				String name= Signature.getSimpleName(types[i]);

				IPackageFragment pack2= sourceFolder.createPackageFragment(pack, false, null);
				StringBuffer buf= new StringBuffer();
				buf.append("package "); buf.append(pack); buf.append(";\n");
				buf.append("public class "); buf.append(name); buf.append(" {\n");
				buf.append("}\n");
				pack2.createCompilationUnit(name + ".java", buf.toString(), false, null);
			}
		}

		StringBuffer body= new StringBuffer();
		body.append("public class C {\n");
		for (int i= 0; i < types.length; i++) {
			String name= Signature.getSimpleName(types[i]);
			body.append(name); body.append(" a"); body.append(i); body.append(";\n");
		}
		body.append("}\n");

		IPackageFragment pack1= sourceFolder.createPackageFragment("pack1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append(body.toString());

		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);
		IChooseImportQuery query= createQuery("C", new String[] {}, new int[] {});

		OrganizeImportsOperation op= createOperation(cu, order, threshold, false, true, true, query);
		op.run(null);

		buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import java.util.*;\n");
		buf.append("\n");
		buf.append("import org.eclipse.core.runtime.IAdaptable;\n");
		buf.append("import org.eclipse.swt.accessibility.ACC;\n");
		buf.append("import org.eclipse.swt.accessibility.AccessibleControlEvent;\n");
		buf.append("\n");
		buf.append("import org.eclipse.gef.*;\n");
		buf.append("\n");
		buf.append("import org.eclipse.draw2d.IFigure;\n");
		buf.append("import org.eclipse.draw2d.LayoutManager;\n");
		buf.append("import org.eclipse.draw2d.geometry.Point;\n");
		buf.append("import org.eclipse.draw2d.geometry.Rectangle;\n");
		buf.append("\n");
		buf.append(body.toString());
		assertEqualString(cu.getSource(), buf.toString());
	}

	@Test
	public void test_bug25113() throws Exception {

		String[] types= new String[] {
			"com.mycompany.Class1",
			"com.foreigncompany.Class2",
			"com.foreigncompany.Class3",
			"com.mycompany.Class4",
			"com.misc.Class5"
		};

		String[] order= new String[] { "com", "com.foreigncompany", "com.mycompany" };
		int threshold= 99;

		IPackageFragmentRoot sourceFolder= JavaProjectHelper.addSourceContainer(fJProject1, "src");
		for (int i= 0; i < types.length; i++) {
			String pack= Signature.getQualifier(types[i]);
			if (!pack.startsWith("java.")) {
				String name= Signature.getSimpleName(types[i]);

				IPackageFragment pack2= sourceFolder.createPackageFragment(pack, false, null);
				StringBuffer buf= new StringBuffer();
				buf.append("package "); buf.append(pack); buf.append(";\n");
				buf.append("public class "); buf.append(name); buf.append(" {\n");
				buf.append("}\n");
				pack2.createCompilationUnit(name + ".java", buf.toString(), false, null);
			}
		}

		StringBuffer body= new StringBuffer();
		body.append("public class C {\n");
		for (int i= 0; i < types.length; i++) {
			String name= Signature.getSimpleName(types[i]);
			body.append(name); body.append(" a"); body.append(i); body.append(";\n");
		}
		body.append("}\n");

		IPackageFragment pack1= sourceFolder.createPackageFragment("pack1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append(body.toString());

		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);
		IChooseImportQuery query= createQuery("C", new String[] {}, new int[] {});

		OrganizeImportsOperation op= createOperation(cu, order, threshold, false, true, true, query);
		op.run(null);

		buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import com.misc.Class5;\n");
		buf.append("\n");
		buf.append("import com.foreigncompany.Class2;\n");
		buf.append("import com.foreigncompany.Class3;\n");
		buf.append("\n");
		buf.append("import com.mycompany.Class1;\n");
		buf.append("import com.mycompany.Class4;\n");
		buf.append("\n");
		buf.append(body.toString());
		assertEqualString(cu.getSource(), buf.toString());
	}

	@Test
	public void testStaticImports1() throws Exception {
		IPackageFragmentRoot sourceFolder= JavaProjectHelper.addSourceContainer(fJProject1, "src");


		IPackageFragment pack1= sourceFolder.createPackageFragment("pack1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import static java.lang.System.out;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("    public int foo() {\n");
		buf.append("        out.print(File.separator);\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);


		String[] order= new String[] { "java", "pack", "#java" };
		IChooseImportQuery query= createQuery("C", new String[] {}, new int[] {});

		OrganizeImportsOperation op= createOperation(cu, order, 99, false, true, true, query);
		op.run(null);

		buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import java.io.File;\n");
		buf.append("\n");
		buf.append("import static java.lang.System.out;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("    public int foo() {\n");
		buf.append("        out.print(File.separator);\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(cu.getSource(), buf.toString());
	}

	@Test
	public void testStaticImports2() throws Exception {
		IPackageFragmentRoot sourceFolder= JavaProjectHelper.addSourceContainer(fJProject1, "src");


		IPackageFragment pack1= sourceFolder.createPackageFragment("pack1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import static java.io.File.*;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("    public String foo() {\n");
		buf.append("        return pathSeparator + separator + File.separator;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);


		String[] order= new String[] { "#java.io.File", "java", "pack" };
		IChooseImportQuery query= createQuery("C", new String[] {}, new int[] {});

		OrganizeImportsOperation op= createOperation(cu, order, 99, false, true, true, query);
		op.run(null);

		buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import static java.io.File.pathSeparator;\n");
		buf.append("import static java.io.File.separator;\n");
		buf.append("\n");
		buf.append("import java.io.File;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("    public String foo() {\n");
		buf.append("        return pathSeparator + separator + File.separator;\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(cu.getSource(), buf.toString());
	}

	@Test
	public void testStaticImports_bug78585() throws Exception {
		IPackageFragmentRoot sourceFolder= JavaProjectHelper.addSourceContainer(fJProject1, "src");

		IPackageFragment pack0= sourceFolder.createPackageFragment("pack0", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack0;\n");
		buf.append("public class Test1 {\n");
		buf.append("	public static final <T> void assertNotEquals(final String msg, final T expected, final T toCheck) {\n");
		buf.append("	}\n");
		buf.append("}\n");
		pack0.createCompilationUnit("Test1.java", buf.toString(), false, null);

		IPackageFragment pack1= sourceFolder.createPackageFragment("pack1", false, null);
		buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import pack0.Test1;\n");
		buf.append("import java.util.List;\n");
		buf.append("\n");
		buf.append("public class Test2 extends Test1 {\n");
		buf.append("	public void testMe() {\n");
		buf.append("	    assertNotEquals(\"A\", \"B\", \"C\");\n");
		buf.append("	}\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("Test2.java", buf.toString(), false, null);


		String[] order= new String[] {};
		IChooseImportQuery query= createQuery("MyClass", new String[] {}, new int[] {});

		OrganizeImportsOperation op= createOperation(cu, order, 99, false, true, true, query);
		op.run(null);

		buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import pack0.Test1;\n"); // no static import for 'assertNotEquals'
		buf.append("\n");
		buf.append("public class Test2 extends Test1 {\n");
		buf.append("	public void testMe() {\n");
		buf.append("	    assertNotEquals(\"A\", \"B\", \"C\");\n");
		buf.append("	}\n");
		buf.append("}\n");
		assertEqualString(cu.getSource(), buf.toString());
	}

	@Test
	public void testStaticImports_bug90556() throws Exception {
		IPackageFragmentRoot sourceFolder= JavaProjectHelper.addSourceContainer(fJProject1, "src");

		IPackageFragment pack0= sourceFolder.createPackageFragment("pack0", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack0;\n");
		buf.append("public class BasePanel<T extends Number> {\n");
		buf.append("	public static void add2panel(String... s) {\n");
		buf.append("	}\n");
		buf.append("}\n");
		pack0.createCompilationUnit("Test1.java", buf.toString(), false, null);

		buf= new StringBuffer();
		buf.append("package pack0;\n");
		buf.append("\n");
		buf.append("public class ManufacturerMainPanel<T extends Number> extends BasePanel<T>{\n");
		buf.append("	public void testMe() {\n");
		buf.append("	    add2panel(null, null);\n");
		buf.append("	}\n");
		buf.append("}\n");
		ICompilationUnit cu= pack0.createCompilationUnit("ManufacturerMainPanel.java", buf.toString(), false, null);


		String[] order= new String[] {};
		IChooseImportQuery query= createQuery("ManufacturerMainPanel", new String[] {}, new int[] {});

		OrganizeImportsOperation op= createOperation(cu, order, 99, false, true, true, query);
		op.run(null);

		buf= new StringBuffer();
		buf.append("package pack0;\n");
		buf.append("\n");
		buf.append("public class ManufacturerMainPanel<T extends Number> extends BasePanel<T>{\n");
		buf.append("	public void testMe() {\n");
		buf.append("	    add2panel(null, null);\n");
		buf.append("	}\n");
		buf.append("}\n");
		assertEqualString(cu.getSource(), buf.toString());
	}

	@Test
	public void testStaticImports_bug113770() throws Exception {
		IPackageFragmentRoot sourceFolder= JavaProjectHelper.addSourceContainer(fJProject1, "src");

		IPackageFragment pack0= sourceFolder.createPackageFragment("pack0", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test;\n");
		buf.append("\n");
		buf.append("public abstract class Test<M>\n");
		buf.append("{\n");
		buf.append("        private static Map<Object, Object[]> facetMap;\n");
		buf.append("\n");
		buf.append("        public void getFacets() {\n");
		buf.append("                facetMap.get(null);\n");
		buf.append("        }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack0.createCompilationUnit("Test.java", buf.toString(), false, null);


		String[] order= new String[] {};
		IChooseImportQuery query= createQuery("Test", new String[] {}, new int[] {});

		OrganizeImportsOperation op= createOperation(cu, order, 99, false, true, true, query);
		op.run(null);

		buf= new StringBuffer();
		buf.append("package test;\n");
		buf.append("\n");
		buf.append("import java.util.Map;\n");
		buf.append("\n");
		buf.append("public abstract class Test<M>\n");
		buf.append("{\n");
		buf.append("        private static Map<Object, Object[]> facetMap;\n");
		buf.append("\n");
		buf.append("        public void getFacets() {\n");
		buf.append("                facetMap.get(null);\n");
		buf.append("        }\n");
		buf.append("}\n");
		assertEqualString(cu.getSource(), buf.toString());
	}

	@Test
	public void testStaticImports_bug81589() throws Exception {
		IPackageFragmentRoot sourceFolder= JavaProjectHelper.addSourceContainer(fJProject1, "src");

		IPackageFragment pack0= sourceFolder.createPackageFragment("pack0", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack0;\n");
		buf.append("public enum E {\n");
		buf.append("	A, B, C;\n");
		buf.append("}\n");
		pack0.createCompilationUnit("E.java", buf.toString(), false, null);

		IPackageFragment pack1= sourceFolder.createPackageFragment("pack1", false, null);
		buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import pack0.E;\n");
		buf.append("import static pack0.E.A;\n");
		buf.append("\n");
		buf.append("public class Test2 {\n");
		buf.append("	public void testMe(E e) {\n");
		buf.append("	    switch (e) {\n");
		buf.append("	      case A:\n");
		buf.append("	    }\n");
		buf.append("	}\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("Test2.java", buf.toString(), false, null);


		String[] order= new String[] {};
		IChooseImportQuery query= createQuery("MyClass", new String[] {}, new int[] {});

		OrganizeImportsOperation op= createOperation(cu, order, 99, false, true, true, query);
		op.run(null);

		buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import pack0.E;\n"); // no import for E.A
		buf.append("\n");
		buf.append("public class Test2 {\n");
		buf.append("	public void testMe(E e) {\n");
		buf.append("	    switch (e) {\n");
		buf.append("	      case A:\n");
		buf.append("	    }\n");
		buf.append("	}\n");
		buf.append("}\n");
		assertEqualString(cu.getSource(), buf.toString());
	}

	public void testStaticImports_bug159424() throws Exception {
		IPackageFragmentRoot sourceFolder= JavaProjectHelper.addSourceContainer(fJProject1, "src");

		IPackageFragment pack0= sourceFolder.createPackageFragment("pack0", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack0;\n");
		buf.append("\n");
		buf.append("import java.util.List;\n");
		buf.append("\n");
		buf.append("public abstract class B {\n");
		buf.append("    private static List logger;\n");
		buf.append("}\n");
		pack0.createCompilationUnit("B.java", buf.toString(), false, null);

		IPackageFragment pack1= sourceFolder.createPackageFragment("pack1", false, null);

		buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import java.util.List;\n");
		buf.append("import pack0.B;\n");
		buf.append("\n");
		buf.append("public abstract class A {\n");
		buf.append("    private static List logger;\n");
		buf.append("\n");
		buf.append("    protected class BSubClass extends B {\n");
		buf.append("        public void someMethod() {\n");
		buf.append("            logger.toString();\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("A.java", buf.toString(), false, null);


		String[] order= new String[] {};
		IChooseImportQuery query= createQuery("testStaticImports_bug159424", new String[] {}, new int[] {});

		OrganizeImportsOperation op= createOperation(cu, order, 99, false, true, true, query);
		op.run(null);

		assertEqualString(cu.getSource(), buf.toString()); // no changes, don't add 'logger' as static import
	}

	@Test
	public void testStaticImports_bug175498() throws Exception {
		IPackageFragmentRoot sourceFolder= JavaProjectHelper.addSourceContainer(fJProject1, "src");
		IPackageFragment pack1= sourceFolder.createPackageFragment("p", false, null);

		StringBuffer buf= new StringBuffer();
		buf.append("package p;\n");
		buf.append("public class Test<T> {\n");
		buf.append("        public static enum TestEnum {\n");
		buf.append("                V1,\n");
		buf.append("                V2\n");
		buf.append("        }\n");
		buf.append("\n");
		buf.append("        public void test(final TestEnum value) {\n");
		buf.append("                switch (value) {\n");
		buf.append("                        case V1:\n");
		buf.append("                        case V2:\n");
		buf.append("                }\n");
		buf.append("        }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("A.java", buf.toString(), false, null);

		String[] order= new String[] {};
		IChooseImportQuery query= createQuery("testStaticImports_bug175498", new String[] {}, new int[] {});

		OrganizeImportsOperation op= createOperation(cu, order, 99, false, true, true, query);
		op.run(null);

		assertEqualString(cu.getSource(), buf.toString()); // no changes, don't add 'V1' and 'V2' as static import
	}

	@Test
	public void testStaticImports_bug181895() throws Exception {
		IPackageFragmentRoot sourceFolder= JavaProjectHelper.addSourceContainer(fJProject1, "src");

		IPackageFragment pack0= sourceFolder.createPackageFragment("pack0", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test;\n");
		buf.append("\n");
		buf.append("import static java.lang.Math.max;\n");
		buf.append("\n");
		buf.append("public class Test {\n");
		buf.append("        /**\n");
		buf.append("         * @see #max\n");
		buf.append("         */\n");
		buf.append("        public void doFoo() {\n");
		buf.append("        }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack0.createCompilationUnit("Test.java", buf.toString(), false, null);


		String[] order= new String[] {};
		IChooseImportQuery query= createQuery("Test", new String[] {}, new int[] {});

		OrganizeImportsOperation op= createOperation(cu, order, 99, false, true, true, query);
		op.run(null);

		buf= new StringBuffer();
		buf.append("package test;\n");
		buf.append("\n");
		buf.append("\n");
		buf.append("public class Test {\n");
		buf.append("        /**\n");
		buf.append("         * @see #max\n");
		buf.append("         */\n");
		buf.append("        public void doFoo() {\n");
		buf.append("        }\n");
		buf.append("}\n");
		assertEqualString(cu.getSource(), buf.toString());
	}

	@Test
	public void testStaticImports_bug187004a() throws Exception {
		IPackageFragmentRoot sourceFolder= JavaProjectHelper.addSourceContainer(fJProject1, "src");

		IPackageFragment pack0= sourceFolder.createPackageFragment("b", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package b;\n");
		buf.append("\n");
		buf.append("abstract public class Parent<T> {\n");
		buf.append("        protected static final int CONSTANT = 42;\n");
		buf.append("}\n");
		pack0.createCompilationUnit("Parent.java", buf.toString(), false, null);

		IPackageFragment pack1= sourceFolder.createPackageFragment("a", false, null);
		buf= new StringBuffer();
		buf.append("package a;\n");
		buf.append("\n");
		buf.append("import b.Parent;\n");
		buf.append("\n");
		buf.append("public class Child extends Parent<String> {\n");
		buf.append("        public Child() {\n");
		buf.append("                System.out.println(CONSTANT);\n");
		buf.append("        }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("Child.java", buf.toString(), false, null);

		String[] order= new String[] {};
		IChooseImportQuery query= createQuery("Child", new String[] {}, new int[] {});

		OrganizeImportsOperation op= createOperation(cu, order, 99, false, true, true, query);
		op.run(null);

		buf= new StringBuffer();
		buf.append("package a;\n");
		buf.append("\n");
		buf.append("import b.Parent;\n"); 		// no static import for CONSTANT
		buf.append("\n");
		buf.append("public class Child extends Parent<String> {\n");
		buf.append("        public Child() {\n");
		buf.append("                System.out.println(CONSTANT);\n");
		buf.append("        }\n");
		buf.append("}\n");
		assertEqualString(cu.getSource(), buf.toString());
	}

	@Test
	public void testStaticImports_bug187004b() throws Exception {
		IPackageFragmentRoot sourceFolder= JavaProjectHelper.addSourceContainer(fJProject1, "src");

		IPackageFragment pack0= sourceFolder.createPackageFragment("b", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package b;\n");
		buf.append("\n");
		buf.append("abstract public class Parent<T> {\n");
		buf.append("        protected static final int CONSTANT() { return 42; }\n");
		buf.append("}\n");
		pack0.createCompilationUnit("Parent.java", buf.toString(), false, null);

		IPackageFragment pack1= sourceFolder.createPackageFragment("a", false, null);
		buf= new StringBuffer();
		buf.append("package a;\n");
		buf.append("\n");
		buf.append("import b.Parent;\n");
		buf.append("\n");
		buf.append("public class Child extends Parent<String> {\n");
		buf.append("        public Child() {\n");
		buf.append("                System.out.println(CONSTANT());\n");
		buf.append("        }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("Child.java", buf.toString(), false, null);

		String[] order= new String[] {};
		IChooseImportQuery query= createQuery("Child", new String[] {}, new int[] {});

		OrganizeImportsOperation op= createOperation(cu, order, 99, false, true, true, query);
		op.run(null);

		buf= new StringBuffer();
		buf.append("package a;\n");
		buf.append("\n");
		buf.append("import b.Parent;\n"); 		// no static import for CONSTANT()
		buf.append("\n");
		buf.append("public class Child extends Parent<String> {\n");
		buf.append("        public Child() {\n");
		buf.append("                System.out.println(CONSTANT());\n");
		buf.append("        }\n");
		buf.append("}\n");
		assertEqualString(cu.getSource(), buf.toString());
	}

	@Test
	public void testStaticImports_bug230067() throws Exception {
		IPackageFragmentRoot sourceFolder= JavaProjectHelper.addSourceContainer(fJProject1, "src");

		IPackageFragment pack1= sourceFolder.createPackageFragment("a", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package a;\n");
		buf.append("\n");
		buf.append("class Test<T> {\n");
		buf.append("    private static String TEST = \"constant\";\n");
		buf.append("\n");
		buf.append("    static class Inner extends Test<String> {\n");
		buf.append("        public void test() {\n");
		buf.append("            TEST.concat(\"access\");\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("Test.java", buf.toString(), false, null);

		String[] order= new String[] {};
		IChooseImportQuery query= createQuery("Test", new String[] {}, new int[] {});

		OrganizeImportsOperation op= createOperation(cu, order, 99, false, true, true, query);
		op.run(null);

		buf= new StringBuffer();
		buf.append("package a;\n");
		buf.append("\n");                            // no static import for 'TEST'
		buf.append("class Test<T> {\n");
		buf.append("    private static String TEST = \"constant\";\n");
		buf.append("\n");
		buf.append("    static class Inner extends Test<String> {\n");
		buf.append("        public void test() {\n");
		buf.append("            TEST.concat(\"access\");\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(cu.getSource(), buf.toString());
	}

	@Test
	public void testImportCountAddNew() throws Exception {
	    IPackageFragmentRoot sourceFolder= JavaProjectHelper.addSourceContainer(fJProject1, "src");


		IPackageFragment pack1= sourceFolder.createPackageFragment("pack1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("    public void foo() {\n");
		buf.append("        HashMap m;\n");
		buf.append("        ArrayList l;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);


		String[] order= new String[] { "java", "pack" };
		IChooseImportQuery query= createQuery("C", new String[] {}, new int[] {});

		OrganizeImportsOperation op= createOperation(cu, order, 99, false, true, true, query);
		op.run(null);

		buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import java.util.ArrayList;\n");
		buf.append("import java.util.HashMap;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("    public void foo() {\n");
		buf.append("        HashMap m;\n");
		buf.append("        ArrayList l;\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(cu.getSource(), buf.toString());

		assertEquals(2, op.getNumberOfImportsAdded());
		assertEquals(0, op.getNumberOfImportsRemoved());
	}

	@Test
	public void testImportCountAddandRemove() throws Exception {
	    IPackageFragmentRoot sourceFolder= JavaProjectHelper.addSourceContainer(fJProject1, "src");


		IPackageFragment pack1= sourceFolder.createPackageFragment("pack1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import java.util.*;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("    public void foo() {\n");
		buf.append("        HashMap m;\n");
		buf.append("        ArrayList l;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);


		String[] order= new String[] { "java", "pack" };
		IChooseImportQuery query= createQuery("C", new String[] {}, new int[] {});

		OrganizeImportsOperation op= createOperation(cu, order, 99, false, true, true, query);
		op.run(null);

		buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import java.util.ArrayList;\n");
		buf.append("import java.util.HashMap;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("    public void foo() {\n");
		buf.append("        HashMap m;\n");
		buf.append("        ArrayList l;\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(cu.getSource(), buf.toString());

		assertEquals(2, op.getNumberOfImportsAdded());
		assertEquals(1, op.getNumberOfImportsRemoved());
	}

	@Test
	public void testImportCountAddandRemoveWithComments() throws Exception {
	    IPackageFragmentRoot sourceFolder= JavaProjectHelper.addSourceContainer(fJProject1, "src");


		IPackageFragment pack1= sourceFolder.createPackageFragment("pack1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("/**comment1*/\n");
		buf.append("/*lead1*/import java.util.*;// trail 1\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("    public void foo() {\n");
		buf.append("        HashMap m;\n");
		buf.append("        ArrayList l;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);


		String[] order= new String[] { "java", "pack" };
		IChooseImportQuery query= createQuery("C", new String[] {}, new int[] {});

		OrganizeImportsOperation op= createOperation(cu, order, 99, false, true, true, query);
		op.run(null);

		buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("/**comment1*/\n");
		buf.append("/*lead1*/");
		buf.append("import java.util.ArrayList;\n");
		buf.append("import java.util.HashMap;// trail 1\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("    public void foo() {\n");
		buf.append("        HashMap m;\n");
		buf.append("        ArrayList l;\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(cu.getSource(), buf.toString());

		assertEquals(2, op.getNumberOfImportsAdded());
		assertEquals(1, op.getNumberOfImportsRemoved());
	}

	@Test
	public void testImportCountKeepOne() throws Exception {
	    IPackageFragmentRoot sourceFolder= JavaProjectHelper.addSourceContainer(fJProject1, "src");


		IPackageFragment pack1= sourceFolder.createPackageFragment("pack1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import java.util.HashMap;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("    public void foo() {\n");
		buf.append("        HashMap m;\n");
		buf.append("        ArrayList l;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		String[] order= new String[] { "java", "pack" };
		IChooseImportQuery query= createQuery("C", new String[] {}, new int[] {});

		OrganizeImportsOperation op= createOperation(cu, order, 99, false, true, true, query);
		op.run(null);

		buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import java.util.ArrayList;\n");
		buf.append("import java.util.HashMap;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("    public void foo() {\n");
		buf.append("        HashMap m;\n");
		buf.append("        ArrayList l;\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(cu.getSource(), buf.toString());

		assertEquals(1, op.getNumberOfImportsAdded());
		assertEquals(0, op.getNumberOfImportsRemoved());
	}

	@Test
	public void testImportCountKeepStar() throws Exception {
	    IPackageFragmentRoot sourceFolder= JavaProjectHelper.addSourceContainer(fJProject1, "src");


		IPackageFragment pack1= sourceFolder.createPackageFragment("pack1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import java.util.*;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("    public void foo() {\n");
		buf.append("        HashMap m;\n");
		buf.append("        ArrayList l;\n");
		buf.append("        Collection c;\n");
		buf.append("        Socket s;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		String[] order= new String[] { "java", "pack" };
		IChooseImportQuery query= createQuery("C", new String[] {}, new int[] {});

		OrganizeImportsOperation op= createOperation(cu, order, 2, false, true, true, query);
		op.run(null);

		buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import java.net.Socket;\n");
		buf.append("import java.util.*;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("    public void foo() {\n");
		buf.append("        HashMap m;\n");
		buf.append("        ArrayList l;\n");
		buf.append("        Collection c;\n");
		buf.append("        Socket s;\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(cu.getSource(), buf.toString());

		assertEquals(1, op.getNumberOfImportsAdded());
		assertEquals(0, op.getNumberOfImportsRemoved());
	}

	@Test
	public void testImportCountAddTwoRemoveOne() throws Exception {
	    IPackageFragmentRoot sourceFolder= JavaProjectHelper.addSourceContainer(fJProject1, "src");


		IPackageFragment pack1= sourceFolder.createPackageFragment("pack1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import java.util.BitSet;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("    public void foo() {\n");
		buf.append("        HashMap m;\n");
		buf.append("        ArrayList l;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);


		String[] order= new String[] { "java", "pack" };
		IChooseImportQuery query= createQuery("C", new String[] {}, new int[] {});

		OrganizeImportsOperation op= createOperation(cu, order, 99, false, true, true, query);
		op.run(null);

		buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import java.util.ArrayList;\n");
		buf.append("import java.util.HashMap;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("    public void foo() {\n");
		buf.append("        HashMap m;\n");
		buf.append("        ArrayList l;\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(cu.getSource(), buf.toString());

		assertEquals(2, op.getNumberOfImportsAdded());
		assertEquals(1, op.getNumberOfImportsRemoved());
	}

	@Test
	public void testImportCountReplaceStar() throws Exception {
	    IPackageFragmentRoot sourceFolder= JavaProjectHelper.addSourceContainer(fJProject1, "src");


		IPackageFragment pack1= sourceFolder.createPackageFragment("pack1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import java.util.BitSet;\n");
		buf.append("import java.util.Calendar;\n");
		buf.append("import java.util.*;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("    public void foo() {\n");
		buf.append("        HashMap m;\n");
		buf.append("        ArrayList l;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);


		String[] order= new String[] { "java", "pack" };
		IChooseImportQuery query= createQuery("C", new String[] {}, new int[] {});

		OrganizeImportsOperation op= createOperation(cu, order, 99, false, true, true, query);
		op.run(null);

		buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import java.util.ArrayList;\n");
		buf.append("import java.util.HashMap;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("    public void foo() {\n");
		buf.append("        HashMap m;\n");
		buf.append("        ArrayList l;\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(cu.getSource(), buf.toString());

		assertEquals(2, op.getNumberOfImportsAdded());
		assertEquals(3, op.getNumberOfImportsRemoved());
	}

	@Test
	public void testImportCountRemoveStatic() throws Exception {
	    IPackageFragmentRoot sourceFolder= JavaProjectHelper.addSourceContainer(fJProject1, "src");


		IPackageFragment pack1= sourceFolder.createPackageFragment("pack1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import java.util.BitSet;\n");
		buf.append("// some comment;\n");
		buf.append("import java.util.Calendar; /*another comment*/\n");
		buf.append("import static java.io.File.pathSeparator;\n");
		buf.append("import java.util.*;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("    public void foo() {\n");
		buf.append("        HashMap m;\n");
		buf.append("        ArrayList l;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);


		String[] order= new String[] { "java", "pack" };
		IChooseImportQuery query= createQuery("C", new String[] {}, new int[] {});

		OrganizeImportsOperation op= createOperation(cu, order, 99, false, true, true, query);
		op.run(null);

		buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import java.util.ArrayList;\n");
		buf.append("import java.util.HashMap;\n");
		buf.append("// some comment;\n");
		buf.append("/*another comment*/\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("    public void foo() {\n");
		buf.append("        HashMap m;\n");
		buf.append("        ArrayList l;\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(cu.getSource(), buf.toString());

		assertEquals(2, op.getNumberOfImportsAdded());
		assertEquals(4, op.getNumberOfImportsRemoved());
	}

	@Test
	public void testImportCountKeepStatic() throws Exception {
		IPackageFragmentRoot sourceFolder= JavaProjectHelper.addSourceContainer(fJProject1, "src");


		IPackageFragment pack1= sourceFolder.createPackageFragment("pack1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import java.util.BitSet;\n");
		buf.append("// some comment;\n");
		buf.append("import java.util.Calendar; /*another comment*/\n");
		buf.append("import static java.io.File.pathSeparator;\n");
		buf.append("import java.util.*;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("    public void foo() {\n");
		buf.append("        String s= pathSeparator;\n");
		buf.append("        HashMap m;\n");
		buf.append("        ArrayList l;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);


		String[] order= new String[] { "java", "pack", "#" };
		IChooseImportQuery query= createQuery("C", new String[] {}, new int[] {});

		OrganizeImportsOperation op= createOperation(cu, order, 99, false, true, true, query);
		op.run(null);

		buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import java.util.ArrayList;\n");
		buf.append("import java.util.HashMap;\n");
		buf.append("\n");
		buf.append("import static java.io.File.pathSeparator;\n");
		buf.append("// some comment;\n");
		buf.append("/*another comment*/\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("    public void foo() {\n");
		buf.append("        String s= pathSeparator;\n");
		buf.append("        HashMap m;\n");
		buf.append("        ArrayList l;\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(cu.getSource(), buf.toString());

		assertEquals(2, op.getNumberOfImportsAdded());
		assertEquals(3, op.getNumberOfImportsRemoved());
	}

	@Test
	public void test_bug78397() throws Exception {
		IPackageFragmentRoot sourceFolder= JavaProjectHelper.addSourceContainer(fJProject1, "src");

		IPackageFragment pack1= sourceFolder.createPackageFragment("pack0", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack0;\n");
		buf.append("import java.util.Collection;\n");
		buf.append("public class A {\n");
		buf.append("    Collection<java.sql.Date> foo;\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("A.java", buf.toString(), false, null);

		String[] order= new String[] {};
		IChooseImportQuery query= createQuery("MyClass", new String[] {}, new int[] {});

		OrganizeImportsOperation op= createOperation(cu, order, 99, false, true, true, query);
		op.run(null);

		buf= new StringBuffer();
		buf.append("package pack0;\n");
		buf.append("import java.util.Collection;\n");
		buf.append("public class A {\n");
		buf.append("    Collection<java.sql.Date> foo;\n"); // no import for Date
		buf.append("}\n");
		assertEqualString(cu.getSource(), buf.toString());
	}

	@Test
	public void test_bug78533() throws Exception {
		IPackageFragmentRoot sourceFolder= JavaProjectHelper.addSourceContainer(fJProject1, "src");

		IPackageFragment pack1= sourceFolder.createPackageFragment("pack0", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack0;\n");
		buf.append("public class A {\n");
		buf.append("    public <T extends Collection> void method1() { }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("A.java", buf.toString(), false, null);

		String[] order= new String[] {};
		IChooseImportQuery query= createQuery("MyClass", new String[] {}, new int[] {});

		OrganizeImportsOperation op= createOperation(cu, order, 99, false, true, true, query);
		op.run(null);

		buf= new StringBuffer();
		buf.append("package pack0;\n");
		buf.append("\n");
		buf.append("import java.util.Collection;\n");
		buf.append("\n");
		buf.append("public class A {\n");
		buf.append("    public <T extends Collection> void method1() { }\n");
		buf.append("}\n");
		assertEqualString(cu.getSource(), buf.toString());
	}

	@Test
	public void test_bug78716() throws Exception {
		IPackageFragmentRoot sourceFolder= JavaProjectHelper.addSourceContainer(fJProject1, "src");

		IPackageFragment pack0= sourceFolder.createPackageFragment("pack0", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack0;\n");
		buf.append("public enum MyEnum {\n");
		buf.append("	A, B, C\n");
		buf.append("}\n");
		pack0.createCompilationUnit("MyEnum.java", buf.toString(), false, null);

		IPackageFragment pack1= sourceFolder.createPackageFragment("pack1", false, null);
		buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import pack0.MyEnum;\n");
		buf.append("import static pack0.MyEnum.*;\n");
		buf.append("\n");
		buf.append("public class Test2 {\n");
		buf.append("	MyEnum e= A;\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("Test2.java", buf.toString(), false, null);

		String[] order= new String[] { "", "#"};
		IChooseImportQuery query= createQuery("MyClass", new String[] {}, new int[] {});

		OrganizeImportsOperation op= createOperation(cu, order, 99, false, true, true, query);
		op.run(null);

		buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import pack0.MyEnum;\n");
		buf.append("\n");
		buf.append("import static pack0.MyEnum.A;\n");
		buf.append("\n");
		buf.append("public class Test2 {\n");
		buf.append("	MyEnum e= A;\n");
		buf.append("}\n");
		assertEqualString(cu.getSource(), buf.toString());
	}

	@Test
	public void test_bug135122() throws Exception {
		IPackageFragmentRoot sourceFolder= JavaProjectHelper.addSourceContainer(fJProject1, "src");

		IPackageFragment pack1= sourceFolder.createPackageFragment("pack1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("public class Foo extends Bar {\n");
		buf.append("  public static final int MYCONSTANT= 9;\n");
		buf.append("\n");
		buf.append("  public void anotherMethod() {\n");
		buf.append("    super.testMethod(MYCONSTANT);\n");
		buf.append("  }\n");
		buf.append("}\n");
		buf.append("\n");
		buf.append("class Bar {\n");
		buf.append("    public void testMethod(int something) {\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("Foo.java", buf.toString(), false, null);

		String[] order= new String[] { "", "#"};
		IChooseImportQuery query= createQuery("Foo", new String[] {}, new int[] {});

		OrganizeImportsOperation op= createOperation(cu, order, 99, false, true, true, query);
		op.run(null);

		buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("public class Foo extends Bar {\n");
		buf.append("  public static final int MYCONSTANT= 9;\n");
		buf.append("\n");
		buf.append("  public void anotherMethod() {\n");
		buf.append("    super.testMethod(MYCONSTANT);\n");
		buf.append("  }\n");
		buf.append("}\n");
		buf.append("\n");
		buf.append("class Bar {\n");
		buf.append("    public void testMethod(int something) {\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(cu.getSource(), buf.toString());
	}

	@Test
	public void test_PackageInfoBug157541a() throws Exception {
		IPackageFragmentRoot sourceFolder= JavaProjectHelper.addSourceContainer(fJProject1, "src");

		IPackageFragment pack1= sourceFolder.createPackageFragment("pack1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("@Foo\n");
		buf.append("package pack1;");
		ICompilationUnit cu= pack1.createCompilationUnit("package-info.java", buf.toString(), false, null);

		IPackageFragment pack2= sourceFolder.createPackageFragment("pack2", false, null);
		buf= new StringBuffer();
		buf.append("package pack2;\n");
		buf.append("public @interface Foo {\n");
		buf.append("}\n");
		pack2.createCompilationUnit("Foo.java", buf.toString(), false, null);

		String[] order= new String[] { "", "#" };
		IChooseImportQuery query= createQuery("Foo", new String[] {}, new int[] {});

		OrganizeImportsOperation op= createOperation(cu, order, 99, false, true, true, query);
		op.run(null);

		buf= new StringBuffer();
		buf.append("@Foo\n");
		buf.append("package pack1;\n");
		buf.append("import pack2.Foo;\n");
		assertEqualString(cu.getSource(), buf.toString());
	}

	@Test
	public void test_PackageInfoBug157541b() throws Exception {
		IPackageFragmentRoot sourceFolder= JavaProjectHelper.addSourceContainer(fJProject1, "src");

		IPackageFragment pack1= sourceFolder.createPackageFragment("pack1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("@Foo @Bar\n");
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import pack2.Foo;\n");
		buf.append("\n");
		ICompilationUnit cu= pack1.createCompilationUnit("package-info.java", buf.toString(), false, null);

		IPackageFragment pack2= sourceFolder.createPackageFragment("pack2", false, null);
		buf= new StringBuffer();
		buf.append("package pack2;\n");
		buf.append("public @interface Foo {\n");
		buf.append("}\n");
		pack2.createCompilationUnit("Foo.java", buf.toString(), false, null);

		buf= new StringBuffer();
		buf.append("package pack2;\n");
		buf.append("public @interface Bar {\n");
		buf.append("}\n");
		pack2.createCompilationUnit("Bar.java", buf.toString(), false, null);

		String[] order= new String[] { "", "#" };
		IChooseImportQuery query= createQuery("Foo", new String[] {}, new int[] {});

		OrganizeImportsOperation op= createOperation(cu, order, 99, false, true, true, query);
		op.run(null);

		buf= new StringBuffer();
		buf.append("@Foo @Bar\n");
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import pack2.Bar;\n");
		buf.append("import pack2.Foo;\n");
		buf.append("\n");
		assertEqualString(cu.getSource(), buf.toString());
	}

	@Test
	public void test_PackageInfoBug216432() throws Exception {
		IPackageFragmentRoot sourceFolder= JavaProjectHelper.addSourceContainer(fJProject1, "src");

		IPackageFragment pack1= sourceFolder.createPackageFragment("pack1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("/**\n");
		buf.append(" * @see Bar\n");
		buf.append(" */\n");
		buf.append("@Foo\n");
		buf.append("package pack1;");
		ICompilationUnit cu= pack1.createCompilationUnit("package-info.java", buf.toString(), false, null);

		IPackageFragment pack2= sourceFolder.createPackageFragment("pack2", false, null);
		buf= new StringBuffer();
		buf.append("package pack2;\n");
		buf.append("public @interface Foo {\n");
		buf.append("}\n");
		pack2.createCompilationUnit("Foo.java", buf.toString(), false, null);

		buf= new StringBuffer();
		buf.append("package pack2;\n");
		buf.append("public @interface Bar {\n");
		buf.append("}\n");
		pack2.createCompilationUnit("Bar.java", buf.toString(), false, null);

		String[] order= new String[] { "", "#" };
		IChooseImportQuery query= createQuery("test_PackageInfoBug216432", new String[] {}, new int[] {});

		OrganizeImportsOperation op= createOperation(cu, order, 99, false, true, true, query);
		op.run(null);

		buf= new StringBuffer();
		buf.append("/**\n");
		buf.append(" * @see Bar\n");
		buf.append(" */\n");
		buf.append("@Foo\n");
		buf.append("package pack1;\n");
		buf.append("import pack2.Foo;\n"); // no import for Bar
		assertEqualString(cu.getSource(), buf.toString());
	}

	@Test
	public void testTypeArgumentImports() throws Exception {
		IPackageFragmentRoot sourceFolder= JavaProjectHelper.addSourceContainer(fJProject1, "src");

		IPackageFragment pack1= sourceFolder.createPackageFragment("pack1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("public class B {\n");
		buf.append("	   public B() {\n");
		buf.append("        <File> this(null);\n"); // constructor invocation
		buf.append("    }\n");
		buf.append("    public <T> B(T t) {\n");
		buf.append("    }\n");
		buf.append("    public <T> void foo(T t) {\n");
		buf.append("        this.<Socket> foo(null);\n");  // method invocation
		buf.append("        new<URL> B(null);\n");  // class instance creation
		buf.append("    }\n");
		buf.append("    class C extends B {\n");
		buf.append("        public C() {\n");
		buf.append("            <Vector> super(null);\n");  // super constructor invocation
		buf.append("            super.<HashMap> foo(null);\n"); // super method invocation
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("B.java", buf.toString(), false, null);

		String[] order= new String[] { "", "#"};
		IChooseImportQuery query= createQuery("B", new String[] {}, new int[] {});

		OrganizeImportsOperation op= createOperation(cu, order, 99, false, true, true, query);
		op.run(null);

		buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import java.io.File;\n");
		buf.append("import java.net.Socket;\n");
		buf.append("import java.net.URL;\n");
		buf.append("import java.util.HashMap;\n");
		buf.append("import java.util.Vector;\n");
		buf.append("\n");
		buf.append("public class B {\n");
		buf.append("	   public B() {\n");
		buf.append("        <File> this(null);\n");
		buf.append("    }\n");
		buf.append("    public <T> B(T t) {\n");
		buf.append("    }\n");
		buf.append("    public <T> void foo(T t) {\n");
		buf.append("        this.<Socket> foo(null);\n");
		buf.append("        new<URL> B(null);\n");
		buf.append("    }\n");
		buf.append("    class C extends B {\n");
		buf.append("        public C() {\n");
		buf.append("            <Vector> super(null);\n");
		buf.append("            super.<HashMap> foo(null);\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(cu.getSource(), buf.toString());
	}

	@Test
	public void testAnnotationImports1() throws Exception {
		IPackageFragmentRoot sourceFolder= JavaProjectHelper.addSourceContainer(fJProject1, "src");

		IPackageFragment pack0= sourceFolder.createPackageFragment("pack0", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack0;\n");
		buf.append("public @interface MyAnnot1 {\n");
		buf.append("}\n");
		pack0.createCompilationUnit("MyAnnot1.java", buf.toString(), false, null);

		buf= new StringBuffer();
		buf.append("package pack0;\n");
		buf.append("public @interface MyAnnot2 {\n");
		buf.append("    int value();\n");
		buf.append("}\n");
		pack0.createCompilationUnit("MyAnnot2.java", buf.toString(), false, null);

		buf= new StringBuffer();
		buf.append("package pack0;\n");
		buf.append("public @interface MyAnnot3 {\n");
		buf.append("}\n");
		pack0.createCompilationUnit("MyAnnot3.java", buf.toString(), false, null);


		IPackageFragment pack1= sourceFolder.createPackageFragment("pack1", false, null);
		buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("@MyAnnot3 public class Test2 {\n");
		buf.append("    @MyAnnot1 Object e;\n");
		buf.append("    @MyAnnot2(1) void foo() {\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("Test2.java", buf.toString(), false, null);

		String[] order= new String[] {};
		IChooseImportQuery query= createQuery("MyClass", new String[] {}, new int[] {});

		OrganizeImportsOperation op= createOperation(cu, order, 99, false, true, true, query);
		op.run(null);

		buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import pack0.MyAnnot1;\n");
		buf.append("import pack0.MyAnnot2;\n");
		buf.append("import pack0.MyAnnot3;\n");
		buf.append("\n");
		buf.append("@MyAnnot3 public class Test2 {\n");
		buf.append("    @MyAnnot1 Object e;\n");
		buf.append("    @MyAnnot2(1) void foo() {\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(cu.getSource(), buf.toString());
	}

	@Test
	public void testAnnotationImports2() throws Exception {
		IPackageFragmentRoot sourceFolder= JavaProjectHelper.addSourceContainer(fJProject1, "src");

		IPackageFragment pack0= sourceFolder.createPackageFragment("pack0", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack0;\n");
		buf.append("public @interface MyAnnot1 {\n");
		buf.append("}\n");
		pack0.createCompilationUnit("MyAnnot1.java", buf.toString(), false, null);

		buf= new StringBuffer();
		buf.append("package pack0;\n");
		buf.append("public @interface MyAnnot2 {\n");
		buf.append("    char value();\n");
		buf.append("}\n");
		pack0.createCompilationUnit("MyAnnot2.java", buf.toString(), false, null);


		IPackageFragment pack1= sourceFolder.createPackageFragment("pack1", false, null);
		buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("@MyAnnot1()\n");
		buf.append("@MyAnnot2(File.separatorChar)\n");
		buf.append("public @interface Test2 {\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("Test2.java", buf.toString(), false, null);

		String[] order= new String[] {};
		IChooseImportQuery query= createQuery("MyClass", new String[] {}, new int[] {});

		OrganizeImportsOperation op= createOperation(cu, order, 99, false, true, true, query);
		op.run(null);

		buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import java.io.File;\n");
		buf.append("\n");
		buf.append("import pack0.MyAnnot1;\n");
		buf.append("import pack0.MyAnnot2;\n");
		buf.append("\n");
		buf.append("@MyAnnot1()\n");
		buf.append("@MyAnnot2(File.separatorChar)\n");
		buf.append("public @interface Test2 {\n");
		buf.append("}\n");
		assertEqualString(cu.getSource(), buf.toString());
	}

	@Test
	public void testJavadocImports_bug319860() throws Exception {
		IPackageFragmentRoot sourceFolder= JavaProjectHelper.addSourceContainer(fJProject1, "src");
		
		IPackageFragment pack0= sourceFolder.createPackageFragment("p", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package p;\n");
		buf.append("\n");
		buf.append("import p.Main.I;\n");
		buf.append("\n");
		buf.append("/**\n");
		buf.append(" * {@link I}.\n");
		buf.append(" * @see C\n");
		buf.append(" */\n");
		buf.append("public class Main {\n");
		buf.append("    public interface I {\n");
		buf.append("    }\n");
		buf.append("    public class C {}\n");
		buf.append("}\n");
		ICompilationUnit cu= pack0.createCompilationUnit("Main.java", buf.toString(), false, null);
		
		String[] order= new String[] {};
		IChooseImportQuery query= createQuery("JavadocImports_bug319860", new String[] {}, new int[] {});
		
		OrganizeImportsOperation op= createOperation(cu, order, 99, false, true, true, query);
		op.run(null);
		
		buf= new StringBuffer();
		buf.append("package p;\n");
		buf.append("\n");
		buf.append("\n");
		buf.append("/**\n");
		buf.append(" * {@link I}.\n");
		buf.append(" * @see C\n");
		buf.append(" */\n");
		buf.append("public class Main {\n");
		buf.append("    public interface I {\n");
		buf.append("    }\n");
		buf.append("    public class C {}\n");
		buf.append("}\n");
		assertEqualString(cu.getSource(), buf.toString());
	}

	protected OrganizeImportsOperation createOperation(ICompilationUnit cu, String[] order, int threshold, boolean ignoreLowerCaseNames, boolean save, boolean allowSyntaxErrors, IChooseImportQuery chooseImportQuery) {
		setOrganizeImportSettings(order, threshold, threshold, cu.getJavaProject());
		return new OrganizeImportsOperation(cu, null, ignoreLowerCaseNames, save, allowSyntaxErrors, null, chooseImportQuery);
	}

	protected void setOrganizeImportSettings(String[] order, int threshold, int staticThreshold, IJavaProject project) {
		IEclipsePreferences scope= new ProjectScope(project.getProject()).getNode("org.eclipse.jdt.ui");
		if (order == null) {
			scope.remove(PreferenceConstants.ORGIMPORTS_IMPORTORDER);
			scope.remove(PreferenceConstants.ORGIMPORTS_ONDEMANDTHRESHOLD);
		} else {
			StringBuffer buf= new StringBuffer();
			for (int i= 0; i < order.length; i++) {
				buf.append(order[i]);
				buf.append(';');
			}
			scope.put(PreferenceConstants.ORGIMPORTS_IMPORTORDER, buf.toString());
			scope.put(PreferenceConstants.ORGIMPORTS_ONDEMANDTHRESHOLD, String.valueOf(threshold));
			scope.put(PreferenceConstants.ORGIMPORTS_STATIC_ONDEMANDTHRESHOLD, String.valueOf(staticThreshold));
		}
	}

}
