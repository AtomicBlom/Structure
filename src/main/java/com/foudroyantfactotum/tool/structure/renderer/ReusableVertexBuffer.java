package com.foudroyantfactotum.tool.structure.renderer;

import net.minecraft.client.renderer.VertexBuffer;

/**
 * Created by codew on 20/12/2016.
 */
class ReusableVertexBuffer extends VertexBuffer {
    public ReusableVertexBuffer(int bufferSizeIn) {
        super(bufferSizeIn);
    }

    @Override
    public void reset() {

    }
}
