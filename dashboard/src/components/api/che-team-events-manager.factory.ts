/*
 * Copyright (c) 2015-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
'use strict';
import {CheAPI} from './che-api.factory';
import {CheJsonRpcApi} from './json-rpc/che-json-rpc-api.factory';
import {CheJsonRpcMasterApi} from './json-rpc/che-json-rpc-master-api';

enum TEAM_EVENTS {
  MEMBER_ADDED,
  MEMBER_REMOVED,
  ORGANIZATION_REMOVED,
  ORGANIZATION_RENAMED
}

/**
 * This class is handling the notifications per each team.
 *
 * @author Ann Shumilova
 */
export class CheTeamEventsManager implements che.api.ICheTeamEventsManager {

  static $inject = ['cheAPI', 'cheJsonRpcApi', 'applicationNotifications', '$log', 'cheUser'];

  cheUser: any;
  cheJsonRpcMasterApi: CheJsonRpcMasterApi;
  $log: ng.ILogService;
  applicationNotifications: any;
  subscribers: Array<string>;
  renameHandlers: Array<Function>;
  newTeamHandlers: Array<Function>;
  deleteHandlers: Array<Function>;

  /**
   * Default constructor that is using resource
   */
  constructor(cheAPI: CheAPI, cheJsonRpcApi: CheJsonRpcApi, applicationNotifications: any, $log: ng.ILogService, cheUser: any) {
    this.cheUser = cheUser;
    this.cheJsonRpcMasterApi = cheJsonRpcApi.getJsonRpcMasterApi(cheAPI.getWorkspace().getJsonRpcApiLocation());
    this.applicationNotifications = applicationNotifications;
    this.$log = $log;
    this.subscribers = [];
    this.renameHandlers = [];
    this.deleteHandlers = [];
    this.newTeamHandlers = [];
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
    this.cheJsonRpcMasterApi.subscribeOrganizationStatus(teamId, (message: any) => {
      switch (message.type) {
        case TEAM_EVENTS[TEAM_EVENTS.ORGANIZATION_RENAMED]:
          this.processRenameTeam(message);
          break;
        case TEAM_EVENTS[TEAM_EVENTS.ORGANIZATION_REMOVED]:
          this.processDeleteTeam(message);
          break;
        default:
          break;
      }
    });
  }

  /**
   * Subscribe team member changing events.
   */
  subscribeTeamMemberNotifications(): void {
    let id = this.cheUser.getUser().id;
    this.cheJsonRpcMasterApi.subscribeOrganizationMembershipStatus(id, (message: any) => {
      switch (message.type) {
        case TEAM_EVENTS[TEAM_EVENTS.MEMBER_ADDED]:
          this.processAddedToTeam(message);
          break;
        case TEAM_EVENTS[TEAM_EVENTS.MEMBER_REMOVED]:
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
    this.cheJsonRpcMasterApi.unSubscribeOrganizationStatus(teamId);
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
