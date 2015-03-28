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
package com.codenvy.ide.ext.helloworld.action;

/**
 * As usual, importing resources, related to Action API.
 * The 3rd import is required to call a default alert box.
 */
import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;
import com.google.gwt.user.client.Window;
import com.google.inject.Inject;

public class HelloWorldAction extends Action
{
    /**
     * Define a constructor and pass over text to be displayed in the dialogue box
     */

    @Inject
    public HelloWorldAction() {
        super("Hello World");
    }

    /**
     * Define the action required when calling this method. In our case it'll open a dialogue box with 'Hello world'
     */

    @Override
    public void actionPerformed(ActionEvent arg0) {
        Window.alert("Hello world");
    }
}
