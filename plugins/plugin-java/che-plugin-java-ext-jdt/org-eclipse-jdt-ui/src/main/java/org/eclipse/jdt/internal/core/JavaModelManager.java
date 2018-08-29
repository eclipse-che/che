/**
 * ***************************************************************************** Copyright (c)
 * 2012-2015 Red Hat, Inc. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: Red Hat, Inc. - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.core;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import org.eclipse.che.jdt.core.launching.JREContainerInitializer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jdt.core.ClasspathContainerInitializer;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModelStatus;
import org.eclipse.jdt.core.IJavaModelStatusConstants;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IParent;
import org.eclipse.jdt.core.IProblemRequestor;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jdt.internal.codeassist.impl.AssistOptions;
import org.eclipse.jdt.internal.compiler.env.AccessRestriction;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.core.builder.JavaBuilder;
import org.eclipse.jdt.internal.core.search.BasicSearchEngine;
import org.eclipse.jdt.internal.core.search.IRestrictedAccessTypeRequestor;
import org.eclipse.jdt.internal.core.search.JavaWorkspaceScope;
import org.eclipse.jdt.internal.core.search.indexing.IndexManager;
import org.eclipse.jdt.internal.core.util.HashtableOfArrayToObject;
import org.eclipse.jdt.internal.core.util.LRUCache;
import org.eclipse.jdt.internal.core.util.Messages;
import org.eclipse.jdt.internal.core.util.Util;
import org.eclipse.jdt.internal.core.util.WeakHashSet;
import org.eclipse.jdt.internal.core.util.WeakHashSetOfCharArray;

/** @author Evgen Vidolob */
public class JavaModelManager {
  private static Map<String, String> defaultOptions = new HashMap<>();

  static {
    defaultOptions.put(JavaCore.COMPILER_COMPLIANCE, org.eclipse.jdt.core.JavaCore.VERSION_1_8);
    defaultOptions.put(
        CompilerOptions.OPTION_TargetPlatform, org.eclipse.jdt.core.JavaCore.VERSION_1_8);
    defaultOptions.put(JavaCore.COMPILER_SOURCE, org.eclipse.jdt.core.JavaCore.VERSION_1_8);
    defaultOptions.put(AssistOptions.OPTION_PerformVisibilityCheck, AssistOptions.ENABLED);
    defaultOptions.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.WARNING);
    defaultOptions.put(CompilerOptions.OPTION_TaskTags, CompilerOptions.WARNING);
    defaultOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.WARNING);
    defaultOptions.put(CompilerOptions.OPTION_SuppressWarnings, CompilerOptions.ENABLED);
    defaultOptions.put(JavaCore.COMPILER_TASK_TAGS, "TODO,FIXME,XXX");
    defaultOptions.put(
        org.eclipse.jdt.core.JavaCore.COMPILER_TASK_PRIORITIES,
        org.eclipse.jdt.core.JavaCore.DEFAULT_TASK_PRIORITIES);
    defaultOptions.put(
        org.eclipse.jdt.core.JavaCore.COMPILER_PB_UNUSED_PARAMETER_INCLUDE_DOC_COMMENT_REFERENCE,
        org.eclipse.jdt.core.JavaCore.ENABLED);
    defaultOptions.put(
        org.eclipse.jdt.core.JavaCore.COMPILER_DOC_COMMENT_SUPPORT,
        org.eclipse.jdt.core.JavaCore.ENABLED);
    defaultOptions.put(
        org.eclipse.jdt.core.JavaCore.COMPILER_ANNOTATION_NULL_ANALYSIS,
        org.eclipse.jdt.core.JavaCore.ENABLED);
    defaultOptions.put(
        CompilerOptions.OPTION_Process_Annotations, org.eclipse.jdt.core.JavaCore.ENABLED);
    defaultOptions.put(
        CompilerOptions.OPTION_GenerateClassFiles, org.eclipse.jdt.core.JavaCore.ENABLED);
    defaultOptions.put(JavaCore.CODEASSIST_FIELD_PREFIXES, "");
    defaultOptions.put(JavaCore.CODEASSIST_FIELD_SUFFIXES, "");
    defaultOptions.put(JavaCore.CODEASSIST_STATIC_FIELD_PREFIXES, "");
    defaultOptions.put(JavaCore.CODEASSIST_STATIC_FIELD_SUFFIXES, "");
    defaultOptions.put(JavaCore.CODEASSIST_ARGUMENT_PREFIXES, "");
    defaultOptions.put(JavaCore.CODEASSIST_ARGUMENT_SUFFIXES, "");
    defaultOptions.put(JavaCore.CODEASSIST_LOCAL_PREFIXES, "");
    defaultOptions.put(JavaCore.CODEASSIST_LOCAL_SUFFIXES, "");
    defaultOptions.put(JavaCore.CODEASSIST_STATIC_FINAL_FIELD_PREFIXES, "");
    defaultOptions.put(JavaCore.CODEASSIST_STATIC_FINAL_FIELD_SUFFIXES, "");
    defaultOptions.put(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, JavaCore.SPACE);
    defaultOptions.put(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, "4");
  }

  public static final String TRUE = "true"; // $NON-NLS-1$
  public static final ICompilationUnit[] NO_WORKING_COPY = new ICompilationUnit[0];
  private static final String INDEXED_SECONDARY_TYPES =
      "#@*_indexing secondary cache_*@#"; // $NON-NLS-1$
  public static boolean VERBOSE = false;
  public static boolean CP_RESOLVE_VERBOSE = false;
  public static boolean CP_RESOLVE_VERBOSE_ADVANCED = false;
  public static boolean CP_RESOLVE_VERBOSE_FAILURE = false;
  public static boolean ZIP_ACCESS_VERBOSE = false;

  // Options
  private static final int UNKNOWN_OPTION = 0;
  private static final int DEPRECATED_OPTION = 1;
  private static final int VALID_OPTION = 2;
  HashSet optionNames = new HashSet(20);
  Map deprecatedOptions = new HashMap();

  /**
   * Name of the JVM parameter to specify whether or not referenced JAR should be resolved for
   * container libraries.
   */
  private static final String RESOLVE_REFERENCED_LIBRARIES_FOR_CONTAINERS =
      "resolveReferencedLibrariesForContainers";
  // $NON-NLS-1$

  public static final IClasspathContainer CONTAINER_INITIALIZATION_IN_PROGRESS =
      new IClasspathContainer() {
        public IClasspathEntry[] getClasspathEntries() {
          return null;
        }

        public String getDescription() {
          return "Container Initialization In Progress";
        } // $NON-NLS-1$

        public int getKind() {
          return 0;
        }

        public IPath getPath() {
          return null;
        }

        public String toString() {
          return getDescription();
        }
      };
  /**
   * A set of java.io.Files used as a cache of external jars that are known to be existing. Note
   * this cache is kept for the whole session.
   */
  public static HashSet<File> existingExternalFiles = new HashSet<>();
  //    /**
  //     * The singleton manager
  //     */
  private static JavaModelManager MANAGER = new JavaModelManager();
  // Non-static, which will give it a chance to retain the default when and if JavaModelManager is
  // restarted.
  boolean resolveReferencedLibrariesForContainers = false;

  /*
   * A HashSet that contains the IJavaProject whose classpath is being resolved.
   */
  private ThreadLocal<HashSet<IJavaProject>> classpathsBeingResolved = new ThreadLocal<>();
  /**
   * A set of external files ({@link #existingExternalFiles}) which have been confirmed as file
   * (i.e. which returns true to {@link File#isFile()}. Note this cache is kept for the whole
   * session.
   */
  public static HashSet<File> existingExternalConfirmedFiles = new HashSet<>();
  /** Unique handle onto the JavaModel */
  final org.eclipse.jdt.internal.core.JavaModel javaModel;
  /* whether an AbortCompilationUnit should be thrown when the source of a compilation unit cannot be retrieved */
  public ThreadLocal abortOnMissingSource = new ThreadLocal();
  public IndexManager indexManager;
  /** Holds the state used for delta processing. */
  public DeltaProcessingState deltaState;
  /*
   * The unique workspace scope
   */
  public JavaWorkspaceScope workspaceScope;
  /** Set of elements which are out of sync with their buffers. */
  protected HashSet elementsOutOfSynchWithBuffers = new HashSet(11);
  /**
   * Table from WorkingCopyOwner to a table of ICompilationUnit (working copy handle) to
   * PerWorkingCopyInfo. NOTE: this object itself is used as a lock to synchronize creation/removal
   * of per working copy infos
   */
  protected Map perWorkingCopyInfos = new HashMap(5);
  /** A weak set of the known search scopes. */
  protected WeakHashMap searchScopes = new WeakHashMap();
  /*
   * A set of IPaths for jars that are known to not contain a chaining (through MANIFEST.MF) to another library
   */
  private Set nonChainingJars;

  /*
   * A set of IPaths for jars that are known to be invalid - such as not being a valid/known format
   */
  private Set invalidArchives;

  /*
   * A set of IPaths for files that are known to be external to the workspace.
   * Need not be referenced by the classpath.
   */
  private Set externalFiles;

  /*
   * A set of IPaths for files that do not exist on the file system but are assumed to be
   * external archives (rather than external folders).
   */
  private Set assumedExternalFiles;
  /**
   * A cache of opened zip files per thread. (for a given thread, the object value is a HashMap from
   * IPath to java.io.ZipFile)
   */
  private ThreadLocal<ZipCache> zipFiles = new ThreadLocal<>();
  /*
   * Temporary cache of newly opened elements
   */
  private ThreadLocal temporaryCache = new ThreadLocal();
  private ThreadLocal containerInitializationInProgress = new ThreadLocal();
  ThreadLocal containersBeingInitialized = new ThreadLocal();
  public Hashtable<String, ClasspathContainerInitializer> containerInitializersCache =
      new Hashtable<>(5);

  /**
   * Table from IProject to PerProjectInfo. NOTE: this object itself is used as a lock to
   * synchronize creation/removal of per project infos
   */
  protected Map<IProject, PerProjectInfo> perProjectInfos = new HashMap<>(5);
  /*
   * Pools of symbols used in the Java model.
   * Used as a replacement for String#intern() that could prevent garbage collection of strings on some VMs.
   */
  private WeakHashSet stringSymbols = new WeakHashSet(5);
  private WeakHashSetOfCharArray charArraySymbols = new WeakHashSetOfCharArray(5);
  public HashMap containers = new HashMap(5);
  public HashMap previousSessionContainers = new HashMap(5);
  public HashMap previousSessionVariables = new HashMap(5);
  /** Infos cache. */
  private JavaModelCache cache;

  private BufferManager DEFAULT_BUFFER_MANAGER;

  Hashtable<String, String> optionsCache;

  public static JavaModelManager getJavaModelManager() {
    return MANAGER;
  }

  public JavaModelManager() {
    // initialize Java model cache
    this.cache = new JavaModelCache();
    optionsCache = new Hashtable<>(defaultOptions);
    javaModel = new org.eclipse.jdt.internal.core.JavaModel();
    this.indexManager = new IndexManager(ResourcesPlugin.getIndexPath());
    deltaState = new DeltaProcessingState(this);
    this.nonChainingJars = new HashSet(); // loadClasspathListCache(NON_CHAINING_JARS_CACHE);
    this.invalidArchives = new HashSet(); // loadClasspathListCache(INVALID_ARCHIVES_CACHE);
    this.externalFiles =
        new CopyOnWriteArraySet<>(); // loadClasspathListCache(EXTERNAL_FILES_CACHE);
    this.assumedExternalFiles =
        new HashSet(); // loadClasspathListCache(ASSUMED_EXTERNAL_FILES_CACHE);
    String includeContainerReferencedLib =
        System.getProperty(RESOLVE_REFERENCED_LIBRARIES_FOR_CONTAINERS);
    containerInitializersCache.put(
        JREContainerInitializer.JRE_CONTAINER, new JREContainerInitializer());
    startIndexing();
  }

  /**
   * Initiate the background indexing process. This should be deferred after the plug-in activation.
   */
  private void startIndexing() {
    if (this.indexManager != null) this.indexManager.reset();
  }

  /**
   * Helper method - returns the {@link IResource} corresponding to the provided {@link IPath}, or
   * <code>null</code> if no such resource exists.
   */
  public static IResource getWorkspaceTarget(IPath path) {
    if (path == null || path.getDevice() != null) return null;
    IWorkspace workspace = ResourcesPlugin.getWorkspace();
    if (workspace == null) return null;
    return workspace.getRoot().findMember(path);
  }

  /**
   * Helper method - returns the targeted item (IResource if internal or java.io.File if external),
   * or null if unbound Internal items must be referred to using container relative paths.
   */
  public static Object getTarget(IPath path, boolean checkResourceExistence) {
    Object target = getWorkspaceTarget(path); // Implicitly checks resource existence
    if (target != null) return target;
    return getExternalTarget(path, checkResourceExistence);
  }

  /**
   * Helper method - returns either the linked {@link IFolder} or the {@link File} corresponding to
   * the provided {@link IPath}. If <code>checkResourceExistence</code> is <code>false</code>, then
   * the IFolder or File object is always returned, otherwise <code>null</code> is returned if it
   * does not exist on the file system.
   */
  public static Object getExternalTarget(IPath path, boolean checkResourceExistence) {
    if (path == null) return null;
    //        ExternalFoldersManager externalFoldersManager = JavaModelManager.getExternalManager();
    //        Object linkedFolder = externalFoldersManager.getFolder(path);
    //        if (linkedFolder != null) {
    //            if (checkResourceExistence) {
    //                check if external folder is present
    //                File externalFile = new File(path.toOSString());
    //                if (!externalFile.isDirectory()) {
    //                    return null;
    //                }
    //            }
    //            return linkedFolder;
    //        }
    File externalFile = new File(path.toOSString());
    if (!checkResourceExistence) {
      return externalFile;
    } else if (externalFile.exists()) {
      return externalFile;
    }
    return null;
  }

  private static synchronized void existingExternalFilesAdd(File externalFile) {
    existingExternalFiles.add(externalFile);
  }

  private static synchronized boolean existingExternalFilesContains(File externalFile) {
    return existingExternalFiles.contains(externalFile);
  }

  public boolean isNonChainingJar(IPath path) {
    return this.nonChainingJars != null && this.nonChainingJars.contains(path);
  }

  /** Flushes the cache of external files known to be existing. */
  public static void flushExternalFileCache() {
    existingExternalFiles = new HashSet<>();
    existingExternalConfirmedFiles = new HashSet<>();
  }

  /**
   * Helper method - returns whether an object is afile (i.e. which returns true to {@link
   * File#isFile()}.
   */
  public static boolean isFile(Object target) {
    return getFile(target) != null;
  }

  /**
   * Helper method - returns the file item (i.e. which returns true to {@link File#isFile()}, or
   * null if unbound
   */
  public static synchronized File getFile(Object target) {
    if (existingExternalConfirmedFiles.contains(target)) return (File) target;
    if (target instanceof File) {
      File f = (File) target;
      if (f.isFile()) {
        existingExternalConfirmedFiles.add(f);
        return f;
      }
    }

    return null;
  }

  public static DeltaProcessingState getDeltaState() {
    return MANAGER.deltaState;
  }

  /**
   * Returns a persisted container from previous session if any. Note that it is not the original
   * container from previous session (i.e. it did not get serialized) but rather a summary of its
   * entries recreated for CP initialization purpose. As such it should not be stored into container
   * caches.
   */
  public IClasspathContainer getPreviousSessionContainer(
      IPath containerPath, IJavaProject project) {
    Map previousContainerValues = (Map) this.previousSessionContainers.get(project);
    if (previousContainerValues != null) {
      IClasspathContainer previousContainer =
          (IClasspathContainer) previousContainerValues.get(containerPath);
      if (previousContainer != null) {
        if (JavaModelManager.CP_RESOLVE_VERBOSE_ADVANCED)
          //                    verbose_reentering_project_container_access(containerPath, project,
          // previousContainer);
          return previousContainer;
      }
    }
    return null; // break cycle if none found
  }

  /**
   * Creates and returns a compilation unit element for the given <code>.java</code> file, its
   * project being the given project. Returns <code>null</code> if unable to recognize the
   * compilation unit.
   */
  public static ICompilationUnit createCompilationUnitFrom(IFile file, IJavaProject project) {

    if (file == null) return null;

    if (project == null) {
      project = JavaCore.create(file.getProject());
    }
    IPackageFragment pkg = (IPackageFragment) determineIfOnClasspath(file, project);
    if (pkg == null) {
      // not on classpath - make the root its folder, and a default package
      PackageFragmentRoot root =
          (PackageFragmentRoot) project.getPackageFragmentRoot(file.getParent());
      pkg = root.getPackageFragment(CharOperation.NO_STRINGS);

      if (VERBOSE) {
        System.out.println(
            "WARNING : creating unit element outside classpath ("
                + Thread.currentThread()
                + "): "
                + file.getFullPath()); // $NON-NLS-1$//$NON-NLS-2$
      }
    }
    return pkg.getCompilationUnit(file.getName());
  }

  public void verifyArchiveContent(IPath path) throws CoreException {
    if (isInvalidArchive(path)) {
      throw new CoreException(
          new Status(
              IStatus.ERROR,
              JavaCore.PLUGIN_ID,
              -1,
              Messages.status_IOException,
              new ZipException()));
    }
    ZipFile file = getZipFile(path);
    closeZipFile(file);
  }

  /**
   * Returns the cached value for whether the file referred to by <code>path</code> exists and is a
   * file, as determined by the return value of {@link File#isFile()}.
   */
  public boolean isExternalFile(IPath path) {
    return this.externalFiles != null && this.externalFiles.contains(path);
  }

  /**
   * Returns whether the provided {@link IPath} appears to be an external file, which is true if the
   * path does not represent an internal resource, does not exist on the file system, and does have
   * a file extension (this is the definition provided by {@link
   * ExternalFoldersManager#isExternalFolderPath}).
   */
  public boolean isAssumedExternalFile(IPath path) {
    if (this.assumedExternalFiles == null) {
      return false;
    }
    return this.assumedExternalFiles.contains(path);
  }
  /**
   * Returns the package fragment root represented by the resource, or the package fragment the
   * given resource is located in, or <code>null</code> if the given resource is not on the
   * classpath of the given project.
   */
  public static IJavaElement determineIfOnClasspath(IResource resource, IJavaProject project) {
    IPath resourcePath = resource.getFullPath();
    boolean isExternal = ExternalFoldersManager.isExternalFolderPath(resourcePath);
    if (isExternal) resourcePath = resource.getLocation();

    try {
      JavaProjectElementInfo projectInfo =
          (JavaProjectElementInfo)
              org.eclipse.jdt.internal.core.JavaModelManager.getJavaModelManager().getInfo(project);
      JavaProjectElementInfo.ProjectCache projectCache =
          projectInfo == null ? null : projectInfo.projectCache;
      HashtableOfArrayToObject allPkgFragmentsCache =
          projectCache == null ? null : projectCache.allPkgFragmentsCache;
      boolean isJavaLike = Util.isJavaLikeFileName(resourcePath.lastSegment());
      IClasspathEntry[] entries =
          isJavaLike
              ? project
                  .getRawClasspath() // JAVA file can only live inside SRC folder (on the raw path)
              : ((JavaProject) project).getResolvedClasspath();

      int length = entries.length;
      if (length > 0) {
        String sourceLevel = project.getOption(JavaCore.COMPILER_SOURCE, true);
        String complianceLevel = project.getOption(JavaCore.COMPILER_COMPLIANCE, true);
        for (int i = 0; i < length; i++) {
          IClasspathEntry entry = entries[i];
          if (entry.getEntryKind() == IClasspathEntry.CPE_PROJECT) continue;
          IPath rootPath = entry.getPath();
          if (rootPath.equals(resourcePath)) {
            if (isJavaLike) return null;
            return project.getPackageFragmentRoot(resource);
          } else if (rootPath.isPrefixOf(resourcePath)) {
            // allow creation of package fragment if it contains a .java file that is included
            if (!Util.isExcluded(
                resourcePath,
                ((ClasspathEntry) entry).fullInclusionPatternChars(),
                ((ClasspathEntry) entry).fullExclusionPatternChars(),
                true)) {
              // given we have a resource child of the root, it cannot be a JAR pkg root
              PackageFragmentRoot root =
                  isExternal
                      ? new ExternalPackageFragmentRoot(rootPath, (JavaProject) project)
                      : (PackageFragmentRoot)
                          ((JavaProject) project).getFolderPackageFragmentRoot(rootPath);
              if (root == null) return null;
              IPath pkgPath = resourcePath.removeFirstSegments(rootPath.segmentCount());

              if (resource.getType() == IResource.FILE) {
                // if the resource is a file, then remove the last segment which
                // is the file name in the package
                pkgPath = pkgPath.removeLastSegments(1);
              }
              String[] pkgName = pkgPath.segments();

              // if package name is in the cache, then it has already been validated
              // (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=133141)
              if (allPkgFragmentsCache != null && allPkgFragmentsCache.containsKey(pkgName))
                return root.getPackageFragment(pkgName);

              if (pkgName.length != 0
                  && JavaConventions.validatePackageName(
                              Util.packageName(pkgPath, sourceLevel, complianceLevel),
                              sourceLevel,
                              complianceLevel)
                          .getSeverity()
                      == IStatus.ERROR) {
                return null;
              }
              return root.getPackageFragment(pkgName);
            }
          }
        }
      }
    } catch (JavaModelException npe) {
      return null;
    }
    return null;
  }

  /**
   * Returns the Java element corresponding to the given resource, or <code>null</code> if unable to
   * associate the given resource with a Java element.
   *
   * <p>The resource must be one of:
   *
   * <ul>
   *   <li>a project - the element returned is the corresponding <code>IJavaProject</code>
   *   <li>a <code>.java</code> file - the element returned is the corresponding <code>
   *       ICompilationUnit</code>
   *   <li>a <code>.class</code> file - the element returned is the corresponding <code>IClassFile
   *       </code>
   *   <li>a ZIP archive (e.g. a <code>.jar</code>, a <code>.zip</code> file, etc.) - the element
   *       returned is the corresponding <code>IPackageFragmentRoot</code>
   *   <li>a folder - the element returned is the corresponding <code>IPackageFragmentRoot</code> or
   *       <code>IPackageFragment</code>
   *   <li>the workspace root resource - the element returned is the <code>IJavaModel</code>
   * </ul>
   *
   * <p>Creating a Java element has the side effect of creating and opening all of the element's
   * parents if they are not yet open.
   */
  public static IJavaElement create(IResource resource, IJavaProject project) {
    if (resource == null) {
      return null;
    }

    int type = resource.getType();
    switch (type) {
      case IResource.PROJECT:
        return JavaCore.create((IProject) resource);
      case IResource.FILE:
        return create((IFile) resource, project);
      case IResource.FOLDER:
        return create((IFolder) resource, project);
      case IResource.ROOT:
        return JavaCore.create((IWorkspaceRoot) resource);
      default:
        return null;
    }
  }
  /**
   * Returns the Java element corresponding to the given file, its project being the given project.
   * Returns <code>null</code> if unable to associate the given file with a Java element.
   *
   * <p>The file must be one of:
   *
   * <ul>
   *   <li>a <code>.java</code> file - the element returned is the corresponding <code>
   *       ICompilationUnit</code>
   *   <li>a <code>.class</code> file - the element returned is the corresponding <code>IClassFile
   *       </code>
   *   <li>a ZIP archive (e.g. a <code>.jar</code>, a <code>.zip</code> file, etc.) - the element
   *       returned is the corresponding <code>IPackageFragmentRoot</code>
   * </ul>
   *
   * <p>Creating a Java element has the side effect of creating and opening all of the element's
   * parents if they are not yet open.
   */
  public static IJavaElement create(IFile file, IJavaProject project) {
    if (file == null) {
      return null;
    }
    if (project == null) {
      project = JavaCore.create(file.getProject());
    }

    if (file.getFileExtension() != null) {
      String name = file.getName();
      if (Util.isJavaLikeFileName(name)) return createCompilationUnitFrom(file, project);
      if (org.eclipse.jdt.internal.compiler.util.Util.isClassFileName(name))
        return createClassFileFrom(file, project);
      return createJarPackageFragmentRootFrom(file, project);
    }
    return null;
  }
  /**
   * Returns the package fragment or package fragment root corresponding to the given folder, its
   * parent or great parent being the given project. or <code>null</code> if unable to associate the
   * given folder with a Java element.
   *
   * <p>Note that a package fragment root is returned rather than a default package.
   *
   * <p>Creating a Java element has the side effect of creating and opening all of the element's
   * parents if they are not yet open.
   */
  public static IJavaElement create(IFolder folder, IJavaProject project) {
    if (folder == null) {
      return null;
    }
    IJavaElement element;
    if (project == null) {
      project = JavaCore.create(folder.getProject());
      element = determineIfOnClasspath(folder, project);
      if (element == null) {
        // walk all projects and find one that have the given folder on its classpath
        IJavaProject[] projects;
        try {
          projects =
              org.eclipse.jdt.internal.core.JavaModelManager.getJavaModelManager()
                  .getJavaModel()
                  .getJavaProjects();
        } catch (JavaModelException e) {
          return null;
        }
        for (int i = 0, length = projects.length; i < length; i++) {
          project = projects[i];
          element = determineIfOnClasspath(folder, project);
          if (element != null) break;
        }
      }
    } else {
      element = determineIfOnClasspath(folder, project);
    }
    return element;
  }

  /**
   * Creates and returns a class file element for the given <code>.class</code> file, its project
   * being the given project. Returns <code>null</code> if unable to recognize the class file.
   */
  public static IClassFile createClassFileFrom(IFile file, IJavaProject project) {
    if (file == null) {
      return null;
    }
    if (project == null) {
      project = JavaCore.create(file.getProject());
    }
    IPackageFragment pkg = (IPackageFragment) determineIfOnClasspath(file, (JavaProject) project);
    if (pkg == null) {
      // fix for 1FVS7WE
      // not on classpath - make the root its folder, and a default package
      PackageFragmentRoot root =
          (PackageFragmentRoot) project.getPackageFragmentRoot(file.getParent());
      pkg = root.getPackageFragment(CharOperation.NO_STRINGS);
    }
    return pkg.getClassFile(file.getName());
  }

  /**
   * Creates and returns a handle for the given JAR file, its project being the given project. The
   * Java model associated with the JAR's project may be created as a side effect. Returns <code>
   * null</code> if unable to create a JAR package fragment root. (for example, if the JAR file
   * represents a non-Java resource)
   */
  public static IPackageFragmentRoot createJarPackageFragmentRootFrom(
      IFile file, IJavaProject project) {
    if (file == null) {
      return null;
    }
    if (project == null) {
      project = JavaCore.create(file.getProject());
    }

    // Create a jar package fragment root only if on the classpath
    IPath resourcePath = file.getFullPath();
    try {
      IClasspathEntry entry = ((JavaProject) project).getClasspathEntryFor(resourcePath);
      if (entry != null) {
        return project.getPackageFragmentRoot(file);
      }
    } catch (JavaModelException e) {
      // project doesn't exist: return null
    }
    return null;
  }

  public static IndexManager getIndexManager() {
    return MANAGER.indexManager;
  }

  public org.eclipse.jdt.internal.core.JavaModel getJavaModel() {
    return javaModel;
  }

  public DeltaProcessor getDeltaProcessor() {
    return this.deltaState.getDeltaProcessor();
  }

  /** Flushes ZipFiles cache if there are no more clients. */
  public void flushZipFiles(Object owner) {
    ZipCache zipCache = this.zipFiles.get();
    if (zipCache == null) {
      return;
    }
    // the owner will be responsible for flushing the cache
    // we want to check object identity to make sure this is the owner that created the cache
    if (zipCache.owner == owner) {
      this.zipFiles.set(null);
      zipCache.flush();
    }
  }

  /** Starts caching ZipFiles. Ignores if there are already clients. */
  public void cacheZipFiles(Object owner) {
    ZipCache zipCache = this.zipFiles.get();
    if (zipCache != null) {
      return;
    }
    // the owner will be responsible for flushing the cache
    this.zipFiles.set(new ZipCache(owner));
  }

  public synchronized char[] intern(char[] array) {
    return this.charArraySymbols.add(array);
  }

  public synchronized String intern(String s) {
    // make sure to copy the string (so that it doesn't hold on the underlying char[] that might be
    // much bigger than necessary)
    return (String) this.stringSymbols.add(new String(s));

    // Note1: String#intern() cannot be used as on some VMs this prevents the string from being
    // garbage collected
    // Note 2: Instead of using a WeakHashset, one could use a WeakHashMap with the following
    // implementation
    // 			   This would costs more per entry (one Entry object and one WeakReference more))

    /*
          WeakReference reference = (WeakReference) this.symbols.get(s);
    String existing;
    if (reference != null && (existing = (String) reference.get()) != null)
    	return existing;
    this.symbols.put(s, new WeakReference(s));
    return s;
    */
  }

  /** Returns the set of elements which are out of synch with their buffers. */
  protected HashSet getElementsOutOfSynchWithBuffers() {
    return this.elementsOutOfSynchWithBuffers;
  }

  /**
   * Returns the open ZipFile at the given path. If the ZipFile does not yet exist, it is created,
   * opened, and added to the cache of open ZipFiles.
   *
   * <p>The path must be a file system path if representing an external zip/jar, or it must be an
   * absolute workspace relative path if representing a zip/jar inside the workspace.
   *
   * @throws CoreException If unable to create/open the ZipFile
   */
  public ZipFile getZipFile(IPath path) throws CoreException {

    if (isInvalidArchive(path))
      throw new CoreException(
          new Status(
              IStatus.ERROR,
              JavaCore.PLUGIN_ID,
              -1,
              Messages.status_IOException,
              new ZipException()));

    ZipCache zipCache;
    ZipFile zipFile;
    if ((zipCache = (ZipCache) this.zipFiles.get()) != null
        && (zipFile = zipCache.getCache(path)) != null) {
      return zipFile;
    }
    File localFile = null;
    IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    IResource file = root.findMember(path);
    if (file != null) {
      // internal resource
      URI location;
      if (file.getType() != IResource.FILE || (location = file.getLocationURI()) == null) {
        throw new CoreException(
            new Status(
                IStatus.ERROR,
                JavaCore.PLUGIN_ID,
                -1,
                Messages.bind(Messages.file_notFound, path.toString()),
                null));
      }
      localFile = Util.toLocalFile(location, null /*no progress availaible*/);
      if (localFile == null)
        throw new CoreException(
            new Status(
                IStatus.ERROR,
                JavaCore.PLUGIN_ID,
                -1,
                Messages.bind(Messages.file_notFound, path.toString()),
                null));
    } else {
      // external resource -> it is ok to use toFile()
      localFile = path.toFile();
    }

    try {
      if (ZIP_ACCESS_VERBOSE) {
        System.out.println(
            "("
                + Thread.currentThread()
                + ") [JavaModelManager.getZipFile(IPath)] Creating ZipFile on "
                + localFile); // $NON-NLS-1$ //$NON-NLS-2$
      }
      zipFile = new ZipFile(localFile);
      if (zipCache != null) {
        zipCache.setCache(path, zipFile);
      }
      return zipFile;
    } catch (IOException e) {
      addInvalidArchive(path);
      throw new CoreException(
          new Status(IStatus.ERROR, JavaCore.PLUGIN_ID, -1, Messages.status_IOException, e));
    }
  }

  public boolean isInvalidArchive(IPath path) {
    return this.invalidArchives != null && this.invalidArchives.contains(path);
  }

  public void removeFromInvalidArchiveCache(IPath path) {
    if (this.invalidArchives != null) {
      this.invalidArchives.remove(path);
    }
  }

  public void addInvalidArchive(IPath path) {
    // unlikely to be null
    if (this.invalidArchives == null) {
      this.invalidArchives = Collections.synchronizedSet(new HashSet<IPath>());
    }
    if (this.invalidArchives != null) {
      this.invalidArchives.add(path);
    }
  }

  public void addNonChainingJar(IPath path) {
    if (this.nonChainingJars != null) this.nonChainingJars.add(path);
  }

  public ICompilationUnit[] getWorkingCopies(WorkingCopyOwner owner, boolean addPrimary) {
    synchronized (this.perWorkingCopyInfos) {
      ICompilationUnit[] primaryWCs =
          addPrimary && owner != DefaultWorkingCopyOwner.PRIMARY
              ? getWorkingCopies(DefaultWorkingCopyOwner.PRIMARY, false)
              : null;
      Map workingCopyToInfos = (Map) this.perWorkingCopyInfos.get(owner);
      if (workingCopyToInfos == null) return primaryWCs;
      int primaryLength = primaryWCs == null ? 0 : primaryWCs.length;
      int size =
          workingCopyToInfos
              .size(); // note size is > 0 otherwise pathToPerWorkingCopyInfos would be null
      ICompilationUnit[] result = new ICompilationUnit[primaryLength + size];
      int index = 0;
      if (primaryWCs != null) {
        for (int i = 0; i < primaryLength; i++) {
          ICompilationUnit primaryWorkingCopy = primaryWCs[i];
          ICompilationUnit workingCopy =
              new CompilationUnit(
                  (PackageFragment) primaryWorkingCopy.getParent(),
                  primaryWorkingCopy.getElementName(),
                  owner);
          if (!workingCopyToInfos.containsKey(workingCopy)) result[index++] = primaryWorkingCopy;
        }
        if (index != primaryLength)
          System.arraycopy(result, 0, result = new ICompilationUnit[index + size], 0, index);
      }
      Iterator iterator = workingCopyToInfos.values().iterator();
      while (iterator.hasNext()) {
        result[index++] = ((JavaModelManager.PerWorkingCopyInfo) iterator.next()).getWorkingCopy();
      }
      return result;
    }
  }

  /** Returns the info for the element. */
  public synchronized Object getInfo(IJavaElement element) {
    HashMap tempCache = (HashMap) this.temporaryCache.get();
    if (tempCache != null) {
      Object result = tempCache.get(element);
      if (result != null) {
        return result;
      }
    }
    return this.cache.getInfo(element);
  }

  /** Returns the info for this element without disturbing the cache ordering. */
  protected synchronized Object peekAtInfo(IJavaElement element) {
    HashMap tempCache = (HashMap) this.temporaryCache.get();
    if (tempCache != null) {
      Object result = tempCache.get(element);
      if (result != null) {
        return result;
      }
    }
    return this.cache.peekAtInfo(element);
  }

  /*
   * Removes all cached info for the given element (including all children)
   * from the cache.
   * Returns the info for the given element, or null if it was closed.
   */
  public synchronized Object removeInfoAndChildren(JavaElement element) throws JavaModelException {
    Object info = this.cache.peekAtInfo(element);
    if (info != null) {
      boolean wasVerbose = false;
      try {
        if (JavaModelCache.VERBOSE) {
          String elementType;
          switch (element.getElementType()) {
            case IJavaElement.JAVA_PROJECT:
              elementType = "project"; // $NON-NLS-1$
              break;
            case IJavaElement.PACKAGE_FRAGMENT_ROOT:
              elementType = "root"; // $NON-NLS-1$
              break;
            case IJavaElement.PACKAGE_FRAGMENT:
              elementType = "package"; // $NON-NLS-1$
              break;
            case IJavaElement.CLASS_FILE:
              elementType = "class file"; // $NON-NLS-1$
              break;
            case IJavaElement.COMPILATION_UNIT:
              elementType = "compilation unit"; // $NON-NLS-1$
              break;
            default:
              elementType = "element"; // $NON-NLS-1$
          }
          System.out.println(
              Thread.currentThread()
                  + " CLOSING "
                  + elementType
                  + " "
                  + element.toStringWithAncestors()); // $NON-NLS-1$//$NON-NLS-2$
          wasVerbose = true;
          JavaModelCache.VERBOSE = false;
        }
        element.closing(info);
        if (element instanceof IParent) {
          closeChildren(info);
        }
        this.cache.removeInfo(element);
        if (wasVerbose) {
          System.out.println(this.cache.toStringFillingRation("-> ")); // $NON-NLS-1$
        }
      } finally {
        JavaModelCache.VERBOSE = wasVerbose;
      }
      return info;
    }
    return null;
  }

  public void removePerProjectInfo(JavaProject javaProject, boolean removeExtJarInfo) {
    synchronized (this.perProjectInfos) { // use the perProjectInfo collection as its own lock
      IProject project = javaProject.getProject();
      PerProjectInfo info = (PerProjectInfo) this.perProjectInfos.get(project);
      if (info != null) {
        this.perProjectInfos.remove(project);
        //                if (removeExtJarInfo) {
        //                    info.forgetExternalTimestampsAndIndexes();
        //                }
      }
    }
    resetClasspathListCache();
  }

  /*
   * The given project is being removed. Remove all containers for this project from the cache.
   */
  public synchronized void containerRemove(IJavaProject project) {
    // TODO
    //        Map initializations = (Map) this.containerInitializationInProgress.get();
    //        if (initializations != null) {
    //            initializations.remove(project);
    //        }
    //        this.containers.remove(project);
  }

  /*
   * Returns whether there is a temporary cache for the current thread.
   */
  public boolean hasTemporaryCache() {
    return this.temporaryCache.get() != null;
  }

  /**
   * Returns the temporary cache for newly opened elements for the current thread. Creates it if not
   * already created.
   */
  public HashMap getTemporaryCache() {
    HashMap result = (HashMap) this.temporaryCache.get();
    if (result == null) {
      result = new HashMap();
      this.temporaryCache.set(result);
    }
    return result;
  }

  /*
   * Puts the infos in the given map (keys are IJavaElements and values are JavaElementInfos)
   * in the Java model cache in an atomic way if the info is not already present in the cache.
   * If the info is already present in the cache, it depends upon the forceAdd parameter.
   * If forceAdd is false it just returns the existing info and if true, this element and it's children are closed and then
   * this particular info is added to the cache.
   */
  protected synchronized Object putInfos(
      IJavaElement openedElement, Object newInfo, boolean forceAdd, Map newElements) {
    // remove existing children as the are replaced with the new children contained in newElements
    Object existingInfo = this.cache.peekAtInfo(openedElement);
    if (existingInfo != null && !forceAdd) {
      // If forceAdd is false, then it could mean that the particular element
      // wasn't in cache at that point of time, but would have got added through
      // another thread. In that case, removing the children could remove it's own
      // children. So, we should not remove the children but return the already existing
      // info.
      // https://bugs.eclipse.org/bugs/show_bug.cgi?id=372687
      return existingInfo;
    }
    if (openedElement instanceof IParent) {
      closeChildren(existingInfo);
    }

    // Need to put any JarPackageFragmentRoot in first.
    // This is due to the way the LRU cache flushes entries.
    // When a JarPackageFragment is flushed from the LRU cache, the entire
    // jar is flushed by removing the JarPackageFragmentRoot and all of its
    // children (see ElementCache.close()). If we flush the JarPackageFragment
    // when its JarPackageFragmentRoot is not in the cache and the root is about to be
    // added (during the 'while' loop), we will end up in an inconsistent state.
    // Subsequent resolution against package in the jar would fail as a result.
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=102422
    // (theodora)
    for (Iterator it = newElements.entrySet().iterator(); it.hasNext(); ) {
      Map.Entry entry = (Map.Entry) it.next();
      IJavaElement element = (IJavaElement) entry.getKey();
      if (element instanceof JarPackageFragmentRoot) {
        Object info = entry.getValue();
        it.remove();
        this.cache.putInfo(element, info);
      }
    }

    Iterator iterator = newElements.entrySet().iterator();
    while (iterator.hasNext()) {
      Map.Entry entry = (Map.Entry) iterator.next();
      this.cache.putInfo((IJavaElement) entry.getKey(), entry.getValue());
    }
    return newInfo;
  }

  private void closeChildren(Object info) {
    if (info instanceof JavaElementInfo) {
      IJavaElement[] children = ((JavaElementInfo) info).getChildren();
      for (int i = 0, size = children.length; i < size; ++i) {
        JavaElement child = (JavaElement) children[i];
        try {
          child.close();
        } catch (JavaModelException e) {
          // ignore
        }
      }
    }
  }

  /*
   * Returns the per-working copy info for the given working copy at the given path.
   * If it doesn't exist and if create, add a new per-working copy info with the given problem requestor.
   * If recordUsage, increment the per-working copy info's use count.
   * Returns null if it doesn't exist and not create.
   */
  public PerWorkingCopyInfo getPerWorkingCopyInfo(
      CompilationUnit workingCopy,
      boolean create,
      boolean recordUsage,
      IProblemRequestor problemRequestor) {
    synchronized (
        this.perWorkingCopyInfos) { // use the perWorkingCopyInfo collection as its own lock
      WorkingCopyOwner owner = workingCopy.owner;
      Map workingCopyToInfos = (Map) this.perWorkingCopyInfos.get(owner);
      if (workingCopyToInfos == null && create) {
        workingCopyToInfos = new HashMap();
        this.perWorkingCopyInfos.put(owner, workingCopyToInfos);
      }

      PerWorkingCopyInfo info =
          workingCopyToInfos == null
              ? null
              : (PerWorkingCopyInfo) workingCopyToInfos.get(workingCopy);
      if (info == null && create) {
        info = new PerWorkingCopyInfo(workingCopy, problemRequestor);
        workingCopyToInfos.put(workingCopy, info);
      }
      if (info != null && recordUsage) info.useCount++;
      return info;
    }
  }

  /*
   * Returns the per-project info for the given project. If specified, create the info if the info doesn't exist.
   */
  public PerProjectInfo getPerProjectInfo(IProject project, boolean create) {
    synchronized (this.perProjectInfos) { // use the perProjectInfo collection as its own lock
      PerProjectInfo info = this.perProjectInfos.get(project);
      if (info == null && create) {
        info = new PerProjectInfo(project);
        this.perProjectInfos.put(project, info);
      }
      return info;
    }
  }

  /*
   * Returns  the per-project info for the given project.
   * If the info doesn't exist, check for the project existence and create the info.
   * @throws JavaModelException if the project doesn't exist.
   */
  public PerProjectInfo getPerProjectInfoCheckExistence(IProject project)
      throws JavaModelException {
    org.eclipse.jdt.internal.core.JavaModelManager.PerProjectInfo info =
        getPerProjectInfo(project, false /* don't create info */);
    if (info == null) {
      //            if (!JavaProject.hasJavaNature(project)) {
      //                throw ((JavaProject)JavaCore.create(project)).newNotPresentException();
      //            }
      info = getPerProjectInfo(project, true /* create info */);
    }
    return info;
  }

  public boolean isClasspathBeingResolved(IJavaProject project) {
    return getClasspathBeingResolved().contains(project);
  }

  private HashSet getClasspathBeingResolved() {
    HashSet<IJavaProject> result = this.classpathsBeingResolved.get();
    if (result == null) {
      result = new HashSet<>();
      this.classpathsBeingResolved.set(result);
    }
    return result;
  }

  public void setClasspathBeingResolved(IJavaProject project, boolean classpathIsResolved) {
    if (classpathIsResolved) {
      getClasspathBeingResolved().add(project);
    } else {
      getClasspathBeingResolved().remove(project);
    }
  }

  /**
   * Get all secondary types for a project and store result in per project info cache.
   *
   * <p>This cache is an <code>Hashtable&lt;String, HashMap&lt;String, IType&gt;&gt;</code>:
   *
   * <ul>
   *   <li>key: package name
   *   <li>value:
   *       <ul>
   *         <li>key: type name
   *         <li>value: java model handle for the secondary type
   *       </ul>
   * </ul>
   *
   * Hashtable was used to protect callers from possible concurrent access. Note that this map may
   * have a specific entry which key is {@link #INDEXED_SECONDARY_TYPES } and value is a map
   * containing all secondary types created during indexing. When this key is in cache and indexing
   * is finished, returned map is merged with the value of this special key. If indexing is not
   * finished and caller does not wait for the end of indexing, returned map is the current
   * secondary types cache content which may be invalid...
   *
   * @param project Project we want get secondary types from
   * @return HashMap Table of secondary type names->path for given project
   */
  public Map secondaryTypes(IJavaProject project, boolean waitForIndexes, IProgressMonitor monitor)
      throws JavaModelException {
    if (VERBOSE) {
      StringBuffer buffer = new StringBuffer("JavaModelManager.secondaryTypes("); // $NON-NLS-1$
      buffer.append(project.getElementName());
      buffer.append(',');
      buffer.append(waitForIndexes);
      buffer.append(')');
      Util.verbose(buffer.toString());
    }

    // Return cache if not empty and there's no new secondary types created during indexing
    final PerProjectInfo projectInfo = getPerProjectInfoCheckExistence(project.getProject());
    Map indexingSecondaryCache =
        projectInfo.secondaryTypes == null
            ? null
            : (Map) projectInfo.secondaryTypes.get(INDEXED_SECONDARY_TYPES);
    if (projectInfo.secondaryTypes != null && indexingSecondaryCache == null) {
      return projectInfo.secondaryTypes;
    }

    // Perform search request only if secondary types cache is not initialized yet (this will happen
    // only once!)
    if (projectInfo.secondaryTypes == null) {
      return secondaryTypesSearching(project, waitForIndexes, monitor, projectInfo);
    }

    // New secondary types have been created while indexing secondary types cache
    // => need to know whether the indexing is finished or not
    boolean indexing = this.indexManager.awaitingJobsCount() > 0;
    if (indexing) {
      if (!waitForIndexes) {
        // Indexing is running but caller cannot wait => return current cache
        return projectInfo.secondaryTypes;
      }

      // Wait for the end of indexing or a cancel
      while (this.indexManager.awaitingJobsCount() > 0) {
        if (monitor != null && monitor.isCanceled()) {
          return projectInfo.secondaryTypes;
        }
        try {
          Thread.sleep(10);
        } catch (InterruptedException e) {
          return projectInfo.secondaryTypes;
        }
      }
    }

    // Indexing is finished => merge caches and return result
    return secondaryTypesMerging(projectInfo.secondaryTypes);
  }

  /*
   * Return secondary types cache merged with new secondary types created while indexing
   * Note that merge result is directly stored in given parameter map.
   */
  private Hashtable secondaryTypesMerging(Hashtable secondaryTypes) {
    if (VERBOSE) {
      Util.verbose("JavaModelManager.getSecondaryTypesMerged()"); // $NON-NLS-1$
      Util.verbose("	- current cache to merge:"); // $NON-NLS-1$
      Iterator entries = secondaryTypes.entrySet().iterator();
      while (entries.hasNext()) {
        Map.Entry entry = (Map.Entry) entries.next();
        String packName = (String) entry.getKey();
        Util.verbose("		+ " + packName + ':' + entry.getValue()); // $NON-NLS-1$
      }
    }

    // Return current cache if there's no indexing cache (double check, this should not happen)
    HashMap indexedSecondaryTypes = (HashMap) secondaryTypes.remove(INDEXED_SECONDARY_TYPES);
    if (indexedSecondaryTypes == null) {
      return secondaryTypes;
    }

    // Merge indexing cache in secondary types one
    Iterator entries = indexedSecondaryTypes.entrySet().iterator();
    while (entries.hasNext()) {
      Map.Entry entry = (Map.Entry) entries.next();
      IFile file = (IFile) entry.getKey();

      // Remove all secondary types of indexed file from cache
      secondaryTypesRemoving(secondaryTypes, file);

      // Add all indexing file secondary types in given secondary types cache
      HashMap fileSecondaryTypes = (HashMap) entry.getValue();
      Iterator entries2 = fileSecondaryTypes.entrySet().iterator();
      while (entries2.hasNext()) {
        Map.Entry entry2 = (Map.Entry) entries2.next();
        String packageName = (String) entry2.getKey();
        HashMap cachedTypes = (HashMap) secondaryTypes.get(packageName);
        if (cachedTypes == null) {
          secondaryTypes.put(packageName, entry2.getValue());
        } else {
          HashMap types = (HashMap) entry2.getValue();
          Iterator entries3 = types.entrySet().iterator();
          while (entries3.hasNext()) {
            Map.Entry entry3 = (Map.Entry) entries3.next();
            String typeName = (String) entry3.getKey();
            cachedTypes.put(typeName, entry3.getValue());
          }
        }
      }
    }
    if (VERBOSE) {
      Util.verbose("	- secondary types cache merged:"); // $NON-NLS-1$
      entries = secondaryTypes.entrySet().iterator();
      while (entries.hasNext()) {
        Map.Entry entry = (Map.Entry) entries.next();
        String packName = (String) entry.getKey();
        Util.verbose("		+ " + packName + ':' + entry.getValue()); // $NON-NLS-1$
      }
    }
    return secondaryTypes;
  }

  /**
   * Returns the last built state for the given project, or null if there is none. Deserializes the
   * state if necessary.
   *
   * <p>For use by image builder and evaluation support only
   */
  public Object getLastBuiltState(IProject project, IProgressMonitor monitor) {
    if (!JavaProject.hasJavaNature(project)) {
      if (JavaBuilder.DEBUG) System.out.println(project + " is not a Java project"); // $NON-NLS-1$
      return null; // should never be requested on non-Java projects
    }
    PerProjectInfo info = getPerProjectInfo(project, true /*create if missing*/);
    if (!info.triedRead) {
      info.triedRead = true;
      //            try {
      if (monitor != null)
        monitor.subTask(Messages.bind(Messages.build_readStateProgress, project.getName()));
      info.savedState = null; // readState(project);
      //            } catch (CoreException e) {
      //                e.printStackTrace();
      //            }
    }
    return info.savedState;
  }

  /**
   * Remove from secondary types cache all types belonging to a given file. Clean secondary types
   * cache built while indexing if requested.
   *
   * <p>Project's secondary types cache is found using file location.
   *
   * @param file File to remove
   */
  public void secondaryTypesRemoving(IFile file, boolean cleanIndexCache) {
    if (VERBOSE) {
      StringBuffer buffer =
          new StringBuffer("JavaModelManager.removeFromSecondaryTypesCache("); // $NON-NLS-1$
      buffer.append(file.getName());
      buffer.append(')');
      Util.verbose(buffer.toString());
    }
    if (file != null) {
      PerProjectInfo projectInfo = getPerProjectInfo(file.getProject(), false);
      if (projectInfo != null && projectInfo.secondaryTypes != null) {
        if (VERBOSE) {
          Util.verbose(
              "-> remove file from cache of project: "
                  + file.getProject().getName()); // $NON-NLS-1$
        }

        // Clean current cache
        secondaryTypesRemoving(projectInfo.secondaryTypes, file);

        // Clean indexing cache if necessary
        HashMap indexingCache = (HashMap) projectInfo.secondaryTypes.get(INDEXED_SECONDARY_TYPES);
        if (!cleanIndexCache) {
          if (indexingCache == null) {
            // Need to signify that secondary types indexing will happen before any request happens
            // see bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=152841
            projectInfo.secondaryTypes.put(INDEXED_SECONDARY_TYPES, new HashMap());
          }
          return;
        }
        if (indexingCache != null) {
          Set keys = indexingCache.keySet();
          int filesSize = keys.size(), filesCount = 0;
          File[] removed = null;
          Iterator cachedFiles = keys.iterator();
          while (cachedFiles.hasNext()) {
            File cachedFile = (File) cachedFiles.next();
            if (file.equals(cachedFile)) {
              if (removed == null) removed = new File[filesSize];
              filesSize--;
              removed[filesCount++] = cachedFile;
            }
          }
          if (removed != null) {
            for (int i = 0; i < filesCount; i++) {
              indexingCache.remove(removed[i]);
            }
          }
        }
      }
    }
  }

  /*
   * Remove from a given cache map all secondary types belonging to a given file.
   * Note that there can have several secondary types per file...
   */
  private void secondaryTypesRemoving(Hashtable secondaryTypesMap, IFile file) {
    if (VERBOSE) {
      StringBuffer buffer =
          new StringBuffer("JavaModelManager.removeSecondaryTypesFromMap("); // $NON-NLS-1$
      Iterator entries = secondaryTypesMap.entrySet().iterator();
      while (entries.hasNext()) {
        Map.Entry entry = (Map.Entry) entries.next();
        String qualifiedName = (String) entry.getKey();
        buffer.append(qualifiedName + ':' + entry.getValue());
      }
      buffer.append(',');
      buffer.append(file.getFullPath());
      buffer.append(')');
      Util.verbose(buffer.toString());
    }
    Set packageEntries = secondaryTypesMap.entrySet();
    int packagesSize = packageEntries.size(), removedPackagesCount = 0;
    String[] removedPackages = null;
    Iterator packages = packageEntries.iterator();
    while (packages.hasNext()) {
      Map.Entry entry = (Map.Entry) packages.next();
      String packName = (String) entry.getKey();
      if (packName
          != INDEXED_SECONDARY_TYPES) { // skip indexing cache entry if present (!= is intentional)
        HashMap types = (HashMap) entry.getValue();
        Set nameEntries = types.entrySet();
        int namesSize = nameEntries.size(), removedNamesCount = 0;
        String[] removedNames = null;
        Iterator names = nameEntries.iterator();
        while (names.hasNext()) {
          Map.Entry entry2 = (Map.Entry) names.next();
          String typeName = (String) entry2.getKey();
          JavaElement type = (JavaElement) entry2.getValue();
          if (file.equals(type.resource())) {
            if (removedNames == null) removedNames = new String[namesSize];
            namesSize--;
            removedNames[removedNamesCount++] = typeName;
          }
        }
        if (removedNames != null) {
          for (int i = 0; i < removedNamesCount; i++) {
            types.remove(removedNames[i]);
          }
        }
        if (types.size() == 0) {
          if (removedPackages == null) removedPackages = new String[packagesSize];
          packagesSize--;
          removedPackages[removedPackagesCount++] = packName;
        }
      }
    }
    if (removedPackages != null) {
      for (int i = 0; i < removedPackagesCount; i++) {
        secondaryTypesMap.remove(removedPackages[i]);
      }
    }
    if (VERBOSE) {
      Util.verbose("	- new secondary types map:"); // $NON-NLS-1$
      Iterator entries = secondaryTypesMap.entrySet().iterator();
      while (entries.hasNext()) {
        Map.Entry entry = (Map.Entry) entries.next();
        String qualifiedName = (String) entry.getKey();
        Util.verbose("		+ " + qualifiedName + ':' + entry.getValue()); // $NON-NLS-1$
      }
    }
  }

  /** Returns the existing element in the cache that is equal to the given element. */
  public synchronized IJavaElement getExistingElement(IJavaElement element) {
    return this.cache.getExistingElement(element);
  }

  /** Remember the info for the jar binary type */
  protected synchronized void putJarTypeInfo(IJavaElement type, Object info) {
    this.cache.jarTypeCache.put(type, info);
  }

  /*
   * Perform search request to get all secondary types of a given project.
   * If not waiting for indexes and indexing is running, will return types found in current built indexes...
   */
  private Map secondaryTypesSearching(
      IJavaProject project,
      boolean waitForIndexes,
      IProgressMonitor monitor,
      final PerProjectInfo projectInfo)
      throws JavaModelException {
    if (VERBOSE || BasicSearchEngine.VERBOSE) {
      StringBuffer buffer =
          new StringBuffer("JavaModelManager.secondaryTypesSearch("); // $NON-NLS-1$
      buffer.append(project.getElementName());
      buffer.append(',');
      buffer.append(waitForIndexes);
      buffer.append(')');
      Util.verbose(buffer.toString());
    }

    final Hashtable secondaryTypes = new Hashtable(3);
    IRestrictedAccessTypeRequestor nameRequestor =
        new IRestrictedAccessTypeRequestor() {
          public void acceptType(
              int modifiers,
              char[] packageName,
              char[] simpleTypeName,
              char[][] enclosingTypeNames,
              String path,
              AccessRestriction access) {
            String key = packageName == null ? "" : new String(packageName); // $NON-NLS-1$
            HashMap types = (HashMap) secondaryTypes.get(key);
            if (types == null) types = new HashMap(3);
            types.put(new String(simpleTypeName), path);
            secondaryTypes.put(key, types);
          }
        };

    // Build scope using prereq projects but only source folders
    IPackageFragmentRoot[] allRoots = project.getAllPackageFragmentRoots();
    int length = allRoots.length, size = 0;
    IPackageFragmentRoot[] allSourceFolders = new IPackageFragmentRoot[length];
    for (int i = 0; i < length; i++) {
      if (allRoots[i].getKind() == IPackageFragmentRoot.K_SOURCE) {
        allSourceFolders[size++] = allRoots[i];
      }
    }
    if (size < length) {
      System.arraycopy(
          allSourceFolders, 0, allSourceFolders = new IPackageFragmentRoot[size], 0, size);
    }

    // Search all secondary types on scope
    new BasicSearchEngine()
        .searchAllSecondaryTypeNames(allSourceFolders, nameRequestor, waitForIndexes, monitor);

    // Build types from paths
    Iterator packages = secondaryTypes.values().iterator();
    while (packages.hasNext()) {
      HashMap types = (HashMap) packages.next();
      HashMap tempTypes = new HashMap(types.size());
      Iterator names = types.entrySet().iterator();
      while (names.hasNext()) {
        Map.Entry entry = (Map.Entry) names.next();
        String typeName = (String) entry.getKey();
        String path = (String) entry.getValue();
        names.remove();
        if (Util.isJavaLikeFileName(path)) {
          IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(path));
          ICompilationUnit unit =
              org.eclipse.jdt.internal.core.JavaModelManager.createCompilationUnitFrom(file, null);
          IType type = unit.getType(typeName);
          tempTypes.put(typeName, type);
        }
      }
      types.putAll(tempTypes);
    }

    // Store result in per project info cache if still null or there's still an indexing cache (may
    // have been set by another thread...)
    if (projectInfo.secondaryTypes == null
        || projectInfo.secondaryTypes.get(INDEXED_SECONDARY_TYPES) != null) {
      projectInfo.secondaryTypes = secondaryTypes;
      if (VERBOSE || BasicSearchEngine.VERBOSE) {
        System.out.print(
            Thread.currentThread() + "	-> secondary paths stored in cache: "); // $NON-NLS-1$
        System.out.println();
        Iterator entries = secondaryTypes.entrySet().iterator();
        while (entries.hasNext()) {
          Map.Entry entry = (Map.Entry) entries.next();
          String qualifiedName = (String) entry.getKey();
          Util.verbose("		- " + qualifiedName + '-' + entry.getValue()); // $NON-NLS-1$
        }
      }
    }
    return projectInfo.secondaryTypes;
  }

  public String getOption(String optionName) {

    if (org.eclipse.jdt.core.JavaCore.CORE_ENCODING.equals(optionName)) {
      return "UTF-8";
    }
    // backward compatibility
    //        if (isDeprecatedOption(optionName)) {
    //            return JavaCore.ERROR;
    //        }
    int optionLevel = getOptionLevel(optionName);
    if (optionLevel != UNKNOWN_OPTION) {

      String value = optionsCache.get(optionName);
      //            if (value == null && optionLevel == DEPRECATED_OPTION) {
      //                 May be a deprecated option, retrieve the new value in compatible options
      //                String[] compatibleOptions = (String[])
      // this.deprecatedOptions.get(optionName);
      //                value = service.get(compatibleOptions[0], null, this.preferencesLookup);
      //            }
      return value == null ? null : value.trim();
    }
    return null;
  }

  public void setOptions(Hashtable newOptions) {
    Hashtable cachedValue = newOptions == null ? null : new Hashtable(newOptions);
    //        IEclipsePreferences defaultPreferences = getDefaultPreferences();
    //        IEclipsePreferences instancePreferences = getInstancePreferences();

    if (newOptions == null) {
      //            try {
      optionsCache.clear();
      //            } catch(BackingStoreException e) {
      //                 ignore
      //            }
    }
    //        else {
    //            Enumeration keys = newOptions.keys();
    //            while (keys.hasMoreElements()){
    //                String key = (String)keys.nextElement();
    //                int optionLevel = getOptionLevel(key);
    //                if (optionLevel == UNKNOWN_OPTION) continue; // unrecognized option
    //                if (key.equals(JavaCore.CORE_ENCODING)) {
    //                    if (cachedValue != null) {
    //                        cachedValue.put(key, JavaCore.getEncoding());
    //                    }
    //                    continue; // skipped, contributed by resource prefs
    //                }
    //                String value = (String) newOptions.get(key);
    //                String defaultValue = defaultPreferences.get(key, null);
    //                // Store value in preferences
    //                if (defaultValue != null && defaultValue.equals(value)) {
    //                    value = null;
    //                }
    //                storePreference(key, value, instancePreferences, newOptions);
    //            }
    //            try {
    //                // persist options
    //                instancePreferences.flush();
    //            } catch(BackingStoreException e) {
    //                // ignore
    //            }
    //        }
    // update cache
    Util.fixTaskTags(cachedValue);
    this.optionsCache.putAll(cachedValue);
  }

  public Hashtable getOptions() {

    // return cached options if already computed
    Hashtable cachedOptions; // use a local variable to avoid race condition (see
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=256329 )
    if ((cachedOptions = this.optionsCache) != null) {
      return new Hashtable(cachedOptions);
    }
    //        if (!Platform.isRunning()) {
    this.optionsCache = getDefaultOptionsNoInitialization();
    return new Hashtable(this.optionsCache);
    //        }
    //        // init
    //        Hashtable options = new Hashtable(10);
    //        IPreferencesService service = Platform.getPreferencesService();
    //
    //        // set options using preferences service lookup
    //        Iterator iterator = this.optionNames.iterator();
    //        while (iterator.hasNext()) {
    //            String propertyName = (String) iterator.next();
    //            String propertyValue = service.get(propertyName, null, this.preferencesLookup);
    //            if (propertyValue != null) {
    //                options.put(propertyName, propertyValue);
    //            }
    //        }
    //
    //        // set deprecated options using preferences service lookup
    //        Iterator deprecatedEntries = this.deprecatedOptions.entrySet().iterator();
    //        while (deprecatedEntries.hasNext()) {
    //            Entry entry = (Entry) deprecatedEntries.next();
    //            String propertyName = (String) entry.getKey();
    //            String propertyValue = service.get(propertyName, null, this.preferencesLookup);
    //            if (propertyValue != null) {
    //                options.put(propertyName, propertyValue);
    //                String[] compatibleOptions = (String[]) entry.getValue();
    //                for (int co=0, length=compatibleOptions.length; co < length; co++) {
    //                    String compatibleOption = compatibleOptions[co];
    //                    if (!options.containsKey(compatibleOption))
    //                        options.put(compatibleOption, propertyValue);
    //                }
    //            }
    //        }
    //
    //        // get encoding through resource plugin
    //        options.put(JavaCore.CORE_ENCODING, JavaCore.getEncoding());
    //
    //        // backward compatibility
    //        addDeprecatedOptions(options);
    //
    //        Util.fixTaskTags(options);
    //        // store built map in cache
    //        this.optionsCache = new Hashtable(options);
    //        // return built map
    //        return options;
  }

  // If modified, also modify the method getDefaultOptionsNoInitialization()
  public Hashtable getDefaultOptions() {

    Hashtable defaultOptions = new Hashtable(10);

    // see JavaCorePreferenceInitializer#initializeDefaultPluginPreferences() for changing default
    // settings
    // If modified, also modify the method getDefaultOptionsNoInitialization()
    //        IEclipsePreferences defaultPreferences = getDefaultPreferences();

    // initialize preferences to their default
    //        Iterator iterator = this.optionNames.iterator();
    //        while (iterator.hasNext()) {
    //            String propertyName = (String) iterator.next();
    //            String value = defaultPreferences.get(propertyName, null);
    //            if (value != null) defaultOptions.put(propertyName, value);
    //        }
    // get encoding through resource plugin
    defaultOptions.put(org.eclipse.jdt.core.JavaCore.CORE_ENCODING, "UTF-8");
    defaultOptions.putAll(getDefaultOptionsNoInitialization());
    // backward compatibility
    //        addDeprecatedOptions(defaultOptions);

    return defaultOptions;
  }

  // Do not modify without modifying getDefaultOptions()
  private Hashtable getDefaultOptionsNoInitialization() {
    Map defaultOptionsMap = new CompilerOptions().getMap(); // compiler defaults

    // Override some compiler defaults
    defaultOptionsMap.put(
        org.eclipse.jdt.core.JavaCore.COMPILER_LOCAL_VARIABLE_ATTR,
        org.eclipse.jdt.core.JavaCore.GENERATE);
    defaultOptionsMap.put(
        org.eclipse.jdt.core.JavaCore.COMPILER_CODEGEN_UNUSED_LOCAL,
        org.eclipse.jdt.core.JavaCore.PRESERVE);
    defaultOptionsMap.put(
        org.eclipse.jdt.core.JavaCore.COMPILER_TASK_TAGS,
        org.eclipse.jdt.core.JavaCore.DEFAULT_TASK_TAGS);
    defaultOptionsMap.put(
        org.eclipse.jdt.core.JavaCore.COMPILER_TASK_PRIORITIES,
        org.eclipse.jdt.core.JavaCore.DEFAULT_TASK_PRIORITIES);
    defaultOptionsMap.put(
        org.eclipse.jdt.core.JavaCore.COMPILER_TASK_CASE_SENSITIVE,
        org.eclipse.jdt.core.JavaCore.ENABLED);
    defaultOptionsMap.put(
        org.eclipse.jdt.core.JavaCore.COMPILER_DOC_COMMENT_SUPPORT,
        org.eclipse.jdt.core.JavaCore.ENABLED);
    defaultOptionsMap.put(
        org.eclipse.jdt.core.JavaCore.COMPILER_PB_FORBIDDEN_REFERENCE,
        org.eclipse.jdt.core.JavaCore.ERROR);

    // Builder settings
    defaultOptionsMap.put(
        org.eclipse.jdt.core.JavaCore.CORE_JAVA_BUILD_RESOURCE_COPY_FILTER, ""); // $NON-NLS-1$
    defaultOptionsMap.put(
        org.eclipse.jdt.core.JavaCore.CORE_JAVA_BUILD_INVALID_CLASSPATH,
        org.eclipse.jdt.core.JavaCore.ABORT);
    defaultOptionsMap.put(
        org.eclipse.jdt.core.JavaCore.CORE_JAVA_BUILD_DUPLICATE_RESOURCE,
        org.eclipse.jdt.core.JavaCore.WARNING);
    defaultOptionsMap.put(
        org.eclipse.jdt.core.JavaCore.CORE_JAVA_BUILD_CLEAN_OUTPUT_FOLDER,
        org.eclipse.jdt.core.JavaCore.CLEAN);

    // JavaCore settings
    defaultOptionsMap.put(
        org.eclipse.jdt.core.JavaCore.CORE_JAVA_BUILD_ORDER, org.eclipse.jdt.core.JavaCore.IGNORE);
    defaultOptionsMap.put(
        org.eclipse.jdt.core.JavaCore.CORE_INCOMPLETE_CLASSPATH,
        org.eclipse.jdt.core.JavaCore.ERROR);
    defaultOptionsMap.put(
        org.eclipse.jdt.core.JavaCore.CORE_CIRCULAR_CLASSPATH, org.eclipse.jdt.core.JavaCore.ERROR);
    defaultOptionsMap.put(
        org.eclipse.jdt.core.JavaCore.CORE_INCOMPATIBLE_JDK_LEVEL,
        org.eclipse.jdt.core.JavaCore.IGNORE);
    defaultOptionsMap.put(
        org.eclipse.jdt.core.JavaCore.CORE_OUTPUT_LOCATION_OVERLAPPING_ANOTHER_SOURCE,
        org.eclipse.jdt.core.JavaCore.ERROR);
    defaultOptionsMap.put(
        org.eclipse.jdt.core.JavaCore.CORE_ENABLE_CLASSPATH_EXCLUSION_PATTERNS,
        org.eclipse.jdt.core.JavaCore.ENABLED);
    defaultOptionsMap.put(
        org.eclipse.jdt.core.JavaCore.CORE_ENABLE_CLASSPATH_MULTIPLE_OUTPUT_LOCATIONS,
        org.eclipse.jdt.core.JavaCore.ENABLED);

    // Formatter settings
    defaultOptionsMap.putAll(DefaultCodeFormatterConstants.getEclipseDefaultSettings());

    // CodeAssist settings
    defaultOptionsMap.put(
        org.eclipse.jdt.core.JavaCore.CODEASSIST_VISIBILITY_CHECK,
        org.eclipse.jdt.core.JavaCore.DISABLED);
    defaultOptionsMap.put(
        org.eclipse.jdt.core.JavaCore.CODEASSIST_DEPRECATION_CHECK,
        org.eclipse.jdt.core.JavaCore.DISABLED);
    defaultOptionsMap.put(
        org.eclipse.jdt.core.JavaCore.CODEASSIST_IMPLICIT_QUALIFICATION,
        org.eclipse.jdt.core.JavaCore.DISABLED);
    defaultOptionsMap.put(
        org.eclipse.jdt.core.JavaCore.CODEASSIST_FIELD_PREFIXES, ""); // $NON-NLS-1$
    defaultOptionsMap.put(
        org.eclipse.jdt.core.JavaCore.CODEASSIST_STATIC_FIELD_PREFIXES, ""); // $NON-NLS-1$
    defaultOptionsMap.put(
        org.eclipse.jdt.core.JavaCore.CODEASSIST_STATIC_FINAL_FIELD_PREFIXES, ""); // $NON-NLS-1$
    defaultOptionsMap.put(
        org.eclipse.jdt.core.JavaCore.CODEASSIST_LOCAL_PREFIXES, ""); // $NON-NLS-1$
    defaultOptionsMap.put(
        org.eclipse.jdt.core.JavaCore.CODEASSIST_ARGUMENT_PREFIXES, ""); // $NON-NLS-1$
    defaultOptionsMap.put(
        org.eclipse.jdt.core.JavaCore.CODEASSIST_FIELD_SUFFIXES, ""); // $NON-NLS-1$
    defaultOptionsMap.put(
        org.eclipse.jdt.core.JavaCore.CODEASSIST_STATIC_FIELD_SUFFIXES, ""); // $NON-NLS-1$
    defaultOptionsMap.put(
        org.eclipse.jdt.core.JavaCore.CODEASSIST_STATIC_FINAL_FIELD_SUFFIXES, ""); // $NON-NLS-1$
    defaultOptionsMap.put(
        org.eclipse.jdt.core.JavaCore.CODEASSIST_LOCAL_SUFFIXES, ""); // $NON-NLS-1$
    defaultOptionsMap.put(
        org.eclipse.jdt.core.JavaCore.CODEASSIST_ARGUMENT_SUFFIXES, ""); // $NON-NLS-1$
    defaultOptionsMap.put(
        org.eclipse.jdt.core.JavaCore.CODEASSIST_FORBIDDEN_REFERENCE_CHECK,
        org.eclipse.jdt.core.JavaCore.ENABLED);
    defaultOptionsMap.put(
        org.eclipse.jdt.core.JavaCore.CODEASSIST_DISCOURAGED_REFERENCE_CHECK,
        org.eclipse.jdt.core.JavaCore.DISABLED);
    defaultOptionsMap.put(
        org.eclipse.jdt.core.JavaCore.CODEASSIST_CAMEL_CASE_MATCH,
        org.eclipse.jdt.core.JavaCore.ENABLED);
    defaultOptionsMap.put(
        org.eclipse.jdt.core.JavaCore.CODEASSIST_SUGGEST_STATIC_IMPORTS,
        org.eclipse.jdt.core.JavaCore.ENABLED);

    // Time out for parameter names
    defaultOptionsMap.put(
        org.eclipse.jdt.core.JavaCore.TIMEOUT_FOR_PARAMETER_NAME_FROM_ATTACHED_JAVADOC,
        "50"); // $NON-NLS-1$

    return new Hashtable(defaultOptionsMap);
  }

  /**
   * Returns the level of the given option.
   *
   * @param optionName The name of the option
   * @return The level of the option as an int which may have the following values:
   *     <ul>
   *       <li>{@link #UNKNOWN_OPTION}: the given option is unknown
   *       <li>{@link #DEPRECATED_OPTION}: the given option is deprecated
   *       <li>{@link #VALID_OPTION}: the given option is valid
   *     </ul>
   */
  public int getOptionLevel(String optionName) {
    if (this.optionNames.contains(optionName)) {
      return VALID_OPTION;
    }
    if (this.deprecatedOptions.get(optionName) != null) {
      return DEPRECATED_OPTION;
    }
    return UNKNOWN_OPTION;
  }

  /**
   * Returns the value of the given option for the given Eclipse preferences. If no value was
   * already set, then inherits from the global options if specified.
   *
   * @param optionName The name of the option
   * @param inheritJavaCoreOptions Tells whether the value can be inherited from global JavaCore
   *     options
   * @param projectPreferences The eclipse preferences from which to get the value
   * @return The value of the option. May be <code>null</code>
   */
  public String getOption(
      String optionName, boolean inheritJavaCoreOptions, IEclipsePreferences projectPreferences) {
    // Return the option value depending on its level
    switch (getOptionLevel(optionName)) {
      case VALID_OPTION:
        // Valid option, return the preference value
        String javaCoreDefault = inheritJavaCoreOptions ? JavaCore.getOption(optionName) : null;
        if (projectPreferences == null) return javaCoreDefault;
        String value = projectPreferences.get(optionName, javaCoreDefault);
        return value == null ? null : value.trim();
      case DEPRECATED_OPTION:
        // Return the deprecated option value if it was already set
        String oldValue = projectPreferences.get(optionName, null);
        if (oldValue != null) {
          return oldValue.trim();
        }
        // Get the new compatible value
        String[] compatibleOptions = (String[]) this.deprecatedOptions.get(optionName);
        String newDefault =
            inheritJavaCoreOptions ? JavaCore.getOption(compatibleOptions[0]) : null;
        String newValue = projectPreferences.get(compatibleOptions[0], newDefault);
        return newValue == null ? null : newValue.trim();
    }
    return null;
  }

  /**
   * Returns whether an option name is known or not.
   *
   * @param optionName The name of the option
   * @return <code>true</code> when the option name is either {@link #VALID_OPTION valid} or {@link
   *     #DEPRECATED_OPTION deprecated}, <code>false</code> otherwise.
   */
  public boolean knowsOption(String optionName) {
    boolean knownOption = this.optionNames.contains(optionName);
    if (!knownOption) {
      knownOption = this.deprecatedOptions.get(optionName) != null;
    }
    return knownOption;
  }

  /**
   * Store the preferences value for the given option name.
   *
   * @param optionName The name of the option
   * @param optionValue The value of the option. If <code>null</code>, then the option will be
   *     removed from the preferences instead.
   * @param eclipsePreferences The eclipse preferences to be updated
   * @param otherOptions more options being stored, used to avoid conflict between deprecated option
   *     and its compatible
   * @return <code>true</code> if the preferences have been changed, <code>false</code> otherwise.
   */
  public boolean storePreference(
      String optionName,
      String optionValue,
      IEclipsePreferences eclipsePreferences,
      Map otherOptions) {
    int optionLevel = this.getOptionLevel(optionName);
    if (optionLevel == UNKNOWN_OPTION) return false; // unrecognized option

    // Store option value
    switch (optionLevel) {
      case org.eclipse.jdt.internal.core.JavaModelManager.VALID_OPTION:
        if (optionValue == null) {
          eclipsePreferences.remove(optionName);
        } else {
          eclipsePreferences.put(optionName, optionValue);
        }
        break;
      case org.eclipse.jdt.internal.core.JavaModelManager.DEPRECATED_OPTION:
        // Try to migrate deprecated option
        eclipsePreferences.remove(optionName); // get rid off old preference
        String[] compatibleOptions = (String[]) this.deprecatedOptions.get(optionName);
        for (int co = 0, length = compatibleOptions.length; co < length; co++) {
          if (otherOptions != null && otherOptions.containsKey(compatibleOptions[co]))
            continue; // don't overwrite explicit value of otherOptions at compatibleOptions[co]
          if (optionValue == null) {
            eclipsePreferences.remove(compatibleOptions[co]);
          } else {
            eclipsePreferences.put(compatibleOptions[co], optionValue);
          }
        }
        break;
      default:
        return false;
    }
    return true;
  }

  /*
   * Discards the per working copy info for the given working copy (making it a compilation unit)
   * if its use count was 1. Otherwise, just decrement the use count.
   * If the working copy is primary, computes the delta between its state and the original compilation unit
   * and register it.
   * Close the working copy, its buffer and remove it from the shared working copy table.
   * Ignore if no per-working copy info existed.
   * NOTE: it must NOT be synchronized as it may interact with the element info cache (if useCount is decremented to 0), see bug 50667.
   * Returns the new use count (or -1 if it didn't exist).
   */
  public int discardPerWorkingCopyInfo(CompilationUnit workingCopy) throws JavaModelException {

    // create the delta builder (this remembers the current content of the working copy)
    // outside the perWorkingCopyInfos lock (see bug 50667)
    JavaElementDeltaBuilder deltaBuilder = null;
    if (workingCopy.isPrimary() && workingCopy.hasUnsavedChanges()) {
      deltaBuilder = new JavaElementDeltaBuilder(workingCopy);
    }
    PerWorkingCopyInfo info = null;
    synchronized (this.perWorkingCopyInfos) {
      WorkingCopyOwner owner = workingCopy.owner;
      Map workingCopyToInfos = (Map) this.perWorkingCopyInfos.get(owner);
      if (workingCopyToInfos == null) return -1;

      info = (PerWorkingCopyInfo) workingCopyToInfos.get(workingCopy);
      if (info == null) return -1;

      if (--info.useCount == 0) {
        // remove per working copy info
        workingCopyToInfos.remove(workingCopy);
        if (workingCopyToInfos.isEmpty()) {
          this.perWorkingCopyInfos.remove(owner);
        }
      }
    }
    if (info.useCount == 0) { // info cannot be null here (check was done above)
      // remove infos + close buffer (since no longer working copy)
      // outside the perWorkingCopyInfos lock (see bug 50667)
      removeInfoAndChildren(workingCopy);
      workingCopy.closeBuffer();

      // compute the delta if needed and register it if there are changes
      if (deltaBuilder != null) {
        deltaBuilder.buildDeltas();
        if (deltaBuilder.delta != null) {
          getDeltaProcessor().registerJavaModelDelta(deltaBuilder.delta);
        }
      }
    }
    return info.useCount;
  }

  public IClasspathContainer getClasspathContainer(
      final IPath containerPath, final IJavaProject project) throws JavaModelException {

    IClasspathContainer container = containerGet(project, containerPath);

    if (container == null) {
      //            if (batchContainerInitializations()) {
      //                // avoid deep recursion while initializing container on workspace restart
      //                // (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=60437)
      //                try {
      //                    container = initializeAllContainers(project, containerPath);
      //                } finally {
      //                    batchInitializationFinished();
      //                }
      //            } else {
      container = initializeContainer(project, containerPath);
      //                containerBeingInitializedRemove(project, containerPath);
      SetContainerOperation operation =
          new SetContainerOperation(
              containerPath, new IJavaProject[] {project}, new IClasspathContainer[] {container});
      operation.runOperation(null);
      //            }
    }
    return container;
  }

  private IClasspathContainer initializeContainer(IJavaProject project, IPath containerPath)
      throws JavaModelException {
    ClasspathContainerInitializer initializer =
        containerInitializersCache.get(containerPath.segment(0));
    IClasspathContainer container = null;
    if (initializer != null) {
      containerPut(
          project,
          containerPath,
          CONTAINER_INITIALIZATION_IN_PROGRESS); // avoid initialization cycles
      try {
        initializer.initialize(containerPath, project);

        //            if (monitor != null)
        //                monitor.subTask(""); //$NON-NLS-1$

        // retrieve value (if initialization was successful)
        container = containerBeingInitializedGet(project, containerPath);
        if (container == null && containerGet(project, containerPath) == null) {
          // initializer failed to do its job: redirect to the failure container
          container = initializer.getFailureContainer(containerPath, project);
          //                if (container == null) {
          //                    if (CP_RESOLVE_VERBOSE || CP_RESOLVE_VERBOSE_FAILURE)
          //                        verbose_container_null_failure_container(project, containerPath,
          // initializer);
          //                    return null; // break cycle
          //                }
          //                if (CP_RESOLVE_VERBOSE || CP_RESOLVE_VERBOSE_FAILURE)
          //                    verbose_container_using_failure_container(project, containerPath,
          // initializer);
          containerPut(project, containerPath, container);
        }
      } catch (CoreException e) {
        if (e instanceof JavaModelException) {
          throw (JavaModelException) e;
        } else {
          throw new JavaModelException(e);
        }
      }
    } else {
      // create a dummy initializer and get the default failure container
      container =
          (new ClasspathContainerInitializer() {
                public void initialize(IPath path, IJavaProject javaProject) throws CoreException {
                  // not used
                }
              })
              .getFailureContainer(containerPath, project);
    }
    return container;
  }

  /**
   * Add a secondary type in temporary indexing cache for a project got from given path.
   *
   * <p>Current secondary types cache is not modified as we want to wait that indexing was finished
   * before taking new secondary types into account.
   *
   * <p>Indexing cache is a specific entry in secondary types cache which key is {@link
   * #INDEXED_SECONDARY_TYPES } and value a map with same structure than secondary types cache
   * itself.
   *
   * @see #secondaryTypes(IJavaProject, boolean, IProgressMonitor)
   */
  public void secondaryTypeAdding(String path, char[] typeName, char[] packageName) {
    if (VERBOSE) {
      StringBuffer buffer = new StringBuffer("JavaModelManager.addSecondaryType("); // $NON-NLS-1$
      buffer.append(path);
      buffer.append(',');
      buffer.append('[');
      buffer.append(new String(packageName));
      buffer.append('.');
      buffer.append(new String(typeName));
      buffer.append(']');
      buffer.append(')');
      Util.verbose(buffer.toString());
    }
    IWorkspaceRoot wRoot = ResourcesPlugin.getWorkspace().getRoot();
    IResource resource = wRoot.findMember(path);
    if (resource != null) {
      if (org.eclipse.jdt.internal.core.util.Util.isJavaLikeFileName(path)
          && resource.getType() == IResource.FILE) {
        IProject project = resource.getProject();
        try {
          PerProjectInfo projectInfo = getPerProjectInfoCheckExistence(project);
          // Get or create map to cache secondary types while indexing (can be not synchronized as
          // indexing insure a non-concurrent usage)
          HashMap indexedSecondaryTypes = null;
          if (projectInfo.secondaryTypes == null) {
            projectInfo.secondaryTypes = new Hashtable(3);
            indexedSecondaryTypes = new HashMap(3);
            projectInfo.secondaryTypes.put(INDEXED_SECONDARY_TYPES, indexedSecondaryTypes);
          } else {
            indexedSecondaryTypes =
                (HashMap) projectInfo.secondaryTypes.get(INDEXED_SECONDARY_TYPES);
            if (indexedSecondaryTypes == null) {
              indexedSecondaryTypes = new HashMap(3);
              projectInfo.secondaryTypes.put(INDEXED_SECONDARY_TYPES, indexedSecondaryTypes);
            }
          }
          // Store the secondary type in temporary cache (these are just handles => no problem to
          // create it now...)
          HashMap allTypes = (HashMap) indexedSecondaryTypes.get(resource);
          if (allTypes == null) {
            allTypes = new HashMap(3);
            indexedSecondaryTypes.put(resource, allTypes);
          }
          ICompilationUnit unit =
              JavaModelManager.createCompilationUnitFrom((IFile) resource, null);
          if (unit != null) {
            String typeString = new String(typeName);
            IType type = unit.getType(typeString);
            // String packageString = new String(packageName);
            // use package fragment name instead of parameter as it may be invalid...
            // see bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=186781
            String packageString = type.getPackageFragment().getElementName();
            HashMap packageTypes = (HashMap) allTypes.get(packageString);
            if (packageTypes == null) {
              packageTypes = new HashMap(3);
              allTypes.put(packageString, packageTypes);
            }
            packageTypes.put(typeString, type);
          }
          if (VERBOSE) {
            Util.verbose("	- indexing cache:"); // $NON-NLS-1$
            Iterator entries = indexedSecondaryTypes.entrySet().iterator();
            while (entries.hasNext()) {
              Map.Entry entry = (Map.Entry) entries.next();
              IFile file = (IFile) entry.getKey();
              Util.verbose("		+ " + file.getFullPath() + ':' + entry.getValue()); // $NON-NLS-1$
            }
          }
        } catch (JavaModelException jme) {
          // do nothing
        }
      }
    }
  }

  public synchronized IClasspathContainer containerGet(IJavaProject project, IPath containerPath) {
    //        // check initialization in progress first
    if (containerIsInitializationInProgress(project, containerPath)) {
      return CONTAINER_INITIALIZATION_IN_PROGRESS;
    }

    Map projectContainers = (Map) this.containers.get(project);
    if (projectContainers == null) {
      return null;
    }
    IClasspathContainer container = (IClasspathContainer) projectContainers.get(containerPath);
    return container;
  }

  public boolean containerPutIfInitializingWithSameEntries(
      IPath containerPath, IJavaProject[] projects, IClasspathContainer[] respectiveContainers) {
    int projectLength = projects.length;
    if (projectLength != 1) return false;
    final IClasspathContainer container = respectiveContainers[0];
    IJavaProject project = projects[0];
    //        // optimize only if initializing, otherwise we are in a regular setContainer(...) call
    if (!containerIsInitializationInProgress(project, containerPath)) return false;
    IClasspathContainer previousContainer =
        containerGetDefaultToPreviousSession(project, containerPath);
    if (container == null) {
      if (previousContainer == null) {
        containerPut(project, containerPath, null);
        return true;
      }
      return false;
    }
    final IClasspathEntry[] newEntries = container.getClasspathEntries();
    if (previousContainer == null)
      if (newEntries.length == 0) {
        containerPut(project, containerPath, container);
        return true;
      } else {
        if (CP_RESOLVE_VERBOSE || CP_RESOLVE_VERBOSE_FAILURE)
          verbose_missbehaving_container(
              containerPath,
              projects,
              respectiveContainers,
              container,
              newEntries,
              null /*no old entries*/);
        return false;
      }
    final IClasspathEntry[] oldEntries = previousContainer.getClasspathEntries();
    if (oldEntries.length != newEntries.length) {
      if (CP_RESOLVE_VERBOSE || CP_RESOLVE_VERBOSE_FAILURE)
        verbose_missbehaving_container(
            containerPath, projects, respectiveContainers, container, newEntries, oldEntries);
      return false;
    }
    for (int i = 0, length = newEntries.length; i < length; i++) {
      if (newEntries[i] == null) {
        if (CP_RESOLVE_VERBOSE || CP_RESOLVE_VERBOSE_FAILURE)
          verbose_missbehaving_container(project, containerPath, newEntries);
        return false;
      }
      if (!newEntries[i].equals(oldEntries[i])) {
        if (CP_RESOLVE_VERBOSE || CP_RESOLVE_VERBOSE_FAILURE)
          verbose_missbehaving_container(
              containerPath, projects, respectiveContainers, container, newEntries, oldEntries);
        return false;
      }
    }
    containerPut(project, containerPath, container);
    return true;
  }

  private boolean containerIsInitializationInProgress(IJavaProject project, IPath containerPath) {
    Map initializations = (Map) this.containerInitializationInProgress.get();
    if (initializations == null) return false;
    HashSet projectInitializations = (HashSet) initializations.get(project);
    if (projectInitializations == null) return false;
    return projectInitializations.contains(containerPath);
  }

  private void containerAddInitializationInProgress(IJavaProject project, IPath containerPath) {
    Map initializations = (Map) this.containerInitializationInProgress.get();
    if (initializations == null)
      this.containerInitializationInProgress.set(initializations = new HashMap());
    HashSet projectInitializations = (HashSet) initializations.get(project);
    if (projectInitializations == null)
      initializations.put(project, projectInitializations = new HashSet());
    projectInitializations.add(containerPath);
  }

  private void containerRemoveInitializationInProgress(IJavaProject project, IPath containerPath) {
    Map initializations = (Map) this.containerInitializationInProgress.get();
    if (initializations == null) return;
    HashSet projectInitializations = (HashSet) initializations.get(project);
    if (projectInitializations == null) return;
    projectInitializations.remove(containerPath);
    if (projectInitializations.size() == 0) initializations.remove(project);
    if (initializations.size() == 0) this.containerInitializationInProgress.set(null);
  }

  public void containerBeingInitializedPut(
      IJavaProject project, IPath containerPath, IClasspathContainer container) {
    Map perProjectContainers = (Map) this.containersBeingInitialized.get();
    if (perProjectContainers == null)
      this.containersBeingInitialized.set(perProjectContainers = new HashMap());
    HashMap perPathContainers = (HashMap) perProjectContainers.get(project);
    if (perPathContainers == null)
      perProjectContainers.put(project, perPathContainers = new HashMap());
    perPathContainers.put(containerPath, container);
  }

  public IClasspathContainer containerBeingInitializedGet(
      IJavaProject project, IPath containerPath) {
    Map perProjectContainers = (Map) this.containersBeingInitialized.get();
    if (perProjectContainers == null) return null;
    HashMap perPathContainers = (HashMap) perProjectContainers.get(project);
    if (perPathContainers == null) return null;
    return (IClasspathContainer) perPathContainers.get(containerPath);
  }

  private void verbose_missbehaving_container(
      IPath containerPath,
      IJavaProject[] projects,
      IClasspathContainer[] respectiveContainers,
      final IClasspathContainer container,
      final IClasspathEntry[] newEntries,
      final IClasspathEntry[] oldEntries) {
    Util.verbose(
        "CPContainer SET  - missbehaving container\n"
            + // $NON-NLS-1$
            "	container path: "
            + containerPath
            + '\n'
            + // $NON-NLS-1$
            "	projects: {"
            + // $NON-NLS-1$
            org.eclipse.jdt.internal.compiler.util.Util.toString(
                projects,
                new org.eclipse.jdt.internal.compiler.util.Util.Displayable() {
                  public String displayString(Object o) {
                    return ((IJavaProject) o).getElementName();
                  }
                })
            + "}\n	values on previous session: {\n"
            + // $NON-NLS-1$
            org.eclipse.jdt.internal.compiler.util.Util.toString(
                respectiveContainers,
                new org.eclipse.jdt.internal.compiler.util.Util.Displayable() {
                  public String displayString(Object o) {
                    StringBuffer buffer = new StringBuffer("		"); // $NON-NLS-1$
                    if (o == null) {
                      buffer.append("<null>"); // $NON-NLS-1$
                      return buffer.toString();
                    }
                    buffer.append(container.getDescription());
                    buffer.append(" {\n"); // $NON-NLS-1$
                    if (oldEntries == null) {
                      buffer.append(" 			"); // $NON-NLS-1$
                      buffer.append("<null>\n"); // $NON-NLS-1$
                    } else {
                      for (int j = 0; j < oldEntries.length; j++) {
                        buffer.append(" 			"); // $NON-NLS-1$
                        buffer.append(oldEntries[j]);
                        buffer.append('\n');
                      }
                    }
                    buffer.append(" 		}"); // $NON-NLS-1$
                    return buffer.toString();
                  }
                })
            + "}\n	new values: {\n"
            + // $NON-NLS-1$
            org.eclipse.jdt.internal.compiler.util.Util.toString(
                respectiveContainers,
                new org.eclipse.jdt.internal.compiler.util.Util.Displayable() {
                  public String displayString(Object o) {
                    StringBuffer buffer = new StringBuffer("		"); // $NON-NLS-1$
                    if (o == null) {
                      buffer.append("<null>"); // $NON-NLS-1$
                      return buffer.toString();
                    }
                    buffer.append(container.getDescription());
                    buffer.append(" {\n"); // $NON-NLS-1$
                    for (int j = 0; j < newEntries.length; j++) {
                      buffer.append(" 			"); // $NON-NLS-1$
                      buffer.append(newEntries[j]);
                      buffer.append('\n');
                    }
                    buffer.append(" 		}"); // $NON-NLS-1$
                    return buffer.toString();
                  }
                })
            + "\n	}"); // $NON-NLS-1$
  }

  void verbose_missbehaving_container(
      IJavaProject project, IPath containerPath, IClasspathEntry[] classpathEntries) {
    Util.verbose(
        "CPContainer GET - missbehaving container (returning null classpath entry)\n"
            + // $NON-NLS-1$
            "	project: "
            + project.getElementName()
            + '\n'
            + // $NON-NLS-1$
            "	container path: "
            + containerPath
            + '\n'
            + // $NON-NLS-1$
            "	classpath entries: {\n"
            + // $NON-NLS-1$
            org.eclipse.jdt.internal.compiler.util.Util.toString(
                classpathEntries,
                new org.eclipse.jdt.internal.compiler.util.Util.Displayable() {
                  public String displayString(Object o) {
                    StringBuffer buffer = new StringBuffer("		"); // $NON-NLS-1$
                    if (o == null) {
                      buffer.append("<null>"); // $NON-NLS-1$
                      return buffer.toString();
                    }
                    buffer.append(o);
                    return buffer.toString();
                  }
                })
            + "\n	}" // $NON-NLS-1$
        );
  }

  public synchronized IClasspathContainer containerGetDefaultToPreviousSession(
      IJavaProject project, IPath containerPath) {
    Map projectContainers = (Map) this.containers.get(project);
    if (projectContainers == null) return getPreviousSessionContainer(containerPath, project);
    IClasspathContainer container = (IClasspathContainer) projectContainers.get(containerPath);
    if (container == null) return getPreviousSessionContainer(containerPath, project);
    return container;
  }

  public IClasspathEntry resolveVariableEntry(IClasspathEntry entry, boolean usePreviousSession) {

    if (entry.getEntryKind() != IClasspathEntry.CPE_VARIABLE) return entry;

    IPath resolvedPath = getResolvedVariablePath(entry.getPath(), usePreviousSession);
    if (resolvedPath == null) return null;
    // By passing a null reference path, we keep it relative to workspace root.
    resolvedPath = ClasspathEntry.resolveDotDot(null, resolvedPath);

    Object target = org.eclipse.jdt.internal.core.JavaModel.getTarget(resolvedPath, false);
    if (target == null) return null;

    // inside the workspace
    if (target instanceof IResource) {
      IResource resolvedResource = (IResource) target;
      switch (resolvedResource.getType()) {
        case IResource.PROJECT:
          // internal project
          return JavaCore.newProjectEntry(
              resolvedPath,
              entry.getAccessRules(),
              entry.combineAccessRules(),
              entry.getExtraAttributes(),
              entry.isExported());
        case IResource.FILE:
          // internal binary archive
          return JavaCore.newLibraryEntry(
              resolvedPath,
              getResolvedVariablePath(entry.getSourceAttachmentPath(), usePreviousSession),
              getResolvedVariablePath(entry.getSourceAttachmentRootPath(), usePreviousSession),
              entry.getAccessRules(),
              entry.getExtraAttributes(),
              entry.isExported());
        case IResource.FOLDER:
          // internal binary folder
          return JavaCore.newLibraryEntry(
              resolvedPath,
              getResolvedVariablePath(entry.getSourceAttachmentPath(), usePreviousSession),
              getResolvedVariablePath(entry.getSourceAttachmentRootPath(), usePreviousSession),
              entry.getAccessRules(),
              entry.getExtraAttributes(),
              entry.isExported());
      }
    }
    if (target instanceof File) {
      File externalFile = JavaModel.getFile(target);
      if (externalFile != null) {
        // external binary archive
        return JavaCore.newLibraryEntry(
            resolvedPath,
            getResolvedVariablePath(entry.getSourceAttachmentPath(), usePreviousSession),
            getResolvedVariablePath(entry.getSourceAttachmentRootPath(), usePreviousSession),
            entry.getAccessRules(),
            entry.getExtraAttributes(),
            entry.isExported());
      } else {
        // non-existing file
        if (resolvedPath.isAbsolute()) {
          return JavaCore.newLibraryEntry(
              resolvedPath,
              getResolvedVariablePath(entry.getSourceAttachmentPath(), usePreviousSession),
              getResolvedVariablePath(entry.getSourceAttachmentRootPath(), usePreviousSession),
              entry.getAccessRules(),
              entry.getExtraAttributes(),
              entry.isExported());
        }
      }
    }
    return null;
  }

  public IPath getResolvedVariablePath(IPath variablePath, boolean usePreviousSession) {

    if (variablePath == null) return null;
    int count = variablePath.segmentCount();
    if (count == 0) return null;

    // lookup variable
    String variableName = variablePath.segment(0);
    IPath resolvedPath =
        usePreviousSession
            ? getPreviousSessionVariable(variableName)
            : /*JavaCore.getClasspathVariable(variableName)*/ null;
    if (resolvedPath == null) return null;

    // append path suffix
    if (count > 1) {
      resolvedPath = resolvedPath.append(variablePath.removeFirstSegments(1));
    }
    return resolvedPath;
  }
  /** Returns a persisted container from previous session if any */
  public IPath getPreviousSessionVariable(String variableName) {
    IPath previousPath = (IPath) this.previousSessionVariables.get(variableName);
    if (previousPath != null) {
      if (CP_RESOLVE_VERBOSE_ADVANCED)
        //                verbose_reentering_variable_access(variableName, previousPath);
        return previousPath;
    }
    return null; // break cycle
  }

  /*
   * Resets the temporary cache for newly created elements to null.
   */
  public void resetTemporaryCache() {
    this.temporaryCache.set(null);
  }

  public synchronized String cacheToString(String prefix) {
    return this.cache.toStringFillingRation(prefix);
  }

  public void closeZipFile(ZipFile zipFile) {
    if (zipFile == null) return;
    if (this.zipFiles.get() != null) {
      return; // zip file will be closed by call to flushZipFiles
    }
    try {
      if (org.eclipse.jdt.internal.core.JavaModelManager.ZIP_ACCESS_VERBOSE) {
        System.out.println(
            "("
                + Thread.currentThread()
                + ") [JavaModelManager.closeZipFile(ZipFile)] Closing ZipFile on "
                + zipFile.getName()); // $NON-NLS-1$	//$NON-NLS-2$
      }
      zipFile.close();
    } catch (IOException e) {
      // problem occured closing zip file: cannot do much more
    }
  }

  public synchronized BufferManager getDefaultBufferManager() {
    if (DEFAULT_BUFFER_MANAGER == null) {
      DEFAULT_BUFFER_MANAGER = new BufferManager();
    }
    return DEFAULT_BUFFER_MANAGER;
  }

  public boolean forceBatchInitializations(boolean initAfterLoad) {
    return false;
  }

  public synchronized void containerPut(
      IJavaProject project, IPath containerPath, IClasspathContainer container) {

    // set/unset the initialization in progress
    if (container == CONTAINER_INITIALIZATION_IN_PROGRESS) {
      containerAddInitializationInProgress(project, containerPath);

      // do not write out intermediate initialization value
      return;
    } else {
      containerRemoveInitializationInProgress(project, containerPath);

      Map projectContainers = (Map) this.containers.get(project);
      if (projectContainers == null) {
        projectContainers = new HashMap(1);
        this.containers.put(project, projectContainers);
      }

      if (container == null) {
        projectContainers.remove(containerPath);
      } else {
        projectContainers.put(containerPath, container);
      }
      // discard obsoleted information about previous session
      Map previousContainers = (Map) this.previousSessionContainers.get(project);
      if (previousContainers != null) {
        previousContainers.remove(containerPath);
      }
    }
    // container values are persisted in preferences during save operations, see
    // #saving(ISaveContext)
  }

  public int getOpenableCacheSize() {
    return this.cache.openableCache.getSpaceLimit();
  }

  /*
   * Resets the cache that holds on binary type in jar files
   */
  protected synchronized void resetJarTypeCache() {
    this.cache.resetJarTypeCache();
  }

  public void resetClasspathListCache() {
    if (this.nonChainingJars != null) this.nonChainingJars.clear();
    if (this.invalidArchives != null) this.invalidArchives.clear();
    if (this.externalFiles != null) this.externalFiles.clear();
    if (this.assumedExternalFiles != null) this.assumedExternalFiles.clear();
  }

  /*
   * Reset project options stored in info cache.
   */
  public void resetProjectOptions(JavaProject javaProject) {
    synchronized (this.perProjectInfos) { // use the perProjectInfo collection as its own lock
      IProject project = javaProject.getProject();
      PerProjectInfo info = (PerProjectInfo) this.perProjectInfos.get(project);
      if (info != null) {
        info.options = null;
      }
    }
  }

  /** Define a zip cache object. */
  static class ZipCache {
    Object owner;
    private Map<IPath, ZipFile> map;

    ZipCache(Object owner) {
      this.map = new HashMap<>();
      this.owner = owner;
    }

    public void flush() {
      Thread currentThread = Thread.currentThread();
      for (ZipFile zipFile : this.map.values()) {
        try {
          if (org.eclipse.jdt.internal.core.JavaModelManager.ZIP_ACCESS_VERBOSE) {
            System.out.println(
                "("
                    + currentThread
                    + ") [JavaModelManager.flushZipFiles()] Closing ZipFile on "
                    + zipFile.getName()); // $NON-NLS-1$//$NON-NLS-2$
          }
          zipFile.close();
        } catch (IOException e) {
          // problem occured closing zip file: cannot do much more
        }
      }
    }

    public ZipFile getCache(IPath path) {
      return this.map.get(path);
    }

    public void setCache(IPath path, ZipFile zipFile) {
      this.map.put(path, zipFile);
    }
  }

  public static class PerProjectInfo {
    static final IJavaModelStatus NEED_RESOLUTION = new JavaModelStatus();
    private static final int JAVADOC_CACHE_INITIAL_SIZE = 10;
    public IProject project;
    public Object savedState;
    public boolean triedRead;
    public IClasspathEntry[] rawClasspath;
    public IClasspathEntry[] referencedEntries;
    public IJavaModelStatus rawClasspathStatus;
    public int rawTimeStamp = 0;
    public boolean writtingRawClasspath = false;
    public IClasspathEntry[] resolvedClasspath;
    public IJavaModelStatus unresolvedEntryStatus;
    public Map
        rootPathToRawEntries; // reverse map from a package fragment root's path to the raw entry
    public Map
        rootPathToResolvedEntries; // map from a package fragment root's path to the resolved entry
    public IPath outputLocation;

    public IEclipsePreferences preferences;
    public Hashtable options;
    public Hashtable secondaryTypes;
    public LRUCache javadocCache;

    public PerProjectInfo(IProject project) {

      this.triedRead = false;
      this.savedState = null;
      this.project = project;
      this.javadocCache = new LRUCache(JAVADOC_CACHE_INITIAL_SIZE);
    }

    public synchronized IClasspathEntry[] getResolvedClasspath() {
      if (this.unresolvedEntryStatus == NEED_RESOLUTION) return null;
      return this.resolvedClasspath;
    }

    public void forgetExternalTimestampsAndIndexes() {
      //            IClasspathEntry[] classpath = this.resolvedClasspath;
      //            if (classpath == null) return;
      //            JavaModelManager manager = JavaModelManager.getJavaModelManager();
      //            IndexManager indexManager = manager.indexManager;
      //            Map externalTimeStamps = manager.deltaState.getExternalLibTimeStamps();
      //            HashMap rootInfos = JavaModelManager.getDeltaState().otherRoots;
      //            for (int i = 0, length = classpath.length; i < length; i++) {
      //                IClasspathEntry entry = classpath[i];
      //                if (entry.getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
      //                    IPath path = entry.getPath();
      //                    if (rootInfos.get(path) == null) {
      //                        externalTimeStamps.remove(path);
      //                        indexManager.removeIndex(
      //                                path); // force reindexing on next reference (see
      // https://bugs.eclipse.org/bugs/show_bug.cgi?id=250083 )
      //                    }
      //                }
      //            }
      throw new UnsupportedOperationException();
    }

    public void rememberExternalLibTimestamps() {
      //            IClasspathEntry[] classpath = this.resolvedClasspath;
      //            if (classpath == null) return;
      //            Map externalTimeStamps =
      // JavaModelManager.getJavaModelManager().deltaState.getExternalLibTimeStamps();
      //            for (int i = 0, length = classpath.length; i < length; i++) {
      //                IClasspathEntry entry = classpath[i];
      //                if (entry.getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
      //                    IPath path = entry.getPath();
      //                    if (externalTimeStamps.get(path) == null) {
      //                        Object target = JavaModel.getExternalTarget(path, true);
      //                        if (target instanceof File) {
      //                            long timestamp =
      // DeltaProcessor.getTimeStamp((java.io.File)target);
      //                            externalTimeStamps.put(path, new Long(timestamp));
      //                        }
      //                    }
      //                }
      //            }
      //            throw new UnsupportedOperationException();
    }

    public synchronized ClasspathChange resetResolvedClasspath() {
      // clear non-chaining jars cache and invalid jars cache
      org.eclipse.jdt.internal.core.JavaModelManager.getJavaModelManager()
          .resetClasspathListCache();

      // null out resolved information
      return setResolvedClasspath(
          null, null, null, null, this.rawTimeStamp, true /*add classpath change*/);
    }

    private ClasspathChange setClasspath(
        IClasspathEntry[] newRawClasspath,
        IClasspathEntry[] referencedEntries,
        IPath newOutputLocation,
        IJavaModelStatus newRawClasspathStatus,
        IClasspathEntry[] newResolvedClasspath,
        Map newRootPathToRawEntries,
        Map newRootPathToResolvedEntries,
        IJavaModelStatus newUnresolvedEntryStatus,
        boolean addClasspathChange) {
      ClasspathChange classpathChange = addClasspathChange ? addClasspathChange() : null;

      if (referencedEntries != null) this.referencedEntries = referencedEntries;
      if (this.referencedEntries == null) this.referencedEntries = ClasspathEntry.NO_ENTRIES;
      this.rawClasspath = newRawClasspath;
      this.outputLocation = newOutputLocation;
      this.rawClasspathStatus = newRawClasspathStatus;
      this.resolvedClasspath = newResolvedClasspath;
      this.rootPathToRawEntries = newRootPathToRawEntries;
      this.rootPathToResolvedEntries = newRootPathToResolvedEntries;
      this.unresolvedEntryStatus = newUnresolvedEntryStatus;
      this.javadocCache = new LRUCache(JAVADOC_CACHE_INITIAL_SIZE);

      return classpathChange;
    }

    protected ClasspathChange addClasspathChange() {
      // remember old info
      org.eclipse.jdt.internal.core.JavaModelManager manager =
          org.eclipse.jdt.internal.core.JavaModelManager.getJavaModelManager();
      ClasspathChange classpathChange =
          manager.deltaState.addClasspathChange(
              this.project, this.rawClasspath, this.outputLocation, this.resolvedClasspath);
      return classpathChange;
    }

    public ClasspathChange setRawClasspath(
        IClasspathEntry[] newRawClasspath,
        IPath newOutputLocation,
        IJavaModelStatus newRawClasspathStatus) {
      return setRawClasspath(newRawClasspath, null, newOutputLocation, newRawClasspathStatus);
    }

    public synchronized ClasspathChange setRawClasspath(
        IClasspathEntry[] newRawClasspath,
        IClasspathEntry[] referencedEntries,
        IPath newOutputLocation,
        IJavaModelStatus newRawClasspathStatus) {
      this.rawTimeStamp++;
      return setClasspath(
          newRawClasspath,
          referencedEntries,
          newOutputLocation,
          newRawClasspathStatus,
          null /*resolved classpath*/,
          null /*root to raw map*/,
          null /*root to resolved map*/,
          null /*unresolved status*/,
          true /*add classpath
               change*/);
    }

    public ClasspathChange setResolvedClasspath(
        IClasspathEntry[] newResolvedClasspath,
        Map newRootPathToRawEntries,
        Map newRootPathToResolvedEntries,
        IJavaModelStatus newUnresolvedEntryStatus,
        int timeStamp,
        boolean addClasspathChange) {
      return setResolvedClasspath(
          newResolvedClasspath,
          null,
          newRootPathToRawEntries,
          newRootPathToResolvedEntries,
          newUnresolvedEntryStatus,
          timeStamp,
          addClasspathChange);
    }

    public synchronized ClasspathChange setResolvedClasspath(
        IClasspathEntry[] newResolvedClasspath,
        IClasspathEntry[] referencedEntries,
        Map newRootPathToRawEntries,
        Map newRootPathToResolvedEntries,
        IJavaModelStatus newUnresolvedEntryStatus,
        int timeStamp,
        boolean addClasspathChange) {
      if (this.rawTimeStamp != timeStamp) return null;
      return setClasspath(
          this.rawClasspath,
          referencedEntries,
          this.outputLocation,
          this.rawClasspathStatus,
          newResolvedClasspath,
          newRootPathToRawEntries,
          newRootPathToResolvedEntries,
          newUnresolvedEntryStatus,
          addClasspathChange);
    }

    /**
     * Reads the classpath and caches the entries. Returns a two-dimensional array, where the number
     * of elements in the row is fixed to 2. The first element is an array of raw classpath entries
     * and the second element is an array of referenced entries that may have been stored by the
     * client earlier. See {@link org.eclipse.jdt.core.IJavaProject#getReferencedClasspathEntries()}
     * for more details.
     */
    public synchronized IClasspathEntry[][] readAndCacheClasspath(JavaProject javaProject) {
      // read file entries and update status
      IClasspathEntry[][] classpath;
      IJavaModelStatus status;
      try {
        classpath =
            javaProject.readFileEntriesWithException(null /*not interested in unknown elements*/);
        status = JavaModelStatus.VERIFIED_OK;
      } catch (CoreException e) {
        classpath =
            new IClasspathEntry[][] {JavaProject.INVALID_CLASSPATH, ClasspathEntry.NO_ENTRIES};
        status =
            new JavaModelStatus(
                IJavaModelStatusConstants.INVALID_CLASSPATH_FILE_FORMAT,
                Messages.bind(
                    Messages.classpath_cannotReadClasspathFile, javaProject.getElementName()));
      } catch (IOException e) {
        classpath =
            new IClasspathEntry[][] {JavaProject.INVALID_CLASSPATH, ClasspathEntry.NO_ENTRIES};
        if (Messages.file_badFormat.equals(e.getMessage()))
          status =
              new JavaModelStatus(
                  IJavaModelStatusConstants.INVALID_CLASSPATH_FILE_FORMAT,
                  Messages.bind(
                      Messages.classpath_xmlFormatError,
                      javaProject.getElementName(),
                      Messages.file_badFormat));
        else
          status =
              new JavaModelStatus(
                  IJavaModelStatusConstants.INVALID_CLASSPATH_FILE_FORMAT,
                  Messages.bind(
                      Messages.classpath_cannotReadClasspathFile, javaProject.getElementName()));
      } catch (ClasspathEntry.AssertionFailedException e) {
        classpath =
            new IClasspathEntry[][] {JavaProject.INVALID_CLASSPATH, ClasspathEntry.NO_ENTRIES};
        status =
            new JavaModelStatus(
                IJavaModelStatusConstants.INVALID_CLASSPATH_FILE_FORMAT,
                Messages.bind(
                    Messages.classpath_illegalEntryInClasspathFile,
                    new String[] {javaProject.getElementName(), e.getMessage()}));
      }

      // extract out the output location
      int rawClasspathLength = classpath[0].length;
      IPath output = null;
      if (rawClasspathLength > 0) {
        IClasspathEntry entry = classpath[0][rawClasspathLength - 1];
        if (entry.getContentKind() == ClasspathEntry.K_OUTPUT) {
          output = entry.getPath();
          IClasspathEntry[] copy = new IClasspathEntry[rawClasspathLength - 1];
          System.arraycopy(classpath[0], 0, copy, 0, copy.length);
          classpath[0] = copy;
        }
      }

      // store new raw classpath, new output and new status, and null out resolved info
      setRawClasspath(classpath[0], classpath[1], output, status);

      return classpath;
    }

    public String toString() {
      StringBuffer buffer = new StringBuffer();
      buffer.append("Info for "); // $NON-NLS-1$
      //            buffer.append(this.project.getFullPath());
      buffer.append("\nRaw classpath:\n"); // $NON-NLS-1$
      if (this.rawClasspath == null) {
        buffer.append("  <null>\n"); // $NON-NLS-1$
      } else {
        for (int i = 0, length = this.rawClasspath.length; i < length; i++) {
          buffer.append("  "); // $NON-NLS-1$
          buffer.append(this.rawClasspath[i]);
          buffer.append('\n');
        }
      }
      buffer.append("Resolved classpath:\n"); // $NON-NLS-1$
      IClasspathEntry[] resolvedCP = this.resolvedClasspath;
      if (resolvedCP == null) {
        buffer.append("  <null>\n"); // $NON-NLS-1$
      } else {
        for (int i = 0, length = resolvedCP.length; i < length; i++) {
          buffer.append("  "); // $NON-NLS-1$
          buffer.append(resolvedCP[i]);
          buffer.append('\n');
        }
      }
      buffer.append("Resolved classpath status: "); // $NON-NLS-1$
      if (this.unresolvedEntryStatus == NEED_RESOLUTION)
        buffer.append("NEED RESOLUTION"); // $NON-NLS-1$
      else
        buffer.append(
            this.unresolvedEntryStatus == null
                ? "<null>\n"
                : this.unresolvedEntryStatus.toString()); // $NON-NLS-1$
      buffer.append("Output location:\n  "); // $NON-NLS-1$
      if (this.outputLocation == null) {
        buffer.append("<null>"); // $NON-NLS-1$
      } else {
        buffer.append(this.outputLocation);
      }
      return buffer.toString();
    }

    public boolean writeAndCacheClasspath(
        JavaProject javaProject,
        final IClasspathEntry[] newRawClasspath,
        IClasspathEntry[] newReferencedEntries,
        final IPath newOutputLocation)
        throws JavaModelException {
      try {
        this.writtingRawClasspath = true;
        if (newReferencedEntries == null) newReferencedEntries = this.referencedEntries;

        // write .classpath
        if (!javaProject.writeFileEntries(
            newRawClasspath, newReferencedEntries, newOutputLocation)) {
          return false;
        }
        // store new raw classpath, new output and new status, and null out resolved info
        setRawClasspath(
            newRawClasspath, newReferencedEntries, newOutputLocation, JavaModelStatus.VERIFIED_OK);
      } finally {
        this.writtingRawClasspath = false;
      }
      return true;
    }

    public boolean writeAndCacheClasspath(
        JavaProject javaProject,
        final IClasspathEntry[] newRawClasspath,
        final IPath newOutputLocation)
        throws JavaModelException {
      return writeAndCacheClasspath(javaProject, newRawClasspath, null, newOutputLocation);
    }
  }

  public static class PerWorkingCopyInfo implements IProblemRequestor {
    int useCount = 0;
    IProblemRequestor problemRequestor;
    CompilationUnit workingCopy;

    public PerWorkingCopyInfo(CompilationUnit workingCopy, IProblemRequestor problemRequestor) {
      this.workingCopy = workingCopy;
      this.problemRequestor = problemRequestor;
    }

    public void acceptProblem(IProblem problem) {
      IProblemRequestor requestor = getProblemRequestor();
      if (requestor == null) return;
      requestor.acceptProblem(problem);
    }

    public void beginReporting() {
      IProblemRequestor requestor = getProblemRequestor();
      if (requestor == null) return;
      requestor.beginReporting();
    }

    public void endReporting() {
      IProblemRequestor requestor = getProblemRequestor();
      if (requestor == null) return;
      requestor.endReporting();
    }

    public IProblemRequestor getProblemRequestor() {
      if (this.problemRequestor == null && this.workingCopy.owner != null) {
        return this.workingCopy.owner.getProblemRequestor(this.workingCopy);
      }
      return this.problemRequestor;
    }

    public ICompilationUnit getWorkingCopy() {
      return this.workingCopy;
    }

    public boolean isActive() {
      IProblemRequestor requestor = getProblemRequestor();
      return requestor != null && requestor.isActive();
    }

    public String toString() {
      StringBuffer buffer = new StringBuffer();
      buffer.append("Info for "); // $NON-NLS-1$
      buffer.append(((JavaElement) this.workingCopy).toStringWithAncestors());
      buffer.append("\nUse count = "); // $NON-NLS-1$
      buffer.append(this.useCount);
      buffer.append("\nProblem requestor:\n  "); // $NON-NLS-1$
      buffer.append(this.problemRequestor);
      if (this.problemRequestor == null) {
        IProblemRequestor requestor = getProblemRequestor();
        buffer.append("\nOwner problem requestor:\n  "); // $NON-NLS-1$
        buffer.append(requestor);
      }
      return buffer.toString();
    }
  }

  /**
   * Adds a path to the external files cache. It is the responsibility of callers to determine the
   * file's existence, as determined by {@link File#isFile()}.
   */
  public void addExternalFile(IPath path) {
    // unlikely to be null
    if (this.externalFiles == null) {
      this.externalFiles = Collections.synchronizedSet(new HashSet());
    }
    if (this.externalFiles != null) {
      this.externalFiles.add(path);
    }
  }

  public JavaWorkspaceScope getWorkspaceScope() {
    //        if (this.workspaceScope == null) {
    this.workspaceScope = new JavaWorkspaceScope();
    //        }
    return this.workspaceScope;
  }
}
