import http from 'k6/http';
import { sleep } from 'k6';

export const options = {
  // A number specifying the number of VUs to run concurrently.
  vus: 10,
  // A string specifying the total duration of the test run.
  duration: '30s',
};

export function setup() {
  for (let i = 1; i <= 5; i++) {
    http.post(`http://localhost:8080/bank/deposit?accountId=${i}&amount=100000000.00`);
  }
  sleep(1)
}

// The function that defines VU logic.
//
// See https://grafana.com/docs/k6/latest/examples/get-started-with-k6/ to learn more
// about authoring k6 scripts.
//
export default function() {
  const rndAccount = Math.floor(Math.random() * 5) + 1
  const rndAmount = Math.floor(Math.random() * 1000) + 1

  http.post(`http://localhost:8080/bank/withdraw?accountId=${rndAccount}&amount=${rndAmount}`);
}