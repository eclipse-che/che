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
package org.eclipse.che.ide.ext.help.client.about.impl;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.eclipse.che.ide.ext.help.client.BuildDetailsProvider;
import org.eclipse.che.ide.ext.help.client.BuildInfo;

/**
 * Default implementation of {@link BuildDetailsProvider} which fetches information from {@link
 * BuildInfo} and build formatted string to display it on the UI.
 *
 * @see BuildDetailsProvider
 * @author Vlad Zhukovskyi
 * @since 6.7.0
 */
@Singleton
public class FormattedBuildDetailsProvider implements BuildDetailsProvider {

  private final BuildInfo buildInfo;

  @Inject
  public FormattedBuildDetailsProvider(BuildInfo buildInfo) {
    this.buildInfo = buildInfo;
  }

  @Override
  public String getBuildDetails() {
    SummaryTableFormatter formatter = new SummaryTableFormatter();

    formatter.addRow("Version:", buildInfo.version());
    formatter.addRow("Build Time:", buildInfo.buildTime());
    formatter.addRow("Revision:", buildInfo.revision());

    return formatter.toString();
  }

  static class SummaryTableFormatter {
    List<String[]> rows = new ArrayList<>();

    public void addRow(String... columns) {
      rows.add(columns);
    }

    private int[] columnWidths() {
      int maxColumnCount =
          rows.stream()
              .max(Comparator.comparingInt(o -> o.length))
              .map(row -> row.length)
              .orElse(-1);

      int[] columnWidths = new int[maxColumnCount];

      for (int column = 0; column < columnWidths.length; column++) {
        final int col = column; // effective final
        columnWidths[column] =
            rows.stream()
                .max(Comparator.comparingInt(row -> row.length <= col ? -1 : row[col].length()))
                .map(row -> row.length <= col ? 1 : row[col].length())
                .orElse(1);
      }

      return columnWidths;
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder();

      int[] columnWidths = columnWidths();

      rows.forEach(
          row -> {
            for (int column = 0; column < row.length; column++) {
              sb.append(Strings.padEnd(row[column], columnWidths[column] + 2, ' '));
            }
            sb.append('\n');
          });

      return sb.toString();
    }
  }
}
