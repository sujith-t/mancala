
package com.bol.mancala.test;

import com.bol.mancala.dao.XmlDao;
import com.bol.mancala.dao.XmlDaoImpl;
import com.bol.mancala.exception.XmlDaoException;
import com.bol.mancala.model.PlayerModel;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.w3c.dom.Element;

/**
 *
 * @author sujith
 */
public class XmlDaoImplTest {
    
    private XmlDao dao;
    private String gameFile = "/tmp/mancala-gaming-test.xml";
    
    @Before
    public void init() throws XmlDaoException, IOException {       
        this.dao = new XmlDaoImpl(this.gameFile);
    }
    
    @After
    public void tearDown() {
        this.dao = null;
        File file = new File(this.gameFile);
        file.delete();        
    }
 
    /**
     * Test Instance
     */    
    @Test
    public void testInstance() {
        assertTrue(this.dao instanceof XmlDao);
        File file = new File(this.gameFile);
        assertTrue(file.exists());
    } 
  
    /**
     * Test Fetch by node name
     * @throws XmlDaoException
     */    
    @Test
    public void testFetchAllByTagName() throws XmlDaoException {
        List<PlayerModel> createdModelList = new ArrayList<>();
        
        for(int x = 0; x < 5; x++) {
            Element element = this.dao.createElement("player");
            Random rand = new Random();
            String attrValue = "value" + rand.nextInt(10000); 
            element.setAttribute("nickname", attrValue);
            element.setAttribute("lastplayed", "some date value");    
            this.dao.commit(element, "SAVE");
            createdModelList.add(new PlayerModel(element));
        }
        
        Map<String, String> filter = new HashMap<>();
        filter.put("node", "player");
        List<PlayerModel> fetchedList = this.dao.fetchAll(PlayerModel.class, filter);
        
        for(PlayerModel player: createdModelList) {
            assertTrue(fetchedList.contains(player));
        }
    }
    
    /**
     * Test Instance
     */    
    @Test
    public void testCreateElement() {
        Random rand = new Random();
        String elementName = "MyElement" + rand.nextInt(10000);
        Element element = this.dao.createElement(elementName);
        
        assertTrue(element instanceof Element);
        assertFalse(element == null);
        assertEquals(element.getNodeName(), elementName);
    }
    
    /**
     * Test Saving + Retrieving by attribute value
     * @throws XmlDaoException
     */    
    @Test
    public void testCommitForSave() throws XmlDaoException {
        Element element = this.dao.createElement("player");
        Random rand = new Random();
        String attrValue = "value" + rand.nextInt(10000); 
        element.setAttribute("nickname", attrValue);
        element.setAttribute("lastplayed", "some date value");
        
        PlayerModel valueToCompare = new PlayerModel(element);
        Map<String, String> filter = new HashMap<>();
        filter.put("node", "player");
        filter.put("attr", "nickname");
        filter.put("attrValue", attrValue);
        
        List<PlayerModel> list = this.dao.fetchAll(PlayerModel.class, filter);
        assertEquals(list.size(), 0);   
        
        this.dao.commit(element, "SAVE");
        list = this.dao.fetchAll(PlayerModel.class, filter);
        assertEquals(list.size(), 1);
        assertEquals(valueToCompare, list.get(0));
    } 
    
    /**
     * Test Delete
     * @throws XmlDaoException
     */    
    @Test
    public void testCommitForDelete() throws XmlDaoException {
        Element element = this.dao.createElement("player");
        Random rand = new Random();
        String attrValue = "value" + rand.nextInt(10000); 
        element.setAttribute("nickname", attrValue);
        element.setAttribute("lastplayed", "some date value");    
        this.dao.commit(element, "SAVE");
        
        Map<String, String> filter = new HashMap<>();
        filter.put("node", "player");
        filter.put("attr", "nickname");
        filter.put("attrValue", attrValue);
        
        List<PlayerModel> list = this.dao.fetchAll(PlayerModel.class, filter);
        assertEquals(list.size(), 1);
        
        //now we delete the same element and check
        this.dao.commit(element, "DELETE");
        list = this.dao.fetchAll(PlayerModel.class, filter);
        assertNotEquals(list.size(), 1);
    }   
}