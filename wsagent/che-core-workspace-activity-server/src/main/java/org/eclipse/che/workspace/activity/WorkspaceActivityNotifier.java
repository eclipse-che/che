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
package org.eclipse.che.workspace.activity;

import java.util.concurrent.atomic.AtomicBoolean;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.commons.schedule.ScheduleRate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Notifies master about activity in workspace, but not more often than once per given threshold.
 *
 * @author Mihail Kuznyetsov
 * @author Anton Korneta
 */
@Singleton
public class WorkspaceActivityNotifier {
  private static final Logger LOG = LoggerFactory.getLogger(WorkspaceActivityNotifier.class);

  private final AtomicBoolean activeDuringThreshold;
  private final HttpJsonRequestFactory httpJsonRequestFactory;
  private final String apiEndpoint;
  private final String wsId;
  private final long threshold;

  private long lastUpdateTime;

  @Inject
  public WorkspaceActivityNotifier(
      HttpJsonRequestFactory httpJsonRequestFactory,
      @Named("che.api") String apiEndpoint,
      @Named("env.CHE_WORKSPACE_ID") String wsId,
      @Named("workspace.activity.notify_time_threshold_ms") long threshold) {
    this.httpJsonRequestFactory = httpJsonRequestFactory;
    this.apiEndpoint = apiEndpoint;
    this.wsId = wsId;
    this.activeDuringThreshold = new AtomicBoolean(false);
    this.threshold = threshold;
  }

  /**
   * Notify workspace master about activity in this workspace.
   *
   * <p>There is a {@code threshold} that limits request sending frequency - if no activity has
   * occurred within the duration of threshold, then the request will be sent immediately (in a
   * separate thread, to prevent it's possible long execution from blocking of the Tomcat thread,
   * from which it may be called).
   *
   * <p>In case there was any recent activity within threshold time, any consecutive activities will
   * not send request immediately, but trigger it's sending in scheduler method {@link
   * WorkspaceActivityNotifier#scheduleActivityNotification}
   */
  public void onActivity() {
    long currentTime = System.currentTimeMillis();
    if (currentTime < (lastUpdateTime + threshold)) {
      activeDuringThreshold.set(true);
    } else {
      Thread activityRequestThread = new Thread(this::notifyActivity);
      activityRequestThread.setName("WorkspaceActivityRequestThread");
      activityRequestThread.setDaemon(true);
      activityRequestThread.start();

      lastUpdateTime = currentTime;
    }
  }

  @ScheduleRate(periodParameterName = "workspace.activity.schedule_period_s")
  private void scheduleActivityNotification() {
    if (activeDuringThreshold.compareAndSet(true, false)) {
      notifyActivity();
    }
  }

  private void notifyActivity() {
    try {
      httpJsonRequestFactory.fromUrl(apiEndpoint + "/activity/" + wsId).usePutMethod().request();
    } catch (Exception e) {
      LOG.error("Cannot notify master about workspace " + wsId + " activity", e);
    }
  }
}
