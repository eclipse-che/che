/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.java.server;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.ide.ext.java.shared.dto.model.JavaProject;
import org.eclipse.che.ide.ext.java.shared.dto.model.PackageFragment;
import org.eclipse.che.ide.ext.java.shared.dto.model.PackageFragmentRoot;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaModel;
import org.eclipse.jdt.internal.core.JavaModelManager;

/**
 * Manager for java navigation operations. Contains methods that convert jdt Java models to DTO
 * objects.
 *
 * @author Evgen Vidolob
 * @author Valeriy Svydenko
 */
@Singleton
public class JavaNavigation {
  @Inject
  public JavaNavigation() {}

  public List<JavaProject> getAllProjectsAndPackages(boolean includePackages)
      throws JavaModelException {
    JavaModel javaModel = JavaModelManager.getJavaModelManager().getJavaModel();
    IJavaProject[] javaProjects = javaModel.getJavaProjects();
    List<JavaProject> result = new ArrayList<>();
    for (IJavaProject javaProject : javaProjects) {
      if (javaProject.exists()) {
        JavaProject project = DtoFactory.newDto(JavaProject.class);
        project.setName(javaProject.getElementName());
        project.setPath(javaProject.getPath().toOSString());
        project.setPackageFragmentRoots(toPackageRoots(javaProject, includePackages));
        result.add(project);
      }
    }
    return result;
  }

  private List<PackageFragmentRoot> toPackageRoots(
      IJavaProject javaProject, boolean includePackages) throws JavaModelException {
    IPackageFragmentRoot[] packageFragmentRoots = javaProject.getAllPackageFragmentRoots();
    List<PackageFragmentRoot> result = new ArrayList<>();
    for (IPackageFragmentRoot packageFragmentRoot : packageFragmentRoots) {
      if (packageFragmentRoot.getKind() == IPackageFragmentRoot.K_SOURCE
          && javaProject.getPath().isPrefixOf(packageFragmentRoot.getPath())) {
        PackageFragmentRoot root = DtoFactory.newDto(PackageFragmentRoot.class);
        root.setPath(packageFragmentRoot.getPath().toOSString());
        root.setProjectPath(packageFragmentRoot.getJavaProject().getPath().toOSString());
        if (includePackages) {
          root.setPackageFragments(toPackageFragments(packageFragmentRoot));
        }
        result.add(root);
      }
    }
    return result;
  }

  private List<PackageFragment> toPackageFragments(IPackageFragmentRoot packageFragmentRoot)
      throws JavaModelException {
    IJavaElement[] children = packageFragmentRoot.getChildren();
    if (children == null) {
      return null;
    }
    List<PackageFragment> result = new ArrayList<>();
    for (IJavaElement child : children) {
      if (child instanceof IPackageFragment) {
        IPackageFragment packageFragment = (IPackageFragment) child;
        PackageFragment fragment = DtoFactory.newDto(PackageFragment.class);
        fragment.setElementName(packageFragment.getElementName());
        fragment.setPath(packageFragment.getPath().toOSString());
        fragment.setProjectPath(packageFragment.getJavaProject().getPath().toOSString());
        result.add(fragment);
      }
    }
    return result;
  }
}
