package com.xxx.util;

import com.google.api.client.repackaged.com.google.common.base.Strings;

import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.LongSummaryStatistics;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Helper {
    private static final Double TIME_UNIT = 1000000.0;

    public static List<Long> calDuration(List<Long> times) {
        List<Long> res = new ArrayList<>();
        for (int i = 1; i < times.size(); i++) {
            res.add(times.get(i) - times.get(i - 1));
        }
        return res;
    }

    public static DoubleSummaryStatistics print(List<Long> times) {
        return print(null, times);
    }

    public static LongSummaryStatistics printData(String title, List<Integer> data) {
        if (!Strings.isNullOrEmpty(title)) log.error("----------------{}--------------", title);
        LongSummaryStatistics statistics = data.stream().mapToLong(t -> t).summaryStatistics();
        log.error("[AVG]:{}", statistics.getAverage());
        log.error("[MAX]:{}", statistics.getMax());
        log.error("[MIN]:{}", statistics.getMin());
        log.error("[SUM]:{}", statistics.getSum());
        log.error("[COUNT]:{}", statistics.getCount());
        return statistics;
    }

    public static DoubleSummaryStatistics print(String title, List<Long> times) {
        if (!Strings.isNullOrEmpty(title)) log.error("----------------{}--------------", title);
        DoubleSummaryStatistics statistics = times.stream().mapToDouble(t -> t / TIME_UNIT).summaryStatistics();
        log.error("[AVG]:{} ms", statistics.getAverage());
        log.error("[MAX]:{} ms", statistics.getMax());
        log.error("[MIN]:{} ms", statistics.getMin());
        log.error("[SUM]:{} ms", statistics.getSum());
        log.error("[COUNT]:{}", statistics.getCount());
        return statistics;
    }

    public static Double toMillis(Long t) {
        return t / TIME_UNIT;
    }

    public static Double calTPS(long count, double time) {
        return 1000 / (time / count);
    }
}
