package io.pace.backend.controller;


import io.pace.backend.service.user_login.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/superadmin")
public class SuperAdminController {

    @Autowired
    UserService userService;
}
