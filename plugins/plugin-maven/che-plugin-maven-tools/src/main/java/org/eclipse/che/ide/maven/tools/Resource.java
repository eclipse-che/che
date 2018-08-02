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
package org.eclipse.che.ide.maven.tools;

import static java.lang.Boolean.parseBoolean;
import static java.util.Collections.emptyList;
import static org.eclipse.che.commons.xml.NewElement.createElement;
import static org.eclipse.che.commons.xml.XMLTreeLocation.after;
import static org.eclipse.che.commons.xml.XMLTreeLocation.afterAnyOf;
import static org.eclipse.che.commons.xml.XMLTreeLocation.before;
import static org.eclipse.che.commons.xml.XMLTreeLocation.inTheBegin;
import static org.eclipse.che.commons.xml.XMLTreeLocation.inTheEnd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.eclipse.che.commons.xml.Element;
import org.eclipse.che.commons.xml.ElementMapper;
import org.eclipse.che.commons.xml.NewElement;

/**
 * Describes <i>/project/build/resources/resource</i>, which contains information about where
 * associated with project files should be included.
 *
 * <p>Supports next data:
 *
 * <ul>
 *   <li>targetPath
 *   <li>filtering
 *   <li>directory
 *   <li>includes
 *   <li>excludes
 * </ul>
 *
 * @author Eugene Voevodin
 */
public class Resource {

  private static final ElementMapper<String> TEXT_MAPPER = new ElementTextMapper();

  private String targetPath;
  private String directory;
  private boolean filtering;
  private List<String> includes;
  private List<String> excludes;

  Element resourceElement;

  public Resource() {}

  Resource(Element resourceElement) {
    this.resourceElement = resourceElement;
    targetPath = resourceElement.getChildText("targetPath");
    directory = resourceElement.getChildText("directory");
    filtering = parseBoolean(resourceElement.getChildText("filtering"));
    if (resourceElement.hasSingleChild("includes")) {
      includes = resourceElement.getSingleChild("includes").getChildren(TEXT_MAPPER);
    }
    if (resourceElement.hasSingleChild("excludes")) {
      excludes = resourceElement.getSingleChild("excludes").getChildren(TEXT_MAPPER);
    }
  }

  /** Returns directory structure to place the set of resources from a build */
  public String getTargetPath() {
    return targetPath;
  }

  /** Specifies the directory structure to place the set of resources from a build */
  public Resource setTargetPath(String targetPath) {
    this.targetPath = targetPath;
    if (!isNew()) {
      if (targetPath == null) {
        resourceElement.removeChild("targetPath");
      } else if (resourceElement.hasChild("targetPath")) {
        resourceElement.getSingleChild("targetPath").setText(targetPath);
      } else {
        resourceElement.insertChild(createElement("targetPath", targetPath), inTheBegin());
      }
    }
    return this;
  }

  /** Returns directory where the resources are to be found */
  public String getDirectory() {
    return directory;
  }

  /** Specifies directory where the resource are to be found */
  public Resource setDirectory(String directory) {
    this.directory = directory;
    if (!isNew()) {
      if (directory == null) {
        resourceElement.removeChild("directory");
      } else if (resourceElement.hasSingleChild("directory")) {
        resourceElement.getSingleChild("directory").setText(directory);
      } else {
        resourceElement.insertChild(
            createElement("directory", directory),
            afterAnyOf("targetPath", "filtering").or(inTheBegin()));
      }
    }
    return this;
  }

  /**
   * Returns {@code true} if filtering is enabled for this resource, otherwise returns {@code false}
   */
  public boolean isFiltering() {
    return filtering;
  }

  public Resource setFiltering(boolean filtering) {
    this.filtering = filtering;
    if (!isNew()) {
      if (resourceElement.hasSingleChild("filtering")) {
        resourceElement.getSingleChild("filtering").setText(Boolean.toString(filtering));
      } else {
        resourceElement.insertChild(
            createElement("filtering", Boolean.toString(filtering)),
            after("targetPath").or(inTheBegin()));
      }
    }
    return this;
  }

  /**
   * Returns list of file patterns which specifies the files to include into specified directory.
   */
  public List<String> getIncludes() {
    if (includes == null) {
      return emptyList();
    }
    return new ArrayList<>(includes);
  }

  /**
   * Specifies list of file patterns which specifies the files to include into specified directory
   */
  public Resource setIncludes(Collection<String> includes) {
    if (includes == null || includes.isEmpty()) {
      removeIncludes();
    } else {
      setIncludes0(includes);
    }
    return this;
  }

  /** Returns list of file patterns which specifies the files to exclude from specified directory */
  public List<String> getExcludes() {
    if (excludes == null) {
      return emptyList();
    }
    return new ArrayList<>(excludes);
  }

  /**
   * Specifies list of file patterns which specifies the files to exclude from specified directory
   */
  public Resource setExcludes(Collection<String> excludes) {
    if (excludes == null || excludes.isEmpty()) {
      removeExcludes();
    } else {
      setExcludes0(excludes);
    }
    return this;
  }

  NewElement asXMLElement() {
    final NewElement resource = createElement("resource");
    if (targetPath != null) {
      resource.appendChild(createElement("targetPath", targetPath));
    }
    if (filtering) {
      resource.appendChild(createElement("filtering", "true"));
    }
    if (directory != null) {
      resource.appendChild(createElement("directory", directory));
    }
    if (includes != null && !includes.isEmpty()) {
      resource.appendChild(newXMLElement(includes, "includes", "include"));
    }
    if (excludes != null && !excludes.isEmpty()) {
      resource.appendChild(newXMLElement(excludes, "excludes", "exclude"));
    }
    return resource;
  }

  private void setIncludes0(Collection<String> includes) {
    this.includes = new ArrayList<>(includes);

    if (isNew()) return;
    // if includes element exists we should replace it children
    // with new set of includes, otherwise create element for it
    if (resourceElement.hasSingleChild("includes")) {
      final Element includesElement = resourceElement.getSingleChild("includes");
      // remove all includes from element
      for (Element inclusion : includesElement.getChildren()) {
        inclusion.remove();
      }
      // append each new inclusion to "includes" element
      for (String inclusion : includes) {
        includesElement.appendChild(createElement("include", inclusion));
      }
    } else {
      resourceElement.insertChild(
          newXMLElement(this.includes, "includes", "include"), before("excludes").or(inTheEnd()));
    }
  }

  private void removeIncludes() {
    if (!isNew()) {
      resourceElement.removeChild("includes");
    }
    includes = null;
  }

  private void removeExcludes() {
    if (!isNew()) {
      resourceElement.removeChild("excludes");
    }
    excludes = null;
  }

  private void setExcludes0(Collection<String> excludes) {
    this.excludes = new ArrayList<>(excludes);

    if (isNew()) return;
    // if excludes element exists we should replace it children
    // with new set of excludes, otherwise create element for it
    if (resourceElement.hasSingleChild("excludes")) {
      final Element excludesElement = resourceElement.getSingleChild("excludes");
      // remove all exclusions from element
      for (Element exclusion : excludesElement.getChildren()) {
        exclusion.remove();
      }
      // append each new exclusion to "excludes" element
      for (String exclusion : excludes) {
        excludesElement.appendChild(createElement("exclude", exclusion));
      }
    } else {
      resourceElement.appendChild(newXMLElement(this.excludes, "excludes", "exclude"));
    }
  }

  private boolean isNew() {
    return resourceElement == null;
  }

  private NewElement newXMLElement(List<String> text, String parentName, String childName) {
    final NewElement element = createElement(parentName);
    for (String line : text) {
      element.appendChild(createElement(childName, line));
    }
    return element;
  }

  private static class ElementTextMapper implements ElementMapper<String> {

    @Override
    public String map(Element element) {
      return element.getText();
    }
  }
}
