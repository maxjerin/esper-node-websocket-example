var WebSocket = require('ws');

var wss = new WebSocket.Server({
    perMessageDeflate: false,
    port: 8080
});

var mdTimer;

wss.on('connection', function connection(ws) {
    console.log('WebSocket connected');

    ws.on('message', function incoming(data) {
        if (data === 'start') {
            console.log('Sending market data');
            mdTimer = setInterval(function () {
                var price = Math.random() * 5;
                ws.send(JSON.stringify({
                    ticker: 'IBM',
                    price: price,
                    timestamp: new Date()
                }));
            }, 1000);
        } else if (data === 'stop') {
            clearInterval(mdTimer);
            console.log('WebSocket disconnected');
        }
    });

    ws.on('close', function close() {
        clearInterval(mdTimer);
        console.log('WebSocket disconnected');
    });
});