package com.ust.user.controller;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
	public ResponseEntity<?> registerUser(@RequestBody User user) throws Exception
	{
		System.out.println(user.getUsername());
		System.out.println(user.getName());
		System.out.println(user.getPassword());
		System.out.println(user.getAddress());
		System.out.println(user.getConpassword());
		System.out.println(user.getEmail());
		System.out.println(user.getPhone());
		
		String tempUsername=user.getUsername();
		if(tempUsername != null && tempUsername!="") {
			User userobj = userService.getUserByUsername(tempUsername);
			if(userobj != null ) {
				throw new Exception("user with "+tempUsername+" already exists");
			}
		}
		
		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		String encodedPassword = passwordEncoder.encode(user.getPassword());
		
		User user2 = new User( user.getName(), user.getUsername(), encodedPassword, user.getEmail(), user.getAddress(), user.getPhone(), encodedPassword,user.getType());
		
		
		User registerUser = userService.registerUser(user2);
		return new ResponseEntity<User>(registerUser, HttpStatus.OK);
	}

	@PostMapping("/users/login")
	public ResponseEntity<?> logIn(@RequestBody User user)
	{
		System.out.println(user.getUsername());
		System.out.println(user.getPassword());

		User user1 = userService.getUserByUsername(user.getUsername());

		if(user1==null && (user.getUsername().equals("admin") && user.getPassword().equals("admin"))){
			user1 = new User();
			user1.setUsername("admin");
			user1.setPassword("admin");
			user1.setType("admin");
		}

		if(user1!=null){
			ResponseEntity<Map<String, String>> response = null;

			String typ = user.getType();
			String typp = user1.getType();
			String usertype;

			if(typp.equals(typ)){
				usertype = typ;
			}
			else{
				return new ResponseEntity<User>(HttpStatus.NOT_FOUND);
			}

			if(usertype.equals("user")){
				Boolean b = BCrypt.checkpw(user.getPassword(), user1.getPassword());
				if(b) {
					Map<String, String> tokenMap = new HashMap<String, String>();
					String token = Jwts.builder().setId(user1.getUsername()).setIssuedAt(new Date())
								.signWith(SignatureAlgorithm.HS256, "usersecretkey").compact();
					tokenMap.put("token", token);
					tokenMap.put("message", "User Successfully logged in");
					response = new ResponseEntity<Map<String, String>>(tokenMap, HttpStatus.OK);
				}
				else{
					return new ResponseEntity<User>(HttpStatus.NOT_FOUND);
				}
			}

			else if(usertype.equals("admin")){
				if(user1.getUsername().equals("admin") && user1.getPassword().equals("admin")){
					Map<String, String> tokenMap = new HashMap<String, String>();
					String token = Jwts.builder().setId(user1.getUsername()).setIssuedAt(new Date())
								.signWith(SignatureAlgorithm.HS256, "adminsecretkey").compact();
					tokenMap.put("token", token);
					tokenMap.put("message", "Admin Successfully logged in");
					response = new ResponseEntity<Map<String, String>>(tokenMap, HttpStatus.OK);
				}
				else{
					return new ResponseEntity<User>(HttpStatus.NOT_FOUND);
				}
			}

			else if(usertype.equals("kitchen staff")) {
				Boolean b = BCrypt.checkpw(user.getPassword(), user1.getPassword());
				if(b) {
					Map<String, String> tokenMap = new HashMap<String, String>();
					String token = Jwts.builder().setId(user1.getUsername()).setIssuedAt(new Date())
								.signWith(SignatureAlgorithm.HS256, "kitchenstaffsecretkey").compact();
					tokenMap.put("token", token);
					tokenMap.put("message", "Kitchen Staff Successfully logged in");
					response = new ResponseEntity<Map<String, String>>(tokenMap, HttpStatus.OK);
				}
				else{
					return new ResponseEntity<User>(HttpStatus.NOT_FOUND);
				}
			}
			return response;
		}
		return new ResponseEntity<User>( HttpStatus.NOT_FOUND);
	}
}
