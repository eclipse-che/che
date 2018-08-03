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
import {CheAPIBuilder} from '../../components/api/builder/che-api-builder.factory';
import {CheHttpBackend} from '../../components/api/test/che-http-backend';

type User = {
  id: string,
  email: string,
  firstName: string,
  lastName: string
};

/**
 * This class creates mock data and sets up backend.
 *
 * @author Oleksii Kurinnyi
 */
export class OrganizationsConfigServiceMock {
  private cheAPIBuilder;
  private cheHttpBackend;

  private users: any[] = [];
  private orgs: any[] = [];
  private permissions: any[] = [];
  private usersByOrgs: Map<string, any[]> = new Map();

  static $inject = ['cheAPIBuilder', 'cheHttpBackend'];

  /**
   * Default constructor
   */
  constructor(cheAPIBuilder: CheAPIBuilder, cheHttpBackend: CheHttpBackend) {
    this.cheAPIBuilder = cheAPIBuilder;
    this.cheHttpBackend = cheHttpBackend;
    this.cheAPIBuilder = cheAPIBuilder;
    this.cheHttpBackend = cheHttpBackend;
  }

  mockData(): void {
    // add default user
    const user1 = this.addAndGetUser(1, true);
    // add users
    const user2 = this.addAndGetUser(2);
    const user3 = this.addAndGetUser(3);

    // add default profile
    this.addProfile(1, true);
    // add profiles
    this.addProfile(2);
    this.addProfile(3);

    // add root organization
    const org1 = this.addAndGetOrganization(1);
    // add children organizations
    const org2 = this.addAndGetOrganization(2, org1);

    // for root organization

    // add permissions
    [user1, user2, user3].forEach((user: any) => {
      this.addPermission(org1, user);
    });
    // add resources
    const totalResources = [
      {
        'type': 'workspace',
        'amount': 30,
        'unit': 'item'
      },
      {
        'type': 'runtime',
        'amount': 10,
        'unit': 'item'
      },
      {
        'type': 'timeout',
        'amount': 240,
        'unit': 'minute'
      },
      {
        'type': 'RAM',
        'amount': 102400,
        'unit': 'mb'
      }
    ];
    totalResources.forEach((resource: any) => {
      this.addResource(org1, 'total', resource);
    });

    // for sub-organization

    // add permissions
    [user1, user2].forEach((user: any) => {
      this.addPermission(org2, user);
    });
    // add total resources
    totalResources.forEach((resource: any) => {
      this.addResource(org2, 'total', resource);
    });
    //  add distributed resources
    totalResources.forEach((resource: any) => {
      this.addResource(org2, 'distributed', resource);
    });

    // build all backends at once
    this.cheHttpBackend.setup();
    this.cheHttpBackend.usersBackendSetup();
    this.cheHttpBackend.organizationsBackendSetup();
    this.cheHttpBackend.permissionsBackendSetup();
    this.cheHttpBackend.resourcesBackendSetup();
  }

  getUsers(): any[] {
    return this.users;
  }

  getUsersByOrganizationId(id: string): any[] {
    return this.usersByOrgs.get(id) || [];
  }

  getOrganizations(): any[] {
    return this.orgs;
  }

  private buildUser(suffix: number|string): User {
    return {
      id: `testUser_${suffix}`,
      email: `testUser_${suffix}@email.org`,
      firstName: `FirstName_${suffix}`,
      lastName: `LastName_${suffix}`
    };
  }

  private addProfile(suffix: number|string, isDefault?: boolean): void {
    const user = this.buildUser(suffix);
    const profile = this.cheAPIBuilder.getProfileBuilder().withId(user.id).withEmail(user.email).withFirstName(user.firstName).withLastName(user.lastName).build();

    if (isDefault) {
      this.cheHttpBackend.addDefaultProfile(profile);
    }

    this.cheHttpBackend.addProfileId(profile);
  }

  private addAndGetUser(suffix: number|string, isDefault?: boolean): any {
    const user = this.buildUser(suffix);
    const testUser = this.cheAPIBuilder.getUserBuilder().withId(user.id).withEmail(user.email).build();

    if (isDefault) {
      this.cheHttpBackend.setDefaultUser(testUser);
    }
    this.cheHttpBackend.addUserById(testUser);

    this.users.push(testUser);
    return testUser;
  }

  private addAndGetOrganization(suffix: number|string, parent?: any): any {
    const id = `testOrgId_${suffix}`;
    const name = `testOrgName_${suffix}`;
    const qualifiedName = (parent ? parent.qualifiedName + '/' : '') + name;

    const testOrganization = parent
      ? this.cheAPIBuilder.getOrganizationsBuilder().withId(id).withName(name).withQualifiedName(qualifiedName).withParentId(parent.id).build()
      : this.cheAPIBuilder.getOrganizationsBuilder().withId(id).withName(name).withQualifiedName(qualifiedName).build();

    this.cheHttpBackend.addOrganizationById(testOrganization);

    this.orgs.push(testOrganization);
    this.usersByOrgs.set(id, []);
    return testOrganization;
  }

  private addPermission(organization: any, user: any): void {
    const domainId = 'organization';

    const testPermission = this.cheAPIBuilder.getPermissionsBuilder().withDomainId(domainId).withInstanceId(organization.id).withUserId(user.id).build();

    this.cheHttpBackend.addPermissions(testPermission);

    this.usersByOrgs.get(organization.id).push(user);
    this.permissions.push(testPermission);
  }

  private addResource(organization: any, scope: string, resource: any): void {
    const testResource = this.cheAPIBuilder.getResourceBuilder().withAmount(resource.amount).withType(resource.type).withUnit(resource.unit).build();

    this.cheHttpBackend.addResource(organization.id, scope, testResource);
  }
}
