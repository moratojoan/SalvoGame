package com.codeoftheweb.salvo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@SpringBootApplication
public class SalvoApplication {

	public static void main(String[] args) {
		SpringApplication.run(SalvoApplication.class, args);

	}

	@Bean
	public CommandLineRunner initData(PlayerRepository repository, GameRepository repository2, GamePlayerRepository repository3){
		return args -> {
			//save Players
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
			Instant i2 = g2.getCreationDateTime().toInstant(ZoneOffset.UTC);
			g2.setCreationDateTime(LocalDateTime.ofInstant(i2.plusSeconds(3600),ZoneOffset.UTC));
			Instant i3 = g3.getCreationDateTime().toInstant(ZoneOffset.UTC);
			g3.setCreationDateTime(LocalDateTime.ofInstant(i3.plusSeconds(2*3600),ZoneOffset.UTC));
			repository2.save(g1);
			repository2.save(g2);
			repository2.save(g3);

			GamePlayer gp1 = new GamePlayer(p1,g1);
			GamePlayer gp2 = new GamePlayer(p2,g1);
			repository3.save(gp1);
			repository3.save(gp2);
			GamePlayer gp3= new GamePlayer(p3,g2);
			GamePlayer gp4 = new GamePlayer(p4,g2);
			repository3.save(gp3);
			repository3.save(gp4);

		};
	}
}
