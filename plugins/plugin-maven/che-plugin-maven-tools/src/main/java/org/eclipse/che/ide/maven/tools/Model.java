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

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Objects.requireNonNull;
import static org.eclipse.che.commons.xml.NewElement.createElement;
import static org.eclipse.che.commons.xml.XMLTreeLocation.after;
import static org.eclipse.che.commons.xml.XMLTreeLocation.afterAnyOf;
import static org.eclipse.che.commons.xml.XMLTreeLocation.beforeAnyOf;
import static org.eclipse.che.commons.xml.XMLTreeLocation.inTheBegin;
import static org.eclipse.che.commons.xml.XMLTreeLocation.inTheEnd;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.che.commons.xml.Element;
import org.eclipse.che.commons.xml.ElementMapper;
import org.eclipse.che.commons.xml.NewElement;
import org.eclipse.che.commons.xml.XMLTree;

/**
 * The {@code <project>} element is the root of the descriptor.
 *
 * <p>Supported next data:
 *
 * <ul>
 *   <li>modelVersion
 *   <li>artifactId
 *   <li>groupId
 *   <li>version
 *   <li>name
 *   <li>description
 *   <li>packaging
 *   <li>parent
 *   <li>build
 *   <li>profiles
 *   <li>dependencyManagement
 *   <li>properties
 *   <li>modules
 *   <li>dependencies
 *   <li>repositories
 *   <li>pluginRepositories
 * </ul>
 *
 * Order of elements in model based on <a
 * href="http://maven.apache.org/developers/conventions/code.html">official recommended order</a>.
 * It means that each newly added element will be added to the right place of delegated xml file -
 * when it is possible to do so.
 *
 * <p>This maven pom model implementation based on {@link XMLTree} and targeted to save pom.xml
 * content formatting and elements positions.
 *
 * @author Eugene Voeovodin
 */
public final class Model {

  /**
   * Reads model from input stream.
   *
   * <p>Doesn't close the stream
   *
   * @param is input stream to read from
   * @return fetched model
   * @throws IOException if any i/o error occurs
   * @throws org.eclipse.che.commons.xml.XMLTreeException when input stream contains not valid xml
   *     content
   * @throws NullPointerException when given {@code is} is {@code null}
   */
  public static Model readFrom(InputStream is) throws IOException {
    return fetchModel(XMLTree.from(requireNonNull(is, "Required not null input stream")));
  }

  /**
   * Reads model from given file, or if given file is a directory reads model from the
   * <i>pom.xml</i> which is under the given directory
   *
   * <p>After reading from pom file model will be associated with it, so {@link #getPomFile()} and
   * will return pom file and {@link #getProjectDirectory()} will return pom file parent directory.
   *
   * @param file <i>pom.xml</i> to read model from or its parent directory
   * @return fetched model
   * @throws IOException if any i/o error occurs
   * @throws org.eclipse.che.commons.xml.XMLTreeException when input stream contains not valid xml
   *     content
   * @throws NullPointerException when given {@code file} is {@code null}
   */
  public static Model readFrom(File file) throws IOException {
    requireNonNull(file, "Required not null file");
    if (file.isDirectory()) {
      return readFrom(new File(file, "pom.xml"));
    }
    return fetchModel(XMLTree.from(file)).setPomFile(file);
  }

  /**
   * Reads model from given path. The behaviour is same to {@link #readFrom(java.io.File)}
   *
   * @param path to read from
   * @return fetched model
   * @throws NullPointerException when given {@code path} is {@code null}
   * @throws IOException if any i/o error occurs
   * @throws org.eclipse.che.commons.xml.XMLTreeException when input stream contains not valid xml
   *     content
   */
  public static Model readFrom(Path path) throws IOException {
    return readFrom(requireNonNull(path.toFile(), "Required not null model"));
  }

  /**
   * Creates new pom xml model with root "project" element.
   *
   * @return created model
   */
  public static Model createModel() {
    final XMLTree tree = XMLTree.create("project");
    tree.getRoot()
        .setAttribute("xmlns", "http://maven.apache.org/POM/4.0.0")
        .setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance")
        .setAttribute(
            "xsi:schemaLocation",
            "http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd");
    return new Model(tree);
  }

  private static final ToModuleMapper TO_MODULE_MAPPER = new ToModuleMapper();
  private static final ToProfileMapper TO_PROFILE_MAPPER = new ToProfileMapper();
  private static final ToDependencyMapper TO_DEPENDENCY_MAPPER = new ToDependencyMapper();
  private static final ToRepositoryMapper TO_REPOSITORY_MAPPER = new ToRepositoryMapper();

  private String modelVersion;
  private String groupId;
  private String artifactId;
  private String version;
  private String packaging;
  private String name;
  private String description;
  private Parent parent;
  private Build build;
  private DependencyManagement dependencyManagement;
  private Map<String, String> properties;
  private List<String> modules;
  private List<Repository> repositories;
  private List<Repository> pluginRepositories;
  private List<Profile> profiles;
  private Dependencies dependencies;
  private File pom;

  private final XMLTree tree;
  private final Element root;

  private Model(XMLTree tree) {
    this.tree = tree;
    root = tree.getRoot();
  }

  /**
   * Get the identifier for this artifact that is unique within the group given by the group ID. An
   * artifact is something that is either produced or used by a project. Examples of artifacts
   * produced by Maven for a project include: JARs, source and binary distributions, and WARs.
   */
  public String getArtifactId() {
    return artifactId;
  }

  /**
   * Get a detailed description of the project, used by Maven whenever it needs to describe the
   * project, such as on the web site. While this element can be specified as CDATA to enable the
   * use of HTML tags within the description, it is discouraged to allow plain text representation.
   * If you need to modify the index page of the generated web site, you are able to specify your
   * own instead of adjusting this text.
   */
  public String getDescription() {
    return description;
  }

  /**
   * Get a universally unique identifier for a project. It is normal to use a fully-qualified
   * package name to distinguish it from other projects with a similar name (eg. <code>
   * org.apache.maven</code>).
   */
  public String getGroupId() {
    return groupId;
  }

  /** Get declares to which version of project descriptor this POM conforms. */
  public String getModelVersion() {
    return modelVersion;
  }

  /** Get the full name of the project. */
  public String getName() {
    return name;
  }

  /**
   * Get the type of artifact this project produces, for example <code>jar</code> <code>war</code>
   * <code>ear</code> <code>pom</code>. Plugins can create their own packaging, and therefore their
   * own packaging types, so this list does not contain all possible types.
   */
  public String getPackaging() {
    return packaging;
  }

  /**
   * Get the location of the parent project, if one exists. Values from the parent project will be
   * the default for this project if they are left unspecified. The location is given as a group ID,
   * artifact ID and version.
   */
  public Parent getParent() {
    return parent;
  }

  /** Get the current version of the artifact produced by this project. */
  public String getVersion() {
    return version;
  }

  /** Returns project build */
  public Build getBuild() {
    return build;
  }

  /**
   * Returns list of dependencies or empty list if project doesn't have dependencies.
   *
   * <p><b>Note: update methods should not be used on returned list</b>
   *
   * @see #dependencies()
   */
  public List<Dependency> getDependencies() {
    return dependencies().get();
  }

  /** Returns returns {@link Dependencies} instance which helps to manage project dependencies */
  public Dependencies dependencies() {
    if (dependencies == null) {
      dependencies = new Dependencies(root);
    }
    return dependencies;
  }

  /**
   * Returns dependency management, which contains default dependency information for projects that
   * inherit from this one. The dependencies in this section are not immediately resolved. Instead,
   * when a POM derived from this one declares a dependency described by a matching groupId and
   * artifactId, the version and other values from this section are used for that dependency if they
   * were not already specified.
   */
  public DependencyManagement getDependencyManagement() {
    return dependencyManagement;
  }

  /**
   * Returns project modules as list of module names, or empty list when project doesn't have
   * modules.
   *
   * <p><b>Note: update methods should not be used on returned list</b>
   */
  public List<String> getModules() {
    if (modules == null) {
      return emptyList();
    }
    return new ArrayList<>(modules);
  }

  /**
   * Returns list of repositories which are collections of artifacts which adhere to the Maven
   * repository directory layout.
   *
   * <p>Repositories exist as a place to collect and store artifacts.
   */
  public List<Repository> getRepositories() {
    if (repositories == null) {
      return emptyList();
    }
    return new ArrayList<>(repositories);
  }

  /** Returns list of profiles. */
  public List<Profile> getProfiles() {
    if (profiles == null) {
      return emptyList();
    }
    return new ArrayList<>(profiles);
  }

  /**
   * Sets list of profiles.
   *
   * <p><b>Note: all existing profiles will be removed from model and xml as well</b>
   *
   * @param profiles new profiles
   */
  public Model setProfiles(Collection<? extends Profile> profiles) {
    // remove existing profiles
    for (Profile profile : profiles()) {
      profile.remove();
    }
    // add profiles if necessary
    if (profiles != null && !profiles.isEmpty()) {
      for (Profile profile : profiles) {
        addProfile(profile);
      }
    } else {
      root.removeChild("profiles");
      this.profiles = null;
    }
    return this;
  }

  /**
   * Returns list of repositories which are collections of plugin artifacts.
   *
   * <p>Serves as a place to collect and store plugin artifacts.
   */
  public List<Repository> getPluginRepositories() {
    if (pluginRepositories == null) {
      return emptyList();
    }
    return new ArrayList<>(pluginRepositories);
  }

  /**
   * Adds plugin repository
   *
   * @param pluginRepository new plugin repository
   */
  public Model addPluginRepository(Repository pluginRepository) {
    requireNonNull(pluginRepository, "Required not null plugin repository");
    pluginRepositories().add(pluginRepository);
    // add plugin repository to xml
    if (root.hasSingleChild("pluginRepositories")) {
      root.getSingleChild("pluginRepositories").appendChild(pluginRepository.asXMLElement());
      pluginRepository.element = root.getLastChild();
    } else {
      root.insertChild(
          createElement("pluginRepositories", pluginRepository.asXMLElement()),
          beforeAnyOf("build", "reporting", "profiles").or(inTheEnd()));
      pluginRepository.element = root.getSingleChild("pluginRepositories").getFirstChild();
    }
    return this;
  }

  /**
   * Sets collection of plugin repositories.
   *
   * <p><b>Note: all existing plugin repositories will be removed from model and xml as well</b>
   *
   * @param pluginRepositories new plugin repositories
   */
  public Model setPluginRepositories(Collection<? extends Repository> pluginRepositories) {
    // remove existing plugin repositories
    for (Repository pluginRepository : pluginRepositories()) {
      pluginRepository.remove();
    }
    // add plugin repositories if necessary
    if (pluginRepositories != null && !pluginRepositories.isEmpty()) {
      for (Repository pluginRepository : pluginRepositories) {
        addPluginRepository(pluginRepository);
      }
    } else {
      root.removeChild("pluginRepositories");
      this.pluginRepositories = null;
    }
    return this;
  }

  /**
   * Adds plugin repository
   *
   * @param repository new plugin repository
   */
  public Model addRepository(Repository repository) {
    requireNonNull(repository, "Required not null repository");
    repositories().add(repository);
    // add repository to xml
    if (root.hasSingleChild("repositories")) {
      root.getSingleChild("repositories").appendChild(repository.asXMLElement());
      repository.element = root.getLastChild();
    } else {
      root.insertChild(
          createElement("repositories", repository.asXMLElement()),
          beforeAnyOf("pluginRepositories", "build", "reporting", "profiles").or(inTheEnd()));
      repository.element = root.getSingleChild("repositories").getFirstChild();
    }
    return this;
  }

  /**
   * Adds a profile to the mocel
   *
   * @param profile profile
   */
  public Model addProfile(Profile profile) {
    requireNonNull(profile, "Required not null profile");
    profiles().add(profile);
    // add profile to xml
    if (root.hasSingleChild("profiles")) {
      root.getSingleChild("profiles").appendChild(profile.asXMLElement());
      profile.element = root.getLastChild();
    } else {
      root.insertChild(
          createElement("profiles", profile.asXMLElement()),
          beforeAnyOf("name", "build", "modules").or(inTheEnd()));
      profile.element = root.getSingleChild("profiles").getFirstChild();
    }
    return this;
  }

  /**
   * Sets collection of repositories.
   *
   * <p><b>Note: all existing repositories will be removed from model and xml as well</b>
   *
   * @param repositories new plugin repositories
   */
  public Model setRepositories(Collection<? extends Repository> repositories) {
    // remove existing repositories
    for (Repository repository : repositories()) {
      repository.remove();
    }
    // add repositories if necessary
    if (repositories != null && !repositories.isEmpty()) {
      for (Repository repository : repositories) {
        addRepository(repository);
      }
    } else {
      root.removeChild("repositories");
      this.repositories = null;
    }
    return this;
  }

  /**
   * Returns project properties or empty map when project doesn't have properties
   *
   * <p><b>Note: update methods should not be used on returned map</b>
   */
  public Map<String, String> getProperties() {
    if (properties == null) {
      return emptyMap();
    }
    return new HashMap<>(properties);
  }

  /**
   * Adds new module to the project. If project doesn't have modules it will be created as well.
   *
   * @param newModule module name to be added
   * @return this model instance
   * @throws NullPointerException when given module name is {@code null}
   */
  public Model addModule(String newModule) {
    requireNonNull(newModule, "Required not null module");
    modules().add(newModule);
    // add module to xml tree
    if (root.hasSingleChild("modules")) {
      root.getSingleChild("modules").appendChild(createElement("module", newModule));
    } else {
      root.insertChild(
          createElement("modules", createElement("module", newModule)),
          beforeAnyOf("dependencyManagement", "dependencies", "build").or(inTheEnd()));
    }
    return this;
  }

  /**
   * Adds new property to the project. If property with given key already exists its value going to
   * be changed with new one.
   *
   * @param name property name
   * @param value property value
   * @return this model instance
   * @throws NullPointerException when given {@code name} or {@code value} is {@code null}
   */
  public Model addProperty(String name, String value) {
    requireNonNull(name, "Property name should not be null");
    requireNonNull(value, "Property value should not be null");
    addPropertyToXML(name, value);
    properties().put(name, value);
    return this;
  }

  /**
   * Removes property with given name from model.
   *
   * <p>If last property was removed from model then properties will be removed from xml. If project
   * doesn't have property with given name then nothing will be done.
   *
   * @param name name of property which should be removed
   * @return this model instance
   * @throws NullPointerException when {@code name} is {@code null}
   */
  public Model removeProperty(String name) {
    if (properties().remove(requireNonNull(name, "Property name should not be null")) != null) {
      removePropertyFromXML(name);
    }
    return this;
  }

  /**
   * Removes module from the model.
   *
   * <p>If last module has been removed from model then modules element will be removed from xml as
   * well. If project doesn't have module with given name then nothing will be done.
   *
   * @param module module which should be removed
   * @return this model instance
   * @throws NullPointerException when {@code module} is {@code null}
   */
  public Model removeModule(String module) {
    if (modules().remove(requireNonNull(module, "Required not null module"))) {
      removeModuleFromXML(module);
    }
    return this;
  }

  /**
   * Sets build settings for project.
   *
   * <p>If {@code build} is {@code null} then it will be removed from model and xml as well.
   *
   * @param build new build
   * @return this model instance
   */
  public Model setBuild(Build build) {
    this.build = build;
    if (build == null) {
      root.removeChild("build");
    } else if (root.hasSingleChild("build")) {
      // replace build
      build.buildElement = root.getSingleChild("build").replaceWith(build.asXMLElement());
    } else {
      // add build
      root.appendChild(this.build.asXMLElement());
      build.buildElement = root.getSingleChild("build");
    }
    return this;
  }

  /**
   * Sets the location of the parent project, if one exists.
   *
   * <p>Values from the parent project will be the default for this project if they are left
   * unspecified. The location is given as a group ID, artifact ID and version. If {@code parent} is
   * {@code null} then it will be removed from model and xml as well.
   *
   * @param parent new project parent
   * @return this model instance
   */
  public Model setParent(Parent parent) {
    this.parent = parent;
    if (parent == null) {
      root.removeChild("parent");
    } else if (root.hasSingleChild("parent")) {
      // replace parent
      parent.parentElement = root.getSingleChild("parent").replaceWith(parent.asXMLElement());
    } else {
      // add parent
      root.insertChild(this.parent.asXMLElement(), after("modelVersion").or(inTheBegin()));
      parent.parentElement = root.getSingleChild("parent");
    }
    return this;
  }

  /**
   * Sets default dependency information for projects that inherit from this one. If new dependency
   * management is {@code null} removes old dependency management from model and from xml as well
   *
   * <p>The dependencies in this section are not immediately resolved. Instead, when a POM derived
   * from this one declares a dependency described by a matching groupId and artifactId, the version
   * and other values from this section are used for that dependency if they were not already
   * specified. If {@code parent} is {@code null} then it will be removed from model and xml as
   * well.
   *
   * @param dependencyManagement new project dependency management
   * @return this model instance
   */
  public Model setDependencyManagement(DependencyManagement dependencyManagement) {
    this.dependencyManagement = dependencyManagement;
    if (dependencyManagement == null) {
      root.removeChild("dependencyManagement");
    } else if (root.hasSingleChild("dependencyManagement")) {
      dependencyManagement.dmElement =
          root.getSingleChild("dependencyManagement")
              .replaceWith(dependencyManagement.asXMLElement());
    } else {
      root.insertChild(
          this.dependencyManagement.asXMLElement(),
          beforeAnyOf("dependencies", "build").or(inTheEnd()));
      dependencyManagement.dmElement = root.getSingleChild("dependencyManagement");
    }
    return this;
  }

  /**
   * Sets the modules (sometimes called sub projects) to build as a part of this project.
   *
   * <p>Each module listed is a relative path to the directory containing the module. If {@code
   * modules} is {@code null} or <i>empty</i> then modules will be removed from model as well as
   * from xml
   *
   * @param modules new project modules
   * @return this model instance
   */
  public Model setModules(Collection<String> modules) {
    if (modules == null || modules.isEmpty()) {
      removeModules();
    } else {
      setModules0(modules);
    }
    return this;
  }

  /**
   * Sets properties that can be used throughout the POM as a substitution, and are used as filters
   * in resources if enabled.
   *
   * <p>The format is {@code <name>value</name>}. if {@code properties} is {@code null} or
   * <i>empty</i> then properties will be removed from model as well as from xml.
   *
   * @param properties new project properties
   * @return this model instance
   */
  public Model setProperties(Map<String, String> properties) {
    if (properties == null || properties.isEmpty()) {
      removeProperties();
    } else {
      setProperties0(properties);
    }
    return this;
  }

  /**
   * Sets the identifier for this artifact that is unique within the group given by the group ID.
   *
   * <p>An artifact is something that is either produced or used by a project. Examples of artifacts
   * produced by Maven for a project include: JARs, source and binary distributions, and WARs. If
   * {@code artifactId} is {@code null} then it will be remove from model as well as from xml.
   *
   * @param artifactId new project artifact identifier
   * @return this model instance
   */
  public Model setArtifactId(String artifactId) {
    this.artifactId = artifactId;
    if (artifactId == null) {
      root.removeChild("artifactId");
    } else if (!root.hasSingleChild("artifactId")) {
      root.insertChild(
          createElement("artifactId", artifactId),
          afterAnyOf("groupId", "parent", "modelVersion").or(inTheBegin()));
    } else {
      tree.updateText("/project/artifactId", artifactId);
    }
    return this;
  }

  /**
   * Sets a detailed description of the project.
   *
   * <p>While this element can be specified as CDATA to enable the use of HTML tags within the
   * description, it is discouraged to allow plain text representation. If you need to modify the
   * index page of the generated web site, you are able to specify your own instead of adjusting
   * this text.
   *
   * <p>If {@code description} is {@code null} then it will be remove from model as well as from xml
   *
   * @param description new project description
   * @return this model instance
   */
  public Model setDescription(String description) {
    this.description = description;
    if (description == null) {
      root.removeChild("description");
    } else if (!root.hasSingleChild("description")) {
      root.insertChild(
          createElement("description", description),
          afterAnyOf("name", "version", "artifactId", "groupId", "parent", "modelVersion")
              .or(inTheBegin()));
    } else {
      tree.updateText("/project/artifactId", artifactId);
    }
    return this;
  }

  /**
   * Sets a universally unique identifier for a project.
   *
   * <p>It is normal to use a fully-qualified package name to distinguish it from other projects
   * with a similar name (eg. <i>org.apache.maven</i>). If {@code groupId} is {@code null} then it
   * will be removed from model as well as from xml.
   *
   * @param groupId new project group identifier
   * @return this model instance
   */
  public Model setGroupId(String groupId) {
    this.groupId = groupId;
    if (groupId == null) {
      root.removeChild("groupId");
    } else if (!root.hasSingleChild("groupId")) {
      root.insertChild(
          createElement("groupId", groupId), afterAnyOf("parent", "modelVersion").or(inTheBegin()));
    } else {
      tree.updateText("/project/groupId", groupId);
    }
    return this;
  }

  /**
   * Sets the current version of the artifact produced by this project.
   *
   * <p>If {@code version} is {@code null} then it will be remove from model as well as from xml.
   *
   * @param version new project version
   * @return this model instance
   */
  public Model setVersion(String version) {
    this.version = version;
    if (version == null) {
      root.removeChild("version");
    } else if (!root.hasSingleChild("version")) {
      root.insertChild(
          createElement("version", version),
          afterAnyOf("artifactId", "groupId", "parent", "modelVersion").or(inTheBegin()));
    } else {
      tree.updateText("/project/version", version);
    }
    return this;
  }

  /**
   * Declares to which version of project descriptor this POM conforms.
   *
   * <p>If {@code modelVersion} is {@code null} it will be removed from model and from xml as well.
   *
   * @param modelVersion new project model version
   * @return this model instance
   */
  public Model setModelVersion(String modelVersion) {
    this.modelVersion = modelVersion;
    if (modelVersion == null) {
      root.removeChild("modelVersion");
    } else if (!root.hasSingleChild("modelVersion")) {
      root.insertChild(createElement("modelVersion", modelVersion), inTheBegin());
    } else {
      tree.updateText("/project/modelVersion", modelVersion);
    }
    return this;
  }

  /**
   * Sets the full name of the project.
   *
   * <p>If {@code name} is {@code null} it will be removed from model as well as from xml.
   *
   * @param name new project name
   * @return this model instance
   */
  public Model setName(String name) {
    this.name = name;
    if (name == null) {
      root.removeChild("name");
    } else if (!root.hasSingleChild("name")) {
      root.insertChild(
          createElement("name", name),
          afterAnyOf("packaging", "version", "artifactId", "groupId", "parent", "modelVersion")
              .or(inTheBegin()));
    } else {
      tree.updateText("/project/name", name);
    }
    return this;
  }

  /**
   * Set the type of artifact this project produces
   *
   * <p>For example: <code>jar</code> <code>war</code> <code>ear</code> <code>pom</code>. Plugins
   * can create their own packaging, and therefore their own packaging types, so this list does not
   * contain all possible types. If {@code packaging} is {@code null} it will be removed from model
   * as well as from xml.
   *
   * @param packaging new project packaging
   * @return this model instance
   */
  public Model setPackaging(String packaging) {
    this.packaging = packaging;
    if (packaging == null) {
      root.removeChild("packaging");
    } else if (!root.hasSingleChild("packaging")) {
      root.insertChild(
          createElement("packaging", packaging),
          afterAnyOf("version", "artifactId", "groupId", "parent", "modelVersion")
              .or(inTheBegin()));
    } else {
      tree.updateText("/project/packaging", packaging);
    }
    return this;
  }

  /**
   * Sets pom file to model.
   *
   * <p>When pom file i set, model methods such as {@link #getProjectDirectory()}, {@link
   * #getPomFile()} or {@link #save()} may be used.
   *
   * @param pom pom file which model should be associated with
   */
  public Model setPomFile(File pom) {
    this.pom = pom;
    return this;
  }

  /**
   * Returns model identifier
   *
   * @return the model id as <code>groupId:artifactId:packaging:version</code>
   */
  public String getId() {
    return (version == null ? "[inherited]" : groupId)
        + ':'
        + artifactId
        + ':'
        + packaging
        + ':'
        + (version == null ? "[inherited]" : version);
  }

  /**
   * Returns model pom file if model has been created with {@link #readFrom(Path)} or {@link
   * #readFrom(File)} methods.
   *
   * <p>This method doesn't guarantee to return actual model pom file because it may be set directly
   * with {@link #setPomFile(File)}
   *
   * @return model pom file or {@code null} if it was not associated yet
   */
  public File getPomFile() {
    return pom;
  }

  /**
   * Returns pom file parent if model is associated with any pom file.
   *
   * @return pom file parent or {@code null} if model has not been associated with any pom file
   */
  public File getProjectDirectory() {
    return pom == null ? null : pom.getParentFile();
  }

  /**
   * Writes model to output stream. Doesn't close the stream
   *
   * @param os stream to write model in
   * @throws IOException when any i/o error occurs
   */
  public void writeTo(OutputStream os) throws IOException {
    tree.writeTo(os);
  }

  /**
   * Writes model to given file.
   *
   * @param file file to write model in
   * @throws IOException when any i/o error occurs
   */
  public void writeTo(File file) throws IOException {
    tree.writeTo(file);
  }

  /**
   * Updates associated with model pom file content
   *
   * @throws IllegalStateException when there is no pom file associated with model
   */
  public void save() throws IOException {
    if (pom == null) {
      throw new IllegalStateException("Model is not associated with any pom file");
    }
    writeTo(pom);
  }

  @Override
  public String toString() {
    return getId();
  }

  private Map<String, String> properties() {
    return properties == null ? properties = new HashMap<>() : properties;
  }

  private List<String> modules() {
    return modules == null ? modules = new ArrayList<>() : modules;
  }

  private List<Repository> repositories() {
    return repositories == null ? repositories = new ArrayList<>() : repositories;
  }

  private List<Profile> profiles() {
    return profiles == null ? profiles = new ArrayList<>() : profiles;
  }

  private List<Repository> pluginRepositories() {
    return pluginRepositories == null ? pluginRepositories = new ArrayList<>() : pluginRepositories;
  }

  private void addPropertyToXML(String key, String value) {
    if (properties().containsKey(key)) {
      root.getSingleChild("properties").getSingleChild(key).setText(value);
    } else if (properties.isEmpty()) {
      root.insertChild(
          createElement("properties", createElement(key, value)),
          beforeAnyOf("dependencyManagement", "dependencies", "build").or(inTheEnd()));
    } else {
      root.getSingleChild("properties").appendChild(createElement(key, value));
    }
  }

  private void removeProperties() {
    root.removeChild("properties");
    this.properties = null;
  }

  private void setProperties0(Map<String, String> properties) {
    this.properties = new HashMap<>(properties);
    // if properties element exists we should replace it children
    // with new set of properties, otherwise create element for it
    if (root.hasSingleChild("properties")) {
      final Element propertiesElement = root.getSingleChild("properties");
      for (Element property : propertiesElement.getChildren()) {
        property.remove();
      }
      for (Map.Entry<String, String> property : properties.entrySet()) {
        propertiesElement.appendChild(createElement(property.getKey(), property.getValue()));
      }
    } else {
      final NewElement newProperties = createElement("properties");
      for (Map.Entry<String, String> property : properties.entrySet()) {
        newProperties.appendChild(createElement(property.getKey(), property.getValue()));
      }
      // insert new properties to xml
      root.insertChild(
          newProperties,
          beforeAnyOf("dependencyManagement", "dependencies", "build").or(inTheEnd()));
    }
  }

  private void removeModules() {
    root.removeChild("modules");
    this.modules = null;
  }

  private void setModules0(Collection<String> modules) {
    this.modules = new ArrayList<>(modules);
    // if modules element exists we should replace it children
    // with new set of modules, otherwise create element for it
    if (root.hasSingleChild("modules")) {
      final Element modulesElement = root.getSingleChild("modules");
      // remove all modules
      for (Element module : modulesElement.getChildren()) {
        module.remove();
      }
      // append each new module to "modules" element
      for (String module : modules) {
        modulesElement.appendChild(createElement("module", module));
      }
    } else {
      final NewElement newModules = createElement("modules");
      for (String module : modules) {
        newModules.appendChild(createElement("module", module));
      }
      root.insertChild(
          newModules,
          beforeAnyOf("properties", "dependencyManagement", "dependencies", "build")
              .or(inTheEnd()));
    }
  }

  private void removeModuleFromXML(String module) {
    if (modules.isEmpty()) {
      root.removeChild("modules");
    } else {
      for (Element element : root.getSingleChild("modules").getChildren()) {
        if (module.equals(element.getText())) {
          element.remove();
        }
      }
    }
  }

  private void removePropertyFromXML(String key) {
    if (properties.isEmpty()) {
      root.removeChild("properties");
    } else {
      root.getSingleChild("properties").removeChild(key);
    }
  }

  private static Model fetchModel(XMLTree tree) {
    final Model model = new Model(tree);
    final Element root = tree.getRoot();
    model.modelVersion = root.getChildText("modelVersion");
    model.groupId = root.getChildText("groupId");
    model.artifactId = root.getChildText("artifactId");
    model.version = root.getChildText("version");
    model.name = root.getChildText("name");
    model.description = root.getChildText("description");
    model.packaging = root.getChildText("packaging");
    if (root.hasSingleChild("parent")) {
      model.parent = new Parent(root.getSingleChild("parent"));
    }
    if (root.hasSingleChild("dependencyManagement")) {
      final Element dm = tree.getSingleElement("/project/dependencyManagement");
      final List<Dependency> dependencies =
          tree.getElements(
              "/project/dependencyManagement/dependencies/dependency", TO_DEPENDENCY_MAPPER);
      model.dependencyManagement = new DependencyManagement(dm, dependencies);
    }
    if (root.hasSingleChild("build")) {
      model.build = new Build(root.getSingleChild("build"));
    }
    if (root.hasSingleChild("dependencies")) {
      final List<Dependency> dependencies =
          tree.getElements("/project/dependencies/dependency", TO_DEPENDENCY_MAPPER);
      model.dependencies = new Dependencies(root, dependencies);
    }
    if (root.hasSingleChild("modules")) {
      model.modules = tree.getElements("/project/modules/module", TO_MODULE_MAPPER);
    }
    if (root.hasSingleChild("profiles")) {
      model.profiles = tree.getElements("/project/profiles/profile", TO_PROFILE_MAPPER);
    }
    if (root.hasSingleChild("repositories")) {
      model.repositories =
          tree.getElements("/project/repositories/repository", TO_REPOSITORY_MAPPER);
    }
    if (root.hasSingleChild("pluginRepositories")) {
      model.pluginRepositories =
          tree.getElements("/project/pluginRepositories/repository", TO_REPOSITORY_MAPPER);
    }
    if (root.hasSingleChild("properties")) {
      model.properties = fetchProperties(root.getSingleChild("properties"));
    }
    return model;
  }

  private static Map<String, String> fetchProperties(Element propertiesElement) {
    final Map<String, String> properties = new HashMap<>();
    for (Element property : propertiesElement.getChildren()) {
      properties.put(property.getName(), property.getText());
    }
    return properties;
  }

  private static class ToDependencyMapper implements ElementMapper<Dependency> {

    @Override
    public Dependency map(Element element) {
      return new Dependency(element);
    }
  }

  private static class ToModuleMapper implements ElementMapper<String> {

    @Override
    public String map(Element element) {
      return element.getText();
    }
  }

  private static class ToProfileMapper implements ElementMapper<Profile> {
    @Override
    public Profile map(Element element) {
      return new Profile(element);
    }
  }

  private static class ToRepositoryMapper implements ElementMapper<Repository> {

    @Override
    public Repository map(Element element) {
      return new Repository(element);
    }
  }
}
