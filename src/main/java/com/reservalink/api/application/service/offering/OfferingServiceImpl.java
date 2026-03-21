package com.reservalink.api.application.service.offering;

import com.reservalink.api.adapter.input.controller.request.OfferingRequest;
import com.reservalink.api.adapter.input.controller.response.OfferingResponse;
import com.reservalink.api.adapter.output.repository.BookingRepository;
import com.reservalink.api.adapter.output.repository.SlotTimeRepository;
import com.reservalink.api.adapter.output.repository.entity.SlotTimeEntity;
import com.reservalink.api.application.output.OfferingCategoryServiceRepositoryPort;
import com.reservalink.api.application.output.OfferingRepositoryPort;
import com.reservalink.api.application.output.SlotTimeRepositoryPort;
import com.reservalink.api.domain.Offering;
import com.reservalink.api.domain.OfferingCategory;
import com.reservalink.api.domain.SlotTime;
import com.reservalink.api.exception.BusinessErrorCodes;
import com.reservalink.api.exception.BusinessRuleException;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class OfferingServiceImpl implements OfferingService {

    private final OfferingRepositoryPort offeringRepository;
    private final OfferingCategoryServiceRepositoryPort categoryRepositoryPort;
    private final BookingRepository bookingRepository;
    private final SlotTimeRepositoryPort slotTimeRepository;
    private final ModelMapper modelMapper;

    @Override
    public OfferingResponse create(OfferingRequest request) {
        Offering offering = modelMapper.map(request, Offering.class);
        offering.setEnabled(true);

        String categoryId = resolveCategoryId(request);
        offering.setCategoryId(categoryId);

        Offering saved = offeringRepository.save(offering);
        return modelMapper.map(saved, OfferingResponse.class);
    }

    @Override
    public OfferingResponse update(OfferingRequest request) {
        if (request.getId() == null) {
            throw new IllegalArgumentException("Invalid offering id");
        }
        Offering existing = offeringRepository.findById(request.getId().toString())
                .orElseThrow(() -> new IllegalArgumentException("Offering not found"));
        modelMapper.map(request, existing);

        String categoryId = resolveCategoryId(request);
        existing.setCategoryId(categoryId);

        Offering updated = offeringRepository.save(existing);

        return modelMapper.map(updated, OfferingResponse.class);
    }

    @Override
    public List<OfferingResponse> findAllByUserId(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("Invalid user id");
        }
        return offeringRepository.findByUserId(userId.toString())
                .stream()
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

        existing.setEnabled(false);

        List<SlotTime> slots = slotTimeRepository.findByOfferingEntityIdAndEnabledTrue(id.toString());
        if (!ObjectUtils.isEmpty(slots)) {
            log.info("Disabling {} active slots for the service {}", slots.size(), id);
            slots.forEach(slot -> slot.setEnabled(false));
            slotTimeRepository.saveAll(slots);
        }

        offeringRepository.save(existing);
    }

    private String resolveCategoryId(OfferingRequest request) {
        if (request.getCategoryId() != null) {
            return request.getCategoryId().toString();
        }

        OfferingCategory defaultCategory = categoryRepositoryPort.findByUserIdAndIsDefault(request.getUserId().toString())
                .orElseThrow(() -> new IllegalArgumentException("Default category not found"));

        return defaultCategory.getId();
    }
}