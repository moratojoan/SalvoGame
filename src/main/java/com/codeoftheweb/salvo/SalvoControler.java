package com.codeoftheweb.salvo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

import static java.util.stream.Collectors.toList;


@RestController
@RequestMapping("/api")
public class SalvoControler {

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private GamePlayerRepository gamePlayerRepository;

    @RequestMapping("/games")
    public List<Object> getAllGames(){
        return gameRepository.findAll().stream().map(this::makeGameDTO).collect(toList());
    }

//    @RequestMapping("/game_view/{nn}")
//    public Optional<Map<String, Object>> getGamePlayerById(@PathVariable("nn") long gpId){
//        return gamePlayerRepository.findById(gpId).map(this::makeGamePlayerDTO);
//    }

    @RequestMapping("/game_view/{nn}")
    public Map<String, Object> getGamePlayerById(@PathVariable("nn") long gpId){
        Optional<GamePlayer> optionalGamePlayer = gamePlayerRepository.findById(gpId);
        Map<String, Object> dto = optionalGamePlayer.map(gamePlayer -> makeGameDTO(gamePlayer.getGame())).orElse(null);
        if(dto != null){
            dto.put("ships", makeListShipDTO(optionalGamePlayer.get().getShipSet()));
        }
        return dto;
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

    private List<Map<String,Object>> makeListShipDTO(Set<Ship> shipSet) {
        return shipSet.stream().map(this::makeShipDTO).collect(toList());
    }

    private Map<String,Object> makeShipDTO(Ship ship){
        Map<String,Object> dto = new LinkedHashMap<>();
        dto.put("type",ship.getType());
        dto.put("locations", ship.getShipLocations());
        return dto;
    }

}
