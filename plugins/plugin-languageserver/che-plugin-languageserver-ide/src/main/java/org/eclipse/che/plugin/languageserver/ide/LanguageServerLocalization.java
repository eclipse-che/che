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
package org.eclipse.che.plugin.languageserver.ide;

import com.google.gwt.i18n.client.Messages;

/** @author Evgen Vidolob */
public interface LanguageServerLocalization extends Messages {

  @Key("go.to.symbol.action.title")
  String goToSymbolActionTitle();

  @Key("go.to.symbol.action.description")
  String goToSymbolActionDescription();

  @Key("go.to.symbol.symbols")
  String goToSymbolSymbols(int num);

  @Key("apply.workspace.edit.action.notification.title")
  String applyWorkspaceActionNotificationTitle();

  @Key("apply.workspace.edit.action.notification.done")
  String applyWorkspaceActionNotificationDone();

  @Key("apply.workspace.edit.action.notification.undoing")
  String applyWorkspaceActionNotificationUndoing();

  @Key("apply.workspace.edit.action.notification.undone")
  String applyWorkspaceActionNotificationUndone();

  @Key("apply.workspace.edit.action.notification.undo.failed")
  String applyWorkspaceActionNotificationUndoFailed();

  @Key("apply.workspace.edit.action.notification.modifying")
  String applyWorkspaceActionNotificationModifying(String uri);

  @Key("modules.type")
  String modulesType(int p0);

  @Key("class.type")
  String classType(int p0);

  @Key("interface.type")
  String interfaceType(int p0);

  @Key("method.type")
  String methodType(int p0);

  @Key("function.type")
  String functionType(int p0);

  @Key("property.type")
  String propertyType(int p0);

  @Key("variable.type")
  String variableType(int p0);

  @Key("constructor.type")
  String constructorType(int p0);

  @Key("find.symbol.action.title")
  String findSymbolActionTitle();

  @Key("rename.action.title")
  String renameActionTitle();

  @Key("rename.view.title")
  String renameViewTitle();

  @Key("rename.view.tooltip")
  String renameViewTooltip();

  @Key("rename.dialog.label")
  String renameDialogLabel();

  @Key("rename.dialog.preview.label")
  String renameDialogPreviewLabel();

  @Key("rename.view.cancel.label")
  String renameViewCancelLabel();

  @Key("rename.view.do.rename.label")
  String renameViewDoRenameLabel();
}
