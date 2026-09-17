package com.scamshield;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:scamshield;MODE=PostgreSQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=none"
})
class ScamShieldApplicationTests {

    @Test
    void applicationContextLoads() {
        // Context smoke test; module-specific tests will be added with each feature.
    }
}
