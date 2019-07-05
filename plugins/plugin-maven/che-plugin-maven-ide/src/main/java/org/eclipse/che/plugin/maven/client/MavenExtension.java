/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.maven.client;

import static org.eclipse.che.ide.api.action.IdeActions.GROUP_ASSISTANT;
import static org.eclipse.che.plugin.maven.client.actions.MavenActionsConstants.MAVEN_GROUP_CONTEXT_MENU_ID;
import static org.eclipse.che.plugin.maven.client.actions.MavenActionsConstants.MAVEN_GROUP_CONTEXT_MENU_NAME;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Arrays;
import java.util.List;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.constraints.Constraints;
import org.eclipse.che.ide.api.extension.Extension;
import org.eclipse.che.ide.api.filetypes.FileTypeRegistry.FileTypeProvider;
import org.eclipse.che.ide.api.project.type.wizard.PreSelectedProjectTypeManager;
import org.eclipse.che.ide.ext.java.client.action.GetEffectivePomAction;
import org.eclipse.che.ide.ext.java.client.action.ReimportMavenDependenciesAction;
import org.eclipse.che.plugin.maven.shared.MavenAttributes;
import org.vectomatic.dom.svg.ui.SVGImage;

/**
 * Maven extension entry point.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
@Extension(title = "Maven", version = "3.0.0")
public class MavenExtension {
  private static List<MavenArchetype> archetypes;
  private final MavenResources resources;

  @Inject
  public MavenExtension(
      PreSelectedProjectTypeManager preSelectedProjectManager, MavenResources resources) {
    this.resources = resources;
    preSelectedProjectManager.setProjectTypeIdToPreselect(MavenAttributes.MAVEN_ID, 100);

    archetypes =
        Arrays.asList(
            new MavenArchetype(
                "org.apache.maven.archetypes", "maven-archetype-quickstart", "RELEASE", null),
            new MavenArchetype(
                "org.apache.maven.archetypes", "maven-archetype-webapp", "RELEASE", null),
            new MavenArchetype(
                "org.apache.openejb.maven", "tomee-webapp-archetype", "1.7.1", null));
  }

  public static List<MavenArchetype> getAvailableArchetypes() {
    return archetypes;
  }

  @Inject
  private void prepareActions(
      ActionManager actionManager,
      GetEffectivePomAction getEffectivePomAction,
      ReimportMavenDependenciesAction reimportMavenDependenciesAction) {
    // register actions
    actionManager.registerAction("getEffectivePom", getEffectivePomAction);
    actionManager.registerAction(
        "reimportMavenDependenciesAction", reimportMavenDependenciesAction);

    // add actions in main menu
    DefaultActionGroup assistantGroup =
        (DefaultActionGroup) actionManager.getAction(GROUP_ASSISTANT);
    assistantGroup.add(getEffectivePomAction, Constraints.LAST);

    // create maven context menu
    DefaultActionGroup mavenContextMenuGroup =
        new DefaultActionGroup(MAVEN_GROUP_CONTEXT_MENU_NAME, true, actionManager);
    actionManager.registerAction(MAVEN_GROUP_CONTEXT_MENU_ID, mavenContextMenuGroup);
    mavenContextMenuGroup
        .getTemplatePresentation()
        .setImageElement(new SVGImage(resources.maven()).getElement());

    // add maven context menu to main context menu
    DefaultActionGroup mainContextMenuGroup =
        (DefaultActionGroup) actionManager.getAction("resourceOperation");
    mainContextMenuGroup.addSeparator();
    mainContextMenuGroup.add(mavenContextMenuGroup, Constraints.LAST);

    // add actions in context menu
    mavenContextMenuGroup.add(reimportMavenDependenciesAction);
    mavenContextMenuGroup.addSeparator();
  }

  @Inject
  private void registerFileType(MavenResources mavenResources, FileTypeProvider fileTypeProvider) {
    fileTypeProvider.getByNamePattern(mavenResources.maven(), ".*[/\\\\]?pom\\.xml$");
  }
}
