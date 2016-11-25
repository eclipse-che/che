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
import org.eclipse.che.api.core.model.factory.OnAppClosed;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static javax.persistence.CascadeType.ALL;

/**
 * Data object for {@link OnAppClosed}.
 *
 * @author Anton Korneta
 */
@Entity(name = "OnAppClosed")
@Table(name = "onappclosed")
public class OnAppClosedImpl implements OnAppClosed {

    @Id
    @GeneratedValue
    @Column(name = "id")
    private Long id;

    @OneToMany(cascade = ALL, orphanRemoval = true)
    @JoinTable(name = "onappclosed_action",
               joinColumns = @JoinColumn(name = "onappclosed_id"),
               inverseJoinColumns = @JoinColumn(name = "actions_entityid"))
    private List<ActionImpl> actions;

    public OnAppClosedImpl() {}

    public OnAppClosedImpl(List<? extends Action> actions) {
        if (actions != null) {
            this.actions = actions.stream()
                                  .map(ActionImpl::new)
                                  .collect(toList());
        }
    }

    public OnAppClosedImpl(OnAppClosed onAppClosed) {
        this(onAppClosed.getActions());
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
        if (!(obj instanceof OnAppClosedImpl)) return false;
        final OnAppClosedImpl other = (OnAppClosedImpl)obj;
        return getActions().equals(other.getActions());
    }

    @Override
    public int hashCode() {
        return getActions().hashCode();
    }

    @Override
    public String toString() {
        return "OnAppClosedImpl{" +
               "actions=" + actions +
               '}';
    }
}
