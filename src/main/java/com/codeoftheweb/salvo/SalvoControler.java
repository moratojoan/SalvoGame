package com.codeoftheweb.salvo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toList;


@RestController
@RequestMapping("/api")
public class SalvoControler {

    @Autowired
    private GameRepository gameRepository;

    @RequestMapping("/games")
    public List<Object> getAllGames(){
        return gameRepository.findAll().stream().map(this::makeGameDTO).collect(toList());
    }

    //------ DTO methods ----

    private Map<String,Object> makeGameDTO(Game game){
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("id",game.getId());
        dto.put("created",game.getCreationDateTime());
        dto.put("gamePlayers", makeListGamePlayerDTO(game.getGamePlayerSet()));
        return dto;
    }

    private List<Map<String,Object>> makeListGamePlayerDTO(Set<GamePlayer> gamePlayerSet) {
        return gamePlayerSet.stream().map(this::makeGamePlayerDTO).collect(toList());
    }

    private Map<String,Object> makeGamePlayerDTO(GamePlayer gamePlayer){
        Map<String,Object> dto = new LinkedHashMap<>();
        dto.put("id",gamePlayer.getId());
        dto.put("player", makePlayerDTO(gamePlayer.getPlayer()));
        return dto;
    }

    private Map<String,Object> makePlayerDTO(Player player) {
        Map<String,Object> dto = new LinkedHashMap<>();
        dto.put("id",player.getId());
        dto.put("email", player.getUserName());
        return dto;
    }

}
