package com.reservateya.api.service.booking;

import com.reservateya.api.controller.request.SlotTimeRequest;
import com.reservateya.api.controller.response.SlotTimeResponse;
import com.reservateya.api.repository.entity.OfferingEntity;
import com.reservateya.api.repository.entity.SlotTimeEntity;
import com.reservateya.api.exception.BusinessErrorCodes;
import com.reservateya.api.exception.BusinessRuleException;
import com.reservateya.api.repository.BookingRepository;
import com.reservateya.api.repository.OfferingRepository;
import com.reservateya.api.repository.SlotTimeRepository;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class SlotTimeServiceImpl implements SlotTimeService {

    private static final Integer MAX_SLOTS_PER_REQUEST = 500;
    private final SlotTimeRepository slotTimeRepository;
    private final ModelMapper modelMapper;
    private final OfferingRepository offeringRepository;
    private final BookingRepository bookingRepository;

    public SlotTimeServiceImpl(SlotTimeRepository slotTimeRepository, ModelMapper modelMapper, OfferingRepository offeringRepository, BookingRepository bookingRepository) {
        this.slotTimeRepository = slotTimeRepository;
        this.modelMapper = modelMapper;
        this.offeringRepository = offeringRepository;
        this.bookingRepository = bookingRepository;
    }

    @Override
    public List<SlotTimeResponse> createList(List<SlotTimeRequest> slotTimeRequestList) {
        if (ObjectUtils.isEmpty(slotTimeRequestList)) {
            throw new IllegalArgumentException("You are trying to create an empty list");
        }

        if (slotTimeRequestList.size() > MAX_SLOTS_PER_REQUEST) {
            throw new IllegalArgumentException("Cantidad de horarios excedida en una sola solicitud. Máximo permitido: " + MAX_SLOTS_PER_REQUEST);
        }

        String offeringId = slotTimeRequestList.get(0).getOfferingId().toString();
        OfferingEntity offeringEntity = offeringRepository.findById(offeringId)
                .orElseThrow(() -> new IllegalArgumentException("Offering id not found"));

        log.info("Creating {} slots for offering id {}", slotTimeRequestList.size(), offeringEntity.getId());

        List<SlotTimeRequest> sorted = slotTimeRequestList.stream()
                .sorted(Comparator.comparing(SlotTimeRequest::getStartDateTime))
                .toList();

        for (int i = 0; i < sorted.size() - 1; i++) {
            SlotTimeRequest current = sorted.get(i);
            SlotTimeRequest next = sorted.get(i + 1);
            if (current.getEndDateTime().isAfter(next.getStartDateTime())) {
                log.error("Creation rejected: slot time overlapped");
                throw new BusinessRuleException(BusinessErrorCodes.SLOT_TIME_OVERLAPPED.name(),
                        Map.of("startDateTime", current.getStartDateTime(), "endDateTime", current.getEndDateTime())
                );
            }
        }

        List<SlotTimeEntity> existingSlots = slotTimeRepository.findByOfferingEntityIdAndEnabledTrue(offeringId);
        for (SlotTimeRequest req : slotTimeRequestList) {
            for (SlotTimeEntity existing : existingSlots) {
                boolean overlaps = req.getStartDateTime().isBefore(existing.getEndDateTime()) &&
                        req.getEndDateTime().isAfter(existing.getStartDateTime());
                if (overlaps) {
                    log.error("Creation rejected: slot time overlapped");
                    throw new BusinessRuleException(BusinessErrorCodes.SLOT_TIME_OVERLAPPED.name(),
                            Map.of("startDateTime", req.getStartDateTime(), "endDateTime", req.getEndDateTime())
                    );
                }
            }
        }

        List<SlotTimeEntity> slotTimeEntities = slotTimeRequestList.stream()
                .map(req -> (SlotTimeEntity) SlotTimeEntity.builder()
                        .offeringEntity(offeringEntity)
                        .startDateTime(req.getStartDateTime())
                        .endDateTime(req.getEndDateTime())
                        .price(req.getPrice())
                        .capacityAvailable(offeringEntity.getCapacity())
                        .maxCapacity(offeringEntity.getCapacity())
                        .enabled(true)
                        .build())
                .toList();

        List<SlotTimeResponse> result = slotTimeRepository.saveAll(slotTimeEntities).stream()
                .map(slot -> modelMapper.map(slot, SlotTimeResponse.class))
                .toList();

        log.info("Slots created successfully for offering id {}", offeringEntity.getId());
        return result;
    }


    @Override
    public Page<SlotTimeResponse> findNextSlotsPageByOfferingId(UUID offeringId, Pageable pageable) {
        return slotTimeRepository.findAllByOfferingEntityIdAndEnabledTrueAndEndDateTimeGreaterThanEqualOrderByStartDateTimeAsc(offeringId.toString(), LocalDateTime.now(), pageable)
                .map(slot -> modelMapper.map(slot, SlotTimeResponse.class));
    }

    @Override
    public SlotTimeResponse update(UUID slotTimeId, SlotTimeRequest slotTimeRequest) {
        SlotTimeEntity slotTimeEntity = slotTimeRepository.findById(slotTimeId.toString())
                .orElseThrow(() -> new IllegalArgumentException("The SlotTimeId: " + slotTimeId + " you are trying to modify is not found"));

        Integer incomingBookings = bookingRepository.getIncomingBookingsCountBySlotId(slotTimeEntity.getId(), LocalDateTime.now());
        if (incomingBookings > 0) {
            log.error("The slot has {} active bookings. Update rejected.", incomingBookings);
            throw new BusinessRuleException(BusinessErrorCodes.OFFERING_HAS_ACTIVE_BOOKINGS.name(), Map.of("count", incomingBookings));
        }

        if (slotTimeRepository.existsOverlappingSlot(slotTimeRequest.getOfferingId().toString(), slotTimeId.toString(),
                slotTimeRequest.getStartDateTime(), slotTimeRequest.getEndDateTime())) {
            log.error("Update rejected: slot time overlapped");
            throw new BusinessRuleException(BusinessErrorCodes.SLOT_TIME_OVERLAPPED.name(),
                    Map.of(
                        "startDateTime", slotTimeRequest.getStartDateTime(),
                        "endDateTime", slotTimeRequest.getEndDateTime()
                    )
            );
        }

        slotTimeEntity.setStartDateTime(slotTimeRequest.getStartDateTime());
        slotTimeEntity.setEndDateTime(slotTimeRequest.getEndDateTime());
        slotTimeEntity.setPrice(slotTimeRequest.getPrice());

        return modelMapper.map(slotTimeRepository.save(slotTimeEntity), SlotTimeResponse.class);
    }

    @Override
    public void delete(UUID slotTimeId) {
        SlotTimeEntity slotTimeEntity = slotTimeRepository.findById(slotTimeId.toString())
                .orElseThrow(() -> new IllegalArgumentException("The SlotTimeId: " + slotTimeId + " you are trying to delete is not found"));

        Integer incomingBookings = bookingRepository.getIncomingBookingsCountBySlotId(slotTimeEntity.getId(), LocalDateTime.now());
        if (incomingBookings > 0) {
            log.error("The slot has {} active bookings. Deletion rejected.", incomingBookings);
            throw new BusinessRuleException(BusinessErrorCodes.OFFERING_HAS_ACTIVE_BOOKINGS.name(), Map.of("count", incomingBookings));
        }

        slotTimeEntity.setEnabled(false);

        slotTimeRepository.save(slotTimeEntity);
    }

}
