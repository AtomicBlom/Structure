package com.foudroyantfactotum.tool.structure.renderer;

import net.minecraft.client.renderer.BufferBuilder;

/**
 * Created by codew on 20/12/2016.
 */
class ReusableBufferBuilder extends BufferBuilder {
    public ReusableBufferBuilder(int bufferSizeIn) {
        super(bufferSizeIn);
    }

    @Override
    public void reset() {

    }
}
