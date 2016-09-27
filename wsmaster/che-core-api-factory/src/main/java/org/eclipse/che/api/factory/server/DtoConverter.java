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
package org.eclipse.che.api.factory.server;

import org.eclipse.che.api.core.model.user.User;
import org.eclipse.che.api.factory.shared.dto.AuthorDto;
import org.eclipse.che.api.factory.shared.dto.ButtonAttributesDto;
import org.eclipse.che.api.factory.shared.dto.ButtonDto;
import org.eclipse.che.api.factory.shared.dto.FactoryDto;
import org.eclipse.che.api.factory.shared.dto.IdeActionDto;
import org.eclipse.che.api.factory.shared.dto.IdeDto;
import org.eclipse.che.api.factory.shared.dto.OnAppClosedDto;
import org.eclipse.che.api.factory.shared.dto.OnAppLoadedDto;
import org.eclipse.che.api.factory.shared.dto.OnProjectsLoadedDto;
import org.eclipse.che.api.factory.shared.dto.PoliciesDto;
import org.eclipse.che.api.core.model.factory.Action;
import org.eclipse.che.api.core.model.factory.Author;
import org.eclipse.che.api.core.model.factory.Button;
import org.eclipse.che.api.core.model.factory.ButtonAttributes;
import org.eclipse.che.api.core.model.factory.Factory;
import org.eclipse.che.api.core.model.factory.Ide;
import org.eclipse.che.api.core.model.factory.OnAppClosed;
import org.eclipse.che.api.core.model.factory.OnAppLoaded;
import org.eclipse.che.api.core.model.factory.OnProjectsLoaded;
import org.eclipse.che.api.core.model.factory.Policies;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.eclipse.che.dto.server.DtoFactory.newDto;

/**
 * Helps to convert to DTOs related to factory.
 *
 * @author Anton Korneta
 */
public final class DtoConverter {

    public static FactoryDto asDto(Factory factory, User user) {
        final FactoryDto factoryDto = newDto(FactoryDto.class).withId(factory.getId())
                                                              .withName(factory.getName())
                                                              .withV(factory.getV());

        if (factory.getWorkspace() != null) {
            factoryDto.withWorkspace(org.eclipse.che.api.workspace.server.DtoConverter.asDto(factory.getWorkspace()));
        }
        if (factory.getCreator() != null) {
            factoryDto.withCreator(asDto(factory.getCreator(), user));
        }
        if (factory.getIde() != null) {
            factoryDto.withIde(asDto(factory.getIde()));
        }
        if (factory.getPolicies() != null) {
            factoryDto.withPolicies(asDto(factory.getPolicies()));
        }
        if (factory.getButton() != null) {
            factoryDto.withButton(asDto(factory.getButton()));
        }
        return factoryDto;
    }

    public static IdeDto asDto(Ide ide) {
        final IdeDto ideDto = newDto(IdeDto.class);
        final OnAppClosed onAppClosed = ide.getOnAppClosed();
        final OnAppLoaded onAppLoaded = ide.getOnAppLoaded();
        final OnProjectsLoaded onProjectsLoaded = ide.getOnProjectsLoaded();
        if (onAppClosed != null) {
            ideDto.withOnAppClosed(newDto(OnAppClosedDto.class).withActions(asDto(onAppClosed.getActions())));
        }
        if (onAppLoaded != null) {
            ideDto.withOnAppLoaded(newDto(OnAppLoadedDto.class).withActions(asDto(onAppLoaded.getActions())));
        }
        if (onProjectsLoaded != null) {
            ideDto.withOnProjectsLoaded(newDto(OnProjectsLoadedDto.class).withActions(asDto(onProjectsLoaded.getActions())));
        }
        return ideDto;
    }

    public static AuthorDto asDto(Author author, User user) {
        return newDto(AuthorDto.class).withUserId(author.getUserId())
                                      .withName(user.getName())
                                      .withEmail(user.getEmail())
                                      .withCreated(author.getCreated());
    }

    public static IdeActionDto asDto(Action action) {
        return newDto(IdeActionDto.class).withId(action.getId())
                                         .withProperties(action.getProperties());
    }

    public static List<IdeActionDto> asDto(List<? extends Action> actions) {
        return actions.stream().map(DtoConverter::asDto).collect(toList());
    }

    public static PoliciesDto asDto(Policies policies) {
        return newDto(PoliciesDto.class).withCreate(policies.getCreate())
                                        .withMatch(policies.getMatch())
                                        .withReferer(policies.getReferer())
                                        .withSince(policies.getSince())
                                        .withUntil(policies.getUntil());
    }

    public static ButtonDto asDto(Button button) {
        final ButtonDto buttonDto = newDto(ButtonDto.class);
        if (button.getAttributes() != null) {
            buttonDto.withAttributes(asDto(button.getAttributes()));
        }

        if (button.getType() != null) {
            buttonDto.withType(button.getType());
        }
        return buttonDto;
    }

    public static ButtonAttributesDto asDto(ButtonAttributes attributes) {
        return newDto(ButtonAttributesDto.class).withColor(attributes.getColor())
                                                .withCounter(attributes.getCounter())
                                                .withLogo(attributes.getLogo())
                                                .withStyle(attributes.getStyle());
    }

    private DtoConverter() {}
}
