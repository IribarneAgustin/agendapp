package com.reservateya.api.service.offering;

import com.reservateya.api.controller.request.OfferingRequest;
import com.reservateya.api.controller.response.OfferingResponse;
import com.reservateya.api.repository.entity.OfferingEntity;
import com.reservateya.api.repository.entity.SlotTimeEntity;
import com.reservateya.api.exception.BusinessErrorCodes;
import com.reservateya.api.exception.BusinessRuleException;
import com.reservateya.api.repository.BookingRepository;
import com.reservateya.api.repository.OfferingRepository;
import com.reservateya.api.repository.SlotTimeRepository;
import com.reservateya.api.service.booking.SlotTimeService;
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
        OfferingEntity offeringEntity = modelMapper.map(offeringRequest, OfferingEntity.class);
        offeringEntity.setEnabled(true);
        return modelMapper.map(offeringRepository.save(offeringEntity), OfferingResponse.class);
    }

    @Override
    public OfferingResponse update(OfferingRequest offeringRequest) {
        if (offeringRequest.getId() == null) {
            throw new IllegalArgumentException("Invalid offering id");
        }
        OfferingEntity existing = offeringRepository.findById(offeringRequest.getId().toString())
                .orElseThrow(() -> new IllegalArgumentException("Offering not found with id: " + offeringRequest.getId()));

        modelMapper.map(offeringRequest, existing);

        OfferingEntity updated = offeringRepository.save(existing);

        return modelMapper.map(updated, OfferingResponse.class);
    }

    @Override
    public List<OfferingResponse> findAllByUserId(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("Invalid user id");
        }
        List<OfferingEntity> offeringEntities = offeringRepository.findByUserEntityIdAndEnabledTrue(userId.toString());
        return offeringEntities.stream()
                .map(off -> modelMapper.map(off, OfferingResponse.class))
                .collect(Collectors.toList());
    }

    @Override
    public void delete(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("Invalid offering id");
        }
        OfferingEntity existing = offeringRepository.findById(id.toString())
                .orElseThrow(() -> new IllegalArgumentException("Offering not found with id: " + id));

        Integer incomingBookings = bookingRepository.getIncomingBookingsCount(existing.getId(), LocalDateTime.now());
        if (incomingBookings > 0) {
            log.error("The offering has {} active bookings. Deletion rejected.", incomingBookings);
            throw new BusinessRuleException(BusinessErrorCodes.OFFERING_HAS_ACTIVE_BOOKINGS.name(), Map.of("count", incomingBookings));
        }

        existing.setEnabled(false);

        List<SlotTimeEntity> slotTimeEntityList = slotTimeRepository.findByOfferingEntityIdAndEnabledTrue(id.toString());
        if (!ObjectUtils.isEmpty(slotTimeEntityList)) {
            log.info("Disabling {} active slots for the service {}", slotTimeEntityList.size(), id);
            slotTimeEntityList.forEach(slotTime -> {
                slotTime.setEnabled(false);
            });
            slotTimeRepository.saveAll(slotTimeEntityList);
        }

        offeringRepository.save(existing);
    }

}
