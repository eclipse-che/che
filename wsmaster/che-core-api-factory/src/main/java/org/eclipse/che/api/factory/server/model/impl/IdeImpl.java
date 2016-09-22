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

import org.eclipse.che.api.core.model.factory.Ide;
import org.eclipse.che.api.core.model.factory.OnAppClosed;
import org.eclipse.che.api.core.model.factory.OnAppLoaded;
import org.eclipse.che.api.core.model.factory.OnProjectsLoaded;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import java.util.Objects;

/**
 * Data object for {@link Ide}.
 *
 * @author Anton Korneta
 */
@Entity(name = "Ide")
public class IdeImpl implements Ide {

    @Id
    @GeneratedValue
    private Long id;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private OnAppLoadedImpl onAppLoaded;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private OnProjectsLoadedImpl onProjectsLoaded;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private OnAppClosedImpl onAppClosed;

    public IdeImpl() {}

    public IdeImpl(OnAppLoaded onAppLoaded,
                   OnProjectsLoaded onProjectsLoaded,
                   OnAppClosed onAppClosed) {
        if (onAppLoaded != null) {
            this.onAppLoaded = new OnAppLoadedImpl(onAppLoaded);
        }
        if (onProjectsLoaded != null) {
            this.onProjectsLoaded = new OnProjectsLoadedImpl(onProjectsLoaded);
        }
        if (onAppClosed != null) {
            this.onAppClosed = new OnAppClosedImpl(onAppClosed);
        }
    }

    public IdeImpl(Ide ide) {
        this(ide.getOnAppLoaded(),
             ide.getOnProjectsLoaded(),
             ide.getOnAppClosed());
    }

    @Override
    public OnAppLoadedImpl getOnAppLoaded() {
        return onAppLoaded;
    }

    public void setOnAppLoaded(OnAppLoadedImpl onAppLoaded) {
        this.onAppLoaded = onAppLoaded;
    }

    @Override
    public OnProjectsLoadedImpl getOnProjectsLoaded() {
        return onProjectsLoaded;
    }

    public void setOnProjectsLoaded(OnProjectsLoadedImpl onProjectsLoaded) {
        this.onProjectsLoaded = onProjectsLoaded;
    }

    @Override
    public OnAppClosedImpl getOnAppClosed() {
        return onAppClosed;
    }

    public void setOnAppClosed(OnAppClosedImpl onAppClosed) {
        this.onAppClosed = onAppClosed;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof IdeImpl)) return false;
        final IdeImpl other = (IdeImpl)obj;
        return Objects.equals(onAppLoaded, other.onAppLoaded)
               && Objects.equals(onProjectsLoaded, other.onProjectsLoaded)
               && Objects.equals(onAppClosed, other.onAppClosed);
    }

    @Override
    public int hashCode() {
        int result = 7;
        result = 31 * result + Objects.hashCode(onAppLoaded);
        result = 31 * result + Objects.hashCode(onProjectsLoaded);
        result = 31 * result + Objects.hashCode(onAppClosed);
        return result;
    }

    @Override
    public String toString() {
        return "IdeImpl{" +
               "onAppLoaded=" + onAppLoaded +
               ", onProjectsLoaded=" + onProjectsLoaded +
               ", onAppClosed=" + onAppClosed +
               '}';
    }
}
