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
}
