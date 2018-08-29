/**
 * ***************************************************************************** Copyright (c) 2004,
 * 2012 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.che.jdt.internal.core.builder;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.internal.codeassist.ISearchRequestor;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException;
import org.eclipse.jdt.internal.compiler.env.AccessRuleSet;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.compiler.util.SimpleSet;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.eclipse.jdt.internal.core.util.Util;

public class ClasspathJar extends CodenvyClasspathLocation {

  //    static SimpleLookupTable PackageCache = new SimpleLookupTable();
  String zipFilename; // keep for equals
  IFile resource;
  ZipFile zipFile;
  long lastModified;
  boolean closeZipFileAtEnd;
  volatile SimpleSet knownPackageNames;
  AccessRuleSet accessRuleSet;
  Set<String[]> packageNames;

  ClasspathJar(IFile resource, AccessRuleSet accessRuleSet) {
    this.resource = resource;
    try {
      java.net.URI location = resource.getLocationURI();
      if (location == null) {
        this.zipFilename = ""; // $NON-NLS-1$
      } else {
        File localFile = Util.toLocalFile(location, null);
        this.zipFilename = localFile.getPath();
      }
    } catch (CoreException e) {
      // ignore
    }
    this.zipFile = null;
    this.knownPackageNames = null;
    packageNames = null;
    this.accessRuleSet = accessRuleSet;
  }

  ClasspathJar(String zipFilename, long lastModified, AccessRuleSet accessRuleSet) {
    this.zipFilename = zipFilename;
    this.lastModified = lastModified;
    this.zipFile = null;
    this.knownPackageNames = null;
    this.accessRuleSet = accessRuleSet;
  }

  public ClasspathJar(ZipFile zipFile, AccessRuleSet accessRuleSet) {
    this.zipFilename = zipFile.getName();
    this.zipFile = zipFile;
    this.closeZipFileAtEnd = true;
    this.knownPackageNames = null;
    this.accessRuleSet = accessRuleSet;
  }

  /**
   * Calculate and cache the package list available in the zipFile.
   *
   * @param jar The ClasspathJar to use
   * @return A SimpleSet with the all the package names in the zipFile.
   */
  static SimpleSet findPackageSet(ClasspathJar jar) {
    String zipFileName = jar.zipFilename;

    SimpleSet packageSet = new SimpleSet(41);
    packageSet.add(""); // $NON-NLS-1$
    nextEntry:
    for (Enumeration e = jar.zipFile.entries(); e.hasMoreElements(); ) {
      String fileName = ((ZipEntry) e.nextElement()).getName();

      // add the package name & all of its parent packages
      int last = fileName.lastIndexOf('/');
      while (last > 0) {
        // extract the package name
        String packageName = fileName.substring(0, last);
        String[] splittedName = Util.splitOn('/', packageName, 0, packageName.length());
        for (String s : splittedName) {
          if (!org.eclipse.jdt.internal.core.util.Util.isValidFolderNameForPackage(
              s, "1.7", "1.7")) {
            continue nextEntry;
          }
        }

        if (packageSet.addIfNotIncluded(packageName) == null) continue nextEntry; // already existed

        last = packageName.lastIndexOf('/');
      }
    }

    return packageSet;
  }

  @Override
  public void findPackages(String[] name, ISearchRequestor requestor) {
    SimpleSet knownPackageNames = getKnownPackages();
    for (Object value : knownPackageNames.values) {
      if (value == null) {
        continue;
      }
      String pkg = value.toString();
      String[] pkgName = Util.splitOn('/', pkg, 0, pkg.length());

      if (pkgName != null && Util.startsWithIgnoreCase(pkgName, name, true)) {
        requestor.acceptPackage(Util.concatWith(pkgName, '.').toCharArray());
      }
    }
  }

  private SimpleSet getKnownPackages() {
    SimpleSet packageNames = knownPackageNames;
    if (packageNames == null) {
      synchronized (this) {
        packageNames = knownPackageNames;
        if (packageNames == null) {
          packageNames = readPackages();
          knownPackageNames = packageNames;
        }
      }
    }
    return packageNames;
  }

  public void cleanup() {
    if (this.zipFile != null && this.closeZipFileAtEnd) {
      try {
        this.zipFile.close();
      } catch (IOException e) { // ignore it
      }
      this.zipFile = null;
    }
    this.knownPackageNames = null;
  }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ClasspathJar)) return false;

    ClasspathJar jar = (ClasspathJar) o;
    if (this.accessRuleSet != jar.accessRuleSet)
      if (this.accessRuleSet == null || !this.accessRuleSet.equals(jar.accessRuleSet)) return false;
    return this.zipFilename.equals(jar.zipFilename) && lastModified() == jar.lastModified();
  }

  public NameEnvironmentAnswer findClass(
      String binaryFileName, String qualifiedPackageName, String qualifiedBinaryFileName) {
    if (!isPackage(qualifiedPackageName)) return null; // most common case

    try {
      ClassFileReader reader = ClassFileReader.read(this.zipFile, qualifiedBinaryFileName);
      if (reader != null) {
        if (this.accessRuleSet == null) return new NameEnvironmentAnswer(reader, null);
        String fileNameWithoutExtension =
            qualifiedBinaryFileName.substring(
                0, qualifiedBinaryFileName.length() - SuffixConstants.SUFFIX_CLASS.length);
        return new NameEnvironmentAnswer(
            reader,
            this.accessRuleSet.getViolatedRestriction(fileNameWithoutExtension.toCharArray()));
      }
    } catch (IOException | ClassFormatException e) { // treat as if class file is missing
    }
    return null;
  }

  public IPath getProjectRelativePath() {
    if (this.resource == null) return null;
    return this.resource.getProjectRelativePath();
  }

  public int hashCode() {
    return this.zipFilename == null ? super.hashCode() : this.zipFilename.hashCode();
  }

  public boolean isPackage(String qualifiedPackageName) {
    SimpleSet knownPackages = getKnownPackages();
    return knownPackages.includes(qualifiedPackageName);
  }

  private SimpleSet readPackages() {
    try {
      if (this.zipFile == null) {
        if (org.eclipse.jdt.internal.core.JavaModelManager.ZIP_ACCESS_VERBOSE) {
          System.out.println(
              "("
                  + Thread.currentThread()
                  + ") [ClasspathJar.isPackage(String)] Creating ZipFile on "
                  + this.zipFilename); // $NON-NLS-1$	//$NON-NLS-2$
        }
        this.zipFile = new ZipFile(this.zipFilename);
        this.closeZipFileAtEnd = true;
      }
      return findPackageSet(this);
    } catch (Exception e) {
      return new SimpleSet(); // assume for this build the zipFile is empty
    }
  }

  public long lastModified() {
    if (this.lastModified == 0) this.lastModified = new File(this.zipFilename).lastModified();
    return this.lastModified;
  }

  public String toString() {
    String start = "Classpath jar file " + this.zipFilename; // $NON-NLS-1$
    if (this.accessRuleSet == null) return start;
    return start + " with " + this.accessRuleSet; // $NON-NLS-1$
  }

  public String debugPathString() {
    long time = lastModified();
    if (time == 0) return this.zipFilename;
    return this.zipFilename + '(' + (new Date(time)) + " : " + time + ')'; // $NON-NLS-1$
  }
}
