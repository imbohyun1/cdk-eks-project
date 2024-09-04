package com.myorg.eks.observability.alarm;

import software.amazon.awscdk.Duration;
import software.amazon.awscdk.services.cloudwatch.*;
import software.amazon.awscdk.services.eks.Cluster;
import software.amazon.awscdk.services.sns.Subscription;
import software.amazon.awscdk.services.sns.SubscriptionProtocol;
import software.amazon.awscdk.services.sns.Topic;
import software.amazon.awscdk.services.cloudwatch.actions.SnsAction;
import software.constructs.Construct;

import java.util.Map;


public class CloudWatchAlarm extends Construct {
    public CloudWatchAlarm(final Construct scope, final String id, final Cluster cluster) {
        super(scope, id);


        // Create an SNS topic
        Topic alarmTopic = Topic.Builder.create(this, "AlarmTopic")
                .displayName("Alarm Notifications")
                .build();

        // Subscribe an email address to the SNS topic
        Subscription.Builder.create(this, "EmailSubscription")
                .topic(alarmTopic)
                .protocol(SubscriptionProtocol.EMAIL)
                .endpoint("libohyun@amazon.com")
                .build();



        // Define a CloudWatch metric for CPU utilization
        Metric cpuUtilizationMetric = new Metric(MetricProps.builder()
                .namespace("ContainerInsights")
                .metricName("node_cpu_utilization")
                .dimensionsMap(Map.of("ClusterName", cluster.getClusterName()))
                .statistic(String.valueOf(Statistic.AVERAGE))
                .period(Duration.minutes(5))
                .build());

        // Create an alarm for CPU utilization
        Alarm cpuAlarm = new Alarm(this, "CpuUtilizationAlarm", AlarmProps.builder()
                .metric(cpuUtilizationMetric)
                .threshold(80)
                .evaluationPeriods(3)
                .comparisonOperator(ComparisonOperator.GREATER_THAN_OR_EQUAL_TO_THRESHOLD)
                .alarmDescription("Alarm when CPU utilization exceeds 80%")
                .actionsEnabled(true)
                .build());

        // Add an SNS action to the alarm
        cpuAlarm.addAlarmAction(new SnsAction(alarmTopic));


        // Define a CloudWatch metric for memory utilization
        Metric memoryUtilizationMetric = new Metric(MetricProps.builder()
                .namespace("ContainerInsights")
                .metricName("node_memory_utilization")
                .dimensionsMap(Map.of("ClusterName", cluster.getClusterName()))
                .statistic(String.valueOf(Statistic.AVERAGE))
                .period(Duration.minutes(5))
                .build());


        // pod alarm 만들기!

        // Create an alarm for memory utilization
        Alarm memoryAlarm = new Alarm(this, "MemoryUtilizationAlarm", AlarmProps.builder()
                .metric(memoryUtilizationMetric)
                .threshold(70)
                .evaluationPeriods(3)
                .comparisonOperator(ComparisonOperator.GREATER_THAN_OR_EQUAL_TO_THRESHOLD)
                .alarmDescription("Alarm when memory utilization exceeds 80%")
                .build());


        // Define a CloudWatch metric for cluster-wide pod CPU utilization
        Metric clusterPodCpuUtilizationMetric = new Metric(MetricProps.builder()
                .namespace("ContainerInsights")
                .metricName("pod_cpu_utilization")
                .dimensionsMap(Map.of("ClusterName", cluster.getClusterName()))
                .statistic(String.valueOf(Statistic.AVERAGE))
                .period(Duration.minutes(5))
                .build());

        // Create the CloudWatch alarm for cluster-wide pod CPU utilization
        Alarm clusterPodCpuAlarm = new Alarm(this, "ClusterPodCpuUtilizationAlarm", AlarmProps.builder()
                .metric(clusterPodCpuUtilizationMetric)
                .threshold(40) // Set threshold to 60%
                .evaluationPeriods(3)
                .comparisonOperator(ComparisonOperator.GREATER_THAN_OR_EQUAL_TO_THRESHOLD)
                .alarmDescription("Alarm when cluster-wide pod average CPU utilization exceeds 60%")
                .build());


    }
}
