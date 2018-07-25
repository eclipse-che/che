/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.java.server.che;

import org.eclipse.che.plugin.java.server.CodeAssist;
import org.junit.Ignore;
import org.junit.Test;

/** @author Evgen Vidolob */
// TODO: rework after new Project API
@Ignore
public class CodeAssistantTest extends BaseTest {
  @Test
  public void testFirst() throws Exception {
    StringBuilder b = new StringBuilder("package org.eclipse.che.test;\n");
    b.append("public class MyClass {\n");
    b.append("  public MyClass(int i){\n");
    b.append("   i\n");
    b.append("}\n}");
    int offset = b.indexOf("   i");
    CodeAssist codeAssist = new CodeAssist();
    //        Proposals proposals = codeAssist.computeProposals(project,
    // "org.eclipse.che.test.MyClass", offset, b.toString());
    //        assertThat(proposals).isNotNull();
    //        assertThat(proposals.getProposals()).isNotEmpty();
  }
}
