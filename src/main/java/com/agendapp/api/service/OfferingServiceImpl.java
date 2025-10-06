package com.agendapp.api.service;

import com.agendapp.api.controller.request.OfferingRequest;
import com.agendapp.api.controller.response.OfferingResponse;
import com.agendapp.api.entity.Offering;
import com.agendapp.api.entity.SlotTime;
import com.agendapp.api.exception.BusinessErrorCodes;
import com.agendapp.api.exception.BusinessRuleException;
import com.agendapp.api.repository.BookingRepository;
import com.agendapp.api.repository.OfferingRepository;
import com.agendapp.api.repository.SlotTimeRepository;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OfferingServiceImpl implements OfferingService {

    private final OfferingRepository offeringRepository;
    private final ModelMapper modelMapper;
    private final SlotTimeRepository slotTimeRepository;
    private final BookingRepository bookingRepository;

    public OfferingServiceImpl(OfferingRepository offeringRepository, ModelMapper modelMapper, SlotTimeService slotTimeService, SlotTimeRepository slotTimeRepository, BookingRepository bookingRepository) {
        this.offeringRepository = offeringRepository;
        this.modelMapper = modelMapper;
        this.slotTimeRepository = slotTimeRepository;
        this.bookingRepository = bookingRepository;
    }

    @Override
    public OfferingResponse create(OfferingRequest offeringRequest) {
        Offering offering = modelMapper.map(offeringRequest, Offering.class);
        offering.setActive(true);
        return modelMapper.map(offeringRepository.save(offering), OfferingResponse.class);
    }

    @Override
    public OfferingResponse update(OfferingRequest offeringRequest) {
        if (offeringRequest.getId() == null) {
            throw new IllegalArgumentException("Invalid offering id");
        }
        Offering existing = offeringRepository.findById(offeringRequest.getId().toString())
                .orElseThrow(() -> new IllegalArgumentException("Offering not found with id: " + offeringRequest.getId()));

        modelMapper.map(offeringRequest, existing);

        Offering updated = offeringRepository.save(existing);

        return modelMapper.map(updated, OfferingResponse.class);
    }

    @Override
    public List<OfferingResponse> findAllByUserId(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("Invalid user id");
        }
        List<Offering> offerings = offeringRepository.findByUserIdAndActiveTrue(userId.toString());
        return offerings.stream()
                .map(off -> modelMapper.map(off, OfferingResponse.class))
                .collect(Collectors.toList());
    }

    @Override
    public void delete(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("Invalid offering id");
        }
        Offering existing = offeringRepository.findById(id.toString())
                .orElseThrow(() -> new IllegalArgumentException("Offering not found with id: " + id));

        Integer incomingBookings = bookingRepository.getIncomingBookingsCount(existing.getId(), LocalDateTime.now());
        if (incomingBookings > 0) {
            log.error("The offering has {} active bookings. Deletion rejected.", incomingBookings);
            throw new BusinessRuleException(BusinessErrorCodes.OFFERING_HAS_ACTIVE_BOOKINGS.name(), Map.of("count", incomingBookings));
        }

        existing.setActive(false);

        List<SlotTime> slotTimeList = slotTimeRepository.findByOfferingIdAndActiveTrue(id.toString());
        if (!ObjectUtils.isEmpty(slotTimeList)) {
            log.info("Disabling {} active slots for the service {}", slotTimeList.size(), id);
            slotTimeList.forEach(slotTime -> {
                slotTime.setActive(false);
            });
            slotTimeRepository.saveAll(slotTimeList);
        }

        offeringRepository.save(existing);
    }

}
