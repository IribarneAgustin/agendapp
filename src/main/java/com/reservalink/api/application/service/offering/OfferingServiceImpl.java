package com.reservalink.api.application.service.offering;

import com.reservalink.api.adapter.input.controller.request.OfferingOrderRequest;
import com.reservalink.api.adapter.input.controller.request.OfferingRequest;
import com.reservalink.api.adapter.input.controller.response.OfferingResponse;
import com.reservalink.api.adapter.output.repository.BookingRepository;
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
import java.util.Set;
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
        existing.setDisplayOrder(0);

        List<SlotTime> slots = slotTimeRepository.findByOfferingEntityIdAndEnabledTrue(id.toString());
        if (!ObjectUtils.isEmpty(slots)) {
            log.info("Disabling {} active slots for the service {}", slots.size(), id);
            slots.forEach(slot -> slot.setEnabled(false));
            slotTimeRepository.saveAll(slots);
        }

        offeringRepository.save(existing);
    }

    @Override
    public void orderOfferings(UUID userId, List<OfferingOrderRequest> request) {
        if (request == null || request.isEmpty()) {
            throw new IllegalArgumentException("Request must not be null or empty");
        }

        Set<Integer> uniqueOrders = request.stream()
                .map(OfferingOrderRequest::order)
                .collect(Collectors.toSet());

        if (uniqueOrders.size() != request.size()) {
            throw new IllegalArgumentException("Duplicate order values");
        }

        List<Offering> offeringList = offeringRepository.findByUserId(userId.toString());

        if (request.size() != offeringList.size()) {
            throw new IllegalArgumentException("All offerings must be included in reorder request");
        }

        Map<String, Integer> orderMap = request.stream()
                .collect(Collectors.toMap(
                        OfferingOrderRequest::offeringId,
                        OfferingOrderRequest::order
                ));

        offeringList.forEach(offering -> {
            Integer newOrder = orderMap.get(offering.getId());
            if (newOrder != null) {
                offering.setDisplayOrder(newOrder);
            }
        });

        offeringRepository.saveAll(offeringList);
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