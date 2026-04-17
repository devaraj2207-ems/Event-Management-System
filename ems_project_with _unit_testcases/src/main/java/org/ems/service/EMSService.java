package org.ems.service;

import org.ems.model.Event;
import org.ems.model.Registration;
import org.ems.model.User;
import org.ems.repository.EventRepository;
import org.ems.repository.RegistrationRepository;
import org.ems.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EMSService {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final RegistrationRepository registrationRepository;
    private final PasswordEncoder passwordEncoder;

    public EMSService(UserRepository userRepository, EventRepository eventRepository, RegistrationRepository registrationRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.registrationRepository = registrationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> authenticate(String email, String rawPassword) {
        return userRepository.findByEmail(email)
                .filter(user -> passwordEncoder.matches(rawPassword, user.getPassword()));
    }

    public User registerUser(String fullName, String email, String rawPassword) {
        User user = new User(fullName, email, passwordEncoder.encode(rawPassword), "USER");
        return userRepository.save(user);
    }

    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    public void resetPassword(String email, String rawPassword) {
        userRepository.findByEmail(email).ifPresent(user -> {
            user.setPassword(passwordEncoder.encode(rawPassword));
            userRepository.save(user);
        });
    }

    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    public Optional<Event> getEvent(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return eventRepository.findById(id);
    }

    public List<Registration> getRegistrationsForUser(User user) {
        return registrationRepository.findByUser(user);
    }

    public List<Registration> getUserRegistrationsForEvent(User user, Event event) {
        return registrationRepository.findByUser(user).stream()
                .filter(reg -> reg.getEvent().getId().equals(event.getId()))
                .collect(Collectors.toList());
    }

    public long countRegistrations(Event event) {
        return registrationRepository.findByEvent(event).size();
    }

    public long countParticipants(Event event) {
        return registrationRepository.findByEvent(event).stream()
                .mapToLong(reg -> {
                    if ("GROUP".equalsIgnoreCase(reg.getRegistrationType()) && reg.getTeamMembers() != null) {
                        return 1 + reg.getTeamMembers().split(",").length;
                    } else {
                        return 1;
                    }
                })
                .sum();
    }

    public Event createEvent(String name, String type, String description, String organizer, LocalDate eventDate, String venue, Integer capacity, Integer teamSize) {
        Event event = new Event(name, type, description, organizer, eventDate, venue, capacity, teamSize);
        return eventRepository.save(event);
    }

    @Transactional
    public boolean deleteEvent(Long eventId) {
        if (eventId == null) {
            return false;
        }
        Optional<Event> eventOpt = eventRepository.findById(eventId);
        if (eventOpt.isEmpty()) {
            return false;
        }
        Event event = eventOpt.get();
        List<Registration> registrations = registrationRepository.findByEvent(event);
        if (registrations != null && !registrations.isEmpty()) {
            registrationRepository.deleteAll(registrations);
        }
        if (event != null) {
            eventRepository.delete(event);
        }
        return true;
    }

    @Transactional
    public Optional<Registration> registerIndividual(User user, Event event, String employeeId, String mobileNumber) {
        if (registrationRepository.findByUserAndEvent(user, event).isPresent()) {
            return Optional.empty();
        }
        long currentParticipants = countParticipants(event);
        if (currentParticipants + 1 > event.getCapacity()) {
            return Optional.empty();
        }
        Registration registration = new Registration(user, event, employeeId, mobileNumber, "INDIVIDUAL", null);
        return Optional.of(registrationRepository.save(registration));
    }

    @Transactional
    public Optional<Registration> registerGroup(User user, Event event, String employeeId, String mobileNumber, List<String> teamMembers) {
        if (registrationRepository.findByUserAndEvent(user, event).isPresent()) {
            return Optional.empty();
        }
        if (event.getTeamSize() == null || teamMembers.size() + 1 != event.getTeamSize()) {
            return Optional.empty();
        }
        long currentParticipants = countParticipants(event);
        if (currentParticipants + teamMembers.size() + 1 > event.getCapacity()) {
            return Optional.empty();
        }
        String members = String.join(",", teamMembers);
        Registration registration = new Registration(user, event, employeeId, mobileNumber, "GROUP", members);
        return Optional.of(registrationRepository.save(registration));
    }
}
