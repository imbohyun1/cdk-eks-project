package com.myorg.eks.observability;

import software.amazon.awscdk.services.eks.Cluster;
import software.amazon.awscdk.services.eks.HelmChart;
import software.constructs.Construct;

/**
 * KubernetesMetricsServer class is responsible for deploying the Metrics Server
 * using Helm in a Kubernetes cluster.
 */
public class KubernetesMetricsServer extends Construct {
    public KubernetesMetricsServer(final Construct scope, final String id, final Cluster cluster) {
        super(scope, id);

        System.out.println("-- Deploy Metrics Server using Helm --");

        // Deploy Metrics Server using Helm
        // Equivalent Helm CLI commands:
        // helm repo add metrics-server https://kubernetes-sigs.github.io/metrics-server/ --namespace kube-system
        // https://artifacthub.io/packages/helm/metrics-server/metrics-server

        HelmChart.Builder.create(this, "MetricsServer")
                .cluster(cluster)
                .chart("metrics-server")
                .repository("https://kubernetes-sigs.github.io/metrics-server/")
                .namespace("kube-system")
                .release("metrics-server-release")
                .build();
    }
}