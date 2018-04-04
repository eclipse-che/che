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
package org.eclipse.che.ide.ext.gwt.client;

import com.google.gwt.i18n.client.Messages;

/**
 * Localized constants contained in 'GwtLocalizationConstants.properties'.
 *
 * @author Artem Zatsarynnyi
 */
public interface GwtLocalizationConstants extends Messages {

  /* GwtCommandPageView */
  @Key("view.gwtCommandPage.gwtModule.text")
  String gwtCommandPageViewGwtModuleText();

  @Key("view.gwtCommandPage.gwtModule.hint")
  String gwtCommandPageViewGwtModuleHint();

  @Key("view.gwtCommandPage.codeServerAddress.text")
  String gwtCommandPageViewCodeServerAddressText();
}
