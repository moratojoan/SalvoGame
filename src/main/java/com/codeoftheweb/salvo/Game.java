package com.codeoftheweb.salvo;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.*;

import static java.util.stream.Collectors.toList;


@Entity
public class Game {

    //Attributes
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private Long id;

    private Date creationDateTime;

    @OneToMany(mappedBy="game", fetch= FetchType.EAGER)
    private Set<GamePlayer> gamePlayerSet = new HashSet<>();

    @OneToMany(mappedBy="game", fetch= FetchType.EAGER)
    private Set<Score> scoreSet = new HashSet<>();

    //Constructors
    public Game(){
        this.creationDateTime = new Date();
    }

    //Getters and Setters of the Attributes
    public Long getId(){
        return this.id;
    }

    public Date getCreationDateTime(){
        return this.creationDateTime;
    }

    public void setCreationDateTime(Date creationDateTime) {
        this.creationDateTime = creationDateTime;
    }

    public Set<GamePlayer> getGamePlayerSet() {
        return this.gamePlayerSet;
    }

    public Set<Score> getScoreSet(){
        return this.scoreSet;
    }

    //Other Methods
    public List<Player> getPlayers(){
        return this.gamePlayerSet.stream().map(GamePlayer::getPlayer).collect(toList());
    }

    public boolean isTheGameOver(){
        List<GamePlayer> gamePlayers = new ArrayList<>(this.getGamePlayerSet());
        if(gamePlayers.size() == 2){
            if(gamePlayers.get(0).getShipSet().size()>0 && gamePlayers.get(1).getShipSet().size()>0){
                if(gamePlayers.get(0).getSalvoSet().size() == gamePlayers.get(1).getSalvoSet().size()){
                    return gamePlayers.get(0).sunkAllOpponentShips() || gamePlayers.get(1).sunkAllOpponentShips();
                }
            }
        }
        return false;
    }


}
