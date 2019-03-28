![Corda](https://www.corda.net/wp-content/uploads/2016/11/fg005_corda_b.png)

# CorDapp Helloworld (Travel)

This CorDapp allows nodes to engage in a flight travel insurance use case. Players are Airline, Insurer and Passengers.

# Pre-requisites:
  
See https://docs.corda.net/getting-set-up.html.

## Interacting with the nodes:

You should interact with this CorDapp using the web front-end. Each node exposes this front-end on a different address:

* Passenger Simon: `localhost:10007/web/travel`
* Passenger Munesh: `localhost:10016/web/travel`
* Airline Qantas: `localhost:10010/web/travel`
* Flight Insurer CBA: `localhost:10013/web/travel`

When using the front-end:

1. Start by creating flight ticket by one of the passengers by selecting airline and travel insurer
2. Passenger raises claim request
3. Airline Qantas approves the ticket claim request 
4. Flight insurer CBA approves the ticket claim request



// root cathalog
----- 1 -----
 gradle clean deploynodes

 as a result
  cd kotlin-source/build/nodes
 ./ runnodes

