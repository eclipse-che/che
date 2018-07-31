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
package org.eclipse.che.ide.api.editor;

import com.google.gwt.i18n.client.Messages;

/**
 * I18n Constants for the Editor module.
 *
 * @author "Mickaël Leduque"
 */
public interface EditorLocalizationConstants extends Messages {

  @DefaultMessage("Close")
  String askWindowCloseTitle();

  @DefaultMessage("{0} has been modified. Save changes?")
  String askWindowSaveChangesMessage(String name);

  @DefaultMessage(
      "An error occurred while initializing the editor.\nReloading the page may be necessary.")
  String editorInitErrorMessage();

  @DefaultMessage("An error occurred while loading the file.")
  String editorFileErrorMessage();

  @DefaultMessage("Finishing editor initialization")
  String waitEditorInitMessage();

  @DefaultMessage("Failed to update content of file(s)")
  String failedToUpdateContentOfFiles();

  @DefaultMessage("Tabs")
  String tabsPropertiesSection();

  @DefaultMessage("Edit")
  String tabsEditSection();

  @DefaultMessage("Typing")
  String typingPropertiesSection();

  @DefaultMessage("White spaces")
  String whiteSpacesPropertiesSection();

  @DefaultMessage("Rulers")
  String rulersPropertiesSection();

  @DefaultMessage("Language tools")
  String languageToolsPropertiesSection();

  @DefaultMessage("Expand Tab")
  String propertyExpandTab();

  @DefaultMessage("Tab Size")
  String propertyTabSize();

  @DefaultMessage("Soft Wrap")
  String propertySoftWrap();

  @DefaultMessage("Enable Autosave")
  String propertyAutoSave();

  @DefaultMessage("Autopair (Parentheses)")
  String propertyAutoPairParentheses();

  @DefaultMessage("Autopair Braces")
  String propertyAutoPairBraces();

  @DefaultMessage("Autopair [Square] Brackets")
  String propertyAutoPairSquareBrackets();

  @DefaultMessage("Autopair <Angle> Brackets")
  String propertyAutoPairAngelBrackets();

  @DefaultMessage("Autopair \"Quotations\"")
  String propertyAutoPairQuotations();

  @DefaultMessage("Autocomplete /** Block Comments */")
  String propertyAutoCompleteComments();

  @DefaultMessage("Smart Indentation")
  String propertySmartIndentation();

  @DefaultMessage("Show Whitespace Characters")
  String propertyShowWhitespaces();

  @DefaultMessage("Show Annotation Ruler")
  String propertyShowAnnotationRuler();

  @DefaultMessage("Show Line Number Ruler")
  String propertyShowLineNumberRuler();

  @DefaultMessage("Show Folding Ruler")
  String propertyShowFoldingRuler();

  @DefaultMessage("Show Overview Ruler")
  String propertyShowOverviewRuler();

  @DefaultMessage("Show Zoom Ruler")
  String propertyShowZoomRuler();

  @DefaultMessage("Show Occurrences")
  String propertyShowOccurrences();

  @DefaultMessage("Show Content Assist automatically")
  String propertyShowContentAssistAutomatically();
}
