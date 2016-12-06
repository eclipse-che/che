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
package org.eclipse.che.api.machine.server.recipe;

import org.eclipse.che.api.core.model.machine.Recipe;
import org.eclipse.che.api.machine.server.jpa.RecipeEntityListener;
import org.eclipse.che.api.machine.shared.ManagedRecipe;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Implementation of {@link ManagedRecipe}
 *
 * @author Eugene Voevodin
 * @author Anton Korneta
 */
@Entity(name = "Recipe")
@Table(name = "recipe")
@EntityListeners(RecipeEntityListener.class)
public class RecipeImpl implements ManagedRecipe {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "name")
    private String name;

    @Column(name = "creator")
    private String creator;

    @Column(name = "type")
    private String type;

    @Column(name = "script", columnDefinition = "TEXT")
    private String script;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @ElementCollection
    @Column(name = "tag")
    @CollectionTable(name = "recipe_tags", joinColumns = @JoinColumn(name = "recipe_id"))
    private List<String> tags;

    public RecipeImpl() {
    }

    public RecipeImpl(Recipe recipe) {
        this.type = recipe.getType();
        this.script = recipe.getScript();
    }

    public RecipeImpl(ManagedRecipe recipe) {
        this(recipe.getId(),
             recipe.getName(),
             recipe.getCreator(),
             recipe.getType(),
             recipe.getScript(),
             recipe.getTags(),
             recipe.getDescription());
    }

    public RecipeImpl(RecipeImpl recipe) {
        this(recipe.getId(),
             recipe.getName(),
             recipe.getCreator(),
             recipe.getType(),
             recipe.getScript(),
             recipe.getTags(),
             recipe.getDescription());
    }

    public RecipeImpl(String id,
                      String name,
                      String creator,
                      String type,
                      String script,
                      List<String> tags,
                      String description) {
        this.id = id;
        this.name = name;
        this.creator = creator;
        this.type = type;
        this.script = script;
        this.tags = tags;
        this.description = description;
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public RecipeImpl withId(String id) {
        this.id = id;
        return this;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RecipeImpl withName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public RecipeImpl withType(String type) {
        this.type = type;
        return this;
    }

    @Override
    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public RecipeImpl withScript(String script) {
        this.script = script;
        return this;
    }

    @Override
    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public RecipeImpl withCreator(String creator) {
        this.creator = creator;
        return this;
    }

    @Override
    public List<String> getTags() {
        if (tags == null) {
            tags = new ArrayList<>();
        }
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public RecipeImpl withTags(List<String> tags) {
        this.tags = tags;
        return this;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public RecipeImpl withDescription(String description) {
        this.description = description;
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof RecipeImpl)) {
            return false;
        }
        final RecipeImpl other = (RecipeImpl)obj;
        return Objects.equals(id, other.id) &&
               Objects.equals(name, other.name) &&
               Objects.equals(creator, other.creator) &&
               Objects.equals(type, other.type) &&
               Objects.equals(script, other.script) &&
               Objects.equals(description, other.description) &&
               getTags().equals(other.getTags());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(id);
        hash = 31 * hash + Objects.hashCode(name);
        hash = 31 * hash + Objects.hashCode(creator);
        hash = 31 * hash + Objects.hashCode(type);
        hash = 31 * hash + Objects.hashCode(script);
        hash = 31 * hash + Objects.hashCode(description);
        hash = 31 * hash + getTags().hashCode();
        return hash;
    }

    @Override
    public String toString() {
        return "RecipeImpl{" +
               "id='" + id + '\'' +
               ", name='" + name + '\'' +
               ", creator='" + creator + '\'' +
               ", type='" + type + '\'' +
               ", script='" + script + '\'' +
               ", description='" + description + '\'' +
               ", tags=" + tags +
               '}';
    }
}
