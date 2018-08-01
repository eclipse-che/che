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
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.eclipse.che.commons.xml.NewElement.createElement;
import static org.eclipse.che.commons.xml.XMLTreeLocation.before;
import static org.eclipse.che.commons.xml.XMLTreeLocation.beforeAnyOf;
import static org.eclipse.che.commons.xml.XMLTreeLocation.inTheBegin;
import static org.eclipse.che.commons.xml.XMLTreeLocation.inTheEnd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.che.commons.xml.Element;
import org.eclipse.che.commons.xml.ElementMapper;
import org.eclipse.che.commons.xml.NewElement;

/**
 * Describes profile for <i>/project/profiles/</i>
 *
 * <p>Supports next data:
 *
 * <ul>
 *   <li>id
 *   <li>activation
 *   <li>build
 *   <li>properties
 *   <li>modules
 *   <li>repositories
 *   <li>dependencies
 *   <li>dependencyManagement
 * </ul>
 */
public class Profile {
  public static final String SOURCE_POM = "pom";
  private static final ElementMapper<String> TEXT_MAPPER = new ElementTextMapper();
  private static final RepositoryMapper REPOSITORY_MAPPER = new RepositoryMapper();
  private static final ToDependencyMapper TO_DEPENDENCY_MAPPER = new ToDependencyMapper();

  public Element element;

  private String id;
  private Activation activation;
  private List<String> modules;
  private Build build;
  private List<Repository> repositories;
  private Map<String, String> properties;
  private Dependencies dependencies;
  private DependencyManagement dependencyManagement;

  Profile(Element element) {
    this.element = element;
    this.id = element.getChildText("id");
    final Element modules = element.getSingleChild("modules");
    if (element.hasSingleChild("modules") && modules != null) {
      this.modules = modules.getChildren(TEXT_MAPPER);
    }
    if (element.hasSingleChild("activation")) {
      activation = new Activation(element.getSingleChild("activation"));
    }
    if (element.hasSingleChild("build")) {
      build = new Build(element.getSingleChild("build"));
    }
    final Element repositories = element.getSingleChild("repositories");
    if (element.hasSingleChild("repositories") && repositories != null) {
      this.repositories = repositories.getChildren(REPOSITORY_MAPPER);
    }
    final Element elementDependencies = element.getSingleChild("dependencies");
    if (element.hasSingleChild("dependencies") && elementDependencies != null) {
      final List<Dependency> dependencies = elementDependencies.getChildren(TO_DEPENDENCY_MAPPER);
      this.dependencies = new Dependencies(element, dependencies);
    }
    final Element properties = element.getSingleChild("properties");
    if (element.hasSingleChild("properties") && properties != null) {
      this.properties = fetchProperties(properties);
    }
    final Element dependencyManagementDependencies =
        element.getSingleChild("dependencyManagement/dependencies");
    if (element.hasSingleChild("dependencyManagement")
        && dependencyManagementDependencies != null) {
      final Element dm = element.getSingleChild("dependencyManagement");
      final List<Dependency> dependencies =
          dependencyManagementDependencies.getChildren(TO_DEPENDENCY_MAPPER);
      this.dependencyManagement = new DependencyManagement(dm, dependencies);
    }
  }

  /** Returns profile identifier. */
  public String getId() {
    return id;
  }

  /**
   * Sets profile identifier.
   *
   * @param id new profile identifier, if it is {@code null} then <i>id</i> element will be removed
   *     from xml if exists
   */
  public Profile setId(String id) {
    this.id = id;
    if (!isNew()) {
      if (isNullOrEmpty(id)) {
        element.removeChild("id");
      } else {
        final Element idElement = element.getSingleChild("id");
        if (element.hasSingleChild("id") && idElement != null) {
          idElement.setText(id);
        } else {
          element.insertChild(createElement("id", id), inTheBegin());
        }
      }
    }
    return this;
  }

  public Activation getActivation() {
    return activation;
  }

  /**
   * Sets activation configuration, see {@link Activation}
   *
   * @param activation new activation
   * @return this profile instance
   */
  public Profile setActivation(Activation activation) {
    this.activation = activation;
    if (!isNew()) {
      if (activation == null) {
        element.removeChild("activation");
      } else if (element.hasSingleChild("activation")) {
        element.removeChild("activation");
        element.appendChild(activation.asXMLElement());
      } else {
        element.appendChild(activation.asXMLElement());
      }
    }
    return this;
  }

  /**
   * Returns profile properties or empty map when the profile doesn't have properties
   *
   * <p><b>Note: update methods should not be used on returned map</b>
   */
  public Map<String, String> getProperties() {
    if (properties == null) {
      return emptyMap();
    }
    return new HashMap<>(properties);
  }

  public List<Repository> getRepositories() {
    if (repositories == null) {
      return emptyList();
    }
    return new ArrayList<>(repositories);
  }

  /**
   * Sets collection of repositories.
   *
   * <p><b>Note: all existing repositories will be removed from profile and xml as well</b>
   *
   * @param repositories new plugin repositories
   */
  public Profile setRepositories(Collection<? extends Repository> repositories) {
    for (Repository repository : repositories()) {
      repository.remove();
    }
    if (repositories != null && !repositories.isEmpty()) {
      for (Repository repository : repositories) {
        addRepository(repository);
      }
    } else {
      element.removeChild("repositories");
      this.repositories = null;
    }
    return this;
  }

  public List<Dependency> getDependencies() {
    return dependencies == null ? emptyList() : dependencies.get();
  }

  public Profile setDependencies(List<Dependency> dependencies) {
    this.dependencies = new Dependencies(element, dependencies);

    return this;
  }

  public DependencyManagement getDependencyManagement() {
    return dependencyManagement;
  }

  /**
   * Sets default dependency information for projects that inherit from this one. If new dependency
   * management is {@code null} removes old dependency management from profile and from xml as well
   *
   * @param dependencyManagement new project dependency management
   * @return this profile instance
   */
  public Profile setDependencyManagement(DependencyManagement dependencyManagement) {
    this.dependencyManagement = dependencyManagement;
    if (dependencyManagement == null) {
      element.removeChild("dependencyManagement");
    } else {
      final Element dependencyManagementElement = element.getSingleChild("dependencyManagement");
      if (element.hasSingleChild("dependencyManagement") && dependencyManagementElement != null) {
        dependencyManagement.dmElement =
            dependencyManagementElement.replaceWith(dependencyManagement.asXMLElement());
      } else {
        element.insertChild(
            this.dependencyManagement.asXMLElement(), beforeAnyOf("dependencies").or(inTheEnd()));
        dependencyManagement.dmElement = dependencyManagementElement;
      }
    }
    return this;
  }

  public Build getBuild() {
    return build;
  }

  /**
   * Sets build configuration, see {@link Activation}
   *
   * @param build new build
   * @return this profile instance
   */
  public Profile setBuild(Build build) {
    this.build = build;
    if (!isNew()) {
      if (build == null) {
        element.removeChild("build");
      } else if (element.hasSingleChild("build")) {
        element.removeChild("build");
        element.appendChild(build.asXMLElement());
      } else {
        element.appendChild(build.asXMLElement());
      }
    }
    return this;
  }

  /** Returns list of modules. */
  public List<String> getModules() {
    if (modules == null) {
      return emptyList();
    }
    return new ArrayList<>(modules);
  }

  /** Specifies list of modules. */
  public Profile setModules(Collection<String> modules) {
    if (modules == null || modules.isEmpty()) {
      removeModules();
    } else {
      setModules0(modules);
    }
    return this;
  }

  public void remove() {
    if (element != null) {
      element.remove();
      element = null;
    }
  }

  /** Returns profile element as XML element. */
  public NewElement asXMLElement() {
    final NewElement profile = createElement("profile");
    if (!isNullOrEmpty(id)) {
      profile.appendChild(createElement("id", id));
    }
    if (modules != null && !modules.isEmpty()) {
      profile.appendChild(newModulesXMLElement(modules));
    }
    if (activation != null) {
      profile.appendChild(activation.asXMLElement());
    }
    if (build != null) {
      profile.appendChild(build.asXMLElement());
    }
    if (repositories != null && !repositories.isEmpty()) {
      profile.appendChild(newRepositoryElement(repositories));
    }
    if (dependencies != null && !dependencies.get().isEmpty()) {
      profile.appendChild(newDependencyElement(dependencies.get()));
    }

    if (properties != null && !properties.isEmpty()) {
      profile.appendChild(newPropertiesElement(properties));
    }

    if (dependencyManagement != null && !dependencyManagement.getDependencies().isEmpty()) {
      profile.appendChild(newDependencyManagementElement(dependencies.get()));
    }

    return profile;
  }

  private Profile addRepository(Repository repository) {
    repositories().add(repository);
    final Element repositoriesElement = element.getSingleChild("repositories");
    if (element.hasSingleChild("repositories") && repositoriesElement != null) {
      repositoriesElement.appendChild(repository.asXMLElement());
      repository.element = element.getLastChild();
    } else {
      element.insertChild(
          createElement("repositories", repository.asXMLElement()),
          beforeAnyOf("pluginRepositories", "activation", "build").or(inTheEnd()));
      if (repositoriesElement != null) {
        repository.element = repositoriesElement.getFirstChild();
      }
    }
    return this;
  }

  private static Map<String, String> fetchProperties(Element propertiesElement) {
    final Map<String, String> properties = new HashMap<>();
    for (Element property : propertiesElement.getChildren()) {
      properties.put(property.getName(), property.getText());
    }
    return properties;
  }

  private Map<String, String> properties() {
    return properties == null ? properties = new HashMap<>() : properties;
  }

  private List<Repository> repositories() {
    return repositories == null ? repositories = new ArrayList<>() : repositories;
  }

  private void setModules0(Collection<String> modules) {
    this.modules = new ArrayList<>(modules);

    if (isNew()) return;
    // if modules element exists we should replace it children
    // with new set of modules, otherwise create element for it
    if (element.hasSingleChild("modules")) {
      final Element modulesElement = element.getSingleChild("modules");
      if (modulesElement == null) {
        return;
      }
      // remove all modules from element
      for (Element module : modulesElement.getChildren()) {
        module.remove();
      }
      // append each new module to "modules" element
      for (String module : modules) {
        element.appendChild(createElement("module", module));
      }
    } else {
      element.insertChild(newModulesXMLElement(this.modules), before("build").or(inTheEnd()));
    }
  }

  private NewElement newRepositoryElement(List<Repository> repositories) {
    final NewElement repositoriesElement = createElement("repositories");
    for (Repository repository : repositories) {
      repositoriesElement.appendChild(repository.asXMLElement());
    }
    return repositoriesElement;
  }

  private NewElement newDependencyElement(List<Dependency> dependencies) {
    final NewElement dependenciesElement = createElement("dependencies");
    for (Dependency dependency : dependencies) {
      dependenciesElement.appendChild(dependency.asXMLElement());
    }
    return dependenciesElement;
  }

  private NewElement newPropertiesElement(Map<String, String> properties) {
    final NewElement propertiesElement = createElement("properties");
    for (String key : properties.keySet()) {
      propertiesElement.appendChild(createElement(key, properties.get(key)));
    }
    return propertiesElement;
  }

  private NewElement newDependencyManagementElement(List<Dependency> dependencies) {
    final NewElement dependenciesElement = createElement("dependencies");
    for (Dependency dependency : dependencies) {
      dependenciesElement.appendChild(dependency.asXMLElement());
    }
    final NewElement dependencyManagement = createElement("dependencyManagement");
    dependencyManagement.appendChild(dependenciesElement);
    return dependencyManagement;
  }

  private NewElement newModulesXMLElement(List<String> text) {
    final NewElement element = createElement("modules");
    for (String line : text) {
      element.appendChild(createElement("module", line));
    }
    return element;
  }

  private void removeModules() {
    if (!isNew()) {
      element.removeChild("modules");
    }
    modules = null;
  }

  private boolean isNew() {
    return element == null;
  }

  private static class ElementTextMapper implements ElementMapper<String> {
    @Override
    public String map(Element element) {
      return element.getText();
    }
  }

  private static class RepositoryMapper implements ElementMapper<Repository> {

    @Override
    public Repository map(Element element) {
      return new Repository(element);
    }
  }

  private static class ToDependencyMapper implements ElementMapper<Dependency> {

    @Override
    public Dependency map(Element element) {
      return new Dependency(element);
    }
  }
}
