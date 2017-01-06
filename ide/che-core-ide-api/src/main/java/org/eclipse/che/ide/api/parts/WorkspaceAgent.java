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
package org.eclipse.che.ide.api.parts;


import org.eclipse.che.ide.api.constraints.Constraints;
import org.eclipse.che.ide.api.extension.SDK;

import javax.validation.constraints.NotNull;


/**
 * Handles IDE Perspective, allows to open/close/switch Parts,
 * manages opened Parts.
 *
 * @author <a href="mailto:nzamosenchuk@exoplatform.com">Nikolay Zamosenchuk</a>
 */
@SDK(title = "ide.api.ui.workspace")
public interface WorkspaceAgent {

    /**
     * Activate given part
     *
     * @param part
     */
    void setActivePart(PartPresenter part);

    void setActivePart(@NotNull PartPresenter part, PartStackType type);

    /**
     * Opens given Part
     *
     * @param part
     * @param type
     */
    void openPart(PartPresenter part, PartStackType type);

    /**
     * Opens Part with constraint
     *
     * @param part
     * @param type
     * @param constraint
     */
    void openPart(PartPresenter part, PartStackType type, Constraints constraint);

    /**
     * Hides given Part
     *
     * @param part
     */
    void hidePart(PartPresenter part);

    /**
     * Remove given Part
     *
     * @param part
     */
    void removePart(PartPresenter part);

    /**
     * Retrieves the instance of the {@link PartStack} for given {@link PartStackType}
     *
     * @param type
     *         one of the enumerated type {@link PartStackType}
     * @return the part stack found, else null
     */
    PartStack getPartStack(PartStackType type);

}