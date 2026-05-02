package com.incidenthub.demo.payment.controller;

import com.incidenthub.demo.payment.dto.SimulationStateResponse;
import com.incidenthub.demo.payment.dto.UpdateFailureModeRequest;
import com.incidenthub.demo.payment.simulation.PaymentSimulationState;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/simulate")
public class SimulationController {

    private final PaymentSimulationState simulationState;

    public SimulationController(PaymentSimulationState simulationState) {
        this.simulationState = simulationState;
    }

    @GetMapping("/failure-mode")
    public SimulationStateResponse getFailureMode() {
        return new SimulationStateResponse(
                simulationState.mode(),
                simulationState.latencyMs(),
                simulationState.failureRate()
        );
    }

    @PostMapping("/failure-mode")
    public SimulationStateResponse updateFailureMode(@RequestBody UpdateFailureModeRequest request) {
        simulationState.update(
                request.mode(),
                request.latencyMs(),
                request.failureRate()
        );

        return new SimulationStateResponse(
                simulationState.mode(),
                simulationState.latencyMs(),
                simulationState.failureRate()
        );
    }

    @PostMapping("/recovery")
    public SimulationStateResponse recover() {
        simulationState.update(null, 0, 0.0);

        return new SimulationStateResponse(
                simulationState.mode(),
                simulationState.latencyMs(),
                simulationState.failureRate()
        );
    }
}