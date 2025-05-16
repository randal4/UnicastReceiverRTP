import jakarta.annotation.PreDestroy;
import jakarta.ejb.Singleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;
import reactor.netty.udp.UdpServer;
import io.netty.channel.ChannelOption;
import reactor.core.Disposable;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

@Configuration
@Singleton
public class DynamicRtpServerManager {

    private static final int BUFFER_SIZE = 4 * 1024 * 1024;

    private final Map<Integer, Disposable> activeServers = new ConcurrentHashMap<>();

    @Autowired
    private MessageChannel rtpChannel;

    @Autowired
    private Executor rtpProcessingExecutor;

    public void startServer(int port) {
        if (activeServers.containsKey(port)) {
            System.out.println("Server already running on port " + port);
            return;
        }

        Disposable server = UdpServer.create()
                .host("0.0.0.0")
                .port(port)
                .option(ChannelOption.SO_RCVBUF, BUFFER_SIZE)
                .option(ChannelOption.SO_REUSEADDR, true)
                .handle((in, out) -> in.receive()
                        .asString(StandardCharsets.UTF_8)
                        .publishOn(Schedulers.fromExecutor(rtpProcessingExecutor))
                        .doOnNext(packet -> sendToChannel(port, packet))
                        .then())
                .bindNow();

        activeServers.put(port, server);
        System.out.println("Started RTP server on port " + port);
    }

    public void stopServer(int port) {
        Disposable server = activeServers.remove(port);
        if (server != null) {
            server.dispose();
            System.out.println("Stopped RTP server on port " + port);
        } else {
            System.out.println("No server running on port " + port);
        }
    }

    public boolean isServerRunning(int port) {
        return activeServers.containsKey(port);
    }

    @PreDestroy
    public void stopAllServers() {
        activeServers.values().forEach(Disposable::dispose);
        activeServers.clear();
        System.out.println("Stopped all RTP servers");
    }

    private void sendToChannel(int port, String packet) {
        rtpChannel.send(MessageBuilder.withPayload(packet)
                .setHeader("rtp_port", port)
                .build());
    }

    @Bean
    public MessageChannel rtpChannel() {
        return new QueueChannel(1000);
    }

    @Bean
    @ServiceActivator(inputChannel = "rtpChannel")
    public void handleRtpMessage(String message) {
        System.out.println("Processed RTP packet: " + message);
        // Add your RTP packet processing logic here
    }
}
