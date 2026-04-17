package org.ems.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        info = @Info(
                title = "EMS Event Management API",
                version = "1.0",
                description = "API documentation for EMS user authentication, event management, registration, and admin workflows.",
                contact = @Contact(name = "EMS Support", email = "support@example.com")
        ),
        servers = {
                @Server(url = "http://localhost:8081", description = "Local EMS development server")
        }
)
@Configuration
public class OpenApiConfig {
}
