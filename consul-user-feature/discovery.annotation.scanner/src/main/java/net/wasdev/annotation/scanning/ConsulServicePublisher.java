/**
 * (C) Copyright IBM Corporation 2015.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.wasdev.annotation.scanning;

import java.security.MessageDigest;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.ecwid.consul.transport.TransportException;
import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.agent.model.NewService;

public class ConsulServicePublisher {

	Set<String> serviceIds = new HashSet<String>();
	final ConsulClient client;
	private final String consulServer;

	public ConsulServicePublisher(String consulServer) {
		this.consulServer = consulServer;
		client = new ConsulClient(consulServer);
	}

	public ConsulServicePublisher(String consulServer, Integer consulPort) {
		this.consulServer = consulServer;
		client = new ConsulClient(consulServer, consulPort);
	}

	/**
	 * Generates a unique name for a service but ensure that
	 * if the same service is registered it will produce the same
	 * ID. This stops multiple registrations from the same Liberty
	 * server for the same service.
	 * 
	 * @param name name of the service
	 * @return ID to use
	 */
	private String generateID(Endpoint endpoint) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(endpoint.getId().getBytes());
			StringBuilder builder = new StringBuilder();
			for(int i = 0; i < hash.length; i++) {
				builder.append(Long.toHexString(0xff & hash[i]));
			}
			return endpoint.getName().replace('/', '_') + builder.toString();
		} catch (Exception e) {
			//fallback option for generating a unique ID
			return endpoint.getName().replace('/', '_') + UUID.randomUUID();
		}
	}
	
	public void init(RestModule module) {
		Collection<Endpoint> endpoints = module.getEndpoints();
		System.out.println("Will register " + endpoints.size()
				+ " endpoints for this module.");
		for (final Endpoint endpoint : endpoints) {
			final NewService service = new NewService();
			String name = endpoint.getPath().replaceAll("/", "");
			// Make sure the id is unique even if multiple instances of a
			// service exist
			String id = generateID(endpoint);
			System.out.println("Registering service with id " + id + " at " + endpoint.getName());
			service.setId(id);
			serviceIds.add(id);
			// Use the path, not the classname, as an identifier for the service
			service.setName(name);
			service.setPort(endpoint.getPort());
			String hostAddress = endpoint.getHost();
			service.setAddress(hostAddress);

			// Add a health check, in case the service doesn't exit gracefully
			NewService.Check check = new NewService.Check();
			// We assume nothing important happens when pinging the application
			// path
			String pingableAddress = hostAddress + ":" + endpoint.getPort()
					+ endpoint.getHealthCheckPath();
			System.out.println("Registering consul health check at "
					+ pingableAddress);
			check.setHttp(pingableAddress);
			service.setCheck(check);

			// register new service
			// This needs to be asynchronous to avoid hanging startup
			// when
			// there is no consul agent running. Use a simple mechanism!
			Thread thread = new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						client.agentServiceRegister(service);
						System.out.println("Service registered. ");
					} catch (TransportException e) {
						System.out.println("Could not register " + endpoint
								+ " because no consul server was running on "
								+ consulServer);
					}
				}
			});
			thread.start();
		}
	}

	public void unregister(RestModule module) {
		System.out.println("Unregistering " + module);

		// deregister all services
		for (String serviceId : serviceIds) {
			try {
				client.agentServiceDeregister(serviceId);
			} catch (TransportException e) {
				System.out.println("Could not unregister " + serviceId
						+ " because no consul server was running.");
			}
		}

	}

}
