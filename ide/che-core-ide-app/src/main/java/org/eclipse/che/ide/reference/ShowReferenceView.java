/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.reference;

import com.google.inject.ImplementedBy;

import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.resource.Path;

import javax.validation.constraints.NotNull;

/**
 * @author Dmitry Shnurenko
 */
@ImplementedBy(ShowReferenceViewImpl.class)
interface ShowReferenceView extends View<ShowReferenceView.ActionDelegate> {

    /**
     * The method displays 'show reference' dialog with passed parameters.
     *
     * @param reference
     *         reference is a fqn of file. This parameter can be {@code null} if method is called for file or folder which doesn't
     *         have fqn (e.g. ExternalLibrariesNode).
     * @param path
     *         location of node
     */
    void show(@Nullable String reference, @NotNull Path path);

    interface ActionDelegate {
    }
}
