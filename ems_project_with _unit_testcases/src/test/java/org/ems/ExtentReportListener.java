package org.ems;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ExtentReportListener implements TestWatcher, AfterAllCallback {

    private static ExtentReports extent;
    private static final ConcurrentHashMap<String, ExtentTest> testMap = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, ExtentTest> moduleTests = new ConcurrentHashMap<>();
    private static final Map<String, Integer> moduleCounters = new ConcurrentHashMap<>();
    private static final String REPORT_DIR = "target/extent-reports";
    private static final AtomicInteger passCount = new AtomicInteger(0);
    private static final AtomicInteger failCount = new AtomicInteger(0);
    private static final AtomicInteger skipCount = new AtomicInteger(0);

    static {
        // Initialize ExtentReports with custom configuration
        new File(REPORT_DIR).mkdirs();
        ExtentSparkReporter htmlReporter = new ExtentSparkReporter(REPORT_DIR + "/ExtentReport.html");
        
        htmlReporter.config().setDocumentTitle("EMS Test Execution Report");
        htmlReporter.config().setReportName("Event Management System - Complete Test Coverage Report");
        htmlReporter.config().setTheme(com.aventstack.extentreports.reporter.configuration.Theme.DARK);
        htmlReporter.config().setTimeStampFormat("dd/MM/yyyy hh:mm:ss a");
        
        extent = new ExtentReports();
        extent.attachReporter(htmlReporter);
        
        // Set system information
        extent.setSystemInfo("Java Version", System.getProperty("java.version"));
        extent.setSystemInfo("Operating System", System.getProperty("os.name") + " " + System.getProperty("os.version"));
        extent.setSystemInfo("JVM", System.getProperty("java.vm.name"));
        extent.setSystemInfo("Test Framework", "JUnit 5 (Jupiter)");
        extent.setSystemInfo("Spring Boot Version", "3.2.4");
        extent.setSystemInfo("Build Tool", "Apache Maven 3.x");
        extent.setSystemInfo("Database", "PostgreSQL (Supabase)");
        extent.setSystemInfo("Test Environment", "Development");
        extent.setSystemInfo("Report generated", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }

    @Override
    public void testDisabled(ExtensionContext context, Optional<String> reason) {
        createAndLogTest(context, "SKIP", "Test Disabled: " + reason.orElse("No reason provided"));
        skipCount.incrementAndGet();
    }

    @Override
    public void testSuccessful(ExtensionContext context) {
        createAndLogTest(context, "PASS", "Test executed successfully");
        passCount.incrementAndGet();
    }

    @Override
    public void testAborted(ExtensionContext context, Throwable cause) {
        createAndLogTest(context, "SKIP", "Test Aborted: " + (cause != null ? cause.getMessage() : "Unknown reason"));
        skipCount.incrementAndGet();
    }

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        String message = "Test Failed";
        if (cause != null) {
            message += ": " + cause.getMessage();
            failCount.incrementAndGet();
        }
        createAndLogTest(context, "FAIL", message);
    }

    private void createAndLogTest(ExtensionContext context, String status, String message) {
        String className = context.getRequiredTestClass().getSimpleName();
        String testName = context.getDisplayName();
        
        // Determine module based on class name
        String moduleName = getModuleName(className);
        
        // Create test under module
        ExtentTest test = extent.createTest(testName);
        test.assignCategory(moduleName);
        
        // Add status and message
        if (status.equals("PASS")) {
            test.pass(message);
        } else if (status.equals("FAIL")) {
            test.fail(message);
        } else if (status.equals("SKIP")) {
            test.skip(message);
        }
        
        testMap.put(testName, test);
        moduleCounters.put(moduleName, moduleCounters.getOrDefault(moduleName, 0) + 1);
    }

    private String getModuleName(String className) {
        if (className.contains("AuthController")) {
            return "🔐 Authentication Controller";
        } else if (className.contains("ApiController")) {
            return "🔌 API Controller";
        } else if (className.contains("RegistrationController")) {
            return "📝 Registration Controller";
        } else if (className.contains("EMSService")) {
            return "⚙️ EMS Service";
        }
        return "General Tests";
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        if (extent != null) {
            extent.flush();
        }
    }
}
