package org.ems.config;

import org.ems.model.Event;
import org.ems.model.User;
import org.ems.repository.EventRepository;
import org.ems.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.dao.DataAccessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final PasswordEncoder passwordEncoder;

    public DataLoader(UserRepository userRepository, EventRepository eventRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        try {
            if (userRepository.count() == 0) {
                User admin = new User("Admin User", "admin@ems.com", passwordEncoder.encode("Admin@123"), "ADMIN");
                userRepository.save(admin);
            }

            if (eventRepository.count() == 0) {
                Event webinar = new Event("Birthday Bash", "Individual", "Teams of 4 compete to design a carbon-neutral skyscraper for NYC.", "Alex Johnson", LocalDate.of(2025, 10, 24), "Grand Ballroom", 120, 1);
                Event quiz = new Event("Quiz", "Individual", "A collaborative 2-day session for small groups to re-imagine the intersection of nature and architecture.", "Sarah Miller", LocalDate.of(2025, 11, 12), "Library Hall", 45, 1);
                Event carrom = new Event("Carrom", "Group", "Teams of 4 compete to design a carbon-neutral skyscraper for NYC.", "David Chen", LocalDate.of(2025, 12, 5), "Sports Arena", 16, 4);
                Event cricket = new Event("Cricket", "Group", "A collaborative 2-day session for small groups to re-imagine the intersection of nature and architecture.", "Mark Thompson", LocalDate.of(2026, 1, 15), "Main Field", 1, 11);
                eventRepository.save(webinar);
                eventRepository.save(quiz);
                eventRepository.save(carrom);
                eventRepository.save(cricket);
            }
        } catch (DataAccessException ex) {
            System.err.println("Skipping startup seed data because the database schema is not present yet: " + ex.getMessage());
            return;
        }

        // Always try to update existing Cricket event
        try {
            eventRepository.findAll().stream()
                .filter(event -> "Cricket".equals(event.getName()))
                .findFirst()
                .ifPresent(cricket -> {
                    if (cricket.getCapacity() != 1 || cricket.getTeamSize() != 11) {
                        cricket.setCapacity(1);
                        cricket.setTeamSize(11);
                        eventRepository.save(cricket);
                        System.out.println("Updated Cricket event capacity to 1 and teamSize to 11");
                    }
                });
        } catch (Exception ex) {
            System.err.println("Could not update Cricket event: " + ex.getMessage());
        }
    }
}
