package com.cxy7.data.fense.controller;

import com.alibaba.fastjson.JSONObject;
import com.cxy7.data.fense.model.User;
import com.cxy7.data.fense.security.FenseUserDetailsService;
import com.cxy7.data.fense.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Optional;

/**
 * @Author: XiaoYu
 * @Date: 2021/3/21 0:10
 */
@Controller
@RequestMapping("/user")
public class UserController {
	private static final Logger logger = LoggerFactory.getLogger(UserController.class);
	@Autowired
	private UserService userService;
	@Autowired
	private FenseUserDetailsService fenseUserDetailsService;

	@RequestMapping("/skin-config.html")
	public String skinConfig() {
		return "skin-config";
	}

	@RequestMapping("/logout")
	public String logout(HttpSession session) {
		session.removeAttribute(User.SESSION_KEY);
        SecurityContextHolder.getContext().setAuthentication(null);
		return "login";
	}
	@RequestMapping("/login")
	public String login(HttpServletRequest request, Model model, String username, String password) {
		if (StringUtils.isAnyBlank(username, password)) {
			logger.info("user:{}, pass:{}", username, password);
			logger.error("用户名或密码输入错误！");
		}
		User user = userService.login(username, password);
		if (user != null) {
			logger.debug("登录成功!{}", username);
			HttpSession session = request.getSession();
			session.setAttribute(User.SESSION_KEY, user);

			UserDetails userDetails = fenseUserDetailsService.loadUserByUsername(user.getName());

			// For simple validation it is completely sufficient to just check the token integrity. You don't have to call
			// the database compellingly. Again it's up to you ;)
			UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
			authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
			SecurityContextHolder.getContext().setAuthentication(authentication);
			return "index";
		} else {
			logger.warn("登录失败！user:{}, pass:{}", username, password);
			logger.error("用户名或密码输入错误！");
		}
		return "login";
	}

	@RequestMapping("/register")
	public String register(Model model, String username, String password, String email) {
		if (StringUtils.isAnyBlank(username, password, email)) {
			logger.info("user:{}, pass:{}, email:{}", username, password, email);
			model.addAttribute("msg", "参数输入错误！");
			return "500";
		}
		// 检查用户名与邮箱是否被占用
		if (userService.checkDuplicateName(username)) {
			model.addAttribute("msg", "名字重复：" + username);
			return "/register";
		}
		// 检查用邮箱是否被占用
		Optional<User> op = userService.findUserByEmail(email);
		if (op.isPresent()) {
			model.addAttribute("msg", "邮箱已被占用：" + email);
			return "/register";
		}

		User user = userService.register(username, password, email);
		logger.info("注册成功，请登录！");

		return "/login";
	}

	@RequestMapping("/iforgot")
	public String iforgot(Model model, String email) {
		if (StringUtils.isAnyBlank(email)) {
			logger.info("email:{}", email);
			model.addAttribute("msg", "参数输入错误！");
			return "/iforgot";
		}
		// 检查用邮箱是否存在
		Optional<User> op = userService.findUserByEmail(email);
		if (!op.isPresent()) {
			model.addAttribute("msg", "邮箱不存在：" + email);
			return "/iforgot";
		}
		User user = op.get();
		userService.getResetHref(user);
		model.addAttribute("email", email);
		return "/iforgot_confirm";
	}

	@RequestMapping("/resetPassword")
	public String checkToken(Model model, String token) {
		if (StringUtils.isAnyBlank(token)) {
			logger.info("token:{}", token);
			model.addAttribute("msg", "参数输入错误！");
			return "/login";
		}

		String email = userService.decryptToken(token);
		if (email == null) {
			logger.warn("解密失败：{}", token);
			return "/login";
		} else {
			model.addAttribute("token", token);
			return "/reset_password";
		}
	}

	@RequestMapping("/resetPassword2")
	public String resetPassword2(Model model, String token, String password) {
		if (StringUtils.isAnyBlank(token, password)) {
			logger.info("email:{}, password:{}", token, password);
			model.addAttribute("msg", "参数输入错误！");
			return "/login";
		}
		String email = userService.decryptToken(token);
		if (email == null) {
			logger.warn("解密失败：{}", token);
			return "/login";
		}

		userService.resetPassword(email, password);
		return "/login";
	}

	@ResponseBody
	@RequestMapping(value = "list")
	public JSONObject list(int page, int rows) {
		if (page < 0 || rows < 0) {
			return new JSONObject();
		}
		PageRequest pageable = PageRequest.of(page - 1, rows);

		return userService.list(pageable);
	}

	@ResponseBody
	@RequestMapping(value = "/edit")
	public boolean edit(Model model, String oper, String id, String name, String pass, String email) {
		boolean succ = false;
		if ("add".equals(oper)) {
			register(model, name, pass, email);

			succ = true;
		} else if ("edit".equals(oper)){
			if (StringUtils.isAnyBlank(id, name, pass, email)) {
				logger.error("参数输入错误！");
				return succ;
			}
			int userId = Integer.parseInt(id);
			return userService.edit(userId, name, pass, email);
		} else if ("del".equals(oper)){
			if (StringUtils.isAnyBlank(id)) {
				logger.error("ID不能为空！");
				return succ;
			}
			int userId = Integer.parseInt(id);
			userService.delete(userId);
			succ = true;
		}
		return succ;
	}

	@ResponseBody
	@RequestMapping(value = "/listForGrant")
	public String listForGrant() {
		return userService.listForGrant();
	}

}
