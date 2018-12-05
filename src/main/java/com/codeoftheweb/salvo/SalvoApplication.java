package com.codeoftheweb.salvo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SpringBootApplication
public class SalvoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SalvoApplication.class, args);

    }

    @Bean
    public CommandLineRunner initData(PlayerRepository repository, GameRepository repository2, GamePlayerRepository repository3, ShipRepository respository4) {
        return args -> {
            //Create Players
            Player p1 = new Player("j.bauer@ctu.gov");
            Player p2 = new Player("c.obrian@ctu.gov");
            Player p3 = new Player("kim_bauer@gmail.com");
            Player p4 = new Player("t.almeida@ctu.gov");
            repository.save(p1);
            repository.save(p2);
            repository.save(p3);
            repository.save(p4);

            //Create Games
            Game g1 = new Game();
            Game g2 = new Game();
            Game g3 = new Game();
            Game g4 = new Game();
            Game g5 = new Game();
            Game g6 = new Game();
            Game g7 = new Game();
            Game g8 = new Game();
            g2.setCreationDateTime(LocalDateTime.ofInstant(g2.getCreationDateTime().toInstant(ZoneOffset.UTC).plusSeconds(3600), ZoneOffset.UTC));
            g3.setCreationDateTime(LocalDateTime.ofInstant(g3.getCreationDateTime().toInstant(ZoneOffset.UTC).plusSeconds(2 * 3600), ZoneOffset.UTC));
            g4.setCreationDateTime(LocalDateTime.ofInstant(g4.getCreationDateTime().toInstant(ZoneOffset.UTC).plusSeconds(3 * 3600), ZoneOffset.UTC));
            g5.setCreationDateTime(LocalDateTime.ofInstant(g5.getCreationDateTime().toInstant(ZoneOffset.UTC).plusSeconds(4 * 3600), ZoneOffset.UTC));
            g6.setCreationDateTime(LocalDateTime.ofInstant(g6.getCreationDateTime().toInstant(ZoneOffset.UTC).plusSeconds(5 * 3600), ZoneOffset.UTC));
            g7.setCreationDateTime(LocalDateTime.ofInstant(g7.getCreationDateTime().toInstant(ZoneOffset.UTC).plusSeconds(6 * 3600), ZoneOffset.UTC));
            g8.setCreationDateTime(LocalDateTime.ofInstant(g8.getCreationDateTime().toInstant(ZoneOffset.UTC).plusSeconds(7 * 3600), ZoneOffset.UTC));
            repository2.save(g1);
            repository2.save(g2);
            repository2.save(g3);
            repository2.save(g4);
            repository2.save(g5);
            repository2.save(g6);
            repository2.save(g7);
            repository2.save(g8);

            //Create GamePlayers
            GamePlayer gp1 = new GamePlayer(p1, g1);
            GamePlayer gp2 = new GamePlayer(p2, g1);

            GamePlayer gp3 = new GamePlayer(p1, g2);
            GamePlayer gp4 = new GamePlayer(p2, g2);

            GamePlayer gp5 = new GamePlayer(p2, g3);
            GamePlayer gp6 = new GamePlayer(p4, g3);

            GamePlayer gp7 = new GamePlayer(p2, g4);
            GamePlayer gp8 = new GamePlayer(p1, g4);

            GamePlayer gp9 = new GamePlayer(p4, g5);
            GamePlayer gp10 = new GamePlayer(p1, g5);

            GamePlayer gp11 = new GamePlayer(p3, g6);

            GamePlayer gp12 = new GamePlayer(p4, g7);

            GamePlayer gp13 = new GamePlayer(p3, g8);
            GamePlayer gp14 = new GamePlayer(p4, g8);
            repository3.save(gp1);
            repository3.save(gp2);
            repository3.save(gp3);
            repository3.save(gp4);
            repository3.save(gp5);
            repository3.save(gp6);
            repository3.save(gp7);
            repository3.save(gp8);
            repository3.save(gp9);
            repository3.save(gp10);
            repository3.save(gp11);
            repository3.save(gp12);
            repository3.save(gp13);
            repository3.save(gp14);

            //Create Ships
            Ship s1 = new Ship("Destroyer", new ArrayList<>(Arrays.asList("H2", "H3", "H4")));
            gp1.addShip(s1);
            respository4.save(s1);
            Ship s2 = new Ship("Submarine", new ArrayList<>(Arrays.asList("E1", "F1", "G1")));
            gp1.addShip(s2);
            respository4.save(s2);
            Ship s3 = new Ship("Patrol Boat", new ArrayList<>(Arrays.asList("B4", "B5")));
            gp1.addShip(s3);
            respository4.save(s3);
            Ship s4 = new Ship("Destroyer", new ArrayList<>(Arrays.asList("B5", "C5", "D5")));
            gp2.addShip(s4);
            respository4.save(s4);
            Ship s5 = new Ship("Patrol Boat", new ArrayList<>(Arrays.asList("F1", "F2")));
            gp2.addShip(s5);
            respository4.save(s5);


            Ship s6 = new Ship("Destroyer", new ArrayList<>(Arrays.asList("B5", "C5", "D5")));
            gp3.addShip(s6);
            respository4.save(s6);
            Ship s7 = new Ship("Patrol Boat", new ArrayList<>(Arrays.asList("C6", "D6")));
            gp3.addShip(s7);
            respository4.save(s7);
            Ship s8 = new Ship("Submarine", new ArrayList<>(Arrays.asList("A2", "A3", "A4")));
            gp4.addShip(s8);
            respository4.save(s8);
            Ship s9 = new Ship("Patrol Boat", new ArrayList<>(Arrays.asList("G6", "H6")));
            gp4.addShip(s9);
            respository4.save(s9);


            Ship s10 = new Ship("Destroyer", new ArrayList<>(Arrays.asList("B5", "C5", "D5")));
            gp5.addShip(s10);
            respository4.save(s10);
            Ship s11 = new Ship("Patrol Boat", new ArrayList<>(Arrays.asList("C6", "D6")));
            gp5.addShip(s11);
            respository4.save(s11);
            Ship s12 = new Ship("Submarine", new ArrayList<>(Arrays.asList("A2", "A3", "A4")));
            gp6.addShip(s12);
            respository4.save(s12);
            Ship s13 = new Ship("Patrol Boat", new ArrayList<>(Arrays.asList("G6", "H6")));
            gp6.addShip(s13);
            respository4.save(s13);


            Ship s14 = new Ship("Destroyer", new ArrayList<>(Arrays.asList("B5", "C5", "D5")));
            gp7.addShip(s14);
            respository4.save(s14);
            Ship s15 = new Ship("Patrol Boat", new ArrayList<>(Arrays.asList("C6", "D6")));
            gp7.addShip(s15);
            respository4.save(s15);
            Ship s16 = new Ship("Submarine", new ArrayList<>(Arrays.asList("A2", "A3", "A4")));
            gp8.addShip(s16);
            respository4.save(s16);
            Ship s17 = new Ship("Patrol Boat", new ArrayList<>(Arrays.asList("G6", "H6")));
            gp8.addShip(s17);
            respository4.save(s17);


            Ship s18 = new Ship("Destroyer", new ArrayList<>(Arrays.asList("B5", "C5", "D5")));
            gp9.addShip(s18);
            respository4.save(s18);
            Ship s19 = new Ship("Patrol Boat", new ArrayList<>(Arrays.asList("C6", "D6")));
            gp9.addShip(s19);
            respository4.save(s19);
            Ship s20 = new Ship("Submarine", new ArrayList<>(Arrays.asList("A2", "A3", "A4")));
            gp10.addShip(s20);
            respository4.save(s20);
            Ship s21 = new Ship("Patrol Boat", new ArrayList<>(Arrays.asList("G6", "H6")));
            gp10.addShip(s21);
            respository4.save(s21);


            Ship s22 = new Ship("Destroyer", new ArrayList<>(Arrays.asList("B5", "C5", "D5")));
            gp11.addShip(s22);
            respository4.save(s22);
            Ship s23 = new Ship("Patrol Boat", new ArrayList<>(Arrays.asList("C6", "D6")));
            gp11.addShip(s23);
            respository4.save(s23);


            Ship s24 = new Ship("Destroyer", new ArrayList<>(Arrays.asList("B5", "C5", "D5")));
            gp13.addShip(s24);
            respository4.save(s24);
            Ship s25 = new Ship("Patrol Boat", new ArrayList<>(Arrays.asList("C6", "D6")));
            gp13.addShip(s25);
            respository4.save(s25);
            Ship s26 = new Ship("Submarine", new ArrayList<>(Arrays.asList("A2", "A3", "A4")));
            gp14.addShip(s26);
            respository4.save(s26);
            Ship s27 = new Ship("Patrol Boat", new ArrayList<>(Arrays.asList("G6", "H6")));
            gp14.addShip(s27);
            respository4.save(s27);


        };
    }
}
