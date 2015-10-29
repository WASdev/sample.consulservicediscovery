package com.ibm.sample.annotation.scanning;

import java.net.*;
import java.util.Enumeration;

public class Endpoint {
	private final String uri;
	private final int port;
	private final String host;
	private String pingablePath = "/rest";

	public Endpoint(String host, int port, String uri) {
		this.port = port;
		this.uri = uri;

		// If we're listening on all interfaces, just choose one
		if ("*".equals(host)) {
			this.host = getInternetHost();
		} else {
			this.host = host;
		}
	}

	private String getInternetHost() {
		String inetHost = "unknown";
		try {
			System.out.println("Host addr: "
					+ InetAddress.getLocalHost().getHostAddress()); // often
																	// returns
																	// "127.0.0.1"
			Enumeration<NetworkInterface> n = NetworkInterface
					.getNetworkInterfaces();
			for (; n.hasMoreElements();) {
				NetworkInterface e = n.nextElement();
				System.out.println("Interface: " + e.getName());
				Enumeration<InetAddress> a = e.getInetAddresses();
				for (; a.hasMoreElements();) {
					InetAddress addr = a.nextElement();
					System.out.println("  " + addr.getHostAddress());
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

	public String getUri() {
		return uri;
	}

	public int getPort() {
		return port;
	}

	public String getHost() {
		return host;
	}

	public String getApplicationPath() {
		return pingablePath;
	}

	public void setPingablePath(String applicationPath) {
		this.pingablePath = applicationPath;
	}

}
