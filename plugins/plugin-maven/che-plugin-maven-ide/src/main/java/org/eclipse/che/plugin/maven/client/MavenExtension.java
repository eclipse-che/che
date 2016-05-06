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
package org.eclipse.che.plugin.maven.client;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.action.IdeActions;
import org.eclipse.che.ide.api.constraints.Anchor;
import org.eclipse.che.ide.api.constraints.Constraints;
import org.eclipse.che.ide.api.editor.EditorRegistry;
import org.eclipse.che.ide.api.extension.Extension;
import org.eclipse.che.ide.api.filetypes.FileType;
import org.eclipse.che.ide.api.filetypes.FileTypeRegistry;
import org.eclipse.che.ide.api.project.type.wizard.PreSelectedProjectTypeManager;
import org.eclipse.che.plugin.maven.client.actions.CreateMavenModuleAction;
import org.eclipse.che.plugin.maven.client.actions.GetEffectivePomAction;
import org.eclipse.che.plugin.maven.client.comunnication.MavenMessagesHandler;
import org.eclipse.che.plugin.maven.client.comunnication.progressor.background.DependencyResolverAction;
import org.eclipse.che.plugin.maven.client.editor.ClassFileSourcesDownloader;
import org.eclipse.che.plugin.maven.client.editor.PomEditorProvider;
import org.eclipse.che.plugin.maven.shared.MavenAttributes;

import java.util.Arrays;
import java.util.List;

import static org.eclipse.che.ide.api.action.IdeActions.GROUP_BUILD_CONTEXT_MENU;
import static org.eclipse.che.ide.api.action.IdeActions.GROUP_FILE_NEW;
import static org.eclipse.che.ide.api.action.IdeActions.GROUP_RIGHT_STATUS_PANEL;

/**
 * Maven extension entry point.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
@Extension(title = "Maven", version = "3.0.0")
public class MavenExtension {
    private static List<MavenArchetype> archetypes;

    @Inject
    public MavenExtension(PreSelectedProjectTypeManager preSelectedProjectManager,
                          MavenMessagesHandler messagesHandler,
                          ClassFileSourcesDownloader downloader) {

        preSelectedProjectManager.setProjectTypeIdToPreselect(MavenAttributes.MAVEN_ID, 100);

        archetypes =
                Arrays.asList(new MavenArchetype("org.apache.maven.archetypes", "maven-archetype-quickstart", "RELEASE", null),
                              new MavenArchetype("org.apache.maven.archetypes", "maven-archetype-webapp", "RELEASE", null),
                              new MavenArchetype("org.apache.openejb.maven", "tomee-webapp-archetype", "1.7.1", null));
    }

    public static List<MavenArchetype> getAvailableArchetypes() {
        return archetypes;
    }

    @Inject
    private void prepareActions(ActionManager actionManager,
                                CreateMavenModuleAction createMavenModuleAction,
                                DependencyResolverAction dependencyResolverAction,
                                GetEffectivePomAction getEffectivePomAction) {
        // register actions
        actionManager.registerAction("createMavenModule", createMavenModuleAction);
        actionManager.registerAction("getEffectivePom", getEffectivePomAction);

        // add actions in main menu
        DefaultActionGroup assistantGroup = (DefaultActionGroup)actionManager.getAction(IdeActions.GROUP_ASSISTANT);
        assistantGroup.add(getEffectivePomAction, Constraints.LAST);

        DefaultActionGroup newGroup = (DefaultActionGroup)actionManager.getAction(GROUP_FILE_NEW);
        newGroup.add(createMavenModuleAction, new Constraints(Anchor.AFTER, "newJavaPackage"));

        // add actions in context menu
        DefaultActionGroup buildContextMenuGroup = (DefaultActionGroup)actionManager.getAction(GROUP_BUILD_CONTEXT_MENU);
        buildContextMenuGroup.addSeparator();

        // add resolver widget on right part of bottom panel
        final DefaultActionGroup rightStatusPanelGroup = (DefaultActionGroup)actionManager.getAction(GROUP_RIGHT_STATUS_PANEL);
        rightStatusPanelGroup.add(dependencyResolverAction);
    }

    @Inject
    private void registerFileType(FileTypeRegistry fileTypeRegistry,
                                  MavenResources mavenResources,
                                  EditorRegistry editorRegistry,
                                  PomEditorProvider editorProvider) {
        FileType pomFile = new FileType(mavenResources.maven(), "pom.xml");
        fileTypeRegistry.registerFileType(pomFile);
        editorRegistry.register(pomFile, editorProvider);
    }

}
