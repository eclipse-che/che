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
package org.eclipse.che.ide.ui.smartTree.handler;

import com.google.gwt.event.shared.HandlerRegistration;
import java.util.HashSet;
import java.util.Set;

/**
 * Grouping handler registration.
 *
 * @author Vlad Zhukovskiy
 */
public class GroupingHandlerRegistration implements HandlerRegistration {

  private Set<HandlerRegistration> registrations;

  public GroupingHandlerRegistration() {
    registrations = new HashSet<>();
  }

  public void add(HandlerRegistration registration) {
    registrations.add(registration);
  }

  public Set<HandlerRegistration> getRegistrations() {
    return registrations;
  }

  public void removeHandler() {
    for (HandlerRegistration r : registrations) {
      r.removeHandler();
    }
    registrations.clear();
  }
}
