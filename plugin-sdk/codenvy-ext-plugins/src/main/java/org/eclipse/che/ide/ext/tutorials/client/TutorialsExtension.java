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
package org.eclipse.che.ide.ext.tutorials.client;

import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.extension.Extension;
import org.eclipse.che.ide.api.icon.Icon;
import org.eclipse.che.ide.api.icon.IconRegistry;
import org.eclipse.che.ide.api.project.tree.TreeStructureProviderRegistry;
import org.eclipse.che.ide.ext.tutorials.client.action.ShowTutorialGuideAction;
import org.eclipse.che.ide.ext.tutorials.client.action.UpdateAction;
import org.eclipse.che.ide.ext.tutorials.shared.Constants;
import org.eclipse.che.ide.extension.maven.client.projecttree.MavenProjectTreeStructureProvider;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import static org.eclipse.che.ide.api.action.IdeActions.GROUP_RUN;
import static org.eclipse.che.ide.api.action.IdeActions.GROUP_WINDOW;

/**
 * Entry point for an extension that adds support to work with tutorial projects.
 *
 * @author Artem Zatsarynnyy
 */
@Singleton
@Extension(title = "Codenvy tutorial projects", version = "3.0.0")
public class TutorialsExtension {
    /** Default name of the file that contains tutorial description. */
    public static final String DEFAULT_GUIDE_FILE_NAME = ".guide/guide.html";

    @Inject
    public TutorialsExtension(TutorialsResources resources,
                              TutorialsLocalizationConstant localizationConstants,
                              ActionManager actionManager,
                              ShowTutorialGuideAction showAction,
                              UpdateAction updateAction,
                              IconRegistry iconRegistry,
                              TreeStructureProviderRegistry treeStructureProviderRegistry) {
        resources.tutorialsCss().ensureInjected();

        // register Icons for samples and codenvy projecttypes
        iconRegistry.registerIcon(new Icon("Samples.samples.category.icon", resources.samplesCategorySamples()));
        iconRegistry.registerIcon(new Icon("Samples - Hello World.samples.category.icon", resources.samplesCategorySamples()));
        iconRegistry.registerIcon(new Icon("Codenvy.samples.category.icon", resources.samplesCategoryCodenvy()));

        // use Maven project tree for 'Codenvy Extension' and 'Tutorial' project types
        treeStructureProviderRegistry.associateProjectTypeToTreeProvider(Constants.TUTORIAL_ID, MavenProjectTreeStructureProvider.ID);
        treeStructureProviderRegistry.associateProjectTypeToTreeProvider(org.eclipse.che.ide.Constants.CODENVY_PLUGIN_ID,
                                                                         MavenProjectTreeStructureProvider.ID);

        // register actions
        DefaultActionGroup windowMenuActionGroup = (DefaultActionGroup)actionManager.getAction(GROUP_WINDOW);

        actionManager.registerAction(localizationConstants.showTutorialGuideActionId(), showAction);
        windowMenuActionGroup.add(showAction);

        actionManager.registerAction(localizationConstants.updateExtensionActionId(), updateAction);
        DefaultActionGroup runMenuActionGroup = (DefaultActionGroup)actionManager.getAction(GROUP_RUN);
        runMenuActionGroup.add(updateAction);
    }
}
