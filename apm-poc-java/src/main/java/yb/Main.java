package yb;
import java.io.*;
import java.net.*;
import java.util.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import com.sun.net.httpserver.BasicAuthenticator;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import co.elastic.apm.api.ElasticApm;
import co.elastic.apm.api.Transaction;
import lombok.SneakyThrows;

public class Main {
public static AppConfig config = new AppConfig();
    public static void main(String[] args) throws IOException {
        if(args.length < 1 || args[0].equals("")) {
            PrintHelpText();
        }
        if(!args[0].contains(";")) {
            PrintHelpText();
        }
        else {
            String[] str = args[0].split(";");
            if (str.length != 7) {
                PrintHelpText();
            } else {
                CreateConfig(str);
                System.out.println("Node Name:"+config.Name);
                System.out.println("Node Type:"+config.LoadType);
                System.out.println("Node URL: http://0.0.0.0:"+config.ApiPort);
                StartLogic(config);
            }
        }
    }

    private static void CreateConfig(String[] s) {
        //name
        String[] name = s[0].split("=");
        if (name[0].equals("name")) {
            config.Name = name[1];
        } else {
            PrintHelpText();
        }
        //apiPort
        String[] apiPort = s[1].split("=");
        if (apiPort[0].equals("apiPort")) {
            config.ApiPort = Integer.parseInt(apiPort[1]);
        } else {
            PrintHelpText();
        }
        //loadType
        String[] loadType = s[2].split("=");
        if (loadType[0].equals("loadType")) {
            if (loadType[1].equals("generator") || loadType[1].equals("caller") || loadType[1].equals("receiver")) {
                config.LoadType = loadType[1];
            } else {
                PrintHelpText();
            }
        } else {
            PrintHelpText();
        }
        //callToServers
        if (!config.LoadType.equals("receiver")) {
            String[] CallToServers = s[3].split("=");
            if (CallToServers[0].equals("CallToServers")) {
                String[] servers = CallToServers[1].split(",");
                if (servers.length < 1) {
                    PrintHelpText();
                } else {
                    if (config.CallToServers == null) {
                        config.CallToServers = new ArrayList<>();
                    }
                    config.CallToServers.addAll(Arrays.asList(servers));
                }
            } else {
                PrintHelpText();
            }
        } else {
            config.CallToServers = null;
        }
        //apmType
        String[] apmType = s[4].split("=");
        if (apmType[0].equals("apmType")) {
            if (apmType[1].equals("none") || apmType[1].equals("elastic") || apmType[1].equals("jaeger")) {
                config.ApmType = apmType[1];
            } else {
                PrintHelpText();
            }
        } else {
            PrintHelpText();
        }
        //asyncType
        String[] asyncType = s[5].split("=");
        if (asyncType[0].equals("asyncType")) {
            if (asyncType[1].equals("sync") || asyncType[1].equals("async") || asyncType[1].equals("future")) {
                config.AsyncType = asyncType[1];
            } else {
                PrintHelpText();
            }
        } else {
            PrintHelpText();
        }
        //generateIntervalMs
        String[] generateIntervalMs = s[6].split("=");
        if (generateIntervalMs[0].equals("generateIntervalMs")) {
            config.GenerateIntervalMs = Integer.parseInt(generateIntervalMs[1]);
        } else {
            PrintHelpText();
        }
    }

    private static void StartLogic(AppConfig config) throws IOException {
        if(config.LoadType.equals("generator")) {
            StartTimer();
        }
        if(config.LoadType.equals("caller")||config.LoadType.equals("receiver")) {
            StartHttpServer(config);
        }
    }

    private static void StartTimer() {
        TimerTask task = new TimerTask() {
            @SneakyThrows
            public void run() {
                UUID uuid = UUID. randomUUID();
                String uuidAsString = uuid. toString();
                SendToDownStreams(uuidAsString);
            }
        };
        Timer timer = new Timer("Timer");

        long delay = 1L;
        timer.scheduleAtFixedRate(task, delay,config.GenerateIntervalMs);
    }

    private static void StartHttpServer(AppConfig config) throws IOException {
        int serverPort = config.ApiPort;
        HttpServer server = HttpServer.create(new InetSocketAddress(serverPort), 0);
        HttpContext context =server.createContext("/api", (exchange -> {
            try {
                Income(exchange);
            } catch (Exception e) {
                e.printStackTrace();
            }
            exchange.close();
        }));
        context.setAuthenticator(new BasicAuthenticator("myrealm") {
            @Override
            public boolean checkCredentials(String user, String pwd) {
                return user.equals("user") && pwd.equals("password");
            }
        });
        server.setExecutor(null);
        server.start();
    }

    private static void PrintHelpText() {
        System.out.println("You need to set startup parameter string:");
        System.out.println("Example, name=node1;apiPort=8065;loadType=generator|caller|receiver;callToServers='none' for 'generator' loadType parameter,http://ip:5052,http://ip,:9052;apmType='none' for enterprise agent|elastic|jaeger;asyncType=sync|async|future;generateIntervalMs=1000");
        System.exit(0);
    }

    private static void Income(HttpExchange exchange) throws Exception {
        Transaction transaction = null;
        if(config.ApmType=="elastic")
        {
            transaction = ElasticApm.startTransaction();
            transaction.setName("HTTP Income");
            transaction.setType(Transaction.TYPE_REQUEST);
        }
        try {
        if ("POST".equals(exchange.getRequestMethod())) {
            StringBuilder textBuilder = new StringBuilder();

            try (Reader reader = new BufferedReader(new InputStreamReader
                    (exchange.getRequestBody(), Charset.forName(StandardCharsets.UTF_8.name())))) {
                int c;
                while ((c = reader.read()) != -1) {
                    textBuilder.append((char) c);
                }
            }
            String respText = config.Name+":"+textBuilder.toString();
            exchange.sendResponseHeaders(200, respText.getBytes().length);
            exchange.getRequestHeaders().put("Content-Type", Collections.singletonList("text/plain"));

            OutputStream output = exchange.getResponseBody();
            output.write(respText.getBytes(StandardCharsets.UTF_8));
            output.flush();
            if(config.LoadType.equals("caller"))
            {
                SendToDownStreams(textBuilder.toString());
            }
        } else {
            exchange.sendResponseHeaders(405, -1);// 405 Method Not Allowed
        }
        } catch (Exception e) {
            transaction.captureException(e);
            throw e;
        } finally {
            if(transaction!=null) {
                transaction.end();
            }
        }
    }

    private static void SendToDownStreams(String message) throws Exception {
        for (String endpoint: config.CallToServers
        ) {
            HttpCaller myHttpCaller = new HttpCaller(message,endpoint);
            Thread t = new Thread(myHttpCaller);
            t.start();
        }
    }
}
