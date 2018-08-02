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
package org.eclipse.che.api.workspace.server.model.impl;

import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import org.eclipse.che.api.core.model.workspace.config.Recipe;

/** @author Alexander Garagatyi */
@Embeddable
public class RecipeImpl implements Recipe {

  @Column(name = "type")
  private String type;

  @Column(name = "contenttype")
  private String contentType;

  @Column(name = "content", columnDefinition = "TEXT")
  private String content;

  @Column(name = "location", columnDefinition = "TEXT")
  private String location;

  public RecipeImpl() {}

  public RecipeImpl(String type, String contentType, String content, String location) {
    this.type = type;
    this.contentType = contentType;
    this.content = content;
    this.location = location;
  }

  public RecipeImpl(Recipe recipe) {
    this.type = recipe.getType();
    this.contentType = recipe.getContentType();
    this.content = recipe.getContent();
    this.location = recipe.getLocation();
  }

  @Override
  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  @Override
  public String getContentType() {
    return contentType;
  }

  public void setContentType(String contentType) {
    this.contentType = contentType;
  }

  @Override
  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  @Override
  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof RecipeImpl)) return false;
    RecipeImpl that = (RecipeImpl) o;
    return Objects.equals(type, that.type)
        && Objects.equals(contentType, that.contentType)
        && Objects.equals(content, that.content)
        && Objects.equals(location, that.location);
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, contentType, content, location);
  }

  @Override
  public String toString() {
    return "RecipeImpl{"
        + "type='"
        + type
        + '\''
        + ", contentType='"
        + contentType
        + '\''
        + ", content='"
        + content
        + '\''
        + ", location='"
        + location
        + '\''
        + '}';
  }
}
