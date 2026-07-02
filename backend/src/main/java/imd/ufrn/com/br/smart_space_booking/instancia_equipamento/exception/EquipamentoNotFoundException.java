package imd.ufrn.com.br.smart_space_booking.instancia_equipamento.exception;

public class EquipamentoNotFoundException extends RuntimeException {
    public EquipamentoNotFoundException(Long id) {
        super("Equipamento com ID " + id + " não encontrado!");
    }
}