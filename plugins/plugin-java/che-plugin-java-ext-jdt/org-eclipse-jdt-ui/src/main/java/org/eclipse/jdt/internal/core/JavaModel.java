/**
 * ***************************************************************************** Copyright (c) 2004,
 * 2012 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.core;

import java.io.File;
import java.util.Map;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.internal.core.util.MementoTokenizer;
import org.eclipse.jdt.internal.core.util.Messages;

/**
 * Implementation of {@link org.eclipse.jdt.core.IJavaModel}. The Java Model maintains a cache of
 * active {@link org.eclipse.jdt.core.IJavaProject}s in a workspace. A Java Model is specific to a
 * workspace. To retrieve a workspace's model, use the {@link
 * org.eclipse.jdt.core.IJavaElement#getJavaModel() #getJavaModel()} method.
 *
 * @see org.eclipse.jdt.core.IJavaModel
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class JavaModel extends Openable implements IJavaModel {

  /**
   * Constructs a new Java Model on the given workspace. Note that only one instance of JavaModel
   * handle should ever be created. One should only indirect through JavaModelManager#getJavaModel()
   * to get access to it.
   *
   * @throws Error if called more than once
   */
  protected JavaModel() throws Error {
    super(null);
  }

  protected boolean buildStructure(
      OpenableElementInfo info,
      IProgressMonitor pm,
      Map newElements,
      IResource underlyingResource) /*throws JavaModelException*/ {

    // determine my children
    IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
    int length = projects.length;
    IJavaElement[] children = new IJavaElement[length];
    int index = 0;
    for (int i = 0; i < length; i++) {
      IProject project = projects[i];
      if (org.eclipse.jdt.internal.core.JavaProject.hasJavaNature(project)) {
        children[index++] = getJavaProject(project);
      }
    }
    if (index < length) System.arraycopy(children, 0, children = new IJavaElement[index], 0, index);
    info.setChildren(children);

    newElements.put(this, info);

    return true;
  }
  /*
   * @see IJavaModel
   */
  public boolean contains(IResource resource) {
    //	switch (resource.getType()) {
    //		case IResource.ROOT:
    //		case IResource.PROJECT:
    //			return true;
    //	}
    //	// file or folder
    //	IJavaProject[] projects;
    //	try {
    //		projects = getJavaProjects();
    //	} catch (JavaModelException e) {
    //		return false;
    //	}
    //	for (int i = 0, length = projects.length; i < length; i++) {
    //		JavaProject project = (JavaProject)projects[i];
    //		if (!project.contains(resource)) {
    //			return false;
    //		}
    //	}
    //	return true;
    throw new UnsupportedOperationException();
  }
  /** @see org.eclipse.jdt.core.IJavaModel */
  public void copy(
      IJavaElement[] elements,
      IJavaElement[] containers,
      IJavaElement[] siblings,
      String[] renamings,
      boolean force,
      IProgressMonitor monitor)
      throws JavaModelException {
    //	if (elements != null && elements.length > 0 && elements[0] != null &&
    // elements[0].getElementType() < IJavaElement.TYPE) {
    //		runOperation(new CopyResourceElementsOperation(elements, containers, force), elements,
    // siblings, renamings, monitor);
    //	} else {
    //		runOperation(new CopyElementsOperation(elements, containers, force), elements, siblings,
    // renamings, monitor);
    //	}
    throw new UnsupportedOperationException();
  }
  /** Returns a new element info for this element. */
  protected Object createElementInfo() {
    return new JavaModelInfo();
  }

  /** @see org.eclipse.jdt.core.IJavaModel */
  public void delete(IJavaElement[] elements, boolean force, IProgressMonitor monitor)
      throws JavaModelException {
    if (elements != null
        && elements.length > 0
        && elements[0] != null
        && elements[0].getElementType() < IJavaElement.TYPE) {
      new DeleteResourceElementsOperation(elements, force).runOperation(monitor);
    } else {
      new DeleteElementsOperation(elements, force).runOperation(monitor);
    }
    //	throw new UnsupportedOperationException();
  }

  public boolean equals(Object o) {
    if (!(o instanceof JavaModel)) return false;
    return super.equals(o);
  }
  /** @see org.eclipse.jdt.core.IJavaElement */
  public int getElementType() {
    return IJavaElement.JAVA_MODEL;
  }

  /*
   * @see JavaElement
   */
  public IJavaElement getHandleFromMemento(
      String token, MementoTokenizer memento, WorkingCopyOwner owner) {
    switch (token.charAt(0)) {
      case JavaElement.JEM_JAVAPROJECT:
        if (!memento.hasMoreTokens()) return this;
        String projectName = memento.nextToken();
        JavaElement project = (JavaElement) getJavaProject(projectName);
        return project.getHandleFromMemento(memento, owner);
    }
    return null;
  }
  /** @see org.eclipse.jdt.internal.core.JavaElement#getHandleMemento(StringBuffer) */
  protected void getHandleMemento(StringBuffer buff) {
    buff.append(getElementName());
  }
  /**
   * Returns the <code>char</code> that marks the start of this handles contribution to a memento.
   */
  protected char getHandleMementoDelimiter() {
    Assert.isTrue(false, "Should not be called"); // $NON-NLS-1$
    return 0;
  }
  /** @see org.eclipse.jdt.core.IJavaModel */
  public IJavaProject getJavaProject(String projectName) {
    return new org.eclipse.jdt.internal.core.JavaProject(
        ResourcesPlugin.getWorkspace().getRoot().getProject(projectName), this);
  }
  /**
   * Returns the active Java project associated with the specified resource, or <code>null</code> if
   * no Java project yet exists for the resource.
   *
   * @exception IllegalArgumentException if the given resource is not one of an IProject, IFolder,
   *     or IFile.
   */
  public IJavaProject getJavaProject(IResource resource) {
    switch (resource.getType()) {
      case IResource.FOLDER:
        return new org.eclipse.jdt.internal.core.JavaProject(
            ((IFolder) resource).getProject(), this);
      case IResource.FILE:
        return new org.eclipse.jdt.internal.core.JavaProject(((IFile) resource).getProject(), this);
      case IResource.PROJECT:
        return new JavaProject((IProject) resource, this);
      default:
        throw new IllegalArgumentException(Messages.element_invalidResourceForProject);
    }
  }
  /** @see org.eclipse.jdt.core.IJavaModel */
  public IJavaProject[] getJavaProjects() throws JavaModelException {
    //    ArrayList list = getChildrenOfType(IJavaElement.JAVA_PROJECT);
    // determine my children
    IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
    int length = projects.length;
    IJavaProject[] children = new IJavaProject[length];
    int index = 0;
    for (int i = 0; i < length; i++) {
      IProject project = projects[i];
      if (org.eclipse.jdt.internal.core.JavaProject.hasJavaNature(project)) {
        children[index++] = getJavaProject(project);
      }
    }
    if (index < length) System.arraycopy(children, 0, children = new IJavaProject[index], 0, index);
    //    IJavaProject[] array = new IJavaProject[list.size()];
    //    list.toArray(array);
    return children;
  }
  /** @see org.eclipse.jdt.core.IJavaModel */
  public Object[] getNonJavaResources() throws JavaModelException {
    //		return ((JavaModelInfo) getElementInfo()).getNonJavaResources();
    throw new UnsupportedOperationException();
  }

  /*
   * @see IJavaElement
   */
  public IPath getPath() {
    return Path.ROOT;
  }
  /*
   * @see IJavaElement
   */
  public IResource resource(PackageFragmentRoot root) {
    return ResourcesPlugin.getWorkspace().getRoot();
  }

  @Override
  public boolean isOpen() {
    return true;
  }

  @Override
  public void open(IProgressMonitor pm) throws JavaModelException {}

  /** @see org.eclipse.jdt.core.IOpenable */
  public IResource getUnderlyingResource() {
    return null;
  }
  /** Returns the workbench associated with this object. */
  public IWorkspace getWorkspace() {
    return ResourcesPlugin.getWorkspace();
  }

  /** @see org.eclipse.jdt.core.IJavaModel */
  public void move(
      IJavaElement[] elements,
      IJavaElement[] containers,
      IJavaElement[] siblings,
      String[] renamings,
      boolean force,
      IProgressMonitor monitor)
      throws JavaModelException {
    if (elements != null
        && elements.length > 0
        && elements[0] != null
        && elements[0].getElementType() < IJavaElement.TYPE) {
      runOperation(
          new MoveResourceElementsOperation(elements, containers, force),
          elements,
          siblings,
          renamings,
          monitor);
    } else {
      runOperation(
          new MoveElementsOperation(elements, containers, force),
          elements,
          siblings,
          renamings,
          monitor);
    }
  }

  /**
   * @see
   *     org.eclipse.jdt.core.IJavaModel#refreshExternalArchives(org.eclipse.jdt.core.IJavaElement[],
   *     IProgressMonitor)
   */
  public void refreshExternalArchives(IJavaElement[] elementsScope, IProgressMonitor monitor)
      throws JavaModelException {
    //	if (elementsScope == null){
    //		elementsScope = new IJavaElement[] { this };
    //	}
    //	JavaModelManager.getJavaModelManager().getDeltaProcessor().checkExternalArchiveChanges(elementsScope, monitor);
    throw new UnsupportedOperationException();
  }

  /** @see org.eclipse.jdt.core.IJavaModel */
  public void rename(
      IJavaElement[] elements,
      IJavaElement[] destinations,
      String[] renamings,
      boolean force,
      IProgressMonitor monitor)
      throws JavaModelException {
    MultiOperation op;
    if (elements != null
        && elements.length > 0
        && elements[0] != null
        && elements[0].getElementType() < IJavaElement.TYPE) {
      op = new RenameResourceElementsOperation(elements, destinations, renamings, force);
    } else {
      op = new RenameElementsOperation(elements, destinations, renamings, force);
    }

    op.runOperation(monitor);
    //	throw new UnsupportedOperationException();
  }
  /** Configures and runs the <code>MultiOperation</code>. */
  protected void runOperation(
      MultiOperation op,
      IJavaElement[] elements,
      IJavaElement[] siblings,
      String[] renamings,
      IProgressMonitor monitor)
      throws JavaModelException {
    op.setRenamings(renamings);
    if (siblings != null) {
      for (int i = 0; i < elements.length; i++) {
        op.setInsertBefore(elements[i], siblings[i]);
      }
    }
    op.runOperation(monitor);
  }
  /** @private Debugging purposes */
  protected void toStringInfo(int tab, StringBuffer buffer, Object info, boolean showResolvedInfo) {
    buffer.append(tabString(tab));
    buffer.append("Java Model"); // $NON-NLS-1$
    if (info == null) {
      buffer.append(" (not open)"); // $NON-NLS-1$
    }
  }

  /**
   * Helper method - for the provided {@link IPath}, returns:
   *
   * <ul>
   *   <li>If the path corresponds to an internal file or folder, the {@link IResource} for that
   *       resource
   *   <li>If the path corresponds to an external folder linked through {@link
   *       org.eclipse.jdt.internal.core.ExternalFoldersManager}, the {@link IFolder} for that
   *       folder
   *   <li>If the path corresponds to an external library archive, the {@link File} for that archive
   *   <li>Can return <code>null</code> if <code>checkResourceExistence</code> is <code>true</code>
   *       and the entity referred to by the path does not exist on the file system
   * </ul>
   *
   * Internal items must be referred to using container-relative paths.
   */
  public static Object getTarget(IPath path, boolean checkResourceExistence) {
    Object target = getWorkspaceTarget(path); // Implicitly checks resource existence
    if (target != null) return target;
    return getExternalTarget(path, checkResourceExistence);
  }

  /**
   * Helper method - returns the {@link IResource} corresponding to the provided {@link
   * org.eclipse.core.runtime .IPath}, or <code>null</code> if no such resource exists.
   */
  public static IResource getWorkspaceTarget(IPath path) {
    if (path == null || path.getDevice() != null) return null;
    IWorkspace workspace = ResourcesPlugin.getWorkspace();
    if (workspace == null) return null;
    return workspace.getRoot().findMember(path);
  }

  /**
   * Helper method - returns either the linked {@link IFolder} or the {@link File} corresponding to
   * the provided {@link IPath}. If <code>checkResourceExistence</code> is <code>false</code>, then
   * the IFolder or File object is always returned, otherwise <code>null</code> is returned if it
   * does not exist on the file system.
   */
  public static Object getExternalTarget(IPath path, boolean checkResourceExistence) {
    if (path == null) return null;
    //	ExternalFoldersManager externalFoldersManager = JavaModelManager.getExternalManager();
    //	Object linkedFolder = externalFoldersManager.getFolder(path);
    //	if (linkedFolder != null) {
    //		if (checkResourceExistence) {
    //			// check if external folder is present
    //			File externalFile = new File(path.toOSString());
    //			if (!externalFile.isDirectory()) {
    //				return null;
    //			}
    //		}
    //		return linkedFolder;
    //	}

    File externalFile = new File(path.toOSString());
    if (externalFile.exists()) {
      //        if(checkResourceExistence){
      //            if(!externalFile.isDirectory()){
      //                return null;
      //            }
      //        }
      return externalFile;
    }
    //	if (!checkResourceExistence) {
    //		return externalFile;
    //	} else if (isExternalFile(path)) {
    return null;
    //	}
    //	throw new UnsupportedOperationException();
  }

  /**
   * Helper method - returns whether an object is a file (i.e., it returns <code>true</code> to
   * {@link File#isFile()}.
   */
  public static boolean isFile(Object target) {
    if (target instanceof File) {
      IPath path = Path.fromOSString(((File) target).getPath());
      return isExternalFile(path);
    }
    return false;
  }

  /**
   * Returns whether the provided path is an external file, checking and updating the
   * JavaModelManager's external file cache.
   */
  private static boolean isExternalFile(IPath path) {
    //	if (JavaModelManager.getJavaModelManager().isExternalFile(path)) {
    //		return true;
    //	}
    //	if (JavaModelManager.ZIP_ACCESS_VERBOSE) {
    //		System.out.println("(" + Thread.currentThread() + ") [JavaModel.isExternalFile(...)]
    // Checking existence of " + path.toString()); //$NON-NLS-1$ //$NON-NLS-2$
    //	}
    //	boolean isFile = path.toFile().isFile();
    //	if (isFile) {
    //		JavaModelManager.getJavaModelManager().addExternalFile(path);
    //	}
    //	return isFile;
    throw new UnsupportedOperationException();
  }

  /**
   * Helper method - returns the {@link File} item if <code>target</code> is a file (i.e., the
   * target returns <code>true</code> to {@link File#isFile()}. Otherwise returns <code>null</code>.
   */
  public static File getFile(Object target) {
    return isFile(target) ? (File) target : null;
  }

  protected IStatus validateExistence(IResource underlyingResource) {
    // Java model always exists
    return JavaModelStatus.VERIFIED_OK;
  }
}
