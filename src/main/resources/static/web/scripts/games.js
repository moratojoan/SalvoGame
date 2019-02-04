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
        loading: true
    },
    methods: {
        //FetchList with Promise.all()
        fetchJson: function (url, init) {
            return fetch(url, init)
            .then(response => {
                if (response.ok) {
                    return response.json();
                }
                throw new Error(response.statusText);
            });
        },
        fetchJsonList: function (urls, init) {
            return Promise.all(urls.map(url => this.fetchJson(url, init)));
        },
        startFetchList: function (urls, init) {
            this.fetchJsonList(urls, init)
            .then(values => {
                this.gamesList = values[0].games;
                if (values[0].player) {
                    this.currentPlayer = values[0].player;
                }
                this.leaderBoard = values[1];
                this.loading = false;
            });
        },
        //Method to convert milliseds to StringHour
        convertMillisecondsToDate: function (milliseconds) {
            let date = new Date(milliseconds);
            return date.getDate() + "/" + (date.getMonth() + 1) + "/" + date.getFullYear() + " - " + date.getHours() + ":" + date.getMinutes();
        },
        //Methods to login, logout and signup
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
                    console.log('Request success: ', response);
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
        //Other Methods
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