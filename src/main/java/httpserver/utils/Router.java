package httpserver.utils;

import httpserver.server.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class Router {
    private final ConcurrentMap<String, Service> serviceRegistry = new ConcurrentHashMap<>();

    public void addService(String route, Service service) {
        this.serviceRegistry.put(route, service);
        System.out.println("✅ Route registriert: " + route);
    }

    public void removeService(String route) {
        this.serviceRegistry.remove(route);
    }

    public Set<String> getRoutes() {
        return serviceRegistry.keySet();
    }

    public Service resolve(String route) {
        System.out.println("🔍 Router sucht nach: " + route);

        // Try exact match first
        Service service = this.serviceRegistry.get(route);

        // If no exact match, try matching base paths
        if (service == null) {
            // Look for the longest matching registered path
            String bestMatch = "";
            for (String registeredPath : serviceRegistry.keySet()) {
                // Check if route starts with this registered path
                if (route.startsWith(registeredPath) &&
                        registeredPath.length() > bestMatch.length()) {
                    // For paths like /tradings/, make sure there's a / after the base path
                    if (registeredPath.endsWith("/") ||
                            route.length() == registeredPath.length() ||
                            route.charAt(registeredPath.length()) == '/') {
                        bestMatch = registeredPath;
                    }
                }
            }

            if (!bestMatch.isEmpty()) {
                service = serviceRegistry.get(bestMatch);
                System.out.println("✅ Found service for base path: " + bestMatch);
            }
        }

        if (service == null) {
            System.out.println("❌ No service found for route: " + route);
        }

        return service;
    }
}