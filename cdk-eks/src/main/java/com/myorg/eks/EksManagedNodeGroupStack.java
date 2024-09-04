package com.myorg.eks;

import com.myorg.eks.config.ConfigInjector;
import com.myorg.eks.config.ConfigLoader;
import com.myorg.eks.config.ConfigProperty;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.services.ec2.InstanceType;
import software.amazon.awscdk.services.eks.Cluster;
import software.amazon.awscdk.services.eks.NodegroupAmiType;
import software.amazon.awscdk.services.eks.NodegroupOptions;
import software.amazon.awscdk.services.eks.NodegroupRemoteAccess;
import software.constructs.Construct;

import java.util.List;


/**
 * EksManagedNodeGroupStack class is responsible for creating a managed node group
 * in an EKS cluster using configuration properties.
 */
public class EksManagedNodeGroupStack extends Stack {

    @ConfigProperty("eks.nodegroup.instance.type")
    private String nodeGroupInstanceType;

    @ConfigProperty("eks.nodegroup.desired")
    private int nodeGroupDesired;

    @ConfigProperty("eks.nodegroup.min")
    private int nodeGroupMin;

    @ConfigProperty("eks.nodegroup.max")
    private int nodeGroupMax;

    @ConfigProperty("eks.nodegroup.remote.access.key")
    private String nodeGroupRemoteAccessKey;


    public EksManagedNodeGroupStack(final Construct scope, final String id, final Cluster cluster, final ConfigLoader configLoader) {
        super(scope, id);

        // Inject configuration properties
        ConfigInjector.injectConfigProperties(this, configLoader);

        System.out.println("-- Create Managed Node Group --");

        // Create a Managed node group
        cluster.addNodegroupCapacity("MNG1", NodegroupOptions.builder()
                .instanceTypes(List.of(new InstanceType(nodeGroupInstanceType)))
                .amiType(NodegroupAmiType.AL2_X86_64)
                .desiredSize(nodeGroupDesired)
                .minSize(nodeGroupMin)
                .maxSize(nodeGroupMax)
                .remoteAccess(NodegroupRemoteAccess.builder().sshKeyName(nodeGroupRemoteAccessKey).build())
                .build());

    }
}
