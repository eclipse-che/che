/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/

package org.eclipse.che.plugin.java.server.jdt.search;

import org.eclipse.che.ide.ext.java.shared.dto.model.CompilationUnit;
import org.eclipse.che.ide.ext.java.shared.dto.model.PackageFragment;
import org.eclipse.che.ide.ext.java.shared.dto.model.PackageFragmentRoot;
import org.eclipse.che.ide.ext.java.shared.dto.search.FindUsagesResponse;
import org.eclipse.che.plugin.java.server.che.BaseTest;
import org.eclipse.che.plugin.java.server.search.SearchException;
import org.eclipse.che.plugin.java.server.search.SearchManager;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.ui.search.JavaSearchQuery;
import org.eclipse.jdt.internal.ui.search.JavaSearchResult;
import org.eclipse.search.ui.ISearchResult;
import org.fest.assertions.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

/**
 * @author Evgen Vidolob
 */
public class FindReferencesTest extends BaseTest {
    private final JUnitSourceSetup setup;

    public FindReferencesTest() {
        setup = new JUnitSourceSetup();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        setup.setUp();
    }

    @After
    public void tearDown() throws Exception {
        setup.tearDown();
    }

    @Test
    public void testFind() throws Exception {
        JavaSearchQuery query = SearchTestHelper.runMethodRefQuery("junit.framework.Test", "countTestCases", new String[0]);
        ISearchResult result = query.getSearchResult();
        JavaSearchResult javaResult = ((JavaSearchResult)result);
        Assertions.assertThat(javaResult.getElements()).hasSize(9);
    }

    @Test
    public void testFind2() throws Exception {
        IPackageFragmentRoot root = ((JavaProject)JUnitSourceSetup.getProject()).getPackageFragmentRoot(
                new Path(JUnitSourceSetup.SRC_CONTAINER));
        IPackageFragment packageFragment = root.createPackageFragment("che", true, null);
        StringBuilder a = new StringBuilder();
        a.append("package che;\n");
        a.append("public class A{}\n");
        packageFragment.createCompilationUnit("A.java", a.toString(), true, null);
        StringBuilder b = new StringBuilder();
        b.append("package che;\n");
        b.append("import java.util.Comparator;\n");
        b.append("public class B extends A implements Comparator<A>{\n");
        b.append("   @Override\n");
        b.append("   public int compare(A o1, A o2) {\n");
        b.append("       A bb = null;\n");
        b.append("       return 0;\n");
        b.append("   }\n");
        b.append("}\n");
        packageFragment.createCompilationUnit("B.java", b.toString(), true, null);

        JavaSearchQuery query = SearchTestHelper.runTypeRefQuery("che.A");
        ISearchResult result = query.getSearchResult();
        JavaSearchResult javaResult = ((JavaSearchResult)result);
        Assertions.assertThat(javaResult.getElements()).hasSize(2);
    }

    @Test(expected = SearchException.class)
    public void testSearchCantFindElement() throws Exception {
        IPackageFragmentRoot root = ((JavaProject)JUnitSourceSetup.getProject()).getPackageFragmentRoot(
                new Path(JUnitSourceSetup.SRC_CONTAINER));
        IPackageFragment packageFragment = root.createPackageFragment("che", true, null);
        StringBuilder a = new StringBuilder();
        a.append("package che;\n");
        a.append("public class A{}\n");
        ICompilationUnit compilationUnitA = packageFragment.createCompilationUnit("A.java", a.toString(), true, null);
        SearchManager manager = new SearchManager();
        manager.findUsage(JUnitSourceSetup.getProject(), compilationUnitA.getResource().getFullPath().toOSString(), 24);

    }

    @Test
    public void testSearchManagerFindUsage() throws Exception {
        IJavaProject aProject = JUnitSourceSetup.getProject();
        IPackageFragmentRoot root = ((JavaProject)aProject).getPackageFragmentRoot(new Path(JUnitSourceSetup.SRC_CONTAINER));
        IPackageFragment packageFragment = root.createPackageFragment("che", true, null);
        StringBuilder a = new StringBuilder();
        a.append("package che;\n");
        a.append("public class A{}\n");
        ICompilationUnit compilationUnitA = packageFragment.createCompilationUnit("A.java", a.toString(), true, null);
        StringBuilder b = new StringBuilder();
        b.append("package che;\n");
        b.append("import java.util.Comparator;\n");
        b.append("import che.A;\n");
        b.append("public class B extends A implements Comparator<A>{\n");
        b.append("   private A a = null;\n");
        b.append("   static{\n");
        b.append("       A ddd = null;\n");
        b.append("   }\n");
        b.append("   @Override\n");
        b.append("   public int compare(A o1, A o2) {\n");
        b.append("       A bb = null;\n");
        b.append("       return 0;\n");
        b.append("   }\n");
        b.append("   class SubB{\n");
        b.append("     public A ccc = null;\n");
        b.append("   }\n");
        b.append("}\n");
        b.append("class SubB2{\n");
        b.append("    private final A foo = null;\n");
        b.append("}\n");
        packageFragment.createCompilationUnit("B.java", b.toString(), true, null);

        SearchManager manager = new SearchManager();
        FindUsagesResponse response = manager.findUsage(aProject, "che.A", 26);

        Assertions.assertThat(response.getSearchElementLabel()).isEqualTo("A");
        List<org.eclipse.che.ide.ext.java.shared.dto.model.JavaProject> projects = response.getProjects();
        Assertions.assertThat(projects).isNotNull().isNotEmpty().hasSize(1);

        String expectedProjectPath = JUnitSourceSetup.getProject().getPath().toOSString();

        org.eclipse.che.ide.ext.java.shared.dto.model.JavaProject project = projects.get(0);
        Assertions.assertThat(project.getName()).isEqualTo(JUnitSourceSetup.PROJECT_NAME);
        Assertions.assertThat(project.getPath()).isEqualTo(expectedProjectPath);
        Assertions.assertThat(project.getPackageFragmentRoots()).isNotNull().isNotEmpty().hasSize(1);

        PackageFragmentRoot fragmentRoot = project.getPackageFragmentRoots().get(0);
        Assertions.assertThat(fragmentRoot.getElementName()).isEqualTo(JUnitSourceSetup.SRC_CONTAINER);
        Assertions.assertThat(fragmentRoot.getProjectPath()).isEqualTo(expectedProjectPath);
        Assertions.assertThat(fragmentRoot.getPackageFragments()).isNotNull().isNotEmpty().hasSize(1);

        PackageFragment fragment = fragmentRoot.getPackageFragments().get(0);

        Assertions.assertThat(fragment.getElementName()).isEqualTo("che");
        Assertions.assertThat(fragment.getProjectPath()).isEqualTo(expectedProjectPath);
        Assertions.assertThat(fragment.getPath()).isEqualTo(expectedProjectPath + "/" + JUnitSourceSetup.SRC_CONTAINER + "/che");
        Assertions.assertThat(fragment.getClassFiles()).isNotNull().isEmpty();
        Assertions.assertThat(fragment.getCompilationUnits()).isNotNull().isNotEmpty().hasSize(1);

        CompilationUnit compilationUnit = fragment.getCompilationUnits().get(0);
        Assertions.assertThat(compilationUnit.getElementName()).isEqualTo("B.java");
        Assertions.assertThat(compilationUnit.getPath()).isEqualTo(expectedProjectPath + "/" + JUnitSourceSetup.SRC_CONTAINER + "/che/B.java");
        Assertions.assertThat(compilationUnit.getImports()).hasSize(1);
        Assertions.assertThat(compilationUnit.getTypes()).hasSize(2);

    }
}
