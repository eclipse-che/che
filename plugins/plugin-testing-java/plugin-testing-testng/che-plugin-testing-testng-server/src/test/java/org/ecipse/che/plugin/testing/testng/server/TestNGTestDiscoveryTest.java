/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.ecipse.che.plugin.testing.testng.server;

import org.eclipse.che.api.testing.shared.TestDetectionContext;
import org.eclipse.che.api.testing.shared.TestPosition;
import org.eclipse.che.plugin.testing.testng.server.TestNGRunner;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.internal.core.DefaultWorkingCopyOwner;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import static org.ecipse.che.plugin.testing.testng.server.TestSetUpUtil.addSourceContainer;
import static org.ecipse.che.plugin.testing.testng.server.TestSetUpUtil.createJavaProject;
import static org.ecipse.che.plugin.testing.testng.server.TestSetUpUtil.getTestNgClassPath;
import static org.fest.assertions.Assertions.assertThat;

public class TestNGTestDiscoveryTest extends BaseTest {

    private IJavaProject     javaProject;
    private IPackageFragment packageFragment;


    @BeforeMethod
    public void setUp() throws Exception {
        javaProject = createJavaProject("testDiscovery", "bin");
        IPackageFragmentRoot packageFragmentRoot = addSourceContainer(javaProject, "src", "bin");
        javaProject.setRawClasspath(getTestNgClassPath("/testDiscovery/src"), null);

        packageFragment = packageFragmentRoot.createPackageFragment("test", false, null);
    }

    @AfterMethod
    public void tearDown() throws Exception {
        if (javaProject != null) {
            TestSetUpUtil.delete(javaProject);
        }
    }

    @Test
    public void testDetectRegularMethod() throws Exception {

        StringBuffer buf = new StringBuffer();
        buf.append("package test;\n");
        buf.append("import org.testng.annotations.Test;\n");
        buf.append("public class E {\n");
        buf.append("    @Test\n");
        buf.append("    public void foo() {\n");
        buf.append("    }\n");
        buf.append("}\n");
        ICompilationUnit compilationUnit = packageFragment.createCompilationUnit("T.java", buf.toString(), false, null);

        compilationUnit.reconcile(0, true, DefaultWorkingCopyOwner.PRIMARY, null);
        TestNGRunner runner = new TestNGRunner("", null, null);

        List<TestPosition> testPositions =
                runner.detectTests(new MockTestDetectionContext("/testDiscovery", "/testDiscovery/src/test/T.java", -1));

        assertThat(testPositions).isNotNull().isNotEmpty().hasSize(1);
        TestPosition testPosition = testPositions.iterator().next();
        assertThat(testPosition.getFrameworkName()).isEqualTo("testng");
        assertThat(testPosition.getTestName()).isEqualTo("foo");
        assertThat(testPosition.getTestNameStartOffset()).isEqualTo(buf.indexOf("foo("));
    }

    @Test
    public void testDetectSeveralTestAnnotation() throws Exception {

        StringBuffer buf = new StringBuffer();
        buf.append("package test;\n");
        buf.append("import org.testng.annotations.Test;\n");
        buf.append("public class E {\n");
        buf.append("    @Test\n");
        buf.append("    public void foo() {\n");
        buf.append("    }\n");
        buf.append("    @org.junit.Test\n");
        buf.append("    public void bar() {\n");
        buf.append("    }\n");
        buf.append("}\n");
        ICompilationUnit compilationUnit = packageFragment.createCompilationUnit("T.java", buf.toString(), false, null);

        compilationUnit.reconcile(0, true, DefaultWorkingCopyOwner.PRIMARY, null);
        TestNGRunner runner = new TestNGRunner("", null, null);

        List<TestPosition> testPositions =
                runner.detectTests(new MockTestDetectionContext("/testDiscovery", "/testDiscovery/src/test/T.java", -1));

        assertThat(testPositions).isNotNull().isNotEmpty().hasSize(1);
        TestPosition testPosition = testPositions.iterator().next();
        assertThat(testPosition.getFrameworkName()).isEqualTo("testng");
        assertThat(testPosition.getTestName()).isEqualTo("foo");
        assertThat(testPosition.getTestNameStartOffset()).isEqualTo(buf.indexOf("foo("));
    }

    @Test
    public void testDetectQualifiedTestAnnotation() throws Exception {
        StringBuffer buf = new StringBuffer();
        buf.append("package test;\n");
        buf.append("public class E {\n");
        buf.append("    @org.testng.annotations.Test\n");
        buf.append("    public void foo() {\n");
        buf.append("    }\n");
        buf.append("}\n");
        ICompilationUnit compilationUnit = packageFragment.createCompilationUnit("T.java", buf.toString(), false, null);

        compilationUnit.reconcile(0, true, DefaultWorkingCopyOwner.PRIMARY, null);
        TestNGRunner runner = new TestNGRunner("", null, null);

        List<TestPosition> testPositions =
                runner.detectTests(new MockTestDetectionContext("/testDiscovery", "/testDiscovery/src/test/T.java", -1));

        assertThat(testPositions).isNotNull().isNotEmpty().hasSize(1);
        TestPosition testPosition = testPositions.iterator().next();
        assertThat(testPosition.getFrameworkName()).isEqualTo("testng");
        assertThat(testPosition.getTestName()).isEqualTo("foo");
        assertThat(testPosition.getTestNameStartOffset()).isEqualTo(buf.indexOf("foo("));
    }

    @Test
    public void testOnDemandImportAnnotation() throws Exception {
        StringBuffer buf = new StringBuffer();
        buf.append("package test;\n");
        buf.append("import org.testng.annotations.*;\n");
        buf.append("public class E {\n");
        buf.append("    @Test\n");
        buf.append("    public void foo() {\n");
        buf.append("    }\n");
        buf.append("}\n");
        ICompilationUnit compilationUnit = packageFragment.createCompilationUnit("T.java", buf.toString(), false, null);

        compilationUnit.reconcile(0, true, DefaultWorkingCopyOwner.PRIMARY, null);
        TestNGRunner runner = new TestNGRunner("", null, null);

        List<TestPosition> testPositions =
                runner.detectTests(new MockTestDetectionContext("/testDiscovery", "/testDiscovery/src/test/T.java", -1));

        assertThat(testPositions).isNotNull().isNotEmpty().hasSize(1);
        TestPosition testPosition = testPositions.iterator().next();
        assertThat(testPosition.getFrameworkName()).isEqualTo("testng");
        assertThat(testPosition.getTestName()).isEqualTo("foo");
        assertThat(testPosition.getTestNameStartOffset()).isEqualTo(buf.indexOf("foo("));
    }

    @Test
    public void testNoImportAnnotation() throws Exception {
        StringBuffer buf = new StringBuffer();
        buf.append("package test;\n");
        buf.append("public class E {\n");
        buf.append("    @Test\n");
        buf.append("    public void foo() {\n");
        buf.append("    }\n");
        buf.append("}\n");
        ICompilationUnit compilationUnit = packageFragment.createCompilationUnit("T.java", buf.toString(), false, null);

        compilationUnit.reconcile(0, true, DefaultWorkingCopyOwner.PRIMARY, null);
        TestNGRunner runner = new TestNGRunner("", null, null);

        List<TestPosition> testPositions =
                runner.detectTests(new MockTestDetectionContext("/testDiscovery", "/testDiscovery/src/test/T.java", -1));

        assertThat(testPositions).isNotNull().isEmpty();
    }

    private static class MockTestDetectionContext implements TestDetectionContext {

        private String projectPath;
        private String filePath;
        private int    offset;

        public MockTestDetectionContext(String projectPath, String filePath, int offset) {
            this.projectPath = projectPath;
            this.filePath = filePath;
            this.offset = offset;
        }

        @Override
        public String getProjectPath() {
            return projectPath;
        }

        @Override
        public void setProjectPath(String projectPath) {
        }

        @Override
        public String getFilePath() {
            return filePath;
        }

        @Override
        public void setFilePath(String filePath) {
        }

        @Override
        public int getOffset() {
            return offset;
        }

        @Override
        public void setOffset(int offset) {
        }
    }
}
