package ec.edu.ups.icc.fundamentos01.core.exceptions.domain;

import org.springframework.http.HttpStatus;

import ec.edu.ups.icc.fundamentos01.core.exceptions.base.AplicationException;

public class ConflictException extends AplicationException {
    
    public ConflictException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
