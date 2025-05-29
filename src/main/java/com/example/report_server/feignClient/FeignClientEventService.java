package com.example.report_server.feignClient;

import com.example.report_server.model.event.EventResponseDTO;
import com.example.report_server.model.event.EventStatusDTO;
import com.example.report_server.model.event.HistoryResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@FeignClient(name="event-manager", url="${event.manager.host}")
public interface FeignClientEventService {

    @GetMapping("/event/getAll")
    EventResponseDTO getAllEvents(@RequestHeader("Authorization") String token, @RequestParam int page, @RequestParam int size);

    @GetMapping("/event/getAll")
    EventResponseDTO getEventsByStatus(@RequestHeader("Authorization") String token, @RequestParam int page, @RequestParam int size, @RequestParam String status);

    @GetMapping("/event/status/getAll")
    List<EventStatusDTO> getAllStatuses(@RequestHeader("Authorization") String token);

    @GetMapping("/event/type/getAll")
    List<EventStatusDTO> getAllEventTypes(@RequestHeader("Authorization") String token);

    @GetMapping("/event/problemType/getAll")
    List<EventStatusDTO> getAllProblemTypes(@RequestHeader("Authorization") String token);

    @GetMapping("/event/getAll")
    EventResponseDTO getEventsByTypes(@RequestHeader("Authorization") String token, @RequestParam int page, @RequestParam int size, @RequestParam String eventType, @RequestParam String problemAreaType);

    @GetMapping("/event/getAll")
    EventResponseDTO getEventsByGeoMarker(@RequestHeader("Authorization") String token, @RequestParam int page, @RequestParam int size, @RequestParam UUID geoPointId);

    @GetMapping("/event/{eventId}/history")
    HistoryResponseDTO getHistoryByEvent(@RequestHeader("Authorization") String token, @PathVariable UUID eventId, @RequestParam int page, @RequestParam int size);
}
