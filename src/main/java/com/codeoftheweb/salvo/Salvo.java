package com.codeoftheweb.salvo;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Salvo {

    //Attributes
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private Long id;

    private Integer turn;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="gamePlayer_id")
    private GamePlayer gamePlayer;

    @ElementCollection
    @Column(name="salvoLocation")
    private List<String> salvoLocations = new ArrayList<>();

    //Constructors
    public Salvo(){}

    public Salvo(Integer turn, List<String> locations){
        this.turn = turn;
        this.salvoLocations = locations;
    }

    //Getters and Setters of the Attributes
    public Long getId(){
        return this.id;
    }

    public Integer getTurn(){
        return this.turn;
    }

    public void setTurn(Integer turn){
        this.turn=turn;
    }

    public GamePlayer getGamePlayer(){
        return this.gamePlayer;
    }

    public void setGamePlayer(GamePlayer gamePlayer){
        this.gamePlayer = gamePlayer;
    }

    public List<String> getSalvoLocations(){
        return this.salvoLocations;
    }

    public void setSalvoLocations(List<String> salvoLocations){
        this.salvoLocations = salvoLocations;
    }

    //Other Methods

}
