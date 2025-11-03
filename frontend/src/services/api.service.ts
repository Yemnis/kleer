import type { ExchangeRatesResponse, ConversionResponse } from '../types/currency.types';

const API_BASE_URL = 'http://localhost:8080/api';

export class ApiService {
  static async getLatestRates(): Promise<ExchangeRatesResponse> {
    const response = await fetch(`${API_BASE_URL}/rates/latest`);
    
    if (!response.ok) {
      throw new Error('Failed to fetch exchange rates');
    }
    
    return response.json();
  }

  static async refreshRates(): Promise<ExchangeRatesResponse> {
    const response = await fetch(`${API_BASE_URL}/rates/refresh`, {
      method: 'POST',
    });
    
    if (!response.ok) {
      throw new Error('Failed to refresh exchange rates');
    }
    
    return response.json();
  }

  static async convertCurrency(
    amount: number,
    from: string,
    to: string
  ): Promise<ConversionResponse> {
    const response = await fetch(
      `${API_BASE_URL}/convert?amount=${amount}&from=${from}&to=${to}`
    );
    
    if (!response.ok) {
      throw new Error('Failed to convert currency');
    }
    
    return response.json();
  }
}

