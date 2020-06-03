# Coinamrketcap Adapter

API adapting communication to Coinmarketcap cryptocurrency platform

Sample requests:

```http
    GET /currencies/BTC HTTP/1.1
    Host: localhost:8080
```

```http
    GET /currencies/BTC?filter=USDT&filter=ETH&filter=XRP&filter=XLM HTTP/1.1
    Host: localhost:8080
```

```http
    POST /currencies/exchange HTTP/1.1
    Host: localhost:8080
    Content-Type: application/json
    
    {
        "from": "BTC",
        "to": [
            "XLM",
            "ETH",
            "USDT"
        ],
        "amount": "123433"
    }
```