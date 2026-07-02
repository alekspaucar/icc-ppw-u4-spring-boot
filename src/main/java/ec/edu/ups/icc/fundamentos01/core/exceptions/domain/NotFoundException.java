package ec.edu.ups.icc.fundamentos01.core.exceptions.domain;

import org.springframework.http.HttpStatus;

import ec.edu.ups.icc.fundamentos01.core.exceptions.base.AplicationException;


public class NotFoundException extends AplicationException {

    public NotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }

}
