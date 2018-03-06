package com.project.glib.controller;

import com.project.glib.dao.implementations.*;
import com.project.glib.model.*;
import com.project.glib.validator.UserValidator;
import javafx.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

//@Controller
@RestController
public class LibrarianController {
    private final UsersDaoImplementation usersDao;
    private final BookDaoImplementation bookDao;
    private final DocumentPhysicalDaoImplementation physicalDaoImplementation;
    private final BookingDaoImplementation bookingDao;
    private final CheckoutDaoImplementation checkoutDao;
    private final UserValidator userValidator;

    @Autowired
    public LibrarianController(UsersDaoImplementation usersDao, BookDaoImplementation bookDao, DocumentPhysicalDaoImplementation physicalDaoImplementation, BookingDaoImplementation bookingDao, CheckoutDaoImplementation checkoutDao, UserValidator userValidator) {
        this.usersDao = usersDao;
        this.bookDao = bookDao;
        this.physicalDaoImplementation = physicalDaoImplementation;
        this.bookingDao = bookingDao;
        this.checkoutDao = checkoutDao;
        this.userValidator = userValidator;
    }


    @RequestMapping(value = "/librarian", method = RequestMethod.GET)
    public ModelAndView librarianDashboard(Model model, String login, String logout) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("user", usersDao.findByLogin(login));
        modelAndView.setViewName("librarian");
        return modelAndView;
    }

    @RequestMapping(value = "/librarian", method = RequestMethod.POST)
    public ModelAndView librarianDashboard(User user) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("error", " ");
        modelAndView.setViewName("librarian");
        return modelAndView;
    }

    @RequestMapping(value = "/librarian/user/confirm", method = RequestMethod.GET)
    //   public ModelAndView librarianConfirm(Model model, String login) {
    public List<User> librarianConfirm(Model model, String login) {
//        ModelAndView modelAndView = new ModelAndView();
//        modelAndView.addObject("allUsers", usersDao.getList());
//        modelAndView.setViewName("confirm");
        return usersDao.getListNotAuthUsers();
    }

    @RequestMapping(value = "/librarian/user/confirm", method = RequestMethod.POST)
//    public ModelAndView librarianConfirm(User user, String login) {
    public String librarianConfirm(@RequestBody User user) throws Exception {
        user.setAuth(true);
        try {
            usersDao.update(user);
            return "- successfully updated -";
        } catch (Exception e) {
            return "- failed! -";
        }
    }

    @RequestMapping(value = "librarian/add/book", method = RequestMethod.POST)
    public void addBook(@RequestBody Book book, @RequestParam(value = "shelf") String shelf,
                        @RequestParam(value = "isReference") boolean flag) {
        if (!bookDao.isAlreadyExist(book)) {
            try {
                bookDao.add(book);
                for (int i = 0; i < book.getCount(); i++) {
                    DocumentPhysical document = new DocumentPhysical();
                    document.setShelf(shelf);
                    document.setIdDoc(book.getId());
                    document.setShelf(shelf);
                    document.setDocType("BOOK");
                    document.setCanBooked(true);
                    document.setReference(flag);
                    physicalDaoImplementation.add(document);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @RequestMapping(value = "/librarian/user/delete", method = RequestMethod.GET)
    //   public ModelAndView librarianConfirm(Model model, String login) {
    public List<User> librarianDeleteUser(Model model, String login) {
//        ModelAndView modelAndView = new ModelAndView();
//        modelAndView.addObject("allUsers", usersDao.getList());
//        modelAndView.setViewName("confirm");
        return usersDao.getListAuthUsers();
    }

    @RequestMapping(value = "/librarian/user/delete", method = RequestMethod.POST)
//    public ModelAndView librarianConfirm(User user, String login) {
    public String librarianDeleteUser(@RequestParam String login) throws Exception {
        try {
            usersDao.remove(usersDao.getIdByLogin(login));
            return "- successfully deleted -";
        } catch (Exception e) {
            return "- failed! -";
        }
    }


    @RequestMapping(value = "/librarian/user/info", method = RequestMethod.GET)
//    public ModelAndView librarianConfirm(User user, String login) {
    public Pair<User,List<Checkout>> librarianGetInfo(@RequestParam String login) throws Exception {
        User user = usersDao.findByLogin(login);
        if (user != null){
       List<Checkout> checkout = checkoutDao.getCheckoutsByUser(user.getId());
        Pair pair = new Pair(user,checkout);
       return pair;}else{
            throw new Exception("User is not exist");
        }
    }

}
