package com.reservalink.api.application.service.offering;

import com.reservalink.api.adapter.input.controller.request.OfferingRequest;
import com.reservalink.api.adapter.input.controller.response.OfferingResponse;
import com.reservalink.api.exception.BusinessErrorCodes;
import com.reservalink.api.exception.BusinessRuleException;
import com.reservalink.api.adapter.output.repository.BookingRepository;
import com.reservalink.api.adapter.output.repository.OfferingRepository;
import com.reservalink.api.adapter.output.repository.SlotTimeRepository;
import com.reservalink.api.adapter.output.repository.UserRepository;
import com.reservalink.api.adapter.output.repository.entity.OfferingEntity;
import com.reservalink.api.adapter.output.repository.entity.SlotTimeEntity;
import com.reservalink.api.adapter.output.repository.entity.UserEntity;
import com.reservalink.api.application.service.packageSession.PackageSessionService;
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
    private final UserRepository userRepository;
    private final PackageSessionService packageSessionService;

    public OfferingServiceImpl(OfferingRepository offeringRepository, ModelMapper modelMapper,
            SlotTimeRepository slotTimeRepository, BookingRepository bookingRepository, UserRepository userRepository,
            PackageSessionService packageSessionService) {
        this.offeringRepository = offeringRepository;
        this.modelMapper = modelMapper;
        this.slotTimeRepository = slotTimeRepository;
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.packageSessionService = packageSessionService;
    }

    @Override
    public OfferingResponse create(OfferingRequest offeringRequest) {
        OfferingEntity offeringEntity = modelMapper.map(offeringRequest, OfferingEntity.class);
        UserEntity user = userRepository.findById(offeringRequest.getUserId().toString())
                .orElseThrow(
                        () -> new IllegalArgumentException("User not found with id: " + offeringRequest.getUserId()));
        offeringEntity.setEnabled(true);
        offeringEntity.setUserEntity(user);
        OfferingResponse response = modelMapper.map(offeringRepository.save(offeringEntity), OfferingResponse.class);
        if (offeringRequest.getSessionLimit() != null && offeringRequest.getPackagePrice() != null) {
            packageSessionService.createPackageSessionTemplate(offeringRequest, response);
        }
        return response;
    }

    @Override
    public OfferingResponse update(OfferingRequest offeringRequest) {
        if (offeringRequest.getId() == null) {
            throw new IllegalArgumentException("Invalid offering id");
        }
        OfferingEntity existing = offeringRepository.findById(offeringRequest.getId().toString())
                .orElseThrow(
                        () -> new IllegalArgumentException("Offering not found with id: " + offeringRequest.getId()));

        modelMapper.map(offeringRequest, existing);

        OfferingEntity updated = offeringRepository.save(existing);

        OfferingResponse response = modelMapper.map(updated, OfferingResponse.class);
        if (offeringRequest.getSessionLimit() != null && offeringRequest.getPackagePrice() != null) {
            packageSessionService.createPackageSessionTemplate(offeringRequest, response);
        }
        return response;
    }

    @Override
    public List<OfferingResponse> findAllByUserId(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("Invalid user id");
        }
        List<OfferingEntity> offeringEntities = offeringRepository.findByUserEntityIdAndEnabledTrue(userId.toString());
        List<OfferingResponse> responses = offeringEntities.stream()
                .map(off -> modelMapper.map(off, OfferingResponse.class))
                .collect(Collectors.toList());
        responses.forEach(packageSessionService::enrichOfferingResponse);
        return responses;
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
            throw new BusinessRuleException(BusinessErrorCodes.OFFERING_HAS_ACTIVE_BOOKINGS.name(),
                    Map.of("count", incomingBookings));
        }

        existing.setEnabled(false);

        List<SlotTimeEntity> slotTimeEntityList = slotTimeRepository
                .findByOfferingEntityIdAndEnabledTrue(id.toString());
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
