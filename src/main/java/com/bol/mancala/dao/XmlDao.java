
package com.bol.mancala.dao;

import com.bol.mancala.exception.XmlDaoException;
import java.util.List;
import java.util.Map;
import org.w3c.dom.Element;

/**
 *
 * @author sujith
 */
public interface XmlDao {
    
    public void commit() throws XmlDaoException;
    
    public void commit(Element element, String mode) throws XmlDaoException;
    
    public Element createElement(String child);
    
    public <T> List<T> fetchAll(Class<T> clazz, Map<String,String> map) throws XmlDaoException;
}
