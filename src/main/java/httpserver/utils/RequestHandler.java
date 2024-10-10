package httpserver.utils;

import httpserver.http.ContentType;
import httpserver.http.HttpStatus;
import httpserver.server.Request;
import httpserver.server.Response;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class RequestHandler implements Runnable {
    private Socket clientSocket;
    private Router router;
    private PrintWriter printWriter;
    private BufferedReader bufferedReader;

    public RequestHandler(Socket clientSocket, Router router) throws IOException {
        this.clientSocket = clientSocket;
        this.bufferedReader = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
        this.printWriter = new PrintWriter(this.clientSocket.getOutputStream(), true);
        this.router = router;
    }

    @Override
    public void run() {
        try {
            Response response;
            Request request = new RequestBuilder().buildRequest(this.bufferedReader);

            // Log request information for debugging
            System.out.println("Received request: " + request.toString());

            if (request.getPathname() == null) {
                response = new Response(
                        HttpStatus.BAD_REQUEST,
                        ContentType.JSON,
                        "[]"
                );
            } else {
                response = this.router.resolve(request.getServiceRoute()).handleRequest(request);
            }

            // Log response before sending
            System.out.println("Sending response: " + response.toString());

            printWriter.write(response.get());

        } catch (IOException e) {
            System.err.println(Thread.currentThread().getName() + " IO Error: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (printWriter != null) {
                    printWriter.close();
                }
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (clientSocket != null && !clientSocket.isClosed()) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
    }

}
