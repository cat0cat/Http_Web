package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class Request {

    private final String method;
    private String body;
    private final String path;
    private final Map<String, String> headers;
    private final InputStream in;

    private Request(String method, String path, Map<String, String> headers, InputStream in) {
        this.method = method;
        this.path = path;
        this.headers = headers;
        this.in = in;
    }

    public static Request fromInputStream(InputStream in) throws IOException {
        var reader = new BufferedReader(new InputStreamReader(in));

        final var requestLine = reader.readLine();
        final var parts = requestLine.split(" ");

        if (parts.length != 3) {
            // just close socket
            throw new IOException("Error!!!");
        }

        var method = parts[0];
        var path = parts[1];

        Map<String, String> headers = new HashMap<>();
        String headerLine;
        while (!(headerLine = reader.readLine()).equals("")) {
            var i = headerLine.indexOf(":");
            var headerName = headerLine.substring(0, i);
            var headerValue = headerLine.substring(i + 2);
            headers.put(headerName, headerValue);
        }
        return new Request(method, path, headers, in);
    }

    public InputStream getIn() {
        return in;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getMethod() {
        return method;
    }

    public String getBody() {
        return body;
    }

    public String getPath() {
        return path;
    }

    @Override
    public String toString() {
        return "server.Request{" +
                "method='" + method + '\'' +
                ", path='" + path + "', " +
                ", headers='" + headers + '\'' +
                ", body='" + body + '\'' +
                '}';
    }
}
