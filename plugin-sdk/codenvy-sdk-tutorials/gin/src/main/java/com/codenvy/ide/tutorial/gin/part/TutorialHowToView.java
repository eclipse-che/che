/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.ide.tutorial.gin.part;

import org.eclipse.che.ide.api.mvp.View;
import com.google.inject.ImplementedBy;

/**
 * The view of {@link TutorialHowToPresenter}.
 *
 * @author <a href="mailto:aplotnikov@codenvy.com">Andrey Plotnikov</a>
 */
@ImplementedBy(TutorialHowToViewImpl.class)
public interface TutorialHowToView extends View<TutorialHowToView.ActionDelegate> {
    /** Required for delegating functions in view. */
    public interface ActionDelegate {
    }
}