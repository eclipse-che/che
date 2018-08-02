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
package org.eclipse.che.plugin.pullrequest.client.workflow;

import static java.util.Objects.requireNonNull;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;

/**
 * Defines a chain of steps.
 *
 * @author Yevhenii Voevodin
 * @see ChainExecutor
 * @see ContributionWorkflow
 */
public final class StepsChain {

  /**
   * Creates a new {@code StepsChain} with the initial step.
   *
   * @param firstStep the first step in the chain
   * @return new chain instance
   * @throws NullPointerException when {@code firstStep} is null
   */
  public static StepsChain first(Step firstStep) {
    return new StepsChain().then(firstStep);
  }

  private final List<Step> steps;

  private StepsChain() {
    steps = new ArrayList<>();
  }

  /**
   * Returns the list of the steps currently added to this chain. The list is unmodifiable copy of
   * added steps, so next chain modification won't affect returned list.
   */
  public List<Step> getSteps() {
    return ImmutableList.copyOf(steps);
  }

  /**
   * Adds the step to the chain.
   *
   * @param step the next chain step
   * @throws NullPointerException when {@code step} is null
   */
  public StepsChain then(Step step) {
    steps.add(step);
    return this;
  }

  /**
   * Adds {@code stepIfTrue} to the chain, and executes added step only if supplier supplies true.
   *
   * @param supplier supplier of a boolean value
   * @param stepIfTrue step which should be added to the chain if expression is true
   * @return this chain instance
   * @throws NullPointerException when {@code stepIfTrue} is null
   */
  public StepsChain thenIf(Supplier<Boolean> supplier, Step stepIfTrue) {
    return then(new ExpressionStep(supplier, stepIfTrue));
  }

  /**
   * Adds all the steps from the {@code chain} to this chain.
   *
   * @param chain chain which steps should be added to this chain
   * @return this chain instance
   * @throws NullPointerException when {@code chain} is null
   */
  public StepsChain thenChain(StepsChain chain) {
    steps.addAll(requireNonNull(chain, "Expected non-null chain").getSteps());
    return this;
  }

  /**
   * Adds all the steps from the {@code chainIfTrue} chain to this chain with a boolean supplier.
   * Each added step will be executed only if supplier provides true value.
   *
   * @param supplier any boolean value or expression
   * @param chainIfTrue chain which steps should be added to this chain if expression is true
   * @return this chain instance
   * @throws NullPointerException when {@code chainIfTrue} is null
   */
  public StepsChain thenChainIf(Supplier<Boolean> supplier, StepsChain chainIfTrue) {
    final CachingSupplier cachingSupplier = new CachingSupplier(supplier);
    for (Step stepIfTrue : chainIfTrue.getSteps()) {
      thenIf(cachingSupplier, stepIfTrue);
    }
    return this;
  }

  /**
   * Adds all the steps from the {@code chainIfTrue} and {@code chainIfFalse} chains to this chain
   * with a opposite suppliers. If supplier supplies true then all the steps from the {@code
   * chainIfTrue} chain will be executed, otherwise all the steps from the chainIfFalse step will be
   * executed.
   *
   * @param supplier any boolean value or expression
   * @param chainIfTrue chain which steps should be added to this chain if expression is true
   * @param chainIfFalse chain which steps should be added to this chain if expression is false
   * @return this chain instance
   * @throws NullPointerException when either {@code chainIfTrue} or {@code chainIfFalse} is null
   */
  public StepsChain thenChainIf(
      Supplier<Boolean> supplier, StepsChain chainIfTrue, StepsChain chainIfFalse) {
    thenChainIf(supplier, chainIfTrue);
    thenChainIf(new NegateSupplier(supplier), chainIfFalse);
    return this;
  }

  public static StepsChain firstIf(Supplier<Boolean> condition, Step stepIfTrue) {
    return new StepsChain().thenIf(condition, stepIfTrue);
  }

  /**
   * Executes given step only if {@code supplier} provides true value, otherwise continues workflow
   * execution with {@link WorkflowExecutor#executeNextStep(Context)} method. This is helpful class
   * for defining steps which execution is based on the future condition.
   */
  private static class ExpressionStep implements SyntheticStep {

    final Supplier<Boolean> supplier;
    final Step stepIfTrue;

    ExpressionStep(Supplier<Boolean> supplier, Step stepIfTrue) {
      this.supplier = supplier;
      this.stepIfTrue = stepIfTrue;
    }

    @Override
    public void execute(WorkflowExecutor executor, Context context) {
      if (supplier.get()) {
        stepIfTrue.execute(executor, context);
      } else {
        executor.done(this, context);
      }
    }
  }

  /**
   * Supplier which caches {@code delegate.get()} value and reuses it. It allows chains to depend on
   * the other chain and future check.
   *
   * <p>For example:
   *
   * <pre>{@code
   * StepsChain.first(...)
   *           .thenChainIf(IS_AUTH_NEEDED, authChain);
   * }</pre>
   *
   * If {@code authChain} contains several steps those steps execution should depend only on the
   * {@code IS_AUTH_NEEDED} supplier result, and this result should be the same for all the next
   * chain steps.
   */
  private static class CachingSupplier implements Supplier<Boolean> {

    final Supplier<Boolean> delegate;

    Boolean cachedResult;

    CachingSupplier(Supplier<Boolean> delegate) {
      this.delegate = delegate;
    }

    @Override
    public Boolean get() {
      if (cachedResult == null) {
        cachedResult = delegate.get();
      }
      return cachedResult;
    }
  }

  /** Delegates the {@code delegate.get()} and negates its result. */
  private static class NegateSupplier implements Supplier<Boolean> {

    final Supplier<Boolean> delegate;

    NegateSupplier(Supplier<Boolean> delegate) {
      this.delegate = delegate;
    }

    @Override
    public Boolean get() {
      return !delegate.get();
    }
  }
}
