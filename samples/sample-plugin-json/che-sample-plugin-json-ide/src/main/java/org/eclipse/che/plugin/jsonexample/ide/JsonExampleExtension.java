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
package org.eclipse.che.plugin.jsonexample.ide;

import com.google.inject.Inject;

import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.extension.Extension;
import org.eclipse.che.ide.api.icon.Icon;
import org.eclipse.che.ide.api.icon.IconRegistry;
import org.eclipse.che.plugin.jsonexample.ide.action.CountLinesAction;
import org.eclipse.che.plugin.jsonexample.ide.action.HelloWorldAction;

import static org.eclipse.che.plugin.jsonexample.shared.Constants.JSON_EXAMPLE_CATEGORY;

/**
 * JSON Example extension that registers actions and icons.
 */
@Extension(title = "JSON Example Extension", version = "0.0.1")
public class JsonExampleExtension {

    /**
     * Constructor.
     *
     * @param actionManager
     *         the {@link ActionManager} that is used to register our actions
     * @param helloWorldAction
     *         action to be registered
     * @param countLinesAction
     *         action to be registered
     * @param jsonExampleResources
     *         the resources that contains our icon
     * @param iconRegistry
     *         the {@link IconRegistry} that is used to register our icon
     */
    @Inject
    public JsonExampleExtension(
            ActionManager actionManager,
            HelloWorldAction helloWorldAction,
            CountLinesAction countLinesAction,
            JsonExampleResources jsonExampleResources,
            IconRegistry iconRegistry) {

        DefaultActionGroup mainContextMenuGroup = (DefaultActionGroup)actionManager.getAction("resourceOperation");
        DefaultActionGroup jsonGroup = new DefaultActionGroup("JSON Example", true, actionManager);
        mainContextMenuGroup.add(jsonGroup);

        actionManager.registerAction("jsonExample", jsonGroup);
        jsonGroup.add(countLinesAction);

        actionManager.registerAction("helloWorldAction", helloWorldAction);
        jsonGroup.add(helloWorldAction);

        iconRegistry.registerIcon(
                new Icon(JSON_EXAMPLE_CATEGORY + ".samples.category.icon", jsonExampleResources.icon()));
    }
}
