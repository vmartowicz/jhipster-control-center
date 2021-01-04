/*
 * Copyright 2020-2021 the original author or authors from the JHipster project.
 *
 * This file is part of the JHipster project, see https://www.jhipster.tech/
 * for more information.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tech.jhipster.controlcenter.config.gateway;

import static org.springframework.cloud.gateway.filter.factory.RewritePathGatewayFilterFactory.REPLACEMENT_KEY;
import static org.springframework.cloud.gateway.handler.predicate.HeaderRoutePredicateFactory.REGEXP_KEY;
import static org.springframework.cloud.gateway.handler.predicate.RoutePredicateFactory.PATTERN_KEY;
import static org.springframework.cloud.gateway.support.NameUtils.normalizeFilterFactoryName;
import static org.springframework.cloud.gateway.support.NameUtils.normalizeRoutePredicateName;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.gateway.discovery.DiscoveryClientRouteDefinitionLocator;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.filter.factory.RewritePathGatewayFilterFactory;
import org.springframework.cloud.gateway.handler.predicate.PathRoutePredicateFactory;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import reactor.core.publisher.Flux;

/**
 * Dynamic Route Locator based on DiscoveryClient but that expose a route for each individual instances
 * rather than load balancing among instances of the same service
 * <p>
 * Inspired by the default DiscoveryClientRouteDefinitionLocator
 *
 * @see org.springframework.cloud.gateway.discovery.DiscoveryClientRouteDefinitionLocator
 */
public class JHipsterControlCenterRouteDefinitionLocator implements RouteDefinitionLocator {
    private final Logger log = LoggerFactory.getLogger(DiscoveryClientRouteDefinitionLocator.class);
    public static final String GATEWAY_PATH = "/gateway/";

    private final DiscoveryClient discoveryClient;

    public JHipsterControlCenterRouteDefinitionLocator(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    @Override
    public Flux<RouteDefinition> getRouteDefinitions() {
        return Flux
            .fromIterable(discoveryClient.getServices())
            .map(discoveryClient::getInstances)
            .filter(instances -> !instances.isEmpty())
            .map(instances -> instances.get(0))
            .map(JHipsterControlCenterRouteDefinitionLocator::getRouteDefinitionForInstance);
    }

    private static RouteDefinition getRouteDefinitionForInstance(ServiceInstance instance) {
        String instance_route = String
            .format("%s/%s", instance.getServiceId(), Optional.ofNullable(instance.getInstanceId()).orElse(instance.getServiceId()))
            .toLowerCase();

        RouteDefinition routeDefinition = new RouteDefinition();
        routeDefinition.setId(instance_route);
        routeDefinition.setUri(instance.getUri());

        // add a predicate that matches the url at /gateway/$instance_route/**
        PredicateDefinition predicate = new PredicateDefinition();
        predicate.setName(normalizeRoutePredicateName(PathRoutePredicateFactory.class));
        predicate.addArg(PATTERN_KEY, GATEWAY_PATH + instance_route + "/**");
        routeDefinition.getPredicates().add(predicate);

        // add a filter that remove /gateway/$instance_route/ in downstream service call path
        FilterDefinition filter = new FilterDefinition();
        filter.setName(normalizeFilterFactoryName(RewritePathGatewayFilterFactory.class));
        String regex = GATEWAY_PATH + instance_route + "/(?<remaining>.*)";
        String replacement = "/${remaining}";
        filter.addArg(REGEXP_KEY, regex);
        filter.addArg(REPLACEMENT_KEY, replacement);
        routeDefinition.getFilters().add(filter);

        return routeDefinition;
    }
}
