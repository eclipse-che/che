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
package org.eclipse.che.api.core;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.reverseOrder;
import static java.util.Collections.singleton;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.TreeSet;
import org.testng.annotations.Test;

/**
 * Tests for {@link Page}.
 *
 * @author Yevhenii Voevodin
 */
public class PageTest {

  @Test(
    expectedExceptions = IllegalArgumentException.class,
    expectedExceptionsMessageRegExp = "Required non-negative value of items before"
  )
  public void shouldThrowIllegalArgumentWhenItemsBeforeIsNegative() throws Exception {
    new Page<>(emptyList(), -1, 1, 10);
  }

  @Test(
    expectedExceptions = IllegalArgumentException.class,
    expectedExceptionsMessageRegExp = "Required positive value of page size"
  )
  public void shouldThrowIllegalArgumentWhenPageSizeIsNotPositive() throws Exception {
    new Page<>(emptyList(), 1, 0, 10);
  }

  @Test(
    expectedExceptions = IllegalArgumentException.class,
    expectedExceptionsMessageRegExp = "Required non-negative value of total items"
  )
  public void shouldThrowIllegalArgumentWhenTotalCountIsNegative() throws Exception {
    new Page<>(emptyList(), 1, 1, -1);
  }

  @Test(
    expectedExceptions = NullPointerException.class,
    expectedExceptionsMessageRegExp = "Required non-null items"
  )
  public void shouldThrownNPEWhenItemsListIsNull() throws Exception {
    new Page<>(null, 1, 1, 1);
  }

  @Test
  public void pageShouldBeEmptyWhenItIsCreatedWithAneEmptyItemsCollection() throws Exception {
    assertTrue(new Page<>(emptyList(), 1, 1, 1).isEmpty());
  }

  @Test
  public void testMiddleDataWindowPage() throws Exception {
    // item1. <= skipped                <- first page start
    // item2. <= skipped
    // item3. <- page start             <- first page end
    // item4.
    // item5. <- page end               <- last page start
    // item6.
    // item7.                           <- last page end
    final Page<String> page = new Page<>(asList("item3", "item4", "item5"), 2, 3, 7);

    assertFalse(page.isEmpty(), "non-empty page");
    assertEquals(page.getItemsCount(), 3, "items size ");
    assertEquals(page.getSize(), 3, "page size");
    assertEquals(page.getTotalItemsCount(), 7, "total elements count");

    final Page.PageRef firstRef = page.getFirstPageRef();
    assertEquals(firstRef.getPageSize(), 3, "first page size");
    assertEquals(firstRef.getItemsBefore(), 0, "first page skip items");

    final Page.PageRef lastRef = page.getLastPageRef();
    assertEquals(lastRef.getPageSize(), 3, "last page size");
    assertEquals(lastRef.getItemsBefore(), 6, "last page skip items");

    assertEquals(page.getNumber(), -1, "page number");

    assertFalse(page.hasPreviousPage(), "has previous page");

    assertFalse(page.hasNextPage(), "page has next page");

    assertEquals(page.getItems(), asList("item3", "item4", "item5"));
    assertEquals(page.getItems(i -> i.substring(4)), asList("3", "4", "5"));
    assertEquals(
        new ArrayList<>(page.fill(new TreeSet<>(reverseOrder()))),
        asList("item5", "item4", "item3"));
  }

  @Test
  public void testMiddlePage() throws Exception {
    //        item1.                      <- previous page start      <- first page start
    //        item2.
    //        item3.                      <- previous page end        <- first page end
    //        item4. <- page start
    //        item5.
    //        item6. <- page end
    //        item7.                      <- next page start
    //        item8.
    //        item9.                      <- next page end
    //        item.10                                                 <- last page start/end
    final Page<String> page = new Page<>(asList("item4", "item5", "item6"), 3, 3, 10);

    assertFalse(page.isEmpty(), "non-empty page");
    assertEquals(page.getItemsCount(), 3, "items size");
    assertEquals(page.getSize(), 3, "page size");
    assertEquals(page.getTotalItemsCount(), 10, "total elements count");

    final Page.PageRef firstRef = page.getFirstPageRef();
    assertEquals(firstRef.getPageSize(), 3, "first page size");
    assertEquals(firstRef.getItemsBefore(), 0, "first page skip items");

    final Page.PageRef lastRef = page.getLastPageRef();
    assertEquals(lastRef.getPageSize(), 3, "last page size");
    assertEquals(lastRef.getItemsBefore(), 9, "last page skip items");

    assertEquals(page.getNumber(), 2, "page number");

    assertTrue(page.hasPreviousPage(), "has previous page");
    final Page.PageRef prevRef = page.getPreviousPageRef();
    assertEquals(prevRef.getItemsBefore(), 0, "items before prev page");
    assertEquals(prevRef.getPageSize(), 3, "prev page size");

    assertTrue(page.hasNextPage(), "page has next page");
    final Page.PageRef nextRef = page.getNextPageRef();
    assertEquals(nextRef.getItemsBefore(), 6, "items before next page");
    assertEquals(nextRef.getPageSize(), 3, "next page size");

    assertEquals(page.getItems(), asList("item4", "item5", "item6"));
    assertEquals(page.getItems(i -> i.substring(4)), asList("4", "5", "6"));
    assertEquals(
        new ArrayList<>(page.fill(new TreeSet<>(reverseOrder()))),
        asList("item6", "item5", "item4"));
  }

  @Test
  public void testFirstPage() throws Exception {
    // item1. <- page start
    // item2.
    // item3.
    // item4.
    // item5. <- page end
    // item6.                           <- last page start
    // item7.                           <- last page end
    final Page<String> page =
        new Page<>(asList("item1", "item2", "item3", "item4", "item5"), 0, 5, 7);

    assertFalse(page.isEmpty(), "page is empty");
    assertEquals(page.getItemsCount(), 5, "items items count");
    assertEquals(page.getSize(), 5, "page size");
    assertEquals(page.getTotalItemsCount(), 7, "total items");

    final Page.PageRef firstRef = page.getFirstPageRef();
    assertEquals(firstRef.getPageSize(), 5, "first page size");
    assertEquals(firstRef.getItemsBefore(), 0, "first page skip items");

    final Page.PageRef lastRef = page.getLastPageRef();
    assertEquals(lastRef.getPageSize(), 5, "last page size");
    assertEquals(lastRef.getItemsBefore(), 5, "last page skip items");

    assertEquals(page.getNumber(), 1, "page number");

    assertFalse(page.hasPreviousPage(), "has previous page");

    assertTrue(page.hasNextPage(), "page has next page");
    final Page.PageRef nextRef = page.getNextPageRef();
    assertEquals(nextRef.getPageSize(), 5, "next page size");
    assertEquals(nextRef.getItemsBefore(), 5, "next page skip items");

    assertEquals(page.getItems(), asList("item1", "item2", "item3", "item4", "item5"));
  }

  @Test
  public void testLastPage() throws Exception {
    // item1.                   <- first page start
    // item2.
    // item3.                   <- first page end
    // item4.                   <- prev page start
    // item5.
    // item6.                   <- prev page end
    // item7. <- page start
    // item8. <- page end
    final Page<String> page = new Page<>(asList("item7", "item8"), 6, 3, 8);

    assertFalse(page.isEmpty(), "page is empty");
    assertEquals(page.getItemsCount(), 2, "items count");
    assertEquals(page.getSize(), 3, "page size");
    assertEquals(page.getTotalItemsCount(), 8, "total items");

    final Page.PageRef firstRef = page.getFirstPageRef();
    assertEquals(firstRef.getPageSize(), 3, "first page size");
    assertEquals(firstRef.getItemsBefore(), 0, "first page skip items");

    final Page.PageRef lastRef = page.getLastPageRef();
    assertEquals(lastRef.getPageSize(), 3, "last page size");
    assertEquals(lastRef.getItemsBefore(), 6, "last page skip items");

    assertEquals(page.getNumber(), 3, "page number");

    assertTrue(page.hasPreviousPage(), "has previous page");
    final Page.PageRef prevRef = page.getPreviousPageRef();
    assertEquals(prevRef.getPageSize(), 3, "prev page size");
    assertEquals(prevRef.getItemsBefore(), 3, "prev page skip items");

    assertFalse(page.hasNextPage(), "has next page");

    assertEquals(page.getItems(), asList("item7", "item8"));
  }

  @Test
  public void testSmallPage() throws Exception {
    final Page<String> page = new Page<>(singleton("item1"), 0, 1, 1);

    assertFalse(page.isEmpty(), "page is empty");
    assertEquals(page.getItemsCount(), 1, "items count");
    assertEquals(page.getSize(), 1, "page size");
    assertEquals(page.getTotalItemsCount(), 1, "total items");

    final Page.PageRef firstRef = page.getFirstPageRef();
    assertEquals(firstRef.getPageSize(), 1, "first page size");
    assertEquals(firstRef.getItemsBefore(), 0, "first page skip items");

    final Page.PageRef lastRef = page.getLastPageRef();
    assertEquals(lastRef.getPageSize(), 1, "last page size");
    assertEquals(lastRef.getItemsBefore(), 0, "last page skip items");

    assertEquals(page.getNumber(), 1, "page number");

    assertFalse(page.hasPreviousPage(), "has previous page");

    assertFalse(page.hasNextPage(), "page has next page");

    assertEquals(page.getItems(), singleton("item1"));
  }
}
