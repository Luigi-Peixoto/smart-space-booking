package imd.ufrn.com.br.smart_space_booking.config;

import java.time.ZonedDateTime;
import java.util.List;

import imd.ufrn.com.br.smart_space_booking.model.*;
import imd.ufrn.com.br.smart_space_booking.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import imd.ufrn.com.br.smart_space_booking.enums.UsuarioStatus;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final SalaRepository salaRepository;
    private final ReservaRepository reservaRepository;
    private final RegraAvaliacaoRepository regraRepository;
    private final TrustScoreHistoricoRepository trustScoreHistoricoRepository;

    public DataSeeder(UsuarioRepository usuarioRepository, SalaRepository salaRepository,
                      ReservaRepository reservaRepository, RegraAvaliacaoRepository regraRepository,
                      TrustScoreHistoricoRepository trustScoreHistoricoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.salaRepository = salaRepository;
        this.reservaRepository = reservaRepository;
        this.regraRepository = regraRepository;
        this.trustScoreHistoricoRepository = trustScoreHistoricoRepository;
    }

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() > 0) {
            System.out.println("Banco de dados já populado. Ignorando o Seeder.");
            return;
        }

        System.out.println("Iniciando o Database Seeder...");

        // Populando Regras de Avaliação (Motor de Regras)
        RegraAvaliacao regra1 = new RegraAvaliacao();
        regra1.setNome("Cancelamento Tardio");
        regra1.setDescricao("Penalidade por cancelar com menos de 2 horas de antecedência.");
        regra1.setLimiBonus(10);
        regra1.setDeltaBonus(0);
        regra1.setLimiPenalidade(0);
        regra1.setDeltaPenalidade(-15);

        RegraAvaliacao regra2 = new RegraAvaliacao();
        regra2.setNome("Excesso de Cancelamentos");
        regra2.setDescricao("Penalidade para quem cancela mais de 3 reservas na mesma semana.");
        regra2.setLimiBonus(10);
        regra2.setDeltaBonus(0);
        regra2.setLimiPenalidade(3);
        regra2.setDeltaPenalidade(-20);

        regraRepository.saveAll(List.of(regra1, regra2));

        // Populando Usuários
        Usuario admin = new Usuario();
        admin.setNome("Admin");
        admin.setEmail("admin@admin.com");
        admin.setPerfil("ADMIN");
        admin.setTrustScore(100);
        admin.setStatus(UsuarioStatus.ATIVO);

        Usuario user1 = new Usuario();
        user1.setNome("João Silva");
        user1.setEmail("joao@ufrn.edu.br");
        user1.setPerfil("USER");
        user1.setTrustScore(100);
        user1.setStatus(UsuarioStatus.ATIVO);

        usuarioRepository.saveAll(List.of(admin, user1));

        // Populando Salas
//        Sala salaReuniao = new Sala("Sala de Reunião Alpha", "Bloco A, 1º Andar",
//                StatusSala.ATIVA, TipoSala.REUNIAO, 10,
//                List.of("Projetor", "Ar Condicionado", "Quadro Branco"),
//                List.of("url_imagem_alpha.jpg"));
//
//        Sala lab = new Sala("Laboratório de Informática", "Bloco B, Térreo",
//                StatusSala.ATIVA, TipoSala.LABORATORIO, 30,
//                List.of("30 Computadores", "Ar Condicionado", "Lousa Digital"),
//                List.of("url_imagem_lab.jpg"));
//
//        salaRepository.saveAll(List.of(salaReuniao, lab));

        ZonedDateTime amanha = ZonedDateTime.now().plusDays(1);
        
//        Reserva reservaNormal = new Reserva();
//        reservaNormal.setSala(salaReuniao);
//        reservaNormal.setUsuario(user1);
//        reservaNormal.setInicioDateTime(amanha.withHour(14).withMinute(0));
//        reservaNormal.setFimDateTime(amanha.withHour(16).withMinute(0));
//        reservaNormal.setTipo(ReservaTipo.PADRAO);
//        reservaNormal.setStatus(ReservaStatus.CONFIRMADA);

//        reservaRepository.save(reservaNormal);

//        TrustScoreHistorico historico = new TrustScoreHistorico();
//        historico.setUsuario(user1);
//        historico.setReserva(null);
//        historico.setRegra(null);
//        historico.setDelta(0);
//        historico.setScoreAnterior(100);
//        historico.setScorePosterior(100);
//        historico.setDescricao("Score inicial do usuário.");
//        trustScoreHistoricoRepository.save(historico);

        System.out.println("Database Seeder concluído com sucesso! Temos 2 usuários, 2 salas e 1 reserva inicial.");
    }
}