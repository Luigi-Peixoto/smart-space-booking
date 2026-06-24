package imd.ufrn.com.br.smart_space_booking.exception;

public class SalaNotFoundException extends RuntimeException {
    public SalaNotFoundException(Long id) {
        super("Sala não encontrada com id: " + id);
    }

    public SalaNotFoundException(String message) {
        super(message);
    }
}