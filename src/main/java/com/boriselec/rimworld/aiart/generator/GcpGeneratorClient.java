package com.boriselec.rimworld.aiart.generator;

import com.google.cloud.compute.v1.Instance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicReference;

@Component
@ConditionalOnProperty("gcp.project")
public class GcpGeneratorClient implements GeneratorClient {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final GcpClient gcpClient;

    private final AtomicReference<LocalDateTime> lastRequest = new AtomicReference<>(LocalDateTime.now());
    private final int stopAfterSeconds;

    public GcpGeneratorClient(GcpClient gcpClient, @Value("${gcp.idle.stopAfterSeconds}") int stopAfterSeconds) {
        this.gcpClient = gcpClient;
        this.stopAfterSeconds = stopAfterSeconds;
    }

    @Override
    public InputStream getImage(String description) throws IOException, URISyntaxException, InterruptedException {
        lastRequest.set(LocalDateTime.now());
        try {
            return getClient().getImage(description);
        } catch (ConnectException e) {
            throw new GeneratorNotReadyException("GCP generator is running but not available yet");
        }
    }

    private GeneratorClient getClient() throws GeneratorNotReadyException {
        Instance currentInstance = gcpClient.get();
        switch (currentInstance.getStatus()) {
            case "RUNNING":
                String ip = currentInstance.getNetworkInterfaces(0).getAccessConfigs(0).getNatIP();
                String url = String.format("http://%s:8081/generate", ip);
                return new StaticGeneratorClient(url);
            case "TERMINATED":
                gcpClient.start();
            default:
                throw new GeneratorNotReadyException("GCP generator state: " + currentInstance.getStatus());
        }
    }

    @Scheduled(fixedDelay = 10_000)
    public void stopIfIdle() {
        boolean isIdle = LocalDateTime.now().minusSeconds(stopAfterSeconds).isAfter(lastRequest.get());
        if (isIdle) {
            Instance currentInstance = gcpClient.get();
            if (!"TERMINATED".equals(currentInstance.getStatus())) {
                log.info("Stopping instance");
                gcpClient.stop();
            }
        }
    }
}
