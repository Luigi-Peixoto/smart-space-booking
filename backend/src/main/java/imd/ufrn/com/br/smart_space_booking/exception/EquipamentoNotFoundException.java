package imd.ufrn.com.br.smart_space_booking.exception;

public class EquipamentoNotFoundException extends RuntimeException {
    public EquipamentoNotFoundException(Long id) {
        super("Equipamento com ID " + id + " não encontrado!");
    }
}