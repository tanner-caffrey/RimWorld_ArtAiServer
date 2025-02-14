package com.boriselec.rimworld.aiart;

import com.boriselec.rimworld.aiart.data.Request;
import com.boriselec.rimworld.aiart.job.JobQueue;
import com.boriselec.rimworld.aiart.job.QueueLimitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@RestController
public class AiArtController {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final ImageRepository imageRepository;
    private final JobQueue jobQueue;
    private final Counters counters;

    public AiArtController(ImageRepository imageRepository, JobQueue jobQueue, Counters counters) {
        this.imageRepository = imageRepository;
        this.jobQueue = jobQueue;
        this.counters = counters;
    }

    @PostMapping("/generate")
    public ResponseEntity<?> generate(@RequestBody String postData) {
        log.info("Received /generate: " + postData);
        Request rq = Request.deserialize(postData);
        return imageRepository.getImage(rq.getArtDescription())
                .map(this::getImageResponse)
                .orElseGet(() -> process(rq));
    }

    @PostMapping("/get")
    public ResponseEntity<?> get(@RequestBody String postData) {
        log.info("Received /get: " + postData);
        Request rq = Request.deserialize(postData);
        return imageRepository.getImage(rq.getArtDescription())
                .map(this::getImageResponse)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private ResponseEntity<InputStreamResource> process(Request rq) {
        String response;
        try {
            int position = jobQueue.putIfNotPresent(rq);
            response = "Image is generating... Please wait" +
                    "\n\nQueued: " + position;
            counters.rsQueued().increment();
        } catch (QueueLimitException e) {
            response = e.getMessage() + ". Try later.";
            counters.rsLimit().increment();
        }
        return getInProgressResponse(response);
    }

    private ResponseEntity<InputStreamResource> getImageResponse(InputStream is) {
        counters.rsImage().increment();
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(new InputStreamResource(is));
    }

    private ResponseEntity<InputStreamResource> getInProgressResponse(String response) {
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .body(new InputStreamResource(new ByteArrayInputStream(response.getBytes())));
    }
}
