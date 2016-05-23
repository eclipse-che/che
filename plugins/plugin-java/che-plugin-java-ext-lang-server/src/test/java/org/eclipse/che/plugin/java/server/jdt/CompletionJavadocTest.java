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

package org.eclipse.che.plugin.java.server.jdt;

import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.SubjectImpl;
import org.eclipse.che.jdt.javaeditor.TextViewer;
import org.eclipse.che.plugin.java.server.jdt.quickfix.QuickFixTest;
import org.eclipse.che.plugin.java.server.jdt.testplugin.Java18ProjectTestSetup;
import org.eclipse.che.plugin.java.server.jdt.testplugin.JavaProjectHelper;
import org.eclipse.che.plugin.java.server.jdt.testplugin.ProjectTestSetup;
import org.eclipse.che.jface.text.contentassist.ICompletionProposal;
import org.eclipse.che.jface.text.contentassist.ICompletionProposalExtension5;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.DocumentAdapter;
import org.eclipse.jdt.internal.ui.text.java.JavaAllCompletionProposalComputer;
import org.eclipse.jdt.internal.ui.text.java.RelevanceSorter;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.graphics.Point;
import org.fest.assertions.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * @author Evgen Vidolob
 */
public class CompletionJavadocTest extends QuickFixTest {

    final String      vfsUser       = "dev";
    final Set<String> vfsUserGroups = new LinkedHashSet<>(Arrays.asList("workspace/developer"));
    private IJavaProject         fJProject1;
    private IPackageFragmentRoot fSourceFolder;

    public CompletionJavadocTest() {

        super(new Java18ProjectTestSetup());
    }

    private static List<ICompletionProposal> computeProposals(ICompilationUnit compilationUnit, int offset) throws JavaModelException {
        IBuffer buffer = compilationUnit.getBuffer();
        IDocument document;
        if (buffer instanceof org.eclipse.jdt.internal.ui.javaeditor.DocumentAdapter) {
            document = ((org.eclipse.jdt.internal.ui.javaeditor.DocumentAdapter)buffer).getDocument();
        } else {
            document = new DocumentAdapter(buffer);
        }
        TextViewer viewer = new TextViewer(document, new Point(offset, 0));
        JavaContentAssistInvocationContext context =
                new JavaContentAssistInvocationContext(viewer, offset, compilationUnit);

        List<ICompletionProposal> proposals = new ArrayList<>();
        proposals.addAll(new JavaAllCompletionProposalComputer().computeCompletionProposals(context, null));
//        proposals.addAll(new TemplateCompletionProposalComputer().computeCompletionProposals(context, null));

        Collections.sort(proposals, new RelevanceSorter());
        return proposals;
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        EnvironmentContext customEnvironment = mock(EnvironmentContext.class);
        doReturn("1q2w3e").when(customEnvironment).getWorkspaceId();
        doReturn(new SubjectImpl(vfsUser, "", "", vfsUserGroups, false)).when(customEnvironment).getSubject();
        EnvironmentContext.setCurrent(customEnvironment);
        fJProject1 = Java18ProjectTestSetup.getProject();
        fSourceFolder = JavaProjectHelper.addSourceContainer(fJProject1, "src");
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
        JavaProjectHelper.clear(fJProject1, ProjectTestSetup.getDefaultClasspath());
    }

    @Test
    public void testJavadoc() throws Exception {
        IPackageFragment pack1 = fSourceFolder.createPackageFragment("test1", false, null);
        StringBuffer buf = new StringBuffer();
        buf.append("package test1;\n");
        buf.append("public class E {\n");
        buf.append("    /**\n");
        buf.append("     * Test JavaDoc.\n");
        buf.append("     */\n");
        buf.append("    public void foo(int i) {\n");
        buf.append("        foo(10);");
        buf.append("    }\n");
        buf.append("}\n");

        ICompilationUnit cu = pack1.createCompilationUnit("E.java", buf.toString(), false, null);
        List<ICompletionProposal> proposals = computeProposals(cu, buf.indexOf("    foo") + "    foo".length());
        Assertions.assertThat(proposals).hasSize(1);
        ICompletionProposal proposal = proposals.get(0);
        String result;
        if (proposal instanceof ICompletionProposalExtension5) {
            result = ((ICompletionProposalExtension5)proposal).getAdditionalProposalInfo(null).toString();
        } else {
            result = proposal.getAdditionalProposalInfo();
        }
        Assertions.assertThat(result).contains("Test JavaDoc.");
    }

    @Test
    public void testInheredJavadoc() throws Exception {
        IPackageFragment pack1 = fSourceFolder.createPackageFragment("test1", false, null);
        StringBuffer buf = new StringBuffer();
        buf.append("package test1;\n");
        buf.append("public class E {\n");
        buf.append("    /**\n");
        buf.append("     * Test JavaDoc.\n");
        buf.append("     */\n");
        buf.append("    public void foo(int i) {\n");
        buf.append("    }\n");
        buf.append("}\n");

        pack1.createCompilationUnit("E.java", buf.toString(), false, null);

        StringBuffer buf2 = new StringBuffer();
        buf2.append("package test1;\n");
        buf2.append("public class B extends E {\n");
        buf2.append("    @Override\n");
        buf2.append("    public void foo(int i) {\n");
        buf2.append("        foo(10);\n");
        buf2.append("    }\n");
        buf2.append("}\n");

        ICompilationUnit cu2 = pack1.createCompilationUnit("B.java", buf2.toString(), false, null);

        List<ICompletionProposal> proposals = computeProposals(cu2, buf2.indexOf("  foo") + "  foo".length());
        Assertions.assertThat(proposals).hasSize(1);
        ICompletionProposal proposal = proposals.get(0);
        String result;
        if (proposal instanceof ICompletionProposalExtension5) {
            result = ((ICompletionProposalExtension5)proposal).getAdditionalProposalInfo(null).toString();
        } else {
            result = proposal.getAdditionalProposalInfo();
        }
        Assertions.assertThat(result).contains("Test JavaDoc.");
    }
}
