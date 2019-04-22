
package com.bol.mancala.exception;

/**
 *
 * @author sujith
 */
public class ServiceException extends Exception {

    private static final long serialVersionUID = 1L;

    public ServiceException(String message, Exception e) {
        super(message,e);
    }    
}
