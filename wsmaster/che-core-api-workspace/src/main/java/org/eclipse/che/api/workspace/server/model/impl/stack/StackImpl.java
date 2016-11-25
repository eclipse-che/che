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
package org.eclipse.che.api.workspace.server.model.impl.stack;

import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;
import org.eclipse.che.api.workspace.server.jpa.StackEntityListener;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.stack.image.StackIcon;
import org.eclipse.che.api.workspace.shared.stack.Stack;
import org.eclipse.che.api.workspace.shared.stack.StackComponent;
import org.eclipse.che.api.workspace.shared.stack.StackSource;
import org.eclipse.che.commons.lang.NameGenerator;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.toList;

/**
 * Data object for {@link Stack}.
 *
 * @author Alexander Andrienko
 * @author Yevhenii Voevodin
 */
@Entity(name = "Stack")
@NamedQueries(
        {
                @NamedQuery(name = "Stack.getByTags",
                            query = "SELECT stack " +
                                    "FROM Stack stack, stack.tags tag " +
                                    "WHERE tag IN :tags " +
                                    "GROUP BY stack.id " +
                                    "HAVING COUNT(tag) = :tagsSize"),
                @NamedQuery(name = "Stack.getAll",
                            query = "SELECT stack FROM Stack stack")
        }

)
@EntityListeners(StackEntityListener.class)
@Table(name = "stack")
public class StackImpl implements Stack {

    public static StackBuilder builder() {
        return new StackBuilder();
    }

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "name", unique = true, nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "scope")
    private String scope;

    @Column(name = "creator")
    private String creator;

    @ElementCollection
    @Column(name = "tag")
    @CollectionTable(name = "stack_tags", joinColumns = @JoinColumn(name = "stack_id"))
    private List<String> tags;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "workspaceconfig_id")
    private WorkspaceConfigImpl workspaceConfig;

    @Embedded
    private StackSourceImpl source;

    @ElementCollection
    @CollectionTable(name = "stack_components", joinColumns = @JoinColumn(name = "stack_id"))
    private List<StackComponentImpl> components;

    @Embedded
    private StackIcon stackIcon;

    public StackImpl() {}

    public StackImpl(StackImpl stack) {
        this(stack.getId(),
             stack.getName(),
             stack.getDescription(),
             stack.getScope(),
             stack.getCreator(),
             stack.getTags(),
             stack.getWorkspaceConfig(),
             stack.getSource(),
             stack.getComponents(),
             stack.getStackIcon());
    }

    public StackImpl(Stack stack) {
        this(stack.getId(),
             stack.getName(),
             stack.getDescription(),
             stack.getScope(),
             stack.getCreator(),
             stack.getTags(),
             stack.getWorkspaceConfig(),
             stack.getSource(),
             stack.getComponents(),
             null);
    }

    public StackImpl(String id,
                     String name,
                     String description,
                     String scope,
                     String creator,
                     List<String> tags,
                     WorkspaceConfig workspaceConfig,
                     StackSource source,
                     List<? extends StackComponent> components,
                     StackIcon stackIcon) {
        this.id = id;
        this.creator = creator;
        this.name = name;
        this.scope = scope;
        this.description = description;
        if (stackIcon != null) {
            this.stackIcon = new StackIcon(stackIcon);
        }
        if (tags != null) {
            this.tags = new ArrayList<>(tags);
        }
        if (workspaceConfig != null) {
            this.workspaceConfig = new WorkspaceConfigImpl(workspaceConfig);
        }
        if (source != null) {
            this.source = new StackSourceImpl(source);
        }
        if (components != null) {
            this.components = components.stream()
                                        .map(StackComponentImpl::new)
                                        .collect(toList());
        }
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    @Override
    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    @Override
    public List<String> getTags() {
        if (tags == null) {
            return new ArrayList<>();
        }
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    @Override
    public WorkspaceConfigImpl getWorkspaceConfig() {
        return workspaceConfig;
    }

    public void setWorkspaceConfig(WorkspaceConfigImpl workspaceConfig) {
        this.workspaceConfig = workspaceConfig;
    }

    @Override
    public StackSourceImpl getSource() {
        return source;
    }

    public void setSource(StackSourceImpl source) {
        this.source = source;
    }

    @Override
    public List<StackComponentImpl> getComponents() {
        if (components == null) {
            return new ArrayList<>();
        }
        return components;
    }

    public void setComponents(List<StackComponentImpl> components) {
        this.components = components;
    }

    public StackIcon getStackIcon() {
        return stackIcon;
    }

    public void setStackIcon(StackIcon stackIcon) {
        this.stackIcon = stackIcon;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof StackImpl)) {
            return false;
        }
        final StackImpl that = (StackImpl)obj;
        return Objects.equals(id, that.id)
               && Objects.equals(name, that.name)
               && Objects.equals(description, that.description)
               && Objects.equals(scope, that.scope)
               && Objects.equals(creator, that.creator)
               && getTags().equals(that.getTags())
               && Objects.equals(workspaceConfig, that.workspaceConfig)
               && Objects.equals(source, that.source)
               && getComponents().equals(that.getComponents())
               && Objects.equals(stackIcon, that.stackIcon);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(id);
        hash = 31 * hash + Objects.hashCode(name);
        hash = 31 * hash + Objects.hashCode(description);
        hash = 31 * hash + Objects.hashCode(scope);
        hash = 31 * hash + Objects.hashCode(creator);
        hash = 31 * hash + getTags().hashCode();
        hash = 31 * hash + Objects.hashCode(workspaceConfig);
        hash = 31 * hash + Objects.hashCode(source);
        hash = 31 * hash + getComponents().hashCode();
        hash = 31 * hash + Objects.hashCode(stackIcon);
        return hash;
    }

    @Override
    public String toString() {
        return "StackImpl{" +
               "id='" + id + '\'' +
               ", name='" + name + '\'' +
               ", description='" + description + '\'' +
               ", scope='" + scope + '\'' +
               ", creator='" + creator + '\'' +
               ", tags=" + tags +
               ", workspaceConfig=" + workspaceConfig +
               ", source=" + source +
               ", components=" + components +
               ", stackIcon=" + stackIcon +
               '}';
    }

    public static class StackBuilder {

        private String                         id;
        private String                         name;
        private String                         description;
        private String                         scope;
        private String                         creator;
        private List<String>                   tags;
        private WorkspaceConfig                workspaceConfig;
        private StackSource                    source;
        private List<? extends StackComponent> components;
        private StackIcon                      stackIcon;

        public StackBuilder generateId() {
            id = NameGenerator.generate("stack", 16);
            return this;
        }

        public StackBuilder setId(String id) {
            this.id = id;
            return this;
        }

        public StackBuilder setName(String name) {
            this.name = name;
            return this;
        }

        public StackBuilder setDescription(String description) {
            this.description = description;
            return this;
        }

        public StackBuilder setScope(String scope) {
            this.scope = scope;
            return this;
        }

        public StackBuilder setCreator(String creator) {
            this.creator = creator;
            return this;
        }

        public StackBuilder setTags(List<String> tags) {
            this.tags = (tags == null) ? new ArrayList<>() : tags;
            return this;
        }

        public StackBuilder setWorkspaceConfig(WorkspaceConfig workspaceConfig) {
            this.workspaceConfig = workspaceConfig;
            return this;
        }

        public StackBuilder setSource(StackSource source) {
            this.source = source;
            return this;
        }

        public StackBuilder setComponents(List<? extends StackComponent> components) {
            this.components = (components == null) ? new ArrayList<>() : components;
            return this;
        }

        public StackBuilder setStackIcon(StackIcon stackIcon) {
            this.stackIcon = stackIcon;
            return this;
        }

        public StackImpl build() {
            return new StackImpl(id,
                                 name,
                                 description,
                                 scope,
                                 creator,
                                 tags,
                                 workspaceConfig,
                                 source,
                                 components,
                                 stackIcon);
        }
    }
}
