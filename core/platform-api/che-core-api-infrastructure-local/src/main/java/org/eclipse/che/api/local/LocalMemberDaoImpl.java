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
package org.eclipse.che.api.local;
//
//import org.eclipse.che.api.core.ConflictException;
//import org.eclipse.che.api.core.NotFoundException;
//import org.eclipse.che.api.core.ServerException;
//import org.eclipse.che.api.user.server.dao.UserDao;
//import org.eclipse.che.api.workspace.server.dao.Member;
//import org.eclipse.che.api.workspace.server.dao.MemberDao;
//import org.eclipse.che.api.workspace.server.dao.WorkspaceDao;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author Anton Korneta
 */
@Singleton
public class LocalMemberDaoImpl {
////    private final List<Member>  members;
//    private final ReadWriteLock lock;
//
////    private final WorkspaceDao workspaceDao;
//    private final UserDao      userDao;
//
//    @Inject
//    public LocalMemberDaoImpl(@Named("codenvy.local.infrastructure.workspace.members") Set<Member> members,
////                              WorkspaceDao workspaceDao,
//                              UserDao userDao) {
////        this.workspaceDao = workspaceDao;
//        this.userDao = userDao;
////        this.members = new LinkedList<>();
//        lock = new ReentrantReadWriteLock();
//        try {
//            for (Member member : members) {
//                create(member);
//            }
//        } catch (Exception e) {
//            // fail if can't validate this instance properly
//            throw new RuntimeException(e);
//        }
//    }
//
//    @Override
//    public void create(Member member) throws NotFoundException, ServerException, ConflictException {
//        lock.writeLock().lock();
//        try {
//            // Check workspace existence
//            workspaceDao.getById(member.getWorkspaceId());
//            // Check user existence
//            userDao.getById(member.getUserId());
//            for (Member m : members) {
//                if (m.getWorkspaceId().equals(member.getWorkspaceId()) && m.getUserId().equals(member.getUserId())) {
//                    throw new ConflictException(
//                            String.format("MembershipDo of user %s in workspace %s already exists. Use update method instead.",
//                                          member.getUserId(), member.getWorkspaceId()));
//                }
//            }
//            members.add(new Member().withUserId(member.getUserId()).withWorkspaceId(member.getWorkspaceId())
//                                    .withRoles(new ArrayList<>(member.getRoles())));
//        } finally {
//            lock.writeLock().unlock();
//        }
//    }
//
//    @Override
//    public void update(Member member) throws NotFoundException, ServerException {
//        lock.writeLock().lock();
//        try {
//            // Check workspace existence
//            workspaceDao.getById(member.getWorkspaceId());
//            // Check user existence
//            userDao.getById(member.getUserId());
//            Member myMember = null;
//            for (int i = 0, size = members.size(); i < size && myMember == null; i++) {
//                Member m = members.get(i);
//                if (m.getWorkspaceId().equals(member.getWorkspaceId()) && m.getUserId().equals(member.getUserId())) {
//                    myMember = m;
//                }
//            }
//            if (myMember == null) {
//                throw new NotFoundException(String.format("Unable to update membership: user %s has no memberships in workspace %s.",
//                                                          member.getUserId(), member.getWorkspaceId()));
//            }
//            myMember.getRoles().clear();
//            myMember.getRoles().addAll(member.getRoles());
//        } finally {
//            lock.writeLock().unlock();
//        }
//    }
//
//    @Override
//    public List<Member> getWorkspaceMembers(String wsId) {
//        final List<Member> result = new LinkedList<>();
//        lock.readLock().lock();
//        try {
//            for (Member member : members) {
//                if (member.getWorkspaceId().equals(wsId)) {
//                    result.add(new Member().withUserId(member.getUserId()).withWorkspaceId(member.getWorkspaceId())
//                                           .withRoles(new ArrayList<>(member.getRoles())));
//                }
//            }
//        } finally {
//            lock.readLock().unlock();
//        }
//        return result;
//    }
//
//    @Override
//    public List<Member> getUserRelationships(String userId) {
//        final List<Member> result = new LinkedList<>();
//        lock.readLock().lock();
//        try {
//            for (Member member : members) {
//                if (member.getUserId().equals(userId)) {
//                    result.add(new Member().withUserId(member.getUserId()).withWorkspaceId(member.getWorkspaceId())
//                                           .withRoles(new ArrayList<>(member.getRoles())));
//                }
//            }
//        } finally {
//            lock.readLock().unlock();
//        }
//        return result;
//    }
//
//    @Override
//    public Member getWorkspaceMember(String wsId, String userId) throws NotFoundException, ServerException {
//        lock.readLock().lock();
//        try {
//            for (Member member : members) {
//                if (member.getWorkspaceId().equals(member.getWorkspaceId()) && member.getUserId().equals(userId)) {
//                    return new Member().withUserId(member.getUserId()).withWorkspaceId(member.getWorkspaceId())
//                                       .withRoles(new ArrayList<>(member.getRoles()));
//                }
//            }
//        } finally {
//            lock.readLock().unlock();
//        }
//        throw new NotFoundException(String.format("User with id %s has no membership in workspace %s", userId, wsId));
//    }
//
//    @Override
//    public void remove(Member member) throws NotFoundException {
//        lock.writeLock().lock();
//        try {
//            Member myMember = null;
//            for (int i = 0, size = members.size(); i < size && myMember == null; i++) {
//                Member m = members.get(i);
//                if (m.getWorkspaceId().equals(member.getWorkspaceId()) && m.getUserId().equals(member.getUserId())) {
//                    myMember = m;
//                }
//            }
//            if (myMember == null) {
//                throw new NotFoundException(String.format("Unable to update membership: user %s has no memberships in workspace %s.",
//                                                          member.getUserId(), member.getWorkspaceId()));
//            }
//            members.remove(myMember);
//        } finally {
//            lock.writeLock().unlock();
//        }
//    }
}
