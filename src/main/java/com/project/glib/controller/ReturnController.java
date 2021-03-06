package com.project.glib.controller;

import com.project.glib.model.Checkout;
import com.project.glib.service.CheckoutService;
import com.project.glib.service.ReturnService;
import com.project.glib.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
public class ReturnController {
    private final UserService userService;
    private final CheckoutService checkoutService;
    private final ReturnService returnService;

    @Autowired
    public ReturnController(UserService userService,
                            CheckoutService checkoutService,
                            ReturnService returnService) {
        this.userService = userService;
        this.checkoutService = checkoutService;
        this.returnService = returnService;
    }

    //return list of books
    @ResponseBody
    @RequestMapping(value = "/return", method = RequestMethod.GET)
    public List<Checkout> checkoutBook(@RequestParam String login) {
        try {
            ModelAndView modelAndView = new ModelAndView();
            modelAndView.addObject("allBookings", checkoutService.getCheckoutsByUser(userService.getIdByLogin(login)));
            modelAndView.setViewName("order_book");
            return checkoutService.getCheckoutsByUser(userService.getIdByLogin(login));
        } catch (Exception e) {
            return null;
        }
    }

    @RequestMapping(value = "/return", method = RequestMethod.POST)
    public String checkoutBook(@RequestBody Checkout checkout, Model model) {
        try {
            return "overdue: " + returnService.toReturnDocument(checkout).getValue().toString();
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return e.getMessage();
        }
    }

    @RequestMapping(value = "/return/all", method = RequestMethod.POST)
    public String returnAllDocument(@RequestParam String login, Model model) {
        try {
            List<Checkout> checkoutList = checkoutService.getCheckoutsByUser(userService.getIdByLogin(login));
            for (Checkout checkout : checkoutList) {
                returnService.toReturnDocument(checkout);
            }
            return "successfully checkout";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return e.getMessage();
        }
    }
}
