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
package org.eclipse.che.workspace.infrastructure.kubernetes.server.external;

import static org.testng.Assert.*;

import java.util.Collections;
import org.testng.annotations.Test;

public class SingleHostIngressExternalServerExposerTest {

  @Test
  public void testDemanglePath() {
    SingleHostIngressExternalServerExposer exposer =
        new SingleHostIngressExternalServerExposer(Collections.emptyMap(), null, "%s(/?.*)");

    String demangledPath = exposer.demanglePath(null, "/path/subpath(/?.*)");

    assertEquals(demangledPath, "/path/subpath");
  }
}
