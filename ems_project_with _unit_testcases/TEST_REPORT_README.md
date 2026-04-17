# EMS Test Report Configuration - Quick Reference

## 📊 Automatic Extent Report Generation

The Event Management System now automatically generates a comprehensive Extent Report when running tests.

### How It Works

When you execute:
```bash
mvn clean test
```

The system automatically:
1. ✅ Compiles all test classes
2. ✅ Executes all tests with the ExtentReportListener
3. ✅ Categorizes tests by module (Authentication, API, Registration, Service)
4. ✅ Captures test results (Pass/Fail/Skip status)
5. ✅ Generates professional HTML report
6. ✅ Saves report to: `target/extent-reports/ExtentReport.html`

---

## 📁 Report Structure

The Extent Report organizes tests into **4 main modules**:

### 🔐 Authentication Controller
- Login validation tests
- User registration tests  
- Password reset tests
- Admin authentication tests
- Email format validation

### 🔌 API Controller
- Dashboard functionality
- Event retrieval and listing
- Event creation (individual/group)
- Event deletion
- Capacity validation

### 📝 Registration Controller
- Individual event registration
- Group event registration
- Duplicate prevention
- Authentication checks
- Capacity management

### ⚙️ EMS Service
- User authentication service
- Event management service
- Registration service
- Participant counting
- Database operations

---

## 📋 Report Contents

Each module displays:
- ✅ Test names and descriptions
- 📊 Pass/Fail/Skip status indicators
- ⏱️ Execution time
- 🔍 Error messages (if applicable)
- 📈 Module-level statistics
- 🖥️ System information (Java version, OS, Spring Boot, Database)

---

## 🖥️ System Information Included

The report captures:
- **Java Version**: 25.0.2
- **Operating System**: Windows/Linux details
- **JVM**: Java Virtual Machine info
- **Test Framework**: JUnit 5 (Jupiter)
- **Spring Boot**: 3.2.4
- **Build Tool**: Apache Maven 3.x
- **Database**: PostgreSQL (Supabase)
- **Test Generation Timestamp**: Automatically captured

---

## 📂 Files Configuration

### Modified Files:
1. **pom.xml** - Added Extent Reports dependency
2. **ExtentReportListener.java** - Custom test listener for report generation
3. **junit-platform.properties** - JUnit 5 extension configuration

### Test Classes Updated:
- AuthControllerTest - Added @ExtendWith(ExtentReportListener.class)
- ApiControllerTest - Added @ExtendWith(ExtentReportListener.class)
- RegistrationControllerTest - Added @ExtendWith(ExtentReportListener.class)
- EMSServiceTest - Added @ExtendWith(ExtentReportListener.class)

---

## 🚀 Running Tests

### Generate Report:
```bash
cd C:\Users\VigneshMohan\EMS\ems_project_for_deployment_changes_fix\ems_project_for_deployment_changes_fix
mvn clean test
```

### View Report:
```
Open: target/extent-reports/ExtentReport.html
```

### Run Specific Test Class:
```bash
mvn test -Dtest=AuthControllerTest
```

### Run Specific Test Method:
```bash
mvn test -Dtest=AuthControllerTest#testLoginWithValidCredentials
```

---

## 📊 Report Features

✨ **Modern Design**
- Dark theme for reduced eye strain
- Responsive layout
- Mobile-friendly interface

🔍 **Search & Filter**
- Search tests by name
- Filter by module/category
- Sort by status

📈 **Statistics**
- Total tests count
- Pass/Fail/Skip breakdown
- Success rate percentage
- Execution time tracking

🏗️ **Module Organization**
- Separate sections for each module
- Category-based grouping
- Hierarchical structure

🔗 **Navigation**
- Easy test discovery
- Quick status overview
- Detailed test information

---

## ⚙️ Technical Details

### ExtentReports Version: 5.1.1
- Professional HTML reporting framework
- Category-based test organization
- Real-time test status capture
- System information logging

### JUnit 5 Integration
- TestWatcher interface for lifecycle hooks
- ExtensionContext for test metadata
- AfterAllCallback for report finalization

### Test Listener Implementation
- Automatic test categorization by module
- Status tracking (PASS/FAIL/SKIP)
- Detailed error messages
- Execution timestamps

---

## 💡 Tips

1. **Always run `mvn clean test`** to ensure fresh test execution
2. **Report is auto-generated** - No manual steps needed
3. **Module organization** - Scroll through different modules in the report
4. **Search functionality** - Use the search bar to find specific tests
5. **Categories** - Tests are automatically grouped by their test class module

---

## 📌 Next Steps

- View the generated report in your browser
- Share the HTML file with stakeholders
- Track test coverage over time
- Monitor module-wise test results
- Analyze failure patterns
