/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.project.server.type;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.model.project.type.Attribute;

/** @author gazarenkov */
public class ProjectTypes {

  private final ProjectTypeRegistry projectTypeRegistry;
  private final ProjectTypeResolver projectTypeResolver;
  private final Map<String, ProjectTypeDef> mixins;
  private final Map<String, ProjectTypeDef> all;
  private final Map<String, Attribute> attributeDefs;
  private ProjectTypeDef primary;

  @AssistedInject
  public ProjectTypes(
      @Assisted("type") String type,
      @Assisted("mixinTypes") List<String> mixinTypes,
      ProjectTypeRegistry projectTypeRegistry,
      ProjectTypeResolver projectTypeResolver) {
    this.projectTypeResolver = projectTypeResolver;
    mixins = new HashMap<>();
    all = new HashMap<>();
    attributeDefs = new HashMap<>();

    this.projectTypeRegistry = projectTypeRegistry;

    ProjectTypeDef tmpPrimary;
    if (type == null) {
      tmpPrimary = ProjectTypeRegistry.BASE_TYPE;
    } else {
      try {
        tmpPrimary = projectTypeRegistry.getProjectType(type);
      } catch (NotFoundException e) {
        tmpPrimary = ProjectTypeRegistry.BASE_TYPE;
      }

      if (!tmpPrimary.isPrimaryable()) {
        tmpPrimary = ProjectTypeRegistry.BASE_TYPE;
      }
    }

    this.primary = tmpPrimary;
    all.put(primary.getId(), primary);

    List<String> mixinsFromConfig = mixinTypes;

    if (mixinsFromConfig == null) {
      mixinsFromConfig = new ArrayList<>();
    }

    for (Attribute attr : primary.getAttributes()) {
      attributeDefs.put(attr.getName(), attr);
    }

    for (String mixinFromConfig : mixinsFromConfig) {
      if (mixinFromConfig.equals(primary.getId())) {
        continue;
      }

      final ProjectTypeDef mixin;
      try {
        mixin = projectTypeRegistry.getProjectType(mixinFromConfig);
      } catch (NotFoundException e) {
        continue;
      }

      if (!mixin.isMixable()) {
        continue;
      }

      if (!mixin.isPersisted()) {
        continue;
      }

      // detect duplicated attributes
      for (Attribute attr : mixin.getAttributes()) {
        final String attrName = attr.getName();
        final Attribute attribute = attributeDefs.get(attrName);
        if (attribute != null && !attribute.getProjectType().equals(attr.getProjectType())) {
          continue;
        }

        attributeDefs.put(attrName, attr);
      }

      // Silently remove repeated items from mixins if any
      mixins.put(mixinFromConfig, mixin);
      all.put(mixinFromConfig, mixin);
    }
  }

  public Map<String, Attribute> getAttributeDefs() {
    return attributeDefs;
  }

  public ProjectTypeDef getPrimary() {
    return primary;
  }

  public Map<String, ProjectTypeDef> getMixins() {
    return mixins;
  }

  public Map<String, ProjectTypeDef> getAll() {
    return all;
  }

  /**
   * Reset project types and atrributes after initialization in case when some attributes are not
   * valid (for instance required attributes are not initialized)
   *
   * @param attributesToDel - invalid attributes
   */
  void reset(Set<Attribute> attributesToDel) {

    Set<String> ptsToDel = new HashSet<>();
    for (Attribute attr : attributesToDel) {
      ptsToDel.add(attr.getProjectType());
    }

    Set<String> attrNamesToDel = new HashSet<>();
    for (String pt : ptsToDel) {
      ProjectTypeDef typeDef = all.get(pt);
      for (Attribute attrDef : typeDef.getAttributes()) {
        attrNamesToDel.add(attrDef.getName());
      }
    }

    // remove project types
    for (String typeId : ptsToDel) {
      this.all.remove(typeId);
      if (this.primary.getId().equals(typeId)) {
        this.primary = ProjectTypeRegistry.BASE_TYPE;
        this.all.put(ProjectTypeRegistry.BASE_TYPE.getId(), ProjectTypeRegistry.BASE_TYPE);
      } else {
        mixins.remove(typeId);
      }
    }

    // remove attributes
    for (String attr : attrNamesToDel) {
      this.attributeDefs.remove(attr);
    }
  }

  public void addTransient(String projectFolder) {
    for (ProjectTypeDef pt : projectTypeRegistry.getProjectTypes()) {
      // NOTE: Only mixable types allowed
      if (pt.isMixable()
          && !pt.isPersisted()
          && projectTypeResolver.resolve(pt, projectFolder).matched()) {
        all.put(pt.getId(), pt);
        mixins.put(pt.getId(), pt);
        for (Attribute attr : pt.getAttributes()) {
          final String attrName = attr.getName();
          final Attribute attribute = attributeDefs.get(attr.getName());
          // If attr from mixin is going to be added but we already have some attribute with the
          // same name,
          // check whether it's the same attribute that comes from the common parent PT, e.g. from
          // Base PT.
          if (attribute != null && !attribute.getProjectType().equals(attr.getProjectType())) {
            continue;
          }

          attributeDefs.put(attrName, attr);
        }
      }
    }
  }
}
