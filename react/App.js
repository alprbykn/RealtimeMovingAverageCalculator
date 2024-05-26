import React, { useEffect, useState, useRef } from 'react';

const App = () => {
  const [log, setLog] = useState([]);
  const [socket, setSocket] = useState(null);
  const intervalRef = useRef(null);

  useEffect(() => {
    return () => {
      disconnect(); // Ensure disconnect is called on unmount
    };
  }, []);

  const connect = () => {
    const ws = new WebSocket('ws://localhost:8090/quotes');

    ws.onopen = () => {
      console.log('Connected to server');
      setSocket(ws);
      startSendingQuotes(ws); // Pass the WebSocket instance
    };

    ws.onmessage = (event) => {
      console.log(event.data);
      const data = JSON.parse(event.data);
      const logEntry = `Moving Average: ${data.average_price.toFixed(2)}, Prices: ${data.prices}`;
      setLog(prevLog => [...prevLog, logEntry]);
    };

    ws.onclose = () => {
      console.log('Disconnected from server');
      setSocket(null);
      clearInterval(intervalRef.current); // Clear interval on close
    };

    ws.onerror = (error) => {
      console.error('WebSocket error:', error);
    };
  };

  const disconnect = () => {
    if (socket) {
      socket.close();
      setSocket(null);
    }
    if (intervalRef.current) {
      clearInterval(intervalRef.current); // Clear interval on disconnect
      intervalRef.current = null; // Ensure intervalRef is set to null
    }
  };

  const startSendingQuotes = (ws) => {
    const sendQuote = () => {
      if (ws.readyState === WebSocket.OPEN) {
        const randomNumber = Math.floor(Math.random() * 101);
        ws.send(JSON.stringify({ "price": randomNumber }));
      }
    };

    intervalRef.current = setInterval(sendQuote, 3000);
  };

  const handleConnect = () => {
    if (!socket || socket.readyState === WebSocket.CLOSED) {
      connect();
    }
  };

  const handleDisconnect = () => {
    disconnect();
  };

  return (
    <div>
      <h1>WebSocket Client For Realtime MA Calculator</h1>
      <div>
        <button onClick={handleConnect}>Connect</button>
        <button onClick={handleDisconnect}>Disconnect</button>
      </div>
      <div>
        {log.map((entry, index) => (
          <div key={index}>{entry}</div>
        ))}
      </div>
    </div>
  );
};

export default App;
