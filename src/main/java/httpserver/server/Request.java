package httpserver.server;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import httpserver.http.Method;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Request {
    private Method method;
    private String urlContent;
    private String pathname;
    private List<String> pathParts;
    private String params;
    private HeaderMap headerMap = new HeaderMap();
    private String body;

    // ObjectMapper zum Parsen von JSON
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // Neue Methode zum Extrahieren von Feldern aus dem Body (JSON)
    public String getBodyField(String fieldName) {
        if (this.body == null || this.body.isEmpty()) {
            return null;  // Kein Body vorhanden
        }

        try {
            // JSON aus dem Body parsen
            JsonNode jsonNode = objectMapper.readTree(this.body);

            // Das Feld extrahieren und zurückgeben
            JsonNode fieldNode = jsonNode.get(fieldName);
            if (fieldNode != null) {
                return fieldNode.asText();  // Den Wert des Feldes als Text zurückgeben
            } else {
                return null;  // Feld existiert nicht im JSON
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;  // Fehler beim Parsen des JSON
        }
    }

    // Bestehende Methoden...

    public String getServiceRoute() {
        if (this.pathParts == null || this.pathParts.isEmpty()) {
            return null;
        }
        return '/' + this.pathParts.get(0);
    }

    public String getUrlContent() {
        return this.urlContent;
    }

    public void setUrlContent(String urlContent) {
        this.urlContent = urlContent;
        boolean hasParams = urlContent.indexOf("?") != -1;

        if (hasParams) {
            String[] pathParts = urlContent.split("\\?");
            this.setPathname(pathParts[0]);
            this.setParams(pathParts[1]);
        } else {
            this.setPathname(urlContent);
            this.setParams(null);
        }
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public String getPathname() {
        return pathname;
    }



    public void setPathname(String pathname) {
        this.pathname = pathname;
        String[] stringParts = pathname.split("/");
        this.pathParts = new ArrayList<>();
        for (String part : stringParts) {
            if (part != null && part.length() > 0) {
                this.pathParts.add(part);
            }
        }
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public HeaderMap getHeaderMap() {
        return headerMap;
    }

    public void setHeaderMap(HeaderMap headerMap) {
        this.headerMap = headerMap;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public List<String> getPathParts() {
        return pathParts;
    }

    public void setPathParts(List<String> pathParts) {
        this.pathParts = pathParts;
    }
}
