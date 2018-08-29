/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2013 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.core.search;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.compiler.env.AccessRuleSet;
import org.eclipse.jdt.internal.core.ClasspathEntry;
import org.eclipse.jdt.internal.core.ExternalFoldersManager;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.JavaModel;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.PackageFragment;
import org.eclipse.jdt.internal.core.PackageFragmentRoot;
import org.eclipse.jdt.internal.core.util.Util;

/** A Java-specific scope for searching relative to one or more java elements. */
@SuppressWarnings({"rawtypes", "unchecked"})
public class JavaSearchScope extends AbstractJavaSearchScope {

  private ArrayList elements;

  /* The paths of the resources in this search scope
      (or the classpath entries' paths if the resources are projects)
  */
  private ArrayList projectPaths = new ArrayList(); // container paths projects
  private int[] projectIndexes; // Indexes of projects in list
  private String[]
      containerPaths; // path to the container (e.g. /P/src, /P/lib.jar, c:\temp\mylib.jar)
  private String[] relativePaths; // path relative to the container (e.g. x/y/Z.class, x/y, (empty))
  private boolean[]
      isPkgPath; // in the case of packages, matches must be direct children of the folder
  protected AccessRuleSet[] pathRestrictions;
  private int pathsCount;
  private int threshold;

  private IPath[] enclosingProjectsAndJars;
  public static final AccessRuleSet NOT_ENCLOSED = new AccessRuleSet(null, (byte) 0, null);

  public JavaSearchScope() {
    this(5);
  }

  private JavaSearchScope(int size) {
    initialize(size);

    // disabled for now as this could be expensive
    // JavaModelManager.getJavaModelManager().rememberScope(this);
  }

  private void addEnclosingProjectOrJar(IPath path) {
    int length = this.enclosingProjectsAndJars.length;
    for (int i = 0; i < length; i++) {
      if (this.enclosingProjectsAndJars[i].equals(path)) return;
    }
    System.arraycopy(
        this.enclosingProjectsAndJars,
        0,
        this.enclosingProjectsAndJars = new IPath[length + 1],
        0,
        length);
    this.enclosingProjectsAndJars[length] = path;
  }

  /**
   * Add java project all fragment roots to current java search scope.
   *
   * @see #add(JavaProject, IPath, int, HashSet, HashSet, IClasspathEntry)
   */
  public void add(JavaProject project, int includeMask, HashSet projectsToBeAdded)
      throws JavaModelException {
    add(project, null, includeMask, projectsToBeAdded, new HashSet(2), null);
  }

  /**
   * Add a path to current java search scope or all project fragment roots if null. Use project
   * resolved classpath to retrieve and store access restriction on each classpath entry. Recurse if
   * dependent projects are found.
   *
   * @param javaProject Project used to get resolved classpath entries
   * @param pathToAdd Path to add in case of single element or null if user want to add all project
   *     package fragment roots
   * @param includeMask Mask to apply on classpath entries
   * @param projectsToBeAdded Set to avoid infinite recursion
   * @param visitedProjects Set to avoid adding twice the same project
   * @param referringEntry Project raw entry in referring project classpath
   * @throws JavaModelException May happen while getting java model info
   */
  void add(
      JavaProject javaProject,
      IPath pathToAdd,
      int includeMask,
      HashSet projectsToBeAdded,
      HashSet visitedProjects,
      IClasspathEntry referringEntry)
      throws JavaModelException {
    IProject project = javaProject.getProject();
    if (!project.isAccessible() || !visitedProjects.add(project)) return;

    IPath projectPath = project.getFullPath();
    String projectPathString = projectPath.toString();
    addEnclosingProjectOrJar(projectPath);

    IClasspathEntry[] entries = javaProject.getResolvedClasspath();
    IJavaModel model = javaProject.getJavaModel();
    JavaModelManager.PerProjectInfo perProjectInfo = javaProject.getPerProjectInfo();
    for (int i = 0, length = entries.length; i < length; i++) {
      IClasspathEntry entry = entries[i];
      AccessRuleSet access = null;
      ClasspathEntry cpEntry = (ClasspathEntry) entry;
      if (referringEntry != null) {
        // Add only exported entries.
        // Source folder are implicitly exported.
        if (!entry.isExported() && entry.getEntryKind() != IClasspathEntry.CPE_SOURCE) {
          continue;
        }
        cpEntry = cpEntry.combineWith((ClasspathEntry) referringEntry);
        //				cpEntry = ((ClasspathEntry)referringEntry).combineWith(cpEntry);
      }
      access = cpEntry.getAccessRuleSet();
      switch (entry.getEntryKind()) {
        case IClasspathEntry.CPE_LIBRARY:
          IClasspathEntry rawEntry = null;
          Map rootPathToRawEntries = perProjectInfo.rootPathToRawEntries;
          if (rootPathToRawEntries != null) {
            rawEntry = (IClasspathEntry) rootPathToRawEntries.get(entry.getPath());
          }
          if (rawEntry == null) break;
          rawKind:
          switch (rawEntry.getEntryKind()) {
            case IClasspathEntry.CPE_LIBRARY:
            case IClasspathEntry.CPE_VARIABLE:
              if ((includeMask & APPLICATION_LIBRARIES) != 0) {
                IPath path = entry.getPath();
                if (pathToAdd == null || pathToAdd.equals(path)) {
                  Object target = JavaModel.getTarget(path, false /*don't check existence*/);
                  if (target instanceof IFolder) // case of an external folder
                  path = ((IFolder) target).getFullPath();
                  String pathToString =
                      path.getDevice() == null ? path.toString() : path.toOSString();
                  add(
                      projectPath.toString(),
                      "",
                      pathToString,
                      false /*not a package*/,
                      access); // $NON-NLS-1$
                  addEnclosingProjectOrJar(entry.getPath());
                }
              }
              break;
            case IClasspathEntry.CPE_CONTAINER:
              IClasspathContainer container =
                  JavaCore.getClasspathContainer(rawEntry.getPath(), javaProject);
              if (container == null) break;
              switch (container.getKind()) {
                case IClasspathContainer.K_APPLICATION:
                  if ((includeMask & APPLICATION_LIBRARIES) == 0) break rawKind;
                  break;
                case IClasspathContainer.K_SYSTEM:
                case IClasspathContainer.K_DEFAULT_SYSTEM:
                  if ((includeMask & SYSTEM_LIBRARIES) == 0) break rawKind;
                  break;
                default:
                  break rawKind;
              }
              IPath path = entry.getPath();
              if (pathToAdd == null || pathToAdd.equals(path)) {
                Object target = JavaModel.getTarget(path, false /*don't check existence*/);
                if (target instanceof IFolder) // case of an external folder
                path = ((IFolder) target).getFullPath();
                String pathToString =
                    path.getDevice() == null ? path.toString() : path.toOSString();
                add(
                    projectPath.toString(),
                    "",
                    pathToString,
                    false /*not a package*/,
                    access); // $NON-NLS-1$
                addEnclosingProjectOrJar(entry.getPath());
              }
              break;
          }
          break;
        case IClasspathEntry.CPE_PROJECT:
          if ((includeMask & REFERENCED_PROJECTS) != 0) {
            IPath path = entry.getPath();
            if (pathToAdd == null || pathToAdd.equals(path)) {
              JavaProject referencedProject = (JavaProject) model.getJavaProject(path.toOSString());
              if (!projectsToBeAdded.contains(
                  referencedProject)) { // do not recurse if depending project was used to create
                // the scope
                add(
                    referencedProject,
                    null,
                    includeMask,
                    projectsToBeAdded,
                    visitedProjects,
                    cpEntry);
              }
            }
          }
          break;
        case IClasspathEntry.CPE_SOURCE:
          if ((includeMask & SOURCES) != 0) {
            IPath path = entry.getPath();
            if (pathToAdd == null || pathToAdd.equals(path)) {
              add(
                  projectPath.toString(),
                  Util.relativePath(path, projectPath.segmentCount() /*remove project segment*/),
                  projectPathString,
                  false /*not a package*/,
                  access);
            }
          }
          break;
      }
    }
  }

  /**
   * Add an element to the java search scope.
   *
   * @param element The element we want to add to current java search scope
   * @throws JavaModelException May happen if some Java Model info are not available
   */
  public void add(IJavaElement element) throws JavaModelException {
    IPath containerPath = null;
    String containerPathToString = null;
    PackageFragmentRoot root = null;
    int includeMask = SOURCES | APPLICATION_LIBRARIES | SYSTEM_LIBRARIES;
    switch (element.getElementType()) {
      case IJavaElement.JAVA_MODEL:
        // a workspace sope should be used
        break;
      case IJavaElement.JAVA_PROJECT:
        add((JavaProject) element, null, includeMask, new HashSet(2), new HashSet(2), null);
        break;
      case IJavaElement.PACKAGE_FRAGMENT_ROOT:
        root = (PackageFragmentRoot) element;
        IPath rootPath = root.internalPath();
        containerPath =
            root.getKind() == IPackageFragmentRoot.K_SOURCE ? root.getParent().getPath() : rootPath;
        containerPathToString =
            containerPath.getDevice() == null
                ? containerPath.toString()
                : containerPath.toOSString();
        IResource rootResource = root.resource();
        String projectPath = root.getJavaProject().getPath().toString();
        if (rootResource != null && rootResource.isAccessible()) {
          String relativePath =
              Util.relativePath(rootResource.getFullPath(), containerPath.segmentCount());
          add(projectPath, relativePath, containerPathToString, false /*not a package*/, null);
        } else {
          add(projectPath, "", containerPathToString, false /*not a package*/, null); // $NON-NLS-1$
        }
        break;
      case IJavaElement.PACKAGE_FRAGMENT:
        root = (PackageFragmentRoot) element.getParent();
        projectPath = root.getJavaProject().getPath().toString();
        if (root.isArchive()) {
          String relativePath = Util.concatWith(((PackageFragment) element).names, '/');
          containerPath = root.getPath();
          containerPathToString =
              containerPath.getDevice() == null
                  ? containerPath.toString()
                  : containerPath.toOSString();
          add(projectPath, relativePath, containerPathToString, true /*package*/, null);
        } else {
          IResource resource = ((JavaElement) element).resource();
          if (resource != null) {
            if (resource.isAccessible()) {
              containerPath =
                  root.getKind() == IPackageFragmentRoot.K_SOURCE
                      ? root.getParent().getPath()
                      : root.internalPath();
            } else {
              // for working copies, get resource container full path
              containerPath = resource.getParent().getFullPath();
            }
            containerPathToString =
                containerPath.getDevice() == null
                    ? containerPath.toString()
                    : containerPath.toOSString();
            String relativePath =
                Util.relativePath(resource.getFullPath(), containerPath.segmentCount());
            add(projectPath, relativePath, containerPathToString, true /*package*/, null);
          }
        }
        break;
      default:
        // remember sub-cu (or sub-class file) java elements
        if (element instanceof IMember) {
          if (this.elements == null) {
            this.elements = new ArrayList();
          }
          this.elements.add(element);
        }
        root = (PackageFragmentRoot) element.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
        projectPath = root.getJavaProject().getPath().toString();
        String relativePath;
        if (root.getKind() == IPackageFragmentRoot.K_SOURCE) {
          containerPath = root.getParent().getPath();
          relativePath =
              Util.relativePath(
                  getPath(element, false /*full path*/),
                  root.getJavaProject().getPath().segmentCount() /*remove project segment*/);
        } else {
          containerPath = root.internalPath();
          relativePath = getPath(element, true /*relative path*/).toString();
        }
        containerPathToString =
            containerPath.getDevice() == null
                ? containerPath.toString()
                : containerPath.toOSString();
        add(projectPath, relativePath, containerPathToString, false /*not a package*/, null);
    }

    if (root != null)
      addEnclosingProjectOrJar(
          root.getKind() == IPackageFragmentRoot.K_SOURCE
              ? root.getParent().getPath()
              : root.getPath());
  }

  /**
   * Adds the given path to this search scope. Remember if subfolders need to be included and
   * associated access restriction as well.
   */
  private void add(
      String projectPath,
      String relativePath,
      String containerPath,
      boolean isPackage,
      AccessRuleSet access) {
    // normalize containerPath and relativePath
    containerPath = normalize(containerPath);
    relativePath = normalize(relativePath);
    int length = this.containerPaths.length,
        index = (containerPath.hashCode() & 0x7FFFFFFF) % length;
    String currentRelativePath, currentContainerPath;
    while ((currentRelativePath = this.relativePaths[index]) != null
        && (currentContainerPath = this.containerPaths[index]) != null) {
      if (currentRelativePath.equals(relativePath) && currentContainerPath.equals(containerPath))
        return;
      if (++index == length) {
        index = 0;
      }
    }
    int idx = this.projectPaths.indexOf(projectPath);
    if (idx == -1) {
      // store project in separated list to minimize memory footprint
      this.projectPaths.add(projectPath);
      idx = this.projectPaths.indexOf(projectPath);
    }
    this.projectIndexes[index] = idx;
    this.relativePaths[index] = relativePath;
    this.containerPaths[index] = containerPath;
    this.isPkgPath[index] = isPackage;
    if (this.pathRestrictions != null) this.pathRestrictions[index] = access;
    else if (access != null) {
      this.pathRestrictions = new AccessRuleSet[this.relativePaths.length];
      this.pathRestrictions[index] = access;
    }

    // assumes the threshold is never equal to the size of the table
    if (++this.pathsCount > this.threshold) rehash();
  }

  /*
   * E.g.
   *
   * 1. /P/src/pkg/X.java
   * 2. /P/src/pkg
   * 3. /P/lib.jar|org/eclipse/jdt/core/IJavaElement.class
   * 4. /home/mylib.jar|x/y/z/X.class
   * 5. c:\temp\mylib.jar|x/y/Y.class
   *
   * @see IJavaSearchScope#encloses(String)
   */
  public boolean encloses(String resourcePathString) {
    int separatorIndex = resourcePathString.indexOf(JAR_FILE_ENTRY_SEPARATOR);
    if (separatorIndex != -1) {
      // internal or external jar (case 3, 4, or 5)
      String jarPath = resourcePathString.substring(0, separatorIndex);
      String relativePath = resourcePathString.substring(separatorIndex + 1);
      return indexOf(jarPath, relativePath) >= 0;
    }
    // resource in workspace (case 1 or 2)
    return indexOf(resourcePathString) >= 0;
  }

  /**
   * Returns paths list index of given path or -1 if not found. NOTE: Use indexOf(String, String)
   * for path inside jars
   *
   * @param fullPath the full path of the resource, e.g. 1. /P/src/pkg/X.java 2. /P/src/pkg
   */
  private int indexOf(String fullPath) {
    // cannot guess the index of the container path
    // fallback to sequentially looking at all known paths
    for (int i = 0, length = this.relativePaths.length; i < length; i++) {
      String currentRelativePath = this.relativePaths[i];
      if (currentRelativePath == null) continue;
      String currentContainerPath = this.containerPaths[i];
      String currentFullPath =
          currentRelativePath.length() == 0
              ? currentContainerPath
              : (currentContainerPath + '/' + currentRelativePath);
      if (encloses(currentFullPath, fullPath, i)) return i;
    }
    return -1;
  }

  /**
   * Returns paths list index of given path or -1 if not found.
   *
   * @param containerPath the path of the container, e.g. 1. /P/src 2. /P 3. /P/lib.jar 4.
   *     /home/mylib.jar 5. c:\temp\mylib.jar
   * @param relativePath the forward slash path relatively to the container, e.g. 1. x/y/Z.class 2.
   *     x/y 3. X.java 4. (empty)
   */
  private int indexOf(String containerPath, String relativePath) {
    // use the hash to get faster comparison
    int length = this.containerPaths.length,
        index = (containerPath.hashCode() & 0x7FFFFFFF) % length;
    String currentContainerPath;
    while ((currentContainerPath = this.containerPaths[index]) != null) {
      if (currentContainerPath.equals(containerPath)) {
        String currentRelativePath = this.relativePaths[index];
        if (encloses(currentRelativePath, relativePath, index)) return index;
      }
      if (++index == length) {
        index = 0;
      }
    }
    return -1;
  }

  /*
   * Returns whether the enclosing path encloses the given path (or is equal to it)
   */
  private boolean encloses(String enclosingPath, String path, int index) {
    // normalize given path as it can come from outside
    path = normalize(path);

    int pathLength = path.length();
    int enclosingLength = enclosingPath.length();
    if (pathLength < enclosingLength) {
      return false;
    }
    if (enclosingLength == 0) {
      return true;
    }
    if (pathLength == enclosingLength) {
      return path.equals(enclosingPath);
    }
    if (!this.isPkgPath[index]) {
      return path.startsWith(enclosingPath) && path.charAt(enclosingLength) == '/';
    } else {
      // if looking at a package, this scope encloses the given path
      // if the given path is a direct child of the folder
      // or if the given path path is the folder path (see bug 13919 Declaration for package not
      // found if scope is not project)
      if (path.startsWith(enclosingPath)
          && ((enclosingPath.length() == path.lastIndexOf('/'))
              || (enclosingPath.length() == path.length()))) {
        return true;
      }
    }
    return false;
  }

  /* (non-Javadoc)
   * @see IJavaSearchScope#encloses(IJavaElement)
   */
  public boolean encloses(IJavaElement element) {
    if (this.elements != null) {
      for (int i = 0, length = this.elements.size(); i < length; i++) {
        IJavaElement scopeElement = (IJavaElement) this.elements.get(i);
        IJavaElement searchedElement = element;
        while (searchedElement != null) {
          if (searchedElement.equals(scopeElement)) return true;
          searchedElement = searchedElement.getParent();
        }
      }
      return false;
    }
    IPackageFragmentRoot root =
        (IPackageFragmentRoot) element.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
    if (root != null && root.isArchive()) {
      // external or internal jar
      IPath rootPath = root.getPath();
      String rootPathToString =
          rootPath.getDevice() == null ? rootPath.toString() : rootPath.toOSString();
      IPath relativePath = getPath(element, true /*relative path*/);
      return indexOf(rootPathToString, relativePath.toString()) >= 0;
    }
    // resource in workspace
    String fullResourcePathString = getPath(element, false /*full path*/).toString();
    return indexOf(fullResourcePathString) >= 0;
  }

  /* (non-Javadoc)
   * @see IJavaSearchScope#enclosingProjectsAndJars()
   */
  public IPath[] enclosingProjectsAndJars() {
    return this.enclosingProjectsAndJars;
  }

  private IPath getPath(IJavaElement element, boolean relativeToRoot) {
    switch (element.getElementType()) {
      case IJavaElement.JAVA_MODEL:
        return Path.EMPTY;
      case IJavaElement.JAVA_PROJECT:
        return element.getPath();
      case IJavaElement.PACKAGE_FRAGMENT_ROOT:
        if (relativeToRoot) return Path.EMPTY;
        return element.getPath();
      case IJavaElement.PACKAGE_FRAGMENT:
        String relativePath = Util.concatWith(((PackageFragment) element).names, '/');
        return getPath(element.getParent(), relativeToRoot).append(new Path(relativePath));
      case IJavaElement.COMPILATION_UNIT:
      case IJavaElement.CLASS_FILE:
        return getPath(element.getParent(), relativeToRoot)
            .append(new Path(element.getElementName()));
      default:
        return getPath(element.getParent(), relativeToRoot);
    }
  }

  /**
   * Get access rule set corresponding to a given path.
   *
   * @param relativePath The path user want to have restriction access
   * @return The access rule set for given path or null if none is set for it. Returns specific
   *     uninit access rule set when scope does not enclose the given path.
   */
  public AccessRuleSet getAccessRuleSet(String relativePath, String containerPath) {
    int index = indexOf(containerPath, relativePath);
    if (index == -1) {
      // this search scope does not enclose given path
      return NOT_ENCLOSED;
    }
    if (this.pathRestrictions == null) return null;
    return this.pathRestrictions[index];
  }

  protected void initialize(int size) {
    this.pathsCount = 0;
    this.threshold = size; // size represents the expected number of elements
    int extraRoom = (int) (size * 1.75f);
    if (this.threshold == extraRoom) extraRoom++;
    this.relativePaths = new String[extraRoom];
    this.containerPaths = new String[extraRoom];
    this.projectPaths = new ArrayList();
    this.projectIndexes = new int[extraRoom];
    this.isPkgPath = new boolean[extraRoom];
    this.pathRestrictions = null; // null to optimize case where no access rules are used

    this.enclosingProjectsAndJars = new IPath[0];
  }

  /*
   * Removes trailing slashes from the given path
   */
  private String normalize(String path) {
    int pathLength = path.length();
    int index = pathLength - 1;
    while (index >= 0 && path.charAt(index) == '/') index--;
    if (index != pathLength - 1) return path.substring(0, index + 1);
    return path;
  }

  /*
   * @see AbstractSearchScope#processDelta(IJavaElementDelta)
   */
  public void processDelta(IJavaElementDelta delta, int eventType) {
    switch (delta.getKind()) {
      case IJavaElementDelta.CHANGED:
        IJavaElementDelta[] children = delta.getAffectedChildren();
        for (int i = 0, length = children.length; i < length; i++) {
          IJavaElementDelta child = children[i];
          processDelta(child, eventType);
        }
        break;
      case IJavaElementDelta.REMOVED:
        IJavaElement element = delta.getElement();
        if (this.encloses(element)) {
          if (this.elements != null) {
            this.elements.remove(element);
          }
          String path = null;
          switch (element.getElementType()) {
            case IJavaElement.JAVA_PROJECT:
              path = ((IJavaProject) element).getProject().getFullPath().toString();
              break;
            case IJavaElement.PACKAGE_FRAGMENT_ROOT:
              path = ((IPackageFragmentRoot) element).getPath().toString();
              break;
            default:
              return;
          }
          for (int i = 0; i < this.pathsCount; i++) {
            if (this.relativePaths[i].equals(path)) {
              this.relativePaths[i] = null;
              rehash();
              break;
            }
          }
        }
        break;
    }
  }

  /** @see AbstractJavaSearchScope#packageFragmentRoot(String, int, String) */
  public IPackageFragmentRoot packageFragmentRoot(
      String resourcePathString, int jarSeparatorIndex, String jarPath) {
    int index = -1;
    boolean isJarFile = jarSeparatorIndex != -1;
    if (isJarFile) {
      // internal or external jar (case 3, 4, or 5)
      String relativePath = resourcePathString.substring(jarSeparatorIndex + 1);
      index = indexOf(jarPath, relativePath);
    } else {
      // resource in workspace (case 1 or 2)
      index = indexOf(resourcePathString);
    }
    if (index >= 0) {
      int idx = this.projectIndexes[index];
      String projectPath = idx == -1 ? null : (String) this.projectPaths.get(idx);
      if (projectPath != null) {
        IJavaProject project =
            JavaCore.create(ResourcesPlugin.getWorkspace().getRoot().getProject(projectPath));
        if (isJarFile) {
          IResource resource = JavaModel.getWorkspaceTarget(new Path(jarPath));
          if (resource != null) return project.getPackageFragmentRoot(resource);
          return project.getPackageFragmentRoot(jarPath);
        }
        Object target =
            JavaModel.getWorkspaceTarget(
                new Path(this.containerPaths[index] + '/' + this.relativePaths[index]));
        if (target != null) {
          if (target instanceof IProject) {
            return project.getPackageFragmentRoot((IProject) target);
          }
          IJavaElement element = JavaModelManager.create((IResource) target, project);
          return (IPackageFragmentRoot) element.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
        }
      }
    }
    return null;
  }

  private void rehash() {
    JavaSearchScope newScope =
        new JavaSearchScope(this.pathsCount * 2); // double the number of expected elements
    newScope.projectPaths.ensureCapacity(this.projectPaths.size());
    String currentPath;
    for (int i = 0, length = this.relativePaths.length; i < length; i++)
      if ((currentPath = this.relativePaths[i]) != null) {
        int idx = this.projectIndexes[i];
        String projectPath = idx == -1 ? null : (String) this.projectPaths.get(idx);
        newScope.add(
            projectPath,
            currentPath,
            this.containerPaths[i],
            this.isPkgPath[i],
            this.pathRestrictions == null ? null : this.pathRestrictions[i]);
      }

    this.relativePaths = newScope.relativePaths;
    this.containerPaths = newScope.containerPaths;
    this.projectPaths = newScope.projectPaths;
    this.projectIndexes = newScope.projectIndexes;
    this.isPkgPath = newScope.isPkgPath;
    this.pathRestrictions = newScope.pathRestrictions;
    this.threshold = newScope.threshold;
  }

  public String toString() {
    StringBuffer result = new StringBuffer("JavaSearchScope on "); // $NON-NLS-1$
    if (this.elements != null) {
      result.append("["); // $NON-NLS-1$
      for (int i = 0, length = this.elements.size(); i < length; i++) {
        JavaElement element = (JavaElement) this.elements.get(i);
        result.append("\n\t"); // $NON-NLS-1$
        result.append(element.toStringWithAncestors());
      }
      result.append("\n]"); // $NON-NLS-1$
    } else {
      if (this.pathsCount == 0) {
        result.append("[empty scope]"); // $NON-NLS-1$
      } else {
        result.append("["); // $NON-NLS-1$
        String[] paths = new String[this.relativePaths.length];
        int index = 0;
        for (int i = 0; i < this.relativePaths.length; i++) {
          String path = this.relativePaths[i];
          if (path == null) continue;
          String containerPath;
          if (ExternalFoldersManager.isInternalPathForExternalFolder(
              new Path(this.containerPaths[i]))) {
            Object target = JavaModel.getWorkspaceTarget(new Path(this.containerPaths[i]));
            containerPath = ((IFolder) target).getLocation().toOSString();
          } else {
            containerPath = this.containerPaths[i];
          }
          if (path.length() > 0) {
            paths[index++] = containerPath + '/' + path;
          } else {
            paths[index++] = containerPath;
          }
        }
        System.arraycopy(paths, 0, paths = new String[index], 0, index);
        Util.sort(paths);
        for (int i = 0; i < index; i++) {
          result.append("\n\t"); // $NON-NLS-1$
          result.append(paths[i]);
        }
        result.append("\n]"); // $NON-NLS-1$
      }
    }
    return result.toString();
  }
}
