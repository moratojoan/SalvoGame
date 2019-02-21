package com.codeoftheweb.salvo;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.*;

import static java.util.stream.Collectors.toList;

@Entity
public class Ship {

    //Attributes
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private Long id;

    private String type;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="gamePlayer_id")
    private GamePlayer gamePlayer;

    @ElementCollection
    @Column(name="shipLocation")
    private List<String> shipLocations = new ArrayList<>();

    //Constructors
    public Ship(){}

    public Ship(String type, List<String> shipLocations){
        this.type = type;
        this.shipLocations = shipLocations;
    }

    //Getters and Setters of the Attributes
    public Long getId(){
        return this.id;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public GamePlayer getGamePlayer() {
        return this.gamePlayer;
    }

    public void setGamePlayer(GamePlayer gamePlayer) {
        this.gamePlayer = gamePlayer;
    }

    public List<String> getShipLocations() {
        return this.shipLocations;
    }

    public void setShipLocations(List<String> shipLocations) {
        this.shipLocations = shipLocations;
    }


    //Other Methods
    public boolean isTheShipSunk(){
        GamePlayer opponent = this.getGamePlayer().getOpponent();
        if(opponent == null){
            return false;
        }

        List<String> opponentSalvoLocations = opponent.getSalvoSet().stream().map(Salvo::getSalvoLocations).flatMap(Collection::stream).collect(toList());
        List<String> shipLocations = this.getShipLocations();
        return opponentSalvoLocations.containsAll(shipLocations);
    }

    public List<String> getListOfHitLocations(){
        GamePlayer opponent = this.getGamePlayer().getOpponent();
        if(opponent == null){
            return new ArrayList<>();
        }

        List<String> opponentSalvoLocations = opponent.getSalvoSet().stream().map(Salvo::getSalvoLocations).flatMap(Collection::stream).collect(toList());
        List<String> shipLocations = this.getShipLocations();
        return shipLocations.stream().filter(opponentSalvoLocations::contains).collect(toList());
    }

    public Map<String,Object> getMapOfShipState(){
        Map<String,Object> mapOfShipState = new HashMap<>();
        mapOfShipState.put("isSunk", this.isTheShipSunk());
        mapOfShipState.put("locations", this.getListOfHitLocations());
        return mapOfShipState;
    }
}
