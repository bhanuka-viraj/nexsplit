# Kibana Dashboards for NexSplit Application

## Overview

This document provides Kibana dashboard configurations and setup instructions for monitoring the NexSplit application using Elasticsearch, Filebeat, and Kibana.

## Prerequisites

- Elasticsearch, Kibana, and Filebeat running (via Docker Compose)
- NexSplit application generating structured logs
- Access to Kibana web interface (http://localhost:5601)
- Real-time log streaming via Filebeat

## Current Setup

### ✅ Working Architecture

```
NexSplit App → Structured JSON logs → Filebeat → Elasticsearch → Kibana
```

### ✅ Index Pattern

- **Current Index**: `nexsplit-structured-YYYY.MM.DD` (e.g., `nexsplit-structured-2025.08.20`)
- **Index Pattern**: `nexsplit-structured-*`
- **Time Field**: `@timestamp`

## Dashboard Setup Instructions

### 1. Access Kibana

1. Open your browser and navigate to `http://localhost:5601`
2. Wait for Kibana to fully load
3. You should see the Kibana welcome page

### 2. Create Data View (Index Pattern)

**Note**: In newer Kibana versions (8.x), "Index Patterns" are now called "Data Views".

#### Create Data View for Structured Logs

1. Go to **Stack Management** → **Data Views**
2. Click **Create data view**
3. Enter **Index pattern**: `nexsplit-structured-*`
4. Click **Next step**
5. Select `@timestamp` as the **Time field**
6. Click **Save data view to Kibana**
7. Name: `nexsplit-structured-logs`

### 3. Verify Data Ingestion

1. Go to **Discover**
2. Select the data view: `nexsplit-structured-logs`
3. Set time range to **Last 15 minutes**
4. You should see structured JSON logs with:
   - Correlation IDs
   - Business events
   - Performance metrics
   - Security events

### 4. Create Dashboards

#### Security Monitoring Dashboard

**Dashboard Name:** NexSplit Security Monitoring

**Visualizations:**

1. **Failed Login Attempts**

   - Type: Line chart
   - Data view: `nexsplit-structured-logs`
   - Query: `message:*LOGIN_FAILURE*`
   - Y-axis: Count
   - X-axis: @timestamp (Date Histogram)

2. **Security Events by Level**

   - Type: Pie chart
   - Data view: `nexsplit-structured-logs`
   - Query: `level: WARN OR level: ERROR`
   - Buckets: Split slices
   - Field: level.keyword
   - Size: 10

3. **Authentication Events**

   - Type: Data table
   - Data view: `nexsplit-structured-logs`
   - Query: `message:*AUTHENTICATE* OR message:*LOGIN*`
   - Columns: message, correlationId, @timestamp, level

4. **Security Events Timeline**
   - Type: Line chart
   - Data view: `nexsplit-structured-logs`
   - Query: `level: WARN OR level: ERROR`
   - Y-axis: Count
   - X-axis: @timestamp (Date Histogram)
   - Split series: level.keyword

#### Performance Monitoring Dashboard

**Dashboard Name:** NexSplit Performance Monitoring

**Visualizations:**

1. **Method Execution Times**

   - Type: Line chart
   - Data view: `nexsplit-structured-logs`
   - Query: `logger_name: com.nexsplit.aspect.LoggingAspect`
   - Y-axis: Count
   - X-axis: @timestamp (Date Histogram)
   - Split series: message.keyword

2. **Slow Operations**

   - Type: Data table
   - Data view: `nexsplit-structured-logs`
   - Query: `logger_name: com.nexsplit.aspect.LoggingAspect AND level: DEBUG`
   - Columns: message, correlationId, @timestamp, thread_name

3. **Performance Distribution**

   - Type: Histogram
   - Data view: `nexsplit-structured-logs`
   - Query: `logger_name: com.nexsplit.aspect.LoggingAspect`
   - Field: @timestamp
   - Interval: 1 minute

4. **Performance Status Overview**
   - Type: Pie chart
   - Data view: `nexsplit-structured-logs`
   - Query: `logger_name: com.nexsplit.aspect.LoggingAspect`
   - Buckets: Split slices
   - Field: level.keyword

#### Business Analytics Dashboard

**Dashboard Name:** NexSplit Business Analytics

**Visualizations:**

1. **User Registration Trends**

   - Type: Line chart
   - Data view: `nexsplit-structured-logs`
   - Query: `message:*USER_REGISTRATION*`
   - Y-axis: Count
   - X-axis: @timestamp (Date Histogram)

2. **User Actions by Type**

   - Type: Pie chart
   - Data view: `nexsplit-structured-logs`
   - Query: `message:*BUSINESS_EVENT*`
   - Buckets: Split slices
   - Field: message.keyword

3. **Business Event Success Rate**

   - Type: Gauge
   - Data view: `nexsplit-structured-logs`
   - Query: `message:*SUCCESS*`
   - Metric: Count
   - Range: 0-100

4. **Top Active Users**
   - Type: Data table
   - Data view: `nexsplit-structured-logs`
   - Query: `message:*BUSINESS_EVENT*`
   - Buckets: Split rows
   - Field: correlationId.keyword
   - Size: 10

#### Error Tracking Dashboard

**Dashboard Name:** NexSplit Error Tracking

**Visualizations:**

1. **Error Rate Over Time**

   - Type: Line chart
   - Data view: `nexsplit-structured-logs`
   - Query: `level: ERROR`
   - Y-axis: Count
   - X-axis: @timestamp (Date Histogram)

2. **Error Types Distribution**

   - Type: Pie chart
   - Data view: `nexsplit-structured-logs`
   - Query: `level: ERROR`
   - Buckets: Split slices
   - Field: logger_name.keyword

3. **Recent Errors**

   - Type: Data table
   - Data view: `nexsplit-structured-logs`
   - Query: `level: ERROR`
   - Columns: message, logger_name, @timestamp, correlationId
   - Sort: @timestamp (Descending)

4. **Error Correlation with Requests**
   - Type: Line chart
   - Data view: `nexsplit-structured-logs`
   - Query: `level: ERROR`
   - Y-axis: Count
   - X-axis: @timestamp (Date Histogram)
   - Split series: correlationId.keyword

### 5. Create Alerts

#### High Error Rate Alert

1. Go to **Stack Management** → **Rules and Alerts**
2. Click **Create rule**
3. Select **Elasticsearch query**
4. Configure:
   - Index: `nexsplit-structured-*`
   - Query: `level: ERROR`
   - Time window: 5 minutes
   - Threshold: > 10 errors
   - Action: Email notification

#### Security Threat Alert

1. Go to **Stack Management** → **Rules and Alerts**
2. Click **Create rule**
3. Select **Elasticsearch query**
4. Configure:
   - Index: `nexsplit-structured-*`
   - Query: `level: WARN AND message:*SECURITY*`
   - Time window: 1 minute
   - Threshold: > 0 events
   - Action: Email notification

#### Performance Degradation Alert

1. Go to **Stack Management** → **Rules and Alerts**
2. Click **Create rule**
3. Select **Elasticsearch query**
4. Configure:
   - Index: `nexsplit-structured-*`
   - Query: `logger_name: com.nexsplit.aspect.LoggingAspect AND level: DEBUG`
   - Time window: 5 minutes
   - Threshold: > 5 slow requests
   - Action: Email notification

## Dashboard Filters

### Time Range Filters

- **Last 1 hour**: For real-time monitoring
- **Last 24 hours**: For daily analysis
- **Last 7 days**: For weekly trends
- **Last 30 days**: For monthly analysis

### Custom Filters

- `level: ERROR` - Error level logs only
- `level: WARN` - Warning level logs only
- `level: INFO` - Info level logs only
- `level: DEBUG` - Debug level logs only
- `logger_name: com.nexsplit.util.StructuredLoggingUtil` - Business events
- `logger_name: com.nexsplit.aspect.LoggingAspect` - Performance logs
- `logger_name: com.nexsplit.config.security` - Security logs

## Useful Queries

### Find Failed Logins

```
message:*LOGIN_FAILURE* AND level: WARN
```

### Find Slow Operations

```
logger_name: com.nexsplit.aspect.LoggingAspect AND level: DEBUG
```

### Find User Registration Events

```
message:*USER_REGISTRATION* AND level: INFO
```

### Find Security Events

```
level: WARN AND message:*SECURITY*
```

### Find Business Events

```
logger_name: com.nexsplit.util.StructuredLoggingUtil
```

### Find Performance Issues

```
logger_name: com.nexsplit.aspect.LoggingAspect AND level: DEBUG
```

### Find Errors by Correlation ID

```
correlationId: "your-correlation-id-here"
```

## Real-Time Monitoring

### Live Data View

1. Go to **Discover**
2. Select data view: `nexsplit-structured-logs`
3. Set time range to **Last 15 minutes**
4. Click **Auto-refresh** (every 10 seconds)
5. Watch logs appear in real-time

### Key Metrics to Monitor

1. **Error Rate**: `level: ERROR`
2. **Performance**: `logger_name: com.nexsplit.aspect.LoggingAspect`
3. **Security**: `level: WARN AND message:*SECURITY*`
4. **Business Events**: `logger_name: com.nexsplit.util.StructuredLoggingUtil`

## Best Practices

1. **Regular Maintenance**

   - Review and update dashboards weekly
   - Clean up old visualizations
   - Update data views as needed

2. **Performance Optimization**

   - Use appropriate time ranges
   - Limit the number of visualizations per dashboard
   - Use filters to reduce data volume

3. **Security**

   - Restrict access to sensitive dashboards
   - Use role-based access control
   - Monitor dashboard access logs

4. **Monitoring**
   - Set up alerts for critical metrics
   - Review alerts regularly
   - Document alert procedures

## Troubleshooting

### No Data in Dashboards

1. Check if Elasticsearch is running: `curl http://localhost:9200/_cluster/health`
2. Verify Filebeat is running: `docker logs nexsplit-filebeat`
3. Check if logs are being sent to Elasticsearch: `curl http://localhost:9200/_cat/indices?v`
4. Verify data view is created correctly
5. Check time range settings

### Slow Dashboard Loading

1. Reduce time range
2. Add filters to reduce data volume
3. Check Elasticsearch cluster health
4. Optimize queries

### Missing Fields

1. Check if new fields are being indexed
2. Refresh data view
3. Verify log format consistency
4. Check field mapping in Elasticsearch

### Filebeat Issues

1. Check Filebeat logs: `docker logs nexsplit-filebeat-debug`
2. Verify log file exists: `docker exec nexsplit-app ls -la /app/logs/structured-logs.json`
3. Check Filebeat configuration in docker-compose.debug.yml
4. **If Filebeat fails to start due to Kibana not being ready:**
   ```bash
   # Wait for Kibana to be healthy, then restart Filebeat
   docker-compose -f docker-compose.debug.yml up filebeat -d
   ```
5. **Alternative restart method:**
   ```bash
   docker restart nexsplit-filebeat-debug
   ```

## Support

For issues with Kibana dashboards:

1. Check Elasticsearch logs: `docker logs nexsplit-elasticsearch`
2. Check Filebeat logs: `docker logs nexsplit-filebeat`
3. Verify data view configuration
4. Test queries in Discover
5. Review application logs for errors

## Quick Start Commands

```bash
# Check if services are running
docker ps

# Check Elasticsearch health
curl http://localhost:9200/_cluster/health

# Check indices
curl http://localhost:9200/_cat/indices?v

# Check Filebeat logs
docker logs nexsplit-filebeat-debug

# Restart Filebeat if needed (wait for Kibana to be healthy first)
docker-compose -f docker-compose.debug.yml up filebeat -d

# Alternative restart method
docker restart nexsplit-filebeat-debug
```
