package pheonix.classconnect.backend.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pheonix.classconnect.backend.com.common.dto.Response;
import pheonix.classconnect.backend.exceptions.ErrorCode;
import pheonix.classconnect.backend.exceptions.MainApplicationException;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/v2")
@RequiredArgsConstructor
public class TestController {
    @GetMapping("/hello")
    public ResponseEntity<Object> sayHello() {
        return ResponseEntity.ok("Hello");
    }

    @GetMapping("/success")
    public Response<String> getSuccess() {
        return Response.ok();
    }

    @GetMapping("/successWithResult")
    public Response<Object> getResult() {
        Map<String, String> res = new HashMap<>();
        res.put("name", "공명규");
        res.put("age", "28");
        res.put("department", "소프트웨어학과");

        return Response.ok(HttpStatus.CREATED, "사용자 조회 성공", res);
    }

    @GetMapping("/error")
    public Response<String> getError() {
        throw new MainApplicationException(ErrorCode.BAK_LOGIC_ERROR, "테스트 에러입니다.");
    }
}
