/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2005 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.ui.search;

import org.eclipse.jdt.ui.search.IQueryParticipant;

/** */
public class SearchParticipantRecord {
  private SearchParticipantDescriptor fDescriptor;
  private IQueryParticipant fParticipant;

  public SearchParticipantRecord(
      SearchParticipantDescriptor descriptor, IQueryParticipant participant) {
    super();
    fDescriptor = descriptor;
    fParticipant = participant;
  }

  /** @return Returns the descriptor. */
  public SearchParticipantDescriptor getDescriptor() {
    return fDescriptor;
  }

  /** @return Returns the participant. */
  public IQueryParticipant getParticipant() {
    return fParticipant;
  }
}
