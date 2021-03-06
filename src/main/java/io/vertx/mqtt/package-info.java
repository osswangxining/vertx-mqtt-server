/*
 * Copyright 2016 Red Hat Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * = Vert.x MQTT server
 *
 * This component provides a server which is able to handle connections, communication and messages exchange with remote
 * link:http://mqtt.org/[MQTT] clients. Its API provides a bunch of events related to raw protocol messages received by
 * clients and exposes some features in order to send messages to them.
 *
 * It's not a fully featured MQTT broker but can be used for building something like that or for protocol translation.
 *
 * WARNING: this module has the tech preview status, this means the API can change between versions.
 *
 * == Using Vert.x MQTT server
 *
 * This component had officially released in the Vert.x stack, just following dependency to the _dependencies_ section
 * of your build descriptor:
 *
 * * Maven (in your `pom.xml`):
 *
 * [source,xml,subs="+attributes"]
 * ----
 * <dependency>
 *     <groupId>io.vertx</groupId>
 *     <artifactId>vertx-mqtt-server</artifactId>
 *     <version>${maven.version}</version>
 * </dependency>
 * ----
 *
 * * Gradle (in your `build.gradle` file):
 *
 * [source,groovy,subs="+attributes"]
 * ----
 * compile io.vertx:vertx-mqtt-server:${maven.version}
 * ----
 *
 * == Getting Started
 *
 * === Handling client connection/disconnection
 *
 * This example shows how it's possible to handle the connection request from a remote MQTT client. First, an
 * {@link io.vertx.mqtt.MqttServer} instance is created and the {@link io.vertx.mqtt.MqttServer#endpointHandler(io.vertx.core.Handler)} method is used to specify the handler called
 * when a remote client sends a CONNECT message for connecting to the server itself. The {@link io.vertx.mqtt.MqttEndpoint}
 * instance, provided as parameter to the handler, brings all main information related to the CONNECT message like client identifier,
 * username/password, "will" information, clean session flag, protocol version and the "keep alive" timeout.
 * Inside that handler, the _endpoint_ instance provides the {@link io.vertx.mqtt.MqttEndpoint#accept(boolean)} method
 * for replying to the remote client with the corresponding CONNACK message : in this way, the connection is established.
 * Finally, the server is started using the {@link io.vertx.mqtt.MqttServer#listen(io.vertx.core.Handler)} method with
 * the default behavior (on localhost and default MQTT port 1883). The same method allows to specify an handler in order
 * to check if the server is started properly or not.
 *
 * [source,$lang]
 * ----
 * {@link examples.VertxMqttServerExamples#example1}
 * ----
 *
 * The same _endpoint_ instance provides the {@link io.vertx.mqtt.MqttEndpoint#disconnectHandler(io.vertx.core.Handler)}
 * for specifying the handler called when the remote client sends a DISCONNECT message in order to disconnect from the server;
 * this handler takes no parameters.
 *
 * [source,$lang]
 * ----
 * {@link examples.VertxMqttServerExamples#example2}
 * ----
 *
 * === Handling client connection/disconnection with SSL/TLS support
 *
 * The server has the support for accepting connection requests through the SSL/TLS protocol for authentication and encryption.
 * In order to do that, the {@link io.vertx.mqtt.MqttServerOptions} class provides the {@link io.vertx.mqtt.MqttServerOptions#setSsl(boolean)} method
 * for setting the usage of SSL/TLS (passing 'true' as value) and some other useful methods for providing server certificate and
 * related private key (as Java key store reference, PEM or PFX format). In the following example, the
 * {@link io.vertx.mqtt.MqttServerOptions#setKeyCertOptions(io.vertx.core.net.KeyCertOptions)} method is used in order to
 * pass the certificates in PEM format. This method requires an instance of the possible implementations of the
 * {@link io.vertx.core.net.KeyCertOptions} interface and in this case the {@link io.vertx.core.net.PemKeyCertOptions} class
 * is used in order to provide the path for the server certificate and the private key with the correspondent
 * {@link io.vertx.core.net.PemKeyCertOptions#setCertPath(java.lang.String)} and
 * {@link io.vertx.core.net.PemKeyCertOptions#setKeyPath(java.lang.String)} methods.
 * The MQTT server is started passing the Vert.x instance as usual and the above MQTT options instance to the creation method.
 *
 * [source,$lang]
 * ----
 * {@link examples.VertxMqttServerExamples#example3}
 * ----
 *
 * All the other stuff related to handle endpoint connection and related disconnection is managed in the same way without SSL/TLS support.
 *
 * === Handling client subscription/unsubscription request
 *
 * After a connection is established between client and server, the client can send a subscription request for a topic
 * using the SUBSCRIBE message. The {@link io.vertx.mqtt.MqttEndpoint} interface allows to specify an handler for the
 * incoming subscription request using the {@link io.vertx.mqtt.MqttEndpoint#subscribeHandler(io.vertx.core.Handler)} method.
 * Such handler receives an instance of the {@link io.vertx.mqtt.messages.MqttSubscribeMessage} interface which brings
 * the list of topics with related QoS levels as desired by the client.
 * Finally, the endpoint instance provides the {@link io.vertx.mqtt.MqttEndpoint#subscribeAcknowledge(int, java.util.List)} method
 * for replying to the client with the related SUBACK message containing the granted QoS levels.
 *
 * [source,$lang]
 * ----
 * {@link examples.VertxMqttServerExamples#example4}
 * ----
 *
 * In the same way, it's possible to use the {@link io.vertx.mqtt.MqttEndpoint#unsubscribeHandler(io.vertx.core.Handler)} method
 * on the endpoint in order to specify the handler called when the client sends an UNSUBSCRIBE message. This handler receives
 * an instance of the {@link io.vertx.mqtt.messages.MqttUnsubscribeMessage} interface as parameter with the list of topics to unsubscribe.
 * Finally, the endpoint instance provides the {@link io.vertx.mqtt.MqttEndpoint#unsubscribeAcknowledge(int)} method
 * for replying to the client with the related UNSUBACK message.
 *
 * [source,$lang]
 * ----
 * {@link examples.VertxMqttServerExamples#example5}
 * ----
 *
 * === Handling client published message
 *
 * In order to handle incoming messages published by the remote client, the {@link io.vertx.mqtt.MqttEndpoint} interface provides
 * the {@link io.vertx.mqtt.MqttEndpoint#publishHandler(io.vertx.core.Handler)} method for specifying the handler called
 * when the client sends a PUBLISH message. This handler receives an instance of the {@link io.vertx.mqtt.messages.MqttPublishMessage}
 * interface as parameter with the payload, the QoS level, the duplicate and retain flags.
 *
 * If the QoS level is 0 (AT_MOST_ONCE), there is no need from the endpoint to reply the client.
 *
 * If the QoS level is 1 (AT_LEAST_ONCE), the endpoind needs to reply with a PUBACK message using the
 * available {@link io.vertx.mqtt.MqttEndpoint#publishAcknowledge(int)} method.
 *
 * If the QoS level is 2 (EXACTLY_ONCE), the endpoint needs to reply with a PUBREC message using the
 * available {@link io.vertx.mqtt.MqttEndpoint#publishReceived(int)} method; in this case the same endpoint should handle
 * the PUBREL message received from the client as well (the remote client sends it after receiving the PUBREC from the endpoint)
 * and it can do that specifying the handler through the {@link io.vertx.mqtt.MqttEndpoint#publishReleaseHandler(io.vertx.core.Handler)} method.
 * In order to close the QoS level 2 delivery, the endpoint can use the {@link io.vertx.mqtt.MqttEndpoint#publishComplete(int)} method
 * for sending the PUBCOMP message to the client.
 *
 * [source,$lang]
 * ----
 * {@link examples.VertxMqttServerExamples#example6}
 * ----
 *
 * === Publish message to the client
 *
 * The endpoint can publish a message to the remote client (sending a PUBLISH message) using the
 * {@link io.vertx.mqtt.MqttEndpoint#publish(java.lang.String, io.vertx.core.buffer.Buffer, io.netty.handler.codec.mqtt.MqttQoS, boolean, boolean)} method
 * which takes the following input parameters : the topic to publish, the payload, the QoS level, the duplicate and retain flags.
 *
 * If the QoS level is 0 (AT_MOST_ONCE), the endpoint won't receiving any feedback from the client.
 *
 * If the QoS level is 1 (AT_LEAST_ONCE), the endpoint needs to handle the PUBACK message received from the client
 * in order to receive final acknowledge of delivery. It's possible using the {@link io.vertx.mqtt.MqttEndpoint#publishAcknowledgeHandler(io.vertx.core.Handler)} method
 * specifying such an handler.
 *
 * If the QoS level is 2 (EXACTLY_ONCE), the endpoint needs to handle the PUBREC message received from the client.
 * The {@link io.vertx.mqtt.MqttEndpoint#publishReceivedHandler(io.vertx.core.Handler)} method allows to specify
 * the handler for that. Inside that handler, the endpoint can use the {@link io.vertx.mqtt.MqttEndpoint#publishRelease(int)} method
 * for replying to the client with the PUBREL message. The last step is to handle the PUBCOMP message received from the client
 * as final acknowledge for the published message; it's possible using the {@link io.vertx.mqtt.MqttEndpoint#publishCompleteHandler(io.vertx.core.Handler)}
 * for specifying the handler called when the final PUBCOMP message is received.
 *
 * [source,$lang]
 * ----
 * {@link examples.VertxMqttServerExamples#example7}
 * ----
 *
 * === Be notified by client keep alive
 *
 * The underlying MQTT keep alive mechanism is handled by the server internally. When the CONNECT message is received,
 * the server takes care of the keep alive timeout specified inside that message in order to check if the client doesn't
 * send messages in such timeout. At same time, for every PINGREQ received, the server replies with the related PINGRESP.
 *
 * Even if there is no need for the high level application to handle that, the {@link io.vertx.mqtt.MqttEndpoint} interface
 * provides the {@link io.vertx.mqtt.MqttEndpoint#pingHandler(io.vertx.core.Handler)} method for specifying an handler
 * called when a PINGREQ message is received from the client. It's just a notification to the application that the client
 * isn't sending meaningful messages but only pings for keeping alive; in any case the PINGRESP is automatically sent
 * by the server internally as described above.
 *
 * [source,$lang]
 * ----
 * {@link examples.VertxMqttServerExamples#example8}
 * ----
 *
 * === Closing the server
 *
 * The {@link io.vertx.mqtt.MqttServer} interface provides the {@link io.vertx.mqtt.MqttServer#close(io.vertx.core.Handler)} method
 * that can be used for closing the server; it stops to listen for incoming connections and closes all the active connections
 * with remote clients. This method is asynchronous and one overload provides the possibility to specify a complention handler
 * that will be called when the server is really closed.
 *
 * [source,$lang]
 * ----
 * {@link examples.VertxMqttServerExamples#example9}
 * ----
 *
 * === Automatic clean-up in verticles
 *
 * If you’re creating MQTT servers from inside verticles, those servers will be automatically closed when the verticle is undeployed.
 *
 * === Scaling : sharing MQTT servers
 *
 * The handlers related to the MQTT server are always executed in the same event loop thread. It means that on a system with
 * more cores, only one instance is deployed so only one core is used. In order to use more cores, it's possible to deploy
 * more instances of the MQTT server.
 *
 * It's possible to do that programmatically:
 *
 * [source,$lang]
 * ----
 * {@link examples.VertxMqttServerExamples#example10}
 * ----
 *
 * or using a verticle specifying the number of instances:
 *
 * [source,$lang]
 * ----
 * {@link examples.VertxMqttServerExamples#example11}
 * ----
 *
 * What's really happen is that even only MQTT server is deployed but as incoming connections arrive, Vert.x distributes
 * them in a round-robin fashion to any of the connect handlers executed on different cores.
 */
@Document(fileName = "index.adoc")
@ModuleGen(name = "vertx-mqtt-server", groupPackage = "io.vertx")
package io.vertx.mqtt;

import io.vertx.codegen.annotations.ModuleGen;
import io.vertx.docgen.Document;
