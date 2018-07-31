/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.project.server.type;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.model.project.type.Attribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registry for Project Type definitions on server
 *
 * <p>All the Project Types definitions should be registered here
 *
 * @author gazarenkov
 */
@Singleton
public class ProjectTypeRegistry {

  private static final Logger LOG = LoggerFactory.getLogger(ProjectTypeRegistry.class);

  public static final ProjectTypeDef BASE_TYPE = new BaseProjectType();
  public static final ChildToParentComparator CHILD_TO_PARENT_COMPARATOR =
      new ChildToParentComparator();
  private static final Pattern NAME_PATTERN = Pattern.compile("[^a-zA-Z0-9-_.]");

  private final Map<String, ProjectTypeDef> projectTypes;
  private final Map<String, ProjectTypeDef> validatedData;

  /**
   * Initialises Set of Project Type definitions
   *
   * @param types
   */
  @Inject
  public ProjectTypeRegistry(Set<ProjectTypeDef> types) {
    projectTypes = new HashMap<>();
    validatedData = new HashMap<>();

    validate(types);

    for (ProjectTypeDef type : validatedData.values()) {
      try {
        init(type);
      } catch (ProjectTypeConstraintException e) {
        LOG.error(e.getMessage());
      }
    }
  }

  /**
   * Registers single projectType May be deprecated in future
   *
   * @param projectType
   * @throws ProjectTypeConstraintException
   */
  public void registerProjectType(ProjectTypeDef projectType)
      throws ProjectTypeConstraintException {
    if (isNameValid(projectType) && isParentValid(projectType, validatedData)) {
      validatedData.put(projectType.getId(), projectType);
      init(projectType);
    }
  }

  /**
   * @param id
   * @return project type by id
   */
  public ProjectTypeDef getProjectType(String id) throws NotFoundException {
    final ProjectTypeDef pt = projectTypes.get(id);
    if (pt == null) {
      throw new NotFoundException("Project Type not found: " + id);
    }
    return pt;
  }

  /** @return all project types */
  public Collection<ProjectTypeDef> getProjectTypes() {
    return projectTypes.values();
  }

  /**
   * @param comparator
   * @return all project types sorted with specified comparator
   * @see ProjectTypeRegistry.ChildToParentComparator
   */
  public List<ProjectTypeDef> getProjectTypes(Comparator<ProjectTypeDef> comparator) {
    List<ProjectTypeDef> list = projectTypes.values().stream().collect(Collectors.toList());
    Collections.sort(list, comparator);
    return list;
  }

  /** project type comparator which sorts collection of project types in child-to-parent order */
  public static class ChildToParentComparator implements Comparator<ProjectTypeDef> {
    @Override
    public int compare(ProjectTypeDef o1, ProjectTypeDef o2) {
      if (o1.isTypeOf(o2.getId())) {
        return -1;
      }
      if (o2.isTypeOf(o1.getId())) {
        return 1;
      }
      return 0;
    }
  }

  /**
   * Validates incoming set of Project Type definitions and forms preliminary collection of
   * validated data to be initialized
   *
   * @param types
   */
  protected final void validate(Collection<? extends ProjectTypeDef> types) {
    Map<String, ProjectTypeDef> pass1 = new HashMap<>();

    if (!types.contains(BASE_TYPE)) {
      pass1.put(BASE_TYPE.getId(), BASE_TYPE);
    }

    types.stream().filter(this::isNameValid).forEach(type -> pass1.put(type.getId(), type));

    // look for parents
    pass1
        .values()
        .stream()
        .filter(type -> isParentValid(type, pass1))
        .forEach(type -> validatedData.put(type.getId(), type));
  }

  /**
   * Checks if incoming Project Type definition has valid ID (Pattern.compile("[^a-zA-Z0-9-_.]") and
   * display name (should not be null or empty)
   *
   * @param type
   * @return true if valid
   */
  private boolean isNameValid(ProjectTypeDef type) {

    boolean valid = true;

    if (type.getId() == null
        || type.getId().isEmpty()
        || NAME_PATTERN.matcher(type.getId()).find()) {
      LOG.error(
          "Could not register Project Type ID is null or invalid (only Alphanumeric, dash, point and underscore allowed): "
              + type.getClass().getName());
      valid = false;
    }

    if (type.getDisplayName() == null || type.getDisplayName().isEmpty()) {
      LOG.error("Could not register Project Type with null or empty display name: " + type.getId());
      valid = false;
    }

    for (Attribute attr : type.getAttributes()) {
      // ID spelling (no spaces, only alphanumeric)
      if (NAME_PATTERN.matcher(attr.getName()).find()) {
        LOG.error(
            "Could not register Project Type with invalid attribute Name (only Alphanumeric, dash and underscore allowed): "
                + attr.getClass().getName()
                + " ID: '"
                + attr.getId()
                + "'");
        valid = false;
      }
    }

    return valid;
  }

  /**
   * Validates if Project Tye definition has parent names which are already registered
   *
   * @param type
   * @param pass1
   * @return
   */
  private boolean isParentValid(ProjectTypeDef type, Map<String, ProjectTypeDef> pass1) {

    boolean contains = true;

    for (String parent : type.getParents()) {
      if (!pass1.keySet().contains(parent)) {
        LOG.error(
            "Could not register Project Type: "
                + type.getId()
                + " : Unregistered parent Type: "
                + parent);
        contains = false;
      }
    }

    // add Base Type as a parent if not pointed
    if (type.getParents() != null
        && type.getParents().isEmpty()
        && !type.getId().equals(BASE_TYPE.getId())) {
      type.addParent(BASE_TYPE.getId());
    }

    return contains;
  }

  /**
   * validates and initializes concrete project type
   *
   * @param type
   * @throws ProjectTypeConstraintException
   */
  protected final void init(ProjectTypeDef type) throws ProjectTypeConstraintException {
    initRecursively(type, type.getId());

    if (!type.factoriesToOverride.isEmpty()) {
      overrideFactories(type);
    }

    this.projectTypes.put(type.getId(), type);

    LOG.debug("Project Type registered: " + type.getId());
  }

  /**
   * initializes all the attributes defined in myType and its ancestors recursively
   *
   * @param myType
   * @param typeId temporary type for recursive (started with initial type)
   * @throws ProjectTypeConstraintException
   */
  private final void initRecursively(ProjectTypeDef myType, String typeId)
      throws ProjectTypeConstraintException {
    ProjectTypeDef type = validatedData.get(typeId);

    for (String superTypeId : type.getParents()) {
      myType.addAncestor(superTypeId);

      ProjectTypeDef supertype = validatedData.get(superTypeId);
      for (Attribute supertypeAttr : supertype.getAttributes()) {
        // check attribute names
        for (Attribute attr : myType.getAttributes()) {
          if (supertypeAttr.getName().equals(attr.getName())) {

            ProjectTypeDef attrOriginProjectType = validatedData.get(attr.getProjectType());
            // myType can't add attribute with the same name as one of its ancestors already has
            if (attrOriginProjectType.getId().equals(myType.id)
                // check whether the attribute isn't inherited from an ancestor PT
                || !attrOriginProjectType.isTypeOf(supertypeAttr.getProjectType())) {
              throw new ProjectTypeConstraintException(
                  "Attribute name conflict. Project type "
                      + myType.getId()
                      + " could not be registered as attribute declaration "
                      + attr.getName()
                      + " is duplicated in its ancestor(s).");
            }
          }
        }
        myType.addAttributeDefinition(supertypeAttr);
      }
      initRecursively(myType, superTypeId);
    }
  }

  private final void overrideFactories(ProjectTypeDef myType)
      throws ProjectTypeConstraintException {
    for (Map.Entry<String, ValueProviderFactory> entry : myType.factoriesToOverride.entrySet()) {
      Attribute old = myType.getAttribute(entry.getKey());
      if (old == null || !old.isVariable()) {
        throw new ProjectTypeConstraintException(
            "Can not override Value Provider Factory. Variable not defined: "
                + myType.getId()
                + ":"
                + entry.getKey());
      }

      myType.attributes.put(
          old.getName(),
          new Variable(
              myType.getId(),
              old.getName(),
              old.getDescription(),
              old.isRequired(),
              entry.getValue()));
    }
  }
}
