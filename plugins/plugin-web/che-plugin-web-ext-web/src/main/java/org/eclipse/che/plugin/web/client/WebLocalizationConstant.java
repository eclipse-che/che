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
package org.eclipse.che.plugin.web.client;

import com.google.gwt.i18n.client.Messages;

/**
 * I18n constants for the Web extension.
 *
 * @author Artem Zatsarynnyi
 */
public interface WebLocalizationConstant extends Messages {

  @Key("action.newCssFile.title")
  String newCssFileActionTitle();

  @Key("action.newCssFile.description")
  String newCssFileActionDescription();

  @Key("action.newLessFile.title")
  String newLessFileActionTitle();

  @Key("action.newLessFile.description")
  String newLessFileActionDescription();

  @Key("action.newHtmlFile.title")
  String newHtmlFileActionTitle();

  @Key("action.newHtmlFile.description")
  String newHtmlFileActionDescription();

  @Key("action.newVueFile.title")
  String newVueFileActionTitle();

  @Key("action.newVueFile.description")
  String newVueFileActionDescription();

  @Key("action.newJavaScriptFile.title")
  String newJavaScriptFileActionTitle();

  @Key("action.newJavaScriptFile.description")
  String newJavaScriptFileActionDescription();

  @Key("action.previewHTML.title")
  String previewHTMLActionTitle();

  @Key("action.previewHTML.description")
  String previewHTMLActionDescription();
}
