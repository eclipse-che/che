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
package org.eclipse.che.ide.api.parts;

import com.google.gwt.user.client.rpc.AsyncCallback;

import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.selection.Selection;
import org.eclipse.che.ide.util.ListenerManager;
import org.eclipse.che.ide.util.ListenerManager.Dispatcher;
import org.vectomatic.dom.svg.ui.SVGResource;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;


/**
 * Abstract base implementation of all PartPresenter
 *
 * @author Evgen Vidolob
 * @author Stéphane Daviet
 * @author Valeriy Svydenko
 */
public abstract class AbstractPartPresenter implements PartPresenter {
    private final ListenerManager<PropertyListener> manager;
    private final List<String>                      rules;

    private Selection<?> selection;

    public AbstractPartPresenter() {
        manager = ListenerManager.create();
        rules = new ArrayList<>();
        selection = new Selection.NoSelectionProvided();
    }

    /** {@inheritDoc} */
    @Override
    public void storeState() {
        //default implementation is empty. Add some logic for particular part to store it's state
    }

    /** {@inheritDoc} */
    @Override
    public void restoreState() {
        //default implementation is empty. Add some logic for particular part to restore it's state
    }

    /** {@inheritDoc} */
    @Override
    public void addRule(@NotNull String perspectiveId) {
        rules.add(perspectiveId);
    }

    /** {@inheritDoc} */
    @Override
    public List<String> getRules() {
        return rules;
    }

    /** {@inheritDoc} */
    @Override
    public void onClose(@NotNull AsyncCallback<Void> callback) {
        callback.onSuccess(null);
    }

    /** {@inheritDoc} */
    @Override
    public void onOpen() {
    }

    /** {@inheritDoc} */
    @Override
    public void addPropertyListener(@NotNull PropertyListener listener) {
        manager.add(listener);
    }

    /** {@inheritDoc} */
    @Override
    public void removePropertyListener(@NotNull PropertyListener listener) {
        manager.remove(listener);
    }

    /**
     * Fires a property changed event.
     *
     * @param propId
     *         the id of the property that changed
     */
    protected void firePropertyChange(final int propId) {
        manager.dispatch(new Dispatcher<PropertyListener>() {
            @Override
            public void dispatch(PropertyListener listener) {
                listener.propertyChanged(AbstractPartPresenter.this, propId);
            }
        });
    }

    /** {@inheritDoc} */
    @NotNull
    @Override
    public Selection<?> getSelection() {
        return this.selection;
    }

    /**
     * Sets the Selection of the Part. It later can be accessible using {@link AbstractPartPresenter#getSelection()}
     *
     * @param selection
     *         instance of Selection
     */
    public void setSelection(@NotNull Selection<?> selection) {
        this.selection = selection;
        firePropertyChange(SELECTION_PROPERTY);
    }

    /** {@inheritDoc} */
    @Override
    public int getSize() {
        return 285;
    }

    /** {@inheritDoc} */
    @Override
    public int getUnreadNotificationsCount() {
        return 0;
    }

    @Nullable
    @Override
    public SVGResource getTitleImage() {
        return null;
    }

}
