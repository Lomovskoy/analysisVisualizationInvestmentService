package com.analysis.visualization.investment.service;

import com.analysis.visualization.investment.dto.PortfolioDto;
import com.analysis.visualization.investment.dto.StockDto;
import com.analysis.visualization.investment.entity.Portfolio;
import com.analysis.visualization.investment.entity.Stock;
import com.analysis.visualization.investment.repository.PortfolioRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import yahoofinance.YahooFinance;

import java.io.IOException;
import java.util.*;

@Service
@AllArgsConstructor
public class PortfolioService {

    private PortfolioRepository portfolioRepository;

    public PortfolioDto save(String text) throws IOException {
        text = text.replaceAll("�", "");
        var arrStr = text.split("\r\n");

        var stocks = new ArrayList<Stock>();
        var id = UUID.randomUUID();

        for (var str : arrStr) {
            var arr = str.split("\t");
            stocks.add(new Stock(id, arr[0], Integer.valueOf(arr[1])));
        }

        var portfolio = new Portfolio(id, stocks);

        portfolioRepository.save(portfolio);

        var tickers = portfolio.getStocks().stream().map(Stock::getTicker).toArray(String[]::new);
        var stocksYahooFinance = YahooFinance.get(tickers);

        var stockDtos = new ArrayList<StockDto>();
        for (var stock : stocksYahooFinance.entrySet()) {
            var count = stocks.stream()
                    .filter(s -> s.getTicker().equals(stock.getKey()))
                    .findFirst().get().getCount();
            if (stock.getValue().getQuote().getAsk() != null) {
                stockDtos.add(new StockDto(stock.getKey(), stock.getValue().getQuote().getAsk().doubleValue() * count));
            } else {
                System.out.println(stock.getKey());
            }
        }
        return new PortfolioDto(id, stockDtos);
    }
}
