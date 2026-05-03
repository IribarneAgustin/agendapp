package com.reservalink.api.application.service.user;

import com.reservalink.api.adapter.output.repository.BookingRepository;
import com.reservalink.api.adapter.output.repository.SlotTimeRepository;
import com.reservalink.api.application.output.ResourceRepositoryPort;
import com.reservalink.api.application.output.UserRepositoryPort;
import com.reservalink.api.application.service.subscription.SubscriptionUsageService;
import com.reservalink.api.domain.Resource;
import com.reservalink.api.domain.Subscription;
import com.reservalink.api.domain.enums.FeatureName;
import com.reservalink.api.exception.BusinessErrorCodes;
import com.reservalink.api.exception.BusinessRuleException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static com.reservalink.api.exception.BusinessErrorCodes.RESOURCE_HAS_ACTIVE_BOOKINGS;

@Service
@RequiredArgsConstructor
public class ResourceServiceImpl implements ResourceService {

    private final ResourceRepositoryPort resourceRepositoryPort;
    private final BookingRepository bookingRepository;
    private final SlotTimeRepository slotTimeRepository;
    private final SubscriptionUsageService subscriptionUsageService;
    private final UserRepositoryPort userRepositoryPort;

    @Override
    public List<Resource> findAllByUserId(String userId) {
        return resourceRepositoryPort.findAllByUserId(userId);
    }

    @Override
    public List<Resource> findAllByUserIdAndOfferingId(String userId, String offeringId) {
        return resourceRepositoryPort.findAllByUserIdAndOfferingId(userId, offeringId);
    }

    @Override
    public Resource create(Resource resource) {
        Subscription subscription = userRepositoryPort.findUserSubscriptionByUserId(resource.getUserId()).orElseThrow(EntityNotFoundException::new);
        if (!subscriptionUsageService.canConsume(subscription.getId(), subscription.getSubscriptionPlanId(), FeatureName.RESOURCES)) {
            throw new BusinessRuleException(BusinessErrorCodes.FEATURE_LIMIT_EXCEEDED.name());
        }
        return resourceRepositoryPort.create(resource);
    }

    @Override
    public Resource update(Resource resource) {
        return resourceRepositoryPort.update(resource);
    }

    @Override
    public void delete(String resourceId) {
        Resource resource = resourceRepositoryPort.findResourceDomainById(resourceId);
        List<Resource> resourceList = resourceRepositoryPort.findAllByUserId(resource.getUserId());
        if (resourceList.size() == 1) {
            throw new RuntimeException("The user should has at least 1 resource");
        }

        boolean hasIncomingBookings = bookingRepository.getIncomingBookingsCountByResourceId(resourceId, LocalDateTime.now()) > 0;
        if (hasIncomingBookings) {
            throw new BusinessRuleException(RESOURCE_HAS_ACTIVE_BOOKINGS.name());
        }

        slotTimeRepository.deleteByResourceId(resourceId);
        resourceRepositoryPort.delete(resourceId);
    }
}