## Spring Boot 4
This project uses the recently release SpringBoot4. You may not be aware of new packages but if the code compiles USE THEM.

## Changes That Must Be Approved
Never change imports without user approval
Never change `IntegrationTestBase` without user approval
Never change `build.gradle` without user approval

## Markdown Management
As new endpoints are created, place a reference to them in corresponding section in `ENDPOINTS.md`

## Code Formatting

- Blank Lines: Use to separate logical blocks of code.
- Line Length: Maximum 120 characters.
- Use IntelliJ IDEA default code style for Java.

## Package Structure
- config - @Configuration classes for bean/property creation during startup
- controller - controllers for handling REST path/parameter definitions go here
- service - business logic for handling REST requests go into this package. Reuse existing services if that makes sense.
- repository - Database Access Repositories or External HTTP connectors go in this package
- dto - here are DTO (Data Transfer Object) for returning to the UI
- dao - here are DAO (Data Access Object) for deserializing database results into a POJO (Plan Old Java Object)
    - hibernate/Spring Data will use these
- exception - all created exceptions go here
- utils - utility functions (usually static) go here

## Java Style

- Use UTF-8 encoding.
- Use descriptive names for classes, methods, and variables.
- Avoid `var` keyword, prefer explicit types.
- All method parameters should be `final`.
- All variables should be declared as `final` where possible.
- Preference for immutability:
- Avoid mutations of objects, specially when using for-each loops or Stream API using `forEach()`.
- Avoid magic numbers and strings; use constants instead.
- Check emptiness and nullness before operations on collections and strings.
- Avoid methods using `throws` clause; prefer unchecked exceptions.

- Avoid comments.
- Comments could be applied for: cron expressions, Regex patterns, TODOs or given/when/then separation in tests.
- Use `@Override` annotation when overriding methods.
- Wrap multiple conditions in a boolean variable for better readibility
- Prefer early returns.
- Avoid else statements when not necessary and try early returns.

## Lombok Annotations

- Use `@RequiredArgsConstructor` from Lombok for dependency injection via constructor.
- Use `@Slf4j` from Lombok for logging.
- Use `@Builder(setterPrefix = "with"))` for complex object creation.
- Avoid `@Data` annotation; prefer `@Getter` and `@Setter` for granular control.

## Annotations

- **`@Service`**: For business logic classes.
- **`@Repository`**: For data access classes that extend JPA repositories or interact with the database.
- **`@RestController`**: For web controllers.
- **`@Component`**: For generic Spring components.
- **`@Configuration`**: For Spring configuration classes.
- **`@Autowired`**: Prefer constructor injection for production code and field injection only for tests.
- **`@ConfigurationProperties`**: For binding related properties avoid multiple `@Value` annotations. From more than 2 properties, consider using this annotation.
- **`@Transactional`**: Only Service classes should be annotated with @Transactional at class level to avoid transaction management in each method.
- Circular dependencies should be avoided. Avoid `@Order` annotation for dependency resolution.

## Exception Handling

- Custom Exceptions: Create custom domain exception classes extending `RuntimeException`.
- Global Exception Handler: Use `@ControllerAdvice` and `@ExceptionHandler` to handle exceptions globally.
- HTTP Status Codes: Map exceptions to appropriate HTTP status codes in REST controllers.
- Error Response Structure: Define a consistent error response structure

## Testing

- Use JUnit 5 for unit and integration testing.
- Use Mockito for mocking dependencies in unit tests.
- Look at existing patterns in tests and match them where possible
- Use `@SpringBootTest` for integration tests that require the Spring context.
- Use `given/when/then` structure in test methods for clarity.
- Method naming could follow snake_case or camelCaset convention for test methods (e.g., `get_user_by_id_ok`, `get_user_by_id_not_found_ko`).
- Avoid reflection in tests.
- Avoid business logic in tests; focus on behavior verification.

## Logging

- Use `@Slf4j` annotation from Lombok for logging to avoid boilerplate code with Logger instances.
- lombok.config changes logger name to LOGGER
- Log at appropriate levels: `DEBUG`, `INFO`, `WARN`, `ERROR`.

## Tests
After any code changes, run all tests and ensure none fail
`./gradlew test`