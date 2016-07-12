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
import org.eclipse.che.api.machine.server.model.impl.AclEntryImpl;
import org.eclipse.che.api.machine.shared.ManagedRecipe;

import javax.persistence.Basic;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.PostLoad;
import javax.persistence.PrePersist;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import static javax.persistence.CascadeType.ALL;

/**
 * Implementation of {@link ManagedRecipe}
 *
 * @author Eugene Voevodin
 * @author Anton Korneta
 */
@Entity(name = "Recipe")
@NamedQueries(
        {
                @NamedQuery(
                        name = "Recipe.search",
                        query = "SELECT DISTINCT rec FROM Recipe rec " +
                                "WHERE (EXISTS (SELECT publicAction FROM rec.publicActions publicAction WHERE publicAction = 'search') " +
                                "   OR (EXISTS (SELECT userAction FROM rec.storedAcl acl, acl.actions userAction " +
                                "WHERE userAction = 'search' AND acl.user =:user))) " +
                                "  AND (:recipeType IS NULL OR rec.type = :recipeType) " +
                                "  AND (:requiredCount = 0 OR :requiredCount = (SELECT COUNT(tag) " +
                                "                                               FROM Recipe recipe JOIN recipe.tags tag " +
                                "                                               WHERE tag IN :tags AND rec.id = recipe.id))"
                )
        }
)
public class RecipeImpl implements ManagedRecipe {

    @Id
    private String id;

    @Basic
    private String name;

    @Basic
    private String creator;

    @Basic
    private String type;

    @Basic
    private String script;

    @Basic
    private String description;

    // TODO: consider how to avoid this field
    @Transient
    private List<AclEntryImpl> acl;

    // list of AclEntry which contain user without wildcard
    @OneToMany(cascade = ALL, orphanRemoval = true)
    @JoinTable(name = "RECIPE_USER_ACL",
               uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "recipe_id"}),
               inverseJoinColumns = {@JoinColumn(name = "user_id", referencedColumnName = "user"),
                                     @JoinColumn(name = "acl_id", referencedColumnName = "id")},
               joinColumns = @JoinColumn(name = "recipe_id", referencedColumnName = "id"))
    private List<AclEntryImpl> storedAcl;

    @ElementCollection
    private List<String> tags;

    // describes the list of actions allowed to any user
    @ElementCollection
    private List<String> publicActions;

    @PrePersist
    private void separateActions() {
        // work around for storing public actions in separate collection
        // so as not to violate the integrity constraint in case of userId = '*'
        storedAcl = new ArrayList<>(getAcl());
        if (publicActions == null) {
            publicActions = new ArrayList<>();
        }
        final Iterator<AclEntryImpl> aclIterator = storedAcl.iterator();
        while (aclIterator.hasNext()) {
            final AclEntryImpl aclEntry = aclIterator.next();
            if ("*".equals(aclEntry.getUser())) {
                publicActions.addAll(aclEntry.getActions());
                aclIterator.remove();
            }
        }
    }

    @PostLoad
    private void collectActions() {
        acl = new ArrayList<>(storedAcl);
        if (publicActions != null && !publicActions.isEmpty()) {
            acl.add(new AclEntryImpl("*", publicActions));
        }
    }

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
             recipe.getDescription(),
             null);
    }

    public RecipeImpl(RecipeImpl recipe) {
        this(recipe.getId(),
             recipe.getName(),
             recipe.getCreator(),
             recipe.getType(),
             recipe.getScript(),
             recipe.getTags(),
             recipe.getDescription(),
             recipe.getAcl());
    }

    public RecipeImpl(String id,
                      String name,
                      String creator,
                      String type,
                      String script,
                      List<String> tags,
                      String description,
                      List<AclEntryImpl> acl) {
        this.id = id;
        this.name = name;
        this.creator = creator;
        this.type = type;
        this.script = script;
        this.tags = tags;
        this.description = description;
        this.acl = acl;
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

    public List<AclEntryImpl> getAcl() {
        if (acl == null) {
            return new ArrayList<>();
        }
        return acl;
    }

    public void setAcl(List<AclEntryImpl> acl) {
        this.acl = acl;
    }

    public RecipeImpl withAcl(List<AclEntryImpl> acl) {
        this.acl = acl;
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
               getTags().equals(other.getTags()) &&
               Objects.equals(acl, other.acl);
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
        hash = 31 * hash + Objects.hashCode(acl);
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
               ", tags=" + tags +
               ", description='" + description + '\'' +
               ", acl=" + acl +
               '}';
    }
}
