package com.project.glib.controller;

import com.project.glib.dao.implementations.UserDaoImplementation;
import com.project.glib.model.User;
import com.project.glib.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class LoginController{

    UserService userService;

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String login(){
        return "login";
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public String login(@RequestBody User form){
        System.out.println();
        System.out.println();
        System.out.println(form);
        System.out.println();
        System.out.println();
        try {
            User user = userService.findByLogin(form.getLogin());
            if(user !=null && user.getPassword().equals(form.getPassword()))
                return "success";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "error";
    }

}