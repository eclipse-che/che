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
package org.eclipse.che.ide.command.editor;

import com.google.gwt.i18n.client.Messages;

/**
 * I18n messages for the Command Editor.
 *
 * @author Artem Zatsarynnyi
 */
public interface EditorMessages extends Messages {

    @Key("editor.description")
    String editorDescription();

    @Key("editor.message.unable_save")
    String editorMessageUnableToSave();

    @Key("button.save.text")
    String buttonSaveText();

    @Key("page.settings.title")
    String pageInfoTitle();

    @Key("page.settings.name.label")
    String pageInfoNameLabel();

    @Key("page.settings.goal.label")
    String pageInfoGoalLabel();

    @Key("page.settings.section.context.label")
    String pageInfoSectionContextLabel();

    @Key("page.settings.workspace.label")
    String pageInfoWorkspaceLabel();

    @Key("page.settings.section.projects.label")
    String pageInfoSectionProjectsLabel();

    @Key("page.settings.projects_table.header.project.label")
    String pageInfoProjectsTableHeaderProjectLabel();

    @Key("page.settings.projects_table.header.applicable.label")
    String pageInfoProjectsTableHeaderApplicableLabel();

    @Key("page.with_text_editor.explore_macros")
    String pageWithTextEditorExploreMacros();

    @Key("page.arguments.title")
    String pageArgumentsTitle();

    @Key("page.preview_url.title")
    String pagePreviewUrlTitle();
}
