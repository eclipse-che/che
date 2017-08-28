/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.jsonexample;

import java.util.Arrays;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

/** Simple service that returns a static list of strings. */
@Path("json-example-completions/")
public class JsonExampleCompletionService {

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
