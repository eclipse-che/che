/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2011 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.mapping.RemoteResourceMappingContext;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.resources.mapping.ResourceMappingContext;
import org.eclipse.core.resources.mapping.ResourceTraversal;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.model.JavaModelProvider;

/**
 * An abstract super class to describe mappings from a Java element to a set of resources. The class
 * also provides factory methods to create resource mappings.
 *
 * @since 3.1
 */
public abstract class JavaElementResourceMapping extends ResourceMapping {

  protected JavaElementResourceMapping() {}

  public IJavaElement getJavaElement() {
    Object o = getModelObject();
    if (o instanceof IJavaElement) return (IJavaElement) o;
    return null;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof JavaElementResourceMapping)) return false;
    return getJavaElement().equals(((JavaElementResourceMapping) obj).getJavaElement());
  }

  @Override
  public int hashCode() {
    IJavaElement javaElement = getJavaElement();
    if (javaElement == null) return super.hashCode();

    return javaElement.hashCode();
  }

  @Override
  public String getModelProviderId() {
    return JavaModelProvider.JAVA_MODEL_PROVIDER_ID;
  }

  @Override
  public boolean contains(ResourceMapping mapping) {
    if (mapping instanceof JavaElementResourceMapping) {
      JavaElementResourceMapping javaMapping = (JavaElementResourceMapping) mapping;
      IJavaElement element = getJavaElement();
      IJavaElement other = javaMapping.getJavaElement();
      if (other != null && element != null) return element.getPath().isPrefixOf(other.getPath());
    }
    return false;
  }

  // ---- the factory code ---------------------------------------------------------------

  private static final class JavaModelResourceMapping extends JavaElementResourceMapping {
    private final IJavaModel fJavaModel;

    private JavaModelResourceMapping(IJavaModel model) {
      Assert.isNotNull(model);
      fJavaModel = model;
    }

    @Override
    public Object getModelObject() {
      return fJavaModel;
    }

    @Override
    public IProject[] getProjects() {
      IJavaProject[] projects = null;
      try {
        projects = fJavaModel.getJavaProjects();
      } catch (JavaModelException e) {
        JavaPlugin.log(e);
        return new IProject[0];
      }
      IProject[] result = new IProject[projects.length];
      for (int i = 0; i < projects.length; i++) {
        result[i] = projects[i].getProject();
      }
      return result;
    }

    @Override
    public ResourceTraversal[] getTraversals(
        ResourceMappingContext context, IProgressMonitor monitor) throws CoreException {
      IJavaProject[] projects = fJavaModel.getJavaProjects();
      ResourceTraversal[] result = new ResourceTraversal[projects.length];
      for (int i = 0; i < projects.length; i++) {
        result[i] =
            new ResourceTraversal(
                new IResource[] {projects[i].getProject()}, IResource.DEPTH_INFINITE, 0);
      }
      return result;
    }
  }

  private static final class JavaProjectResourceMapping extends JavaElementResourceMapping {
    private final IJavaProject fProject;

    private JavaProjectResourceMapping(IJavaProject project) {
      Assert.isNotNull(project);
      fProject = project;
    }

    @Override
    public Object getModelObject() {
      return fProject;
    }

    @Override
    public IProject[] getProjects() {
      return new IProject[] {fProject.getProject()};
    }

    @Override
    public ResourceTraversal[] getTraversals(
        ResourceMappingContext context, IProgressMonitor monitor) throws CoreException {
      return new ResourceTraversal[] {
        new ResourceTraversal(new IResource[] {fProject.getProject()}, IResource.DEPTH_INFINITE, 0)
      };
    }
  }

  private static final class PackageFragementRootResourceMapping
      extends JavaElementResourceMapping {
    private final IPackageFragmentRoot fRoot;

    private PackageFragementRootResourceMapping(IPackageFragmentRoot root) {
      Assert.isNotNull(root);
      fRoot = root;
    }

    @Override
    public Object getModelObject() {
      return fRoot;
    }

    @Override
    public IProject[] getProjects() {
      return new IProject[] {fRoot.getJavaProject().getProject()};
    }

    @Override
    public ResourceTraversal[] getTraversals(
        ResourceMappingContext context, IProgressMonitor monitor) throws CoreException {
      return new ResourceTraversal[] {
        new ResourceTraversal(new IResource[] {fRoot.getResource()}, IResource.DEPTH_INFINITE, 0)
      };
    }
  }

  private static final class PackageFragmentResourceMapping extends JavaElementResourceMapping {
    private final IPackageFragment fPack;

    private PackageFragmentResourceMapping(IPackageFragment pack) {
      Assert.isNotNull(pack);
      fPack = pack;
    }

    @Override
    public Object getModelObject() {
      return fPack;
    }

    @Override
    public IProject[] getProjects() {
      return new IProject[] {fPack.getJavaProject().getProject()};
    }

    @Override
    public ResourceTraversal[] getTraversals(
        ResourceMappingContext context, IProgressMonitor monitor) throws CoreException {
      if (context instanceof RemoteResourceMappingContext) {
        return getRemotePackageFragmentTraversals(
            fPack, (RemoteResourceMappingContext) context, monitor);
      } else {
        return getPackageFragmentTraversals(fPack);
      }
    }
  }

  private static ResourceTraversal[] getPackageFragmentTraversals(IPackageFragment pack)
      throws CoreException {
    ArrayList<ResourceTraversal> res = new ArrayList<ResourceTraversal>();
    IContainer container = (IContainer) pack.getResource();

    if (container != null) {
      res.add(new ResourceTraversal(new IResource[] {container}, IResource.DEPTH_ONE, 0));
      if (pack.exists()) { // folder may not exist any more, see
        // https://bugs.eclipse.org/bugs/show_bug.cgi?id=269167
        Object[] nonJavaResources = pack.getNonJavaResources();
        for (int i = 0; i < nonJavaResources.length; i++) {
          Object resource = nonJavaResources[i];
          if (resource instanceof IFolder) {
            res.add(
                new ResourceTraversal(
                    new IResource[] {(IResource) resource}, IResource.DEPTH_INFINITE, 0));
          }
        }
      }
    }

    return res.toArray(new ResourceTraversal[res.size()]);
  }

  private static ResourceTraversal[] getRemotePackageFragmentTraversals(
      IPackageFragment pack, RemoteResourceMappingContext context, IProgressMonitor monitor)
      throws CoreException {
    ArrayList<ResourceTraversal> res = new ArrayList<ResourceTraversal>();
    IContainer container = (IContainer) pack.getResource();

    if (container != null) {
      res.add(new ResourceTraversal(new IResource[] {container}, IResource.DEPTH_ONE, 0));
      IResource[] remoteMembers = context.fetchRemoteMembers(container, monitor);
      if (remoteMembers == null) {
        remoteMembers = context.fetchMembers(container, monitor);
      }
      if (remoteMembers != null) {
        for (int i = 0; i < remoteMembers.length; i++) {
          IResource member = remoteMembers[i];
          if (member instanceof IFolder
              && JavaConventionsUtil.validatePackageName(member.getName(), pack).getSeverity()
                  == IStatus.ERROR) {
            res.add(new ResourceTraversal(new IResource[] {member}, IResource.DEPTH_INFINITE, 0));
          }
        }
      }
    }
    return res.toArray(new ResourceTraversal[res.size()]);
  }

  private static final class CompilationUnitResourceMapping extends JavaElementResourceMapping {
    private final ICompilationUnit fUnit;

    private CompilationUnitResourceMapping(ICompilationUnit unit) {
      Assert.isNotNull(unit);
      fUnit = unit;
    }

    @Override
    public Object getModelObject() {
      return fUnit;
    }

    @Override
    public IProject[] getProjects() {
      return new IProject[] {fUnit.getJavaProject().getProject()};
    }

    @Override
    public ResourceTraversal[] getTraversals(
        ResourceMappingContext context, IProgressMonitor monitor) throws CoreException {
      return new ResourceTraversal[] {
        new ResourceTraversal(new IResource[] {fUnit.getResource()}, IResource.DEPTH_ONE, 0)
      };
    }
  }

  private static final class ClassFileResourceMapping extends JavaElementResourceMapping {
    private final IClassFile fClassFile;

    private ClassFileResourceMapping(IClassFile classFile) {
      fClassFile = classFile;
    }

    @Override
    public Object getModelObject() {
      return fClassFile;
    }

    @Override
    public IProject[] getProjects() {
      return new IProject[] {fClassFile.getJavaProject().getProject()};
    }

    @Override
    public ResourceTraversal[] getTraversals(
        ResourceMappingContext context, IProgressMonitor monitor) throws CoreException {
      return new ResourceTraversal[] {
        new ResourceTraversal(new IResource[] {fClassFile.getResource()}, IResource.DEPTH_ONE, 0)
      };
    }
  }

  private static final class LogicalPackageResourceMapping extends ResourceMapping {
    private final IPackageFragment[] fFragments;

    private LogicalPackageResourceMapping(IPackageFragment[] fragments) {
      fFragments = fragments;
    }

    @Override
    public Object getModelObject() {
      return fFragments;
    }

    @Override
    public IProject[] getProjects() {
      Set<IProject> result = new HashSet<IProject>();
      for (int i = 0; i < fFragments.length; i++) {
        result.add(fFragments[i].getJavaProject().getProject());
      }
      return result.toArray(new IProject[result.size()]);
    }

    @Override
    public ResourceTraversal[] getTraversals(
        ResourceMappingContext context, IProgressMonitor monitor) throws CoreException {
      List<ResourceTraversal> result = new ArrayList<ResourceTraversal>();
      if (context instanceof RemoteResourceMappingContext) {
        for (int i = 0; i < fFragments.length; i++) {
          result.addAll(
              Arrays.asList(
                  getRemotePackageFragmentTraversals(
                      fFragments[i], (RemoteResourceMappingContext) context, monitor)));
        }
      } else {
        for (int i = 0; i < fFragments.length; i++) {
          result.addAll(Arrays.asList(getPackageFragmentTraversals(fFragments[i])));
        }
      }
      return result.toArray(new ResourceTraversal[result.size()]);
    }

    @Override
    public String getModelProviderId() {
      return JavaModelProvider.JAVA_MODEL_PROVIDER_ID;
    }
  }

  public static ResourceMapping create(IJavaElement element) {
    switch (element.getElementType()) {
      case IJavaElement.TYPE:
        return create((IType) element);
      case IJavaElement.COMPILATION_UNIT:
        return create((ICompilationUnit) element);
      case IJavaElement.CLASS_FILE:
        return create((IClassFile) element);
      case IJavaElement.PACKAGE_FRAGMENT:
        return create((IPackageFragment) element);
      case IJavaElement.PACKAGE_FRAGMENT_ROOT:
        return create((IPackageFragmentRoot) element);
      case IJavaElement.JAVA_PROJECT:
        return create((IJavaProject) element);
      case IJavaElement.JAVA_MODEL:
        return create((IJavaModel) element);
      default:
        return null;
    }
  }

  public static ResourceMapping create(final IJavaModel model) {
    return new JavaModelResourceMapping(model);
  }

  public static ResourceMapping create(final IJavaProject project) {
    return new JavaProjectResourceMapping(project);
  }

  public static ResourceMapping create(final IPackageFragmentRoot root) {
    if (root.isExternal()) return null;
    return new PackageFragementRootResourceMapping(root);
  }

  public static ResourceMapping create(final IPackageFragment pack) {
    // test if in an archive
    IPackageFragmentRoot root =
        (IPackageFragmentRoot) pack.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
    if (!root.isArchive() && !root.isExternal()) {
      return new PackageFragmentResourceMapping(pack);
    }
    return null;
  }

  public static ResourceMapping create(ICompilationUnit unit) {
    if (unit == null) return null;
    return new CompilationUnitResourceMapping(unit.getPrimary());
  }

  public static ResourceMapping create(IClassFile classFile) {
    // test if in a archive
    IPackageFragmentRoot root =
        (IPackageFragmentRoot) classFile.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
    if (!root.isArchive() && !root.isExternal()) {
      return new ClassFileResourceMapping(classFile);
    }
    return null;
  }

  public static ResourceMapping create(IType type) {
    // top level types behave like the CU
    IJavaElement parent = type.getParent();
    if (parent instanceof ICompilationUnit) {
      return create((ICompilationUnit) parent);
    }
    return null;
  }

  //	public static ResourceMapping create(LogicalPackage logicalPackage) {
  //		IPackageFragment[] fragments = logicalPackage.getFragments();
  //		List<IPackageFragment> toProcess = new ArrayList<IPackageFragment>(fragments.length);
  //		for (int i = 0; i < fragments.length; i++) {
  //			// only add if not part of an archive
  //			IPackageFragmentRoot root =
  // (IPackageFragmentRoot)fragments[i].getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
  //			if (!root.isArchive() && !root.isExternal()) {
  //				toProcess.add(fragments[i]);
  //			}
  //		}
  //		if (toProcess.size() == 0)
  //			return null;
  //		return new LogicalPackageResourceMapping(toProcess.toArray(new
  // IPackageFragment[toProcess.size()]));
  //	}
}
