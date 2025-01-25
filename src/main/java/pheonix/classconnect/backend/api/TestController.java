package pheonix.classconnect.backend.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v2")
@RequiredArgsConstructor
public class TestController {
    @GetMapping("/hello")
    public ResponseEntity<Object> sayHello() {
        return ResponseEntity.ok("Hello");
    }

}
