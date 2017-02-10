/*******************************************************************************
 * Copyright (c) 2013, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.java.server.jdt.testplugin;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

public class Java18ProjectTestSetup extends ProjectTestSetup {

	public static final String PROJECT_NAME18= "TestSetupProject18";

	public static IJavaProject getProject() {
		IProject project= ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT_NAME18);
		return JavaCore.create(project);
	}

	public static IClasspathEntry[] getDefaultClasspath() throws CoreException {
		IPath[] rtJarPath= JavaProjectHelper.findRtJar(JavaProjectHelper.RT_STUBS_18);
		return new IClasspathEntry[] { JavaCore.newLibraryEntry(rtJarPath[0], rtJarPath[1], rtJarPath[2], true) };
	}

//	public static String getJdtAnnotations20Path() throws IOException {
//		Bundle[] annotationsBundles= JavaPlugin.getDefault().getBundles("org.eclipse.jdt.annotation", "2.0.0"); //$NON-NLS-1$
//		File bundleFile= FileLocator.getBundleFile(annotationsBundles[0]);
//		String path= bundleFile.getPath();
//		if (bundleFile.isDirectory()) {
//			path= bundleFile.getPath() + "/bin";
//		}
//		return path;
//	}

//	public Java18ProjectTestSetup(Test test) {
//		super(test);
//	}

	@Override
	protected boolean projectExists() {
		return getProject().exists();
	}

	@Override
	protected IJavaProject createAndInitializeProject() throws CoreException {
		IJavaProject javaProject= JavaProjectHelper.createJavaProject(PROJECT_NAME18, "bin");
		javaProject.setRawClasspath(getDefaultClasspath(), null);
		JavaProjectHelper.set18CompilerOptions(javaProject);
		return javaProject;
	}

}
