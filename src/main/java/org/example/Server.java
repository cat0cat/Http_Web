package org.example;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Server {

    ExecutorService executorService;
    private final Map<String, Map<String, Handler>> handlers;
    private final Handler notFoundHandler = new Handler() {
        public void handle(Request request, BufferedOutputStream out) {
            try {
                out.write((
                        "HTTP/1.1 404 Not Found\r\n" +
                                "Content-Length: 0\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    public Server(int threads) {
        this.executorService = Executors.newFixedThreadPool(threads);
        this.handlers = new ConcurrentHashMap<>();
    }

    public void listen(int port) {
        try (final var serverSocket = new ServerSocket(port)) {
            while (true) {
                final var socket = serverSocket.accept();
                executorService.submit(() -> connect(socket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addHandler(String method, String path, Handler handler) {
        if (handlers.get(method) == null) {
            handlers.put(method, new ConcurrentHashMap<>());
        }
        handlers.get(method).put(path, handler);

    }

    public void connect(Socket socket) {
        try (socket;
             final var in = socket.getInputStream();
             final var out = new BufferedOutputStream(socket.getOutputStream());) {

            var request = Request.fromInputStream(in);
            var pathHandlerMap = handlers.get(request.getMethod());
            if (pathHandlerMap == null) {
                notFoundHandler.handle(request, out);
                return;
            }
            var handler = pathHandlerMap.get(request.getPath());
            if (handler == null) {
                notFoundHandler.handle(request, out);
                return;
            }
            handler.handle(request, out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

