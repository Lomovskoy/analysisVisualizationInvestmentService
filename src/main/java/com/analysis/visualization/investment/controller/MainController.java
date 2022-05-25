package com.analysis.visualization.investment.controller;

import com.analysis.visualization.investment.service.PortfolioService;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.Map;

@Controller
@AllArgsConstructor
public class MainController {

    private PortfolioService portfolioService;


    @GetMapping("/greeting")
    public String greeting(@RequestParam(name="name", required=false, defaultValue="World")
                           String name, Map<String, Object> model) {
        model.put("name", name);
        return "greeting";
    }

    @GetMapping
    public String main(Map<String, Object> model) {
        model.put("some", "hello, letsCode!");
        return "main";
    }

    @PostMapping(value = "/save", produces = MediaType.ALL_VALUE + "; charset=utf-8")
    public String save(Map<String, Object> model, @RequestParam String text) throws IOException {
        var portfolioId = portfolioService.save(text);
        var portfolioPieChartDto = portfolioService.buildPieChart(portfolioId);
        var portfolioAreaChartDto = portfolioService.buildAreaChart(portfolioId);


        model.put("id", "portfolio id - " + portfolioId);
        model.put("pieCharts", portfolioPieChartDto.getTicker());
        model.put("areaChart", portfolioAreaChartDto.getTicker());
        return "charts";
    }
}
