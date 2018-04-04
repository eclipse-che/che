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
package org.eclipse.che.ltk.core.refactoring.participants;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant;

/** @author Evgen Vidolob */
public class CheRefactoringParticipantsRegistry {

  private static Map<String, Set<Class<? extends RefactoringParticipant>>> registry =
      new HashMap<>();

  public static void registerParticipant(String id, Class<? extends RefactoringParticipant> clazz) {
    if (!registry.containsKey(id)) {
      registry.put(id, new HashSet<>());
    }
    registry.get(id).add(clazz);
  }

  public static Set<Class<? extends RefactoringParticipant>> getParticipantsFor(String id) {
    Set<Class<? extends RefactoringParticipant>> classes = registry.get(id);
    if (classes == null) {
      classes = new HashSet<>(0);
    }
    return classes;
  }
}
