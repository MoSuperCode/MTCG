package httpserver.http;

public enum Method {
    GET,
    POST,
    PUT,
    PATCH,
    DELETE,
    ;

    public String trim() {
        return this.name().toLowerCase();
    }
}
