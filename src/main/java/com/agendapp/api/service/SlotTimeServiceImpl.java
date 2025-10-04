package com.agendapp.api.service;

import com.agendapp.api.controller.request.SlotTimeRequest;
import com.agendapp.api.controller.response.SlotTimeResponse;
import com.agendapp.api.entity.Offering;
import com.agendapp.api.entity.SlotTime;
import com.agendapp.api.repository.OfferingRepository;
import com.agendapp.api.repository.SlotTimeRepository;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class SlotTimeServiceImpl implements SlotTimeService {

    private static final Integer MAX_SLOTS_PER_REQUEST = 500;
    private final SlotTimeRepository slotTimeRepository;
    private final ModelMapper modelMapper;
    private final OfferingRepository offeringRepository;

    public SlotTimeServiceImpl(SlotTimeRepository slotTimeRepository, ModelMapper modelMapper, OfferingRepository offeringRepository) {
        this.slotTimeRepository = slotTimeRepository;
        this.modelMapper = modelMapper;
        this.offeringRepository = offeringRepository;
    }

    @Override
    public List<SlotTimeResponse> createList(List<SlotTimeRequest> slotTimeRequestList) {
        if (ObjectUtils.isEmpty(slotTimeRequestList)) {
           throw new IllegalArgumentException("You are trying to create an empty list");
        }

        if (slotTimeRequestList.size() > MAX_SLOTS_PER_REQUEST) {
            throw new IllegalArgumentException("Cantidad de horarios excedida en una sola solicitud. Máximo permitido: " + MAX_SLOTS_PER_REQUEST);
        }

        Offering offering = offeringRepository.findById(slotTimeRequestList.get(0).getOfferingId().toString()).orElseThrow(
                () -> new IllegalArgumentException("Offering id not found")
        );
        log.info("Creating {} slots for offering id {}", slotTimeRequestList.size(), offering.getId());

        //We avoid Model Mapper here because it set Offering id as slotTime Id
        List<SlotTime> slotTimes = slotTimeRequestList.stream()
                .map(req -> SlotTime.builder()
                        .offering(offering)
                        .startDateTime(req.getStartDateTime())
                        .endDateTime(req.getEndDateTime())
                        .price(req.getPrice())
                        .active(true)
                        .build())
                .toList();

        List<SlotTimeResponse> result = slotTimeRepository.saveAll(slotTimes).stream()
                .map(slot -> modelMapper.map(slot, SlotTimeResponse.class))
                .toList();

        log.info("Slots created successfully");

        return result;
    }

    @Override
    public Page<SlotTimeResponse> findNextSlotsPageByOfferingId(UUID offeringId, Pageable pageable) {
        return slotTimeRepository.findAllByOfferingIdAndActiveTrueAndEndDateTimeGreaterThanEqualOrderByStartDateTimeAsc(offeringId.toString(), LocalDateTime.now(), pageable)
                .map(slot -> modelMapper.map(slot, SlotTimeResponse.class));
    }

    @Override
    public SlotTimeResponse update(UUID slotTimeId, SlotTimeRequest slotTimeRequest) {
        SlotTime slotTime = slotTimeRepository.findById(slotTimeId.toString())
                .orElseThrow(() -> new IllegalArgumentException("The SlotTimeId: " + slotTimeId + " you are trying to modify is not found"));

        slotTime.setStartDateTime(slotTimeRequest.getStartDateTime());
        slotTime.setEndDateTime(slotTimeRequest.getEndDateTime());
        slotTime.setPrice(slotTimeRequest.getPrice());

        return modelMapper.map(slotTimeRepository.save(slotTime), SlotTimeResponse.class);
    }

    @Override
    public void delete(UUID slotTimeId) {
        SlotTime slotTime = slotTimeRepository.findById(slotTimeId.toString())
                .orElseThrow(() -> new IllegalArgumentException("The SlotTimeId: " + slotTimeId + " you are trying to delete is not found"));
        slotTime.setActive(false);
        slotTimeRepository.save(slotTime);
    }

}
