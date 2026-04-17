package org.ems.controller;

import org.ems.model.Event;
import org.ems.model.Registration;
import org.ems.model.User;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.ems.service.EMSService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.net.URI;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@Tag(name = "EMS API", description = "Endpoints for user, event, registration and dashboard workflows")
public class ApiController {

    private final EMSService emsService;

    public ApiController(EMSService emsService) {
        this.emsService = emsService;
    }

    @GetMapping("/api/current-user")
    @ResponseBody
    public ResponseEntity<?> getCurrentUser(HttpServletRequest request) {
        User user = getSessionUser(request);
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not logged in"));
        }
        return ResponseEntity.ok(Map.<String, Object>of(
                "fullName", user.getFullName(),
                "email", user.getEmail(),
                "role", user.getRole()
        ));
    }

    @GetMapping("/api/events")
    @ResponseBody
    public List<Map<String, Object>> getEvents() {
        return emsService.getAllEvents().stream().map(event -> {
            boolean expired = event.getEventDate().isBefore(LocalDate.now());
            Map<String, Object> eventMap = new java.util.LinkedHashMap<>();
            eventMap.put("id", event.getId());
            eventMap.put("name", event.getName());
            eventMap.put("type", event.getType());
            eventMap.put("description", event.getDescription());
            eventMap.put("organizer", event.getOrganizer());
            eventMap.put("eventDate", event.getEventDate().toString());
            eventMap.put("venue", event.getVenue());
            eventMap.put("capacity", event.getCapacity());
            eventMap.put("teamSize", event.getTeamSize());
            eventMap.put("registeredCount", emsService.countParticipants(event));
            eventMap.put("status", expired ? "Expired" : "Active");
            return eventMap;
        }).collect(Collectors.toList());
    }

    @PostMapping("/create-event")
    @ResponseBody
    public ResponseEntity<?> createEvent(@RequestParam String name,
                                         @RequestParam String type,
                                         @RequestParam String description,
                                         @RequestParam String organizer,
                                         @RequestParam String eventDate,
                                         @RequestParam String venue,
                                         @RequestParam Integer capacity,
                                         @RequestParam(required = false) Integer teamSize) {
        LocalDate date;
        try {
            date = LocalDate.parse(eventDate);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid date format"));
        }
        if (capacity == null || capacity <= 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "Capacity must be greater than zero"));
        }
        if (type == null || type.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Event type is required"));
        }
        if (type.equalsIgnoreCase("Group") && (teamSize == null || teamSize <= 0)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Team limit must be greater than zero for group events"));
        }
        Event createdEvent = emsService.createEvent(name, type, description, organizer, date, venue, capacity, teamSize);
        return ResponseEntity.created(URI.create("/create-event/" + createdEvent.getId())).body(Map.of(
                "status", "created",
                "eventId", createdEvent.getId()
        ));
    }

    @PostMapping("/delete-event/{eventId}")
    @ResponseBody
    public ResponseEntity<?> deleteEvent(@PathVariable Long eventId) {
        Optional<Event> eventOptional = emsService.getEvent(eventId);
        if (eventOptional.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Event not found"));
        }
        Event event = eventOptional.get();
        if (!event.getEventDate().isBefore(LocalDate.now())) {
            return ResponseEntity.status(403).body(Map.of("error", "Only expired events can be deleted"));
        }
        boolean deleted = emsService.deleteEvent(eventId);
        if (!deleted) {
            return ResponseEntity.status(500).body(Map.of("error", "Unable to delete event"));
        }
        return ResponseEntity.ok(Map.of("status", "deleted"));
    }

    @GetMapping("/api/dashboard")
    @ResponseBody
    public ResponseEntity<?> getDashboard(HttpServletRequest request) {
        User user = getSessionUser(request);
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not logged in"));
        }
        List<Map<String, Object>> events = emsService.getRegistrationsForUser(user).stream()
                .sorted(Comparator.comparing(Registration::getCreatedAt).reversed())
                .map(reg -> Map.<String, Object>of(
                "eventName", reg.getEvent().getName(),
                "eventType", reg.getEvent().getType(),
                "organizer", reg.getEvent().getOrganizer(),
                "eventDate", reg.getEvent().getEventDate().toString(),
                "venue", reg.getEvent().getVenue(),
                "status", reg.getEvent().getEventDate().isBefore(java.time.LocalDate.now()) ? "Expired" : "Active"
        )).collect(Collectors.toList());
        return ResponseEntity.ok(events);
    }

    @GetMapping("/api/user-registrations")
    @ResponseBody
    public ResponseEntity<?> getUserRegistrations(HttpServletRequest request) {
        User user = getSessionUser(request);
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not logged in"));
        }
        List<Long> registeredEventIds = emsService.getRegistrationsForUser(user).stream()
                .map(reg -> reg.getEvent().getId())
                .collect(Collectors.toList());
        return ResponseEntity.ok(Map.of("eventIds", registeredEventIds));
    }

    private User getSessionUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        return (User) session.getAttribute("currentUser");
    }
}
