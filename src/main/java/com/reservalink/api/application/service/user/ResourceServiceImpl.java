package com.reservalink.api.application.service.user;

import com.reservalink.api.application.output.ResourceRepositoryPort;
import com.reservalink.api.domain.Resource;
import com.reservalink.api.exception.BusinessRuleException;
import com.reservalink.api.adapter.output.repository.BookingRepository;
import com.reservalink.api.adapter.output.repository.ResourceRepositoryAdapter;
import com.reservalink.api.adapter.output.repository.SlotTimeRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static com.reservalink.api.exception.BusinessErrorCodes.RESOURCE_HAS_ACTIVE_BOOKINGS;

@Service
public class ResourceServiceImpl implements ResourceService {

    private final ResourceRepositoryPort resourceRepositoryPort;
    private final BookingRepository bookingRepository;
    private final SlotTimeRepository slotTimeRepository;

    public ResourceServiceImpl(ResourceRepositoryPort resourceRepositoryPort, BookingRepository bookingRepository, SlotTimeRepository slotTimeRepository) {
        this.resourceRepositoryPort = resourceRepositoryPort;
        this.bookingRepository = bookingRepository;
        this.slotTimeRepository = slotTimeRepository;
    }

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