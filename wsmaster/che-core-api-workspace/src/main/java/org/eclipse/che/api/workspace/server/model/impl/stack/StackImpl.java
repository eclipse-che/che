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

import org.eclipse.che.api.machine.server.model.impl.AclEntryImpl;
import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.stack.image.StackIcon;
import org.eclipse.che.api.workspace.shared.stack.Stack;
import org.eclipse.che.api.workspace.shared.stack.StackComponent;
import org.eclipse.che.api.workspace.shared.stack.StackSource;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.commons.lang.NameGenerator;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.toList;
import static javax.persistence.CascadeType.ALL;

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
public class StackImpl implements Stack {

    public static StackBuilder builder() {
        return new StackBuilder();
    }

    @Id
    private String id;

    @Column(unique = true, nullable = false)
    private String name;

    @Basic
    private String description;

    @Basic
    private String scope;

    @Basic
    private String creator;

    @ElementCollection
    @Column(name = "tag")
    @CollectionTable(indexes = @Index(columnList = "tag"))
    private List<String> tags;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private WorkspaceConfigImpl workspaceConfig;

    @Embedded
    private StackSourceImpl source;

    @ElementCollection
    private List<StackComponentImpl> components;

    @Embedded
    private StackIcon stackIcon;

    @OneToMany(cascade = ALL, orphanRemoval = true)
    @JoinColumn
    private List<AclEntryImpl> acl;

    @ElementCollection
    private List<String> publicActions;

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
             stack.getStackIcon(),
             stack.getAcl(),
             stack.getPublicActions());
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
             null,
             null,
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
                     StackIcon stackIcon,
                     List<AclEntryImpl> acl,
                     List<String> publicActions) {
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
        if (acl != null) {
            this.acl = new ArrayList<>(acl);
        }
        if (publicActions != null) {
            this.publicActions = new ArrayList<>(publicActions);
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

    @Nullable
    public List<AclEntryImpl> getAcl() {
        if (acl == null) {
            acl = new ArrayList<>();
        }
        return acl;
    }

    public void setAcl(List<AclEntryImpl> acl) {
        this.acl = acl;
    }

    public StackImpl withAcl(List<AclEntryImpl> acl) {
        this.acl = acl;
        return this;
    }

    public List<String> getPublicActions() {
        if (publicActions == null) {
            publicActions = new ArrayList<>();
        }
        return publicActions;
    }

    public void setPublicActions(List<String> publicActions) {
        this.publicActions = publicActions;
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
               && Objects.equals(stackIcon, that.stackIcon)
               && getAcl().equals(that.getAcl())
               && getPublicActions().equals(that.getPublicActions());
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
        hash = 31 * hash + getAcl().hashCode();
        hash = 31 * hash + getPublicActions().hashCode();
        return hash;
    }

    @Override
    public String toString() {
        return "StackImpl{" +
               "publicActions=" + publicActions +
               ", acl=" + acl +
               ", stackIcon=" + stackIcon +
               ", components=" + components +
               ", source=" + source +
               ", workspaceConfig=" + workspaceConfig +
               ", tags=" + tags +
               ", creator='" + creator + '\'' +
               ", scope='" + scope + '\'' +
               ", description='" + description + '\'' +
               ", name='" + name + '\'' +
               ", id='" + id + '\'' +
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
        private List<AclEntryImpl>             acl;
        private List<String>                   publicActions;

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

        public StackBuilder setAcl(List<AclEntryImpl> acl) {
            this.acl = acl;
            return this;
        }

        public StackBuilder setPublicActions(List<String> publicActions) {
            this.publicActions = publicActions;
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
                                 stackIcon,
                                 acl,
                                 publicActions);
        }
    }
}
