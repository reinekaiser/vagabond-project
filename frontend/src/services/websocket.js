import SockJS from 'sockjs-client'
import {Client} from '@stomp/stompjs'

class WebSocketService {
    constructor() {
        this.client = null
        this.subscriptions = new Map();
        this.reconnectAttempts = 0
        this.maxReconnectAttempts = 5
    }

    connect(userId, role, onConnect, onError) {
        const socketUrl = `${import.meta.env.VITE_API_URL || 'http://localhost:8080'}/ws?userId=${userId}&role=${role}`;

        this.client = new Client({
            webSocketFactory: () => new SockJS(socketUrl),
            reconnectDelay: 5000,
            heartbeatIncoming: 4000,
            heartbeatOutgoing: 4000,

            onConnect: (frame) => {
                console.log('Connected to WebSocket');
                this.reconnectAttempts = 0;
                if (onConnect) onConnect(frame);
            },

            onStompError: (frame) => {
                console.error('STOMP error:', frame.headers['message']);
                if (onError) onError(frame);
            },

            onWebSocketError: (error) => {
                console.error('WebSocket error:', error);
                this.handleReconnect();
            },

            onDisconnect: () => {
                console.log('Disconnected from WebSocket');
            }
        });

        this.client.activate()
    }

    handleReconnect() {
        if (this.reconnectAttempts < this.maxReconnectAttempts) {
            this.reconnectAttempts++
            setTimeout(() => {
                console.log(`Reconnecting attempt ${this.reconnectAttempts}...`);
                this.client.activate();
            })
        }
    }

    subscribe(destination, callback) {
        if (!this.client || !this.client.connected) {
            console.error('Client not connected');
            return null;
        }

        const subscription = this.client.subscribe(destination, (message) => {
            try {
                const body = JSON.parse(message.body)
                callback(body)
            } catch (e) {
                console.error('Error parsing message:', e);
                callback(message.body);
            }
        })

        this.subscriptions.set(destination, subscription);
        return subscription;
    }

    unsubscribe(destination) {
        const subscription = this.subscriptions.get(destination)

        if (subscription) {
            subscription.unsubscribe()
            this.subscriptions.delete(destination)
        }
    }

    send(destination, body) {
        if (!this.client || !this.client.connected) {
            console.error('Cannot send message: client not connected');
            return false;
        }

        this.client.publish({
            destination: `/app${destination}`,
            body: JSON.stringify(body)
        })

        return true
    }

    disconnect() {
        if (this.client) {
            // Hủy tất cả subscriptions
            this.subscriptions.forEach((sub, destination) => {
                sub.unsubscribe();
            });
            this.subscriptions.clear();

            this.client.deactivate();
        }
    }

    isConnected() {
        return this.client && this.client.connected;
    }
}

export default new WebSocketService();