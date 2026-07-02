package ec.edu.ups.icc.fundamentos01.core.exceptions.domain;

import org.springframework.http.HttpStatus;

import ec.edu.ups.icc.fundamentos01.core.exceptions.base.AplicationException;

public class BadRequestException extends AplicationException {
    
    public BadRequestException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
    
}
