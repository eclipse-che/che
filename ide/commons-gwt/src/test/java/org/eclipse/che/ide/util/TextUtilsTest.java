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
package org.eclipse.che.ide.util;

import static java.nio.charset.Charset.defaultCharset;
import static org.junit.Assert.assertEquals;

import com.google.common.hash.Hashing;
import org.junit.Test;

/** @author Valeriy Svydenko */
public class TextUtilsTest {
  private static final String TEXT = "to be or not to be";

  @Test
  public void textShouldBeEncodedInMD5Hash() {
    assertEquals(TextUtils.md5(TEXT), Hashing.md5().hashString(TEXT, defaultCharset()).toString());
  }
}
