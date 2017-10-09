/*
 * Copyright (c) 2015-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
'use strict';

enum TEAM_EVENTS {MEMBER_ADDED, MEMBER_REMOVED, ORGANIZATION_REMOVED, ORGANIZATION_RENAMED}


/**
 * This class is handling the notifications per each team.
 *
 * @author Ann Shumilova
 */
export class CheTeamEventsManager implements che.api.ICheTeamEventsManager {
  cheUser: any;
  $log: ng.ILogService;
  cheWebsocket: any;
  applicationNotifications: any;
  TEAM_CHANNEL: string = 'organization:';
  TEAM_MEMBER_CHANNEL: string = 'organization:member:';
  subscribers: Array<string>;
  renameHandlers: Array<Function>;
  newTeamHandlers: Array<Function>;
  deleteHandlers: Array<Function>;

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor(cheWebsocket: any, applicationNotifications: any, $log: ng.ILogService, cheUser: any) {
    this.cheUser = cheUser;
    this.cheWebsocket = cheWebsocket;
    this.applicationNotifications = applicationNotifications;
    this.$log = $log;
    this.subscribers = [];
    this.renameHandlers = [];
    this.deleteHandlers = [];
    this.newTeamHandlers = [];
    this.fetchUser();
  }

  /**
   * Subscribe team changing events.
   *
   * @param teamId team id to subscribe on events
   */
  subscribeTeamNotifications(teamId: string): void {
    if (this.subscribers.indexOf(teamId) >= 0) {
      return;
    }
    this.subscribers.push(teamId);
    let bus = this.cheWebsocket.getBus();
    bus.subscribe(this.TEAM_CHANNEL + teamId, (message: any) => {
      // todo
      switch (TEAM_EVENTS[message.type]) {
        case TEAM_EVENTS.ORGANIZATION_RENAMED:
          this.processRenameTeam(message);
          break;
        case TEAM_EVENTS.ORGANIZATION_REMOVED:
          this.processDeleteTeam(message);
          break;
        default:
          break;
      }
    });
  }

  fetchUser(): void {
    this.cheUser.fetchUser().then(() => {
      this.subscribeTeamMemberNotifications();
    }, (error: any) => {
      if (error.status === 304) {
        this.subscribeTeamMemberNotifications();
      }
    });
  }

  /**
   * Subscribe team member changing events.
   */
  subscribeTeamMemberNotifications(): void {
    let id = this.cheUser.getUser().id;
    let bus = this.cheWebsocket.getBus();
    bus.subscribe(this.TEAM_MEMBER_CHANNEL + id, (message: any) => {
      switch (TEAM_EVENTS[message.type]) {
        case TEAM_EVENTS.MEMBER_ADDED:
          this.processAddedToTeam(message);
          break;
        case TEAM_EVENTS.MEMBER_REMOVED:
          this.processDeleteMember(message);
          break;
        default:
          break;
      }
    });
  }

  /**
   * Unsubscribe team changing events.
   *
   * @param teamId
   */
  unSubscribeTeamNotifications(teamId: string): void {
    let bus = this.cheWebsocket.getBus();
    bus.unsubscribe(this.TEAM_CHANNEL + teamId);
  }

  /**
   * Adds rename handler.
   *
   * @param handler rename handler function
   */
  addRenameHandler(handler: Function): void {
    this.renameHandlers.push(handler);
  }

  /**
   * Removes rename handler.
   *
   * @param handler handler to remove
   */
  removeRenameHandler(handler: Function): void {
    this.renameHandlers.splice(this.renameHandlers.indexOf(handler), 1);
  }

  /**
   * Adds delete handler.
   *
   * @param handler delete handler function
   */
  addDeleteHandler(handler: Function): void {
    this.deleteHandlers.push(handler);
  }

  /**
   * Removes delete handler.
   *
   * @param handler delete handler to remove
   */
  removeDeleteHandler(handler: Function): void {
    this.deleteHandlers.splice(this.deleteHandlers.indexOf(handler), 1);
  }

  /**
   * Adds new team handler.
   *
   * @param handler new team handler function
   */
  addNewTeamHandler(handler: Function): void {
    this.newTeamHandlers.push(handler);
  }

  /**
   * Process team renamed event.
   *
   * @param info
   */
  processRenameTeam(info: any): void {
    let isCurrentUser = this.isCurrentUser(info.initiator);
    if (isCurrentUser) {
      // todo
    } else {
      let title = 'Team renamed';
      let content = 'Team \"' + info.oldName + '\" has been renamed to \"' + info.newName + '\" by ' + info.initiator;
      this.applicationNotifications.addInfoNotification(title, content);

      this.renameHandlers.forEach((handler: Function) => {
        handler(info);
      });
    }
  }

  /**
   * Process team renamed event.
   *
   * @param info
   */
  processAddedToTeam(info: any): void {
    let isCurrentUser = this.isCurrentUser(info.initiator);
    if (isCurrentUser) {
      // todo
    } else {
      let title = 'You were added to team';
      let content = info.initiator + ' added you to team called \"' + info.organization.qualifiedName  + '\".';
      this.applicationNotifications.addInfoNotification(title, content);

      this.newTeamHandlers.forEach((handler: Function) => {
        handler(info);
      });
    }
  }

  /**
   * Process team deleted event.
   *
   * @param info
   */
  processDeleteTeam(info: any): void {
    let isCurrentUser = this.isCurrentUser(info.initiator);
    if (isCurrentUser) {
      // todo
    } else {
      let title = 'Team deleted';
      let content = 'Team \"' + info.organization.qualifiedName + '\" has been deleted by ' + info.initiator;
      this.applicationNotifications.addInfoNotification(title, content);

      this.unSubscribeTeamNotifications(info.organization.id);

      this.deleteHandlers.forEach((handler: Function) => {
        handler(info);
      });
    }
  }

  /**
   * Process member deleted event.
   *
   * @param info
   */
  processDeleteMember(info: any): void {
    let isCurrentUserInitiator = this.isCurrentUser(info.initiator);
    if (isCurrentUserInitiator) {
      // todo
    } else {
      let title = 'You have been removed from team';
      let content = info.initiator + ' removed you from team called \"' + info.organization.qualifiedName + '\".';
      this.applicationNotifications.addInfoNotification(title, content);

      this.deleteHandlers.forEach((handler: Function) => {
        handler(info);
      });
    }
  }

  /**
   * Checks current user is the performer of the action, that causes team changes.
   *
   * @param name
   * @returns {boolean}
   */
  isCurrentUser(name: string): boolean {
    return name === this.cheUser.getUser().name;
  }
}
