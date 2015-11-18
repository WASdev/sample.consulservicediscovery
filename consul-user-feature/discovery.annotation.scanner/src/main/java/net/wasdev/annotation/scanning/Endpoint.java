package net.wasdev.annotation.scanning;

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

import java.net.*;
import java.util.Enumeration;

public class Endpoint {
	private final String path;
	private final int port;
	private final String host;
	private final String id;	//convenience ID for this end point
	private final String name;

	/**
	 * A path which can be accessed to see if the application is up. Defaults to
	 * the root context.
	 */
	private String healthCheckPath = "/";

	public Endpoint(String host, int port, String path, String name) {
		this.port = port;
		this.path = path;
		// If we're listening on all interfaces, just choose one
		if ("*".equals(host)) {
			this.host = getInternetHost();
		} else {
			this.host = host;
		}
		id = host + port + path;
		this.name = (name == null) ? id : name;
	}

	/**
	 * Looks for a network address, preferring ones which are valid IP
	 * addresses, and which are not the loopback interface (which some OSes will
	 * default to for the getLocalHost() method)
	 */
	private String getInternetHost() {
		String inetHost = "unknown";
		try {

			Enumeration<NetworkInterface> n = NetworkInterface
					.getNetworkInterfaces();
			while (n.hasMoreElements()) {
				NetworkInterface e = n.nextElement();
				System.out
						.println("Checking network interface: " + e.getName());
				Enumeration<InetAddress> a = e.getInetAddresses();
				while (a.hasMoreElements()) {
					InetAddress addr = a.nextElement();
					System.out.println(addr.getHostAddress());
					if (!addr.isLoopbackAddress()
							&& addr instanceof Inet4Address) {
						System.out.println("Using " + addr.getHostAddress());
						return addr.getHostAddress();
					}
				}
			}
			System.out.println("Could not find a non-loopback IP address.");
			inetHost = Inet4Address.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (SocketException e1) {
			e1.printStackTrace();
		}
		return inetHost;
	}

	/**
	 * Gets the path part of the URI for this endpoint.
	 */
	public String getPath() {
		return path;
	}

	public int getPort() {
		return port;
	}

	public String getHost() {
		return host;
	}

	/**
	 * A path which can be accessed to see if the application is up.
	 */
	public String getHealthCheckPath() {
		return healthCheckPath;
	}

	/**
	 * Sets a path which can be accessed to see if the application is up. Should
	 * be prefixed with '/'.
	 */
	public void setHealthCheckPath(String applicationPath) {
		this.healthCheckPath = applicationPath;
	}

	@Override
	public boolean equals(Object o) {
		if(o instanceof Endpoint) {
			return id.equals(((Endpoint)o).id);
		}
		return false; 
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public String toString() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getId() {
		return id;
	}
	
	
}
