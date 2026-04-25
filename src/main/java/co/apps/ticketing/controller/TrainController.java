package co.apps.ticketing.controller;

import co.apps.ticketing.service.train.TrainService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/train")
public class TrainController {

    private final TrainService trainService;

    @GetMapping("/station-list")
    public ResponseEntity<Object> getStationList() {
        return ResponseEntity.ok(trainService.getStationList());
    }
}
