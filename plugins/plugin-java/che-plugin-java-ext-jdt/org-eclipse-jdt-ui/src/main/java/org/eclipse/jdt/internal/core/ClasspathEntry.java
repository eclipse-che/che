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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaModelStatus;
import org.eclipse.jdt.core.IJavaModelStatusConstants;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.env.AccessRestriction;
import org.eclipse.jdt.internal.compiler.env.AccessRule;
import org.eclipse.jdt.internal.compiler.env.AccessRuleSet;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.util.ManifestAnalyzer;
import org.eclipse.jdt.internal.core.util.Messages;
import org.eclipse.jdt.internal.core.util.Util;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/** @see org.eclipse.jdt.core.IClasspathEntry */
public class ClasspathEntry implements IClasspathEntry {

  public static final String TAG_CLASSPATH = "classpath"; // $NON-NLS-1$
  public static final String TAG_CLASSPATHENTRY = "classpathentry"; // $NON-NLS-1$
  public static final String TAG_REFERENCED_ENTRY = "referencedentry"; // $NON-NLS-1$
  public static final String TAG_OUTPUT = "output"; // $NON-NLS-1$
  public static final String TAG_KIND = "kind"; // $NON-NLS-1$
  public static final String TAG_PATH = "path"; // $NON-NLS-1$
  public static final String TAG_SOURCEPATH = "sourcepath"; // $NON-NLS-1$
  public static final String TAG_ROOTPATH = "rootpath"; // $NON-NLS-1$
  public static final String TAG_EXPORTED = "exported"; // $NON-NLS-1$
  public static final String TAG_INCLUDING = "including"; // $NON-NLS-1$
  public static final String TAG_EXCLUDING = "excluding"; // $NON-NLS-1$
  public static final String TAG_ATTRIBUTES = "attributes"; // $NON-NLS-1$
  public static final String TAG_ATTRIBUTE = "attribute"; // $NON-NLS-1$
  public static final String TAG_ATTRIBUTE_NAME = "name"; // $NON-NLS-1$
  public static final String TAG_ATTRIBUTE_VALUE = "value"; // $NON-NLS-1$
  public static final String TAG_COMBINE_ACCESS_RULES = "combineaccessrules"; // $NON-NLS-1$
  public static final String TAG_ACCESS_RULES = "accessrules"; // $NON-NLS-1$
  public static final String TAG_ACCESS_RULE = "accessrule"; // $NON-NLS-1$
  public static final String TAG_PATTERN = "pattern"; // $NON-NLS-1$
  public static final String TAG_ACCESSIBLE = "accessible"; // $NON-NLS-1$
  public static final String TAG_NON_ACCESSIBLE = "nonaccessible"; // $NON-NLS-1$
  public static final String TAG_DISCOURAGED = "discouraged"; // $NON-NLS-1$
  public static final String TAG_IGNORE_IF_BETTER = "ignoreifbetter"; // $NON-NLS-1$
  private static final IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
  // $NON-NLS-1$
  public static final ClasspathEntry[] NO_ENTRIES = new ClasspathEntry[0];
  /*
   * Default inclusion pattern set
   */
  public static final IPath[] INCLUDE_ALL = {};
  /*
   * Default exclusion pattern set
   */
  public static final IPath[] EXCLUDE_NONE = {};
  /*
   * Default extra attributes
   */
  public static final IClasspathAttribute[] NO_EXTRA_ATTRIBUTES = {};
  /*
   * Default access rules
   */
  public static final IAccessRule[] NO_ACCESS_RULES = {};
  /** A constant indicating an output location. */
  public static final int K_OUTPUT = 10;

  public static final String DOT_DOT = ".."; // $NON-NLS-1$
  private static final char[][] UNINIT_PATTERNS =
      new char[][] {"Non-initialized yet".toCharArray()};
  private static final IPath[] NO_PATHS = new IPath[0];
  /**
   * Describes the kind of classpath entry - one of CPE_PROJECT, CPE_LIBRARY, CPE_SOURCE,
   * CPE_VARIABLE or CPE_CONTAINER
   */
  public int entryKind;
  /**
   * Describes the kind of package fragment roots found on this classpath entry - either K_BINARY or
   * K_SOURCE or K_OUTPUT.
   */
  public int contentKind;
  //    private final static IWorkspaceRoot                                 workspaceRoot   =
  // ResourcesPlugin.getWorkspace().getRoot();
  /**
   * The meaning of the path of a classpath entry depends on its entry kind:
   *
   * <ul>
   *   <li>Source code in the current project (<code>CPE_SOURCE</code>) - The path associated with
   *       this entry is the absolute path to the root folder.
   *   <li>A binary library in the current project (<code>CPE_LIBRARY</code>) - the path associated
   *       with this entry is the absolute path to the JAR (or root folder), and in case it refers
   *       to an external JAR, then there is no associated resource in the workbench.
   *   <li>A required project (<code>CPE_PROJECT</code>) - the path of the entry denotes the path to
   *       the corresponding project resource.
   *   <li>A variable entry (<code>CPE_VARIABLE</code>) - the first segment of the path is the name
   *       of a classpath variable. If this classpath variable is bound to the path <it>P</it>, the
   *       path of the corresponding classpath entry is computed by appending to <it>P</it> the
   *       segments of the returned path without the variable.
   *   <li>A container entry (<code>CPE_CONTAINER</code>) - the first segment of the path is
   *       denoting the unique container identifier (for which a <code>ClasspathContainerInitializer
   *       </code> could be registered), and the remaining segments are used as additional hints for
   *       resolving the container entry to an actual <code>IClasspathContainer</code>.
   */
  public IPath path;
  /**
   * Describes the path to the source archive associated with this classpath entry, or <code>null
   * </code> if this classpath entry has no source attachment.
   *
   * <p>Only library and variable classpath entries may have source attachments. For library
   * classpath entries, the result path (if present) locates a source archive. For variable
   * classpath entries, the result path (if present) has an analogous form and meaning as the
   * variable path, namely the first segment is the name of a classpath variable.
   */
  public IPath sourceAttachmentPath;
  /**
   * Describes the path within the source archive where package fragments are located. An empty path
   * indicates that packages are located at the root of the source archive. Returns a non-<code>null
   * </code> value if and only if <code>getSourceAttachmentPath</code> returns a non-<code>null
   * </code> value.
   */
  public IPath sourceAttachmentRootPath;
  /** See {@link org.eclipse.jdt.core.IClasspathEntry#getReferencingEntry()} */
  public IClasspathEntry referencingEntry;
  /** Specific output location (for this source entry) */
  public IPath specificOutputLocation;
  /** The export flag */
  public boolean isExported;
  /** The extra attributes */
  public IClasspathAttribute[] extraAttributes;
  /**
   * Patterns allowing to include/exclude portions of the resource tree denoted by this entry path.
   */
  private IPath[] inclusionPatterns;

  private char[][] fullInclusionPatternChars;
  private IPath[] exclusionPatterns;
  private char[][] fullExclusionPatternChars;
  private boolean combineAccessRules;
  private String rootID;
  private AccessRuleSet accessRuleSet;

  public ClasspathEntry(
      int contentKind,
      int entryKind,
      IPath path,
      IPath[] inclusionPatterns,
      IPath[] exclusionPatterns,
      IPath sourceAttachmentPath,
      IPath sourceAttachmentRootPath,
      IPath specificOutputLocation,
      boolean isExported,
      IAccessRule[] accessRules,
      boolean combineAccessRules,
      IClasspathAttribute[] extraAttributes) {

    this(
        contentKind,
        entryKind,
        path,
        inclusionPatterns,
        exclusionPatterns,
        sourceAttachmentPath,
        sourceAttachmentRootPath,
        specificOutputLocation,
        null,
        isExported,
        accessRules,
        combineAccessRules,
        extraAttributes);
  }

  /** Creates a class path entry of the specified kind with the given path. */
  public ClasspathEntry(
      int contentKind,
      int entryKind,
      IPath path,
      IPath[] inclusionPatterns,
      IPath[] exclusionPatterns,
      IPath sourceAttachmentPath,
      IPath sourceAttachmentRootPath,
      IPath specificOutputLocation,
      IClasspathEntry referencingEntry,
      boolean isExported,
      IAccessRule[] accessRules,
      boolean combineAccessRules,
      IClasspathAttribute[] extraAttributes) {

    this.contentKind = contentKind;
    this.entryKind = entryKind;
    this.path = path;
    this.inclusionPatterns = inclusionPatterns;
    this.exclusionPatterns = exclusionPatterns;
    this.referencingEntry = referencingEntry;

    int length;
    if (accessRules != null && (length = accessRules.length) > 0) {
      AccessRule[] rules = new AccessRule[length];
      System.arraycopy(accessRules, 0, rules, 0, length);
      byte classpathEntryType;
      String classpathEntryName;
      JavaModelManager manager = JavaModelManager.getJavaModelManager();
      if (this.entryKind == IClasspathEntry.CPE_PROJECT
          || this.entryKind
              == IClasspathEntry.CPE_SOURCE) { // can be remote source entry when reconciling
        classpathEntryType = AccessRestriction.PROJECT;
        classpathEntryName = manager.intern(getPath().segment(0));
      } else {
        classpathEntryType = AccessRestriction.LIBRARY;
        //                Object target = JavaModel.getWorkspaceTarget(path);
        //                if (target == null) {
        classpathEntryName = manager.intern(path.toOSString());
        //                } else {
        //                    classpathEntryName = manager.intern(path.makeRelative().toString());
        //                }
      }
      this.accessRuleSet = new AccessRuleSet(rules, classpathEntryType, classpathEntryName);
    }
    //		else { -- implicit!
    //			this.accessRuleSet = null;
    //		}

    this.combineAccessRules = combineAccessRules;
    this.extraAttributes = extraAttributes;

    if (inclusionPatterns != INCLUDE_ALL && inclusionPatterns.length > 0) {
      this.fullInclusionPatternChars = UNINIT_PATTERNS;
    }
    if (exclusionPatterns.length > 0) {
      this.fullExclusionPatternChars = UNINIT_PATTERNS;
    }
    this.sourceAttachmentPath = sourceAttachmentPath;
    this.sourceAttachmentRootPath = sourceAttachmentRootPath;
    this.specificOutputLocation = specificOutputLocation;
    this.isExported = isExported;
  }

  static IClasspathAttribute[] decodeExtraAttributes(NodeList attributes) {
    if (attributes == null) return NO_EXTRA_ATTRIBUTES;
    int length = attributes.getLength();
    if (length == 0) return NO_EXTRA_ATTRIBUTES;
    IClasspathAttribute[] result = new IClasspathAttribute[length];
    int index = 0;
    for (int i = 0; i < length; ++i) {
      Node node = attributes.item(i);
      if (node.getNodeType() == Node.ELEMENT_NODE) {
        Element attribute = (Element) node;
        String name = attribute.getAttribute(TAG_ATTRIBUTE_NAME);
        if (name == null) continue;
        String value = attribute.getAttribute(TAG_ATTRIBUTE_VALUE);
        if (value == null) continue;
        result[index++] = new ClasspathAttribute(name, value);
      }
    }
    if (index != length)
      System.arraycopy(result, 0, result = new IClasspathAttribute[index], 0, index);
    return result;
  }

  static IAccessRule[] decodeAccessRules(NodeList list) {
    if (list == null) return null;
    int length = list.getLength();
    if (length == 0) return null;
    IAccessRule[] result = new IAccessRule[length];
    int index = 0;
    for (int i = 0; i < length; i++) {
      Node accessRule = list.item(i);
      if (accessRule.getNodeType() == Node.ELEMENT_NODE) {
        Element elementAccessRule = (Element) accessRule;
        String pattern = elementAccessRule.getAttribute(TAG_PATTERN);
        if (pattern == null) continue;
        String tagKind = elementAccessRule.getAttribute(TAG_KIND);
        int kind;
        if (TAG_ACCESSIBLE.equals(tagKind)) kind = IAccessRule.K_ACCESSIBLE;
        else if (TAG_NON_ACCESSIBLE.equals(tagKind)) kind = IAccessRule.K_NON_ACCESSIBLE;
        else if (TAG_DISCOURAGED.equals(tagKind)) kind = IAccessRule.K_DISCOURAGED;
        else continue;
        boolean ignoreIfBetter =
            "true".equals(elementAccessRule.getAttribute(TAG_IGNORE_IF_BETTER)); // $NON-NLS-1$
        result[index++] =
            new ClasspathAccessRule(
                new Path(pattern), ignoreIfBetter ? kind | IAccessRule.IGNORE_IF_BETTER : kind);
      }
    }
    if (index != length) System.arraycopy(result, 0, result = new IAccessRule[index], 0, index);
    return result;
  }

  /** Decode some element tag containing a sequence of patterns into IPath[] */
  private static IPath[] decodePatterns(NamedNodeMap nodeMap, String tag) {
    String sequence = removeAttribute(tag, nodeMap);
    if (!sequence.equals("")) { // $NON-NLS-1$
      char[][] patterns = CharOperation.splitOn('|', sequence.toCharArray());
      int patternCount;
      if ((patternCount = patterns.length) > 0) {
        IPath[] paths = new IPath[patternCount];
        int index = 0;
        for (int j = 0; j < patternCount; j++) {
          char[] pattern = patterns[j];
          if (pattern.length == 0)
            continue; // see https://bugs.eclipse.org/bugs/show_bug.cgi?id=105581
          paths[index++] = new Path(new String(pattern));
        }
        if (index < patternCount) System.arraycopy(paths, 0, paths = new IPath[index], 0, index);
        return paths;
      }
    }
    return null;
  }

  /*
   * Returns whether the given path as a ".." segment
   */
  public static boolean hasDotDot(IPath path) {
    for (int i = 0, length = path.segmentCount(); i < length; i++) {
      if (DOT_DOT.equals(path.segment(i))) return true;
    }
    return false;
  }

  public static NodeList getChildAttributes(
      String childName, NodeList children, boolean[] foundChildren) {
    for (int i = 0, length = foundChildren.length; i < length; i++) {
      Node node = children.item(i);
      if (childName.equals(node.getNodeName())) {
        foundChildren[i] = true;
        return node.getChildNodes();
      }
    }
    return null;
  }

  private static String removeAttribute(String nodeName, NamedNodeMap nodeMap) {
    Node node = removeNode(nodeName, nodeMap);
    if (node == null) return ""; // //$NON-NLS-1$
    return node.getNodeValue();
  }

  private static Node removeNode(String nodeName, NamedNodeMap nodeMap) {
    try {
      return nodeMap.removeNamedItem(nodeName);
    } catch (DOMException e) {
      if (e.code != DOMException.NOT_FOUND_ERR) throw e;
      return null;
    }
  }

  /*
   * Read the Class-Path clause of the manifest of the jar pointed by this path, and return
   * the corresponding paths.
   */
  public static IPath[] resolvedChainedLibraries(IPath jarPath) {
    ArrayList result = new ArrayList();
    resolvedChainedLibraries(jarPath, new HashSet(), result);
    if (result.size() == 0) return NO_PATHS;
    return (IPath[]) result.toArray(new IPath[result.size()]);
  }

  private static void decodeUnknownNode(Node node, StringBuffer buffer, IJavaProject project) {
    ByteArrayOutputStream s = new ByteArrayOutputStream();
    OutputStreamWriter writer;
    try {
      writer = new OutputStreamWriter(s, "UTF8"); // $NON-NLS-1$
      XMLWriter xmlWriter = new XMLWriter(writer, project, false /*don't print XML version*/);
      decodeUnknownNode(node, xmlWriter, true /*insert new line*/);
      xmlWriter.flush();
      xmlWriter.close();
      buffer.append(s.toString("UTF8")); // $NON-NLS-1$
    } catch (UnsupportedEncodingException e) {
      // ignore (UTF8 is always supported)
    }
  }

  private static void decodeUnknownNode(Node node, XMLWriter xmlWriter, boolean insertNewLine) {
    switch (node.getNodeType()) {
      case Node.ELEMENT_NODE:
        NamedNodeMap attributes;
        HashMap parameters = null;
        if ((attributes = node.getAttributes()) != null) {
          int length = attributes.getLength();
          if (length > 0) {
            parameters = new HashMap();
            for (int i = 0; i < length; i++) {
              Node attribute = attributes.item(i);
              parameters.put(attribute.getNodeName(), attribute.getNodeValue());
            }
          }
        }
        NodeList children = node.getChildNodes();
        int childrenLength = children.getLength();
        String nodeName = node.getNodeName();
        xmlWriter.printTag(
            nodeName,
            parameters,
            false /*don't insert tab*/,
            false /*don't insert new line*/,
            childrenLength == 0 /*close
tag if no children*/);
        if (childrenLength > 0) {
          for (int i = 0; i < childrenLength; i++) {
            decodeUnknownNode(children.item(i), xmlWriter, false /*don't insert new line*/);
          }
          xmlWriter.endTag(nodeName, false /*don't insert tab*/, insertNewLine);
        }
        break;
      case Node.TEXT_NODE:
        String data = ((Text) node).getData();
        xmlWriter.printString(data, false /*don't insert tab*/, false /*don't insert new line*/);
        break;
    }
  }

  private static void resolvedChainedLibraries(IPath jarPath, HashSet visited, ArrayList result) {
    if (visited.contains(jarPath)) return;
    visited.add(jarPath);
    JavaModelManager manager = JavaModelManager.getJavaModelManager();
    if (manager.isNonChainingJar(jarPath)) return;
    List calledFileNames = getCalledFileNames(jarPath);
    if (calledFileNames == null) {
      manager.addNonChainingJar(jarPath);
    } else {
      Iterator calledFilesIterator = calledFileNames.iterator();
      IPath directoryPath = jarPath.removeLastSegments(1);
      while (calledFilesIterator.hasNext()) {
        String calledFileName = (String) calledFilesIterator.next();
        if (!directoryPath.isValidPath(calledFileName)) {
          if (JavaModelManager.CP_RESOLVE_VERBOSE_FAILURE) {
            Util.verbose(
                "Invalid Class-Path entry "
                    + calledFileName
                    + " in manifest of jar file: "
                    + jarPath.toOSString()); // $NON-NLS-1$ //$NON-NLS-2$
          }
        } else {
          IPath calledJar = directoryPath.append(new Path(calledFileName));
          // Ignore if segment count is Zero (https://bugs.eclipse.org/bugs/show_bug.cgi?id=308150)
          if (calledJar.segmentCount() == 0) {
            if (JavaModelManager.CP_RESOLVE_VERBOSE_FAILURE) {
              Util.verbose(
                  "Invalid Class-Path entry "
                      + calledFileName
                      + " in manifest of jar file: "
                      + jarPath.toOSString()); // $NON-NLS-1$ //$NON-NLS-2$
            }
            continue;
          }
          resolvedChainedLibraries(calledJar, visited, result);
          result.add(calledJar);
        }
      }
    }
  }

  private static List getCalledFileNames(IPath jarPath) {
    Object target =
        JavaModel.getTarget(
            jarPath, true /*check existence, otherwise the manifest cannot be read*/);
    if (!(target instanceof IFile || target instanceof File)) return null;
    JavaModelManager manager = JavaModelManager.getJavaModelManager();
    ZipFile zip = null;
    InputStream inputStream = null;
    List calledFileNames = null;
    try {
      zip = manager.getZipFile(jarPath);
      ZipEntry manifest = zip.getEntry("META-INF/MANIFEST.MF"); // $NON-NLS-1$
      if (manifest == null) return null;
      // non-null implies regular file
      ManifestAnalyzer analyzer = new ManifestAnalyzer();
      inputStream = zip.getInputStream(manifest);
      boolean success = analyzer.analyzeManifestContents(inputStream);
      calledFileNames = analyzer.getCalledFileNames();
      if (!success || analyzer.getClasspathSectionsCount() == 1 && calledFileNames == null) {
        if (JavaModelManager.CP_RESOLVE_VERBOSE_FAILURE) {
          Util.verbose(
              "Invalid Class-Path header in manifest of jar file: "
                  + jarPath.toOSString()); // $NON-NLS-1$
        }
        return null;
      } else if (analyzer.getClasspathSectionsCount() > 1) {
        if (JavaModelManager.CP_RESOLVE_VERBOSE_FAILURE) {
          Util.verbose(
              "Multiple Class-Path headers in manifest of jar file: "
                  + jarPath.toOSString()); // $NON-NLS-1$
        }
        return null;
      }
    } catch (CoreException e) {
      // not a zip file
      if (JavaModelManager.CP_RESOLVE_VERBOSE_FAILURE) {
        Util.verbose(
            "Could not read Class-Path header in manifest of jar file: "
                + jarPath.toOSString()); // $NON-NLS-1$
        e.printStackTrace();
      }
    } catch (IOException e) {
      // not a zip file
      if (JavaModelManager.CP_RESOLVE_VERBOSE_FAILURE) {
        Util.verbose(
            "Could not read Class-Path header in manifest of jar file: "
                + jarPath.toOSString()); // $NON-NLS-1$
        e.printStackTrace();
      }
    } finally {
      if (inputStream != null) {
        try {
          inputStream.close();
        } catch (IOException e) {
          // best effort
        }
      }
      manager.closeZipFile(zip);
    }
    return calledFileNames;
  }

  /** Returns the XML encoding of the class path. */
  public void elementEncode(
      XMLWriter writer,
      IPath projectPath,
      boolean indent,
      boolean newLine,
      Map unknownElements,
      boolean isReferencedEntry) {
    HashMap parameters = new HashMap();

    parameters.put(TAG_KIND, ClasspathEntry.kindToString(this.entryKind));

    IPath xmlPath = this.path;
    if (this.entryKind != IClasspathEntry.CPE_VARIABLE
        && this.entryKind != IClasspathEntry.CPE_CONTAINER) {
      // translate to project relative from absolute (unless a device path)
      if (xmlPath.isAbsolute()) {
        if (projectPath != null && projectPath.isPrefixOf(xmlPath)) {
          if (xmlPath.segment(0).equals(projectPath.segment(0))) {
            xmlPath = xmlPath.removeFirstSegments(projectPath.segmentCount());
            xmlPath = xmlPath.makeRelative();
          } else {
            xmlPath = xmlPath.makeAbsolute();
          }
        }
      }
    }
    parameters.put(TAG_PATH, String.valueOf(xmlPath));

    if (this.sourceAttachmentPath != null) {
      xmlPath = this.sourceAttachmentPath;
      // translate to project relative from absolute
      if (this.entryKind != IClasspathEntry.CPE_VARIABLE
          && projectPath != null
          && projectPath.isPrefixOf(xmlPath)) {
        if (xmlPath.segment(0).equals(projectPath.segment(0))) {
          xmlPath = xmlPath.removeFirstSegments(1);
          xmlPath = xmlPath.makeRelative();
        }
      }
      parameters.put(TAG_SOURCEPATH, String.valueOf(xmlPath));
    }
    if (this.sourceAttachmentRootPath != null) {
      parameters.put(TAG_ROOTPATH, String.valueOf(this.sourceAttachmentRootPath));
    }
    if (this.isExported) {
      parameters.put(TAG_EXPORTED, "true"); // $NON-NLS-1$
    }
    encodePatterns(this.inclusionPatterns, TAG_INCLUDING, parameters);
    encodePatterns(this.exclusionPatterns, TAG_EXCLUDING, parameters);
    if (this.entryKind == IClasspathEntry.CPE_PROJECT && !this.combineAccessRules)
      parameters.put(TAG_COMBINE_ACCESS_RULES, "false"); // $NON-NLS-1$

    // unknown attributes
    UnknownXmlElements unknownXmlElements =
        unknownElements == null ? null : (UnknownXmlElements) unknownElements.get(this.path);
    String[] unknownAttributes;
    if (unknownXmlElements != null && (unknownAttributes = unknownXmlElements.attributes) != null)
      for (int i = 0, length = unknownAttributes.length; i < length; i += 2) {
        String tagName = unknownAttributes[i];
        String tagValue = unknownAttributes[i + 1];
        parameters.put(tagName, tagValue);
      }

    if (this.specificOutputLocation != null) {
      IPath outputLocation = this.specificOutputLocation.removeFirstSegments(1);
      outputLocation = outputLocation.makeRelative();
      parameters.put(TAG_OUTPUT, String.valueOf(outputLocation));
    }

    boolean hasExtraAttributes = this.extraAttributes.length != 0;
    boolean hasRestrictions =
        getAccessRuleSet() != null; // access rule set is null if no access rules
    ArrayList unknownChildren = unknownXmlElements != null ? unknownXmlElements.children : null;
    boolean hasUnknownChildren = unknownChildren != null;

    /* close tag if no extra attributes, no restriction and no unknown children */
    String tagName = isReferencedEntry ? TAG_REFERENCED_ENTRY : TAG_CLASSPATHENTRY;
    writer.printTag(
        tagName,
        parameters,
        indent,
        newLine,
        !hasExtraAttributes && !hasRestrictions && !hasUnknownChildren);

    if (hasExtraAttributes) encodeExtraAttributes(writer, indent, newLine);

    if (hasRestrictions) encodeAccessRules(writer, indent, newLine);

    if (hasUnknownChildren) encodeUnknownChildren(writer, indent, newLine, unknownChildren);

    if (hasExtraAttributes || hasRestrictions || hasUnknownChildren)
      writer.endTag(tagName, indent, true /*insert new line*/);
  }

  void encodeExtraAttributes(XMLWriter writer, boolean indent, boolean newLine) {
    writer.startTag(TAG_ATTRIBUTES, indent);
    for (int i = 0; i < this.extraAttributes.length; i++) {
      IClasspathAttribute attribute = this.extraAttributes[i];
      HashMap parameters = new HashMap();
      parameters.put(TAG_ATTRIBUTE_NAME, attribute.getName());
      parameters.put(TAG_ATTRIBUTE_VALUE, attribute.getValue());
      writer.printTag(TAG_ATTRIBUTE, parameters, indent, newLine, true);
    }
    writer.endTag(TAG_ATTRIBUTES, indent, true /*insert new line*/);
  }

  void encodeAccessRules(XMLWriter writer, boolean indent, boolean newLine) {

    writer.startTag(TAG_ACCESS_RULES, indent);
    AccessRule[] rules = getAccessRuleSet().getAccessRules();
    for (int i = 0, length = rules.length; i < length; i++) {
      encodeAccessRule(rules[i], writer, indent, newLine);
    }
    writer.endTag(TAG_ACCESS_RULES, indent, true /*insert new line*/);
  }

  private void encodeAccessRule(
      AccessRule accessRule, XMLWriter writer, boolean indent, boolean newLine) {

    HashMap parameters = new HashMap();
    parameters.put(TAG_PATTERN, new String(accessRule.pattern));

    switch (accessRule.getProblemId()) {
      case IProblem.ForbiddenReference:
        parameters.put(TAG_KIND, TAG_NON_ACCESSIBLE);
        break;
      case IProblem.DiscouragedReference:
        parameters.put(TAG_KIND, TAG_DISCOURAGED);
        break;
      default:
        parameters.put(TAG_KIND, TAG_ACCESSIBLE);
        break;
    }
    if (accessRule.ignoreIfBetter()) parameters.put(TAG_IGNORE_IF_BETTER, "true"); // $NON-NLS-1$

    writer.printTag(TAG_ACCESS_RULE, parameters, indent, newLine, true);
  }

  private void encodeUnknownChildren(
      XMLWriter writer, boolean indent, boolean newLine, ArrayList unknownChildren) {
    for (int i = 0, length = unknownChildren.size(); i < length; i++) {
      String child = (String) unknownChildren.get(i);
      writer.printString(child, indent, false /*don't insert new line*/);
    }
  }

  public static IClasspathEntry elementDecode(
      Element element, IJavaProject project, Map unknownElements) {

    IPath projectPath = project.getProject().getFullPath();
    NamedNodeMap attributes = element.getAttributes();
    NodeList children = element.getChildNodes();
    boolean[] foundChildren = new boolean[children.getLength()];
    String kindAttr = removeAttribute(TAG_KIND, attributes);
    String pathAttr = removeAttribute(TAG_PATH, attributes);

    // ensure path is absolute
    IPath path = new Path(pathAttr);
    int kind = kindFromString(kindAttr);
    if (kind != IClasspathEntry.CPE_VARIABLE
        && kind != IClasspathEntry.CPE_CONTAINER
        && !path.isAbsolute()) {
      if (!(path.segmentCount() > 0
          && path.segment(0).equals(org.eclipse.jdt.internal.core.ClasspathEntry.DOT_DOT))) {
        path = projectPath.append(path);
      }
    }
    // source attachment info (optional)
    IPath sourceAttachmentPath =
        element.hasAttribute(TAG_SOURCEPATH)
            ? new Path(removeAttribute(TAG_SOURCEPATH, attributes))
            : null;
    if (kind != IClasspathEntry.CPE_VARIABLE
        && sourceAttachmentPath != null
        && !sourceAttachmentPath.isAbsolute()) {
      sourceAttachmentPath = projectPath.append(sourceAttachmentPath);
    }
    IPath sourceAttachmentRootPath =
        element.hasAttribute(TAG_ROOTPATH)
            ? new Path(removeAttribute(TAG_ROOTPATH, attributes))
            : null;

    // exported flag (optional)
    boolean isExported = removeAttribute(TAG_EXPORTED, attributes).equals("true"); // $NON-NLS-1$

    // inclusion patterns (optional)
    IPath[] inclusionPatterns = decodePatterns(attributes, TAG_INCLUDING);
    if (inclusionPatterns == null) inclusionPatterns = INCLUDE_ALL;

    // exclusion patterns (optional)
    IPath[] exclusionPatterns = decodePatterns(attributes, TAG_EXCLUDING);
    if (exclusionPatterns == null) exclusionPatterns = EXCLUDE_NONE;

    // access rules (optional)
    NodeList attributeList = getChildAttributes(TAG_ACCESS_RULES, children, foundChildren);
    IAccessRule[] accessRules = decodeAccessRules(attributeList);

    // backward compatibility
    if (accessRules == null) {
      accessRules = getAccessRules(inclusionPatterns, exclusionPatterns);
    }

    // combine access rules (optional)
    boolean combineAccessRestrictions =
        !removeAttribute(TAG_COMBINE_ACCESS_RULES, attributes).equals("false"); // $NON-NLS-1$

    // extra attributes (optional)
    attributeList = getChildAttributes(TAG_ATTRIBUTES, children, foundChildren);
    IClasspathAttribute[] extraAttributes = decodeExtraAttributes(attributeList);

    // custom output location
    IPath outputLocation =
        element.hasAttribute(TAG_OUTPUT)
            ? projectPath.append(removeAttribute(TAG_OUTPUT, attributes))
            : null;

    String[] unknownAttributes = null;
    ArrayList unknownChildren = null;

    if (unknownElements != null) {
      // unknown attributes
      int unknownAttributeLength = attributes.getLength();
      if (unknownAttributeLength != 0) {
        unknownAttributes = new String[unknownAttributeLength * 2];
        for (int i = 0; i < unknownAttributeLength; i++) {
          Node attribute = attributes.item(i);
          unknownAttributes[i * 2] = attribute.getNodeName();
          unknownAttributes[i * 2 + 1] = attribute.getNodeValue();
        }
      }

      // unknown children
      for (int i = 0, length = foundChildren.length; i < length; i++) {
        if (!foundChildren[i]) {
          Node node = children.item(i);
          if (node.getNodeType() != Node.ELEMENT_NODE) continue;
          if (unknownChildren == null) unknownChildren = new ArrayList();
          StringBuffer buffer = new StringBuffer();
          decodeUnknownNode(node, buffer, project);
          unknownChildren.add(buffer.toString());
        }
      }
    }

    // recreate the CP entry
    IClasspathEntry entry = null;
    switch (kind) {
      case IClasspathEntry.CPE_PROJECT:
        entry =
            new org.eclipse.jdt.internal.core.ClasspathEntry(
                IPackageFragmentRoot.K_SOURCE,
                IClasspathEntry.CPE_PROJECT,
                path,
                org.eclipse.jdt.internal.core.ClasspathEntry.INCLUDE_ALL, // inclusion patterns
                org.eclipse.jdt.internal.core.ClasspathEntry.EXCLUDE_NONE, // exclusion patterns
                null, // source attachment
                null, // source attachment root
                null, // specific output folder
                isExported,
                accessRules,
                combineAccessRestrictions,
                extraAttributes);
        break;
      case IClasspathEntry.CPE_LIBRARY:
        entry =
            JavaCore.newLibraryEntry(
                path,
                sourceAttachmentPath,
                sourceAttachmentRootPath,
                accessRules,
                extraAttributes,
                isExported);
        break;
      case IClasspathEntry.CPE_SOURCE:
        // must be an entry in this project or specify another project
        String projSegment = path.segment(0);
        if (projSegment != null
            && path.toOSString()
                .startsWith(project.getProject().getFullPath().toOSString())) { // this project
          entry =
              JavaCore.newSourceEntry(
                  path, inclusionPatterns, exclusionPatterns, outputLocation, extraAttributes);
        } else {
          if (path.segmentCount() == 1
              || path.segment(0).equals(project.getProject().getFullPath().segment(0))) {
            // another project
            entry =
                JavaCore.newProjectEntry(
                    path, accessRules, combineAccessRestrictions, extraAttributes, isExported);
          } else {
            // an invalid source folder
            entry =
                JavaCore.newSourceEntry(
                    path, inclusionPatterns, exclusionPatterns, outputLocation, extraAttributes);
          }
        }
        break;
      case IClasspathEntry.CPE_VARIABLE:
        entry =
            JavaCore.newVariableEntry(
                path,
                sourceAttachmentPath,
                sourceAttachmentRootPath,
                accessRules,
                extraAttributes,
                isExported);
        break;
      case IClasspathEntry.CPE_CONTAINER:
        entry = JavaCore.newContainerEntry(path, accessRules, extraAttributes, isExported);
        break;
      case org.eclipse.jdt.internal.core.ClasspathEntry.K_OUTPUT:
        if (!path.isAbsolute()) return null;
        entry =
            new org.eclipse.jdt.internal.core.ClasspathEntry(
                org.eclipse.jdt.internal.core.ClasspathEntry.K_OUTPUT,
                IClasspathEntry.CPE_LIBRARY,
                path,
                INCLUDE_ALL,
                EXCLUDE_NONE,
                null, // source attachment
                null, // source attachment root
                null, // custom output location
                false,
                null, // no access rules
                false, // no accessible files to combine
                NO_EXTRA_ATTRIBUTES);
        break;
      default:
        throw new AssertionFailedException(Messages.bind(Messages.classpath_unknownKind, kindAttr));
    }

    if (unknownAttributes != null || unknownChildren != null) {
      UnknownXmlElements unknownXmlElements = new UnknownXmlElements();
      unknownXmlElements.attributes = unknownAttributes;
      unknownXmlElements.children = unknownChildren;
      unknownElements.put(path, unknownXmlElements);
    }

    return entry;
  }

  /** Encode some patterns into XML parameter tag */
  private static void encodePatterns(IPath[] patterns, String tag, Map parameters) {
    if (patterns != null && patterns.length > 0) {
      StringBuffer rule = new StringBuffer(10);
      for (int i = 0, max = patterns.length; i < max; i++) {
        if (i > 0) rule.append('|');
        rule.append(patterns[i]);
      }
      parameters.put(tag, String.valueOf(rule));
    }
  }

  private static boolean equalAttributes(
      IClasspathAttribute[] firstAttributes, IClasspathAttribute[] secondAttributes) {
    if (firstAttributes != secondAttributes) {
      if (firstAttributes == null) return false;
      int length = firstAttributes.length;
      if (secondAttributes == null || secondAttributes.length != length) return false;
      for (int i = 0; i < length; i++) {
        if (!firstAttributes[i].equals(secondAttributes[i])) return false;
      }
    }
    return true;
  }

  private static boolean equalPatterns(IPath[] firstPatterns, IPath[] secondPatterns) {
    if (firstPatterns != secondPatterns) {
      if (firstPatterns == null) return false;
      int length = firstPatterns.length;
      if (secondPatterns == null || secondPatterns.length != length) return false;
      for (int i = 0; i < length; i++) {
        // compare toStrings instead of IPaths
        // since IPath.equals is specified to ignore trailing separators
        if (!firstPatterns[i].toString().equals(secondPatterns[i].toString())) return false;
      }
    }
    return true;
  }

  /** Returns the kind of a <code>PackageFragmentRoot</code> from its <code>String</code> form. */
  static int kindFromString(String kindStr) {

    if (kindStr.equalsIgnoreCase("prj")) // $NON-NLS-1$
    return IClasspathEntry.CPE_PROJECT;
    if (kindStr.equalsIgnoreCase("var")) // $NON-NLS-1$
    return IClasspathEntry.CPE_VARIABLE;
    if (kindStr.equalsIgnoreCase("con")) // $NON-NLS-1$
    return IClasspathEntry.CPE_CONTAINER;
    if (kindStr.equalsIgnoreCase("src")) // $NON-NLS-1$
    return IClasspathEntry.CPE_SOURCE;
    if (kindStr.equalsIgnoreCase("lib")) // $NON-NLS-1$
    return IClasspathEntry.CPE_LIBRARY;
    if (kindStr.equalsIgnoreCase("output")) // $NON-NLS-1$
    return org.eclipse.jdt.internal.core.ClasspathEntry.K_OUTPUT;
    return -1;
  }

  /** Returns a <code>String</code> for the kind of a class path entry. */
  static String kindToString(int kind) {

    switch (kind) {
      case IClasspathEntry.CPE_PROJECT:
        return "src"; // backward compatibility //$NON-NLS-1$
      case IClasspathEntry.CPE_SOURCE:
        return "src"; // $NON-NLS-1$
      case IClasspathEntry.CPE_LIBRARY:
        return "lib"; // $NON-NLS-1$
      case IClasspathEntry.CPE_VARIABLE:
        return "var"; // $NON-NLS-1$
      case IClasspathEntry.CPE_CONTAINER:
        return "con"; // $NON-NLS-1$
      case org.eclipse.jdt.internal.core.ClasspathEntry.K_OUTPUT:
        return "output"; // $NON-NLS-1$
      default:
        return "unknown"; // $NON-NLS-1$
    }
  }

  /*
   * Backward compatibility: only accessible and non-accessible files are suported.
   */
  public static IAccessRule[] getAccessRules(IPath[] accessibleFiles, IPath[] nonAccessibleFiles) {
    int accessibleFilesLength = accessibleFiles == null ? 0 : accessibleFiles.length;
    int nonAccessibleFilesLength = nonAccessibleFiles == null ? 0 : nonAccessibleFiles.length;
    int length = accessibleFilesLength + nonAccessibleFilesLength;
    if (length == 0) return null;
    IAccessRule[] accessRules = new IAccessRule[length];
    for (int i = 0; i < accessibleFilesLength; i++) {
      accessRules[i] = JavaCore.newAccessRule(accessibleFiles[i], IAccessRule.K_ACCESSIBLE);
    }
    for (int i = 0; i < nonAccessibleFilesLength; i++) {
      accessRules[accessibleFilesLength + i] =
          JavaCore.newAccessRule(nonAccessibleFiles[i], IAccessRule.K_NON_ACCESSIBLE);
    }
    return accessRules;
  }

  private static IJavaModelStatus validateLibraryContents(
      IPath path, IJavaProject project, String entryPathMsg) {
    JavaModelManager manager = JavaModelManager.getJavaModelManager();
    try {
      manager.verifyArchiveContent(path);
    } catch (CoreException e) {
      if (e.getStatus().getMessage() == Messages.status_IOException) {
        return new JavaModelStatus(
            IJavaModelStatusConstants.INVALID_CLASSPATH,
            Messages.bind(
                Messages.classpath_archiveReadError,
                new String[] {entryPathMsg, project.getElementName()}));
      }
    }
    return JavaModelStatus.VERIFIED_OK;
  }

  /*
   * Resolves the ".." in the given path. Returns the given path if it contains no ".." segment.
   */
  public static IPath resolveDotDot(IPath reference, IPath path) {
    IPath newPath = null;
    IPath workspaceLocation = workspaceRoot.getLocation();
    if (reference == null || workspaceLocation.isPrefixOf(reference)) {
      for (int i = 0, length = path.segmentCount(); i < length; i++) {
        String segment = path.segment(i);
        if (DOT_DOT.equals(segment)) {
          if (newPath == null) {
            if (i == 0) {
              newPath = workspaceLocation;
            } else {
              newPath = path.removeFirstSegments(i);
            }
          } else {
            if (newPath.segmentCount() > 0) {
              newPath = newPath.removeLastSegments(1);
            } else {
              newPath = workspaceLocation;
            }
          }
        } else if (newPath != null) {
          if (newPath.equals(workspaceLocation)
              && workspaceRoot.getProject(segment).isAccessible()) {
            newPath = new Path(segment).makeAbsolute();
          } else {
            newPath = newPath.append(segment);
          }
        }
      }
    } else {
      for (int i = 0, length = path.segmentCount(); i < length; i++) {
        String segment = path.segment(i);
        if (DOT_DOT.equals(segment)) {
          if (newPath == null) {
            newPath = reference;
          }
          if (newPath.segmentCount() > 0) {
            newPath = newPath.removeLastSegments(1);
          }
        } else if (newPath != null) {
          newPath = newPath.append(segment);
        }
      }
    }
    if (newPath == null) return path;
    return newPath;
  }

  public boolean combineAccessRules() {
    return this.combineAccessRules;
  }

  /** Used to perform export/restriction propagation across referring projects/containers */
  public org.eclipse.jdt.internal.core.ClasspathEntry combineWith(
      org.eclipse.jdt.internal.core.ClasspathEntry referringEntry) {
    if (referringEntry == null) return this;
    if (referringEntry.isExported() || referringEntry.getAccessRuleSet() != null) {
      boolean combine =
          this.entryKind == IClasspathEntry.CPE_SOURCE || referringEntry.combineAccessRules();
      return new org.eclipse.jdt.internal.core.ClasspathEntry(
          getContentKind(),
          getEntryKind(),
          getPath(),
          this.inclusionPatterns,
          this.exclusionPatterns,
          getSourceAttachmentPath(),
          getSourceAttachmentRootPath(),
          getOutputLocation(),
          referringEntry.isExported()
              || this.isExported, // duplicate container entry for tagging it as exported
          combine(referringEntry.getAccessRules(), getAccessRules(), combine),
          this.combineAccessRules,
          this.extraAttributes);
    }
    // no need to clone
    return this;
  }

  private IAccessRule[] combine(
      IAccessRule[] referringRules, IAccessRule[] rules, boolean combine) {
    if (!combine) return rules;
    if (rules == null || rules.length == 0) return referringRules;

    // concat access rules
    int referringRulesLength = referringRules.length;
    int accessRulesLength = rules.length;
    int rulesLength = referringRulesLength + accessRulesLength;
    IAccessRule[] result = new IAccessRule[rulesLength];
    System.arraycopy(referringRules, 0, result, 0, referringRulesLength);
    System.arraycopy(rules, 0, result, referringRulesLength, accessRulesLength);

    return result;
  }

  /*
   * Returns a char based representation of the exclusions patterns full path.
   */
  public char[][] fullExclusionPatternChars() {

    if (this.fullExclusionPatternChars == UNINIT_PATTERNS) {
      int length = this.exclusionPatterns.length;
      this.fullExclusionPatternChars = new char[length][];
      IPath prefixPath = this.path.removeTrailingSeparator();
      for (int i = 0; i < length; i++) {
        this.fullExclusionPatternChars[i] =
            prefixPath.append(this.exclusionPatterns[i]).toString().toCharArray();
      }
    }
    return this.fullExclusionPatternChars;
  }

  /*
   * Returns a char based representation of the exclusions patterns full path.
   */
  public char[][] fullInclusionPatternChars() {

    if (this.fullInclusionPatternChars == UNINIT_PATTERNS) {
      int length = this.inclusionPatterns.length;
      this.fullInclusionPatternChars = new char[length][];
      IPath prefixPath = this.path.removeTrailingSeparator();
      for (int i = 0; i < length; i++) {
        this.fullInclusionPatternChars[i] =
            prefixPath.append(this.inclusionPatterns[i]).toString().toCharArray();
      }
    }
    return this.fullInclusionPatternChars;
  }

  /** Returns true if the given object is a classpath entry with equivalent attributes. */
  public boolean equals(Object object) {
    if (this == object) return true;
    if (object instanceof org.eclipse.jdt.internal.core.ClasspathEntry) {
      org.eclipse.jdt.internal.core.ClasspathEntry otherEntry =
          (org.eclipse.jdt.internal.core.ClasspathEntry) object;

      if (this.contentKind != otherEntry.getContentKind()) return false;

      if (this.entryKind != otherEntry.getEntryKind()) return false;

      if (this.isExported != otherEntry.isExported()) return false;

      if (!this.path.equals(otherEntry.getPath())) return false;

      IPath otherPath = otherEntry.getSourceAttachmentPath();
      if (this.sourceAttachmentPath == null) {
        if (otherPath != null) return false;
      } else {
        if (!this.sourceAttachmentPath.equals(otherPath)) return false;
      }

      otherPath = otherEntry.getSourceAttachmentRootPath();
      if (this.sourceAttachmentRootPath == null) {
        if (otherPath != null) return false;
      } else {
        if (!this.sourceAttachmentRootPath.equals(otherPath)) return false;
      }

      if (!equalPatterns(this.inclusionPatterns, otherEntry.getInclusionPatterns())) return false;
      if (!equalPatterns(this.exclusionPatterns, otherEntry.getExclusionPatterns())) return false;
      AccessRuleSet otherRuleSet = otherEntry.getAccessRuleSet();
      if (getAccessRuleSet() != null) {
        if (!getAccessRuleSet().equals(otherRuleSet)) return false;
      } else if (otherRuleSet != null) return false;
      if (this.combineAccessRules != otherEntry.combineAccessRules()) return false;
      otherPath = otherEntry.getOutputLocation();
      if (this.specificOutputLocation == null) {
        if (otherPath != null) return false;
      } else {
        if (!this.specificOutputLocation.equals(otherPath)) return false;
      }
      if (!equalAttributes(this.extraAttributes, otherEntry.getExtraAttributes())) return false;
      return true;
    } else {
      return false;
    }
  }

  /** @see org.eclipse.jdt.core.IClasspathEntry#getAccessRules() */
  public IAccessRule[] getAccessRules() {
    if (this.accessRuleSet == null) return NO_ACCESS_RULES;
    AccessRule[] rules = this.accessRuleSet.getAccessRules();
    int length = rules.length;
    if (length == 0) return NO_ACCESS_RULES;
    IAccessRule[] result = new IAccessRule[length];
    System.arraycopy(rules, 0, result, 0, length);
    return result;
  }

  public AccessRuleSet getAccessRuleSet() {
    return this.accessRuleSet;
  }

  /** @see org.eclipse.jdt.core.IClasspathEntry */
  public int getContentKind() {
    return this.contentKind;
  }

  /** @see org.eclipse.jdt.core.IClasspathEntry */
  public int getEntryKind() {
    return this.entryKind;
  }

  /** @see org.eclipse.jdt.core.IClasspathEntry#getExclusionPatterns() */
  public IPath[] getExclusionPatterns() {
    return this.exclusionPatterns;
  }

  public IClasspathAttribute[] getExtraAttributes() {
    return this.extraAttributes;
  }

  /** @see org.eclipse.jdt.core.IClasspathEntry#getExclusionPatterns() */
  public IPath[] getInclusionPatterns() {
    return this.inclusionPatterns;
  }

  /** @see org.eclipse.jdt.core.IClasspathEntry#getOutputLocation() */
  public IPath getOutputLocation() {
    return this.specificOutputLocation;
  }

  /** @see org.eclipse.jdt.core.IClasspathEntry */
  public IPath getPath() {
    return this.path;
  }

  /** @see org.eclipse.jdt.core.IClasspathEntry */
  public IPath getSourceAttachmentPath() {
    return this.sourceAttachmentPath;
  }

  /** @see org.eclipse.jdt.core.IClasspathEntry */
  public IPath getSourceAttachmentRootPath() {
    return this.sourceAttachmentRootPath;
  }

  public IClasspathEntry getReferencingEntry() {
    return this.referencingEntry;
  }

  /** Returns the hash code for this classpath entry */
  public int hashCode() {
    return this.path.hashCode();
  }

  /** @see org.eclipse.jdt.core.IClasspathEntry#isExported() */
  public boolean isExported() {
    return this.isExported;
  }

  public boolean isOptional() {
    for (int i = 0, length = this.extraAttributes.length; i < length; i++) {
      IClasspathAttribute attribute = this.extraAttributes[i];
      if (IClasspathAttribute.OPTIONAL.equals(attribute.getName())
          && "true".equals(attribute.getValue())) // $NON-NLS-1$
      return true;
    }
    return false;
  }

  public String getSourceAttachmentEncoding() {
    for (int i = 0, length = this.extraAttributes.length; i < length; i++) {
      IClasspathAttribute attribute = this.extraAttributes[i];
      if (IClasspathAttribute.SOURCE_ATTACHMENT_ENCODING.equals(attribute.getName()))
        return attribute.getValue();
    }
    return null;
  }

  /** Returns a printable representation of this classpath entry. */
  public String toString() {
    StringBuffer buffer = new StringBuffer();
    //		Object target = JavaModel.getTarget(getPath(), true);
    //		if (target instanceof File)
    buffer.append(getPath().toOSString());
    //		else
    //			buffer.append(String.valueOf(getPath()));
    buffer.append('[');
    switch (getEntryKind()) {
      case IClasspathEntry.CPE_LIBRARY:
        buffer.append("CPE_LIBRARY"); // $NON-NLS-1$
        break;
      case IClasspathEntry.CPE_PROJECT:
        buffer.append("CPE_PROJECT"); // $NON-NLS-1$
        break;
      case IClasspathEntry.CPE_SOURCE:
        buffer.append("CPE_SOURCE"); // $NON-NLS-1$
        break;
      case IClasspathEntry.CPE_VARIABLE:
        buffer.append("CPE_VARIABLE"); // $NON-NLS-1$
        break;
      case IClasspathEntry.CPE_CONTAINER:
        buffer.append("CPE_CONTAINER"); // $NON-NLS-1$
        break;
    }
    buffer.append("]["); // $NON-NLS-1$
    switch (getContentKind()) {
      case IPackageFragmentRoot.K_BINARY:
        buffer.append("K_BINARY"); // $NON-NLS-1$
        break;
      case IPackageFragmentRoot.K_SOURCE:
        buffer.append("K_SOURCE"); // $NON-NLS-1$
        break;
      case org.eclipse.jdt.internal.core.ClasspathEntry.K_OUTPUT:
        buffer.append("K_OUTPUT"); // $NON-NLS-1$
        break;
    }
    buffer.append(']');
    if (getSourceAttachmentPath() != null) {
      buffer.append("[sourcePath:"); // $NON-NLS-1$
      buffer.append(getSourceAttachmentPath());
      buffer.append(']');
    }
    if (getSourceAttachmentRootPath() != null) {
      buffer.append("[rootPath:"); // $NON-NLS-1$
      buffer.append(getSourceAttachmentRootPath());
      buffer.append(']');
    }
    buffer.append("[isExported:"); // $NON-NLS-1$
    buffer.append(this.isExported);
    buffer.append(']');
    IPath[] patterns = this.inclusionPatterns;
    int length;
    if ((length = patterns == null ? 0 : patterns.length) > 0) {
      buffer.append("[including:"); // $NON-NLS-1$
      for (int i = 0; i < length; i++) {
        buffer.append(patterns[i]);
        if (i != length - 1) {
          buffer.append('|');
        }
      }
      buffer.append(']');
    }
    patterns = this.exclusionPatterns;
    if ((length = patterns == null ? 0 : patterns.length) > 0) {
      buffer.append("[excluding:"); // $NON-NLS-1$
      for (int i = 0; i < length; i++) {
        buffer.append(patterns[i]);
        if (i != length - 1) {
          buffer.append('|');
        }
      }
      buffer.append(']');
    }
    if (this.accessRuleSet != null) {
      buffer.append('[');
      buffer.append(this.accessRuleSet.toString(false /*on one line*/));
      buffer.append(']');
    }
    if (this.entryKind == IClasspathEntry.CPE_PROJECT) {
      buffer.append("[combine access rules:"); // $NON-NLS-1$
      buffer.append(this.combineAccessRules);
      buffer.append(']');
    }
    if (getOutputLocation() != null) {
      buffer.append("[output:"); // $NON-NLS-1$
      buffer.append(getOutputLocation());
      buffer.append(']');
    }
    if ((length = this.extraAttributes == null ? 0 : this.extraAttributes.length) > 0) {
      buffer.append("[attributes:"); // $NON-NLS-1$
      for (int i = 0; i < length; i++) {
        buffer.append(this.extraAttributes[i]);
        if (i != length - 1) {
          buffer.append(',');
        }
      }
      buffer.append(']');
    }
    return buffer.toString();
  }

  /*
   * Read the Class-Path clause of the manifest of the jar pointed by this entry, and return
   * the corresponding library entries.
   */
  public org.eclipse.jdt.internal.core.ClasspathEntry[] resolvedChainedLibraries() {
    IPath[] paths = resolvedChainedLibraries(getPath());
    int length = paths.length;
    if (length == 0) return NO_ENTRIES;
    org.eclipse.jdt.internal.core.ClasspathEntry[] result =
        new org.eclipse.jdt.internal.core.ClasspathEntry[length];
    for (int i = 0; i < length; i++) {
      // Chained(referenced) libraries can have their own attachment path. Hence, set them to null
      result[i] =
          new org.eclipse.jdt.internal.core.ClasspathEntry(
              getContentKind(),
              getEntryKind(),
              paths[i],
              this.inclusionPatterns,
              this.exclusionPatterns,
              null,
              null,
              getOutputLocation(),
              this,
              this.isExported,
              getAccessRules(),
              this.combineAccessRules,
              NO_EXTRA_ATTRIBUTES);
    }
    return result;
  }

  //	public ClasspathEntry resolvedDotDot(IPath reference) {
  //		IPath resolvedPath = resolveDotDot(reference, this.path);
  //		if (resolvedPath == this.path)
  //			return this;
  //		return new ClasspathEntry(
  //							getContentKind(),
  //							getEntryKind(),
  //							resolvedPath,
  //							this.inclusionPatterns,
  //							this.exclusionPatterns,
  //							getSourceAttachmentPath(),
  //							getSourceAttachmentRootPath(),
  //							getOutputLocation(),
  //							this.getReferencingEntry(),
  //							this.isExported,
  //							getAccessRules(),
  //							this.combineAccessRules,
  //							this.extraAttributes);
  //	}

  /**
   * Answers an ID which is used to distinguish entries during package fragment root computations
   */
  public String rootID() {

    if (this.rootID == null) {
      switch (this.entryKind) {
        case IClasspathEntry.CPE_LIBRARY:
          this.rootID = "[LIB]" + this.path; // $NON-NLS-1$
          break;
        case IClasspathEntry.CPE_PROJECT:
          this.rootID = "[PRJ]" + this.path; // $NON-NLS-1$
          break;
        case IClasspathEntry.CPE_SOURCE:
          this.rootID = "[SRC]" + this.path; // $NON-NLS-1$
          break;
        case IClasspathEntry.CPE_VARIABLE:
          this.rootID = "[VAR]" + this.path; // $NON-NLS-1$
          break;
        case IClasspathEntry.CPE_CONTAINER:
          this.rootID = "[CON]" + this.path; // $NON-NLS-1$
          break;
        default:
          this.rootID = ""; // $NON-NLS-1$
          break;
      }
    }
    return this.rootID;
  }

  /**
   * @see org.eclipse.jdt.core.IClasspathEntry
   * @deprecated
   */
  public IClasspathEntry getResolvedEntry() {

    return JavaCore.getResolvedClasspathEntry(this);
  }

  public org.eclipse.jdt.internal.core.ClasspathEntry resolvedDotDot(IPath reference) {
    IPath resolvedPath = resolveDotDot(reference, this.path);
    if (resolvedPath == this.path) return this;
    return new org.eclipse.jdt.internal.core.ClasspathEntry(
        getContentKind(),
        getEntryKind(),
        resolvedPath,
        this.inclusionPatterns,
        this.exclusionPatterns,
        getSourceAttachmentPath(),
        getSourceAttachmentRootPath(),
        getOutputLocation(),
        this.getReferencingEntry(),
        this.isExported,
        getAccessRules(),
        this.combineAccessRules,
        this.extraAttributes);
  }

  /**
   * This function computes the URL of the index location for this classpath entry. It returns null
   * if the URL is invalid.
   */
  public URL getLibraryIndexLocation() {
    switch (getEntryKind()) {
      case IClasspathEntry.CPE_LIBRARY:
      case IClasspathEntry.CPE_VARIABLE:
        break;
      default:
        return null;
    }
    if (this.extraAttributes == null) return null;
    for (int i = 0; i < this.extraAttributes.length; i++) {
      IClasspathAttribute attrib = this.extraAttributes[i];
      if (IClasspathAttribute.INDEX_LOCATION_ATTRIBUTE_NAME.equals(attrib.getName())) {
        String value = attrib.getValue();
        try {
          return new URL(value);
        } catch (MalformedURLException e) {
          return null;
        }
      }
    }
    return null;
  }

  public boolean ignoreOptionalProblems() {
    if (this.entryKind == IClasspathEntry.CPE_SOURCE) {
      for (int i = 0; i < this.extraAttributes.length; i++) {
        IClasspathAttribute attrib = this.extraAttributes[i];
        if (IClasspathAttribute.IGNORE_OPTIONAL_PROBLEMS.equals(attrib.getName())) {
          return "true".equals(attrib.getValue()); // $NON-NLS-1$
        }
      }
    }
    return false;
  }

  public static class AssertionFailedException extends RuntimeException {

    private static final long serialVersionUID = -171699380721189572L;

    public AssertionFailedException(String message) {
      super(message);
    }
  }

  /**
   * Validate a given classpath and output location for a project, using the following rules:
   *
   * <ul>
   *   <li>Classpath entries cannot collide with each other; that is, all entry paths must be
   *       unique.
   *   <li>The project output location path cannot be null, must be absolute and located inside the
   *       project.
   *   <li>Specific output locations (specified on source entries) can be null, if not they must be
   *       located inside the project,
   *   <li>A project entry cannot refer to itself directly (that is, a project cannot prerequisite
   *       itself).
   *   <li>Classpath entries or output locations cannot coincidate or be nested in each other,
   *       except for the following scenario listed below:
   *       <ul>
   *         <li>A source folder can coincidate with its own output location, in which case this
   *             output can then contain library archives. However, a specific output location
   *             cannot coincidate with any library or a distinct source folder than the one
   *             referring to it.
   *         <li>A source/library folder can be nested in any source folder as long as the nested
   *             folder is excluded from the enclosing one.
   *         <li>An output location can be nested in a source folder, if the source folder
   *             coincidates with the project itself, or if the output location is excluded from the
   *             source folder.
   *       </ul>
   * </ul>
   *
   * Note that the classpath entries are not validated automatically. Only bound variables or
   * containers are considered in the checking process (this allows to perform a consistency check
   * on a classpath which has references to yet non existing projects, folders, ...).
   *
   * <p>This validation is intended to anticipate classpath issues prior to assigning it to a
   * project. In particular, it will automatically be performed during the classpath setting
   * operation (if validation fails, the classpath setting will not complete).
   *
   * <p>
   *
   * @param javaProject the given java project
   * @param rawClasspath a given classpath
   * @param projectOutputLocation a given output location
   * @return a status object with code <code>IStatus.OK</code> if the given classpath and output
   *     location are compatible, otherwise a status object indicating what is wrong with the
   *     classpath or output location
   */
  public static IJavaModelStatus validateClasspath(
      IJavaProject javaProject, IClasspathEntry[] rawClasspath, IPath projectOutputLocation) {

    //        IProject project = javaProject.getProject();
    //        IPath projectPath= project.getFullPath();
    //        String projectName = javaProject.getElementName();
    //
    //		/* validate output location */
    //        if (projectOutputLocation == null) {
    //            return new JavaModelStatus(IJavaModelStatusConstants.NULL_PATH);
    //        }
    //        if (projectOutputLocation.isAbsolute()) {
    //            if (!projectPath.isPrefixOf(projectOutputLocation)) {
    //                return new JavaModelStatus(IJavaModelStatusConstants.PATH_OUTSIDE_PROJECT,
    // javaProject, projectOutputLocation.toString());
    //            }
    //        } else {
    //            return new JavaModelStatus(IJavaModelStatusConstants.RELATIVE_PATH,
    // projectOutputLocation);
    //        }
    //
    //        boolean hasSource = false;
    //        boolean hasLibFolder = false;
    //
    //
    //        // tolerate null path, it will be reset to default
    //        if (rawClasspath == null)
    //            return JavaModelStatus.VERIFIED_OK;
    //
    //        // check duplicate entries on raw classpath only (see
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=175226 )
    //        int rawLength = rawClasspath.length;
    //        HashSet pathes = new HashSet(rawLength);
    //        for (int i = 0 ; i < rawLength; i++) {
    //            IPath entryPath = rawClasspath[i].getPath();
    //            if (!pathes.add(entryPath)){
    //                String entryPathMsg = projectName.equals(entryPath.segment(0)) ?
    // entryPath.removeFirstSegments(1).toString() : entryPath.makeRelative().toString();
    //                return new JavaModelStatus(IJavaModelStatusConstants.NAME_COLLISION,
    // Messages.bind(Messages.classpath_duplicateEntryPath,
    //
    //     new String[]{entryPathMsg, projectName}));
    //            }
    //        }
    //
    //        // retrieve resolved classpath
    //        IClasspathEntry[] classpath;
    //        try {
    //            // don't resolve chained libraries: see
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=259685
    //            classpath = ((JavaProject)javaProject).resolveClasspath(rawClasspath, false/*don't
    // use previous session*/, false/*don't resolve chained libraries*/).resolvedClasspath;
    //        } catch(JavaModelException e){
    //            return e.getJavaModelStatus();
    //        }
    //        int length = classpath.length;
    //
    //        int outputCount = 1;
    //        IPath[] outputLocations	= new IPath[length+1];
    //        boolean[] allowNestingInOutputLocations = new boolean[length+1];
    //        outputLocations[0] = projectOutputLocation;
    //
    //        // retrieve and check output locations
    //        IPath potentialNestedOutput = null; // for error reporting purpose
    //        int sourceEntryCount = 0;
    //        boolean disableExclusionPatterns =
    // JavaCore.DISABLED.equals(javaProject.getOption(JavaCore.CORE_ENABLE_CLASSPATH_EXCLUSION_PATTERNS, true));
    //        boolean disableCustomOutputLocations =
    // JavaCore.DISABLED.equals(javaProject.getOption(JavaCore.CORE_ENABLE_CLASSPATH_MULTIPLE_OUTPUT_LOCATIONS, true));
    //
    //        for (int i = 0 ; i < length; i++) {
    //            IClasspathEntry resolvedEntry = classpath[i];
    //            if (disableExclusionPatterns &&
    //                ((resolvedEntry.getInclusionPatterns() != null &&
    // resolvedEntry.getInclusionPatterns().length > 0)
    //                 || (resolvedEntry.getExclusionPatterns() != null &&
    // resolvedEntry.getExclusionPatterns().length > 0))) {
    //                return new
    // JavaModelStatus(IJavaModelStatusConstants.DISABLED_CP_EXCLUSION_PATTERNS, javaProject,
    // resolvedEntry.getPath());
    //            }
    //            switch(resolvedEntry.getEntryKind()){
    //                case IClasspathEntry.CPE_SOURCE :
    //                    sourceEntryCount++;
    //
    //                    IPath customOutput;
    //                    if ((customOutput = resolvedEntry.getOutputLocation()) != null) {
    //
    //                        if (disableCustomOutputLocations) {
    //                            return new
    // JavaModelStatus(IJavaModelStatusConstants.DISABLED_CP_MULTIPLE_OUTPUT_LOCATIONS, javaProject,
    // resolvedEntry.getPath());
    //                        }
    //                        // ensure custom output is in project
    //                        if (customOutput.isAbsolute()) {
    //                            if (!javaProject.getPath().isPrefixOf(customOutput)) {
    //                                return new
    // JavaModelStatus(IJavaModelStatusConstants.PATH_OUTSIDE_PROJECT, javaProject,
    // customOutput.toString());
    //                            }
    //                        } else {
    //                            return new
    // JavaModelStatus(IJavaModelStatusConstants.RELATIVE_PATH, customOutput);
    //                        }
    //
    //                        // ensure custom output doesn't conflict with other outputs
    //                        // check exact match
    //                        if (Util.indexOfMatchingPath(customOutput, outputLocations,
    // outputCount) != -1) {
    //                            continue; // already found
    //                        }
    //                        // accumulate all outputs, will check nesting once all available (to
    // handle ordering issues)
    //                        outputLocations[outputCount++] = customOutput;
    //                    }
    //            }
    //        }
    //        // check nesting across output locations
    //        for (int i = 1 /*no check for default output*/ ; i < outputCount; i++) {
    //            IPath customOutput = outputLocations[i];
    //            int index;
    //            // check nesting
    //            if ((index = Util.indexOfEnclosingPath(customOutput, outputLocations,
    // outputCount)) != -1 && index != i) {
    //                if (index == 0) {
    //                    // custom output is nested in project's output: need to check if all
    // source entries have a custom
    //                    // output before complaining
    //                    if (potentialNestedOutput == null) potentialNestedOutput = customOutput;
    //                } else {
    //                    return new JavaModelStatus(IJavaModelStatusConstants.INVALID_CLASSPATH,
    // Messages.bind(
    //                            Messages.classpath_cannotNestOutputInOutput,
    //                            new String[]{customOutput.makeRelative().toString(),
    // outputLocations[index].makeRelative().toString()}));
    //                }
    //            }
    //        }
    //        // allow custom output nesting in project's output if all source entries have a custom
    // output
    //        if (sourceEntryCount <= outputCount-1) {
    //            allowNestingInOutputLocations[0] = true;
    //        } else if (potentialNestedOutput != null) {
    //            return new JavaModelStatus(IJavaModelStatusConstants.INVALID_CLASSPATH,
    // Messages.bind(
    //                    Messages.classpath_cannotNestOutputInOutput,
    //                    new String[]{potentialNestedOutput.makeRelative().toString(),
    // outputLocations[0].makeRelative().toString()}));
    //        }
    //
    //        for (int i = 0 ; i < length; i++) {
    //            IClasspathEntry resolvedEntry = classpath[i];
    //            IPath path = resolvedEntry.getPath();
    //            int index;
    //            switch(resolvedEntry.getEntryKind()){
    //
    //                case IClasspathEntry.CPE_SOURCE :
    //                    hasSource = true;
    //                    if ((index = Util.indexOfMatchingPath(path, outputLocations, outputCount))
    // != -1){
    //                        allowNestingInOutputLocations[index] = true;
    //                    }
    //                    break;
    //
    //                case IClasspathEntry.CPE_LIBRARY:
    //                    Object target = JavaModel.getTarget(path, false/*don't check resource
    // existence*/);
    //                    hasLibFolder |= target instanceof IContainer;
    //                    if ((index = Util.indexOfMatchingPath(path, outputLocations, outputCount))
    // != -1){
    //                        allowNestingInOutputLocations[index] = true;
    //                    }
    //                    break;
    //            }
    //        }
    //        if (!hasSource && !hasLibFolder) { // if no source and no lib folder, then allowed
    //            for (int i = 0; i < outputCount; i++) allowNestingInOutputLocations[i] = true;
    //        }
    //
    //        // check all entries
    //        for (int i = 0 ; i < length; i++) {
    //            IClasspathEntry entry = classpath[i];
    //            if (entry == null) continue;
    //            IPath entryPath = entry.getPath();
    //            int kind = entry.getEntryKind();
    //
    //            // no further check if entry coincidates with project or output location
    //            if (entryPath.equals(projectPath)){
    //                // complain if self-referring project entry
    //                if (kind == IClasspathEntry.CPE_PROJECT){
    //                    return new JavaModelStatus(IJavaModelStatusConstants.INVALID_PATH,
    // Messages.bind(Messages.classpath_cannotReferToItself,
    //
    //       entryPath.makeRelative().toString()));
    //                }
    //                // tolerate nesting output in src if src==prj
    //                continue;
    //            }
    //
    //            // allow nesting source entries in each other as long as the outer entry excludes
    // the inner one
    //            if (kind == IClasspathEntry.CPE_SOURCE
    //                || (kind == IClasspathEntry.CPE_LIBRARY && (JavaModel.getTarget(entryPath,
    // false/*don't check existence*/) instanceof IContainer))) {
    //                for (int j = 0; j < classpath.length; j++){
    //                    IClasspathEntry otherEntry = classpath[j];
    //                    if (otherEntry == null) continue;
    //                    int otherKind = otherEntry.getEntryKind();
    //                    IPath otherPath = otherEntry.getPath();
    //                    if (entry != otherEntry
    //                        && (otherKind == IClasspathEntry.CPE_SOURCE
    //                            || (otherKind == IClasspathEntry.CPE_LIBRARY
    //                                && (JavaModel.getTarget(otherPath, false/*don't check
    // existence*/) instanceof IContainer)))) {
    //                        char[][] inclusionPatterns, exclusionPatterns;
    //                        if (otherPath.isPrefixOf(entryPath)
    //                            && !otherPath.equals(entryPath)
    //                            && !Util.isExcluded(entryPath.append("*"), inclusionPatterns =
    // ((ClasspathEntry)otherEntry).fullInclusionPatternChars(), exclusionPatterns =
    // ((ClasspathEntry)otherEntry).fullExclusionPatternChars(), false)) { //$NON-NLS-1$
    //                            String exclusionPattern =
    // entryPath.removeFirstSegments(otherPath.segmentCount()).segment(0);
    //                            if (Util.isExcluded(entryPath, inclusionPatterns,
    // exclusionPatterns, false)) {
    //                                return new
    // JavaModelStatus(IJavaModelStatusConstants.INVALID_CLASSPATH, Messages.bind(
    //                                        Messages.classpath_mustEndWithSlash,
    //                                        new String[]{exclusionPattern,
    // entryPath.makeRelative().toString()}));
    //                            } else {
    //                                if (otherKind == IClasspathEntry.CPE_SOURCE) {
    //                                    exclusionPattern += '/';
    //                                    if (!disableExclusionPatterns) {
    //                                        return new
    // JavaModelStatus(IJavaModelStatusConstants.INVALID_CLASSPATH, Messages.bind(
    //                                                Messages.classpath_cannotNestEntryInEntry, new
    // String[]{entryPath.makeRelative().toString(),
    //
    //          otherEntry.getPath().makeRelative()
    //
    //                    .toString(),
    //
    //          exclusionPattern}));
    //                                    } else {
    //                                        return new
    // JavaModelStatus(IJavaModelStatusConstants.INVALID_CLASSPATH, Messages.bind(
    //
    // Messages.classpath_cannotNestEntryInEntryNoExclusion,
    //                                                new
    // String[]{entryPath.makeRelative().toString(),
    //
    // otherEntry.getPath().makeRelative().toString(), exclusionPattern}));
    //                                    }
    //                                } else {
    //                                    return new
    // JavaModelStatus(IJavaModelStatusConstants.INVALID_CLASSPATH, Messages.bind(
    //                                            Messages.classpath_cannotNestEntryInLibrary, new
    // String[]{entryPath.makeRelative().toString(),
    //
    //        otherEntry.getPath().makeRelative()
    //
    //                  .toString()}));
    //                                }
    //                            }
    //                        }
    //                    }
    //                }
    //            }
    //
    //            // prevent nesting output location inside entry unless enclosing is a source entry
    // which explicitly exclude the output location
    //            char[][] inclusionPatterns = ((ClasspathEntry)entry).fullInclusionPatternChars();
    //            char[][] exclusionPatterns = ((ClasspathEntry)entry).fullExclusionPatternChars();
    //            for (int j = 0; j < outputCount; j++){
    //                IPath currentOutput = outputLocations[j];
    //                if (entryPath.equals(currentOutput)) continue;
    //                if (entryPath.isPrefixOf(currentOutput)) {
    //                    if (kind != IClasspathEntry.CPE_SOURCE || !Util.isExcluded(currentOutput,
    // inclusionPatterns, exclusionPatterns, true)) {
    //                        return new
    // JavaModelStatus(IJavaModelStatusConstants.INVALID_CLASSPATH, Messages.bind(
    //                                Messages.classpath_cannotNestOutputInEntry,
    //                                new String[]{currentOutput.makeRelative().toString(),
    // entryPath.makeRelative().toString()}));
    //                    }
    //                }
    //            }
    //
    //            // prevent nesting entry inside output location - when distinct from project or a
    // source folder
    //            for (int j = 0; j < outputCount; j++){
    //                if (allowNestingInOutputLocations[j]) continue;
    //                IPath currentOutput = outputLocations[j];
    //                if (currentOutput.isPrefixOf(entryPath)) {
    //                    return new JavaModelStatus(IJavaModelStatusConstants.INVALID_CLASSPATH,
    // Messages.bind(
    //                            Messages.classpath_cannotNestEntryInOutput,
    //                            new String[]{entryPath.makeRelative().toString(),
    // currentOutput.makeRelative().toString()}));
    //                }
    //            }
    //        }
    //        // ensure that no specific output is coincidating with another source folder (only
    // allowed if matching current source folder)
    //        // 36465 - for 2.0 backward compatibility, only check specific output locations (the
    // default can still coincidate)
    //        // perform one separate iteration so as to not take precedence over previously checked
    // scenarii (in particular should
    //        // diagnose nesting source folder issue before this one, for example, [src]"Project/",
    // [src]"Project/source/" and output="Project/" should
    //        // first complain about missing exclusion pattern
    //        IJavaModelStatus cachedStatus = null;
    //        for (int i = 0 ; i < length; i++) {
    //            IClasspathEntry entry = classpath[i];
    //            if (entry == null) continue;
    //            IPath entryPath = entry.getPath();
    //            int kind = entry.getEntryKind();
    //
    //            // Build some common strings for status message
    //            boolean isProjectRelative = projectName.equals(entryPath.segment(0));
    //            String entryPathMsg = isProjectRelative ?
    // entryPath.removeFirstSegments(1).toString() : entryPath.makeRelative().toString();
    //
    //            if (kind == IClasspathEntry.CPE_SOURCE) {
    //                IPath output = entry.getOutputLocation();
    //                if (output == null) output = projectOutputLocation; // if no specific output,
    // still need to check using default output (this line would check default output)
    //                for (int j = 0; j < length; j++) {
    //                    IClasspathEntry otherEntry = classpath[j];
    //                    if (otherEntry == entry) continue;
    //
    //                    switch (otherEntry.getEntryKind()) {
    //                        case IClasspathEntry.CPE_SOURCE :
    //                            // Bug 287164 : Report errors of overlapping output locations only
    // if the user sets the corresponding preference.
    //                            // The check is required for backward compatibility with bug-fix
    // 36465.
    //                            String option =
    // javaProject.getOption(JavaCore.CORE_OUTPUT_LOCATION_OVERLAPPING_ANOTHER_SOURCE, true);
    //                            if (otherEntry.getPath().equals(output)
    //                                && !JavaCore.IGNORE.equals(option)) {
    //                                boolean opStartsWithProject =
    // projectName.equals(otherEntry.getPath().segment(0));
    //                                String otherPathMsg = opStartsWithProject ?
    // otherEntry.getPath().removeFirstSegments(1).toString() :
    // otherEntry.getPath().makeRelative().toString();
    //                                if (JavaCore.ERROR.equals(option)) {
    //                                    return new JavaModelStatus(IStatus.ERROR,
    // IJavaModelStatusConstants.OUTPUT_LOCATION_OVERLAPPING_ANOTHER_SOURCE,
    //
    // Messages.bind(Messages.classpath_cannotUseDistinctSourceFolderAsOutput, new String[] {
    //                                                                       entryPathMsg,
    // otherPathMsg, projectName }));
    //                                }
    //                                if (cachedStatus == null) {
    //                                    // Note that the isOK() is being overridden to return
    // true. This is an exceptional scenario
    //                                    cachedStatus = new JavaModelStatus(IStatus.OK,
    // IJavaModelStatusConstants.OUTPUT_LOCATION_OVERLAPPING_ANOTHER_SOURCE,
    //
    // Messages.bind(Messages.classpath_cannotUseDistinctSourceFolderAsOutput, new String[] {
    //                                                                               entryPathMsg,
    // otherPathMsg, projectName })){
    //                                        public boolean isOK() {
    //                                            return true;
    //                                        }
    //                                    };
    //                                }
    //                            }
    //                            break;
    //                        case IClasspathEntry.CPE_LIBRARY :
    //                            if (output != projectOutputLocation &&
    // otherEntry.getPath().equals(output)) {
    //                                boolean opStartsWithProject =
    // projectName.equals(otherEntry.getPath().segment(0));
    //                                String otherPathMsg = opStartsWithProject ?
    // otherEntry.getPath().removeFirstSegments(1).toString() :
    // otherEntry.getPath().makeRelative().toString();
    //                                return new
    // JavaModelStatus(IJavaModelStatusConstants.INVALID_CLASSPATH,
    // Messages.bind(Messages.classpath_cannotUseLibraryAsOutput, new String[] {entryPathMsg,
    // otherPathMsg, projectName}));
    //                            }
    //                    }
    //                }
    //            }
    //        }
    //
    //        // NOTE: The above code that checks for
    // IJavaModelStatusConstants.OUTPUT_LOCATION_OVERLAPPING_ANOTHER_SOURCE, can be configured to
    // return
    //        // a WARNING status and hence should be at the end of this validation method. Any
    // other code that might return a more severe ERROR should be
    //        // inserted before the mentioned code.
    //        if (cachedStatus != null) return cachedStatus;

    return JavaModelStatus.VERIFIED_OK;
  }

  //	/**
  //	 * Returns a Java model status describing the problem related to this classpath entry if any,
  //	 * a status object with code <code>IStatus.OK</code> if the entry is fine (that is, if the
  //	 * given classpath entry denotes a valid element to be referenced onto a classpath).
  //	 *
  //	 * @param project the given java project
  //	 * @param entry the given classpath entry
  //	 * @param checkSourceAttachment a flag to determine if source attachment should be checked
  //	 * @param referredByContainer flag indicating whether the given entry is referred by a
  // classpath container
  //	 * @return a java model status describing the problem related to this classpath entry if any, a
  // status object with code <code>IStatus
  // .OK</code> if the entry is fine
  //	 */
  //	public static IJavaModelStatus validateClasspathEntry(IJavaProject project, IClasspathEntry
  // entry, boolean checkSourceAttachment,
  // boolean referredByContainer){
  //		if (entry.getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
  //			JavaModelManager.getJavaModelManager().removeFromInvalidArchiveCache(entry.getPath());
  //		}
  //		IJavaModelStatus status = validateClasspathEntry(project, entry, null, checkSourceAttachment,
  // referredByContainer);
  //		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=171136 and
  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=300136
  //		// Ignore class path errors from optional entries.
  //		int statusCode = status.getCode();
  //		if ( (statusCode == IJavaModelStatusConstants.INVALID_CLASSPATH ||
  //				statusCode == IJavaModelStatusConstants.CP_CONTAINER_PATH_UNBOUND ||
  //				statusCode == IJavaModelStatusConstants.CP_VARIABLE_PATH_UNBOUND ||
  //				statusCode == IJavaModelStatusConstants.INVALID_PATH) &&
  //				((ClasspathEntry) entry).isOptional())
  //			return JavaModelStatus.VERIFIED_OK;
  //		return status;
  //	}

  private static IJavaModelStatus validateClasspathEntry(
      IJavaProject project,
      IClasspathEntry entry,
      IClasspathContainer entryContainer,
      boolean checkSourceAttachment,
      boolean referredByContainer) {

    IPath path = entry.getPath();

    // Build some common strings for status message
    String projectName = project.getElementName();
    String entryPathMsg =
        projectName.equals(path.segment(0))
            ? path.removeFirstSegments(1).makeRelative().toString()
            : path.toString();

    switch (entry.getEntryKind()) {

        // container entry check
      case IClasspathEntry.CPE_CONTAINER:
        if (path.segmentCount() >= 1) {
          try {
            IJavaModelStatus status = null;
            // Validate extra attributes
            IClasspathAttribute[] extraAttributes = entry.getExtraAttributes();
            if (extraAttributes != null) {
              int length = extraAttributes.length;
              HashSet set = new HashSet(length);
              for (int i = 0; i < length; i++) {
                String attName = extraAttributes[i].getName();
                if (!set.add(attName)) {
                  status =
                      new JavaModelStatus(
                          IJavaModelStatusConstants.NAME_COLLISION,
                          Messages.bind(
                              Messages.classpath_duplicateEntryExtraAttribute,
                              new String[] {attName, entryPathMsg, projectName}));
                  break;
                }
              }
            }
            IClasspathContainer container =
                JavaModelManager.getJavaModelManager().getClasspathContainer(path, project);
            // container retrieval is performing validation check on container entry kinds.
            if (container == null) {
              if (status != null) return status;
              return new JavaModelStatus(
                  IJavaModelStatusConstants.CP_CONTAINER_PATH_UNBOUND, project, path);
            } else if (container == JavaModelManager.CONTAINER_INITIALIZATION_IN_PROGRESS) {
              // don't create a marker if initialization is in progress (case of cp initialization
              // batching)
              return JavaModelStatus.VERIFIED_OK;
            }
            IClasspathEntry[] containerEntries = container.getClasspathEntries();
            if (containerEntries != null) {
              for (int i = 0, length = containerEntries.length; i < length; i++) {
                IClasspathEntry containerEntry = containerEntries[i];
                int kind = containerEntry == null ? 0 : containerEntry.getEntryKind();
                if (containerEntry == null
                    || kind == IClasspathEntry.CPE_SOURCE
                    || kind == IClasspathEntry.CPE_VARIABLE
                    || kind == IClasspathEntry.CPE_CONTAINER) {
                  return new JavaModelStatus(
                      IJavaModelStatusConstants.INVALID_CP_CONTAINER_ENTRY, project, path);
                }
                IJavaModelStatus containerEntryStatus =
                    validateClasspathEntry(
                        project,
                        containerEntry,
                        container,
                        checkSourceAttachment,
                        true /*referred by container*/);
                if (!containerEntryStatus.isOK()) {
                  return containerEntryStatus;
                }
              }
            }
          } catch (JavaModelException e) {
            return new JavaModelStatus(e);
          }
        } else {
          return new JavaModelStatus(
              IJavaModelStatusConstants.INVALID_CLASSPATH,
              Messages.bind(
                  Messages.classpath_illegalContainerPath,
                  new String[] {entryPathMsg, projectName}));
        }
        break;

        // variable entry check
      case IClasspathEntry.CPE_VARIABLE:
        if (path.segmentCount() >= 1) {
          try {
            entry = JavaCore.getResolvedClasspathEntry(entry);
          } catch (AssertionFailedException e) {
            // Catch the assertion failure and throw java model exception instead
            // see bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=55992
            return new JavaModelStatus(IJavaModelStatusConstants.INVALID_PATH, e.getMessage());
          }
          if (entry == null) {
            return new JavaModelStatus(
                IJavaModelStatusConstants.CP_VARIABLE_PATH_UNBOUND, project, path);
          }

          // get validation status
          IJavaModelStatus status =
              validateClasspathEntry(
                  project, entry, null, checkSourceAttachment, false /*not referred by
 container*/);
          if (!status.isOK()) return status;

          // return deprecation status if any
          String variableName = path.segment(0);
          String deprecatedMessage = JavaCore.getClasspathVariableDeprecationMessage(variableName);
          if (deprecatedMessage != null) {
            return new JavaModelStatus(
                IStatus.WARNING,
                IJavaModelStatusConstants.DEPRECATED_VARIABLE,
                project,
                path,
                deprecatedMessage);
          }
          return status;
        } else {
          return new JavaModelStatus(
              IJavaModelStatusConstants.INVALID_CLASSPATH,
              Messages.bind(
                  Messages.classpath_illegalVariablePath,
                  new String[] {entryPathMsg, projectName}));
        }

        // library entry check
      case IClasspathEntry.CPE_LIBRARY:
        path = ClasspathEntry.resolveDotDot(project.getProject().getLocation(), path);

        // do not validate entries from Class-Path: in manifest
        // (these entries are considered optional since the user cannot act on them)
        // see https://bugs.eclipse.org/bugs/show_bug.cgi?id=252392

        String containerInfo = null;
        if (entryContainer != null) {
          if (entryContainer instanceof UserLibraryClasspathContainer) {
            containerInfo =
                Messages.bind(
                    Messages.classpath_userLibraryInfo,
                    new String[] {entryContainer.getDescription()});
          } else {
            containerInfo =
                Messages.bind(
                    Messages.classpath_containerInfo,
                    new String[] {entryContainer.getDescription()});
          }
        }
        IJavaModelStatus status =
            validateLibraryEntry(
                path,
                project,
                containerInfo,
                checkSourceAttachment ? entry.getSourceAttachmentPath() : null,
                entryPathMsg);
        // https://bugs.eclipse.org/bugs/show_bug.cgi?id=171136, ignore class path errors from
        // optional entries
        if (status.getCode() == IJavaModelStatusConstants.INVALID_CLASSPATH
            && ((ClasspathEntry) entry).isOptional()) status = JavaModelStatus.VERIFIED_OK;
        if (!status.isOK()) return status;
        break;

        // project entry check
      case IClasspathEntry.CPE_PROJECT:
        if (path.isAbsolute() && path.segmentCount() == 1) {
          IProject prereqProjectRsc = workspaceRoot.getProject(path.segment(0));
          IJavaProject prereqProject = JavaCore.create(prereqProjectRsc);
          try {
            if (!prereqProjectRsc.exists() || !prereqProjectRsc.hasNature(JavaCore.NATURE_ID)) {
              return new JavaModelStatus(
                  IJavaModelStatusConstants.INVALID_CLASSPATH,
                  Messages.bind(
                      Messages.classpath_unboundProject,
                      new String[] {path.segment(0), projectName}));
            }
            if (!prereqProjectRsc.isOpen()) {
              return new JavaModelStatus(
                  IJavaModelStatusConstants.INVALID_CLASSPATH,
                  Messages.bind(Messages.classpath_closedProject, new String[] {path.segment(0)}));
            }
            if (!JavaCore.IGNORE.equals(
                project.getOption(JavaCore.CORE_INCOMPATIBLE_JDK_LEVEL, true))) {
              long projectTargetJDK =
                  CompilerOptions.versionToJdkLevel(
                      project.getOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, true));
              long prereqProjectTargetJDK =
                  CompilerOptions.versionToJdkLevel(
                      prereqProject.getOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, true));
              if (prereqProjectTargetJDK > projectTargetJDK) {
                return new JavaModelStatus(
                    IJavaModelStatusConstants.INCOMPATIBLE_JDK_LEVEL,
                    project,
                    path,
                    Messages.bind(
                        Messages.classpath_incompatibleLibraryJDKLevel,
                        new String[] {
                          project.getElementName(),
                          CompilerOptions.versionFromJdkLevel(projectTargetJDK),
                          path.makeRelative().toString(),
                          CompilerOptions.versionFromJdkLevel(prereqProjectTargetJDK)
                        }));
              }
            }
          } catch (CoreException e) {
            return new JavaModelStatus(
                IJavaModelStatusConstants.INVALID_CLASSPATH,
                Messages.bind(
                    Messages.classpath_unboundProject,
                    new String[] {path.segment(0), projectName}));
          }
        } else {
          return new JavaModelStatus(
              IJavaModelStatusConstants.INVALID_CLASSPATH,
              Messages.bind(
                  Messages.classpath_illegalProjectPath,
                  new String[] {path.toString(), projectName}));
        }
        break;

        // project source folder
      case IClasspathEntry.CPE_SOURCE:
        if (((entry.getInclusionPatterns() != null && entry.getInclusionPatterns().length > 0)
                || (entry.getExclusionPatterns() != null
                    && entry.getExclusionPatterns().length > 0))
            && JavaCore.DISABLED.equals(
                project.getOption(JavaCore.CORE_ENABLE_CLASSPATH_EXCLUSION_PATTERNS, true))) {
          return new JavaModelStatus(
              IJavaModelStatusConstants.DISABLED_CP_EXCLUSION_PATTERNS, project, path);
        }
        if (entry.getOutputLocation() != null
            && JavaCore.DISABLED.equals(
                project.getOption(
                    JavaCore.CORE_ENABLE_CLASSPATH_MULTIPLE_OUTPUT_LOCATIONS, true))) {
          return new JavaModelStatus(
              IJavaModelStatusConstants.DISABLED_CP_MULTIPLE_OUTPUT_LOCATIONS, project, path);
        }
        if (path.isAbsolute() && !path.isEmpty()) {
          IPath projectPath = project.getProject().getFullPath();
          if (!projectPath.isPrefixOf(path) || JavaModel.getTarget(path, true) == null) {
            return new JavaModelStatus(
                IJavaModelStatusConstants.INVALID_CLASSPATH,
                Messages.bind(
                    Messages.classpath_unboundSourceFolder,
                    new String[] {entryPathMsg, projectName}));
          }
        } else {
          return new JavaModelStatus(
              IJavaModelStatusConstants.INVALID_CLASSPATH,
              Messages.bind(
                  Messages.classpath_illegalSourceFolderPath,
                  new String[] {entryPathMsg, projectName}));
        }
        break;
    }

    // Validate extra attributes
    IClasspathAttribute[] extraAttributes = entry.getExtraAttributes();
    if (extraAttributes != null) {
      int length = extraAttributes.length;
      HashSet set = new HashSet(length);
      for (int i = 0; i < length; i++) {
        String attName = extraAttributes[i].getName();
        if (!set.add(attName)) {
          return new JavaModelStatus(
              IJavaModelStatusConstants.NAME_COLLISION,
              Messages.bind(
                  Messages.classpath_duplicateEntryExtraAttribute,
                  new String[] {attName, entryPathMsg, projectName}));
        }
      }
    }

    return JavaModelStatus.VERIFIED_OK;
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=232816, Now we have the facility to include a
  // container
  // name in diagnostics. If the parameter ``container'' is not null, it is used to point to the
  // library
  // more fully.
  private static IJavaModelStatus validateLibraryEntry(
      IPath path,
      IJavaProject project,
      String container,
      IPath sourceAttachment,
      String entryPathMsg) {
    if (path.isAbsolute() && !path.isEmpty()) {
      Object target = JavaModel.getTarget(path, true);
      if (target == null) { // https://bugs.eclipse.org/bugs/show_bug.cgi?id=248661
        IPath workspaceLocation = workspaceRoot.getLocation();
        if (workspaceLocation.isPrefixOf(path)) {
          target = JavaModel.getTarget(path.makeRelativeTo(workspaceLocation).makeAbsolute(), true);
        }
      }
      if (target != null
          && !JavaCore.IGNORE.equals(
              project.getOption(JavaCore.CORE_INCOMPATIBLE_JDK_LEVEL, true))) {
        long projectTargetJDK =
            CompilerOptions.versionToJdkLevel(
                project.getOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, true));
        long libraryJDK = Util.getJdkLevel(target);
        if (libraryJDK != 0 && libraryJDK > projectTargetJDK) {
          if (container != null) {
            return new JavaModelStatus(
                IJavaModelStatusConstants.INCOMPATIBLE_JDK_LEVEL,
                project,
                path,
                Messages.bind(
                    Messages.classpath_incompatibleLibraryJDKLevelInContainer,
                    new String[] {
                      project.getElementName(),
                      CompilerOptions.versionFromJdkLevel(projectTargetJDK),
                      path.makeRelative().toString(),
                      container,
                      CompilerOptions.versionFromJdkLevel(libraryJDK)
                    }));
          } else {
            return new JavaModelStatus(
                IJavaModelStatusConstants.INCOMPATIBLE_JDK_LEVEL,
                project,
                path,
                Messages.bind(
                    Messages.classpath_incompatibleLibraryJDKLevel,
                    new String[] {
                      project.getElementName(),
                      CompilerOptions.versionFromJdkLevel(projectTargetJDK),
                      path.makeRelative().toString(),
                      CompilerOptions.versionFromJdkLevel(libraryJDK)
                    }));
          }
        }
      }
      if (target instanceof IResource) {
        IResource resolvedResource = (IResource) target;
        switch (resolvedResource.getType()) {
          case IResource.FILE:
            if (sourceAttachment != null
                && !sourceAttachment.isEmpty()
                && JavaModel.getTarget(sourceAttachment, true) == null) {
              if (container != null) {
                return new JavaModelStatus(
                    IJavaModelStatusConstants.INVALID_CLASSPATH,
                    Messages.bind(
                        Messages.classpath_unboundSourceAttachmentInContainedLibrary,
                        new String[] {sourceAttachment.toString(), path.toString(), container}));
              } else {
                return new JavaModelStatus(
                    IJavaModelStatusConstants.INVALID_CLASSPATH,
                    Messages.bind(
                        Messages.classpath_unboundSourceAttachment,
                        new String[] {
                          sourceAttachment.toString(), path.toString(), project.getElementName()
                        }));
              }
            }
            // https://bugs.eclipse.org/bugs/show_bug.cgi?id=229042
            // Validate the contents of the archive
            IJavaModelStatus status = validateLibraryContents(path, project, entryPathMsg);
            if (status != JavaModelStatus.VERIFIED_OK) return status;
            break;
          case IResource.FOLDER: // internal binary folder
            if (sourceAttachment != null
                && !sourceAttachment.isEmpty()
                && JavaModel.getTarget(sourceAttachment, true) == null) {
              if (container != null) {
                return new JavaModelStatus(
                    IJavaModelStatusConstants.INVALID_CLASSPATH,
                    Messages.bind(
                        Messages.classpath_unboundSourceAttachmentInContainedLibrary,
                        new String[] {sourceAttachment.toString(), path.toString(), container}));
              } else {
                return new JavaModelStatus(
                    IJavaModelStatusConstants.INVALID_CLASSPATH,
                    Messages.bind(
                        Messages.classpath_unboundSourceAttachment,
                        new String[] {
                          sourceAttachment.toString(), path.toString(), project.getElementName()
                        }));
              }
            }
        }
      } else if (target instanceof File) {
        File file = JavaModel.getFile(target);
        if (file == null) {
          if (container != null) {
            return new JavaModelStatus(
                IJavaModelStatusConstants.INVALID_CLASSPATH,
                Messages.bind(
                    Messages.classpath_illegalExternalFolderInContainer,
                    new String[] {path.toOSString(), container}));
          } else {
            return new JavaModelStatus(
                IJavaModelStatusConstants.INVALID_CLASSPATH,
                Messages.bind(
                    Messages.classpath_illegalExternalFolder,
                    new String[] {path.toOSString(), project.getElementName()}));
          }
        } else {
          if (sourceAttachment != null
              && !sourceAttachment.isEmpty()
              && JavaModel.getTarget(sourceAttachment, true) == null) {
            if (container != null) {
              return new JavaModelStatus(
                  IJavaModelStatusConstants.INVALID_CLASSPATH,
                  Messages.bind(
                      Messages.classpath_unboundSourceAttachmentInContainedLibrary,
                      new String[] {sourceAttachment.toString(), path.toOSString(), container}));
            } else {
              return new JavaModelStatus(
                  IJavaModelStatusConstants.INVALID_CLASSPATH,
                  Messages.bind(
                      Messages.classpath_unboundSourceAttachment,
                      new String[] {
                        sourceAttachment.toString(), path.toOSString(), project.getElementName()
                      }));
            }
          }
          // https://bugs.eclipse.org/bugs/show_bug.cgi?id=229042
          // Validate the contents of the archive
          if (file.isFile()) {
            IJavaModelStatus status = validateLibraryContents(path, project, entryPathMsg);
            if (status != JavaModelStatus.VERIFIED_OK) return status;
          }
        }
      } else {
        boolean isExternal =
            path.getDevice() != null
                || !ResourcesPlugin.getWorkspace().getRoot().getProject(path.segment(0)).exists();
        if (isExternal) {
          if (container != null) {
            return new JavaModelStatus(
                IJavaModelStatusConstants.INVALID_CLASSPATH,
                Messages.bind(
                    Messages.classpath_unboundLibraryInContainer,
                    new String[] {path.toOSString(), container}));
          } else {
            return new JavaModelStatus(
                IJavaModelStatusConstants.INVALID_CLASSPATH,
                Messages.bind(
                    Messages.classpath_unboundLibrary,
                    new String[] {path.toOSString(), project.getElementName()}));
          }
        } else {
          if (entryPathMsg == null)
            entryPathMsg =
                project.getElementName().equals(path.segment(0))
                    ? path.removeFirstSegments(1).makeRelative().toString()
                    : path.toString();
          if (container != null) {
            return new JavaModelStatus(
                IJavaModelStatusConstants.INVALID_CLASSPATH,
                Messages.bind(
                    Messages.classpath_unboundLibraryInContainer,
                    new String[] {entryPathMsg, container}));
          } else {
            return new JavaModelStatus(
                IJavaModelStatusConstants.INVALID_CLASSPATH,
                Messages.bind(
                    Messages.classpath_unboundLibrary,
                    new String[] {entryPathMsg, project.getElementName()}));
          }
        }
      }
    } else {
      if (entryPathMsg == null)
        entryPathMsg =
            project.getElementName().equals(path.segment(0))
                ? path.removeFirstSegments(1).makeRelative().toString()
                : path.toString();
      if (container != null) {
        return new JavaModelStatus(
            IJavaModelStatusConstants.INVALID_CLASSPATH,
            Messages.bind(
                Messages.classpath_illegalLibraryPathInContainer,
                new String[] {entryPathMsg, container}));
      } else {
        return new JavaModelStatus(
            IJavaModelStatusConstants.INVALID_CLASSPATH,
            Messages.bind(
                Messages.classpath_illegalLibraryPath,
                new String[] {entryPathMsg, project.getElementName()}));
      }
    }
    return JavaModelStatus.VERIFIED_OK;
  }

  static class UnknownXmlElements {
    String[] attributes;
    ArrayList children;
  }
}
