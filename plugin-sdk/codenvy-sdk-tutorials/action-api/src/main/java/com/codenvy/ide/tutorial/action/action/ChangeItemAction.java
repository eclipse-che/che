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
package com.codenvy.ide.tutorial.action.action;

import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;
import com.codenvy.ide.tutorial.action.ActionTutorialResources;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import static com.codenvy.ide.tutorial.action.ActionTutorialExtension.SHOW_ITEM;

/**
 * The action for changing visibility and availability of other items.
 *
 * @author <a href="mailto:aplotnikov@codenvy.com">Andrey Plotnikov</a>
 */
@Singleton
public class ChangeItemAction extends Action {

    @Inject
    public ChangeItemAction(ActionTutorialResources resources) {
        super("Change visibility and availability", "The action for changing visibility and availability of other items",
              resources.item());
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(ActionEvent e) {
        SHOW_ITEM = !SHOW_ITEM;
    }
}