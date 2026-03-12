import { Client } from "@stomp/stompjs";

let stompClient = null;
let connected = false;
let connectPromise = null;

export function connect() {
  if (connectPromise) {
    return connectPromise;
  }

  const token = localStorage.getItem("token");

  const wsProtocol = window.location.protocol === "https:" ? "wss:" : "ws:";
  const brokerURL = `${wsProtocol}//${window.location.host}/ws`;

  stompClient = new Client({
    brokerURL,
    connectHeaders: {
      Authorization: "Bearer " + token
    },
    reconnectDelay: 5000,

    onDisconnect: () => {
      console.log("WS DISCONNECTED");
      connected = false;
      connectPromise = null;
    }
  });

  connectPromise = new Promise((resolve) => {
    stompClient.onConnect = () => {
      console.log("WS CONNECTED ✅");
      connected = true;
      resolve();
    };
  });

  stompClient.activate();

  return connectPromise;
}

export async function subscribe(destination, callback) {
  await connectPromise;

  return stompClient.subscribe(destination, (message) => {
    const body = JSON.parse(message.body);
    callback(body);
  });
}

export function sendMessage(payload) {
  if (!connected) return;

  stompClient.publish({
    destination: "/app/chat",
    body: JSON.stringify(payload)
  });
}

export function sendTyping(payload) {
  if (!connected) return;
  stompClient.publish({
    destination: "/app/chat.typing",
    body: JSON.stringify(payload)
  });
}

/**
 * Notify the server that the current user has read messages sent by payload.senderId.
 * payload: { senderId: Number }
 */
export function sendRead(payload) {
  if (!connected) return;
  stompClient.publish({
    destination: "/app/chat.read",
    body: JSON.stringify(payload)
  });
}

/** payload: { messageId: Number, content: String } */
export function sendEdit(payload) {
  if (!connected) return;
  stompClient.publish({
    destination: "/app/chat.edit",
    body: JSON.stringify(payload)
  });
}

export function disconnect() {
  if (stompClient) {
    stompClient.deactivate();
    stompClient = null;
    connected = false;
    connectPromise = null;
    console.log("WS manually disconnected");
  }
}