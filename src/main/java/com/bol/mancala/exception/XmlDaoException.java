
package com.bol.mancala.exception;

/**
 *
 * @author sujith
 */
public class XmlDaoException extends Exception {
    
    private static final long serialVersionUID = 1L;

    public XmlDaoException(String message, Exception e) {
        super(message,e);
    }
}
