var app = new Vue({
    el: "#app",
    data: {
        gridSize: 11,
        rowNames: ["", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J"],
        columnNames: ["1", "2", "3", "4", "5", "6", "7", "8", "9", "10"],
        intervalId: null,
        loading: true,
        urlGamesPage: "games.html",
        urlApiGameViewGPID: "/api/game_view/",
        gameData: null,
        gamePlayersObj: {
            gp: {
                id: null,
                email: null
            },
            opponent: {
                id: null,
                email: null
            }
        },
        shipDragged: null,
        listOfShips: [
            {
                type: "Aircraft Carrier",
                shipLocations: []
            },
            {
                type: "Battleship",
                shipLocations: []
            },
            {
                type: "Submarine",
                shipLocations: []
            },
            {
                type: "Destroyer",
                shipLocations: []
            },
            {
                type: "Patrol Boat",
                shipLocations: []
            }
        ],
        MAX_NUMBER_SHOTS: 0,
        currentSalvo: {
            turn: null,
            salvoLocations: []
        }
    },
    methods: {
        getCurrentGamePlayerId: function () {
            let parsedUrl = new URL(window.location.href);
            return parsedUrl.searchParams.get("gp") * 1;
        },

        //Fetch
        startFetch: function (url) {
            fetch(url, {
                    method: "GET"
                })
                .then(response => response.json())
                .then(myData => {
                    if (myData.error) {
                        alert(myData.error);
                        history.back();
                    } else {
                        this.gameData = myData;
                        this.fillGamePlayersObj();

                        this.MAX_NUMBER_SHOTS = (this.gameData.status.allowedToEnterSalvo) ? 5 : 0;

                        if(this.intervalId === null && this.gameData.status.message != "The Game Is Over"){
                            this.startFetchInterval(url);
                        }
                        if(this.gameData.status.message === "The Game Is Over" && this.intervalId != null){
                            this.stopFetchInterval();
                        }

                        this.loading = false;
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
                        this.goToHomePage();
                    }
                })
                .catch(error => {
                    console.log('Request failure: ', error);
                });
        },
        goToHomePage: function () {
            window.location.href = this.urlGamesPage;
        },
        postShipsToServer: function () {
            if (this.areAllShipsPlaced()) {
                fetch("/api/games/players/" + this.gamePlayersObj.gp.id + "/ships", {
                        credentials: 'include',
                        headers: {
                            'Accept': 'application/json',
                            'Content-Type': 'application/json'
                        },
                        method: 'POST',
                        body: JSON.stringify(this.listOfShips)
                    })
                    .then(function (response) {
                        return response.json();
                    }).then(function (myData) {
                        if (myData.error) {
                            console.log(myData.error);
                        } else {
                            console.log(myData.ok);
                            window.location.reload();
                        }
                    })
                    .catch(function (error) {
                        console.log('Request failure: ', error);
                    });
            } else {
                alert("You have to place all the ships!");
            }
        },
        postSalvoToServer: function () {
            let salvo = this.currentSalvo;
            fetch("/api/games/players/" + this.gamePlayersObj.gp.id + "/salvoes", {
                    credentials: 'include',
                    headers: {
                        'Accept': 'application/json',
                        'Content-Type': 'application/json'
                    },
                    method: 'POST',
                    body: JSON.stringify(salvo)
                })
                .then(response => response.json())
                .then(myData => {
                    if (myData.error) {
                        alert(myData.error);
                        this.deleteCurrentSalvo();
                    } else {
                        window.location.reload();
                    }
                })
                .catch(function (error) {
                    console.log('Request failure: ', error);
                });
        },

        //Grid
        getRow: function (i) {
            //The v-for starts with i=1. I want that starts with i=0.
            i--;
            return Math.trunc(i / 11);
        },
        getColumn(i) {
            //The v-for starts with i=1. I want that starts with i=0.
            i--;
            return i % 11;
        },
        getCellPosition: function (i) {
            let inRow = this.getRow(i);
            let inColumn = this.getColumn(i);
            if (inColumn == 0) {
                return this.rowNames[inRow]
            } else {
                return this.rowNames[inRow] + inColumn;
            }
        },
        isAMarginCell: function (i) {
            let cellPosition = this.getCellPosition(i);
            return (this.rowNames.includes(cellPosition) || this.columnNames.includes(cellPosition) || i == 1);
        },

        //GamePlayer Object: Current and Opponent
        fillGamePlayersObj: function () {
            let gamePlayers = this.gameData.gamePlayers;
            if (gamePlayers.length == 2) {
                if (gamePlayers[0].id == this.gamePlayersObj.gp.id) {
                    this.gamePlayersObj.gp.email = gamePlayers[0].player.email;
                    this.gamePlayersObj.opponent.id = gamePlayers[1].id;
                    this.gamePlayersObj.opponent.email = gamePlayers[1].player.email;
                } else {
                    this.gamePlayersObj.gp.email = gamePlayers[1].player.email;
                    this.gamePlayersObj.opponent.id = gamePlayers[0].id;
                    this.gamePlayersObj.opponent.email = gamePlayers[0].player.email;
                }
            } else {
                this.gamePlayersObj.gp.email = gamePlayers[0].player.email;
                this.gamePlayersObj.opponent.email = "'WAITING FOR AN OPPONENT!'";
            }
        },

        //Detect Ships and Salvoes
        thereIsAShip: function (i) {
            for (let j = 0; j < this.gameData.ships.length; j++) {
                if (this.gameData.ships[j].locations.includes(this.getCellPosition(i))) {
                    return true;
                }
            }
            return false;
        },
        thereIsASalvoOpponent: function (i) {
            let salvoes = this.gameData.salvoes[this.gamePlayersObj.opponent.id];
            let cellPosition = this.getCellPosition(i);
            for (key in salvoes) {
                if (salvoes[key].includes(cellPosition)) {
                    return [true, key];
                }
            }
            return [false];
        },
        thereIsASalvoGP: function (i) {
            let salvoes = this.gameData.salvoes[this.gamePlayersObj.gp.id];
            let cellPosition = this.getCellPosition(i);
            for (key in salvoes) {
                if (salvoes[key].includes(cellPosition)) {
                    return [true, key];
                }
            }
            return [false];
        },
        getSalvoClasstoOPGrid: function (i) {
            let cellPosition = this.getCellPosition(i);
            if (this.mySalvoHitAOPShip(cellPosition)) {
                if (this.mySalvoSunkAShip(cellPosition)) {
                    return "grid-item-OP-Salvo-Sunk";
                }
                return "grid-item-OP-Salvo-Hit";
            }
            return "grid-item-OP-Salvo-noHit";
        },
        mySalvoHitAOPShip(cellPosition) {
            for (key in this.gameData.hits[this.gamePlayersObj.opponent.id]) {
                if (this.gameData.hits[this.gamePlayersObj.opponent.id][key].locations.includes(cellPosition)) {
                    return true;
                }
            }
        },
        mySalvoSunkAShip(cellPosition) {
            for (key in this.gameData.hits[this.gamePlayersObj.opponent.id]) {
                if (this.gameData.hits[this.gamePlayersObj.opponent.id][key].locations.includes(cellPosition)) {
                    return this.gameData.hits[this.gamePlayersObj.opponent.id][key].isSunk;
                }
            }
        },
        getCellNameShips: function (i) {
            let cellPosition = this.getCellPosition(i);
            if (this.rowNames.includes(cellPosition) || this.columnNames.includes(cellPosition)) {
                return cellPosition;
            } else {
                let thereIsASalvo = this.thereIsASalvoOpponent(i);
                if (thereIsASalvo[0]) {
                    return thereIsASalvo[1];
                }
            }
            return null;
        },
        getCellNameSalvoes: function (i) {
            let cellPosition = this.getCellPosition(i);
            if (this.rowNames.includes(cellPosition) || this.columnNames.includes(cellPosition)) {
                return cellPosition;
            } else {
                let thereIsASalvo = this.thereIsASalvoGP(i);
                if (thereIsASalvo[0]) {
                    return thereIsASalvo[1];
                }
            }
            return null;
        },
        getTurn: function(){
            return Object.keys(this.gameData.salvoes[this.getCurrentGamePlayerId()]).length + 1;
        },

        //Place Ships
        areAllShipsPlaced: function () {
            let areAllShipsPlaced = true;
            for (let i = 0; i < this.listOfShips.length; i++) {
                if (this.listOfShips[i].shipLocations == 0) {
                    areAllShipsPlaced = false;
                }
            }
            return areAllShipsPlaced;
        },
        showImg: function (btnId) {
            document.getElementById(btnId).classList.remove('invisible');
        },
        hideImg: function (btnId) {
            document.getElementById(btnId).classList.add('invisible');
        },
        rotateShip: function (shipId, btnId) {
            let ship = document.getElementById(shipId);
            let btn = document.getElementById(btnId);
            let firstCell;
            for (let i = 0; i < this.listOfShips.length; i++) {
                if (this.listOfShips[i].type == ship.getAttribute("data-shipType")) {
                    if (this.listOfShips[i].shipLocations.length != 0) {
                        firstCell = document.querySelector("[data-cellPosition=" + this.listOfShips[i].shipLocations[0] + "]");
                    }
                }
            }
            const HvsV = {
                H: "V",
                V: "H"
            };
            const imgHvsV = {
                H: "styles/img/VToH.png",
                V: "styles/img/HToV.png"
            };
            let initialDirection = ship.getAttribute("data-direction");
            ship.setAttribute("data-direction", HvsV[initialDirection]);
            ship.className = shipId + "-" + HvsV[initialDirection];
            btn.setAttribute("src", imgHvsV[initialDirection]);
            if (firstCell != undefined) {
                if (!this.correctPlaced(firstCell, ship)) {
                    ship.className += " red";
                    setTimeout(() => {
                        ship.setAttribute("data-direction", initialDirection);
                        ship.className = shipId + "-" + initialDirection;
                        btn.setAttribute("src", imgHvsV[HvsV[initialDirection]]);
                    }, 500);
                } else {
                    for (let i = 0; i < this.listOfShips.length; i++) {
                        if (this.listOfShips[i].type == ship.getAttribute("data-shipType")) {
                            this.listOfShips[i].shipLocations = this.getShipLocations(firstCell, ship);
                        }
                    }
                }
            }
        },
        dragStart: function (ev) {
            this.shipDragged = ev.target;
            setTimeout(() => (this.shipDragged.classList.add('invisible')), 0);
        },
        dragEnter: function (ev) {
            this.allGridItemsClassToDrop();

            let cell = ev.target;
            if (this.insideTheGrid(cell)) {
                if (this.correctPlaced(cell, this.shipDragged)) {
                    cell.setAttribute("data-drop", "droppable");
                    this.printCellsHovered(cell, this.shipDragged);
                } else {
                    cell.setAttribute("data-drop", "undroppable");
                    this.printCellsHovered(cell, this.shipDragged);
                }
            }
        },
        insideTheGrid: function (cell) {
            let cellColumn = +cell.getAttribute("data-cellColumn");
            let cellRow = +cell.getAttribute("data-cellRow");
            return (cellColumn != 0 && cellRow != 0);
        },
        correctPlaced: function (cell, ship) {
            let cellColumn = +cell.getAttribute("data-cellColumn");
            let cellRow = +cell.getAttribute("data-cellRow");
            let shipDirection = ship.getAttribute("data-direction");
            let shipLength = +ship.getAttribute("data-length");

            if (!this.areShipsOverlap(cell, ship)) {
                if (shipDirection == "H") {
                    return (cellColumn + shipLength - 1 <= 10);
                } else {
                    return (cellRow + shipLength - 1 <= 10);
                }
            }
            return false;
        },
        areShipsOverlap: function (cell, ship) {
            let shipType = ship.getAttribute("data-shipType");
            let currentLocations = this.getShipLocations(cell, ship);

            for (let i = 0; i < currentLocations.length; i++) {
                for (let j = 0; j < this.listOfShips.length; j++) {
                    if (shipType != this.listOfShips[j].type && this.listOfShips[j].shipLocations.includes(currentLocations[i])) {
                        return true;
                    }
                }
            }
        },
        printCellsHovered: function (cell, ship) {
            let cellColumn = +cell.getAttribute("data-cellColumn");
            let cellRow = +cell.getAttribute("data-cellRow");
            let shipLength = +ship.getAttribute("data-length");
            let shipDirection = ship.getAttribute("data-direction");
            let className = (cell.getAttribute("data-drop") == "droppable") ? "grid-item-drop-allowed": "grid-item-drop-denied";

            if (shipDirection == "H") {
                let end = cellColumn + shipLength - 1;
                if (end > 10) {
                    end = 10;
                }
                for (let i = cellColumn; i <= end; i++) {
                    document.querySelector("[data-cellPosition=" + this.rowNames[cellRow] + i + "]").classList.add(className);
                }
            } else if (shipDirection == "V") {
                let end = cellRow + shipLength - 1;
                if (end > 10) {
                    end = 10;
                }
                for (let i = cellRow; i <= end; i++) {
                    document.querySelector("[data-cellPosition=" + this.rowNames[i] + cellColumn + "]").classList.add(className);
                }
            }
        },
        allowDrop: function (ev) {
            ev.preventDefault();
        },
        dragLeave: function (ev) {
            
        },
        dragDrop: function (ev) {
            ev.preventDefault();
            let cell = ev.target;
            if (cell.getAttribute("data-drop") == "droppable") {
                cell.appendChild(document.getElementById(this.shipDragged.id));
                for (let i = 0; i < this.listOfShips.length; i++) {
                    if (this.listOfShips[i].type == this.shipDragged.getAttribute("data-shipType")) {
                        this.listOfShips[i].shipLocations = this.getShipLocations(cell, this.shipDragged);
                    }
                }
            }
        },
        getShipLocations: function (firstCell, ship) {
            let shipDirection = ship.getAttribute("data-direction");
            let shipLength = +ship.getAttribute("data-length");
            let cellColumn = +firstCell.getAttribute("data-cellColumn");
            let cellRow = +firstCell.getAttribute("data-cellRow");
            let locations = [];
            if (shipDirection == "H") {
                let ini = cellColumn;
                let end = cellColumn + shipLength - 1;
                for (let i = ini; i <= end; i++) {
                    locations.push(this.rowNames[cellRow] + i);
                }
            } else {
                let ini = cellRow;
                let end = cellRow + shipLength - 1;
                for (let i = ini; i <= end; i++) {
                    locations.push(this.rowNames[i] + cellColumn);
                }
            }
            return locations;
        },
        dragEnd: function () {
            this.shipDragged.className = this.shipDragged.id + "-" + this.shipDragged.getAttribute("data-direction");
            this.shipDragged = null;
            this.allGridItemsClassToDrop();
        },
        allGridItemsClassToDrop: function () {
            Array.from(document.getElementsByClassName("grid-item-drop-allowed")).map(cell => {
                cell.classList.remove("grid-item-drop-allowed");
            });
            Array.from(document.getElementsByClassName("grid-item-drop-denied")).map(cell => {
                cell.classList.remove("grid-item-drop-denied");
            });
        },

        //Shot Salvoes
        salvoEnter: function (i) {
            if (!this.isAMarginCell(i) && this.allowAnotherShot()) {
                let cell = this.getCellByNumber(i);
                if (cell.children[0].className == "grid-item-OP-Salvo-Empty") {
                    cell.children[0].className = "grid-item-OP-Salvo-Aim";
                }
            }
        },
        salvoLeave: function (i) {
            if (!this.isAMarginCell(i) && this.allowAnotherShot()) {
                let cell = this.getCellByNumber(i);
                if (cell.children[0].className == "grid-item-OP-Salvo-Aim") {
                    cell.children[0].className = "grid-item-OP-Salvo-Empty";
                }
            }
        },
        salvoClick: function (i) {
            if (!this.isAMarginCell(i)) {
                let cell = this.getCellByNumber(i);
                if (cell.textContent == "") {
                    if (cell.children[0].className == "grid-item-OP-Salvo-Aim") {
                        if (this.allowAnotherShot()) {
                            cell.children[0].className = "grid-item-OP-Salvo";
                            this.currentSalvo.salvoLocations.push(this.getCellPosition(i));
                        }
                    } else if (cell.children[0].className == "grid-item-OP-Salvo") {
                        cell.children[0].className = "grid-item-OP-Salvo-Aim";
                        let index = this.currentSalvo.salvoLocations.indexOf(this.getCellPosition(i));
                        this.currentSalvo.salvoLocations.splice(index, 1);
                    }
                }
            }
        },
        getCellByNumber: function (i) {
            return document.querySelector("div.grid-container-salvoes div[data-cellPosition=" + this.getCellPosition(i) + "]");
        },
        getCellByPos: function (pos) {
            return document.querySelector("div.grid-container-salvoes div[data-cellPosition=" + pos + "]");
        },
        allowAnotherShot: function () {
            return (this.currentSalvo.salvoLocations.length < this.MAX_NUMBER_SHOTS);
        },
        deleteCurrentSalvo: function () {
            this.currentSalvo.salvoLocations.forEach(this.deleteCurrentShots);
            this.currentSalvo.salvoLocations = [];
        },
        deleteCurrentShots: function (pos) {
            let cell = this.getCellByPos(pos);
            cell.children[0].className = "grid-item-OP-Salvo-Empty";
        },
        startFetchInterval: function(url){
            const INTERVAL = 2000;
            let i=0;
            this.intervalId = setInterval(() => {
                i++;
                this.startFetch(url); 
            }, INTERVAL);
        },
        stopFetchInterval: function(){
            clearInterval(this.intervalId);
        }
    },
    created: function () {
        this.gamePlayersObj.gp.id = this.getCurrentGamePlayerId();
        this.urlApiGameViewGPID += this.gamePlayersObj.gp.id;
        this.startFetch(this.urlApiGameViewGPID);
    }
});