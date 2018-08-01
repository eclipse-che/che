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
package org.eclipse.che.plugin.maven.server.core.classpath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.che.maven.data.MavenArtifactKey;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;

/**
 * Helps to create and manage {@link org.eclipse.jdt.core.IClasspathEntry}. Inspired by
 * org.eclipse.m2e.jdt.internal.ClasspathEntryDescriptor
 *
 * @author Evgen Vidolob
 */
public class ClasspathEntryHelper {
  private IPath path;
  private IPath sourcePath;
  private IPath sourceRootPath;
  private IPath outputLocation;

  private boolean exported;
  private boolean combineAccessRules;

  private int kind;

  private List<IAccessRule> accessRules = new ArrayList<>();

  private Map<String, String> attributes = new HashMap<>();

  private Set<IPath> inclusionPatterns;
  private Set<IPath> exclusionPatterns;

  private MavenArtifactKey artifactKey;
  private Path sources;

  public ClasspathEntryHelper(IPath path, int kind) {
    this.path = path;
    this.kind = kind;
  }

  public ClasspathEntryHelper(IClasspathEntry classpathEntry) {
    setClasspathEntry(classpathEntry);
  }

  // Copied from org.eclipse.m2e.jdt.internal.ClasspathEntryDescriptor
  public IClasspathEntry toClasspathEntry() {
    Map<String, String> attributes = new HashMap<String, String>(this.attributes);

    if (artifactKey != null) {
      attributes.put(ClasspathManager.GROUP_ID_ATTRIBUTE, artifactKey.getGroupId());
      attributes.put(ClasspathManager.ARTIFACT_ID_ATTRIBUTE, artifactKey.getArtifactId());
      attributes.put(ClasspathManager.VERSION_ATTRIBUTE, artifactKey.getVersion());
      attributes.put(ClasspathManager.PACKAGING_ATTRIBUTE, artifactKey.getPackaging());
      if (artifactKey.getClassifier() != null) {
        attributes.put(ClasspathManager.CLASSIFIER_ATTRIBUTE, artifactKey.getClassifier());
      }
    }

    IClasspathAttribute[] attributesArray = new IClasspathAttribute[attributes.size()];
    int attributeIndex = 0;
    for (Map.Entry<String, String> attribute : attributes.entrySet()) {
      attributesArray[attributeIndex++] =
          JavaCore.newClasspathAttribute(attribute.getKey(), attribute.getValue());
    }

    IAccessRule[] accessRulesArray = accessRules.toArray(new IAccessRule[accessRules.size()]);
    IClasspathEntry entry;
    switch (kind) {
      case IClasspathEntry.CPE_CONTAINER:
        entry =
            JavaCore.newContainerEntry(
                path, //
                accessRulesArray, //
                attributesArray, //
                exported);
        break;
      case IClasspathEntry.CPE_LIBRARY:
        entry =
            JavaCore.newLibraryEntry(
                path, //
                sourcePath, //
                sourceRootPath, //
                accessRulesArray, //
                attributesArray, //
                exported);
        break;
      case IClasspathEntry.CPE_SOURCE:
        entry =
            JavaCore.newSourceEntry(
                path, //
                getInclusionPatterns(), //
                getExclusionPatterns(), //
                outputLocation, //
                attributesArray);
        break;
      case IClasspathEntry.CPE_PROJECT:
        entry =
            JavaCore.newProjectEntry(
                path, //
                accessRulesArray, //
                combineAccessRules, //
                attributesArray, //
                exported);
        break;
      case IClasspathEntry.CPE_VARIABLE:
        entry =
            JavaCore.newVariableEntry(
                path, //
                sourcePath, //
                sourceRootPath, //
                accessRulesArray, //
                attributesArray, //
                exported);
        break;
      default:
        throw new IllegalArgumentException(
            "Unsupported IClasspathEntry kind=" + kind); // $NON-NLS-1$
    }
    return entry;
  }

  private IPath[] getExclusionPatterns() {
    if (exclusionPatterns != null) {
      return exclusionPatterns.toArray(new IPath[exclusionPatterns.size()]);
    }
    return null;
  }

  private IPath[] getInclusionPatterns() {
    if (inclusionPatterns != null) {
      return inclusionPatterns.toArray(new IPath[inclusionPatterns.size()]);
    }
    return null;
  }

  // Copied from org.eclipse.m2e.jdt.internal.ClasspathEntryDescriptor
  private void setClasspathEntry(IClasspathEntry entry) {
    this.kind = entry.getEntryKind();
    this.path = entry.getPath();
    this.exported = entry.isExported();
    this.outputLocation = entry.getOutputLocation();

    this.accessRules = new ArrayList<>();
    for (IAccessRule rule : entry.getAccessRules()) {
      this.accessRules.add(rule);
    }

    this.attributes = new HashMap<>();
    for (IClasspathAttribute attribute : entry.getExtraAttributes()) {
      attributes.put(attribute.getName(), attribute.getValue());
    }

    this.sourcePath = entry.getSourceAttachmentPath();
    this.sourceRootPath = entry.getSourceAttachmentRootPath();
    setInclusionPatterns(entry.getInclusionPatterns());
    setExclusionPatterns(entry.getExclusionPatterns());
    this.combineAccessRules = entry.combineAccessRules();

    String groupId = attributes.get(ClasspathManager.GROUP_ID_ATTRIBUTE);
    String artifactId = attributes.get(ClasspathManager.ARTIFACT_ID_ATTRIBUTE);
    String version = attributes.get(ClasspathManager.VERSION_ATTRIBUTE);
    String packaging = attributes.get(ClasspathManager.PACKAGING_ATTRIBUTE);
    String classifier = attributes.get(ClasspathManager.CLASSIFIER_ATTRIBUTE);
    if (groupId != null && artifactId != null && version != null) {
      this.artifactKey = new MavenArtifactKey(groupId, artifactId, version, packaging, classifier);
    }
  }

  private void setInclusionPatterns(IPath[] inclusionPatterns) {
    if (inclusionPatterns == null) {
      this.inclusionPatterns = null;
    } else {
      this.inclusionPatterns = new HashSet<>(Arrays.asList(inclusionPatterns));
    }
  }

  public void setExclusionPatterns(IPath[] exclusionPatterns) {
    if (exclusionPatterns == null) {
      this.exclusionPatterns = null;
    } else {
      this.exclusionPatterns = new HashSet<>(Arrays.asList(exclusionPatterns));
    }
  }

  public IPath getPath() {
    return path;
  }

  public Map<String, String> getClasspathAttribute() {
    return attributes;
  }

  public void setClasspathAttribute(String key, String value) {
    attributes.put(key, value);
  }

  public void setArtifactKey(MavenArtifactKey artifactKey) {
    this.artifactKey = artifactKey;
  }

  public MavenArtifactKey getArtifactKey() {
    return artifactKey;
  }

  public void setSourcePath(Path sources) {
    this.sourcePath = sources;
  }

  public void setOutputLocation(IPath outputLocation) {
    this.outputLocation = outputLocation;
  }

  public IPath getOutputLocation() {
    return outputLocation;
  }
}
