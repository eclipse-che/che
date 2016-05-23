/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Ferenc Hechler, ferenc_hechler@users.sourceforge.net - 83258 [jar exporter] Deploy java application as executable jar
 *******************************************************************************/
package org.eclipse.che.plugin.java.server.jdt.testplugin;

import junit.framework.TestCase;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
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
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.TypeNameRequestor;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.util.CoreUtility;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.ui.wizards.datatransfer.ImportOperation;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Map;
import java.util.zip.ZipFile;


/**
 * Helper methods to set up a IJavaProject.
 */
public class JavaProjectHelper {

    /**
     * XXX: Flag to enable/disable dummy search to synchronize with indexer. See https://bugs.eclipse.org/391927 .
     */
    private static final boolean PERFORM_DUMMY_SEARCH = false;

    /**
     * @deprecated use {@link #RT_STUBS_15}
     */
    public static final IPath RT_STUBS_13 = new Path("testresources/rtstubs.jar");
    /**
     * @deprecated use {@link #JUNIT_SRC_381}
     */
    public static final IPath JUNIT_SRC   = new Path("testresources/junit37-noUI-src.zip");

    public static final IPath  RT_STUBS_15        = new Path("testresources/rtstubs15.jar");
    public static final IPath  RT_STUBS_16        = new Path("testresources/rtstubs16.jar");
    public static final IPath  RT_STUBS_17        = new Path("testresources/rtstubs17.jar");
    public static final IPath  RT_STUBS_18        = new Path("testresources/rtstubs18.jar");
    public static final IPath  JUNIT_SRC_381      = new Path("testresources/junit381-noUI-src.zip");
    public static final String JUNIT_SRC_ENCODING = "ISO-8859-1";

    public static final IPath MYLIB        = new Path("testresources/mylib.jar");
    public static final IPath MYLIB_STDOUT = new Path("testresources/mylib_stdout.jar");
    public static final IPath MYLIB_SIG    = new Path("testresources/mylib_sig.jar");
    public static final IPath NLS_LIB      = new Path("testresources/nls.jar");

    private static final int MAX_RETRY   = 5;
    private static final int RETRY_DELAY = 1000;

    public static final int COUNT_CLASSES_RT_STUBS_15    = 661;
    public static final int COUNT_INTERFACES_RT_STUBS_15 = 135;

    public static final int COUNT_CLASSES_JUNIT_SRC_381    = 76;
    public static final int COUNT_INTERFACES_JUNIT_SRC_381 = 8;
    public static final int COUNT_CLASSES_MYLIB            = 3;

    /**
     * If set to <code>true</code> all resources that are
     * deleted using {@link #delete(IJavaElement)} and that contain mixed
     * line delimiters will result in a test failure.
     * <p>
     * Should be <code>false</code> during normal and Releng test runs
     * due to performance impact and because the search plug-in gets
     * loaded which results in a test failure.
     * </p>
     */
    private static final boolean ASSERT_NO_MIXED_LINE_DELIMIERS = false;

    /**
     * Creates a IJavaProject.
     * @param projectName The name of the project
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

        IFolder codenvyFolder = project.getFolder(".codenvy");
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

//	/**
//	 * Creates a Java project with JUnit source and rt.jar from
//	 * {@link #addVariableRTJar(IJavaProject, String, String, String)}.
//	 *
//	 * @param projectName the project name
//	 * @param srcContainerName the source container name
//	 * @param outputFolderName the output folder name
//	 * @return the IJavaProject
//	 * @throws CoreException
//	 * @throws IOException
//	 * @throws InvocationTargetException
//	 * @since 3.1
//	 */
//	public static IJavaProject createJavaProjectWithJUnitSource(String projectName, String srcContainerName, String outputFolderName)
// throws CoreException, IOException, InvocationTargetException {
//		IJavaProject project= createJavaProject(projectName, outputFolderName);
//
//		IPackageFragmentRoot jdk= JavaProjectHelper.addVariableRTJar(project, "JRE_LIB_TEST", null, null);//$NON-NLS-1$
//		TestCase.assertNotNull(jdk);
//
//		File junitSrcArchive= JavaTestPlugin.getDefault().getFileInPlugin(JUNIT_SRC_381);
//		TestCase.assertTrue(junitSrcArchive != null && junitSrcArchive.exists());
//
//		JavaProjectHelper.addSourceContainerWithImport(project, srcContainerName, junitSrcArchive, JUNIT_SRC_ENCODING);
//
//		return project;
//	}

    /**
     * Sets the compiler options to 1.8 for the given project.
     *
     * @param project the java project
     * @since 3.10
     */
    public static void set18CompilerOptions(IJavaProject project) {
        Map options = project.getOptions(false);
        set18CompilerOptions(options);
        project.setOptions(options);
    }

    /**
     * Sets the compiler options to 1.7 for the given project.
     * @param project the java project
     */
    public static void set17CompilerOptions(IJavaProject project) {
        Map options = project.getOptions(false);
        JavaProjectHelper.set17CompilerOptions(options);
        project.setOptions(options);
    }

    /**
     * Sets the compiler options to 1.6 for the given project.
     * @param project the java project
     */
    public static void set16CompilerOptions(IJavaProject project) {
        Map options = project.getOptions(false);
        JavaProjectHelper.set16CompilerOptions(options);
        project.setOptions(options);
    }

    /**
     * Sets the compiler options to 1.5 for the given project.
     * @param project the java project
     */
    public static void set15CompilerOptions(IJavaProject project) {
        Map options = project.getOptions(false);
        JavaProjectHelper.set15CompilerOptions(options);
        project.setOptions(options);
    }

    /**
     * Sets the compiler options to 1.4 for the given project.
     * @param project the java project
     */
    public static void set14CompilerOptions(IJavaProject project) {
        Map options = project.getOptions(false);
        JavaProjectHelper.set14CompilerOptions(options);
        project.setOptions(options);
    }

    /**
     * Sets the compiler options to 1.8
     *
     * @param options the compiler options to configure
     * @since 3.10
     */
    public static void set18CompilerOptions(Map options) {
        JavaCore.setComplianceOptions(org.eclipse.jdt.core.JavaCore.VERSION_1_8, options);
    }

    /**
     * Sets the compiler options to 1.7
     * @param options The compiler options to configure
     */
    public static void set17CompilerOptions(Map options) {
        JavaCore.setComplianceOptions(org.eclipse.jdt.core.JavaCore.VERSION_1_7, options);
    }

    /**
     * Sets the compiler options to 1.6
     * @param options The compiler options to configure
     */
    public static void set16CompilerOptions(Map options) {
        JavaCore.setComplianceOptions(org.eclipse.jdt.core.JavaCore.VERSION_1_6, options);
    }

    /**
     * Sets the compiler options to 1.5
     * @param options The compiler options to configure
     */
    public static void set15CompilerOptions(Map options) {
        JavaCore.setComplianceOptions(org.eclipse.jdt.core.JavaCore.VERSION_1_5, options);
    }

    /**
     * Sets the compiler options to 1.4
     * @param options The compiler options to configure
     */
    public static void set14CompilerOptions(Map options) {
        JavaCore.setComplianceOptions(org.eclipse.jdt.core.JavaCore.VERSION_1_4, options);
    }

    /**
     * Sets the compiler options to 1.3
     * @param options The compiler options to configure
     */
    public static void set13CompilerOptions(Map options) {
        JavaCore.setComplianceOptions(org.eclipse.jdt.core.JavaCore.VERSION_1_3, options);
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
            ((JavaProject)elem).close();
            JavaModelManager.getJavaModelManager().removePerProjectInfo((JavaProject)elem, true);
        }
        JavaModelManager.getJavaModelManager().resetTemporaryCache();
        IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
            public void run(IProgressMonitor monitor) throws CoreException {
//				performDummySearch();
                if (elem instanceof IJavaProject) {
                    IJavaProject jproject = (IJavaProject)elem;
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

    /**
     * Removes a package fragment. Retries if deletion failed (e.g. because the indexer
     * still locks a file).
     *
     * @param pack the package to delete
     * @throws CoreException if operation failed
     */
    public static void deletePackage(IPackageFragment pack) throws CoreException {
        for (int i = 0; i < MAX_RETRY; i++) {
            try {
                pack.delete(true, null);
                i = MAX_RETRY;
            } catch (CoreException e) {
                if (i == MAX_RETRY - 1) {
                    JavaPlugin.log(e);
                    throw e;
                }
                try {
                    JavaPlugin.log(new IllegalStateException(
                            "sleep before retrying JavaProjectHelper.delete() for package " + pack.getHandleIdentifier()));
                    Thread.sleep(RETRY_DELAY); // give other threads time to close the file
                } catch (InterruptedException e1) {
                }
            }
        }
    }

    /**
     * Removes all files in the project and sets the given classpath
     * @param jproject The project to clear
     * @param entries The default class path to set
     * @throws Exception Clearing the project failed
     */
    public static void clear(final IJavaProject jproject, final IClasspathEntry[] entries) throws Exception {
//		performDummySearch();
        IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
            public void run(IProgressMonitor monitor) throws CoreException {
                jproject.setRawClasspath(entries, null);

                IResource[] resources = jproject.getProject().members();
                for (int i = 0; i < resources.length; i++) {
//					if (!resources[i].getName().startsWith(".")) {
                    delete(resources[i]);
//					}
                }
            }
        };
        ResourcesPlugin.getWorkspace().run(runnable, null);

//		JavaProjectHelper.emptyDisplayLoop();
    }
//

	public static void mustPerformDummySearch() throws JavaModelException {
		performDummySearch(SearchEngine.createWorkspaceScope(), true);
	}

	public static void mustPerformDummySearch(IJavaElement element) throws JavaModelException {
		performDummySearch(SearchEngine.createJavaSearchScope(new IJavaElement[] { element }), true);
	}

	public static void performDummySearch() throws JavaModelException {
		performDummySearch(SearchEngine.createWorkspaceScope(), PERFORM_DUMMY_SEARCH);
	}

	public static void performDummySearch(IJavaElement element) throws JavaModelException {
		performDummySearch(SearchEngine.createJavaSearchScope(new IJavaElement[] { element }), PERFORM_DUMMY_SEARCH);
	}

	private static void performDummySearch(IJavaSearchScope searchScope, boolean doIt) throws JavaModelException {
		/*
		 * Workaround for intermittent test failures. The problem is that the Java indexer
		 * may still be reading a file that has just been created, but a test already tries to delete
		 * the file again.
		 *
		 * This can theoretically also happen in real life, but it's expected to be very rare,
		 * and there's no good solution for the problem, since the Java indexer should not
		 * take a workspace lock for these files.
		 *
		 * performDummySearch() was found to be a performance bottleneck, so we've disabled it in most situations.
		 * Use a mustPerformDummySearch() method if you really need it and you can't
		 * use a delete(..) method that retries a few times before failing.
		 */
		if (!doIt)
			return;

		new SearchEngine().searchAllTypeNames(
				null,
				SearchPattern.R_EXACT_MATCH,
				"XXXXXXXXX".toCharArray(), // make sure we search a concrete name. This is faster according to Kent
				SearchPattern.R_EXACT_MATCH | SearchPattern.R_CASE_SENSITIVE,
				IJavaSearchConstants.CLASS,
				searchScope,
				new Requestor(),
				IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
				null);
	}

    /**
     * Adds a source container to a IJavaProject.
     * @param jproject The parent project
     * @param containerName The name of the new source container
     * @return The handle to the new source container
     * @throws CoreException Creation failed
     */
    public static IPackageFragmentRoot addSourceContainer(IJavaProject jproject, String containerName) throws CoreException {
        return addSourceContainer(jproject, containerName, new Path[0]);
    }

    /**
     * Adds a source container to a IJavaProject.
     * @param jproject The parent project
     * @param containerName The name of the new source container
     * @param exclusionFilters Exclusion filters to set
     * @return The handle to the new source container
     * @throws CoreException Creation failed
     */
    public static IPackageFragmentRoot addSourceContainer(IJavaProject jproject, String containerName, IPath[] exclusionFilters)
            throws CoreException {
        return addSourceContainer(jproject, containerName, new Path[0], exclusionFilters);
    }

    /**
     * Adds a source container to a IJavaProject.
     * @param jproject The parent project
     * @param containerName The name of the new source container
     * @param inclusionFilters Inclusion filters to set
     * @param exclusionFilters Exclusion filters to set
     * @return The handle to the new source container
     * @throws CoreException Creation failed
     */
    public static IPackageFragmentRoot addSourceContainer(IJavaProject jproject, String containerName, IPath[] inclusionFilters,
                                                          IPath[] exclusionFilters) throws CoreException {
        return addSourceContainer(jproject, containerName, inclusionFilters, exclusionFilters, null);
    }

    /**
     * Adds a source container to a IJavaProject.
     * @param jproject The parent project
     * @param containerName The name of the new source container
     * @param inclusionFilters Inclusion filters to set
     * @param exclusionFilters Exclusion filters to set
     * @param outputLocation The location where class files are written to, <b>null</b> for project output folder
     * @return The handle to the new source container
     * @throws CoreException Creation failed
     */
    public static IPackageFragmentRoot addSourceContainer(IJavaProject jproject, String containerName, IPath[] inclusionFilters,
                                                          IPath[] exclusionFilters, String outputLocation) throws CoreException {
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
        IClasspathEntry cpe = JavaCore.newSourceEntry(root.getPath(), inclusionFilters, exclusionFilters, outputPath);
        addToClasspath(jproject, cpe);
        return root;
    }

    /**
     * Adds a source container to a IJavaProject and imports all files contained
     * in the given ZIP file.
     * @param jproject The parent project
     * @param containerName Name of the source container
     * @param zipFile Archive to import
     * @param containerEncoding encoding for the generated source container
     * @return The handle to the new source container
     * @throws InvocationTargetException Creation failed
     * @throws CoreException Creation failed
     * @throws IOException Creation failed
     */
    public static IPackageFragmentRoot addSourceContainerWithImport(IJavaProject jproject, String containerName, File zipFile, String
            containerEncoding) throws InvocationTargetException, CoreException, IOException {
        return addSourceContainerWithImport(jproject, containerName, zipFile, containerEncoding, new Path[0]);
    }

    /**
     * Adds a source container to a IJavaProject and imports all files contained
     * in the given ZIP file.
     * @param jproject The parent project
     * @param containerName Name of the source container
     * @param zipFile Archive to import
     * @param containerEncoding encoding for the generated source container
     * @param exclusionFilters Exclusion filters to set
     * @return The handle to the new source container
     * @throws InvocationTargetException Creation failed
     * @throws CoreException Creation failed
     * @throws IOException Creation failed
     */
    public static IPackageFragmentRoot addSourceContainerWithImport(IJavaProject jproject, String containerName, File zipFile, String
            containerEncoding, IPath[] exclusionFilters) throws InvocationTargetException, CoreException, IOException {
        ZipFile file = new ZipFile(zipFile);
        try {
            IPackageFragmentRoot root = addSourceContainer(jproject, containerName, exclusionFilters);
            ((IContainer)root.getCorrespondingResource()).setDefaultCharset(containerEncoding, null);
            importFilesFromZip(file, root.getPath(), null);
            return root;
        } finally {
            file.close();
        }
    }

    /**
     * Removes a source folder from a IJavaProject.
     * @param jproject The parent project
     * @param containerName Name of the source folder to remove
     * @throws CoreException Remove failed
     */
    public static void removeSourceContainer(IJavaProject jproject, String containerName) throws CoreException {
        IFolder folder = jproject.getProject().getFolder(containerName);
        removeFromClasspath(jproject, folder.getFullPath());
        folder.delete(true, null);
    }

    /**
     * Adds a library entry to a IJavaProject.
     * @param jproject The parent project
     * @param path The path of the library to add
     * @return The handle of the created root
     * @throws JavaModelException
     */
    public static IPackageFragmentRoot addLibrary(IJavaProject jproject, IPath path) throws JavaModelException {
        return addLibrary(jproject, path, null, null);
    }

    /**
     * Adds a library entry with source attachment to a IJavaProject.
     * @param jproject The parent project
     * @param path The path of the library to add
     * @param sourceAttachPath The source attachment path
     * @param sourceAttachRoot The source attachment root path
     * @return The handle of the created root
     * @throws JavaModelException
     */
    public static IPackageFragmentRoot addLibrary(IJavaProject jproject, IPath path, IPath sourceAttachPath, IPath sourceAttachRoot)
            throws JavaModelException {
        IClasspathEntry cpe = JavaCore.newLibraryEntry(path, sourceAttachPath, sourceAttachRoot);
        addToClasspath(jproject, cpe);
        IResource workspaceResource = ResourcesPlugin.getWorkspace().getRoot().findMember(path);
        if (workspaceResource != null) {
            return jproject.getPackageFragmentRoot(workspaceResource);
        }
        return jproject.getPackageFragmentRoot(path.toString());
    }


    /**
     * Copies the library into the project and adds it as library entry.
     * @param jproject The parent project
     * @param jarPath
     * @param sourceAttachPath The source attachment path
     * @param sourceAttachRoot The source attachment root path
     * @return The handle of the created root
     * @throws IOException
     * @throws CoreException
     */
    public static IPackageFragmentRoot addLibraryWithImport(IJavaProject jproject, IPath jarPath, IPath sourceAttachPath,
                                                            IPath sourceAttachRoot) throws IOException, CoreException {
        IProject project = jproject.getProject();
        IFile newFile = project.getFile(jarPath.lastSegment());
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(jarPath.toFile());
            newFile.create(inputStream, true, null);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                }
            }
        }
        return addLibrary(jproject, newFile.getFullPath(), sourceAttachPath, sourceAttachRoot);
    }

    /**
     * Creates and adds a class folder to the class path.
     * @param jproject The parent project
     * @param containerName
     * @param sourceAttachPath The source attachment path
     * @param sourceAttachRoot The source attachment root path
     * @return The handle of the created root
     * @throws CoreException
     */
    public static IPackageFragmentRoot addClassFolder(IJavaProject jproject, String containerName, IPath sourceAttachPath,
                                                      IPath sourceAttachRoot) throws CoreException {
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
        IClasspathEntry cpe = JavaCore.newLibraryEntry(container.getFullPath(), sourceAttachPath, sourceAttachRoot);
        addToClasspath(jproject, cpe);
        return jproject.getPackageFragmentRoot(container);
    }

//	/**
//	 * Creates and adds a class folder to the class path and imports all files
//	 * contained in the given ZIP file.
//	 * @param jproject The parent project
//	 * @param containerName
//	 * @param sourceAttachPath The source attachment path
//	 * @param sourceAttachRoot The source attachment root path
//	 * @param zipFile
//	 * @return The handle of the created root
//	 * @throws IOException
//	 * @throws CoreException
//	 * @throws InvocationTargetException
//	 */
//	public static IPackageFragmentRoot addClassFolderWithImport(IJavaProject jproject, String containerName, IPath sourceAttachPath, IPath
// sourceAttachRoot, File zipFile) throws IOException, CoreException, InvocationTargetException {
//		ZipFile file= new ZipFile(zipFile);
//		try {
//			IPackageFragmentRoot root= addClassFolder(jproject, containerName, sourceAttachPath, sourceAttachRoot);
//			importFilesFromZip(file, root.getPath(), null);
//			return root;
//		} finally {
//			file.close();
//		}
//	}

    /**
     * Adds a library entry pointing to a JRE (stubs only)
     * and sets the right compiler options.
     * <p>Currently, the compiler compliance level is 1.5.
     *
     * @param jproject target
     * @return the new package fragment root
     * @throws CoreException
     */
    public static IPackageFragmentRoot addRTJar(IJavaProject jproject) throws CoreException {
        return addRTJar15(jproject);
    }

    public static IPackageFragmentRoot addRTJar13(IJavaProject jproject) throws CoreException {
        IPath[] rtJarPath = findRtJar(RT_STUBS_13);

        Map options = jproject.getOptions(false);
        JavaProjectHelper.set13CompilerOptions(options);
        jproject.setOptions(options);

        return addLibrary(jproject, rtJarPath[0], rtJarPath[1], rtJarPath[2]);
    }

    public static IPackageFragmentRoot addRTJar15(IJavaProject jproject) throws CoreException, JavaModelException {
        IPath[] rtJarPath = findRtJar(RT_STUBS_15);
        set15CompilerOptions(jproject);
        return addLibrary(jproject, rtJarPath[0], rtJarPath[1], rtJarPath[2]);
    }

    public static IPackageFragmentRoot addRTJar16(IJavaProject jproject) throws CoreException {
        IPath[] rtJarPath = findRtJar(RT_STUBS_16);
        set16CompilerOptions(jproject);
        return addLibrary(jproject, rtJarPath[0], rtJarPath[1], rtJarPath[2]);
    }

    public static IPackageFragmentRoot addRTJar17(IJavaProject jproject) throws CoreException {
        IPath[] rtJarPath = findRtJar(RT_STUBS_17);
        set17CompilerOptions(jproject);
        return addLibrary(jproject, rtJarPath[0], rtJarPath[1], rtJarPath[2]);
    }

    public static IPackageFragmentRoot addRTJar18(IJavaProject jproject) throws CoreException {
        IPath[] rtJarPath = findRtJar(RT_STUBS_18);
        set18CompilerOptions(jproject);
        return addLibrary(jproject, rtJarPath[0], rtJarPath[1], rtJarPath[2]);
    }

//	/**
//	 * Adds a variable entry with source attachment to a IJavaProject.
//	 * Can return null if variable can not be resolved.
//	 * @param jproject The parent project
//	 * @param path The variable path
//	 * @param sourceAttachPath The source attachment path (variable path)
//	 * @param sourceAttachRoot The source attachment root path (variable path)
//	 * @return The added package fragment root
//	 * @throws JavaModelException
//	 */
//	public static IPackageFragmentRoot addVariableEntry(IJavaProject jproject, IPath path, IPath sourceAttachPath, IPath sourceAttachRoot)
// throws JavaModelException {
//		IClasspathEntry cpe= JavaCore.newVariableEntry(path, sourceAttachPath, sourceAttachRoot);
//		addToClasspath(jproject, cpe);
//		IPath resolvedPath= JavaCore.getResolvedVariablePath(path);
//		if (resolvedPath != null) {
//			return jproject.getPackageFragmentRoot(resolvedPath.toString());
//		}
//		return null;
//	}
//
//	public static IPackageFragmentRoot addVariableRTJar13(IJavaProject jproject, String libVarName, String srcVarName, String
// srcrootVarName) throws CoreException {
//		return addVariableRTJar(jproject, RT_STUBS_13, libVarName, srcVarName, srcrootVarName);
//	}

//	/**
//	 * Adds a variable entry pointing to a current JRE (stubs only)
//	 * and sets the compiler compliance level on the project accordingly.
//	 * The arguments specify the names of the variables to be used.
//	 * Currently, the compiler compliance level is set to 1.5.
//	 *
//	 * @param jproject the project to add the variable RT JAR
//	 * @param libVarName Name of the variable for the library
//	 * @param srcVarName Name of the variable for the source attachment. Can be <code>null</code>.
//	 * @param srcrootVarName name of the variable for the source attachment root. Can be <code>null</code>.
//	 * @return the new package fragment root
//	 * @throws CoreException Creation failed
//	 */
//	public static IPackageFragmentRoot addVariableRTJar(IJavaProject jproject, String libVarName, String srcVarName, String
// srcrootVarName) throws CoreException {
//		return addVariableRTJar(jproject, RT_STUBS_15, libVarName, srcVarName, srcrootVarName);
//	}
//
//	/**
//	 * Adds a variable entry pointing to a current JRE (stubs only).
//	 * The arguments specify the names of the variables to be used.
//	 * Clients must not forget to set the right compiler compliance level on the project.
//	 *
//	 * @param jproject the project to add the variable RT JAR
//	 * @param rtStubsPath path to an rt.jar
//	 * @param libVarName name of the variable for the library
//	 * @param srcVarName Name of the variable for the source attachment. Can be <code>null</code>.
//	 * @param srcrootVarName Name of the variable for the source attachment root. Can be <code>null</code>.
//	 * @return the new package fragment root
//	 * @throws CoreException Creation failed
//	 */
//	private static IPackageFragmentRoot addVariableRTJar(IJavaProject jproject, IPath rtStubsPath, String libVarName, String srcVarName,
// String srcrootVarName) throws CoreException {
//		IPath[] rtJarPaths= findRtJar(rtStubsPath);
//		IPath libVarPath= new Path(libVarName);
//		IPath srcVarPath= null;
//		IPath srcrootVarPath= null;
//		JavaCore.setClasspathVariable(libVarName, rtJarPaths[0], null);
//		if (srcVarName != null) {
//			IPath varValue= rtJarPaths[1] != null ? rtJarPaths[1] : Path.EMPTY;
//			JavaCore.setClasspathVariable(srcVarName, varValue, null);
//			srcVarPath= new Path(srcVarName);
//		}
//		if (srcrootVarName != null) {
//			IPath varValue= rtJarPaths[2] != null ? rtJarPaths[2] : Path.EMPTY;
//			JavaCore.setClasspathVariable(srcrootVarName, varValue, null);
//			srcrootVarPath= new Path(srcrootVarName);
//		}
//		return addVariableEntry(jproject, libVarPath, srcVarPath, srcrootVarPath);
//	}

    /**
     * Adds a required project entry.
     * @param jproject Parent project
     * @param required Project to add to the build path
     * @throws JavaModelException Creation failed
     */
    public static void addRequiredProject(IJavaProject jproject, IJavaProject required) throws JavaModelException {
        IClasspathEntry cpe = JavaCore.newProjectEntry(required.getProject().getFullPath());
        addToClasspath(jproject, cpe);
    }

    public static void removeFromClasspath(IJavaProject jproject, IPath path) throws JavaModelException {
        IClasspathEntry[] oldEntries = jproject.getRawClasspath();
        int nEntries = oldEntries.length;
        ArrayList list = new ArrayList(nEntries);
        for (int i = 0; i < nEntries; i++) {
            IClasspathEntry curr = oldEntries[i];
            if (!path.equals(curr.getPath())) {
                list.add(curr);
            }
        }
        IClasspathEntry[] newEntries = (IClasspathEntry[])list.toArray(new IClasspathEntry[list.size()]);
        jproject.setRawClasspath(newEntries, null);
    }

    public static void addToClasspath(IJavaProject jproject, IClasspathEntry cpe) throws JavaModelException {
        IClasspathEntry[] oldEntries = jproject.getRawClasspath();
        for (int i = 0; i < oldEntries.length; i++) {
            if (oldEntries[i].equals(cpe)) {
                return;
            }
        }
        int nEntries = oldEntries.length;
        IClasspathEntry[] newEntries = new IClasspathEntry[nEntries + 1];
        System.arraycopy(oldEntries, 0, newEntries, 0, nEntries);
        newEntries[nEntries] = cpe;
        jproject.setRawClasspath(newEntries, null);
    }

    /**
     * @param rtStubsPath the path to the RT stubs
     * @return a rt.jar (stubs only)
     * @throws CoreException
     */
    public static IPath[] findRtJar(IPath rtStubsPath) throws CoreException {
        File rtStubs = new File(JavaProjectHelper.class.getClassLoader().getResource(rtStubsPath.toOSString()).getFile());
        TestCase.assertNotNull(rtStubs);
        TestCase.assertTrue(rtStubs.exists());
        return new IPath[]{
                Path.fromOSString(rtStubs.getPath()),
                null,
                null
        };
    }

    private static void addNatureToProject(IProject proj, String natureId, IProgressMonitor monitor) throws CoreException {
        IProjectDescription description = proj.getDescription();
        String[] prevNatures = description.getNatureIds();
        String[] newNatures = new String[prevNatures.length + 1];
        System.arraycopy(prevNatures, 0, newNatures, 0, prevNatures.length);
        newNatures[prevNatures.length] = natureId;
        description.setNatureIds(newNatures);
        proj.setDescription(description, monitor);
    }

    public static void importFilesFromZip(ZipFile srcZipFile, IPath destPath, IProgressMonitor monitor) throws InvocationTargetException {
        ZipFileStructureProvider structureProvider = new ZipFileStructureProvider(srcZipFile);
        try {
            ImportOperation op = new ImportOperation(destPath, structureProvider.getRoot(), structureProvider, new ImportOverwriteQuery());
            op.run(monitor);
        } catch (InterruptedException e) {
            // should not happen
        }
    }

//	/**
//	 * Imports resources from <code>bundleSourcePath</code> inside <code>bundle</code> into <code>importTarget</code>.
//	 *
//	 * @param importTarget the parent container
//	 * @param bundle the bundle
//	 * @param bundleSourcePath the path to a folder containing resources
//	 *
//	 * @throws CoreException import failed
//	 * @throws IOException import failed
//	 */
//	public static void importResources(IContainer importTarget, Bundle bundle, String bundleSourcePath) throws CoreException, IOException {
//		Enumeration entryPaths= bundle.getEntryPaths(bundleSourcePath);
//		while (entryPaths.hasMoreElements()) {
//			String path= (String) entryPaths.nextElement();
//			IPath name= new Path(path.substring(bundleSourcePath.length()));
//			if (path.endsWith("/")) {
//				IFolder folder= importTarget.getFolder(name);
//				folder.create(false, true, null);
//				importResources(folder, bundle, path);
//			} else {
//				URL url= bundle.getEntry(path);
//				IFile file= importTarget.getFile(name);
//				file.create(url.openStream(), true, null);
//			}
//		}
//	}

    private static class ImportOverwriteQuery implements IOverwriteQuery {
        public String queryOverwrite(String file) {
            return ALL;
        }
    }

	private static class Requestor extends TypeNameRequestor {
	}

//	public static void emptyDisplayLoop() {
//		boolean showDebugInfo= false;
//
//		Display display= Display.getCurrent();
//		if (display != null) {
//			if (showDebugInfo) {
//				try {
//					Synchronizer synchronizer= display.getSynchronizer();
//					Field field= Synchronizer.class.getDeclaredField("messageCount");
//					field.setAccessible(true);
//					System.out.println("Processing " + field.getInt(synchronizer) + " messages in queue");
//				} catch (Exception e) {
//					// ignore
//					System.out.println(e);
//				}
//			}
//			while (display.readAndDispatch()) { /*loop*/ }
//		}
//	}
}

