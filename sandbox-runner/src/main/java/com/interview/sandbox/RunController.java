package com.interview.sandbox;

import org.springframework.web.bind.annotation.*;

@RestController
class RunController {

    /**
     * Phase 0 placeholder. Phase 2 will replace this with a Docker-in-Docker
     * sandbox: javac+java in a disposable container, no network, ulimits, time cap.
     */
    @PostMapping("/run")
    public RunResponse run(@RequestBody RunRequest req) {
        return new RunResponse(
                "",
                "sandbox-runner placeholder: Phase 2 will execute the code here",
                -1,
                0L);
    }

    @GetMapping("/health")
    public java.util.Map<String, String> health() {
        return java.util.Map.of("status", "ok");
    }

    record RunRequest(String code, String stdin, Integer timeoutMs) {}
    record RunResponse(String stdout, String stderr, int exitCode, long timeMs) {}
}
