/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2013 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.che.jdt.core.launching;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.eclipse.che.jdt.core.launching.environments.IExecutionEnvironment;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.osgi.util.NLS;

/** JRE Container - resolves a classpath container variable to a JRE */
public class JREContainer implements IClasspathContainer {

  /** Corresponding JRE */
  private IVMInstallType fVMInstall = null;

  /** Container path used to resolve to this JRE */
  private IPath fPath = null;

  /** The project this container is for */
  private IJavaProject fProject = null;

  /** Cache of classpath entries per VM install. Cleared when a VM changes. */
  private static Map<IVMInstallType, IClasspathEntry[]> fgClasspathEntries = new HashMap<>(10);

  /** Variable to return an empty array of <code>IAccessRule</code>s */
  private static IAccessRule[] EMPTY_RULES = new IAccessRule[0];

  /**
   * Map of {IVMInstall -> Map of {{IExeuctionEnvironment, IAccessRule[][]} -> {IClasspathEntry[]}}
   */
  private static Map<RuleKey, RuleEntry> fgClasspathEntriesWithRules =
      new HashMap<RuleKey, RuleEntry>(10);

  /**
   * A single key entry for the cache of access rules and classpath entries A rule key is made up of
   * an <code>IVMInstall</code> and an execution environment id
   *
   * @since 3.3
   */
  static class RuleKey {
    private String fEnvironmentId = null;
    private IVMInstallType fInstall = null;

    /**
     * Constructor
     *
     * @param install the VM
     * @param environmentId the environment
     */
    public RuleKey(IVMInstallType install, String environmentId) {
      fInstall = install;
      fEnvironmentId = environmentId;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
      if (obj instanceof RuleKey) {
        RuleKey key = (RuleKey) obj;
        return fEnvironmentId.equals(key.fEnvironmentId) && fInstall.equals(key.fInstall);
      }
      return false;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
      return fEnvironmentId.hashCode() + fInstall.hashCode();
    }
  }

  /**
   * Holds an entry for the cache of access rules/classpath entries. An entry is made up of an array
   * of classpath entries and the collection of access rules.
   *
   * @since 3.3
   */
  static class RuleEntry {
    private IAccessRule[][] fRules = null;
    private IClasspathEntry[] fEntries = null;

    /**
     * Constructor
     *
     * @param rules the rules
     * @param entries the entries
     */
    public RuleEntry(IAccessRule[][] rules, IClasspathEntry[] entries) {
      fRules = rules;
      fEntries = entries;
    }

    /**
     * Returns the collection of classpath entries for this RuleEntry
     *
     * @return the cached array of classpath entries
     */
    public IClasspathEntry[] getClasspathEntries() {
      return fEntries;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
      IAccessRule[][] rules = null;
      if (obj instanceof RuleEntry) {
        rules = ((RuleEntry) obj).fRules;
      }
      if (obj instanceof IAccessRule[][]) {
        rules = (IAccessRule[][]) obj;
      }
      if (fRules == rules) {
        return true;
      }
      if (rules != null) {
        if (fRules.length == rules.length) {
          for (int i = 0; i < fRules.length; i++) {
            if (!rulesEqual(fRules[i], rules[i])) {
              return false;
            }
          }
          return true;
        }
      }
      return false;
    }

    /**
     * Checks if the two arrays of rules are equal (same rules in each position in the array)
     *
     * @param a First list of rules to compare, must not be <code>null</code>
     * @param b Second list of rules to compare, must not be <code>null</code>
     * @return <code>true</code> if the arrays are equal, <code>false</code> otherwise
     */
    private static boolean rulesEqual(IAccessRule[] a, IAccessRule[] b) {
      if (a == b) {
        return true;
      }
      if (a.length != b.length) {
        return false;
      }
      for (int j = 0; j < a.length; j++) {
        if (!a[j].equals(b[j])) {
          return false;
        }
      }
      return true;
    }
  }

  //	/**
  //	 * Add a VM changed listener to clear cached values when a VM changes or is removed
  //	 */
  //	static {
  //		IVMInstallChangedListener listener = new IVMInstallChangedListener() {
  //
  //			/* (non-Javadoc)
  //			 * @see
  // org.eclipse.jdt.launching.IVMInstallChangedListener#defaultVMInstallChanged(org.eclipse.jdt.launching.IVMInstall, org
  // .eclipse.jdt.launching.IVMInstall)
  //			 */
  //			public void defaultVMInstallChanged(IVMInstall previous, IVMInstall current) {}
  //
  //			/* (non-Javadoc)
  //			 * @see
  // org.eclipse.jdt.launching.IVMInstallChangedListener#vmAdded(org.eclipse.jdt.launching.IVMInstall)
  //			 */
  //			public void vmAdded(IVMInstall newVm) {}
  //
  //			/* (non-Javadoc)
  //			 * @see
  // org.eclipse.jdt.launching.IVMInstallChangedListener#vmChanged(org.eclipse.jdt.launching.PropertyChangeEvent)
  //			 */
  //			public void vmChanged(PropertyChangeEvent event) {
  //				if (event.getSource() != null) {
  //					fgClasspathEntries.remove(event.getSource());
  //					removeRuleEntry(event.getSource());
  //				}
  //			}
  //
  //			/* (non-Javadoc)
  //			 * @see
  // org.eclipse.jdt.launching.IVMInstallChangedListener#vmRemoved(org.eclipse.jdt.launching.IVMInstall)
  //			 */
  //			public void vmRemoved(IVMInstall removedVm) {
  //				fgClasspathEntries.remove(removedVm);
  //				removeRuleEntry(removedVm);
  //			}
  //
  //			/**
  //			 * Removes all occurrences of the given VM found as part key members in the current
  //			 * cache for classpath entries
  //			 * @param obj an object which should be castable to IVMInstall
  //			 */
  //			private void removeRuleEntry(Object obj) {
  //				if(obj instanceof IVMInstall) {
  //					IVMInstall install = (IVMInstall) obj;
  //					RuleKey key = null;
  //					ArrayList<RuleKey> list = new ArrayList<RuleKey>();
  //					for(Iterator<RuleKey> iter = fgClasspathEntriesWithRules.keySet().iterator();
  // iter.hasNext();) {
  //						key  = iter.next();
  //						if(key.fInstall.equals(install)) {
  //							list.add(key);
  //						}
  //					}
  //					for(int i = 0; i < list.size(); i++) {
  //						fgClasspathEntriesWithRules.remove(list.get(i));
  //					}
  //				}
  //			}
  //		};
  //		JavaRuntime.addVMInstallChangedListener(listener);
  //	}

  /**
   * Returns the classpath entries associated with the given VM in the context of the given path and
   * project.
   *
   * @param vm the VM
   * @param containerPath the container path resolution is for
   * @param project project the resolution is for
   * @return classpath entries
   */
  private static IClasspathEntry[] getClasspathEntries(
      IVMInstallType vm, IPath containerPath, IJavaProject project) {
    String id = null; // JavaRuntime.getExecutionEnvironmentId(containerPath);
    IClasspathEntry[] entries = null;
    if (id == null) {
      // cache classpath entries per JRE when not bound to an EE
      entries = fgClasspathEntries.get(vm);
      if (entries == null) {
        entries = computeClasspathEntries(vm, project, id);
        fgClasspathEntries.put(vm, entries);
      }
    } else {
      if (Launching.DEBUG_JRE_CONTAINER) {
        Launching.log("\tEE:\t" + id); // $NON-NLS-1$
      }
      // dynamically compute entries when bound to an EE
      entries = computeClasspathEntries(vm, project, id);
    }
    return entries;
  }

  /**
   * Evaluates library locations for a IVMInstall. If no library locations are set on the install, a
   * default location is evaluated and checked if it exists.
   *
   * @param vm the {@link org.eclipse.che.jdt.core.launching.IVMInstallType} to compute locations
   *     for
   * @return library locations with paths that exist or are empty
   * @since 2.0
   */
  public static LibraryLocation[] getLibraryLocations(IVMInstallType vm) {
    IPath[] libraryPaths;
    IPath[] sourcePaths;
    IPath[] sourceRootPaths;
    URL[] javadocLocations;
    LibraryLocation[] locations = null; // vm.getLibraryLocations();
    if (locations == null) {
      URL defJavaDocLocation = null; // vm.getJavadocLocation();
      File installLocation = vm.detectInstallLocation();
      if (installLocation == null) {
        return new LibraryLocation[0];
      }
      LibraryLocation[] dflts = vm.getDefaultLibraryLocations(installLocation);
      libraryPaths = new IPath[dflts.length];
      sourcePaths = new IPath[dflts.length];
      sourceRootPaths = new IPath[dflts.length];
      javadocLocations = new URL[dflts.length];
      for (int i = 0; i < dflts.length; i++) {
        libraryPaths[i] = dflts[i].getSystemLibraryPath();
        if (defJavaDocLocation == null) {
          javadocLocations[i] = dflts[i].getJavadocLocation();
        } else {
          javadocLocations[i] = defJavaDocLocation;
        }
        if (!libraryPaths[i].toFile().isFile()) {
          libraryPaths[i] = Path.EMPTY;
        }

        sourcePaths[i] = dflts[i].getSystemLibrarySourcePath();
        if (sourcePaths[i].toFile().isFile()) {
          sourceRootPaths[i] = dflts[i].getPackageRootPath();
        } else {
          sourcePaths[i] = Path.EMPTY;
          sourceRootPaths[i] = Path.EMPTY;
        }
      }
    } else {
      libraryPaths = new IPath[locations.length];
      sourcePaths = new IPath[locations.length];
      sourceRootPaths = new IPath[locations.length];
      javadocLocations = new URL[locations.length];
      for (int i = 0; i < locations.length; i++) {
        libraryPaths[i] = locations[i].getSystemLibraryPath();
        sourcePaths[i] = locations[i].getSystemLibrarySourcePath();
        sourceRootPaths[i] = locations[i].getPackageRootPath();
        javadocLocations[i] = locations[i].getJavadocLocation();
      }
    }
    locations = new LibraryLocation[sourcePaths.length];
    for (int i = 0; i < sourcePaths.length; i++) {
      locations[i] =
          new LibraryLocation(
              libraryPaths[i], sourcePaths[i], sourceRootPaths[i], javadocLocations[i]);
    }
    return locations;
  }

  /**
   * Computes the classpath entries associated with a VM - one entry per library in the context of
   * the given path and project.
   *
   * @param vm the VM
   * @param project the project the resolution is for
   * @param environmentId execution environment the resolution is for, or <code>null</code>
   * @return classpath entries
   */
  private static IClasspathEntry[] computeClasspathEntries(
      IVMInstallType vm, IJavaProject project, String environmentId) {
    LibraryLocation[] libs = null; // vm.getLibraryLocations();
    boolean overrideJavaDoc = false;
    if (libs == null) {
      libs = getLibraryLocations(vm);
      overrideJavaDoc = true;
    }
    IAccessRule[][] rules = null;
    //		if (environmentId != null) {
    // compute access rules for execution environment
    IExecutionEnvironment environment = JavaRuntime.getEnvironment(environmentId);
    if (environment != null) {
      rules = environment.getAccessRules(vm, libs, project);
    }
    //		}
    RuleKey key = null;
    if (vm != null && rules != null && environmentId != null) {
      key = new RuleKey(vm, environmentId);
      RuleEntry entry = fgClasspathEntriesWithRules.get(key);
      if (entry != null && entry.equals(rules)) {
        return entry.getClasspathEntries();
      }
    }
    List<IClasspathEntry> entries = new ArrayList<IClasspathEntry>(libs.length);
    for (int i = 0; i < libs.length; i++) {
      if (!libs[i].getSystemLibraryPath().isEmpty()) {
        IPath sourcePath = libs[i].getSystemLibrarySourcePath();
        if (sourcePath.isEmpty()) {
          sourcePath = null;
        }
        IPath rootPath = libs[i].getPackageRootPath();
        if (rootPath.isEmpty()) {
          rootPath = null;
        }
        // construct the classpath attributes for this library location
        IClasspathAttribute[] attributes =
            JREContainer.buildClasspathAttributes(vm, libs[i], overrideJavaDoc);
        IAccessRule[] libRules = null;
        if (rules != null) {
          libRules = rules[i];
        } else {
          libRules = EMPTY_RULES;
        }
        entries.add(
            JavaCore.newLibraryEntry(
                libs[i].getSystemLibraryPath(), sourcePath, rootPath, libRules, attributes, false));
      }
    }
    IClasspathEntry[] cpEntries = entries.toArray(new IClasspathEntry[entries.size()]);
    if (key != null && rules != null) {
      fgClasspathEntriesWithRules.put(key, new RuleEntry(rules, cpEntries));
    }
    return cpEntries;
  }

  private static IClasspathAttribute[] buildClasspathAttributes(
      final IVMInstallType vm, final LibraryLocation lib, final boolean overrideJavaDoc) {

    List<IClasspathAttribute> classpathAttributes = new LinkedList<IClasspathAttribute>();
    // process the javadoc location
    URL javadocLocation = lib.getJavadocLocation();
    if (overrideJavaDoc && javadocLocation == null) {
      javadocLocation = null; // vm.getJavadocLocation();
    }
    if (javadocLocation != null) {
      IClasspathAttribute javadocCPAttribute =
          JavaCore.newClasspathAttribute(
              IClasspathAttribute.JAVADOC_LOCATION_ATTRIBUTE_NAME,
              javadocLocation.toExternalForm());
      classpathAttributes.add(javadocCPAttribute);
    }
    // process the index location
    URL indexLocation = lib.getIndexLocation();
    if (indexLocation != null) {
      IClasspathAttribute indexCPLocation =
          JavaCore.newClasspathAttribute(
              IClasspathAttribute.INDEX_LOCATION_ATTRIBUTE_NAME, indexLocation.toExternalForm());
      classpathAttributes.add(indexCPLocation);
    }
    return classpathAttributes.toArray(new IClasspathAttribute[classpathAttributes.size()]);
  }

  /**
   * Constructs a JRE classpath container on the given VM install
   *
   * @param vm VM install - cannot be <code>null</code>
   * @param path container path used to resolve this JRE
   * @param project the project context
   */
  public JREContainer(IVMInstallType vm, IPath path, IJavaProject project) {
    fVMInstall = vm;
    fPath = path;
    fProject = project;
  }

  /** @see IClasspathContainer#getClasspathEntries() */
  public IClasspathEntry[] getClasspathEntries() {
    if (Launching.DEBUG_JRE_CONTAINER) {
      Launching.log("<JRE_CONTAINER> getClasspathEntries() " + this.toString()); // $NON-NLS-1$
      Launching.log("\tJRE:\t" + fVMInstall.getName()); // $NON-NLS-1$
      Launching.log("\tPath:\t" + getPath().toString()); // $NON-NLS-1$
      Launching.log("\tProj:\t" + fProject.getProject().getName()); // $NON-NLS-1$
    }
    IClasspathEntry[] entries = getClasspathEntries(fVMInstall, getPath(), fProject);
    if (Launching.DEBUG_JRE_CONTAINER) {
      Launching.log("\tResolved " + entries.length + " entries:"); // $NON-NLS-1$//$NON-NLS-2$
    }
    return entries;
  }

  /** @see IClasspathContainer#getDescription() */
  public String getDescription() {
    String environmentId = null; // JavaRuntime.getExecutionEnvironmentId(getPath());
    String tag = null;
    if (environmentId == null) {
      tag = fVMInstall.getName();
    } else {
      tag = environmentId;
    }
    return NLS.bind("JRE System Library [{0}]", new String[] {tag});
  }

  /** @see IClasspathContainer#getKind() */
  public int getKind() {
    return IClasspathContainer.K_DEFAULT_SYSTEM;
  }

  /** @see IClasspathContainer#getPath() */
  public IPath getPath() {
    return fPath;
  }
}
