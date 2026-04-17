package org.ems.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.ems.model.User;
import org.ems.service.EMSService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.net.URI;
import java.util.Map;
import java.util.Optional;

@Controller
@Tag(name = "Auth", description = "Authentication and password reset operations")
public class AuthController {

    private final EMSService emsService;

    public AuthController(EMSService emsService) {
        this.emsService = emsService;
    }

    private boolean isHtmlRequest(HttpServletRequest request) {
        String accept = request.getHeader("Accept");
        return accept != null && accept.contains("text/html");
    }

    private boolean hasInvalidPasswordChars(String password) {
        return password != null && (password.contains(" ") || password.contains(","));
    }

    private boolean containsUppercase(String input) {
        return input != null && input.matches(".*[A-Z].*");
    }

    private boolean isValidGmail(String email) {
        return email != null && email.matches("^[a-z0-9._%+-]+@gmail\\.com$");
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,}$");
    }

    @Operation(summary = "User login", description = "Authenticate a registered user and create a session")
    @PostMapping("/login")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> login(@RequestParam String email,
                                                     @RequestParam String password,
                                                     HttpServletRequest request) {
        if (!isValidGmail(email)) {
            if (containsUppercase(email)) {
                if (isHtmlRequest(request)) {
                    return ResponseEntity.status(302).location(URI.create("/index.html?error=Please+enter+a+valid+lowercase+Gmail+address")).build();
                }
                return ResponseEntity.badRequest().body(Map.of("error", "Please enter a valid lowercase Gmail address"));
            }
            if (isHtmlRequest(request)) {
                return ResponseEntity.status(302).location(URI.create("/index.html?error=Please+enter+a+valid+Gmail+address+using+%40gmail.com")).build();
            }
            return ResponseEntity.badRequest().body(Map.of("error", "Please enter a valid Gmail address using @gmail.com"));
        }
        if (hasInvalidPasswordChars(password)) {
            if (isHtmlRequest(request)) {
                return ResponseEntity.status(302).location(URI.create("/index.html?error=Password+cannot+contain+spaces+or+commas")).build();
            }
            return ResponseEntity.badRequest().body(Map.of("error", "Password cannot contain spaces or commas"));
        }
        Optional<User> user = emsService.authenticate(email, password);
        if (user.isEmpty()) {
            if (isHtmlRequest(request)) {
                return ResponseEntity.status(302).location(URI.create("/index.html?error=Invalid+credentials")).build();
            }
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
        }
        if (user.get().isAdmin()) {
            if (isHtmlRequest(request)) {
                return ResponseEntity.status(302).location(URI.create("/index.html?error=Admin+users+must+use+admin+login")).build();
            }
            return ResponseEntity.status(403).body(Map.of("error", "Admin users must use admin login"));
        }
        HttpSession session = request.getSession();
        session.setAttribute("currentUser", user.get());
        if (isHtmlRequest(request)) {
            return ResponseEntity.status(302).location(URI.create("/dashboard.html")).build();
        }
        return ResponseEntity.ok(Map.of(
                "status", "login successful",
                "email", user.get().getEmail(),
                "role", user.get().getRole()
        ));
    }

    @Operation(summary = "User registration", description = "Register a new user account")
    @PostMapping("/register")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> register(@RequestParam String full_name,
                                                        @RequestParam String email,
                                                        @RequestParam String password,
                                                        HttpServletRequest request) {
        if (!isValidGmail(email)) {
            if (containsUppercase(email)) {
                if (isHtmlRequest(request)) {
                    return ResponseEntity.status(302).location(URI.create("/register.html?error=Please+enter+a+valid+lowercase+Gmail+address")).build();
                }
                return ResponseEntity.badRequest().body(Map.of("error", "Please enter a valid lowercase Gmail address"));
            }
            if (isHtmlRequest(request)) {
                return ResponseEntity.status(302).location(URI.create("/register.html?error=Please+enter+a+valid+Gmail+address+using+%40gmail.com")).build();
            }
            return ResponseEntity.badRequest().body(Map.of("error", "Please enter a valid Gmail address using @gmail.com"));
        }
        if (hasInvalidPasswordChars(password)) {
            if (isHtmlRequest(request)) {
                return ResponseEntity.status(302).location(URI.create("/register.html?error=Password+cannot+contain+spaces+or+commas")).build();
            }
            return ResponseEntity.badRequest().body(Map.of("error", "Password cannot contain spaces or commas"));
        }
        if (emsService.emailExists(email)) {
            if (isHtmlRequest(request)) {
                return ResponseEntity.status(302).location(URI.create("/register.html?error=Email+already+exists")).build();
            }
            return ResponseEntity.status(409).body(Map.of("error", "Email already exists"));
        }
        emsService.registerUser(full_name, email, password);
        if (isHtmlRequest(request)) {
            return ResponseEntity.status(302).location(URI.create("/index.html?success=Registration+successful")).build();
        }
        return ResponseEntity.created(URI.create("/register")).body(Map.of(
                "status", "registration successful",
                "email", email
        ));
    }

    @Operation(summary = "Reset password", description = "Reset a registered user's password")
    @PostMapping("/reset-password")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> resetPassword(@RequestParam String email,
                                                             @RequestParam String new_password,
                                                             @RequestParam String confirm_password,
                                                             HttpServletRequest request) {
        if (!isValidGmail(email)) {
            if (containsUppercase(email)) {
                if (isHtmlRequest(request)) {
                    return ResponseEntity.status(302).location(URI.create("/recover-password.html?error=Please+enter+a+valid+lowercase+Gmail+address")).build();
                }
                return ResponseEntity.badRequest().body(Map.of("error", "Please enter a valid lowercase Gmail address"));
            }
            if (isHtmlRequest(request)) {
                return ResponseEntity.status(302).location(URI.create("/recover-password.html?error=Please+enter+a+valid+Gmail+address+using+%40gmail.com")).build();
            }
            return ResponseEntity.badRequest().body(Map.of("error", "Please enter a valid Gmail address using @gmail.com"));
        }
        if (hasInvalidPasswordChars(new_password) || hasInvalidPasswordChars(confirm_password)) {
            if (isHtmlRequest(request)) {
                return ResponseEntity.status(302).location(URI.create("/recover-password.html?error=Password+cannot+contain+spaces+or+commas")).build();
            }
            return ResponseEntity.badRequest().body(Map.of("error", "Password cannot contain spaces or commas"));
        }
        if (!new_password.equals(confirm_password)) {
            if (isHtmlRequest(request)) {
                return ResponseEntity.status(302).location(URI.create("/recover-password.html?error=Passwords+do+not+match")).build();
            }
            return ResponseEntity.badRequest().body(Map.of("error", "Passwords do not match"));
        }
        Optional<User> existing = emsService.findUserByEmail(email);
        if (existing.isEmpty()) {
            if (isHtmlRequest(request)) {
                return ResponseEntity.status(302).location(URI.create("/recover-password.html?error=Email+not+found")).build();
            }
            return ResponseEntity.status(404).body(Map.of("error", "Email not found"));
        }
        if (existing.get().isAdmin()) {
            if (isHtmlRequest(request)) {
                return ResponseEntity.status(302).location(URI.create("/recover-password.html?error=Admin+password+cannot+be+reset")).build();
            }
            return ResponseEntity.status(403).body(Map.of("error", "Admin password cannot be reset"));
        }
        emsService.resetPassword(email, new_password);
        if (isHtmlRequest(request)) {
            return ResponseEntity.status(302).location(URI.create("/index.html?success=Password+reset+successful")).build();
        }
        return ResponseEntity.ok(Map.of("status", "password reset successful"));
    }

    @Operation(summary = "Admin login", description = "Authenticate an admin user and create an admin session")
    @PostMapping("/admin-login")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> adminLogin(@RequestParam String admin_id,
                                                          @RequestParam String password,
                                                          HttpServletRequest request) {
        if (!isValidEmail(admin_id)) {
            if (containsUppercase(admin_id)) {
                if (isHtmlRequest(request)) {
                    return ResponseEntity.status(302).location(URI.create("/admin-login.html?error=Please+enter+a+valid+lowercase+email+address")).build();
                }
                return ResponseEntity.badRequest().body(Map.of("error", "Please enter a valid lowercase email address"));
            }
            if (isHtmlRequest(request)) {
                return ResponseEntity.status(302).location(URI.create("/admin-login.html?error=Please+enter+a+valid+email+address")).build();
            }
            return ResponseEntity.badRequest().body(Map.of("error", "Please enter a valid email address"));
        }
        if (hasInvalidPasswordChars(password)) {
            if (isHtmlRequest(request)) {
                return ResponseEntity.status(302).location(URI.create("/admin-login.html?error=Password+cannot+contain+spaces+or+commas")).build();
            }
            return ResponseEntity.badRequest().body(Map.of("error", "Password cannot contain spaces or commas"));
        }
        Optional<User> user = emsService.authenticate(admin_id, password);
        if (user.isEmpty()) {
            if (isHtmlRequest(request)) {
                return ResponseEntity.status(302).location(URI.create("/admin-login.html?error=Invalid+admin+credentials")).build();
            }
            return ResponseEntity.status(401).body(Map.of("error", "Invalid admin credentials"));
        }
        if (!user.get().isAdmin()) {
            if (isHtmlRequest(request)) {
                return ResponseEntity.status(302).location(URI.create("/admin-login.html?error=Access+forbidden%3A+admin+only")).build();
            }
            return ResponseEntity.status(403).body(Map.of("error", "Access forbidden: admin only"));
        }
        HttpSession session = request.getSession();
        session.setAttribute("currentUser", user.get());
        if (isHtmlRequest(request)) {
            return ResponseEntity.status(302).location(URI.create("/admin-dashboard.html")).build();
        }
        return ResponseEntity.ok(Map.of(
                "status", "admin login successful",
                "email", user.get().getEmail()
        ));
    }

    @Operation(summary = "Logout", description = "Invalidate the current user session")
    @PostMapping("/logout")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return ResponseEntity.status(401).body(Map.of("error", "No active session"));
        }
        session.invalidate();
        return ResponseEntity.ok(Map.of("status", "logout successful"));
    }

    @GetMapping("/logout")
    public String logoutRedirect(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return "redirect:/index.html?success=Logout+successful";
    }
}
