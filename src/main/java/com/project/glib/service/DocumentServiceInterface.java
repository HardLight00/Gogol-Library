package com.project.glib.service;

import com.project.glib.model.Document;
import javafx.util.Pair;

import java.util.List;

public interface DocumentServiceInterface<T extends Document> extends ModifyByLibrarianService<T> {
    String PRICE_EXCEPTION = " price must be positive ";
    String ISSUE_EXCEPTION = " issue must be positive ";
    String COUNT_EXCEPTION = " count must be not negative ";
    String TITLE_EXCEPTION = " title must exist ";
    String AUTHOR_EXCEPTION = " author must exist ";
    String EDITOR_EXCEPTION = " editor must exist ";
    String EDITION_EXCEPTION = " edition must be positive ";
    String PUBLISHER_EXCEPTION = " publisher must exist ";
    String NAME_EXCEPTION = " name must exist ";
    String NOTE_EXCEPTION = " invalid note ";
    String YEAR_EXCEPTION = " year must be less or equal than current ";

    void update(T t) throws Exception;

    void remove(long id) throws Exception;

    void removeCopy(long docId, long copyId) throws Exception;

    void removeCopyByShelf(long docId, String shelf) throws Exception;

    void removeAllCopiesByShelf(long docId, String shelf) throws Exception;

    void add(T t, String shelf) throws Exception;

    boolean isNote(String note);

    void incrementCountById(long docId) throws Exception;

    void decrementCountById(long docId) throws Exception;

    int getCountById(long docId) throws Exception;

    int getPriceById(long docId) throws Exception;

    T isAlreadyExist(T t);

    List<Pair<String, Integer>> getListOfShelvesAndCounts(long bookId);
}
