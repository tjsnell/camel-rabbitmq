/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.component.rabbitmq;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import com.rabbitmq.client.SaslConfig;

public class RabitMQConfiguration {
    private String userName;
    private String password;
    private String uri; //todo maybe not?
    private String virtualHost;
    private String host;
    private int port;
    private int timeout;
    private int requestedChannelMax;
    private int requestedFrameMax;
    private int requestedHeartbeat;
    private SaslConfig saslConfig;

    private boolean durable;
    private boolean autoDelete;
    /**
     * maximum number of messages that the server will deliver, 0 if unlimited
     */
    private int prefetchCount;

    private String routingKey = "";
    private String exchange = "";
    private String queue = "";

    // consumer only
    private List<String> bindingKeys = new ArrayList<String>();

    private String bindingKey = "";
    private String exchangeType = "";

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getVirtualHost() {
        return virtualHost;
    }

    public void setVirtualHost(String virtualHost) {
        this.virtualHost = virtualHost;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getRequestedChannelMax() {
        return requestedChannelMax;
    }

    public void setRequestedChannelMax(int requestedChannelMax) {
        this.requestedChannelMax = requestedChannelMax;
    }

    public int getRequestedFrameMax() {
        return requestedFrameMax;
    }

    public void setRequestedFrameMax(int requestedFrameMax) {
        this.requestedFrameMax = requestedFrameMax;
    }

    public int getRequestedHeartbeat() {
        return requestedHeartbeat;
    }

    public void setRequestedHeartbeat(int requestedHeartbeat) {
        this.requestedHeartbeat = requestedHeartbeat;
    }

    public SaslConfig getSaslConfig() {
        return saslConfig;
    }

    public void setSaslConfig(SaslConfig saslConfig) {
        this.saslConfig = saslConfig;
    }

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public String getQueue() {
        return queue;
    }

    public void setQueue(String queue) {
        this.queue = queue;
    }

    public boolean getDurable() {
        return durable;
    }

    public void setDurable(boolean durable) {
        this.durable = durable;
    }

    public boolean getAutoDelete() {
        return autoDelete;
    }

    public void setAutoDelete(boolean autoDelete) {
        this.autoDelete = autoDelete;
    }

    public int getPrefetchCount() {
        return prefetchCount;
    }

    public void setPrefetchCount(int prefetchCount) {
        this.prefetchCount = prefetchCount;
    }

    public String getRoutingKey() {
        return routingKey;
    }

    public void setRoutingKey(String routingKey) {
        this.routingKey = routingKey;
    }

    public List<String> getBindingKeys() {
        return bindingKeys;
    }


    public void setBindingKey(String bindingKey) {
        StringTokenizer tok = new StringTokenizer(bindingKey, ",");
        bindingKeys.clear();
        while (tok.hasMoreElements()) {
            bindingKeys.add(tok.nextToken());
        }
    }

    public String getExchangeType() {
        return exchangeType;
    }

    public void setExchangeType(String exchangeType) {
        this.exchangeType = exchangeType;
    }

    public String getExchangeTypej() {
        String type = "fanout";
        type = "direct";
        if (bindingKeys.size() > 0) {
            type = "direct"; // todo when is it a topic?
        } else if (!routingKey.isEmpty()) {
            type = "direct";
        }
        return type;
    }

    public String toString() {
        StringBuilder str = new StringBuilder();

        str.append("Configuration\n");
        str.append("\tHost: ");
        str.append(host);
        str.append("\n\tPort: ");
        str.append(port);
        str.append("\n\tQueue: ");
        str.append(queue);
        str.append("\n\tExchange: ");
        str.append(exchange);
        str.append("\n\tExchange Type: ");
        str.append(getExchangeType());
        str.append("\n\tRouting Key: ");
        str.append(routingKey);
        str.append("\n\tBinding Keys: ");
        str.append(bindingKeys);
        str.append("\n\tDurable: ");
        str.append(durable);

        return str.toString();
    }
}
