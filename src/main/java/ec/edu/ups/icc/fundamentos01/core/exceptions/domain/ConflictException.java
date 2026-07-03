package ec.edu.ups.icc.fundamentos01.core.exceptions.domain;
import ec.edu.ups.icc.fundamentos01.core.exceptions.base.AplicationException;
import org.springframework.http.HttpStatus;
public class ConflictException extends AplicationException {
    public ConflictException(String message) { super(HttpStatus.CONFLICT, message); }
}