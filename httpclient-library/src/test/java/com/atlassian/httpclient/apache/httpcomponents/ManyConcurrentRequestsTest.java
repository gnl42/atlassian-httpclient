package com.atlassian.httpclient.apache.httpcomponents;

import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.httpclient.api.Response;
import com.atlassian.httpclient.api.ResponsePromise;
import com.sun.net.httpserver.HttpServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;

public class ManyConcurrentRequestsTest {

    private HttpServer server;

    private HttpClient client;

    private int port;

    @Before
    public void setUp() {
        server = createOnFreePort();
        port = server.getAddress().getPort();
        server.setExecutor(Executors.newFixedThreadPool(40));
        server.createContext("/", x -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            String resp = "OK";
            x.sendResponseHeaders(200, resp.length());
            x.getResponseBody().write(resp.getBytes());
            x.getResponseBody().close();
        });
        server.start();
        client = new ApacheAsyncHttpClient<Void>("test");
    }

    @After
    public void tearDown() {
        server.stop(0);
    }

    @Test
    public void testTooManyRequests() throws InterruptedException, ExecutionException, TimeoutException {
        final String url = serverUrl(port);
        List<ResponsePromise> requests = IntStream.range(0, 1000)
                .mapToObj(this::request)
                .collect(Collectors.toList());

        requests.stream()
                .map(r -> waitForResponse(r).getStatusCode())
                .forEach(c -> assertEquals(Integer.valueOf(200), c));
    }

    public ResponsePromise request(int i) {
        ResponsePromise p = client.newRequest(serverUrl(port)).get();
        p.fail(e -> System.out.println("Request no " + i + " failed with " + e));
        return p;
    }

    private static Response waitForResponse(ResponsePromise promise) {
        try {
            return promise.get(1, TimeUnit.MINUTES);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    private static HttpServer createOnFreePort() {
        try {
            return HttpServer.create(new InetSocketAddress(0), 0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String serverUrl(int port) {
        return String.format("http://localhost:%d/", port);
    }
}
