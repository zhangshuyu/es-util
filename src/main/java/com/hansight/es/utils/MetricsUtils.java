package com.hansight.es.utils;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

/**
 * Created by taoistwar on 2014/12/10.
 */
public class MetricsUtils {
	public static final MetricRegistry metrics = new MetricRegistry();

	public static <T> void register(String name, Gauge<T> t) {
		try {
			metrics.register(name, t);
		} catch (Exception e) {
		}
	}

	public static void newGauge(String name, final Queue<?> queue) {
		try {
			metrics.register(name, new Gauge<Integer>() {
				public Integer getValue() {
					return queue.size();
				}
			});
		} catch (Exception e) {
		}
	}

	public static void newGauge(String name, final String tips) {
		try {
			metrics.register(name, new Gauge<String>() {
				public String getValue() {
					return tips;
				}
			});
		} catch (Exception e) {
		}
	}

	public static Counter newCounter(String name) {
		return metrics.counter(name);
	}

	public static MetricRegistry getMetrics() {
		return metrics;
	}

	public static void register(final Thread thread) {
		try {
			metrics.register(thread.getName(), new Gauge<Map<String, Object>>() {
				public Map<String, Object> getValue() {
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("alive", thread.isAlive());
					map.put("daemon", thread.isDaemon());
					map.put("priority", thread.getPriority());
					map.put("state", thread.getState());
					map.put("stackTrace", thread.getStackTrace());
					map.put("threadGroup", thread.getThreadGroup());
					return map;
				}
			});
		} catch (Exception e) {
		}
	}

	public static void startConsole() {
		ConsoleReporter reporter = ConsoleReporter.forRegistry(metrics).convertRatesTo(TimeUnit.SECONDS).convertDurationsTo(TimeUnit.MILLISECONDS).build();
		reporter.start(1, TimeUnit.SECONDS);
	}

}
