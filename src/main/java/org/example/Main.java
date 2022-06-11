package org.example;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Main {
    public static final int PORT = 9999;
    public static final int THREADS = 64;

    public static void main(String[] args) {
        final var server = new Server(THREADS);

        // добавление handler'ов (обработчиков)
        server.addHandler("GET", "/classic.html", new Handler() {
            public void handle(Request request, BufferedOutputStream out) {
                try {
                    var filePath = Path.of(".", "public", request.getPath());
                    var mimeType = Files.probeContentType(filePath);
                    final var template = Files.readString(filePath);
                    final var content = template.replace("{time}",
                                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")))
                            .getBytes();
                    outWrite(mimeType, content, out);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        server.addHandler("POST", "/events.html", new Handler() {
            public void handle(Request request, BufferedOutputStream out) {
                try {
                    final var filePath = Path.of(".", "public", request.getPath());
                    final var mimeType = Files.probeContentType(filePath);

                    final var content = Files.readAllBytes(filePath);
                    outWrite(mimeType, content, out);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        server.listen(PORT);

    }

    private static void outWrite(String mimeType, byte[] content, BufferedOutputStream out) throws IOException {
        out.write((
                "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: " + mimeType + "\r\n" +
                        "Content-Length: " + content.length + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        out.write(content);
        out.flush();
    }

}