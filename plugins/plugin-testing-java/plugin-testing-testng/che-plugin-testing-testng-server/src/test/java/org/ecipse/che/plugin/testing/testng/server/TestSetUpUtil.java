/*******************************************************************************
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package org.ecipse.che.plugin.testing.testng.server;

import com.beust.jcommander.JCommander;
import org.eclipse.che.jdt.core.launching.JREContainerInitializer;
import org.eclipse.che.plugin.java.testing.ClasspathUtil;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.util.CoreUtility;
import org.testng.annotations.Test;

public class TestSetUpUtil {

    private static final int MAX_RETRY = 5;
    private static final int RETRY_DELAY = 1000;

    /**
     * Creates a IJavaProject.
     *
     * @param projectName   The name of the project
     * @param binFolderName Name of the output folder
     * @return Returns the Java project handle
     * @throws CoreException Project creation failed
     */
    public static IJavaProject createJavaProject(String projectName, String binFolderName) throws CoreException {
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        IProject project = root.getProject(projectName);
        if (!project.exists()) {
            project.create(null);
        } else {
            project.refreshLocal(IResource.DEPTH_INFINITE, null);
        }

        if (!project.isOpen()) {
            project.open(null);
        }

        IPath outputLocation;
        if (binFolderName != null && binFolderName.length() > 0) {
            IFolder binFolder = project.getFolder(binFolderName);
            if (!binFolder.exists()) {
                CoreUtility.createFolder(binFolder, false, true, null);
            }
            outputLocation = binFolder.getFullPath();
        } else {
            outputLocation = project.getFullPath();
        }

        IFolder codenvyFolder = project.getFolder(".che");
        if (!codenvyFolder.exists()) {
            CoreUtility.createFolder(codenvyFolder, false, true, null);
        }

//		if (!project.hasNature(JavaCore.NATURE_ID)) {
//			addNatureToProject(project, JavaCore.NATURE_ID, null);
//		}

        IJavaProject jproject = JavaCore.create(project);
//		jproject.setOutputLocation(outputLocation, null);
        jproject.setRawClasspath(new IClasspathEntry[0], null);
        IFolder folder = project.getFolder(JavaProject.INNER_DIR);
        CoreUtility.createFolder(folder, true, true, null);

        return jproject;
    }

    /**
     * Adds a source container to a IJavaProject.
     *
     * @param jproject         The parent project
     * @param containerName    The name of the new source container
     * @param inclusionFilters Inclusion filters to set
     * @param exclusionFilters Exclusion filters to set
     * @param outputLocation   The location where class files are written to, <b>null</b> for project output folder
     * @return The handle to the new source container
     * @throws CoreException Creation failed
     */
    public static IPackageFragmentRoot addSourceContainer(IJavaProject jproject, String containerName, String outputLocation) throws CoreException {
        IProject project = jproject.getProject();
        IContainer container = null;
        if (containerName == null || containerName.length() == 0) {
            container = project;
        } else {
            IFolder folder = project.getFolder(containerName);
            if (!folder.exists()) {
                CoreUtility.createFolder(folder, false, true, null);
            }
            container = folder;
        }
        IPackageFragmentRoot root = jproject.getPackageFragmentRoot(container);

        IPath outputPath = null;
        if (outputLocation != null) {
            IFolder folder = project.getFolder(outputLocation);
            if (!folder.exists()) {
                CoreUtility.createFolder(folder, false, true, null);
            }
            outputPath = folder.getFullPath();
        }
        return root;
    }

    public static IClasspathEntry[] getTestNgClassPath(String sourcePath) {
        IClasspathEntry jreContainer = JavaCore.newContainerEntry(new Path(JREContainerInitializer.JRE_CONTAINER));
        IClasspathEntry testNg = JavaCore.newLibraryEntry(new Path(ClasspathUtil.getJarPathForClass(Test.class)), null, null);
        IClasspathEntry jCommander = JavaCore.newLibraryEntry(new Path(ClasspathUtil.getJarPathForClass(JCommander.class)), null, null);

        IClasspathEntry source = JavaCore.newSourceEntry(new Path(sourcePath), null, new Path("bin"));

        return new IClasspathEntry[]{jreContainer, testNg, jCommander, source};
    }

    /**
     * Removes an IJavaElement's resource. Retries if deletion failed (e.g. because the indexer
     * still locks the file).
     *
     * @param elem the element to delete
     * @throws CoreException if operation failed
     * @see #ASSERT_NO_MIXED_LINE_DELIMIERS
     */
    public static void delete(final IJavaElement elem) throws CoreException {
//		if (ASSERT_NO_MIXED_LINE_DELIMIERS)
//			MixedLineDelimiterDetector.assertNoMixedLineDelimiters(elem);
        if (elem instanceof JavaProject) {
            ((JavaProject) elem).close();
            JavaModelManager.getJavaModelManager().removePerProjectInfo((JavaProject) elem, true);
        }
        JavaModelManager.getJavaModelManager().resetTemporaryCache();
        IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
            public void run(IProgressMonitor monitor) throws CoreException {
//				performDummySearch();
                if (elem instanceof IJavaProject) {
                    IJavaProject jproject = (IJavaProject) elem;
                    jproject.setRawClasspath(new IClasspathEntry[0], jproject.getProject().getFullPath(), null);
                }
                delete(elem.getResource());
            }
        };
        ResourcesPlugin.getWorkspace().run(runnable, null);
//		emptyDisplayLoop();
    }

    /**
     * Removes a resource. Retries if deletion failed (e.g. because the indexer
     * still locks the file).
     *
     * @param resource the resource to delete
     * @throws CoreException if operation failed
     */
    public static void delete(IResource resource) throws CoreException {
        for (int i = 0; i < MAX_RETRY; i++) {
            try {
                resource.delete(IResource.FORCE | IResource.ALWAYS_DELETE_PROJECT_CONTENT, null);
                i = MAX_RETRY;
            } catch (CoreException e) {
                if (i == MAX_RETRY - 1) {
                    JavaPlugin.log(e);
                    throw e;
                }
                try {
                    JavaPlugin.log(new IllegalStateException(
                            "sleep before retrying JavaProjectHelper.delete() for " + resource.getLocationURI()));
                    Thread.sleep(RETRY_DELAY); // give other threads time to close the file
                } catch (InterruptedException e1) {
                }
            }
        }
    }
}
