## Sanlam Technical Project
### Summary
The basic goal of this project was to showcase a solution where withdrawals could be executed against and account.

In addition to the basic requirement, focus should be given to significantly enhance its:
- structure 
- throughput
- maintainability
- scalability
- flexibility
- consistency
- fault
tolerance
- testability
- dependency management
- observability
- auditability
- portability
- correctness,
- cost efficiency and
- overall quality, while preserving the existing business functionality

### Implementation
In implementing and researching the best approach for this, I realised that several fundamental requirements for a financial system were not addressed, and the changes I implemented reflect this.

#### Notes
A system tracking transactions has to have several things to be useful and correct. Among these is double entry bookkeeping to keep to accounting practices, immutable transactions and the correct classes to model the process.

To this end, I added endpoints to get information about an account, deposit and withdraw, and check a transaction status.

I have made an assumption for this project that all transactions are cash based and therefore not between accounts. Therefore a "cash-on-hand" account is created in Tigerbeetle as a liability type account.

#### Structure, maintainability and testability
Using the SOLID principles paradigm, I implemented distinct classes for 
- repositories (speaking to the datasource)
- services (interfacing between the datasources or external clients - where the business logic lives)
- models (classes representing domain objects)

By implementing this structure, I have ensured that all units of work are testable. No tests were implemented as the spec mentioned that these are not expected. In a general situation, I would have used a test driven approach to developing the solution.

#### Throughput, auditability, fault tolerance, correctness
While implementing this solution, I had an interview with Natu where he mentioned a project called [TigerBeetle](https://tigerbeetle.com/). TigerBeetle is designed from the ground-up for high-throughput, fault-tolerant transactions that enforces financial paradigms such as double-entry bookkeeping and auditability.

I have implemented this by having a "global" database that tracks data about the accounts and transactions, and having the tigerbeetle cluster to manage the transactional side of things.

Tigerbeetle is responsible for tracking an accounts balances and ensuring a transaction is allowed. Everything else is tracked in the general global database.

The technical specifics are as follows:
Transactions received are stored in the general database with a PENDING flag. Every 50ms, a scheduled task is executed that executes the transactions that are pending. This is done in order to make sure that the bottlenecks which are the controller and the database are removed.

While stress-testing, I found that the biggest-bottleneck was publishing to the SNS topic. Therefore I setup an ExecutorPool to send the notifications asynchronously. Perhaps better would be to have an external service that would be monitoring the database and sending the notifications when necessary in order to ensure that these two requirements are not tightly coupled. 

In a real world scenario, a withdrawal or deposit would be sent via a POST request, and the transaction then polled with a GET request to the `transaction` endpoint until either a timeout or a response. From a human's perspective this would be virtually instant.

#### Observability
Observability was not implemented in this solution as much of it would have extended this over the deadline. I suggest the following to be done in a real-world situation
- Open-telemetry added to all endpoints and service methods
- A solution such as grafana set up that will track all metrics to ensure things are running correctly, such as time taken to process transactions, up-time
- This would be connected to an alerting system to ensure that when things go wrong, the correct people are alerted.

### Prerequisites
Localstack for the SNS queue. The settings for this can be configured in the application.properties file.
To run the stress test, k6 must be installed - see the guide [here](https://grafana.com/docs/k6/latest/get-started/running-k6/)

### Running
All commands are executed in powershell.

Before every run, the tigerbeetle cluster needs to be formatted and created:

```./tigerbeetle format --cluster=0 --replica=0 --replica-count=1 0_0.tigerbeetle; ./tigerbeetle start --addresses=3000 0_0.tigerbeetle```

The project can then be started either in an IDE or with ```gradle bootRun```

The stress test can be run with k6

```k6 run script.js```