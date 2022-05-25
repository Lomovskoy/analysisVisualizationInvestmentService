package com.analysis.visualization.investment.service;

import com.analysis.visualization.investment.dto.PortfolioAreaChartDto;
import com.analysis.visualization.investment.dto.PortfolioPieChartDto;
import com.analysis.visualization.investment.dto.StockAreaChartDto;
import com.analysis.visualization.investment.dto.StockPieChartDto;
import com.analysis.visualization.investment.entity.Portfolio;
import com.analysis.visualization.investment.entity.Stock;
import com.analysis.visualization.investment.repository.PortfolioRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.Interval;

import java.io.IOException;
import java.time.ZoneId;
import java.util.*;

@Service
@AllArgsConstructor
public class PortfolioService {

    private PortfolioRepository portfolioRepository;

    public UUID save(String text) {
        text = text.replaceAll("�", "");
        var arrStr = text.split("\r\n");

        var stocks = new ArrayList<Stock>();
        var id = UUID.randomUUID();

        for (var str : arrStr) {
            var arr = str.split("\t");
            stocks.add(new Stock(id, arr[0], Integer.valueOf(arr[1])));
        }

        var portfolio = new Portfolio(id, stocks);

        return portfolioRepository.save(portfolio).getId();

    }

    public PortfolioPieChartDto buildPieChart(UUID id) throws IOException {
        var portfolio = portfolioRepository.findById(id).orElseThrow();
        var stocks = portfolio.getStocks();

        var tickers = portfolio.getStocks().stream().map(Stock::getTicker).toArray(String[]::new);
        var stocksYahooFinance = YahooFinance.get(tickers, true);

        var stockDtos = new ArrayList<StockPieChartDto>();
        for (var stock : stocksYahooFinance.entrySet()) {
            var count = stocks.stream()
                    .filter(s -> s.getTicker().equals(stock.getKey()))
                    .findFirst().get().getCount();
            if (stock.getValue().getQuote().getAsk() != null) {
                stockDtos.add(new StockPieChartDto(stock.getKey(), stock.getValue().getQuote().getAsk().doubleValue() * count));
            } else {
                System.out.println(stock.getKey());
            }
        }
        return new PortfolioPieChartDto(id, stockDtos);
    }

    public PortfolioAreaChartDto buildAreaChart(UUID id) throws IOException {
        var portfolio = portfolioRepository.findById(id).orElseThrow();

        Calendar from = Calendar.getInstance();
        Calendar to = Calendar.getInstance();
        from.add(Calendar.YEAR, -5); // from 5 years ago

        var google = YahooFinance.get("GOOG", from, to, Interval.WEEKLY);

        var stockDtos = new ArrayList<StockAreaChartDto>();

        for (var history : google.getHistory()) {
            var date = history.getDate().getTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            var avgPrise = (history.getOpen().doubleValue() + history.getClose().doubleValue()) / 2;
            stockDtos.add(new StockAreaChartDto(date, avgPrise));
        }

        return new PortfolioAreaChartDto(id, stockDtos);
    }
}
