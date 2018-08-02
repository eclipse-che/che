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
package org.eclipse.che.ide.util;

import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import java.math.BigDecimal;

/**
 * Allow to manage bytes value by providing human values Also human values can be transformed into
 * some units
 *
 * @author Florent Benoit
 */
public class Bytes {

  /** Pattern to split size unit */
  private static RegExp UNIT_PATTERN = RegExp.compile("(\\s*\\d*\\.?\\d+)(.*)");

  /** All units supported */
  public static enum Unit {
    B(1),
    KiB(1024),
    KB(1e3),
    MiB(KiB.value * 1024),
    MB(1e6),
    GiB(MiB.value * 1024),
    GB(1e9),
    TiB(GiB.value * 1024),
    TB(1e12),
    PiB(TiB.value * 1024),
    PB(1e15),
    EiB(PiB.value * 1024),
    EB(1e18);

    private double value;

    Unit(double value) {
      this.value = value;
    }

    public double getValue() {
      return value;
    }
  }

  /** Utility class so private constructor. */
  private Bytes() {}

  /**
   * Returns a simplified version of the given size. For example giving '4000MB' will return '4GB'
   *
   * @param humanSize a stringified version
   * @return the new size by using the higher range value
   */
  public static String toHumanSize(String humanSize) {
    // convert to bytes first
    return toHumanSize(fromHumanSize(humanSize));
  }

  /**
   * Convert a value in bytes to value readable by humans
   *
   * @param bytesValue the bytes value
   * @return the updated value
   */
  public static String toHumanSize(long bytesValue) {
    if (0 == bytesValue) {
      return "0B";
    }
    // it's on base 2
    boolean powerOfTwo = false;
    if ((bytesValue & (bytesValue - 1L)) == 0) {
      powerOfTwo = true;
    }
    // or an exact modulo like 1.5
    long multiple = (long) (bytesValue / 1.5);
    if ((multiple & (multiple - 1L)) == 0) {
      powerOfTwo = true;
    }

    Unit unit = Unit.B;
    // power of two
    if (powerOfTwo) {
      if (bytesValue >= Unit.EiB.value) {
        unit = Unit.EiB;
      } else if (bytesValue >= Unit.PiB.value) {
        unit = Unit.PiB;
      } else if (bytesValue >= Unit.TiB.value) {
        unit = Unit.TiB;
      } else if (bytesValue >= Unit.GiB.value) {
        unit = Unit.GiB;
      } else if (bytesValue >= Unit.MiB.value) {
        unit = Unit.MiB;
      } else if (bytesValue >= Unit.KiB.value) {
        unit = Unit.KiB;
      }
    } else {
      // base 10
      if (bytesValue >= Unit.EB.value) {
        unit = Unit.EB;
      } else if (bytesValue >= Unit.PB.value) {
        unit = Unit.PB;
      } else if (bytesValue >= Unit.TB.value) {
        unit = Unit.TB;
      } else if (bytesValue >= Unit.GB.value) {
        unit = Unit.GB;
      } else if (bytesValue >= Unit.MB.value) {
        unit = Unit.MB;
      } else if (bytesValue >= Unit.KB.value) {
        unit = Unit.KB;
      }
    }

    // one digit float
    double val = (bytesValue / unit.getValue());
    double roundedValue = Math.round(val * 10.0) / 10.0;
    // Strip any trailing 0
    BigDecimal myDecimal = BigDecimal.valueOf(roundedValue);
    BigDecimal updated = myDecimal.stripTrailingZeros();
    return updated.toPlainString() + unit.name();
  }

  /**
   * Convert human size unit to a bytes value
   *
   * @param humanSize the human size to convert
   * @return the byes value
   */
  public static long fromHumanSize(String humanSize) {
    // first convert it to bytes
    Pair<Double, Unit> value = splitValueAndUnit(humanSize);
    // now convert it to bytes
    return convertToBytes(value);
  }

  /** */
  protected static long convertToBytes(Pair<Double, Unit> value) {
    // first index is value, second one is unit
    return new Double(value.getFirst().doubleValue() * value.getSecond().getValue()).longValue();
  }

  /**
   * Extract unit and values from the given string
   *
   * @param humanSize the value to analyze
   * @return array with first index the value, second one the unit
   */
  protected static Pair<Double, Unit> splitValueAndUnit(String humanSize) {
    MatchResult matchResult = UNIT_PATTERN.exec(humanSize);
    if (matchResult.getGroupCount() != 3 || matchResult.getGroup(2).isEmpty()) {
      throw new IllegalArgumentException(
          "Unable to get unit in the given value '" + humanSize + "'");
    }
    Double val = Double.parseDouble(matchResult.getGroup(1).trim());
    Unit unitVal = Unit.valueOf(matchResult.getGroup(2).trim());
    Pair<Double, Unit> value = new Pair<>(val, unitVal);
    return value;
  }
}
