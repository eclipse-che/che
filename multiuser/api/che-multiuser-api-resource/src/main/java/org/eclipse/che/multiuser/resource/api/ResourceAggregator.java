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
package org.eclipse.che.multiuser.resource.api;

import static java.util.stream.Collectors.toSet;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.multiuser.resource.api.exception.NoEnoughResourcesException;
import org.eclipse.che.multiuser.resource.api.type.ResourceType;
import org.eclipse.che.multiuser.resource.model.Resource;

/**
 * Helps aggregate resources by theirs type.
 *
 * @author Sergii Leschenko
 */
@Singleton
public class ResourceAggregator {
  private final Map<String, ResourceType> resourcesTypes;

  @Inject
  public ResourceAggregator(Set<ResourceType> resourcesTypes) {
    this.resourcesTypes =
        resourcesTypes.stream().collect(Collectors.toMap(ResourceType::getId, Function.identity()));
  }

  /**
   * Aggregates resources of the same type.
   *
   * @param resources resources list which can contain more that one instance for some type
   * @return map where key is resources type and value is aggregated resource
   * @throws IllegalArgumentException when resources list contains resource with not supported type
   */
  public Map<String, Resource> aggregateByType(List<? extends Resource> resources) {
    checkSupporting(resources);

    Map<String, Resource> type2Resource = new HashMap<>();
    for (Resource resource : resources) {
      final Resource resource1 = type2Resource.get(resource.getType());
      if (resource1 != null) {
        type2Resource.put(resource.getType(), aggregate(resource1, resource));
      } else {
        type2Resource.put(resource.getType(), resource);
      }
    }
    return type2Resource;
  }

  /**
   * Returns list which is result of deduction {@code resourceToDeduct} from {@code
   * sourceResources}.
   *
   * @param sourceResources the source resources
   * @param resourcesToDeduct the resources which should be deducted from {@code sourceResources}
   * @throws NoEnoughResourcesException when {@code sourceResources} list doesn't contain enough
   *     resources
   * @throws IllegalArgumentException when {@code sourceResources} or {@code resourcesToDeduct}
   *     contain resource with not supported type
   */
  public List<? extends Resource> deduct(
      List<? extends Resource> sourceResources, List<? extends Resource> resourcesToDeduct)
      throws NoEnoughResourcesException {
    checkSupporting(sourceResources);
    checkSupporting(resourcesToDeduct);

    final Map<String, Resource> result =
        sourceResources.stream().collect(Collectors.toMap(Resource::getType, Function.identity()));
    final List<Resource> missingResources = new ArrayList<>();

    for (Resource toDeduct : resourcesToDeduct) {
      final Resource sourceResource = result.get(toDeduct.getType());
      if (sourceResource != null) {
        try {
          result.put(toDeduct.getType(), deduct(sourceResource, toDeduct));
        } catch (NoEnoughResourcesException e) {
          result.remove(toDeduct.getType());
          missingResources.addAll(e.getMissingResources());
        }
      } else {
        missingResources.add(toDeduct);
      }
    }

    if (!missingResources.isEmpty()) {
      throw new NoEnoughResourcesException(sourceResources, resourcesToDeduct, missingResources);
    }

    return new ArrayList<>(result.values());
  }

  /**
   * Returns list which contains resources from specified {@code sourceResources} which have
   * excessive amount in compare to specified {@code resourcesToCompare}.
   *
   * <p>Example :
   *
   * <pre>
   * |      \      | Source    | To compare| Result   |
   * |:------------|:----------|:----------|:---------|
   * | Resource1   | 5         | 3         | 2        |
   * | ----------- | --------- | --------- | -------- |
   * | Resource2   | -         | 9         | -        |
   * | ----------- | --------- | --------- | -------- |
   * | Resource3   | 1         | -         | 1        |
   * | ----------- | --------- | --------- | -------- |
   * </pre>
   *
   * @param sourceResources the source resources
   * @param resourcesToCompare the resources which should be compared to {@code sourceResources}
   * @throws IllegalArgumentException when {@code sourceResources} or {@code resourcesToCompare}
   *     contain resource with not supported type
   */
  public List<? extends Resource> excess(
      List<? extends Resource> sourceResources, List<? extends Resource> resourcesToCompare) {
    checkSupporting(sourceResources);
    checkSupporting(resourcesToCompare);

    final Map<String, Resource> result =
        sourceResources.stream().collect(Collectors.toMap(Resource::getType, Function.identity()));
    for (Resource toCompare : resourcesToCompare) {
      String resourceType = toCompare.getType();
      final Resource sourceResource = result.get(resourceType);
      if (sourceResource != null) {
        if (sourceResource.getAmount() == toCompare.getAmount()) {
          // source resource doesn't have excessive amount
          result.remove(resourceType);
          continue;
        }
        try {
          Resource excess = deduct(sourceResource, toCompare);
          if (excess.getAmount() == 0) {
            // source resource doesn't have excessive amount
            result.remove(resourceType);
          } else {
            result.put(resourceType, excess);
          }
        } catch (NoEnoughResourcesException e) {
          // source resource doesn't have excessive amount
          result.remove(resourceType);
        }
      }
    }

    return new ArrayList<>(result.values());
  }

  /**
   * Aggregates two resources which have the same type.
   *
   * @param resourceA resources A
   * @param resourceB resource B
   * @return one resources with type {@code T} that is result of aggregating {@code resourceA} and
   *     {@code resourceB}
   * @throws IllegalArgumentException when {@code T} is not supported type
   */
  public Resource aggregate(Resource resourceA, Resource resourceB) {
    final String typeId = resourceA.getType();
    final ResourceType resourceType = getResourceType(typeId);
    return resourceType.aggregate(resourceA, resourceB);
  }

  /**
   * Deducts two resources which have the same type.
   *
   * @param totalResource total resource
   * @param deduction resources which should be deducted from {@code totalResource}
   * @return one resources with type {@code T} that is result of subtraction {@code totalResource}
   *     and {@code deduction}
   * @throws NoEnoughResourcesException when {@code totalResource}'s amount is less than {@code
   *     deduction}'s amount
   * @throws IllegalArgumentException when {@code T} is not supported type
   */
  public Resource deduct(Resource totalResource, Resource deduction)
      throws NoEnoughResourcesException {
    final String typeId = totalResource.getType();
    final ResourceType resourceType = getResourceType(typeId);
    return resourceType.deduct(totalResource, deduction);
  }

  /**
   * Returns resources list that contains resources with types that are contained by both input
   * lists.
   *
   * @throws IllegalArgumentException when {@code resources} list contains resource with not
   *     supported type
   */
  public List<? extends Resource> intersection(
      List<? extends Resource> resourcesA, List<? extends Resource> resourcesB) {
    checkSupporting(resourcesA);
    checkSupporting(resourcesB);

    final Set<String> keysA = resourcesA.stream().map(Resource::getType).collect(toSet());
    final Set<String> keysB = resourcesB.stream().map(Resource::getType).collect(toSet());
    final Set<String> commonKeys = ImmutableSet.copyOf(Sets.intersection(keysA, keysB));
    return Stream.concat(resourcesA.stream(), resourcesB.stream())
        .filter(res -> commonKeys.contains(res.getType()))
        .collect(Collectors.toList());
  }

  /**
   * Returns list that contains one resource with minimum amount for each resource type.
   *
   * @throws IllegalArgumentException when {@code resources} list contains resource with not
   *     supported type
   */
  public List<? extends Resource> min(Collection<? extends Resource> resources) {
    checkSupporting(resources);
    Map<String, Resource> result = new HashMap<>();
    for (Resource resource : resources) {
      String type = resource.getType();
      Resource min = result.get(type);
      if (min == null) {
        result.put(type, resource);
      } else if (resource.getAmount() != -1) {
        if (min.getAmount() == -1 || min.getAmount() > resource.getAmount()) {
          result.put(type, resource);
        }
      }
    }
    return new ArrayList<>(result.values());
  }

  /**
   * Check supporting of all given resources.
   *
   * @param resources resources to check types
   * @throws IllegalArgumentException when {@code resources} list contains resource with not
   *     supported type
   */
  private void checkSupporting(Collection<? extends Resource> resources) {
    final Set<String> resourcesTypes =
        resources.stream().map(Resource::getType).collect(Collectors.toSet());
    for (String resourcesType : resourcesTypes) {
      if (!this.resourcesTypes.containsKey(resourcesType)) {
        throw new IllegalArgumentException(
            String.format("'%s' resource type is not supported", resourcesType));
      }
    }
  }

  /**
   * Returns resources type by given id.
   *
   * @param typeId id of resources type
   * @return resources type by given id
   * @throws IllegalArgumentException when type by given id is not supported type
   */
  private ResourceType getResourceType(String typeId) {
    final ResourceType resourceType = resourcesTypes.get(typeId);
    if (resourceType == null) {
      throw new IllegalArgumentException(
          String.format("'%s' resource type is not supported", typeId));
    }
    return resourceType;
  }
}
