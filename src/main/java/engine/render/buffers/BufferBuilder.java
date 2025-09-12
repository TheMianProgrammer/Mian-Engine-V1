package engine.render.buffers;

import engine.render.Triangle;
import obj.entity.Entity;

public interface BufferBuilder {
    void build(Entity entity);
}
