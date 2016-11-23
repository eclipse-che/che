/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.workspace.server.model.impl;

import org.eclipse.che.api.core.model.workspace.EnvironmentRecipe;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.util.Objects;

/**
 * @author Alexander Garagatyi
 */
@Embeddable
public class EnvironmentRecipeImpl implements EnvironmentRecipe {

    @Column(name = "type")
    private String type;

    @Column(name = "contenttype")
    private String contentType;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "location", columnDefinition = "TEXT")
    private String location;

    public EnvironmentRecipeImpl() {}

    public EnvironmentRecipeImpl(String type,
                                 String contentType,
                                 String content,
                                 String location) {
        this.type = type;
        this.contentType = contentType;
        this.content = content;
        this.location = location;
    }

    public EnvironmentRecipeImpl(EnvironmentRecipe recipe) {
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
        if (!(o instanceof EnvironmentRecipeImpl)) return false;
        EnvironmentRecipeImpl that = (EnvironmentRecipeImpl)o;
        return Objects.equals(type, that.type) &&
               Objects.equals(contentType, that.contentType) &&
               Objects.equals(content, that.content) &&
               Objects.equals(location, that.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, contentType, content, location);
    }

    @Override
    public String toString() {
        return "EnvironmentRecipeImpl{" +
               "type='" + type + '\'' +
               ", contentType='" + contentType + '\'' +
               ", content='" + content + '\'' +
               ", location='" + location + '\'' +
               '}';
    }
}
