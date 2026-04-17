package org.ems;

import org.ems.controller.RegistrationController;
import org.ems.model.Event;
import org.ems.model.Registration;
import org.ems.model.User;
import org.ems.service.EMSService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(ExtentReportListener.class)
@WebMvcTest(RegistrationController.class)
public class RegistrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EMSService emsService;

    // User Story 7: Register for an Individual Event

    @Test
    public void testRegisterIndividualWithValidData() throws Exception {
        User user = new User("John", "john@gmail.com", "pass", "USER");
        Event event = new Event("Event1", "Individual", "Desc", "Org", LocalDate.now().plusDays(1), "Venue", 10, null);
        event.setId(1L);
        Registration reg = new Registration(user, event, "EMP1", "1234567890", "INDIVIDUAL", null);
        Mockito.when(emsService.getEvent(1L)).thenReturn(Optional.of(event));
        Mockito.when(emsService.getUserRegistrationsForEvent(user, event)).thenReturn(Arrays.asList());
        Mockito.when(emsService.countParticipants(event)).thenReturn(5L);
        Mockito.when(emsService.registerIndividual(user, event, "EMP1", "1234567890")).thenReturn(Optional.of(reg));

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("currentUser", user);

        mockMvc.perform(MockMvcRequestBuilders.post("/register-individual")
                .param("eventId", "1")
                .param("employeeId", "EMP1")
                .param("mobileNumber", "1234567890")
                .session(session)
                .header("Accept", "application/json"))
                .andExpect(MockMvcResultMatchers.status().isCreated());
    }

    @Test
    public void testRegisterIndividualWithoutLogin() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/register-individual")
                .param("eventId", "1")
                .param("employeeId", "EMP1")
                .param("mobileNumber", "1234567890")
                .header("Accept", "application/json"))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    public void testRegisterIndividualForNonExistentEvent() throws Exception {
        User user = new User("John", "john@gmail.com", "pass", "USER");
        Mockito.when(emsService.getEvent(1L)).thenReturn(Optional.empty());

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("currentUser", user);

        mockMvc.perform(MockMvcRequestBuilders.post("/register-individual")
                .param("eventId", "1")
                .param("employeeId", "EMP1")
                .param("mobileNumber", "1234567890")
                .session(session)
                .header("Accept", "application/json"))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    public void testRegisterIndividualForExpiredEvent() throws Exception {
        User user = new User("John", "john@gmail.com", "pass", "USER");
        Event event = new Event("Event1", "Individual", "Desc", "Org", LocalDate.now().minusDays(1), "Venue", 10, null);
        event.setId(1L);
        Mockito.when(emsService.getEvent(1L)).thenReturn(Optional.of(event));

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("currentUser", user);

        mockMvc.perform(MockMvcRequestBuilders.post("/register-individual")
                .param("eventId", "1")
                .param("employeeId", "EMP1")
                .param("mobileNumber", "1234567890")
                .session(session)
                .header("Accept", "application/json"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testRegisterIndividualForGroupEvent() throws Exception {
        User user = new User("John", "john@gmail.com", "pass", "USER");
        Event event = new Event("Event1", "Group", "Desc", "Org", LocalDate.now().plusDays(1), "Venue", 10, 5);
        event.setId(1L);
        Mockito.when(emsService.getEvent(1L)).thenReturn(Optional.of(event));

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("currentUser", user);

        mockMvc.perform(MockMvcRequestBuilders.post("/register-individual")
                .param("eventId", "1")
                .param("employeeId", "EMP1")
                .param("mobileNumber", "1234567890")
                .session(session)
                .header("Accept", "application/json"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testRegisterIndividualWithEmptyEmployeeId() throws Exception {
        User user = new User("John", "john@gmail.com", "pass", "USER");
        Event event = new Event("Event1", "Individual", "Desc", "Org", LocalDate.now().plusDays(1), "Venue", 10, null);
        event.setId(1L);
        Mockito.when(emsService.getEvent(1L)).thenReturn(Optional.of(event));

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("currentUser", user);

        mockMvc.perform(MockMvcRequestBuilders.post("/register-individual")
                .param("eventId", "1")
                .param("employeeId", "")
                .param("mobileNumber", "1234567890")
                .session(session)
                .header("Accept", "application/json"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testRegisterIndividualWithEmptyMobileNumber() throws Exception {
        User user = new User("John", "john@gmail.com", "pass", "USER");
        Event event = new Event("Event1", "Individual", "Desc", "Org", LocalDate.now().plusDays(1), "Venue", 10, null);
        event.setId(1L);
        Mockito.when(emsService.getEvent(1L)).thenReturn(Optional.of(event));

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("currentUser", user);

        mockMvc.perform(MockMvcRequestBuilders.post("/register-individual")
                .param("eventId", "1")
                .param("employeeId", "EMP1")
                .param("mobileNumber", "")
                .session(session)
                .header("Accept", "application/json"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testRegisterIndividualAlreadyRegistered() throws Exception {
        User user = new User("John", "john@gmail.com", "pass", "USER");
        Event event = new Event("Event1", "Individual", "Desc", "Org", LocalDate.now().plusDays(1), "Venue", 10, null);
        event.setId(1L);
        Registration existingReg = new Registration(user, event, "EMP1", "1234567890", "INDIVIDUAL", null);
        Mockito.when(emsService.getEvent(1L)).thenReturn(Optional.of(event));
        Mockito.when(emsService.getUserRegistrationsForEvent(user, event)).thenReturn(Arrays.asList(existingReg));

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("currentUser", user);

        mockMvc.perform(MockMvcRequestBuilders.post("/register-individual")
                .param("eventId", "1")
                .param("employeeId", "EMP1")
                .param("mobileNumber", "1234567890")
                .session(session)
                .header("Accept", "application/json"))
                .andExpect(MockMvcResultMatchers.status().isConflict());
    }

    @Test
    public void testRegisterIndividualWhenEventFull() throws Exception {
        User user = new User("John", "john@gmail.com", "pass", "USER");
        Event event = new Event("Event1", "Individual", "Desc", "Org", LocalDate.now().plusDays(1), "Venue", 10, null);
        event.setId(1L);
        Mockito.when(emsService.getEvent(1L)).thenReturn(Optional.of(event));
        Mockito.when(emsService.getUserRegistrationsForEvent(user, event)).thenReturn(Arrays.asList());
        Mockito.when(emsService.countParticipants(event)).thenReturn(10L);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("currentUser", user);

        mockMvc.perform(MockMvcRequestBuilders.post("/register-individual")
                .param("eventId", "1")
                .param("employeeId", "EMP1")
                .param("mobileNumber", "1234567890")
                .session(session)
                .header("Accept", "application/json"))
                .andExpect(MockMvcResultMatchers.status().isConflict());
    }

    // User Story 8: Register for a Group Event

    @Test
    public void testRegisterGroupWithValidData() throws Exception {
        User user = new User("John", "john@gmail.com", "pass", "USER");
        Event event = new Event("Event1", "Group", "Desc", "Org", LocalDate.now().plusDays(1), "Venue", 10, 3);
        event.setId(1L);
        Registration reg = new Registration(user, event, "EMP1", "1234567890", "GROUP", "EMP2,EMP3");
        Mockito.when(emsService.getEvent(1L)).thenReturn(Optional.of(event));
        Mockito.when(emsService.getUserRegistrationsForEvent(user, event)).thenReturn(Arrays.asList());
        Mockito.when(emsService.countParticipants(event)).thenReturn(5L);
        Mockito.when(emsService.registerGroup(user, event, "EMP1", "1234567890", Arrays.asList("EMP2", "EMP3"))).thenReturn(Optional.of(reg));

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("currentUser", user);

        mockMvc.perform(MockMvcRequestBuilders.post("/register-group")
                .param("eventId", "1")
                .param("employeeId", "EMP1")
                .param("mobileNumber", "1234567890")
                .param("team_member", "EMP2")
                .param("team_member", "EMP3")
                .session(session)
                .header("Accept", "application/json"))
                .andExpect(MockMvcResultMatchers.status().isCreated());
    }

    @Test
    public void testRegisterGroupForIndividualEvent() throws Exception {
        User user = new User("John", "john@gmail.com", "pass", "USER");
        Event event = new Event("Event1", "Individual", "Desc", "Org", LocalDate.now().plusDays(1), "Venue", 10, null);
        event.setId(1L);
        Mockito.when(emsService.getEvent(1L)).thenReturn(Optional.of(event));

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("currentUser", user);

        mockMvc.perform(MockMvcRequestBuilders.post("/register-group")
                .param("eventId", "1")
                .param("employeeId", "EMP1")
                .param("mobileNumber", "1234567890")
                .param("team_member", "EMP2")
                .session(session)
                .header("Accept", "application/json"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testRegisterGroupWithNoTeamMembers() throws Exception {
        User user = new User("John", "john@gmail.com", "pass", "USER");
        Event event = new Event("Event1", "Group", "Desc", "Org", LocalDate.now().plusDays(1), "Venue", 10, 3);
        event.setId(1L);
        Mockito.when(emsService.getEvent(1L)).thenReturn(Optional.of(event));

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("currentUser", user);

        mockMvc.perform(MockMvcRequestBuilders.post("/register-group")
                .param("eventId", "1")
                .param("employeeId", "EMP1")
                .param("mobileNumber", "1234567890")
                .session(session)
                .header("Accept", "application/json"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testRegisterGroupWithDuplicateTeamMembers() throws Exception {
        User user = new User("John", "john@gmail.com", "pass", "USER");
        Event event = new Event("Event1", "Group", "Desc", "Org", LocalDate.now().plusDays(1), "Venue", 10, 3);
        event.setId(1L);
        Mockito.when(emsService.getEvent(1L)).thenReturn(Optional.of(event));

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("currentUser", user);

        mockMvc.perform(MockMvcRequestBuilders.post("/register-group")
                .param("eventId", "1")
                .param("employeeId", "EMP1")
                .param("mobileNumber", "1234567890")
                .param("team_member", "EMP2")
                .param("team_member", "EMP2")
                .session(session)
                .header("Accept", "application/json"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testRegisterGroupWithWrongTeamSize() throws Exception {
        User user = new User("John", "john@gmail.com", "pass", "USER");
        Event event = new Event("Event1", "Group", "Desc", "Org", LocalDate.now().plusDays(1), "Venue", 10, 5);
        event.setId(1L);
        Mockito.when(emsService.getEvent(1L)).thenReturn(Optional.of(event));

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("currentUser", user);

        mockMvc.perform(MockMvcRequestBuilders.post("/register-group")
                .param("eventId", "1")
                .param("employeeId", "EMP1")
                .param("mobileNumber", "1234567890")
                .param("team_member", "EMP2")
                .param("team_member", "EMP3")
                .session(session)
                .header("Accept", "application/json"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testRegisterGroupWhenCapacityExceeded() throws Exception {
        User user = new User("John", "john@gmail.com", "pass", "USER");
        Event event = new Event("Event1", "Group", "Desc", "Org", LocalDate.now().plusDays(1), "Venue", 10, 3);
        event.setId(1L);
        Mockito.when(emsService.getEvent(1L)).thenReturn(Optional.of(event));
        Mockito.when(emsService.getUserRegistrationsForEvent(user, event)).thenReturn(Arrays.asList());
        Mockito.when(emsService.countParticipants(event)).thenReturn(8L);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("currentUser", user);

        mockMvc.perform(MockMvcRequestBuilders.post("/register-group")
                .param("eventId", "1")
                .param("employeeId", "EMP1")
                .param("mobileNumber", "1234567890")
                .param("team_member", "EMP2")
                .param("team_member", "EMP3")
                .session(session)
                .header("Accept", "application/json"))
                .andExpect(MockMvcResultMatchers.status().isConflict());
    }

}