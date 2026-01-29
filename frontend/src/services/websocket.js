import SockJS from 'sockjs-client'
import {Client} from '@stomp/stompjs'

class WebSocketService {
    constructor() {
        this.client = null;
        this.subscriptions = new Map();
        this.reconnectAttempts = 0;
        this.maxReconnectAttempts = 5;
        this.connectionPromise = null; // Thêm promise để đợi connection
        this.connectionInfo = null; // Lưu thông tin connection hiện tại
        this.isConnecting = false;
    }

    connect(userId, role, onConnect, onError) {
        // Nếu đã connect với cùng user, không connect lại
        if (this.client && this.client.connected && 
            this.connectionInfo?.userId === userId && 
            this.connectionInfo?.role === role) {
            console.log('Already connected with same user, reusing connection');
            if (onConnect) onConnect();
            return Promise.resolve();
        }

        // Nếu đang connect, đợi connection hiện tại hoàn thành
        if (this.isConnecting) {
            console.log('Connection in progress, waiting...');
            return this.connectionPromise;
        }

        // Disconnect connection cũ nếu khác user
        if (this.client && this.client.connected) {
            console.log('Disconnecting old connection...');
            this.disconnect();
        }

        this.isConnecting = true;
        this.connectionInfo = { userId, role };

        const socketUrl = `${import.meta.env.VITE_API_URL || 'http://localhost:8080'}/ws?userId=${userId}&role=${role}`;

        this.connectionPromise = new Promise((resolve, reject) => {
            this.client = new Client({
                webSocketFactory: () => new SockJS(socketUrl),
                reconnectDelay: 5000,
                heartbeatIncoming: 4000,
                heartbeatOutgoing: 4000,

                onConnect: (frame) => {
                    console.log('Connected to WebSocket');
                    this.reconnectAttempts = 0;
                    this.isConnecting = false;
                    if (onConnect) onConnect(frame);
                    resolve(frame);
                },

                onStompError: (frame) => {
                    console.error('STOMP error:', frame.headers['message']);
                    this.isConnecting = false;
                    if (onError) onError(frame);
                    reject(frame);
                },

                onWebSocketError: (error) => {
                    console.error('WebSocket error:', error);
                    this.isConnecting = false;
                    this.handleReconnect();
                    reject(error);
                },

                onDisconnect: () => {
                    console.log('Disconnected from WebSocket');
                }
            });

            this.client.activate();
        });

        return this.connectionPromise;
    }

    handleReconnect() {
        if (this.reconnectAttempts < this.maxReconnectAttempts) {
            this.reconnectAttempts++;
            setTimeout(() => {
                console.log(`Reconnecting attempt ${this.reconnectAttempts}...`);
                if (this.connectionInfo) {
                    this.connect(
                        this.connectionInfo.userId,
                        this.connectionInfo.role
                    );
                }
            }, 1000 * this.reconnectAttempts); // Tăng dần thời gian reconnect
        }
    }

    async subscribe(destination, callback) {
        // Đợi connection hoàn thành nếu đang connect
        if (this.isConnecting && this.connectionPromise) {
            await this.connectionPromise;
        }

        if (!this.client || !this.client.connected) {
            console.error('Client not connected');
            return null;
        }

        // Nếu đã subscribe destination này rồi, unsubscribe trước
        if (this.subscriptions.has(destination)) {
            console.log(`Already subscribed to ${destination}, resubscribing...`);
            this.unsubscribe(destination);
        }

        console.log(`Subscribing to ${destination}`);
        const subscription = this.client.subscribe(destination, (message) => {
            try {
                const body = JSON.parse(message.body);
                callback(body);
            } catch (e) {
                console.error('Error parsing message:', e);
                callback(message.body);
            }
        });

        this.subscriptions.set(destination, subscription);
        return subscription;
    }

    unsubscribe(destination) {
        const subscription = this.subscriptions.get(destination);

        if (subscription) {
            subscription.unsubscribe();
            this.subscriptions.delete(destination);
            console.log(`Unsubscribed from ${destination}`);
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
        });

        return true;
    }

    // QUAN TRỌNG: Chỉ disconnect khi thực sự cần (logout, close app)
    disconnect(force = false) {
        if (!force) {
            console.warn('disconnect() called but not forcing - connection will be kept alive');
            return;
        }

        if (this.client) {
            console.log('Force disconnecting WebSocket');
            // Hủy tất cả subscriptions
            this.subscriptions.forEach((sub, destination) => {
                sub.unsubscribe();
            });
            this.subscriptions.clear();

            this.client.deactivate();
            this.client = null;
            this.connectionInfo = null;
            this.connectionPromise = null;
        }
    }

    isConnected() {
        return this.client && this.client.connected;
    }
}

export default new WebSocketService();