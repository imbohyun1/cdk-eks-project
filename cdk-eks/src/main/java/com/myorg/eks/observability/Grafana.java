package com.myorg.eks.observability;

import com.myorg.eks.config.ConfigInjector;
import com.myorg.eks.config.ConfigLoader;
import com.myorg.eks.config.ConfigProperty;
import software.amazon.awscdk.services.eks.Cluster;
import software.amazon.awscdk.services.eks.HelmChart;
import software.constructs.Construct;

import java.util.Map;

/**
 * Grafana class is responsible for deploying Grafana using Helm in a Kubernetes cluster.
 */
public class Grafana extends Construct{

    @ConfigProperty("kubernetes.grafana.helm.version")
    private String grafanaHelmVersion;

    @ConfigProperty("kubernetes.grafana.helm.repo")
    private String grafanaHelmRepo;

    @ConfigProperty("kubernetes.grafana.helm.namespace")
    private String grafanaNamespace;

    @ConfigProperty("kubernetes.grafana.helm.adminPassword")
    private String grafanaAdminPassword;

    public Grafana(final Construct scope, final String id, final Cluster cluster, final ConfigLoader configLoader) {
        super(scope, id);

        System.out.println("-- Deploy Grafana using Helm --");

        // Inject configuration properties
        ConfigInjector.injectConfigProperties(this, configLoader);

        // Deploy Grafana using Helm
        //
        // helm repo add grafana https://grafana.github.io/helm-charts
        // helm install grafana grafana/grafana
        //--namespace prometheus
        //--set persistence.enabled=true
        //--set adminPassword='test123!'
        //--set service.type=LoadBalancer
        //

        HelmChart.Builder.create(this, "Grafana")
                .cluster(cluster)
                .chart("grafana")
                .repository(grafanaHelmRepo)
                .namespace(grafanaNamespace)
                .release("grafana-release")
                .values(Map.of (
                                "persistence", Map.of(
                                        "enabled", true
                                ),
                                "adminPassword", grafanaAdminPassword
                        )
                )
                .build();
    }
}
