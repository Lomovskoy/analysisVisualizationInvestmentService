package com.analysis.visualization.investment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Date;
import java.util.GregorianCalendar;

@Getter
@Setter
@AllArgsConstructor
public class StockAreaChartDto {

    private LocalDate date;

    private Double avgPrise;

    private Double indexAvgPrise;
}
