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
package org.eclipse.che.ide.api.parts.base;

import org.eclipse.che.ide.api.parts.AbstractPartPresenter;
import org.eclipse.che.ide.api.parts.PartStack;

import javax.validation.constraints.NotNull;

/**
 * Base presenter for parts that support minimizing by part toolbar button.
 *
 * @author Evgen Vidolob
 */
public abstract class BasePresenter extends AbstractPartPresenter implements BaseActionDelegate {

    protected PartStack partStack;

    protected BasePresenter() {
    }

    /** {@inheritDoc} */
    @Override
    public void minimize() {
        if (partStack != null) {
            partStack.hidePart(this);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void activatePart() {
        partStack.setActivePart(this);
    }

    /**
     * Set PartStack where this part added.
     *
     * @param partStack
     */
    public void setPartStack(@NotNull PartStack partStack) {
        this.partStack = partStack;
    }

}
