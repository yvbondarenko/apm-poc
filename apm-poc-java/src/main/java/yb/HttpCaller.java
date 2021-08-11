package yb;

import lombok.SneakyThrows;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class HttpCaller implements Runnable {
    private String message;
    private String urlString;

    public HttpCaller(String message, String endPoint) throws Exception {
        this.message = message;

        if (endPoint.startsWith(":")) {
            urlString = "http://localhost" + endPoint;
        } else if (endPoint.startsWith("http")) {
            urlString = endPoint;
        } else {
            throw new Exception("Unknown URL EndPoint. Allowed only ':port' or 'http://hostname:port(port in optional)'");
        }
    }

    @SneakyThrows
    public void run() {
        SendHttpPost();
    }
    private void SendHttpPost() throws IOException {
        URL url = new URL (urlString);
        HttpURLConnection con = (HttpURLConnection)url.openConnection();
        con.setReadTimeout(15000);
        con.setConnectTimeout(15000);
        con.setRequestMethod("POST");
        con.setDoInput(true);
        con.setDoOutput(true);
        OutputStream os = con.getOutputStream();
        BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(os, StandardCharsets.UTF_8));
        writer.write(message);
        writer.flush();
        writer.close();
        os.close();
    }
}
