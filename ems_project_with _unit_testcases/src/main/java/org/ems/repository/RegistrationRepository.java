package org.ems.repository;

import org.ems.model.Event;
import org.ems.model.Registration;
import org.ems.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface RegistrationRepository extends JpaRepository<Registration, Long> {
    List<Registration> findByUser(User user);
    Optional<Registration> findByUserAndEvent(User user, Event event);
    long countByEvent(Event event);
    List<Registration> findByEvent(Event event);
}
