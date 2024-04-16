package crackhash.worker.controller;

import crackhash.worker.model.dtos.WorkerPingDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class WorkerController {
    @Value("${workerNum}")
    private Integer workerNumber;

    @PostMapping("/worker/ping")
    public ResponseEntity<WorkerPingDto> getPing() {
        return new ResponseEntity<>(new WorkerPingDto(workerNumber), HttpStatus.OK);
    }
}
