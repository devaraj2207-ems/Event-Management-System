package org.ems.controller;

import org.ems.model.Event;
import org.ems.model.Registration;
import org.ems.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.ems.service.EMSService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@Tag(name = "Registration", description = "Individual and group event registration endpoints")
public class RegistrationController {

    private final EMSService emsService;

    public RegistrationController(EMSService emsService) {
        this.emsService = emsService;
    }

    @Operation(summary = "Register individual event", description = "Register the logged in user for an individual event")
    @PostMapping("/register-individual")
    public ResponseEntity<Map<String, Object>> registerIndividual(@RequestParam Long eventId,
                                                                  @RequestParam String employeeId,
                                                                  @RequestParam String mobileNumber,
                                                                  HttpServletRequest request) {
        User user = getSessionUser(request);
        if (user == null) {
            return respond(request, 401, "Authentication required", "/index.html?error=" + encode("Authentication required"));
        }
        Optional<Event> event = emsService.getEvent(eventId);
        if (event.isEmpty()) {
            return respond(request, 404, "Event does not exist", "/events.html?error=" + encode("Event does not exist"));
        }
        if (event.get().getEventDate().isBefore(LocalDate.now())) {
            return respond(request, 400, "Cannot register for expired event", "/events.html?error=" + encode("Cannot register for expired event"));
        }
        if (event.get().getType() != null && event.get().getType().equalsIgnoreCase("Group")) {
            return respond(request, 400, "Use individual registration for this event", "/event-registration-individual.html?eventId=" + eventId + "&error=" + encode("Use individual registration for this event"));
        }
        if (employeeId == null || employeeId.isBlank() || mobileNumber == null || mobileNumber.isBlank()) {
            return respond(request, 400, "Employee ID and mobile number are required", "/event-registration-individual.html?eventId=" + eventId + "&error=" + encode("Employee ID and mobile number are required"));
        }
        if (emsService.getUserRegistrationsForEvent(user, event.get()).size() > 0) {
            return respond(request, 409, "User already registered for this event", "/event-registration-individual.html?eventId=" + eventId + "&error=" + encode("User already registered for this event"));
        }
        long currentParticipants = emsService.countParticipants(event.get());
        if (currentParticipants + 1 > event.get().getCapacity()) {
            return respond(request, 409, "Event is full", "/event-registration-individual.html?eventId=" + eventId + "&error=" + encode("Event is full"));
        }
        Optional<Registration> registration = emsService.registerIndividual(user, event.get(), employeeId, mobileNumber);
        if (registration.isEmpty()) {
            return respond(request, 500, "Unable to register for event", "/event-registration-individual.html?eventId=" + eventId + "&error=" + encode("Unable to register for event"));
        }
        return respondCreated(request, "/dashboard.html?success=" + encode("Registration successful"), Map.of(
                "status", "registration successful",
                "eventId", eventId,
                "type", "INDIVIDUAL"
        ));
    }

    @Operation(summary = "Register group event", description = "Register the logged in user and their team for a group event")
    @PostMapping("/register-group")
    public ResponseEntity<Map<String, Object>> registerGroup(@RequestParam Long eventId,
                                                             @RequestParam String employeeId,
                                                             @RequestParam String mobileNumber,
                                                             @RequestParam(name = "team_member") String[] teamMemberIds,
                                                             HttpServletRequest request) {
        User user = getSessionUser(request);
        if (user == null) {
            return respond(request, 401, "Authentication required", "/index.html?error=" + encode("Authentication required"));
        }
        Optional<Event> event = emsService.getEvent(eventId);
        if (event.isEmpty()) {
            return respond(request, 404, "Event does not exist", "/events.html?error=" + encode("Event does not exist"));
        }
        if (event.get().getEventDate().isBefore(LocalDate.now())) {
            return respond(request, 400, "Cannot register for expired event", "/events.html?error=" + encode("Cannot register for expired event"));
        }
        if (event.get().getType() == null || !event.get().getType().equalsIgnoreCase("Group")) {
            return respond(request, 400, "This event is not a group event", "/event-registration-group.html?eventId=" + eventId + "&error=" + encode("This event is not a group event"));
        }
        List<String> teamMembers = Arrays.stream(teamMemberIds)
                .filter(value -> value != null && !value.trim().isEmpty())
                .toList();
        if (teamMembers.isEmpty()) {
            return respond(request, 400, "Please add at least one team member", "/event-registration-group.html?eventId=" + eventId + "&error=" + encode("Please add at least one team member"));
        }
        if (teamMembers.stream().distinct().count() != teamMembers.size()) {
            return respond(request, 400, "Duplicate team member IDs are not allowed", "/event-registration-group.html?eventId=" + eventId + "&error=" + encode("Duplicate team member IDs are not allowed"));
        }
        if (event.get().getTeamSize() == null || teamMembers.size() + 1 != event.get().getTeamSize()) {
            return respond(request, 400, "Team must have exactly " + (event.get().getTeamSize() - 1) + " members", "/event-registration-group.html?eventId=" + eventId + "&error=" + encode("Team must have exactly " + (event.get().getTeamSize() - 1) + " members"));
        }
        if (emsService.getUserRegistrationsForEvent(user, event.get()).size() > 0) {
            return respond(request, 409, "User already registered for this event", "/event-registration-group.html?eventId=" + eventId + "&error=" + encode("User already registered for this event"));
        }
        long currentParticipants = emsService.countParticipants(event.get());
        if (currentParticipants + teamMembers.size() + 1 > event.get().getCapacity()) {
            return respond(request, 409, "Event capacity would be exceeded", "/event-registration-group.html?eventId=" + eventId + "&error=" + encode("Event capacity would be exceeded"));
        }
        Optional<Registration> registration = emsService.registerGroup(user, event.get(), employeeId, mobileNumber, teamMembers);
        if (registration.isEmpty()) {
            return respond(request, 500, "Unable to register team for event", "/event-registration-group.html?eventId=" + eventId + "&error=" + encode("Unable to register team for event"));
        }
        return respondCreated(request, "/dashboard.html?success=" + encode("Group registration successful"), Map.of(
                "status", "group registration successful",
                "eventId", eventId,
                "teamMemberCount", teamMembers.size()
        ));
    }

    private User getSessionUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        return (User) session.getAttribute("currentUser");
    }

    private boolean isHtmlRequest(HttpServletRequest request) {
        String accept = request.getHeader("Accept");
        return accept != null && accept.contains("text/html");
    }

    private ResponseEntity<Map<String, Object>> redirectToLocation(String location) {
        return ResponseEntity.status(302).location(URI.create(location)).build();
    }

    private ResponseEntity<Map<String, Object>> respond(HttpServletRequest request, int status, String message, String redirectLocation) {
        if (isHtmlRequest(request)) {
            return redirectToLocation(redirectLocation);
        }
        return ResponseEntity.status(status).body(Map.of("error", message));
    }

    private ResponseEntity<Map<String, Object>> respondCreated(HttpServletRequest request, String redirectLocation, Map<String, Object> body) {
        if (isHtmlRequest(request)) {
            return redirectToLocation(redirectLocation);
        }
        return ResponseEntity.status(201).body(body);
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
