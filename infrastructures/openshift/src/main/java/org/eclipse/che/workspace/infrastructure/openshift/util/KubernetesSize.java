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
package org.eclipse.che.workspace.infrastructure.openshift.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helps to convert bytes from/to Kubernetes format.
 *
 * @author Anton Korneta
 */
public class KubernetesSize {

  // si system multipliers
  private static final int K = 1000;
  private static final long M = K * K;
  private static final long G = M * K;
  private static final long T = G * K;
  private static final long P = T * K;
  private static final long E = P * K;

  // power of 2 multipliers
  private static final int KI = 1024;
  private static final long MI = KI * KI;
  private static final long GI = MI * KI;
  private static final long TI = GI * KI;
  private static final long PI = TI * KI;
  private static final long EI = PI * KI;

  private static final Pattern HUMAN_SIZE_PATTERN =
      Pattern.compile("^([-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?)\\s*(\\S+)?$");

  /** Converts memory in Kubernetes format to bytes. */
  public static long toBytes(String sizeString) {
    final Matcher matcher;
    if ((matcher = HUMAN_SIZE_PATTERN.matcher(sizeString)).matches()) {
      final float size = Float.parseFloat(matcher.group(1));
      final String suffix = matcher.group(3);
      if (suffix == null) {
        return (long) size;
      }
      switch (suffix.toLowerCase()) {
        case "b":
        case "bi":
          return (long) size;
        case "k":
          return (long) (size * K);
        case "ki":
        case "kib":
          return (long) (size * KI);
        case "m":
          return (long) (size * M);
        case "mi":
        case "mib":
          return (long) (size * MI);
        case "g":
          return (long) (size * G);
        case "gi":
        case "gib":
          return (long) (size * GI);
        case "t":
          return (long) (size * T);
        case "ti":
        case "tib":
          return (long) (size * TI);
        case "p":
          return (long) (size * P);
        case "pi":
        case "pib":
          return (long) (size * PI);
        case "e":
          return (long) (size * E);
        case "ei":
        case "eib":
          return (long) (size * EI);
      }
    }
    throw new IllegalArgumentException("Invalid Kubernetes size format provided: " + sizeString);
  }

  /** Converts memory from bytes into Kubernetes human readable format. */
  public static String toKubeSize(long bytes, boolean si) {
    final int multiplier = si ? K : KI;
    if (bytes < multiplier) {
      return bytes + "B";
    }
    final int e = (int) (Math.log(bytes) / Math.log(multiplier));
    final String unit = (si ? "kMGTPE" : "KMGTPE").charAt(e - 1) + (si ? "" : "i");
    final float size = bytes / (float) Math.pow(multiplier, e);
    return String.format((size % 1.0f == 0) ? "%.0f%s" : "%.1f%s", size, unit);
  }
}
