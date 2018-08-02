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

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.eclipse.che.commons.xml.NewElement.createElement;
import static org.eclipse.che.commons.xml.XMLTreeLocation.after;
import static org.eclipse.che.commons.xml.XMLTreeLocation.inTheBegin;
import static org.eclipse.che.commons.xml.XMLTreeLocation.inTheEnd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.eclipse.che.commons.xml.Element;
import org.eclipse.che.commons.xml.ElementMapper;
import org.eclipse.che.commons.xml.NewElement;

/**
 * Describes build for <i>/project/profiles/profile/activation.</i>
 *
 * <p>Supports next data:
 *
 * <ul>
 *   <li>defaultGoal
 *   <li>resources
 *   <li>testResources
 *   <li>finalName
 * </ul>
 */
public class BuildBase {
  private static final ElementMapper<Resource> RESOURCE_MAPPER = new ResourceMapper();

  public Element element;

  private String finalName;
  private String defaultGoal;
  private List<Resource> resources;
  private List<Resource> testResources;

  BuildBase() {}

  BuildBase(Element element) {
    this.element = element;

    if (element.hasSingleChild("finalName")) {
      finalName = element.getChildText("finalName");
    }
    if (element.hasSingleChild("defaultGoal")) {
      defaultGoal = element.getChildText("defaultGoal");
    }
    if (element.hasSingleChild("resources")) {
      resources = element.getSingleChild("resources").getChildren(RESOURCE_MAPPER);
    }
    if (element.hasSingleChild("testResources")) {
      testResources = element.getSingleChild("testResources").getChildren(RESOURCE_MAPPER);
    }
  }

  public String getFinalName() {
    return finalName;
  }

  /**
   * Sets final name of current build element.
   *
   * <p>If {@code finalName} is {@code null} and this build instance is associated with xml element
   * then {@code finalName} will be removed from model as well as from xml.
   *
   * @param finalName new final name
   */
  public BuildBase setFinalName(String finalName) {
    this.finalName = finalName;
    if (!isNew()) {
      if (isNullOrEmpty(finalName)) {
        element.removeChild("finalName");
      } else if (element.hasSingleChild("finalName")) {
        element.getSingleChild("finalName").setText(finalName);
      } else {
        element.insertChild(createElement("finalName", finalName), inTheBegin());
      }
    }
    return this;
  }

  public String getDefaultGoal() {
    return defaultGoal;
  }

  /**
   * Sets default goal of current build element.
   *
   * <p>If {@code defaultGoal} is {@code null} and this build instance is associated with xml
   * element then {@code defaultGoal} will be removed from model as well as from xml.
   *
   * @param defaultGoal new default goal
   */
  public BuildBase setDefaultGoal(String defaultGoal) {
    this.defaultGoal = defaultGoal;
    if (!isNew()) {
      if (isNullOrEmpty(defaultGoal)) {
        element.removeChild("defaultGoal");
      } else if (element.hasSingleChild("defaultGoal")) {
        element.getSingleChild("defaultGoal").setText(defaultGoal);
      } else {
        element.insertChild(
            createElement("defaultGoal", defaultGoal), after("finalName").or(inTheEnd()));
      }
    }
    return this;
  }

  public List<Resource> getResources() {
    return resources;
  }

  /**
   * Sets build resources, each resource contains information about where associated with project
   * files should be included.
   *
   * <p>If {@code resources} is {@code null} or <i>empty</i> and this build instance is associated
   * with xml element then {@code resources} will be removed from model as well as from xml.
   *
   * @param resources new build resources
   * @return this build instance
   */
  public BuildBase setResources(Collection<? extends Resource> resources) {
    if (resources == null || resources.isEmpty()) {
      removeResources();
    } else {
      setResources0(resources);
    }
    return this;
  }

  public List<Resource> getTestResources() {
    return testResources;
  }

  /**
   * Sets build test resources, each resource contains information about where associated with
   * project test files should be included.
   *
   * <p>If {@code resources} is {@code null} or <i>empty</i> and this build instance is associated
   * with xml element then {@code resources} will be removed from model as well as from xml.
   *
   * @param testResources new build test resources
   * @return this build instance
   */
  public BuildBase setTestResources(List<Resource> testResources) {
    if (testResources == null || testResources.isEmpty()) {
      removeTestResources();
    } else {
      setTestResources0(testResources);
    }
    return this;
  }

  public void remove() {
    if (element != null) {
      element.remove();
      element = null;
    }
  }

  private void setResources0(Collection<? extends Resource> resources) {
    this.resources = new ArrayList<>(resources);

    if (isNew()) {
      return;
    }
    if (element.hasSingleChild("resources")) {
      final Element resourcesElement = element.getSingleChild("resources");
      for (Element resource : resourcesElement.getChildren()) {
        resource.remove();
      }
      for (Resource resource : resources) {
        resourcesElement.appendChild(resource.asXMLElement());
        resource.resourceElement = resourcesElement.getLastChild();
      }
    } else {
      element.appendChild(newResourcesElement(this.resources));
    }
  }

  private void removeResources() {
    if (!isNew()) {
      element.removeChild("resources");
    }
    this.resources = null;
  }

  private void setTestResources0(Collection<? extends Resource> resources) {
    this.testResources = new ArrayList<>(resources);

    if (isNew()) {
      return;
    }
    if (element.hasSingleChild("testResources")) {
      final Element resourcesElement = element.getSingleChild("testResources");
      for (Element resource : resourcesElement.getChildren()) {
        resource.remove();
      }
      for (Resource resource : resources) {
        resourcesElement.appendChild(resource.asXMLElement());
        resource.resourceElement = resourcesElement.getLastChild();
      }
    } else {
      element.appendChild(newResourcesElement(this.testResources));
    }
  }

  private void removeTestResources() {
    if (!isNew()) {
      element.removeChild("testResources");
    }
    this.testResources = null;
  }

  private NewElement newResourcesElement(List<Resource> resources) {
    final NewElement resourcesElement = createElement("resources");
    for (Resource resource : resources) {
      resourcesElement.appendChild(resource.asXMLElement());
    }
    return resourcesElement;
  }

  protected NewElement newTestResourcesElement(List<Resource> resources) {
    final NewElement resourcesElement = createElement("testResources");
    for (Resource resource : resources) {
      resourcesElement.appendChild(resource.asXMLElement());
    }
    return resourcesElement;
  }

  private static class ResourceMapper implements ElementMapper<Resource> {

    @Override
    public Resource map(Element element) {
      return new Resource(element);
    }
  }

  private static class ElementTextMapper implements ElementMapper<String> {
    @Override
    public String map(Element element) {
      return element.getText();
    }
  }

  private boolean isNew() {
    return element == null;
  }
}
