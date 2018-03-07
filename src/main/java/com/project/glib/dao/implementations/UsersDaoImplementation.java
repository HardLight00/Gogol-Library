package com.project.glib.dao.implementations;

import com.project.glib.dao.interfaces.ModifyByLibrarian;
import com.project.glib.dao.interfaces.RoleRepository;
import com.project.glib.dao.interfaces.UserRepository;
import com.project.glib.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UsersDaoImplementation implements ModifyByLibrarian<User> {
    private static final Logger logger = (Logger) LoggerFactory.getLogger(BookDaoImplementation.class);
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CheckoutDaoImplementation checkoutDao;

    @Autowired
    public UsersDaoImplementation(UserRepository userRepository,
                                  RoleRepository roleRepository, CheckoutDaoImplementation checkoutDao) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.checkoutDao = checkoutDao;
    }

    public void add(User user) throws Exception {
        //user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        //userRepository.save(user);
        //user.setRole(roleRepository.findOne(user.getId()));
        try {
            user.setAuth(false);
            userRepository.save(user);
            logger.info("User successfully saved. Document details : " + user);
        } catch (Exception e) {
            logger.info("Try to add user with wrong parameters. New user information : " + user);
            System.out.println(e.getMessage());
            throw new Exception("Can't add this user, some parameters are wrong");
        }

    }

    public void update(User user) throws Exception {
//        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        try {
            userRepository.saveAndFlush(user);
            logger.info("User successfully update. User details : " + user);
        } catch (Exception e) {
            logger.info("Try to update this user, user don't exist or some new user parameters are wrong. " +
                    "Update user information : " + user);
            throw new Exception("Can't update this user, user don't exist or some new user parameters are wrong");
        }
    }

    public void remove(long userId) throws Exception {
        try {
            logger.info("Try to delete user with user id = " + userId);
            System.out.println(checkoutDao.getCheckoutsByUser(userId));
            if (checkoutDao.getCheckoutsByUser(userId).size() == 0) {
                userRepository.delete(userId);
            } else {
                throw new Exception("User should return all documents before deleting");
            }
        } catch (Exception e) {
            logger.info("Try to delete user with wrong user id = " + userId);
            e.printStackTrace();
            throw new Exception("Delete this patron not available, patron doesn't exist");
        }
    }

    @SuppressWarnings("unchecked")
    public List<User> getList() {
        List<User> users = userRepository.findAll();

        for (User user : users) {
            logger.info("User list : " + user);
        }

        logger.info("User list successfully printed");
        return users;
    }

    public User getById(long userId) throws Exception {
        try {
            logger.info("Try to get user with user id = " + userId);
            return userRepository.findOne(userId);
        } catch (Exception e) {
            logger.info("Try to get user with wrong user id = " + userId);
            throw new Exception("Information not available, patron don't exist");
        }
    }

    public User findByLogin(String login) {
        logger.info("Try to get user with user login = \"" + login + "\"");
        return userRepository.findUserByLogin(login);
    }

    public List<User> getListAuthUsers() {
        logger.info("Authorized user list successfully printed");
        return userRepository.findAll().stream().filter(User -> User.getAuth()).collect(Collectors.toList());
    }

    public List<User> getListNotAuthUsers() {
        logger.info("Authorized user list successfully printed");
        return userRepository.findAll().stream().filter(User -> !User.getAuth()).collect(Collectors.toList());
    }

    public boolean getIsAuthById(long userId) throws Exception {
        try {
            logger.info("Try to get authorize user with user id = " + userId);
            return userRepository.findOne(userId).getAuth();
        } catch (Exception e) {
            logger.info("Try to get authorize user with wrong user id = " + userId);
            throw new Exception("Information not available, user don't exist");
        }
    }

    public String getTypeById(long userId) throws Exception {
        try {
            logger.info("Try to get user type with user id = " + userId);
            return userRepository.findOne(userId).getRole().toString();
        } catch (Exception e) {
            logger.info("Try to get user type with wrong user id = " + userId);
            throw new Exception("Information not available, user don't exist");
        }
    }

    public long getIdByLogin(String login) {
        User user = userRepository.findUserByLogin(login);
        return user.getId();
    }

}
