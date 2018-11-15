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
package org.eclipse.che.commons.tracing;

import com.google.common.annotations.Beta;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.tag.BooleanTag;
import io.opentracing.tag.IntTag;
import io.opentracing.tag.StringTag;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A utility class for creating simple traces. Importantly, only string, boolean and integer tags
 * are supported.
 *
 * <pre>{@code
 * Traces.using(tracer)
 *     .create("operation")
 *     .tagged(tag(CheTags.WORKSPACE_ID, "wsid"), tag(CheTags.MACHINE_NAME, "theia"))
 *     .calling(() -> {
 *       ... code that is now traced ...
 *     });
 * }</pre>
 */
@Beta
public class Traces {

  private Traces() {
    throw new AssertionError("This class cannot be instantiated.");
  }

  public static TraceBuilder using(Tracer tracer) {
    return new TraceBuilder(tracer);
  }

  /**
   * A helper method for creating a tag value that can be supplied to
   * {@link SpanProducer#tagged(TagValue...)} or {@link TraceBuilder#create(String, TagValue...)}.
   * This method is supposed to be statically imported, see the example in the class description.
   *
   * @param tag the tag to apply to the span being created
   * @param value the value of the tag
   * @return a tag value tuple
   * @see Traces
   */
  public static TagValue tag(StringTag tag, String value) {
    return TagValue.string(tag, value);
  }

  /**
   * A helper method for creating a tag value that can be supplied to
   * {@link SpanProducer#tagged(TagValue...)} or {@link TraceBuilder#create(String, TagValue...)}.
   * This method is supposed to be statically imported, see the example in the class description.
   *
   * @param tag the tag to apply to the span being created
   * @param value the value of the tag
   * @return a tag value tuple
   * @see Traces
   */
  public static TagValue tag(BooleanTag tag, Boolean value) {
    return TagValue.bool(tag, value);
  }

  /**
   * A helper method for creating a tag value that can be supplied to
   * {@link SpanProducer#tagged(TagValue...)} or {@link TraceBuilder#create(String, TagValue...)}.
   * This method is supposed to be statically imported, see the example in the class description.
   *
   * @param tag the tag to apply to the span being created
   * @param value the value of the tag
   * @return a tag value tuple
   * @see Traces
   */
  public static TagValue tag(IntTag tag, Integer value) {
    return TagValue.integer(tag, value);
  }

  /**
   * A helper interface to {@link SpanProducer#calling(ThrowingRunnable)} to accept exception
   * throwing lambdas.
   *
   * @param <E> the type of the exception thrown
   */
  public interface ThrowingRunnable<E extends Throwable> {

    void run() throws E;
  }

  /**
   * A helper interface to {@link SpanProducer#calling(ThrowingSupplier)} to accept exception
   * throwing lambdas.
   *
   * @param <T> the type of the produced value
   * @param <E> the type of the exception thrown
   */
  public interface ThrowingSupplier<T, E extends Throwable> {
    T get() throws E;
  }

  public static final class TraceBuilder {

    private final Tracer tracer;

    private TraceBuilder(Tracer tracer) {
      this.tracer = tracer;
    }

    /**
     * Creates a {@link SpanProducer} that will create a span with the provided name.
     * @param operationName the name of the operation covered by the span being created
     */
    public SpanProducer create(String operationName, TagValue... tags) {
      return new SpanProducer(tracer, operationName, tags);
    }
  }

  /**
   * A builder for the spans. Can apply tags and create a span around a lambda, see {@link Traces}
   * for an example.
   */
  public static final class SpanProducer {

    private final Tracer tracer;
    private final String operationName;
    private final List<TagValue> tags;

    private SpanProducer(Tracer tracer, String operationName, TagValue[] tags) {
      this.tracer = tracer;
      this.operationName = operationName;
      this.tags = new ArrayList<>(Arrays.asList(tags));
    }

    public SpanProducer tagged(StringTag tag, String value) {
      tags.add(TagValue.string(tag, value));
      return this;
    }

    public SpanProducer tagged(BooleanTag tag, Boolean value) {
      tags.add(TagValue.bool(tag, value));
      return this;
    }

    public SpanProducer tagged(IntTag tag, Integer value) {
      tags.add(TagValue.integer(tag, value));
      return this;
    }

    public SpanProducer tagged(TagValue... tags) {
      this.tags.addAll(Arrays.asList(tags));
      return this;
    }

    /**
     * Given the provided tracer, operation name and tags, this method wraps the provided action
     * in a span that traces the execution of the action.
     *
     * @param action the action to run
     * @param <E> the type of the exception that can be thrown by the action
     * @throws E on error in the action
     */
    public <E extends Throwable> void calling(ThrowingRunnable<E> action) throws E {
      if (tracer == null) {
        action.run();
        return;
      }

      Tracer.SpanBuilder bld = tracer.buildSpan(operationName).asChildOf(tracer.activeSpan());

      try (Scope scope = bld.startActive(true)) {
        addTags(scope.span());
        action.run();
      }
    }

    /**
     * Given the provided tracer, operation name and tags, this method wraps the provided action
     * in a span that traces the execution of the action.
     *
     * @param action the action to run
     * @param <T> the return type of the action
     * @param <E> the type of the exception that can be thrown by the action
     * @return the outcome of the action
     * @throws E on error in the action
     */
    public <T, E extends Throwable> T calling(ThrowingSupplier<T, E> action) throws E {
      if (tracer == null) {
        return action.get();
      }

      Tracer.SpanBuilder bld = tracer.buildSpan(operationName).asChildOf(tracer.activeSpan());

      try (Scope scope = bld.startActive(true)) {
        addTags(scope.span());
        return action.get();
      }
    }

    private void addTags(Span span) {
      tags.forEach(tv -> tv.applyTo(span));
    }
  }

  /**
   * A helper opaque class representing a tag with a value. To be used solely with
   * {@link SpanProducer#tagged(TagValue...)} or {@link TraceBuilder#create(String, TagValue...)}.
   * Instances of this class can be obtained by calling {@link Traces#tag(IntTag, Integer)},
   * {@link Traces#tag(StringTag, String)} or {@link Traces#tag(BooleanTag, Boolean)} methods.
   */
  public static abstract class TagValue {
    
    private static TagValue string(StringTag tag, String value) {
      return new StringTagValue(tag, value);
    }
    
    private static TagValue bool(BooleanTag tag, Boolean value) {
      return new BooleanTagValue(tag, value);
    }
    
    private static TagValue integer(IntTag tag, Integer value) {
      return new IntTagValue(tag, value);
    }
    
    protected abstract void applyTo(Span span);

    TagValue() {
      // package protected to prevent unwanted subclassing
    }
  }
  
  private static final class StringTagValue extends TagValue {
    private final StringTag tag;
    private final String value;
    
    private StringTagValue(StringTag tag, String value) {
      this.tag = tag;
      this.value = value;
    }

    @Override
    protected void applyTo(Span span) {
      tag.set(span, value);
    }
  }

  private static final class BooleanTagValue extends TagValue {
    private final BooleanTag tag;
    private final Boolean value;

    private BooleanTagValue(BooleanTag tag, Boolean value) {
      this.tag = tag;
      this.value = value;
    }

    @Override
    protected void applyTo(Span span) {
      tag.set(span, value);
    }
  }

  private static final class IntTagValue extends TagValue {
    private final IntTag tag;
    private final Integer value;

    private IntTagValue(IntTag tag, Integer value) {
      this.tag = tag;
      this.value = value;
    }

    @Override
    protected void applyTo(Span span) {
      tag.set(span, value);
    }
  }
}
