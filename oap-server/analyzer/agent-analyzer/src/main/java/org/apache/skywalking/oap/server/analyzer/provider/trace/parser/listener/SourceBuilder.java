/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.skywalking.oap.server.analyzer.provider.trace.parser.listener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.skywalking.apm.network.common.v3.KeyStringValuePair;
import org.apache.skywalking.oap.server.core.analysis.Layer;
import org.apache.skywalking.oap.server.library.util.StringUtil;
import org.apache.skywalking.oap.server.core.config.NamingControl;
import org.apache.skywalking.oap.server.core.source.All;
import org.apache.skywalking.oap.server.core.source.DatabaseAccess;
import org.apache.skywalking.oap.server.core.source.DetectPoint;
import org.apache.skywalking.oap.server.core.source.Endpoint;
import org.apache.skywalking.oap.server.core.source.EndpointRelation;
import org.apache.skywalking.oap.server.core.source.RequestType;
import org.apache.skywalking.oap.server.core.source.Service;
import org.apache.skywalking.oap.server.core.source.ServiceInstance;
import org.apache.skywalking.oap.server.core.source.ServiceInstanceRelation;
import org.apache.skywalking.oap.server.core.source.ServiceMeta;
import org.apache.skywalking.oap.server.core.source.ServiceRelation;

@RequiredArgsConstructor
class SourceBuilder {
    private final NamingControl namingControl;

    @Getter
    @Setter
    private String sourceServiceName;
    @Getter
    @Setter
    private Layer sourceLayer;
    @Getter
    @Setter
    private boolean isSourceNormal = true;
    @Getter
    @Setter
    private String sourceServiceInstanceName;
    /**
     * Source endpoint could be not owned by {@link #sourceServiceName}, such as in the MQ or un-instrumented proxy
     * cases. This service always comes from the span.ref, so it is always a normal service.
     */
    @Getter
    @Setter
    private String sourceEndpointOwnerServiceName;
    @Getter
    @Setter
    private String sourceEndpointName;
    @Getter
    @Setter
    private String destServiceName;
    @Getter
    @Setter
    private Layer destLayer;
    @Getter
    @Setter
    private boolean isDestNormal = true;
    @Getter
    @Setter
    private String destServiceInstanceName;
    @Getter
    @Setter
    private String destEndpointName;
    @Getter
    @Setter
    private int componentId;
    @Getter
    @Setter
    private int latency;
    @Getter
    @Setter
    private boolean status;
    @Getter
    @Setter
    @Deprecated
    private int responseCode;
    @Getter
    @Setter
    private int httpResponseStatusCode;
    @Getter
    @Setter
    private String rpcStatusCode;
    @Getter
    @Setter
    private RequestType type;
    @Getter
    @Setter
    private DetectPoint detectPoint;
    @Getter
    @Setter
    private long timeBucket;
    @Getter
    private final List<String> tags = new ArrayList<>();
    @Getter
    private final Map<String, String> originalTags = new HashMap<>();

    void prepare() {
        this.sourceServiceName = namingControl.formatServiceName(sourceServiceName);
        this.sourceEndpointOwnerServiceName = namingControl.formatServiceName(sourceEndpointOwnerServiceName);
        this.sourceServiceInstanceName = namingControl.formatInstanceName(sourceServiceInstanceName);
        this.sourceEndpointName = namingControl.formatEndpointName(sourceServiceName, sourceEndpointName);
        this.destServiceName = namingControl.formatServiceName(destServiceName);
        this.destServiceInstanceName = namingControl.formatInstanceName(destServiceInstanceName);
        this.destEndpointName = namingControl.formatEndpointName(destServiceName, destEndpointName);
    }

    /**
     * The global level metrics source
     */
    All toAll() {
        All all = new All();
        all.setName(destServiceName);
        all.setServiceInstanceName(destServiceInstanceName);
        all.setEndpointName(destEndpointName);
        all.setLatency(latency);
        all.setStatus(status);
        all.setResponseCode(responseCode);
        all.setHttpResponseStatusCode(httpResponseStatusCode);
        all.setRpcStatusCode(rpcStatusCode);
        all.setType(type);
        all.setTimeBucket(timeBucket);
        all.setTags(tags);
        all.setOriginalTags(originalTags);
        return all;
    }

    /**
     * Service meta and metrics related source of {@link #destServiceName}. The metrics base on the OAL scripts.
     */
    Service toService() {
        Service service = new Service();
        service.setName(destServiceName);
        service.setServiceInstanceName(destServiceInstanceName);
        service.setEndpointName(destEndpointName);
        service.setLayer(destLayer);
        service.setNormal(isDestNormal);
        service.setLatency(latency);
        service.setStatus(status);
        service.setResponseCode(responseCode);
        service.setHttpResponseStatusCode(httpResponseStatusCode);
        service.setRpcStatusCode(rpcStatusCode);
        service.setType(type);
        service.setTags(tags);
        service.setTimeBucket(timeBucket);
        service.setOriginalTags(originalTags);
        return service;
    }

    /**
     * Service topology meta and metrics related source. The metrics base on the OAL scripts.
     */
    ServiceRelation toServiceRelation() {
        ServiceRelation serviceRelation = new ServiceRelation();
        serviceRelation.setSourceServiceName(sourceServiceName);
        serviceRelation.setSourceNormal(isSourceNormal);
        serviceRelation.setSourceServiceInstanceName(sourceServiceInstanceName);
        serviceRelation.setDestServiceName(destServiceName);
        serviceRelation.setDestNormal(isDestNormal);
        serviceRelation.setDestServiceInstanceName(destServiceInstanceName);
        serviceRelation.setEndpoint(destEndpointName);
        serviceRelation.setComponentId(componentId);
        serviceRelation.setLatency(latency);
        serviceRelation.setStatus(status);
        serviceRelation.setResponseCode(responseCode);
        serviceRelation.setHttpResponseStatusCode(httpResponseStatusCode);
        serviceRelation.setRpcStatusCode(rpcStatusCode);
        serviceRelation.setType(type);
        serviceRelation.setDetectPoint(detectPoint);
        serviceRelation.setTimeBucket(timeBucket);
        return serviceRelation;
    }

    /**
     * Service instance meta and metrics of {@link #destServiceInstanceName} related source. The metrics base on the OAL
     * scripts.
     */
    ServiceInstance toServiceInstance() {
        ServiceInstance serviceInstance = new ServiceInstance();
        serviceInstance.setName(destServiceInstanceName);
        serviceInstance.setServiceName(destServiceName);
        serviceInstance.setServiceNormal(isDestNormal);
        serviceInstance.setLayer(destLayer);
        serviceInstance.setEndpointName(destEndpointName);
        serviceInstance.setLatency(latency);
        serviceInstance.setStatus(status);
        serviceInstance.setResponseCode(responseCode);
        serviceInstance.setHttpResponseStatusCode(httpResponseStatusCode);
        serviceInstance.setRpcStatusCode(rpcStatusCode);
        serviceInstance.setType(type);
        serviceInstance.setTags(tags);
        serviceInstance.setOriginalTags(originalTags);
        serviceInstance.setTimeBucket(timeBucket);
        return serviceInstance;
    }

    /**
     * Service instance topology/dependency meta and metrics related source. The metrics base on the OAL scripts.
     */
    ServiceInstanceRelation toServiceInstanceRelation() {
        if (StringUtil.isEmpty(sourceServiceInstanceName) || StringUtil.isEmpty(destServiceInstanceName)) {
            return null;
        }
        ServiceInstanceRelation serviceInstanceRelation = new ServiceInstanceRelation();
        serviceInstanceRelation.setSourceServiceName(sourceServiceName);
        serviceInstanceRelation.setSourceServiceNormal(isSourceNormal);
        serviceInstanceRelation.setSourceServiceInstanceName(sourceServiceInstanceName);
        serviceInstanceRelation.setDestServiceName(destServiceName);
        serviceInstanceRelation.setDestServiceNormal(isDestNormal);
        serviceInstanceRelation.setDestServiceInstanceName(destServiceInstanceName);
        serviceInstanceRelation.setEndpoint(destEndpointName);
        serviceInstanceRelation.setComponentId(componentId);
        serviceInstanceRelation.setLatency(latency);
        serviceInstanceRelation.setStatus(status);
        serviceInstanceRelation.setResponseCode(responseCode);
        serviceInstanceRelation.setHttpResponseStatusCode(httpResponseStatusCode);
        serviceInstanceRelation.setRpcStatusCode(rpcStatusCode);
        serviceInstanceRelation.setType(type);
        serviceInstanceRelation.setDetectPoint(detectPoint);
        serviceInstanceRelation.setTimeBucket(timeBucket);
        return serviceInstanceRelation;
    }

    /**
     * Endpoint meta and metrics of {@link #destEndpointName} related source. The metrics base on the OAL scripts.
     */
    Endpoint toEndpoint() {
        Endpoint endpoint = new Endpoint();
        endpoint.setName(destEndpointName);
        endpoint.setServiceName(destServiceName);
        endpoint.setServiceNormal(isDestNormal);
        endpoint.setServiceInstanceName(destServiceInstanceName);
        endpoint.setLatency(latency);
        endpoint.setStatus(status);
        endpoint.setResponseCode(responseCode);
        endpoint.setHttpResponseStatusCode(httpResponseStatusCode);
        endpoint.setRpcStatusCode(rpcStatusCode);
        endpoint.setType(type);
        endpoint.setTags(tags);
        endpoint.setOriginalTags(originalTags);
        endpoint.setTimeBucket(timeBucket);
        return endpoint;
    }

    /**
     * Endpoint depedency meta and metrics related source. The metrics base on the OAL scripts.
     */
    EndpointRelation toEndpointRelation() {
        if (StringUtil.isEmpty(sourceEndpointName) || StringUtil.isEmpty(destEndpointName)) {
            return null;
        }
        EndpointRelation endpointRelation = new EndpointRelation();
        endpointRelation.setEndpoint(sourceEndpointName);
        if (sourceEndpointOwnerServiceName == null) {
            endpointRelation.setServiceName(sourceServiceName);
            endpointRelation.setServiceNormal(isSourceNormal);
        } else {
            endpointRelation.setServiceName(sourceEndpointOwnerServiceName);
            endpointRelation.setServiceNormal(true);
        }
        endpointRelation.setServiceInstanceName(sourceServiceInstanceName);
        endpointRelation.setChildEndpoint(destEndpointName);
        endpointRelation.setChildServiceName(destServiceName);
        endpointRelation.setChildServiceNormal(isDestNormal);
        endpointRelation.setChildServiceInstanceName(destServiceInstanceName);
        endpointRelation.setComponentId(componentId);
        endpointRelation.setRpcLatency(latency);
        endpointRelation.setStatus(status);
        endpointRelation.setResponseCode(responseCode);
        endpointRelation.setHttpResponseStatusCode(httpResponseStatusCode);
        endpointRelation.setRpcStatusCode(rpcStatusCode);
        endpointRelation.setType(type);
        endpointRelation.setDetectPoint(detectPoint);
        endpointRelation.setTimeBucket(timeBucket);
        return endpointRelation;
    }

    /**
     * Service meta is only for building the service list, but wouldn't be same as {@link #toService()}, which could
     * generate traffic and metrics both.
     */
    ServiceMeta toServiceMeta() {
        ServiceMeta service = new ServiceMeta();
        service.setName(destServiceName);
        service.setLayer(destLayer);
        service.setNormal(isDestNormal);
        service.setTimeBucket(timeBucket);
        return service;
    }

    /**
     * Database traffic metrics source. The metrics base on the OAL scripts.
     */
    DatabaseAccess toDatabaseAccess() {
        if (!RequestType.DATABASE.equals(type)) {
            return null;
        }
        DatabaseAccess databaseAccess = new DatabaseAccess();
        databaseAccess.setDatabaseTypeId(componentId);
        databaseAccess.setLatency(latency);
        databaseAccess.setName(destServiceName);
        databaseAccess.setStatus(status);
        databaseAccess.setTimeBucket(timeBucket);
        return databaseAccess;
    }

    public void setTag(KeyStringValuePair tag) {
        tags.add(tag.getKey().trim() + ":" + tag.getValue().trim());
        originalTags.put(tag.getKey(), tag.getValue());
    }
}
