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
package org.eclipse.che.ide.jseditor.client.editoradapter;

import javax.validation.constraints.NotNull;

import org.eclipse.che.ide.api.mvp.Presenter;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Kind of presenter that can be nested inside an {@link EditorAdapter}.
 */
public interface NestablePresenter extends Presenter, HasEditor {

    /**
     * Action to do when closing the component.
     * @param callback the callback
     */
    void onClose(@NotNull final AsyncCallback<Void> callback);
}
