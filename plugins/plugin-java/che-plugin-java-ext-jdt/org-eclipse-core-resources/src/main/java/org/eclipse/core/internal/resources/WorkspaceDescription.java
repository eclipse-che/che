/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2012 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.core.internal.resources;

import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;

/** @see IWorkspaceDescription */
public class WorkspaceDescription extends ModelObject implements IWorkspaceDescription {
  protected boolean autoBuilding;
  protected String[] buildOrder;
  protected long fileStateLongevity;
  protected int maxBuildIterations;
  protected int maxFileStates;
  protected long maxFileStateSize;
  protected boolean applyFileStatePolicy;
  private long snapshotInterval;
  protected int operationsPerSnapshot;
  protected long deltaExpiration;

  public WorkspaceDescription(String name) {
    super(name);
    // initialize based on the values in the default preferences
    IEclipsePreferences node = DefaultScope.INSTANCE.getNode(ResourcesPlugin.PI_RESOURCES);
    autoBuilding =
        node.getBoolean(
            ResourcesPlugin.PREF_AUTO_BUILDING, PreferenceInitializer.PREF_AUTO_BUILDING_DEFAULT);
    maxBuildIterations =
        node.getInt(
            ResourcesPlugin.PREF_MAX_BUILD_ITERATIONS,
            PreferenceInitializer.PREF_MAX_BUILD_ITERATIONS_DEFAULT);
    applyFileStatePolicy =
        node.getBoolean(
            ResourcesPlugin.PREF_APPLY_FILE_STATE_POLICY,
            PreferenceInitializer.PREF_APPLY_FILE_STATE_POLICY_DEFAULT);
    fileStateLongevity =
        node.getLong(
            ResourcesPlugin.PREF_FILE_STATE_LONGEVITY,
            PreferenceInitializer.PREF_FILE_STATE_LONGEVITY_DEFAULT);
    maxFileStates =
        node.getInt(
            ResourcesPlugin.PREF_MAX_FILE_STATES,
            PreferenceInitializer.PREF_MAX_FILE_STATES_DEFAULT);
    maxFileStateSize =
        node.getLong(
            ResourcesPlugin.PREF_MAX_FILE_STATE_SIZE,
            PreferenceInitializer.PREF_MAX_FILE_STATE_SIZE_DEFAULT);
    snapshotInterval =
        node.getLong(
            ResourcesPlugin.PREF_SNAPSHOT_INTERVAL,
            PreferenceInitializer.PREF_SNAPSHOT_INTERVAL_DEFAULT);
    operationsPerSnapshot =
        node.getInt(
            PreferenceInitializer.PREF_OPERATIONS_PER_SNAPSHOT,
            PreferenceInitializer.PREF_OPERATIONS_PER_SNAPSHOT_DEFAULT);
    deltaExpiration =
        node.getLong(
            PreferenceInitializer.PREF_DELTA_EXPIRATION,
            PreferenceInitializer.PREF_DELTA_EXPIRATION_DEFAULT);
  }

  /** @see IWorkspaceDescription#getBuildOrder() */
  public String[] getBuildOrder() {
    return getBuildOrder(true);
  }

  public String[] getBuildOrder(boolean makeCopy) {
    if (buildOrder == null) return null;
    return makeCopy ? (String[]) buildOrder.clone() : buildOrder;
  }

  public long getDeltaExpiration() {
    return deltaExpiration;
  }

  public void setDeltaExpiration(long value) {
    deltaExpiration = value;
  }

  /** @see IWorkspaceDescription#getFileStateLongevity() */
  public long getFileStateLongevity() {
    return fileStateLongevity;
  }

  /** @see IWorkspaceDescription#getMaxBuildIterations() */
  public int getMaxBuildIterations() {
    return maxBuildIterations;
  }

  /** @see IWorkspaceDescription#getMaxFileStates() */
  public int getMaxFileStates() {
    return maxFileStates;
  }

  /** @see IWorkspaceDescription#getMaxFileStateSize() */
  public long getMaxFileStateSize() {
    return maxFileStateSize;
  }

  /** @see IWorkspaceDescription#isApplyFileStatePolicy() */
  public boolean isApplyFileStatePolicy() {
    return applyFileStatePolicy;
  }

  public int getOperationsPerSnapshot() {
    return operationsPerSnapshot;
  }

  /** @see IWorkspaceDescription#getSnapshotInterval() */
  public long getSnapshotInterval() {
    return snapshotInterval;
  }

  public void internalSetBuildOrder(String[] value) {
    buildOrder = value;
  }

  /** @see IWorkspaceDescription#isAutoBuilding() */
  public boolean isAutoBuilding() {
    return autoBuilding;
  }

  public void setOperationsPerSnapshot(int value) {
    operationsPerSnapshot = value;
  }

  /** @see IWorkspaceDescription#setAutoBuilding(boolean) */
  public void setAutoBuilding(boolean value) {
    autoBuilding = value;
  }

  /** @see IWorkspaceDescription#setBuildOrder(String[]) */
  public void setBuildOrder(String[] value) {
    buildOrder = (value == null) ? null : (String[]) value.clone();
  }

  /** @see IWorkspaceDescription#setFileStateLongevity(long) */
  public void setFileStateLongevity(long time) {
    fileStateLongevity = time;
  }

  /** @see IWorkspaceDescription#setMaxBuildIterations(int) */
  public void setMaxBuildIterations(int number) {
    maxBuildIterations = number;
  }

  /** @see IWorkspaceDescription#setMaxFileStates(int) */
  public void setMaxFileStates(int number) {
    maxFileStates = number;
  }

  /** @see IWorkspaceDescription#setMaxFileStateSize(long) */
  public void setMaxFileStateSize(long size) {
    maxFileStateSize = size;
  }

  /** @see IWorkspaceDescription#setApplyFileStatePolicy(boolean) */
  public void setApplyFileStatePolicy(boolean apply) {
    applyFileStatePolicy = apply;
  }

  /** @see IWorkspaceDescription#setSnapshotInterval(long) */
  public void setSnapshotInterval(long snapshotInterval) {
    this.snapshotInterval = snapshotInterval;
  }
}
