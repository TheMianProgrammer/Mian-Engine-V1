package engine.render;

import javax.swing.text.html.parser.Entity;

public interface BufferBuilder {
    void build(Entity entity);
}
