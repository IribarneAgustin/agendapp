package com.reservalink.api.controller;

import com.reservalink.api.controller.request.SlotTimeRequest;
import com.reservalink.api.controller.response.SlotTimeResponse;
import com.reservalink.api.service.booking.SlotTimeService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/slot-time")
public class SlotTimeController {

    private final SlotTimeService slotTimeService;

    public SlotTimeController(SlotTimeService slotTimeService) {
        this.slotTimeService = slotTimeService;
    }

    @PostMapping("/list")
    public ResponseEntity<List<SlotTimeResponse>> createList(@Valid @RequestBody List<SlotTimeRequest> slotTimeRequestList) {
        List<SlotTimeResponse> response = slotTimeService.createList(slotTimeRequestList);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/offering/{offeringId}")
    public ResponseEntity<Page<SlotTimeResponse>> findAllByOfferingId(@NotNull @PathVariable UUID offeringId,
                                                                       @RequestParam(defaultValue = "0") Integer page,
                                                                       @RequestParam(defaultValue = "10") Integer pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);
        Page<SlotTimeResponse> response = slotTimeService.findNextSlotsPageByOfferingId(offeringId, pageable);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{slotTimeId}")
    public ResponseEntity<SlotTimeResponse> updateSlot(@NotNull @PathVariable UUID slotTimeId,
                                                       @Valid @RequestBody SlotTimeRequest slotTimeRequest) {
        log.info("New request to modify the following slot: {}", slotTimeId);
        SlotTimeResponse response = slotTimeService.update(slotTimeId, slotTimeRequest);
        log.info("Slot modified successfully");
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);

    }

    @DeleteMapping("/{slotTimeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSlot(@NotNull @PathVariable UUID slotTimeId) {
        log.info("New request to delete the following slot: {}", slotTimeId);
        slotTimeService.delete(slotTimeId);
        log.info("Slot deleted successfully");
    }
}
