package com.project.glib.service;

import com.project.glib.dao.implementations.CheckoutDaoImplementation;
import com.project.glib.model.*;
import javafx.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CheckoutService implements ModifyByLibrarianService<Checkout> {
    public static final long WEEK_IN_MILLISECONDS = 604800000L;
    public static final String TYPE = Checkout.TYPE;
    public static final String ADD_EXCEPTION = ModifyByLibrarianService.ADD_EXCEPTION + TYPE + SMTH_WRONG;
    public static final String EXIST_EXCEPTION = INFORMATION_NOT_AVAILABLE + TYPE + DOES_NOT_EXIST;
    public static final String ALREADY_HAS_THIS_CHECKOUT_EXCEPTION = "Sorry, but your already have this check out ";
    public static final String CHECKOUT_TIME_EXCEPTION = " checkout cannot be in future ";
    public static final String RETURN_TIME_EXCEPTION = " return time cannot be less than checkout time ";
    public static final String DOC_TYPE_EXCEPTION = " invalid document type ";

    private final BookService bookService;
    private final JournalService journalService;
    private final AudioVideoService avService;
    private final DocumentPhysicalService docPhysService;
    private final BookingService bookingService;
    private final UserService userService;
    private final MessageService messageService;
    private final CheckoutDaoImplementation checkoutDao;
    private final LoggerService loggerService;

    @Autowired
    public CheckoutService(BookService bookService,
                           JournalService journalService,
                           AudioVideoService avService,
                           DocumentPhysicalService docPhysService,
                           @Lazy BookingService bookingService,
                           @Lazy UserService userService,
                           MessageService messageService,
                           CheckoutDaoImplementation checkoutDao, LoggerService loggerService) {
        this.bookService = bookService;
        this.journalService = journalService;
        this.avService = avService;
        this.docPhysService = docPhysService;
        this.bookingService = bookingService;
        this.checkoutDao = checkoutDao;
        this.userService = userService;
        this.messageService = messageService;
        this.loggerService = loggerService;
    }

    /**
     * Check out document by user booking
     *
     * @param booking current booking
     */
    public void toCheckoutDocument(Booking booking) throws Exception {
        long additionalTime;
        String userType = userService.getTypeById(booking.getUserId());

        switch (booking.getDocType()) {
            case Document.BOOK:
                if (userType.equals(User.STUDENT)) {
                    long bookId = booking.getDocVirId();
                    boolean isBestseller = bookService.getNote(bookId).equals(Book.BESTSELLER);
                    additionalTime = isBestseller ? 2 * WEEK_IN_MILLISECONDS : 3 * WEEK_IN_MILLISECONDS;
                } else if (Arrays.asList(User.FACULTY).contains(userType)) {
                    additionalTime = 4 * WEEK_IN_MILLISECONDS;
                } else if (userType.equals(User.PROFESSOR_VISITING)) {
                    additionalTime = WEEK_IN_MILLISECONDS;
                } else {
                    throw new Exception(TYPE_EXCEPTION);
                }
                loggerService.addLog(booking.getUserId(), booking.getDocPhysId(), LoggerService.CHECKEDOUT_BOOK, System.currentTimeMillis(), Document.BOOK, true);
                break;
            case Document.JOURNAL:
            case Document.AV:
                if (booking.getDocType().equals(Document.AV)) {
                    loggerService.addLog(booking.getUserId(), booking.getDocPhysId(), LoggerService.CHECKEDOUT_AV, System.currentTimeMillis(), Document.AV, true);
                } else {
                    loggerService.addLog(booking.getUserId(), booking.getDocPhysId(), LoggerService.CHECKEDOUT_JOURNAL, System.currentTimeMillis(), Document.JOURNAL, true);
                }
                if (userType.equals(User.STUDENT) || Arrays.asList(User.FACULTY).contains(userType)) {
                    additionalTime = 2 * WEEK_IN_MILLISECONDS;
                } else if (userType.equals(User.PROFESSOR_VISITING)) {
                    additionalTime = WEEK_IN_MILLISECONDS;
                } else {
                    throw new Exception(TYPE_EXCEPTION);
                }
                break;
            default:
                throw new Exception(DOC_TYPE_EXCEPTION);
        }

        bookingService.removeBecauseCheckout(booking.getId());

        try {
            messageService.removeOneByUserID(booking.getUserId(), booking.getDocPhysId(), MessageService.CHECKOUT_DOCUMENT);
        } catch (Exception ignore) {
        }

        add(new Checkout(booking.getUserId(), booking.getDocPhysId(), System.currentTimeMillis(),
                System.currentTimeMillis() + additionalTime, booking.getShelf(), false));
    }

    /**
     * Adds new checkout to DB
     *
     * @param checkout Checkout model
     * @throws Exception
     */
    protected void add(Checkout checkout) throws Exception {
        checkValidParameters(checkout);
        if (alreadyHasThisCheckout(checkout.getDocPhysId(), checkout.getUserId()))
            throw new Exception(ALREADY_HAS_THIS_CHECKOUT_EXCEPTION);
        try {
            checkoutDao.add(checkout);
        } catch (Exception e) {
            throw new Exception(ADD_EXCEPTION);
        }
    }

    /**
     * Removes checkout from DB
     *
     * @param checkoutId ID of checkout in DB
     * @throws Exception
     */
    public void remove(long checkoutId) throws Exception {
        try {
            checkoutDao.remove(checkoutId);
        } catch (Exception e) {
            throw new Exception(REMOVE_EXCEPTION);
        }
    }

    /**
     * Checks valid parameters for checkout
     *
     * @param checkout Checkout model
     * @throws Exception
     */
    @Override
    public void checkValidParameters(Checkout checkout) throws Exception {
        if (checkout.getDocPhysId() <= 0) {
            throw new Exception(ID_EXCEPTION);
        }

        if (checkout.getUserId() <= 0) {
            throw new Exception(ID_EXCEPTION);
        }

        if (checkout.getCheckoutTime() > System.currentTimeMillis()) {
            throw new Exception(CHECKOUT_TIME_EXCEPTION);
        }

        if (checkout.getReturnTime() <= checkout.getCheckoutTime()) {
            throw new Exception(RETURN_TIME_EXCEPTION);
        }

        if (checkout.getShelf().equals("")) {
            throw new Exception(SHELF_EXCEPTION);
        }
    }

    public List<CheckoutList> getListOfCheckouts() throws Exception {
        ArrayList<CheckoutList> checkoutList = new ArrayList<>();
        List<Checkout> checkouts = getList();

        for (Checkout checkout : checkouts) {
            User user = userService.getById(checkout.getUserId());

            String type = docPhysService.getTypeById(checkout.getDocPhysId());
            long id = docPhysService.getDocVirIdById(checkout.getDocPhysId());

            String title = "";
            String author = "";

            switch (type) {
                case Document.BOOK:
                    Book book = bookService.getById(id);
                    title = book.getTitle();
                    author = book.getAuthor();
                    break;
                case Document.JOURNAL:
                    Journal journal = journalService.getById(id);
                    title = journal.getTitle();
                    author = journal.getAuthor();
                    break;
                case Document.AV:
                    AudioVideo audioVideo = avService.getById(id);
                    title = audioVideo.getTitle();
                    author = audioVideo.getAuthor();
                    break;
            }

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(checkout.getCheckoutTime());
            int cYear = calendar.get(Calendar.YEAR);
            int cMonth = calendar.get(Calendar.MONTH) % 12 + 1;
            int cDay = calendar.get(Calendar.DAY_OF_MONTH);

            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(checkout.getReturnTime());
            int rYear = cal.get(Calendar.YEAR);
            int rMonth = cal.get(Calendar.MONTH) % 12 + 1;
            int rDay = cal.get(Calendar.DAY_OF_MONTH);


            checkoutList.add(new CheckoutList(user.getName(), user.getSurname(),
                    user.getPhone(), title, author,
                    cDay + "." + cMonth + "." + cYear, rDay + "." + rMonth + "." + rYear));
        }
        return checkoutList;
    }

    /**
     * Gets checkout by checkout physical ID
     *
     * @param checkoutId checkout ID
     * @return
     */
    @Override
    public Checkout getById(long checkoutId) {
        return checkoutDao.getById(checkoutId);
    }

    /**
     * Gets ID of checkout instance. Also, checks existence in DB
     *
     * @param checkout Checkout model
     * @return ID of checkout instance
     * @throws Exception
     */
    @Override
    public long getId(Checkout checkout) throws Exception {
        try {
            return checkoutDao.getId(checkout);
        } catch (NullPointerException | NoSuchElementException e) {
            throw new Exception(EXIST_EXCEPTION);
        }
    }

    /**
     * Gets list of all checkouts in DB
     *
     * @return list of checkouts
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<Checkout> getList() {
        try {
            return checkoutDao.getList();
        } catch (NoSuchElementException e) {
            return new ArrayList<>();
        }
    }

    /**
     * Gets number of checkout documents by the user
     *
     * @param userId user ID
     * @return number of documents at user
     * @throws Exception
     */
    public int getNumberOfCheckoutDocumentsByUser(long userId) throws Exception {
        try {
            return getCheckoutsByUser(userId).size();
        } catch (NullPointerException | NoSuchElementException e) {
            throw new Exception(EXIST_EXCEPTION);
        }
    }

    /**
     * Gets list of all checkouts by user
     *
     * @param userId user ID
     * @return list of checkouts
     */
    public List<Checkout> getCheckoutsByUser(long userId) {
        try {
            return getList().stream()
                    .filter(checkout -> checkout.getUserId() == userId)
                    .collect(Collectors.toList());
        } catch (NullPointerException | NoSuchElementException e) {
            return new ArrayList<>();
        }
    }

    /**
     * Checks if this user already has this checkout
     *
     * @param docPhysId physical ID of document
     * @param userId    ID of user
     * @return
     */
    public boolean alreadyHasThisCheckout(long docPhysId, long userId) {
        DocumentPhysical docPhys = docPhysService.getById(docPhysId);

        List<DocumentPhysical> docPhysList = docPhysService.getList().stream()
                .filter(doc -> doc.getDocVirId() == docPhys.getDocVirId())
                .filter(doc -> doc.getDocType().equals(docPhys.getDocType()))
                .collect(Collectors.toList());

        try {
            List<Checkout> checkoutList = getList().stream()
                    .filter(checkout -> checkout.getUserId() == userId)
                    .collect(Collectors.toList());

            for (Checkout checkout : checkoutList) {
                for (DocumentPhysical currentDocPhys : docPhysList) {
                    if (checkout.getDocPhysId() == currentDocPhys.getId()) return true;
                }
            }
        } catch (NullPointerException e) {
            return false;
        }


        return false;
    }


    /**
     * Deletes all checkouts
     *
     * @throws Exception
     */
    public void deleteAllCheckouts() throws Exception {
        List<Checkout> checkouts = getList();
        for (Checkout checkout : checkouts) {
            remove(checkout.getId());
        }
    }

    public void update(Checkout checkout) {
        checkoutDao.update(checkout);
    }

    public long getUserIdByDocPhysId(long docPhysId) throws Exception {
        try {
            return getByDocPhysId(docPhysId).getUserId();
        } catch (NullPointerException e) {
            throw new Exception(EXIST_EXCEPTION);
        }
    }

    /**
     * Gets checkout by physical ID
     *
     * @param docPhysId physical ID
     * @return checkout
     * @throws Exception
     */
    public Checkout getByDocPhysId(long docPhysId) throws Exception {
        try {
            return getList().stream()
                    .filter(checkout -> checkout.getDocPhysId() == docPhysId)
                    .findFirst().get();
        } catch (NoSuchElementException | NullPointerException e) {
            throw new Exception(EXIST_EXCEPTION);
        }
    }

    public List<Checkout> getByDocVirIdAndDocType(long docVirId, String docType) {
        List<DocumentPhysical> docPhysList = docPhysService.getByDocVirIdAndDocType(docVirId, docType);

        List<Checkout> checkoutList = new ArrayList<>();
        for (DocumentPhysical docPhys : docPhysList) {
            try {
                Checkout checkout = getByDocPhysId(docPhys.getId());
                checkoutList.add(checkout);
            } catch (Exception ignore) {
            }
        }
        return checkoutList;
    }

    public List<Checkout> getCheckoutsByUserId(long userId) {
        try {
            return getList().stream()
                    .filter(checkout -> checkout.getUserId() == userId)
                    .collect(Collectors.toList());
        } catch (NullPointerException | NoSuchElementException e) {
            return new ArrayList<>();
        }
    }

    /**
     * Renew document
     *
     * @param checkout checkout model
     * @throws Exception
     */
    public void renew(Checkout checkout) throws Exception {
        String userType = userService.getTypeById(checkout.getUserId());
        boolean canRenewedAgain = userType.equals(User.PROFESSOR_VISITING);
        boolean hasOutstandingRequest = false;

        for (Booking booking : bookingService.getList()) {
            if (booking.getPriority() == BookingService.PRIORITY.get(BookingService.OUTSTANDING)) {
                hasOutstandingRequest = true;
            }
        }

        if (checkout.isRenewed() && !canRenewedAgain || hasOutstandingRequest) {
            throw new Exception("Sorry, you can't renew this checkout");
        }

        checkout.setReturnTime(System.currentTimeMillis() + checkout.getReturnTime() - checkout.getCheckoutTime());
        checkout.setRenewed(true);
        update(checkout);
    }

    public List<Pair<User, Checkout>> getAllUsersAndTheisCheckouts() {
        List<Pair<User, Checkout>> userCheckout = new ArrayList<>();

        getList().forEach(checkout -> userCheckout.add(new Pair<>(userService.getById(checkout.getUserId()), checkout)));

        return userCheckout;
    }
}
