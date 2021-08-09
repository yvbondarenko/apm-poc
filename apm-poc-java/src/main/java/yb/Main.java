package yb;
import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import com.sun.net.httpserver.BasicAuthenticator;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

public class Main {

    public static void main(String[] args) throws IOException {
        //StartTimer();
        int serverPort = 5002;
        HttpServer server = HttpServer.create(new InetSocketAddress(serverPort), 0);
        HttpContext context =server.createContext("/api", (exchange -> {
            Income(exchange);
            exchange.close();
        }));
        context.setAuthenticator(new BasicAuthenticator("myrealm") {
            @Override
            public boolean checkCredentials(String user, String pwd) {
                return user.equals("user") && pwd.equals("password");
            }
        });
        server.setExecutor(null); // creates a default executor
        server.start();
    }

    private static void Income(HttpExchange exchange) throws IOException {
        if ("POST".equals(exchange.getRequestMethod())) {
            StringBuilder textBuilder = new StringBuilder();
            try (Reader reader = new BufferedReader(new InputStreamReader
                    (exchange.getRequestBody(), Charset.forName(StandardCharsets.UTF_8.name())))) {
                int c;
                while ((c = reader.read()) != -1) {
                    textBuilder.append((char) c);
                }
            }
            String respText = "ok";
            //SendHttpPost();
            exchange.sendResponseHeaders(200, respText.getBytes().length);
            OutputStream output = exchange.getResponseBody();
            output.write(respText.getBytes());
            output.flush();
        } else {
            exchange.sendResponseHeaders(405, -1);// 405 Method Not Allowed
        }
    }

    private static int SendHttpPost() throws IOException {
        URL url = new URL ("http://poc.apm:5030/weatherforecast");
        HttpURLConnection con = (HttpURLConnection)url.openConnection();
        con.setReadTimeout(15000);
        con.setConnectTimeout(15000);
        con.setRequestMethod("POST");
        con.setDoInput(true);
        con.setDoOutput(true);
        OutputStream os = con.getOutputStream();
        BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(os, StandardCharsets.UTF_8));
        writer.write("empty");
        writer.flush();
        writer.close();
        os.close();
        return con.getResponseCode();
    }
}
