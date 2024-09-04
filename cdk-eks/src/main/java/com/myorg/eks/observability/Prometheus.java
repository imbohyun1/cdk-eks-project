package com.myorg.eks.observability;

import com.myorg.eks.config.ConfigInjector;
import com.myorg.eks.config.ConfigLoader;
import com.myorg.eks.config.ConfigProperty;
import software.amazon.awscdk.services.eks.Cluster;
import software.amazon.awscdk.services.eks.HelmChart;
import software.constructs.Construct;

import java.util.Map;

/**
 * Prometheus class is responsible for deploying Prometheus using Helm in a Kubernetes cluster.
 */
public class Prometheus extends Construct {


    @ConfigProperty("kubernetes.prometheus.helm.version")
    private String prometheusHelmVersion;

    @ConfigProperty("kubernetes.prometheus.helm.repo")
    private String prometheusHelmRepo;

    @ConfigProperty("kubernetes.prometheus.helm.namespace")
    private String prometheusHelmNamespace;


    public Prometheus(final Construct scope, final String id, final Cluster cluster, final ConfigLoader configLoader) {
        super(scope, id);

        System.out.println("-- Deploy Prometheus using Helm --");

        // Inject configuration properties
        ConfigInjector.injectConfigProperties(this, configLoader);

        // Deploy Prometheus using Helm
        //
        // helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
        // helm upgrade -i prometheus prometheus-community/prometheus \
        //    --namespace prometheus
        //
        // https://kubeapps.dev/

        HelmChart.Builder.create(this, "Prometheus")
                .cluster(cluster)
                .chart("prometheus")
                .repository(prometheusHelmRepo)
                .namespace(prometheusHelmNamespace)
                // Uncomment the following line if you want to specify a specific version
                // .version(prometheusHelmVersion)
                .release("prometheus-release")
                .build();
    }
}
