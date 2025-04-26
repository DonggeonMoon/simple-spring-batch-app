package com.dgmoonlabs.simplespringbatchapp.config;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class JobConfig {
    private static final String[] SYMBOLS = {"005930", // 삼성전자
            "000660", // SK하이닉스
            "373220", // LG에너지솔루션
            "207940", // 삼성바이오로직스
            "005380", // 현대차
            "012450", // 한화에어로스페이스
            "068270", // 셀트리온
            "000270", // 기아
            "105560" // KB금융
    };
    public static final String URL = "https://fchart.stock.naver.com/sise.nhn?symbol=%s&timeframe=day&count=3000&requestType=0";
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final RestTemplate restTemplate;


    @Bean
    public Job stockPriceJob() {
        return new JobBuilder("job", jobRepository).start(step()).build();
    }

    @Bean
    public Step step() {
        return new StepBuilder("step", jobRepository).tasklet(tasklet(), transactionManager).build();
    }

    @Bean
    public Tasklet tasklet() {
        return (contribution, chunkContext) -> {
            String lastDate = getLastBusinessDay();

            FileWriter fileWriter = new FileWriter(String.format("output/stock-price.%s.txt", lastDate));
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            StringBuilder data = new StringBuilder(20);
            for (String symbol : SYMBOLS) {
                ResponseEntity<String> response = restTemplate.getForEntity(String.format(URL, symbol), String.class);
                log.info("lastDate: {}", lastDate);

                Pattern rowPattern = Pattern.compile(lastDate + "\\|[0-9]+\\|[0-9]+\\|[0-9]+\\|[0-9]+");
                Matcher rowMatcher = rowPattern.matcher(Objects.requireNonNull(response.getBody()));

                if (rowMatcher.find()) {
                    String price = rowMatcher.group().split("\\|")[4];

                    data.append(symbol)
                            .append(":")
                            .append(price)
                            .append("\n");
                    log.info("{} price = {}", symbol, price);
                }
            }
            bufferedWriter.append(data);
            bufferedWriter.close();
            fileWriter.close();
            return RepeatStatus.FINISHED;
        };
    }

    private String getLastBusinessDay() {
        LocalDateTime localDateTime = switch (LocalDateTime.now().minusDays(1).getDayOfWeek()) {
            case SATURDAY -> LocalDateTime.now().minusDays(2);
            case SUNDAY -> LocalDateTime.now().minusDays(3);
            default -> LocalDateTime.now().minusDays(1);
        };
        return localDateTime.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }
}
