package com.reservalink.api.service.user;

import com.reservalink.api.domain.Resource;
import com.reservalink.api.exception.BusinessRuleException;
import com.reservalink.api.repository.BookingRepository;
import com.reservalink.api.repository.ResourceRepository;
import com.reservalink.api.repository.SlotTimeRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static com.reservalink.api.exception.BusinessErrorCodes.RESOURCE_HAS_ACTIVE_BOOKINGS;

@Service
public class ResourceServiceImpl implements ResourceService {

    private final ResourceRepository resourceRepository;
    private final BookingRepository bookingRepository;
    private final SlotTimeRepository slotTimeRepository;

    public ResourceServiceImpl(ResourceRepository resourceRepository, BookingRepository bookingRepository, SlotTimeRepository slotTimeRepository) {
        this.resourceRepository = resourceRepository;
        this.bookingRepository = bookingRepository;
        this.slotTimeRepository = slotTimeRepository;
    }

    @Override
    public List<Resource> findAllByUserId(String userId) {
        return resourceRepository.findAllByUserId(userId);
    }

    @Override
    public List<Resource> findAllByUserIdAndOfferingId(String userId, String offeringId) {
        return resourceRepository.findAllByUserIdAndOfferingId(userId, offeringId);
    }

    @Override
    public Resource create(Resource resource) {
        return resourceRepository.create(resource);
    }

    @Override
    public Resource update(Resource resource) {
        return resourceRepository.update(resource);
    }

    @Override
    public void delete(String resourceId) {
        Resource resource = resourceRepository.findResourceDomainById(resourceId);
        List<Resource> resourceList = resourceRepository.findAllByUserId(resource.getUserId());
        if (resourceList.size() == 1) {
            throw new RuntimeException("The user should has at least 1 resource");
        }

        boolean hasIncomingBookings = bookingRepository.getIncomingBookingsCountByResourceId(resourceId, LocalDateTime.now()) > 0;
        if (hasIncomingBookings) {
            throw new BusinessRuleException(RESOURCE_HAS_ACTIVE_BOOKINGS.name());
        }

        slotTimeRepository.deleteByResourceId(resourceId);
        resourceRepository.delete(resourceId);
    }
}