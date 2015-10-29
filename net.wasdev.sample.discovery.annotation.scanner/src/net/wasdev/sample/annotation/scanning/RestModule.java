package net.wasdev.sample.annotation.scanning;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.Path;
import javax.ws.rs.ext.Provider;

import com.ibm.ws.container.service.annotations.WebAnnotations;
import com.ibm.wsspi.anno.info.*;
import com.ibm.wsspi.anno.targets.AnnotationTargets_Targets;

public class RestModule {
	private static final int POLICY_SEED = AnnotationTargets_Targets.POLICY_SEED;
	private final Set<Endpoint> endpoints = new HashSet<Endpoint>();

	public Set<Endpoint> getEndpoints() {
		return endpoints;
	}

	public RestModule(String host, int port, String contextRoot,
			WebAnnotations webAnnotations) {
		try {
			InfoStore infoStore = webAnnotations.getInfoStore();

			webAnnotations.openInfoStore();

			// Ignore all complexities like @Parent annotations for the moment,
			// and also inheritance.

			AnnotationTargets_Targets annotationTargets = webAnnotations
					.getAnnotationTargets();

			String applicationPath = "";
			Set<String> classesWithApplicationPath = annotationTargets
					.getAnnotatedClasses(ApplicationPath.class.getName(),
							POLICY_SEED);
			if (classesWithApplicationPath.size() == 0) {
				System.err
						.println("The REST application path must be set using annotations. ");
			} else if (classesWithApplicationPath.size() > 1) {
				System.err
						.println("There should only be one REST application path per application.");
			} else {
				applicationPath = getValue(infoStore, ApplicationPath.class,
						classesWithApplicationPath.iterator().next());
			}

			// Scan annotation for @Provider, @Path
			processAnnotations(host, port, contextRoot, infoStore,
					annotationTargets, Provider.class, applicationPath);
			processAnnotations(host, port, contextRoot, infoStore,
					annotationTargets, Path.class, applicationPath);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void processAnnotations(String host, int port, String contextRoot,
			InfoStore infoStore, AnnotationTargets_Targets annotationTargets,
			Class<? extends Annotation> annotationClass, String applicationPath) {
		Set<String> annotatedClasses = annotationTargets.getAnnotatedClasses(
				Path.class.getName(), POLICY_SEED);
		for (String s : annotatedClasses) {
			String path = getValue(infoStore, Path.class, s);

			// We'll ignore method-level annotations for the moment since
			// they're
			// 'extra' information

			String uri = contextRoot + applicationPath + "/" + path;
			Endpoint newEndpoint = new Endpoint(host, port, uri);

			// Hitting a rest endpoint might have an action, so lets just
			// check the server is up occasionally. If it stops gracefully,
			// it will unregister the application, if it's not graceful,
			// this will catch that.
			newEndpoint.setPingablePath("/");
			endpoints.add(newEndpoint);
		}
	}

	private String getValue(InfoStore infoStore,
			Class<? extends Annotation> annotationClass, String s) {
		ClassInfo i = infoStore.getDelayableClassInfo(s);
		AnnotationInfo info = i.getAnnotation(annotationClass);
		AnnotationValue path = info.getValue("value");
		return path.getStringValue();
	}
}
