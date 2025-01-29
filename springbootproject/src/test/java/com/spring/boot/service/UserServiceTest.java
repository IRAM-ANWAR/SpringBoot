package com.spring.boot.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.spring.boot.entity.User;
import com.spring.boot.repository.UserRepository;

@SpringBootTest
public class UserServiceTest {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	UserService userService;

	@BeforeEach
	void setUp() {

	}

	@BeforeAll
	void setupDone() {
	}

	@Disabled
	public void test(int a, int b, int expected) {
		assertEquals(expected, a + b);
	}

	@ParameterizedTest
	@CsvSource({ "Ram,Iram" })
	public void testFindByUserName(String userName) {
		assertNotNull(this.userRepository.findByUserName(userName), "Failed for " + userName);
	}

	@ParameterizedTest
	@ValueSource(strings = { "Ram", "Iram" })
	public void testFindByUserName2(String userName) {
		assertNotNull(this.userRepository.findByUserName(userName), "Failed for " + userName);
	}

	@ParameterizedTest
	@ArgumentsSource(UserArguementsProvider.class)
	public void testSaveUser(User user) {
		this.userService.saveNewUser(user);
		assertNotNull(this.userService.saveNewUser(user));
	}

}
