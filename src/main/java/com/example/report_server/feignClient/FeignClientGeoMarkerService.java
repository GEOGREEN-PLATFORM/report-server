package com.example.report_server.feignClient;

import com.example.report_server.model.geo.GeoMarkerDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@FeignClient(name="geo-marker", url="${geospatial.server.host}")
public interface FeignClientGeoMarkerService {
    @GetMapping("/geo/info/getAll")
    List<GeoMarkerDTO> getAllMarkers(@RequestHeader("Authorization") String token);

    @GetMapping("/geo/dict/work-stages")
    List<String> getWorkStages(@RequestHeader("Authorization") String token);
}
