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
package io.tonlabs;

import java.util.Arrays;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

/** Simple service that returns a static list of strings. */
@Path("json-example-completions/")
public class TonProjectCompletionService {

  /**
   * Returns a static list of completable keywords.
   *
   * @return list of keywords
   */
  @GET
  public List<String> completeKeywords() {
    return Arrays.asList("weight", "height", "address");
  }
}
