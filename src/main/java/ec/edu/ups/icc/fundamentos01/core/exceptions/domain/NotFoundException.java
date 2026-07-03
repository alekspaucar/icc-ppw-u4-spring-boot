package ec.edu.ups.icc.fundamentos01.core.exceptions.domain;
import ec.edu.ups.icc.fundamentos01.core.exceptions.base.AplicationException;
import org.springframework.http.HttpStatus;
public class NotFoundException extends AplicationException {
    public NotFoundException(String message) { super(HttpStatus.NOT_FOUND, message); }
}