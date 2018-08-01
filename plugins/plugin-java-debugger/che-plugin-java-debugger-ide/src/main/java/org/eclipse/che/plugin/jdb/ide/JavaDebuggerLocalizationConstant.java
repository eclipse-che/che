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
package org.eclipse.che.plugin.jdb.ide;

/**
 * I18n constants for the Debugger extension.
 *
 * @author Artem Zatsarynnyi
 */
public interface JavaDebuggerLocalizationConstant extends com.google.gwt.i18n.client.Messages {

  /* JavaDebugConfigurationPage */
  @Key("view.javaDebugConfigurationPage.notice")
  String javaDebugConfigurationPageViewNotice();

  @Key("view.javaDebugConfigurationPage.devHostCheckbox")
  String javaDebugConfigurationPageViewDevHostCheckbox();

  @Key("view.javaDebugConfigurationPage.hostLabel")
  String javaDebugConfigurationPageViewHostLabel();

  @Key("view.javaDebugConfigurationPage.portLabel")
  String javaDebugConfigurationPageViewPortLabel();
}
