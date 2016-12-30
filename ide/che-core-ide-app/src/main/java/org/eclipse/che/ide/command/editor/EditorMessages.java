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

    @Key("page.info.title")
    String pageInfoTitle();

    @Key("page.info.tooltip")
    String pageInfoTooltip();

    @Key("page.info.name.label")
    String pageInfoNameLabel();

    @Key("page.info.goal.label")
    String pageInfoGoalLabel();

    @Key("page.info.section.context.label")
    String pageInfoSectionContextLabel();

    @Key("page.info.workspace.label")
    String pageInfoWorkspaceLabel();

    @Key("page.info.section.projects.label")
    String pageInfoSectionProjectsLabel();

    @Key("page.info.projects_table.header.project.label")
    String pageInfoProjectsTableHeaderProjectLabel();

    @Key("page.info.projects_table.header.applicable.label")
    String pageInfoProjectsTableHeaderApplicableLabel();

    @Key("page.with_text_editor.explore_macros")
    String pageWithTextEditorExploreMacros();

    @Key("page.arguments.title")
    String pageArgumentsTitle();

    @Key("page.arguments.tooltip")
    String pageArgumentsTooltip();

    @Key("page.preview_url.title")
    String pagePreviewUrlTitle();

    @Key("page.preview_url.tooltip")
    String pagePreviewUrlTooltip();
}
