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
package org.eclipse.che.api.core;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * Defines paged result of data selection, it is rather dynamic data window than regular page, as it
 * refers to the specified selection region based on the items before and the page size, but not on
 * the page number and page size.
 *
 * <p>Examples:
 *
 * <p>Regular page.<br>
 * For page input arguments {@code skipItems = 3}, {@code pageSize = 3} and {@code totalCount = 10}
 * the page is normalized and will refer to the second page which is(item4, item5, item6):
 *
 * <pre>
 *     item1.                      <- previous page start      <- first page start
 *     item2.
 *     item3.                      <- previous page end        <- first page end
 *     item4. <- page start
 *     item5.
 *     item6. <- page end
 *     item7.                      <- next page start
 *     item8.
 *     item9.                      <- next page end
 *     item.10                                                  <- last page start/end
 * </pre>
 *
 * <ul>
 *   Result page:
 *   <li>Non-empty
 *   <li>Contains 3 items(item4, item5, item6)
 *   <li>Has the previous page(item1, item2, item3)
 *   <li>Has the next page(item7, item8, item9)
 * </ul>
 *
 * <p>Data window.<br>
 * For page input arguments {@code skipItems = 2}, {@code pageSize = 3} and {@code totalCount = 7}
 * the page will refer to the following data window:
 *
 * <pre>
 *     item1.                   <- first page start
 *     item2.
 *     item3. <- page start     <- first page end
 *     item4.
 *     item5. <- page end
 *     item6.
 *     item7.                    <- last page start/end
 * </pre>
 *
 * <ul>
 *   Result page:
 *   <li>Non-empty
 *   <li>Contains 3 items(item3, item4, item5)
 *   <li>Doesn't have the previous page(because page refers to the elements which are partially
 *       present in the first page(item3) and the second page(item4, item5)
 *   <li>Doesn't have the next page(the reason is the same to previous statement)
 * </ul>
 *
 * <p>Note that all {@code Page} instances perform reference calculations based on the given {@code
 * itemsBefore} and {@code pageSize} values which means that implementor is responsible for
 * providing correct bounds and data management.
 *
 * <p>The instances of this class are <b>NOT thread safe</b>.
 *
 * @param <ITEM_T> the type of the page items
 * @author Yevhenii Voevodin
 */
public class Page<ITEM_T> {

  private final int pageSize;
  private final long itemsBefore;
  private final long totalCount;
  private final List<ITEM_T> items;

  /**
   * Creates a new page.
   *
   * @param items page items
   * @param itemsBefore items count before this page
   * @param pageSize page size
   * @param totalCount count of all the items
   * @throws NullPointerException when {@code items} collection is null
   * @throws IllegalArgumentException when {@code itemsBefore} is negative
   * @throws IllegalArgumentException when {@code pageSize} is non-positive
   * @throws IllegalArgumentException when {@code totalCount} is negative
   */
  public Page(Collection<? extends ITEM_T> items, long itemsBefore, int pageSize, long totalCount) {
    requireNonNull(items, "Required non-null items");
    this.items = new ArrayList<>(items);
    checkArgument(itemsBefore >= 0, "Required non-negative value of items before");
    this.itemsBefore = itemsBefore;
    checkArgument(pageSize > 0, "Required positive value of page size");
    this.pageSize = pageSize;
    checkArgument(totalCount >= 0, "Required non-negative value of total items");
    this.totalCount = totalCount;
  }

  /** Returns true whether this page doesn't contain items, returns false if it does. */
  public boolean isEmpty() {
    return items.isEmpty();
  }

  /**
   * Returns true when the current page has the next page, otherwise when the page is the last page
   * false will be returned.
   */
  public boolean hasNextPage() {
    return getNumber() != -1 && itemsBefore + pageSize < totalCount;
  }

  /**
   * Returns true when this page has the previous page, otherwise when the page is the first page
   * false will be returned.
   */
  public boolean hasPreviousPage() {
    return getNumber() != -1 && itemsBefore != 0;
  }

  /**
   * Returns a reference to the next page.
   *
   * <p>Note: This method was designed to be used in couple with {@link #hasNextPage()}. Returns
   * reference to the next page even when {@link #hasNextPage()} returns false.
   */
  public PageRef getNextPageRef() {
    return new PageRef(itemsBefore + pageSize, pageSize);
  }

  /**
   * Returns a reference to the previous page.
   *
   * <p>Note: This method was designed to be used in couple with {@link #hasPreviousPage()}. Returns
   * reference to the first page when {@link #hasPreviousPage()} returns false.
   */
  public PageRef getPreviousPageRef() {
    final long skipItems = itemsBefore <= pageSize ? 0 : itemsBefore - pageSize;
    return new PageRef(skipItems, pageSize);
  }

  /** Returns the reference to the last page. */
  public PageRef getLastPageRef() {
    final long lastPageItems = totalCount % pageSize;
    if (lastPageItems == 0) {
      return new PageRef(totalCount <= pageSize ? 0 : totalCount - pageSize, pageSize);
    }
    return new PageRef(totalCount - lastPageItems, pageSize);
  }

  /** Returns the reference to the first page. */
  public PageRef getFirstPageRef() {
    return new PageRef(0, pageSize);
  }

  /**
   * Returns the size of the current page.
   *
   * <p>Returned value is always positive and greater or equal to the value returned by the {@link
   * #getItemsCount()} method.
   */
  public int getSize() {
    return pageSize;
  }

  /**
   * Returns page number starting from 1.
   *
   * <p>If the page is not regular page(it refers rather to the data window than to the certain
   * page(e.g. skip=2, pageSize=4)) then this method returns -1.
   */
  public long getNumber() {
    if (itemsBefore % pageSize != 0) {
      return -1;
    }
    return itemsBefore / pageSize + 1;
  }

  /**
   * Returns the size of the page items, returned value may be equal to 0 when the page {@link
   * #isEmpty() is empty}, the values is the same to {@code page.getItems().size()}.
   */
  public int getItemsCount() {
    return items.size();
  }

  /** Returns the count of all the items. */
  public long getTotalItemsCount() {
    return totalCount;
  }

  /**
   * Returns page items or an empty list when page doesn't contain items.
   *
   * <p>Note that returned instance is modifiable list and modification applied on that list will
   * affect the origin page result items, which allows components to modify items before propagating
   * page.
   */
  public List<ITEM_T> getItems() {
    return items;
  }

  /**
   * Gets the page items and maps them with given {@code mapper}.
   *
   * @param mapper items mapper
   * @param <R> the type of the result items
   * @return the list of mapped items
   */
  public <R> List<R> getItems(Function<? super ITEM_T, ? extends R> mapper) {
    requireNonNull(mapper, "Required non-null mapper for page items");
    return items.stream().map(mapper::apply).collect(toList());
  }

  /**
   * Fills the given collection with page items. This method may be convenient when needed
   * collection different from the {@link List}.
   *
   * <p>The common example:
   *
   * <pre>{@code
   * Set<User> user = page.fill(new TreeSet<>(comparator));
   * }</pre>
   *
   * <p>Note that this method uses {@code container.addAll(items)} so be aware of putting modifiable
   * collection.
   *
   * @param container collection which is used to fill result into
   * @param <COL_T> collection type
   * @return given collection instance {@code container} with items filled
   * @throws NullPointerException when {@code container} is null
   */
  public <COL_T extends Collection<? super ITEM_T>> COL_T fill(COL_T container) {
    requireNonNull(container, "Required non-null items container");
    container.addAll(items);
    return container;
  }

  /** Represents page reference as a combination of {@code skipItems & pageSize}. */
  public static class PageRef {
    private final long skipItems;
    private final int pageSize;

    private PageRef(long skipItems, int pageSize) {
      this.skipItems = skipItems;
      this.pageSize = pageSize;
    }

    public long getItemsBefore() {
      return skipItems;
    }

    public int getPageSize() {
      return pageSize;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (!(obj instanceof PageRef)) {
        return false;
      }
      final PageRef that = (PageRef) obj;
      return skipItems == that.skipItems && pageSize == that.pageSize;
    }

    @Override
    public int hashCode() {
      int hash = 7;
      hash = 31 * hash + Long.hashCode(skipItems);
      hash = 31 * hash + pageSize;
      return hash;
    }
  }
}
