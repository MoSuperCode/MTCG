package httpserver.utils;

import httpserver.server.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class Router {
    private final ConcurrentMap<String, Service> serviceRegistry = new ConcurrentHashMap<>();

    public void addService(String route, Service service) {
        this.serviceRegistry.put(route, service);
    }

    public void removeService(String route) {
        this.serviceRegistry.remove(route);
    }

    public Service resolve(String route) {
        Service service = this.serviceRegistry.get(route);

        if (service == null) {
            System.out.println("No service found for route: " + route);
        }

        return service;
    }
}
