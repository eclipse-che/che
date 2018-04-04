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
package org.eclipse.che.ide.command.goal;

import com.google.gwt.i18n.client.Messages;

/**
 * I18n messages relate to the command goals.
 *
 * @author Artem Zatsarynnyi
 */
public interface GoalMessages extends Messages {

  @Key("message.goal_already_registered")
  String messageGoalAlreadyRegistered(String goalId);
}
