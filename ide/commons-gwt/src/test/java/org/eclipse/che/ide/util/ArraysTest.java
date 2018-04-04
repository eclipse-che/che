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

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for the {@link Arrays}.
 *
 * @author Vlad Zhukovskyi
 */
public class ArraysTest {

  private static final Object O1 = new Object();
  private static final Object O2 = new Object();
  private static final Object O3 = new Object();

  @Test
  public void arrayShouldCheckForEmptiness() throws Exception {
    Assert.assertTrue(Arrays.isNullOrEmpty(new Object[] {}));
    Assert.assertTrue(Arrays.isNullOrEmpty(null));
  }

  @Test
  public void shouldCheckAddOperations() throws Exception {
    final Object[] arr1 = new Object[] {O1, O2};
    final Object[] arr2 = Arrays.add(arr1, O3);

    Assert.assertTrue(arr1.length == 2);
    Assert.assertTrue(arr2.length == 3);
    Assert.assertTrue(arr1 != arr2);
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldCheckFailedAddOperations() throws Exception {
    Arrays.add(null, new Object());
  }

  @Test
  public void shouldCheckContainsOperation() throws Exception {
    final Object[] arr1 = new Object[] {O1, O2};

    Assert.assertTrue(Arrays.contains(arr1, O1));
    Assert.assertTrue(Arrays.contains(arr1, O2));
    Assert.assertFalse(Arrays.contains(arr1, O3));
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldCheckFailedContainsOperations() throws Exception {
    Arrays.contains(null, new Object());
  }

  @Test
  public void shouldCheckIndexOfOperations() throws Exception {
    final Object[] arr1 = new Object[] {O1, O2};

    Assert.assertTrue(Arrays.indexOf(arr1, O1) == 0);
    Assert.assertTrue(Arrays.indexOf(arr1, O2) == 1);
    Assert.assertTrue(Arrays.indexOf(arr1, O3) == -1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldCheckFailedIndexOfOperations() throws Exception {
    Arrays.indexOf(null, new Object());
  }

  @Test
  public void shouldCheckRemoveOperations() throws Exception {
    final Object[] arr1 = new Object[] {O1, O2, O3};
    final Object[] arr2 = Arrays.remove(arr1, O3);

    Assert.assertTrue(arr1.length == 3);
    Assert.assertTrue(arr2.length == 2);
    Assert.assertTrue(arr1 != arr2);
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldCheckFailedRemoveOperations() throws Exception {
    Arrays.remove(null, new Object());
  }

  @Test
  public void shouldCheckRetainOperations() throws Exception {
    final Object[] arr1 = new Object[] {O1, O2};
    final Object[] arr2 = new Object[] {O2, O3};

    final Object[] result = Arrays.removeAll(arr1, arr2, true);

    Assert.assertTrue(result.length == 1);
    Assert.assertTrue(Arrays.indexOf(result, O1) == -1);
    Assert.assertTrue(Arrays.indexOf(result, O2) == 0);
    Assert.assertTrue(Arrays.indexOf(result, O3) == -1);
  }

  @Test
  public void shouldCheckRemoveAllOperations() throws Exception {
    final Object[] arr1 = new Object[] {O1, O2};
    final Object[] arr2 = new Object[] {O2, O3};

    final Object[] result = Arrays.removeAll(arr1, arr2, false);

    Assert.assertTrue(result.length == 1);
    Assert.assertTrue(Arrays.indexOf(result, O1) == 0);
    Assert.assertTrue(Arrays.indexOf(result, O2) == -1);
    Assert.assertTrue(Arrays.indexOf(result, O3) == -1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldCheckFailedRetainOperations() throws Exception {
    Arrays.removeAll(null, null, false);
  }
}
