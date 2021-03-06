package com.project.glib.service;

import com.project.glib.dao.implementations.MessageDaoImplementation;
import com.project.glib.dao.interfaces.MessagesRepository;
import com.project.glib.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MessageService {
    public static final String RETURN_DOCUMENT = "Please, return next document(s) to the library: ";
    public static final String CHECKOUT_DOCUMENT = "Please, visit a library and checkout a document:  ";
    public static final String DELETED_QUEUE = "Sorry, but you were deleted from the queue for the next document: ";
    public static final String LATE_DELETED = "Sorry, but you are late to checkout document: ";
    private static final org.slf4j.Logger logger = (Logger) LoggerFactory.getLogger(MessageService.class);
    private final MessageDaoImplementation messageDao;
    private final MessagesRepository messagesRepository;
    private final DocumentPhysicalService documentPhysicalService;
    private final BookService bookService;
    private final JournalService journalService;
    private final AudioVideoService audioVideoService;
    private final UserService userService;

    @Autowired
    MessageService(MessageDaoImplementation messageDao,
                   MessagesRepository messagesRepository,
                   DocumentPhysicalService documentPhysicalService,
                   BookService bookService,
                   JournalService journalService,
                   AudioVideoService audioVideoService,
                   @Lazy UserService userService) {
        this.messageDao = messageDao;
        this.messagesRepository = messagesRepository;
        this.documentPhysicalService = documentPhysicalService;
        this.bookService = bookService;
        this.journalService = journalService;
        this.audioVideoService = audioVideoService;
        this.userService = userService;
    }

    @Scheduled(fixedDelay = BookingService.DAY_IN_MILLISECONDS)
    public void deleteReadMes() throws Exception {
        List<Messages> list = messageDao.getList();
        for (Messages mes : list) {
            if (!(mes.getMessage().equals(RETURN_DOCUMENT) ||
                    mes.getMessage().equals(CHECKOUT_DOCUMENT))) {
                if (mes.getIsRead()) {
                    try {
                        messageDao.remove(mes.getId());
                        logger.info("Message successfully saved. Message details : " + mes);
                    } catch (Exception e) {
                        logger.info("Try to add message with wrong parameters. New message information : " + mes);
                        throw new Exception("Can't add this message, some parameters are wrong");
                    }
                }
            }
        }
    }

    /**
     * Add message to user
     *
     * @param id_user user ID
     * @param id_doc  document ID
     * @param type    type of document
     * @param message message to user
     * @throws Exception
     */
    public void addMes(long id_user, long id_doc, String type, String message) throws Exception {
        Messages messages = new Messages(id_user, message, id_doc, type, false);
        try {
            if (!alreadyHasThisMessage(id_user, id_doc, message)) {
                try {
                    messageDao.add(messages);
                    logger.info("Message successfully saved. Message details : " + message);
                } catch (Exception e) {
                    logger.info("Try to add message with wrong parameters. New message information : " + message);
                    throw new Exception("Can't add this message, some parameters are wrong");
                }
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    public boolean alreadyHasThisMessage(long id_user, long id_doc, String message) {
        try {
            Messages mes = messagesRepository.findAll().stream()
                    .filter(messages -> messages.getUserId() == id_doc)
                    .filter(messages -> messages.getDocPhysId() == id_user)
                    .findAny().get();
            return mes.getMessage().equals(message);
        } catch (Exception e) {
            return false;
        }
    }

    public List<Messages> getAllByUserID(long userId) {
        return messagesRepository.findAll().stream()
                .filter(messages -> messages.getUserId() == userId)
                .collect(Collectors.toList());
    }


    public void removeAllByUserID(long userId) throws Exception {
        try {
            List<Messages> list = messagesRepository.findAll().stream()
                    .filter(messages -> messages.getUserId() == userId)
                    .collect(Collectors.toList());
            for (Messages aList : list) {
                try {
                    messageDao.remove(aList.getId());
                    logger.info("Message successfully saved. Message details : " + aList);
                } catch (Exception e) {
                    logger.info("Try to add message with wrong parameters. New message information : " + aList);
                    throw new Exception("Can't add this message, some parameters are wrong");
                }
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    public List<String> getMessages(String login) throws Exception {
        List<Messages> mes = getAllByUserID(userService.getIdByLogin(login));
        ArrayList<String> result = new ArrayList<>();

        for (Messages me : mes) {
            result.add(me.getMessage() + createMessage(me.getDocPhysId()));
        }

        if (result.size() == 0) {
            return new ArrayList<>();
        } else {
            return result;
        }

    }

    public void sendMessagesToLib(String login) throws Exception {
        List<Messages> messages = getAllByUserID(userService.getIdByLogin(login));

        List<User> librarians = userService.getList().stream()
                .filter(User -> User.getRole().equals(com.project.glib.model.User.LIBRARIANS))
                .collect(Collectors.toList());

        for (Messages message1 : messages) {
            String message = "User " + login +
                    " read the message: " + message1.getMessage() +
                    createMessage(message1.getDocPhysId());
            for (int i = 0; i < librarians.size(); i++) {
                addMes(librarians.get(i).getId(), -1, "NULL", message);
            }
        }
    }


    public void removeOneByUserID(long userId, long doc_id, String mes) throws Exception {
        List<Messages> list = messagesRepository.findAll().stream()
                .filter(messages -> messages.getUserId() == userId)
                .filter(messages -> messages.getDocPhysId() == doc_id)
                .filter(messages -> messages.getMessage().equals(mes))
                .collect(Collectors.toList());
        if (list.size() != 0) {
            if (list.get(0) != null) {
                try {
                    for (Messages aList : list) {
                        try {
                            messageDao.remove(aList.getId());
                            logger.info("Message successfully saved. Message details : " + aList);
                        } catch (Exception e) {
                            logger.info("Try to add message with wrong parameters. New message information : " + aList);
                            throw new Exception("Can't add this message, some parameters are wrong");
                        }
                    }

                } catch (Exception e) {
                    throw new Exception(e.getMessage());
                }
            } else {
                throw new Exception("There is no messages for user " + userId + " about document " + doc_id);
            }
        } else {
            throw new Exception("There is no messages for user " + userId + " about document " + doc_id);
        }
    }

    private String createMessage(long idPhys) {
        DocumentPhysical documentPhysical = documentPhysicalService.getById(idPhys);
        String result = "";
        if (idPhys > 0) {
            long id = documentPhysical.getDocVirId();
            String type = documentPhysical.getDocType();

            switch (type) {
                case Document.BOOK:
                    Book book = bookService.getById(id);
                    result = book.getTitle() + book.getAuthor();
                    break;
                case Document.JOURNAL:
                    Journal journal = journalService.getById(id);
                    result = journal.getTitle() + journal.getAuthor();
                    break;
                case Document.AV:
                    AudioVideo av = audioVideoService.getById(id);
                    result = av.getTitle() + av.getAuthor();
                    break;
            }
        }

        return result;
    }
}
