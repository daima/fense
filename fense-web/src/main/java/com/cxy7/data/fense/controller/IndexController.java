package com.cxy7.data.fense.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;


/**
 * @Author: XiaoYu
 * @Date: 2021/3/21 0:10
 */
@Controller
public class IndexController {
	private static final Logger logger = LoggerFactory.getLogger(IndexController.class);

	@RequestMapping("/")
	public String index() {
		return "index";
	}

	@RequestMapping("/login")
	public String login() {
		return "login";
	}

	@RequestMapping("/register")
	public String register() {
		return "register";
	}

	@RequestMapping("/confirm_email")
	public String confirm_email() {
		return "confirm_email";
	}

	@RequestMapping("/iforgot")
	public String iforgot() {
		return "iforgot";
	}

	@RequestMapping("/iforgot_confirm")
	public String iforgot_confirm() {
		return "iforgot_confirm";
	}

	@RequestMapping("/users")
	@PreAuthorize("hasRole('ADMIN')")
	public String users() {
		return "users";
	}

	@RequestMapping("/roles")
	@PreAuthorize("hasRole('ADMIN')")
	public String roles() {
		return "roles";
	}

	@RequestMapping("/datasources")
	public String datasources() {
		return "datasources";
	}

	@RequestMapping("/datasets")
	public String datasets() {
		return "datasets";
	}

	@RequestMapping("/privileges")
	@PreAuthorize("hasRole('ADMIN')")
	public String privileges() {
		return "privileges";
	}

	@RequestMapping("/apply")
	public String apply() {
		return "apply";
	}

	@RequestMapping("/applies")
	public String applies() {
		return "applies";
	}

	@RequestMapping("/approvals")
	public String apply_history() {
		return "approvals";
	}

	@RequestMapping("/role_grant")
	@PreAuthorize("hasRole('ADMIN')")
	public String role_grant() {
		return "role_grant";
	}

	@RequestMapping("/skin-config.html")
	public String skinConfig() {
		return "skin-config";
	}
}
