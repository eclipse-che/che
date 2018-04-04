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
package org.eclipse.che.api.git.params;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.api.git.shared.CheckoutRequest;

/**
 * Arguments holder for {@link org.eclipse.che.api.git.GitConnection#checkout(CheckoutParams)}.
 *
 * @author Igor Vinokur
 */
public class CheckoutParams {

  private List<String> filesToCheckout;
  private String name;
  private String startPoint;
  private String trackBranch;
  private boolean isCreateNew;
  private boolean noTrack;

  private CheckoutParams() {}

  /**
   * Create new {@link CheckoutParams} instance.
   *
   * @param name name of the branch to checkout
   */
  public static CheckoutParams create(String name) {
    return new CheckoutParams().withName(name);
  }

  /** @see CheckoutRequest#getName() */
  public String getName() {
    return name;
  }

  /** @see CheckoutRequest#withName(String) */
  public CheckoutParams withName(String name) {
    this.name = name;
    return this;
  }

  /** @see CheckoutRequest#getStartPoint() */
  public String getStartPoint() {
    return startPoint;
  }

  /** @see CheckoutRequest#withStartPoint(String) */
  public CheckoutParams withStartPoint(String startPoint) {
    this.startPoint = startPoint;
    return this;
  }

  /** @see CheckoutRequest#isCreateNew() */
  public boolean isCreateNew() {
    return isCreateNew;
  }

  /** @see CheckoutRequest#withCreateNew(boolean) */
  public CheckoutParams withCreateNew(boolean isCreateNew) {
    this.isCreateNew = isCreateNew;
    return this;
  }

  /** @see CheckoutRequest#getTrackBranch() */
  public String getTrackBranch() {
    return trackBranch;
  }

  /** @see CheckoutRequest#withTrackBranch(String) */
  public CheckoutParams withTrackBranch(String trackBranch) {
    this.trackBranch = trackBranch;
    return this;
  }

  /** @see CheckoutRequest#getFiles() */
  public List<String> getFiles() {
    return filesToCheckout == null ? new ArrayList<>() : filesToCheckout;
  }

  /** @see CheckoutRequest#withFiles(List) */
  public CheckoutParams withFiles(List<String> filesToCheckout) {
    this.filesToCheckout = filesToCheckout;
    return this;
  }

  /** @see CheckoutRequest#isNoTrack() */
  public boolean isNoTrack() {
    return noTrack;
  }

  /** @see CheckoutRequest#withNoTrack(boolean) */
  public CheckoutParams withNoTrack(boolean noTrack) {
    this.noTrack = noTrack;
    return this;
  }
}
