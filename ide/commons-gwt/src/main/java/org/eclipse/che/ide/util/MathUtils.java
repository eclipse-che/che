/*
 * Copyright (c) 2012-2015 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.util;

import static java.lang.Math.pow;

/**
 * Provides utility methods that helps math computations.
 *
 * @author Vlad Zhukovskiy
 */
public final class MathUtils {
  /**
   * A Bézier curve is a parametric curve frequently used in computer graphics and related fields.
   * In vector graphics, Bézier curves are used to model smooth curves that can be scaled
   * indefinitely.
   *
   * <p>Four points x1, x2, x3 and x4 in the plane or in higher-dimensional space define a cubic
   * Bézier curve. The curve starts at x1 going toward x2 and arrives at x4 coming from the
   * direction of x3. Usually, it will not pass through x2 or x3; these points are only there to
   * provide directional information. The distance between x1 and x2 determines "how far" and "how
   * fast" the curve moves towards x2 before turning towards x3.
   *
   * <p>https://en.wikipedia.org/wiki/B%C3%A9zier_curve
   *
   * @param t should be between 0.0 and 1.0
   * @param x1 coordinate for the x1
   * @param x2 coordinate for the x2
   * @param x3 coordinate for the x3
   * @param x4 coordinate for the x4
   * @return coordinate for the point
   */
  public static double getCubicBezier(double t, double x1, double x2, double x3, double x4) {
    return pow((1. - t), 3.) * x1
        + 3. * pow((1. - t), 2.) * t * x2
        + 3. * (1. - t) * pow(t, 2.) * x3
        + pow(t, 3.) * x4;
  }

  private MathUtils() {}
}
