import { useState, useEffect } from 'react';
import type { Currency, CurrencyOption, ConversionResponse } from '../types/currency.types';
import { ApiService } from '../services/api.service';
import './CurrencyExchange.css';

const CURRENCY_OPTIONS: CurrencyOption[] = [
  { value: 'SEK', label: 'SEK - Svenska kronor', flag: '🇸🇪' },
  { value: 'EUR', label: 'EUR - Euro', flag: '🇪🇺' },
  { value: 'USD', label: 'USD - US Dollar', flag: '🇺🇸' },
];

export const CurrencyExchange = () => {
  const [amount, setAmount] = useState<string>('100');
  const [fromCurrency, setFromCurrency] = useState<Currency>('SEK');
  const [toCurrency, setToCurrency] = useState<Currency>('EUR');
  const [result, setResult] = useState<ConversionResponse | null>(null);
  const [lastUpdated, setLastUpdated] = useState<string>('');
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string>('');

  useEffect(() => {
    loadInitialData();
  }, []);

  useEffect(() => {
    if (amount && parseFloat(amount) > 0) {
      convertCurrency();
    }
  }, [amount, fromCurrency, toCurrency]);

  const loadInitialData = async () => {
    try {
      const rates = await ApiService.getLatestRates();
      setLastUpdated(rates.lastUpdated);
    } catch (err) {
      setError('Kunde inte hämta växelkurser. Kontrollera att backend är igång.');
      console.error(err);
    }
  };

  const convertCurrency = async () => {
    const numAmount = parseFloat(amount);
    if (!numAmount || numAmount <= 0) {
      setResult(null);
      return;
    }

    try {
      const conversionResult = await ApiService.convertCurrency(
        numAmount,
        fromCurrency,
        toCurrency
      );
      setResult(conversionResult);
      setError('');
    } catch (err) {
      setError('Kunde inte växla valuta.');
      console.error(err);
    }
  };

  const handleSwapCurrencies = () => {
    setFromCurrency(toCurrency);
    setToCurrency(fromCurrency);
  };

  const handleRefreshRates = async () => {
    setLoading(true);
    try {
      const rates = await ApiService.refreshRates();
      setLastUpdated(rates.lastUpdated);
      setError('');
      alert('✅ Växelkurser uppdaterade!');
      convertCurrency();
    } catch (err) {
      setError('Kunde inte uppdatera växelkurser.');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const formatAmount = (value: number, currency: Currency): string => {
    const symbols: Record<Currency, string> = {
      SEK: 'kr',
      EUR: '€',
      USD: '$',
    };

    const formatted = value.toFixed(2);
    const symbol = symbols[currency];

    return currency === 'USD' ? `${symbol}${formatted}` : `${formatted} ${symbol}`;
  };

  const formatLastUpdated = (timestamp: string): string => {
    if (!timestamp) return '--';
    const date = new Date(timestamp);
    return date.toLocaleString('sv-SE', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  return (
    <div className="container">
      <div className="card">
        <div className="header">
          <h1>Valutaväxling</h1>
          <p className="subtitle">Växla mellan SEK, EUR och USD</p>
        </div>

        <div className="exchange-form">
          <div className="input-group">
            <label htmlFor="amount">Belopp</label>
            <input
              type="number"
              id="amount"
              placeholder="0.00"
              value={amount}
              onChange={(e) => setAmount(e.target.value)}
              step="0.01"
              min="0"
            />
          </div>

          <div className="currency-row">
            <div className="currency-select-group">
              <label htmlFor="fromCurrency">Från</label>
              <div className="select-wrapper">
                <select
                  id="fromCurrency"
                  className="currency-select"
                  value={fromCurrency}
                  onChange={(e) => setFromCurrency(e.target.value as Currency)}
                >
                  {CURRENCY_OPTIONS.map((option) => (
                    <option key={option.value} value={option.value}>
                      {option.flag} {option.label}
                    </option>
                  ))}
                </select>
              </div>
            </div>

            <button
              className="swap-button"
              onClick={handleSwapCurrencies}
              title="Byt valutor"
            >
              <svg
                width="24"
                height="24"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
              >
                <path d="M7 16V4M7 4L3 8M7 4L11 8"></path>
                <path d="M17 8V20M17 20L21 16M17 20L13 16"></path>
              </svg>
            </button>

            <div className="currency-select-group">
              <label htmlFor="toCurrency">Till</label>
              <div className="select-wrapper">
                <select
                  id="toCurrency"
                  className="currency-select"
                  value={toCurrency}
                  onChange={(e) => setToCurrency(e.target.value as Currency)}
                >
                  {CURRENCY_OPTIONS.map((option) => (
                    <option key={option.value} value={option.value}>
                      {option.flag} {option.label}
                    </option>
                  ))}
                </select>
              </div>
            </div>
          </div>

          <div className="result">
            <div className="result-amount">
              {result ? formatAmount(result.convertedAmount, toCurrency) : '0.00'}
            </div>
            <div className="exchange-rate">
              {result
                ? `Växelkurs: 1 ${fromCurrency} = ${result.rate.toFixed(4)} ${toCurrency}`
                : 'Växelkurs: --'}
            </div>
            <div className="last-updated">
              Senast uppdaterad: {formatLastUpdated(lastUpdated)}
            </div>
          </div>

          <button
            className="refresh-button"
            onClick={handleRefreshRates}
            disabled={loading}
          >
            <svg
              width="16"
              height="16"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="2"
              className={loading ? 'spinning' : ''}
            >
              <path d="M21.5 2v6h-6M2.5 22v-6h6M2 11.5a10 10 0 0 1 18.8-4.3M22 12.5a10 10 0 0 1-18.8 4.2"></path>
            </svg>
            {loading ? 'Uppdaterar...' : 'Uppdatera växelkurser'}
          </button>

          {error && (
            <div className="error-message">
               {error}
            </div>
          )}
        </div>

        <div className="info-box">
          <p>
            <strong>Information:</strong> Växelkurser hämtas från Riksbankens
            öppna API
          </p>
        </div>
      </div>
    </div>
  );
};

