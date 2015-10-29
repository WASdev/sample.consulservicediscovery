package com.ibm.sample.annotation.scanning;

import java.util.*;

import com.ecwid.consul.transport.TransportException;
import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.agent.model.NewService;

public class ConsulServicePublisher {

	private static final Random random = new Random();
	Set<String> serviceIds = new HashSet<String>();
	final ConsulClient client;
	private final String consulServer;

	public ConsulServicePublisher(String consulServer) {
		this.consulServer = consulServer;
		client = new ConsulClient(consulServer);
	}

	public void init(RestModule module) {

		Collection<Endpoint> endpoints = module.getEndpoints();
		System.out.println("Will register " + endpoints.size()
				+ " endpoints for this module.");
		for (final Endpoint endpoint : endpoints) {
			final NewService service = new NewService();
			String name = endpoint.getUri().replaceAll("/", "");

			// Make sure the id is unique even if multiple instances of a
			// service exist
			String id = name.replace('/', '_')
					+ random.nextInt(Integer.MAX_VALUE);
			System.out.println("Registering service with id " + id + " at "
					+ name);
			service.setId(id);
			serviceIds.add(id);
			service.setName(name);
			service.setPort(endpoint.getPort());
			String hostAddress = endpoint.getHost();
			service.setAddress(hostAddress);

			// Add a health check, in case the service doesn't exit gracefully
			NewService.Check check = new NewService.Check();
			// We assume nothing important happens when pinging the application
			// path
			String pingableAddress = hostAddress + ":" + endpoint.getPort()
					+ endpoint.getApplicationPath();
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
