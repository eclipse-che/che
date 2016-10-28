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
package org.eclipse.che.ide.part.editor.multipart;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.ImplementedBy;

import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.constraints.Constraints;
import org.eclipse.che.ide.api.parts.EditorMultiPartStackState;
import org.eclipse.che.ide.api.parts.EditorPartStack;

import javax.validation.constraints.NotNull;

/**
 * Provides methods to control view representation of multi part stack container.
 *
 * @author Roman Nikitenko
 */
@ImplementedBy(EditorMultiPartStackViewImpl.class)
public interface EditorMultiPartStackView extends IsWidget {

    /**
     * Adds editor part stack:
     * <li>  -  if {@code relativePartStack} is null  - Editor Part Stack will be added to the main editor area </li>
     * <li>  -  if {@code relativePartStack} not null - view of {@code relativePartStack} will be split corresponding to {@code
     * constraints} on two areas and Editor Part Stack will be added into created area</li>
     *  @param partStack
     *         editor part stack to adding in corresponding area
     * @param relativePartStack
     *         relative editor part stack which will be split
     * @param constraints
 *         contains info about way how view of {@code relativePartStack} should be split
     * @param size
     */
    void addPartStack(@NotNull final EditorPartStack partStack, @Nullable final EditorPartStack relativePartStack,
                      @Nullable final Constraints constraints, double size);

    /**
     * Remove given editor part stack from editor area.
     *
     * @param partStack
     *         editor part stack to removing from corresponding editor area
     */
    void removePartStack(@NotNull final EditorPartStack partStack);

    /**
     * @return the editor multi part stack state
     */
    EditorMultiPartStackState getState();
}
