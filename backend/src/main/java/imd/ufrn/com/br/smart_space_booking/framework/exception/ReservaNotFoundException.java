package imd.ufrn.com.br.smart_space_booking.framework.exception;

public class ReservaNotFoundException extends RuntimeException {
    public ReservaNotFoundException(String message) {
        super(message);
    }
}