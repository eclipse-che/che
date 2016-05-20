/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
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
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.junit.After;
import org.junit.Before;


/**
 * Sets up an 1.5 project with rtstubs15.jar and compiler, code formatting, code generation, and template options.
 */
public class ProjectTestSetup /*extends TestSetup */{

	public static final String PROJECT_NAME= "TestSetupProject";

	public static IJavaProject getProject() {
		IProject project= ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT_NAME);
		return JavaCore.create(project);
	}

	public static IClasspathEntry[] getDefaultClasspath() throws CoreException {
		IPath[] rtJarPath= JavaProjectHelper.findRtJar(JavaProjectHelper.RT_STUBS_15);
		return new IClasspathEntry[] {  JavaCore.newLibraryEntry(rtJarPath[0], rtJarPath[1], rtJarPath[2], true) };
	}


	private IJavaProject fJProject;

	private boolean fAutobuilding;

//	public ProjectTestSetup(Test test) {
//		super(test);
//	}

	/* (non-Javadoc)
	 * @see junit.extensions.TestSetup#setUp()
	 */
	@Before
	public void setUp() throws Exception {
//		super.setUp();

		if (projectExists()) { // allow nesting of ProjectTestSetups
			return;
		}

//		fAutobuilding = CoreUtility.setAutoBuilding(false);

		fJProject= createAndInitializeProject();
		JavaCore.setOptions(TestOptions.getDefaultOptions());
		TestOptions.initializeCodeGenerationOptions();
		JavaPlugin.getDefault().getCodeTemplateStore().load();
	}

	protected boolean projectExists() {
		return getProject().exists();
	}

	protected IJavaProject createAndInitializeProject() throws CoreException {
		IJavaProject javaProject= JavaProjectHelper.createJavaProject(PROJECT_NAME, "bin");
		javaProject.setRawClasspath(getDefaultClasspath(), null);
		TestOptions.initializeProjectOptions(javaProject);
		return javaProject;
	}

	@After
	public void tearDown() throws Exception {
		if (fJProject != null) {
			JavaProjectHelper.delete(fJProject);
//			CoreUtility.setAutoBuilding(fAutobuilding);
		}
	}

}
