# Currency Exchange Frontend

A modern, beautiful currency exchange application built with React, Vite, and TypeScript.

## Features

- 💱 Currency conversion between SEK, EUR, and USD
- 🎨 Beautiful, modern UI with smooth animations
- 📱 Fully responsive design
- ⚡ Real-time conversion as you type
- 🔄 Swap currencies with one click
- 🔃 Refresh exchange rates from Riksbank API
- 🎯 Type-safe with TypeScript

## Tech Stack

- **React 18** - UI library
- **TypeScript** - Type safety
- **Vite** - Fast build tool and dev server
- **CSS3** - Modern styling with animations

## Getting Started

### Prerequisites

- Node.js 18+ and npm

### Installation

```bash
# Install dependencies
npm install
```

### Development

```bash
# Start development server (default: http://localhost:5173)
npm run dev
```

### Build

```bash
# Build for production
npm run build

# Preview production build
npm run preview
```

## Project Structure

```
frontend/
├── src/
│   ├── components/
│   │   ├── CurrencyExchange.tsx    # Main currency exchange component
│   │   └── CurrencyExchange.css    # Component styles
│   ├── services/
│   │   └── api.service.ts          # API integration service
│   ├── types/
│   │   └── currency.types.ts       # TypeScript type definitions
│   ├── App.tsx                     # Root component
│   ├── App.css                     # App styles
│   ├── main.tsx                    # Entry point
│   └── index.css                   # Global styles
├── public/                         # Static assets
├── package.json                    # Dependencies
├── tsconfig.json                   # TypeScript config
└── vite.config.ts                  # Vite config
```

## API Integration

The frontend connects to the Java backend at `http://localhost:8080/api` with the following endpoints:

- `GET /api/rates/latest` - Get latest exchange rates
- `POST /api/rates/refresh` - Refresh rates from Riksbank
- `GET /api/convert?amount={amount}&from={from}&to={to}` - Convert currency

## Scripts

- `npm run dev` - Start development server
- `npm run build` - Build for production
- `npm run preview` - Preview production build
- `npm run lint` - Run ESLint

## Supported Currencies

- 🇸🇪 SEK - Svenska kronor
- 🇪🇺 EUR - Euro
- 🇺🇸 USD - US Dollar

## License

MIT
