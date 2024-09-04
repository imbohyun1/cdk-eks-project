package com.myorg.eks.scaling;

import software.amazon.awscdk.Stack;
import software.amazon.awscdk.services.eks.Cluster;
import software.amazon.awscdk.services.eks.HelmChartOptions;
import software.amazon.awscdk.services.eks.KubernetesManifest;
import software.amazon.awscdk.services.iam.PolicyDocument;
import software.amazon.awscdk.services.iam.Role;
import software.amazon.awscdk.services.iam.ServicePrincipal;
import software.constructs.Construct;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

//import org.yaml.snakeyaml.Yaml;

public class Karpenter extends Stack {
    public Karpenter(final Construct scope, final String id, final Cluster cluster) {
        super(scope, id);

        Role karpenterRole = Role.Builder.create(this, "KarpenterRole")
                .assumedBy(new ServicePrincipal("karpenter.sh"))
                .inlinePolicies(Map.of(
                        "KarpenterPolicy", PolicyDocument.fromJson(Map.of(
                                "Statement", Arrays.asList(Map.of(
                                        "Effect", "Allow",
                                        "Action", Arrays.asList("ec2:*", "iam:*", "eks:*"),
                                        "Resource", "*"
                                ))
                        ))
                ))
                .build();

        // Install Karpenter using Helm
        cluster.addHelmChart("KarpenterHelmChart", HelmChartOptions.builder()
                .repository("https://charts.karpenter.sh/")
                .chart("karpenter")
                .release("karpenter")
                .namespace("karpenter")
                .values(Map.of(
                        "serviceAccount.create", true,
                        "serviceAccount.name", "karpenter",
                        "controller.resources.requests.cpu", "100m",
                        "controller.resources.requests.memory", "256Mi",
                        "controller.resources.limits.cpu", "1",
                        "controller.resources.limits.memory", "1Gi",
                        "clusterName", cluster.getClusterName(),
                        "clusterEndpoint", cluster.getClusterEndpoint(),
                        "defaultInstanceProfile", karpenterRole.getRoleName()
                ))
                .build());

        // Load the Karpenter Provisioner YAML file
        /*String provisionerYaml;
        try {
            provisionerYaml = new String(Files.readAllBytes(Paths.get("manifests/karpenter-provisioner.yaml")));

        } catch (IOException e) {
            throw new RuntimeException("Failed to load Karpenter provisioner YAML file", e);
        }


        if(!provisionerYaml.isEmpty()){

            // Parse the YAML content
            Yaml yaml = new Yaml();
            Iterable<Object> yamlObjects = yaml.loadAll(provisionerYaml);

            // Convert the parsed YAML to a list of maps
            List<Map<String, Object>> manifestList = (List<Map<String, Object>>) yamlObjects;

            // Apply the YAML as a KubernetesManifest
            KubernetesManifest karpenterProvisioner = KubernetesManifest.Builder.create(this, "KarpenterProvisioner")
                    .cluster(cluster)
                    .manifest(manifestList)
                    .build();
        }*/

        // Apply the Karpenter Provisioner
        KubernetesManifest karpenterProvisioner = KubernetesManifest.Builder.create(this, "KarpenterProvisioner")
                .cluster(cluster)
                .manifest(List.of(
                        Map.of(
                                "apiVersion", "karpenter.sh/v1alpha5",
                                "kind", "Provisioner",
                                "metadata", Map.of("name", "default"),
                                "spec", Map.of(
                                        "requirements", List.of(
                                                Map.of(
                                                        "key", "node.kubernetes.io/instance-type",
                                                        "operator", "In",
                                                        "values", List.of("m5.large", "m5.xlarge")
                                                ),
                                                Map.of(
                                                        "key", "topology.kubernetes.io/zone",
                                                        "operator", "In",
                                                        "values", List.of("us-west-2a", "us-west-2b")
                                                )
                                        ),
                                        "limits", Map.of(
                                                "resources", Map.of(
                                                        "cpu", "1000",
                                                        "memory", "500Gi"
                                                )
                                        ),
                                        "provider", Map.of(
                                                "amiFamily", "AL2",
                                                "instanceProfile", karpenterRole.getRoleName()
                                        ),
                                        "ttlSecondsAfterEmpty", 30
                                )
                        )
                ))
                .build();

    }
}
