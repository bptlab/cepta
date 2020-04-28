package org.bptlab.cepta.containers;

import org.testcontainers.images.ImagePullPolicy;
import org.testcontainers.utility.DockerImageName;

class NeverPullPolicy implements ImagePullPolicy {
    @Override
    public boolean shouldPull(DockerImageName imageName) {
        return false;
    }
}

public class PullPolicy {

    public static ImagePullPolicy neverPull() {
        return new NeverPullPolicy();
    } 

}