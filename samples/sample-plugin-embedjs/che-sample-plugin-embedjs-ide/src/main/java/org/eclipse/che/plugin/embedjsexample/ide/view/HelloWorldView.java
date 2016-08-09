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
package org.eclipse.che.plugin.embedjsexample.ide.view;

import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.api.parts.base.BaseActionDelegate;

/**
 * @author Mathias Schaefer <mathias.schaefer@eclipsesource.com>
 */
public interface HelloWorldView extends View<HelloWorldView.ActionDelegate> {

    interface ActionDelegate extends BaseActionDelegate {
    }

    void sayHello(String content);

    void setVisible(boolean visible);
}
