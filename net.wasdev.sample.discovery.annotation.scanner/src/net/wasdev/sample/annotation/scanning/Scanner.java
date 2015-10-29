package net.wasdev.sample.annotation.scanning;

import java.util.HashMap;
import java.util.Map;

import javax.management.DynamicMBean;

import org.osgi.service.component.ComponentContext;

import com.ibm.websphere.endpoint.EndPointInfoMBean;
import com.ibm.ws.container.service.annotations.WebAnnotations;
import com.ibm.ws.container.service.app.deploy.ModuleInfo;
import com.ibm.ws.container.service.app.deploy.WebModuleInfo;
import com.ibm.ws.container.service.state.ModuleStateListener;
import com.ibm.ws.container.service.state.StateChangeException;
import com.ibm.wsspi.adaptable.module.Adaptable;

public class Scanner implements ModuleStateListener {

	private ConsulServicePublisher servicePublisher = null;
	private final Map<String, RestModule> modules = new HashMap<String, RestModule>();

	private int port;
	private String host;
	private String consulServer;
	private String consulEnv;

	public void bindBean(DynamicMBean mbean) {
		if (mbean instanceof EndPointInfoMBean) {
			EndPointInfoMBean endpointInfo = (EndPointInfoMBean) mbean;
			// If there are multiple ports, just take the
			// last one to start. At the moment, things work better if
			// there's only one port.
			port = endpointInfo.getPort();
			host = endpointInfo.getHost();
		} else {
			System.err.println("Unrecognised instance: " + mbean);
		}
	}

	public void unbindBean(DynamicMBean yay) {
		// This will already be captured by module state events
	}

	private void unregisterEndpoints(WebModuleInfo info) {
		String name = info.getContextRoot();
		System.out.println("Destroying " + name);
		RestModule module = modules.get(name);
		servicePublisher.unregister(module);
		modules.remove(name);
	}

	private void registerEndpoints(WebModuleInfo info) {
		try {
			Adaptable containerToAdapt = info.getContainer();
			WebAnnotations webAnnotations = containerToAdapt
					.adapt(WebAnnotations.class);
			String name = info.getContextRoot();
			if (host != null) {
				RestModule module = new RestModule(host, port, name,
						webAnnotations);

				modules.put(name, module);
				servicePublisher.init(module);
			} else {
				// This is really unexpected
				System.err.println("Could not register " + info
						+ " since no HTTP endpoints are known.");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void moduleStarted(ModuleInfo info) throws StateChangeException {
		if (info instanceof WebModuleInfo) {
			registerEndpoints((WebModuleInfo) info);
		} else {
			System.out.println("Ignoring start of non-web-module " + info);
		}

	}

	@Override
	public void moduleStarting(ModuleInfo arg0) throws StateChangeException {
		// Do nothing
	}

	@Override
	public void moduleStopped(ModuleInfo arg0) {
		// Do nothing
	}

	@Override
	public void moduleStopping(ModuleInfo info) {
		if (info instanceof WebModuleInfo) {
			unregisterEndpoints((WebModuleInfo) info);
		} else {
			System.out.println("Ignoring stop of non-web-module " + info);
		}
	}

	protected void activate(ComponentContext cc) {

		Map<String, String> env = System.getenv();

		// Prefer an environment variable if one is set
		consulEnv = env.get("CONSUL_SERVER");
		if (consulEnv == null) {

			consulServer = (String) cc.getProperties().get("server");
		} else {
			consulServer = consulEnv;
		}
		servicePublisher = new ConsulServicePublisher(consulServer);

	}

	protected void modified(Map<?, ?> newProperties) {
		// If we're using the environment variable, don't do anything
		if (consulEnv != null) {
			consulServer = (String) newProperties.get("server");
			// Redo any registrations
			servicePublisher = new ConsulServicePublisher(consulServer);
		}

	}

}
