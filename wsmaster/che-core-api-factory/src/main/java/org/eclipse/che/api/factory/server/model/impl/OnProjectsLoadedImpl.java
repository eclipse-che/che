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
package org.eclipse.che.api.factory.server.model.impl;

import org.eclipse.che.api.core.model.factory.Action;
import org.eclipse.che.api.core.model.factory.OnProjectsLoaded;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Data object for {@link OnProjectsLoaded}.
 *
 * @author Anton Korneta
 */
@Entity(name = "OnProjectsLoaded")
public class OnProjectsLoadedImpl implements OnProjectsLoaded {

    @Id
    @GeneratedValue
    private Long id;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ActionImpl> actions;

    public OnProjectsLoadedImpl() {}

    public OnProjectsLoadedImpl(List<? extends Action> actions) {
        if (actions != null) {
            this.actions = actions.stream()
                                  .map(ActionImpl::new)
                                  .collect(toList());
        }
    }

    public OnProjectsLoadedImpl(OnProjectsLoaded onProjectsLoaded) {
        this(onProjectsLoaded.getActions());
    }

    @Override
    public List<ActionImpl> getActions() {
        if (actions == null) {
            return new ArrayList<>();
        }
        return actions;
    }

    public void setActions(List<ActionImpl> actions) {
        this.actions = actions;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof OnProjectsLoadedImpl)) return false;
        final OnProjectsLoadedImpl other = (OnProjectsLoadedImpl)obj;
        return getActions().equals(other.getActions());
    }

    @Override
    public int hashCode() {
        return getActions().hashCode();
    }

    @Override
    public String toString() {
        return "OnProjectsLoadedImpl{" +
               "actions=" + actions +
               '}';
    }
}
