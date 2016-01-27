/**
 * 
 */
package com.cxjhihihi.springmvc.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.alibaba.fastjson.JSONObject;
import com.cxjhihihi.springmvc.domain.User;
import com.cxjhihihi.springmvc.service.UserService;

/**
 * 
 * @author hzcaixinjia
 * 
 * @date 2016-1-27
 * 
 */
@Controller
public class UserController {

	@Autowired
	@Qualifier("userService")
	UserService userService;

	@RequestMapping("/user/addUser.m")
	public void addUser(
			HttpServletRequest request,
			HttpServletResponse response,
			@RequestParam(value = "username", required = false) String username,
			@RequestParam(value = "password", required = false) String password)
			throws IOException {
		JSONObject jv = new JSONObject();
		User user = new User();
		user.setUsername(username);
		user.setPassword(password);
		userService.addUser(user);
		jv.put("code", "insert success");
		response.getWriter().write(jv.toJSONString());
	}
}
