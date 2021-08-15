package yb;

import co.elastic.apm.api.ElasticApm;
import co.elastic.apm.api.Span;
import lombok.SneakyThrows;
import co.elastic.apm.api.Transaction;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class HttpCaller implements Runnable {
    private final String message;
    private final String urlString;

    public HttpCaller(String message, String endPoint) throws Exception {
        this.message = message;
        endPoint += "/api/";
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

    private void SendHttpPost()  {
        Transaction tx;
        Span span = null;
        if (Main.config.ApmType.equals("elastic")) {
            tx = ElasticApm.currentTransaction();
            span = tx.startSpan();
            span.setName("Call to downstream " + urlString);
        }
        try {
            URL url = new URL(urlString);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            String auth = "user:password";
            byte[] encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes()).getBytes(StandardCharsets.UTF_8);
            String authHeaderValue = "Basic " + new String(encodedAuth);
            con.setRequestProperty("Authorization", authHeaderValue);
            con.setRequestProperty("Content-Type", "text/plain; charset=utf-8");
            con.setRequestProperty("Accept", "text/plain");
            con.setRequestProperty("Content-Length", "" + message.length());
            con.setReadTimeout(15000);
            con.setConnectTimeout(15000);
            con.setRequestMethod("POST");
            con.setDoInput(true);
            con.setDoOutput(true);
            con.connect();
            con.getOutputStream().write(message.getBytes());
            con.getOutputStream().flush();

            StringBuilder buf = new StringBuilder(1024);
            InputStream is = con.getInputStream();
            for (; ; ) {
                byte[] b = new byte[256];
                int l = is.read(b);
                if (l > 0)
                    buf.append(new String(b, 0, l));
                else
                    break;
            }
        } catch (Exception e) {
            if (span != null) {
                span.captureException(e);
            }
        } finally {
            if (span != null) {
                span.end();
            }
        }
    }
}
