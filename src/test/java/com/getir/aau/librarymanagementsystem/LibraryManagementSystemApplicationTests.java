package com.getir.aau.librarymanagementsystem;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class LibraryManagementSystemApplicationTests {

	@Test
	@DisplayName("The context loads successfully")
	void contextLoads() {
	}

	@Nested
	@DisplayName("Application Class Tests")
	class ApplicationClassTests {

		@Test
		@DisplayName("Main method should not throw exception")
		void mainMethodShouldNotThrowException() {
			LibraryManagementSystemApplication.main(new String[]{});
		}
	}
}