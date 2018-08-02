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
package org.eclipse.che.api.project.server.type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.core.model.project.type.Attribute;
import org.eclipse.che.api.core.model.project.type.ProjectType;

/** @author gazarenkov */
public abstract class ProjectTypeDef implements ProjectType {

  protected final Map<String, Attribute> attributes;
  protected final List<String> parents;
  protected final List<String> ancestors;
  protected final Map<String, ValueProviderFactory> factoriesToOverride;

  protected final String id;
  protected final String displayName;
  protected final boolean primaryable;
  protected final boolean mixable;
  protected final boolean persisted;

  protected ProjectTypeDef(
      String id, String displayName, boolean primaryable, boolean mixable, boolean persisted) {
    ancestors = new ArrayList<>();
    attributes = new HashMap<>();
    parents = new ArrayList<>();
    factoriesToOverride = new HashMap<>();

    this.id = id;
    this.displayName = displayName;
    this.primaryable = primaryable;
    this.mixable = mixable;
    this.persisted = persisted;
  }

  /**
   * @param id
   * @param displayName
   * @param primaryable whether the ProjectTypeDef can be used as Primary
   * @param mixable whether the projectType can be used as Mixin
   */
  protected ProjectTypeDef(String id, String displayName, boolean primaryable, boolean mixable) {
    this(id, displayName, primaryable, mixable, true);
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public boolean isPersisted() {
    return persisted;
  }

  @Override
  public String getDisplayName() {
    return displayName;
  }

  @Override
  public List<Attribute> getAttributes() {
    return new ArrayList<>(attributes.values());
  }

  @Override
  public List<String> getParents() {
    return parents;
  }

  @Override
  public boolean isMixable() {
    return mixable;
  }

  @Override
  public boolean isPrimaryable() {
    return primaryable;
  }

  /** @return ids of ancestors */
  public List<String> getAncestors() {
    return ancestors;
  }

  /**
   * whether this type is subtype of typeId
   *
   * @param typeId
   * @return true if it is a subtype
   */
  public boolean isTypeOf(String typeId) {
    return this.id.equals(typeId) || ancestors.contains(typeId);
  }

  /**
   * @param name
   * @return attribute by name
   */
  public Attribute getAttribute(String name) {
    return attributes.get(name);
  }

  protected void addConstantDefinition(String name, String description, AttributeValue value) {
    attributes.put(name, new Constant(id, name, description, value));
  }

  protected void addConstantDefinition(String name, String description, String value) {
    attributes.put(name, new Constant(id, name, description, value));
  }

  protected void addVariableDefinition(String name, String description, boolean required) {
    attributes.put(name, new Variable(id, name, description, required));
  }

  protected void addVariableDefinition(
      String name, String description, boolean required, AttributeValue value) {
    attributes.put(name, new Variable(id, name, description, required, value));
  }

  protected void addVariableDefinition(
      String name, String description, boolean required, ValueProviderFactory factory) {
    attributes.put(name, new Variable(id, name, description, required, factory));
  }

  /**
   * Sets new value for ValueProviderFactory Useful if child project type needs to redefine
   * ValueProviderFactory of parent PT attribute todo check if attribute is provided
   *
   * @param name attribute name
   * @param factory to set
   * @return true if attribute found and factory set
   */
  protected void setValueProviderFactory(String name, ValueProviderFactory factory) {
    this.factoriesToOverride.put(name, factory);
  }

  protected void addAttributeDefinition(Attribute attr) {
    attributes.put(attr.getName(), attr);
  }

  protected void addParent(String parentId) {
    for (String pid : parents) {
      if (pid.equals(parentId)) return;
    }
    parents.add(parentId);
  }

  void addAncestor(String ancestor) {
    this.ancestors.add(ancestor);
  }
}
