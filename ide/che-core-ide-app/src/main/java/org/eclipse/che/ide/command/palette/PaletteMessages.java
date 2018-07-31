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
package org.eclipse.che.ide.command.palette;

import com.google.gwt.i18n.client.Messages;

/**
 * I18n messages for the Commands Palette.
 *
 * @author Artem Zatsarynnyi
 */
public interface PaletteMessages extends Messages {

  @Key("action.show_palette.title")
  String actionShowPaletteTitle();

  @Key("action.show_palette.description")
  String actionShowPaletteDescription();

  @Key("view.title")
  String viewTitle();

  @Key("view.filter.placeholder")
  String filterPlaceholder();

  @Key("view.hint.text")
  String viewHintText();

  @Key("message.no_machine")
  String messageNoMachine();
}
