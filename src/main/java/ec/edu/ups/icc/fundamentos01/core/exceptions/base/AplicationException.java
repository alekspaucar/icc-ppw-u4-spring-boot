package ec.edu.ups.icc.fundamentos01.core.exceptions.base;
import org.springframework.http.HttpStatus;

public class AplicationException extends RuntimeException {
    
    private final HttpStatus status;

    protected AplicationException(HttpStatus status,String message) {
        super(message);
        this.status = status;
    }
    public HttpStatus getStatus() {
        return status;
    };

}
