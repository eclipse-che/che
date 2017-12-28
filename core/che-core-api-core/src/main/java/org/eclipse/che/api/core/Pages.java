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
package org.eclipse.che.api.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.eclipse.che.api.core.Page.PageRef;

/**
 * Static utility methods to interact with page suppliers.
 *
 * @author Yevhenii Voevodin
 */
public final class Pages {

  /** An experimental value used as default page size where necessary. */
  public static final int DEFAULT_PAGE_SIZE = 50;

  /**
   * Defines an interface for page supplier.
   *
   * @param <E> the type of the element held by page
   * @param <X> the type of exception thrown by page supplier
   */
  @FunctionalInterface
  public interface PageSupplier<E, X extends Exception> {

    /**
     * Gets a single page.
     *
     * @param maxItems max items to retrieve
     * @param skipCount how many elements to skip
     * @return page
     * @throws X exception thrown by supplier
     */
    Page<? extends E> getPage(int maxItems, long skipCount) throws X;
  }

  /**
   * Eagerly fetches all the elements page by page and returns a stream of them.
   *
   * @param supplier page supplier
   * @param size how many items to retrieve per page
   * @param <E> the type of the element held by page
   * @param <X> the type of exception thrown by page supplier
   * @return stream of fetched elements
   * @throws X when supplier throws exception
   */
  public static <E, X extends Exception> Stream<E> stream(PageSupplier<E, X> supplier, int size)
      throws X {
    return eagerFetch(supplier, size).stream();
  }

  /**
   * Fetches elements like {@link #stream(PageSupplier, int)} method does using default page size
   * which is equal to {@value #DEFAULT_PAGE_SIZE}.
   */
  public static <E, X extends Exception> Stream<E> stream(PageSupplier<E, X> supplier) throws X {
    return stream(supplier, DEFAULT_PAGE_SIZE);
  }

  /**
   * Eagerly fetches all the elements page by page and returns an iterable of them.
   *
   * @param supplier pages supplier
   * @param size how many items to retrieve per page
   * @param <E> the type of the element held by page
   * @param <X> the type of exception thrown by page supplier
   * @return iterable of fetched elements
   * @throws X when supplier throws exception
   */
  public static <E, X extends Exception> Iterable<E> iterate(PageSupplier<E, X> supplier, int size)
      throws X {
    return eagerFetch(supplier, size);
  }

  /**
   * Fetches elements like {@link #iterate(PageSupplier, int)} method does using default page size
   * which is equal to {@value #DEFAULT_PAGE_SIZE}.
   */
  public static <E, X extends Exception> Iterable<E> iterate(PageSupplier<E, X> supplier) throws X {
    return iterate(supplier, DEFAULT_PAGE_SIZE);
  }

  /**
   * Returns a stream which is based on lazy fetching paged iterator.
   *
   * @param supplier pages supplier
   * @param size how many items to retrieve per page
   * @param <E> the type of the element held by page
   * @param <X> exception thrown by supplier
   * @return stream of elements
   * @throws RuntimeException wraps any exception occurred during pages fetch
   */
  public static <E, X extends Exception> Stream<E> streamLazily(
      PageSupplier<E, X> supplier, int size) {
    return StreamSupport.stream(new PagedIterable<>(supplier, size).spliterator(), false);
  }

  /**
   * Fetches elements like {@link #streamLazily(PageSupplier, int)} method does using default page
   * size which is equal to {@value #DEFAULT_PAGE_SIZE}.
   */
  public static <E, X extends Exception> Stream<E> streamLazily(PageSupplier<E, X> supplier) {
    return streamLazily(supplier, DEFAULT_PAGE_SIZE);
  }

  /**
   * Returns an iterable which iterator lazily fetches page by page, doesn't poll the next page
   * until the last item from previous page is not processed. The first page is polled while
   * iterable is created.
   *
   * @param supplier pages supplier
   * @param size how many items to retrieve per page
   * @param <E> the type of the element held by page
   * @param <X> the type of exception thrown by page supplier
   * @return stream of elements
   * @throws RuntimeException wraps any exception occurred during pages fetch
   */
  public static <E, X extends Exception> Iterable<E> iterateLazily(
      PageSupplier<E, X> supplier, int size) {
    return new PagedIterable<>(supplier, size);
  }

  /**
   * Returns an iterable like {@link #streamLazily(PageSupplier, int)} method does using default
   * page size which is equal to {@value #DEFAULT_PAGE_SIZE}.
   */
  public static <E, X extends Exception> Iterable<E> iterateLazily(PageSupplier<E, X> supplier) {
    return iterateLazily(supplier, DEFAULT_PAGE_SIZE);
  }

  private static <E, X extends Exception> List<E> eagerFetch(PageSupplier<E, X> supplier, int size)
      throws X {
    Page<? extends E> page = supplier.getPage(size, 0);
    ArrayList<E> container =
        new ArrayList<>(page.hasNextPage() ? page.getItemsCount() * 2 : page.getItemsCount());
    while (page.hasNextPage()) {
      container.addAll(page.getItems());
      PageRef next = page.getNextPageRef();
      page = supplier.getPage(next.getPageSize(), next.getItemsBefore());
    }
    container.addAll(page.getItems());
    return container;
  }

  private static class PagedIterable<E> implements Iterable<E> {

    private final PageSupplier<E, ?> supplier;
    private final int size;

    private PagedIterable(PageSupplier<E, ?> supplier, int size) {
      this.supplier = supplier;
      this.size = size;
    }

    @Override
    public Iterator<E> iterator() {
      return new PagedIterator<>(supplier, size);
    }
  }

  private static class PagedIterator<E> implements Iterator<E> {

    private final PageSupplier<E, ?> supplier;
    private final int size;

    private Page<? extends E> page;
    private Iterator<? extends E> delegate;

    private PagedIterator(PageSupplier<E, ?> supplier, int size) {
      this.supplier = supplier;
      this.size = size;
      fetchPage(0);
    }

    @Override
    public boolean hasNext() {
      if (delegate.hasNext()) {
        return true;
      }
      if (!page.hasNextPage()) {
        return false;
      }
      fetchPage(page.getNextPageRef().getItemsBefore());
      return delegate.hasNext();
    }

    @Override
    public E next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }
      return delegate.next();
    }

    private void fetchPage(long skip) {
      try {
        page = supplier.getPage(size, skip);
        delegate = page.getItems().iterator();
      } catch (Exception x) {
        throw new RuntimeException(x.getMessage(), x);
      }
    }
  }

  private Pages() {}
}
