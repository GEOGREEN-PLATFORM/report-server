package com.example.report_server.feignClient;

import com.example.report_server.model.geo.GeoResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name="geo-marker", url="${geospatial.server.host}")
public interface FeignClientGeoMarkerService {
    @GetMapping("/geo/info")
    GeoResponseDTO getAllMarkers(@RequestHeader("Authorization") String token, @RequestParam int page, @RequestParam int size);

    @GetMapping("/geo/dict/work-stages")
    List<String> getWorkStages(@RequestHeader("Authorization") String token);

    @GetMapping("/geo/dict/land-types")
    List<String> getLandTypes(@RequestHeader("Authorization") String token);

    @GetMapping("/geo/info")
    GeoResponseDTO getMarkersByStatus(@RequestHeader("Authorization") String token, @RequestParam int page, @RequestParam int size, @RequestParam String workStage);

    @GetMapping("/geo/info")
    GeoResponseDTO getMarkersByStatusAndLand(@RequestHeader("Authorization") String token, @RequestParam int page, @RequestParam int size, @RequestParam String workStage, @RequestParam String landType);
}
