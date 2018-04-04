/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.command.explorer;

import com.google.gwt.i18n.client.Messages;

/**
 * I18n messages for the Command Explorer.
 *
 * @author Artem Zatsarynnyi
 */
public interface ExplorerMessages extends Messages {

  @Key("explorer.part.title")
  String partTitle();

  @Key("explorer.part.tooltip")
  String partTooltip();

  @Key("explorer.view.title")
  String viewTitle();

  @Key("explorer.message.unable_create")
  String unableCreate();

  @Key("explorer.message.unable_duplicate")
  String unableDuplicate();

  @Key("explorer.message.unable_remove")
  String unableRemove();

  @Key("explorer.remove_confirmation.title")
  String removeCommandConfirmationTitle();

  @Key("explorer.remove_confirmation.message")
  String removeCommandConfirmationMessage(String commandName);
}
