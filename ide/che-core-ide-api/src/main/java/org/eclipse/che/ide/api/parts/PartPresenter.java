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
import com.google.gwt.user.client.ui.IsWidget;

import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.mvp.Presenter;
import org.eclipse.che.ide.api.selection.Selection;
import org.vectomatic.dom.svg.ui.SVGResource;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Part is a main UI block of the IDE.
 *
 * @author Nikolay Zamosenchuk
 * @author Stéphane Daviet
 */
public interface PartPresenter extends Presenter {

    /** The property id for <code>getTitle</code>, <code>getTitleImage</code> and <code>getTitleToolTip</code>. */
    int TITLE_PROPERTY     = 0x001;

    /** The property id for <code>getSelection</code>. */
    int SELECTION_PROPERTY = 0x002;

    /** Store part state before changing perspective. */
    void storeState();

    /** Restore part state after changing perspective. */
    void restoreState();

    /** @return Title of the Part */
    @NotNull
    String getTitle();

    void addRule(@NotNull String perspectiveId);

    List<String> getRules();

    IsWidget getView();

    /**
     * Returns the title SVG image resource of this part. If this value changes the part must fire a property listener event with
     * <code>PROP_TITLE</code>.
     * <p>
     * The title image is usually used to populate the title bar of this part's visual container.
     *
     * @return the title SVG image resource
     */
    @Nullable
    SVGResource getTitleImage();

    /**
     * Returns count of unread notifications.
     * Is used to display a badge on part button.
     *
     * @return count of unread notifications
     */
    int getUnreadNotificationsCount();

    /**
     * Returns the title tool tip text of this part.
     * An empty string result indicates no tool tip.
     * If this value changes the part must fire a property listener event with <code>PROP_TITLE</code>.
     * <p>
     * The tool tip text is used to populate the title bar of this part's visual container.
     * </p>
     *
     * @return the part title tool tip (not <code>null</code>)
     */
    @Nullable
    String getTitleToolTip();

    /**
     * Return size of part. If current part is vertical panel then size is height. If current part is horizontal panel then size is width.
     *
     * @return size of part
     */
    int getSize();

    /**
     * This method is called when Part is opened.
     * Note: this method is NOT called when part gets focused. It is called when new tab in PartStack created.
     */
    void onOpen();

    /**
     * This method is called when part is going to be closed. Part itself can deny blocking, by calling onFailure() on callback, i.e. when
     * document is
     * being edited and accidentally close button pressed.
     *
     * @param callback
     */
    void onClose(@NotNull AsyncCallback<Void> callback);

    /**
     * Adds a listener for changes to properties of this part. Has no effect if an identical listener is already registered.
     *
     * @param listener
     *         a property listener
     */
    void addPropertyListener(@NotNull PropertyListener listener);

    /** @return The {@link org.eclipse.che.ide.api.selection.Selection} of this Part. */
    @NotNull
    Selection<?> getSelection();

    /**
     * Removes the given property listener from this part. Has no effect if an identical listener is not registered.
     *
     * @param listener
     *         a property listener
     */
    void removePropertyListener(@NotNull PropertyListener listener);

}
