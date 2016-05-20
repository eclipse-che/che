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
package org.eclipse.che.plugin.java.server.jdt.refactoring;

import org.eclipse.che.plugin.java.server.jdt.testplugin.JavaProjectHelper;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;

/**
 * Sets up an 1.5 project with rtstubs15.jar and compiler, code formatting, code generation, and template options.
 */
public class RefactoringTestSetup extends AbstractRefactoringTestSetup {

//	public RefactoringTestSetup(Test test) {
//		super(test);
//	}

	public static final String CONTAINER= "src";
	private static IPackageFragmentRoot fgRoot;
	private static IPackageFragment fgPackageP;
	private static IJavaProject fgJavaTestProject;
	private static IPackageFragmentRoot fgJRELibrary;

	public static IPackageFragmentRoot getDefaultSourceFolder() throws Exception {
		if (fgRoot != null)
			return fgRoot;
		throw new Exception(RefactoringTestSetup.class.getName() + " not initialized");
	}

	public static IPackageFragmentRoot getJRELibrary() throws Exception {
		if (fgJRELibrary != null)
			return fgJRELibrary;
		throw new Exception(RefactoringTestSetup.class.getName() + " not initialized");
	}

	public static IJavaProject getProject()throws Exception {
		if (fgJavaTestProject != null)
			return fgJavaTestProject;
		throw new Exception(RefactoringTestSetup.class.getName() + " not initialized");
	}

	public static IPackageFragment getPackageP()throws Exception {
		if (fgPackageP != null)
			return fgPackageP;
		throw new Exception(RefactoringTestSetup.class.getName() + " not initialized");
	}

	public void setUp() throws Exception {
		super.setUp();
//		if (JavaPlugin.getActivePage() != null)
//			JavaPlugin.getActivePage().close(); // Closed perspective is NOT restored in tearDown()!

		fgJavaTestProject= JavaProjectHelper.createJavaProject("TestProject" + System.currentTimeMillis(), "bin");
		fgJRELibrary= addRTJar(fgJavaTestProject);
		fgRoot= JavaProjectHelper.addSourceContainer(fgJavaTestProject, CONTAINER);
		fgPackageP= fgRoot.createPackageFragment("p", true, null);

	}

	protected IPackageFragmentRoot addRTJar(IJavaProject project) throws CoreException {
		return JavaProjectHelper.addRTJar(project);
	}

	public void tearDown() throws Exception {
		JavaProjectHelper.delete(fgJavaTestProject);
		super.tearDown();
	}
}

