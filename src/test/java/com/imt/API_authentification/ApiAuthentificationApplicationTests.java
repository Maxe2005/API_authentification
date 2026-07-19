package com.imt.API_authentification;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "app.security.secret=app-context-test-secret",
        "app.security.salt=app-context-test-salt",
        // Avoid an eager, blocking connection attempt to a real Mongo server at context
        // startup just to create the unique index — this test only checks that the bean
        // graph wires together, not that Mongo is reachable.
        "spring.data.mongodb.auto-index-creation=false"
})
class ApiAuthentificationApplicationTests {

	@Test
	void contextLoads() {
	}

}
