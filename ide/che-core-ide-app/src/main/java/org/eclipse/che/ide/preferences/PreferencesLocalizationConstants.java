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
package org.eclipse.che.ide.preferences;

import com.google.gwt.i18n.client.Messages;

public interface PreferencesLocalizationConstants extends Messages {

  @Key("ide.general.theme-label")
  String ideGeneralThemeLabel();

  @Key("ide.general.ask-before-closing-tab-label")
  String ideGeneralAskBeforeClosingTabLabel();
}
