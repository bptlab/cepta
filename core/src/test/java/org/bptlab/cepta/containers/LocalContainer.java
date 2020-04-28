package org.bptlab.cepta.containers;

import org.testcontainers.containers.GenericContainer;
import org.bptlab.cepta.containers.PullPolicy;

public class LocalContainer extends GenericContainer {
    
    public LocalContainer(String target) {
        /*
            //auxiliary/producers/replayer -> bazel/auxiliary/producers/replayer:image
        */
        super(String.format("bazel/%s:image", target.replaceFirst("//", "")));
        this.withImagePullPolicy(PullPolicy.neverPull());
    }
}