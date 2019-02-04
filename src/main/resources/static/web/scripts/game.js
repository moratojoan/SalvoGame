var app = new Vue({
    el: "#app",
    data: {
        gridSize: 11,
        rowNames: ["", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J"],
        columnNames: ["1", "2", "3", "4", "5", "6", "7", "8", "9", "10"],
        urlApiGameViewGPID: "/api/game_view/",
        gamePlayersObj: {
            gp: {
                id: null,
                email: null
            },
            oponent: {
                id: null,
                email: null
            }
        },
        gameData: null,
        loading: true,
        shipDragged: null,
        listOfShips: [{
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
        ]
    },
    methods: {
        getURLParams: function () {
            let parsedUrl = new URL(window.location.href);
            this.gamePlayersObj.gp.id = parsedUrl.searchParams.get("gp") * 1;
        },
        startFetch: function (url) {
            fetch(url, {
                    method: "GET"
                })
                .then(response => response.json())
                .then(myData => {
                    if (myData.error) {
                        console.log(myData.error);
                        alert(myData.error);
                        history.back();
                    } else {
                        this.gameData = myData;
                        console.log(myData);
                        this.fillGamePlayersObj();
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
            window.location.href = "games.html";
        },
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
        thereIsAShip: function (i) {
            for (let j = 0; j < this.gameData.ships.length; j++) {
                if (this.gameData.ships[j].locations.includes(this.getCellPosition(i))) {
                    return true;
                }
            }
            return false;
        },
        thereIsASalvoOponent: function (i) {
            let salvoes = this.gameData.salvoes[this.gamePlayersObj.oponent.id];
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
        getCellNameShips: function (i) {
            let cellPosition = this.getCellPosition(i);
            if (this.rowNames.includes(cellPosition) || this.columnNames.includes(cellPosition)) {
                return cellPosition;
            } else {
                let thereIsASalvo = this.thereIsASalvoOponent(i);
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
        fillGamePlayersObj: function () {
            let gamePlayers = this.gameData.gamePlayers;
            if (gamePlayers.length == 2) {
                if (gamePlayers[0].id == this.gamePlayersObj.gp.id) {
                    this.gamePlayersObj.gp.email = gamePlayers[0].player.email;
                    this.gamePlayersObj.oponent.id = gamePlayers[1].id;
                    this.gamePlayersObj.oponent.email = gamePlayers[1].player.email;
                } else {
                    this.gamePlayersObj.gp.email = gamePlayers[1].player.email;
                    this.gamePlayersObj.oponent.id = gamePlayers[0].id;
                    this.gamePlayersObj.oponent.email = gamePlayers[0].player.email;
                }
            } else {
                this.gamePlayersObj.gp.email = gamePlayers[0].player.email;
                this.gamePlayersObj.oponent.email = "'WAITING FOR AN OPONENT!'";
            }
        },
        areAllShipsPlaced: function () {
            let areAllShipsPlaced = true;
            for (let i = 0; i < this.listOfShips.length; i++) {
                if (this.listOfShips[i].shipLocations == 0) {
                    areAllShipsPlaced = false;
                }
            }
            return areAllShipsPlaced;
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
        showImg: function (btnId) {
            document.getElementById(btnId).style.display = "block";
        },
        hideImg: function (btnId) {
            document.getElementById(btnId).style.display = "none";
        },
        rotateShip2: function (shipId, btnId) {
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
            setTimeout(() => (this.shipDragged.className = 'invisible'), 0);
        },
        dragEnter: function (ev) {
            this.allGridItemsClassToDrop();
            console.log("enter", ev.target.getAttribute("data-cellPosition"));
            let cell = ev.target;
            let shipDirection = this.shipDragged.getAttribute("data-direction");

            if (this.insideTheGrid(cell)) {
                if (this.correctPlaced(cell, this.shipDragged)) {
                    cell.setAttribute("data-drop", "droppable");
                    if (shipDirection == "H") {
                        this.printCellsHoritzontal(cell, this.shipDragged, "grid-item-drop-allowed");
                    } else {
                        this.printCellsVertical(cell, this.shipDragged, "grid-item-drop-allowed");
                    }
                } else {
                    cell.setAttribute("data-drop", "undroppable");
                    if (shipDirection == "H") {
                        this.printCellsHoritzontal(cell, this.shipDragged, "grid-item-drop-denied");
                    } else {
                        this.printCellsVertical(cell, this.shipDragged, "grid-item-drop-denied");
                    }
                }
            }
        },
        insideTheGrid: function (cell) {
            let cellColumn = +cell.getAttribute("data-cellColumn");
            let cellRow = +cell.getAttribute("data-cellRow");
            return (cellColumn != 0 && cellRow != 0);
        },
        correctPlaced: function (cell, ship) {
            let shipDirection = ship.getAttribute("data-direction");
            let shipLength = +ship.getAttribute("data-length");
            let cellColumn = +cell.getAttribute("data-cellColumn");
            let cellRow = +cell.getAttribute("data-cellRow");

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
        printCellsVertical: function (cell, ship, className) {
            let cellColumn = +cell.getAttribute("data-cellColumn");
            let cellRow = +cell.getAttribute("data-cellRow");
            let shipLength = +ship.getAttribute("data-length");

            let end = cellRow + shipLength - 1;
            if (end > 10) {
                end = 10;
            }
            for (let i = cellRow; i <= end; i++) {
                document.querySelector("[data-cellPosition=" + this.rowNames[i] + cellColumn + "]").className = className;
            }
        },
        printCellsHoritzontal: function (cell, ship, className) {
            let cellColumn = +cell.getAttribute("data-cellColumn");
            let cellRow = +cell.getAttribute("data-cellRow");
            let shipLength = +ship.getAttribute("data-length");

            let end = cellColumn + shipLength - 1;
            if (end > 10) {
                end = 10;
            }
            for (let i = cellColumn; i <= end; i++) {
                document.querySelector("[data-cellPosition=" + this.rowNames[cellRow] + i + "]").className = className;
            }
        },
        allowDrop: function (ev) {
            ev.preventDefault();
        },
        dragLeave: function (ev) {
            console.log("leave", ev.target.getAttribute("data-cellPosition"));
            // this.allGridItemsClassToDrop();
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
                cell.className = "grid-item-drop"
            });
            Array.from(document.getElementsByClassName("grid-item-drop-denied")).map(cell => {
                cell.className = "grid-item-drop"
            });
        }
    },
    created: function () {
        this.getURLParams();
        this.startFetch(this.urlApiGameViewGPID + this.gamePlayersObj.gp.id);
    }
});