/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.user.server.dao;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.user.shared.model.Membership;

import java.util.List;

/**
 * @author gazarenkov
 */
public abstract class MembershipDao {

    /**
     * Creates a new Member
     *
     * @param member
     *         POJO representation of workspace member
     */
    public abstract void createMembership(Membership member) throws ConflictException, NotFoundException, ServerException;


    /**
     * Updates member.
     *
     * @param member
     *         POJO representation of workspace member
     */
    public abstract void updateMembership(Membership member) throws NotFoundException, ServerException, ConflictException;


    /**
     * Removes a given member from specified workspace.
     *
     * @param member
     *         member to remove
     */
    public abstract void removeMembership(Membership member) throws NotFoundException, ConflictException, ServerException;

    /**
     * Gets a list of all members of the given subject.
     *
     *         subject to search in
     * @return list of members
     */
    public abstract List<Membership> getAllMemberships(String scope, String subjectId) throws ServerException;


    /**
     * Gets a list of all relationships of the given user and workspaces.
     *
     * @param userId
     *         user to get relationships
     * @return list of user relations
     */
    public abstract List<Membership> getMemberships(String userId) throws ServerException;

    /**
     * Gets a list of all members for the whole scope.
     *
     *
     * @return list of workspace members
     */
    public abstract List<Membership> getMemberships(String userId, String scope) throws ServerException;


    /**
     * Gets a list of all members for the whole scope.
     *
     *
     * @return list of workspace members
     */
    public abstract Membership getMembership(String userId, String scope, String subjectId) throws NotFoundException, ServerException;

    /**
     * Whether user is in role for given subject
     *
     * @return true if it is, false otherwise
     */
    public abstract boolean isUserInRole(String userId, String scope, String subjectId, String role) throws ServerException;


    /**
     * Whether user has some role for this subject
     *
     * @return true if it is, false otherwise
     */
    public abstract boolean membershipExists(String userId, String scope, String subjectId) throws ServerException;


}
