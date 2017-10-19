/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2011 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
/*
 * Created on Apr 13, 2004
 *
 * TODO To change the template for this generated file go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
package org.eclipse.jdt.internal.ui.search;

import java.util.Set;
import org.eclipse.core.resources.IProject;

public class SearchParticipantsExtensionPoint {

  private Set<SearchParticipantDescriptor> fActiveParticipants = null;
  private static SearchParticipantsExtensionPoint fgInstance;

  //	public boolean hasAnyParticipants() {
  //		return
  // Platform.getExtensionRegistry().getConfigurationElementsFor(JavaSearchPage.PARTICIPANT_EXTENSION_POINT).length > 0;
  //	}

  //	private synchronized Set<SearchParticipantDescriptor> getAllParticipants() {
  //		if (fActiveParticipants != null)
  //			return fActiveParticipants;
  //		IConfigurationElement[] allParticipants =
  //
  //	Platform.getExtensionRegistry().getConfigurationElementsFor(JavaSearchPage.PARTICIPANT_EXTENSION_POINT);
  //		fActiveParticipants = new HashSet<SearchParticipantDescriptor>(allParticipants.length);
  //		for (int i = 0; i < allParticipants.length; i++) {
  //			SearchParticipantDescriptor descriptor = new SearchParticipantDescriptor(allParticipants[i]);
  //			IStatus status = descriptor.checkSyntax();
  //			if (status.isOK()) {
  //				fActiveParticipants.add(descriptor);
  //			} else {
  //				JavaPlugin.log(status);
  //			}
  //		}
  //		return fActiveParticipants;
  //	}
  //
  //	private void collectParticipants(Set<SearchParticipantRecord> participants, IProject[]
  // projects) {
  //		Iterator<SearchParticipantDescriptor> activeParticipants = getAllParticipants().iterator();
  //		Set<String> seenParticipants = new HashSet<String>();
  //		while (activeParticipants.hasNext()) {
  //			SearchParticipantDescriptor participant = activeParticipants.next();
  //			String id = participant.getID();
  //			if (participant.isEnabled() && !seenParticipants.contains(id)) {
  //				for (int i= 0; i < projects.length; i++) {
  //					try {
  //						if (projects[i].hasNature(participant.getNature())) {
  //							participants.add(new SearchParticipantRecord(participant, participant.create()));
  //							seenParticipants.add(id);
  //							break;
  //						}
  //					} catch (CoreException e) {
  //						JavaPlugin.log(e.getStatus());
  //						participant.disable();
  //					}
  //				}
  //			}
  //		}
  //	}

  public SearchParticipantRecord[] getSearchParticipants(IProject[] concernedProjects) {
    //		Set<SearchParticipantRecord> participantSet= new HashSet<SearchParticipantRecord>();
    //		collectParticipants(participantSet, concernedProjects);
    //		SearchParticipantRecord[] participants= new SearchParticipantRecord[participantSet.size()];
    //		return participantSet.toArray(participants);
    return new SearchParticipantRecord[0];
  }

  public static SearchParticipantsExtensionPoint getInstance() {
    if (fgInstance == null) fgInstance = new SearchParticipantsExtensionPoint();
    return fgInstance;
  }

  public static void debugSetInstance(SearchParticipantsExtensionPoint instance) {
    fgInstance = instance;
  }
}
