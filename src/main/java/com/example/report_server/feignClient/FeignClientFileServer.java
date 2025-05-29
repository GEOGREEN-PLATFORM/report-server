package com.example.report_server.feignClient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name="file-server", url="${file.server.host}")
public interface FeignClientFileServer {
    @GetMapping("/file/image/download/{imageId}")
    byte[] downloadImage(@PathVariable UUID imageId);
}
