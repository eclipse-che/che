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

import org.eclipse.che.plugin.java.server.jdt.testplugin.JavaProjectHelper;
import org.eclipse.che.plugin.java.server.jdt.testplugin.TestOptions;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.search.SearchParticipantRecord;
import org.eclipse.jdt.internal.ui.search.SearchParticipantsExtensionPoint;

import java.io.File;


public class JUnitSourceSetup /*extends TestSetup*/ {
    public static final String PROJECT_NAME  = "JUnitSource";
    public static final String SRC_CONTAINER = "src";

    private IJavaProject                     fProject;
    private SearchParticipantsExtensionPoint fExtensionPoint;

    static class NullExtensionPoint extends SearchParticipantsExtensionPoint {
        public SearchParticipantRecord[] getSearchParticipants(IProject[] concernedProjects) {
            return new SearchParticipantRecord[0];
        }
    }

    public static IJavaProject getProject() {
        IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT_NAME);
        return JavaCore.create(project);
    }

    public JUnitSourceSetup(SearchParticipantsExtensionPoint participants) {
        fExtensionPoint = participants;
    }

    public JUnitSourceSetup(/*Test test*/) {
        this(/*test,*/ new NullExtensionPoint());
    }

    protected void setUp() throws Exception {
        SearchParticipantsExtensionPoint.debugSetInstance(fExtensionPoint);
        fProject = JavaProjectHelper.createJavaProject(PROJECT_NAME, "bin");
        JavaProjectHelper.addRTJar(fProject);
        File junitSrcArchive =
                new File(JUnitSourceSetup.class.getClassLoader().getResource(JavaProjectHelper.JUNIT_SRC_381.toOSString()).getFile());
        JavaProjectHelper.addSourceContainerWithImport(fProject, SRC_CONTAINER, junitSrcArchive, JavaProjectHelper.JUNIT_SRC_ENCODING);
        JavaCore.setOptions(TestOptions.getDefaultOptions());
        TestOptions.initializeCodeGenerationOptions();
        JavaPlugin.getDefault().getCodeTemplateStore().load();
    }

    /* (non-Javadoc)
     * @see junit.extensions.TestSetup#tearDown()
     */
    protected void tearDown() throws Exception {
        JavaProjectHelper.delete(fProject);
        SearchParticipantsExtensionPoint.debugSetInstance(null);
    }
}
