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
package org.eclipse.che.plugin.product.info.client;

import com.google.gwt.i18n.client.Messages;

/**
 * Che product information constant.
 *
 * @author Vitalii Parfonov
 * @author Oleksii Orel
 */
public interface LocalizationConstant extends Messages {

  @Key("che.tab.title")
  String cheTabTitle();

  @Key("che.tab.title.with.workspace.name")
  String cheTabTitle(String workspaceName);

  @Key("get.support.link")
  String getSupportLink();

  @Key("get.product.name")
  String getProductName();

  @Key("support.title")
  String supportTitle();

  @Key("support.public.chat")
  String publicChat();

  @Key("support.public.chat.url")
  String publicChatUrl();

  @Key("support.report.issue")
  String issueTracker();

  @Key("support.report.issue.url")
  String issueTrackerUrl();
}
