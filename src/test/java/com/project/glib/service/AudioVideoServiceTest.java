package com.project.glib.service;

import com.project.glib.model.AudioVideo;
import com.project.glib.model.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static com.project.glib.service.AudioVideoService.*;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AudioVideoServiceTest {
    @Autowired
    private AudioVideoService avService;
    @Autowired
    private DocumentPhysicalService docPhysService;
    private AudioVideo av, avInDao;
    private String shelf1, shelf2;
    private int count1, count2;

    @Before
    public void setup() {
        av = new AudioVideo("title", "author", 400, 2, "picture", null);
        shelf1 = "Shelf1";
        shelf2 = "Shelf2";
        count1 = 2;
        count2 = 4;
    }

    @After
    public void tearDown() {
        try {
            avService.remove(av.getId());
        } catch (Exception ignore) {
        }
    }

    @Test
    public void addNotExistedAV() throws Exception {
        avService.add(av, shelf1);
        avInDao = avService.isAlreadyExist(av);

        assertEquals(av, avInDao);
        assertEquals(docPhysService.getCount(avInDao.getId(), Document.AV), avInDao.getCount());
    }

    @Test
    public void addExistedAV() throws Exception {
        av.setCount(count1);
        avService.add(av, shelf1);
        av.setCount(count2);
        avService.add(av, shelf2);
        avInDao = avService.isAlreadyExist(av);

        assertEquals(count1 + count2, avInDao.getCount());
        assertEquals(docPhysService.getCount(avInDao.getId(), Document.AV), avInDao.getCount());
    }

    @Test
    public void addShelfException() {
        try {
            avService.add(av, "");
        } catch (Exception e) {
            assertEquals(SHELF_EXCEPTION, e.getMessage());
        }
    }

    @Test
    public void update() throws Exception {
        // don't change count when using update
        avService.add(av, shelf1);

        av.setTitle("title2");
        av.setAuthor("author2");
        av.setPrice(200);

        avService.update(av);
        avInDao = avService.isAlreadyExist(av);

        assertEquals(av, avInDao);
        assertEquals(docPhysService.getCount(avInDao.getId(), Document.AV), avInDao.getCount());
    }

    @Test
    public void remove() throws Exception {
        avService.add(av, shelf1);
        avService.remove(av.getId());

        avInDao = avService.isAlreadyExist(av);
        assertNull(avInDao);
        assertEquals(0, docPhysService.getCount(av.getId(), Document.AV));
    }

    @Test
    public void checkValidParametersReturnException1() {
        try {
            av.setTitle("");
            avService.checkValidParameters(av);
        } catch (Exception e) {
            assertEquals(TITLE_EXCEPTION, e.getMessage());
        }
    }

    @Test
    public void checkValidParametersReturnException2() {
        try {
            av.setAuthor("");
            avService.checkValidParameters(av);
        } catch (Exception e) {
            assertEquals(AUTHOR_EXCEPTION, e.getMessage());
        }
    }

    @Test
    public void checkValidParametersReturnException3() {
        try {
            av.setPrice(-1);
            avService.checkValidParameters(av);
        } catch (Exception e) {
            assertEquals(PRICE_EXCEPTION, e.getMessage());
        }
    }

    @Test
    public void checkValidParametersReturnException4() {
        try {
            av.setCount(-10);
            avService.checkValidParameters(av);
        } catch (Exception e) {
            assertEquals(COUNT_EXCEPTION, e.getMessage());
        }
    }

    @Test
    public void isAlreadyExistReturnNull() {
        avInDao = avService.isAlreadyExist(av);

        assertNull(avInDao);
    }

    @Test
    public void isAlreadyExistReturnAV() throws Exception {
        avService.add(av, shelf1);
        avInDao = avService.isAlreadyExist(av);

        assertNotNull(avInDao);
        assertEquals(av, avInDao);
    }

    @Test
    public void getIdReturnException() {
        try {
            avService.getId(av);
        } catch (Exception e) {
            assertEquals(EXIST_EXCEPTION, e.getMessage());
        }
    }

    @Test
    public void getIdReturnId() throws Exception {
        avService.add(av, shelf1);
        assertEquals(av.getId(), avService.getId(av));
    }
}