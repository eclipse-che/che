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

import com.google.inject.Inject;
import com.google.inject.Singleton;

import javax.validation.constraints.NotNull;
import org.eclipse.che.commons.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The class stores current perspective type. Contains listeners which do some actions when type is changed. By default PROJECT
 * perspective type is set.
 *
 * @author Dmitry Shnurenko
 */
@Singleton
public class PerspectiveManager {

    private final List<PerspectiveTypeListener> listeners;
    private final Map<String, Perspective>      perspectives;

    private String currentPerspectiveId;

    @Inject
    public PerspectiveManager(Map<String, Perspective> perspectives) {
        this.perspectives = perspectives;
        listeners = new ArrayList<>();

        //perspective by default
        currentPerspectiveId = "Project Perspective";
    }

    /** Returns current active perspective. The method can return null, if current perspective isn't found. */
    @Nullable
    public Perspective getActivePerspective() {
        return perspectives.get(currentPerspectiveId);
    }

    /**
     * Changes perspective type and notifies listeners.
     *
     * @param perspectiveId
     *         type which need set
     */
    public void setPerspectiveId(@NotNull String perspectiveId) {
        currentPerspectiveId = perspectiveId;

        for (PerspectiveTypeListener container : listeners) {
            container.onPerspectiveChanged();
        }
    }

    /** Returns current perspective type. */
    @NotNull
    public String getPerspectiveId() {
        return currentPerspectiveId;
    }

    /**
     * Adds listeners which will react on changing of perspective type.
     *
     * @param listener
     *         listener which need add
     */
    public void addListener(@NotNull PerspectiveTypeListener listener) {
        listeners.add(listener);
    }

    /** The interface which must be implemented by all elements who need react on perspective changing. */
    public interface PerspectiveTypeListener {
        /** Performs some action when perspective was changed. */
        void onPerspectiveChanged();
    }

}
