export type Currency = 'SEK' | 'EUR' | 'USD';

export interface ExchangeRate {
  fromCurrency: string;
  toCurrency: string;
  rate: number;
  lastUpdated: string;
}

export interface ExchangeRatesResponse {
  rates: ExchangeRate[];
  lastUpdated: string;
}

export interface ConversionResponse {
  convertedAmount: number;
  rate: number;
  fromCurrency: string;
  toCurrency: string;
  originalAmount: number;
}

export interface CurrencyOption {
  value: Currency;
  label: string;
  flag: string;
}

