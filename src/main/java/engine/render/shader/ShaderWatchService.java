package engine.render.shader;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import engine.Main;

public class ShaderWatchService {
    Shader shader;

    public ShaderWatchService(Shader shader)
    {
        this.shader = shader;
        try {
            this.init();
        } catch(Exception e)
        {
            System.err.println("Failed to initilize Watch Service:\n" + e);
        }
    }
    private void init() throws Exception
    {
        Path shaderDir = Paths.get(shader.getVertexPath()).getParent();
        WatchService watchService = FileSystems.getDefault().newWatchService();
        shaderDir.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);

        Thread shaderWatcher = new Thread(()->{
            System.out.println("Shader watcher is now Watching...");
            while (!Main.GetWindow().shouldClose()) {
                try{
                    WatchKey key = watchService.take();
                    for (WatchEvent<?> event : key.pollEvents()) {
                        Path changed = (Path) event.context();
                        if (changed.toString().endsWith(".vert")||
                            changed.toString().endsWith(".frag")||
                            changed.toString().endsWith(".glsl")) {
                            System.out.println("Shader changed: " + changed);
                            
                            shader.markForReload();
                        }
                    }
                    key.reset();
                } catch(InterruptedException e)
                {
                    Thread.currentThread().interrupt();
                    System.out.println("Shader Watch service crashed:\n" + e);
                    break;
                }
                Thread.currentThread().interrupt();
                System.out.println("Shader Watch service stopped");
            }
        });
        shaderWatcher.start();
    }
}
