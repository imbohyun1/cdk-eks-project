package com.myorg.eks.observability;

import com.myorg.eks.config.ConfigInjector;
import com.myorg.eks.config.ConfigLoader;
import com.myorg.eks.config.ConfigProperty;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.services.eks.Cluster;
import software.amazon.awscdk.services.eks.HelmChart;
import software.constructs.Construct;

import java.util.Map;

/**
 * KubeOpsView class is responsible for deploying Kube Ops View using Helm in a Kubernetes cluster.
 */
public class KubeOpsView extends Construct {


    @ConfigProperty("kubernetes.kubeopsview.helm.version")
    private String kubeopsviewHelmVersion;

    @ConfigProperty("kubernetes.kubeopsview.helm.repo")
    private String kubeopsviewHelmRepo;

    @ConfigProperty("kubernetes.kubeopsview.helm.namespace")
    private String kubeopsviewHelmNamespace;


    public KubeOpsView(final Construct scope, final String id, final Cluster cluster, final ConfigLoader configLoader) {
        super(scope, id);

        System.out.println("-- Deploy KUBE-OPS-VIEW using Helm --");

        // Inject configuration properties
        ConfigInjector.injectConfigProperties(this, configLoader);

        // Deploy Kube Ops View using Helm
        // Equivalent Helm CLI commands:
        // helm repo add k8s-at-home https://k8s-at-home.com/charts/
        // helm repo update
        // helm install kube-ops-view k8s-at-home/kube-ops-view --namespace <namespace>
        // --version <version> --set service.type=LoadBalancer
        // https://kubeapps.dev/

        HelmChart.Builder.create(this, "KubeOpsView")
                .cluster(cluster)
                .chart("kube-ops-view")
                .repository(kubeopsviewHelmRepo)
                .namespace(kubeopsviewHelmNamespace)
                .version(kubeopsviewHelmVersion)
                .release("kube-ops-view-release")
                .values(Map.of(
                                "service", Map.of(
                                        "type", "LoadBalancer"
                                )
                        )
                )
                .build();
    }
}
