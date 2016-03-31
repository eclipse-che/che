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
package org.eclipse.che.plugin.svn.ide.update;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Event fired when update to revision has been performed.
 *
 * @author Vladyslav Zhukovskyi
 */
public class SubversionProjectUpdatedEvent extends GwtEvent<SubversionProjectUpdatedHandler> {

    /** Type class used to register this event. */
    public static Type<SubversionProjectUpdatedHandler> TYPE = new Type<>();

    private long revision;

    public SubversionProjectUpdatedEvent() {
    }

    public SubversionProjectUpdatedEvent(long revision) {
        this.revision = revision;
    }

    /** {@inheritDoc} */
    @Override
    public Type<SubversionProjectUpdatedHandler> getAssociatedType() {
        return TYPE;
    }

    /** {@inheritDoc} */
    @Override
    protected void dispatch(SubversionProjectUpdatedHandler handler) {
        handler.onProjectUpdated(this);
    }

    /** Return revision for updated working copy. */
    public long getRevision() {
        return revision;
    }
}
