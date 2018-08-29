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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.PerformanceStats;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.SourceElementParser;
import org.eclipse.jdt.internal.core.builder.JavaBuilder;
import org.eclipse.jdt.internal.core.hierarchy.TypeHierarchy;
import org.eclipse.jdt.internal.core.search.AbstractSearchScope;
import org.eclipse.jdt.internal.core.search.JavaWorkspaceScope;
import org.eclipse.jdt.internal.core.search.indexing.IndexManager;
import org.eclipse.jdt.internal.core.util.Util;

/**
 * This class is used by <code>JavaModelManager</code> to convert <code>IResourceDelta</code>s into
 * <code>IJavaElementDelta</code>s. It also does some processing on the <code>JavaElement</code>s
 * involved (e.g. closing them or updating classpaths).
 *
 * <p>High level summary of what the delta processor does:
 *
 * <ul>
 *   <li>reacts to resource deltas
 *   <li>fires corresponding Java element deltas
 *   <li>deltas also contain non-Java resources changes
 *   <li>updates the model to reflect the Java element changes
 *   <li>notifies type hierarchies of the changes
 *   <li>triggers indexing of the changed elements
 *   <li>refresh external archives (delta, model update, indexing)
 *   <li>is thread safe (one delta processor instance per thread, see
 *       DeltaProcessingState#resourceChanged(...))
 *   <li>handles .classpath changes (updates package fragment roots, update project references,
 *       validate classpath (.classpath format, resolved classpath, cycles))
 * </ul>
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class DeltaProcessor {

  public static final int DEFAULT_CHANGE_EVENT =
      0; // must not collide with ElementChangedEvent event masks
  private static final int IGNORE = 0;
  private static final int SOURCE = 1;
  private static final int BINARY = 2;
  private static final String EXTERNAL_JAR_ADDED = "external jar added"; // $NON-NLS-1$
  private static final String EXTERNAL_JAR_CHANGED = "external jar changed"; // $NON-NLS-1$
  private static final String EXTERNAL_JAR_REMOVED = "external jar removed"; // $NON-NLS-1$
  private static final String EXTERNAL_JAR_UNCHANGED = "external jar unchanged"; // $NON-NLS-1$
  private static final String INTERNAL_JAR_IGNORE = "internal jar ignore"; // $NON-NLS-1$
  private static final int NON_JAVA_RESOURCE = -1;
  public static boolean DEBUG = false;
  public static boolean VERBOSE = false;
  public static boolean PERF = false;
  /*
   * Used to update the JavaModel for <code>IJavaElementDelta</code>s.
   */
  private final ModelUpdater modelUpdater = new ModelUpdater();
  /*
   * Queue of deltas created explicily by the Java Model that
   * have yet to be fired.
   */
  public ArrayList javaModelDeltas = new ArrayList();
  /*
   * Queue of reconcile deltas on working copies that have yet to be fired.
   * This is a table form IWorkingCopy to IJavaElementDelta
   */
  public HashMap reconcileDeltas = new HashMap();
  /* A set of IJavaProject whose caches need to be reset */
  public HashSet projectCachesToReset = new HashSet();
  /* A table from IJavaProject to an array of IPackageFragmentRoot.
   * This table contains the pkg fragment roots of the project that are being deleted.
   */
  public Map oldRoots;
  /*
   * Type of event that should be processed no matter what the real event type is.
   */
  public int overridenEventType = -1;
  /*
   * The Java model manager
   */
  JavaModelManager manager;
  /*
   * The global state of delta processing.
   */
  private DeltaProcessingState state;
  /*
   * The <code>JavaElementDelta</code> corresponding to the <code>IResourceDelta</code> being translated.
   */
  private JavaElementDelta currentDelta;
  /* The java element that was last created (see createElement(IResource)).
   * This is used as a stack of java elements (using getParent() to pop it, and
   * using the various get*(...) to push it. */
  private Openable currentElement;
  /*
   * Turns delta firing on/off. By default it is on.
   */
  private boolean isFiring = true;
  /*
   * Cache SourceElementParser for the project being visited
   */
  private SourceElementParser sourceElementParserCache;

  public DeltaProcessor(DeltaProcessingState state, JavaModelManager manager) {
    this.state = state;
    this.manager = manager;
  }

  /*
   * Answer a combination of the lastModified stamp and the size.
   * Used for detecting external JAR changes
   */
  public static long getTimeStamp(File file) {
    return file.lastModified() + file.length();
  }

  /*
   * Adds the dependents of the given project to the list of the projects
   * to update.
   */
  private void addDependentProjects(
      IJavaProject project, HashMap projectDependencies, HashSet result) {
    IJavaProject[] dependents = (IJavaProject[]) projectDependencies.get(project);
    if (dependents == null) return;
    for (int i = 0, length = dependents.length; i < length; i++) {
      IJavaProject dependent = dependents[i];
      if (result.contains(dependent))
        continue; // no need to go further as the project is already known
      result.add(dependent);
      addDependentProjects(dependent, projectDependencies, result);
    }
  }

  /*
   * Adds the given child handle to its parent's cache of children.
   */
  private void addToParentInfo(Openable child) {
    Openable parent = (Openable) child.getParent();
    if (parent != null && parent.isOpen()) {
      try {
        OpenableElementInfo info = (OpenableElementInfo) parent.getElementInfo();
        // https://bugs.eclipse.org/bugs/show_bug.cgi?id=338006
        // Insert the package fragment roots in the same order as the classpath order.
        if (child instanceof IPackageFragmentRoot)
          addPackageFragmentRoot(info, (IPackageFragmentRoot) child);
        else info.addChild(child);
      } catch (JavaModelException e) {
        // do nothing - we already checked if open
      }
    }
  }

  private void addPackageFragmentRoot(OpenableElementInfo parent, IPackageFragmentRoot child)
      throws JavaModelException {

    IJavaElement[] roots = parent.getChildren();
    if (roots.length > 0) {
      IClasspathEntry[] resolvedClasspath =
          ((JavaProject) child.getJavaProject()).getResolvedClasspath();
      IPath currentEntryPath = child.getResolvedClasspathEntry().getPath();
      int indexToInsert = -1;
      int lastComparedIndex = -1;
      int i = 0, j = 0;
      for (; i < roots.length && j < resolvedClasspath.length; ) {

        IClasspathEntry classpathEntry = resolvedClasspath[j];
        if (lastComparedIndex != j && currentEntryPath.equals(classpathEntry.getPath())) {
          indexToInsert = i;
          break;
        }
        lastComparedIndex = j;

        IClasspathEntry rootEntry = ((IPackageFragmentRoot) roots[i]).getResolvedClasspathEntry();
        if (rootEntry.getPath().equals(classpathEntry.getPath())) i++;
        else j++;
      }

      for (; i < roots.length; i++) {
        // If the new root is already among the children, no need to proceed further. Just return.
        if (roots[i].equals(child)) {
          return;
        }
        // If we start seeing root's classpath entry different from the child's entry, then the
        // child can't
        // be present further down the roots array.
        if (!((IPackageFragmentRoot) roots[i])
            .getResolvedClasspathEntry()
            .getPath()
            .equals(currentEntryPath)) break;
      }

      if (indexToInsert >= 0) {
        int newSize = roots.length + 1;
        IPackageFragmentRoot[] newChildren = new IPackageFragmentRoot[newSize];

        if (indexToInsert > 0) System.arraycopy(roots, 0, newChildren, 0, indexToInsert);

        newChildren[indexToInsert] = child;
        System.arraycopy(
            roots, indexToInsert, newChildren, indexToInsert + 1, (newSize - indexToInsert - 1));
        parent.setChildren(newChildren);
        return;
      }
    }
    parent.addChild(child);
  }

  /*
   * Process the given delta and look for projects being added, opened, closed or
   * with a java nature being added or removed.
   * Note that projects being deleted are checked in deleting(IProject).
   * In all cases, add the project's dependents to the list of projects to update
   * so that the classpath related markers can be updated.
   */
  private void checkProjectsAndClasspathChanges(IResourceDelta delta) {
    //		IResource resource = delta.getResource();
    //		IResourceDelta[] children = null;
    //
    //		switch (resource.getType()) {
    //			case IResource.ROOT:
    //				// workaround for bug 15168 circular errors not reported
    //				this.state.getOldJavaProjecNames(); // force list to be computed
    //				children = delta.getAffectedChildren();
    //				break;
    //			case IResource.PROJECT:
    //				// NB: No need to check project's nature as if the project is not a java project:
    //				//     - if the project is added or changed this is a noop for projectsBeingDeleted
    //				//     - if the project is closed, it has already lost its java nature
    //				IProject project = (IProject)resource;
    //				JavaProject javaProject = (JavaProject)JavaCore.create(project);
    //				switch (delta.getKind()) {
    //					case IResourceDelta.ADDED:
    //						this.manager.forceBatchInitializations(false/*not initAfterLoad*/);
    //
    //						// remember that the project's cache must be reset
    //						this.projectCachesToReset.add(javaProject);
    //
    //						// workaround for bug 15168 circular errors not reported
    //						if (JavaProject.hasJavaNature(project)) {
    //							addToParentInfo(javaProject);
    //							readRawClasspath(javaProject);
    //							// ensure project references are updated (see
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=121569)
    //							checkProjectReferenceChange(project, javaProject);
    //							// and external folders as well
    //							checkExternalFolderChange(project, javaProject);
    //						}
    //
    //						this.state.rootsAreStale = true;
    //						break;
    //
    //					case IResourceDelta.CHANGED:
    //						if ((delta.getFlags() & IResourceDelta.OPEN) != 0) {
    //							this.manager.forceBatchInitializations(false/*not initAfterLoad*/);
    //
    //							// remember that the project's cache must be reset
    //							this.projectCachesToReset.add(javaProject);
    //
    //							// workaround for bug 15168 circular errors not reported
    //							if (project.isOpen()) {
    //								if (JavaProject.hasJavaNature(project)) {
    //									addToParentInfo(javaProject);
    //									readRawClasspath(javaProject);
    //									// ensure project references are updated
    //									checkProjectReferenceChange(project, javaProject);
    //									// and external folders as well
    //									checkExternalFolderChange(project, javaProject);
    //								}
    //							} else {
    //								try {
    //									javaProject.close();
    //								} catch (JavaModelException e) {
    //									// java project doesn't exist: ignore
    //								}
    //								removeFromParentInfo(javaProject);
    //								this.manager.removePerProjectInfo(javaProject, false /* don't remove index files and
    // timestamp info of
    //								external jar */);
    //								this.manager.containerRemove(javaProject);
    //							}
    //							this.state.rootsAreStale = true;
    //						} else if ((delta.getFlags() & IResourceDelta.DESCRIPTION) != 0) {
    //							boolean wasJavaProject = this.state.findJavaProject(project.getName()) != null;
    //							boolean isJavaProject = JavaProject.hasJavaNature(project);
    //							if (wasJavaProject != isJavaProject) {
    //								this.manager.forceBatchInitializations(false/*not initAfterLoad*/);
    //
    //								// java nature added or removed: remember that the project's cache must be reset
    //								this.projectCachesToReset.add(javaProject);
    //
    //								// workaround for bug 15168 circular errors not reported
    //								if (isJavaProject) {
    //									addToParentInfo(javaProject);
    //									readRawClasspath(javaProject);
    //									// ensure project references are updated (see
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=172666)
    //									checkProjectReferenceChange(project, javaProject);
    //									// and external folders as well
    //									checkExternalFolderChange(project, javaProject);
    //								} else {
    //									// remove classpath cache so that initializeRoots() will not consider the project has
    // a classpath
    //									this.manager
    //											.removePerProjectInfo(javaProject, true /* remove external jar files indexes and
    // timestamps
    //											*/);
    //									// remove container cache for this project
    //									this.manager.containerRemove(javaProject);
    //									// close project
    //									try {
    //										javaProject.close();
    //									} catch (JavaModelException e) {
    //										// java project doesn't exist: ignore
    //									}
    //									removeFromParentInfo(javaProject);
    //								}
    //								this.state.rootsAreStale = true;
    //							} else {
    //								// in case the project was removed then added then changed (see bug 19799)
    //								if (isJavaProject) { // need nature check - 18698
    //									addToParentInfo(javaProject);
    //									children = delta.getAffectedChildren();
    //								}
    //							}
    //						} else {
    //							// workaround for bug 15168 circular errors not reported
    //							// in case the project was removed then added then changed
    //							if (JavaProject.hasJavaNature(project)) { // need nature check - 18698
    //								addToParentInfo(javaProject);
    //								children = delta.getAffectedChildren();
    //							}
    //						}
    //						break;
    //
    //					case IResourceDelta.REMOVED:
    //						this.manager.forceBatchInitializations(false/*not initAfterLoad*/);
    //
    //						// remove classpath cache so that initializeRoots() will not consider the project has a
    // classpath
    //						this.manager.removePerProjectInfo(javaProject, true /* remove external jar files indexes
    // and timestamps*/);
    //						// remove container cache for this project
    //						this.manager.containerRemove(javaProject);
    //
    //						this.state.rootsAreStale = true;
    //						break;
    //				}
    //
    //				break;
    //			case IResource.FOLDER:
    //				if (delta.getKind() == IResourceDelta.CHANGED) { // look for .jar file change to update
    // classpath
    //					children = delta.getAffectedChildren();
    //				}
    //				break;
    //			case IResource.FILE :
    //				IFile file = (IFile) resource;
    //				int kind = delta.getKind();
    //				RootInfo rootInfo;
    //				if (file.getName().equals(JavaProject.CLASSPATH_FILENAME)) {
    //					/* classpath file change */
    //					this.manager.forceBatchInitializations(false/*not initAfterLoad*/);
    //					switch (kind) {
    //						case IResourceDelta.CHANGED :
    //							int flags = delta.getFlags();
    //							if ((flags & IResourceDelta.CONTENT) == 0  // only consider content change
    //								&& (flags & IResourceDelta.ENCODING) == 0 // and encoding change
    //								&& (flags & IResourceDelta.MOVED_FROM) == 0) {// and also move and overide scenario
    // (see http://dev
    // .eclipse.org/bugs/show_bug.cgi?id=21420)
    //								break;
    //							}
    //						//$FALL-THROUGH$
    //						case IResourceDelta.ADDED :
    //						case IResourceDelta.REMOVED :
    //							javaProject = (JavaProject)JavaCore.create(file.getProject());
    //
    //							// force to (re)read the .classpath file
    //							// in case of removal (IResourceDelta.REMOVED) this will reset the classpath to its
    // default and create the
    // right delta
    //							// (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=211290)
    //							readRawClasspath(javaProject);
    //							break;
    //					}
    //					this.state.rootsAreStale = true;
    //				} else if ((rootInfo = rootInfo(file.getFullPath(), kind)) != null && rootInfo.entryKind
    // == IClasspathEntry.CPE_LIBRARY) {
    //					javaProject = (JavaProject)JavaCore.create(file.getProject());
    //					javaProject.resetResolvedClasspath();
    //					this.state.rootsAreStale = true;
    //				}
    //				break;
    //
    //		}
    //		if (children != null) {
    //			for (int i = 0; i < children.length; i++) {
    //				checkProjectsAndClasspathChanges(children[i]);
    //			}
    //		}
  }

  private void checkExternalFolderChange(IProject project, JavaProject javaProject) {
    //		ClasspathChange change = this.state.getClasspathChange(project);
    //		this.state.addExternalFolderChange(javaProject, change == null ? null :
    // change.oldResolvedClasspath);
  }

  private void checkProjectReferenceChange(IProject project, JavaProject javaProject) {
    //		ClasspathChange change = this.state.getClasspathChange(project);
    //		this.state.addProjectReferenceChange(javaProject, change == null ? null :
    // change.oldResolvedClasspath);
  }

  private void readRawClasspath(JavaProject javaProject) {
    //		// force to (re)read the .classpath file
    //		try {
    //			PerProjectInfo perProjectInfo = javaProject.getPerProjectInfo();
    //			if (!perProjectInfo.writtingRawClasspath) // to avoid deadlock, see
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=221680
    //				perProjectInfo.readAndCacheClasspath(javaProject);
    //		} catch (JavaModelException e) {
    //			if (VERBOSE) {
    //				e.printStackTrace();
    //			}
    //		}
  }

  private void checkSourceAttachmentChange(IResourceDelta delta, IResource res) {
    IPath rootPath = (IPath) this.state.sourceAttachments.get(externalPath(res));
    if (rootPath != null) {
      RootInfo rootInfo = rootInfo(rootPath, delta.getKind());
      if (rootInfo != null) {
        IJavaProject projectOfRoot = rootInfo.project;
        IPackageFragmentRoot root = null;
        try {
          // close the root so that source attachment cache is flushed
          root = projectOfRoot.findPackageFragmentRoot(rootPath);
          if (root != null) {
            root.close();
          }
        } catch (JavaModelException e) {
          // root doesn't exist: ignore
        }
        if (root == null) return;
        switch (delta.getKind()) {
          case IResourceDelta.ADDED:
            currentDelta().sourceAttached(root);
            break;
          case IResourceDelta.CHANGED:
            currentDelta().sourceDetached(root);
            currentDelta().sourceAttached(root);
            break;
          case IResourceDelta.REMOVED:
            currentDelta().sourceDetached(root);
            break;
        }
      }
    }
  }

  /*
   * Closes the given element, which removes it from the cache of open elements.
   */
  private void close(Openable element) {
    try {
      element.close();
    } catch (JavaModelException e) {
      // do nothing
    }
  }

  /*
   * Generic processing for elements with changed contents:<ul>
   * <li>The element is closed such that any subsequent accesses will re-open
   * the element reflecting its new structure.
   * <li>An entry is made in the delta reporting a content change (K_CHANGE with F_CONTENT flag set).
   * </ul>
   * Delta argument could be null if processing an external JAR change
   */
  private void contentChanged(Openable element) {

    boolean isPrimary = false;
    boolean isPrimaryWorkingCopy = false;
    if (element.getElementType() == IJavaElement.COMPILATION_UNIT) {
      CompilationUnit cu = (CompilationUnit) element;
      isPrimary = cu.isPrimary();
      isPrimaryWorkingCopy = isPrimary && cu.isWorkingCopy();
    }
    if (isPrimaryWorkingCopy) {
      // filter out changes to primary compilation unit in working copy mode
      // just report a change to the resource (see
      // https://bugs.eclipse.org/bugs/show_bug.cgi?id=59500)
      currentDelta().changed(element, IJavaElementDelta.F_PRIMARY_RESOURCE);
    } else {
      close(element);
      int flags = IJavaElementDelta.F_CONTENT;
      if (element instanceof JarPackageFragmentRoot) {
        flags |= IJavaElementDelta.F_ARCHIVE_CONTENT_CHANGED;
        // need also to reset project cache otherwise it will be out-of-date
        // see bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=162621
        this.projectCachesToReset.add(element.getJavaProject());
      }
      if (isPrimary) {
        flags |= IJavaElementDelta.F_PRIMARY_RESOURCE;
      }
      currentDelta().changed(element, flags);
    }
  }

  /*
   * Creates the openables corresponding to this resource.
   * Returns null if none was found.
   */
  private Openable createElement(IResource resource, int elementType, RootInfo rootInfo) {
    if (resource == null) return null;

    IPath path = resource.getFullPath();
    IJavaElement element = null;
    switch (elementType) {
      case IJavaElement.JAVA_PROJECT:

        // note that non-java resources rooted at the project level will also enter this code with
        // an elementType JAVA_PROJECT (see #elementType(...)).
        if (resource instanceof IProject) {

          popUntilPrefixOf(path);

          if (this.currentElement != null
              && this.currentElement.getElementType() == IJavaElement.JAVA_PROJECT
              && ((IJavaProject) this.currentElement).getProject().equals(resource)) {
            return this.currentElement;
          }
          if (rootInfo != null && rootInfo.project.getProject().equals(resource)) {
            element = rootInfo.project;
            break;
          }
          IProject proj = (IProject) resource;
          if (JavaProject.hasJavaNature(proj)) {
            element = JavaCore.create(proj);
          } else {
            // java project may have been been closed or removed (look for
            // element amongst old java project s list).
            element = this.state.findJavaProject(proj.getName());
          }
        }
        break;
      case IJavaElement.PACKAGE_FRAGMENT_ROOT:
        element =
            rootInfo == null
                ? JavaCore.create(resource)
                : rootInfo.getPackageFragmentRoot(resource);
        break;
      case IJavaElement.PACKAGE_FRAGMENT:
        if (rootInfo != null) {
          if (rootInfo.project.contains(resource)) {
            PackageFragmentRoot root = (PackageFragmentRoot) rootInfo.getPackageFragmentRoot(null);
            // create package handle
            IPath pkgPath = path.removeFirstSegments(root.resource().getFullPath().segmentCount());
            String[] pkgName = pkgPath.segments();
            element = root.getPackageFragment(pkgName);
          }
        } else {
          // find the element that encloses the resource
          popUntilPrefixOf(path);

          if (this.currentElement == null) {
            element = JavaCore.create(resource);
          } else {
            // find the root
            PackageFragmentRoot root = this.currentElement.getPackageFragmentRoot();
            if (root == null) {
              element = JavaCore.create(resource);
            } else if (((JavaProject) root.getJavaProject()).contains(resource)) {
              // create package handle
              IPath pkgPath = path.removeFirstSegments(root.getPath().segmentCount());
              String[] pkgName = pkgPath.segments();
              element = root.getPackageFragment(pkgName);
            }
          }
        }
        break;
      case IJavaElement.COMPILATION_UNIT:
      case IJavaElement.CLASS_FILE:
        // find the element that encloses the resource
        popUntilPrefixOf(path);

        if (this.currentElement == null) {
          element =
              rootInfo == null
                  ? JavaCore.create(resource)
                  : JavaModelManager.create(resource, rootInfo.project);
        } else {
          // find the package
          IPackageFragment pkgFragment = null;
          switch (this.currentElement.getElementType()) {
            case IJavaElement.PACKAGE_FRAGMENT_ROOT:
              PackageFragmentRoot root = (PackageFragmentRoot) this.currentElement;
              IPath rootPath = root.getPath();
              IPath pkgPath = path.removeLastSegments(1);
              String[] pkgName = pkgPath.removeFirstSegments(rootPath.segmentCount()).segments();
              pkgFragment = root.getPackageFragment(pkgName);
              break;
            case IJavaElement.PACKAGE_FRAGMENT:
              Openable pkg = this.currentElement;
              if (pkg.getPath().equals(path.removeLastSegments(1))) {
                pkgFragment = (IPackageFragment) pkg;
              } // else case of package x which is a prefix of x.y
              break;
            case IJavaElement.COMPILATION_UNIT:
            case IJavaElement.CLASS_FILE:
              pkgFragment = (IPackageFragment) this.currentElement.getParent();
              break;
          }
          if (pkgFragment == null) {
            element =
                rootInfo == null
                    ? JavaCore.create(resource)
                    : JavaModelManager.create(resource, rootInfo.project);
          } else {
            if (elementType == IJavaElement.COMPILATION_UNIT) {
              // create compilation unit handle
              // fileName validation has been done in elementType(IResourceDelta, int, boolean)
              String fileName = path.lastSegment();
              element = pkgFragment.getCompilationUnit(fileName);
            } else {
              // create class file handle
              // fileName validation has been done in elementType(IResourceDelta, int, boolean)
              String fileName = path.lastSegment();
              element = pkgFragment.getClassFile(fileName);
            }
          }
        }
        break;
    }
    if (element == null) return null;
    this.currentElement = (Openable) element;
    return this.currentElement;
  }

  public void checkExternalArchiveChanges(IJavaElement[] elementsScope, IProgressMonitor monitor)
      throws JavaModelException {
    checkExternalArchiveChanges(elementsScope, false, monitor);
  }

  /*
   * Check all external archive (referenced by given roots, projects or model) status and issue a corresponding root delta.
   * Also triggers index updates
   */
  private void checkExternalArchiveChanges(
      IJavaElement[] elementsScope, boolean asynchronous, IProgressMonitor monitor)
      throws JavaModelException {
    //		if (monitor != null && monitor.isCanceled())
    //			throw new OperationCanceledException();
    //		try {
    //			if (monitor != null) monitor.beginTask("", 1); //$NON-NLS-1$
    //
    //			boolean hasExternalWorkingCopyProject = false;
    //			for (int i = 0, length = elementsScope.length; i < length; i++) {
    //				IJavaElement element = elementsScope[i];
    //				this.state.addForRefresh(elementsScope[i]);
    //				if (element.getElementType() == IJavaElement.JAVA_MODEL) {
    //					// ensure external working copies' projects' caches are reset
    //					HashSet projects =
    // JavaModelManager.getJavaModelManager().getExternalWorkingCopyProjects();
    //					if (projects != null) {
    //						hasExternalWorkingCopyProject = true;
    //						Iterator iterator = projects.iterator();
    //						while (iterator.hasNext()) {
    //							JavaProject project = (JavaProject) iterator.next();
    //							project.resetCaches();
    //						}
    //					}
    //				}
    //			}
    //			HashSet elementsToRefresh = this.state.removeExternalElementsToRefresh();
    //			boolean hasDelta = elementsToRefresh != null &&
    // createExternalArchiveDelta(elementsToRefresh, monitor);
    //			if (hasDelta){
    //				IJavaElementDelta[] projectDeltas = this.currentDelta.getAffectedChildren();
    //				final int length = projectDeltas.length;
    //				final IProject[] projectsToTouch = new IProject[length];
    //				for (int i = 0; i < length; i++) {
    //					IJavaElementDelta delta = projectDeltas[i];
    //					JavaProject javaProject = (JavaProject)delta.getElement();
    //					projectsToTouch[i] = javaProject.getProject();
    //				}
    //				if (projectsToTouch.length > 0) {
    //					if (asynchronous){
    //						WorkspaceJob touchJob = new WorkspaceJob(Messages.updating_external_archives_jobName) {
    //
    //							public IStatus runInWorkspace(IProgressMonitor progressMonitor) throws CoreException {
    //								try {
    //									if (progressMonitor != null)
    //										progressMonitor.beginTask("", projectsToTouch.length); //$NON-NLS-1$
    //									touchProjects(projectsToTouch, progressMonitor);
    //								}
    //								finally {
    //									if (progressMonitor != null)
    //										progressMonitor.done();
    //								}
    //								return Status.OK_STATUS;
    //							}
    //
    //							public boolean belongsTo(Object family) {
    //								return ResourcesPlugin.FAMILY_MANUAL_REFRESH == family;
    //							}
    //						};
    //						touchJob.schedule();
    //					}
    //					else {
    //						// touch the projects to force them to be recompiled while taking the workspace lock
    //						//	 so that there is no concurrency with the Java builder
    //						// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=96575
    //						IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
    //							public void run(IProgressMonitor progressMonitor) throws CoreException {
    //								for (int i = 0; i < projectsToTouch.length; i++) {
    //									IProject project = projectsToTouch[i];
    //
    //									// touch to force a build of this project
    //									if (JavaBuilder.DEBUG)
    //										System.out.println("Touching project " + project.getName() + " due to external jar
    // file change");
    // $NON-NLS-1$ //$NON-NLS-2$
    //									project.touch(progressMonitor);
    //								}
    //							}
    //						};
    //						try {
    //							ResourcesPlugin.getWorkspace().run(runnable, monitor);
    //						} catch (CoreException e) {
    //							throw new JavaModelException(e);
    //						}
    //					}
    //				}
    //
    //				if (this.currentDelta != null) { // if delta has not been fired while creating markers
    //					fire(this.currentDelta, DEFAULT_CHANGE_EVENT);
    //				}
    //			} else if (hasExternalWorkingCopyProject) {
    //				// flush jar type cache
    //				JavaModelManager.getJavaModelManager().resetJarTypeCache();
    //			}
    //		} finally {
    //			this.currentDelta = null;
    //			if (monitor != null) monitor.done();
    //		}
    throw new UnsupportedOperationException();
  }

  protected void touchProjects(final IProject[] projectsToTouch, IProgressMonitor progressMonitor)
      throws CoreException {
    for (int i = 0; i < projectsToTouch.length; i++) {
      IProgressMonitor monitor =
          progressMonitor == null ? null : new SubProgressMonitor(progressMonitor, 1);
      IProject project = projectsToTouch[i];
      // touch to force a build of this project
      if (JavaBuilder.DEBUG)
        System.out.println(
            "Touching project "
                + project.getName()
                + " due to external jar file change"); // $NON-NLS-1$ //$NON-NLS-2$
      project.touch(monitor);
    }
  }

  /*
   * Check if external archives have changed for the given elements and create the corresponding deltas.
   * Returns whether at least one delta was created.
   */
  private boolean createExternalArchiveDelta(HashSet refreshedElements, IProgressMonitor monitor) {

    HashMap externalArchivesStatus = new HashMap();
    boolean hasDelta = false;

    // find JARs to refresh
    HashSet archivePathsToRefresh = new HashSet();
    Iterator iterator = refreshedElements.iterator();
    while (iterator.hasNext()) {
      IJavaElement element = (IJavaElement) iterator.next();
      switch (element.getElementType()) {
        case IJavaElement.PACKAGE_FRAGMENT_ROOT:
          archivePathsToRefresh.add(element.getPath());
          break;
        case IJavaElement.JAVA_PROJECT:
          JavaProject javaProject = (JavaProject) element;
          if (!JavaProject.hasJavaNature(javaProject.getProject())) {
            // project is not accessible or has lost its Java nature
            break;
          }
          IClasspathEntry[] classpath;
          try {
            classpath = javaProject.getResolvedClasspath();
            for (int j = 0, cpLength = classpath.length; j < cpLength; j++) {
              if (classpath[j].getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
                archivePathsToRefresh.add(classpath[j].getPath());
              }
            }
          } catch (JavaModelException e) {
            // project doesn't exist -> ignore
          }
          break;
        case IJavaElement.JAVA_MODEL:
          //					Iterator projectNames = this.state.getOldJavaProjecNames().iterator();
          //					while (projectNames.hasNext()) {
          //						String projectName = (String) projectNames.next();
          //						IProject project =
          // ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
          //						if (!JavaProject.hasJavaNature(project)) {
          //							// project is not accessible or has lost its Java nature
          //							continue;
          //						}
          //						javaProject = (JavaProject) JavaCore.create(project);
          //						try {
          //							classpath = javaProject.getResolvedClasspath();
          //							for (int k = 0, cpLength = classpath.length; k < cpLength; k++){
          //								if (classpath[k].getEntryKind() == IClasspathEntry.CPE_LIBRARY){
          //									archivePathsToRefresh.add(classpath[k].getPath());
          //								}
          //							}
          //						} catch (JavaModelException e2) {
          //							// project doesn't exist -> ignore
          //							continue;
          //						}
          //					}
          throw new UnsupportedOperationException();
          //					break;
      }
    }

    //		// perform refresh
    //		Iterator projectNames = this.state.getOldJavaProjecNames().iterator();
    //		IWorkspaceRoot wksRoot = ResourcesPlugin.getWorkspace().getRoot();
    //		while (projectNames.hasNext()) {
    //
    //			if (monitor != null && monitor.isCanceled()) break;
    //
    //			String projectName = (String) projectNames.next();
    //			IProject project = wksRoot.getProject(projectName);
    //			if (!JavaProject.hasJavaNature(project)) {
    //				// project is not accessible or has lost its Java nature
    //				continue;
    //			}
    //			JavaProject javaProject = (JavaProject) JavaCore.create(project);
    //			IClasspathEntry[] entries;
    //			try {
    //				entries = javaProject.getResolvedClasspath();
    //			} catch (JavaModelException e1) {
    //				// project does not exist -> ignore
    //				continue;
    //			}
    //			boolean deltaContainsModifiedJar = false;
    //			for (int j = 0; j < entries.length; j++){
    //				if (entries[j].getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
    //					IPath entryPath = entries[j].getPath();
    //
    //					if (!archivePathsToRefresh.contains(entryPath)) continue; // not supposed to be refreshed
    //
    //					String status = (String)externalArchivesStatus.get(entryPath);
    //					if (status == null){
    //
    //						// Clear the external file state for this path, since this method is responsible for
    // updating it.
    //						this.manager.clearExternalFileState(entryPath);
    //
    //						// compute shared status
    //						Object targetLibrary = JavaModel.getTarget(entryPath, true);
    //
    //						if (targetLibrary == null){ // missing JAR
    //							if (this.state.getExternalLibTimeStamps().remove(entryPath) != null /* file was known*/
    //									&& this.state.roots.get(entryPath) != null /* and it was on the classpath*/) {
    //								externalArchivesStatus.put(entryPath, EXTERNAL_JAR_REMOVED);
    //								// the jar was physically removed: remove the index
    //								this.manager.indexManager.removeIndex(entryPath);
    //							}
    //
    //						} else if (targetLibrary instanceof File){ // external JAR
    //
    //							File externalFile = (File)targetLibrary;
    //
    //							// check timestamp to figure if JAR has changed in some way
    //							Long oldTimestamp =(Long) this.state.getExternalLibTimeStamps().get(entryPath);
    //							long newTimeStamp = getTimeStamp(externalFile);
    //							if (oldTimestamp != null){
    //
    //								if (newTimeStamp == 0){ // file doesn't exist
    //									externalArchivesStatus.put(entryPath, EXTERNAL_JAR_REMOVED);
    //									this.state.getExternalLibTimeStamps().remove(entryPath);
    //									// remove the index
    //									this.manager.indexManager.removeIndex(entryPath);
    //
    //								} else if (oldTimestamp.longValue() != newTimeStamp){
    //									externalArchivesStatus.put(entryPath, EXTERNAL_JAR_CHANGED);
    //									this.state.getExternalLibTimeStamps().put(entryPath, new Long(newTimeStamp));
    //									// first remove the index so that it is forced to be re-indexed
    //									this.manager.indexManager.removeIndex(entryPath);
    //									// then index the jar
    //									this.manager.indexManager.indexLibrary(entryPath, project.getProject(),
    // ((ClasspathEntry)entries[j])
    // .getLibraryIndexLocation(), true);
    //								} else {
    //									URL indexLocation = ((ClasspathEntry)entries[j]).getLibraryIndexLocation();
    //									if (indexLocation != null) { // force reindexing, this could be faster rather than
    // maintaining the list
    //										this.manager.indexManager.indexLibrary(entryPath, project.getProject(),
    // indexLocation);
    //									}
    //									externalArchivesStatus.put(entryPath, EXTERNAL_JAR_UNCHANGED);
    //								}
    //							} else {
    //								if (newTimeStamp == 0){ // jar still doesn't exist
    //									externalArchivesStatus.put(entryPath, EXTERNAL_JAR_UNCHANGED);
    //								} else {
    //									externalArchivesStatus.put(entryPath, EXTERNAL_JAR_ADDED);
    //									this.state.getExternalLibTimeStamps().put(entryPath, new Long(newTimeStamp));
    //									// index the new jar
    //									this.manager.indexManager.removeIndex(entryPath);
    //									this.manager.indexManager.indexLibrary(entryPath, project.getProject(),
    // ((ClasspathEntry)entries[j])
    // .getLibraryIndexLocation());
    //								}
    //							}
    //						} else { // internal JAR
    //							externalArchivesStatus.put(entryPath, INTERNAL_JAR_IGNORE);
    //						}
    //					}
    //					// according to computed status, generate a delta
    //					status = (String)externalArchivesStatus.get(entryPath);
    //					if (status != null){
    //						if (status == EXTERNAL_JAR_ADDED){
    //							PackageFragmentRoot root = (PackageFragmentRoot)
    // javaProject.getPackageFragmentRoot(entryPath.toString());
    //							if (VERBOSE){
    //								System.out.println("- External JAR ADDED, affecting root: "+root.getElementName());
    // //$NON-NLS-1$
    //							}
    //							elementAdded(root, null, null);
    //							deltaContainsModifiedJar = true;
    //							this.state.addClasspathValidation(javaProject); // see
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=185733
    //							hasDelta = true;
    //						} else if (status == EXTERNAL_JAR_CHANGED) {
    //							PackageFragmentRoot root = (PackageFragmentRoot)
    // javaProject.getPackageFragmentRoot(entryPath.toString());
    //							if (VERBOSE){
    //								System.out.println("- External JAR CHANGED, affecting root: "+root.getElementName());
    // //$NON-NLS-1$
    //							}
    //							contentChanged(root);
    //							deltaContainsModifiedJar = true;
    //							hasDelta = true;
    //						} else if (status == EXTERNAL_JAR_REMOVED) {
    //							PackageFragmentRoot root = (PackageFragmentRoot)
    // javaProject.getPackageFragmentRoot(entryPath.toString());
    //							if (VERBOSE){
    //								System.out.println("- External JAR REMOVED, affecting root: "+root.getElementName());
    // //$NON-NLS-1$
    //							}
    //							elementRemoved(root, null, null);
    //							deltaContainsModifiedJar = true;
    //							this.state.addClasspathValidation(javaProject); // see
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=185733
    //							hasDelta = true;
    //						}
    //					}
    //				}
    //			}
    //
    //			if (deltaContainsModifiedJar) {
    //				javaProject.resetResolvedClasspath();
    //			}
    //		}
    //
    //		if (hasDelta){
    //			// flush jar type cache
    //			JavaModelManager.getJavaModelManager().resetJarTypeCache();
    //		}
    return hasDelta;
  }

  private JavaElementDelta currentDelta() {
    if (this.currentDelta == null) {
      this.currentDelta = new JavaElementDelta(this.manager.getJavaModel());
    }
    return this.currentDelta;
  }

  /*
   * Note that the project is about to be deleted.
   */
  private void deleting(IProject project) {

    //		try {
    //			// discard indexing jobs that belong to this project so that the project can be
    //			// deleted without interferences from the index manager
    //			this.manager.indexManager.discardJobs(project.getName());
    //
    //			JavaProject javaProject = (JavaProject)JavaCore.create(project);
    //
    //			// remember roots of this project
    //			if (this.oldRoots == null) {
    //				this.oldRoots = new HashMap();
    //			}
    //			if (javaProject.isOpen()) {
    //				this.oldRoots.put(javaProject, javaProject.getPackageFragmentRoots());
    //			} else {
    //				// compute roots without opening project
    //				this.oldRoots.put(
    //					javaProject,
    //					javaProject.computePackageFragmentRoots(
    //						javaProject.getResolvedClasspath(),
    //						false,
    //						null /*no reverse map*/));
    //			}
    //
    //			javaProject.close();
    //
    //			// workaround for bug 15168 circular errors not reported
    //			this.state.getOldJavaProjecNames(); // foce list to be computed
    //
    //			removeFromParentInfo(javaProject);
    //
    //			// remove preferences from per project info
    //			this.manager.resetProjectPreferences(javaProject);
    //		} catch (JavaModelException e) {
    //			// java project doesn't exist: ignore
    //		}
    throw new UnsupportedOperationException();
  }

  /*
   * Processing for an element that has been added:<ul>
   * <li>If the element is a project, do nothing, and do not process
   * children, as when a project is created it does not yet have any
   * natures - specifically a java nature.
   * <li>If the elemet is not a project, process it as added (see
   * <code>basicElementAdded</code>.
   * </ul>
   * Delta argument could be null if processing an external JAR change
   */
  private void elementAdded(Openable element, IResourceDelta delta, RootInfo rootInfo) {
    int elementType = element.getElementType();

    if (elementType == IJavaElement.JAVA_PROJECT) {
      // project add is handled by JavaProject.configure() because
      // when a project is created, it does not yet have a Java nature
      IProject project;
      if (delta != null && JavaProject.hasJavaNature(project = (IProject) delta.getResource())) {
        addToParentInfo(element);
        this.manager
            .getPerProjectInfo(project, true /*create info if needed*/)
            .rememberExternalLibTimestamps();
        if ((delta.getFlags() & IResourceDelta.MOVED_FROM) != 0) {
          Openable movedFromElement =
              (Openable)
                  element.getJavaModel().getJavaProject(delta.getMovedFromPath().lastSegment());
          currentDelta().movedTo(element, movedFromElement);
        } else {
          // Force the project to be closed as it might have been opened
          // before the resource modification came in and it might have a new child
          // For example, in an IWorkspaceRunnable:
          // 1. create a Java project P (where P=src)
          // 2. open project P
          // 3. add folder f in P's pkg fragment root
          // When the resource delta comes in, only the addition of P is notified,
          // but the pkg fragment root of project P is already opened, thus its children are not
          // recomputed
          // and it appears to contain only the default package.
          close(element);

          currentDelta().added(element);
        }
        this.state.updateRoots(element.getPath(), delta, this);

        // remember that the project's cache must be reset
        this.projectCachesToReset.add(element);
      }
    } else {
      if (delta == null || (delta.getFlags() & IResourceDelta.MOVED_FROM) == 0) {
        // regular element addition
        if (isPrimaryWorkingCopy(element, elementType)) {
          // filter out changes to primary compilation unit in working copy mode
          // just report a change to the resource (see
          // https://bugs.eclipse.org/bugs/show_bug.cgi?id=59500)
          currentDelta().changed(element, IJavaElementDelta.F_PRIMARY_RESOURCE);
        } else {
          addToParentInfo(element);

          // Force the element to be closed as it might have been opened
          // before the resource modification came in and it might have a new child
          // For example, in an IWorkspaceRunnable:
          // 1. create a package fragment p using a java model operation
          // 2. open package p
          // 3. add file X.java in folder p
          // When the resource delta comes in, only the addition of p is notified,
          // but the package p is already opened, thus its children are not recomputed
          // and it appears empty.
          close(element);

          currentDelta().added(element);
        }
      } else {
        // element is moved
        addToParentInfo(element);
        close(element);

        IPath movedFromPath = delta.getMovedFromPath();
        IResource res = delta.getResource();
        IResource movedFromRes;
        if (res instanceof IFile) {
          movedFromRes = res.getWorkspace().getRoot().getFile(movedFromPath);
        } else {
          movedFromRes = res.getWorkspace().getRoot().getFolder(movedFromPath);
        }

        // find the element type of the moved from element
        IPath rootPath = externalPath(movedFromRes);
        RootInfo movedFromInfo = enclosingRootInfo(rootPath, IResourceDelta.REMOVED);
        int movedFromType =
            elementType(
                movedFromRes,
                IResourceDelta.REMOVED,
                element.getParent().getElementType(),
                movedFromInfo);

        // reset current element as it might be inside a nested root (popUntilPrefixOf() may use the
        // outer root)
        this.currentElement = null;

        // create the moved from element
        Openable movedFromElement =
            elementType != IJavaElement.JAVA_PROJECT && movedFromType == IJavaElement.JAVA_PROJECT
                ? null
                : // outside classpath
                createElement(movedFromRes, movedFromType, movedFromInfo);
        if (movedFromElement == null) {
          // moved from outside classpath
          currentDelta().added(element);
        } else {
          currentDelta().movedTo(element, movedFromElement);
        }
      }

      switch (elementType) {
        case IJavaElement.PACKAGE_FRAGMENT_ROOT:
          // when a root is added, and is on the classpath, the project must be updated
          JavaProject project = (JavaProject) element.getJavaProject();

          // remember that the project's cache must be reset
          this.projectCachesToReset.add(project);

          break;
        case IJavaElement.PACKAGE_FRAGMENT:
          // reset project's package fragment cache
          project = (JavaProject) element.getJavaProject();
          this.projectCachesToReset.add(project);

          break;
      }
    }
  }

  /*
   * Generic processing for a removed element:<ul>
   * <li>Close the element, removing its structure from the cache
   * <li>Remove the element from its parent's cache of children
   * <li>Add a REMOVED entry in the delta
   * </ul>
   * Delta argument could be null if processing an external JAR change
   */
  private void elementRemoved(Openable element, IResourceDelta delta, RootInfo rootInfo) {

    int elementType = element.getElementType();
    if (delta == null || (delta.getFlags() & IResourceDelta.MOVED_TO) == 0) {
      // regular element removal
      if (isPrimaryWorkingCopy(element, elementType)) {
        // filter out changes to primary compilation unit in working copy mode
        // just report a change to the resource (see
        // https://bugs.eclipse.org/bugs/show_bug.cgi?id=59500)
        currentDelta().changed(element, IJavaElementDelta.F_PRIMARY_RESOURCE);
      } else {
        close(element);
        removeFromParentInfo(element);
        currentDelta().removed(element);
      }
    } else {
      // element is moved
      close(element);
      removeFromParentInfo(element);
      IPath movedToPath = delta.getMovedToPath();
      IResource res = delta.getResource();
      IResource movedToRes;
      //            movedToRes = movedToPath.toFile();
      switch (res.getType()) {
        case IResource.PROJECT:
          movedToRes = res.getWorkspace().getRoot().getProject(movedToPath.lastSegment());
          break;
        case IResource.FOLDER:
          movedToRes = res.getWorkspace().getRoot().getFolder(movedToPath);
          break;
        case IResource.FILE:
          movedToRes = res.getWorkspace().getRoot().getFile(movedToPath);
          break;
        default:
          return;
      }

      // find the element type of the moved from element
      IPath rootPath = externalPath(movedToRes);
      RootInfo movedToInfo = enclosingRootInfo(rootPath, IResourceDelta.ADDED);
      int movedToType =
          elementType(
              movedToRes, IResourceDelta.ADDED, element.getParent().getElementType(), movedToInfo);

      // reset current element as it might be inside a nested root (popUntilPrefixOf() may use the
      // outer root)
      this.currentElement = null;

      // create the moved To element
      Openable movedToElement =
          elementType != IJavaElement.JAVA_PROJECT && movedToType == IJavaElement.JAVA_PROJECT
              ? null
              : // outside classpath
              createElement(movedToRes, movedToType, movedToInfo);
      if (movedToElement == null) {
        // moved outside classpath
        currentDelta().removed(element);
      } else {
        currentDelta().movedFrom(element, movedToElement);
      }
    }

    switch (elementType) {
      case IJavaElement.JAVA_MODEL:
        this.manager.indexManager.reset();
        break;
      case IJavaElement.JAVA_PROJECT:
        this.state.updateRoots(element.getPath(), delta, this);
        // TODO: this is quick fix for https://jira.codenvycorp.com/browse/IDEX-4221
        // we do it because we need clear cache for deleted project but we don't know how do it
        // right way
        // so we clean all cache totally
        this.state.roots.clear();
        // remember that the project's cache must be reset
        this.projectCachesToReset.add(element);

        break;
      case IJavaElement.PACKAGE_FRAGMENT_ROOT:
        JavaProject project = (JavaProject) element.getJavaProject();

        // remember that the project's cache must be reset
        this.projectCachesToReset.add(project);

        break;
      case IJavaElement.PACKAGE_FRAGMENT:
        // reset package fragment cache
        project = (JavaProject) element.getJavaProject();
        this.projectCachesToReset.add(project);

        break;
    }
  }

  /*
   * Returns the type of the java element the given delta matches to.
   * Returns NON_JAVA_RESOURCE if unknown (e.g. a non-java resource or excluded .java file)
   */
  private int elementType(IResource res, int kind, int parentType, RootInfo rootInfo) {
    switch (parentType) {
      case IJavaElement.JAVA_MODEL:
        // case of a movedTo or movedFrom project (other cases are handled in
        // processResourceDelta(...)
        return IJavaElement.JAVA_PROJECT;

      case NON_JAVA_RESOURCE:
      case IJavaElement.JAVA_PROJECT:
        if (rootInfo == null) {
          rootInfo = enclosingRootInfo(res.getFullPath(), kind);
        }
        if (rootInfo != null && rootInfo.isRootOfProject(res.getFullPath())) {
          return IJavaElement.PACKAGE_FRAGMENT_ROOT;
        }
        // not yet in a package fragment root or root of another project
        // or package fragment to be included (see below)
        // $FALL-THROUGH$

      case IJavaElement.PACKAGE_FRAGMENT_ROOT:
      case IJavaElement.PACKAGE_FRAGMENT:
        if (rootInfo == null) {
          IPath rootPath = externalPath(res);
          rootInfo = enclosingRootInfo(rootPath, kind);
        }
        if (rootInfo == null) {
          return NON_JAVA_RESOURCE;
        }
        if (Util.isExcluded(res, rootInfo.inclusionPatterns, rootInfo.exclusionPatterns)) {
          return NON_JAVA_RESOURCE;
        }
        if (res.getType() == IResource.FOLDER) {
          if (parentType == NON_JAVA_RESOURCE
              && !Util.isExcluded(
                  res.getParent(), rootInfo.inclusionPatterns, rootInfo.exclusionPatterns)) {
            // parent is a non-Java resource because it doesn't have a valid package name (see
            // https://bugs.eclipse
            // .org/bugs/show_bug.cgi?id=130982)
            return NON_JAVA_RESOURCE;
          }
          String sourceLevel =
              rootInfo.project == null
                  ? null
                  : rootInfo.project.getOption(JavaCore.COMPILER_SOURCE, true);
          String complianceLevel =
              rootInfo.project == null
                  ? null
                  : rootInfo.project.getOption(JavaCore.COMPILER_COMPLIANCE, true);
          if (Util.isValidFolderNameForPackage(res.getName(), sourceLevel, complianceLevel)) {
            return IJavaElement.PACKAGE_FRAGMENT;
          }
          return NON_JAVA_RESOURCE;
        }
        String fileName = res.getName();
        String sourceLevel =
            rootInfo.project == null
                ? null
                : rootInfo.project.getOption(JavaCore.COMPILER_SOURCE, true);
        String complianceLevel =
            rootInfo.project == null
                ? null
                : rootInfo.project.getOption(JavaCore.COMPILER_COMPLIANCE, true);
        if (Util.isValidCompilationUnitName(fileName, sourceLevel, complianceLevel)) {
          return IJavaElement.COMPILATION_UNIT;
        } else if (Util.isValidClassFileName(fileName, sourceLevel, complianceLevel)) {
          return IJavaElement.CLASS_FILE;
        } else {
          IPath rootPath = externalPath(res);
          if ((rootInfo = rootInfo(rootPath, kind)) != null
              && rootInfo
                  .project
                  .getProject()
                  .getFullPath()
                  .isPrefixOf(
                      rootPath) /*ensure root is a root of its project (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=185310)
                                 */) {
            // case of proj=src=bin and resource is a jar file on the classpath
            return IJavaElement.PACKAGE_FRAGMENT_ROOT;
          } else {
            return NON_JAVA_RESOURCE;
          }
        }

      default:
        return NON_JAVA_RESOURCE;
    }
  }

  /*
   * Flushes all deltas without firing them.
   */
  public void flush() {
    this.javaModelDeltas = new ArrayList();
  }

  private SourceElementParser getSourceElementParser(Openable element) {
    if (this.sourceElementParserCache == null)
      this.sourceElementParserCache =
          this.manager.indexManager.getSourceElementParser(
              element.getJavaProject(), null /*requestor will be set by indexer*/);
    return this.sourceElementParserCache;
  }

  /*
   * Finds the root info this path is included in.
   * Returns null if not found.
   */
  private RootInfo enclosingRootInfo(IPath path, int kind) {
    while (path != null && path.segmentCount() > 0) {
      RootInfo rootInfo = rootInfo(path, kind);
      if (rootInfo != null) return rootInfo;
      path = path.removeLastSegments(1);
    }
    return null;
  }

  private IPath externalPath(IResource res) {
    IPath resourcePath = res.getFullPath();
    //		if (ExternalFoldersManager.isInternalPathForExternalFolder(resourcePath))
    //			return res.getLocation();
    return resourcePath;
  }

  /*
   * Fire Java Model delta, flushing them after the fact after post_change notification.
   * If the firing mode has been turned off, this has no effect.
   */
  public void fire(IJavaElementDelta customDelta, int eventType) {
    if (!this.isFiring) return;

    if (DEBUG) {
      System.out.println(
          "-----------------------------------------------------------------------------------------------------------------------"); // $NON-NLS-1$
    }

    IJavaElementDelta deltaToNotify;
    if (customDelta == null) {
      deltaToNotify = mergeDeltas(this.javaModelDeltas);
    } else {
      deltaToNotify = customDelta;
    }

    // Refresh internal scopes
    if (deltaToNotify != null) {
      Iterator scopes = this.manager.searchScopes.keySet().iterator();
      while (scopes.hasNext()) {
        AbstractSearchScope scope = (AbstractSearchScope) scopes.next();
        scope.processDelta(deltaToNotify, eventType);
      }
      JavaWorkspaceScope workspaceScope = this.manager.workspaceScope;
      if (workspaceScope != null) workspaceScope.processDelta(deltaToNotify, eventType);
    }

    // Notification

    // Important: if any listener reacts to notification by updating the listeners list or mask,
    // these lists will
    // be duplicated, so it is necessary to remember original lists in a variable (since field
    // values may change under us)
    IElementChangedListener[] listeners;
    int[] listenerMask;
    int listenerCount;
    synchronized (this.state) {
      listeners = this.state.elementChangedListeners;
      listenerMask = this.state.elementChangedListenerMasks;
      listenerCount = this.state.elementChangedListenerCount;
    }

    switch (eventType) {
      case DEFAULT_CHANGE_EVENT:
      case ElementChangedEvent.POST_CHANGE:
        firePostChangeDelta(deltaToNotify, listeners, listenerMask, listenerCount);
        fireReconcileDelta(listeners, listenerMask, listenerCount);
        break;
    }
  }

  private void firePostChangeDelta(
      IJavaElementDelta deltaToNotify,
      IElementChangedListener[] listeners,
      int[] listenerMask,
      int listenerCount) {

    // post change deltas
    if (DEBUG) {
      System.out.println(
          "FIRING POST_CHANGE Delta [" + Thread.currentThread() + "]:"); // $NON-NLS-1$//$NON-NLS-2$
      System.out.println(
          deltaToNotify == null ? "<NONE>" : deltaToNotify.toString()); // $NON-NLS-1$
    }
    if (deltaToNotify != null) {
      // flush now so as to keep listener reactions to post their own deltas for subsequent
      // iteration
      flush();

      //			// mark the operation stack has not modifying resources since resource deltas are being
      // fired
      //			JavaModelOperation.setAttribute(JavaModelOperation.HAS_MODIFIED_RESOURCE_ATTR, null);

      notifyListeners(
          deltaToNotify, ElementChangedEvent.POST_CHANGE, listeners, listenerMask, listenerCount);
    }
  }

  private void fireReconcileDelta(
      IElementChangedListener[] listeners, int[] listenerMask, int listenerCount) {

    IJavaElementDelta deltaToNotify = mergeDeltas(this.reconcileDeltas.values());
    if (DEBUG) {
      System.out.println(
          "FIRING POST_RECONCILE Delta ["
              + Thread.currentThread()
              + "]:"); // $NON-NLS-1$//$NON-NLS-2$
      System.out.println(
          deltaToNotify == null ? "<NONE>" : deltaToNotify.toString()); // $NON-NLS-1$
    }
    if (deltaToNotify != null) {
      // flush now so as to keep listener reactions to post their own deltas for subsequent
      // iteration
      this.reconcileDeltas = new HashMap();

      notifyListeners(
          deltaToNotify,
          ElementChangedEvent.POST_RECONCILE,
          listeners,
          listenerMask,
          listenerCount);
    }
  }

  /*
   * Returns whether a given delta contains some information relevant to the JavaModel,
   * in particular it will not consider SYNC or MARKER only deltas.
   */
  private boolean isAffectedBy(IResourceDelta rootDelta) {
    // if (rootDelta == null) System.out.println("NULL DELTA");
    // long start = System.currentTimeMillis();
    if (rootDelta != null) {
      // use local exception to quickly escape from delta traversal
      class FoundRelevantDeltaException extends RuntimeException {
        private static final long serialVersionUID = 7137113252936111022L; // backward compatible
        // only the class name is used (to differenciate from other RuntimeExceptions)
      }
      try {
        rootDelta.accept(
            new IResourceDeltaVisitor() {
              @Override
              public boolean visit(org.eclipse.core.resources.IResourceDelta delta)
                  throws CoreException {
                switch (delta.getKind()) {
                  case IResourceDelta.ADDED:
                  case IResourceDelta.REMOVED:
                    throw new FoundRelevantDeltaException();
                  case IResourceDelta.CHANGED:
                    // if any flag is set but SYNC or MARKER, this delta should be considered
                    //												 if (delta.getAffectedChildren().length == 0 // only check leaf
                    // delta nodes
                    //													 && (delta.getFlags() & ~(IResourceDelta.SYNC |
                    // IResourceDelta.MARKERS)) != 0) {
                    throw new FoundRelevantDeltaException();
                    //												 }
                }
                return true;
              }
            },
            IContainer.INCLUDE_HIDDEN);
      } catch (FoundRelevantDeltaException e) {
        // System.out.println("RELEVANT DELTA detected in: "+ (System.currentTimeMillis() - start));
        return true;
      } catch (CoreException e) { // ignore delta if not able to traverse
      }
    }
    // System.out.println("IGNORE SYNC DELTA took: "+ (System.currentTimeMillis() - start));
    return false;
  }

  /*
   * Returns whether the given element is a primary compilation unit in working copy mode.
   */
  private boolean isPrimaryWorkingCopy(IJavaElement element, int elementType) {
    if (elementType == IJavaElement.COMPILATION_UNIT) {
      CompilationUnit cu = (CompilationUnit) element;
      return cu.isPrimary() && cu.isWorkingCopy();
    }
    return false;
  }

  /*
   * Returns whether the given resource is in one of the given output folders and if
   * it is filtered out from this output folder.
   */
  private boolean isResFilteredFromOutput(
      RootInfo rootInfo, OutputsInfo info, IResource res, int elementType) {
    if (info != null) {
      JavaProject javaProject = null;
      String sourceLevel = null;
      String complianceLevel = null;
      IPath resPath = res.getFullPath();
      for (int i = 0; i < info.outputCount; i++) {
        if (info.paths[i].isPrefixOf(resPath)) {
          if (info.traverseModes[i] != IGNORE) {
            // case of bin=src
            if (info.traverseModes[i] == SOURCE && elementType == IJavaElement.CLASS_FILE) {
              return true;
            }
            // case of .class file under project and no source folder
            // proj=bin
            if (elementType == IJavaElement.JAVA_PROJECT && res instanceof IFile) {
              //							if (sourceLevel == null) {
              //								// Get java project to use its source and compliance levels
              //								javaProject = rootInfo == null ?
              //									(JavaProject)createElement(res.getProject(), IJavaElement.JAVA_PROJECT,
              // null) :
              //									rootInfo.project;
              //								if (javaProject != null) {
              //									sourceLevel = javaProject.getOption(JavaCore.COMPILER_SOURCE, true);
              //									complianceLevel = javaProject.getOption(JavaCore.COMPILER_COMPLIANCE,
              // true);
              //								}
              //							}
              if (Util.isValidClassFileName(res.getName(), sourceLevel, complianceLevel)) {
                return true;
              }
            }
          } else {
            return true;
          }
        }
      }
    }
    return false;
  }

  /*
   * Merges all awaiting deltas.
   */
  private IJavaElementDelta mergeDeltas(Collection deltas) {
    if (deltas.size() == 0) return null;
    if (deltas.size() == 1) return (IJavaElementDelta) deltas.iterator().next();

    if (VERBOSE) {
      System.out.println(
          "MERGING "
              + deltas.size()
              + " DELTAS ["
              + Thread.currentThread()
              + "]"); // $NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    Iterator iterator = deltas.iterator();
    JavaElementDelta rootDelta = new JavaElementDelta(this.manager.javaModel);
    boolean insertedTree = false;
    while (iterator.hasNext()) {
      JavaElementDelta delta = (JavaElementDelta) iterator.next();
      if (VERBOSE) {
        System.out.println(delta.toString());
      }
      IJavaElement element = delta.getElement();
      if (this.manager.javaModel.equals(element)) {
        IJavaElementDelta[] children = delta.getAffectedChildren();
        for (int j = 0; j < children.length; j++) {
          JavaElementDelta projectDelta = (JavaElementDelta) children[j];
          rootDelta.insertDeltaTree(projectDelta.getElement(), projectDelta);
          insertedTree = true;
        }
        IResourceDelta[] resourceDeltas = delta.getResourceDeltas();
        if (resourceDeltas != null) {
          for (int i = 0, length = resourceDeltas.length; i < length; i++) {
            rootDelta.addResourceDelta(resourceDeltas[i]);
            insertedTree = true;
          }
        }
      } else {
        rootDelta.insertDeltaTree(element, delta);
        insertedTree = true;
      }
    }
    if (insertedTree) return rootDelta;
    return null;
  }

  private void notifyListeners(
      IJavaElementDelta deltaToNotify,
      int eventType,
      IElementChangedListener[] listeners,
      int[] listenerMask,
      int listenerCount) {
    final ElementChangedEvent extraEvent = new ElementChangedEvent(deltaToNotify, eventType);
    for (int i = 0; i < listenerCount; i++) {
      if ((listenerMask[i] & eventType) != 0) {
        final IElementChangedListener listener = listeners[i];
        long start = -1;
        if (VERBOSE) {
          System.out.print(
              "Listener #" + (i + 1) + "=" + listener.toString()); // $NON-NLS-1$//$NON-NLS-2$
          start = System.currentTimeMillis();
        }
        // wrap callbacks with Safe runnable for subsequent listeners to be called when some are
        // causing grief
        SafeRunner.run(
            new ISafeRunnable() {
              public void handleException(Throwable exception) {
                Util.log(
                    exception,
                    "Exception occurred in listener of Java element change notification"); // $NON-NLS-1$
              }

              public void run() throws Exception {
                PerformanceStats stats = null;
                if (PERF) {
                  //							stats = PerformanceStats.getStats(JavaModelManager.DELTA_LISTENER_PERF,
                  // listener);
                  //							stats.startRun();
                }
                listener.elementChanged(extraEvent);
                if (PERF) {
                  stats.endRun();
                }
              }
            });
        if (VERBOSE) {
          System.out.println(
              " -> " + (System.currentTimeMillis() - start) + "ms"); // $NON-NLS-1$ //$NON-NLS-2$
        }
      }
    }
  }

  private void notifyTypeHierarchies(IElementChangedListener[] listeners, int listenerCount) {
    for (int i = 0; i < listenerCount; i++) {
      final IElementChangedListener listener = listeners[i];
      if (!(listener instanceof TypeHierarchy)) continue;

      // wrap callbacks with Safe runnable for subsequent listeners to be called when some are
      // causing grief
      SafeRunner.run(
          new ISafeRunnable() {
            public void handleException(Throwable exception) {
              Util.log(
                  exception,
                  "Exception occurred in listener of Java element change notification"); // $NON-NLS-1$
            }

            public void run() throws Exception {
              TypeHierarchy typeHierarchy = (TypeHierarchy) listener;
              if (typeHierarchy.hasFineGrainChanges()) {
                // case of changes in primary working copies
                typeHierarchy.needsRefresh = true;
                typeHierarchy.fireChange();
              }
            }
          });
    }
  }

  /*
   * Generic processing for elements with changed contents:<ul>
   * <li>The element is closed such that any subsequent accesses will re-open
   * the element reflecting its new structure.
   * <li>An entry is made in the delta reporting a content change (K_CHANGE with F_CONTENT flag set).
   * </ul>
   */
  private void nonJavaResourcesChanged(Openable element, IResourceDelta delta)
      throws JavaModelException {
    //		// reset non-java resources if element was open
    //		if (element.isOpen()) {
    //			JavaElementInfo info = (JavaElementInfo)element.getElementInfo();
    //			switch (element.getElementType()) {
    //				case IJavaElement.JAVA_MODEL :
    //					((JavaModelInfo) info).nonJavaResources = null;
    //					if (!ExternalFoldersManager.isInternalPathForExternalFolder(delta.getFullPath()))
    //						currentDelta().addResourceDelta(delta);
    //					return;
    //				case IJavaElement.JAVA_PROJECT :
    //					((org.eclipse.jdt.internal.core.JavaProjectElementInfo) info).setNonJavaResources(null);
    //
    //					// if a package fragment root is the project, clear it too
    //					JavaProject project = (JavaProject) element;
    //					PackageFragmentRoot projectRoot =
    //						(PackageFragmentRoot) project.getPackageFragmentRoot(project.getProject());
    //					if (projectRoot.isOpen()) {
    //						((org.eclipse.jdt.internal.core.PackageFragmentRootInfo)
    // projectRoot.getElementInfo()).setNonJavaResources(null);
    //					}
    //					break;
    //				case IJavaElement.PACKAGE_FRAGMENT :
    //					 ((org.eclipse.jdt.internal.core.PackageFragmentInfo) info).setNonJavaResources(null);
    //					break;
    //				case IJavaElement.PACKAGE_FRAGMENT_ROOT :
    //					 ((org.eclipse.jdt.internal.core.PackageFragmentRootInfo)
    // info).setNonJavaResources(null);
    //			}
    //		}
    //
    //		JavaElementDelta current = currentDelta();
    //		JavaElementDelta elementDelta = current.find(element);
    //		if (elementDelta == null) {
    //			// don't use find after creating the delta as it can be null (see
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=63434)
    //			elementDelta = current.changed(element, IJavaElementDelta.F_CONTENT);
    //		}
    //		if (!ExternalFoldersManager.isInternalPathForExternalFolder(delta.getFullPath()))
    //			elementDelta.addResourceDelta(delta);
    throw new UnsupportedOperationException();
  }

  /*
   * Returns the old root info for the given path and project.
   */
  private RootInfo oldRootInfo(IPath path, JavaProject project) {
    RootInfo oldInfo = (RootInfo) this.state.oldRoots.get(path);
    if (oldInfo == null) return null;
    if (oldInfo.project.equals(project)) return oldInfo;
    ArrayList oldInfos = (ArrayList) this.state.oldOtherRoots.get(path);
    if (oldInfos == null) return null;
    for (int i = 0, length = oldInfos.size(); i < length; i++) {
      oldInfo = (RootInfo) oldInfos.get(i);
      if (oldInfo.project.equals(project)) return oldInfo;
    }
    return null;
  }

  /*
   * Returns the other root infos for the given path. Look in the old other roots table if kind is REMOVED.
   */
  private ArrayList otherRootsInfo(IPath path, int kind) {
    if (kind == IResourceDelta.REMOVED) {
      return (ArrayList) this.state.oldOtherRoots.get(path);
    }
    return (ArrayList) this.state.otherRoots.get(path);
  }

  private OutputsInfo outputsInfo(RootInfo rootInfo, File res) {
    //		try {
    //			JavaProject proj =
    //				rootInfo == null ?
    //					(JavaProject)createElement(res.getProject(), IJavaElement.JAVA_PROJECT, null) :
    //					rootInfo.project;
    //			if (proj != null) {
    //				IPath projectOutput = proj.getOutputLocation();
    //				int traverseMode = IGNORE;
    //				if (proj.getProject().getFullPath().equals(projectOutput)){ // case of proj==bin==src
    //					return new OutputsInfo(new IPath[] {projectOutput}, new int[] {SOURCE}, 1);
    //				}
    //				IClasspathEntry[] classpath = proj.getResolvedClasspath();
    //				IPath[] outputs = new IPath[classpath.length+1];
    //				int[] traverseModes = new int[classpath.length+1];
    //				int outputCount = 1;
    //				outputs[0] = projectOutput;
    //				traverseModes[0] = traverseMode;
    //				for (int i = 0, length = classpath.length; i < length; i++) {
    //					IClasspathEntry entry = classpath[i];
    //					IPath entryPath = entry.getPath();
    //					IPath output = entry.getOutputLocation();
    //					if (output != null) {
    //						outputs[outputCount] = output;
    //						// check case of src==bin
    //						if (entryPath.equals(output)) {
    //							traverseModes[outputCount++] = (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE) ?
    // SOURCE : BINARY;
    //						} else {
    //							traverseModes[outputCount++] = IGNORE;
    //						}
    //					}
    //
    //					// check case of src==bin
    //					if (entryPath.equals(projectOutput)) {
    //						traverseModes[0] = (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE) ? SOURCE :
    // BINARY;
    //					}
    //				}
    //				return new OutputsInfo(outputs, traverseModes, outputCount);
    //			}
    //		} catch (JavaModelException e) {
    //			// java project doesn't exist: ignore
    //		}
    //		return null;
    throw new UnsupportedOperationException();
  }

  private void popUntilPrefixOf(IPath path) {
    while (this.currentElement != null) {
      IPath currentElementPath = null;
      if (this.currentElement instanceof IPackageFragmentRoot) {
        currentElementPath = ((IPackageFragmentRoot) this.currentElement).getPath();
      } else {
        IResource currentElementResource = this.currentElement.resource();
        if (currentElementResource != null) {
          currentElementPath = currentElementResource.getFullPath();
        }
      }
      if (currentElementPath != null) {
        if (this.currentElement instanceof IPackageFragment
            && ((IPackageFragment) this.currentElement).isDefaultPackage()
            && currentElementPath.segmentCount() != path.segmentCount() - 1) {
          // default package and path is not a direct child
          this.currentElement = (Openable) this.currentElement.getParent();
        }
        if (currentElementPath.isPrefixOf(path)) {
          return;
        }
      }
      this.currentElement = (Openable) this.currentElement.getParent();
    }
  }

  /*
   * Converts a <code>IResourceDelta</code> rooted in a <code>Workspace</code> into
   * the corresponding set of <code>IJavaElementDelta</code>, rooted in the
   * relevant <code>JavaModel</code>s.
   */
  private IJavaElementDelta processResourceDelta(IResourceDelta changes) {

    try {
      IJavaModel model = this.manager.getJavaModel();
      if (!model.isOpen()) {
        // force opening of java model so that java element delta are reported
        try {
          model.open(null);
        } catch (JavaModelException e) {
          if (VERBOSE) {
            e.printStackTrace();
          }
          return null;
        }
      }
      this.state.initializeRoots(false /*not initiAfterLoad*/);
      this.currentElement = null;

      // get the workspace delta, and start processing there.
      //			IResourceDelta[] deltas =
      // (IResourceDelta[])changes.getAffectedChildren(IResourceDelta.ADDED | IResourceDelta.REMOVED
      // |
      // IResourceDelta.CHANGED, IContainer.INCLUDE_HIDDEN);
      //			for (int i = 0; i < deltas.length; i++) {
      //				IResourceDelta delta = deltas[i];
      //				File res = delta.getFile();
      //
      //				// find out the element type
      //				RootInfo rootInfo = null;
      //				int elementType;
      //				IProject proj = (IProject)res;
      //				boolean wasJavaProject = this.state.findJavaProject(proj.getName()) != null;
      //				boolean isJavaProject = JavaProject.hasJavaNature(proj);
      //				if (!wasJavaProject && !isJavaProject) {
      //					elementType = NON_JAVA_RESOURCE;
      //				} else {
      //					IPath rootPath = externalPath(res);
      //					rootInfo = enclosingRootInfo(rootPath, delta.getKind());
      //					if (rootInfo != null && rootInfo.isRootOfProject(rootPath)) {
      //						elementType = IJavaElement.PACKAGE_FRAGMENT_ROOT;
      //					} else {
      //						elementType = IJavaElement.JAVA_PROJECT;
      //					}
      //				}
      //
      //				// traverse delta
      //				traverseDelta(changes, IJavaElement.JAVA_PROJECT, null, null);
      updateCurrentDeltaAndIndex(changes, IJavaElement.COMPILATION_UNIT, null);
      //
      //				if (elementType == NON_JAVA_RESOURCE
      //						|| (wasJavaProject != isJavaProject && (delta.getKind()) == IResourceDelta.CHANGED)) {
      // // project has changed
      // nature (description or open/closed)
      //					try {
      //						// add child as non java resource
      //						nonJavaResourcesChanged((JavaModel)model, delta);
      //					} catch (JavaModelException e) {
      //						// java model could not be opened
      //					}
      //				}
      //
      //			}
      resetProjectCaches();

      return this.currentDelta;
    } finally {
      this.currentDelta = null;
    }
  }

  /*
   * Traverse the set of projects which have changed namespace, and reset their
   * caches and their dependents
   */
  public void resetProjectCaches() {
    if (this.projectCachesToReset.size() == 0) return;

    //		JavaModelManager.getJavaModelManager().resetJarTypeCache();

    Iterator iterator = this.projectCachesToReset.iterator();
    HashMap projectDepencies = this.state.projectDependencies;
    HashSet affectedDependents = new HashSet();
    while (iterator.hasNext()) {
      JavaProject project = (JavaProject) iterator.next();
      project.resetCaches();
      addDependentProjects(project, projectDepencies, affectedDependents);
    }
    // reset caches of dependent projects
    iterator = affectedDependents.iterator();
    while (iterator.hasNext()) {
      JavaProject project = (JavaProject) iterator.next();
      project.resetCaches();
    }

    this.projectCachesToReset.clear();
  }

  /*
   * Registers the given delta with this delta processor.
   */
  public void registerJavaModelDelta(IJavaElementDelta delta) {
    this.javaModelDeltas.add(delta);
  }

  /*
   * Removes the given element from its parents cache of children. If the
   * element does not have a parent, or the parent is not currently open,
   * this has no effect.
   */
  private void removeFromParentInfo(Openable child) {

    Openable parent = (Openable) child.getParent();
    if (parent != null && parent.isOpen()) {
      try {
        OpenableElementInfo info = (OpenableElementInfo) parent.getElementInfo();
        info.removeChild(child);
      } catch (JavaModelException e) {
        // do nothing - we already checked if open
      }
    }
  }

  /*
   * Notification that some resource changes have happened
   * on the platform, and that the Java Model should update any required
   * internal structures such that its elements remain consistent.
   * Translates <code>IResourceDeltas</code> into <code>IJavaElementDeltas</code>.
   *
   * @see IResourceDelta
   * @see IResource
   */
  public void resourceChanged(IResourceChangeEvent event) {

    int eventType = this.overridenEventType == -1 ? event.getType() : this.overridenEventType;
    IResource resource = event.getResource();
    IResourceDelta delta = (IResourceDelta) event.getDelta();

    switch (eventType) {
      case IResourceChangeEvent.PRE_DELETE:
        //				try {
        //					if(resource.getType() == IResource.PROJECT
        //						/*&& ((IProject) resource).hasNature(JavaCore.NATURE_ID)*/) {
        //
        //						deleting((IProject)resource);
        //					}
        //				} catch(CoreException e){
        //					// project doesn't exist or is not open: ignore
        //				}
        return;

      case IResourceChangeEvent.PRE_REFRESH:
        //				IProject[] projects = null;
        //				Object o = event.getSource();
        //				if (o instanceof IProject) {
        //					projects = new IProject[] { (IProject) o };
        //				} else if (o instanceof IWorkspace) {
        //					// https://bugs.eclipse.org/bugs/show_bug.cgi?id=261594. The single workspace refresh
        //					// notification we see, implies that all projects are about to be refreshed.
        //					 projects = ((IWorkspace) o).getRoot().getProjects(IContainer.INCLUDE_HIDDEN);
        //				}
        //				//https://bugs.eclipse.org/bugs/show_bug.cgi?id=302295
        //				// Refresh all project references together in a single job
        //				JavaModelManager.getExternalManager().refreshReferences(projects, null);
        //
        //				IJavaProject[] javaElements = new IJavaProject[projects.length];
        //				for (int index = 0; index < projects.length; index++) {
        //					javaElements[index] = JavaCore.create(projects[index]);
        //				}
        //				try {
        //					checkExternalArchiveChanges(javaElements, true, null);
        //				} catch (JavaModelException e) {
        //		        	if (!e.isDoesNotExist())
        //		        		Util.log(e, "Exception while updating external archives"); //$NON-NLS-1$
        //				}
        return;

      case IResourceChangeEvent.POST_CHANGE:
        HashSet elementsToRefresh = this.state.removeExternalElementsToRefresh();
        if (isAffectedBy(delta) // avoid populating for SYNC or MARKER deltas
            || elementsToRefresh != null) {
          try {
            try {
              stopDeltas();
              checkProjectsAndClasspathChanges(delta);

              // generate external archive change deltas
              if (elementsToRefresh != null) {
                createExternalArchiveDelta(elementsToRefresh, null);
              }

              // generate classpath change deltas
              HashMap classpathChanges = this.state.removeAllClasspathChanges();
              //							if (classpathChanges.size() > 0) {
              //								boolean hasDelta = this.currentDelta != null;
              //								JavaElementDelta javaDelta = currentDelta();
              //								Iterator changes = classpathChanges.values().iterator();
              //								while (changes.hasNext()) {
              //									ClasspathChange change = (ClasspathChange) changes.next();
              //									int result = change.generateDelta(javaDelta, false/*don't add classpath
              // change*/);
              //									if ((result & ClasspathChange.HAS_DELTA) != 0) {
              //										hasDelta = true;
              //
              //										// need to recompute root infos
              //										this.state.rootsAreStale = true;
              //
              //										change.requestIndexing();
              //										this.state.addClasspathValidation(change.project);
              //									}
              //									if ((result & ClasspathChange.HAS_PROJECT_CHANGE) != 0) {
              //										this.state.addProjectReferenceChange(change.project,
              // change.oldResolvedClasspath);
              //									}
              //									if ((result & ClasspathChange.HAS_LIBRARY_CHANGE) != 0) {
              //										this.state.addExternalFolderChange(change.project,
              // change.oldResolvedClasspath);
              //									}
              //								}
              //								// process late coming external elements to refresh (see
              // https://bugs.eclipse.org/bugs/show_bug
              // .cgi?id=212769 )
              //								elementsToRefresh = this.state.removeExternalElementsToRefresh();
              //								if (elementsToRefresh != null) {
              //									hasDelta |= createExternalArchiveDelta(elementsToRefresh, null);
              //								}
              //								if (!hasDelta)
              //									this.currentDelta = null;
              //							}

              // generate Java deltas from resource changes
              IJavaElementDelta translatedDelta = processResourceDelta(delta);
              if (translatedDelta != null) {
                registerJavaModelDelta(translatedDelta);
              }
            } finally {
              this.sourceElementParserCache = null; // don't hold onto parser longer than necessary
              startDeltas();
            }
            IElementChangedListener[] listeners;
            int listenerCount;
            synchronized (this.state) {
              listeners = this.state.elementChangedListeners;
              listenerCount = this.state.elementChangedListenerCount;
            }
            notifyTypeHierarchies(listeners, listenerCount);
            fire(null, ElementChangedEvent.POST_CHANGE);
          } finally {
            // workaround for bug 15168 circular errors not reported
            this.state.resetOldJavaProjectNames();
            this.oldRoots = null;
          }
        }
        return;

      case IResourceChangeEvent.PRE_BUILD:
        //				// force initialization of roots before builders run to avoid deadlock in another
        // thread
        //				// (note this is no-op if already initialized)
        //				// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=241751
        //				this.state.initializeRoots(false/*not initiAfterLoad*/);
        //
        //				boolean isAffected = isAffectedBy(delta);
        //				boolean needCycleValidation = isAffected && validateClasspaths(delta);
        //
        //				// update external folders if necessary
        //			    ExternalFolderChange[] folderChanges = this.state.removeExternalFolderChanges();
        //				if (folderChanges != null) {
        //				    for (int i = 0, length = folderChanges.length; i < length; i++) {
        //				        try {
        //					        folderChanges[i].updateExternalFoldersIfNecessary(false/*do not refresh since
        // we are not in the thread that
        // added the external folder to the classpath*/, null);
        //				        } catch (JavaModelException e) {
        //				        	if (!e.isDoesNotExist())
        //				        		Util.log(e, "Exception while updating external folders"); //$NON-NLS-1$
        //				        }
        //				    }
        //				}
        //
        //				// create classpath markers if necessary
        //				ClasspathValidation[] validations = this.state.removeClasspathValidations();
        //				if (validations != null) {
        //					for (int i = 0, length = validations.length; i < length; i++) {
        //						ClasspathValidation validation = validations[i];
        //						validation.validate();
        //					}
        //				}
        //
        //				// update project references if necessary
        //			    ProjectReferenceChange[] projectRefChanges =
        // this.state.removeProjectReferenceChanges();
        //				if (projectRefChanges != null) {
        //				    for (int i = 0, length = projectRefChanges.length; i < length; i++) {
        //				        try {
        //					        projectRefChanges[i].updateProjectReferencesIfNecessary();
        //				        } catch(JavaModelException e) {
        //				            // project doesn't exist any longer, continue with next one
        //				        	if (!e.isDoesNotExist())
        //				        		Util.log(e, "Exception while updating project references"); //$NON-NLS-1$
        //				        }
        //				    }
        //				}
        //
        //				if (needCycleValidation || projectRefChanges != null) {
        //					// update all cycle markers since the project references changes may have affected
        // cycles
        //					try {
        //						JavaProject.validateCycles(null);
        //					} catch (JavaModelException e) {
        //						// a project no longer exists
        //					}
        //				}
        //
        //				if (isAffected) {
        //					Object source = event.getSource();
        //					projects = null;
        //					if (source instanceof IWorkspace) {
        //						 projects = ((IWorkspace) source).getRoot().getProjects();
        //					} else if (source instanceof IProject) {
        //						projects = new IProject[] {(IProject) source};
        //					} else {
        //						Util.log(new Exception(),
        //								 "Expected to see a workspace or project on the PRE_BUILD resource change but was:
        // " + source.toString());
        // $NON-NLS-1$
        //					}
        //					if (projects != null) {
        //						// If we are about to do a build and a Java project's first builder is not the Java
        // builder,
        //						// then it is possible that one of the earlier builders will build a jar file that
        // is on that
        //						// project's classpath. If we see that, then to be safe we must flush the caching of
        // the
        //						// JavaModelManager's external file state.
        //						// A possible further optimization for this situation where earlier builders can
        // affect the
        //						// Java builder would be to add a new classpath element attribute that identifies
        // whether
        //						// or not a library jar is "stable" and needs to be flushed.
        //						for (int i = 0; i < projects.length; i++) {
        //							try {
        //								IProject project = projects[i];
        //								if (project.isOpen() && project.hasNature(JavaCore.NATURE_ID)) {
        //									IBuildConfiguration[] configs = project.getBuildConfigs();
        //									if (configs.length > 1 && !JavaCore.BUILDER_ID.equals(configs[0].getName())) {
        //										this.manager.resetExternalFilesCache();
        //										break;
        //									}
        //								}
        //							} catch (CoreException exception) {
        //				        		Util.log(exception, "Exception while checking builder configuration
        // ordering"); //$NON-NLS-1$
        //							}
        //						 }
        //					}
        //					JavaBuilder.buildStarting();
        //				}

        // does not fire any deltas
        return;

      case IResourceChangeEvent.POST_BUILD:
        //                JavaBuilder.buildFinished();
        return;
    }
  }

  /*
   * Returns the root info for the given path. Look in the old roots table if kind is REMOVED.
   */
  private RootInfo rootInfo(IPath path, int kind) {
    if (kind == IResourceDelta.REMOVED) {
      return (RootInfo) this.state.oldRoots.get(path);
    }
    return (RootInfo) this.state.roots.get(path);
  }

  /*
   * Turns the firing mode to on. That is, deltas that are/have been
   * registered will be fired.
   */
  private void startDeltas() {
    this.isFiring = true;
  }

  /*
   * Turns the firing mode to off. That is, deltas that are/have been
   * registered will not be fired until deltas are started again.
   */
  private void stopDeltas() {
    this.isFiring = false;
  }

  /*
   * Converts an <code>IResourceDelta</code> and its children into
   * the corresponding <code>IJavaElementDelta</code>s.
   */
  private void traverseDelta(
      IResourceDelta delta, int elementType, RootInfo rootInfo, OutputsInfo outputsInfo) {

    IResource res = delta.getResource();

    // set stack of elements
    if (this.currentElement == null && rootInfo != null) {
      this.currentElement = rootInfo.project;
    }

    // process current delta
    boolean processChildren = true;
    if (res instanceof IProject) {
      // reset source element parser cache
      this.sourceElementParserCache = null;

      processChildren =
          updateCurrentDeltaAndIndex(
              delta,
              elementType == IJavaElement.PACKAGE_FRAGMENT_ROOT
                  ? IJavaElement.JAVA_PROJECT
                  : // case of prj=src
                  elementType,
              rootInfo);
    } else if (rootInfo != null) {
      processChildren = updateCurrentDeltaAndIndex(delta, elementType, rootInfo);
    } else {
      // not yet inside a package fragment root
      processChildren = true;
    }

    // get the project's output locations and traverse mode
    //		if (outputsInfo == null) outputsInfo = outputsInfo(rootInfo, res);

    // process children if needed
    if (processChildren) {
      IResourceDelta[] children = (IResourceDelta[]) delta.getAffectedChildren();
      boolean oneChildOnClasspath = false;
      int length = children.length;
      IResourceDelta[] orphanChildren = null;
      Openable parent = null;
      boolean isValidParent = true;
      for (int i = 0; i < length; i++) {
        IResourceDelta child = children[i];
        IResource childRes = child.getResource();

        // check source attachment change
        checkSourceAttachmentChange(child, childRes);

        // find out whether the child is a package fragment root of the current project
        IPath childPath = externalPath(childRes);
        int childKind = child.getKind();
        RootInfo childRootInfo = rootInfo(childPath, childKind);
        RootInfo originalChildRootInfo = childRootInfo;
        if (childRootInfo != null && !childRootInfo.isRootOfProject(childPath)) {
          // package fragment root of another project (dealt with later)
          childRootInfo = null;
        }

        // compute child type
        int childType =
            elementType(
                childRes, childKind, elementType, rootInfo == null ? childRootInfo : rootInfo);

        // is childRes in the output folder and is it filtered out ?
        boolean isResFilteredFromOutput =
            isResFilteredFromOutput(rootInfo, outputsInfo, childRes, childType);

        boolean isNestedRoot = rootInfo != null && childRootInfo != null;
        if (!isResFilteredFromOutput
            && !isNestedRoot) { // do not treat as non-java rsc if nested root

          traverseDelta(
              child,
              childType,
              rootInfo == null ? childRootInfo : rootInfo,
              outputsInfo); // traverse delta for child in the same project

          if (childType == NON_JAVA_RESOURCE) {
            if (rootInfo != null) { // if inside a package fragment root
              if (!isValidParent) continue;
              if (parent == null) {
                // find the parent of the non-java resource to attach to
                if (this.currentElement == null
                    || !rootInfo.project.equals(
                        this.currentElement.getJavaProject())) { // note if currentElement is the
                  // IJavaModel, getJavaProject() is null
                  // force the currentProject to be used
                  this.currentElement = rootInfo.project;
                }
                if (elementType == IJavaElement.JAVA_PROJECT
                    || (elementType == IJavaElement.PACKAGE_FRAGMENT_ROOT
                        && res instanceof IProject)) {
                  // NB: attach non-java resource to project (not to its package fragment root)
                  parent = rootInfo.project;
                } else {
                  parent = createElement(res, elementType, rootInfo);
                }
                if (parent == null) {
                  isValidParent = false;
                  continue;
                }
              }
              // add child as non java resource
              try {
                nonJavaResourcesChanged(parent, child);
              } catch (JavaModelException e) {
                // ignore
              }
            } else {
              // the non-java resource (or its parent folder) will be attached to the java project
              if (orphanChildren == null) orphanChildren = new IResourceDelta[length];
              orphanChildren[i] = child;
            }
          } else {
            if (rootInfo == null && childRootInfo == null) {
              // the non-java resource (or its parent folder) will be attached to the java project
              if (orphanChildren == null) orphanChildren = new IResourceDelta[length];
              orphanChildren[i] = child;
            }
          }
        } else {
          oneChildOnClasspath = true; // to avoid reporting child delta as non-java resource delta
        }

        // if child is a nested root
        // or if it is not a package fragment root of the current project
        // but it is a package fragment root of another project, traverse delta too
        if (isNestedRoot || (childRootInfo == null && originalChildRootInfo != null)) {
          traverseDelta(
              child,
              IJavaElement.PACKAGE_FRAGMENT_ROOT,
              originalChildRootInfo,
              null); // binary output of childRootInfo.project cannot be this root
        }

        // if the child is a package fragment root of one or several other projects
        ArrayList rootList;
        if ((rootList = otherRootsInfo(childPath, childKind)) != null) {
          Iterator iterator = rootList.iterator();
          while (iterator.hasNext()) {
            originalChildRootInfo = (RootInfo) iterator.next();
            this.currentElement =
                null; // ensure that 2 roots refering to the same resource don't share the current
            // element (see
            // https://bugs.eclipse.org/bugs/show_bug.cgi?id=210746 )
            traverseDelta(
                child,
                IJavaElement.PACKAGE_FRAGMENT_ROOT,
                originalChildRootInfo,
                null); // binary output of childRootInfo.project cannot be this root
          }
        }
      }
      if (orphanChildren != null
          && (oneChildOnClasspath // orphan children are siblings of a package fragment root
              || res instanceof IProject)) { // non-java resource directly under a project

        //				// attach orphan children
        //				IProject rscProject = res.getProject();
        //				JavaProject adoptiveProject = (JavaProject)JavaCore.create(rscProject);
        //				if (adoptiveProject != null
        //						&& JavaProject.hasJavaNature(rscProject)) { // delta iff Java project (18698)
        //					for (int i = 0; i < length; i++) {
        //						if (orphanChildren[i] != null) {
        //							try {
        //								nonJavaResourcesChanged(adoptiveProject, orphanChildren[i]);
        //							} catch (JavaModelException e) {
        //								// ignore
        //							}
        //						}
        //					}
        //				}
      } // else resource delta will be added by parent
    } // else resource delta will be added by parent
  }

  private void validateClasspaths(IResourceDelta delta, HashSet affectedProjects) {
    //		IResource resource = delta.getResource();
    //		boolean processChildren = false;
    //		switch (resource.getType()) {
    //			case IResource.ROOT :
    //				if (delta.getKind() == IResourceDelta.CHANGED) {
    //					processChildren = true;
    //				}
    //				break;
    //			case IResource.PROJECT :
    //				IProject project = (IProject)resource;
    //				int kind = delta.getKind();
    //				boolean isJavaProject = JavaProject.hasJavaNature(project);
    //				switch (kind) {
    //					case IResourceDelta.ADDED:
    //						processChildren = isJavaProject;
    //						affectedProjects.add(project.getFullPath());
    //						break;
    //					case IResourceDelta.CHANGED:
    //						processChildren = isJavaProject;
    //						if ((delta.getFlags() & IResourceDelta.OPEN) != 0) {
    //							// project opened or closed
    //							if (isJavaProject) {
    //								JavaProject javaProject = (JavaProject)JavaCore.create(project);
    //								this.state.addClasspathValidation(javaProject); // in case .classpath got modified
    // while closed
    //							}
    //							affectedProjects.add(project.getFullPath());
    //						} else if ((delta.getFlags() & IResourceDelta.DESCRIPTION) != 0) {
    //							boolean wasJavaProject = this.state.findJavaProject(project.getName()) != null;
    //							if (wasJavaProject  != isJavaProject) {
    //								// project gained or lost Java nature
    //								JavaProject javaProject = (JavaProject)JavaCore.create(project);
    //								this.state.addClasspathValidation(javaProject); // add/remove classpath markers
    //								affectedProjects.add(project.getFullPath());
    //							}
    //						}
    //						break;
    //					case IResourceDelta.REMOVED:
    //						affectedProjects.add(project.getFullPath());
    //						break;
    //				}
    //				break;
    //			case IResource.FILE :
    //				/* check classpath or prefs files change */
    //				IFile file = (IFile) resource;
    //				String fileName = file.getName();
    //				RootInfo rootInfo = null;
    //				// https://bugs.eclipse.org/bugs/show_bug.cgi?id=229042
    //				// Mark a validation if a library with package fragment root in the project has changed
    //				if (fileName.equals(JavaProject.CLASSPATH_FILENAME)
    //						|| ((rootInfo = rootInfo(file.getFullPath(), delta.getKind())) != null &&
    // rootInfo.entryKind == IClasspathEntry
    // .CPE_LIBRARY)) {
    //					JavaProject javaProject = (JavaProject)JavaCore.create(file.getProject());
    //					this.state.addClasspathValidation(javaProject);
    //					affectedProjects.add(file.getProject().getFullPath());
    //				}
    //				break;
    //		}
    //		if (processChildren) {
    //			IResourceDelta[] children = delta.getAffectedChildren();
    //			for (int i = 0; i < children.length; i++) {
    //				validateClasspaths(children[i], affectedProjects);
    //			}
    //		}
  }

  /*
   * Update the current delta (i.e. add/remove/change the given element) and update the corresponding index.
   * Returns whether the children of the given delta must be processed.
   * @throws a JavaModelException if the delta doesn't correspond to a java element of the given type.
   */
  public boolean updateCurrentDeltaAndIndex(
      IResourceDelta delta, int elementType, RootInfo rootInfo) {
    Openable element;
    switch (delta.getKind()) {
      case IResourceDelta.ADDED:
        IResource deltaRes = delta.getResource();
        element = createElement(deltaRes, elementType, rootInfo);
        if (element == null) {
          // resource might be containing shared roots (see bug 19058)
          this.state.updateRoots(deltaRes.getFullPath(), delta, this);
          return rootInfo != null && rootInfo.inclusionPatterns != null;
        }
        updateIndex(element, delta);
        elementAdded(element, delta, rootInfo);
        //				if (elementType == IJavaElement.PACKAGE_FRAGMENT_ROOT)
        //					this.state.addClasspathValidation(rootInfo.project);
        return elementType == IJavaElement.PACKAGE_FRAGMENT;
      case IResourceDelta.REMOVED:
        deltaRes = delta.getResource();
        element = createElement(deltaRes, elementType, rootInfo);
        if (element == null) {
          // resource might be containing shared roots (see bug 19058)
          this.state.updateRoots(deltaRes.getFullPath(), delta, this);
          return rootInfo != null && rootInfo.inclusionPatterns != null;
        }
        updateIndex(element, delta);
        elementRemoved(element, delta, rootInfo);
        //                if (elementType == IJavaElement.PACKAGE_FRAGMENT_ROOT)
        //					this.state.addClasspathValidation(rootInfo.project);

        //				if (deltaRes.getType() == IResource.PROJECT){
        //					// reset the corresponding project built state, since cannot reuse if added back
        //					if (JavaBuilder.DEBUG)
        //						System.out.println("Clearing last state for removed project : " + deltaRes);
        // //$NON-NLS-1$
        //					this.manager.setLastBuiltState((IProject)deltaRes, null /*no state*/);
        //
        //					// clean up previous session containers (see
        // https://bugs.eclipse.org/bugs/show_bug.cgi?id=89850)
        //					this.manager.previousSessionContainers.remove(element);
        //				}
        return elementType == IJavaElement.PACKAGE_FRAGMENT;
      case IResourceDelta.CHANGED:
        int flags = delta.getFlags();
        if (elementType == IJavaElement.PACKAGE_FRAGMENT_ROOT
            && (flags & IResourceDelta.LOCAL_CHANGED) != 0) {
          // external folder added or removed
          if (oldRootInfo(rootInfo.rootPath, rootInfo.project) == null) {
            // root just added to the classpath
            break;
          }
          deltaRes = delta.getResource();
          Object target =
              JavaModel.getExternalTarget(
                  deltaRes.getFullPath(), true /*check resource existence*/);
          element = createElement(deltaRes, elementType, rootInfo);
          updateIndex(element, delta);
          if (target != null) {
            // external folder added
            elementAdded(element, delta, rootInfo);
          } else {
            // external folder removed
            elementRemoved(element, delta, rootInfo);
          }
          //					this.state.addClasspathValidation(rootInfo.project);
        } else if ((flags & IResourceDelta.CONTENT) != 0
            || (flags & IResourceDelta.ENCODING) != 0) {
          // content or encoding has changed
          element = createElement(delta.getResource(), elementType, rootInfo);
          if (element == null) return false;
          updateIndex(element, delta);
          contentChanged(element);
        } else if (elementType == IJavaElement.JAVA_PROJECT) {
          //					if ((flags & IResourceDelta.OPEN) != 0) {
          //						// project has been opened or closed
          //						IProject res = (IProject)delta.getResource();
          //						element = createElement(res, elementType, rootInfo);
          //						if (element == null) {
          //							// resource might be containing shared roots (see bug 19058)
          //							this.state.updateRoots(res.getFullPath(), delta, this);
          //							return false;
          //						}
          //						if (res.isOpen()) {
          //							if (JavaProject.hasJavaNature(res)) {
          //								addToParentInfo(element);
          //								currentDelta().opened(element);
          //								this.state.updateRoots(element.getPath(), delta, this);
          //
          //								// remember that the project's cache must be reset
          //								this.projectCachesToReset.add(element);
          //
          //								this.manager.indexManager.indexAll(res);
          //							}
          //						} else {
          //							boolean wasJavaProject = this.state.findJavaProject(res.getName()) != null;
          //							if (wasJavaProject) {
          //								close(element);
          //								removeFromParentInfo(element);
          //								currentDelta().closed(element);
          //								this.manager.indexManager.discardJobs(element.getElementName());
          //								this.manager.indexManager.removeIndexFamily(res.getFullPath());
          //							}
          //						}
          //						return false; // when a project is open/closed don't process children
          //					}
          //					if ((flags & IResourceDelta.DESCRIPTION) != 0) {
          //						IProject res = (IProject)delta.getResource();
          //						boolean wasJavaProject = this.state.findJavaProject(res.getName()) != null;
          //						boolean isJavaProject = JavaProject.hasJavaNature(res);
          //						if (wasJavaProject != isJavaProject) {
          //							// project's nature has been added or removed
          //							element = createElement(res, elementType, rootInfo);
          //							if (element == null) return false; // note its resources are still visible as
          // roots to other projects
          //							if (isJavaProject) {
          //								elementAdded(element, delta, rootInfo);
          //								this.manager.indexManager.indexAll(res);
          //							} else {
          //								elementRemoved(element, delta, rootInfo);
          //								this.manager.indexManager.discardJobs(element.getElementName());
          //								this.manager.indexManager.removeIndexFamily(res.getFullPath());
          //								// reset the corresponding project built state, since cannot reuse if added back
          //								if (JavaBuilder.DEBUG)
          //									System.out.println("Clearing last state for project loosing Java nature: " +
          // res); //$NON-NLS-1$
          //								this.manager.setLastBuiltState(res, null /*no state*/);
          //							}
          //							return false; // when a project's nature is added/removed don't process children
          //						}
          //					}
        }
        return true;
    }
    return true;
  }

  private void updateIndex(Openable element, IResourceDelta delta) {

    IndexManager indexManager = this.manager.indexManager;
    if (indexManager == null) return;

    switch (element.getElementType()) {
      case IJavaElement.JAVA_PROJECT:
        switch (delta.getKind()) {
          case IResourceDelta.ADDED:
            indexManager.indexAll(element.getJavaProject().getProject());
            break;
          case IResourceDelta.REMOVED:
            indexManager.removeIndexFamily(element.getJavaProject().getProject().getFullPath());
            // NB: Discarding index jobs belonging to this project was done during PRE_DELETE
            break;
            // NB: Update of index if project is opened, closed, or its java nature is added or
            // removed
            //     is done in updateCurrentDeltaAndIndex
        }
        break;
      case IJavaElement.PACKAGE_FRAGMENT_ROOT:
        if (element instanceof JarPackageFragmentRoot) {
          JarPackageFragmentRoot root = (JarPackageFragmentRoot) element;
          // index jar file only once (if the root is in its declaring project)
          IPath jarPath = root.getPath();
          switch (delta.getKind()) {
            case IResourceDelta.ADDED:
              // index the new jar
              indexManager.indexLibrary(
                  jarPath, root.getJavaProject().getProject(), root.getIndexPath());
              break;
            case IResourceDelta.CHANGED:
              // first remove the index so that it is forced to be re-indexed
              indexManager.removeIndex(jarPath);
              // then index the jar
              indexManager.indexLibrary(
                  jarPath, root.getJavaProject().getProject(), root.getIndexPath());
              break;
            case IResourceDelta.REMOVED:
              // the jar was physically removed: remove the index
              indexManager.discardJobs(jarPath.toString());
              indexManager.removeIndex(jarPath);
              break;
          }
          break;
        }
        int kind = delta.getKind();
        if (kind == IResourceDelta.ADDED
            || kind == IResourceDelta.REMOVED
            || (kind == IResourceDelta.CHANGED
                && (delta.getFlags() & IResourceDelta.LOCAL_CHANGED) != 0)) {
          PackageFragmentRoot root = (PackageFragmentRoot) element;
          updateRootIndex(root, CharOperation.NO_STRINGS, delta);
          break;
        }
        // don't break as packages of the package fragment root can be indexed below
        // $FALL-THROUGH$
      case IJavaElement.PACKAGE_FRAGMENT:
        switch (delta.getKind()) {
          case IResourceDelta.CHANGED:
            if ((delta.getFlags() & IResourceDelta.LOCAL_CHANGED) == 0) break;
            // $FALL-THROUGH$
          case IResourceDelta.ADDED:
          case IResourceDelta.REMOVED:
            IPackageFragment pkg = null;
            if (element instanceof IPackageFragmentRoot) {
              PackageFragmentRoot root = (PackageFragmentRoot) element;
              pkg = root.getPackageFragment(CharOperation.NO_STRINGS);
            } else {
              pkg = (IPackageFragment) element;
            }
            RootInfo rootInfo = rootInfo(pkg.getParent().getPath(), delta.getKind());
            boolean isSource =
                rootInfo == null // if null, defaults to source
                    || rootInfo.entryKind == IClasspathEntry.CPE_SOURCE;
            IResourceDelta[] children = (IResourceDelta[]) delta.getAffectedChildren();
            for (int i = 0, length = children.length; i < length; i++) {
              IResourceDelta child = children[i];
              IResource resource = child.getResource();
              // TODO (philippe) Why do this? Every child is added anyway as the delta is walked
              if (resource instanceof IFile) {
                String name = resource.getName();
                if (isSource) {
                  if (org.eclipse.jdt.internal.core.util.Util.isJavaLikeFileName(name)) {
                    Openable cu = (Openable) pkg.getCompilationUnit(name);
                    updateIndex(cu, child);
                  }
                } else if (org.eclipse.jdt.internal.compiler.util.Util.isClassFileName(name)) {
                  Openable classFile = (Openable) pkg.getClassFile(name);
                  updateIndex(classFile, child);
                }
              }
            }
            break;
        }
        break;
      case IJavaElement.CLASS_FILE:
        //				IFile file = (IFile) delta.getResource();
        //				IJavaProject project = element.getJavaProject();
        //				PackageFragmentRoot root = element.getPackageFragmentRoot();
        //				IPath binaryFolderPath = root.isExternal() && !root.isArchive() ?
        // root.resource().getFullPath() : root.getPath();
        //				// if the class file is part of the binary output, it has been created by
        //				// the java builder -> ignore
        //				try {
        //					if (binaryFolderPath.equals(project.getOutputLocation())) {
        //						break;
        //					}
        //				} catch (JavaModelException e) {
        //					// project doesn't exist: ignore
        //				}
        //				switch (delta.getKind()) {
        //					case IResourceDelta.CHANGED :
        //						// no need to index if the content has not changed
        //						int flags = delta.getFlags();
        //						if ((flags & IResourceDelta.CONTENT) == 0 && (flags & IResourceDelta.ENCODING) == 0)
        //							break;
        //						// $FALL-THROUGH$
        //					case IResourceDelta.ADDED :
        //						indexManager.addBinary(file, binaryFolderPath);
        //						break;
        //					case IResourceDelta.REMOVED :
        //						String containerRelativePath = Util.relativePath(file.getFullPath(),
        // binaryFolderPath.segmentCount());
        //						indexManager.remove(containerRelativePath, binaryFolderPath);
        //						break;
        //				}
        break;
      case IJavaElement.COMPILATION_UNIT:
        IFile file = (IFile) delta.getResource();
        switch (delta.getKind()) {
          case IResourceDelta.CHANGED:
            // no need to index if the content has not changed
            int flags = delta.getFlags();
            if ((flags & IResourceDelta.CONTENT) == 0 && (flags & IResourceDelta.ENCODING) == 0)
              break;
            // $FALL-THROUGH$
          case IResourceDelta.ADDED:
            indexManager.addSource(
                file, element.getJavaProject().getPath(), getSourceElementParser(element));
            // Clean file from secondary types cache but do not update indexing secondary type cache
            // as it will be updated through indexing itself
            this.manager.secondaryTypesRemoving(file, false);
            break;
          case IResourceDelta.REMOVED:
            indexManager.remove(
                Util.relativePath(file.getFullPath(), 1 /*remove project segment*/),
                element.getJavaProject().getPath());
            // Clean file from secondary types cache and update indexing secondary type cache as
            // indexing cannot remove secondary types from cache
            this.manager.secondaryTypesRemoving(file, true);
            break;
        }
    }
  }

  //	/*
  //	 * Validate the classpaths of the projects affected by the given delta.
  //	 * Create markers if necessary.
  //	 * Returns whether cycle markers should be recomputed.
  //	 */
  //	private boolean validateClasspaths(IResourceDelta delta) {
  //		HashSet affectedProjects = new HashSet(5);
  //		validateClasspaths(delta, affectedProjects);
  //		boolean needCycleValidation = false;
  //
  //		// validate classpaths of affected projects (dependent projects
  //		// or projects that reference a library in one of the projects that have changed)
  //		if (!affectedProjects.isEmpty()) {
  //			IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
  //			IProject[] projects = workspaceRoot.getProjects();
  //			int length = projects.length;
  //			for (int i = 0; i < length; i++){
  //				IProject project = projects[i];
  //				JavaProject javaProject = (JavaProject)JavaCore.create(project);
  //				try {
  //					IPath projectPath = project.getFullPath();
  //					IClasspathEntry[] classpath = javaProject.getResolvedClasspath(); // allowed to reuse model
  // cache
  //					for (int j = 0, cpLength = classpath.length; j < cpLength; j++) {
  //						IClasspathEntry entry = classpath[j];
  //						switch (entry.getEntryKind()) {
  //							case IClasspathEntry.CPE_PROJECT:
  //								if (affectedProjects.contains(entry.getPath())) {
  //									this.state.addClasspathValidation(javaProject);
  //									needCycleValidation = true;
  //								}
  //								break;
  //							case IClasspathEntry.CPE_LIBRARY:
  //								IPath entryPath = entry.getPath();
  //								IPath libProjectPath = entryPath.removeLastSegments(entryPath.segmentCount()-1);
  //								if (!libProjectPath.equals(projectPath) // if library contained in another project
  //										&& affectedProjects.contains(libProjectPath)) {
  //									this.state.addClasspathValidation(javaProject);
  //								}
  //								break;
  //						}
  //					}
  //				} catch(JavaModelException e) {
  //						// project no longer exists
  //				}
  //			}
  //		}
  //		return needCycleValidation;
  //	}

  /*
   * Update Java Model given some delta
   */
  public void updateJavaModel(IJavaElementDelta customDelta) {

    if (customDelta == null) {
      for (int i = 0, length = this.javaModelDeltas.size(); i < length; i++) {
        IJavaElementDelta delta = (IJavaElementDelta) this.javaModelDeltas.get(i);
        this.modelUpdater.processJavaDelta(delta);
      }
    } else {
      this.modelUpdater.processJavaDelta(customDelta);
    }
  }

  /*
   * Updates the index of the given root (assuming it's an addition or a removal).
   * This is done recusively, pkg being the current package.
   */
  private void updateRootIndex(PackageFragmentRoot root, String[] pkgName, IResourceDelta delta) {
    Openable pkg = root.getPackageFragment(pkgName);
    updateIndex(pkg, delta);
    IResourceDelta[] children = (IResourceDelta[]) delta.getAffectedChildren();
    for (int i = 0, length = children.length; i < length; i++) {
      IResourceDelta child = children[i];
      IResource resource = child.getResource();
      if (resource instanceof IFolder) {
        String[] subpkgName = Util.arrayConcat(pkgName, resource.getName());
        updateRootIndex(root, subpkgName, child);
      }
    }
  }

  /*
   * An object to hold information about a project's output folders (where .class files are generated).
   */
  static class OutputsInfo {
    int outputCount;
    IPath[] paths;
    int[] traverseModes;

    OutputsInfo(IPath[] paths, int[] traverseModes, int outputCount) {
      this.paths = paths;
      this.traverseModes = traverseModes;
      this.outputCount = outputCount;
    }

    public String toString() {
      if (this.paths == null) return "<none>"; // $NON-NLS-1$
      StringBuffer buffer = new StringBuffer();
      for (int i = 0; i < this.outputCount; i++) {
        buffer.append("path="); // $NON-NLS-1$
        buffer.append(this.paths[i].toString());
        buffer.append("\n->traverse="); // $NON-NLS-1$
        switch (this.traverseModes[i]) {
          case BINARY:
            buffer.append("BINARY"); // $NON-NLS-1$
            break;
          case IGNORE:
            buffer.append("IGNORE"); // $NON-NLS-1$
            break;
          case SOURCE:
            buffer.append("SOURCE"); // $NON-NLS-1$
            break;
          default:
            buffer.append("<unknown>"); // $NON-NLS-1$
        }
        if (i + 1 < this.outputCount) {
          buffer.append('\n');
        }
      }
      return buffer.toString();
    }
  }

  /*
   * An object to hold information about IPackageFragmentRoots (which correspond to
   * individual classpath entry items, e.g., a java/javatests source root or library
   * archive jar.)
   */
  public static class RootInfo {
    public final JavaProject project;
    final char[][] inclusionPatterns;
    final char[][] exclusionPatterns;
    final IPath rootPath;
    final int entryKind;
    IPackageFragmentRoot root;
    IPackageFragmentRoot cache;

    RootInfo(
        JavaProject project,
        IPath rootPath,
        char[][] inclusionPatterns,
        char[][] exclusionPatterns,
        int entryKind) {
      this.project = project;
      this.rootPath = rootPath;
      this.inclusionPatterns = inclusionPatterns;
      this.exclusionPatterns = exclusionPatterns;
      this.entryKind = entryKind;
      this.cache = getPackageFragmentRoot();
    }

    public IPackageFragmentRoot getPackageFragmentRoot() {
      IPackageFragmentRoot tRoot = null;
      Object target = JavaModel.getTarget(this.rootPath, false /*don't check existence*/);
      if (target instanceof IResource) {
        tRoot = this.project.getPackageFragmentRoot((IResource) target);
      } else {
        tRoot = this.project.getPackageFragmentRoot(this.rootPath.toOSString());
      }
      return tRoot;
    }

    public IPackageFragmentRoot getPackageFragmentRoot(IResource resource) {
      if (this.root == null) {
        if (resource != null) {
          this.root = this.project.getPackageFragmentRoot(resource);
        } else {
          this.root = getPackageFragmentRoot();
        }
      }
      if (this.root != null) this.cache = this.root;
      return this.root;
    }

    boolean isRootOfProject(IPath path) {
      return this.rootPath.equals(path) && this.project.getProject().getFullPath().isPrefixOf(path);
    }

    public String toString() {
      StringBuffer buffer = new StringBuffer("project="); // $NON-NLS-1$
      if (this.project == null) {
        buffer.append("null"); // $NON-NLS-1$
      } else {
        buffer.append(this.project.getElementName());
      }
      buffer.append("\npath="); // $NON-NLS-1$
      if (this.rootPath == null) {
        buffer.append("null"); // $NON-NLS-1$
      } else {
        buffer.append(this.rootPath.toString());
      }
      buffer.append("\nincluding="); // $NON-NLS-1$
      if (this.inclusionPatterns == null) {
        buffer.append("null"); // $NON-NLS-1$
      } else {
        for (int i = 0, length = this.inclusionPatterns.length; i < length; i++) {
          buffer.append(new String(this.inclusionPatterns[i]));
          if (i < length - 1) {
            buffer.append("|"); // $NON-NLS-1$
          }
        }
      }
      buffer.append("\nexcluding="); // $NON-NLS-1$
      if (this.exclusionPatterns == null) {
        buffer.append("null"); // $NON-NLS-1$
      } else {
        for (int i = 0, length = this.exclusionPatterns.length; i < length; i++) {
          buffer.append(new String(this.exclusionPatterns[i]));
          if (i < length - 1) {
            buffer.append("|"); // $NON-NLS-1$
          }
        }
      }
      return buffer.toString();
    }
  }
}
