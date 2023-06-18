package com.ust.user.controller;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.ust.user.entity.User;
import com.ust.user.service.UserService;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/api")
public class UserController {

	@Autowired
	UserService userService;

	@PostMapping("/user/register")
	public ResponseEntity<?> registerUser(@RequestBody User user) throws Exception {
		String tempUsername = user.getUsername();
		if (tempUsername != null && !tempUsername.isEmpty()) {
			User userObj = userService.getUserByUsername(tempUsername);
			if (userObj != null) {
				throw new Exception("User with username " + tempUsername + " already exists");
			}
		}

		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		String encodedPassword = passwordEncoder.encode(user.getPassword());

		if(user.getRestname()!=null){
			user.setType("kitchen staff");
		}
		User newUser = new User(user.getName(), user.getUsername(), encodedPassword, user.getEmail(),
				user.getAddress(), user.getPhone(), encodedPassword, user.getType(), user.getRestname());

		User registeredUser = userService.registerUser(newUser);
		return new ResponseEntity<User>(registeredUser, HttpStatus.OK);
	}

	@PostMapping("/users/login")
	public ResponseEntity<?> logIn(@RequestBody User user) {
		String username = user.getUsername();
		String password = user.getPassword();

		User user1 = userService.getUserByUsername(username);

		if (user1 == null) {
			if (username.equals("admin") && password.equals("admin")) {
				User adminUser = new User();
				adminUser.setUsername("admin");
				adminUser.setPassword("admin");
				adminUser.setType("admin");
				user1 = adminUser;
			} else {
				return new ResponseEntity<User>(HttpStatus.NOT_FOUND);
			}
		}

		if (user1.getRestname() != null) {
			user1.setType("kitchen staff");
		}

		ResponseEntity<Map<String, String>> response = null;

		if (user1.getType().equals("user")) {
			BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
			if (passwordEncoder.matches(password, user1.getPassword())) {
				Map<String, String> tokenMap = new HashMap<String, String>();
				String token = Jwts.builder().setId(user1.getUsername()).setIssuedAt(new Date())
						.signWith(SignatureAlgorithm.HS256, "usersecretkey").compact();
				tokenMap.put("token", token);
				tokenMap.put("message", "User successfully logged in");
				response = new ResponseEntity<Map<String, String>>(tokenMap, HttpStatus.OK);
			} else {
				return new ResponseEntity<User>(HttpStatus.NOT_FOUND);

			}
		} else if (user1.getUsername().equals("admin") && user1.getPassword().equals("admin")) {
			if (user1.getType().equals("admin")) {
				Map<String, String> tokenMap = new HashMap<String, String>();
				String token = Jwts.builder().setId(user1.getUsername()).setIssuedAt(new Date())
						.signWith(SignatureAlgorithm.HS256, "adminsecretkey").compact();
				tokenMap.put("token", token);
				tokenMap.put("message", "Admin successfully logged in");
				response = new ResponseEntity<Map<String, String>>(tokenMap, HttpStatus.OK);
			} else {
				return new ResponseEntity<User>(HttpStatus.NOT_FOUND);
			}
		} else if (user1.getType().equals("kitchen staff")) {
			BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
			if (passwordEncoder.matches(password, user1.getPassword())) {
				Map<String, String> tokenMap = new HashMap<String, String>();
				String token = Jwts.builder().setId(user1.getUsername()).setIssuedAt(new Date())
						.signWith(SignatureAlgorithm.HS256, "kitchenstaffsecretkey").compact();
				tokenMap.put("token", token);
				tokenMap.put("message", "Kitchen Staff successfully logged in");
				response = new ResponseEntity<Map<String, String>>(tokenMap, HttpStatus.OK);
			} else {
				return new ResponseEntity<User>(HttpStatus.NOT_FOUND);
			}
		}

		if (response == null) {
			return new ResponseEntity<User>(HttpStatus.NOT_FOUND);
		}
		return response;
	}

	@GetMapping("/user/details/{username}")
	public User getUserByUsername(@PathVariable String username){
		return userService.getUserByUsername(username);
	}
}
