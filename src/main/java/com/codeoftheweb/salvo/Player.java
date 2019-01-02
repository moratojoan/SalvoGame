package com.codeoftheweb.salvo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;


@Entity
public class Player {

    //Attributes
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private Long id;

    private String userName;

    private String password;

    @OneToMany(mappedBy = "player", fetch = FetchType.EAGER)
    private Set<GamePlayer> gamePlayerSet = new HashSet<>();

    @OneToMany(mappedBy = "player", fetch = FetchType.EAGER)
    private Set<Score> scoreSet = new HashSet<>();

    //Constructors
    public Player() {
    }

    public Player(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }

    //Getters and Setters of the Attributes
    public Long getId(){
        return this.id;
    }

    public String getUserName() {
        return this.userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword(){
        return this.password;
    }

    public void setPassword(String password){
        this.password = password;
    }

    public Set<GamePlayer> getGamePlayerSet() {
        return this.gamePlayerSet;
    }

    public Set<Score> getScoreSet(){
        return this.scoreSet;
    }

    //Other Methods
    @JsonIgnore
    public List<Game> getGames(){
        return this.gamePlayerSet.stream().map(GamePlayer::getGame).collect(toList());
    }

    public Score getScore(Game game) {
        return game.getScoreSet().stream().filter(score -> score.getPlayer().getId().equals(this.id)).findFirst().orElse(null);
    }
}
