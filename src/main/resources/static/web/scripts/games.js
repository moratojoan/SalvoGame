var app = new Vue({
    el: "#app",
    data: {
        urlApiGames: "/api/games",
        urlApiLeaderBoard: "/api/leaderboard",
        urlLogin: "/api/login",
        urlSignup: "/api/players",
        formUserName: "",
        formPassword: "",
        urlLogout: "api/logout",
        gamesList: null,
        currentPlayer: null,
        leaderBoard: null,
        intervalId: null,
        loading: true
    },
    methods: {
        //FetchList with Promise.all()
        startFetchList: function (URLS, INIT) {
            this.fetchJsonList(URLS, INIT)
            .then(([gamesData, leaderBoard]) => {
                this.gamesList = gamesData.games;
                if(gamesData.player){
                    this.currentPlayer = gamesData.player;
                }
                this.leaderBoard = leaderBoard;

                if(this.intervalId === null){
                    this.startFetchListInterval(URLS, INIT);
                }

                this.loading = false;
            });
        },
        fetchJsonList: function (urls, init) {
            return Promise.all(urls.map(url => this.fetchJson(url, init)));
        },
        fetchJson: function (url, init) {
            return fetch(url, init)
            .then(response => {
                if (response.ok) {
                    return response.json();
                }
                throw new Error(response.statusText);
            });
        },
        
        convertMillisecondsToDate: function (milliseconds) {
            let date = new Date(milliseconds);
            return this.correctDigitsOfDateNumber(date.getDate()) + "/" +
                    this.correctDigitsOfDateNumber(date.getMonth() + 1) + "/" +
                    this.correctDigitsOfDateNumber(date.getFullYear()) + " - " +
                    this.correctDigitsOfDateNumber(date.getHours()) + ":" +
                    this.correctDigitsOfDateNumber(date.getMinutes());
        },
        correctDigitsOfDateNumber: function(number){
            stringNumber = number+"";
            if(stringNumber.length == 1){
                stringNumber = "0" + stringNumber;
            }
            return stringNumber;
        },
        
        login: function () {
            fetch(this.urlLogin, {
                    credentials: 'include',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded'
                    },
                    method: 'POST',
                    body: 'userName=' + this.formUserName + '&password=' + this.formPassword,
                })
                .then(response => {
                    if (response.status == 200) {
                        location.reload();
                    } else if (response.status == 401) {
                        alert("Login failed!");
                    }
                })
                .catch(error => {
                    console.log('Request failure: ', error);
                });
        },
        logout: function () {
            fetch("/api/logout", {
                    credentials: 'include',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded'
                    },
                    method: 'POST',
                })
                .then(response => {
                    console.log('Request success: ', response);
                    if (response.ok) {
                        location.reload();
                    }
                })
                .catch(error => {
                    console.log('Request failure: ', error);
                });
        },
        signup: function () {
            fetch(this.urlSignup, {
                    credentials: 'include',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded'
                    },
                    method: 'POST',
                    body: 'userName=' + this.formUserName + '&password=' + this.formPassword,
                })
                .then(response => response.json())
                .then(myData => {
                    if (myData.error) {
                        alert(myData.error);
                    } else {
                        this.login();
                    }
                })
                .catch(error => {
                    console.log('Request failure: ', error);
                });
        },
        
        playerCanEnterThisGame: function (game) {
            if (this.currentPlayer != null) {
                return (this.filterGamePlayers(game).length == 1);
            }
            return false;
        },
        enterGameButtonHref: function (game) {
            if (this.currentPlayer != null) {
                let filterGamePlayers = this.filterGamePlayers(game)
                if (filterGamePlayers.length == 1) {
                    window.location.href = "game.html?gp=" + filterGamePlayers[0].id;
                }
            }
        },
        filterGamePlayers: function(game){
            return game.gamePlayers.filter(gameplayer => gameplayer.player.id == this.currentPlayer.id);
        },
        createNewGame: function () {
            fetch("/api/games", {
                    credentials: 'include',
                    headers: {
                        'Accept': 'application/json',
                        'Content-Type': 'application/x-www-form-urlencoded'
                    },
                    method: 'POST',
                })
                .then(function (response) {
                    return response.json();
                }).then(function (data) {
                    if (data.error) {
                        alert(data.error);
                    } else {
                        window.location = 'game.html?gp=' + data.gpId;
                    }

                })
                .catch(function (error) {});
        },
        playerCanJoinThisGame: function (game) {
            if (this.currentPlayer != null) {
                if (game.gamePlayers.length < 2) {
                    return (this.filterGamePlayers(game).length == 0);
                }
            }
            return false;
        },
        joinGameButtonHref: function (game) {
            fetch("/api/game/" + game.id + "/players", {
                    credentials: 'include',
                    headers: {
                        'Accept': 'application/json',
                        'Content-Type': 'application/x-www-form-urlencoded'
                    },
                    method: 'POST',
                })
                .then(function (response) {
                    return response.json();
                }).then(function (myData) {
                    if(myData.error){
                        alert(myData.error);
                    }else{
                        window.location.href = "game.html?gp=" + myData.gpId;
                    }
                })
                .catch(function (error) {});
        },

        startFetchListInterval: function(URLS, INIT){
            const INTERVAL = 10000;
            let i=0;
            this.intervalId = setInterval(() => {
                i++;
                this.startFetchList(URLS, INIT);
            }, INTERVAL);
        },
        stopFetchListInterval: function(){
            clearInterval(this.intervalId);
        }
    },
    created: function () {
        const URLS = [this.urlApiGames, this.urlApiLeaderBoard];
        const INIT = {
            method: "GET"
        };
        this.startFetchList(URLS, INIT);
    }
});