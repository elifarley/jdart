package com.google.dart.compiler.metrics;

import java.io.Writer;
import java.util.List;

public final class Tracer {
  /**
   * Represents a node in a tree of SpeedTracer events.
   */
  public class TraceEvent {
    protected final EventType type;
    List<TraceEvent> children;
    List<String> data;

    long elapsedDurationNanos;
    long elapsedStartTimeNanos;

    long processCpuDurationNanos;
    long processCpuStartTimeNanos;

    long threadCpuDurationNanos;
    long threadCpuStartTimeNanos;

    TraceEvent() {
      type = null;
    }

    TraceEvent(TraceEvent parent, EventType type, String... data) {
      this.type = type;
    }

    public void addData(String... data) {
    }

    public void end(String... data) {
    }

    public long getDurationNanos() {
      return 0;
    }

    public long getElapsedDurationNanos() {
      return 0;
    }

    public long getElapsedStartTimeNanos() {
      return 0;
    }

    public long getStartTimeNanos() {
      return 0;
    }

    public EventType getType() {
      return type;
    }

    void extendDuration(TraceEvent refEvent) {
    }

    void setStartsAfter(TraceEvent refEvent) {
    }

    void updateDuration() {
    }
  }

  public interface EventType {
    String getColor();
    String getName();
  }

  static enum Format {
    HTML,
    RAW
  }

  public static void addData(String... data) {
  }

  public static void init() {
  }

  public static boolean canTrace() {
    return false;
  }

  public static void markTimeline(String message) {
  }

  public static TraceEvent start(EventType type, String... data) {
    return null;
  }

  private static double convertToMilliseconds(long nanos) {
    return 0;
  }

  public static void end(TraceEvent event, String... data) {
  }

  Tracer(Writer writer, Format format) {
  }

  public void addDataImpl(String... data) {
  }

  public void markTimelineImpl(String message) {
  }

  void addGcEvents(TraceEvent refEvent) {
  }

  void addOverheadEvent(TraceEvent refEvent) {
  }

  void endImpl(TraceEvent event, String... data) {
  }

  void flush() {
  }

  TraceEvent startImpl(EventType type, String... data) {
    return null;
  }
}

