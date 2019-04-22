package com.bol.mancala.dao;

import com.bol.mancala.exception.XmlDaoException;
import com.bol.mancala.model.Model;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.ejb.Local;
import javax.ejb.Lock;
import javax.ejb.LockType;
import static javax.ejb.LockType.READ;
import javax.ejb.Singleton;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author sujith
 */
@Local
@Singleton
public class XmlDaoImpl implements XmlDao {

    private static Document dom;
    private static final String GAMING_FILE = "/tmp/mancala-gaming.xml";

    /**
     * @throws XmlDaoException
     * @throws IOException
     */
    public XmlDaoImpl() throws XmlDaoException, IOException {
        
        //first check whether config file is present if not we create
        File file = new File(GAMING_FILE);

        if (!file.exists() || !file.isFile()) {
            try (PrintWriter writer = new PrintWriter(GAMING_FILE, "UTF-8")) {
                writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
                writer.println("<game><players></players><sessions></sessions></game>");
                writer.close();
            } catch (IOException e) {
                throw new XmlDaoException("Error in creating gaming file", e);
            }
        }

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;

        if (dom == null) {
            try {
                dBuilder = dbFactory.newDocumentBuilder();
                dom = dBuilder.parse(file);
            } catch (ParserConfigurationException | SAXException | IOException e) {
                throw new XmlDaoException("Error in parsing XML file I/O", e);
            }

            dom.getDocumentElement().normalize();
        }
    }

    /**
     * Saves the modification to XML file
     *
     * @throws XmlDaoException
     */
    @Lock(LockType.WRITE)
    @Override
    public void commit() throws XmlDaoException {
        try {
            dom.getDocumentElement().normalize();
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            DOMSource source = new DOMSource(dom);
            StreamResult result = new StreamResult(new File(GAMING_FILE));
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(source, result);
        } catch (TransformerException ex) {
            throw new XmlDaoException("Commiting failed in gaming file", ex);
        }
    }

    /**
     * Saves the modification to XML file
     *
     * @param element
     * @param mode
     * @throws XmlDaoException
     */
    @Lock(LockType.WRITE)
    @Override
    public void commit(Element element, String mode) throws XmlDaoException {

        String parentElementName = element.getNodeName() + "s";
        Element nParent = (Element) dom.getElementsByTagName(parentElementName).item(0);

        if (mode.equals("SAVE")) {
            nParent.appendChild(element);
        } else if (mode.equals("DELETE")) {
            nParent.removeChild(element);
        }
        this.commit();
    }

    /**
     * Creates Element
     * 
     * @param child
     * 
     * @return Element
     */    
    @Override
    public Element createElement(String child) {
        return dom.createElement(child);
    }

    /**
     * Returns a list of Objects By searching the XML File
     * 
     * @param <T>
     * @param clazz
     * @param map
     * @return List
     * @throws com.bol.mancala.exception.XmlDaoException
     */
    @Lock(READ)    
    @Override
    public <T> List<T> fetchAll(Class<T> clazz, Map<String, String> map) throws XmlDaoException {
        List<T> list = new ArrayList<>();
        NodeList nlist = null;
        
        if(map.containsKey("node")) {
            nlist = dom.getElementsByTagName(map.get("node"));
        }
        
        if(nlist != null && nlist.getLength() > 0) {
            for(int x = 0; x < nlist.getLength(); x++) {
                Element element = (Element)nlist.item(x);
                
                try {
                    //no search by attr only by tagname
                    if(!map.containsKey("attr")) {
                        Model obj = (Model)clazz.newInstance();
                        obj.toModel(element);
                        T t = clazz.cast(obj);
                        list.add(t);
                    }                    
                    
                    //checking for element + attr values
                    if(map.containsKey("attr") && map.containsKey("attrValue") && element.hasAttribute(map.get("attr"))) {
                        String attr = map.get("attr");
                        String value = map.get("attrValue");

                        if(element.getAttribute(attr).equals(value)) {
                            Model obj = (Model)clazz.newInstance();
                            obj.toModel(element);
                            T t = clazz.cast(obj);
                            list.add(t);
                        }
                    }
                } catch (Exception ex) {
                    throw new XmlDaoException("Creating instance for Fetchall failed", ex);
                }
            }
        }
        
        return list;
    }
}