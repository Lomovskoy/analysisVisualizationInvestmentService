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
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class PortfolioService {

    private final int yearsAgo = -12;
    private PortfolioRepository portfolioRepository;

    public UUID save(String text) {
        text = text.replaceAll("�", "");
        var arrStr = text.split("\r\n");

        var stocks = new ArrayList<Stock>();
        var id = UUID.randomUUID();

        for (var str : arrStr) {
            var arr = str.split("\t");
            stocks.add(new Stock(id, arr[0].replace(".", "-"), Integer.valueOf(arr[1])));
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
                    .findFirst().orElseThrow().getCount();
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
        var symbols = portfolio.getStocks().stream().map(Stock::getTicker).toArray(String[]::new);

        var tickerCountMap = portfolio.getStocks().stream()
                .collect(Collectors.toMap(Stock::getTicker, Stock::getCount));

        Calendar from = Calendar.getInstance();
        Calendar to = Calendar.getInstance();
        from.add(Calendar.YEAR, yearsAgo); // from 5 years ago

        var stocksYahooFinance = YahooFinance.get(symbols, from, to, Interval.WEEKLY);

        int i = 0;
        var datePriseMap = new LinkedHashMap<LocalDate, Double>();

        for (var entrySet : stocksYahooFinance.entrySet()) {
            // приделай пропрции иначе акции во всем портфеде 1 к 1
            var count = tickerCountMap.get(entrySet.getKey());
            for (var history : entrySet.getValue().getHistory()) {
                var date = history.getDate().getTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                var avgPrise = (history.getOpen().doubleValue() + history.getClose().doubleValue()) * count / 2 ;
                if (i == 0) {
                    datePriseMap.put(date, avgPrise);
                } else {
                    if (datePriseMap.get(date) != null) {
                        datePriseMap.put(date, datePriseMap.get(date) + avgPrise);
                    }
                }

            }
            i++;
        }

        var stockDtos = new ArrayList<StockAreaChartDto>();
        for (var stock : datePriseMap.entrySet()) {
            stockDtos.add(new StockAreaChartDto(stock.getKey(), stock.getValue(), null));
        }


        // приведение к нулю
        var firstAvgPrise = stockDtos.get(0).getAvgPrise();

        for (var stock : stockDtos) {
            stock.setAvgPrise(stock.getAvgPrise() - firstAvgPrise);
        }

        return new PortfolioAreaChartDto(id, stockDtos, firstAvgPrise);
    }

    public void buildIndexAreaChart(String ticker, PortfolioAreaChartDto portfolioAreaChartDto) throws IOException {

        Calendar from = Calendar.getInstance();
        Calendar to = Calendar.getInstance();
        from.add(Calendar.YEAR, yearsAgo); // from 5 years ago

        var stocksYahooFinance = YahooFinance.get(ticker, from, to, Interval.WEEKLY);

        var datePriseMap = new LinkedHashMap<LocalDate, Double>();
        // Уравнивание цен активов для сравненеия
        var factor = getFactor(portfolioAreaChartDto, stocksYahooFinance);

        for (var history : stocksYahooFinance.getHistory()) {
            var date = history.getDate().getTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            var avgPrise = (history.getOpen().doubleValue() + history.getClose().doubleValue()) / 2 ;
            avgPrise = avgPrise * factor;
            datePriseMap.put(date, avgPrise);
        }

        int i = 0;
        for (var stock : datePriseMap.entrySet()) {
            portfolioAreaChartDto.getTicker().get(i++).setIndexAvgPrise(stock.getValue());
        }

        // приведение к нулю
        var firstAvgPrise = portfolioAreaChartDto.getTicker().get(0).getIndexAvgPrise();

        for (var stock : portfolioAreaChartDto.getTicker()) {
            stock.setIndexAvgPrise(stock.getIndexAvgPrise() - firstAvgPrise);
        }
    }

    private double getFactor(PortfolioAreaChartDto portfolioAreaChartDto, yahoofinance.Stock stocksYahooFinance) throws IOException {
        var avgPrise = (stocksYahooFinance.getHistory().get(0).getOpen().doubleValue() +
                stocksYahooFinance.getHistory().get(0).getClose().doubleValue()) / 2 ;
        return (portfolioAreaChartDto.getFirstAvgPrise() / avgPrise);
    }
}
