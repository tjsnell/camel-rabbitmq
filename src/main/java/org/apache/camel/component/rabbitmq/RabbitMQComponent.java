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

import java.net.URI;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.impl.DefaultComponent;
import org.apache.camel.util.ObjectHelper;

/**
 * Represents the component that manages {@link RabbitMQEndpoint}.
 */
public class RabbitMQComponent extends DefaultComponent {

    public RabbitMQComponent() {

    }

    public RabbitMQComponent(CamelContext context) {
        super(context);
    }

    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
        RabitMQConfiguration configuration = new RabitMQConfiguration();
        setProperties(configuration, parameters);

        if (remaining == null || remaining.trim().length() == 0) {
            throw new IllegalArgumentException("Queljlkjlkjlkjlue name must be specified.");
        }

        URI mqURI = new URI(uri);

        configure(configuration, mqURI);

        Endpoint endpoint = new RabbitMQEndpoint(uri, this, configuration);
        setProperties(endpoint, parameters);
        return endpoint;
    }

    private void configure(RabitMQConfiguration config, URI uri) {
        // UserInfo can contain both username and password as: user:pwd@ftpserver
        // see: http://en.wikipedia.org/wiki/URI_scheme
        String username = uri.getUserInfo();
        String pw = null;
        if (username != null && username.contains(":")) {
            pw = ObjectHelper.after(username, ":");
            username = ObjectHelper.before(username, ":");
        }
        if (username != null) {
            config.setUserName(username);
        }
        if (pw != null) {
            config.setPassword(pw);
        }

        config.setHost(uri.getHost());
        if (uri.getPort() != 0) {
            config.setPort(uri.getPort());
        }
        if (uri.getPath() != null) {
            String vhost = uri.getPath();
            if (vhost.startsWith("/")) {
                vhost = vhost.substring(1);
            }
            config.setVirtualHost(vhost);
        }
    }
}
