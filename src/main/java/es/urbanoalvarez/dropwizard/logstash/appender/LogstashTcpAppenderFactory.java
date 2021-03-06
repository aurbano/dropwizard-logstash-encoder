package es.urbanoalvarez.dropwizard.logstash.appender;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.Appender;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.dropwizard.logging.async.AsyncAppenderFactory;
import io.dropwizard.logging.filter.LevelFilterFactory;
import io.dropwizard.logging.layout.LayoutFactory;
import net.logstash.logback.appender.LogstashTcpSocketAppender;
import net.logstash.logback.encoder.LogstashEncoder;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.io.IOException;

@JsonTypeName("logstash-tcp")
public class LogstashTcpAppenderFactory extends AbstractLogstashAppenderFactory {
  private boolean includeCallerData = false;

  @Min(1)
  @Max(65535)
  private int queueSize = LogstashTcpSocketAppender.DEFAULT_QUEUE_SIZE;

  public LogstashTcpAppenderFactory() {
    this.port = LogstashTcpSocketAppender.DEFAULT_PORT;
  }

  @JsonProperty
  public void setIncludeCallerData(boolean includeCallerData) {
    this.includeCallerData = includeCallerData;
  }

  @JsonProperty
  public boolean getIncludeCallerData() {
    return includeCallerData;
  }

  @JsonProperty
  public int getQueueSize() {
    return queueSize;
  }

  @JsonProperty
  public void setQueueSize(int queueSize) {
    this.queueSize = queueSize;
  }

  public Appender build(LoggerContext context, String applicationName, LayoutFactory layoutFactory, LevelFilterFactory levelFilterFactory, AsyncAppenderFactory asyncAppenderFactory) {
    final LogstashTcpSocketAppender appender = new LogstashTcpSocketAppender();
    final LogstashEncoder encoder = new LogstashEncoder();

    appender.setName("logstash-tcp-appender");
    appender.setContext(context);
    appender.setRemoteHost(host);
    appender.setPort(port);
    appender.setIncludeCallerData(includeCallerData);
    appender.setQueueSize(queueSize);

    encoder.setIncludeContext(includeContext);
    encoder.setIncludeMdc(includeMdc);
    encoder.setIncludeCallerInfo(includeCallerInfo);
    if (customFields != null) {
      try {
        String custom = LogstashAppenderFactoryHelper.getCustomFieldsFromHashMap(customFields);
        encoder.setCustomFields(custom);
      } catch (IOException e) {
        System.out.println("unable to parse customFields: "+e.getMessage());
      }
    }

    if (fieldNames != null) {
      encoder.setFieldNames(LogstashAppenderFactoryHelper.getFieldNamesFromHashMap(fieldNames));
    }

    appender.setEncoder(encoder);
    levelFilterFactory.build(threshold);
    encoder.start();
    appender.start();

    return wrapAsync(appender, asyncAppenderFactory);
  }
}
