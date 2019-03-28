"use strict";

// --------
// WARNING:
// --------

// THIS CODE IS ONLY MADE AVAILABLE FOR DEMONSTRATION PURPOSES AND IS NOT SECURE!
// DO NOT USE IN PRODUCTION!

// FOR SECURITY REASONS, USING A JAVASCRIPT WEB APP HOSTED VIA THE CORDA NODE IS
// NOT THE RECOMMENDED WAY TO INTERFACE WITH CORDA NODES! HOWEVER, FOR THIS
// PRE-ALPHA RELEASE IT'S A USEFUL WAY TO EXPERIMENT WITH THE PLATFORM AS IT ALLOWS
// YOU TO QUICKLY BUILD A UI FOR DEMONSTRATION PURPOSES.

// GOING FORWARD WE RECOMMEND IMPLEMENTING A STANDALONE WEB SERVER THAT AUTHORISES
// VIA THE NODE'S RPC INTERFACE. IN THE COMING WEEKS WE'LL WRITE A TUTORIAL ON
// HOW BEST TO DO THIS.

const app = angular.module('demoAppModule', ['ui.bootstrap']).service('sharedProperties', function(){
    var property = {};

    return {
        getProperty: function(){
            return property;
        },
        setProperty: function(value){
            property = value;
        }
    };
})



// Fix for unhandled rejections bug.
app.config(['$qProvider', function ($qProvider) {
    $qProvider.errorOnUnhandledRejections(false);
}]);

app.controller('DemoAppController', function($http, $location, $uibModal, sharedProperties) {
    const demoApp = this;

    // We identify the node.
    const apiBaseURL = "/api/travel/";
    let peers = [];

    $http.get(apiBaseURL + "me").then((response) => demoApp.thisNode = response.data.me);

    console.log("My node info found");

    $http.get(apiBaseURL + "peers").then( function(response){
     peers = response.data.peers;
     demoApp.thisPeers = "";
     for(var i = 0; i < peers.length; i++){
          demoApp.thisPeers += peers[i].split(",")[2].split("=")[1] + " "
     }

    });

    console.log("Peers found");

    demoApp.approvalByAirline = (claimData) => {
        console.log('Airline Approval Invoked for: ');
        console.log(claimData.ref.txhash);
        $http.get(apiBaseURL + "airline-approval?ref="+claimData.ref.txhash).then(function(response){
            console.log('Airline Approval Result');
            console.log(response);
            var message = "Claim is approved successfully by Airline. Following transaction id is committed to the ledger. "+ response.data.transactionId;
            console.log(message);
            demoApp.displayMessage(message);
            demoApp.getFlightClaims();
        });
    }

    demoApp.approvalByInsurer = (claimData) => {
        console.log('Insurer Approval Invoked for: ');
        console.log(claimData.ref.txhash);
        $http.get(apiBaseURL + "insurer-approval?ref="+claimData.ref.txhash).then(function(response){
            console.log('Insurer Approval Result');
            console.log(response);
            var message = "Claim is approved successfully by Insurer. Following transaction id is committed to the ledger. "+ response.data.transactionId;
            console.log(message);
            demoApp.displayMessage(message);
            demoApp.getFlightClaims();
        });
    }

    demoApp.displayMessage = (message) => {
        console.log("Ticket Display Message");
        console.log(message);
        const modalInstanceTwo = $uibModal.open({
            templateUrl: 'messageContent.html',
            controller: 'messageCtrl',
            controllerAs: 'modalInstanceTwo',
            resolve: { message: () => message }
        });

        // No behaviour on close / dismiss.
        modalInstanceTwo.result.then(() => {}, () => {});
    };

    demoApp.openCreateTicketModal = () => {
        const modalInstance = $uibModal.open({
            templateUrl: 'createTicketModal.html',
            controller: 'CreateTicketCtrl',
            controllerAs: 'modalInstance',
            resolve: {
                demoApp: () => demoApp,
                apiBaseURL: () => apiBaseURL,
                peers: () => peers
            }
        });

        modalInstance.result.then(() => {}, () => {});
    };

    demoApp.openCreateTicketClaimModal = (tmpFlightTicket) => {

            console.log("openCreateTicketClaimModal- Shared tmpFlightTicket");
            console.log(tmpFlightTicket);

            sharedProperties.setProperty(tmpFlightTicket);

            const claimModalInstance = $uibModal.open({
                templateUrl: 'createTicketClaimModal.html',
                controller: 'CreateTicketClaimCtrl',
                controllerAs: 'claimModalInstance',
                resolve: {
                    demoApp: () => demoApp,
                    apiBaseURL: () => apiBaseURL,
                    peers: () => peers
                }
            });

            claimModalInstance.result.then(() => {}, () => {});
    };

    demoApp.getFlightTickets = function(){
        $http.get(apiBaseURL + "flight-tickets").then(
            function(response){
                console.log("Flight Ticket Response");
                console.log(response);
                demoApp.flightTickets =  response.data;
                console.log("Flight Ticket Data");
                console.log(demoApp.flightTickets);
            });
    }

    demoApp.getFlightClaims = function(){
            $http.get(apiBaseURL + "ticket-claims").then(
                function(response){
                    console.log("Ticket Claims Response");
                    console.log(response);

                    demoApp.flightClaims =  response.data;
                    console.log("Ticket Claims Data");
                    console.log(demoApp.flightClaims);
                });
        }

    demoApp.isAirline = function(){
        return demoApp.thisNode=="Qantas";
    }

    demoApp.isInsurer = function(){
        return demoApp.thisNode=="CBA";
    }

    demoApp.isPassenger = function(){
        return (demoApp.thisNode!="Qantas" && demoApp.thisNode!="CBA");
    }

    demoApp.getFlightTickets();
    demoApp.getFlightClaims();
    console.log("Flight Tickets Invoked");
});

app.controller('CreateTicketCtrl', function ($http, $location, $uibModalInstance, $uibModal, demoApp, apiBaseURL, peers) {

    const modalInstance = this;

    modalInstance.peers = peers;
    modalInstance.form = {};
    modalInstance.formError = false;

    // Validate and create IOU.
    modalInstance.create = () => {
        if (invalidFormInput()) {
            modalInstance.formError = true;
        } else {
            modalInstance.formError = false;

            $uibModalInstance.close();

            const createTicketEndpoint = `${apiBaseURL}create-ticket`;

            // Create PO and handle success / fail responses.
            $http.post(createTicketEndpoint, modalInstance.form).then(
                (result) => {
                    console.log("Result Details");
                    console.log(result);
                    console.log("Transaction Data Details");
                    console.log(result.data);
                    console.log("Transaction Id Details");
                    var message = "Ticket is created successfully. Following transaction id is committed to the ledger. "+ result.data.transactionId;
                    console.log(message);
                    modalInstance.displayMessage(message);
                    demoApp.getFlightTickets();

                },
                (result) => {
                    console.log("Error Message");
                    console.log(result);
                    modalInstance.displayMessage(result.data);
                }
            );
        }
    };

    modalInstance.displayMessage = (message) => {
        console.log("Ticket Display Message");
        console.log(message);
        const modalInstanceTwo = $uibModal.open({
            templateUrl: 'messageContent.html',
            controller: 'messageCtrl',
            controllerAs: 'modalInstanceTwo',
            resolve: { message: () => message }
        });

        // No behaviour on close / dismiss.
        modalInstanceTwo.result.then(() => {}, () => {});
    };

    // Close create IOU modal dialogue.
    modalInstance.cancel = () => $uibModalInstance.dismiss();

    // Validate the Ticket.
    function invalidFormInput() {
        var status = false
        if(modalInstance.form.airlineName === undefined){
           status = true
        }
        else if(modalInstance.form.flightInsurerName === undefined){
           status = true
        }
        else if(modalInstance.form.ticketNo === undefined){
           status = true
        }
        else if(modalInstance.form.passportNo === undefined){
           status = true
        }
        else if(modalInstance.form.passportIssueDate === undefined){
           status = true
        }
        else if(modalInstance.form.passportExpiryDate === undefined){
           status = true
        }
        else if(modalInstance.form.passengerName === undefined){
           status = true
        }
        else if(modalInstance.form.passengerCount === undefined){
           status = true
        }
        else if(modalInstance.form.amount === undefined){
           status = true
        }

        return status;

    }
});

app.controller('CreateTicketClaimCtrl', function ($http, $location, $uibModalInstance, $uibModal, demoApp, apiBaseURL, peers, sharedProperties) {
    const claimModalInstance = this;

    console.log("CreateTicketClaimCtrl - Shared flientTicketDetail");
    console.log(sharedProperties.getProperty());

    var claimAmount = sharedProperties.getProperty().state.data.props.amount.split(" ")[0];
    var claimCurrency = sharedProperties.getProperty().state.data.props.amount.split(" ")[1];
    var arrAirline = sharedProperties.getProperty().state.data.airline.split(",");
    var airlineName = arrAirline[2].split("=")[1];
    var arrInsurer = sharedProperties.getProperty().state.data.flightInsurer.split(",");
    var insurerName = arrInsurer[2].split("=")[1];

    claimModalInstance.peers = peers;
    claimModalInstance.form = {flightTicketNo: sharedProperties.getProperty().state.data.props.ticketNo,
    passengerName: sharedProperties.getProperty().state.data.props.passengerName,
    claimAmount: parseInt(claimAmount),
    airlineName: airlineName,
    flightInsurerName: insurerName,
    currency: claimCurrency,
    flightTicketTx: sharedProperties.getProperty().ref.txhash};

    console.log("CreateTicketClaimCtrl - Form");
    console.log(claimModalInstance.form);

    claimModalInstance.formError = false;

     // Validate and create Claim
        claimModalInstance.create = () => {
            if (invalidFormInput()) {
                claimModalInstance.formError = true;
            } else {
                claimModalInstance.formError = false;

                $uibModalInstance.close();

                const createTicketEndpoint = `${apiBaseURL}create-claims`;

                // Create Claim and handle success / fail responses.
                $http.post(createTicketEndpoint, claimModalInstance.form).then(
                    (result) => {
                        console.log("Result Details");
                        console.log(result);
                        console.log("Transaction Data Details");
                        console.log(result.data);
                        console.log("Transaction Id Details");
                        var message = "Claim is created successfully. Following transaction id is committed to the ledger. "+ result.data.transactionId;
                        console.log(message);
                        claimModalInstance.displayMessage(message);
                        demoApp.getFlightClaims();
                        demoApp.getFlightTickets();
                    },
                    (result) => {
                        console.log("Error Message");
                        console.log(result);
                        claimModalInstance.displayMessage(result.data);
                    }
                );
            }
        };

        claimModalInstance.displayMessage = (message) => {
            console.log("Claim Display Message");
            console.log(message);

            const modalInstanceTwo = $uibModal.open({
                templateUrl: 'messageContent.html',
                controller: 'messageCtrl',
                controllerAs: 'modalInstanceTwo',
                resolve: { message: () => message }
            });

            // No behaviour on close / dismiss.
            modalInstanceTwo.result.then(() => {}, () => {});
        };

        // Close create modal dialogue.
        claimModalInstance.cancel = () => $uibModalInstance.dismiss();

        // Validate the Ticket.
        function invalidFormInput() {
            var status = false
            if(claimModalInstance.form.airlineName === undefined){
               status = true
            }

            return status;

        }
});

// Controller for success/fail modal dialogue.
app.controller('messageCtrl', function ($uibModalInstance, message) {
    const modalInstanceTwo = this;
    console.log("MessageCtrl");
    console.log(message);

    modalInstanceTwo.message = message;
});