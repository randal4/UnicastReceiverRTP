import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rtp")
public class RtpServerController {

    @Autowired
    private DynamicRtpServerManager serverManager;

    @PostMapping("/start/{port}")
    public String startServer(@PathVariable int port) {
        serverManager.startServer(port);
        return "Server started on port " + port;
    }

    @PostMapping("/stop/{port}")
    public String stopServer(@PathVariable int port) {
        serverManager.stopServer(port);
        return "Server stopped on port " + port;
    }

    @GetMapping("/status/{port}")
    public String serverStatus(@PathVariable int port) {
        return serverManager.isServerRunning(port) ? "Running" : "Stopped";
    }
}
