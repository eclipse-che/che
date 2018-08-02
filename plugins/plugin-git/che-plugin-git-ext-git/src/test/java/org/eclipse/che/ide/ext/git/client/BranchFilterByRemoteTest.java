/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.ext.git.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.che.api.git.shared.Branch;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

/** @author Sergii Leschenko */
@RunWith(MockitoJUnitRunner.class)
public class BranchFilterByRemoteTest {
  private static final String REMOTE_NAME = "origin";
  private static final String ANOTHER_REMOTE_NAME = "another";

  BranchFilterByRemote branchFilterByRemote;

  Branch branch = mock(Branch.class);

  @Before
  public void disarm() {
    branchFilterByRemote = new BranchFilterByRemote(REMOTE_NAME);
  }

  @Test
  public void shouldDetermineLinkBranchToRemote() throws Exception {
    when(branch.getName()).thenReturn("refs/remotes/" + REMOTE_NAME + "/master");
    assertTrue(branchFilterByRemote.isLinkedTo(branch));
  }

  @Test
  public void shouldNotDetermineLinkBranchToRemote() throws Exception {
    when(branch.getName()).thenReturn("refs/remotes/" + ANOTHER_REMOTE_NAME + "/master");
    assertFalse(branchFilterByRemote.isLinkedTo(branch));
  }

  @Test
  public void shouldDetermineSimpleBranchNameWhenBranchNameContainsRefs() throws Exception {
    String branchName = "master";
    when(branch.getName()).thenReturn("refs/remotes/" + REMOTE_NAME + "/" + branchName);

    assertEquals(branchFilterByRemote.getBranchNameWithoutRefs(branch), branchName);
  }

  @Test
  public void shouldJustReturnBranchNameWhenBranchNameContainsRefsToAnotherRemote()
      throws Exception {
    String branchName = "master";
    when(branch.getName()).thenReturn("refs/remotes/" + ANOTHER_REMOTE_NAME + "/" + branchName);

    assertNotEquals(branchFilterByRemote.getBranchNameWithoutRefs(branch), branchName);
  }

  @Test
  public void shouldDetermineSimpleBranchNameWhenBranchNameIsSimpleName() throws Exception {
    String branchName = "master";
    when(branch.getName()).thenReturn(branchName);

    assertEquals(branchFilterByRemote.getBranchNameWithoutRefs(branch), branchName);
  }
}
