package com.project.glib.dao.implementations;

import com.project.glib.dao.interfaces.BookingRepository;
import com.project.glib.dao.interfaces.ModifyByLibrarian;
import com.project.glib.model.Booking;
import com.project.glib.service.BookingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class BookingDaoImplementation implements ModifyByLibrarian<Booking> {
    private static final Logger logger = (Logger) LoggerFactory.getLogger(BookDaoImplementation.class);
    private static final String TYPE = Booking.TYPE;
    private static final String ADD_BOOKING = TYPE + ADD;
    private static final String UPDATE_BOOKING = TYPE + UPDATE;
    private static final String REMOVE_BOOKING = TYPE + REMOVE;
    private static final String LIST = TYPE + ModifyByLibrarian.LIST;
    private final BookingRepository bookingRepository;

    @Autowired
    public BookingDaoImplementation(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    /**
     * Adds new Booking instance to the database
     *
     * @param booking booking instance
     */
    @Override
    public void add(Booking booking) {
        bookingRepository.saveAndFlush(booking);
        logger.info(ADD_BOOKING + booking);
    }

    /**
     * Updates booking record in the database
     *
     * @param booking booking instance
     */
    @Override
    public void update(Booking booking) {
        bookingRepository.saveAndFlush(booking);
        logger.info(UPDATE_BOOKING + booking);
    }

    /**
     * Deletes the booking from the database by its ID
     *
     * @param bookingId ID of booking to delete
     */
    @Override
    public void remove(long bookingId) {
        bookingRepository.delete(bookingId);
        logger.info(REMOVE_BOOKING + bookingId);
    }

    /**
     * Gets the Booking record by its ID
     *
     * @param bookingId ID of booking to get
     * @return
     */
    @Override
    public Booking getById(long bookingId) {
        return bookingRepository.findOne(bookingId);
    }

    /**
     * Gets ID of booking
     *
     * @param booking Booking instance
     * @return
     */
    @Override
    public long getId(Booking booking) {
        return bookingRepository.findAll().stream()
                .filter(b -> b.getPriority() == booking.getPriority() &&
                        b.getDocVirId() == booking.getDocVirId() &&
                        b.getDocPhysId() == booking.getDocPhysId() &&
                        b.getUserId() == booking.getUserId() &&
                        b.getBookingDate() == booking.getBookingDate() &&
                        b.isActive() == booking.isActive() &&
                        b.getDocType().equals(booking.getDocType()) &&
                        b.getShelf().equals(booking.getShelf()))
                .findFirst().get().getId();
    }

    /**
     * Collect all Bookings to the list
     *
     * @return list of all bookings
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<Booking> getList() {
        List<Booking> bookings = bookingRepository.findAll();

        for (Booking booking : bookings) {
            logger.info(LIST + booking);
        }

        return bookings;
    }

    /**
     * Retrieve the booking on certain document with max priority
     *
     * @param docVirId ID of virtual document
     * @param docType
     * @return
     */
    public Booking getBookingWithMaxPriority(long docVirId, String docType) {
        List<Booking> bookings = bookingRepository.findAll().stream()
                .filter(booking -> booking.getPriority() != BookingService.PRIORITY.get(BookingService.ACTIVE))
                .sorted(Booking::compareTo).collect(Collectors.toList());
        return bookings.get(bookings.size() - 1);
    }
}