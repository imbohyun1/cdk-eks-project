import React, { useState, useEffect } from 'react';
import './App.css';
import config from './config';


function App() {
  const [fortune, setFortune] = useState('');
  const [luckyNumbers, setLuckyNumbers] = useState('');
  const [isButtonDisabled, setIsButtonDisabled] = useState(false);

  useEffect(() => {
    fetchFortune();
  }, []);

  const fetchFortune = async () => {

    setIsButtonDisabled(true); // Disable button

    try {
      const response = await fetch(`${config.API_URL}/fortunes/random`);
      const data = await response.json();
      setFortune(data.message);
      // Display Lucky numbers with separator ','
      if (Array.isArray(data.luckyNumbers)) {
        setLuckyNumbers(data.luckyNumbers.join(', '));
      } else {
        setLuckyNumbers('');
      }
    } catch (error) {
      console.error('Error fetching fortune message:', error);
      setFortune('Error fetching fortune');
      setLuckyNumbers('');
    }  finally {
      setIsButtonDisabled(false); // Enable button
    }

    // setTimeout(() => {
    //   setIsButtonDisabled(false); // Enable button
    // }, 1000);
  };

  return (
      <div className="App">
        <header className="App-header">
          <h1>Fortune Cookie</h1>
          <p>{fortune}</p>
          {luckyNumbers && <p>Lucky Numbers: {luckyNumbers}</p>}
          <button
              onClick={fetchFortune}
              disabled={isButtonDisabled}
          >
            {isButtonDisabled ? 'Loading...' : 'Get New Fortune'}
          </button>
        </header>
      </div>
  );
}

export default App;