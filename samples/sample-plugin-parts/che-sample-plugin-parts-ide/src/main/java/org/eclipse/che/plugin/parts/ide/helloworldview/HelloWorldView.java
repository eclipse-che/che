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
package org.eclipse.che.plugin.parts.ide.helloworldview;


import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.api.parts.base.BaseActionDelegate;

/**
 * Simple view only containing a label.
 *
 * @author Edgar Mueller
 */
public interface HelloWorldView extends View<HelloWorldView.ActionDelegate> {

    /**
     * Make this view visible.
     *
     * @param visible whether the view is visible
     */
    void setVisible(boolean visible);

    /**
     * Empty action delegate.
     */
    interface ActionDelegate extends BaseActionDelegate {

    }
}
