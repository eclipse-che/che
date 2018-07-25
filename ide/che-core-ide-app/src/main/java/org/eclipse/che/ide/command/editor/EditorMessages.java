/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
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

  @Key("button.test.text")
  String buttonRunText();

  @Key("button.save.text")
  String buttonSaveText();

  @Key("button.cancel.text")
  String buttonCancelText();

  @Key("page.name.title")
  String pageNameTitle();

  @Key("page.command_line.title")
  String pageCommandLineTitle();

  @Key("page.goal.title")
  String pageGoalTitle();

  @Key("page.goal.new_goal.title")
  String pageGoalNewGoalTitle();

  @Key("page.goal.new_goal.label")
  String pageGoalNewGoalLabel();

  @Key("page.goal.new_goal.button.create")
  String pageGoalNewGoalButtonCreate();

  @Key("page.goal.new_goal.already_exists.message")
  String pageGoalNewGoalAlreadyExistsMessage(String newGoalName);

  @Key("page.projects.title")
  String pageProjectsTitle();

  @Key("page.projects.table.header.project.label")
  String pageProjectsTableHeaderProjectLabel();

  @Key("page.projects.table.header.applicable.label")
  String pageProjectsTableHeaderApplicableLabel();

  @Key("page.with_text_editor.macros")
  String pageWithTextEditorMacros();

  @Key("page.preview_url.title")
  String pagePreviewUrlTitle();
}
