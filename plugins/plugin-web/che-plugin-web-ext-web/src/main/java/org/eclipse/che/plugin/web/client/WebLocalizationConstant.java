/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

  @Key("action.newJavaScriptFile.title")
  String newJavaScriptFileActionTitle();

  @Key("action.newJavaScriptFile.description")
  String newJavaScriptFileActionDescription();

  @Key("action.previewHTML.title")
  String previewHTMLActionTitle();

  @Key("action.previewHTML.description")
  String previewHTMLActionDescription();
}
